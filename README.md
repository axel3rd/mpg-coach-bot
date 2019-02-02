
[![Build Status](https://travis-ci.org/axel3rd/mpg-coach-bot.svg?branch=master)](https://travis-ci.org/axel3rd/mpg-coach-bot) [![SonarCloud Status](https://sonarcloud.io/api/project_badges/measure?project=org.blondin%3Ampg-coach-bot&metric=alert_status)](https://sonarcloud.io/dashboard?id=org.blondin%3Ampg-coach-bot) [ ![Download](https://api.bintray.com/packages/axel3rd/generic/mpg-coach-bot/images/download.svg) ](https://bintray.com/axel3rd/generic/mpg-coach-bot/_latestVersion#files) &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Development version: [ ![download](https://api.bintray.com/packages/axel3rd/generic-dev/mpg-coach-bot/images/download.svg) ](https://bintray.com/axel3rd/generic-dev/mpg-coach-bot/_latestVersion#files)

# mpg-coach-bot

MPG (Mon Petit Gazon) coach bot, to automate and optimize weekly league actions

## Concept

Automate and optimize your [MPG](http://mpg.football/) weekly league actions, using external datas like [mpgstats](https://www.mpgstats.fr) and [equipeactu](http://www.equipeactu.fr/blessures-et-suspensions/).

The efficiency algorithm used to calculate players score is:

    player.matchs / championshipDays * player.average * (1 + player.goals() * efficiency.coeff)

*With efficiency coeff : Attacker = 1.2 ; Midfielder = 1.05 ; Defender = 1.025 ; Goalkeeper = 1 (Before v1.2, 1.2 for all lines).*

## Usage

*Prerequisite: [java](https://www.java.com/fr/download/) should be installed and on your PATH.*

Extract ZIP file and update the given `mpg.properties` file with your *MPG* credentials and main options:

    email = firstName.lastName@gmail.com
    password = foobar
    
    # Auto update team ('false' by default, since v1.1)
    team.update = true

Depending your environment system, run `mpg-coach-bot.bat` (Windows) or `mpg-coach-bot.sh` (Linux).

## Usage (advanced)

### Configuration

Environments variable with `MPG_` and key name in upper case + '_' (ex: `MPG_EMAIL`, `MPG_PASSWORD`, ...) could be used (override configuration file in this case).

If you are using this program the Friday @work, you can have to configure the company proxy in `mpg.properties` file (since v1.1):

    # Usage behind a company proxy
    proxy.uri = http://company.proxy.com:80
    proxy.user = foo
    proxy.password = bar

To change default notes for tactical substitutes, use in `mpg.properties` file (since v1.1):

    # Notes for tactical substitutes
    tactical.substitute.attacker = 6.0
    tactical.substitute.midfielder = 5.0
    tactical.substitute.defender = 5.0

To change default notes for proposal of selling players, use in `mpg.properties` file (since v1.2):

    # Notes for proposal of selling players
    efficiency.sell.attacker = 3.0
    efficiency.sell.midfielder= 3.0
    efficiency.sell.defender = 3.0
    efficiency.sell.goalkeeper = 3.0

To change default efficiency coefficient for players, use in `mpg.properties` file (since v1.2):

    # Efficiency coefficient
    efficiency.coefficient.attacker = 1.2
    efficiency.coefficient.midfielder = 1.05
    efficiency.coefficient.defender = 1.025
    efficiency.coefficient.goalkeeper = 1.0

To add some debug logs about execution (since v1.2):

    # Enable debug logs
    logs.debug = true

### Execution

You can execute in command line:

    java -jar mpg-coach-bot-x.y.z.jar your-mpg-config.properties

Note : If no file provided as parameter, a `mpg.properties` file will be used in working directory (if exist).

## Roadmap

Details of working progress in [milestones](../../milestones) (and [issues](../../issues)).

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

## Development process

This project is using [Maven](https://maven.apache.org/) as integration tool.

For development/SNAPSHOT build, use:

```
mvn install
```

Locally on your computer, you can improve end2end tests by providing file `src/test/resources/mpg.properties` file with *real* datas (see `mpg.properties.here` file in directory for details):

```
email = firstName.lastName@gmail.com
password = foobar
```

For release build, use (you will have to fill your GitHub credentials):
```
git reset --hard origin/master 
git branch -m next-version 
mvn clean release:clean release:prepare
```

After that, you would have to create pull-request from 'next-version' branch and rebase it on master for next version development.
