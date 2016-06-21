package football

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
    String url
    String leagueUrl
    String tablesUrl
    Set<ClubInfo> clubs

    LeagueInfo(Map<String, Object> fields) {
        name = fields['name']
        url = fields['url']
        leagueUrl = fields['leagueUrl']
        tablesUrl = fields['tablesUrl']
        clubs = fields['clubs'] as Set<ClubInfo>
        id = COUNTER.getAndIncrement()
    }
}
