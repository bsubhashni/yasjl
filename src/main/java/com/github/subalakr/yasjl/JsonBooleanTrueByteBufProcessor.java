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

import io.netty.buffer.ByteBufProcessor;

/**
 * Processes JSON true value
 *
 * @author Subhashni Balakrishnan
 */
public class JsonBooleanTrueByteBufProcessor implements ByteBufProcessor {
    private final byte T1 = (byte)'t';
    private final byte T2 = (byte)'r';
    private final byte T3 = (byte)'u';
    private final byte T4 = (byte)'e';

    private byte lastValue;

    public JsonBooleanTrueByteBufProcessor() {
        this.lastValue = T1;
    }

    public void reset() {
        this.lastValue = T1;
    }

    public boolean process(byte value) throws Exception {
        switch (value) {
            case T2:
                if (this.lastValue == T1) {
                    this.lastValue = T2;
                    return true;
                }
                break;
            case T3:
                if (this.lastValue == T2) {
                    this.lastValue = T3;
                    return true;
                }
                break;
            case T4:
                if (this.lastValue == T3) {
                    reset();
                    return false;
                }
                break;
        }
        throw new IllegalStateException("Invalid json");
    }
}
