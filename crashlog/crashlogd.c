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

#define KERNEL_CRASH "IPANIC"
#define ANR_CRASH "ANR"
#define JAVA_CRASH "JAVACRASH"
#define TOMB_CRASH "TOMBSTONE"
#define MODEM_CRASH "MPANIC"
#define CURRENT_UPTIME "CURRENTUPTIME"
#define PER_UPTIME "UPTIME"
#define SYS_REBOOT "REBOOT"
#define AP_INI_M_RST "APIMR"
#define M_RST_WN_COREDUMP "MRESET"
#define FABRIC_ERROR "FABRICERR"

#define FILESIZE_MAX  (10*1024*1024)
#define PATHMAX 512
#define BUILD_FIELD "ro.build.version.incremental"
#define PROP_CRASH "persist.service.crashlog.enable"
#define PROP_PROFILE "persist.service.profile.enable"
#define SYS_PROP "/system/build.prop"
#define SAVEDLINES  1
#define MAX_RECORDS 5000
#define HISTORY_FILE_DIR  "/data/logs"
#define APLOG_FILE_0        "/data/logs/aplog"
#define APLOG_FILE_1    "/data/logs/aplog.1"
#define BPLOG_FILE_0    "/data/logs/bplog"
#define BPLOG_FILE_1    "/data/logs/bplog.1"
#define APLOG_TYPE       0
#define BPLOG_TYPE       1
#define SDCARD_CRASH_DIR "/mnt/sdcard/data/logs/crashlog"
#define EMMC_CRASH_DIR "/data/logs/crashlog"
#define CURRENT_LOG "/data/logs/currentcrashlog"
#define HISTORY_FILE  "/data/logs/history_event"
#define HISTORY_UPTIME "/data/logs/uptime"
#define PANIC_CONSOLE_NAME "/proc/emmc_ipanic_console"
#define PROC_FABRIC_ERROR_NAME "/proc/ipanic_fabric_err"
#define SAVED_CONSOLE_NAME "/data/dontpanic/emmc_ipanic_console"
#define SAVED_THREAD_NAME "/data/dontpanic/emmc_ipanic_threads"
#define SAVED_FABRIC_ERROR_NAME "/data/dontpanic/ipanic_fabric_err"
#define CONSOLE_NAME "emmc_ipanic_console"
#define THREAD_NAME "emmc_ipanic_threads"
#define FABRIC_ERROR_NAME "ipanic_fabric_err"

char *CRASH_DIR = NULL;

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

	if (chown(file, decode_uid(uid), decode_uid(gid)))
		return -errno;

	return 0;
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

static int get_uptime(int *time)
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
	*time = ts.tv_sec;
	return 0;
}

static int history_file_write(char *type, char *log, char *reason)
{
	char date_tmp[32];
	struct stat info;
	time_t t;
	FILE *to;
	char tmp[PATHMAX];
	char * p;
	int tm=0;
	int seconds, minutes;

	if (stat(HISTORY_FILE, &info) != 0) {
		get_uptime(&tm);
		seconds = tm % 60;
		tm /= 60;
		minutes = tm % 60;
		tm /= 60;
		snprintf(date_tmp,sizeof(date_tmp),"%04d:%02d:%02d",tm, minutes,seconds);
		to = fopen(HISTORY_FILE, "w");
		fprintf(to, "%-16s%-24s\n", CURRENT_UPTIME, date_tmp);
		fclose(to);
	}

	if ((log != NULL) && stat(log, &info) == 0) {
		snprintf(tmp, sizeof(tmp), "%s", log);
		if((p = strrchr(tmp,'/'))){
			p[0] = '\0';
		}
		strftime(date_tmp, 32, "%Y-%m-%d %H:%M:%S  ",
			 localtime((const time_t *)&info.st_mtime));
		date_tmp[31] = 0;
		to = fopen(HISTORY_FILE, "a");
		fprintf(to, "%-16s%-24s%s\n", type, date_tmp, tmp);
		fclose(to);
		LOGE("%-16s%-24s%s\n", type, date_tmp, log);
	} else {
		time(&t);
		strftime(date_tmp, 32, "%Y-%m-%d %H:%M:%S  ",
			 localtime((const time_t *)&t));
		date_tmp[31] = 0;
		to = fopen(HISTORY_FILE, "a");
		fprintf(to, "%-16s%-24s%s\n", type, date_tmp, reason);
		fclose(to);
		LOGE("%-16s%-24s%s\n", type, date_tmp, reason);
	}
	return 0;
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

	if (stat("/mnt/sdcard/data/logs", &info) == 0)
		CRASH_DIR = SDCARD_CRASH_DIR;
	else{
		mkdir("/mnt/sdcard/data/", 0777);
		mkdir("/mnt/sdcard/data/logs", 0777);
		if (stat("/mnt/sdcard/data/logs", &info) == 0)
			CRASH_DIR = SDCARD_CRASH_DIR;
		else
			CRASH_DIR = EMMC_CRASH_DIR;
	}
	return;
}

static unsigned int find_crash_dir(unsigned int max)
{
	struct stat sb;
	char path[PATHMAX];
	unsigned int i, oldest = 0;
	FILE *fd;
	DIR *d;
	struct dirent *de;
	struct stat st;

	sdcard_exist();

	snprintf(path, sizeof(path), CURRENT_LOG);
	if ((!stat(path, &sb))) {
		fd = fopen(path, "r");
		fscanf(fd, "%d", &i);
		fclose(fd);
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
	snprintf(path, sizeof(path),  "%s%d", CRASH_DIR, oldest);
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
			snprintf(path, sizeof(path),  "%s%d/%s", CRASH_DIR, oldest,
				 de->d_name);
			remove(path);
		}
		if (closedir(d) < 0){
			LOGE("closedir failed, %s\n", strerror(errno));
			return -1;
		}
		rmdir(path);
		snprintf(path, sizeof(path),  "%s%d", CRASH_DIR, oldest);
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


struct wd_name {
	int wd;
	int mask;
	char *eventname;
	char *filename;
	char *cmp;
};

struct wd_name wd_array[] = {
	{0, IN_MOVED_TO|IN_DELETE_SELF|IN_MOVE_SELF, ANR_CRASH, "/data/system/dropbox", "anr"},
	{0, IN_CLOSE_WRITE|IN_DELETE_SELF|IN_MOVE_SELF, TOMB_CRASH, "/data/tombstones", "tombstone"},
	{0, IN_MOVED_TO|IN_DELETE_SELF|IN_MOVE_SELF, JAVA_CRASH, "/data/system/dropbox", "crash"},
	{0, IN_CLOSE_WRITE|IN_DELETE_SELF|IN_MOVE_SELF, MODEM_CRASH ,"/data/logs/modemcrash", ".tar.gz"},/*for modem crash */
	{0, IN_CLOSE_WRITE|IN_DELETE_SELF|IN_MOVE_SELF, AP_INI_M_RST ,"/data/logs/modemcrash", "apimr.txt"},
	{0, IN_CLOSE_WRITE|IN_DELETE_SELF|IN_MOVE_SELF, M_RST_WN_COREDUMP ,"/data/logs/modemcrash", "mreset.txt"},
/* -------------------------above is dir, below is file---------------------------------------------------------------- */
	{0, IN_CLOSE_WRITE, CURRENT_UPTIME, "/data/logs/uptime", ""},
};

#define WDCOUNT ((int)(sizeof(wd_array)/sizeof(struct wd_name)))

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
	int hours;
	int seconds, minutes;
	time_t t;

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
		LOGE("%s has been snooped\n", wd_array[i].filename);
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
					LOGE("%s has been deleted or moved, we watch it again.\n", wd_array[i].filename);
					}
			}
			if (!(event->mask & IN_ISDIR)) {
				for (i = 0; i < WDCOUNT; i++) {
					if (event->wd != wd_array[i].wd)
						continue;
					if(!event->len){
						/* for file */
						if (!memcmp(wd_array[i].filename,HISTORY_UPTIME,strlen(HISTORY_UPTIME))) {
							if (!get_uptime(&hours)) {
								seconds = hours % 60; hours /= 60;
								minutes = hours % 60; hours /= 60;
								snprintf(date_tmp,sizeof(date_tmp),"%04d:%02d:%02d",hours, minutes,seconds);
								snprintf(destion,sizeof(destion),"%-16s%-24s\n",wd_array[i].eventname,date_tmp);
								fd1 = open(HISTORY_FILE,O_RDWR);
								if (fd1 > 0) {
									write(fd1,destion,strlen(destion));
									close(fd1);
								}
							}
						}
						break;
					}
					else{
						/* for modem reset */
						if(strstr(event->name, wd_array[i].cmp) && (strstr(event->name, "apimr.txt" ) ||strstr(event->name, "mreset.txt" ) )){
							dir = find_crash_dir(files);
							snprintf(path, sizeof(path),"%s/%s",wd_array[i].filename,event->name);
							if((stat(path, &info) == 0) && (info.st_size != 0)){
								snprintf(destion,sizeof(destion),"%s%d/%s", CRASH_DIR,dir,event->name);
								do_copy(path, destion, FILESIZE_MAX);
								history_file_write(wd_array[i].eventname,destion, 0);
							}
							else{
								snprintf(destion,sizeof(destion),"%s%d/", CRASH_DIR,dir);
								history_file_write(wd_array[i].eventname,destion, 0);
							}

							time(&t);
							strftime(date_tmp, 32,"%Y%m%d%H%M%S",localtime((const time_t *)&t));
							date_tmp[31] = 0;
							usleep(20*1000);
							do_log_copy(wd_array[i].eventname,dir,date_tmp,APLOG_TYPE);

							del_file_more_lines(HISTORY_FILE);
							do_log_copy(wd_array[i].eventname,dir,date_tmp,BPLOG_TYPE);
							break;
						}
						/* for other case */
						else if (strstr(event->name, wd_array[i].cmp)) {
							/* for restart profile anr */
							if (strstr(event->name, "anr")){
								restart_profile_srv();
							}

							dir = find_crash_dir(files);
							snprintf(path, sizeof(path),"%s/%s",wd_array[i].filename,event->name);
							if (stat(path, &info) == 0) {
								strftime(date_tmp, 32,"%Y%m%d%H%M%S",localtime((const time_t *)&info.st_mtime));
								date_tmp[31] = 0;
								snprintf(destion,sizeof(destion),"%s%d/%s",CRASH_DIR,dir,event->name);
								do_copy(path, destion, FILESIZE_MAX);
								history_file_write(wd_array[i].eventname,destion, 0);
								usleep(20*1000);
								do_log_copy(wd_array[i].eventname,dir,date_tmp,APLOG_TYPE);
								del_file_more_lines(HISTORY_FILE);
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
		sleep(5 * 60);
		fd = open(HISTORY_UPTIME, O_RDWR | O_CREAT, 0666);
		close(fd);
	}
}

static void crashlog_check_fabric(unsigned int files)
{
	char date_tmp[32];
	struct stat info;
	time_t t;
	char destion[PATHMAX];
	unsigned int dir;

	if (stat(PROC_FABRIC_ERROR_NAME, &info) == 0) {

		time(&t);
		strftime(date_tmp, 32, "%Y%m%d%H%M%S",
			 localtime((const time_t *)&t));
		date_tmp[31] = '\0';
		dir = find_crash_dir(files);

		destion[0] = '\0';
		snprintf(destion, sizeof(destion), "%s%d/%s_%s.txt", CRASH_DIR, dir,
			 FABRIC_ERROR_NAME, date_tmp);
		do_copy(SAVED_FABRIC_ERROR_NAME, destion, FILESIZE_MAX);
		history_file_write(FABRIC_ERROR, destion, 0);
		do_log_copy(FABRIC_ERROR,dir,date_tmp,APLOG_TYPE);
	}
	return;
}

static void crashlog_check_panic(unsigned int files)
{
	char date_tmp[32];
	struct stat info;
	time_t t;
	char destion[PATHMAX];
	unsigned int dir;

	if (stat(PANIC_CONSOLE_NAME, &info) == 0) {

		time(&t);
		strftime(date_tmp, 32, "%Y%m%d%H%M%S",
			 localtime((const time_t *)&t));
		date_tmp[31] = '\0';
		dir = find_crash_dir(files);

		destion[0] = '\0';
		snprintf(destion, sizeof(destion), "%s%d/%s_%s.txt", CRASH_DIR, dir,
			 THREAD_NAME, date_tmp);
		do_copy(SAVED_THREAD_NAME, destion, FILESIZE_MAX);
		history_file_write(KERNEL_CRASH, destion, 0);
		do_chown(destion, "root", "log");
		do_chown(destion, "root", "log");

		destion[0] = '\0';
		snprintf(destion, sizeof(destion), "%s%d/%s_%s.txt", CRASH_DIR, dir,
			 CONSOLE_NAME, date_tmp);
		do_copy(SAVED_CONSOLE_NAME, destion, FILESIZE_MAX);
		do_chown(destion, "root", "log");
		do_chown(destion, "root", "log");

		write_file(PANIC_CONSOLE_NAME, "1");
	}
	return;
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
	FILE *to;
	int fd;
	pthread_t thread;
	char value[PROPERTY_VALUE_MAX];

	if (argc > 2) {
		LOGE("USAGE: %s [number] \n", argv[0]);
		return -1;
	}

	if (argc == 2) {
		errno = 0;
		files = (unsigned int)strtoul(argv[1], 0, 0);

		if (errno) {
			LOGE(" saved files number must be digital \n");
			return -1;
		}

	}

	property_get(PROP_CRASH, value, "");
	if (strncmp(value, "1", 1)){
		if (stat(PANIC_CONSOLE_NAME, &info) == 0){
			write_file(PANIC_CONSOLE_NAME, "1");
		}
		return -1;
	}

/* mkdir -p HISTORY_FILE_DIR */
	if (mkdir(HISTORY_FILE_DIR, 0777) < 0) {
		if (errno != EEXIST)
			return errno;
	}

	sdcard_exist();

/* uptime record */
	if (stat(HISTORY_FILE, &info) != 0) {

		to = fopen(HISTORY_FILE, "w");
		fprintf(to, "%-16s%-24s\n", CURRENT_UPTIME, "0000:00:00");
		fclose(to);

	} else {
		char name[32];
		char uptime[32];
		to = fopen(HISTORY_FILE, "r");
		fscanf(to, "%16s%24s\n", name, uptime);
		fclose(to);
		if (!memcmp(name, CURRENT_UPTIME, sizeof(CURRENT_UPTIME))) {

			to = fopen(HISTORY_FILE, "r+");
			fprintf(to, "%-16s%-24s\n", CURRENT_UPTIME,
				"0000:00:00");
			strcpy(name, PER_UPTIME);
			fseek(to, 0, SEEK_END);
			fprintf(to, "%-16s%-24s\n", name, uptime);
			fclose(to);
		}

	}

	value[0] = '\0';
	property_get(BUILD_FIELD, value, "");
	history_file_write(SYS_REBOOT, NULL, value);

	crashlog_check_fabric(files);
	crashlog_check_panic(files);

	fd = open(HISTORY_UPTIME, O_RDWR | O_CREAT, 0666);
	if (fd < 0){
		LOGE("open HISTORY_UPTIME error\n");
		return -1;
		}
	close(fd);

	ret = pthread_create(&thread, NULL, (void *)do_timeup, NULL);
	if (ret < 0) {
		LOGE("pthread_create error");
		return -1;
	}
	do_crashlogd(files);

	return 0;

}
