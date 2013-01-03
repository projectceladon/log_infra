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

/**
 * @file send_intent.h
 * @brief send_intent provide an API to send broadcast intents
 *
 * Send_intent construct an "am broadcast" command which is send
 * through system function.
 *
 * It provides a generic way to construct you own intent API.
 *
 * Include this file, and create an intent_elements array which
 * contains your own intent elements using I_KEYWORD macro.
 *
 * You could also provide a function to better structure your API and
 * manage the intent object life cycle.
 *
 * Implementation example is available in send_event.[h|c]
 */

/**
 * Define the different "am broadcast" options you could use to define
 * your intent elements.
 */
enum AM_TYPE{
    AM_ACTION, /**< define intent action */
    AM_EXTRA_STRING, /**< define intent extra string */
    AM_COUNT /**< am elements count, internal use only */
};

/**
 * Intent element definition structure.
 */
struct intent_elements {
    const char* name; /**< Element name, exported to your intent API users */
    unsigned int am_type; /**< AM_TYPE of the element */
    const char* data; /**< The full name of you element, usually with a package prefix */
};

/**
 * I_KEYWORD macro used to define your intent elements.
 *
 * Usage :
 * - First, define an enum which contains all your element names
 * starting with I_ and ending with I_COUNT. Define it in your .h,
 * this will make your exported API intent element names.
 * - Then, internally, define an intent_elements array of I_COUNT size
 * with each element consist of a I_KEYWORD.
 *   - name : element name, defined in the enum without I_
 *   - am_type : AM_TYPE (action, extra string, ...)
 *   - data : the full string name of your element
 */
#define I_KEYWORD(name, am_type, data)          \
    [ I_##name ] = { #name, am_type, data, },

/**
 * Macros used to refer to intent elements attributes. First argument
 * is the definition array, second is the element name as defined in
 * enum.
 */
#define ie_name(elms, ie) (elms[ie].name)
#define ie_am_type(elms, ie) (elms[ie].am_type)
#define ie_data(elms, ie) (elms[ie].data)

/**
 * Intent element object structure.
 */
typedef struct intent_elm intent_t;

/**
 * Create a new intent object based on the provided action
 *
 * @param action from the defined intent elements, must be an AM_ACTION
 * @return NULL if malloc failed, else a properly intent_t object with the provided action
 */
intent_t* new_intent(unsigned int action);

/**
 * Delete the provided intent object
 *
 * @param intent to delete
 */
void delete_intent(intent_t* intent);

/**
 * Add an extra to the provided intent object
 *
 * @param i the intent object to add extra to
 * @param key the intent extra name as defined in the intent elements and enum
 * @param value the data you want to provide
 */
void intent_put_extra(intent_t* i, unsigned int key, const char* value);

/**
 * Send the provided intent based on the intent elements provided too.
 *
 * @param elms the intent elements array, as defined before with I_KEYWORD
 * @param intent object to send
 */
void send_intent(const struct intent_elements* elms, intent_t* intent);
