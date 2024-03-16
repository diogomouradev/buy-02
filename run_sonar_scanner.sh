#!/bin/bash

# Move to the specified directory
cd $1

# Run SonarScanner with the provided arguments
sonar-scanner "${@:2}"
