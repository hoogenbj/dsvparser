# DsvParser
This is DsvParser. It is a delimited-text parser that can handle any delimited text containing quoted strings, xml and even line terminators.
The requirement to parse a csv file that also contained xml documents in some of the fields turned out to be something none of the other
open source libraries (like opencsv, jcsv and others) that I tried, could handle. I was in a hurry, so I didn't have time to fork an OSS library
and make it work. I had an existing Finite State Machine-based parser that I was using in a personal project and found it easy to modify. A
friend suggested that I share it, so here it is.

Copyright 2013-2021 Johan Hoogenboezem

To use, download it or add to your pom.xml:
  	
```
<dependency>
	<groupId>za.co.clock24</groupId>
	<artifactId>DsvParser</artifactId>
	<version>2.0.1</version>
</dependency>
```

Then go to the wiki page here: https://github.com/hoogenbj/dsvparser/wiki

This library contains a module-info and has been tested with Java 16.
