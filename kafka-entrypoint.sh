#!/bin/bash

/wait-for-it.sh zookeeper:2181 --timeout=30

exec /usr/bin/start-kafka.sh
