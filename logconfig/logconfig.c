#include <errno.h>
#include <string.h>
#include <cutils/log.h>
#include <cutils/sockets.h>
#include <cutils/properties.h>
#include <arpa/inet.h>
#include <fcntl.h>

#define SERVICE_NAME "logconfig"

#ifdef LOG_TAG
#undef LOG_TAG
#define LOG_TAG SERVICE_NAME
#endif

#define ACK "ACK\n"
#define BUFFER_SIZE 255

#define CMD_WRITE_FILE 0
#define CMD_SET_PROP 1

void reply_ack(int fd) {
  int ret = write(fd, ACK, 4);
  LOGD("Reply ACK");
  if (ret < 0) {
    LOGE("Write ACK failed, errno: %s\n", strerror(errno));
  }
}

int open_socket() {
  int s = android_get_control_socket(SERVICE_NAME);
  LOGD("Get socket\n");
  if (s < 0) {
    LOGE("android_get_control_socket(%s): %s\n", SERVICE_NAME, strerror(errno));
    exit(1);
  }
  if (listen(s, 4) < 0) {
    LOGE("listen(control socket): %s\n", strerror(errno));
    exit(1);
  }
  LOGD("Listen end\n");

  struct sockaddr addr;
  socklen_t alen = sizeof(addr);
  int fd = accept(s, &addr, &alen);
  if (fd < 0) {
    LOGE("accept(control socket): %s\n", strerror(errno));
    exit(1);
  }
  LOGD("Accept done\n");

  return fd;
}

void apply_set_prop_cmd(int socket) {
  union {
    int integer;
    char buf[sizeof(int)];
  } net_int;
  int prop_length = 0;
  int value_length = 0;
  char *prop;
  char *value;

  // read prop_length
  if (read(socket, net_int.buf, sizeof(int)) < 0) {
    LOGE("read failed: %s\n", strerror(errno));
    return;
  }
  prop_length = ntohl(net_int.integer);
  LOGD("prop_length : %d\n", prop_length);

  // read prop
  prop = malloc((prop_length + 1) * sizeof(char));
  if (read(socket, prop, prop_length) < 0) {
    LOGE("read failed: %s\n", strerror(errno));
    goto out_free_prop;
  }
  prop[prop_length] = '\0';
  LOGD("prop : %s", prop);

  // read value_length
  if (read(socket, net_int.buf, sizeof(int)) < 0) {
    LOGE("read failed: %s\n", strerror(errno));
    goto out_free_prop;
  }
  value_length = ntohl(net_int.integer);
  LOGD("value_length : %d", value_length);

  // read value
  value = malloc((value_length + 1) * sizeof(char));
  if (read(socket, value, value_length) < 0) {
    LOGE("read failed: %s\n", strerror(errno));
    goto out_free_value;
  }
  value[value_length] = '\0';
  LOGD("value : %s", value);

  // read end of command char
  char buf;
  if (read(socket, &buf, 1) < 0) {
    LOGE("read failed: %s\n", strerror(errno));
    goto out_free_value;
  } else if (buf == '\0') {
    LOGD("Cmd finished");
  } else {
    LOGE("Error reading write file command\n");
  }

  // set property
  if (property_set(prop, value) < 0) {
    LOGE("Could not set property %s:%s, err: %s\n",
         prop, value, strerror(errno));
  }

 out_free_value:
  free(value);
 out_free_prop:
  free(prop);
}

void apply_write_fs_cmd(int socket) {
  char append;
  union {
    int integer;
    char buf[sizeof(int)];
  } net_int;
  int path_length = 0;
  int value_length = 0;
  char *path;
  char *value;

  // read append
  if (read(socket, &append, 1) < 0) {
    LOGE("read failed: %s\n", strerror(errno));
    return;
  }
  LOGD("Append : %d\n", append);

  // read path_length
  if (read(socket, net_int.buf, sizeof(int)) < 0) {
    LOGE("read failed: %s\n", strerror(errno));
    return;
  }
  path_length = ntohl(net_int.integer);
  LOGD("path_length : %d\n", path_length);

  // read path
  path = malloc((path_length + 1) * sizeof(char));
  if (read(socket, path, path_length) < 0) {
    LOGE("read failed: %s\n", strerror(errno));
    goto out_free_path;
  }
  path[path_length] = '\0';
  LOGD("path : %s", path);

  // read value_length
  if (read(socket, net_int.buf, sizeof(int)) < 0) {
    LOGE("read failed: %s\n", strerror(errno));
    goto out_free_path;
  }
  value_length = ntohl(net_int.integer);
  LOGD("value_length : %d", value_length);

  // read value
  value = malloc((value_length + 1) * sizeof(char));
  if (read(socket, value, value_length) < 0) {
    LOGE("read failed: %s\n", strerror(errno));
    goto out_free_value;
  }
  value[value_length] = '\0';
  LOGD("value : %s", value);

  // read end of command char
  char buf;
  if (read(socket, &buf, 1) < 0) {
    LOGE("read failed: %s\n", strerror(errno));
    goto out_free_value;
  } else if (buf == '\0') {
    LOGD("Cmd finished");
  } else {
    LOGE("Error reading write file command\n");
  }

  // write to file
  int fd;
  fd = open(path, O_WRONLY | (O_APPEND && append));
  if (fd < 0) {
    LOGE("open file failed: %s\n", strerror(errno));
    goto out_free_value;
  }
  if (write(fd, value, value_length) < 0) {
    LOGE("write in file failed: %s\n", strerror(errno));
    goto out_free_value;
  }
  close(fd);

 out_free_value:
  free(value);
 out_free_path:
  free(path);
}

void apply_logconfig(int socket) {
  char buffer[BUFFER_SIZE];
  int s_read;

  for(;;) {
    LOGD("Read cmd\n");
    s_read = read(socket, buffer, 1);
    if (s_read < 0) {
      LOGE("read failed: %s\n", strerror(errno));
      return;
    } else if (s_read == 0) {
      LOGD("EOF\n");
      return;
    } else {
      if (buffer[0] == CMD_WRITE_FILE) {
        apply_write_fs_cmd(socket);
      } else if (buffer[0] == CMD_SET_PROP) {
        apply_set_prop_cmd(socket);
      } else {
        LOGE("Command not recognized: %d\n", buffer[0]);
      }
    }
  }
  return;
}

int main(int argc, char *argv[]) {
  int socket = open_socket();
  apply_logconfig(socket);
  return 0;
}
