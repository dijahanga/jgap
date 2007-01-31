package examples.gp.tictactoe;

import org.jgap.gp.impl.*;
import org.jgap.gp.*;
import org.jgap.gp.function.*;
import org.jgap.gp.terminal.*;

/**
 * Validates evolved nodes for the Tic Tac Toe problem.
 *
 * @author Klaus Meffert
 * @since 3.2
 */
public class GameNodeValidator
    implements INodeValidator {
  /**
   * Validates a_node in the context of a_chrom during evolution. Considers the
   * recursion level (a_recursLevel), the type needed (a_type) for the node, the
   * functions available (a_functionSet) and the depth of the whole chromosome
   * needed (a_depth), and whether grow mode is used (a_grow is true) or not.
   *
   * @param a_chrom the chromosome that will contain the node, if valid (ignored
   * in this implementation)
   * @param a_node the node selected and to be validated
   * @param a_tries number of times the validator has been called, useful for
   * stopping by returning true if the number exceeds a limit
   * @param a_num the chromosome's index in the individual of this chromosome
   * @param a_recurseLevel level of recursion, i.e. the depth of a node
   * @param a_type the return type of the node needed
   * @param a_functionSet the array of available functions (ignored in this
   * implementation)
   * @param a_depth the allowed remaining depth of the program chromosome
   * @param a_grow true: use grow mode, false: use full mode (ignored in this
   * implementation)
   * @return true: node is valid; false: node is invalid
   *
   * @author Klaus Meffert
   * @since 3.2
   */
  public boolean validate(ProgramChromosome a_chrom, CommandGene a_node,
                          CommandGene a_rootNode,
                          int a_tries, int a_num, int a_recurseLevel,
                          Class a_type, CommandGene[] a_functionSet,
                          int a_depth, boolean a_grow) {
    // Guard to avoid endless validation.
    // ----------------------------------
    if (true || a_tries > 50) {
      return true;
    }
    // Chromosome 0.
    // -------------
    if (a_num == 0) {
      // SubProgram needed as root
      if (a_recurseLevel == 0 && a_type == CommandGene.VoidClass
          && a_node.getClass() != SubProgram.class) {
        return false;
      }
      // SubProgram forbidden other than at beginning
//      if (a_recurseLevel > 1 && a_node.getClass() == SubProgram.class) {
//        return false;
//      }
      // CountStones forbidden other than under root node
      if (a_recurseLevel > 1 && a_node.getClass() == CountStones.class) {
        return false;
      }

      // CountStones forbidden other than under SubProgram
      if ( (a_rootNode == null || a_rootNode.getClass() != SubProgram.class) &&
          a_node.getClass() == CountStones.class) {
        return false;
      }
      // CountStones needed one under root
//      if (a_recurseLevel == 1 && a_node.getClass() != CountStones.class) {
//        return false;
//      }
    }
    // Chromosome 1.
    // -------------
    if (a_num == 1) {
      // SubProgram needed as root
      if (a_recurseLevel == 0 && a_depth < 4 && a_type == CommandGene.VoidClass
          && a_node.getClass() != SubProgram.class) {
        return false;
      }
      // ReadTerminal needed one under root
      if (a_recurseLevel == 1 && a_depth < 2 && a_node.getClass() != ReadTerminal.class) {
        return false;
      }
    }
    return true;
  }
}