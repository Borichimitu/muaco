#!/bin/bash
irace > log 2> errlog

SCRIPT_NAME=$(basename $0)

if [ $? -ne 0 ] ; 
then
    tail --lines 20 errlog | mail -s "$SCRIPT_NAME - FAILURE" chivdan@gmail.com
else
    tail --lines 20 log | mail -s "$SCRIPT_NAME - SUCCESS" chivdan@gmail.com
fi
