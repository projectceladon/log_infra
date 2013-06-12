.PHONY: kdumpramdisk
kdumpbootimage: busybox build_bzImage_kdump

kdumpramdisk: kdumpbootimage $(MKBOOTFS) $(MINIGZIP)
# kdumpramdisk: busybox
	mkdir -p $(PRODUCT_OUT)/kdump/root/bin
	mkdir -p $(PRODUCT_OUT)/kdump/root/data
	mkdir -p $(PRODUCT_OUT)/kdump/root/proc
	mkdir -p $(PRODUCT_OUT)/kdump/root/sys
	cp -rf $(TOP)/vendor/intel/tools/PRIVATE/log_infra/kdump_root/etc $(PRODUCT_OUT)/kdump/root/
	cp -f $(PRODUCT_OUT)/system/xbin/busybox $(PRODUCT_OUT)/kdump/root/bin
	pwd
	ln -sf busybox $(PRODUCT_OUT)/kdump/root/bin/mount
	ln -sf busybox $(PRODUCT_OUT)/kdump/root/bin/umount
	ln -sf busybox $(PRODUCT_OUT)/kdump/root/bin/sh
	ln -sf busybox $(PRODUCT_OUT)/kdump/root/bin/mdev
	ln -sf busybox $(PRODUCT_OUT)/kdump/root/bin/mkdir
	ln -sf busybox $(PRODUCT_OUT)/kdump/root/bin/echo
	ln -sf busybox $(PRODUCT_OUT)/kdump/root/bin/touch
	ln -sf busybox $(PRODUCT_OUT)/kdump/root/bin/cp
	ln -sf busybox $(PRODUCT_OUT)/kdump/root/bin/init
	ln -sf busybox $(PRODUCT_OUT)/kdump/root/bin/sleep
	ln -sf busybox $(PRODUCT_OUT)/kdump/root/bin/sync
	ln -sf busybox $(PRODUCT_OUT)/kdump/root/bin/ls
	ln -sf bin/busybox $(PRODUCT_OUT)/kdump/root/init
	cat $(TOP)/vendor/intel/tools/PRIVATE/log_infra/kdump_root/etc/profile |awk '{ gsub(/"please use actual partition to replace it"/, "/bin/mount -t ext4 /dev/mmcblk0p9 /data"); print }' > $(PRODUCT_OUT)/kdump/root/etc/profile
	$(MKBOOTFS) $(PRODUCT_OUT)/kdump/root | $(MINIGZIP) > $(PRODUCT_OUT)/kdump/kdumpramdisk.img
	mkdir -p $(PRODUCT_OUT)/system/lib/kdump
	cp $(PRODUCT_OUT)/kdump/kdumpramdisk.img $(PRODUCT_OUT)/system/lib/kdump/kdumpramdisk.img
	cp $(PRODUCT_OUT)/kdumpbzImage $(PRODUCT_OUT)/system/lib/kdump/kdumpbzImage
