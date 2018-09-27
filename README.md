
[![Build Status](https://travis-ci.org/axel3rd/mpg-coach-bot.svg?branch=master)](https://travis-ci.org/axel3rd/mpg-coach-bot) [![SonarCloud Status](https://sonarcloud.io/api/project_badges/measure?project=org.blondin%3Ampg-coach-bot&metric=alert_status)](https://sonarcloud.io/dashboard?id=org.blondin%3Ampg-coach-bot) [ ![Download](https://api.bintray.com/packages/axel3rd/generic/mpg-coach-bot/images/download.svg) ](https://bintray.com/axel3rd/generic/mpg-coach-bot/_latestVersion)

# mpg-coach-bot

MPG (Mon Petit Gazon) coach bot, to automate and optimize weekly league actions

## Concept

Automate and optimize your [MPG](http://mpg.football/) weekly league actions, using external data like [mpgstats](https://www.mpgstats.fr)

## Usage

Create a `mpg.properties` file with your *MPG* credentials:

    email = firstName.lastName@gmail.com
    password = foobar

And run program with this file as first parameter:

    java -jar mpg-coach-bot-x.y.z.jar mpg.properties

Note :

- If no file provided as parameter, a `mpg.properties` will be used in working directory (if exist).
- Environments variable `MPG_EMAIL` & `MPG_PASSWORD` could be used for configuration (override configuration file in this case).

## Roadmap

### v1

Simple Java auto-executable batch (which jersey 2, jackson, ...), which include feature like:

- List your league players by efficiency (titular's regularity, score, ...) for current formation, to establish the best team for next match
- Propose changes to reach the probable best team
- Update team automatically
- Propose to sell and buy some players to optimize team
- ...

### v2

Web application with friendly UI (Spring boot, docker container, JS Frontend, ...) to facilitate usage.

### v3

Data mining on media resources to optimize team (injuries, coach announcements, ...).
