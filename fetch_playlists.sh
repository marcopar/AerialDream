#!/bin/sh

RES_PATH="src/main/res/raw"

# Make sure raw directory exists
mkdir $RES_PATH

# Check if json files are already there
COUNT=`ls -l $RES_PATH/tvos*.json | wc -l`

# Skip download if files already exist
if [ $COUNT != 1 ]
then
    ### NOT TESTED ON LINUX/MACOS !!! ###
    echo Downloading...
    wget http://a1.phobos.apple.com/us/r1000/000/Features/atv/AutumnResources/videos/entries.json -qO - > $RES_PATH/tvos10.json
    wget http://sylvan.apple.com/Aerials/2x/entries.json -qO - > $RES_PATH/tvos11.json
    wget http://sylvan.apple.com/Aerials/resources.tar -qO - | tar -xOf - entries.json > $RES_PATH/tvos12.json
    wget http://sylvan.apple.com/Aerials/resources-13.tar -qO - | tar -xOf - entries.json > $RES_PATH/tvos13.json
fi

