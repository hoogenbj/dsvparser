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


import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.ArrayStack;

/**
 * 
 * This parser tries to implement the "standard" for csv processing, at least as far as the Wikipedia article describes it.
 * It can handle xml embedded into fields as well as embedded delimiters and embedded quoted strings. It also handles line
 * terminators in fields.
 * 
 * @author Johan Hoogenboezem
 *
 */
public class DsvParser<T> {
	private static final int EOF = -1;
	private static final char CARRIAGE_RETURN = '\r';
	private static final char NEW_LINE = '\n';

	private Reader reader;
	private List<T> lines = new ArrayList<T>();
	private List<String> line;
	private StringWriter fieldWriter;
	private ArrayStack stack = new ArrayStack();
	private char delimeter = ',';
	private char quote = '"';
	private DsvRecordParser<T> dsvRecordParser;
	private DsvFieldCallback fieldCallback;
	private DsvRecordCallback<T> recordCallback;

	/**
	 * Set a callback object that will be invoked after every field is processed. 
	 * 
	 * @param fieldCallback
	 */
	public void setFieldCallback(DsvFieldCallback fieldCallback) {
		this.fieldCallback = fieldCallback;
	}

	/**
	 * Set a callback object that will be invoked after every record/line is processed,
	 * but before the line/record is added to the results.
	 *  
	 * @param recordCallback
	 */
	public void setRecordCallback(DsvRecordCallback<T> recordCallback) {
		this.recordCallback = recordCallback;
	}

	/**
	 * If you use the default parser implementation, this is the record parser which will be used.
	 * 
	 * @author johan
	 *
	 */
	public static class DefaultRecordParser implements DsvRecordParser<String[]> {
		public String[] parseRecord(String[] fields) {
			return fields;
		}
	}

	/**
	 * A convenience method for constructing and returning a default parser implementation
	 * which parses the fields as an array of strings.
	 * 
	 * @param reader
	 * @return
	 */
	public static DsvParser<String[]> createWithDefaultParser(Reader reader) {
		DsvParser<String[]> parser = new DsvParser<String[]>(reader, new DefaultRecordParser());
		return parser;
	}
	
	/**
	 * If you want to specify your own record parser to provide you with read-made records
	 * in the results, then use this constructor.
	 * 
	 * @param reader
	 * @param parser
	 */
	public DsvParser(Reader reader, DsvRecordParser<T> parser) {
		if (!reader.markSupported())
			this.reader = new BufferedReader(reader);
		else
			this.reader = reader;
		this.dsvRecordParser = parser;
	}
	
	private abstract static class State {
		abstract State process(DsvParser<?> parser) throws IOException;
	}

	private static class StartState extends State {

		State process(DsvParser<?> parser) throws IOException {
			parser.reader.mark(1);
			int character = parser.reader.read();
			if (character == EOF)
				return new EndState();
			else {
				parser.reader.reset();
				return new LineStartState();
			}
		}
	}

	private static class LineStartState extends State {
		State process(DsvParser<?> parser) throws IOException {
			parser.reader.mark(1);
			int character = parser.reader.read();
			if (character == parser.quote) {
				parser.line = new ArrayList<String>();
				return new StartStringFieldState();
			}
			else {
				parser.reader.reset();
				parser.line = new ArrayList<String>();
				return new StartFieldState();
			}
		}
	}

	private static class EndState extends State {
		State process(DsvParser<?> parser) {
			return null;
		}
	}
	
	private static class DelimiterState extends State {

		State process(DsvParser<?> parser) throws IOException {
			parser.reader.mark(1);
			int character = parser.reader.read();
			if (character == EOF) {
				parser.newField();
				parser.addField();
				parser.addLine();
				return new EndState();
			}
			else if (character == parser.quote) {
				return new StartStringFieldState();
			}
			else if (character == CARRIAGE_RETURN) {
				parser.newField();
				parser.addField();
				return new CarriageReturnState();
			}
			else if (character == NEW_LINE) {
				parser.newField();
				parser.addField();
				return new LineFeedState();
			}
			else {
				parser.reader.reset();
				return new StartFieldState();
			}
		}
	}
	
	private static class LineFeedState extends State {
		State process(DsvParser<?> parser) throws IOException {
			parser.reader.mark(1);
			int character = parser.reader.read();
			if (character == EOF) {
				parser.addLine();
				return new EndState();
			}
			else {
				parser.addLine();
				parser.reader.reset();
				return new LineStartState();
			}
		}
	}
	
	private static class CarriageReturnState extends State {
		State process(DsvParser<?> parser) throws IOException {
			parser.reader.mark(1);
			int character = parser.reader.read();
			if (character == EOF) {
				parser.addLine();
				return new EndState();
			}
			else if (character == NEW_LINE) {
				return new LineFeedState();
			}
			else {
				parser.addLine();
				parser.reader.reset();
				return new LineStartState();
			}
		}
	}
	
	private static class StartStringFieldState extends State {
		State process(DsvParser<?> parser) throws IOException {
			parser.newField();
			return new StringFieldState();
		}
	}
	
	private static class StartFieldState extends State {
		State process(DsvParser<?> parser) throws IOException {
			parser.newField();
			return new FieldState();
		}
	}
	
	private static class StringFieldState extends State {
		State process(DsvParser<?> parser) throws IOException {
			int character = parser.reader.read();
			if (character == EOF) {
				parser.addField();
				parser.addLine();
				return new EndState();
			}
			else if (character == parser.quote) {
				parser.reader.mark(1);
				int next = parser.reader.read();
				if (next == EOF) {
					parser.addField();
					parser.addLine();
					return new EndState();
				}
				else if (next == parser.delimeter) {
					parser.addField();
					return new DelimiterState();
				}
				else if (next == CARRIAGE_RETURN) {
					parser.addField();
					return new CarriageReturnState();
				}
				else if (next == NEW_LINE) { 
					parser.addField();
					return new LineFeedState();
				}
				else {
					parser.writeToField(character);
					parser.reader.reset();
					parser.stack.push(this);
					return new EmbeddedStringState();
				}
			}
			else {
				parser.writeToField(character);
				return this;
			}
		}
	}
	
	private static class FieldState extends State {
		State process(DsvParser<?> parser) throws IOException {
			int character = parser.reader.read();
			if (character == EOF) {
				parser.addField();
				parser.addLine();
				return new EndState();
			}
			else if (character == parser.quote) {
				parser.writeToField(character);
				parser.stack.push(this);
				return new EmbeddedStringState();
			}
			else if (character == parser.delimeter) {
				parser.addField();
				return new DelimiterState();
			}
			else if (character == CARRIAGE_RETURN) {
				parser.addField();
				return new CarriageReturnState();
			}
			else if (character == NEW_LINE) { 
				parser.addField();
				return new LineFeedState();
			}
			else {
				parser.writeToField(character);
				return this;
			}
		}
	}
	
	private static class EmbeddedStringState extends State {
		State process(DsvParser<?> parser) throws IOException {
			int character = parser.reader.read();
			if (character == EOF) {
				parser.addField();
				parser.addLine();
				return new EndState();
			}
			else if (character == parser.quote) {
				parser.writeToField(character);
				return (State) parser.stack.pop();
			}
			else {
				parser.writeToField(character);
				return this;
			}
		}
	}
	
	private void newField() {
		this.fieldWriter = new StringWriter();
	}

	private void writeToField(int character) {
		this.fieldWriter.write(character);
	}

	/**
	 * Once you have instantiated the parser and configured it (i.e. by setting callbacks),
	 * you call this method to start the parsing.
	 * 
	 * @return
	 * @throws IOException
	 */
	public List<T> readAll() throws IOException {
		State s = new StartState();
		while (!(s instanceof EndState)) {
			s = s.process(this);
		}
		return lines;
	}
	
	private void addField() {
		String field = this.fieldWriter.toString();
		if (this.fieldCallback != null)
			this.fieldCallback.processingField(field);
		line.add(field);
	}
	
	private void addLine() {
		T record = dsvRecordParser.parseRecord(line.toArray(new String[0]));
		boolean filter = false;
		if (this.recordCallback != null) {
			filter = !this.recordCallback.processingRecord(record);
		}
		if (!filter)
			lines.add(record);
	}
	
	/**
	 * Call this method to specify a delimiter different from a comma. Returns
	 * the parser instance so you can do method chaining.
	 * 
	 * @param delimeter
	 * @return
	 */
	public DsvParser<T> useDelimiter(char delimeter) {
		this.delimeter = delimeter;
		return this;
	}
	
	/**
	 * Call this method to specify a different character for quoting strings with.
	 * Returns the parser instance so you can do method chaining.
	 * 
	 * @param quote
	 * @return
	 */
	public DsvParser<T> useQuoteCharacter(char quote) {
		this.quote = quote;
		return this;
	}
}