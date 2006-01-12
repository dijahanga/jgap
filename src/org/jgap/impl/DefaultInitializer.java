/*
 * This file is part of JGAP.
 *
 * JGAP offers a dual license model containing the LGPL as well as the MPL.
 *
 * For licencing information please see the file license.txt included with JGAP
 * or have a look at the top of class org.jgap.Chromosome which representatively
 * includes the JGAP license policy applicable for any file delivered with JGAP.
 */
package org.jgap.impl;

import org.jgap.*;
import java.lang.reflect.*;

/**
 * Default implementation for initializing Chromosome's.
 *
 * @author Klaus Meffert
 * @since 2.6
 */
public class DefaultInitializer
    implements IInitializer {
  /** String containing the CVS revision. Read out via reflection!*/
  private static final String CVS_REVISION = "$Revision: 1.1 $";

  public boolean isHandlerFor(Object a_obj, Class a_class) {
    if (Chromosome.class.isAssignableFrom(a_class)) {
      return true;
    }
    else {
      if (a_obj != null && IInitializer.class.isAssignableFrom(a_class)) {
        IInitializer initer = (IInitializer)a_obj;
        return initer.isHandlerFor(null, a_class);
      }
      else {
        return false;
      }
    }
  }

  public Object perform(Object a_obj, Class a_class, Object a_params)
      throws Exception {
    if (IInitializer.class.isAssignableFrom(a_class)) {
      return ((IInitializer)a_obj).perform(null, a_class, a_params);
    }
//    if (Chromosome.class.isAssignableFrom(a_class)) {
//      Method m = a_class.getMethod("randomInitialChromosome",
//                                   new Class[] {});
//      return m.invoke(null, new Object[] {});
//    }
    if (a_class == Chromosome.class) {
      return Chromosome.randomInitialChromosome();
    }
    else {
      throw new IllegalArgumentException("DefaultInitializer not suited for"
                                         + " class"
                                         + a_class.getName()
                                         + " !");
    }
  }
}