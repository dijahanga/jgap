/*
 * This file is part of JGAP.
 *
 * JGAP offers a dual license model containing the LGPL as well as the MPL.
 *
 * For licencing information please see the file license.txt included with JGAP
 * or have a look at the top of class org.jgap.Chromosome which representatively
 * includes the JGAP license policy applicable for any file delivered with JGAP.
 */
package org.jgap;

import org.jgap.impl.*;

import junit.framework.*;
import junitx.util.*;

/**
 * Tests for Genotype class
 *
 * @author Klaus Meffert
 * @since 1.1
 */
public class GenotypeTest
    extends TestCase {

  /** String containing the CVS revision. Read out via reflection!*/
  private final static String CVS_REVISION = "$Revision: 1.12 $";

  public GenotypeTest() {
  }

  public static Test suite() {
    TestSuite suite = new TestSuite(GenotypeTest.class);
    return suite;
  }

  public void setUp() {
    Genotype.setConfiguration(null);
  }

  /**
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 2.0
   */
  public void testConstruct_0() {
    try {
      new Genotype(null, new Population(0));
      fail();
    }
    catch (InvalidConfigurationException invex) {
      ; //this is OK
    }
    catch (IllegalArgumentException invex) {
      ; //this is OK
    }
  }

  /**
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 2.0
   */
  public void testConstruct_2() throws Exception {
    Chromosome[] chroms = new Chromosome[1];
    chroms[0] = new Chromosome(new Gene[] {
                               new IntegerGene(1, 5)});
    try {
      new Genotype(null, chroms);
      fail();
    }
    catch (InvalidConfigurationException invex) {
      ; //this is OK
    }
    catch (IllegalArgumentException invex) {
      ; //this is OK
    }
  }

  /**
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 2.0
   */
  public void testConstruct_3() throws Exception {
    Chromosome[] chroms = new Chromosome[1];
    chroms[0] = new Chromosome(new Gene[] {
                               new IntegerGene(1, 5)});
    try {
      new Genotype(new DefaultConfiguration(), chroms);
      fail();
    }
    catch (InvalidConfigurationException invex) {
      ; //this is OK
    }
  }

  /**
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 2.0
   */
  public void testConstruct_4() throws Exception {
    Chromosome[] chroms = new Chromosome[1];
    chroms[0] = new Chromosome(new Gene[] {
                               new IntegerGene(1, 5)});
    Configuration conf = new DefaultConfiguration();
    conf.setFitnessFunction(new StaticFitnessFunction(5));
    try {
      new Genotype(conf, chroms);
      fail();
    }
    catch (InvalidConfigurationException invex) {
      ; //this is OK
    }
  }

  /**
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 2.0
   */
  public void testConstruct_5() throws Exception {
    Chromosome[] chroms = new Chromosome[1];
    chroms[0] = new Chromosome(new Gene[] {
                               new IntegerGene(1, 5)});
    Configuration conf = new DefaultConfiguration();
    conf.setFitnessFunction(new StaticFitnessFunction(5));
    conf.setSampleChromosome(new Chromosome(new BooleanGene(), 9));
    try {
      new Genotype(conf, chroms);
      fail();
    }
    catch (InvalidConfigurationException invex) {
      ; //this is OK
    }
  }

  /**
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 2.0
   */
  public void testConstruct_6() throws Exception {
    Chromosome[] chroms = new Chromosome[1];
    chroms[0] = new Chromosome(new Gene[] {
                               new IntegerGene(1, 5)});
    Configuration conf = new DefaultConfiguration();
    conf.setFitnessFunction(new StaticFitnessFunction(5));
    conf.setSampleChromosome(new Chromosome(new BooleanGene(), 9));
    conf.setPopulationSize(7);
    Genotype genotype = new Genotype(conf, chroms);
    assertTrue(genotype.getConfiguration().getFitnessEvaluator() instanceof
               DefaultFitnessEvaluator);
  }

  /**
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 2.0
   */
  public void testConstruct_7() throws Exception {
    Configuration conf = new DefaultConfiguration();
    conf.setFitnessFunction(new StaticFitnessFunction(5));
    conf.setSampleChromosome(new Chromosome(new BooleanGene(), 9));
    conf.setPopulationSize(7);
    try {
      new Genotype(conf, new Chromosome[]{null});
      fail();
    }
    catch (IllegalArgumentException illex) {
      ; //this is OK
    }
  }

  /**
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 2.0
   */
  public void testConstruct_8() throws Exception {
    Chromosome[] chroms = new Chromosome[1];
    chroms[0] = new Chromosome(new Gene[] {
                               new IntegerGene(1, 5)});
    Configuration conf = new DefaultConfiguration();
    conf.setFitnessFunction(new StaticFitnessFunction(5));
    conf.setSampleChromosome(new Chromosome(new BooleanGene(), 9));
    conf.setPopulationSize(7);
    conf.setFitnessEvaluator(new DefaultFitnessEvaluator());
    new Genotype(conf, chroms);
  }

  /**
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 2.0
   */
  public void testConstruct_9() throws Exception {
    Chromosome[] chroms = new Chromosome[1];
    Configuration conf = new DefaultConfiguration();
    conf.setFitnessFunction(new StaticFitnessFunction(5));
    conf.setSampleChromosome(new Chromosome(new BooleanGene(), 9));
    conf.setPopulationSize(7);
    try {
      new Genotype(conf, chroms);
      fail();
    }
    catch (IllegalArgumentException iex) {
      ; //this is OK
    }
  }

  /**
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 2.0
   */
  public void testGetChromosomes_0() throws Exception {
    Configuration conf = new DefaultConfiguration();
    conf.setFitnessFunction(new StaticFitnessFunction(5));
    Chromosome[] chroms = new Chromosome[1];
    Chromosome chrom = new Chromosome(new Gene[] {
                                      new IntegerGene(1, 5)});
    chroms[0] = chrom;
    conf.setSampleChromosome(chrom);
    conf.setPopulationSize(7);
    Genotype genotype = new Genotype(conf, chroms);
    assertEquals(1, genotype.getChromosomes().length);
    assertEquals(chrom, genotype.getChromosomes()[0]);
  }

  /**
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 2.0
   */
  public void testGetFittestChromosome_0() throws Exception {
    Configuration conf = new DefaultConfiguration();
    conf.setFitnessFunction(new StaticFitnessFunction(5));
    Chromosome[] chroms = new Chromosome[1];
    Chromosome chrom = new Chromosome(new Gene[] {
                                      new IntegerGene(1, 5)});
    chroms[0] = chrom;
    conf.setSampleChromosome(chrom);
    conf.setPopulationSize(7);
    Genotype genotype = new Genotype(conf, chroms);
    PrivateAccessor.setField(genotype, "m_population", new Population(1));
    assertEquals(null, genotype.getFittestChromosome());
  }

  /**
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 2.0
   */
  public void testGetFittestChromosome_1() throws Exception {
    Configuration conf = new DefaultConfiguration();
    conf.setFitnessFunction(new StaticFitnessFunction(5));
    Chromosome[] chroms = new Chromosome[1];
    Chromosome chrom = new Chromosome(new Gene[] {
                                      new IntegerGene(1, 5)});
    chroms[0] = chrom;
    conf.setSampleChromosome(chrom);
    conf.setPopulationSize(7);
    Genotype genotype = new Genotype(conf, chroms);
    Chromosome chrom2 = genotype.getFittestChromosome();
    assertEquals(chrom, chrom2);
  }

  public void testEvolve_0() {
    /**@todo implement*/
    /**@todo check for correcctness of method when NaturalSelctors missing*/
  }

  /**
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 2.0
   */
  public void testToString_0() throws Exception {
    Configuration conf = new DefaultConfiguration();
    conf.setFitnessFunction(new StaticFitnessFunction(5));
    Chromosome[] chroms = new Chromosome[1];
    Chromosome chrom = new Chromosome(new Gene[] {
                                      new IntegerGene(1, 55)});
    chroms[0] = chrom;
    conf.setSampleChromosome(chrom);
    conf.setPopulationSize(7);
    Genotype genotype = new Genotype(conf, chroms);
    assertTrue(genotype.toString() != null);
    assertTrue(genotype.toString().length() > 0);
  }

  /**
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 2.0
   */
  public void testRandomInitialGenotype_0() throws Exception {
    try {
      Genotype genotype = Genotype.randomInitialGenotype(null);
      fail();
    }
    catch (IllegalArgumentException illex) {
      ; //this is OK
    }
  }

  /**
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 2.0
   */
  public void testRandomInitialGenotype_1() throws Exception {
    Configuration conf = new DefaultConfiguration();
    Chromosome chrom = new Chromosome(new Gene[] {
                                      new IntegerGene(1, 9999)});
    conf.setPopulationSize(7777);
    conf.setFitnessFunction(new StaticFitnessFunction(5));
    conf.setSampleChromosome(chrom);
    Genotype genotype = Genotype.randomInitialGenotype(conf);
    assertEquals(7777, genotype.getChromosomes().length);
  }

  /**
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 2.0
   */
  public void testEquals_0() throws Exception {
    Chromosome[] chroms = new Chromosome[1];
    chroms[0] = new Chromosome(new Gene[] {
                               new IntegerGene(1, 5)});
    Configuration conf = new DefaultConfiguration();
    conf.setFitnessFunction(new StaticFitnessFunction(5));
    conf.setSampleChromosome(new Chromosome(new BooleanGene(), 9));
    conf.setPopulationSize(99999);
    Genotype genotype = new Genotype(conf, chroms);
    assertEquals(false, genotype.equals(null));
    Genotype genotype2 = new Genotype(conf, chroms);
    assertTrue(genotype.equals(genotype2));
    assertEquals(genotype.toString(), genotype2.toString());
    assertEquals(genotype.toString(), genotype2.toString());
  }

  /**
   * Test if hashcode working in general
   *
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 2.1
   */
  public void testHashcode_0() throws Exception {
    Chromosome[] chroms = new Chromosome[1];
    chroms[0] = new Chromosome(new Gene[] {
                               new IntegerGene(1, 5)});
    Configuration conf = new DefaultConfiguration();
    conf.setFitnessFunction(new StaticFitnessFunction(5));
    conf.setSampleChromosome(new Chromosome(new BooleanGene(), 9));
    conf.setPopulationSize(99999);
    Genotype genotype = new Genotype(conf, chroms);
    genotype.hashcode();
  }

  public void testHashcode_1() throws Exception {
    /**@todo implement*/
  }
}
