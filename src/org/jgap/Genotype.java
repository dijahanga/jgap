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

import java.io.*;
import java.util.*;

import org.jgap.event.*;

/**
 * Genotypes are fixed-length populations of chromosomes. As an instance of
 * a Genotype is evolved, all of its Chromosomes are also evolved. A Genotype
 * may be constructed normally, whereby an array of Chromosomes must be
 * provided, or the static randomInitialGenotype() method can be used to
 * generate a Genotype with a randomized Chromosome population.
 *
 * @author Neil Rotstan
 * @author Klaus Meffert
 * @since 1.0
 */
public class Genotype
    implements Serializable {
  /** String containing the CVS revision. Read out via reflection!*/
  private final static String CVS_REVISION = "$Revision: 1.29 $";

  /**
   * The current active Configuration instance.
   * @since 1.0
   */
  transient protected static Configuration m_activeConfiguration;

  /**
   * The array of Chromosomes that makeup the Genotype's population.
   * @since 2.0
   */
  protected Population m_population;

  /**
   * The working pool of Chromosomes, which is where Chromosomes that are
   * to be candidates for the next natural selection process are deposited.
   * This list is passed to each of the genetic operators as they are
   * invoked during each phase of evolution so that they can add the
   * Chromosomes they operated upon to it, and then it is eventually passed
   * to the NaturalSelector so that it can choose which Chromosomes will
   * go on to the next generation and which will be discarded. It is wiped
   * clean after each cycle of evolution.
   * @since 1.0
   */
  transient protected List m_workingPool;

  /**
   * Constructs a new Genotype instance with the given array of
   * Chromosomes and the given active Configuration instance. Note
   * that the Configuration object must be in a valid state
   * when this method is invoked, or a InvalidconfigurationException
   * will be thrown.
   *
   * @param a_activeConfiguration The current active Configuration object.
   * @param a_initialChromosomes The Chromosome population to be
   *                              managed by this Genotype instance.
   * @throws IllegalArgumentException if either the given Configuration object
   *         or the array of Chromosomes is null, or if any of the Genes
   *         in the array of Chromosomes is null.
   * @throws InvalidConfigurationException if the given Configuration object
   *         is in an invalid state.
   *
   * @author Neil Rotstan
   * @author Klaus Meffert
   * @since 1.0
   * @deprecated use Genotype(Configuration, Population) instead
   */
  public Genotype(Configuration a_activeConfiguration,
                  Chromosome[] a_initialChromosomes)
      throws InvalidConfigurationException {
    this(a_activeConfiguration, new Population(a_initialChromosomes));
  }

  /**
   * Constructs a new Genotype instance with the given array of
   * Chromosomes and the given active Configuration instance. Note
   * that the Configuration object must be in a valid state
   * when this method is invoked, or a InvalidconfigurationException
   * will be thrown.
   *
   * @param a_activeConfiguration The current active Configuration object.
   * @param a_population The Chromosome population to be managed by this
   *        Genotype instance.
   * @throws InvalidConfigurationException
   *
   * @author Neil Rotstan
   * @author Klaus Meffert
   * @since 2.0
   */
  public Genotype(Configuration a_activeConfiguration,
                  Population a_population)
      throws InvalidConfigurationException {
    // Sanity checks: Make sure neither the Configuration, the array
    // of Chromosomes, nor any of the Genes inside the array are null.
    // ---------------------------------------------------------------
    if (a_activeConfiguration == null) {
      throw new IllegalArgumentException(
          "The Configuration instance may not be null.");
    }
    if (a_population == null) {
      throw new IllegalArgumentException(
          "The Population may not be null.");
    }
    for (int i = 0; i < a_population.size(); i++) {
      if (a_population.getChromosome(i) == null) {
        throw new IllegalArgumentException(
            "The Chromosome instance at index " + i + " of the array of " +
            "Chromosomes is null. No Chromosomes instance in this array " +
            "may be null.");
      }
    }
    // Lock the settings of the Configuration object so that the cannot
    // be altered.
    // ----------------------------------------------------------------
    a_activeConfiguration.lockSettings();
    m_population = a_population;
    m_activeConfiguration = a_activeConfiguration;
    m_workingPool = new ArrayList();
  }

  /**
   * Sets the active Configuration object on this Genotype and its
   * member Chromosomes. This method should be invoked immediately following
   * deserialization of this Genotype. If an active Configuration has already
   * been set on this Genotype, then this method will do nothing.
   *
   * @param a_activeConfiguration The current active Configuration object
   *                              that is to be referenced internally by
   *                              this Genotype and its member Chromosome
   *                              instances.
   *
   * @throws InvalidConfigurationException if the Configuration object is
   *         null or cannot be locked because it is in an invalid or
   *         incomplete state.
   *
   * @author Neil Rotstan
   * @author Klaus Meffert
   * @since 1.0
   */
  public void setActiveConfiguration(Configuration a_activeConfiguration)
      throws InvalidConfigurationException {
    // Only assign the given Configuration object if we don't already
    // have one.
    // --------------------------------------------------------------
    if (m_activeConfiguration == null) {
      if (a_activeConfiguration == null) {
        throw new InvalidConfigurationException(
            "The given Configuration object may not be null.");
      }
      else {
        // Make sure the Configuration object is locked and cannot be
        // changed.
        // ----------------------------------------------------------
        a_activeConfiguration.lockSettings();
        m_activeConfiguration = a_activeConfiguration;
        // Since this method is invoked following deserialization of
        // this Genotype, the constructor hasn't been invoked. So make
        // sure any other transient fields are initialized properly.
        // -----------------------------------------------------------
        m_workingPool = new ArrayList();
      }
    }
  }

  /**
   * Retrieves the array of Chromosomes that make up the population of this
   * Genotype instance.
   *
   * @return The population of Chromosomes.
   *
   * @author Neil Rotstan
   * @author Klaus Meffert
   * @since 1.0
   * @deprecated uses getPopulation() instead
   */
  public synchronized Chromosome[] getChromosomes() {
    Iterator it = m_population.iterator();
    Chromosome[] result = new Chromosome[m_population.size()];
    int i = 0;
    while (it.hasNext()) {
      result[i++] = (Chromosome) it.next();
    }
    return result;
  }

  public Population getPopulation() {
    return m_population;
  }

  /**
   * Retrieves the Chromosome in the population with the highest fitness
   * value.
   *
   * @return The Chromosome with the highest fitness value, or null if
   *         there are no chromosomes in this Genotype.
   *
   * @author Neil Rotstan
   * @author Klaus Meffert
   * @since 1.0
   */
  public synchronized Chromosome getFittestChromosome() {
    int len = m_population.size();
    if (len == 0) {
      return null;
    }
    // Set the best fitness value to that of the first chromosome.
    // Then loop over the rest of the chromosomes and see if any has
    // a better fitness value.
    // The decision whether a fitness value is better than another is
    // delegated to a FitnessEvaluator
    // --------------------------------------------------------------
    Chromosome fittestChromosome = m_population.getChromosome(0);
    double fittestValue = fittestChromosome.getFitnessValue();
    for (int i = 1; i < len; i++) {
      if (m_activeConfiguration.getFitnessEvaluator().isFitter(m_population.getChromosome(i).
                                      getFitnessValue(),
                                      fittestValue)) {
        fittestChromosome = m_population.getChromosome(i);
        fittestValue = fittestChromosome.getFitnessValue();
      }
    }
    return fittestChromosome;
  }

  /**
   * Evolve the population of Chromosomes within this Genotype. This will
   * execute all of the genetic operators added to the present active
   * configuration and then invoke the natural selector to choose which
   * chromosomes will be included in the next generation population. Note
   * that the population size not always remains constant (dependent on the
   * NaturalSelector's used!).
   *
   * @author Neil Rotstan
   * @author Klaus Meffert
   * @since 1.0
   */
  public synchronized void evolve() {
    verifyConfigurationAvailable();

    applyNaturalSelectors(true);

    // Execute all of the Genetic Operators.
    // -------------------------------------
    List geneticOperators = m_activeConfiguration.getGeneticOperators();
    Iterator operatorIterator = geneticOperators.iterator();
    while (operatorIterator.hasNext()) {
      ( (GeneticOperator) operatorIterator.next()).operate(
          m_activeConfiguration, m_population, m_workingPool);
    }

    // Add the chromosomes in the working pool to the population.
    // ----------------------------------------------------------
    Iterator iterator = m_workingPool.iterator();

    while ( iterator.hasNext() )
    {
        Chromosome currentChromosome = (Chromosome) iterator.next();
        m_population.addChromosome(currentChromosome);
    }

    // If a bulk fitness function has been provided, then convert the
    // working pool to an array and pass it to the bulk fitness
    // function so that it can evaluate and assign fitness values to
    // each of the Chromosomes.
    // --------------------------------------------------------------
    BulkFitnessFunction bulkFunction =
        m_activeConfiguration.getBulkFitnessFunction();
    if (bulkFunction != null) {
      Chromosome[] candidateChromosomes = (Chromosome[])
          m_workingPool.toArray(new Chromosome[m_workingPool.size()]);
      bulkFunction.evaluate(candidateChromosomes);
    }

    applyNaturalSelectors(false);

    // Fill up population if size dropped below 10% of original size
    if (m_activeConfiguration.getMinimumPopSizePercent() > 0) {
      int minSize = (int) (m_activeConfiguration.getPopulationSize() *
                           m_activeConfiguration.getMinimumPopSizePercent() /
                           100);
      if (m_population.size() < minSize) {
        Chromosome newChrom;
        try {
          while (m_population.size() < minSize) {
            newChrom = Chromosome.randomInitialChromosome(m_activeConfiguration);
            m_population.addChromosome(newChrom);
          }
        }
        catch (InvalidConfigurationException invex) {
          invex.printStackTrace();
        }
      }
    }

    // Fire an event to indicate we've performed an evolution.
    // -------------------------------------------------------
    m_activeConfiguration.getEventManager().fireGeneticEvent(
        new GeneticEvent(GeneticEvent.GENOTYPE_EVOLVED_EVENT, this));
    // Iterate over the Chromosomes in the working pool. Clean up any that
    // haven't been selected to go on to the next generation.
    // -------------------------------------------------------------------
    Iterator workingPoolIterator = m_workingPool.iterator();
    Chromosome currentChromosome;
    while (workingPoolIterator.hasNext()) {
      currentChromosome = (Chromosome) workingPoolIterator.next();
      if (!currentChromosome.isSelectedForNextGeneration()) {
        currentChromosome.cleanup();
      }
    }
    // Clear out the working pool in preparation for the next evolution
    // cycle.
    // ----------------------------------------------------------------
    m_workingPool.clear();
  }

  /**
   * Evolves this Genotype the specified number of times. This is
   * equivalent to invoking the standard evolve() method the given number
   * of times in a row.
   *
   * @param a_numberOfEvolutions The number of times to evolve this Genotype
   *                             before returning.
   *
   * @author Klaus Meffert
   * @since 1.1
   */
  public void evolve(int a_numberOfEvolutions) {
    //done as given by request 708781
    for (int i = 0; i < a_numberOfEvolutions; i++) {
      evolve();
    }
  }

  /**
   * Return a string representation of this Genotype instance,
   * useful for display purposes.
   *
   * @return A string representation of this Genotype instance.
   *
   * @author Neil Rotstan
   * @since 1.0
   */
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < m_population.size(); i++) {
      buffer.append(m_population.getChromosome(i).toString());
      buffer.append(" [");
      buffer.append(m_population.getChromosome(i).getFitnessValue());
      buffer.append(']');
      buffer.append('\n');
    }
    return buffer.toString();
  }

  /**
   * Convenience method that returns a newly constructed Genotype
   * instance configured according to the given Configuration instance.
   * The population of Chromosomes will created according to the setup of
   * the sample Chromosome in the Configuration object, but the gene values
   * (alleles) will be set to random legal values.
   * <p>
   * Note that the given Configuration instance must be in a valid state
   * at the time this method is invoked, or an InvalidConfigurationException
   * will be thrown.
   *
   * @param a_activeConfiguration The current active Configuration object.
   * @return A newly constructed Genotype instance.
   *
   * @throws IllegalArgumentException if the given Configuration object is
   *         null.
   * @throws InvalidConfigurationException if the given Configuration
   *         instance not in a valid state.
   *
   * @author Neil Rotstan
   * @since 1.0
   */
  public static Genotype randomInitialGenotype(Configuration a_activeConfiguration)
      throws InvalidConfigurationException {
    if (a_activeConfiguration == null) {
      throw new IllegalArgumentException(
          "The Configuration instance may not be null.");
    }
    a_activeConfiguration.lockSettings();
    // Create an array of chromosomes equal to the desired size in the
    // active Configuration and then populate that array with Chromosome
    // instances constructed according to the setup in the sample
    // Chromosome, but with random gene values (alleles). The Chromosome
    // class' randomInitialChromosome() method will take care of that for
    // us.
    // ------------------------------------------------------------------
    int populationSize = a_activeConfiguration.getPopulationSize();
    Population pop = new Population(populationSize);
    for (int i = 0; i < populationSize; i++) {
      pop.addChromosome(Chromosome.randomInitialChromosome(
          a_activeConfiguration));
    }
    return new Genotype(a_activeConfiguration, pop);
  }

  /**
   * Compares this Genotype against the specified object. The result is true
   * if the argument is an instance of the Genotype class, has exactly the
   * same number of chromosomes as the given Genotype, and, for each
   * Chromosome in this Genotype, there is an equal chromosome in the
   * given Genotype. The chromosomes do not need to appear in the same order
   * within the populations.
   *
   * @param other The object to compare against.
   * @return true if the objects are the same, false otherwise.
   *
   * @author Neil Rotstan
   * @author Klaus Meffert
   * @since 1.0
   */
  public boolean equals(Object other) {
    try {
      // First, if the other Genotype is null, then they're not equal.
      // -------------------------------------------------------------
      if (other == null) {
        return false;
      }
      Genotype otherGenotype = (Genotype) other;
      // First, make sure the other Genotype has the same number of
      // chromosomes as this one.
      // ----------------------------------------------------------
      if (m_population.size() != otherGenotype.m_population.size()) {
        return false;
      }
      // Next, prepare to compare the chromosomes of the other Genotype
      // against the chromosomes of this Genotype. To make this a lot
      // simpler, we first sort the chromosomes in both this Genotype
      // and the one we're comparing against. This won't affect the
      // genetic algorithm (it doesn't care about the order), but makes
      // it much easier to perform the comparison here.
      // --------------------------------------------------------------
      Collections.sort(m_population.getChromosomes());
      Collections.sort(otherGenotype.getPopulation().getChromosomes());
      for (int i = 0; i < m_population.size(); i++) {
        if (! (m_population.getChromosome(i).equals(
            otherGenotype.m_population.getChromosome(i)))) {
          return false;
        }
      }
      return true;
    }
    catch (ClassCastException e) {
      return false;
    }
  }

  /**
   * Verifies that a Configuration object has been properly set on this
   * Genotype instance. If not, then an IllegalStateException is thrown.
   * In general, this method should be invoked by any operation on this
   * Genotype that makes use of the Configuration instance.
   *
   * @author Neil Rotstan
   * @since 1.0
   */
  private void verifyConfigurationAvailable() {
    if (m_activeConfiguration == null) {
      throw new IllegalStateException(
          "The active Configuration object must be set on this " +
          "Genotype prior to invocation of other operations.");
    }
  }

  /**
   * Applies all NaturalSelector's registered with the Configuration
   * @param processBeforeGeneticOperators true apply NaturalSelector's
   *   applicable before GeneticOperator's, false: apply the ones applicable
   *   after GeneticOperator's
   *
   * @author Klaus Meffert
   * @since 2.0
   */
  protected void applyNaturalSelectors(boolean processBeforeGeneticOperators) {
    // Process all natural selectors applicable before executing the
    // genetic operators (reproduction, crossing over, mutation...).
    // -------------------------------------------------------------
    int selectorSize = m_activeConfiguration.getNaturalSelectorsSize(
        processBeforeGeneticOperators);
    if (selectorSize > 0) {
      int m_population_size = m_activeConfiguration.getPopulationSize();
      int m_single_selection_size = m_population_size / selectorSize;
      Population m_new_population;
      if (selectorSize > 1) {
        /**@todo following is very time consuming!*/
        m_new_population = new Population(m_population_size);
      }
      else {
       m_new_population = null;
      }

      NaturalSelector selector;
      // Repopulate the population of chromosomes with those selected
      // by the natural selector.
      // Iterate over all natural selectors.
      // ------------------------------------------------------------
      for (int i = 0; i < selectorSize; i++) {
        selector = m_activeConfiguration.getNaturalSelector(
            processBeforeGeneticOperators, i);

        // Add the chromosomes pool to the natural selector
        // (of type BestChromosomesSelector, WeightedRouletteSelector...).
        // ----------------------------------------------------------------
        Iterator iterator1 = m_population.iterator();
        while (iterator1.hasNext()) {
          Chromosome currentChromosome = (Chromosome) iterator1.next();
          selector.add(currentChromosome);
        }

        if (i == selectorSize - 1 && i > 0) {
          // Ensure the last NaturalSelector adds the remaining Chromosome's
          // ---------------------------------------------------------------
          m_single_selection_size = m_population_size - m_new_population.size();
        }

        // Do selection of Chromosome's
        // ----------------------------
        if (selectorSize > 1) {
          Population m_partial_result = selector.select(m_single_selection_size);
          m_new_population.addChromosomes(m_partial_result);
        }
        else {
          // Optimized procedure for single NaturalSelector
          // ----------------------------------------------
//          m_population.getChromosomes().clear();
//          m_population.addChromosomes(selector.select(m_activeConfiguration, m_single_selection_size));

//          m_population.setChromosomes(selector.select(m_activeConfiguration, m_single_selection_size).getChromosomes());
          m_population = selector.select(m_single_selection_size);
        }
        // Clean up the natural selector.
        // ------------------------------
        selector.empty();
      }
      if (selectorSize > 1) {
        m_population = m_new_population;
      }
    }

  }

  public static Configuration getConfiguration() {
    return m_activeConfiguration;
  }

  public static void setConfiguration(Configuration a_configuration) {
    m_activeConfiguration = a_configuration;
  }
}
