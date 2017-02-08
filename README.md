# yasjl

Yet another simple/streaming/stack-based json library

#### Features
- Works on bytebuf, allows streaming partial reads
- Use *json pointers* to read specific values in bytebufs
- Callbacks to get notifications on values
- Supports "-" for json array any element match
- Zero copy for parsing, but copies value only on request using a callback

Takes inspiration from Jsonsl, blackberry json-parser

#### Example

Create Json pointers with callback to fetch value
```
JsonPointer[] jsonPointers = {
    new JsonPointer("/results/-",  new JsonPointerCB1() {
        public void call(ByteBuf buf) {
            //get value for the json pointer
            System.out.println(buf.toString(Charset.defaultCharset()));
            buf.release();
        })
};
```

Initialize the parser with the ByteBuf which has the JSON content and json pointers
```
parser.initialize(buf, jsonPointers);
```

Parse the current readable bytes and does callback if there was a reference to a 
json pointer. This will throw EOFException if more content is required to finish parsing 
```
parser.parse(); 

```

#### Simple benchmark results
```
 size: 3.2 MB,       level-depth: 2,     time: 2327ms
 size: 429.2 kB,     level-depth: 4,     time: 93ms
 size: 9.7 MB,       level-depth: 5,     time: 102ms
 size: 9.7 MB,       level-depth: 5,     time: 55ms
 size: 16.8 kB,      level-depth: 2,     time: 1ms
 size: 874 B,        level-depth: 4,     time: 0ms
 size: 775 B,        level-depth: 5,     time: 0ms
 size: 243 B,        level-depth: 4,     time: 0ms
 size: 3.5 kB,       level-depth: 4,     time: 1ms
 size: 4.2 kB,       level-depth: 4,     time: 0ms
 size: 602 B,        level-depth: 4,     time: 0ms
 size: 1.1 kB,       level-depth: 6,     time: 0ms
 size: 1.4 kB,       level-depth: 5,     time: 0ms
 size: 18.4 kB,      level-depth: 5,     time: 1ms
 size: 39.5 kB,      level-depth: 4,     time: 2ms
 size: 1.2 kB,       level-depth: 6,     time: 0ms
```
