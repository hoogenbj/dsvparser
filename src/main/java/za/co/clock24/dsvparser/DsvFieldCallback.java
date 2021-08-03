/*
 * Copyright 2013 Johan Hoogenboezem
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package za.co.clock24.dsvparser;

/**
 * 
 * A callback implementation can do whatever, but it cannot filter the field from processing as a
 * DsvRecordCallback can do with lines/records.
 * 
 * @author Johan Hoogenboezem
 *
 */
public interface DsvFieldCallback {

	void processingField(String field);
	
}
