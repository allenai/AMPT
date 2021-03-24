/*
 *  Copyright (c) 2021 Vulcan Inc. All rights reserved.
 *  Licensed under the Apache 2.0 license. See LICENSE file in the project root for full license information.
 */

package com.vulcan.vmlci.orca.calculator;

import com.cedarsoftware.util.io.JsonObject;
import com.cedarsoftware.util.io.JsonReader;
import com.vulcan.vmlci.orca.helpers.ConfigurationFileLoadException;

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
   * @throws ConfigurationFileLoadException if the figuration file can't be found.
   */
  public CalculatorConfig(String config_path) throws ConfigurationFileLoadException {
    final FileInputStream reader;
    try {
      reader = new FileInputStream(config_path);
    } catch (final FileNotFoundException e) {
      throw new ConfigurationFileLoadException("Could not open '" + config_path + "'", e);
    }
    final HashMap<String, Object> args = new HashMap<>();
    args.put(JsonReader.USE_MAPS, true);
    final Object[] loaded_items = (Object[]) JsonReader.jsonToJava(reader, args);

    // Process the loaded configuration.
    process_configuration(loaded_items);
  }

  /**
   * Convert the JSON representation to a collection of <code>CalculatorConfigItem</code>s.
   *
   * @param loaded_items the loaded configuration in hash of hashs form.
   */
  private void process_configuration(Object[] loaded_items) {
    for (final Object raw_item : loaded_items) {
      final JsonObject<String, Object> json_item = (JsonObject<String, Object>) raw_item;
      final int nParameters = ((Object[]) json_item.get("parameters")).length;
      final Object[] parameters = new Object[nParameters];
      for (int i = 0; i < nParameters; i++) {
        final Object temp = ((Object[]) json_item.get("parameters"))[i];
        parameters[i] = temp.getClass().cast(temp);
      }
      final CalculatorConfigItem item =
          new CalculatorConfigItem(
              (String) json_item.get("target"), parameters, (String) json_item.get("function"));
      put(item.target, item);
    }
  }
}
