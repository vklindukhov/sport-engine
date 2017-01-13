package basketball.entity

import groovy.transform.EqualsAndHashCode
import jdk.nashorn.internal.ir.annotations.Immutable

import java.time.LocalDateTime

@Immutable
@EqualsAndHashCode(includes = ['url'])
class MyScoreMatchInfo {
    String url
    LocalDateTime dateTime
    String countryName
    String tournamentName
    String homeTeamName
    String awayTeamName
    short minTotal
    double total
    short maxTotal
    double borderShiftCoefficient = 0.1

    MyScoreMatchInfo(Map<String, Object> fields) {
        url = fields['url'] as String
        dateTime = fields['dateTime'] as LocalDateTime
        countryName = fields['countryName'] as String
        tournamentName = fields['tournamentName'] as String
        homeTeamName = fields['team1Name'] as String
        awayTeamName = fields['team2Name'] as String
    }

    public setAwayTeamName(String awayTeamName) {
        this.awayTeamName = awayTeamName
    }


    @Override
    public String toString() {
        def hour = dateTime.getHour() < 10 ? '0' + dateTime.getHour() : dateTime.getHour()
        def minute = dateTime.getMinute() < 10 ? '0' + dateTime.getMinute() : dateTime.getMinute()
        hour + ':' + minute + ' - ' + url + ' - ' + countryName + ': ' + tournamentName + ': ' +
                homeTeamName + ' vs ' + awayTeamName + ': ' +
                (Math.round((1 - borderShiftCoefficient) * total) as int) + '-' + minTotal + '/' +
                total + '/' +
                maxTotal + '-' + (Math.round((1 + borderShiftCoefficient) * total) as int)
    }
}
