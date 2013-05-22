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
package hoogenbj;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

public class DsvParserTest {
	private static Logger L = Logger.getLogger(DsvParserTest.class);

	@Test
	public void test() throws IOException {
		StringReader reader = new StringReader("");
		DsvParser<String[]> parser = DsvParser.createWithDefaultParser(reader);
		List<String[]> list = parser.readAll();
		assertNotNull(list);
		assertTrue(list.size() == 0);
	}

	@Test
	public void testPlain() throws IOException {
		StringReader reader = new StringReader("1,2,3,4");
		DsvParser<String[]> parser = DsvParser.createWithDefaultParser(reader);
		List<String[]> list = parser.readAll();
		assertNotNull(list);
		assertTrue(list.size() == 1);
		assertTrue(list.get(0).length == 4);
	}

	static class Person {
		public int employeeNumber = -1;
		public String emailAddress;
		public String surname;
		public String firstName;
	}
	
	@Test
	public void testCustomRecord() throws IOException {
		StringReader reader = new StringReader("1,\"email@example.org\",\"Joe\",\"Citizen\"");
		DsvParser<Person> parser = new DsvParser<DsvParserTest.Person>(reader, new DsvRecordParser<Person>() {
			public Person parseRecord(String[] fields) {
				Person person = new Person();
				person.emailAddress = fields[1];
				person.employeeNumber = Integer.valueOf(fields[0]);
				person.firstName = fields[2];
				person.surname = fields[3];
				return person;
			}
		});
		List<Person> list = parser.readAll();
		assertNotNull(list);
		assertTrue(list.size() == 1);
		assertNotNull(list.get(0));
		Person person = list.get(0);
		assertNotNull(person.emailAddress);
		assertNotNull(person.employeeNumber);
		assertNotNull(person.firstName);
		assertNotNull(person.surname);
		assertFalse(person.employeeNumber == -1);
		assertEquals(person.emailAddress, "email@example.org");
		assertEquals(person.surname, "Citizen");
		assertEquals(person.firstName, "Joe");
		assertTrue(person.employeeNumber == 1);
	}

	int fieldCallbackCount = 0;
	@Test
	public void testFieldCallback() throws IOException {
		StringReader reader = new StringReader("1,2,3,4");
		DsvParser<String[]> parser = DsvParser.createWithDefaultParser(reader);
		parser.setFieldCallback(new DsvFieldCallback() {
			public void processingField(String field) {
				fieldCallbackCount++;
			}
		});
		List<String[]> list = parser.readAll();
		assertNotNull(list);
		assertTrue(list.size() == 1);
		assertTrue(list.get(0).length == 4);
		assertTrue(fieldCallbackCount == 4);
	}

	int recordCallbackCount = 0;
	@Test
	public void testRecordCallback() throws IOException {
		StringReader reader = new StringReader("1,2,3,4\n5,6,7,8");
		DsvParser<String[]> parser = DsvParser.createWithDefaultParser(reader);
		// Test filtering of first line, possibly a header line
		parser.setRecordCallback(new DsvRecordCallback<String[]>() {
			public boolean processingRecord(String[] t) {
				recordCallbackCount++;
				if (recordCallbackCount == 1)
					return false;
				else
					return true;
			}
		});
		List<String[]> list = parser.readAll();
		assertNotNull(list);
		assertTrue(list.size() == 1);
		assertTrue(list.get(0).length == 4);
		assertTrue(recordCallbackCount == 2);
		assertTrue(list.get(0)[0].equals("5"));
		assertTrue(list.get(0)[1].equals("6"));
		assertTrue(list.get(0)[2].equals("7"));
		assertTrue(list.get(0)[3].equals("8"));
	}

	@Test
	public void testStringFields() throws IOException {
		StringReader reader = new StringReader("1,\"2\",3,4");
		DsvParser<String[]> parser = DsvParser.createWithDefaultParser(reader);
		List<String[]> list = parser.readAll();
		assertNotNull(list);
		assertTrue(list.size() == 1);
		assertTrue(list.get(0).length == 4);
	}

	@Test
	public void testStringFieldsEmbeddedComma() throws IOException {
		StringReader reader = new StringReader("1,\"2.1,2.2\",3,4");
		DsvParser<String[]> parser = DsvParser.createWithDefaultParser(reader);
		List<String[]> list = parser.readAll();
		assertNotNull(list);
		assertTrue(list.size() == 1);
		assertTrue(list.get(0).length == 4);
	}

	@Test
	public void testStringFieldsEmbeddedXml() throws IOException {
		StringReader reader = new StringReader("1,2,3,\"<element xml:ns=\"some-name-space\" someAttribute=\"some-attribute-value\">some-content</element>\"");
		DsvParser<String[]> parser = DsvParser.createWithDefaultParser(reader);
		List<String[]> list = parser.readAll();
		assertNotNull(list);
		assertTrue(list.size() == 1);
		assertTrue(list.get(0).length == 4);
		for (String string : list.get(0)) {
			L.debug("field="+string);
		}
	}

	@Test
	public void testStringFieldsEmbeddedXmlMultiLines() throws IOException {
		StringReader reader = new StringReader("1,2,3,\"<element xml:ns=\"some-name-space\" someAttribute=\"some-attribute-value\">some-content</element>\"\n" +
				"4,5,6,\"<element xml:ns=\"some-name-space2\" someAttribute=\"some-attribute-value2\">some-content2</element>\"");
		DsvParser<String[]> parser = DsvParser.createWithDefaultParser(reader);
		List<String[]> list = parser.readAll();
		assertNotNull(list);
		assertTrue(list.size() == 2);
		assertTrue(list.get(0).length == 4);
		assertTrue(list.get(1).length == 4);
		for (String string : list.get(1)) {
			L.debug("field="+string);
		}
	}

	@Test
	public void testStringFieldsEmbeddedXmlMultiLines2() throws IOException {
		StringReader reader = new StringReader("1,2,3,\"<element xml:ns=\"some-name-space\" someAttribute=\"some-attribute-value\">some-content</element>\"\n" +
				"4,5,6,\"<element xml:ns=\"some-name-space2\" someAttribute=\"some-attribute-value2\">some-content2</element>\"\r\n"+
				"7,8,9,\"<element xml:ns=\"some-name-space3\" someAttribute=\"some-attribute-value3\">some-content3</element>\"");
		DsvParser<String[]> parser = DsvParser.createWithDefaultParser(reader);
		List<String[]> list = parser.readAll();
		assertNotNull(list);
		assertTrue(list.size() == 3);
		assertTrue(list.get(0).length == 4);
		assertTrue(list.get(1).length == 4);
		assertTrue(list.get(2).length == 4);
		for (String string : list.get(2)) {
			L.debug("field="+string);
		}
	}

	@Test
	public void testStringFieldsWithEmbeddedLineTerminators() throws IOException {
		StringReader reader = new StringReader("1,2,3,\"4\n5\",\"6\r7\"");
		DsvParser<String[]> parser = DsvParser.createWithDefaultParser(reader);
		List<String[]> list = parser.readAll();
		assertNotNull(list);
		assertTrue(list.size() == 1);
		assertTrue(list.get(0).length == 5);
		for (String string : list.get(0)) {
			L.debug("field="+string);
		}
	}

	@Test
	public void testStringFieldsWithEmbeddedLineTerminatorsMultiLine() throws IOException {
		StringReader reader = new StringReader("1,2,3,\"4\n5\",\"6\r7\"\n" +
				"1,2,3,\"4\n5\",\"6\r7\"\r"+
				"1,2,3,\"4\n5\",\"6\r7\"\r\n"+
				"1,2,3,\"4\n5\",\"6\r7\"");
		DsvParser<String[]> parser = DsvParser.createWithDefaultParser(reader);
		List<String[]> list = parser.readAll();
		assertNotNull(list);
		assertTrue(list.size() == 4);
		assertTrue(list.get(0).length == 5);
		assertTrue(list.get(1).length == 5);
		assertTrue(list.get(2).length == 5);
		for (String string : list.get(0)) {
			L.debug("field="+string);
		}
	}

	@Test
	public void testEmptyFields() throws IOException {
		StringReader reader = new StringReader(",,,");
		DsvParser<String[]> parser = DsvParser.createWithDefaultParser(reader);
		List<String[]> list = parser.readAll();
		assertNotNull(list);
		assertTrue(list.size() == 1);
		assertTrue(list.get(0).length == 4);
	}

	@Test
	public void testEmptyFields2() throws IOException {
		StringReader reader = new StringReader(",\"s\",\"s\",\n");
		DsvParser<String[]> parser = DsvParser.createWithDefaultParser(reader);
		List<String[]> list = parser.readAll();
		assertNotNull(list);
		assertTrue(list.size() == 1);
		assertTrue(list.get(0).length == 4);
	}

}
