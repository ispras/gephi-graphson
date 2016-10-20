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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import model.GraphSONNode;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

/**
 *
 * @author Ivan Mashonskiy
 */
public class GraphSONNodeSerializer implements JsonSerializer<GraphSONNode> {

    Integer idCounter = 0;
    JsonObject nodeObject = new JsonObject();
    
    static List<String> edgeMetaAttributes = Arrays.asList("labelE", "Label", "Weight", "Id", "Timeset");
    static List<String> nodeMetaAttributes = Arrays.asList("labelV", "Label", "Id", "Timeset");
    
    private boolean isEdgeMetaAttribute(String property) {
        if (edgeMetaAttributes.contains(property)) {
            return true;
        }
        return false;
    }
    
    private boolean isNodeMetaAttribute(String property) {
        if (nodeMetaAttributes.contains(property)) {
            return true;
        }
        return false;
    }
    
    private Map<String, List<Edge>> groupEdges(List<Edge> edges) {
        Map<String, List<Edge>> groupedEdges = new HashMap<>();
        edges.forEach(edge -> {
            
            String edgeType = edge.getAttribute("labelE").toString();
            if (!groupedEdges.containsKey(edgeType)) {
                List<Edge> edgesSublist = new ArrayList<>();
                edgesSublist.add(edge);
                groupedEdges.put(edgeType, edgesSublist);
            } else {
                groupedEdges.get(edgeType).add(edge);
            }
        });
        return groupedEdges;
    }
    
    private void addEdges(JsonSerializationContext jsc, Map<String, List<Edge>> groupedEdges, 
            String type) {
        if (groupedEdges.isEmpty()) {
            return;
        }
        JsonObject edgesObject = new JsonObject();
        groupedEdges.keySet().forEach(key -> {
            List<Edge> edgesSublist = groupedEdges.get(key);
            JsonArray edgesArray = new JsonArray();
            edgesSublist.forEach(edge -> {
                JsonObject edgeObject = new JsonObject();
                edgeObject.addProperty("id", edge.getId().toString());
                String edgeDirProperty = "inV";
                Integer nodeId = Integer.parseInt(edge.getTarget().getId().toString());
                if (type.equals("inE")) {
                    edgeDirProperty = "outV"; 
                    nodeId = Integer.parseInt(edge.getSource().getId().toString());
                }
                edgeObject.addProperty(edgeDirProperty, nodeId);
                
                Iterator<Column> edgeColumns = edge.getAttributeColumns().iterator();
                JsonObject edgePropertiesObject = new JsonObject();
                edgeColumns.forEachRemaining(property -> {
                    Object propertyValue = edge.getAttribute(property);
                    if (!isEdgeMetaAttribute(property.getTitle()) && propertyValue != null) {
                        edgePropertiesObject.add(property.getTitle(), jsc.serialize(propertyValue));
                    }
                });
                
                if (!edgePropertiesObject.entrySet().isEmpty()) {
                    edgeObject.add("properties", edgePropertiesObject);
                }
                edgesArray.add(edgeObject);
            });
            edgesObject.add(key, edgesArray);
        });
        nodeObject.add(type, edgesObject);
    }
    
    private void addPropertyValue(JsonSerializationContext jsc, 
            JsonArray propertyValuesArray, Object propertyValue) {
        JsonObject propertyValueObject = new JsonObject();
        propertyValueObject.addProperty("id", idCounter.toString());
        idCounter++;
        propertyValueObject.add("value", jsc.serialize(propertyValue));
        propertyValuesArray.add(propertyValueObject);
    }
    
    private void addNodeProperties(JsonSerializationContext jsc, Node node) {
        
        Iterator<Column> columns = node.getAttributeColumns().iterator();
        JsonObject nodePropertiesObject = new JsonObject();
        columns.forEachRemaining(property -> {
            Object propertyValue = node.getAttribute(property);
            if (!isNodeMetaAttribute(property.getTitle()) && propertyValue != null) {
                JsonArray propertyValuesArray = new JsonArray();
                List<String> values = new ArrayList<>(Arrays.asList(propertyValue.toString().split(",")));
                if (values.size() > 1) {
                    values.forEach(value -> {
                        addPropertyValue(jsc, propertyValuesArray, value);
                    });
                } else {
                    addPropertyValue(jsc, propertyValuesArray, propertyValue);
                }
                nodePropertiesObject.add(property.getTitle(), propertyValuesArray);
            }
        });
        if (!nodePropertiesObject.entrySet().isEmpty()) {
            nodeObject.add("properties", nodePropertiesObject);
        }
    }
    
    @Override
    public JsonElement serialize(GraphSONNode graphSONNode, Type type, JsonSerializationContext jsc) {
        
        nodeObject.addProperty("id", Integer.parseInt(graphSONNode.getNode().getId().toString()));
        nodeObject.addProperty("label", graphSONNode.getNode().getAttribute("labelV").toString());
        List<Edge> inE = new LinkedList<>();
        List<Edge> outE = new LinkedList<>();
        graphSONNode.getEdges().forEachRemaining( edge -> {
            if (edge.getSource().getId().toString().equals(graphSONNode.getNode().getId().toString())) {
                outE.add(edge);
            } else {
                inE.add(edge);
            }
        });
        addEdges(jsc, groupEdges(inE), "inE");
        addEdges(jsc, groupEdges(outE), "outE");
        addNodeProperties(jsc, graphSONNode.getNode());
        return nodeObject;
    }
    
}
