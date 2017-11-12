CardExchange
==================

This is a Google App Engine app. The goal of this app is to collect cards.

# Installation
## Dependencies

* openjdk-8-jre
* maven
* [Google Cloud SDK](https://cloud.google.com/sdk/) (*optional* : will be installed automatically by maven during execution)

```bash
sudo apt-get install maven openjdk-8-jdk
```

## Setup

```bash
git clone https://github.com/kiralex/CardExchange.git
cd  CardExchange
```

Then, put your api key of pixabay in a new file at configuration/.env
For example : 
```
PIXABAY_API_KEY=012345678909876543210
```

It is needed for creating cards

## Maven commands
### Running devserver locally

    mvn appengine:devserver

### Deploying (you need to have admin rights in the Google App Engine project to deploy)

    mvn appengine:update

## Testing

    mvn verify

As you add / modify the source code (`src/main/java/...`) it's very useful to add
[unit testing](https://cloud.google.com/appengine/docs/java/tools/localunittesting)
to (`src/main/test/...`).  The following resources are quite useful:

* [Junit4](http://junit.org/junit4/)
* [Mockito](http://mockito.org/)
* [Truth](http://google.github.io/truth/)

## Updating to latest Artifacts

An easy way to keep your projects up to date is to use the maven [Versions plugin][versions-plugin].

    mvn versions:display-plugin-updates
    mvn versions:display-dependency-updates
    mvn versions:use-latest-versions

Note - Be careful when changing `javax.servlet` as App Engine Standard uses 3.1 for Java 8, and 2.5
for Java 7.

Our usual process is to test, update the versions, then test again before committing back.

[plugin]: http://www.mojohaus.org/versions-maven-plugin/
