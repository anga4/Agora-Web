# Agora-Web

## Readme

_Rest API for [Agora](https://gitlab.com/aossie/Agora/): An Electronic Voting Library implemented in Scala_


This project is created using the play framework 2.6 seeds [template](https://github.com/playframework/play-scala-seed.g8).


![build status](https://gitlab.com/aossie/Agora-Web/badges/master/build.svg)

| Scala | Play | Updated
| :-: | :-: | :-:
| <img src="https://raw.githubusercontent.com/OlegIlyenko/scala-icon/master/scala-icon.png " width="25"> | <img src="https://raw.githubusercontent.com/OlegIlyenko/scala-icon/master/play-icon.png " width="25"> | May 04, 2018

To run the development environment for this REST API, you need [Git](https://git-scm.com/), [Sbt](http://www.scala-sbt.org/) and [MongoDB](https://www.mongodb.com/) installed.

## Table of contents

- [Installation](#installation)
- [Running the application](#running-the-application)
- [Troubleshooting your local environment](#troubleshooting-your-local-environment)
- [Testing](#testing)
- [Further Reading / Useful Links](#further-reading--useful-links)


### Installation

To install the frontend, please do the following:

1. Install [Git](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git).
2. Clone this repo with `https://gitlab.com/aossie/Agora-Web`
  - **Note:** *If you just want to use the project, cloning is the best option. However, if you wish to contribute to the project, you will need to fork the project first, and then clone your `Agora-Web` fork and make your contributions via a branch on your fork.*
3. Install and run [MongoDB](https://www.mongodb.com/)
4. As above, make a copy of `application.conf` and rename it to `applicationLocal.conf`. Assign your MongoDB URI (e.g. `mongodb://localhost`, if you are connecting to a MongoDB server running in your local computer) to the `mongodb.default.uri` field (e.g `mongodb.default.uri = "mongodb://localhost"`), set your [SendGrid](https://sendgrid.com) username and password, and include `applicationLocal.conf` into `application.conf`.

### Running the application

To start the API, please do the following:

- Make sure you have java 8 installed and not java 9. For some unknown reasons the build fails with java 9
- Start the server by running `sbt run` in the root folder.
- Go to [http://localhost:9000/](http://localhost:9000/) in a browser. Use one of your social accounts to login to the system.
    - **Note:** *Changing any source code while the server is running will automatically recompile and reload the application on the next HTTP request.*

## API documentation

REST API documentation is available under address: [REST API](http://localhost:9000/docs)

### Troubleshooting your local environment

Always `git pull` and get the latest from master. [Google](https://www.google.com) and [Stackoverflow](https://stackoverflow.com/) are your friends. You can find answers for most technical problems there. If you run into problems you can't resolve, feel free to open an issue.

### Running Tests

To run the test suite locally while developing, just run `sbt test` from the project root.

Tests will also run automatically via Gitlab CI when you push commits to a branch in the repository. You can view the output of the tests in GitLab's pipeline tab or in a Merge Request's acceptance box in its discussion tab. For this, you have to configure `.gitlab-ci.yml` and add your `HEROKU-API-KEY` as a secret variable in the Gitlab.


## Further Reading / Useful Links

* [sbt](http://www.scala-sbt.org/)
* [Play framework](https://www.playframework.com/)
* [Scala](https://www.scala-lang.org/)
* [Silhouette Documentation](https://www.silhouette.rocks/docs)
* [Play2-ReactiveMongoDB](http://reactivemongo.org/releases/0.1x/documentation/tutorial/play.html)
* [Swagger-play](https://github.com/swagger-api/swagger-play)
