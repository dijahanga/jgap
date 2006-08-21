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
 * Easily creates single and batched consistent command objects
 *
 * @author Klaus Meffert
 * @since 3.0
 */
public class CommandFactory {
  /** String containing the CVS revision. Read out via reflection!*/
  private final static String CVS_REVISION = "$Revision: 1.1 $";

  public static CommandGene[] createStoreCommands(CommandGene[] a_target,
                                         Configuration a_conf,
                                         Class a_type, String a_prefix,
                                         int a_count)
      throws InvalidConfigurationException {
    CommandGene[] result = new CommandGene[a_count * 2 + a_target.length];
    for (int i = 0; i < a_target.length; i++) {
      result[i] = a_target[i];
    }
    for (int i = 0; i < a_count; i++) {
      result[i * 2 + a_target.length] = new StoreTerminalCommand(a_conf, a_type,
          a_prefix + i);
      result[i * 2 + 1 +
          a_target.length] = new ReadTerminalCommand(a_conf, a_type,
          a_prefix + i);
    }
    return result;
  }
}
