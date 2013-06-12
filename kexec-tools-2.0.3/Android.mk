# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

LOCAL_PATH:= $(call my-dir)

ifeq ($(TARGET_ARCH),arm)
	arch := arm
else ifeq ($(TARGET_ARCH),x86)
	arch := i386
endif
ifdef arch

# build kdump host tool (bin-to-hex)
ifeq ($(HOST_OS),linux)
include $(CLEAR_VARS)
LOCAL_MODULE := bin-to-hex
LOCAL_MODULE_TAGS := eng

LOCAL_SRC_FILES := \
	util/bin-to-hex.c

include $(BUILD_HOST_EXECUTABLE)
endif


# Build purgatory.ro
include $(CLEAR_VARS)
LOCAL_MODULE := purgatory-$(arch)
LOCAL_MODULE_TAGS := eng
LOCAL_MODULE_PATH := $(TARGET_OUT_SHARED_LIBRARIES)/kdump
LOCAL_FORCE_STATIC_EXECUTABLE := true
LOCAL_NO_CRT := true

LOCAL_SRC_FILES := \
	purgatory/purgatory.c \
	purgatory/printf.c \
	purgatory/string.c \
	purgatory/arch/i386/entry32-16.S \
	purgatory/arch/i386/entry32-16-debug.S \
	purgatory/arch/i386/entry32.S \
	purgatory/arch/i386/setup-x86.S \
	purgatory/arch/i386/stack.S \
	purgatory/arch/i386/compat_x86_64.S \
	purgatory/arch/i386/purgatory-x86.c \
	purgatory/arch/i386/console-x86.c \
	purgatory/arch/i386/vga.c \
	purgatory/arch/i386/pic.c \
	purgatory/arch/i386/crashdump_backup.c \
	util_lib/sha256.c

LOCAL_C_INCLUDES := $(LOCAL_PATH)/purgatory/include \
	$(LOCAL_PATH)/purgatory/arch/i386/include \
	$(LOCAL_PATH)/util_lib/include \
	$(LOCAL_PATH)/include
LOCAL_CFLAGS := -O0  -DDEBUG -fno-strict-aliasing -Wall \
	-Wstrict-prototypes -fno-zero-initialized-in-bss \
	-fno-builtin -ffreestanding -fno-PIC -fno-PIE -fno-stack-protector
LOCAL_LDFLAGS := $(LOCAL_CFLAGS) -Wl,--no-undefined -nostartfiles -nostdlib \
	-nodefaultlibs -e purgatory_start

include $(BUILD_EXECUTABLE)


# Build libutil.a
include $(CLEAR_VARS)
LOCAL_MODULE := libutil
LOCAL_MODULE_TAGS := eng
LOCAL_SRC_FILES := \
	util_lib/compute_ip_checksum.c \
	util_lib/sha256.c

LOCAL_C_INCLUDES := $(LOCAL_PATH)/include \
	$(LOCAL_PATH)/util_lib/include \
	$(LOCAL_PATH)/kexec/arch/$(arch)/libfdt \
	$(LOCAL_PATH)/kexec/arch/$(arch)/include
LOCAL_CFLAGS := -O0  -DDEBUG -fno-strict-aliasing -Wall -Wstrict-prototypes

include $(BUILD_STATIC_LIBRARY)


# Build kdump
include $(CLEAR_VARS)
LOCAL_MODULE := kdump-$(arch)
LOCAL_MODULE_TAGS := eng
#LOCAL_NO_CRT := true

LOCAL_SRC_FILES := \
	kdump/kdump.c

LOCAL_C_INCLUDES := $(LOCAL_PATH)/include \
	$(LOCAL_PATH)/util_lib/include \
	$(LOCAL_PATH)/kexec/arch/$(arch)/libfdt
LOCAL_CFLAGS := -O0  -DDEBUG -fno-strict-aliasing -Wall -Wstrict-prototypes

include $(BUILD_EXECUTABLE)


# Build vmcore-dmesg
include $(CLEAR_VARS)
LOCAL_MODULE := vmcore-dmesg-$(arch)
LOCAL_MODULE_TAGS := eng
#LOCAL_NO_CRT := true

LOCAL_SRC_FILES := \
	vmcore-dmesg/vmcore-dmesg.c

LOCAL_C_INCLUDES := $(LOCAL_PATH)/include \
	$(LOCAL_PATH)/util_lib/include \
	$(LOCAL_PATH)/kexec/arch/$(arch)/libfdt
LOCAL_CFLAGS := -O0  -DDEBUG -fno-strict-aliasing -Wall -Wstrict-prototypes

include $(BUILD_EXECUTABLE)


# Build kexec-tools
#include $(CLEAR_VARS)
#LOCAL_MODULE := kexec-tools-$(arch)
#LOCAL_MODULE_TAGS := eng
#LOCAL_NO_CRT := true

#LOCAL_SRC_FILES := \
#	kexec_test/kexec_test16.S \
#	kexec_test/kexec_test.S

#LOCAL_C_INCLUDES := $(LOCAL_PATH)/include \
#	$(LOCAL_PATH)/util_lib/include \
#	$(LOCAL_PATH)/kexec/arch/$(arch)/libfdt
#LOCAL_CFLAGS := -O0  -DDEBUG -DRELOC=0x10000
#LOCAL_LDFLAGS := -melf_i386 -e _start -Ttext=0x1000

#include $(BUILD_EXECUTABLE)

# Build kexec
include $(CLEAR_VARS)
LOCAL_MODULE := kexec-$(arch)
LOCAL_MODULE_TAGS := eng
LOCAL_MODULE_CLASS := EXECUTABLES

intermediates:= $(local-intermediates-dir)
GEN := $(intermediates)/purgatory.c
$(GEN): PRIVATE_CUSTOM_TOOL = $(HOST_OUT_EXECUTABLES)/bin-to-hex purgatory < $(OUT)/obj/EXECUTABLES/purgatory-i386_intermediates/LINKED/purgatory-i386-havestack > $@
$(GEN): $(HOST_OUT_EXECUTABLES)/bin-to-hex purgatory-$(arch)
	prebuilts/gcc/linux-x86/x86/i686-linux-android-4.6/bin/i686-linux-android-g++ -m32 -nostdlib -Bstatic -o $(OUT)/obj/EXECUTABLES/purgatory-i386_intermediates/LINKED/purgatory-i386-havestack -L$(OUT)/obj/lib  -fno-strict-aliasing -Wall -Wstrict-prototypes -fno-zero-initialized-in-bss -fno-builtin -ffreestanding  -fno-function-sections -fno-PIC -Wl,--no-undefined -nostartfiles -nostdlib -nodefaultlibs -e purgatory_start -r  -Wl,--no-undefined $(OUT)/obj/EXECUTABLES/purgatory-i386_intermediates/purgatory/arch/i386/entry32-16.o $(OUT)/obj/EXECUTABLES/purgatory-i386_intermediates/purgatory/arch/i386/entry32-16-debug.o $(OUT)/obj/EXECUTABLES/purgatory-i386_intermediates/purgatory/arch/i386/entry32.o $(OUT)/obj/EXECUTABLES/purgatory-i386_intermediates/purgatory/arch/i386/setup-x86.o $(OUT)/obj/EXECUTABLES/purgatory-i386_intermediates/purgatory/arch/i386/stack.o $(OUT)/obj/EXECUTABLES/purgatory-i386_intermediates/purgatory/arch/i386/compat_x86_64.o        $(OUT)/obj/EXECUTABLES/purgatory-i386_intermediates/purgatory/purgatory.o $(OUT)/obj/EXECUTABLES/purgatory-i386_intermediates/purgatory/printf.o $(OUT)/obj/EXECUTABLES/purgatory-i386_intermediates/purgatory/string.o $(OUT)/obj/EXECUTABLES/purgatory-i386_intermediates/purgatory/arch/i386/purgatory-x86.o $(OUT)/obj/EXECUTABLES/purgatory-i386_intermediates/purgatory/arch/i386/console-x86.o $(OUT)/obj/EXECUTABLES/purgatory-i386_intermediates/purgatory/arch/i386/vga.o $(OUT)/obj/EXECUTABLES/purgatory-i386_intermediates/purgatory/arch/i386/pic.o $(OUT)/obj/EXECUTABLES/purgatory-i386_intermediates/purgatory/arch/i386/crashdump_backup.o $(OUT)/obj/EXECUTABLES/purgatory-i386_intermediates/util_lib/sha256.o       -Wl,--start-group prebuilts/gcc/linux-x86/x86/i686-linux-android-4.6/bin/../lib/gcc/i686-linux-android/4.6/libgcc.a -Wl,--end-group
	$(transform-generated-source)
LOCAL_GENERATED_SOURCES += $(GEN)

LOCAL_SRC_FILES := \
	kexec/kexec.c \
	kexec/ifdown.c \
	kexec/kexec-elf.c \
	kexec/kexec-elf-exec.c \
	kexec/kexec-elf-core.c \
	kexec/kexec-elf-rel.c \
	kexec/kexec-elf-boot.c \
	kexec/kexec-iomem.c \
	kexec/firmware_memmap.c \
	kexec/crashdump.c \
	kexec/crashdump-xen.c \
	kexec/phys_arch.c \
	kexec/kernel_version.c \
	kexec/lzma.c \
	kexec/zlib.c \
	kexec/proc_iomem.c \
	kexec/virt_to_phys.c \
	kexec/phys_to_virt.c \
	kexec/add_segment.c \
	kexec/add_buffer.c \
	kexec/arch_reuse_initrd.c \
	kexec/arch/i386/kexec-x86.c \
	kexec/arch/i386/kexec-x86-common.c \
	kexec/arch/i386/kexec-elf-x86.c \
	kexec/arch/i386/kexec-elf-rel-x86.c \
	kexec/arch/i386/kexec-bzImage.c \
	kexec/arch/i386/kexec-multiboot-x86.c \
	kexec/arch/i386/kexec-beoboot-x86.c \
	kexec/arch/i386/kexec-nbi.c \
	kexec/arch/i386/x86-linux-setup.c \
	kexec/arch/i386/crashdump-x86.c

LOCAL_C_INCLUDES := $(LOCAL_PATH)/include \
	$(LOCAL_PATH)/util_lib/include \
	$(LOCAL_PATH)/kexec/arch/$(arch)/libfdt \
	$(LOCAL_PATH)/kexec/arch/$(arch)/include \
	external/zlib
LOCAL_CFLAGS := -O0  -DDEBUG -fno-strict-aliasing -Wall -Wstrict-prototypes
LOCAL_STATIC_LIBRARIES := libutil libz libc

include $(BUILD_EXECUTABLE)

endif
