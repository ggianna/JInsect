/*
 * test.java
 *
 * Created on May 4, 2007, 2:40 PM
 *
 */

package gr.demokritos.iit.jinsect.console;

import gr.demokritos.iit.jinsect.documentModel.comparators.NGramCachedGraphComparator;
import gr.demokritos.iit.jinsect.documentModel.representations.DocumentNGramGraph;
import gr.demokritos.iit.jinsect.documentModel.representations.DocumentNGramSymWinGraph;

/** Utility class for testing purposes only. */
public class test {
    public static void main(String[] args) {
        DocumentNGramGraph ngs1 = new DocumentNGramGraph();
        ngs1.setDataString("abcdef");

        DocumentNGramGraph ngs2 = new DocumentNGramGraph();
        ngs2.setDataString("abcdef");

        System.out.println(new NGramCachedGraphComparator().getSimilarityBetween(ngs1,
                ngs2).getOverallSimilarity());
    }
    
}
