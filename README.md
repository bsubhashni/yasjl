# yasjl: Yet another simple/streaming/stack-based json library

(under development)

#### Key features
- Works on bytebuf and doesn't perform blocking read
- Use *json pointers* to read specific values in bytebufs
- Can use reactive subject to get onNext notifications
- Supports "-" for json array any element match


#### Example
See `ByteBufJsonParserExample` for some example. (Tests under development) 
   ``` 
   BehaviorSubject<ByteBuf> results = BehaviorSubject.create();
    JsonPointer[] jsonPointers = {
        new JsonPointer("/clientContextID"),
        new JsonPointer("/metrics/elapsedTime"),
        new JsonPointer("/results/-", results),
        new JsonPointer("/status")
    };

    results.subscribe(new Action1<ByteBuf>(){
        public void call(ByteBuf buf) {
        System.out.println("got next val: " + buf.toString(Charset.defaultCharset()));
        }
     });
     
    ByteBuf buf = Unpooled.buffer();
    parser.initialize(buf, jsonPointers);
    parser.parse(); 
    ```

    
#### Inspiration
Jsonsl, Blackberry http-parser
