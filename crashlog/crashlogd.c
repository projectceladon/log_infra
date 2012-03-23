/*
* Copyright (C) Intel 2010
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

#include <stdarg.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <fcntl.h>
#include <ctype.h>
#include <errno.h>
#include <time.h>
#include <dirent.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <private/android_filesystem_config.h>
#include <linux/ioctl.h>
#include <linux/rtc.h>
#define LOG_TAG "CRASHLOG"
#include <linux/android_alarm.h>
#include "cutils/log.h"
#include <sys/inotify.h>
#include <cutils/properties.h>

#include <sha1.h>

#define CRASHEVENT "CRASH"
#define STATSEVENT "STATS"
#define STATSTRIGGER "STTRIG"
#define KERNEL_CRASH "IPANIC"
#define SYSSERVER_WDT "UIWDT"
#define KERNEL_FORCE_CRASH "IPANIC_FORCED"
#define ANR_CRASH "ANR"
#define JAVA_CRASH "JAVACRASH"
#define WTF_CRASH "WTF"
#define TOMB_CRASH "TOMBSTONE"
#define LOST "LOST_DROPBOX"
#define AP_COREDUMP "APCOREDUMP"
#define MODEM_CRASH "MPANIC"
#define MODEM_SHUTDOWN "MSHUTDOWN"
#define CURRENT_UPTIME "CURRENTUPTIME"
#define PER_UPTIME "UPTIME"
#define SYS_REBOOT "REBOOT"
#define AP_INI_M_RST "APIMR"
#define M_RST_WN_COREDUMP "MRESET"
#define FABRIC_ERROR "FABRICERR"

#define FILESIZE_MAX  (10*1024*1024)
#define PATHMAX 512
#define UPTIME_FREQUENCY (5 * 60)
#define UPTIME_HOUR_FREQUENCY 12
#define SIZE_FOOTPRINT_MAX (PROPERTY_VALUE_MAX + 1) * 11
#define BUILD_FIELD "ro.build.version.incremental"
#define BOARD_FIELD "ro.product.model"
#define FINGERPRINT_FIELD "ro.build.fingerprint"
#define KERNEL_FIELD "sys.kernel.version"
#define USER_FIELD "ro.build.user"
#define HOST_FIELD "ro.build.host"
#define IFWI_FIELD "sys.ifwi.version"
#define SCUFW_VERSION "sys.scu.version"
#define PUNIT_VERSION "sys.punit.version"
#define IAFW_VERSION "sys.ia32.version"
#define VALHOOKS_VERSION "sys.valhooks.version"
#define MODEM_FIELD "gsm.version.baseband"
#define IMEI_FIELD "persist.radio.device.imei"
#define PROP_CRASH "persist.service.crashlog.enable"
#define PROP_PROFILE "persist.service.profile.enable"
#define PROP_COREDUMP "persist.core.enabled"
#define SYS_PROP "/system/build.prop"
#define SAVEDLINES  1
#define MAX_RECORDS 5000
#define MAX_DIR 1000
#define HISTORY_FILE_DIR  "/data/logs"
#define HISTORY_CORE_DIR  "/data/logs/core"
#define APLOG_FILE_0        "/data/logs/aplog"
#define APLOG_FILE_1    "/data/logs/aplog.1"
#define BPLOG_FILE_0    "/data/logs/bplog"
#define BPLOG_FILE_1    "/data/logs/bplog.1"
#define APLOG_TYPE       0
#define BPLOG_TYPE       1
#define SDCARD_CRASH_DIR "/mnt/sdcard/data/logs/crashlog"
#define EMMC_CRASH_DIR "/data/logs/crashlog"
#define SDCARD_STATS_DIR "/mnt/sdcard/data/logs/stats"
#define EMMC_STATS_DIR "/data/logs/stats"
#define CRASH_CURRENT_LOG "/data/logs/currentcrashlog"
#define STATS_CURRENT_LOG "/data/logs/currentstatslog"
#define HISTORY_FILE  "/data/logs/history_event"
#define HISTORY_UPTIME "/data/logs/uptime"
#define LOG_UUID "/data/logs/uuid.txt"
#define LOG_BUILDID "/data/logs/buildid.txt"
#define KERNEL_CMDLINE "/proc/cmdline"
#define STARTUP_STR "androidboot.wakesrc="
#define STARTUP_STR_OLD "androidboot.mode="
#define PANIC_CONSOLE_NAME "/proc/emmc_ipanic_console"
#define PROC_FABRIC_ERROR_NAME "/proc/ipanic_fabric_err"
#define PROC_UUID  "/proc/emmc0_id_entry"

#define SAVED_CONSOLE_NAME "/data/dontpanic/emmc_ipanic_console"
#define SAVED_THREAD_NAME "/data/dontpanic/emmc_ipanic_threads"
#define SAVED_LOGCAT_NAME "/data/dontpanic/emmc_ipanic_logcat"
#define SAVED_FABRIC_ERROR_NAME "/data/dontpanic/ipanic_fabric_err"
#define CONSOLE_NAME "emmc_ipanic_console"
#define THREAD_NAME "emmc_ipanic_threads"
#define LOGCAT_NAME "emmc_ipanic_logcat"
#define FABRIC_ERROR_NAME "ipanic_fabric_err"

#define MODEM_SHUTDOWN_TRIGGER "/data/logs/modemcrash/mshutdown.txt"

char *CRASH_DIR = NULL;
char *STATS_DIR = NULL;

char buildVersion[PROPERTY_VALUE_MAX];
char boardVersion[PROPERTY_VALUE_MAX];
char uuid[256];
int loop_uptime_event = 1;
int test_flag = 0;

static int do_mv(char *src, char *des)
{
	struct stat st;

	/* check if destination exists */
	if (stat(des, &st)) {
		/* an error, unless the destination was missing */
		if (errno != ENOENT) {
			LOGE("failed on %s - %s\n", des, strerror(errno));
			return -1;
		}
	}

	/* attempt to move it */
	if (rename(src, des)) {
		LOGE("failed on '%s' - %s\n", src, strerror(errno));
		return -1;
	}

	return 0;
}

static unsigned int android_name_to_id(const char *name)
{
	const struct android_id_info *info = android_ids;
	unsigned int n;

	for (n = 0; n < android_id_count; n++) {
		if (!strcmp(info[n].name, name))
			return info[n].aid;
	}

	return -1U;
}

static unsigned int decode_uid(const char *s)
{
	unsigned int v;

	if (!s || *s == '\0')
		return -1U;
	if (isalpha(s[0]))
		return android_name_to_id(s);

	errno = 0;
	v = (unsigned int)strtoul(s, 0, 0);
	if (errno)
		return -1U;
	return v;
}

static int do_chown(char *file, char *uid, char *gid)
{
	if (strstr(file, SDCARD_CRASH_DIR))
		return 0;

	if (chown(file, decode_uid(uid), decode_uid(gid)))
		return -errno;

	return 0;
}

static int do_copy(char *src, char *des, int limit)
{
	int buflen = 4*1024;
	char buffer[4*1024];
	int rc = 0;
	int fd1 = -1, fd2 = -1;
	struct stat info;
	int brtw, brtr;
	char *p;
	int filelen,tmp;

	if (stat(src, &info) < 0)
		return -1;

	if ((fd1 = open(src, O_RDONLY)) < 0)
		goto out_err;

	if ((fd2 = open(des, O_WRONLY | O_CREAT | O_TRUNC, 0660)) < 0)
		goto out_err;

	if ( (limit == 0) || (limit >= info.st_size) )
		filelen = info.st_size;
	else{
		filelen = limit;
		lseek(fd1, info.st_size-limit, SEEK_SET);
		}

	while(filelen){
		p = buffer;
		tmp = ((filelen>buflen) ? buflen : filelen);
		brtr = tmp;
		while (brtr) {
			rc = read(fd1, p, brtr);
			if (rc < 0)
				goto out_err;
			if (rc == 0)
				break;
			p += rc;
			brtr -= rc;
		}

		p = buffer;
		brtw = tmp;
		while (brtw) {
			rc = write(fd2, p, brtw);
			if (rc < 0)
				goto out_err;
			if (rc == 0)
				break;
			p += rc;
			brtw -= rc;
		}

		filelen = filelen - tmp;
	}

	rc = 0;
	goto out;
out_err:
	rc = -1;
out:
	if (fd1 >= 0)
		close(fd1);
	if (fd2 >= 0)
		close(fd2);

	do_chown(des, "root", "log");
	return rc;
}

static void do_log_copy(char *mode, int dir, char* ts, int type)
{
	char destion[PATHMAX];
	struct stat info;

	if(type == APLOG_TYPE){
		if(stat(APLOG_FILE_0, &info) == 0){
			snprintf(destion,sizeof(destion), "%s%d/%s_%s_%s", CRASH_DIR, dir,strrchr(APLOG_FILE_0,'/')+1,mode,ts);
			do_copy(APLOG_FILE_0,destion, FILESIZE_MAX);
			if(info.st_size < 1*1024*1024){
				if(stat(APLOG_FILE_1, &info) == 0){
					snprintf(destion,sizeof(destion), "%s%d/%s_%s_%s", CRASH_DIR, dir,strrchr(APLOG_FILE_1,'/')+1,mode,ts);
					do_copy(APLOG_FILE_1,destion, FILESIZE_MAX);
				}
			}
		}
	}
	if(type == BPLOG_TYPE){
		if(stat(BPLOG_FILE_0, &info) == 0){
			snprintf(destion,sizeof(destion), "%s%d/%s_%s_%s%s", CRASH_DIR, dir,strrchr(BPLOG_FILE_0,'/')+1,mode,ts,".istp");
			do_copy(BPLOG_FILE_0,destion, FILESIZE_MAX);
			if(info.st_size < 1*1024*1024){
				if(stat(BPLOG_FILE_1, &info) == 0){
					snprintf(destion,sizeof(destion), "%s%d/%s_%s_%s%s", CRASH_DIR, dir,strrchr(BPLOG_FILE_1,'/')+1,mode,ts,".istp");
					do_copy(BPLOG_FILE_1,destion, FILESIZE_MAX);
				}
			}
		}
	}
	return ;
}

static mode_t get_mode(const char *s)
{
	mode_t mode = 0;
	while (*s) {
		if (*s >= '0' && *s <= '7') {
			mode = (mode << 3) | (*s - '0');
		} else {
			return -1;
		}
		s++;
	}
	return mode;
}

static int do_chmod(char *file, char *mod)
{
	mode_t mode = get_mode(mod);
	if (chmod(file, mode) < 0) {
		return -errno;
	}
	return 0;
}

static int write_file(const char *path, const char *value)
{
	int fd, ret, len;

	fd = open(path, O_WRONLY | O_CREAT, 0622);

	if (fd < 0)
		return -errno;

	len = strlen(value);

	do {
		ret = write(fd, value, len);
	} while (ret < 0 && errno == EINTR);

	close(fd);
	if (ret < 0) {
		return -errno;
	} else {
		return 0;
	}
}

static char *get_version_info(char *fn, char *field)
{

	char *data;
	int sz;
	int fd;
	int i = 0;
	char *info = NULL;
	int len;
	int p;

	data = 0;
	fd = open(fn, O_RDONLY);
	if (fd < 0)
		return 0;

	sz = lseek(fd, 0, SEEK_END);
	if (sz < 0)
		goto oops;

	if (lseek(fd, 0, SEEK_SET) != 0)
		goto oops;

	data = (char *)malloc(sz + 2);
	if (data == 0)
		goto oops;

	if (read(fd, data, sz) != sz)
		goto oops;

	data[sz] = '\n';
	data[sz + 1] = 0;

	while (i < sz) {
		if (data[i] == '=')
			if (i - strlen(field) > 0)
				if (!memcmp
				    (&data[i - strlen(field)], field,
				     strlen(field))) {
					p = ++i;
					while ((data[i] != '\n')
					       && (data[i] != 0))
						i++;
					len = i - p;
					if (len > 0) {
						info = (char *)malloc(len + 1);
						if (info == 0)
							goto oops;
						memcpy(info, &data[p], len);
						info[len] = 0;
						break;
					}
					/*else
					   goto oops; */

				}
		i++;
	}

oops:
	close(fd);
	if (data != 0)
		free(data);
	return info;

}

static int get_uptime(long long *time_ns)
{
	struct timespec ts;
	int fd, result;

	fd = open("/dev/alarm", O_RDONLY);
	if (fd < 0)
		return -1;

	result =
	    ioctl(fd,
		  ANDROID_ALARM_GET_TIME(ANDROID_ALARM_ELAPSED_REALTIME), &ts);
	close(fd);
	*time_ns = (((long long) ts.tv_sec) * 1000000000LL) + ((long long) ts.tv_nsec);
	return 0;
}

static void compute_key(char* key, char *event, char *type)
{
	SHA1_CTX sha;
	char buf[256] = { '\0', };
	long long time_ns=0;
	char *tmp_key = key;
        unsigned char results[SHA1_DIGEST_LENGTH];
	int i;

	get_uptime(&time_ns);
	snprintf(buf, 256, "%s%s%s%s%lld", buildVersion, uuid, event, type, time_ns);

	SHA1Init(&sha);
	SHA1Update(&sha, (unsigned char*) buf, strlen(buf));
	SHA1Final(results, &sha);
	for (i = 0; i < SHA1_DIGEST_LENGTH/2; i++)
	{
	  sprintf(tmp_key, "%02x", results[i]);
	  tmp_key+=2;
	}
	*tmp_key=0;
}

static void backup_apcoredump(unsigned int dir, char* name, char* path)
{
	char src[512] = { '\0', };
	char des[512] = { '\0', };
	snprintf(src, sizeof(src), "%s", path);
	snprintf(des, sizeof(des), "%s%d/%s", CRASH_DIR, dir, name);
	int status = do_copy(src, des, 0);
	if (status != 0)
		LOGE("backup ap core dump status: %d.\n",status);
	else
		remove(path);
}

static void build_footprint(char *id)
{
	char prop[PROPERTY_VALUE_MAX];

	/* footprint contains:
	* buildId
	* fingerPrint
	* kernelVersion
	* buildUserHostname
	* modemVersion
	* ifwiVersion
	* iafwVersion
	* scufwVersion
	* punitVersion
	* valhooksVersion */

	snprintf(id, SIZE_FOOTPRINT_MAX, "%s,", buildVersion);

	property_get(FINGERPRINT_FIELD, prop, "");
	strncat(id, prop, SIZE_FOOTPRINT_MAX);
	strncat(id, ",", SIZE_FOOTPRINT_MAX);

	property_get(KERNEL_FIELD, prop, "");
	strncat(id, prop, SIZE_FOOTPRINT_MAX);
	strncat(id, ",", SIZE_FOOTPRINT_MAX);

	property_get(USER_FIELD, prop, "");
	strncat(id, prop, SIZE_FOOTPRINT_MAX);
	strncat(id, "@", SIZE_FOOTPRINT_MAX);

	property_get(HOST_FIELD, prop, "");
	strncat(id, prop, SIZE_FOOTPRINT_MAX);
	strncat(id, ",", SIZE_FOOTPRINT_MAX);

	property_get(MODEM_FIELD, prop, "");
	strncat(id, prop, SIZE_FOOTPRINT_MAX);
	strncat(id, ",", SIZE_FOOTPRINT_MAX);

	property_get(IFWI_FIELD, prop, "");
	strncat(id, prop, SIZE_FOOTPRINT_MAX);
	strncat(id, ",", SIZE_FOOTPRINT_MAX);

	property_get(IAFW_VERSION, prop, "");
	strncat(id, prop, SIZE_FOOTPRINT_MAX);
	strncat(id, ",", SIZE_FOOTPRINT_MAX);

	property_get(SCUFW_VERSION, prop, "");
	strncat(id, prop, SIZE_FOOTPRINT_MAX);
	strncat(id, ",", SIZE_FOOTPRINT_MAX);

	property_get(PUNIT_VERSION, prop, "");
	strncat(id, prop, SIZE_FOOTPRINT_MAX);
	strncat(id, ",", SIZE_FOOTPRINT_MAX);

	property_get(VALHOOKS_VERSION, prop, "");
	strncat(id, prop, SIZE_FOOTPRINT_MAX);
}

static void analyze_crash(char* type, char* path, char* key, char* uptime)
{
	char cmd[512] = { '\0', };
	char footprint[SIZE_FOOTPRINT_MAX] = { '\0', };
	char imei[PROPERTY_VALUE_MAX];

	property_get(IMEI_FIELD, imei, "");

	build_footprint(footprint);

	snprintf(cmd, sizeof(cmd)-1, "/system/bin/analyze_crash %s %s %s %s %s %s %s", type, path, key, uptime, footprint, boardVersion, imei);
	int status = system(cmd);
	if (status != 0)
		LOGE("analyze crash status: %d.\n", status);
}

static void notify_crashreport()
{
	int status = system("am broadcast -n com.intel.crashreport/.NotificationReceiver -a com.intel.crashreport.intent.CRASH_NOTIFY -c android.intent.category.ALTERNATIVE");
	if (status != 0)
		LOGI("notify crashreport status: %d.\n", status);
}

static void history_file_write(char *event, char *type, char *subtype, char *log, char* lastuptime)
{
	char date_tmp[32];
	char uptime[32];
	struct stat info;
	long long tm=0;
	time_t t;
	int hours, seconds, minutes;
	FILE *to;
	char tmp[PATHMAX];
	char key[SHA1_DIGEST_LENGTH+1];
	char * p;

	// compute subtype
	if (!subtype)
		subtype = type;

	// compute uptime
	get_uptime(&tm);
	hours = (int) (tm / 1000000000LL);
	seconds = hours % 60;
	hours /= 60;
	minutes = hours % 60;
	hours /= 60;
	snprintf(uptime,sizeof(uptime),"%04d:%02d:%02d",hours, minutes,seconds);
	//compute ID
	compute_key(key, event, type);

	if (stat(HISTORY_FILE, &info) != 0) {
		to = fopen(HISTORY_FILE, "w");
		do_chown(HISTORY_FILE, "root", "log");
		fprintf(to, "#V1.0 %-16s%-24s\n", CURRENT_UPTIME, uptime);
		fprintf(to, "#EVENT  ID                    DATE                 TYPE\n");
		fclose(to);
	}

	if (log != NULL) {
		snprintf(tmp, sizeof(tmp), "%s", log);
		if((p = strrchr(tmp,'/'))){
			p[0] = '\0';
		}
		strftime(date_tmp, 32, "%Y-%m-%d/%H:%M:%S  ",
			 localtime((const time_t *)&info.st_mtime));
		date_tmp[31] = 0;
		to = fopen(HISTORY_FILE, "a");
		fprintf(to, "%-8s%-22s%-20s%s %s\n", event, key, date_tmp, type, tmp);
		fclose(to);
		LOGE("%-8s%-22s%-20s%s %s\n", event, key, date_tmp, type, tmp);
		if (!strncmp(event, CRASHEVENT, sizeof(CRASHEVENT)))
			analyze_crash(subtype, tmp, key, uptime);
	} else if (type != NULL) {
		time(&t);
		strftime(date_tmp, 32, "%Y-%m-%d/%H:%M:%S  ",
			 localtime((const time_t *)&t));
		date_tmp[31] = 0;

		to = fopen(HISTORY_FILE, "a");
		fprintf(to, "%-8s%-22s%-20s%-16s %s\n", event, key, date_tmp, type, lastuptime);
		fclose(to);
		LOGE("%-8s%-22s%-20s%s\n", event, key, date_tmp, type);
	} else {
		time(&t);
		strftime(date_tmp, 32, "%Y-%m-%d/%H:%M:%S  ",
			 localtime((const time_t *)&t));
		date_tmp[31] = 0;

		to = fopen(HISTORY_FILE, "a");
		fprintf(to, "%-8s%-22s%-20s%s\n", event, key, date_tmp, lastuptime);
		fclose(to);
		LOGE("%-8s%-22s%-20s%s\n", event, key, date_tmp, lastuptime);
	}
	return;
}

static int del_file_more_lines(char *fn)
{
	char *data;
	int sz;
	int fd, i;
	int count = 0;
	int tmp = 0;
	int dest = 0;
	data = 0;
	fd = open(fn, O_RDWR);
	if (fd < 0)
		return 0;

	sz = lseek(fd, 0, SEEK_END);
	if (sz < 0) {
		close(fd);
		return 0;
	}

	if (lseek(fd, 0, SEEK_SET) != 0) {
		close(fd);
		return 0;
	}

	data = (char *)malloc(sz + 2);
	if (data == 0) {
		close(fd);
		return 0;
	}

	if (read(fd, data, sz) != sz) {
		close(fd);
		if (data != 0)
			free(data);
		return 0;
	}

	close(fd);

	data[sz] = '\n';
	data[sz + 1] = 0;

	for (i = 0; i < sz; i++)
		if (data[i] == '\n')
			count++;

	if (count >= MAX_RECORDS + SAVEDLINES) {

		count = count - (MAX_RECORDS >> 1);
		for (i = 0; i < sz; i++) {
			if (data[i] == '\n') {
				tmp++;
				if (tmp == SAVEDLINES)
					dest = i;
				if (tmp >= count)
					break;
			}
		}
		memcpy(&data[dest + 1], &data[i + 1], sz - i - 1);
		fd = open(fn, O_RDWR | O_TRUNC);
		if (fd < 0) {
			free(data);
			return 0;
		}

		if (write(fd, &data[0], sz - i - 1 + dest + 1) !=
		    (sz - i - 1 + dest + 1)) {
			close(fd);
			free(data);
			return 0;
		}
		close(fd);
	}

	if (data != 0)
		free(data);
	return 0;
}

static void sdcard_exist(void)
{
	struct stat info;

	if (stat("/mnt/sdcard/data/logs", &info) == 0){
		CRASH_DIR = SDCARD_CRASH_DIR;
		STATS_DIR = SDCARD_STATS_DIR;
	} else {
		mkdir("/mnt/sdcard/data/", 0777);
		mkdir("/mnt/sdcard/data/logs", 0777);
		if (stat("/mnt/sdcard/data/logs", &info) == 0){
			CRASH_DIR = SDCARD_CRASH_DIR;
			STATS_DIR = SDCARD_STATS_DIR;
		} else {
			CRASH_DIR = EMMC_CRASH_DIR;
			STATS_DIR = EMMC_STATS_DIR;
		}
	}
	return;
}

#define CRASH_MODE 0
#define STATS_MODE 1
static unsigned int find_dir(unsigned int max, int mode)
{
	struct stat sb;
	char path[PATHMAX];
	unsigned int i, oldest = 0;
	FILE *fd;
	DIR *d;
	struct dirent *de;
	struct stat st;
	char *dir;

	sdcard_exist();

	if(mode == CRASH_MODE)
		snprintf(path, sizeof(path), CRASH_CURRENT_LOG);
	else
		snprintf(path, sizeof(path), STATS_CURRENT_LOG);
	if ((!stat(path, &sb))) {
		fd = fopen(path, "r");
		if (fscanf(fd, "%d", &i)==EOF) {
			i = 0;
		}
		fclose(fd);
		i = i % MAX_DIR;
		oldest = i++;
		fd = fopen(path, "w");
		fprintf(fd, "%d", (i % max));
		fclose(fd);
	} else {

		fd = fopen(path, "w");
		oldest = 0;
		fprintf(fd, "%d", 1);
		fclose(fd);
	}

	/* we didn't find an available file, so we clobber the oldest one */
	if(mode == CRASH_MODE)
		dir = CRASH_DIR;
	else
		dir = STATS_DIR;
	snprintf(path, sizeof(path),  "%s%d", dir, oldest);
	if (stat(path, &st) < 0)
		mkdir(path, 0777);
	else{
		d = opendir(path);
		if (d == 0) {
			LOGE("opendir failed, %s\n", strerror(errno));
			return -1;
		}
		while ((de = readdir(d)) != 0) {
			if (!strcmp(de->d_name, ".") || !strcmp(de->d_name, ".."))
				continue;
			snprintf(path, sizeof(path),  "%s%d/%s", dir, oldest,
				 de->d_name);
			remove(path);
		}
		if (closedir(d) < 0){
			LOGE("closedir failed, %s\n", strerror(errno));
			return -1;
		}
		rmdir(path);
		snprintf(path, sizeof(path),  "%s%d", dir, oldest);
		mkdir(path, 0777);
	}

	return oldest;
}

static void restart_profile_srv(void)
{
	char value[PROPERTY_VALUE_MAX];

	property_get(PROP_PROFILE, value, "");
	if (!strncmp(value, "1", 1)){
		property_set("ctl.start", "profile_svc_rest");
	}
}

static void init_profile_srv(void)
{
	char value[PROPERTY_VALUE_MAX];

	property_get(PROP_PROFILE, value, "");
	if (!strncmp(value, "1", 1)){
		property_set("ctl.start", "profile_svc_init");
	}
}

static int mv_modem_crash(char *spath, char *dpath)
{

	char src[512] = { '\0', };
	char des[512] = { '\0', };
	struct stat st;
	DIR *d;
	struct dirent *de;

	if (stat(spath, &st))
		return -1;
	if (stat(dpath, &st))
		return -1;

	d = opendir(spath);
	if (d == 0) {
		LOGE("opendir failed, %s\n", strerror(errno));
		return -1;
	}
	while ((de = readdir(d)) != 0) {
		if (!strcmp(de->d_name, ".") || !strcmp(de->d_name, ".."))
			continue;
		if (strstr(de->d_name, "cd") && strstr(de->d_name, ".tar.gz")){
			snprintf(src, sizeof(src), "%s/%s", spath, de->d_name);
			snprintf(des, sizeof(des), "%s/%s", dpath, de->d_name);
			do_copy(src, des, 0);
			remove(src);
		}
	}
	if (closedir(d) < 0){
		LOGE("closedir failed, %s\n", strerror(errno));
		return -1;
	}
	return 0;

}

struct wd_name {
	int wd;
	int mask;
	char *eventname;
	char *filename;
	char *cmp;
};

struct wd_name wd_array[] = {
	{0, IN_CLOSE_WRITE, CURRENT_UPTIME, "/data/logs/uptime", ""},
/* -------------------------above is file, below is dir---------------------------------------------------------------- */
	{0, IN_CLOSE_WRITE|IN_DELETE_SELF|IN_MOVE_SELF, MODEM_CRASH ,"/data/logs/modemcrash", "mpanic.txt"},/*for modem crash */
	{0, IN_CLOSE_WRITE|IN_DELETE_SELF|IN_MOVE_SELF, AP_INI_M_RST ,"/data/logs/modemcrash", "apimr.txt"},
	{0, IN_CLOSE_WRITE|IN_DELETE_SELF|IN_MOVE_SELF, M_RST_WN_COREDUMP ,"/data/logs/modemcrash", "mreset.txt"},
/* -------------------------above is modem, below is AP---------------------------------------------------------------- */
	{0, IN_MOVED_TO|IN_DELETE_SELF|IN_MOVE_SELF, SYSSERVER_WDT, "/data/system/dropbox", "system_server_watchdog"},
	{0, IN_MOVED_TO|IN_DELETE_SELF|IN_MOVE_SELF, ANR_CRASH, "/data/system/dropbox", "anr"},
	{0, IN_CLOSE_WRITE|IN_DELETE_SELF|IN_MOVE_SELF, TOMB_CRASH, "/data/tombstones", "tombstone"},
	{0, IN_MOVED_TO|IN_DELETE_SELF|IN_MOVE_SELF, JAVA_CRASH, "/data/system/dropbox", "crash"},
	{0, IN_CLOSE_WRITE|IN_DELETE_SELF|IN_MOVE_SELF, AP_COREDUMP ,"/data/logs/core", ".core"},
	{0, IN_MOVED_TO|IN_CLOSE_WRITE|IN_DELETE_SELF|IN_MOVE_SELF, LOST ,"/data/system/dropbox", ".lost"}, /* for full dropbox */
	{0, IN_CLOSE_WRITE|IN_DELETE_SELF|IN_MOVE_SELF, STATSTRIGGER, "/data/logs/stats", "trigger"},
};

int WDCOUNT = ((int)(sizeof(wd_array)/sizeof(struct wd_name)));

static int do_crashlogd(unsigned int files)
{
	int fd, fd1;
	int wd;
	char buffer[PATHMAX];
	char *offset = NULL;
	struct inotify_event *event;
	int len, tmp_len;
	int i = 0;
	char path[PATHMAX];
	char destion[PATHMAX];
	char date_tmp[32];
	struct stat info;
	unsigned int dir;
	long long tm;
	int hours, seconds, minutes;
	time_t t;
	char cmd[512] = { '\0', };

	fd = inotify_init();
	if (fd < 0) {
		LOGE("inotify_init failed, %s\n", strerror(errno));
		return 1;
	}

	for (i = 0; i < WDCOUNT; i++) {
		wd = inotify_add_watch(fd, wd_array[i].filename,
				       wd_array[i].mask);
		if (wd < 0) {
			LOGE("Can't add watch for %s.\n", wd_array[i].filename);
			return -1;
		}
		wd_array[i].wd = wd;
		LOGW("%s has been snooped\n", wd_array[i].filename);
	}

	while ((len = read(fd, buffer, sizeof(buffer)))) {
		offset = buffer;
		event = (struct inotify_event *)buffer;
		while (((char *)event - buffer) < len) {
			/* for dir to be delete */
			if((event->mask & IN_DELETE_SELF) ||(event->mask & IN_MOVE_SELF)){
				for (i = 0; i < WDCOUNT; i++) {
					if (event->wd != wd_array[i].wd)
						continue;
					mkdir(wd_array[i].filename, 0777);
					wd = inotify_add_watch(fd, wd_array[i].filename, wd_array[i].mask);
					if (wd < 0) {
						LOGE("Can't add watch for %s.\n", wd_array[i].filename);
						return -1;
						}
					wd_array[i].wd = wd;
					LOGW("%s has been deleted or moved, we watch it again.\n", wd_array[i].filename);
					}
			}
			if (!(event->mask & IN_ISDIR)) {
				for (i = 0; i < WDCOUNT; i++) {
					if (event->wd != wd_array[i].wd)
						continue;
					if(!event->len){
						/* for file */
						if (!memcmp(wd_array[i].filename,HISTORY_UPTIME,strlen(HISTORY_UPTIME))) {
							if (!get_uptime(&tm)) {
								hours = (int) (tm / 1000000000LL);
								seconds = hours % 60; hours /= 60;
								minutes = hours % 60; hours /= 60;
								snprintf(date_tmp,sizeof(date_tmp),"%04d:%02d:%02d",hours, minutes,seconds);
								snprintf(destion,sizeof(destion),"#V1.0 %-16s%-24s",wd_array[i].eventname,date_tmp);
								fd1 = open(HISTORY_FILE,O_RDWR);
								if (fd1 > 0) {
									write(fd1,destion,strlen(destion));
									close(fd1);
								}
								/*Update event every 12 hours*/
								if ((hours / UPTIME_HOUR_FREQUENCY) >= loop_uptime_event) {
									history_file_write(PER_UPTIME, NULL, NULL, NULL, date_tmp);
									del_file_more_lines(HISTORY_FILE);
									loop_uptime_event = (hours / UPTIME_HOUR_FREQUENCY) + 1;
									notify_crashreport();
								}
							}
						}
						break;
					}
					else{
						/* for modem reset */
						if(strstr(event->name, wd_array[i].cmp) && (strstr(event->name, "apimr.txt" ) ||strstr(event->name, "mreset.txt" ) )){
							dir = find_dir(files,CRASH_MODE);
							snprintf(path, sizeof(path),"%s/%s",wd_array[i].filename,event->name);
							if((stat(path, &info) == 0) && (info.st_size != 0)){
								snprintf(destion,sizeof(destion),"%s%d/%s", CRASH_DIR,dir,event->name);
								do_copy(path, destion, FILESIZE_MAX);
							}
							snprintf(destion,sizeof(destion),"%s%d/", CRASH_DIR,dir);
							time(&t);
							strftime(date_tmp, 32,"%Y%m%d%H%M%S",localtime((const time_t *)&t));
							date_tmp[31] = 0;
							usleep(20*1000);
							do_log_copy(wd_array[i].eventname,dir,date_tmp,APLOG_TYPE);
							do_log_copy(wd_array[i].eventname,dir,date_tmp,BPLOG_TYPE);
							history_file_write(CRASHEVENT, wd_array[i].eventname, NULL, destion, NULL);
							del_file_more_lines(HISTORY_FILE);
							notify_crashreport();
							break;
						}
						/* for modem crash */
						else if(strstr(event->name, wd_array[i].cmp) && strstr(event->name, "mpanic.txt" )){
							dir = find_dir(files,CRASH_MODE);
							snprintf(destion,sizeof(destion),"%s%d", CRASH_DIR,dir);
							int status = mv_modem_crash(wd_array[i].filename, destion);
							if (status != 0)
								LOGE("backup modem core dump status: %d.\n", status);
							snprintf(path, sizeof(path),"%s/%s",wd_array[i].filename,event->name);
							snprintf(destion,sizeof(destion),"%s%d/%s", CRASH_DIR,dir,event->name);
							do_copy(path, destion, 0);
							snprintf(destion,sizeof(destion),"%s%d/", CRASH_DIR,dir);

							time(&t);
							strftime(date_tmp, 32,"%Y%m%d%H%M%S",localtime((const time_t *)&t));
							date_tmp[31] = 0;
							usleep(20*1000);
							do_log_copy(wd_array[i].eventname,dir,date_tmp,APLOG_TYPE);
							do_log_copy(wd_array[i].eventname,dir,date_tmp,BPLOG_TYPE);
							history_file_write(CRASHEVENT, wd_array[i].eventname, NULL, destion, NULL);
							del_file_more_lines(HISTORY_FILE);
							notify_crashreport();
							break;
						}
						/* for full dropbox */
						else if(strstr(event->name, wd_array[i].cmp) && (strstr(event->name, ".lost" ))){
							char lostevent[32] = { '\0', };
							char lostevent_subtype[32] = { '\0', };
							if (strstr(event->name, "anr"))
								strcpy(lostevent, ANR_CRASH);
							else if (strstr(event->name, "crash"))
								strcpy(lostevent, JAVA_CRASH);
							else
								break;
							snprintf(lostevent_subtype, sizeof(lostevent_subtype), "%s_%s", LOST, lostevent);
							dir = find_dir(files,CRASH_MODE);
							snprintf(destion,sizeof(destion),"%s%d/",CRASH_DIR,dir);
							strftime(date_tmp, 32,"%Y%m%d%H%M%S",localtime((const time_t *)&info.st_mtime));
							date_tmp[31] = 0;
							usleep(20*1000);
							do_log_copy(lostevent,dir,date_tmp,APLOG_TYPE);
							history_file_write(CRASHEVENT, lostevent, lostevent_subtype, destion, NULL);
							del_file_more_lines(HISTORY_FILE);
							notify_crashreport();
							break;
						}
						/*for stat trigger*/
						else if(strstr(event->name, wd_array[i].cmp) && (strstr(event->name, "trigger" ))){
							char *p;
							char tmp[16];
							dir = find_dir(files,STATS_MODE);
							/*copy data file*/
							snprintf(tmp,sizeof(tmp),"%s",event->name);
                                                        p = strstr(tmp,"trigger");
							if ( p ){
								strcpy(p,"data");
								snprintf(path, sizeof(path),"%s/%s",wd_array[i].filename,tmp);
								snprintf(destion,sizeof(destion),"%s%d/%s", STATS_DIR,dir,tmp);
								do_copy(path, destion, 0);
								remove(path);
							}
							/*copy trigger file*/
							snprintf(path, sizeof(path),"%s/%s",wd_array[i].filename,event->name);
							snprintf(destion,sizeof(destion),"%s%d/%s", STATS_DIR,dir,event->name);
							do_copy(path, destion, 0);
							remove(path);
							snprintf(destion,sizeof(destion),"%s%d/", STATS_DIR,dir);
							history_file_write(STATSEVENT, tmp, NULL, destion, NULL);
							del_file_more_lines(HISTORY_FILE);
							notify_crashreport();
							break;
						}
						/* for other case */
						else if (strstr(event->name, wd_array[i].cmp)) {
							dir = find_dir(files,CRASH_MODE);

							snprintf(path, sizeof(path),"%s/%s",wd_array[i].filename,event->name);
							if (stat(path, &info) == 0) {
								strftime(date_tmp, 32,"%Y%m%d%H%M%S",localtime((const time_t *)&info.st_mtime));
								date_tmp[31] = 0;
								if (strstr(event->name, ".core" ))
									backup_apcoredump(dir, event->name, path);
								else
								{
									snprintf(destion,sizeof(destion),"%s%d/%s",CRASH_DIR,dir,event->name);
									do_copy(path, destion, FILESIZE_MAX);
								}
								snprintf(destion,sizeof(destion),"%s%d/",CRASH_DIR,dir);
								usleep(20*1000);
								do_log_copy(wd_array[i].eventname,dir,date_tmp,APLOG_TYPE);
								history_file_write(CRASHEVENT, wd_array[i].eventname, NULL, destion, NULL);
								del_file_more_lines(HISTORY_FILE);
								notify_crashreport();
							}

							/* for restart profile anr */
							if (strstr(event->name, "anr")){
								restart_profile_srv();
							}
							break;
						}
					}
				}
			}
			tmp_len = sizeof(struct inotify_event) + event->len;
			event = (struct inotify_event *)(offset + tmp_len);
			offset += tmp_len;
		}

	}

	return 0;
}

void do_timeup()
{
	int fd;

	while (1) {
		sleep(UPTIME_FREQUENCY);
		fd = open(HISTORY_UPTIME, O_RDWR | O_CREAT, 0666);
		close(fd);
	}
}

static int find_str_in_file(char *file, char *keyword, char *tail)
{
	char buffer[4*1024];
	int rc = 0;
	FILE *fd1;
	struct stat info;
	int brtw, brtr;
	char *p;
	int filelen,tmp,stringlen,buflen;

	if (stat(file, &info) < 0)
		return -1;

	if (keyword == NULL)
		return -1;

	fd1 = fopen(file,"r");
	if(fd1 == NULL)
		goto out_err;

	while(!feof(fd1)){
		if (fgets(buffer, sizeof(buffer), fd1) != NULL){
			if (keyword && strstr(buffer,keyword)){
				if (!tail){
					rc = 0;
					goto out;
				} else{
					int buflen = strlen(buffer);
					int str2len = strlen(tail);
					if ((buflen > str2len) && (!strncmp(&(buffer[buflen-str2len-1]), tail, strlen(tail)))){
						rc = 0;
						goto out;
					}
				}
			}
		}
	}

out_err:
	rc = -1;
out:
	if (fd1 != NULL)
		fclose(fd1);

	return rc;
}

struct fabric_type {
	char *keyword;
	char *tail;
	char *name;
};

struct fabric_type ft_array[] = {
	{"DW0:", "f501", "MEMERR"},
	{"DW0:", "f502", "INSTERR"},
	{"DW0:", "f504", "SRAMECCERR"},
	{"DW0:",   "dd", "HWWDTLOGERR"},
};

static void crashlog_check_fabric(char *reason, unsigned int files)
{
	char date_tmp[32];
	struct stat info;
	time_t t;
	char destion[PATHMAX];
	unsigned int dir;
	unsigned int i = 0;

	if ((stat(PROC_FABRIC_ERROR_NAME, &info) == 0)  || (test_flag == 1)) {

		time(&t);
		strftime(date_tmp, 32, "%Y%m%d%H%M%S",
			 localtime((const time_t *)&t));
		date_tmp[31] = '\0';
		dir = find_dir(files,CRASH_MODE);

		destion[0] = '\0';
		snprintf(destion, sizeof(destion), "%s%d/%s_%s.txt", CRASH_DIR, dir,
			 FABRIC_ERROR_NAME, date_tmp);
		do_copy(SAVED_FABRIC_ERROR_NAME, destion, FILESIZE_MAX);

		for (i = 0; i < sizeof(ft_array)/sizeof(struct fabric_type); i++){
			if (!find_str_in_file(destion, ft_array[i].keyword, ft_array[i].tail)){
				snprintf(destion,sizeof(destion),"%s%d/",CRASH_DIR,dir);
				history_file_write(CRASHEVENT, ft_array[i].name, NULL, destion, NULL);
				break;
			}
		}
		if (i == sizeof(ft_array)/sizeof(struct fabric_type)){
			snprintf(destion,sizeof(destion),"%s%d/",CRASH_DIR,dir);
			history_file_write(CRASHEVENT, FABRIC_ERROR, NULL, destion, NULL);
		}

		del_file_more_lines(HISTORY_FILE);
	}
	return;
}

static void crashlog_check_panic(char *reason, unsigned int files)
{
	char date_tmp[32];
	struct stat info;
	time_t t;
	char destion[PATHMAX];
	unsigned int dir;

	if ((stat(PANIC_CONSOLE_NAME, &info) == 0) || (test_flag == 1)) {

		time(&t);
		strftime(date_tmp, 32, "%Y%m%d%H%M%S",
			 localtime((const time_t *)&t));
		date_tmp[31] = '\0';
		dir = find_dir(files,CRASH_MODE);

		destion[0] = '\0';
		snprintf(destion, sizeof(destion), "%s%d/%s_%s.txt", CRASH_DIR, dir,
			 THREAD_NAME, date_tmp);
		do_copy(SAVED_THREAD_NAME, destion, FILESIZE_MAX);
		snprintf(destion,sizeof(destion),"%s%d/",CRASH_DIR,dir);

		destion[0] = '\0';
		snprintf(destion, sizeof(destion), "%s%d/%s_%s.txt", CRASH_DIR, dir,
			 CONSOLE_NAME, date_tmp);
		do_copy(SAVED_CONSOLE_NAME, destion, FILESIZE_MAX);

		destion[0] = '\0';
		snprintf(destion, sizeof(destion), "%s%d/%s_%s.txt", CRASH_DIR, dir,
			 LOGCAT_NAME, date_tmp);
		do_copy(SAVED_LOGCAT_NAME, destion, FILESIZE_MAX);

		write_file(PANIC_CONSOLE_NAME, "1");
		if (!find_str_in_file(SAVED_CONSOLE_NAME, "Kernel panic - not syncing: Kernel Watchdog", NULL))
			history_file_write(CRASHEVENT, KERNEL_FORCE_CRASH, NULL, destion, NULL);
		else
			history_file_write(CRASHEVENT, KERNEL_CRASH, NULL, destion, NULL);
		del_file_more_lines(HISTORY_FILE);
	}
	return;
}

static void crashlog_check_modem_shutdown(char *reason, unsigned int files)
{
	char date_tmp[32];
	struct stat info;
	time_t t;
	char destion[PATHMAX];
	unsigned int dir;

	if (stat(MODEM_SHUTDOWN_TRIGGER, &info) == 0) {

		time(&t);
		strftime(date_tmp, 32, "%Y%m%d%H%M%S",
			 localtime((const time_t *)&t));
		date_tmp[31] = '\0';
		dir = find_dir(files,CRASH_MODE);

		destion[0] = '\0';
		snprintf(destion, sizeof(destion), "%s%d/", CRASH_DIR, dir);

		do_log_copy(MODEM_SHUTDOWN, dir, date_tmp, APLOG_TYPE);
		history_file_write(CRASHEVENT, MODEM_SHUTDOWN, NULL, destion, NULL);
		del_file_more_lines(HISTORY_FILE);
		remove(MODEM_SHUTDOWN_TRIGGER);
	}
	return;
}

static void crashlog_check_startupreason(char *reason, unsigned int files)
{
	char date_tmp[32];
	time_t t;
	char destion[PATHMAX];
	unsigned int dir;

	if (strstr(reason, "WDT_RESET")) {

		time(&t);
		strftime(date_tmp, 32, "%Y%m%d%H%M%S",
			 localtime((const time_t *)&t));
		date_tmp[31] = '\0';

		dir = find_dir(files,CRASH_MODE);
		destion[0] = '\0';
		snprintf(destion, sizeof(destion), "%s%d/", CRASH_DIR, dir);

		usleep(20*1000);
		do_log_copy("WDT", dir, date_tmp, APLOG_TYPE);
		history_file_write(CRASHEVENT, "WDT", reason, destion, NULL);
		del_file_more_lines(HISTORY_FILE);
	}
	return;
}

static int file_read_value(const char *path, char *value, const char *default_value)
{
	struct stat info;
	FILE *fd;
	int ret = -1;

	if ( stat(path, &info) == 0 ) {
		fd = fopen(path, "r");
		ret = fscanf(fd, "%s", value);
		fclose(fd);
		if (ret == 1)
			return 0;
	}
	if (default_value) {
		strcpy(value, default_value);
		return ret;
	} else {
		return ret;
	}
}

static void write_uuid(char *uuid_value)
{
	FILE *fd;

	fd = fopen(LOG_UUID, "w");
	fprintf(fd, "%s", uuid_value);
	fclose(fd);
	do_chown(LOG_UUID, "root", "log");
}

static void read_uuid(void)
{
	char temp_uuid[256];
	struct stat info;
	FILE *fd;

	if (file_read_value(PROC_UUID, uuid, "Medfield") != 0) {
		write_uuid(uuid);
		LOGE("PROC_UUID error\n");
		return;
	}
	file_read_value(LOG_UUID, temp_uuid, "");
	if (strcmp(uuid, temp_uuid) != 0)
		write_uuid(uuid);
}

static int swupdated(char *buildname)
{
	struct stat info;
	FILE *fd;
	char currentbuild[PROPERTY_VALUE_MAX];

	if (stat(LOG_BUILDID, &info) == 0) {
		fd = fopen(LOG_BUILDID, "r");
		fscanf(fd, "%s", currentbuild);
		fclose(fd);

		if (strcmp(currentbuild, buildname)) {
			fd = fopen(LOG_BUILDID, "w");
			do_chown(LOG_BUILDID, "root", "log");
			fprintf(fd, "%s", buildname);
			fclose(fd);
			LOGI("Reset history after build update -> %s\n", buildname);
			return 1;
		}
	} else {
		fd = fopen(LOG_BUILDID, "w");
		do_chown(LOG_BUILDID, "root", "log");
		fprintf(fd, "%s", buildname);
		fclose(fd);
		LOGI("Reset history after blank device update -> %s\n", buildname);
		return 1;

	}
	return 0;
}


static void reset_history(void)
{
	FILE *to;
	int fd;

	to = fopen(HISTORY_FILE, "w");
	do_chown(HISTORY_FILE, "root", "log");
	fprintf(to, "#V1.0 %-16s%-24s\n", CURRENT_UPTIME, "0000:00:00");
	fprintf(to, "#EVENT  ID                    DATE                 TYPE\n");
	fclose(to);

	fd = open(HISTORY_UPTIME, O_RDWR | O_CREAT, 0666);
	if (fd < 0){
		LOGE("open HISTORY_UPTIME error\n");
	}
	close(fd);
}

static void reset_crashlog(void)
{
	char path[PATHMAX];
	FILE *fd;

	snprintf(path, sizeof(path), CRASH_CURRENT_LOG);
	fd = fopen(path, "w");
	fprintf(fd, "%d", 0);
	fclose(fd);
}
static void reset_statslog(void)
{
	char path[PATHMAX];
	FILE *fd;
	snprintf(path, sizeof(path), STATS_CURRENT_LOG);
	fd = fopen(path, "w");
	fprintf(fd, "%d", 0);
	fclose(fd);
}

static void uptime_history(char *lastuptime)
{
	FILE *to;
	int fd;
	struct stat info;

	char name[32];
	char date_tmp[32];

	to = fopen(HISTORY_FILE, "r");
	fscanf(to, "#V1.0 %16s%24s\n", name, lastuptime);
	fclose(to);
	if (!memcmp(name, CURRENT_UPTIME, sizeof(CURRENT_UPTIME))) {

		to = fopen(HISTORY_FILE, "r+");
		fprintf(to, "#V1.0 %-16s%-24s\n", CURRENT_UPTIME, "0000:00:00");
		strcpy(name, PER_UPTIME);
		fseek(to, 0, SEEK_END);
		strftime(date_tmp, 32, "%Y-%m-%d/%H:%M:%S  ", localtime((const time_t *)&info.st_mtime));
		date_tmp[31] = 0;
		fprintf(to, "%-8s00000000000000000000  %-20s%s\n", name, date_tmp, lastuptime);
		fclose(to);
	}
}

static void read_startupreason(char *startupreason)
{
	char cmdline[512] = { '\0', };
	char *p;
	unsigned int reason;
	char *bootmode_reason[] = {"BATT_INSERT", "PWR_BUTTON_PRESS", "RTC_TIMER", "USB_CHRG_INSERT", "Reserved", "COLD_RESET", "COLD_BOOT", "UNKNOWN", "SWWDT_RESET", "HWWDT_RESET"};
	struct stat info;
	FILE *fd;

	strcpy(startupreason, bootmode_reason[7]);

	if (stat(KERNEL_CMDLINE, &info) == 0) {
		fd = fopen(KERNEL_CMDLINE, "r");
		fread(cmdline, 1, sizeof(cmdline)-1, fd);
		fclose(fd);
		p = strstr(cmdline, STARTUP_STR);
		if(p) {
			reason=atoi(p+strlen(STARTUP_STR));
			if (reason < (sizeof(bootmode_reason)/sizeof(char*)))
				strcpy(startupreason, bootmode_reason[reason]);
		} else {
			p = strstr(cmdline, STARTUP_STR_OLD);
			if(p) {
				reason=atoi(p+strlen(STARTUP_STR_OLD));
				if (reason < (sizeof(bootmode_reason)/sizeof(char*)))
					strcpy(startupreason, bootmode_reason[reason]);
			}
		}
	}
}

int main(int argc, char **argv)
{

	int i;
	int ret = 0;
	unsigned int files = 0xFFFFFFFF;
	char date_tmp[32];
	struct stat info;
	time_t t;
	char destion[PATHMAX];
	char *vinfo;
	pid_t pid;
	unsigned int dir;

	pthread_t thread;
	char value[PROPERTY_VALUE_MAX];

	if (argc > 2) {
		LOGE("USAGE: %s [number] \n", argv[0]);
		return -1;
	}

	if (argc == 2) {
		if(!memcmp(argv[1], "-modem", 6)){
			WDCOUNT=4;
			LOGI(" crashlogd only snoop modem \n");
		}
		else if(!memcmp(argv[1], "-test", 5)){
			test_flag = 1;
		}
		else{
			errno = 0;
			files = (unsigned int)strtoul(argv[1], 0, 0);

			if (errno) {
				LOGE(" saved files number must be digital \n");
				return -1;
			}
		}
	}

	property_get(PROP_CRASH, value, "");
	if (strncmp(value, "1", 1)){
		if (stat(PANIC_CONSOLE_NAME, &info) == 0){
			write_file(PANIC_CONSOLE_NAME, "1");
		}
		return -1;
	}

	property_get(PROP_COREDUMP, value, "");
        if (!strncmp(value, "1", 1)){
		chmod(HISTORY_FILE_DIR,0777);
		chmod(HISTORY_CORE_DIR,0777);
        }
	else
	{
		chmod(HISTORY_FILE_DIR,0750);
                chmod(HISTORY_CORE_DIR,0750);
	}
	property_get(BUILD_FIELD, buildVersion, "");
	property_get(BOARD_FIELD, boardVersion, "");
	read_uuid();

	sdcard_exist();

	// check startup reason and sw update
	char startupreason[16] = { '\0', };
	struct stat st;
	char lastuptime[32];

	if (swupdated(buildVersion)) {
		strcpy(lastuptime, "0000:00:00");
		strcpy(startupreason,"SWUPDATE");
		reset_crashlog();
		reset_statslog();
		reset_history();
	}
	else {
		read_startupreason(startupreason);
		uptime_history(lastuptime);
	}

	crashlog_check_fabric(startupreason, files);
	crashlog_check_panic(startupreason, files);
	crashlog_check_modem_shutdown(startupreason, files);
	crashlog_check_startupreason(startupreason, files);

	history_file_write(SYS_REBOOT, startupreason, NULL, NULL, lastuptime);
	del_file_more_lines(HISTORY_FILE);
	notify_crashreport();

	ret = pthread_create(&thread, NULL, (void *)do_timeup, NULL);
	if (ret < 0) {
		LOGE("pthread_create error");
		return -1;
	}
	do_crashlogd(files);

	return 0;

}
