---
name: Bug report
about: Create a report to help us improve
title: ''
labels: bug
assignees: ''

---

**Describe the bug**

A clear and concise description of what the bug is, with a copy/paste of Java exception if present.

**Join debug files**

Depending your championship, join data files set in attachment.

1. The [MPG](https://mpg.football/) JSon *Response* of these *Request* on https://api.monpetitgazon.com (use the browser network monitor - F12, see wiki [Get MPG data for opening a bug](https://github.com/axel3rd/mpg-coach-bot/wiki/Get-MPG-data-for-opening-a-bug) for more details):

- From home : `GET /user/dashboard`
- From coach: `GET /league/[yourLeagueId]/coach`
- From transfer : `GET /league/[yourLeagueId]/transfer/buy`
- From mercato : `GET /league/[yourLeagueId]/mercato`

2. The [Players statistics](https://www.mpgstats.fr/) data, one JSon from:

- https://api.mlnstats.com/leagues/Ligue-1
- https://api.mlnstats.com/leagues/Ligue-2
- https://api.mlnstats.com/leagues/Premier-League
- https://api.mlnstats.com/leagues/Liga
- https://api.mlnstats.com/leagues/Serie-A

3. The [Players statistics](https://www.mpgstats.fr/) time update data : https://api.mlnstats.com/builds

4. The [Injury / Suspended](https://www.sportsgambler.com/football/injuries-suspensions/) data, one full HTML from:

- https://maligue2.fr/2019/08/05/joueurs-blesses-et-suspendus/
- https://www.sportsgambler.com/football/injuries-suspensions/france-ligue-1/
- https://www.sportsgambler.com/football/injuries-suspensions/england-premier-league/
- https://www.sportsgambler.com/football/injuries-suspensions/spain-la-liga/
- https://www.sportsgambler.com/football/injuries-suspensions/italy-serie-a/

**Expected behavior**

A clear and concise description of what you expected to happen.

If problem on *update team* feature, please join the *Request* and *Response* of `POST /league/[yourLeagueId]/coach` when you save your team in [MPG](https://mpg.football/).
