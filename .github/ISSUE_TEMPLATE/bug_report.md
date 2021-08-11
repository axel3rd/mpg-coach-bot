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

1. The MPG Mobile App JSon *Response* of these *Request* on https://api.mpg.football (use the browser network monitor - F12, see wiki [Get MPG data for opening a bug](https://github.com/axel3rd/mpg-coach-bot/wiki/Get-MPG-data-for-opening-a-bug) for more details):

| Feature | URL | Reason |
| --- | --- | --- | 
| login | `GET /user/sign-in` | Retrieve **userId** |
| dashboard  | `GET /dashboard/leagues` | Retrieve **divisionId** (~ `mpg_division_MLEXXXXX_3_1`) | 
| division | `GET /division/mpg_division_MLEXXXXX_3_1` | Retrieve `mpg_team_MLEXXXXX_3_1_2` team for user league |
| team | `GET /team/mpg_team_MLEXXXXX_3_1_2` |  Retrieve Team and Bonus for team |
| coach | `GET /division/mpg_division_MLEXXXXX_3_1/coach` | Retrieve formation |
| availablePlayers | `GET /division/mpg_division_MLEXXXXX_3_1/available-players` | Retrieves available players details for incoming mercato or trading |
| poolPlayer | `GET /championship-players-pool/X` | Retrieve league players details (With `X`: 1=Ligue-1 / 2=Premier-League / 3=Liga / 4=Ligue-2 / 5=Serie-A) |
| clubs | `GET /championship-clubs` | Retrieve club names |

2. The [Players statistics](https://www.mpgstats.fr/) data, one JSon from:

- https://api.mlnstats.com/leagues/Ligue-1
- https://api.mlnstats.com/leagues/Ligue-2
- https://api.mlnstats.com/leagues/Premier-League
- https://api.mlnstats.com/leagues/Liga
- https://api.mlnstats.com/leagues/Serie-A

3. The [Players statistics](https://www.mpgstats.fr/) time update data : https://api.mlnstats.com/builds

4. The [Injury / Suspended](https://www.sportsgambler.com/injuries/football/) data, one full HTML from:

- https://maligue2.fr/2020/08/20/joueurs-blesses-et-suspendus/
- https://www.sportsgambler.com/injuries/football/france-ligue-1/
- https://www.sportsgambler.com/injuries/football/england-premier-league/
- https://www.sportsgambler.com/injuries/football/spain-la-liga/
- https://www.sportsgambler.com/injuries/football/italy-serie-a/

**Expected behavior**

A clear and concise description of what you expected to happen.

If problem on *update team* feature, please join the *Request* and *Response* of `PUT /match-team-formation/['matchTeamFormation.id' from 'coach' request, start with 'mpg_match_team_formation_']` when you save your team in MPG Mobile App.
