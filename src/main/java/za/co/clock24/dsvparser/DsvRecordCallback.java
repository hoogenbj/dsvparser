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
 * A callback can do whatever it wants with the line/record. Once done, if it returns false, the line/record will be filtered from the output.
 * Implement the callback to, for example, keep count of the lines processed, or to filter lines/records containing certain values.
 * 
 * @author Johan Hoogenboezem
 *
 * @param <T>
 */
public interface DsvRecordCallback<T> {
	
	boolean processingRecord(T t);

}
