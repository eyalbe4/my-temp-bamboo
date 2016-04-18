/*
 * Copyright (C) 2010 JFrog Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jfrog.bamboo.util;

import org.apache.log4j.Logger;
import org.jfrog.build.api.util.Log;

/**
 * @author Noam Y. Tenne
 */
public class BambooBuildInfoLog implements Log {

    private Logger log;

    public BambooBuildInfoLog(Logger log) {
        this.log = log;
    }

    public void debug(String message) {
        log.debug(message);
    }

    public void info(String message) {
        log.info(message);
    }

    public void warn(String message) {
        log.warn(message);
    }

    public void error(String message) {
        log.error(message);
    }

    public void error(String message, Throwable e) {
        log.error(message, e);
    }
}
