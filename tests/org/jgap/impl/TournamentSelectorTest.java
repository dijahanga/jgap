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
import junit.framework.*;
import java.util.*;
import org.jgap.*;
import junit.framework.*;
import junitx.util.*;

/**
 * Tests for TournamentSelector class
 *
 * @author Klaus Meffert
 * @since 2.0
 */
public class TournamentSelectorTest
    extends TestCase {
  /** String containing the CVS revision. Read out via reflection!*/
  private final static String CVS_REVISION = "$Revision: 1.4 $";

  public TournamentSelectorTest() {
  }

  public void setUp() {
    Genotype.setConfiguration(null);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite(TournamentSelectorTest.class);
    return suite;
  }

  /**
   * Valid construction
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 2.1
   */
  public void testConstruct_0()
      throws Exception {
    new TournamentSelector(1, 1.0d);
    new TournamentSelector(1, 0.5d);
    new TournamentSelector(10, 0.00001d);
    new TournamentSelector(50, 0.4d);
  }

  /**
   * Invalid construction
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 2.1
   */
  public void testConstruct_1()
      throws Exception {
    try {
      new TournamentSelector(0, 0.5d);
      fail();
    }
    catch (IllegalArgumentException iex) {
      ; //this is OK
    }
  }

  /**
   * Invalid construction
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 2.1
   */
  public void testConstruct_2()
      throws Exception {
    try {
      new TournamentSelector( -1, 0.5d);
      fail();
    }
    catch (IllegalArgumentException iex) {
      ; //this is OK
    }
  }

  /**
   * Invalid construction
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 2.1
   */
  public void testConstruct_3()
      throws Exception {
    try {
      new TournamentSelector(4, 0.0d);
      fail();
    }
    catch (IllegalArgumentException iex) {
      ; //this is OK
    }
  }

  /**
   * Invalid construction
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 2.1
   */
  public void testConstruct_4()
      throws Exception {
    try {
      new TournamentSelector(4, 1.0001d);
      fail();
    }
    catch (IllegalArgumentException iex) {
      ; //this is OK
    }
  }

  public void testAdd_0()
      throws Exception {
    TournamentSelector selector = new TournamentSelector(5, 0.5d);
    Configuration conf = new DefaultConfiguration();
    Genotype.setConfiguration(conf);

    Gene gene = new BooleanGene();
    Chromosome chrom = new Chromosome(gene, 5);
    selector.add(chrom);
    List chromosomes = ( (Vector) PrivateAccessor.getField(selector,
        "m_chromosomes"));
    assertEquals(1, chromosomes.size());
    assertEquals(chrom, chromosomes.get(0));
    selector.add(chrom);
    assertEquals(chrom, chromosomes.get(0));
    assertEquals(2, chromosomes.size());
    selector.add(chrom);
    assertEquals(3, chromosomes.size());
  }

  public void testEmpty_0()
      throws Exception {
    TournamentSelector selector = new TournamentSelector(4, 0.1d);
    Configuration conf = new DefaultConfiguration();
    Genotype.setConfiguration(conf);
    Gene gene = new BooleanGene();
    Chromosome chrom = new Chromosome(gene, 5);
    selector.add(chrom);
    selector.empty();
    List chromosomes = ( (Vector) PrivateAccessor.getField(selector,
        "m_chromosomes"));
    assertEquals(0, chromosomes.size());
  }

  /**
   * Test if clear()-method does not affect original Population.
   *
   * @throws Exception
   * @author Klaus Meffert
   * @since 2.1
   */
  public void testEmpty_1()
      throws Exception {
    TournamentSelector selector = new TournamentSelector(4, 0.1d);
    Configuration conf = new DefaultConfiguration();
    Genotype.setConfiguration(conf);
    Gene gene = new BooleanGene();
    Chromosome chrom = new Chromosome(gene, 5);
    Population pop = new Population(1);
    pop.addChromosome(chrom);
    selector.add(chrom);
    Population popNew = new Population();
    selector.select(1, null, popNew);
    selector.empty();
    assertEquals(1, popNew.size());
    assertNotNull(popNew.getChromosome(0));
  }

  /**
   * Test if clear()-method does not affect return value.
   *
   * @throws Exception
   * @author Klaus Meffert
   * @since 2.1
   */
  public void testEmpty_2()
      throws Exception {
    TournamentSelector selector = new TournamentSelector(10, 1.0d);
    Configuration conf = new DefaultConfiguration();
    Genotype.setConfiguration(conf);
    Gene gene = new BooleanGene();
    Chromosome chrom = new Chromosome(gene, 5);
    Population pop = new Population(1);
    pop.addChromosome(chrom);
    selector.add(chrom);
    Population popNew = new Population();
    selector.select(1, null, popNew);
    pop = popNew;
    selector.empty();
    assertEquals(1, pop.size());
    assertNotNull(pop.getChromosome(0));
  }

  /**
   * Test if below functionality working without error
   *
   * @throws Exception
   * @author Klaus Meffert
   * @since 2.1
   */
  public void testSelect_0()
      throws Exception {
    Configuration conf = new DefaultConfiguration();
    Genotype.setConfiguration(conf);

    TournamentSelector selector = new TournamentSelector(4, 0.3d);
    Gene gene = new IntegerGene();
    gene.setAllele(new Integer(444));
    Chromosome secondBestChrom = new Chromosome(gene, 3);
    secondBestChrom.setFitnessValue(11);
    selector.add(secondBestChrom);
    gene = new BooleanGene();
    gene.setAllele(new Boolean(false));
    Chromosome bestChrom = new Chromosome(gene, 3);
    bestChrom.setFitnessValue(12);
    selector.add(bestChrom);
    selector.select(1, null, new Population());
  }

  /**
   * Always select best chromosome for granted if prob is 1.0 and index for
   * selected chromosomes in tournament is equal to index of best chromosome.
   *
   * @throws Exception
   * @author Klaus Meffert
   * @since 2.1
   */
  public void testSelect_1()
      throws Exception {
    Configuration conf = new DefaultConfiguration();
    // random generator always returning 1 (index of best chromosome below)
    RandomGeneratorForTest rn = new RandomGeneratorForTest(1);
    conf.setRandomGenerator(rn);
    Genotype.setConfiguration(conf);

    TournamentSelector selector = new TournamentSelector(4, 1.0d);
    // add first chromosome
    // --------------------
    Gene gene = new BooleanGene();
    gene.setAllele(new Boolean(true));
    Chromosome thirdBestChrom = new Chromosome(gene, 7);
    thirdBestChrom.setFitnessValue(10);
    selector.add(thirdBestChrom);
    // add second chromosome
    // ---------------------
    gene = new BooleanGene();
    gene.setAllele(new Boolean(false));
    Chromosome bestChrom = new Chromosome(gene, 3);
    bestChrom.setFitnessValue(12);
    selector.add(bestChrom);
    // add third chromosome
    // ---------------------
    gene = new IntegerGene();
    gene.setAllele(new Integer(444));
    Chromosome secondBestChrom = new Chromosome(gene, 3);
    secondBestChrom.setFitnessValue(11);
    selector.add(secondBestChrom);
    // receive top 1 (= best) chromosome
    // ---------------------------------
    Population pop = new Population();
    selector.select(1, null, pop);
    Chromosome[] bestChroms = pop.toChromosomes();
    assertEquals(1, bestChroms.length);
    assertEquals(bestChrom, bestChroms[0]);
    // receive top 3 chromosomes
    // -------------------------
    pop.getChromosomes().clear();
    selector.select(3, null, pop);
    bestChroms = pop.toChromosomes();
    assertEquals(3, bestChroms.length);
    assertEquals(bestChrom, bestChroms[0]);
    assertEquals(bestChrom, bestChroms[1]);
    assertEquals(bestChrom, bestChroms[2]);
  }

  /**
   * Always select best chromosome for granted if prob is 1.0 and index for
   * selected chromosomes in tournament is equal to index of best chromosome.
   *
   * @throws Exception
   * @author Klaus Meffert
   * @since 2.1
   */
  public void testSelect_2()
      throws Exception {
    Configuration conf = new DefaultConfiguration();
    // random generator always returning 1 (index of best chromosome below)
    RandomGeneratorForTest rn = new RandomGeneratorForTest(1);
    conf.setRandomGenerator(rn);
    Genotype.setConfiguration(conf);
    TournamentSelector selector = new TournamentSelector(4, 1.0d);
    // add first chromosome
    // --------------------
    Gene gene = new BooleanGene();
    gene.setAllele(new Boolean(true));
    Chromosome thirdBestChrom = new Chromosome(gene, 7);
    thirdBestChrom.setFitnessValue(10);
    selector.add(thirdBestChrom);
    // add second chromosome
    // ---------------------
    gene = new BooleanGene();
    gene.setAllele(new Boolean(false));
    Chromosome bestChrom = new Chromosome(gene, 3);
    bestChrom.setFitnessValue(12);
    selector.add(bestChrom);
    // receive top 1 (= best) chromosome
    // ---------------------------------
    Population pop = new Population();
    selector.select(1, null, pop);
    Chromosome[] bestChroms = pop.toChromosomes();
    assertEquals(1, bestChroms.length);
    assertEquals(bestChrom, bestChroms[0]);
    // receive top 30 chromosomes.
    // ---------------------------
    pop.getChromosomes().clear();
    selector.select(30, null, pop);
    bestChroms = pop.toChromosomes();
    assertEquals(30, bestChroms.length);
    assertEquals(bestChrom, bestChroms[0]);
    assertEquals(bestChrom, bestChroms[1]);
    assertTrue(bestChrom == bestChroms[0]);
  }

  /**
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 2.1
   */
  public void testSelect_3()
      throws Exception {
    Configuration conf = new DefaultConfiguration();
    //Set index of chromosome to be selected by ThresholdSelector to 1.
    //1 because the best chromosome will be index 0 and the other one has
    // index 1.
    RandomGeneratorForTest rn = new RandomGeneratorForTest(0);
    rn.setNextDouble(0.0d);
    conf.setRandomGenerator(rn);
    Genotype.setConfiguration(conf);
    TournamentSelector selector = new TournamentSelector(2, 1.0d);
    // add first chromosome
    // --------------------
    Gene gene = new BooleanGene();
    gene.setAllele(new Boolean(true));
    Chromosome thirdBestChrom = new Chromosome(gene, 7);
    thirdBestChrom.setFitnessValue(10);
    selector.add(thirdBestChrom);
    // add second chromosome
    // ---------------------
    gene = new BooleanGene();
    gene.setAllele(new Boolean(false));
    Chromosome bestChrom = new Chromosome(gene, 3);
    bestChrom.setFitnessValue(12);
    selector.add(bestChrom);
    // receive top 1 (= best) chromosome
    // ---------------------------------
    Population pop = new Population();
    selector.select(1, null, pop);
    Chromosome[] bestChroms = pop.toChromosomes();
    assertFalse(bestChroms[0].equals(bestChrom));
  }

  /**
   * Ensure that selected Chromosome's are not equal to added Chromosome's.
   *
   * @throws Exception
   * @author Klaus Meffert
   * @since 2.1
   */
  public void testSelect_4()
      throws Exception {
    Configuration conf = new DefaultConfiguration();
    Genotype.setConfiguration(conf);
    TournamentSelector selector = new TournamentSelector(1, 0.2d);
    // add first chromosome
    // --------------------
    Gene gene = new BooleanGene();
    gene.setAllele(new Boolean(true));
    Chromosome thirdBestChrom = new Chromosome(gene, 7);
    thirdBestChrom.setFitnessValue(10);
    selector.add(thirdBestChrom);
    // add second chromosome
    // ---------------------
    gene = new BooleanGene();
    gene.setAllele(new Boolean(false));
    Chromosome bestChrom = new Chromosome(gene, 3);
    bestChrom.setFitnessValue(12);
    selector.add(bestChrom);
    // receive top 30 chromosomes.
    // ---------------------------
    Population pop = new Population();
    selector.select(30, null, pop);
    Population bestChroms = pop;
    List chromosomes = (Vector) PrivateAccessor.getField(selector,
        "m_chromosomes");
    assertFalse(bestChroms.equals(chromosomes));
  }

  /**
   * Never select best chromosome if prob is 0.0d. it is not allowed to select
   * probability to 0.0d therefor we set it via reflection.
   *
   * @throws Exception
   * @author Klaus Meffert
   * @since 2.1
   */
  public void testSelect_5()
      throws Exception {
    Configuration conf = new DefaultConfiguration();
    RandomGeneratorForTest rn = new RandomGeneratorForTest(0);
    conf.setRandomGenerator(rn);
    Genotype.setConfiguration(conf);
    TournamentSelector selector = new TournamentSelector(4, 0.00001d);
    PrivateAccessor.setField(selector, "m_probability", new Double(0.0d));
    // add first chromosome
    // --------------------
    Gene gene = new BooleanGene();
    gene.setAllele(new Boolean(true));
    Chromosome thirdBestChrom = new Chromosome(gene, 7);
    thirdBestChrom.setFitnessValue(10);
    selector.add(thirdBestChrom);
    // add second chromosome
    // ---------------------
    gene = new BooleanGene();
    gene.setAllele(new Boolean(false));
    Chromosome bestChrom = new Chromosome(gene, 3);
    bestChrom.setFitnessValue(12);
    selector.add(bestChrom);
    // receive top 30 chromosomes.
    // ---------------------------
    Population pop = new Population();
    Chromosome[] bestChroms;
    selector.select(5, null, pop);
    bestChroms = pop.toChromosomes();
    assertEquals(thirdBestChrom, bestChroms[0]);
    assertEquals(thirdBestChrom, bestChroms[1]);
  }

  /**
   * @throws Exception
   * @author Klaus Meffert
   * @since 2.2
   */
  public void testSelect_6()
      throws Exception {
    Configuration conf = new DefaultConfiguration();
    // random generator always returning 1 (index of best chromosome below)
    RandomGeneratorForTest rn = new RandomGeneratorForTest(1);
    conf.setRandomGenerator(rn);
    Genotype.setConfiguration(conf);

    TournamentSelector selector = new TournamentSelector(4, 1.0d);

    Population toAddFrom = new Population();

    // add first chromosome
    // --------------------
    Gene gene = new BooleanGene();
    gene.setAllele(new Boolean(true));
    Chromosome thirdBestChrom = new Chromosome(gene, 7);
    thirdBestChrom.setFitnessValue(10);
    toAddFrom.addChromosome(thirdBestChrom);
    // add second chromosome
    // ---------------------
    gene = new BooleanGene();
    gene.setAllele(new Boolean(false));
    Chromosome bestChrom = new Chromosome(gene, 3);
    bestChrom.setFitnessValue(12);
    toAddFrom.addChromosome(bestChrom);
    // add third chromosome
    // ---------------------
    gene = new IntegerGene();
    gene.setAllele(new Integer(444));
    Chromosome secondBestChrom = new Chromosome(gene, 3);
    secondBestChrom.setFitnessValue(11);
    toAddFrom.addChromosome(secondBestChrom);
    // receive top 1 (= best) chromosome
    // ---------------------------------
    Population pop = new Population();
    selector.select(1, null, pop);
    Chromosome[] bestChroms = pop.toChromosomes();
    // nothing selected (from nothing!)
    assertEquals(0, bestChroms.length);
    selector.select(1, toAddFrom, pop);
    bestChroms = pop.toChromosomes();
    assertEquals(1, bestChroms.length);
    assertEquals(bestChrom, bestChroms[0]);
    // receive top 3 chromosomes
    // -------------------------
    pop.getChromosomes().clear();
    selector.select(3, toAddFrom, pop);
    bestChroms = pop.toChromosomes();
    assertEquals(3, bestChroms.length);
    assertEquals(bestChrom, bestChroms[0]);
    assertEquals(bestChrom, bestChroms[1]);
    assertEquals(bestChrom, bestChroms[2]);
  }

  /**
   * @author Klaus Meffert
   * @since 2.2
   */
  public void testReturnsUniqueChromosomes_0() {
    TournamentSelector selector = new TournamentSelector(2, 0.5d);
    assertFalse(selector.returnsUniqueChromosomes());
  }
}
