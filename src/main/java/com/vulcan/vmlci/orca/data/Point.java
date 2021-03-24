/*
 *  Copyright (c) 2021 Vulcan Inc. All rights reserved.
 *  Licensed under the Apache 2.0 license. See LICENSE file in the project root for full license information.
 */

package com.vulcan.vmlci.orca.data;

import java.io.Serializable;
import java.util.Objects;

public class Point implements Cloneable, Serializable {

  public double x;
  public double y;

  public Point(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public Point() {
    this(0, 0);
  }

  public Point(int x, int y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getX(), getY());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (null == o || getClass() != o.getClass()) return false;
    Point point = (Point) o;
    return getX() == point.getX() && getY() == point.getY();
  }

  /**
   * Creates and returns a copy of this object. The precise meaning of "copy" may depend on the
   * class of the object. The general intent is that, for any object {@code x}, the expression:
   *
   * <blockquote>
   *
   * <pre>
   * x.clone() != x</pre>
   *
   * </blockquote>
   *
   * will be true, and that the expression:
   *
   * <blockquote>
   *
   * <pre>
   * x.clone().getClass() == x.getClass()</pre>
   *
   * </blockquote>
   *
   * will be {@code true}, but these are not absolute requirements. While it is typically the case
   * that:
   *
   * <blockquote>
   *
   * <pre>
   * x.clone().equals(x)</pre>
   *
   * </blockquote>
   *
   * will be {@code true}, this is not an absolute requirement.
   *
   * <p>By convention, the returned object should be obtained by calling {@code super.clone}. If a
   * class and all of its superclasses (except {@code Object}) obey this convention, it will be the
   * case that {@code x.clone().getClass() == x.getClass()}.
   *
   * <p>By convention, the object returned by this method should be independent of this object
   * (which is being cloned). To achieve this independence, it may be necessary to modify one or
   * more fields of the object returned by {@code super.clone} before returning it. Typically, this
   * means copying any mutable objects that comprise the internal "deep structure" of the object
   * being cloned and replacing the references to these objects with references to the copies. If a
   * class contains only primitive fields or references to immutable objects, then it is usually the
   * case that no fields in the object returned by {@code super.clone} need to be modified.
   *
   * <p>The method {@code clone} for class {@code Object} performs a specific cloning operation.
   * First, if the class of this object does not implement the interface {@code Cloneable}, then a
   * {@code CloneNotSupportedException} is thrown. Note that all arrays are considered to implement
   * the interface {@code Cloneable} and that the return type of the {@code clone} method of an
   * array type {@code T[]} is {@code T[]} where T is any reference or primitive type. Otherwise,
   * this method creates a new instance of the class of this object and initializes all its fields
   * with exactly the contents of the corresponding fields of this object, as if by assignment; the
   * contents of the fields are not themselves cloned. Thus, this method performs a "shallow copy"
   * of this object, not a "deep copy" operation.
   *
   * <p>The class {@code Object} does not itself implement the interface {@code Cloneable}, so
   * calling the {@code clone} method on an object whose class is {@code Object} will result in
   * throwing an exception at run time.
   *
   * @return a clone of this instance.
   * @see Cloneable
   */
  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      // this shouldn't happen, since we are Cloneable
      throw new InternalError(e);
    }
  }

  @Override
  public String toString() {
    return "Point{" + "x=" + x + ", y=" + y + '}';
  }

  public double getX() {
    return x;
  }

  public void setX(double x) {
    this.x = x;
  }

  public double getY() {
    return y;
  }

  public void setY(double y) {
    this.y = y;
  }

  public void setLocation(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public void setLocation(int x, int y) {
    this.x = x;
    this.y = y;
  }
}
