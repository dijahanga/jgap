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

/**
 * Natural selectors are responsible for actually selecting a specified number
 * of Chromosome specimens from a population, using the fitness values as a
 * guide. Usually fitness is treated as a statistic probability of survival,
 * not as the sole determining factor. Therefore, Chromosomes with higher
 * fitness values are more likely to survive than those with lesser fitness
 * values, but it's not guaranteed.
 *
 * @author Neil Rotstan
 * @author Klaus Meffert
 * @since 2.0 (previously named "NaturalSelector")
 */
public interface INaturalSelector {
  /** String containing the CVS revision. Read out via reflection!*/
  final static String CVS_REVISION = "$Revision: 1.5 $";

  /**
   * Select a given number of Chromosomes from the pool that will move on
   * to the next generation population. This selection should be guided by
   * the fitness values, but fitness should be treated as a statistical
   * probability of survival, not as the sole determining factor. In other
   * words, Chromosomes with higher fitness values should be more likely to
   * be selected than those with lower fitness values, but it should not be
   * guaranteed.
   *
   * @param a_howManyToSelect The number of Chromosomes to select.
   * @param a_from_population the population the Chromosomes will be selected from.
   * @param a_to_population the population the Chromosomes will be added to.
   *
   * @author Neil Rotstan
   * @author Klaus Meffert
   * @since 2.0 (since 1.0 with different return type)
   */
  void select(int a_howManyToSelect, Population a_from_population,
              Population a_to_population);

  /**
   * Empty out the working pool of Chromosomes. This will be invoked after
   * each evolution cycle so that the natural selector can be reused for
   * the next one.
   *
   * @author Neil Rotstan
   * @since 1.0
   */

  void empty();

  /**
   * @return true: The implementation of the NaturalSelector only returns
   *   unique Chromosome's (example: BestChromosomesSelector).
   *   false: Also doublette could be returned (example: WeightedRouletteSelector)
   * @author Klaus Meffert
   * @since 2.0
   */
  boolean returnsUniqueChromosomes();
}
