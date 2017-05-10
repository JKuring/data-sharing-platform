#!/bin/bash

cd `dirname $0`

CONTROLLER="Controller"
LOADER="Loader"
AGGREGATOR="Aggregator"
PUBLISHER="Publisher"


function start_all(){
        cd ./DataController/bin
        echo `./controller.sh start Controller`
        cd ../../

        cd ./DataLoader/bin
        echo `./loader.sh start Loader`
        cd ../../

        cd ./DataAggregator/bin
        echo `./aggregator.sh start Aggregator`
        cd ../../

        cd ./DataPublisher/bin
        echo `./publisher.sh start Publisher`
        cd ../../
}


function stop_all(){
        cd ./DataController/bin
        echo `./controller.sh stop Controller`
        cd ../../

        cd ./DataLoader/bin
        echo `./loader.sh stop Loader`
        cd ../../

        cd ./DataAggregator/bin
        echo `./aggregator.sh stop Aggregator`
        cd ../../

        cd ./DataPublisher/bin
        echo `./publisher.sh stop Publisher`
        cd ../../
}


function restartAll(){
    stop_all
    start_all
}

case $1 in
        startAll)
                start_all
                ;;
        stopAll)
                stop_all
                ;;
        restartAll)
                restartAll
                ;;
        *)
                echo "Invalid parametter!"
esac