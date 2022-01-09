# Testing files

This directory contains files used by [tomakehurst/wiremock](https://github.com/tomakehurst/wiremock) for mocking third-party Websites requests.

See [Bug Report](../../../../.github/ISSUE_TEMPLATE/bug_report.md) for details.

## Naming conventions

The `XXX` should be replaced by a use case scenario (like date, some league, data ...).

### Mpg Mobile App

| **File**                                  | **URL**                                                                         | **Description**                      |
| ----------------------------------------- | ------------------------------------------------------------------------------- | ------------------------------------ |
| `mpg.user-signIn.XXX.json`                | <https://api.mpg.football/user/sign-in>                                         | **Login**                            |
| `mpg.dashboard.XXX.json`                  | <https://api.mpg.football/dashboard/leagues>                                    | **Dashboard / Home**                 |
| `mpg.division.XXX.json`                   | <https://api.mpg.football/division/mpg_division_MLEXXXXX_3_1>                   | **User->Team association**           |
| `mpg.team.XXX.json`                       | <https://api.mpg.football/team/mpg_team_MLEXXXXX_3_1_2>                         | **Current User Team**                |
| `mpg.coach.XXX.json`                      | <https://api.mpg.football/division/mpg_division_MLEXXXXX_3_1/coach>             | **coach**, `GET` requests            |
| `mpg.coach.XXX-Request-XXX.json`          | <https://api.mpg.football/match-team-formation/[matchTeamFormation.id]>         | MPG league **coach**, `PUT` requests |
| `mpg.division.available.players.XXX.json` | <https://api.mpg.football/division/mpg_division_MLEXXXXX_3_1/available-players> | MPG league **transfer**              |
| `mpg.poolPlayers.X.YYYY.json`             | <https://api.mpg.football/championship-players-pool/X>                          | **League Pool Player**               |
| `mpg.clubs.YYYY.json`                     | <https://api.mpg.football/championship-clubs>                                   | **Clubs details**                    |

### Players statistics

| **File**                           | **URL**                                           | **Description**                            |
| ---------------------------------- | ------------------------------------------------- | ------------------------------------------ |
| `mlnstats.builds.XXX.json`         | <https://api.mlnstats.com/builds>                 | MpgStats leagues **time update**           |
| `mlnstats.liga.XXX.json`           | <https://api.mlnstats.com/leagues/Liga>           | MpgStats for **Liga (Spain)**              |
| `mlnstats.ligue-1.XXX.json`        | <https://api.mlnstats.com/leagues/Ligue-1>        | MpgStats for **Ligue 1 (France)**          |
| `mlnstats.ligue-2.XXX.json`        | <https://api.mlnstats.com/leagues/Ligue-2>        | MpgStats for **Ligue 2 (France)**          |
| `mlnstats.premier-league.XXX.json` | <https://api.mlnstats.com/leagues/Premier-League> | MpgStats for **Premiere League (England)** |
| `mlnstats.serie-a.XXX.json`        | <https://api.mlnstats.com/leagues/Serie-A>        | MpgStats for **Seria A (Italia)**          |

### Players Injury / Suspended

| **File**                                         | **URL**                                                                                | **Description**                                                                                                                                                   |
| ------------------------------------------------ | -------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `maligue2.joueurs-blesses-et-suspendus.XXX.html` | <https://maligue2.fr/2020/08/20/joueurs-blesses-et-suspendus/>                         | Injury / Suspended players for **Ligue 2 (France)**                                                                                                               |
| `sportsgambler.liga.XXX.html`                    | <https://www.sportsgambler.com/injuries/football/spain-la-liga/>                       | Injury / Suspended players for **Liga (Spain)**                                                                                                                   |
| `sportsgambler.ligue-1.XXX.html`                 | <https://www.sportsgambler.com/injuries/football/france-ligue-1/>                      | Injury / Suspended players for **Ligue 1 (France)**                                                                                                               |
| `sportsgambler.premier-league.XXX.html`          | <https://www.sportsgambler.com/injuries/football/england-premier-league/>              | Injury / Suspended players for **Premiere League (England)**                                                                                                      |
| `sportsgambler.serie-a.XXX.html`                 | <https://www.sportsgambler.com/injuries/football/italy-serie-a/>                       | Injury / Suspended players for **Seria A (Italia)**                                                                                                               |
| `equipeactu.liga.XXX.html`                       | <https://www.equipeactu.fr/blessures-et-suspensions/fodbold/espagne/primera-division>  | **Deprecated since October 20, 2020** ([#169](https://github.com/axel3rd/mpg-coach-bot/issues/169)). Injury / Suspended players for **Liga (Spain)**              |
| `equipeactu.ligue-1.XXX.html`                    | <https://www.equipeactu.fr/blessures-et-suspensions/fodbold/france/ligue-1>            | **Deprecated since October 20, 2020** ([#169](https://github.com/axel3rd/mpg-coach-bot/issues/169)). Injury / Suspended players for **Ligue 1 (France)**          |
| `equipeactu.premier-league.XXX.html`             | <https://www.equipeactu.fr/blessures-et-suspensions/fodbold/angleterre/premier-league> | **Deprecated since October 20, 2020** ([#169](https://github.com/axel3rd/mpg-coach-bot/issues/169)). Injury / Suspended players for **Premiere League (England)** |
| `equipeactu.serie-a.XXX.html`                    | <https://www.equipeactu.fr/blessures-et-suspensions/fodbold/italie/serie-a>            | **Deprecated since October 20, 2020** ([#169](https://github.com/axel3rd/mpg-coach-bot/issues/169)). Injury / Suspended players for **Seria A (Italia)**          |
| `equipeactu.ligue-2.XXX.html`                    |                                                                                        | **Not used**, see below and [#99](https://github.com/axel3rd/mpg-coach-bot/issues/99), EquipeActu Website doesn't contain Ligue 2 (France)                        |

### Mpg Mobile App (complement, if needed)

Mercato history:

* GET <https://api.mpg.football/division/mpg_division_MLEXXXXX_4_1/history>
* GET <https://api.mpg.football/division/mpg_division_MLEXXXXX_2_1/history>

User leagues:

* GET <https://api.mpg.football/user> ; use info, see `hiddenLeaguesIds` field in addition
* GET <https://api.mpg.football/user/leagues> ; Leagues details

Enable/disable one league:

* PATCH <https://api.mpg.football/league/mpg_league_MLEXXXXX/un-hide>
* PATCH <https://api.mpg.football/league/mpg_league_MLEXXXXX/hide>

### Mpg WebSite (deprecated since 19 July 2021)

| **File**                         | **URL**                                                        | **Description**                       |
| -------------------------------- | -------------------------------------------------------------- | ------------------------------------- |
| `mpg.coach.XXX.json`             | <https://api.monpetitgazon.com/league/[leagueId]/coach>        | MPG league **coach**, `GET` requests  |
| `mpg.coach.XXX-Request-XXX.json` | <https://api.monpetitgazon.com/league/[leagueId]/coach>        | MPG league **coach**, `POST` requests |
| `mpg.dashboard.XXX.json`         | <https://api.monpetitgazon.com/user/dashboard>                 | MPG **dashboard**                     |
| `mpg.mercato.XXX.json`           | <https://api.monpetitgazon.com/league/[leagueId]/mercato>      | MPG league **mercato**                |
| `mpg.transfer.buy.XXX.json`      | <https://api.monpetitgazon.com/league/[leagueId]/transfer/buy> | MPG league **transfer**               |
| `mpg.user-signIn.XXX.json`       | <https://api.monpetitgazon.com/user/signIn>                    | **Login**                             |

### Players statistics (deprecated since 15 December 2019)

| **File**                           | **URL**                                                       | **Description**                                                                         |
| ---------------------------------- | ------------------------------------------------------------- | --------------------------------------------------------------------------------------- |
| `mpgstats.leagues.XXX.json`        | <https://www.mpgstats.fr/json/leagues.json>                   | Used prior end of 2019. See [#134](https://github.com/axel3rd/mpg-coach-bot/issues/134) |
| `mpgstats.liga.XXX.json`           | <https://www.mpgstats.fr/json/customteam.json/Liga>           | Used prior end of 2019. See [#134](https://github.com/axel3rd/mpg-coach-bot/issues/134) |
| `mpgstats.ligue-1.XXX.json`        | <https://www.mpgstats.fr/json/customteam.json/Ligue-1>        | Used prior end of 2019. See [#134](https://github.com/axel3rd/mpg-coach-bot/issues/134) |
| `mpgstats.ligue-2.XXX.json`        | <https://www.mpgstats.fr/json/customteam.json/Ligue-2>        | Used prior end of 2019. See [#134](https://github.com/axel3rd/mpg-coach-bot/issues/134) |
| `mpgstats.premier-league.XXX.json` | <https://www.mpgstats.fr/json/customteam.json/Premier-League> | Used prior end of 2019. See [#134](https://github.com/axel3rd/mpg-coach-bot/issues/134) |
| `mpgstats.serie-a.XXX.json`        | <https://www.mpgstats.fr/json/customteam.json/Serie-A>        | Used prior end of 2019. See [#134](https://github.com/axel3rd/mpg-coach-bot/issues/134) |
