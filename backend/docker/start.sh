#!/bin/bash
set -e

echo "Updating ClamAV database..."

freshclam || echo "Freshclam failed, continuing with existing database..."

echo "Configuring ClamAV..."

sed -i 's/^LocalSocket/#LocalSocket/' /etc/clamav/clamd.conf

cat >> /etc/clamav/clamd.conf <<EOF

TCPSocket 3310
TCPAddr 127.0.0.1
EOF

echo "Starting ClamAV..."

clamd &

echo "Waiting for ClamAV..."

for i in {1..60}
do
    if nc -z 127.0.0.1 3310
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
-XX:MaxRAMPercentage=45.0 \
-Djava.security.egd=file:/dev/./urandom \
-jar app.jar