package football

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger

@EqualsAndHashCode(includes = ['url'])
@ToString(includePackage = false, includes = ['home', 'away', 'url'])
class MatchReport {
    final static AtomicInteger COUNTER = new AtomicInteger(1)
    long id;
    String url
    LocalDate date;
    ClubInfo home
    ClubInfo away
    Map<ClubInfo, MatchLineUp> lineups = new HashMap<>();
    StatisticsReport resultReport;

    MatchReport(Map<String, Object> fields) {
        url = fields['url']
        date = fields['date'] as LocalDate
        home = fields['home'] as ClubInfo
        away = fields['away'] as ClubInfo
        resultReport = new StatisticsReport(home, away)
        id = COUNTER.getAndIncrement()
    }
}
