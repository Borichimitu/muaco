#!/bin/bash
SCRIPT_DIR="$( cd "$( dirname "$0")" && pwd )"
mkdir Instances
for i in $(ls ../instances/instances)
do 
    echo "$SCRIPT_DIR/../instances/instances/$i" > "Instances/$i"
done
