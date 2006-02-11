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

import java.io.*;
import java.util.*;
import org.jgap.util.*;

/**
 * List of chromosomes held in the Genotype (or possibly later in the
 * Configuration object).
 *
 * @author Klaus Meffert
 * @since 2.0
 */
public class Population
    implements Serializable {
  /** String containing the CVS revision. Read out via reflection!*/
  private static final String CVS_REVISION = "$Revision: 1.38 $";

  /**
   * The array of Chromosomes that makeup the Genotype's population.
   */
  private List m_chromosomes;

  /**
   * The fittest Chromosome of the population.
   */
  private IChromosome m_fittestChromosome;

  /**
   * Indicates whether at least one of the chromosomes has been changed
   * (deleted, added, modified).
   */
  private boolean m_changed;

  /**
   * Indicates that the list of Chromosomes has been sorted.
   */
  private boolean m_sorted;

  /*
   * Constructs the Population from a list of Chromosomes.
   * @param a_chromosomes the Chromosome's to be used for building the
   * Population
   *
   * @author Klaus Meffert
   * @since 2.0
   */
  public Population(final IChromosome[] a_chromosomes) {
    this(a_chromosomes.length);
    for (int i = 0; i < a_chromosomes.length; i++) {
      m_chromosomes.add(a_chromosomes[i]);
    }
    setChanged(true);
  }

  /*
   * Constructs an empty Population with the given initial size.
   * @param a_size the initial size of the empty Population. The initial size
   * is not fix, it is just for optimized list creation.
   *
   * @author Klaus Meffert
   * @since 2.0
   */
  public Population(final int a_size) {
    m_chromosomes = new ArrayList(a_size);
    setChanged(true);
  }

  /*
   * Constructs an empty Population with initial array size 100.
   *
   * @author Klaus Meffert
   * @since 2.0
   */
  public Population() {
    this(100);
  }

  /**
   * Adds a Chromosome to this Population. Does nothing when given null.
   *
   * @param a_toAdd the Chromosome to add
   *
   * @author Klaus Meffert
   * @since 2.0
   */
  public void addChromosome(final IChromosome a_toAdd) {
    if (a_toAdd != null) {
      m_chromosomes.add(a_toAdd);
      setChanged(true);
    }
  }

  /**
   * Adds all the Chromosomes in the given Population.
   * Does nothing on null or an empty Population.
   *
   * @param a_population the Population to add
   *
   * @author Klaus Meffert
   * @since 2.0
   */
  public void addChromosomes(final Population a_population) {
    if (a_population != null) {
      m_chromosomes.addAll(a_population.getChromosomes());
      // The following would do the same:
//      if (a_population.getChromosomes() != null) {
//        int size = a_population.getChromosomes().size();
//        for (int i = 0; i < size; i++) {
//          IChromosome chrom = a_population.getChromosome(i);
//          m_chromosomes.add(chrom);
//        }
//      }
      setChanged(true);
    }
  }

  /**
   * Replaces all chromosomes in the population with the give list of
   * chromosomes
   * @param a_chromosomes the chromosomes to make the population up from
   *
   * @author Klaus Meffert
   */
  public void setChromosomes(final List a_chromosomes) {
    m_chromosomes = a_chromosomes;
    setChanged(true);
  }

  /**
   * Sets in the given Chromosome on the given index in the list of chromosomes.
   * @param a_index the index to set the Chromosome in
   * @param a_chromosome the Chromosome to be set in
   *
   * @author Klaus Meffert
   * @since 2.0
   */
  public void setChromosome(final int a_index, final IChromosome a_chromosome) {
    if (m_chromosomes.size() == a_index) {
      addChromosome(a_chromosome);
    }
    else {
      m_chromosomes.set(a_index, a_chromosome);
      setChanged(true);
    }
  }

  /**
   * @return the list of Chromosome's in the Population. Don't modify the
   * retrieved list by using clear(), remove(int) etc. If you do so, you need to
   * call setChanged(true)
   *
   * @author Klaus Meffert
   * @since 2.0
   */
  public List getChromosomes() {
    return m_chromosomes;
  }

  /**
   * Returns a Chromosome at given index in the Population.
   * @param a_index the index of the Chromosome to be returned
   * @return Chromosome at given index in the Population
   *
   * @author Klaus Meffert
   * @since 2.0
   */
  public IChromosome getChromosome(final int a_index) {
    return (IChromosome) m_chromosomes.get(a_index);
  }

  /**
   * @return number of Chromosome's in the Population
   *
   * @author Klaus Meffert
   * @since 2.0
   */
  public int size() {
    return m_chromosomes.size();
  }

  /**
   * @return Iterator for the Chromosome list in the Population. Please be aware
   * that using remove() forces you to call setChanged(true)
   *
   * @author Klaus Meffert
   * @since 2.0
   */
  public Iterator iterator() {
    return m_chromosomes.iterator();
  }

  /**
   * @return the Population converted into a list of Chromosome's
   *
   * @author Klaus Meffert
   * @since 2.0
   */
  public IChromosome[] toChromosomes() {
    IChromosome[] result = new IChromosome[m_chromosomes.size()];
    for (int i = 0; i < m_chromosomes.size(); i++) {
      result[i] = (IChromosome) m_chromosomes.get(i);
    }
    return result;
  }

  /**
   * Determines the fittest Chromosome in the population (the one with the
   * highest fitness value) and memorizes it. This is an optimized version
   * compared to calling determineFittesChromosomes(1).
   * @return the fittest Chromosome of the population
   *
   * @author Klaus Meffert
   * @since 2.0
   */
  public IChromosome determineFittestChromosome() {
    if (!m_changed && m_fittestChromosome != null) {
      return m_fittestChromosome;
    }
    Iterator it = m_chromosomes.iterator();
    double bestFitness = -1.0d;
    FitnessEvaluator evaluator = Genotype.getConfiguration().
        getFitnessEvaluator();
    double fitness;
    while (it.hasNext()) {
      IChromosome chrom = (IChromosome) it.next();
      fitness = chrom.getFitnessValue();
      if (evaluator.isFitter(fitness, bestFitness)
          || m_fittestChromosome == null) {
        m_fittestChromosome = chrom;
        bestFitness = fitness;
      }
    }
    setChanged(false);
    return m_fittestChromosome;
  }

  /**
   * Mark that the population needs sorting for returning the fittest
   * chromosome.
   *
   * @param a_changed true: population needs sorting for returning the fittest
   * chromosome. false: pop. needs no sorting for this
   *
   * @author Klaus Meffert
   * @since 2.2
   */
  protected void setChanged(final boolean a_changed) {
    m_changed = a_changed;
    setSorted(false);
  }

  /**
   * @return true: population's chromosomes (maybe) have been changed.
   * false: not changed for sure
   *
   * @since 2.6
   */
  public boolean isChanged() {
    return m_changed;
  }

  /**
   * Mark the population as sorted.
   * @param a_sorted true: mark population as sorted
   *
   * @author Klaus Meffert
   * @since 2.6
   */
  protected void setSorted(final boolean a_sorted) {
    m_sorted = a_sorted;
  }

  /**
   * Determines whether the given chromosome is contained within the population.
   * @param a_chromosome the chromosome to check
   * @return true: chromosome contained within population
   *
   * @author Klaus Meffert
   * @since 2.1
   */
  public boolean contains(final IChromosome a_chromosome) {
    return m_chromosomes.contains(a_chromosome);
  }

  /**
   * Removes a chromosome in the list at the given index. Method has package
   * visibility to signal that this is a method not to be used outside the
   * JGAP kernel under normal circumstances.
   *
   * @param a_index index of chromosome to be removed in list
   * @return removed Chromosome
   *
   * @author Klaus Meffert
   * @since 2.4
   */
  IChromosome removeChromosome(final int a_index) {
    if (a_index < 0 || a_index >= size()) {
      throw new IllegalArgumentException("Index must be within bounds!");
    }
    setChanged(true);
    return (IChromosome) m_chromosomes.remove(a_index);
  }

  /**
   * Sorts the Chromosome list and returns the fittest n Chromosomes in
   * the population.
   * @param a_numberOfChromosomes number of top performer chromosomes to be
   * returned
   * @return list of the fittest n Chromosomes of the population, or the fittest
   * x Chromosomes with x = number of chromosomes in case n > x.
   *
   * @author Charles Kevin Hill
   * @since 2.4
   */
  public List determineFittestChromosomes(final int a_numberOfChromosomes) {
    int numberOfChromosomes = Math.min(a_numberOfChromosomes,
                                       getChromosomes().size());
    if (numberOfChromosomes <= 0) {
      return null;
    }
    if (!m_changed && m_sorted) {
      return getChromosomes().subList(0, numberOfChromosomes);
    }
    // Sort the list of chromosomes using the fitness comparator
    sortByFitness();
    return getChromosomes().subList(0, numberOfChromosomes);
  }

  /**
   * Sorts the chromosomes within the population according to their fitness
   * value using ChromosomFitnessComparator.
   *
   * @author Klaus Meffert
   * @since 2.6
   */
  public void sortByFitness() {
    /**@todo the following construction could be cached but wrt that the
     * evaluator registered with the configuration could change!*/
    sort(new ChromosomeFitnessComparator(Genotype.getConfiguration().
                                         getFitnessEvaluator()));
    setChanged(false);
    setSorted(true);
    m_fittestChromosome = (IChromosome) m_chromosomes.get(0);
  }

  /**
   * Sorts the chromosomes within the population utilzing the given comparator.
   *
   * @param a_comparator the comparator to utilize for sorting
   *
   * @author Klaus Meffert
   * @since 2.6
   */
  public void sort(Comparator a_comparator) {
    Collections.sort(getChromosomes(), a_comparator);
  }

  /**
   * Returns the genotype of the population, i.e. the list of genes in the
   * population.
   * @param a_resolveCompositeGenes true: split encountered CompositeGenes
   * into their single (atomic) genes
   * @return genotype of the population
   *
   * @author Klaus Meffert
   * @since 2.3
   */
  public List getGenome(final boolean a_resolveCompositeGenes) {
    List result = new Vector();
    List chroms = getChromosomes();
    int len = chroms.size();
    for (int i = 0; i < len; i++) {
      IChromosome chrom = (IChromosome) chroms.get(i);
      Gene[] genes = chrom.getGenes();
      int len2 = genes.length;
      for (int j = 0; j < len2; j++) {
        Gene gene = genes[j];
        if (a_resolveCompositeGenes && gene instanceof ICompositeGene) {
          addCompositeGene(result, (ICompositeGene) gene);
        }
        else {
          addAtomicGene(result, gene);
        }
      }
    }
    return result;
  }

  /**
   * Adds all the genes of a CompositeGene to a result list.
   * Method calls itself recursively.
   *
   * @param a_result the list to add to
   * @param a_gene the gene to start with
   *
   * @author Klaus Meffert
   * @since 2.3
   */
  private void addCompositeGene(final List a_result, final Gene a_gene) {
    if (a_gene instanceof ICompositeGene) {
      int len = a_gene.size();
      for (int i = 0; i < len; i++) {
        addCompositeGene(a_result, ( (ICompositeGene) a_gene).geneAt(i));
      }
    }
    else {
      addAtomicGene(a_result, a_gene);
    }
  }

  /**
   * Helper method for addCompositeGene
   * @param a_result List
   * @param a_gene Gene
   *
   * @author Klaus Meffert
   * @since 2.3
   */
  private void addAtomicGene(final List a_result, final Gene a_gene) {
    a_result.add(a_gene);
  }

  public boolean isSorted() {
    return m_sorted;
  }

  /**
   *
   * @param a_pop the population instance to compare with
   * @return true: given object equal to comparing one
   *
   * @author Klaus Meffert
   * @since 2.6
   */
  public boolean equals(Object a_pop) {
    try {
      return compareTo(a_pop) == 0;
    }
    catch (ClassCastException e) {
      // If the other object isn't an Population instance
      // then we're not equal.
      // ------------------------------------------------
      return false;
    }
  }

  /**
   * This method is not producing symmetric results as -1 is more often returned
   * than 1 (see description of return value).
   *
   * @param a_pop the other population to compare
   * @return -1: other object null or with fewer chromosomes or with equal number
   * of chromosomes but at least one not contained. 0: both populations
   * containing the same chromosomes. 1: this population contains fewer
   * chromosomes
   *
   * @author Klaus Meffert
   * @since 2.6
   */
  public int compareTo(Object a_pop) {
    Population other = (Population) a_pop;
    if (a_pop == null) {
      return 1;
    }
    int size1 = size();
    int size2 = other.size();
    if (size1 != size2) {
      if (size1 < size2) {
        return -1;
      }
      else {
        return 1;
      }
    }
    List chroms2 = other.getChromosomes();
    for (int i = 0; i < size1; i++) {
      if (!chroms2.contains(m_chromosomes.get(i))) {
        return 1;
      }
    }
    return 0;
  }
}
