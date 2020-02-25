/*
 * Copyright 2018 M. Isuru Tharanga Chrishantha Perera
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
package com.github.chrishantha.sample.memoryleak;

import com.beust.jcommander.Parameter;
import com.github.chrishantha.sample.base.SampleApplication;

import java.util.HashMap;
import java.util.Map;

public class MemoryLeakApplication implements SampleApplication {

    private enum KeyType {
        BAD,
        GOOD
    }

    @Parameter(names = "--length", description = "Length of the Key")
    private long length = 1024 * 1024;

    @Parameter(names = "--max", description = "Maximum limit to generate keys")
    private int max = 100;

    @Parameter(names = "--key-type", description = "Key Type: Good or Bad")
    private KeyType keyType = KeyType.BAD;

    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void start() {
        Map<Key, String> map = new HashMap<>();
        while (true) {
            for (int i = 0; i < max; i++) {
                Key key = KeyType.GOOD.equals(keyType) ? new GoodKey(i, length) : new BadKey(i, length);
                if (!map.containsKey(key)) {
                    map.put(key, "Number:" + i);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "MemoryLeakApplication{" +
                "length=" + length +
                ", max=" + max +
                ", keyType=" + keyType +
                '}';
    }
}
