#!/bin/bash

cd `dirname $0`


# Process Name to display
PROC_NAME="PM_TASK_TRANS_HW"$2


# Process Tag to identify
# Use this tag should identify the process.
# The command is :  ps -ef | grep -w $PROC_TAG
PROC_TAG="PM_TASK_TRANS_HW"$2


# Process Description
PROC_DESC="EASTCOM Software PM_TASK_TRANS_HW 1.0"


# Flags for java Virtal Machine
#VM_FLAG="-d64 -Xrs -Xms16G -Xmx32G -Duser.name=mqm -Djava.security.auth.login.config=/opt/hadoopclient/HDFS/hadoop/etc/hadoop/jaas.conf -Djava.security.krb5.conf=/opt/hadoopclient/KrbClient/kerberos/var/krb5kdc/krb5.conf"
VM_FLAG="-Dauth.config.path=conf/hbase/auth/ -Dzookeeper.sasl.clientconfig=client -Dzookeeper.server.principal=zookeeper/hadoop.hadoop_b.com -Dhadoop.user.root.path=/user/east_wys"


# List of blank-separated paths defining the contents of the classes and resources
LOADER_PATH="../lib/*.jar ../conf"


# Specify the java_home
java_home=$JAVA_HOME

# Process JAR
PACKAGE=data-controller-1.0-SNAPSHOT.jar

# Process Entrance class
MAIN_CLASS=com.eastcom.Controller


# Process Arguments
PROC_ARGS=


# Process log file
LOG_FILE=../logs/logs.log


# Define information tag
RUNNING_TAG="[V]"
NOT_RUNNING_TAG="[X]"
ERROR_TAG="[E]"
INFO_TAG="[I]"

#=======================================================================
# Define functions for process
#=======================================================================

set_classpath(){
	set ${LOADER_PATH}
        while [ $# -gt 0 ]; do
                classpath=${classpath}:$1
                shift
        done
        CLASSPATH=${classpath}:${CLASSPATH}
        CLASSPATH=../.:../${PACKAGE}:${CLASSPATH}
}

is_proc_run(){
        ps -efww | grep -w "${PROC_TAG}" | grep -v grep &>/dev/null
        return $?
}

status_proc(){
        is_proc_run
        if [ $? -eq 0 ]; then
                echo "${RUNNING_TAG} ${PROC_NAME} is running !"
        	ps -efww | grep -w "${PROC_TAG}" | grep -v grep
        else
                echo "${NOT_RUNNING_TAG} ${PROC_NAME} is not running !"
        fi
}

start_proc(){
        is_proc_run
        if [ $? -eq 0 ]; then
                echo "${INFO_TAG} ${PROC_NAME} is already running !"
        else
                echo "${INFO_TAG} Starting ${PROC_NAME} ..."
		set_classpath
                echo "CLASSPATH ${CLASSPATH}"
#                nohup ${java_home}/bin/java -Diname=${PROC_TAG} ${VM_FLAG} -cp ${CLASSPATH} ${MAIN_CLASS} ${PROC_ARGS} >> ${LOG_FILE} 2>&1 &
                setsid ${java_home}/bin/java -Diname=${PROC_TAG} ${VM_FLAG} -cp ${CLASSPATH} ${MAIN_CLASS} ${PROC_ARGS} &
		sleep 1
		is_proc_run
		if [ $? -eq 0 ]; then
                	echo "${INFO_TAG} ${PROC_NAME} started !"
		else
                	echo "${ERROR_TAG} ${PROC_NAME} starts failed !"
		fi

        fi
}

stop_proc(){
        pid=`ps -efww | grep -w "${PROC_TAG}" | grep -v grep | awk '{print $2}'`
        if [ -z "$pid" ]; then
                echo "${INFO_TAG} ${PROC_NAME} has already stopped !"
        else
                echo "${INFO_TAG} Stopping ${PROC_NAME} ..."
                kill $pid
		sleep 3
		is_proc_run
		if [ $? -eq 0 ]; then
                	echo "${ERROR_TAG} ${PROC_NAME} stops failed !"
		else
                	echo "${INFO_TAG} ${PROC_NAME} stopped !"
		fi

        fi
}

usage(){
	echo ${PROC_DESC} usage:
        echo -e "`basename $0` <start|stop|status|restart>"
        echo -e "\tstart   - start   ${PROC_NAME}"
        echo -e "\tstop    - stop    ${PROC_NAME}"
        echo -e "\tstatus  - list    ${PROC_NAME}"
        echo -e "\trestart - restart ${PROC_NAME}"
}


#=======================================================================
# Main Program begin
#=======================================================================

case $1 in
        start)
                start_proc
                ;;
        status)
                status_proc
                ;;
        stop)
                stop_proc
                ;;
        restart)
                stop_proc
                start_proc
                ;;
        *)
                usage
esac


