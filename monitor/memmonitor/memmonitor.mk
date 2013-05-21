# memmonitor
MONITOR_DEBUG_PATH := $(TOP)/vendor/intel/tools/PRIVATE/log_infra/monitor/memmonitor

PRODUCT_COPY_FILES += $(MONITOR_DEBUG_PATH)/do_gather.sh:/system/bin/do_gather.sh
PRODUCT_COPY_FILES += $(MONITOR_DEBUG_PATH)/memmonitor_hours.sh:/system/bin/memmonitor_hours.sh
PRODUCT_COPY_FILES += $(MONITOR_DEBUG_PATH)/memmonitor_seconds.sh:/system/bin/memmonitor_seconds.sh
PRODUCT_COPY_FILES += $(MONITOR_DEBUG_PATH)/memmonitor.sh:/system/bin/memmonitor.sh
