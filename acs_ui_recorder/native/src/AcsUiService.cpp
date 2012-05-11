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


#include "AcsUiService.h"
#include <binder/IPCThreadState.h>
#include <binder/IServiceManager.h>
#include <binder/Parcel.h>
#include <utils/Log.h>
#include <cutils/properties.h>
#include <sys/system_properties.h>
#include <sys/stat.h>
#include <sys/types.h>

namespace android {

    void AcsUiService::instantiate()
    {
        if(defaultServiceManager()->addService(String16("AcsUiService"),
                    new AcsUiService()) != NO_ERROR)
            LOGE("AcsUiService not added to service managererror");
    }

    AcsUiService::AcsUiService() {
    }

    AcsUiService::~AcsUiService() {
    }

    int32_t AcsUiService::record(const char* path) {
        int32_t status=1;

        LOGI("AcsUiService::record(%s)", path);

        if (mkdir(path, S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH) == 0) {
            property_set("acs.ui_recorder.base_path", path);
            property_set("acs.ui_recorder.record_run", "start");
            sleep(2);
            status=0;
        }
        return status;
    }

    int32_t AcsUiService::replay(const char* path) {
        LOGI("AcsUiService::replay(%s)", path);

        //property_set("acs.ui_recorder.base_path", path);
        property_set("acs.ui_recorder.replay_run", "start");

        sleep(3);

        return 0;
    }

    int32_t AcsUiService::stop() {
        LOGI("AcsUiService::stop");

        char status[PROPERTY_VALUE_MAX];

        /* RECORD*/
        if(__system_property_find("init.svc.acs_ui_record"))
        {
            __system_property_get("init.svc.acs_ui_record", status);

            if (status[0]=='r')
            {
                property_set("acs.ui_recorder.record", 0);
                sleep(3);
                property_set("acs.ui_recorder.record_run", "stop");
            }
        }

        /*REPLAY*/
        if(__system_property_find("init.svc.acs_ui_replay"))
        {
            __system_property_get("init.svc.acs_ui_replay", status);

            if (status[0]=='r')
                property_set("acs.ui_recorder.replay_run", "stop");
        }

        return 0;
    }

}

using namespace android;

int main(int argc, char** argv)
{
    sp<ProcessState> processState(ProcessState::self());
    sp<IServiceManager> sm = defaultServiceManager();
    AcsUiService::instantiate();
    ProcessState::self()->startThreadPool();
    IPCThreadState::self()->joinThreadPool();
    return 0;
}
