# Crashinfo
CRASHINFO_PATH := $(TOP)/device/intel/PRIVATE/log_infra/crashinfo

PRODUCT_COPY_FILES += \
	$(CRASHINFO_PATH)/crashinfo:system/bin/crashinfo
