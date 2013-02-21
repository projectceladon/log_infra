#!/system/bin/sh


for i in 0 1 2 3 4 5 6 7 8 9 10 11
  do
	out=$(printf "/logs/stats/memmonitor_data_%d.rjz" $i)
	/system/bin/do_gather.sh 0 60 3600 > $out
  done
