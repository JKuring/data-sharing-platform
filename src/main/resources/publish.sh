#!/bin/bash
cd `dirname "$0"`/../..
INAS_SCRIPT_ROOT=`pwd`
. $INAS_SCRIPT_ROOT/bintenv.sh

set -x

EXPORT_TABLE=$1
EXPORT_TIME=$2
EXPORT_FILE_NAME=$3
FTP_SERVER=$4
FTP_USER=$5
FTP_PASSWORD=$6
FTP_PATH=$7

FILE_TIME=$(echo $EXPORT_TIME | sed 's/://g;s/ //g;s/-//g')
HOUR_PARTITION=$(echo $FILE_TIME | awk '{print substr($0,1,12)}')
DAY_PARTITION=$(echo $FILE_TIME | awk '{print substr($0,1,8)}')0000
MONTH_PARTITION=$(echo $FILE_TIME | awk '{print substr($0,1,6)}')000000

PROCESS_TMP_DIR=$INAS_SCRIPT_ROOT/data/temp
LOG_FILE=$INAS_SCRIPT_ROOT/logs/export/export_ftp_$(echo $FILE_TIME | awk '{print substr($0,1,8)}').log

if [[ "$EXPORT_TABLE" =~ _h$ ]]; then
  HDFS_EXPORT_PATH="/user/hive/warehouse/ipms.db/${EXPORT_TABLE}/hour_partition=${HOUR_PARTITION}"
  PARTITION_TYPE=hour_partition
  PARTITION_VALUE=$HOUR_PARTITION
elif [[ "$EXPORT_TABLE" =~ _d$ ]]; then
  HDFS_EXPORT_PATH="/user/hive/warehouse/ipms.db/${EXPORT_TABLE}/day_partition=${DAY_PARTITION}"
  PARTITION_TYPE=day_partition
  PARTITION_VALUE=$DAY_PARTITION
elif [[ "$EXPORT_TABLE" =~ _m$ ]]; then
  HDFS_EXPORT_PATH="/user/hive/warehouse/ipms.db/${EXPORT_TABLE}/month_partition=${MONTH_PARTITION}"
  PARTITION_TYPE=month_partition
  PARTITION_VALUE=$MONTH_PARTITION
else
	echo "Invalid export source table name, table name must be ended with _d , _m , _h "
	exit 2;
fi

EXPORT_FILE_LIST=$(hdfs dfs -ls "$HDFS_EXPORT_PATH" | grep -v ^d | grep -v ^Found   | awk '{print $8}')
	for FILE in $EXPORT_FILE_LIST; do
      hdfs dfs -cat $FILE >> ${PROCESS_TMP_DIR}/${EXPORT_FILE_NAME}
	done