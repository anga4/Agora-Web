# Agora-Web

## Readme

_Rest API for Agora Web that uses [Agora](https://gitlab.com/aossie/Agora/): An Electronic Voting Library implemented in Scala_


This project is created using the play framework 2.6 seeds [template](https://github.com/playframework/play-scala-seed.g8).


![build status](https://gitlab.com/aossie/Agora-Web/badges/master/build.svg)

| Scala | Play | Updated
| :-: | :-: | :-:
| <img src="https://raw.githubusercontent.com/OlegIlyenko/scala-icon/master/scala-icon.png " width="25"> | <img src="https://raw.githubusercontent.com/OlegIlyenko/scala-icon/master/play-icon.png " width="25"> | August 08, 2018

To run the development environment for this REST API, you need [Git](https://git-scm.com/), [Sbt](http://www.scala-sbt.org/) and [MongoDB](https://www.mongodb.com/) installed.

## Table of contents

- [Agora-Web](#agora-web)
  - [Readme](#readme)
  - [Table of contents](#table-of-contents)
    - [Installation](#installation)
    - [Running the application](#running-the-application)
    - [API documentation](#api-documentation)
    - [Deployment](#deployment)
    - [Troubleshooting your local environment](#troubleshooting-your-local-environment)
  - [Further Reading / Useful Links](#further-reading--useful-links)


### Installation

To install the backend, please do the following:

1. Install [Git](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git).
2. Clone this repo with `https://gitlab.com/aossie/Agora-Web`
  - **Note:** *If you just want to use the project, cloning is the best option. However, if you wish to contribute to the project, you will need to fork the project first, and then clone your `Agora-Web` fork and make your contributions via a branch on your fork.*
3. Install and run [MongoDB](https://www.mongodb.com/)
4. Configure [Silhouette](https://www.silhouette.rocks/) to allow Agora's frontend to do Oauth2 authentication:
    1. Make a copy of `silhouette.conf` and rename it to `silhouetteLocal.conf`.
    2. Create new applications in [Facebook](https://developers.facebook.com/), [Twitter](https://dev.twitter.com/) and [Google](https://console.cloud.google.com/)
    3. Fill the following fields in `silhouetteLocal.conf` with the ids, keys and secrets from your created applications. You will need to provide only the keys for Facebook since it's the only social provider we support for now, though we intend to support the others in the future.

        ```
        facebook.clientID=${?FACEBOOK_CLIENT_ID}
        facebook.clientSecret=${?FACEBOOK_CLIENT_SECRET}
        google.clientID = ${?GOOGLE_CLIENT_ID}
        google.clientSecret = ${?GOOGLE_CLIENT_SECRET}
        ```
    4. Change the redirect URL in `silhouetteLocal.conf` to your localhost `localhost:9000`.
    5. Delete the line `include "silhouetteLocal.conf"` from `silhouetteLocal.conf`.

5. As above, make a copy of `application.conf` and rename it to `applicationLocal.conf`. 
    1. Assign your MongoDB URI (e.g. `mongodb://localhost`, if you are connecting to a MongoDB server running in your local computer) to the `mongodb.default.uri` field (e.g `mongodb.default.uri = "mongodb://localhost"`). 
    2. Set your [SendGrid](https://sendgrid.com) username and password in `applicationLocal.conf`.
        ```
        user = ${?SENDGRID_USERNAME}
        password = ${?SENDGRID_PASSWORD}
        ```
    3. Delete the lines `include "silhouette.conf"` and `include "applicationLocal"` from `applicationLocal.conf`.

### Running the application

To start the API, please do the following:

- Make sure you have java 8 installed and not java 9. For some reasons the build fails with java 9
- Start the server by running `sbt run` in the root folder.
- Go to [http://localhost:9000/](http://localhost:9000/) in a browser. Where you will see the API documentation hosted using swagger UI.
    - **Note:** *Changing any source code while the server is running will automatically recompile and reload the application on the next HTTP request.*

### API documentation

REST API documentation is available under address: [REST API](http://localhost:9000/)

### Deployment 
The current development branch is deployed on heroku and is available at http://agora-rest-api.herokuapp.com/

### Troubleshooting your local environment

Always `git pull` and get the latest from master. [Google](https://www.google.com) and [Stackoverflow](https://stackoverflow.com/) are your friends. You can find answers for most technical problems there. If you run into problems you can't resolve, feel free to open an issue.

## Further Reading / Useful Links

* [sbt](http://www.scala-sbt.org/)
* [Play framework](https://www.playframework.com/)
* [Scala](https://www.scala-lang.org/)
* [Silhouette Documentation](https://www.silhouette.rocks/docs)
* [Play2-ReactiveMongoDB](http://reactivemongo.org/releases/0.1x/documentation/tutorial/play.html)
* [Swagger-play](https://github.com/swagger-api/swagger-play)
