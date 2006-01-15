/*
 * This file is part of JGAP.
 *
 * JGAP offers a dual license model containing the LGPL as well as the MPL.
 *
 * For licencing information please see the file license.txt included with JGAP
 * or have a look at the top of class org.jgap.Chromosome which representatively
 * includes the JGAP license policy applicable for any file delivered with JGAP.
 */
package org.jgap.xml;

import org.jgap.*;
import org.jgap.impl.*;
import org.jgap.data.*;
import junit.framework.*;
import org.w3c.dom.*;

/**
 * Tests for XMLDocumentBuilder class
 *
 * @author Klaus Meffert
 * @since 1.0
 */
public class XMLDocumentBuilderTest
    extends JGAPTestCase {
  /** String containing the CVS revision. Read out via reflection!*/
  private final static String CVS_REVISION = "$Revision: 1.1 $";

  private final static String FILENAME_WRITE = "GAtestWrite.xml";

  public static Test suite() {
    TestSuite suite = new TestSuite(XMLDocumentBuilderTest.class);
    return suite;
  }

  /**
   * @throws Exception
   * @author Klaus Meffert
   * @since 2.6
   */
  public void testBuildDocument_0()
      throws Exception {
    XMLDocumentBuilder doc = new XMLDocumentBuilder();
    DataTreeBuilder builder = DataTreeBuilder.getInstance();
    Chromosome chrom = new Chromosome(new Gene[] {
                                      new IntegerGene(1, 5),
                                      new IntegerGene(1, 10)});
    chrom.getGene(0).setAllele(new Integer(1));
    chrom.getGene(1).setAllele(new Integer( -3));
    IDataCreators doc2 = builder.representChromosomeAsDocument(chrom);
    Document result = (Document)doc.buildDocument(doc2);
    assertEquals(null, result.getParentNode());
    assertEquals(1, result.getChildNodes().getLength());
    Node child = result.getChildNodes().item(0);
    assertEquals("chromosome", result.getNodeName());
    assertEquals(2, child.getChildNodes().getLength());
  }
}
