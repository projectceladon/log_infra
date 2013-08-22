LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := kdumpramdisk
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := ETC
LOCAL_MODULE_SUFFIX := .img
LOCAL_MODULE_PATH := $(TARGET_OUT)/lib/kdump
LOCAL_REQUIRED_MODULES := kdumpbzImage
include $(BUILD_SYSTEM)/base_rules.mk

LOCAL_BUILT_BUSYBOX := \
	$(call intermediates-dir-for,EXECUTABLES,busybox_binary)/busybox
$(LOCAL_BUILT_MODULE): BUILT_BUSYBOX := $(LOCAL_BUILT_BUSYBOX)
$(LOCAL_BUILT_MODULE): BUSYBOX_LINKS := \
	mount umount sh mdev mkdir echo touch cp init sleep sync ls
$(LOCAL_BUILT_MODULE): KDUMP_ROOT := $(PRODUCT_OUT)/kdump/root
LOCAL_KDUMP_ETC := $(LOCAL_PATH)/etc
$(LOCAL_BUILT_MODULE): KDUMP_ETC := $(LOCAL_KDUMP_ETC)

$(LOCAL_BUILT_MODULE): $(MKBOOTFS) $(MINIGZIP) $(LOCAL_BUILT_BUSYBOX) $(LOCAL_KDUMP_ETC)
	@echo "Building: $@"
	$(hide) mkdir -p $(KDUMP_ROOT)/{bin,data,proc,sys}
	$(hide) mkdir -p $(@D)
	$(hide) cp -rf $(KDUMP_ETC) $(KDUMP_ROOT)
	$(hide) cp -f $(BUILT_BUSYBOX) $(KDUMP_ROOT)/bin
	$(hide) for _lnk in $(BUSYBOX_LINKS); do \
				ln -sf busybox $(KDUMP_ROOT)/bin/$$_lnk; \
			done
	$(hide) ln -sf bin/busybox $(KDUMP_ROOT)/init
	$(hide) cat $(KDUMP_ETC)/profile \
			| awk '{ gsub(/"please use actual partition to replace it"/, "/bin/mount -t ext4 /dev/mmcblk0p9 /data"); print }' \
			> $(KDUMP_ROOT)/etc/profile
	$(MKBOOTFS) $(KDUMP_ROOT) | $(MINIGZIP) > $@

include $(CLEAR_VARS)
LOCAL_MODULE := kdumpbzImage
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := ETC
LOCAL_MODULE_PATH := $(TARGET_OUT)/lib/kdump
include $(BUILD_SYSTEM)/base_rules.mk

$(LOCAL_BUILT_MODULE): build_bzImage_kdump
	@echo "Copying: $@"
	@mkdir -p $(@D)
	$(hide) cp $(PRODUCT_OUT)/kdumpbzImage $@
