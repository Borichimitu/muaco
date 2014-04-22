#!/bin/bash
rm -r attempts
mkdir attempts && java -Xms2g -Xmx2g -jar gabp.jar elevator.xml -1 1000000 1000 > log.log 2>errlog.log
