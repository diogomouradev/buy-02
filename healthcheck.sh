#!/bin/bash

service=$1
check=$(curl -o /dev/null -s -w "%{http_code}\n" -k http://localhost:8080/api/${service}Health)

if [ "$check" == "200" ] || [ "$check" == "401" ]
then
  echo "success"
  exit 0;
else
  echo "error"
  exit 1;
fi
