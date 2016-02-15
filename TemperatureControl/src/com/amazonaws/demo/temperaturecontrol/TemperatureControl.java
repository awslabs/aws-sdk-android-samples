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
 *       "setPoint": 72,
 *       "enabled": true
 *     },
 *     "delta": {
 *       "setPoint": 72,
 *       "enabled": true
 *     }
 *   },
 *   "metadata": {
 *     "desired": {
 *       "setPoint": {
 *         "timestamp": 1449791001
 *       },
 *       "enabled": {
 *         "timestamp": 1449791171
 *       }
 *     }
 *   },
 *   "version": 113,
 *   "timestamp": 1450303596
 * }
 * </pre>
 */
public class TemperatureControl {
    public State state;

    TemperatureControl() {
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

            public Integer setPoint;
            public Boolean enabled;
        }

        public class Delta {
            Delta() {
            }

            public Integer setPoint;
            public Boolean enabled;
        }
    }

    public Long version;
    public Long timestamp;
}
