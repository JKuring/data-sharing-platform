#!/bin/bash
cd `dirname "$0"`/../..
INAS_SCRIPT_ROOT=`pwd`
. $INAS_SCRIPT_ROOT/bin/setenv.sh

#load delay  minutes
LOAD_DELAY=15

Day=$(date +"%Y%m%d" --date="-$LOAD_DELAY min")
Hour=$(date +"%H" --date="-$LOAD_DELAY min")
Minute=$(date +"%M" --date="-$LOAD_DELAY min")


#Day=20150417
#Hour=13
#Minute=50

if [  ! -e "$INAS_SCRIPT_ROOT/logs/load" ] ; then
        mkdir -p "$INAS_SCRIPT_ROOT/logs/load"
fi 

CONFIG_DIR=$INAS_SCRIPT_ROOT/conf
CUR_LOG_FILE=$INAS_SCRIPT_ROOT/logs/load/ld_hbase_$Day.log


JOB_RUN_LIMIT=100

check_prev_running_count()
{
	 local  RUNNING_COUNT=$(ps -ef|grep org.apache.hadoop.hbase.mapreduce.ImportTsv | grep -v grep |wc -l)
	 if [ $RUNNING_COUNT -gt $JOB_RUN_LIMIT ]; then
				return 1
    else
        log_message  $CUR_LOG_FILE  " Hbase load  has $RUNNING_COUNT  jobs not finished in past cycles . " 
        return 0
		fi
	
}


log_message  $CUR_LOG_FILE  "==================  ($Day/$Hour/$Minute) hbase load begin     ======================================="

check_prev_running_count

if [ $? -eq 1 ] ; then
	  log_message  $CUR_LOG_FILE  " Hbase load exceed running  limit $JOB_RUN_LIMIT , skip load cycle ($Day/$Hour/$Minute) !"
		exit 1
fi

 while read line; do
	{
		table=$(echo $line | awk 'gsub(/^ *| *$/,"")' | sed 's/^#.*//' )
		if [ ! -z "$table"  ] ; then
			$INAS_SCRIPT_ROOT/bin/load/new_bulkload_hbase_table.sh  $Day $Hour $Minute $table
		fi
	} &
done < $CONFIG_DIR/hbase_group1.conf 

wait
log_message  $CUR_LOG_FILE  "==================  ($Day/$Hour/$Minute) hbase load finished   ======================================="

