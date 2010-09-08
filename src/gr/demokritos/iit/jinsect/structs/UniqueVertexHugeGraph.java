/*
 * Under LGPL
 * by George Giannakopoulos
 */

package gr.demokritos.iit.jinsect.structs;

import java.util.HashMap;
import salvo.jesus.graph.Vertex;

/**
 *
 * @author ggianna
 */
public class UniqueVertexHugeGraph extends UniqueVertexGraph {

    public UniqueVertexHugeGraph() {
        UniqueVertices = new HashMap<String, Vertex>(1000000);
    }


}
