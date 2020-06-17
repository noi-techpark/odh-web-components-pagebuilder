#!/bin/bash

mkdir -p ~/.m2

cat > ~/.m2/settings.xml << EOF
<settings>
    <localRepository>$PWD/infrastructure/docker/.m2</localRepository>
</settings>
EOF

cp /code/src/main/resources/application.properties.example /code/src/main/resources/application.properties
cp /code/src/main/resources/application.users-file.example /code/src/main/resources/application.users-file

export MAVEN_CONFIG="$HOME"

/bin/bash -c "$@"
