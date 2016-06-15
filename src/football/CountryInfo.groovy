package football

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import jdk.nashorn.internal.ir.annotations.Immutable

import java.util.concurrent.atomic.AtomicInteger

@Immutable
@EqualsAndHashCode(includes = ['id'])
@ToString(includePackage = false, includes = ['name'])
class CountryInfo {
    final static AtomicInteger COUNTER = new AtomicInteger(1)
    long id;
    String name;
    String leaguesUrlRequest
    List<LeagueInfo> leagueInfos

    CountryInfo(Map<String, Object> fields) {
        name = fields['name']
        leaguesUrlRequest = fields['leaguesUrlRequest']
        leagueInfos = fields['leagueInfos'] as List<LeagueInfo>
        id = COUNTER.getAndIncrement()
    }
}
