/*
 * This file is part of JGAP.
 *
 * JGAP offers a dual license model containing the LGPL as well as the MPL.
 *
 * For licencing information please see the file license.txt included with JGAP
 * or have a look at the top of class org.jgap.Chromosome which representatively
 * includes the JGAP license policy applicable for any file delivered with JGAP.
 */
package org.jgap.gp;

import org.jgap.*;

/**
 * Abstract base class for all GP commands. A CommandGene can hold additional
 * CommandGene's, it acts sort of like a Composite (also see CompositeGene for
 * a smiliar characteristics, although for a GA).
 *
 * @author Klaus Meffert
 * @since 3.0
 */
public abstract class CommandGene
    extends BaseGene implements Gene {
  /** String containing the CVS revision. Read out via reflection!*/
  private final static String CVS_REVISION = "$Revision: 1.9 $";

  public final static Class BooleanClass = Boolean.class;

  public final static Class IntegerClass = Integer.class;

  public final static Class LongClass = Long.class;

  public final static Class FloatClass = Float.class;

  public final static Class DoubleClass = Double.class;

  public final static Class VoidClass = Void.class;

  /**
   * Should isValid() be called? True = no!
   */
  private boolean m_noValidation;

  /**
   * The return type of this node.
   */
  private Class m_returnType;

  private int m_arity;

  private boolean m_integerType;

  private boolean m_floatType;

  /**
   * Initializations, called from each Constructor.
   */
  protected void init() {
  }

  public CommandGene(final Configuration a_conf, int a_arity,
                     Class a_returnType)
      throws InvalidConfigurationException {
    super(a_conf);
    init();
    m_arity = a_arity;
    m_returnType = a_returnType;
    if (a_returnType == Integer.class
        || a_returnType == Long.class) {
      m_integerType = true;
    }
    else if (a_returnType == Double.class
             || a_returnType == Float.class) {
      m_floatType = true;
    }
  }

  public void setAllele(Object a_newValue) {
    throw new java.lang.UnsupportedOperationException(
        "Method setAllele() not used.");
  }

  public Object getAllele() {
    return null;
  }

  public String getPersistentRepresentation()
      throws UnsupportedOperationException {
    /**@todo Implement this org.jgap.Gene method*/
    throw new java.lang.UnsupportedOperationException(
        "Method getPersistentRepresentation() not yet implemented.");
  }

  public void setValueFromPersistentRepresentation(String a_representation)
      throws UnsupportedOperationException, UnsupportedRepresentationException {
    /**@todo Implement this org.jgap.Gene method*/
    throw new java.lang.UnsupportedOperationException(
        "Method setValueFromPersistentRepresentation() not yet implemented.");
  }

  public void setToRandomValue(RandomGenerator a_numberGenerator) {
    // do nothing for GP here!
  }

  public void cleanup() {
    // do nothing for GP here!
  }

  public int size() {
    return m_arity;
  }

  private int getArity() {
    return size();
  }

  /**
   * Override if arity of GP command depends on individual.
   *
   * @param a_indvividual the invididual the command's arity depends on
   * @return arity of the command
   *
   * @author Klaus Meffert
   * @since 3.0
   */
  public int getArity(GPProgram a_indvividual) {
    return getArity();
  }

  public int compareTo(Object a_other) {
    CommandGene o2 = (CommandGene) a_other;
    if (size() != o2.size()) {
      if (size() > o2.size()) {
        return 1;
      }
      else {
        return -1;
      }
    }
    if (getClass() != o2.getClass()) {
      /**@todo do it more precisely*/
      return -1;
    }
    else {
      return 0;
    }
  }

  public boolean equals(Object a_other) {
    if (a_other == null) {
      return false;
    }
    else {
      try {
        CommandGene other = (CommandGene) a_other;
        if (getClass() == a_other.getClass()) {
          if (getInternalValue() == null) {
            if (other.getInternalValue() == null) {
              return true;
            }
            else {
              return false;
            }
          }
          else {
            if (other.getInternalValue() == null) {
              return false;
            }
            else {
              return true;
            }
          }
        }
        else {
          return false;
        }
      } catch (ClassCastException cex) {
        return false;
      }
    }
  }

  /**
   * @return the string representation of the command. Especially usefull to
   * output a resulting formula in human-readable form.
   */
  public abstract String toString();

  /**
   * Executes this node without knowing its return type.
   *
   * @param c the current Chromosome which is executing
   * @param n the index of the Function in the Chromosome's Function array which
   * is executing
   * @param args the arguments to the current Chromosome which is executing
   * @return the object which wraps the return value of this node, or null
   * if the return type is null or unknown
   * @throws UnsupportedOperationException if the type of this node is not
   * boolean
   *
   * @author Klaus Meffert
   * @since 3.0
   */
  public Object execute(ProgramChromosome c, int n, Object[] args) {
    if (m_returnType == BooleanClass)
      return new Boolean(execute_boolean(c, n, args));
    if (m_returnType == IntegerClass)
      return new Integer(execute_int(c, n, args));
    if (m_returnType == LongClass)
      return new Long(execute_long(c, n, args));
    if (m_returnType == FloatClass)
      return new Float(execute_float(c, n, args));
    if (m_returnType == DoubleClass)
      return new Double(execute_double(c, n, args));
    if (m_returnType == VoidClass)
      execute_void(c, n, args);
    else {
      return execute_object(c, n, args);
    }
    return null;
  }

  /**
   * @return the return type of this node
   *
   * @author Klaus Meffert
   * @since 3.0
   */
  public Class getReturnType() {
    return m_returnType;
  }

  /**
   * Sets the return type of this node.
   *
   * @param a_type the type to set the return type to
   *
   * @author Klaus Meffert
   * @since 3.0
   */
  public void setReturnType(Class a_type) {
    m_returnType = a_type;
  }

  /**
   * Executes this node as a boolean. Override to implement.
   *
   * @param c ignored here
   * @param n ignored here
   * @param args ignored here
   * @return nothing but exception
   * @throws UnsupportedOperationException
   *
   * @author Klaus Meffert
   * @since 3.0
   */
  public boolean execute_boolean(ProgramChromosome c, int n, Object[] args) {
    throw new UnsupportedOperationException(getName() +
        " cannot return boolean");
  }

  /**
   * Executes this node, returning nothing. Override to implement.
   *
   * @param c ignored here
   * @param n ignored here
   * @param args ignored here
   * @throws UnsupportedOperationException
   *
   * @author Klaus Meffert
   * @since 3.0
   */
  public void execute_void(ProgramChromosome c, int n, Object[] args) {
    throw new UnsupportedOperationException(getName() +
        " cannot return void");
  }

  /**
   * Executes this node as an integer. Override to implement.
   *
   * @param c ignored here
   * @param n ignored here
   * @param args ignored here
   * @return nothing but exception
   * @throws UnsupportedOperationException
   *
   * @author Klaus Meffert
   * @since 3.0
   */
  public int execute_int(ProgramChromosome c, int n, Object[] args) {
    throw new UnsupportedOperationException(getName() +
        " cannot return int");
  }

  /**
   * Executes this node as a long. Override to implement.
   *
   * @param c ignored here
   * @param n ignored here
   * @param args ignored here
   * @return nothing but exception
   * @throws UnsupportedOperationException
   *
   * @author Klaus Meffert
   * @since 3.0
   */
  public long execute_long(ProgramChromosome c, int n, Object[] args) {
    throw new UnsupportedOperationException(getName() +
        " cannot return long");
  }

  /**
   * Executes this node as a float. Override to implement.
   *
   * @param c ignored here
   * @param n ignored here
   * @param args ignored here
   * @return nothing but exception
   * @throws UnsupportedOperationException
   *
   * @author Klaus Meffert
   * @since 3.0
   */
  public float execute_float(ProgramChromosome c, int n, Object[] args) {
    throw new UnsupportedOperationException(getName() +
        " cannot return float");
  }

  /**
   * Executes this node as a double. Override to implement.
   *
   * @param c ignored here
   * @param n ignored here
   * @param args ignored here
   * @return nothing but exception
   * @throws UnsupportedOperationException
   *
   * @author Klaus Meffert
   * @since 3.0
   */
  public double execute_double(ProgramChromosome c, int n, Object[] args) {
    throw new UnsupportedOperationException(getName() +
        " cannot return double");
  }

  /**
   * Executes this node as an object. Override to implement.
   *
   * @param c ignored here
   * @param n ignored here
   * @param args ignored here
   * @return nothing but exception
   * @throws UnsupportedOperationException
   *
   * @author Klaus Meffert
   * @since 3.0
   */
  public Object execute_object(ProgramChromosome c, int n, Object[] args) {
    throw new UnsupportedOperationException(getName() +
        " cannot return Object");
  }

  public String getName() {
    return toString();
  }

  /**
   * Gets the type of node allowed form the given child number. Must be
   * overridden in subclasses.
   *
   * @param a_ind the individual the child belongs to
   * @param a_chromNum the chromosome number
   * @return the type of node allowed for that child
   *
   * @author Klaus Meffert
   * @since 3.0
   */
  public Class getChildType(GPProgram a_ind, int a_chromNum) {
    return getReturnType();
  }

  protected Object getInternalValue() {
    return null;
  }

  /**
   * Retrieves the hash code value for a CommandGene.
   * Override if another hashCode() implementation is necessary or more
   * appropriate than this default implementation.
   *
   * @return this Gene's hash code
   *
   * @author Klaus Meffert
   * @since 3.0
   */
  public int hashCode() {
    // If our internal value is null, then return zero. Otherwise,
    // just return the hash code of the allele Object.
    // -----------------------------------------------------------
    if (getInternalValue() == null) {
      return getClass().getName().hashCode();
    }
    else {
      return getInternalValue().hashCode();
    }
  }

  public boolean isIntegerType() {
    return m_integerType;
  }

  public boolean isFloatType() {
    return m_floatType;
  }

  /**
   * @return true: command affects global state (i.e. stack or memory)
   *
   * @author Klaus Meffert
   * @since 3.0
   */
  public boolean isAffectGlobalState() {
    return false;
  }

  /**
   * Subclasses capable of validating programs should overwrite this method.
   * See PushCommand as a sample.
   * @param a_program the ProgramChromosome to validate
   * @return true: a_program is (superficially) valid with the current Command
   *
   * @author Klaus Meffert
   * @since 3.0
   */
  public boolean isValid(ProgramChromosome a_program) {
    return true;
  }

  public boolean isValid(ProgramChromosome a_program, int a_index) {
    return true;
  }

  protected void check(ProgramChromosome a_program) {
    if (m_noValidation) {
      return;
    }
    if (!isValid(a_program)) {
      throw new IllegalStateException("State for GP-command not valid");
    }
  }

  protected void check(ProgramChromosome a_program, int a_index) {
    if (m_noValidation) {
      return;
    }
    if (!isValid(a_program, a_index)) {
      throw new IllegalStateException("State for GP-command not valid");
    }
  }

  public void applyMutation(int index, double a_percentage) {
    //do nothing here
  }

  public void setNoValidation(boolean a_noValidation) {
    m_noValidation = a_noValidation;
  }
}
