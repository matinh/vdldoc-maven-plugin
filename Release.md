# How to perform a Release for the plugin

* Run integration-tests with `mvn -Prun-its clean install`
* Update version information with `mvn versions:set -DnewVersion=x.y`
* Commit the changes in `pom.xml`: git commit
