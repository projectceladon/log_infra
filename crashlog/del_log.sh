#!/sbin/ash

if [ $# -eq 1 ] ; then
        dir="/data/logs/crashlog"
        num=$1
        tmp=$dir$num
        echo $tmp
        cd /data/logs
        rm -r $tmp

else

  echo "USAGE: del_log number"

fi

