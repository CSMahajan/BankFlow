#!/bin/bash
set -e

echo "Updating ClamAV database..."

freshclam || echo "Freshclam failed, continuing..."

echo "Configuring ClamAV TCP..."

sed -i 's/^LocalSocket/#LocalSocket/' /etc/clamav/clamd.conf

echo "TCPSocket 3310" >> /etc/clamav/clamd.conf
echo "TCPAddr 127.0.0.1" >> /etc/clamav/clamd.conf


echo "Starting ClamAV..."

clamd &


echo "Waiting for ClamAV..."

for i in {1..30}
do
    if nc -z localhost 3310
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