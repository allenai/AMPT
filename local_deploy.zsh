#!/usr/bin/env zsh
#
#  Copyright (c) 2021 The Allen Institute for Artificial Intelligence.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

FIJI_PLUGIN_DIR=/Applications/Fiji.app/plugins/jar

for AMPT in "${FIJI_PLUGIN_DIR}"/AMPT*.jar ; do
  echo "Removing ${AMPT}"
  rm -f "${AMPT}"
done

echo "Installing new jar(s)"
cp target/AMPT-*.*.*-all.jar "${FIJI_PLUGIN_DIR}"
