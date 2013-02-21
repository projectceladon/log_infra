#!/system/bin/sh
while [ 1 ]; do
  if [ -f /logs/stats/memmonitor_flush ] ; then
    echo "DATA0=" > /logs/stats/memmonitor_trigger
	sleep 20
  fi
  /system/bin/memmonitor_hours.sh & /system/bin/memmonitor_seconds.sh
  echo "DATA0=" > /logs/stats/memmonitor_trigger
  sleep 20
done
