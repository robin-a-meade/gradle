/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.integtests.fixtures

import groovy.transform.CompileStatic

import static org.gradle.api.artifacts.ArtifactRepositoryContainer.GOOGLE_URL
import static org.gradle.api.artifacts.ArtifactRepositoryContainer.MAVEN_CENTRAL_URL
import static org.gradle.api.internal.artifacts.dsl.DefaultRepositoryHandler.BINTRAY_JCENTER_URL

@CompileStatic
class RepoScriptBlockUtil {

    private static enum MirroredRepository {
        JCENTER(BINTRAY_JCENTER_URL, System.getProperty('org.gradle.integtest.mirrors.jcenter'), "maven"),
        MAVEN_CENTRAL(MAVEN_CENTRAL_URL, System.getProperty('org.gradle.integtest.mirrors.mavencentral'), "maven"),
        GOOGLE(GOOGLE_URL, System.getProperty('org.gradle.integtest.mirrors.google'), "maven"),
        LIGHTBEND_MAVEN("https://repo.lightbend.com/lightbend/maven-releases", System.getProperty('org.gradle.integtest.mirrors.lightbendmaven'), "maven"),
        LIGHTBEND_IVY("https://repo.lightbend.com/lightbend/ivy-releases", System.getProperty('org.gradle.integtest.mirrors.lightbendivy'), "ivy")

        String originalUrl
        String mirrorUrl
        String name
        String type

        private MirroredRepository(String originalUrl, String mirrorUrl, String type) {
            this.originalUrl = originalUrl
            this.mirrorUrl = mirrorUrl ?: originalUrl
            this.name = mirrorUrl ? name() + "_MIRROR" : name()
            this.type = type
        }

        String getRepositoryDefinition() {
            """
                ${type}{
                    name '${name}'
                    url '${mirrorUrl}'
                }
            """
        }
    }

    private RepoScriptBlockUtil() {
    }

    static String jcenterRepository() {
        return """
            repositories {
                ${jcenterRepositoryDefinition()}
            }
        """
    }

    static String mavenCentralRepository() {
        return """
            repositories {
                ${mavenCentralRepositoryDefinition()}
            }
        """
    }

    static String googleRepository() {
        return """
            repositories {
                ${googleRepositoryDefinition()}
            }
        """
    }

    static String jcenterRepositoryDefinition() {
        MirroredRepository.JCENTER.repositoryDefinition
    }

    static String mavenCentralRepositoryDefinition() {
        MirroredRepository.MAVEN_CENTRAL.repositoryDefinition
    }

    static String lightbendMavenRepositoryDefinition() {
        MirroredRepository.LIGHTBEND_MAVEN.repositoryDefinition
    }

    static String lightbendIvyRepositoryDefinition() {
        MirroredRepository.LIGHTBEND_IVY.repositoryDefinition
    }

    static String googleRepositoryDefinition() {
        MirroredRepository.GOOGLE.repositoryDefinition
    }

    static File createMirrorInitScript() {
        File mirrors = File.createTempFile("mirrors", ".gradle")
        mirrors.deleteOnExit()
        def mirrorConditions = MirroredRepository.values().collect { MirroredRepository mirror ->
            """
                if (repo.url.toString() == '${mirror.originalUrl}') {
                    repo.url = '${mirror.mirrorUrl}'
                }
            """
        }.join("")
        mirrors << """
            def withMirrors(repos) {
                repos.all { repo ->
                    if (repo.hasProperty('url')) {
                        mirror(repo)
                    }
                }
            }

            def mirror(repo) {
                ${mirrorConditions}
            }

            allprojects {
                buildscript.configurations.classpath.incoming.beforeResolve {
                    withMirrors(buildscript.repositories)
                }
                afterEvaluate {
                    withMirrors(repositories)
                }
            }

            settingsEvaluated { settings ->
                withMirrors(settings.pluginManagement.repositories)
            }
        """
        mirrors
    }
}
