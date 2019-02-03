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

1. The [MPG](https://mpg.football/) JSon *Response* of these *Request* (visible via F12 in your browser):
- From home : `GET /user/dashboard`
- From coach: `GET /league/[yourLeagueId]/coach`

2. The [Players statistics](https://www.mpgstats.fr/) data, one JSon from:
- https://www.mpgstats.fr/json/customteam.json/Ligue-1
- https://www.mpgstats.fr/json/customteam.json/Premier-League
- https://www.mpgstats.fr/json/customteam.json/Liga

3. The [Players statistics](https://www.mpgstats.fr/) time update datas : https://www.mpgstats.fr/json/leagues.json

4. The [Injury / Suspended](http://www.equipeactu.fr/blessures-et-suspensions/fodbold/) data, one full HTML from:
- http://www.equipeactu.fr/blessures-et-suspensions/fodbold/france/ligue-1
- http://www.equipeactu.fr/blessures-et-suspensions/fodbold/angleterre/championship
- http://www.equipeactu.fr/blessures-et-suspensions/fodbold/espagne/primera-division

**Expected behavior**

A clear and concise description of what you expected to happen.

If problem on *update team* feature, please join the *Request* and *Response* (visible via F12 in your browser) of `POST /league/[yourLeagueId]/coach` when you save your team in [MPG](https://mpg.football/).
