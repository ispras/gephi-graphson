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

import org.gephi.io.importer.api.FileType;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.io.importer.spi.FileImporterBuilder;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;


/**
 *
 * @author Ivan Mashonskiy
 */
@ServiceProvider(service = FileImporterBuilder.class)
public class GraphSONImporterBuilder implements FileImporterBuilder {
    
    @Override
    public String getName() {
        return "GraphSON importer";
    }
    
    @Override
    public FileType[] getFileTypes() {
        return new FileType[]{
            new FileType(".json", "JSON files"), 
            new FileType(".graphson", "GraphSON files")};
    }

    @Override
    public boolean isMatchingImporter(FileObject fo) {
        return fo.getExt().equalsIgnoreCase("json") || 
               fo.getExt().equalsIgnoreCase("graphson");
    }

    @Override
    public FileImporter buildImporter() {
        return new GraphSONImporter();
    }
}
