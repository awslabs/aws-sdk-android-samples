/**
 * Copyright 2010-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *    http://aws.amazon.com/apache2.0
 *
 * This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and
 * limitations under the License.
 */

package com.amazonaws.demo.temperaturecontrol;

/**
 * <pre>
 * {
 *   "state": {
 *     "desired": {
 *       "intTemp": 72,
 *       "extTemp": 45,
 *       "curState": "stopped"
 *     },
 *     "delta": {
 *       "intTemp": 72,
 *       "extTemp": 45,
 *       "curState": "stopped"
 *     }
 *   },
 *   "metadata": {
 *     "desired": {
 *       "intTemp": {
 *         "timestamp": 1449791237
 *       },
 *       "extTemp": {
 *         "timestamp": 1449791237
 *       },
 *       "curState": {
 *         "timestamp": 1449791237
 *       }
 *     }
 *   },
 *   "version": 6151,
 *   "timestamp": 1449791576
 * }
 * </pre>
 */
public class TemperatureStatus {
    public State state;

    TemperatureStatus() {
        state = new State();
    }

    public class State {
        Desired desired;
        Delta delta;

        State() {
            desired = new Desired();
            delta = new Delta();
        }

        public class Desired {
            Desired() {
            }

            public Integer intTemp;
            public Integer extTemp;
            public String curState;
        }

        public class Delta {
            Delta() {
            }

            public Integer intTemp;
            public Integer extTemp;
            public String curState;
        }
    }

    public Long version;
    public Long timestamp;
}
