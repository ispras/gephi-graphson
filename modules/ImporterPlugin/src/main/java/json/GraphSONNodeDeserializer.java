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
package json;

import model.GraphSONNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gephi.io.importer.api.ColumnDraft;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.NodeDraft;

/**
 *
 * @author Ivan Mashonskiy
 */
public class GraphSONNodeDeserializer implements JsonDeserializer<GraphSONNode> {

    ContainerLoader container = null;
    NodeDraft nd = null;

    public GraphSONNodeDeserializer(ContainerLoader cl) {
        this.container = cl;
    }
    
    private Object getTypedValue(JsonPrimitive valueJson) {
        if (valueJson.isBoolean()) {
            return valueJson.getAsBoolean();
        } else if (valueJson.isNumber()) {
            return valueJson.getAsInt();
        } else {
            return valueJson.getAsString();
        }
    }
    
    private void setNodeProperties(JsonObject nodeObject) {
        Set<Map.Entry<String, JsonElement>> properties = nodeObject
                .getAsJsonObject("properties").entrySet();
        properties.forEach(property -> {
            JsonArray propertyAttributesArray = property.getValue().getAsJsonArray();
            List<Object> values = new ArrayList<>();
            propertyAttributesArray.forEach(propertyAttributes -> {
                values.add(getTypedValue(propertyAttributes.getAsJsonObject().get("value").getAsJsonPrimitive()));
            });
            if (values.size() > 1) {
                ColumnDraft cd = container.addNodeColumn(property.getKey(), String.class);
                cd.setTitle(property.getKey());
                nd.setValue(cd.getTitle(), values.toString().replace("[", "").replace("]", "").trim());
            } else {
                Object value = values.get(0);
                ColumnDraft cd = container.addNodeColumn(property.getKey(), value.getClass());
                cd.setTitle(property.getKey());
                nd.setValue(cd.getTitle(), value);
            }
        });
    }
    
    private List<EdgeDraft> getEdges(JsonObject nodeJson) {
        JsonObject inE = nodeJson.getAsJsonObject("inE");
        List<EdgeDraft> edges = new LinkedList<>();
        if (inE != null) {
            Set<Map.Entry<String, JsonElement>> edgesWithLabels = inE.entrySet();
            edgesWithLabels.forEach(labelEdgesPair -> {
                JsonArray edgesAttributesArray = labelEdgesPair.getValue().getAsJsonArray();
                edgesAttributesArray.forEach(edgeAttributesElement -> {
                    JsonObject edgeAttributesObject = edgeAttributesElement.getAsJsonObject();
                    EdgeDraft newEdge = container.factory().newEdgeDraft();
                    newEdge.setLabel(labelEdgesPair.getKey());
                    ColumnDraft cd = container.addEdgeColumn("labelE", String.class);
                    cd.setTitle("labelE");
                    newEdge.setValue(cd.getTitle(), labelEdgesPair.getKey());
                    newEdge.setTarget(nd);
                    NodeDraft sourceNode = container.getNode(edgeAttributesObject
                            .get("outV").getAsString());
                    newEdge.setSource(sourceNode);
                    setEdgeProperties(newEdge, edgeAttributesObject);
                    
                    edges.add(newEdge);
                });
            });
        }
        return edges;
    }
    
    private void setEdgeProperties(EdgeDraft edge, JsonObject edgeAttributesObject) {
        JsonObject edgeProperties = edgeAttributesObject.getAsJsonObject("properties");
        if (edgeProperties != null) {
            Set<Map.Entry<String, JsonElement>> edgePropertiesSet = 
                    edgeProperties.entrySet();
            edgePropertiesSet.forEach(property -> {
                Object value = getTypedValue(property.getValue().getAsJsonPrimitive());
                ColumnDraft cd = container.addEdgeColumn(property.getKey(), value.getClass());
                cd.setTitle(property.getKey());
                edge.setValue(cd.getTitle(), value);
            });
        }
    }
    
    @Override
    public GraphSONNode deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        JsonObject nodeObject = je.getAsJsonObject();
         
        nd = container.getNode(nodeObject.get("id").getAsString());
        ColumnDraft cd = container.addNodeColumn("labelV", String.class);
        cd.setTitle("labelV");
        nd.setValue(cd.getTitle(), nodeObject.get("label").getAsString());
        nd.setLabel(nodeObject.get("id").getAsString());
        
        setNodeProperties(nodeObject);
        List<EdgeDraft> edges = getEdges(nodeObject);
        
        return new GraphSONNode(edges, nd);
    }
    
}
