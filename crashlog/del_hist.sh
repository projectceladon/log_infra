#!/sbin/sh

if [ $# -le 1 ] ; then
        if [ $# -eq 0 ]; then
        filename="history_event"
        else
        filename=$1
        fi
        tmp=$filename".tmp"

        cd /data/logs
        if [ -f $filename ]; then
                read line < $filename
                echo $line > $tmp
                mv $tmp $filename
        fi
else

  echo "USAGE: del_hist [filename]"

fi

