package football

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(includes = ['id'])
class StatisticsReport {
    long id;
    Map<ClubInfo, Map<String, Byte>> statistics = new HashMap<>()
    Map<ClubInfo, List<EventReport>> eventsReport;
    private ClubInfo home
    private ClubInfo away

    StatisticsReport(ClubInfo home, ClubInfo away) {
        this.home = home
        this.away = away
        statistics[home] = new HashMap<>()
        statistics[away] = new HashMap<>()
        eventsReport = new HashMap<>()
        eventsReport[home] = []
        eventsReport[away] = []
    }

    void addStatisticParameter(String parameterName, homeValue, awayValue) {
        statistics[home][parameterName] = homeValue
        statistics[away][parameterName] = awayValue
    }

    void addEventReport(ClubInfo toClub, EventReport eventReport) {
        eventsReport[toClub] << eventReport
    }
}
