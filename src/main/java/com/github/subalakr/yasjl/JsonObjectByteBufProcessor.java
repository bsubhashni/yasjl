/*
 * Copyright (c) 2017 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.subalakr.yasjl;

import static com.github.subalakr.yasjl.JsonParserUtils.*;

import io.netty.buffer.ByteBufProcessor;

/**
 * Processes JSON object value
 *
 * @author Subhashni Balakrishnan
 */
public class JsonObjectByteBufProcessor implements ByteBufProcessor {
    private boolean isString;
    private int count;
    private JsonStringByteBufProcessor stProcessor;


    public JsonObjectByteBufProcessor(JsonStringByteBufProcessor stProcessor) {
        this.count = 1;
        this.stProcessor = stProcessor;
    }

    public void reset() {
        this.count = 1;
        this.isString = false;
        this.stProcessor.reset();
    }

    public boolean process(byte value) throws Exception {
        if (this.isString) {
            this.isString = this.stProcessor.process(value);
            return true;
        } else {
            switch (value) {
                case O_CURLY:
                    this.count++;
                    return true;
                case C_CURLY:
                    this.count--;
                    if (count == 0) {
                        return false;
                    } else {
                        return true;
                    }
                case JSON_ST:
                    this.isString = true;
                    return true;
                default:
                    return true;
            }
        }
    }
}