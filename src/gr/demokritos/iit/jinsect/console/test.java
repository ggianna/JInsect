/*
 * test.java
 *
 * Created on May 4, 2007, 2:40 PM
 *
 */

package gr.demokritos.iit.jinsect.console;

import gr.demokritos.iit.conceptualIndex.structs.Distribution;
import gr.demokritos.iit.jinsect.algorithms.statistics.statisticalCalculation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Utility class for testing purposes only. */
public class test {
    public static void main(String[] args) {
        Distribution<String> d = new Distribution<String>();
        d.setValue("F1", 9.7);
//        d.setValue("F2",5.0);
//        d.setValue("F3", 5.0);
        for (int iCnt=10; iCnt<20; iCnt++) {
            d.setValue("F" + iCnt, 0.25);
        }
// 1 VVImp, 1 VImp, 1 Imp, 10 Un
// Entropy 2.4989230680527736

// 1 VVImp, 1 VImp, 1 Imp, 1 Un
// Entropy 1.6411919536602873

// 1 VVImp, 0 VImp, 2 Imp, 1 Un
// Entropy 1.6374847246128807

        System.err.println(d.toString());
        double dEntropy = statisticalCalculation.entropy(
                d.getProbabilityDistribution());
        System.err.println(dEntropy);

        Distribution<String> dNorm = new Distribution<String>();
        for (String sKey: d.asTreeMap().keySet()) {
            dNorm.setValue(sKey, Math.pow(d.getValue(sKey), 
                    (dEntropy < 1.0) ? 1.0 : dEntropy));
        }
        System.err.println(dNorm);
        dEntropy = statisticalCalculation.entropy(
                dNorm.getProbabilityDistribution());
        System.err.println(dEntropy);
    }
    
}
