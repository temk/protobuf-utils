# Google Protocol Buffers' Utils
==============

## Streams
classes Message{Input,Ouput}Stream are factories for compressed/plain and binary/text message streams.

## Command line utiilities
In the folder 'precompiled' we placed the following:
* precompiled jar containing all dependencies: protobuf-utils-1.0-SNAPSHOT-jar-with-dependencies.jar
* simple protocol definition Geom.proto (copied from test/resources)
* example of compressed binary message stream shapes.bin.gz

### Cat
Command line utility for printing from/to different file formats.

#### Usage
net.katros.services.proto.PCat [-zZtTbB] {root-msg} [{input-file}|-]*

Flags:
* -z input is gzipped
* -t input is text
* -b input is binary
* -Z input will gzipped
* -T input will text
* -B input will binary

#### Example: print binary gizpped stream as a text
cd precompiled
java -cp geim.jar:protobuf-utils-1.0-SNAPSHOT-jar-with-dependencies.jar net.katros.services.proto.PCat -zbT Shape shapes.bin.gz

### Grep
Command line utility for searching messages within file(s).

#### Usage
net.katros.services.proto.PGrep  [options]* {input-file}*

Options: 
* -q {<query-expression}	Mandatory. Should apear only once.
* -p {print-expression}	Optional. Default is "$". Can apear several times.
* -d {delimiter}	Optional. Default is "-"
* -o {output-file}	Optional. Default is "-"
* -m {message-name>	Mandatory. Root message.
* -z	Optional. Default false. Input is gzipped.
* -Z	Optional. Default false. Output will gzipped.
* -t	Optional. Default false. Input format assumed text.
* -T	Optional. Default true.  Output format will text.
* -b	Optional. Default true.  Input format assumed binary.
* -B	Optional. Default false. Output format will binary.
* -t	Optional. Default true.  Input format assumed binary.
* -T	Optional. Default false. Output format will binary.
* -d	Optional. Default false. Dry run. I.e. just check syntax.

Notes:
* $ current message
* $$ previous message
* use any brakets: [] {} ()

'cd precompiled;, for runexamples

#### Example I: find all polygons
./proto-grep -zbmq Shape "type == 'POLYGON'" shapes.bin.gz

#### Example II: find all squares where previous two shapes also square. print the first one.
./proto-grep -zbmq Shape 'type == "SQUARE" && $$.type == "SQUARE" && $$$.type == "SQUARE"' -p '$$$' shapes.bin.gz

#### Example III: find all squares with even width, print corner
./proto-grep -zbmq Shape 'type == "SQUARE" && shape@Square.width % 2 == 0' -p 'shape@Square.corner' shapes.bin.gz

#### Example IV: find all squares with width > corner.x * corner.y
./proto-grep -zbmq Shape 'type == "SQUARE" && shape@Square.width > shape@Square.corner.x * shape@Square.corner.y'  shapes.bin.gz

#### Example V: The same as Example IV, but shorter version. 
./proto-grep -zbmq Shape 'type == "SQUARE" && shape@Square.{width > corner.{x * y}}'  shapes.bin.gz

#### Example VI: Find all polygons with polygons with point i,j s.t. points[i].x > points[i].y , print points indexs, and id of shape.
./proto-grep -zabmq Shape 'type == "POLYGON" && shape@Polygon.points[i].x >  shape@Polygon.points[j].y' -p 'id,i,j' shapes.bin.gz

#### Example VII: Same as Example VI, but find all appearances. (flag -a)
./proto-grep -zabmq Shape  'type == "POLYGON" && shape@Polygon.points[i].x >  shape@Polygon.points[j].y' -p 'id,i,j' shapes.bin.gz

## FAQ
TBA

==============
Please do not hesitate contact me (boris@temk.org), in case of any questions.

==============
Special thanks to Katros Ltd. for allowing to publish this code for public domain.


[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/ec0721d2abc3ef980ef6c3275f19133f "githalytics.com")](http://githalytics.com/temk/protobuf-utils)
