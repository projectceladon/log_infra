#!/system/bin/sh

for i in 0 1
  do
	out=$(printf "/logs/stats/memmonitor_data_2%d.rjz" $i)
	/system/vendor/bin/do_gather.sh 0 10 60 > $out
  done
