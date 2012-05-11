/* ACS UI Recorder
*
* Copyright (C) Intel 2012
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
* Author: Vincent Tinelli <vincent.tinelli@intel.com>
*/



#ifndef IACSUISERVICE_H_
#define IACSUISERVICE_H_

#include <utils/RefBase.h>
#include <utils/String8.h>
#include <binder/IInterface.h>
#include <binder/MemoryHeapBase.h>

#define K_ACS_UI_SERVICE_NAME "android.acs.ui"
#define K_MAX_PATH_LEN 1024

namespace android {

    class IAcsUiService :
        public IInterface
    {
        public:
            DECLARE_META_INTERFACE(AcsUiService);

            virtual int32_t record(const char* path) = 0;
            virtual int32_t replay(const char* path) = 0;
            virtual int32_t stop() = 0;
    };

    class BnAcsUiService :
        public BnInterface<IAcsUiService>
    {
        public:
            enum {
                RECORD= IBinder::FIRST_CALL_TRANSACTION,
                REPLAY,
                STOP,
            };

            BnAcsUiService();

            virtual status_t onTransact(uint32_t code,
                    const Parcel& data,
                    Parcel* reply,
                    uint32_t flags = 0);
    };

}

#endif
