
[![Build Status](https://travis-ci.org/axel3rd/mpg-coach-bot.svg?branch=master)](https://travis-ci.org/axel3rd/mpg-coach-bot) [![SonarCloud Status](https://sonarcloud.io/api/project_badges/measure?project=org.blondin%3Ampg-coach-bot&metric=alert_status)](https://sonarcloud.io/dashboard?id=org.blondin%3Ampg-coach-bot) [ ![Download](https://api.bintray.com/packages/axel3rd/generic/mpg-coach-bot/images/download.svg) ](https://bintray.com/axel3rd/generic/mpg-coach-bot/_latestVersion#files) &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Development version: [ ![download](https://api.bintray.com/packages/axel3rd/generic-dev/mpg-coach-bot/images/download.svg) ](https://bintray.com/axel3rd/generic-dev/mpg-coach-bot/_latestVersion#files)

# mpg-coach-bot

MPG (Mon Petit Gazon) coach bot, to automate and optimize weekly league actions

## Concept

Automate and optimize your [MPG](http://mpg.football/) weekly league actions, using external data like [mpgstats](https://www.mpgstats.fr) and [sportsgambler](https://www.sportsgambler.com/football/injuries-suspensions/).

**The common features are:**

- Displaying your teams, line by line, ordered by players efficiency (with injured players)
- Updating your team
- Proposing some players to buy, better than the one you have (if option `transactions.proposal` is enabled and *MPG* expert mode is bought)
- When league not started (aka: *mercato*), the best players to buy for your incoming team

**NB:** Your tactical organization and selected bonus are not updated and let as configured.

The efficiency algorithm used to calculate players efficiency score is:

    player.matchs / championshipDays * player.average * (1 + player.goals() * efficiency.coeff)

*With efficiency coeff : Attacker = 1.2 ; Midfielder = 1.05 ; Defender = 1.025 ; Goalkeeper = 1 (Before v1.2, 1.2 for all lines).*

This efficiency is focused on recent days (8 by default), current season notation can be used by disabling option `efficiency.recent.focus`.

## Usage

*Prerequisite: [java](https://www.java.com/fr/download/) should be installed and on your PATH.*

Download package (Via Download buttons at top of this documentation, not the GitHub "releases" link), extract ZIP files and update the given `mpg.properties` file with your *MPG* credentials and main options:

    email = firstName.lastName@gmail.com
    password = foobar
    
Depending your environment system, run `mpg-coach-bot.bat` (Windows) or `mpg-coach-bot.sh` (Linux).

To fully automate update your weekly actions on your *MPG* leagues, you can use a Linux *crontab* (daily at 6pm, for sample).

**Disclaimer**: *MPG* credentials are used to connect on *MPG* website API, to retrieve your leagues informations. These credentials are not stored outside the configuration file, but are in memory during the program execution.

## Sample output

### Principal

The main output is displaying:

- Injured players, to remove of your team
- Your team line by line, ordered by efficiency score (*Eff.*), with the players quotation/prices (*Q.*):

```
========== Your league name ==========
Out: Aouar Houssem (M - 34.88) - INJURY_ORANGE - Inconnu (depuis 12/11) - Inconnu
Out: Ambroise Oyongo (D - 11.85) - INJURY_ORANGE - Blessure au genou (depuis 04/11) - Inconnu

Optimized team:
+---+--------------------+-------+----+
| P |    Player name     | Eff.  | Q. |
+---+--------------------+-------+----+
| G | Costil Benoit      |  5.58 | 20 |
| G | Prior Jerome       |  0.00 | 7  |
+---+--------------------+-------+----+
| D | Le Tallec Damien   | 17.35 | 23 |
| D | Meunier Thomas     | 13.13 | 24 |
| D | Marquinhos         | 10.43 | 25 |
| D | Kamara Boubacar    |  4.15 | 11 |
| D | Dubois Léo         |  2.27 | 11 |
+---+--------------------+-------+----+
| M | Thauvin Florian    | 41.47 | 38 |
| M | Tielemans Youri    | 17.73 | 13 |
| M | Nkunku Christopher |  8.75 | 13 |
| M | Lucas Evangelista  |  7.10 | 8  |
| M | Luiz Gustavo       |  4.42 | 15 |
| M | Lienard Dimitri    |  3.16 | 12 |
+---+--------------------+-------+----+
| A | Sala Emiliano      | 74.84 | 30 |
| A | Neymar             | 73.48 | 52 |
| A | Pepe Nicolas       | 65.61 | 36 |
| A | Laborde Gaetan     | 44.12 | 24 |
| A | Diony Lois         | 23.55 | 15 |
| A | Ripart Renaud      | 18.19 | 17 |
| A | Leya Iseka Aaron   | 11.38 | 10 |
+---+--------------------+-------+----+
```

### Team Update

When option `team.update` is enabled and *MPG* expert mode is bought, you will have in addition:

    Updating team ...

**NB**:  Injured players are not taken into account to compose your team during an update.

### Transaction proposal

When option `transactions.proposal` is enabled, you will have in addition:

    Transactions proposal ...
    Budget: 2
    Budget if last players by line sold: 69
    Player(s) to buy (3 best choice by line):
    +---+----------------------------+-------+----+
    | P |        Player name         | Eff.  | Q. |
    +---+----------------------------+-------+----+
    | D | Badiashile Mukinayi Benoit |  9.62 | 9  |
    | D | Eboa Eboa Félix            |  5.89 | 11 |
    | D | Alakouch Sofiane           |  5.89 | 11 |
    | M | Lopez Maxime               | 11.27 | 15 |
    | M | Sarr Bouna                 | 10.64 | 14 |
    | M | Fulgini Angelo             | 10.64 | 13 |
    | A | Otero Juan Ferney          |  9.14 | 13 |
    | A | Kalifa Coulibaly           |  3.96 | 11 |
    | A | Skuletic Petar             |  3.50 | 12 |
    +---+----------------------------+-------+----+

## Usage (advanced)

### Configuration

Environments variable with `MPG_` and key name in upper case + '_' (ex: `MPG_EMAIL`, `MPG_PASSWORD`, ...) could be used (override configuration file in this case).

The `mpg.properties` file could be used to change the program behavior on multiple items. Here the option with default values for information (except for proxy).

If you are using this program the Friday @work, you can have to configure the company proxy (since v1.1):

    # Usage behind a company proxy
    proxy.uri = http://company.proxy.com:80
    proxy.user = foo
    proxy.password = bar

To update team automatically ('true' by default since v1.6):

    # Enable auto-update team
    team.update = true

To focus on recent efficiency, about team proposal and buy transaction (since v1.3, 'true' by default since v1.6):

    # Focus on recent efficiency
    efficiency.recent.focus = true
    efficiency.recent.days = 8

To use bonus if not already selected and some would be lost (since v1.6):

    # Use bonus
    use.bonus = true

To include only some leagues or remove some of them, leagues id separated by comma (since v1.5):

    # Include/exclude leagues
    leagues.include = KX24XMUJ,KLGXSSUM
    leagues.exclude = LJT3FXDX

To enable/disable and change default notes for tactical substitutes (since v1.1):

    # Notes for tactical substitutes
    tactical.substitutes = true
    tactical.substitute.attacker = 6.0
    tactical.substitute.midfielder = 5.0
    tactical.substitute.defender = 5.0

To enable/disable and change default notes for proposal of selling players (since v1.2):

    # Enable sell/buy players proposal
    transactions.proposal = true
    
    # Notes for proposal of selling players
    efficiency.sell.attacker = 3.0
    efficiency.sell.midfielder= 3.0
    efficiency.sell.defender = 3.0
    efficiency.sell.goalkeeper = 3.0

To change default efficiency coefficient for players (since v1.2):

    # Efficiency coefficient
    efficiency.coefficient.attacker = 1.2
    efficiency.coefficient.midfielder = 1.05
    efficiency.coefficient.defender = 1.025
    efficiency.coefficient.goalkeeper = 1.0

To enable/disable SSL certificates check (since v1.7):

    # Check SSL certificates of third part Website
    ssl.certificates.check = true

To add some debug logs about execution (since v1.2):

    # Enable debug logs
    logs.debug = false

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
mvn package
```

Locally on your computer, you can improve end2end tests by providing file `src/test/resources/mpg.properties` file with *real* data (see `mpg.properties.here` file in directory for details):

```
email = firstName.lastName@gmail.com
password = foobar
```

For release build, use:
```
git reset --hard origin/master 
git branch -m next-version 
mvn -B clean release:clean release:prepare -Dusername=yourGitHubLogin -Dpassword=yourGitHubPasswordOrToken
```

After that, you would have to create pull-request from 'next-version' branch and rebase it on master for next version development.
