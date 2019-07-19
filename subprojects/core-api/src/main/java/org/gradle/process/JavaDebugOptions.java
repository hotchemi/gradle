/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.process;

import org.gradle.api.Incubating;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

/**
 * Contains a subset of the <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/jpda/conninv.html">Java Debug Wire Protocol</a> properties.
 *
 * @since 5.6
 */
@Incubating
public interface JavaDebugOptions {
    // TODO: Docs
    @Input Property<Boolean> getEnabled();
    @Input Property<Integer> getPort();
    @Input Property<Boolean> getServer();
    @Input Property<Boolean> getSuspend();
}
