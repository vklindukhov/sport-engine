package football.entity

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import java.time.LocalDate
import java.time.LocalDateTime

@EqualsAndHashCode(includes = ['url'])
@ToString(includePackage = false, includes = ['id'])
class MatchReport {
    long id;
    String url
    LocalDateTime date;
    ClubInfo home
    ClubInfo away
    Map<ClubInfo, MatchLineUp> lineups = new HashMap<>();
    StatisticsReport resultReport;

    MatchReport(Map<String, Object> fields) {
        id = fields['id'] as long
        url = fields['url']
        date = fields['date'] as LocalDateTime
        home = fields['home'] as ClubInfo
        away = fields['away'] as ClubInfo
        resultReport = new StatisticsReport(home, away)
    }
}
