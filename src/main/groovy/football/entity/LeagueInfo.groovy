package football.entity

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import jdk.nashorn.internal.ir.annotations.Immutable

import java.util.concurrent.atomic.AtomicInteger

@Immutable
@EqualsAndHashCode(includes = ['url'])
@ToString(includePackage = false, includes = ['name'])
class LeagueInfo {
    final static AtomicInteger COUNTER = new AtomicInteger(1)
    long id;
    String name
    URL url
//    String leagueUrl
//    String tablesUrl
    Map<Short, ClubInfo> clubs
    Map<URL, Object> jsons
    Map<Short, List<MatchReport>> seasons

    LeagueInfo(Map<String, Object> fields) {
        name = fields['name']
        url = new URL(fields['url'] as String)
//        leagueUrl = fields['leagueUrl']
//        tablesUrl = fields['tablesUrl']
//        clubs = fields['clubs'] as Set<ClubInfo>
        jsons = new HashMap<>()
        seasons = new HashMap<>()
        id = COUNTER.getAndIncrement()
    }
}
