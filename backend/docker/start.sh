#!/bin/bash
set -e

echo "Starting ClamAV..."

clamd &

echo "Waiting for ClamAV..."

for i in {1..30}
do
    if clamdscan --ping 1 >/dev/null 2>&1
    then
        echo "ClamAV is ready"
        break
    fi

    echo "Waiting for ClamAV..."
    sleep 2
done


echo "Starting Spring Boot..."

exec java \
-XX:+UseContainerSupport \
-XX:MaxRAMPercentage=75.0 \
-Djava.security.egd=file:/dev/./urandom \
-jar app.jar