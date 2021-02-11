/*
 * Copyright Vulcan Inc. 2021
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.vulcan.vmlci.orca.calculator;

import com.cedarsoftware.util.io.JsonObject;
import com.cedarsoftware.util.io.JsonReader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;

/**
 * Manage the configuration for BaseCalculators.
 *
 * @see HashMap
 */
public class CalculatorConfig extends HashMap<String, CalculatorConfigItem> {

  /**
   * Construct a populated calculator configuration.
   *
   * @param config_path the name of configuration file.
   * @throws FileNotFoundException if the figuration file can't be found.
   */
  public CalculatorConfig(String config_path) throws FileNotFoundException {
    FileInputStream reader = new FileInputStream(config_path);
    HashMap<String, Object> args = new HashMap<>();
    args.put(JsonReader.USE_MAPS, true);
    Object[] loaded_items = (Object[]) JsonReader.jsonToJava(reader, args);

    // Process the loaded configuration.
    process_configuration(loaded_items);
  }

  /**
   * Convert the JSON representation to a collection of <code>CalculatorConfigItem</code>s.
   *
   * @param loaded_items the loaded configuration in hash of hashs form.
   */
  private void process_configuration(Object[] loaded_items) {
    for (Object raw_item : loaded_items) {
      final JsonObject<String, Object> json_item = (JsonObject<String, Object>) raw_item;
      int nParameters = ((Object[]) json_item.get("parameters")).length;
      Object[] parameters = new Object[nParameters];
      for (int i = 0; i < nParameters; i++) {
        final Object temp = ((Object[]) json_item.get("parameters"))[i];
        parameters[i] = temp.getClass().cast(temp);
      }
      final CalculatorConfigItem item =
          new CalculatorConfigItem(
              (String) json_item.get("target"), parameters, (String) json_item.get("function"));
      this.put(item.target, item);
    }
  }
}
