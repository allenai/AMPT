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

import com.vulcan.vmlci.orca.DataStore;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Math.sqrt;

public abstract class BaseCalculator {
  protected static String configuration_file = null;
  protected final DataStore dataStore;
  public HashMap<String, MethodHandle> measurement_funcs;
  private CalculatorConfig measurement_dependencies;
  private HashMap<String, ArrayList<String>> possible_measurements;

  public BaseCalculator(DataStore ds) {
    dataStore = ds;
    loadMethods();
    loadConfiguration();
  }

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

  private void loadConfiguration() {
    measurement_dependencies = null;
  }

  /**
   * Compute the length from (x_start,y_start) to (x_end,y_end)
   *
   * @param x_start the x coordinate for the start of the line
   * @param y_start the y coordinate for the start of the line
   * @param x_end the x coordinate for the end of the line
   * @param y_end the y coordinate for the end of the line
   * @return the Euclidean distance from start -> end if all of the arguments are != null, null
   *     otherwise.
   */
  public static Double length(Double x_start, Double y_start, Double x_end, Double y_end) {
    if (!(x_start == null || y_start == null || x_end == null || y_end == null)) {
      Double delta_x = x_start - x_end;
      Double delta_y = y_start - y_end;
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
  public static Double copy(Double source) {
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
   * @param distance the interpolation distance
   * @return the interpolated value
   */
  public static Double parametric_point(Double start, Double end, Double distance) {
    if (!(start == null || end == null || distance == null)) {
      return start + (end - start) * distance;
    }
    return null;
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
      value = dataStore.get_value(title, (String) param);
    } else {
      value = param;
    }
    return value;
  }
  /*


   def __load_configuration(self):
       """Build the lookup tables that make the measurement manager work."""
       self.measurement_dependencies = configuration_loader.get_file(self.config_file)
       self.possible_measurements = defaultdict(list)
       for measurement, (parameters, _) in self.measurement_dependencies.items():
           for parameter in parameters:
               self.possible_measurements[parameter].append(measurement)

   def parameters_defined(self, measure, title):
       """Returns True"""
       valid_dict = dict()
       parameters, _ = self.measurement_dependencies[measure]
       for param in parameters:
           if isinstance(param, basestring):
               valid_dict[param] = self.retrieve_scalar_argument(title, param) is not None
       return valid_dict

   def do_measurement(self, measure, title):
       """Perform the measurement for image title"""
       arguments = []
       parameters, measurement_function = self.measurement_dependencies[measure]
       for param in parameters:
           arguments.append(self.retrieve_scalar_argument(title, param))
       try:
           measurement_result = self.measurement_funcs[measurement_function](*arguments)
       except KeyError:
           raise KeyError("%s is not a known measurement" % measurement_function)
       return measurement_result

  */
}
