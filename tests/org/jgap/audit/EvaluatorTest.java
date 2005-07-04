/*
 * This file is part of JGAP.
 *
 * JGAP offers a dual license model containing the LGPL as well as the MPL.
 *
 * For licencing information please see the file license.txt included with JGAP
 * or have a look at the top of class org.jgap.Chromosome which representatively
 * includes the JGAP license policy applicable for any file delivered with JGAP.
 */
package org.jgap.audit;

import org.jgap.*;
import org.jgap.impl.*;
import junit.framework.*;

/**
 * Tests for Evaluator class
 *
 * @author Klaus Meffert
 * @since 2.2
 */
public class EvaluatorTest
    extends TestCase {
  /** String containing the CVS revision. Read out via reflection!*/
  private final static String CVS_REVISION = "$Revision: 1.3 $";

  private final static double DELTA = 0.00000001d;

  public void setUp() {
    Genotype.setConfiguration(null);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite(EvaluatorTest.class);
    return suite;
  }

  public void testStoreGenotype_0()
      throws Exception {
    Configuration conf = new ConfigurationForTest();
    PermutingConfiguration pconf = new PermutingConfiguration(conf);
    Evaluator eval = new Evaluator(pconf);
    Population pop = new Population();
    Gene[] genes = new Gene[1];
    Gene gene = new BooleanGene();
    genes[0] = gene;
    Chromosome chrom = new Chromosome(genes);
    chrom.setFitnessValue(7.3d);
    pop.addChromosome(chrom);
    chrom = new Chromosome(genes);
    chrom.setFitnessValue(4.8d);
    pop.addChromosome(chrom);
    chrom = new Chromosome(genes);
    chrom.setFitnessValue(11.4d);
    pop.addChromosome(chrom);
    Genotype genotype = new Genotype(conf, pop);
    eval.storeGenotype(0, 0, genotype);
    Evaluator.GenotypeData genotypeData = eval.retrieveGenotype(0, 0);
    assertEquals(genotype.getPopulation().size(), genotypeData.size);
    assertEquals(genotype.getConfiguration().getGenerationNr(),
                 genotypeData.generation);
    Evaluator.ChromosomeData[] chromData = genotypeData.chromosomeData;
    for (int i = 0; i < chromData.length; i++) {
      assertEquals(genotype.getPopulation().getChromosome(i).getFitnessValue(),
                   chromData[i].fitnessValue, DELTA);
      assertEquals(genotype.getPopulation().getChromosome(i).size(),
                   chromData[i].size);
    }
    assertEquals(7.3d,
                 genotype.getPopulation().getChromosome(0).getFitnessValue(),
                 DELTA);
  }

  public void testCalcPerformance_0()
      throws Exception {
    Configuration conf = new ConfigurationForTest();
    PermutingConfiguration pconf = new PermutingConfiguration(conf);
    Evaluator eval = new Evaluator(pconf);
    Population pop = new Population();
    Gene[] genes = new Gene[1];
    Gene gene = new BooleanGene();
    genes[0] = gene;
    Chromosome chrom = new Chromosome(genes);
    chrom.setFitnessValue(7.3d);
    pop.addChromosome(chrom);
    chrom = new Chromosome(genes);
    chrom.setFitnessValue(4.8d);
    pop.addChromosome(chrom);
    chrom = new Chromosome(genes);
    chrom.setFitnessValue(11.4d);
    pop.addChromosome(chrom);
    Genotype genotype = new Genotype(conf, pop);
    eval.storeGenotype(0, 0, genotype);
    Evaluator.GenotypeDataAvg avg = eval.calcPerformance(0);
    assertEquals(pop.determineFittestChromosome().getFitnessValue(),
                 avg.bestFitnessValue, DELTA);
    assertEquals(0, avg.bestFitnessValueGeneration);
    assertEquals( (7.3 + 4.8 + 11.4) / 3, avg.avgFitnessValue, DELTA);
    assertEquals( (Math.abs(4.8 - 7.3) / 2 + Math.abs(11.4 - 4.8) / 2) / 1,
                 avg.avgDiversityFitnessValue, DELTA);
    assertEquals(0.0d, avg.avgBestDeltaFitnessValue, DELTA); //because only one run
  }

  public void testCalcPerformance_1()
      throws Exception {
    Configuration conf = new ConfigurationForTest();
    PermutingConfiguration pconf = new PermutingConfiguration(conf);
    Evaluator eval = new Evaluator(pconf);
    // run 0
    Population pop = new Population();
    Gene[] genes = new Gene[1];
    Gene gene = new BooleanGene();
    genes[0] = gene;
    Chromosome chrom = new Chromosome(genes);
    chrom.setFitnessValue(7.3d);
    pop.addChromosome(chrom);
    chrom = new Chromosome(genes);
    chrom.setFitnessValue(4.8d);
    pop.addChromosome(chrom);
    chrom = new Chromosome(genes);
    chrom.setFitnessValue(11.4d);
    pop.addChromosome(chrom);
    Genotype genotype = new Genotype(conf, pop);
    eval.storeGenotype(0, 0, genotype);
    // run 1
    pop = new Population();
    genes = new Gene[1];
    gene = new BooleanGene();
    genes[0] = gene;
    chrom = new Chromosome(genes);
    chrom.setFitnessValue(7);
    pop.addChromosome(chrom);
    chrom = new Chromosome(genes);
    chrom.setFitnessValue(17);
    pop.addChromosome(chrom);
    chrom = new Chromosome(genes);
    chrom.setFitnessValue(19);
    pop.addChromosome(chrom);
    genotype = new Genotype(conf, pop);
    eval.storeGenotype(0, 1, genotype);
    Evaluator.GenotypeDataAvg avg = eval.calcPerformance(0);
    assertEquals(pop.determineFittestChromosome().getFitnessValue(),
                 avg.bestFitnessValue, DELTA);
    assertEquals(0, avg.bestFitnessValueGeneration);
    assertEquals( ( (7.3 + 4.8 + 11.4) / 3) / 2 +
                 ( (7.0d + 17.0d + 19) / 3) / 2, avg.avgFitnessValue, DELTA);
    assertEquals( (Math.abs(4.8 - 7.3) / 2 + Math.abs(11.4 - 4.8) / 2) / 2
                 + (Math.abs(17.0 - 7) / 2 + Math.abs(19.0 - 17) / 2) / 2,
                 avg.avgDiversityFitnessValue, DELTA);
    assertEquals( (Math.abs(19 - 11.4d)) / 1, avg.avgBestDeltaFitnessValue,
                 DELTA);
    assertEquals( (double) 3 / 2 + (double) 3 / 2, avg.sizeAvg, DELTA);
  }

  public void testGetNumberOfRuns_0()
      throws Exception {
    Configuration conf = new ConfigurationForTest();
    PermutingConfiguration pconf = new PermutingConfiguration(conf);
    Evaluator eval = new Evaluator(pconf);
    Population pop = new Population();
    Gene[] genes = new Gene[1];
    Gene gene = new BooleanGene();
    genes[0] = gene;
    Chromosome chrom = new Chromosome(genes);
    chrom.setFitnessValue(7.3d);
    pop.addChromosome(chrom);
    chrom = new Chromosome(genes);
    chrom.setFitnessValue(4.8d);
    pop.addChromosome(chrom);
    chrom = new Chromosome(genes);
    chrom.setFitnessValue(11.4d);
    pop.addChromosome(chrom);
    Genotype genotype = new Genotype(conf, pop);
    eval.storeGenotype(0, 0, genotype);
    assertEquals(1, eval.getNumberOfRuns(0));
    assertEquals(0, eval.getNumberOfRuns(1));
  }

  /**
   * @throws Exception
   *
   * @author Klaus Meffert
   * @since 2.4
   */
  public void testCalcPerformance_2()
      throws Exception {
    Configuration conf = new ConfigurationForTest();
    PermutingConfiguration pconf = new PermutingConfiguration(conf);
    Evaluator eval = new Evaluator(pconf);
    // run 0, permutation 0
    Population pop00 = new Population();
    Gene[] genes = new Gene[1];
    Gene gene = new BooleanGene();
    genes[0] = gene;
    Chromosome chrom = new Chromosome(genes);
    chrom.setFitnessValue(7.3d);
    pop00.addChromosome(chrom);
    chrom = new Chromosome(genes);
    chrom.setFitnessValue(4.8d);
    pop00.addChromosome(chrom);
    chrom = new Chromosome(genes);
    chrom.setFitnessValue(11.4d);
    pop00.addChromosome(chrom);
    Genotype genotype = new Genotype(conf, pop00);
    eval.storeGenotype(0, 0, genotype);
    // run 1, permutation 0
    Population pop10 = new Population();
    genes = new Gene[1];
    gene = new BooleanGene();
    genes[0] = gene;
    chrom = new Chromosome(genes);
    chrom.setFitnessValue(7);
    pop10.addChromosome(chrom);
    chrom = new Chromosome(genes);
    chrom.setFitnessValue(17);
    pop10.addChromosome(chrom);
    chrom = new Chromosome(genes);
    chrom.setFitnessValue(19);
    pop10.addChromosome(chrom);
    genotype = new Genotype(conf, pop10);
    eval.storeGenotype(0, 1, genotype);
    // run 0, permutation 1
    Population pop01 = new Population();
    genes = new Gene[1];
    gene = new BooleanGene();
    genes[0] = gene;
    chrom = new Chromosome(genes);
    chrom.setFitnessValue(4);
    pop01.addChromosome(chrom);
    chrom = new Chromosome(genes);
    chrom.setFitnessValue(9);
    pop01.addChromosome(chrom);
    chrom = new Chromosome(genes);
    chrom.setFitnessValue(8);
    pop01.addChromosome(chrom);
    genotype = new Genotype(conf, pop01);
    eval.storeGenotype(1, 0, genotype);
    // run 1, permutation 1
    Population pop11 = new Population();
    genes = new Gene[1];
    gene = new BooleanGene();
    genes[0] = gene;
    chrom = new Chromosome(genes);
    chrom.setFitnessValue(14);
    pop11.addChromosome(chrom);
    chrom = new Chromosome(genes);
    chrom.setFitnessValue(11);
    pop11.addChromosome(chrom);
    chrom = new Chromosome(genes);
    chrom.setFitnessValue(28);
    pop11.addChromosome(chrom);
    genotype = new Genotype(conf, pop11);
    eval.storeGenotype(1, 1, genotype);
    Evaluator.GenotypeDataAvg avg = eval.calcPerformance(0);
    assertEquals(Math.max(pop00.determineFittestChromosome().getFitnessValue(),
                          pop10.determineFittestChromosome().getFitnessValue()),
                 avg.bestFitnessValue, DELTA);
    assertEquals(0, avg.bestFitnessValueGeneration);
    assertEquals( ( (7.3 + 4.8 + 11.4) / 3) / 2 +
                 ( (7.0d + 17.0d + 19) / 3) / 2, avg.avgFitnessValue, DELTA);
    assertEquals( (Math.abs(4.8 - 7.3) / 2 + Math.abs(11.4 - 4.8) / 2) / 2
                 + (Math.abs(17.0 - 7) / 2 + Math.abs(19.0 - 17) / 2) / 2,
                 avg.avgDiversityFitnessValue, DELTA);
    assertEquals( (Math.abs(19 - 11.4d)) / 1, avg.avgBestDeltaFitnessValue,
                 DELTA);
    assertEquals( (double) 3 / 2 + (double) 3 / 2, avg.sizeAvg, DELTA);
    avg = eval.calcPerformance(1);
    assertEquals(Math.max(pop01.determineFittestChromosome().getFitnessValue(),
                          pop11.determineFittestChromosome().getFitnessValue()),
                 avg.bestFitnessValue, DELTA);
    assertEquals(0, avg.bestFitnessValueGeneration);
    assertEquals( ( (4 + 9 + 8.0d) / 3) / 2 +
                 ( (14 + 11.0d + 28) / 3) / 2, avg.avgFitnessValue, DELTA);
    assertEquals( (Math.abs(9.0d - 4) / 2 + Math.abs(8.0d - 9) / 2) / 2
                 + (Math.abs(11.0d - 14) / 2 + Math.abs(28.0d - 11) / 2) / 2,
                 avg.avgDiversityFitnessValue, DELTA);
    assertEquals( (Math.abs(28 - 9)) / 1, avg.avgBestDeltaFitnessValue,
                 DELTA);
    assertEquals( (double) 3 / 2 + (double) 3 / 2, avg.sizeAvg, DELTA);
  }
}
