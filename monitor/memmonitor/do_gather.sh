#!/system/bin/sh
# Goal: Script to record system metrics to follow platform usage
# Usage
#   do_gather.sh [recording level] [sampling interval] [life duration]
# Example :
#   On PC: adb shell "/data/do_gather.sh | busybox uuencode -" | uudecode > ./records.rjz
#   On Device : /data/do_gather.sh > /mnt/asec/records.rjz
#
# Inputs:
#   recording level   : empty or "0" => only general recordings
#                       "1"          => all per-process statistics
#   sampling interval : seconds between each records sampling
#   life duration     : seconds after which the script terminates
#
# Default Inputs:
#   recoding level    =   1
#   sampling interval =  10 seconds
#   life duration     = 120 seconds
#
# Output: on stdout outputs a record stream

# The record stream follows a strict format, described in a RFC.
# See : Intel TeamForge, mcg_sad-system_analysers project
# https://git-ger-3.devtools.intel.com/gerrit/gitweb?p=mcg_sad-system_analysers.git

seq=0
total_written=0
epoch0=$(date +%s)
SECONDS0=$SECONDS
TS=$EPOCH0
TOTAL_SECONDS=0

compute_timestamp()
{
	global TS
	global TOTAL_SECONDS
	global SECONDS
	global SECONDS0
	TS=$(($EPOCH0 + $SECONDS - $SECONDS0))
	TOTAL_SECONDS=$(($SECONDS - $SECONDS0))
}

write_one_record()
{
	global total_written

	ts="$1"
	content="$2"
	content_lg=${#content}
	seq=$(($seq+1))
	out="1\n$ts\n$seq\n$content_lg\n$content"
	echo -n -e "$out"
	total_written=$(( $total_written + ${#out} ))
}

record_file()
{
	ts="$1"
	filename="$2"
	filecontent="$(<$filename 2>/dev/null)"
	# This next sentence is ugly, but a form like content=$(echo ...) or
	# content=$(print ...) implies a fork() we don't want to pay.
	content="$filename
$filecontent"
	if [ ${#filecontent} -gt 0 ]; then
		write_one_record $ts "$content"
	fi
}

record_fileglob()
{
	ts=$1
	shift
	fglob=$@
	for filename in $fglob; do
		record_file $ts "$filename"
	done
}

record_dirglob()
{
	ts=$1
	dglob="$2"
	shift 2
	files=$@
	for dir in $dglob; do
		for file in $files; do
			record_file $ts "$dir/$file"
		done
	done
}

write_empty_stream_header()
{
        echo -n -e "0.1\n0\n"
}

### MAIN

level=${1:-"1"}
interval=${2:-"10"}
duration=${3:-"120"}
max_stream_size=5000000

mmcstats=/sys/block/mmcblk*/mmcblk*p*/stat
cpufreqs=/sys/devices/system/cpu/cpu*/cpufreq/stats/time_in_state
cpuidles=/sys/devices/system/cpu/cpu*/cpuidle/state*/time

write_empty_stream_header
compute_timestamp
record_fileglob $TS "/sys/devices/system/cpu/cpu*/cpuidle/state*/name"

while [ $total_written -lt 5000000 -a $TOTAL_SECONDS -lt $duration ]; do
	compute_timestamp
	record_file $TS "/proc/loadavg"
	record_file $TS "/proc/pagetypeinfo"
	record_file $TS "/proc/meminfo"
	record_file $TS "/proc/schedstat"
	record_file $TS "/proc/vmstat"
	if [ "$level" == "1" ]; then
		record_dirglob $TS '/proc/[0-9]*' io stat statm
		record_fileglob $TS $mmcstats
		record_fileglob $TS $cpufreqs
		record_fileglob $TS $cpuidles
	fi

	sleep $interval
done
