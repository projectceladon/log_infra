#!/sbin/ash
#
#
# Copyright (C) Intel 2010
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
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

if [ $# -eq 1 ] ; then
        dir="/data/logs/crashlog"
        num=$1
        tmp=$dir$num
        echo $tmp
        cd /data/logs
        rm -r $tmp

else

  echo "USAGE: del_log number"

fi

