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
package exporter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import json.GraphSONNodeSerializer;
import model.GraphSONNode;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.io.exporter.spi.CharacterExporter;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.project.api.Workspace;
import org.openide.util.Exceptions;

/**
 *
 * @author Ivan Mashonskiy
 */
public class GraphSONExporter implements GraphExporter, CharacterExporter {

    private boolean exportVisible = false;
    private Workspace workspace;
    private Writer writer;
    
    @Override
    public void setExportVisible(boolean exportVisible) {
        this.exportVisible = exportVisible;
    }

    @Override
    public boolean isExportVisible() {
        return exportVisible;
    }

    @Override
    public boolean execute() {
        GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);
        Graph graph;
        if (exportVisible) {
           graph = graphModel.getGraphVisible();
        } else {
           graph = graphModel.getGraph();
        }
        Gson gson = new GsonBuilder().registerTypeAdapter(GraphSONNode.class, 
                new GraphSONNodeSerializer()).create();
        Iterator<Node> nodes = graph.getNodes().iterator();
        while (nodes.hasNext()) {
            Node node = nodes.next();
            Iterator<Edge> edges = graph.getEdges(node).iterator();
            GraphSONNode graphSONNode = new GraphSONNode(edges, node);     
            String graphSONNodeJson = gson.toJson(graphSONNode);
            try {
                writer.write(graphSONNodeJson + "\n");
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return true;
    }

    @Override
    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }

    @Override
    public void setWriter(Writer writer) {
        this.writer = writer;
    }
    
}
