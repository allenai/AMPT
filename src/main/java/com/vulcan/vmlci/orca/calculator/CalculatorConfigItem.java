/*
 *  Copyright (c) 2021 Vulcan Inc. All rights reserved.
 *  Licensed under the Apache 2.0 license. See LICENSE file in the project root for full license information.
 */

package com.vulcan.vmlci.orca.calculator;

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
