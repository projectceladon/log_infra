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
ifeq ($(TARGET_ARCH),x86)
# build kdump host tool (bin-to-hex)
ifeq ($(HOST_OS),linux)
include $(CLEAR_VARS)
LOCAL_MODULE := bin-to-hex
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := util/bin-to-hex.c
include $(BUILD_HOST_EXECUTABLE)
endif

include $(LOCAL_PATH)/purgatory/arch/$(arch)/Makefile

purgatory_src_files := \
    purgatory/purgatory.c \
    purgatory/printf.c \
    purgatory/string.c \
    $($(arch)_PURGATORY_SRCS) \
    util_lib/sha256.c

# Build purgatory.ro
include $(CLEAR_VARS)
LOCAL_MODULE := purgatory-$(arch)
#LOCAL_NO_DEFAULT_COMPILER_FLAGS := true
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_PATH := $(TARGET_OUT_SHARED_LIBRARIES)/kdump
LOCAL_FORCE_STATIC_EXECUTABLE := true
LOCAL_NO_CRT := true
LOCAL_SRC_FILES := $(purgatory_src_files)
LOCAL_C_INCLUDES := \
    $(LOCAL_PATH)/purgatory/include \
    $(LOCAL_PATH)/purgatory/arch/$(arch)/include \
    $(LOCAL_PATH)/util_lib/include \
    $(LOCAL_PATH)/include

LOCAL_CFLAGS := \
    -O0  -DDEBUG -fno-strict-aliasing -Wall \
    -Wstrict-prototypes -fno-zero-initialized-in-bss \
    -fno-builtin -ffreestanding -fno-PIC -fno-PIE -fno-stack-protector
LOCAL_LDFLAGS := \
    $(LOCAL_CFLAGS) -Wl,--no-undefined -nostartfiles -nostdlib \
    -nodefaultlibs -e purgatory_start
include $(BUILD_EXECUTABLE)

# Build libutil.a
include $(CLEAR_VARS)
LOCAL_MODULE := libutil
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := \
    util_lib/compute_ip_checksum.c \
    util_lib/sha256.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/util_lib/include
include $(BUILD_STATIC_LIBRARY)

# Build kdump
include $(CLEAR_VARS)
LOCAL_MODULE := kdump-$(arch)
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := kdump/kdump.c
include $(BUILD_EXECUTABLE)


# Build vmcore-dmesg
include $(CLEAR_VARS)
LOCAL_MODULE := vmcore-dmesg-$(arch)
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := vmcore-dmesg/vmcore-dmesg.c
include $(BUILD_EXECUTABLE)


# Build kexec-tools
include $(CLEAR_VARS)
LOCAL_MODULE := kexec-tools-$(arch)
LOCAL_MODULE_TAGS := optional
LOCAL_NO_CRT := true

LOCAL_SRC_FILES := \
    kexec_test/kexec_test16.S \
    kexec_test/kexec_test.S

LOCAL_C_INCLUDES := \
    $(LOCAL_PATH)/include \
    $(LOCAL_PATH)/util_lib/include \
    $(LOCAL_PATH)/kexec/arch/$(arch)/libfdt

LOCAL_CFLAGS := -O0  -DDEBUG -DRELOC=0x10000
LOCAL_LDFLAGS := -melf_i386 -e _start -Ttext=0x1000

include $(BUILD_EXECUTABLE)

include $(LOCAL_PATH)/kexec/arch/$(arch)/Makefile

# Build kexec
include $(CLEAR_VARS)
LOCAL_MODULE := kexec-$(arch)
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := EXECUTABLES
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
    $($(arch)_KEXEC_SRCS)

kexec_intermediates := $(call intermediates-dir-for,EXECUTABLES,kexec-$(arch))
purgatory_intermediates := $(call intermediates-dir-for,EXECUTABLES,purgatory-$(arch))

GEN := $(kexec_intermediates)/purgatory.c
$(GEN) : PRIVATE_PURGATORY_GEN := $(addprefix $(purgatory_intermediates)/, $(patsubst %.S,%.o,$(purgatory_src_files:%.c=%.o)))
$(GEN) : PRIVATE_CUSTOM_TOOL = $(HOST_OUT_EXECUTABLES)/bin-to-hex purgatory < $(purgatory_intermediates)/purgatory-$(arch)-havestack > $@
$(GEN) : $(HOST_OUT_EXECUTABLES)/bin-to-hex purgatory-$(arch)
	$(TARGET_CXX) -m32 -nostdlib -Bstatic -o $(purgatory_intermediates)/purgatory-$(arch)-havestack \
		-L$(OUT)/obj/lib  -fno-strict-aliasing -Wall -Wstrict-prototypes -fno-zero-initialized-in-bss \
		-fno-builtin -ffreestanding  -fno-function-sections -fno-PIC -Wl,--no-undefined -nostartfiles \
		-nostdlib -nodefaultlibs -e purgatory_start -r  -Wl,--no-undefined \
		$(PRIVATE_PURGATORY_GEN) -Wl,--start-group $(TARGET_LIBGCC) -Wl,--end-group
	$(transform-generated-source)

LOCAL_REQUIRED_MODULES := purgatory-$(arch)
LOCAL_GENERATED_SOURCES := $(GEN)

LOCAL_C_INCLUDES := \
    $(LOCAL_PATH)/kexec/arch/$(arch)/include \
    $(LOCAL_PATH)/include \
    $(LOCAL_PATH)/util_lib/include \
    external/zlib

LOCAL_CFLAGS := -O0 -DDEBUG -fno-strict-aliasing -Wall -Wstrict-prototypes
LOCAL_STATIC_LIBRARIES := libutil
LOCAL_SHARED_LIBRARIES := libz libc

include $(BUILD_EXECUTABLE)

endif
