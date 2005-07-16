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

import java.util.*;
import org.jgap.*;

/**
 * Implementation of a NaturalSelector that takes the top n chromosomes into
 * the next generation. n can be specified. Which chromosomes are the best is
 * decided by evaluating their fitness value.
 *
 * @author Klaus Meffert
 * @since 1.1
 */
public class BestChromosomesSelector
    extends NaturalSelector {
  /** String containing the CVS revision. Read out via reflection!*/
  private final static String CVS_REVISION = "$Revision: 1.27 $";

  /**
   * Stores the chromosomes to be taken into account for selection
   */
  private Population m_chromosomes;

  /**
   * Allows or disallows doublette chromosomes to be added to the selector
   */
  private boolean m_doublettesAllowed;

  /**
   * Indicated whether the list of added chromosomes needs sorting
   */
  private boolean m_needsSorting;

  /**
   * Comparator that is only concerned about fitness values
   */
  private FitnessValueComparator m_fitnessValueComparator;

  /**
   * The rate of original Chromosomes selected. This is because we otherwise
   * would always return the original input as output
   */
  private double m_originalRate;

  /**
   * Constructor
   *
   * @author Klaus Meffert
   * @since 1.1
   */
  public BestChromosomesSelector() {
    this(1.0d);
  }

  public BestChromosomesSelector(double a_originalRate) {
    super();
    m_chromosomes = new Population();
    m_needsSorting = false;
    setOriginalRate(a_originalRate);
    m_fitnessValueComparator = new FitnessValueComparator();
  }

  /**
   * Add a Chromosome instance to this selector's working pool of Chromosomes.
   * @param a_chromosomeToAdd the specimen to add to the pool
   *
   * @author Klaus Meffert
   * @since 1.1
   */
  protected synchronized void add(Chromosome a_chromosomeToAdd) {
    // If opted-in: Check if chromosome already added
    // This speeds up the process by orders of magnitude but could lower the
    // quality of evolved results because of fewer Chromosome's used!!!
    if (!m_doublettesAllowed &&
        m_chromosomes.getChromosomes().contains(a_chromosomeToAdd)) {
      return;
    }
    // New chromosome, insert it into the sorted collection of chromosomes
    a_chromosomeToAdd.setIsSelectedForNextGeneration(false);
    m_chromosomes.addChromosome(a_chromosomeToAdd);
    // Indicate that the list of chromosomes to add needs sorting.
    // -----------------------------------------------------------
    m_needsSorting = true;
  }

  /**
   * Select a given number of Chromosomes from the pool that will move on
   * to the next generation population. This selection will be guided by the
   * fitness values. The chromosomes with the best fitness value win.
   *
   * @param a_from_pop the population the Chromosomes will be selected from
   * @param a_to_pop the population the Chromosomes will be added to
   * @param a_howManyToSelect the number of Chromosomes to select
   *
   * @author Klaus Meffert
   * @since 1.1
   */
  public synchronized void select(int a_howManyToSelect, Population a_from_pop,
                                  Population a_to_pop) {
    if (a_from_pop != null) {
      int popSize = a_from_pop.size();
      for (int i = 0; i < popSize; i++) {
        add(a_from_pop.getChromosome(i));
      }
    }
    int canBeSelected;
    int chromsSize = m_chromosomes.size();
    if (a_howManyToSelect > chromsSize) {
      canBeSelected = chromsSize;
    }
    else {
      canBeSelected = a_howManyToSelect;
    }
    int neededSize = a_howManyToSelect;
    if (m_originalRate < 1.0d) {
      canBeSelected = (int) Math.round(
          (double) canBeSelected * m_originalRate);
      if (canBeSelected < 1) {
        canBeSelected = 1;
      }
    }
    // Sort the collection of chromosomes previously added for evaluation.
    // Only do this if necessary.
    // -------------------------------------------------------------------
    if (m_needsSorting) {
      Collections.sort(m_chromosomes.getChromosomes(),
                       m_fitnessValueComparator);
      m_needsSorting = false;
    }
    // To select a chromosome, we just go thru the sorted list.
    // --------------------------------------------------------
    Chromosome selectedChromosome;
    for (int i = 0; i < canBeSelected; i++) {
      selectedChromosome = m_chromosomes.getChromosome(i);
      selectedChromosome.setIsSelectedForNextGeneration(true);
      a_to_pop.addChromosome(selectedChromosome);
    }
    if (getDoubletteChromosomesAllowed()) {
      int toAdd;
      do {
        toAdd = neededSize - a_to_pop.size();
        int loopCount;
        if (toAdd > a_howManyToSelect) {
          loopCount = a_howManyToSelect;
        }
        else {
          loopCount = toAdd;
        }
        // Add existing Chromosome's by cloning them to fill up the return
        // result to contain the desired number of Chromosome's.
        // ---------------------------------------------------------------
        for (int i = 0; i < loopCount; i++) {
          selectedChromosome = m_chromosomes.getChromosome(i % chromsSize);
          selectedChromosome.setIsSelectedForNextGeneration(true);
          a_to_pop.addChromosome(selectedChromosome);
        }
      }
      while (toAdd > 0);
    }
  }

  /**
   * Empty out the working pool of Chromosomes.
   *
   * @author Klaus Meffert
   * @since 1.1
   */
  public synchronized void empty() {
    // clear the list of chromosomes
    // -----------------------------
    m_chromosomes.getChromosomes().clear();
    m_needsSorting = false;
  }

  /**
   * Determines whether doublette chromosomes may be added to the selector or
   * will be ignored.
   * @param a_doublettesAllowed true: doublette chromosomes allowed to be
   * added to the selector. FALSE: doublettes will be ignored and not added
   *
   * @author Klaus Meffert
   * @since 2.0
   */
  public void setDoubletteChromosomesAllowed(boolean a_doublettesAllowed) {
    m_doublettesAllowed = a_doublettesAllowed;
  }

  /**
   * @return TRUE: doublette chromosomes allowed to be added to the selector
   *
   * @author Klaus Meffert
   * @since 2.0
   */
  public boolean getDoubletteChromosomesAllowed() {
    return m_doublettesAllowed;
  }

  /**
   * @return always true as no Chromosome can be returnd multiple times
   *
   * @author Klaus Meffert
   * @since 2.0
   */
  public boolean returnsUniqueChromosomes() {
    return true;
  }

  /**
   *
   * @param a_originalRate the rate of how many of the original chromosomes
   * will be selected according to BestChromosomeSelector's strategy. The rest
   * (non-original) of the chromosomes is added as duplicates
   *
   * @author Klaus Meffert
   * @since 2.0
   */
  public void setOriginalRate(double a_originalRate) {
    if (a_originalRate < 0.0d || a_originalRate > 1.0d) {
      throw new IllegalArgumentException("Original rate must be greater than"
                                         + " zero and not greater than one!");
    }
    m_originalRate = a_originalRate;
  }

  /**
   *
   * @return see setOriginalRate
   *
   * @author Klaus Meffert
   * @since 2.0
   */
  public double getOriginalRate() {
    return m_originalRate;
  }
  
  //methods derived from the Configurable interface
  
  /**
   * Return a ConfigurationHandler for this class.
   * @author Siddhartha Azad.
   * @since 2.3
   * */
  public ConfigurationHandler getConfigurationHandler() {
  	// @todo write this
  	return null;
  }
  
  
  /**
   * Pass the name and value of a property to be set.
   * @author Siddhartha Azad.
   * @param name The name of the property.
   * @param value The value of the property.
   * @since 2.3
   * */
  public void setConfigProperty(String name, String value) throws ConfigException,
  	InvalidConfigurationException   {
  	/**@todo write this */
  }
  
 
  
  /**
   * Pass the name and values of a property to be set.
   * @author Siddhartha Azad.
   * @param name The name of the property.
   * @param values The different values of the property.
   * @since 2.3
   * */
  public void setConfigMultiProperty(String name, ArrayList values) throws 
  	ConfigException, InvalidConfigurationException {
  	/**@todo write this */
  }
}
