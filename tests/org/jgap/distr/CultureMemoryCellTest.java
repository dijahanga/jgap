/*
 * This file is part of JGAP.
 *
 * JGAP offers a dual license model containing the LGPL as well as the MPL.
 *
 * For licencing information please see the file license.txt included with JGAP
 * or have a look at the top of class org.jgap.Chromosome which representatively
 * includes the JGAP license policy applicable for any file delivered with JGAP.
 */
package org.jgap.distr;

import org.jgap.*;
import junit.framework.*;

/**
 * Test class for CultureMemoryCell class
 *
 * @author Klaus Meffert
 * @since 2.3
 */
public class CultureMemoryCellTest
    extends TestCase {
  /** String containing the CVS revision. Read out via reflection!*/
  private static final String CVS_REVISION = "$Revision: 1.5 $";

  private final static double DELTA = 0.00000001d;

  public void setUp() {
    Genotype.setConfiguration(null);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite(CultureMemoryCellTest.class);
    return suite;
  }

  /**
   * @author Klaus Meffert
   * @since 2.3
   */
  public void testToString_0() {
    CultureMemoryCell cell = new CultureMemoryCell("aName", 77);
    assertEquals(
        "[Name:aName;Value:null;Version:0;Read accessed:0;History Size:77;History:[]",
        cell.toString());
    cell.setDouble(45.9d);
    String result = cell.toString();
    assertEquals(
        "[Name:aName;Value:45.9;Version:1;Read accessed:0;History Size:77;"
        + "History:[[Name:aName;Value:null;Version:0;Read accessed:0;"
        + "History Size:77;History:[]]]", result);
  }

  /**
   * @author Klaus Meffert
   * @since 2.3
   */
  public void testToString_1() {
    CultureMemoryCell cell = new CultureMemoryCell("aName", 77);
    assertEquals(
        "[Name:aName;Value:null;Version:0;Read accessed:0;History Size:77;History:[]",
        cell.toString());
    cell.setDouble(17.3d);
    cell.setDouble( -45.9d);
    String result = cell.toString();
    System.err.println(result);
    assertEquals(
        "[Name:aName;Value:-45.9;Version:2;Read accessed:0;History Size:77;"
        + "History:[[Name:aName;Value:null;Version:0;Read accessed:0;"
        + "History Size:77;History:[]];[Name:aName;Value:17.3;Version:1;"
        + "Read accessed:0;History Size:77;History:[]]]", result);
  }

  /**
   * Empty cell
   *
   * @author Klaus Meffert
   * @since 2.3
   */
  public void testToString_2() {
    CultureMemoryCell cell = new CultureMemoryCell();
    assertEquals(
        "[Name:null;Value:null;Version:0;Read accessed:0;History Size:3;History:[]",
        cell.toString());
  }

  /**
   * @author Klaus Meffert
   * @since 2.3
   */
  public void testSetDouble_0() {
    CultureMemoryCell cell = new CultureMemoryCell("ANAME", 55);
    cell.setDouble(17.3d);
    assertEquals(17.3d, cell.getCurrentValueAsDouble(), DELTA);
    cell.setDouble(1.8d);
    assertEquals(1.8d, ( (Double) cell.getCurrentValue()).doubleValue(), DELTA);
  }
}
