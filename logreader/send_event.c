/* Copyright (C) Intel 2013
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <stdlib.h>
#include "log.h"
#include "send_event.h"

static const struct intent_elements intent_e[I_COUNT] = {
    I_KEYWORD(ACTION_INFO, AM_ACTION, "intel.intent.action.phonedoctor.REPORT_INFO")
    I_KEYWORD(ACTION_ERROR, AM_ACTION, "intel.intent.action.phonedoctor.REPORT_ERROR")
    I_KEYWORD(ACTION_STATS, AM_ACTION, "intel.intent.action.phonedoctor.REPORT_STATS")
    I_KEYWORD(EXTRA_TYPE, AM_EXTRA_STRING, "intel.intent.extra.phonedoctor.TYPE")
    I_KEYWORD(EXTRA_DATA0, AM_EXTRA_STRING, "intel.intent.extra.phonedoctor.DATA0")
    I_KEYWORD(EXTRA_DATA1, AM_EXTRA_STRING, "intel.intent.extra.phonedoctor.DATA1")
    I_KEYWORD(EXTRA_DATA2, AM_EXTRA_STRING, "intel.intent.extra.phonedoctor.DATA2")
    I_KEYWORD(EXTRA_DATA3, AM_EXTRA_STRING, "intel.intent.extra.phonedoctor.DATA3")
    I_KEYWORD(EXTRA_DATA4, AM_EXTRA_STRING, "intel.intent.extra.phonedoctor.DATA4")
    I_KEYWORD(EXTRA_DATA5, AM_EXTRA_STRING, "intel.intent.extra.phonedoctor.DATA5")
    I_KEYWORD(EXTRA_FILE, AM_EXTRA_STRING, "intel.intent.extra.phonedoctor.FILE")
};

void send_event(unsigned int action, const char* type, const char* data0,
                const char* data1, const char* data2, const char* data3,
                const char* data4, const char* data5, const char* file) {
    intent_t* i;

    if (action >= I_COUNT) {
        LOG_E("bad intent am type, must be in range [0-%d]: %d",
              I_COUNT, action);
        return;
    }
    if (ie_am_type(intent_e, action) != AM_ACTION) {
        LOG_E("bad intent am type, must be an action: %d:%s",
              action, ie_data(intent_e, action));
        return;
    }

    i = new_intent(action);
    if (type != NULL)
        intent_put_extra(i, I_EXTRA_TYPE, type);
    if (data0 != NULL)
        intent_put_extra(i, I_EXTRA_DATA0, data0);
    if (data1 != NULL)
        intent_put_extra(i, I_EXTRA_DATA1, data1);
    if (data2 != NULL)
        intent_put_extra(i, I_EXTRA_DATA2, data2);
    if (data3 != NULL)
        intent_put_extra(i, I_EXTRA_DATA3, data3);
    if (data4 != NULL)
        intent_put_extra(i, I_EXTRA_DATA4, data4);
    if (data5 != NULL)
        intent_put_extra(i, I_EXTRA_DATA5, data5);
    if (file != NULL)
        intent_put_extra(i, I_EXTRA_FILE, file);
    send_intent(intent_e, i);
    delete_intent(i);
}
