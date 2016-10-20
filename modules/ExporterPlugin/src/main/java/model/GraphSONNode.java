/*
 * Copyright 2016 Institute for System Programming of the Russian Academy of Sciences.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package model;

import java.util.Iterator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

/**
 *
 * @author Ivan Mashonskiy
 */
public class GraphSONNode {
    
    Iterator<Edge> edges = null;
    Node node = null;
    
    public GraphSONNode(Iterator<Edge> edges, Node node) {
        this.edges = edges;
        this.node = node;
    }
    
    public void setEdges(Iterator<Edge> edges) {
        this.edges = edges;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Iterator<Edge> getEdges() {
        return edges;
    }

    public Node getNode() {
        return node;
    }
}
