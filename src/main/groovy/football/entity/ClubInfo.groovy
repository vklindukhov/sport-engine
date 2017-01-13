package football.entity

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import jdk.nashorn.internal.ir.annotations.Immutable

import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger

@Immutable
@EqualsAndHashCode(includes = ['url'])
@ToString(includePackage = false, includes = ['id'])
class ClubInfo {
    final static AtomicInteger COUNTER = new AtomicInteger(1)
    long id;
    String name
    Set<PlayerInfo> allPlayers
    Map<String, Set<MatchReport>> matches

    ClubInfo(long id, String name) {
        this.id = id
        this.name = name
        allPlayers = []
        matches = new HashMap<>()
    }

    void addPlayer(PlayerInfo player, LocalDate startFrom) {
        player.clubs << new PlayerClubHistory(startFrom, this)
        allPlayers << player
    }

    void removePlayer(PlayerInfo player, LocalDate endAt) {
         player.clubs[-1].end = endAt
        allPlayers.remove player
    }

    Set<MatchReport> getCompetitionMatches(String competitionName) {
        if(!matches.containsKey(competitionName)) matches[competitionName] = new HashSet<>()
        return matches[competitionName]
    }
}
