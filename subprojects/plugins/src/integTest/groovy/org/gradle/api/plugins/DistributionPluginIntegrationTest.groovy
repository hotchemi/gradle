/*
 * Copyright 2012 the original author or authors.
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

package org.gradle.api.plugins

import org.gradle.integtests.fixtures.WellBehavedPluginTest
import org.gradle.integtests.fixtures.archives.TestReproducibleArchives
import org.gradle.test.fixtures.archive.TarTestFixture
import org.gradle.test.fixtures.maven.MavenPom

@TestReproducibleArchives
class DistributionPluginIntegrationTest extends WellBehavedPluginTest {
    @Override
    String getMainTask() {
        return "distZip"
    }

    def setup() {
        file("settings.gradle").text = "rootProject.name='TestProject'"
        file("someFile").createFile()
        using m2
    }

    def createTaskForCustomDistribution() {
        when:
        buildFile << """
            apply plugin:'distribution'

            distributions {
                custom{
                    contents {
                        from { "someFile" }
                    }
                }
            }

            """
        then:
        succeeds('customDistZip')
        and:
        file('build/distributions/TestProject-custom.zip').usingNativeTools().unzipTo(file("unzip"))
        file("unzip/TestProject-custom/someFile").assertIsFile()
    }

    def "can publish distribution"() {
        when:
        buildFile << """
            apply plugin:'distribution'
            apply plugin:'maven'
            group = "org.acme"
            version = "1.0"

            distributions {
                main {
                    contents {
                        from { "someFile" }
                    }
                }
            }

            uploadArchives {
                repositories {
                    mavenDeployer {
                        repository(url: "${file("repo").toURI()}")
                    }
                }
            }
            """
        then:
        executer.expectDeprecationWarning("The maven plugin has been deprecated. This is scheduled to be removed in Gradle 7.0. Please use the maven-publish plugin instead.")
        executer.expectDeprecationWarning("The uploadArchives task has been deprecated. This is scheduled to be removed in Gradle 7.0. Use the 'maven-publish' plugin instead")
        succeeds("uploadArchives")
        file("repo/org/acme/TestProject/1.0/TestProject-1.0.zip").assertIsFile()

        and:
        def pom = new MavenPom(file("repo/org/acme/TestProject/1.0/TestProject-1.0.pom"))
        pom.groupId == "org.acme"
        pom.artifactId == "TestProject"
        pom.version == "1.0"
        pom.packaging == "zip"
    }

    def createTaskForCustomDistributionWithCustomName() {
        when:
        buildFile << """
            apply plugin:'distribution'

            distributions {
                custom{
                    distributionBaseName = 'customName'
                    contents {
                        from { "someFile" }
                    }
                }
            }
            """
        then:
        succeeds('customDistZip')
        and:
        file('build/distributions/customName.zip').usingNativeTools().unzipTo(file("unzip"))
        file("unzip/customName/someFile").assertIsFile()
    }

    def createTaskForCustomDistributionWithEmptyCustomName() {
        when:
        buildFile << """
            apply plugin:'distribution'
            distributions {
                custom{
                    distributionBaseName = ''
                    contents {
                        from { "someFile" }
                    }
                }
            }


            """
        then:
        runAndFail('customDistZip')
        failure.assertHasCause "Distribution 'custom' must not have an empty distributionBaseName."
    }

    def createDistributionWithoutVersion() {
        given:
        createDir('src/main/dist') {
            file 'file1.txt'
            dir2 {
                file 'file2.txt'
            }
        }
        and:
        buildFile << """
            apply plugin: 'distribution'


            distributions {
                main{
                    distributionBaseName = 'myDistribution'
                }
            }
            """
        when:
        run('distZip')
        then:
        file('build/distributions/myDistribution.zip').exists()
    }

    def assembleAllDistribution() {
        given:
        createDir('src/main/dist') {
            file 'file1.txt'
            dir2 {
                file 'file2.txt'
            }
        }
        and:
        buildFile << """
            apply plugin:'distribution'


            distributions {
                main{
                    distributionBaseName = 'myDistribution'
                }
            }
            """
        when:
        run('assemble')
        then:
        file('build/distributions/myDistribution.zip').exists()
        file('build/distributions/myDistribution.tar').exists()
    }

    def createDistributionWithVersion() {
        given:
        createDir('src/main/dist') {
            file 'file1.txt'
            dir2 {
                file 'file2.txt'
            }
        }
        and:
        buildFile << """
            apply plugin:'distribution'

            version = '1.2'
            distributions {
                main{
                    distributionBaseName = 'myDistribution'
                }
            }
            distZip{

            }
            """
        when:
        run('distZip')
        then:
        file('build/distributions/myDistribution-1.2.zip').exists()
    }

    def createDistributionWithoutBaseNameAndVersion() {
        given:
        createDir('src/main/dist') {
            file 'file1.txt'
            dir2 {
                file 'file2.txt'
            }
        }
        and:
        buildFile << """
            apply plugin:'distribution'

            """
        when:
        run('distZip')
        then:
        file('build/distributions/TestProject.zip').exists()
    }

    def createDistributionWithoutBaseNameAndWithVersion() {
        given:
        createDir('src/main/dist') {
            file 'file1.txt'
            dir2 {
                file 'file2.txt'
            }
        }
        and:
        buildFile << """
            apply plugin:'distribution'

            version = 1.2
            """
        when:
        run('distZip')
        then:
        file('build/distributions/TestProject-1.2.zip').exists()
    }

    def createCreateArchiveForCustomDistribution(){
        given:
        createDir('src/custom/dist') {
            file 'file1.txt'
            dir2 {
                file 'file2.txt'
            }
        }
        and:
        buildFile << """
            apply plugin:'distribution'

            distributions{
                custom
            }
            """
        when:
        run('customDistZip')
        then:
        file('build/distributions/TestProject-custom.zip').exists()
    }


    def includeFileFromSrcMainCustom() {
        given:
        createDir('src/custom/dist'){
            file 'file1.txt'
            dir {
                file 'file2.txt'
            }
        }
        and:
        buildFile << """
            apply plugin:'distribution'

            version = 1.2

            distributions{
                custom
            }
            """
        when:
        run('customDistZip')
        then:
        file('build/distributions/TestProject-custom-1.2.zip').usingNativeTools().unzipTo(file("unzip"))
        file("unzip").assertHasDescendants(
                'TestProject-custom-1.2/file1.txt',
                'TestProject-custom-1.2/dir/file2.txt')
    }

    def includeFileFromDistContent() {
        given:
        createDir('src/custom/dist'){
            file 'file1.txt'
            dir {
                file 'file2.txt'
            }
        }
        createDir("docs"){
            file 'file3.txt'
            dir2 {
                file 'file4.txt'
            }
        }
        and:
        buildFile << """
            apply plugin:'distribution'

            version = 1.2

            distributions{
                custom {
                    contents {
                        from ( 'docs' ){
                            into 'docs'
                        }


                    }
                }
            }
            """
        when:
        run('customDistZip')
        then:
        file('build/distributions/TestProject-custom-1.2.zip').usingNativeTools().unzipTo(file("unzip"))
        file("unzip").assertHasDescendants(
                'TestProject-custom-1.2/file1.txt',
                'TestProject-custom-1.2/dir/file2.txt',
                'TestProject-custom-1.2/docs/file3.txt',
                'TestProject-custom-1.2/docs/dir2/file4.txt')
    }

    def installFromDistContent() {
        given:
        createDir('src/custom/dist'){
            file 'file1.txt'
            dir {
                file 'file2.txt'
            }
        }
        createDir("docs"){
            file 'file3.txt'
            dir2 {
                file 'file4.txt'
            }
        }
        and:
        buildFile << """
            apply plugin:'distribution'

            version = 1.2

            distributions{
                custom {
                    contents {
                        from ( 'docs' ){
                            into 'docs'
                        }
                    }
                }
            }
            """
        when:
        run('installCustomDist')

        then:
        file('build/install/TestProject-custom').exists()
        file('build/install/TestProject-custom').assertHasDescendants(
                'file1.txt',
                'dir/file2.txt',
                'docs/file3.txt',
                'docs/dir2/file4.txt')
    }

    def installDistCanBeRerun() {
        when:
        buildFile << """
            apply plugin:'distribution'

            distributions {
                custom{
                    contents {
                        from { "someFile" }
                    }
                }
            }

            """
        succeeds('installCustomDist')
        // update the file so that when it re-runs it is not UP-TO-DATE
        file("someFile") << "updated"
        then:
        succeeds('installCustomDist')
        and:
        file('build/install/TestProject-custom/someFile').assertIsCopyOf(file("someFile"))
    }

    def createTarTaskForCustomDistribution() {
        when:
        buildFile << """
            apply plugin:'distribution'

            distributions {
                custom{
                    contents {
                        from { "someFile" }
                    }
                }
            }

            """
        then:
        succeeds('customDistTar')
        and:
        file('build/distributions/TestProject-custom.tar').usingNativeTools().untarTo(file("untar"))
        file("untar/TestProject-custom/someFile").assertIsFile()
    }

    def "can create distribution with .tar in project name"() {
        when:
        buildFile << """
            apply plugin: 'application'
            apply plugin: 'java'
            mainClassName = "Main"
        """
        file("src/main/java/Main.java") << "public class Main {}"
        settingsFile << """
            rootProject.name = 'projectWithtarInName'
        """
        then:
        succeeds("distTar")
        new TarTestFixture(file("build/distributions/projectWithtarInName.tar")).assertContainsFile("projectWithtarInName/lib/projectWithtarInName.jar")
    }
}
