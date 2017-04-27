#!/bin/bash
cd `dirname "$0"`/../..
INAS_SCRIPT_ROOT=`pwd`
. $INAS_SCRIPT_ROOT/bin/setenv.sh

###set -x
CONF_DIR=$INAS_SCRIPT_ROOT/conf
PROCESS_TMP_DIR=$INAS_SCRIPT_ROOT/data/temp
PROCESS_LOG_DIR=${INAS_SCRIPT_ROOT}/tmp/log

MQ_SERVER_IP=10.221.247.50
MQ_SERVER_PORT=61613
MQ_USER=admin
MQ_PASSWORD=admin
###MQ_QUEUE_NAME=Q_EVENT_ESB_NOTIFY Q_EVENT_GROUP_REPORT

###PHONES="13916354958,13402076518"
PHONES="13402076518"
#  parameter  list ( msg_content )
sendNotify()
{
    MSG_BODY=$1
    MQ_QUEUE_NAME=$2
    sendMqNotify $MQ_SERVER_IP $MQ_SERVER_PORT $MQ_USER $MQ_PASSWORD "$MQ_QUEUE_NAME" "$MSG_BODY"
}

CATALOG_ID=$1
SOURCE_TABLE=$2
EXPORT_TYPE=$3
EXPORT_TIME=$4
EXPORT_PATH=$5
FILE_PREFIX=$6
TABLE_PREFIX=$(echo "$1" | sed 's/:/_/g;s/\./_/g')
FTP_SERVER=$7
TIME_FORMAT=$8
SERVICE_CODE=$9

PARTITION_TYPE=
PARTITION_VALUE=

EXIT_CODE=1


put_to_nfs_dir()
{
          FILE_NAME=$1
          NFS_DIR=$2
          mv $PROCESS_TMP_DIR/$FILE_NAME  $NFS_DIR/${FILE_NAME}.tmp
          mv $NFS_DIR/${FILE_NAME}.tmp $NFS_DIR/${FILE_NAME}
          #rm $PROCESS_TMP_DIR/$FILE_NAME

          return 0
}

put_to_ftp_dir()
{
          local FILE_NAME=$1
          local FTP_DIR=$2
          local FTP_SERVER=$3
    local SERVICE_CODE=$4

    local RET_CODE=0

          cd $PROCESS_TMP_DIR
          gzip -1 $PROCESS_TMP_DIR/${FILE_NAME}
          mv $PROCESS_TMP_DIR/${FILE_NAME}.gz $PROCESS_TMP_DIR/${FILE_NAME}.gz.tmp
          ls -l $PROCESS_TMP_DIR/${FILE_NAME}.gz.tmp
          $(ftp_upload $FTP_DIR  $PROCESS_TMP_DIR  ${FILE_NAME}.gz.tmp  ${FILE_NAME}.gz $FTP_SERVER $SERVICE_CODE)

          RET_CODE=$?

          rm $PROCESS_TMP_DIR/${FILE_NAME}.gz.tmp
          cd -
          return        $RET_CODE
}

ftp_upload()
{
  local REMOTE_PATH=$1
  local LOCAL_PATH=$2
  local TEMP_FILE_NAME=$3
  local FILE_NAME=$4
  local FTP_SERVER=$5
  local SERVICE_CODE=$6

  if [ -n "$FTP_SERVER" ]; then
    local FTP_USER=$(readConfigValue $CONF_DIR/ftpuser.conf $FTP_SERVER FTP_USER)
    local FTP_PASSWORD=$(readConfigValue $CONF_DIR/ftpuser.conf $FTP_SERVER FTP_PASSWORD)
    local FTP_PATH_PREFIX=$(readConfigValue $CONF_DIR/ftpuser.conf $FTP_SERVER FTP_PATH_PREFIX)
    local FTP_LOG_SEND=$(readConfigValue $CONF_DIR/ftpuser.conf $FTP_SERVER FTP_LOG_SEND)
    local MQ_QUEUE_NAME=$(readConfigValue $CONF_DIR/ftpuser.conf $FTP_SERVER MQ_QUEUE_NAME)
  fi

  curl -s -u ${FTP_USER}:${FTP_PASSWORD} -T ${LOCAL_PATH}/${TEMP_FILE_NAME} ftp://${FTP_SERVER}${REMOTE_PATH}/ > /dev/null 2>&1
  if [[ $? -gt 0 ]];then
    sendDqSyslog DQ_DATA_EXPORT "10.221.246.86 FTP failed, May not be enough space."
    ###sendSms $PHONES "10.221.246.84 FTP failed, May not be enough space."
        return $?
  fi

  curl -u ${FTP_USER}:${FTP_PASSWORD} ftp://${FTP_SERVER} -Q "RNFR ${FTP_PATH_PREFIX}/${REMOTE_PATH}/${TEMP_FILE_NAME}"  -Q "RNTO ${FTP_PATH_PREFIX}/${REMOTE_PATH}/${FILE_NAME}" > /dev/null 2>&1
  if [[ $? -gt 0 ]];then
        return $?
  fi

  if [ "$FTP_LOG_SEND" == "86ESB" ]; then
    local PATH_2=${REMOTE_PATH#*/}
    local PATH='/'${PATH_2#*/}
    MSG_BODY="{ \"service_code\" : \"${SERVICE_CODE}\", \"ftp_url\" : \"ftp://${FTP_USER}:${FTP_PASSWORD}@${FTP_SERVER}\", \"files\" : [ { \"path\" : \"${PATH}/${FILE_NAME}\" } ] }"
    sendNotify "$MSG_BODY" "$MQ_QUEUE_NAME"
    if [[ $? -gt 0 ]];then
          log_message  $CUR_LOG_FILE  " Table $CATALOG_ID:$SOURCE_TABLE 94, Send MQ Message Failure !"
          return 94
    fi
  fi
  if [ "$FTP_LOG_SEND" == "51HQ" ] ;then
    MSG_BODY="ftp://${FTP_USER}:${FTP_PASSWORD}@${FTP_SERVER}/${REMOTE_PATH}/${FILE_NAME}"
    sendNotify "$MSG_BODY" "$MQ_QUEUE_NAME"
  fi

  return 0
}

check_export_data_ready()
{
        local EXPORT_TABLE_NAME=$1
        local PARTITION_TYPE=$2
        local PARTITION_VALUE=$3

        impala-shell -q "refresh ext_data.${EXPORT_TABLE_NAME};"
        local ROW=$(impala-shell -B -q "select count(*) from  ext_data.${EXPORT_TABLE_NAME} where ${PARTITION_TYPE}=${PARTITION_VALUE} ;")

        if [[ -z $ROW  || $ROW -eq 0  ]];then
                return 1
        else
                return 0
        fi
}


#  parameter  list ( table_name  )
find_template_file()
{
        local TABLE_NAME=$1
        echo $(find  $TEMPLATE_DIR  -regex ".*/${TABLE_NAME}.tpl" -print | head -1)
}

FILE_TIME=$(echo $EXPORT_TIME | sed 's/://g;s/ //g;s/-//g')

HOUR_PARTITION=$(echo $FILE_TIME | awk '{print substr($0,1,12)}')
DAY_PARTITION=$(echo $FILE_TIME | awk '{print substr($0,1,8)}')0000
MONTH_PARTITION=$(echo $FILE_TIME | awk '{print substr($0,1,6)}')000000

if [ "$TIME_FORMAT" == "YYYYMMDD" ] ;then
  FILE_TIME=$(echo $FILE_TIME | awk '{print substr($0,1,8)}')
elif [ "$TIME_FORMAT" == "YYYYMMDDHH24MI" ] ;then
  FILE_TIME=$(echo $FILE_TIME | awk '{print substr($0,1,12)}')
fi

EXPORT_TABLE_NAME="exp_${TABLE_PREFIX}"

if [[ "$SOURCE_TABLE" =~ _h$ ]]; then
  HDFS_EXPORT_PATH="/user/hive/warehouse/ext_data.db/${EXPORT_TABLE_NAME}/hour_partition=${HOUR_PARTITION}"
  PARTITION_TYPE=hour_partition
  PARTITION_VALUE=$HOUR_PARTITION
elif [[ "$SOURCE_TABLE" =~ _d$ ]]; then
  HDFS_EXPORT_PATH="/user/hive/warehouse/ext_data.db/${EXPORT_TABLE_NAME}/day_partition=${DAY_PARTITION}"
  PARTITION_TYPE=day_partition
  PARTITION_VALUE=$DAY_PARTITION
elif [[ "$SOURCE_TABLE" =~ _m$ ]]; then
  HDFS_EXPORT_PATH="/user/hive/warehouse/ext_data.db/${EXPORT_TABLE_NAME}/month_partition=${MONTH_PARTITION}"
  PARTITION_TYPE=month_partition
  PARTITION_VALUE=$MONTH_PARTITION
else
        echo "Invalid export source table name, table name must be ended with _d , _m , _h "
        exit 2;
fi

PUBDAY=$(echo $EXPORT_TIME | awk '{print substr($0,1,10)}' )
CUR_LOG_FILE=$INAS_SCRIPT_ROOT/logs/export/export_${PUBDAY}.log


TEMPLATE_DIR=$INAS_SCRIPT_ROOT/template/export

###TEMPLATE_FILE=${TEMPLATE_DIR}/${TABLE_PREFIX}.tpl
TEMPLATE_FILE=$(find_template_file $TABLE_PREFIX)

EXPORT_FILE_NAME=${FILE_PREFIX}_${FILE_TIME}.csv

if [ ! -e "$TEMPLATE_FILE" ] ; then
     log_message  $CUR_LOG_FILE  "Template file $TEMPLATE_FILE  not found !"
     echo "Template file $TEMPLATE_FILE  not found !"
     exit 2
fi

TMP_DIR=$INAS_SCRIPT_ROOT/tmp
EXEC_FILE=${TMP_DIR}/EXP_${TABLE_PREFIX}_${FILE_TIME}.sql

sed_arg="s/#{HOUR_PARTITION}/${HOUR_PARTITION}/ig;s/#{DAY_PARTITION}/${DAY_PARTITION}/ig;s/#{MONTH_PARTITION}/${MONTH_PARTITION}/ig"
sed "$sed_arg" $TEMPLATE_FILE > $EXEC_FILE


main_process()
{
  ###impala-shell -q "alter table ext_data.exp_${SOURCE_TABLE} set tblproperties('serialization.null.format'='') ;"
  ####impala-shell -q "refresh ext_data.${EXPORT_TABLE_NAME};"

  if [ "$CATALOG_ID" != "o.se.ur.volte.timeseg" ] ;then
  impala-shell -i 10.11.58.21 -q "refresh ipms.${SOURCE_TABLE};"
  else
  impala-shell -i 10.11.58.21 -q "refresh xdr_data.o_se_ur_volte_timeseg;"
  fi

  impala-shell -i 10.11.58.21 -f $EXEC_FILE >> $PROCESS_LOG_DIR/$EXPORT_TABLE_NAME.log 2>&1
  if [ $? -gt 0 ] ; then
                EXIT_CODE=93
  else
    sleep 120
    ###check_export_data_ready $EXPORT_TABLE_NAME $PARTITION_TYPE $PARTITION_VALUE

    ###EXPORT_FILE_LIST=$(hdfs dfs -ls "$HDFS_EXPORT_PATH" | grep -v ^d | grep -v ^Found   | awk '{print $8}')
    ###EXPORT_FILE_LIST=$(hdfs dfs -ls -R "$HDFS_EXPORT_PATH" | grep -v ^d| grep -v ^Found | grep -v HIVE_DEFAULT_PARTITION | grep data.0.$ |awk '{print $8}')
    EXPORT_FILE_LIST=$(hdfs dfs -ls -R "$HDFS_EXPORT_PATH" | grep -v ^d| grep -v ^Found | grep data.0.$ |awk '{print $8}')
    if [[ -n "$EXPORT_FILE_LIST" ]]; then

        if [ "$FTP_SERVER" == "10.221.247.51" ] ;then
                 echo $(grep $SOURCE_TABLE $CONF_DIR/hq.conf | awk -F '=' '{print $2}') >> ${PROCESS_TMP_DIR}/${EXPORT_FILE_NAME}
        fi

        for FILE in $EXPORT_FILE_LIST; do
                        ####if [ "$FTP_SERVER" == "10.221.247.51" -o "$EXPORT_TYPE" == "nfs" ] ;then
            hdfs dfs -cat $FILE   >> ${PROCESS_TMP_DIR}/${EXPORT_FILE_NAME}
          ####else
                        ####  hdfs dfs -cat $FILE | iconv -c -f utf-8 -t gbk - >> ${PROCESS_TMP_DIR}/${EXPORT_FILE_NAME}
                        ####fi
        done

        if [[ "$EXPORT_TYPE" == "nfs" ]];then
                put_to_nfs_dir $EXPORT_FILE_NAME   $EXPORT_PATH
                EXIT_CODE=$?
        elif [[ "$EXPORT_TYPE" == "ftp" ]];then
          put_to_ftp_dir $EXPORT_FILE_NAME $EXPORT_PATH $FTP_SERVER $SERVICE_CODE
          EXIT_CODE=$?
        else
                log_message  " Invalid export type  [  $EXPORT_TYPE   ] for  $CATALOG_ID  .  "
                EXIT_CODE=91
        fi

        ###impala-shell -q "alter table ext_data.${EXPORT_TABLE_NAME} drop partition(${PARTITION_TYPE}=cast(${PARTITION_VALUE} as decimal(12)));"
      sudo -u impala hive -e "alter table ext_data.${EXPORT_TABLE_NAME} drop partition(${PARTITION_TYPE}=${PARTITION_VALUE});"
    else
      if [[ "$CATALOG_ID" = "dw.ft.se.h.lte_cl_52_ho4" || "$CATALOG_ID" = "dw.ft.se.h.lte_cl_52_erab6" || "$CATALOG_ID" = "dw.ft.se.h.lte_cl_52_ho6" ]];then
          touch ${PROCESS_TMP_DIR}/${EXPORT_FILE_NAME}
          put_to_ftp_dir $EXPORT_FILE_NAME $EXPORT_PATH $FTP_SERVER $SERVICE_CODE
      fi
        EXIT_CODE=92
        log_message  $CUR_LOG_FILE  " Table $CATALOG_ID:$SOURCE_TABLE is no data !"
    fi
  fi


  return $EXIT_CODE
}

main_process
if [[ $? -ne 0 && $EXIT_CODE -ne 92 && $EXIT_CODE -ne 94 ]];then
 sleep 120
 log_message  $CUR_LOG_FILE  " Table $CATALOG_ID retry export at $EXPORT_TIME ! $EXIT_CODE"
 main_process
fi

rm $EXEC_FILE

exit $EXIT_CODE;

