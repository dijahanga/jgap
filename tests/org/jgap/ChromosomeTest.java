/*
 * This file is part of JGAP.
 *
 * JGAP is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * JGAP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with JGAP; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.jgap;

import org.jgap.impl.*;

import junit.framework.*;
import junitx.util.*;

/**
 * Tests for Chromosome class
 *
 * @author Klaus Meffert
 * @since 1.1
 */
public class ChromosomeTest
    extends TestCase {

  /** String containing the CVS revision. Read out via reflection!*/
  private final static String CVS_REVISION = "$Revision: 1.11 $";

  public ChromosomeTest() {
  }

  public void setUp() {
    Genotype.setConfiguration(null);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite(ChromosomeTest.class);
    return suite;
  }

  /**
   * Illegal constructions regarding first parameter
   */
  public void testConstruct_0() {
    try {
      new Chromosome(null, 1);
      fail();
    }
    catch (IllegalArgumentException iex) {
      ; //this is OK
    }
  }

  /**
   * Illegal constructions regarding second parameter
   */
  public void testConstruct_1() {
    try {
      new Chromosome(new IntegerGene(), 0);
      new Chromosome(new IntegerGene(), -1);
      new Chromosome(new IntegerGene(), -500);
      fail();
    }
    catch (IllegalArgumentException iex) {
      ; //this is OK
    }
  }

  /**
   * Illegal constructions regarding first and second parameter
   */
  public void testConstruct_2() {
    try {
      new Chromosome(null, 0);
      fail();
    }
    catch (IllegalArgumentException iex) {
      ; //this is OK
    }
  }

  /**
   * Legal construction
   */
  public void testConstruct_3() {
    new Chromosome(new IntegerGene(), 1);
  }

  /**
   * Illegal constructions regarding the only parameter
   */
  public void testConstruct_4() {
    try {
      new Chromosome(null);
      fail();
    }
    catch (IllegalArgumentException iex) {
      ; //this is OK
    }
  }

  /**
   * Illegal constructions regarding an element of the only parameter
   */
  public void testConstruct_51() {
    try {
      Gene[] genes = new IntegerGene[2];
      genes[0] = new IntegerGene();
      genes[1] = null;
      new Chromosome(genes);
    }
    catch (IllegalArgumentException iex) {
      ; //this is OK
    }
  }

  /**
   * Illegal constructions regarding an element of the only parameter
   */
  public void testConstruct_52() {
    try {
      Gene[] genes = new IntegerGene[1];
      genes[0] = null;
      new Chromosome(genes);
    }
    catch (IllegalArgumentException iex) {
      ; //this is OK
    }
  }

  /**
   * Illegal constructions regarding first parameter
   */
  public void testConstruct_6() {
    try {
      new Chromosome(null);
      fail();
    }
    catch (IllegalArgumentException illex) {
      ; //this is OK
    }
  }

  /**
   * Illegal constructions regarding first parameter
   */
  public void testConstruct_8() {
    try {
      Gene[] genes = new IntegerGene[2];
      genes[0] = new IntegerGene();
      genes[1] = null;
      new Chromosome(genes);
      fail();
    }
    catch (IllegalArgumentException illex) {
      ; //this is OK
    }
  }

  /**
   * Illegal constructions regarding configuration
   */
  public void testConstruct_10() throws Exception {
    Gene[] genes = new IntegerGene[2];
    genes[0] = new IntegerGene();
    genes[1] = new IntegerGene();
    Configuration conf = new DefaultConfiguration();
    Genotype.setConfiguration(conf);
    // fitness function missing in config.,
    new Chromosome(genes);
  }

  /**
   * Illegal constructions regarding configuration
   */
  public void testConstruct_11() throws Exception {
    Gene[] genes = new IntegerGene[2];
    genes[0] = new IntegerGene();
    genes[1] = new IntegerGene();
    Configuration conf = new DefaultConfiguration();
    conf.setFitnessFunction(new RandomFitnessFunction());
    Genotype.setConfiguration(conf);
      new Chromosome(genes);
  }

  /**
   * Illegal constructions regarding configuration
   */
  public void testConstruct_12() throws Exception {
    Gene[] genes = new IntegerGene[2];
    genes[0] = new IntegerGene();
    genes[1] = new IntegerGene();
    Configuration conf = new DefaultConfiguration();
    conf.setFitnessFunction(new RandomFitnessFunction());
    Genotype.setConfiguration(conf);
    Chromosome chrom2 = new Chromosome(genes);
    conf.setSampleChromosome(chrom2);
      new Chromosome(genes);
  }

  public void testConstruct_14() throws Exception {
    Gene gene = new BooleanGene();
    Chromosome chrom = new Chromosome(gene, 7);
    Gene[] genes = (Gene[]) PrivateAccessor.getField(chrom, "m_genes");
    assertEquals(7, genes.length);
    Gene sample;
    for (int i = 0; i < genes.length; i++) {
      sample = (Gene) genes[i];
      assertEquals(gene, sample);
    }
  }

  /**
   * Test clone without setting a configuration
   */
  public void testClone_0() throws InvalidConfigurationException {
    Gene[] genes = new IntegerGene[2];
    genes[0] = new IntegerGene();
    genes[1] = new IntegerGene();
    Chromosome chrom = new Chromosome(genes);
    try {
      Chromosome chrom2 = (Chromosome) chrom.clone();
      fail();
    }
    catch (IllegalStateException illex) {
      ; //this is OK
    }
  }

  /**
   * Test hashcode for intensity of diversity
   */
  public void testHashcode_0() throws InvalidConfigurationException {
    Gene[] genes = new IntegerGene[2];
    genes[0] = new IntegerGene();
    genes[1] = new IntegerGene();
    Configuration conf = new DefaultConfiguration();
    conf.setFitnessFunction(new RandomFitnessFunction());
    Chromosome chrom2 = new Chromosome(genes);
    conf.setSampleChromosome(chrom2);
    conf.setPopulationSize(5);
    Chromosome chrom = new Chromosome(genes);
    /**@todo implement random chromosomes to check how diverse the hashcode
     * function is.
     * E.g., if we have 10 chromosomes and only 2 different hashcodes, then
     * something must be wrong most probably*/
  }

  /**
   * Test clone with setting a configuration
   */
  public void testClone_1() throws InvalidConfigurationException {
    Gene[] genes = new IntegerGene[2];
    genes[0] = new IntegerGene();
    genes[1] = new IntegerGene();
    Configuration conf = new DefaultConfiguration();
    conf.setFitnessFunction(new StaticFitnessFunction(20));
    Chromosome chrom2 = new Chromosome(genes);
    conf.setSampleChromosome(chrom2);
    conf.setPopulationSize(5);
    Genotype.setConfiguration(conf);
    Chromosome chrom = new Chromosome(genes);
    chrom2 = (Chromosome) chrom.clone();
    assertEquals(chrom.hashCode(), chrom2.hashCode());
    assertEquals(chrom.getFitnessValue(), chrom2.getFitnessValue(), 0.0000001d);
    assertEquals(chrom.isSelectedForNextGeneration(),
                 chrom2.isSelectedForNextGeneration());
    assertEquals(chrom.size(), chrom2.size());
    assertEquals(chrom.toString(), chrom2.toString());
    assertTrue(chrom.equals(chrom2));
  }

  /**
   * Test clone with set application data implementing interface Cloneable
   */
  public void testClone_2() throws InvalidConfigurationException {
    Gene[] genes = new IntegerGene[2];
    genes[0] = new IntegerGene();
    genes[1] = new IntegerGene();
    Configuration conf = new DefaultConfiguration();
    conf.setFitnessFunction(new StaticFitnessFunction(20));
    Genotype.setConfiguration(conf);
    Chromosome chrom2 = new Chromosome(genes);
    conf.setSampleChromosome(chrom2);
    conf.setPopulationSize(5);
    Chromosome chrom = new Chromosome(genes);
    Object appObj = new MyAppObject();
    chrom.setApplicationData(appObj);
    chrom2 = (Chromosome) chrom.clone();
  }

  /**
   * Test clone with set application data, where cloning supported
   */
  public void testClone_3() throws InvalidConfigurationException {
    Gene[] genes = new IntegerGene[2];
    genes[0] = new IntegerGene();
    genes[1] = new IntegerGene();
    Configuration conf = new DefaultConfiguration();
    conf.setFitnessFunction(new StaticFitnessFunction(20));
    Genotype.setConfiguration(conf);
    Chromosome chrom2 = new Chromosome(genes);
    conf.setSampleChromosome(chrom2);
    conf.setPopulationSize(5);
    Chromosome chrom = new Chromosome(genes);
    Object appObj = new MyAppObject2();
    chrom.setApplicationData(appObj);
    chrom2 = (Chromosome) chrom.clone();
    assertTrue(chrom.equals(chrom2));
  }


  public void testEquals_0() {
    Gene[] genes = new IntegerGene[2];
    genes[0] = new IntegerGene();
    genes[1] = new IntegerGene();
    Chromosome chrom = new Chromosome(genes);
    assertTrue(chrom.equals(chrom));
    Chromosome chrom2 = new Chromosome(genes);
    assertTrue(chrom.equals(chrom2));
  }

  public void testEquals_1() {
    Gene[] genes = new IntegerGene[2];
    genes[0] = new IntegerGene();
    genes[1] = new IntegerGene();
    Chromosome chrom = new Chromosome(genes);
    assertTrue(chrom.equals(chrom));
    genes = new BooleanGene[2];
    genes[0] = new BooleanGene();
    genes[1] = new BooleanGene();
    Chromosome chrom2 = new Chromosome(genes);
    try {
      assertFalse(chrom.equals(chrom2));
    }
    catch (ClassCastException castex) {
      ; //this is OK
    }
  }

  public void testEquals_2() {
    Gene[] genes = new IntegerGene[2];
    Gene gen0 = new IntegerGene();
    gen0.setAllele(new Integer(1));
    Gene gen1 = new IntegerGene();
    gen1.setAllele(new Integer(2));
    genes[0] = gen0;
    genes[1] = gen1;
    Chromosome chrom = new Chromosome(genes);
    assertTrue(chrom.equals(chrom));
    genes = new IntegerGene[2];
    gen0 = new IntegerGene();
    gen0.setAllele(new Integer(1));
    gen1 = new IntegerGene();
    gen1.setAllele(new Integer(3));
    genes[0] = gen0;
    genes[1] = gen1;
    Chromosome chrom2 = new Chromosome(genes);
    assertFalse(chrom.equals(chrom2));
    gen1.setAllele(new Integer(2));
    assertTrue(chrom.equals(chrom2));
    gen0.setAllele(new Integer(2));
    assertFalse(chrom.equals(chrom2));
    gen1.setAllele(new Integer(1));
    assertFalse(chrom.equals(chrom2));
  }

  public void testEquals_3() {
    Gene[] genes = new IntegerGene[2];
    genes[0] = new IntegerGene();
    genes[1] = new IntegerGene();
    Chromosome chrom = new Chromosome(genes);
    assertFalse(chrom.equals(null));
    // no ClassCastException is expected next!
    // ---------------------------------------
    assertFalse(chrom.equals(genes));
  }

  public void testGetFitnessValue_0() throws Exception {
    Gene[] genes = new IntegerGene[2];
    genes[0] = new IntegerGene();
    genes[1] = new IntegerGene();
    Configuration conf = new DefaultConfiguration();
    StaticFitnessFunction ff = new StaticFitnessFunction(20);
    conf.setFitnessFunction(ff);
    Genotype.setConfiguration(conf);
    Chromosome chrom = new Chromosome(genes);
    conf.setSampleChromosome(chrom);
    conf.setPopulationSize(5);
    chrom = new Chromosome(genes);
    assertEquals(ff.getStaticFitnessValue(), chrom.getFitnessValue(), 0.0000001d);
    //intentionally assert it a second time
    assertEquals(ff.getStaticFitnessValue(), chrom.getFitnessValue(), 0.0000001d);
  }

  public void testGetFitnessValue_1() throws Exception {
    Gene[] genes = new IntegerGene[2];
    genes[0] = new IntegerGene();
    genes[1] = new IntegerGene();
    Configuration conf = new DefaultConfiguration();
    StaticFitnessFunction ff = new StaticFitnessFunction(20);
    conf.setFitnessFunction(ff);
    Genotype.setConfiguration(conf);
    Chromosome chrom = new Chromosome(genes);
    conf.setSampleChromosome(chrom);
    conf.setPopulationSize(5);
    chrom = new Chromosome(genes);
    assertEquals(ff.getStaticFitnessValue(), chrom.getFitnessValue(), 0.0000001d);
    //set fitness value to a different one
    ff.setStaticFitnessValue(44.235d);
    assertEquals(ff.getStaticFitnessValue(), chrom.getFitnessValue(), 0.0000001d);
  }

  public void testSize_0() throws Exception {
    Gene[] genes = new IntegerGene[2];
    genes[0] = new IntegerGene();
    genes[1] = new IntegerGene();
    Chromosome chrom = new Chromosome(genes);
    assertEquals(2, chrom.size());
  }

  public void testSize_1() throws Exception {
    Gene[] genes = new IntegerGene[1];
    genes[0] = new IntegerGene();
    Chromosome chrom = new Chromosome(genes);
    assertEquals(1, chrom.size());
  }

  public void testSize_2() throws Exception {
    Gene[] genes = new IntegerGene[0];
    Chromosome chrom = new Chromosome(genes);
    assertEquals(0, chrom.size());
  }

  public void testCompareTo_0() throws Exception {
    Gene[] genes = new IntegerGene[2];
    genes[0] = new IntegerGene();
    genes[1] = new IntegerGene();
    Configuration conf = new DefaultConfiguration();
    conf.setFitnessFunction(new StaticFitnessFunction(20));
    Chromosome chrom2 = new Chromosome(genes);
    conf.setSampleChromosome(chrom2);
    conf.setPopulationSize(5);
    Chromosome chrom = new Chromosome(genes);
    assertTrue(chrom.compareTo(chrom2) == 0);
    assertTrue(chrom2.compareTo(chrom) == 0);
  }

  public void testCompareTo_1() throws Exception {
    Gene[] genes = new IntegerGene[2];
    Gene gen0 = new IntegerGene();
    gen0.setAllele(new Integer(1147));
    genes[0] = gen0;
    genes[1] = new IntegerGene();
    Chromosome chrom0 = new Chromosome(genes);
    Configuration conf = new DefaultConfiguration();
    conf.setFitnessFunction(new StaticFitnessFunction(20));
    conf.setSampleChromosome(chrom0);
    conf.setPopulationSize(5);
    Chromosome chrom = new Chromosome(genes);
    Gene[] genes2 = new IntegerGene[2];
    Gene gene01 = new IntegerGene();
    gene01.setAllele(new Integer(4711));
    genes2[0] = gene01;
    genes2[1] = new IntegerGene();
    Chromosome chrom2 = new Chromosome(genes2);
    assertTrue(chrom.compareTo(chrom2) < 0);
    assertTrue(chrom2.compareTo(chrom) > 0);
  }

  public void testCompareTo_2() throws Exception {
    Gene[] genes = new IntegerGene[2];
    genes[0] = new IntegerGene();
    genes[1] = new IntegerGene();
    Chromosome chrom = new Chromosome(genes);
    Configuration conf = new DefaultConfiguration();
    conf.setFitnessFunction(new StaticFitnessFunction(20));
    Gene[] genes2 = new IntegerGene[3];
    genes2[0] = new IntegerGene();
    genes2[1] = new IntegerGene();
    genes2[2] = new IntegerGene();
    Chromosome chrom2 = new Chromosome(genes2);
    conf.setSampleChromosome(chrom2);
    conf.setPopulationSize(5);
    assertTrue(chrom.compareTo(chrom2) < 0);
    assertTrue(chrom2.compareTo(chrom) > 0);
  }

  public void testCompareTo_3() throws Exception {
    Gene[] genes = new IntegerGene[1];
    genes[0] = new IntegerGene();
    Chromosome chrom = new Chromosome(genes);
    assertTrue(chrom.compareTo(null) > 0);
  }

  public void testCompareTo_4() throws Exception {
    Gene[] genes = new IntegerGene[2];
    Gene gen0 = new IntegerGene();
    gen0.setAllele(new Integer(4711));
    genes[0] = gen0;
    genes[1] = new IntegerGene();
    Chromosome chrom0 = new Chromosome(genes);
    Configuration conf = new DefaultConfiguration();
    conf.setFitnessFunction(new StaticFitnessFunction(20));
    conf.setSampleChromosome(chrom0);
    conf.setPopulationSize(5);
    Chromosome chrom = new Chromosome(genes);
    Gene[] genes2 = new IntegerGene[2];
    Gene gene01 = new IntegerGene();
    gene01.setAllele(new Integer(4711));
    genes2[0] = gene01;
    genes2[1] = new IntegerGene();
    Chromosome chrom2 = new Chromosome(genes2);
    assertTrue(chrom.compareTo(chrom2) == 0);
    assertTrue(chrom2.compareTo(chrom) == 0);
  }
}

class MyAppObject extends TestFitnessFunction implements Cloneable {
  public int compareTo(Object o) {
    return 0;
  }

  public Object clone() throws CloneNotSupportedException {
    return null;
  }
}

class MyAppObject2 extends TestFitnessFunction implements IApplicationData {
  public boolean equals(Object o2) {
    return compareTo(o2) == 0;
  }

  public int compareTo(Object o) {
    return 0;
  }

  public Object clone() throws CloneNotSupportedException {
    return new MyAppObject2();
  }
}
