package examples.functionFinder.test;

import java.io.*;
import java.util.*;

import org.jgap.*;
import org.jgap.impl.*;

import junit.framework.*;
import examples.functionFinder.*;

/**
 * Tests for GeneExtractor class
 *
 * @author Klaus Meffert
 * @since 2.2
 */
public class GeneExtractorTest extends TestCase {

  /** String containing the CVS revision. Read out via reflection!*/
  private static final String CVS_REVISION = "$Revision: 1.2 $";

  private static int numberOfFunctions;
  private static int numberOfConstants;
  private static int numberOfOperators;

  public GeneExtractorTest(String name) {
    super(name);
  }

  public static void init() {
    Repository.init();
    numberOfFunctions = Repository.getFunctions().size();
    numberOfConstants = Repository.getConstants().size();
    numberOfOperators = Repository.getOperators().size();
  }

  public void setUp() {
    init();
  }

  /**
   * Use a function
   */
  public void testGene_0() {
    Gene[] genes = new Gene[ 2 ];
    genes[ 0 ] = new TestGene(3);
    final int opNr = 2;
    genes[ 1 ] = new TestGene(opNr);
    Term elem = constructTerm(genes);
    assertEquals(Repository.getFunctions().get(3), elem.termName);
    assertEquals( ( (String) Repository.getOperators().get(opNr)).charAt(0)
        ,elem.operator);
  }

  /**
   * Use a constant
   */
  public void testGene_1() {
    Gene[] genes = new Gene[ 2 ];
    genes[ 0 ] = new TestGene(numberOfFunctions + 0);
    genes[ 1 ] = new TestGene(2);
    Term elem = constructTerm(genes);
    assertEquals(Repository.getConstants().get(0), elem.termName);
    assertEquals( ( (String) Repository.getOperators().get(2)).charAt(0),elem.operator);
  }

  /**
   * Use a constant (forcing modulo for functions)
   */
  public void testGene_2() {
    Gene[] genes = new Gene[ 2 ];
    genes[ 0 ] = new TestGene( (numberOfFunctions + 0) + 33 * (numberOfConstants
                 + numberOfFunctions));
    genes[ 1 ] = new TestGene(2);
    Term elem = constructTerm(genes);
    assertEquals(Repository.getConstants().get(0), elem.termName);
    assertEquals( ( (String) Repository.getOperators().get(2)).charAt(0),elem.operator);
  }

  /**
   * Use a function (forcing modulo for operator)
   */
  public void testGene_3() {
    Gene[] genes = new Gene[ 2 ];
    genes[ 0 ] = new TestGene(3);
    final int opNr = 1;
    genes[ 1 ] = new TestGene(numberOfOperators * 24 + opNr);
    Term elem = constructTerm(genes);
    assertEquals(Repository.getFunctions().get(3), elem.termName);
    assertEquals( ( (String) Repository.getOperators().get(opNr)).charAt(0)
        ,elem.operator);
  }

  /**
   * Use a function
   */
  public void testGene_4() {
    CompositeGene comp = new CompositeGene();
    Gene[] genes = new Gene[ 2 ];
    comp.addGene(new TestGene(3));
    genes[ 0 ] = comp;
    final int opNr = 2;
    try {
      Vector elems = constructTerms(genes);
    }catch (ArrayIndexOutOfBoundsException nex) {
    ;//this is OK
    }
  }
  public void testGene_5() {
    CompositeGene comp = new CompositeGene();
    Gene[] genes = new Gene[ 1 ];
    final int fktNr = 4;
    comp.addGene(new TestGene(fktNr));
    final int opNr = 2;
    comp.addGene(new TestGene(opNr));
    genes[ 0 ] = comp;
    Vector elems = constructTerms(genes);
    Term elem = (Term)elems.elementAt(0);
    assertEquals(Repository.getFunctions().get(fktNr), elem.termName);
    assertEquals( ( (String) Repository.getOperators().get(opNr)).charAt(0),
        elem.operator);
  }

  public static Vector constructTerms(Gene[] genes) {
    Vector result = new Vector();
    Term term;
    Gene[] geneTupel;
    CompositeGene comp;
    Gene gene1;
    Gene gene2;
    int totalTiefe = 0;
    for (int i = 0; i < genes.length; i++) {
      comp = (CompositeGene) genes[i];
      gene1 = comp.geneAt(0);
      gene2 = comp.geneAt(1);
      geneTupel = new Gene[] {gene1, gene2};
      term = constructTerm(geneTupel);

      //Alle Tiefen von Funktionen addieren (nicht die von Konstanten)
      if (term.termType == 2) {
        totalTiefe += term.depth;
      }

      result.add(term);
    }
    //Die Tiefe pro Term anpassen: Reicht die Anzahl der Terme insgesamt nicht aus,
    //dann alles pauschal k�rzen. Sind zu viele Terme da, dann alles pauschal
    //erh�hen. Zuerst alles relativ gleich stark k�rzen/erh�hen.
    //Dann ab dem ersten Term so lange die Terme durchlaufen und bei jedem die Tiefe
    //um 1 k�rzen/erh�hen, bis exakt alle Terme abgedeckt sind
    /**@todo folgendes wegkicken, oder?*/
    int termAnz = result.size();
    int pauschalDazu;
    boolean negativ;
    if (totalTiefe > 0) {
      if (termAnz < totalTiefe) {
        pauschalDazu = totalTiefe / termAnz - 1;
        negativ = true;
      }
      else {
        pauschalDazu = termAnz / totalTiefe - 1;
        negativ = false;
      }
    }
    else {
      //what to do here?
      pauschalDazu = 0;
      negativ = false;
    }
    int restTiefe;
    if (negativ) {
      restTiefe = totalTiefe - termAnz * pauschalDazu;
    }
    else {
      restTiefe = termAnz - totalTiefe * pauschalDazu;
    }
    /**@todo folgendes wegkicken, oder?*/
    if (false && (restTiefe > 0 || pauschalDazu > 0)) {
      //tiefenpunkt verteilen auf Formelterme
      for (int i = 0; i < termAnz; i++) {
        term = (Term) result.elementAt(i);
        if (term.termType == 2) {
          if (!negativ) {
            term.depth += 1 + pauschalDazu;
            restTiefe--;
          }
          else {
            if (term.depth > 1) {
              if (restTiefe > 0) {
                term.depth -= 1;
                restTiefe--;
              }
              if (term.depth > 1 + pauschalDazu) {
                term.depth -= pauschalDazu;
              }
              else {
                if (term.depth > 1) {
                  int reduzier = term.depth - 1;
                  term.depth = 1;
                  restTiefe -= reduzier;
                }
              }
            }
          }
        }
      }
    }
    return result;
  }

  public static Term constructTerm(Gene[] genes) {
    Gene fkt = genes[ 0 ];
    Gene op = genes[ 1 ];
    Integer allele = (Integer) genes[ 0 ].getAllele();
    int fktNr = allele.intValue();
    fktNr = fktNr % (numberOfFunctions + numberOfConstants);
    String fktName;
    int type;
    if (fktNr >= numberOfFunctions) {
      //Konstante auslesen
      fktName = (String) Repository.getConstants().get(fktNr - numberOfFunctions);
      type = 1;
    }
    else {
      //Funktion auslesen
      fktName = (String) Repository.getFunctions().get(fktNr);
      type = 2;
    }

    allele = (Integer) op.getAllele();
    int opNr = allele.intValue();
    opNr = opNr % numberOfOperators;
    char opCh = ( (String) Repository.getOperators().get(opNr)).charAt(0);
    Term elem = new Term(type, fktName, 1, opCh);
    return elem;
  }

  /**
   * Gene that always returns the same (Integer) allele value
   * @version 1.0
   */
  private class TestGene
      implements Gene, Comparable, Serializable {
    private int value;

    public TestGene(int value) {
      this.value = value;
    }

    public Object getAllele() {
      return new Integer(value);
    }

    public void cleanup() {

    }

    public void setToRandomValue(RandomGenerator a_numberGenerator) {

    }

    public void setValueFromPersistentRepresentation(String a_representation)
        throws UnsupportedOperationException, UnsupportedRepresentationException {

    }

    public String getPersistentRepresentation()
        throws UnsupportedOperationException {
      return null;
    }

    public void setAllele(Object a_newValue) {

    }

    public Gene newGene(Configuration a_activeConfiguration) {
      return null;
    }

    public int compareTo(Object o) {
      return 0;
    }

    public void applyMutation(int index, double a_percentage) {

    }

    public int size() {
      return 1;
    }

    public Gene newGene() {
      return null;
    }

  }
}
