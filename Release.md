# How to perform a Release for the plugin

* Run integration-tests with `mvn -Prun-its clean install`
* Update version information with `mvn versions:set -DnewVersion=x.y`
* Update `Changes.md` for release
* Commit the changes: `git commit -a -m"prepare for x.y release"`
* Tag the release `git tag x.y` and push changes to github: `git push origin --tags`
* Perform the release and upload it to maven central: `mvn -Prelease deploy`
* Update the documentation (see below)
* Update version information for development: `mvn versions:set -DnewVersion=1.1-SNAPSHOT`
* Commit the changes in `pom.xml`: `git commit -a -m"prepare for SNAPSHOT development"`
* push changes to github: `git push`

# Update the plugin's homepage

* Generate documentation: `mvn -Preporting clean site`
* Sync it to some local directory that holds the `gh-pages` branch:
  `rsync --delete --exclude .git --exclude .gitignore -vaz target/site/ .../vdldoc-maven-plugin.gh-pages/`
* add new files with `git add` if necessary
* commit the changes: `git commit -m"updated documentation for 1.0"`
* push changes to github: `git push`
