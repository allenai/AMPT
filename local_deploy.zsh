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

FIJI_ROOT=${FIJI_HOME:=/Applications/Fiji.app}
FIJI_PLUGIN_DIR=${FIJI_ROOT}/plugins/jar

if [[ ! -d "${FIJI_ROOT}" ]]; then
  echo "${FIJI_ROOT} does not exist, or is not a directory." >&2
  echo "Please set FIJI_HOME to the root directory of your Fiji Install." >&2
  else
    mkdir -pv "${FIJI_PLUGIN_DIR}"
fi

OLD_AMPT_JARS=("${FIJI_PLUGIN_DIR}"/AMPT*.jar(N))
for AMPT in $OLD_AMPT_JARS; do
  echo "Removing ${AMPT}">&2
  rm -f "${AMPT}"
done

NEW_BUILD="AMPT-$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout).jar"
echo "Installing: ${NEW_BUILD}" >&2
cp -v "target/${NEW_BUILD}" "${FIJI_PLUGIN_DIR}" >&2
