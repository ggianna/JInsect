/*
 * Under LGPL
 * by George Giannakopoulos
 */

package gr.demokritos.iit.jinsect.structs;

import gr.demokritos.iit.conceptualIndex.structs.Distribution;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import salvo.jesus.graph.Edge;
import salvo.jesus.graph.GraphFactory;
import salvo.jesus.graph.GraphListener;
import salvo.jesus.graph.Vertex;
import salvo.jesus.graph.WeightedEdge;
import salvo.jesus.graph.WeightedGraph;
import salvo.jesus.graph.algorithm.GraphTraversal;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 *
 * @author ggianna
 */
public class UniqueVertexHugeGraph extends UniqueVertexGraph {

    public static final char VERTEX_LABEL_SEP = (char)26;
    protected UniqueVertexGraph[] UnderlyingGraphs;
//    ThreadQueue tRunner = new ThreadQueue();
    ExecutorService tRunner = Executors.newCachedThreadPool();

    public UniqueVertexHugeGraph(int iSegments) {
        UnderlyingGraphs = new UniqueVertexGraph[iSegments];
        for (int iCnt = 0; iCnt < iSegments; iCnt++) {
           UnderlyingGraphs[iCnt] = new UniqueVertexGraph();
        }
        UniqueVertices= new HashMap<String, Vertex>(1000000);
    }

    public final int getHash(String s) {
        int iRes = Math.abs(s.hashCode()) % UnderlyingGraphs.length;
        return iRes;
    }

    @Override
    public synchronized void add(Vertex v) throws Exception {
        // Add to self vertices, if not here already
        if (UniqueVertices.containsKey(v.getLabel()))
            return;
        else
            UniqueVertices.put(v.getLabel(), v);
        
//        // Add to graphs asynchronously
//        for (int iCnt = 0; iCnt < UnderlyingGraphs.length; iCnt++) {
//           final UniqueVertexGraph uArg = UnderlyingGraphs[iCnt];
//           final int iCntArg = iCnt;
//           final Vertex vArg = v;
//           tRunner.execute(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        uArg.add(vArg);
//                    } catch (Exception ex) {
//                        // Could not add. Not handled.
//                        Logger.getAnonymousLogger().log(Level.INFO,
//                                ex.getLocalizedMessage());
//                    }
//                }
//            });
//        }
    }

    @Override
    public synchronized Edge addEdge(Vertex vHead, Vertex vTail) throws Exception {
        String sHashKey = vHead.getLabel() + VERTEX_LABEL_SEP + vTail.getLabel();
        return UnderlyingGraphs[getHash(sHashKey)].addEdge(vHead, vTail);
    }

    @Override
    public synchronized WeightedEdge addEdge(Vertex vHead, Vertex vTail, double dWeight) throws Exception {
        String sHashKey = vHead.getLabel() + VERTEX_LABEL_SEP + vTail.getLabel();
        return UnderlyingGraphs[getHash(sHashKey)].addEdge(vHead, vTail, dWeight);
    }

    @Override
    public synchronized void addEdge(Edge edge) throws Exception {
        String sHashKey = edge.getVertexA().getLabel() + VERTEX_LABEL_SEP +
                edge.getVertexB().getLabel();
        UnderlyingGraphs[getHash(sHashKey)].addEdge(edge);
    }

    @Override
    public void addListener(GraphListener listener) {
        for (int iCnt = 0; iCnt < UnderlyingGraphs.length; iCnt++) {
            super.addListener(listener);
        }
    }

    @Override
    public boolean contains(Vertex v) {
        return UniqueVertices.containsKey(v.getLabel());
    }

    @Override
    public boolean containsEdge(Edge edge) {
        String sHashKey = edge.getVertexA().getLabel() + VERTEX_LABEL_SEP +
            edge.getVertexB().getLabel();
        return UnderlyingGraphs[getHash(sHashKey)].containsEdge(edge);

    }

    @Override
    public boolean containsVertex(Vertex v) {
        return UniqueVertices.containsKey(v.getLabel());
    }

    @Override
    public List getAdjacentVertices(Vertex v) {
        ArrayList<Vertex> lRes = new ArrayList<Vertex>();
        for (int iCnt=0; iCnt < UnderlyingGraphs.length; iCnt++) {
            lRes.addAll(UnderlyingGraphs[iCnt].getAdjacentVertices(v));
        }
        return lRes;
    }

    @Override
    public int getDegree(Vertex v) {
        int iDegree = 0;
        for (int iCnt=0; iCnt < UnderlyingGraphs.length; iCnt++) {
            if (UnderlyingGraphs[iCnt].contains(v))
                iDegree += UnderlyingGraphs[iCnt].getDegree(v);
        }
        return iDegree;
    }

    @Override
    public Set getEdgeSet() {
        HashSet<Edge> hRes = new HashSet<Edge>();
        for (int iCnt=0; iCnt < UnderlyingGraphs.length; iCnt++) {
            hRes.addAll(UnderlyingGraphs[iCnt].getEdgeSet());
        }
        return Collections.unmodifiableSet(hRes);
    }

    @Override
    public List getEdges(Vertex v) {
        ArrayList<Edge> lRes = new ArrayList<Edge>();
        for (int iCnt=0; iCnt < UnderlyingGraphs.length; iCnt++) {
            lRes.addAll(UnderlyingGraphs[iCnt].getEdges(v));
        }
        return lRes;
    }

    @Override
    public int getEdgesCount() {
        int iEdgeCount = 0;
        for (int iCnt=0; iCnt < UnderlyingGraphs.length; iCnt++) {
            iEdgeCount += UnderlyingGraphs[iCnt].getEdgesCount();
        }
        return iEdgeCount;
    }

    public Distribution<Double> getEdgeCountDistro() {
        Distribution<Double> dRes = new Distribution();
        for (int iCnt=0; iCnt < UnderlyingGraphs.length; iCnt++) {
            dRes.setValue((double)iCnt, UnderlyingGraphs[iCnt].getEdgesCount());
        }

        return dRes;
    }
    @Override
    public Set getVertexSet() {
        HashSet<Vertex> hRes = new HashSet<Vertex>(UniqueVertices.values());
        return Collections.unmodifiableSet(hRes);
    }

    @Override
    public int getVerticesCount() {
        int iVerticesCount = 0;
        for (int iCnt=0; iCnt < UnderlyingGraphs.length; iCnt++) {
            iVerticesCount += UnderlyingGraphs[iCnt].getVerticesCount();
        }
        return iVerticesCount;
    }

    @Override
    public Iterator getVerticesIterator() {
        return UniqueVertices.values().iterator();
    }

    @Override
    public synchronized Vertex locateVertex(String sVertexLabel) {
        return UniqueVertices.get(sVertexLabel);
    }

    @Override
    public synchronized Vertex locateVertex(Vertex v) {
        return locateVertex(v.getLabel());
    }

    @Override
    public void remove(Vertex v) throws Exception {
        for (int iCnt=0; iCnt < UnderlyingGraphs.length; iCnt++) {
            UnderlyingGraphs[iCnt].remove(v);
        }
        super.remove(v);
    }

    @Override
    public void removeEdge(Edge edge) throws Exception {
        for (int iCnt=0; iCnt < UnderlyingGraphs.length; iCnt++) {
            UnderlyingGraphs[iCnt].removeEdge(edge);
        }
    }

    @Override
    public void removeEdges(Vertex v) throws Exception {
        for (int iCnt=0; iCnt < UnderlyingGraphs.length; iCnt++) {
            UnderlyingGraphs[iCnt].removeEdges(v);
        }
        
    }

    ///////////////////////////////////////////////////
    // Not implemented.
    // TODO: Implement?
    ///////////////////////////////////////////////////
    @Override
    public void forgetConnectedSets() {
        throw new NotImplementedException();
    }

    @Override
    public Vertex getClosest(Vertex v) {
        throw new NotImplementedException();
    }

    @Override
    public int getDegree() {
        throw new NotImplementedException();
    }

    @Override
    public Collection getConnectedSet() {
        throw new NotImplementedException();
    }

    @Override
    public Set getConnectedSet(Vertex v) {
        throw new NotImplementedException();
    }

    @Override
    public GraphFactory getGraphFactory() {
        throw new NotImplementedException();
    }

    @Override
    public WeightedGraph shortestPath(Vertex vertex) {
        throw new NotImplementedException();
    }

    @Override
    public List traverse(Vertex startat) {
        throw new NotImplementedException();
    }


    @Override
    public List cloneVertices() {
        throw new NotImplementedException();
    }

    @Override
    public Set getAdjacentVertices(List vertices) {
        throw new NotImplementedException();
    }

    @Override
    public GraphTraversal getTraversal() {
        throw new NotImplementedException();
    }

    @Override
    public boolean isConnected(Vertex v1, Vertex v2) {
        throw new NotImplementedException();
    }


}
