package football

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import jdk.nashorn.internal.ir.annotations.Immutable

import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger

@Immutable
@EqualsAndHashCode(includes = ['url'])
@ToString(includePackage = false, includes = ['name', 'allPlayers'])
class ClubInfo {
    final static AtomicInteger COUNTER = new AtomicInteger(1)
    long id;
    String name
    Set<PlayerInfo> allPlayers
    Map<String, Set<MatchReport>> matches
    String url

    ClubInfo(String name) {
        this.name = name
        allPlayers = []
        matches = new HashMap<>()
        id = COUNTER.getAndIncrement()
    }

    void addPlayer(PlayerInfo player, LocalDate startFrom) {
        player.clubs << new PlayerClubHistory(startFrom, this)
        allPlayers << player
    }

    void removePlayer(PlayerInfo player, LocalDate endAt) {
         player.clubs[-1].end = endAt
        allPlayers.remove player
    }
}
