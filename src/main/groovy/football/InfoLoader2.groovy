package football

import football.entity.ClubInfo
import football.entity.CountryInfo
import football.entity.LeagueInfo
import football.entity.MatchReport
import groovy.json.JsonSlurper
import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.NodeChild
import utils.XmlUtils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

import static java.time.LocalDate.parse
import static java.time.format.DateTimeFormatter.ofPattern
import static java.util.Locale.ENGLISH

COUNTRIES = new TreeMap<>()
LEAGUES = new HashMap<>()
CLUBS = new HashMap<>()
PLAYERS = new HashMap<>()
MATCH_REPORTS = new HashMap<>()

Map<String, Map<String, String>> urlsCompetitionsForEachCountry = [
//        "England":
//                [
//                        "Premier League": "http://www.sofascore.com/tournament/football/england/premier-league/17",
//                        "Championship"  : "http://www.sofascore.com/ru/tournament/football/england/championship/18",
//                        "League One"    : "http://www.sofascore.com/ru/tournament/football/england/league-one/24",
//                        "League Two"    : "http://www.sofascore.com/ru/tournament/football/england/league-two/25"
//                ],
//        "Spain"  :
//                [
//                        "Primera Division"  : "http://www.sofascore.com/ru/tournament/football/spain/primera-division/8",
//                        "Segunda Division"  : "http://www.sofascore.com/ru/tournament/football/spain/segunda-division/54",
//                        "Segunda Division B": "http://www.sofascore.com/ru/tournament/football/spain/segunda-b-group-i/544"
//                ],
"Germany":
        [
//                        "Bundesliga"   : "http://www.sofascore.com/ru/tournament/football/germany/bundesliga/35",
//                        "2. Bundesliga": "http://www.sofascore.com/ru/tournament/football/germany/2nd-bundesliga/44",
"3. Liga": "http://www.sofascore.com/ru/tournament/football/germany/3rd-liga/491"
        ]
]

urlsCompetitionsForEachCountry.each { country, leaguesMap -> initCountries(country, leaguesMap) }

LEAGUES.each { k, v ->
    LeagueInfo leagueInfo = v as LeagueInfo
    leagueInfo.jsons.each { url, json ->
        Short year = getYear(json)
        if (leagueInfo.seasons[year] == null || leagueInfo.seasons[year].isEmpty()) leagueInfo.seasons[year] = new ArrayList<>()

        List list = json['standingsTables'] as List
        def iterator = list.iterator()
        if (iterator.hasNext()) {
            Byte roundsAmount = iterator.next()['round'] as Byte
            1.upto(roundsAmount) {
                URL roundUrl = new URL(url.toString() + "/matches/round/" + it)
                def roundJson = new JsonSlurper().parseText(roundUrl.text)
                List matches = roundJson['roundMatches']['tournaments']['events'] as List
                def iter = matches.iterator()
                if (iter.hasNext()) {
                    def subList = iter.next()
                    subList.each { match ->
                        MatchReport matchReport = createMatchReport(match)
                        MATCH_REPORTS[matchReport.id] = matchReport
                    }
                }
            }

            matches.each {
            }
        }

//        json.value['teamEvents'].each { e -> addMatchReport(e, seasons, year) }
    }
}

private MatchReport createMatchReport(match) {
    Integer matchId = match['id'] as Integer
    def date = parse(match['formatedStartDate'] as String, ofPattern('dd.MM.yyyy.', ENGLISH))
    def time = LocalTime.parse(match['startTime'] as String, ofPattern('kk:mm', ENGLISH))
    def localDateTime = LocalDateTime.of(date, time)
    println(match['id'] + ' - ' + match['name'] + ' at ' + date)


    String homeTeam = match['homeTeam']['name'] as String
    Integer homeScore = match['homeScore']['current'] as Integer
    long homeId = match['homeTeam']['id'] as long
    ClubInfo homeClub = getClub(homeId, homeTeam)
    Integer homeRedCards = match['homeRedCards'] as Integer


    Integer awayScore = match['awayScore']['current'] as Integer
    String awayTeam = match['awayTeam']['name'] as String
    long awayId = match['awayTeam']['id'] as long
    ClubInfo awayClub = getClub(awayId, awayTeam)
    Integer awayRedCards = match['awayRedCards'] as Integer


    MatchReport matchReport = new MatchReport(id: matchId, home: homeClub, away: awayClub, date: localDateTime)
    matchReport
}

private ClubInfo getClub(long id, String name) {
    ClubInfo club = CLUBS[id] as ClubInfo
    if (club == null) {
        club = new ClubInfo(id, name)
        CLUBS[id] = club
    }
    club
}

private void initCountries(String country, Map<String, String> leaguesMap) {
    CountryInfo countryInfo = new CountryInfo(
            name: country,
            leaguesUrlRequest: "http://www.sofascore.com/ru/football/" + country.replace(' ', '-').toLowerCase()
    )
    COUNTRIES[country] = countryInfo
    leaguesMap.each { leagueName, url -> initLeagues(leagueName, url) }
}

private void initLeagues(String leagueName, String url) {
    LeagueInfo leagueInfo = new LeagueInfo(name: leagueName, url: url)
    LEAGUES[url] = leagueInfo
    initSeasonUrls(leagueInfo)

}

private ArrayList initSeasonUrls(LeagueInfo leagueInfo) {
    def html = XmlUtils.getHtmlByUrl(leagueInfo.url, null)

    GPathResult seasons = findTagWithAttributeValue(
            html,
            'ul',
            'class',
            'dropdown__menu dropdown__menu--compact js-uniqueTournament-page-seasons-select'
    )

    seasons."**".findAll {
        NodeChild node = it as NodeChild
        if (isTagWithName(node, 'li')) {
            Iterator iterator = node.children().iterator()
            NodeChild a = iterator.next() as NodeChild
            String tournamentId = a.attributes().get('data-uniquetournament-id')
            String seasonId = a.attributes().get('data-season-id')
            String seasonUrl = "http://www.sofascore.com/u-tournament/" + tournamentId + "/season/" + seasonId
            def url = new URL(seasonUrl)
            leagueInfo.jsons[url] = new JsonSlurper().parseText(new URL(seasonUrl + "/json").text)
        }
    }
}

GPathResult findTagWithAttributeValue(matchSummaryHtml, String tagName, String attrName, String className) {
    matchSummaryHtml."**".find {
        isTagWithAttributeValue(it as NodeChild, tagName, attrName, className)
    } as GPathResult
}

boolean isTagWithName(NodeChild node, String tagName) {
    !isBlank(tagName) && node.name().toLowerCase() == tagName
}

boolean isTagWithAttributeValue(NodeChild node, String tagName, String attrName, String className) {
    try {
        if (!isBlank(tagName)) false
        def isTag = node.name().toLowerCase() == tagName
        if (!isBlank(attrName) && !isBlank(className)) {
            if (isTag && (node.attributes().get(attrName) as String)?.contains(className)) {
                return true
            }
            return false
        }
        return isTag
    } catch (e) {
        System.err.println('isTagWithAttributeValue - ' + e.getMessage())
        return false
    }
}

boolean isBlank(String str) {
    str == null || str.isEmpty()
}

private Short getYear(Object json) {
//
//private Object addMatchReport(e, Map<Short, List<MatchReport>> seasons, short year) {
//    e.each { ee ->
//        ee.value.each { eee ->
//            eee.value['total'].each { map ->
//                MatchReport report = createMatchReport(map)
//                seasons[year] << report
//            }
//        }
//    }
//}
    Short tail = json['season']['year'].split('/')[0] as Short
    Short startYear = 2000 + tail
    if (LocalDate.now().year < startYear) startYear = 1900 + tail
    startYear
}
