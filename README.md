CardExchange
==================

This is a Google App Engine app. The goal of this app is to collect cards.

# Installation
## Dependencies

* git (obviously !)
* openjdk-8-jre
* maven
* [Google Cloud SDK](https://cloud.google.com/sdk/) (*optional* : will be installed automatically by maven during execution)

```bash
sudo apt-get install maven openjdk-8-jdk git
```

## Setup

```bash
git clone https://github.com/kiralex/CardExchange.git
cd  CardExchange
```

Then, put your api key of pixabay in a new file named **env** int the folder **configuration**
For example : 
```
PIXABAY_API_KEY=012345678909876543210
```

It is needed to create cards

## Maven commands
### Running devserver locally

    mvn appengine:devserver

### Deploying (you need to have admin rights in the Google App Engine project to deploy)

    mvn appengine:update

## Updating to latest Artifacts

An easy way to keep your projects up to date is to use the maven [Versions plugin][versions-plugin].

    mvn versions:display-plugin-updates
    mvn versions:display-dependency-updates
    mvn versions:use-latest-versions

Note - Be careful when changing `javax.servlet` as App Engine Standard uses 3.1 for Java 8, and 2.5
for Java 7.
