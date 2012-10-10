# Developer notes for suite_authorization_source.rb

## Building

This project uses [buildr][] for build automation. Buildr is a ruby build tool
for Java projects. To install it and the other gems this project uses run the
`install_gems.rb` script in the project root:

    $ ruby install_gems.rb

Then you can use buildr's standard tasks for testing, packaging etc.

[buildr]: http://buildr.apache.org/

## Releasing

This project deploys build artifacts to GitHub downloads. It replaces buildr's
standard `release` and `deploy` tasks to achieve this. Release process:

* Set the project version to the desired release version in `buildfile`. E.g.,
  update `3.9.4.DEV` to `3.9.4.RELEASE`.
* Commit this change alone with the message (e.g.) `Version 3.9.4.RELEASE`.
* Tag the change:

    $ git tag -a '3.9.4.RELEASE' -m 'Version 3.9.4.RELEASE'
    $ git push --tags

* Build and deploy the project:

    $ buildr full_release TEST=no

* Update the project version to the next dev version in `buildfile`. E.g.,
  update `3.9.4.RELEASE` to `3.9.5.DEV`.
* Add a new subheading for the next release to `CHANGELOG.md`.
* Commit and push these two change alone:

    $ git add buildfile CHANGELOG.md
    $ git commit -m 'Update for ongoing development'
    $ git push
