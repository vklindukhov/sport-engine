package football2.entity

import basketball.entity.SportType
import groovy.transform.EqualsAndHashCode
import jdk.nashorn.internal.ir.annotations.Immutable

import java.time.LocalDateTime

@Immutable
@EqualsAndHashCode(includes = ['url'])
class ForkInfoLog {
    SportType sportType
    long id
    String url
    String tournamentTitle
    String team1Name
    String team2Name
    byte sinceMin
    double odd
    LocalDateTime kickOffDateTime
    LocalDateTime matchedAt

    ForkInfoLog(Map<String, Object> fields) {
        sportType = fields['sportType'] as SportType
        id = fields['id'] as long
        url = fields['url'] as String
        tournamentTitle = fields['tournamentTitle'] as String
        team1Name = fields['team1Name'] as String
        team2Name = fields['team2Name'] as String
        kickOffDateTime = fields['kickOffDateTime'] as LocalDateTime
    }

    @Override
    public String toString() {
        url + ' - ' + tournamentTitle + ' - ' + team1Name + ' vs ' + team2Name +
                " - matched at ${matchedAt}, kick off at ${kickOffDateTime} - ${sinceMin}-90 with odd ${odd}"

    }
}
