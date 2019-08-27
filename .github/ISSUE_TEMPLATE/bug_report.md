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

2. The [Players statistics](https://www.mpgstats.fr/) data, one JSon from:

- https://www.mpgstats.fr/json/customteam.json/Ligue-1
- https://www.mpgstats.fr/json/customteam.json/Ligue-2
- https://www.mpgstats.fr/json/customteam.json/Premier-League
- https://www.mpgstats.fr/json/customteam.json/Liga
- https://www.mpgstats.fr/json/customteam.json/Serie-A

3. The [Players statistics](https://www.mpgstats.fr/) time update data : https://www.mpgstats.fr/json/leagues.json

4. The [Injury / Suspended](http://www.equipeactu.fr/blessures-et-suspensions/fodbold/) data, one full HTML from:

- http://www.equipeactu.fr/blessures-et-suspensions/fodbold/france/ligue-1
- http://www.equipeactu.fr/blessures-et-suspensions/fodbold/france/ligue-2
- http://www.equipeactu.fr/blessures-et-suspensions/fodbold/angleterre/championship
- http://www.equipeactu.fr/blessures-et-suspensions/fodbold/espagne/primera-division
- http://www.equipeactu.fr/blessures-et-suspensions/fodbold/italie/serie-a

**Expected behavior**

A clear and concise description of what you expected to happen.

If problem on *update team* feature, please join the *Request* and *Response* of `POST /league/[yourLeagueId]/coach` when you save your team in [MPG](https://mpg.football/).
