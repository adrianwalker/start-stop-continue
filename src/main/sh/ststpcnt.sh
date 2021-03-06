#!/bin/sh

#Jetty
#export STARTSTOPCONTINUE_HTTP_PORT=8080

# Redis
#export STARTSTOPCONTINUE_CACHE_HOSTNAME=localhost
#export STARTSTOPCONTINUE_CACHE_PORT=6379
#export STARTSTOPCONTINUE_PUBSUB_HOSTNAME=localhost
#export STARTSTOPCONTINUE_PUBSUB_PORT=6379

# MinIO
#export STARTSTOPCONTINUE_DATA_ENDPOINT=http://localhost
#export STARTSTOPCONTINUE_DATA_PORT=9000
#export STARTSTOPCONTINUE_DATA_ACCESS_KEY=accesskey
#export STARTSTOPCONTINUE_DATA_SECRET_KEY=secretkey
#export STARTSTOPCONTINUE_DATA_BUCKET=start-stop-continue

# ElastiCache
#export STARTSTOPCONTINUE_CACHE_HOSTNAME=start-stop-continue.xllhsl.0001.euw2.cache.amazonaws.com
#export STARTSTOPCONTINUE_CACHE_PORT=6379
#export STARTSTOPCONTINUE_PUBSUB_HOSTNAME=start-stop-continue.xllhsl.0001.euw2.cache.amazonaws.com
#export STARTSTOPCONTINUE_PUBSUB_PORT=6379

# S3
#export STARTSTOPCONTINUE_DATA_ENDPOINT=http://start-stop-continue.s3.eu-west-2.amazonaws.com
#export STARTSTOPCONTINUE_DATA_PORT=80
#export STARTSTOPCONTINUE_DATA_ACCESS_KEY=accesskey
#export STARTSTOPCONTINUE_DATA_SECRET_KEY=secretkey
#export STARTSTOPCONTINUE_DATA_BUCKET=start-stop-continue
#export STARTSTOPCONTINUE_DATA_SECURE=false
#export STARTSTOPCONTINUE_DATA_REGION=eu-west-2

export STARTSTOPCONTINUE_DATA_PATH=/var/tmp/ststpcnt.com

java -server -Xmx512M -jar start-stop-continue-jar-with-dependencies.jar