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
package importer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import model.GraphSONNode;
import json.GraphSONNodeDeserializer;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.Report;
import org.gephi.io.importer.spi.FileImporter;

/**
 *
 * @author Ivan Mashonskiy
 */
public class GraphSONImporter implements FileImporter {

    private BufferedReader reader;
    private ContainerLoader container;
    private Report report;
    private boolean cancel = false;

    GraphSONImporter() {

    }

    @Override
    public void setReader(Reader reader) {
        this.reader = new BufferedReader(reader);
    }

    @Override
    public boolean execute(ContainerLoader cl) {
        this.container = cl;
        this.report = new Report();

        Iterator<String> lines = reader.lines().iterator();
        Gson gson = new GsonBuilder().registerTypeAdapter(GraphSONNode.class, 
                new GraphSONNodeDeserializer(cl)).create();
        lines.forEachRemaining(ln -> {
            GraphSONNode graphSONNode = gson.fromJson(ln, GraphSONNode.class);
            List<EdgeDraft> edges = graphSONNode.getEdges();
            edges.forEach(edge -> container.addEdge(edge));
        });
        return !cancel;
    }

    @Override
    public ContainerLoader getContainer() {
        return container;
    }

    @Override
    public Report getReport() {
        return report;
    }

    public boolean cancel() {
        cancel = true;
        return true;
    }
}
