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


#include "IAcsUiService.h"
#include <binder/IPCThreadState.h>
#include <binder/IServiceManager.h>
#include <binder/Parcel.h>
#include <string.h>

namespace android {

    class BpAcsUiService :
        public BpInterface<IAcsUiService>
    {
        public:
            BpAcsUiService(const sp<IBinder>& impl) :
                BpInterface<IAcsUiService>(impl) {
                }
            virtual int32_t record(const char* path) {
                Parcel data, reply;
                data.writeInterfaceToken(IAcsUiService::getInterfaceDescriptor());
                data.writeCString(path);
                remote()->transact(BnAcsUiService::RECORD, data, &reply);
                return reply.readInt32();
            }

            virtual int32_t replay(const char* path) {
                Parcel data, reply;
                data.writeInterfaceToken(IAcsUiService::getInterfaceDescriptor());
                data.writeCString(path);
                remote()->transact(BnAcsUiService::RECORD, data, &reply);
                return reply.readInt32();
            }

            virtual int32_t stop() {
                Parcel data, reply;
                data.writeInterfaceToken(IAcsUiService::getInterfaceDescriptor());
                remote()->transact(BnAcsUiService::STOP, data, &reply);
                return reply.readInt32();
            }

    };

    IMPLEMENT_META_INTERFACE(AcsUiService, K_ACS_UI_SERVICE_NAME);

    BnAcsUiService::BnAcsUiService() {
    }

    static void parseParcel(const Parcel& data, char* path)
    {
        const char* message = data.readCString();
        unsigned int i,j;

        for (i=8+8+((sizeof(K_ACS_UI_SERVICE_NAME)-1)*2), j=0;
                i<data.dataSize(); i+=2, j<K_MAX_PATH_LEN)
            path[j++]=data.data()[i];

        path[K_MAX_PATH_LEN]=0;

        LOGI("Path: %s", path);
    }

    status_t BnAcsUiService::onTransact(uint32_t code, const Parcel& data, Parcel* reply, uint32_t flags) {

        char path[K_MAX_PATH_LEN+1];
        status_t res;

        switch (code) {
            case RECORD:
                CHECK_INTERFACE(IAcsUiService, data, reply);
                parseParcel(data, path);
                res=record(path);
                reply->writeInt32(0);
                break;

            case REPLAY:
                CHECK_INTERFACE(IAcsUiService, data, reply);
                parseParcel(data, path);
                res=replay(path);
                reply->writeInt32(0);
                break;

            case STOP:
                CHECK_INTERFACE(IAcsUiService, data, reply);
                res = stop();
                reply->writeInt32(res);
                break;

            default:
                return BBinder::onTransact(code, data, reply, flags);
        }
        return NO_ERROR;
    }

}
