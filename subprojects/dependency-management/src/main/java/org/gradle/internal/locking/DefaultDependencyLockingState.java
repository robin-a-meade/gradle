/*
 * Copyright 2018 the original author or authors.
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

package org.gradle.internal.locking;

import org.gradle.api.artifacts.DependencyConstraint;
import org.gradle.api.internal.artifacts.dsl.dependencies.DependencyLockingState;

import java.util.Set;

import static java.util.Collections.emptySet;

public class DefaultDependencyLockingState implements DependencyLockingState {

    public static final DefaultDependencyLockingState EMPTY_LOCK_CONSTRAINT = new DefaultDependencyLockingState();

    private final boolean lockDefined;
    private final Set<DependencyConstraint> constraints;

    private DefaultDependencyLockingState() {
        lockDefined = false;
        constraints = emptySet();
    }
    public DefaultDependencyLockingState(Set<DependencyConstraint> constraints) {
        lockDefined = true;
        this.constraints = constraints;
    }

    @Override
    public boolean hasLockState() {
        return lockDefined;
    }

    @Override
    public Set<DependencyConstraint> getLockedDependencies() {
        return constraints;
    }
}
