#!/bin/bash

cd `dirname "$0"`/../..
INAS_SCRIPT_ROOT=`pwd`
source /opt/hadoopclient/bigdata_env
. $INAS_SCRIPT_ROOT/bin/setenv.sh

CONF_DIR=$INAS_SCRIPT_ROOT/conf
CONF_FILE=$INAS_SCRIPT_ROOT/conf/hbase_load.conf
LOG_DIR=$INAS_SCRIPT_ROOT/tmp/hbase

DATA_ROOT=
ROWKEY_INDEX=
ROWKEY_STRATEGIES=
ROWKEY_ENCRYPTS=
SEPARATOR=""
ENCRYPT=yes
REAL_TABLE=

#NOENCRYPT_CLASS=org.apache.hadoop.hbase.mapreduce.TsvImporterPutMapperNoEncrypt
#ENCRYPT_CLASS=org.apache.hadoop.hbase.mapreduce.TsvImporterPutMapper


#  parameter  list ( target_table )
read_load_conf()
{
	local TARGET_TABLE=$1
	DATA_ROOT=$(readConfigValue $CONF_FILE $TARGET_TABLE DATA_PATH)
	ROWKEY_INDEX=$(readConfigValue $CONF_FILE $TARGET_TABLE ROWKEY_INDEX)
	ROWKEY_STRATEGIES=$(readConfigValue $CONF_FILE $TARGET_TABLE ROWKEY_STRATEGIES)
	ROWKEY_ENCRYPTS=$(readConfigValue $CONF_FILE $TARGET_TABLE ROWKEY_ENCRYPTS)
	SEPARATOR=$(readConfigValue $CONF_FILE $TARGET_TABLE SEPARATOR)
	ENCRYPT=$(readConfigValue $CONF_FILE $TARGET_TABLE VALUE_ENCRYPT)
	REAL_TABLE=$(readConfigValue $CONF_FILE $TARGET_TABLE REAL_TABLE)
	
	return 0;
}

#  parameter  list ( day , hour ,minitue , target_table )
load_data()
{
	local DAY=$1
	local HOUR=$2
	local MINUTE=$3
	local TARGET_TABLE=$4
	
	local MAPPER_CLASS=org.apache.hadoop.hbase.mapreduce.TsvImporterPutMapper
	
	DEFAULT_LOG_FILE=$INAS_SCRIPT_ROOT/logs/load/ld_fatal_error.log
	
	echo "TARGET_TABLE=" $TARGET_TABLE 
		
	read_load_conf  $TARGET_TABLE
	
	if [ $? -eq 1 ] ; then
		log_message  $DEFAULT_LOG_FILE  " Not found  $TARGET_TABLE  load configuration !"
		return  1
	fi 
	
	if [[ -z $REAL_TABLE ]] ;then
		REAL_TABLE=$TARGET_TABLE
	fi
	
	echo "DATA_ROOT=" $DATA_ROOT 
	
	CUR_LOG_FILE=$INAS_SCRIPT_ROOT/logs/load/ld_hbase_$DAY.log
	CUR_DATA_PATH=${HADOOP_USER_ROOT}${DATA_ROOT}/$DAY/$HOUR/$MINUTE
	
	echo "ROWKEY_INDEX=" $ROWKEY_INDEX >> $LOG_DIR/$TARGET_TABLE.log
	echo "ROWKEY_STRATEGIES=" $ROWKEY_STRATEGIES >> $LOG_DIR/$TARGET_TABLE.log
	echo "ROWKEY_ENCRYPTS=" $ROWKEY_ENCRYPTS >> $LOG_DIR/$TARGET_TABLE.log
	echo "SEPARATOR=" $SEPARATOR >> $LOG_DIR/$TARGET_TABLE.log
	echo "TARGET_TABLE=" $TARGET_TABLE >> $LOG_DIR/$TARGET_TABLE.log
	echo "ENCRYPT=" $ENCRYPT >> $LOG_DIR/$TARGET_TABLE.log  
	echo "REAL_TABLE=" $REAL_TABLE >> $LOG_DIR/$TARGET_TABLE.log
	
	START_TIME=`date +'%s'`
	
	#echo "hdfs dfs -test -e $CUR_DATA_PATH"
  	hdfs dfs -test -e $CUR_DATA_PATH
	if [ $? -gt 0 ] ; then
		log_message  $CUR_LOG_FILE  " Data file path $CUR_DATA_PATH  not exists !"
		return  1
	fi 
	

	
	echo "CUR_DATA_PATH=" $CUR_DATA_PATH >> $LOG_DIR/$TARGET_TABLE.log
	
	local CV_TABLE_NAME=$(echo "$TARGET_TABLE" | sed 's/:/_/g;s/\./_/g')
	
	local HFILE_TMP_DIR="/tmp/hbload/${CV_TABLE_NAME}/${DAY}${HOUR}${MINUTE}"
	
	if [ $ENCRYPT == "yes" ];then
		echo " ENCRYPT   value  .... "  >> $LOG_DIR/$TARGET_TABLE.log
	  echo "hbase org.apache.hadoop.hbase.mapreduce.ImportTsv \
	  -Dimporttsv.columns=HBASE_ROW_KEY,cf \
	  -Dimporttsv.bulk.output=$HFILE_TMP_DIR \
	  -Dimporttsv.mapper.class=$MAPPER_CLASS \
	  -Dmapreduce.map.memory.mb=2048 -Dhbase.client.retries.number=10 \
	  -Dimporttsv.rowkey.indexs=$ROWKEY_INDEX \
		-Dimporttsv.rowkey.strategies=$ROWKEY_STRATEGIES \
		-Dimporttsv.rowkey.encrypts=$ROWKEY_ENCRYPTS  '-Dimporttsv.separator=\|'  \
     $REAL_TABLE $CUR_DATA_PATH   " >> $LOG_DIR/$TARGET_TABLE.log
	
		hbase org.apache.hadoop.hbase.mapreduce.ImportTsv \
		-Dimporttsv.columns=HBASE_ROW_KEY,cf \
		-Dimporttsv.bulk.output=$HFILE_TMP_DIR \
		-Dimporttsv.mapper.class=$MAPPER_CLASS \
		-Dmapreduce.map.memory.mb=2048 -Dhbase.client.retries.number=10  \
		-Dimporttsv.rowkey.indexs="$ROWKEY_INDEX" \
		-Dimporttsv.rowkey.strategies="$ROWKEY_STRATEGIES" \
		-Dimporttsv.rowkey.encrypts="$ROWKEY_ENCRYPTS"  '-Dimporttsv.separator=\|'  \
		 $REAL_TABLE $CUR_DATA_PATH  >> $LOG_DIR/$TARGET_TABLE.log  2>&1
	
	else
		echo " Don't  ENCRYPT   value  .... "  >> $LOG_DIR/$TARGET_TABLE.log
		
		echo "hbase org.apache.hadoop.hbase.mapreduce.ImportTsv \
		-Dimporttsv.columns=HBASE_ROW_KEY,cf \
		-Dmapreduce.map.memory.mb=2048 \
		-Dhbase.client.retries.number=10  -Dimporttsv.mapper.class=$MAPPER_CLASS \
		-Dimporttsv.bulk.output=$HFILE_TMP_DIR \
		-Dimporttsv.value.encrypts=null \
		-Dimporttsv.rowkey.indexs=$ROWKEY_INDEX -Dimporttsv.rowkey.strategies=$ROWKEY_STRATEGIES \
		-Dimporttsv.rowkey.encrypts=$ROWKEY_ENCRYPTS  '-Dimporttsv.separator=\|'  \
		 $REAL_TABLE $CUR_DATA_PATH "  >> $LOG_DIR/$TARGET_TABLE.log 
		
		hbase org.apache.hadoop.hbase.mapreduce.ImportTsv 
		-Dimporttsv.columns=HBASE_ROW_KEY,cf \
		-Dmapreduce.map.memory.mb=2048 \
		-Dhbase.client.retries.number=10  -Dimporttsv.mapper.class=$MAPPER_CLASS \
		-Dimporttsv.bulk.output=$HFILE_TMP_DIR \
		-Dimporttsv.value.encrypts=null \
		-Dimporttsv.rowkey.indexs="'$ROWKEY_INDEX'" -Dimporttsv.rowkey.strategies="$ROWKEY_STRATEGIES" \
		-Dimporttsv.rowkey.encrypts="$ROWKEY_ENCRYPTS"  '-Dimporttsv.separator=\|'  \
		 $REAL_TABLE $CUR_DATA_PATH   >> $LOG_DIR/$TARGET_TABLE.log  2>&1 
	fi
	
	if [ $? -gt 0 ] ; then
		log_message  $CUR_LOG_FILE  "[ $TARGET_TABLE ]  Data file path $CUR_DATA_PATH  create HFile  failure !"
		hdfs dfs -rm -r -skipTrash  $HFILE_TMP_DIR
		return  1
	fi
	
	hbase   org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles  $HFILE_TMP_DIR $REAL_TABLE >> $LOG_DIR/$TARGET_TABLE.log  2>&1 
	
	if [ $? -gt 0 ] ; then
		log_message  $CUR_LOG_FILE  "[ $TARGET_TABLE ]  Data file path $CUR_DATA_PATH  LoadIncrementalHFiles  failure !"
		hdfs dfs -rm -r -skipTrash  $HFILE_TMP_DIR
		return  1
	fi
			
	END_TIME=`date +'%s'`
	DURATION=`expr "$END_TIME" - "$START_TIME"`
	
	hdfs dfs -rm -r -skipTrash  $HFILE_TMP_DIR
	
	log_message  $CUR_LOG_FILE  "[ $TARGET_TABLE ] Data file under $CUR_DATA_PATH  bulkload finished , estimate $DURATION  seconds ."
	return 0;
}

load_data $1 $2 $3 $4
exit $?




