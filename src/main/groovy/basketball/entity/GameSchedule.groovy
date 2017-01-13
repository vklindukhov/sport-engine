package basketball.entity

import groovy.transform.EqualsAndHashCode
import jdk.nashorn.internal.ir.annotations.Immutable

import java.time.LocalDate

@Immutable
@EqualsAndHashCode(includes = ['gameId'])
class GameSchedule {
    int seasonId
    String gameId
    LocalDate gameDate
    Tuple2<Club, Club> matchup

    GameSchedule(Map<String, Object> fields) {
        this.seasonId = fields['seasonId'] as int
        this.gameId = fields['gameId'] as String
        this.gameDate = fields['gameDate'] as LocalDate
        this.matchup = fields['matchup'] as Tuple2
    }


    @Override
    public String toString() {
        "${gameId} - ${gameDate} - ${matchup.first.name()} vs ${matchup.second.name()}"
    }
}
