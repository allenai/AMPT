/*
 *  Copyright (c) 2021 The Allen Institute for Artificial Intelligence.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.allenai.allenmli.orca.calculator;

public class CalculatorConfigItem {
  public String target;
  public Object[] parameters;
  public String function;

  public CalculatorConfigItem(String target, Object[] parameters, String function) {
    this.target = target;
    this.parameters = parameters;
    this.function = function;
  }

  /**
   * Returns a string representation of the object. In general, the {@code toString} method returns
   * a string that "textually represents" this object. The result should be a concise but
   * informative representation that is easy for a person to read. It is recommended that all
   * subclasses override this method.
   *
   * <p>The {@code toString} method for class {@code Object} returns a string consisting of the name
   * of the class of which the object is an instance, the at-sign character `{@code @}', and the
   * unsigned hexadecimal representation of the hash code of the object. In other words, this method
   * returns a string equal to the value of:
   *
   * <blockquote>
   *
   * <pre>
   * getClass().getName() + '@' + Integer.toHexString(hashCode())
   * </pre>
   *
   * </blockquote>
   *
   * @return a string representation of the object.
   */
  @Override
  public String toString() {
    StringBuilder parameterString = new StringBuilder("[");
    for (int i = 0, parametersLength = parameters.length; i < parametersLength; i++) {
      Object parameter = parameters[i];
      if (0 < i) {
        parameterString.append(", ");
      }
      parameterString.append(parameter.toString());
    }
    parameterString.append("]");
    return String.format(
        "{target: %s, parameters: %s, function: %s}", target, parameterString.toString(), function);
  }
}
