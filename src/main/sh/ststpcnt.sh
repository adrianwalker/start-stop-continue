#!/bin/sh

# Redis
#export STARTSTOPCONTINUE_CACHE_HOSTNAME=localhost
#export STARTSTOPCONTINUE_CACHE_PORT=6379

# MinIO
#export STARTSTOPCONTINUE_DATA_ENDPOINT=http://localhost
#export STARTSTOPCONTINUE_DATA_PORT=9000
#export STARTSTOPCONTINUE_DATA_ACCESS_KEY=accesskey
#export STARTSTOPCONTINUE_DATA_SECRET_KEY=secretkey

export STARTSTOPCONTINUE_DATA_PATH=/var/tmp/ststpcnt.com

java -server -Xmx512M -jar start-stop-continue-jar-with-dependencies.jar