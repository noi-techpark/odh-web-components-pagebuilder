#!/bin/bash

cp /code/src/main/resources/application.properties.example /code/src/main/resources/application.properties
cp /code/src/main/resources/application.users-file.example /code/src/main/resources/application.users-file

export MAVEN_CONFIG="$HOME"

/bin/bash -c "$@"
