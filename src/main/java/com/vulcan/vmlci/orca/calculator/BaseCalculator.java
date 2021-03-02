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

import com.vulcan.vmlci.orca.helpers.ConfigurationLoader;
import com.vulcan.vmlci.orca.data.DataStore;
import org.scijava.log.Logger;
import org.scijava.log.StderrLogService;
import org.scijava.plugin.Parameter;

import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Math.sqrt;

public abstract class BaseCalculator {
  protected final DataStore dataStore;
  public HashMap<String, MethodHandle> measurement_funcs;
  protected CalculatorConfig measurement_dependencies;
  protected HashMap<String, ArrayList<String>> possible_measurements;
  @Parameter Logger logger;

  /**
   * Default constructor.
   *
   * <p>This constructor should not be invoked.
   *
   * @throws UnsupportedOperationException when invoked
   */
  public BaseCalculator() {
    throw new UnsupportedOperationException(
        "BaseCalculator and it's subclasses require a DataStore.");
  }

  /**
   * @param ds the <code>DataStore</code> that the Calculator will operate on.
   * @throws FileNotFoundException when the configuration file is not present.
   */
  public BaseCalculator(DataStore ds) throws FileNotFoundException {
    logger = new StderrLogService();
    dataStore = ds;
    loadMethods();
    loadConfiguration();
  }

  /**
   * Prepare the measurement functions for execution.
   *
   * <p>Identify all static methods in the invoking class and build a MethodHandle for later
   * execution.
   *
   * <p>Process:
   *
   * <ol>
   *   <li>Extract all public static methods.
   *   <li>For each static method resolve the name, defining class, return type, and parameter
   *       types. (The signature)
   *   <li>Via <code>MethodHandles.Lookup</code> instance find a method matching the signature and
   *       save it in a hashmap.
   * </ol>
   */
  private void loadMethods() {
    MethodHandles.Lookup lookup = MethodHandles.publicLookup();
    measurement_funcs = new HashMap<>();
    for (Method method : this.getClass().getMethods()) {
      if (Modifier.isStatic(method.getModifiers())) {
        MethodType methodType =
            MethodType.methodType(method.getReturnType(), method.getParameterTypes());
        try {
          MethodHandle mh =
              lookup.findStatic(method.getDeclaringClass(), method.getName(), methodType);
          measurement_funcs.put(method.getName(), mh);
        } catch (NoSuchMethodException | IllegalAccessException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Build the lookup tables that make the measurement manager work.
   *
   * <p>Responsible for triggering the load of the configuration.
   *
   * <p>The possible_measurements table is used to map a parameter back to the measurements that it
   * contributes to.
   *
   * @throws FileNotFoundException when the configuration file can't be found.
   */
  private void loadConfiguration() throws FileNotFoundException {

    measurement_dependencies =
        new CalculatorConfig(ConfigurationLoader.getFullConfigPath(getConfigurationFile()));
    possible_measurements = new HashMap<>();
    for (CalculatorConfigItem item : measurement_dependencies.values()) {
      for (Object raw_parameter : item.parameters) {
        if (!(raw_parameter instanceof String)) {
          continue;
        }
        String parameter = (String) raw_parameter;
        if (!possible_measurements.containsKey(parameter)) {
          possible_measurements.put(parameter, new ArrayList<>());
        }
        possible_measurements.get(parameter).add(item.target);
      }
    }
  }

  /** @return The name of the configuration file required for this calculator. */
  protected abstract String getConfigurationFile();

  /**
   * Compute the length from (x_start,y_start) to (x_end,y_end)
   *
   * @param x_start the x coordinate for the start of the line
   * @param y_start the y coordinate for the start of the line
   * @param x_end the x coordinate for the end of the line
   * @param y_end the y coordinate for the end of the line
   * @return the Euclidean distance from start -&lt; end if all of the arguments are != null, null
   *     otherwise.
   */
  public static Double length(Number x_start, Number y_start, Number x_end, Number y_end) {
//    System.err.println("BaseCalculator.length");
    if (!(x_start == null || y_start == null || x_end == null || y_end == null)) {
      double delta_x = x_start.doubleValue() - x_end.doubleValue();
      double delta_y = y_start.doubleValue() - y_end.doubleValue();
      return sqrt(delta_x * delta_x + delta_y * delta_y);
    }
    return null;
  }

  /**
   * Simple return source
   *
   * @param source the value for the source column
   * @return the contents of source
   */
  public static Object copy(Object source) {
    return source;
  }

  /**
   * Compute ratio of antecedent:consequent
   *
   * @param antecedent the numerator
   * @param consequent the denominator
   * @return numerator/denominator if both are != null, null otherwise.
   */
  public static Double ratio(Double antecedent, Double consequent) {
    if (!(antecedent == null || consequent == null)) {
      return antecedent / consequent;
    }
    return null;
  }

  /**
   * Compute the distance along a 1D parametric function.
   *
   * <p>pt = start + (end-start) * distance
   *
   * @param start the starting value
   * @param end the ending value
   * @param distance the interpolation distance as a
   * @return the interpolated value
   */
  public static Integer parametric_point(Integer start, Integer end, Double distance) {
//    System.err.println("BaseCalculator.parametric_point");
    if (!(start == null || end == null || distance == null)) {
      return (int)(start.doubleValue() + (end.doubleValue() - start.doubleValue()) * distance.doubleValue() + 0.5);
    }
    return null;
  }

  /**
   * Verifies that all non-literal parameters for a measure are defined in the data store.
   *
   * @param measure The measure being checked
   * @param title The image whose values are being checked.
   * @return true iff all non-literal parameters have a non-null value.
   */
  public boolean preflight_measurement(String measure, String title) {
    for (Object parameter : measurement_dependencies.get(measure).parameters) {
      if (!(parameter instanceof String)) {
        continue;
      }
      if(((String) parameter).contains("\"")){
        continue;
      }
      if (dataStore.get_value(title, (String) parameter) == null) {
        return false;
      }
    }
    return true;
  }

  /**
   * Perform measurement <code>measure</code> on <code>title</code>
   *
   * @param measure the name of the measurement.
   * @param title the image to measure.
   * @return the measurement. May be null if all parameters are not present.
   * @throws IllegalArgumentException if a measure or measurement function is not available.
   */
  public Object do_measurement(String measure, String title) {
    if (!measurement_dependencies.containsKey(measure)) {
      final String message = String.format("'%s' is not a known measurement", measure);
      IllegalArgumentException err = new IllegalArgumentException(message);
      logger.error(err);
      throw err;
    }
    CalculatorConfigItem measurement_def = measurement_dependencies.get(measure);
    final String function = measurement_def.function;

    if (!measurement_funcs.containsKey(function)) {
      String message = String.format("'%s' is not a known function", function);
      IllegalArgumentException err = new IllegalArgumentException(message);
      logger.error(err);
      throw err;
    }

    // Unmarshalling
    Object[] arguments = gatherArguments(title, measurement_def);

    // Execution
    MethodHandle mh = measurement_funcs.get(function);
    Object measurement_result = null;
    try {
      measurement_result = mh.invokeWithArguments(arguments);
    } catch (Throwable throwable) {
      logger.error(throwable);
    }
    return measurement_result;
  }

  /**
   * Collect the arguments for measurement from the datastore.
   *
   * @param title of the image being measured
   * @param measurement_def the
   * @return an array of arguments
   */
  private Object[] gatherArguments(String title, CalculatorConfigItem measurement_def) {
    Object[] parameters = measurement_def.parameters;
    int nArgs = parameters.length;
    Object[] arguments = new Object[nArgs];
    for (int i = 0; i < nArgs; i++) {
      arguments[i] = retrieve_scalar_argument(title, parameters[i]);
    }
    return arguments;
  }

  /**
   * Retrieves a scalar value from table
   *
   * @param title The name of the image.
   * @param param The parameter being retrieved.
   * @return The value of the parameter.
   */
  Object retrieve_scalar_argument(String title, Object param) {
    Object value;
    if (param instanceof String) {
      if(((String) param).contains("\"")){ // Extract a string literal
        value = ((String) param).substring(1,((String) param).length()-1);
      } else {
        value = dataStore.get_value(title, (String) param);
      }
    } else {
      value = param;
    }
    return value;
  }
}
