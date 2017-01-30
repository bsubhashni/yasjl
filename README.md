# yasjl: Yet another simple/streaming/stack-based json library

#### Features
- Works on bytebuf, allows streaming partial reads
- Use *json pointers* to read specific values in bytebufs
- Callbacks to get notifications on values
- Supports "-" for json array any element match
- Zero copy for parsing, but copies value only on request using a callback

Takes inspiration from Jsonsl, blackberry http-parser

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


