# Testing files

This directory contains files used by [tomakehurst/wiremock](https://github.com/tomakehurst/wiremock) for mocking third-party Websites requests.

See [Bug Report](../../../../.github/ISSUE_TEMPLATE/bug_report.md) for details.

## Naming conventions

The `XXX` should be replaced by a use case scenario (like data, some league, ...).

### Mpg

| **File** | **URL** | **Description** |
| --- | --- | --- |
| `mpg.coach.XXX.json` | https://api.monpetitgazon.com/league/[leagueId]/coach | MPG league **coach**, `GET` requests |
| `mpg.coach.XXX-Request-XXX.json` | https://api.monpetitgazon.com/league/[leagueId]/coach | MPG league **coach**, `POST` requests |
| `mpg.dashboard.XXX.json` | https://api.monpetitgazon.com/user/dashboard | MPG **dashboard** |
| `mpg.mercato.XXX.json` | https://api.monpetitgazon.com | MPG league **mercato** |
| `mpg.transfer.buy.XXX.json` | https://api.monpetitgazon.com/league/[leagueId]/transfer/buy | MPG league **transfer** |
| `mpg.user-signIn.XXX.json` | https://api.monpetitgazon.com/user/signIn | **Login** |

### Players statistics

| **File** | **URL** | **Description** |
| --- | --- | --- |
| `mlnstats.builds.XXX.json` | https://api.mlnstats.com/builds | MpgStats leagues **time update** |
| `mlnstats.liga.XXX.json` | https://api.mlnstats.com/leagues/Liga | MpgStats for **Liga (Spain)** |
| `mlnstats.ligue-1.XXX.json` | https://api.mlnstats.com/leagues/Ligue-1 | MpgStats for **Ligue 1 (France)** |
| `mlnstats.ligue-2.XXX.json` | https://api.mlnstats.com/leagues/Ligue-2 | MpgStats for **Ligue 2 (France)** |
| `mlnstats.premier-league.XXX.json` | https://api.mlnstats.com/leagues/Premier-League | MpgStats for **Premiere League (England)** |
| `mlnstats.serie-a.XXX.json` | https://api.mlnstats.com/leagues/Serie-A | MpgStats for **Seria A (Italia)** |
| `mpgstats.leagues.XXX.json` | https://www.mpgstats.fr/json/leagues.json | Used prior end of 2019. See [#134](https://github.com/axel3rd/mpg-coach-bot/issues/134) |
| `mpgstats.liga.XXX.json` | https://www.mpgstats.fr/json/customteam.json/Liga | Used prior end of 2019. See [#134](https://github.com/axel3rd/mpg-coach-bot/issues/134) |
| `mpgstats.ligue-1.XXX.json` | https://www.mpgstats.fr/json/customteam.json/Ligue-1 | Used prior end of 2019. See [#134](https://github.com/axel3rd/mpg-coach-bot/issues/134) |
| `mpgstats.ligue-2.XXX.json` | https://www.mpgstats.fr/json/customteam.json/Ligue-2 | Used prior end of 2019. See [#134](https://github.com/axel3rd/mpg-coach-bot/issues/134) |
| `mpgstats.premier-league.XXX.json` | https://www.mpgstats.fr/json/customteam.json/Premier-League | Used prior end of 2019. See [#134](https://github.com/axel3rd/mpg-coach-bot/issues/134) |
| `mpgstats.serie-a.XXX.json` | https://www.mpgstats.fr/json/customteam.json/Serie-A | Used prior end of 2019. See [#134](https://github.com/axel3rd/mpg-coach-bot/issues/134) |

### Players Injury / Suspended 

| **File** | **URL** | **Description** |
| --- | --- | --- |
| `equipeactu.liga.XXX.html` | https://www.equipeactu.fr/blessures-et-suspensions/fodbold/espagne/primera-division | Injury / Suspended players for **Liga (Spain)** |
| `equipeactu.ligue-1.XXX.html` | https://www.equipeactu.fr/blessures-et-suspensions/fodbold/france/ligue-1 | Injury / Suspended players for **Ligue 1 (France)** |
| `equipeactu.ligue-2.XXX.html` |  | **Not used**, see below and [#99](https://github.com/axel3rd/mpg-coach-bot/issues/99), EquipeActu Website doesn't contain Ligue 2 (France) |
| `equipeactu.premier-league.XXX.html` | https://www.equipeactu.fr/blessures-et-suspensions/fodbold/angleterre/premier-league | Injury / Suspended players for **Premiere League (England)** |
| `equipeactu.serie-a.XXX.html` | https://www.equipeactu.fr/blessures-et-suspensions/fodbold/italie/serie-a | Injury / Suspended players for **Seria A (Italia)** |
| `maligue2.joueurs-blesses-et-suspendus.XXX.html` | https://maligue2.fr/2019/08/05/joueurs-blesses-et-suspendus/ | Injury / Suspended players for **Ligue 2 (France)** |
