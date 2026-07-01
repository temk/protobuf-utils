# protobuf-utils

Java utilities for reading, converting, searching, and filtering [Protocol Buffers](https://protobuf.dev/) message streams.

`protobuf-utils` provides two main command-line tools:

* **PCat** — convert protobuf message streams between binary/text and compressed/plain formats.
* **PGrep** — search protobuf message streams using a small query language with support for nested fields, repeated fields, previous-message references, and regular expressions.

The project is useful for debugging protobuf data pipelines, inspecting binary message streams, and extracting matching protobuf messages from large files.

## Features

* Read protobuf messages from plain or gzip-compressed streams.
* Work with both binary and text protobuf formats.
* Search messages with boolean expressions.
* Access nested fields and typed sub-messages.
* Match strings using regular expressions.
* Refer to previous messages in a stream.
* Print selected fields or complete matching messages.
* Use as command-line tools or as a Java library.

## Example use cases

* Inspect a compressed binary protobuf stream as text.
* Find all messages matching a field condition.
* Extract only selected fields from matching messages.
* Search for patterns across consecutive messages.
* Debug protobuf-based logging, storage, or messaging systems.

## Repository structure

```text
.
├── precompiled/   # Prebuilt jar and sample protobuf data
├── src/           # Java source code and ANTLR grammar
├── pom.xml        # Maven build configuration
└── README.md
```

## Requirements

* Java 7 or newer
* Maven
* Protocol Buffers Java runtime

The project is built with Maven and uses ANTLR for the query language parser.

## Build

```bash
mvn clean package
```

This builds the project jar and a jar with dependencies.

## Precompiled examples

The `precompiled` directory contains:

* `protobuf-utils-1.0-SNAPSHOT-jar-with-dependencies.jar`
* `Geom.proto` — example protobuf schema
* `shapes.bin.gz` — example compressed binary protobuf message stream
* helper scripts for running the command-line tools

You can run the examples from inside the `precompiled` directory.

## PCat

`PCat` converts protobuf message streams between binary/text and compressed/plain formats.

### Usage

```bash
java -cp <classpath> net.katros.services.proto.PCat [options] <root-message> [<input-file>|-]*
```

### Options

```text
-z    input is gzip-compressed
-t    input is text protobuf format
-b    input is binary protobuf format

-Z    output should be gzip-compressed
-T    output should be text protobuf format
-B    output should be binary protobuf format
```

Input format options `-t` and `-b` are mutually exclusive.
Output format options `-T` and `-B` are mutually exclusive.

### Example: print a compressed binary stream as text

```bash
cd precompiled

java -cp geom.jar:protobuf-utils-1.0-SNAPSHOT-jar-with-dependencies.jar \
  net.katros.services.proto.PCat \
  -zbT Shape shapes.bin.gz
```

## PGrep

`PGrep` searches protobuf message streams using query expressions.

### Usage

```bash
java -cp <classpath> net.katros.services.proto.PGrep [options] <input-file>*
```

### Options

```text
-q <query-expression>    query expression; required
-p <print-expression>    expression to print; optional, default is "$"
-d <delimiter>           output delimiter; optional
-o <output-file>         output file; optional, default is stdout
-m <message-name>        root protobuf message name; required

-z                       input is gzip-compressed
-Z                       output should be gzip-compressed

-t                       input is text protobuf format
-T                       output should be text protobuf format

-b                       input is binary protobuf format
-B                       output should be binary protobuf format

-a                       print all matching appearances
-D                       dry run: validate query syntax without processing data
```

## Query language basics

```text
$      current message
$$     previous message
$$$    message before the previous one
=~     regular expression match
```

Most Java-style boolean and arithmetic operators can be used in expressions.

Nested fields can be accessed with dot notation:

```text
shape@Square.width
shape@Square.corner.x
```

Brackets can be used to group expressions:

```text
shape@Square.{width > corner.{x * y}}
```

## Examples

Run the examples from the `precompiled` directory.

### Find all polygons

```bash
./proto-grep -zbmq Shape 'type == "POLYGON"' shapes.bin.gz
```

### Find three consecutive squares and print the first one

```bash
./proto-grep -zbmq Shape \
  'type == "SQUARE" && $$.type == "SQUARE" && $$$.type == "SQUARE"' \
  -p '$$$' \
  shapes.bin.gz
```

### Find squares with even width and print their corner

```bash
./proto-grep -zbmq Shape \
  'type == "SQUARE" && shape@Square.width % 2 == 0' \
  -p 'shape@Square.corner' \
  shapes.bin.gz
```

### Find squares where width is greater than `corner.x * corner.y`

```bash
./proto-grep -zbmq Shape \
  'type == "SQUARE" && shape@Square.width > shape@Square.corner.x * shape@Square.corner.y' \
  shapes.bin.gz
```

### Same query using grouped field syntax

```bash
./proto-grep -zbmq Shape \
  'type == "SQUARE" && shape@Square.{width > corner.{x * y}}' \
  shapes.bin.gz
```

### Find polygons with matching point indices and print selected fields

```bash
./proto-grep -zabmq Shape \
  'type == "POLYGON" && shape@Polygon.points[i].x > shape@Polygon.points[j].y' \
  -p 'id,i,j' \
  shapes.bin.gz
```

## Java API

The project also includes stream helpers for reading and writing protobuf messages.

The `MessageInputStream` and `MessageOutputStream` classes provide factories for working with:

* compressed and plain streams
* binary and text protobuf formats

These classes can be used when embedding protobuf stream processing into Java applications.

## Documentation

The original project documentation is available here:

https://temk.github.io/protobuf-utils/

## Project status

This is a historical open-source utility project. It is kept public as a reference implementation and as a useful toolkit for inspecting and querying protobuf message streams.

The codebase demonstrates:

* Java stream processing
* Protocol Buffers integration
* ANTLR-based query parsing
* CLI tool design
* binary/text data conversion
* query evaluation over structured messages

## License

This project is licensed under the MIT License.
