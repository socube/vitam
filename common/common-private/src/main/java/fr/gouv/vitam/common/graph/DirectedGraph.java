/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2019)
 *
 * contact.vitam@culture.gouv.fr
 * 
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitam.common.graph;



import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;

/**
 * DirectedGraph
 */
// TODO merge Graph and DirectedGraph since most of the code is the same
public class DirectedGraph {

    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(DirectedGraph.class);

    private static final String NEWLINE = System.getProperty("line.separator");
    // number of vertices
    private int vertices;
    // adjacency list for vertex v
    private NodeIterable<Integer>[] adj;
    // indegree of vertex v
    private int[] indegree;

    // map id_xml index
    BidiMap<Integer, String> indexMapping;

    int count = 0;

    /**
     * Initializes an empty DirectedGraph with n vertices.
     *
     * @param vertices the number of vertices
     * @throws IllegalArgumentException if V < 0
     */
    private DirectedGraph(int vertices) {
        if (vertices < 0) {
            throw new IllegalArgumentException("Number of vertices in a DirectedGraph must be nonnegative");
        }
        this.vertices = vertices;
        indegree = new int[vertices];
        adj = (NodeIterable<Integer>[]) new NodeIterable[vertices];
        for (int v = 0; v < vertices; v++) {
            adj[v] = new NodeIterable<Integer>();
        }
    }

    /**
     * Initializes a DirectedGraph from the specified JsonNode.
     *
     * @param in the input stream
     * @throws IndexOutOfBoundsException if the endpoints of any edge are not in prescribed range
     * @throws IllegalArgumentException if the number of vertices or edges is negative
     */

    public DirectedGraph(JsonNode jsonGraph) {
        Iterator<Entry<String, JsonNode>> iterator = jsonGraph.fields();
        indexMapping = new DualHashBidiMap<Integer, String>();
        vertices = 1;
        // FIXME use jsonGraph.size();
        while (iterator.hasNext()) {
            vertices++;
            iterator.next();
        }
        adj = (NodeIterable<Integer>[]) new NodeIterable[vertices];

        for (int v = 0; v < vertices; v++) {
            adj[v] = new NodeIterable<Integer>();
        }
        indegree = new int[vertices];
        // parse json to create graph
        int i = 0;
        Iterator<Entry<String, JsonNode>> iterator2 = jsonGraph.fields();
        while (iterator2.hasNext()) {
            Entry<String, JsonNode> cycle = iterator2.next();
            i++;
            String idChild = cycle.getKey();
            JsonNode up = cycle.getValue();

            // create mappping
            addMapIdToIndex(idChild);
            // indexMapping.put(i, idChild);

            if (up != null && up.size() > 0) {
                final JsonNode arrNode = up.get("_up");

                for (final JsonNode idParent : arrNode) {


                    addEdge(getIndex(idParent.textValue()), getIndex(idChild));

                    LOGGER.info("source:" + idParent);
                    LOGGER.info("destin:" + idChild);

                }

            }
        }
    }

    /**
     * Returns the number of vertices in this DirectedGraph.
     *
     * @return the number of vertices in this DirectedGraph
     */
    public int getVertices() {
        return vertices;
    }


    // throw an IndexOutOfBoundsException unless 0 <= v < V
    private void validateVertex(int v) {
        if (v < 0 || v >= vertices) {
            throw new IndexOutOfBoundsException("vertex " + v + " is not between 0 and " + (vertices - 1));
        }
    }

    /**
     * Adds the directed edge v->w to this DirectedGraph.
     *
     * @param v the tail vertex
     * @param w the head vertex
     * @throws IndexOutOfBoundsException unless both 0 <= v < V and 0 <= w < vertices
     */
    // FIXME private
    public void addEdge(int v, int w) {
        validateVertex(v);
        validateVertex(w);
        adj[v].add(w);
        indegree[w]++;
    }

    /**
     * Returns the vertices adjacent from vertex <tt>vertices</tt> in this DirectedGraph.
     *
     * @param v the vertex
     * @return the vertices adjacent from vertex <tt>vertices</tt> in this DirectedGraph, as an iterable
     * @throws IndexOutOfBoundsException unless 0 <= v < V
     */
    // FIXME private
    public Iterable<Integer> adj(int v) {
        validateVertex(v);
        return adj[v];
    }

    /**
     * Returns the number of directed edges incident to vertex <tt>vertices</tt>. This is known as the <em>indegree</em>
     * of vertex <tt>vertices</tt>.
     *
     * @param vertices the vertex
     * @return the indegree of vertex <tt>vertices</tt>
     * @throws IndexOutOfBoundsException unless 0 <= v < vertices
     */
    public int indegree(int vertices) {
        validateVertex(vertices);
        return indegree[vertices];
    }

    /**
     * Returns the reverse of the DirectedGraph.
     *
     * @return the reverse of the DirectedGraph (child[parents] to parent[children])
     */
    public DirectedGraph reverse() {
        DirectedGraph reverse = new DirectedGraph(vertices);
        for (int v = 0; v < vertices; v++) {
            for (int w : adj(v)) {
                reverse.addEdge(w, v);
            }
        }
        // TODO clean memory ?
        return reverse;
    }

    /**
     * Returns a string representation of the graph.
     *
     * @return the number of vertice, adjacency lists
     */
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(vertices + " vertices,  " + NEWLINE);
        for (int v = 0; v < vertices; v++) {
            s.append(String.format("%d: ", v));
            for (int w : adj[v]) {
                s.append(String.format("%d ", w));
            }
            s.append(NEWLINE);
        }
        return s.toString();
    }

    private int getIndex(String id) {
        int key = 0;
        if (indexMapping != null) {
            // FIXME better to directly get the private xmlIdToIndex.get(id) and checking if not null
            if (indexMapping.containsValue(id)) {
                BidiMap<String, Integer> xmlIdToIndex = indexMapping.inverseBidiMap();
                key = xmlIdToIndex.get(id);
            } else {
                key = addMapIdToIndex(id);
            }

            return key;
        }
        return key;
    }

    private int addMapIdToIndex(String idXml) {
        if (indexMapping != null) {
            // FIXME better to directly get the private xmlIdToIndex.get(id) and checking if not null
            BidiMap<String, Integer> xmlIdToIndex = indexMapping.inverseBidiMap();
            if (!xmlIdToIndex.containsKey(idXml)) {
                count++;
                indexMapping.put(count, idXml);
            }
        }
        return count;
    }
}
