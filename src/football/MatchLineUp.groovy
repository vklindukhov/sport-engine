package football

import groovy.transform.EqualsAndHashCode

import java.util.concurrent.atomic.AtomicInteger

@EqualsAndHashCode(includes = ['id'])
class MatchLineUp {
    final static AtomicInteger COUNTER = new AtomicInteger(1)
    long id;
    List<PlayerInfo> start11;
    List<PlayerInfo> substitutions;

    MatchLineUp(Map<String, Object> fields) {
        start11 = fields['start11'] as List<PlayerInfo>
        substitutions = fields['substitutions'] as List<PlayerInfo>
        id = COUNTER.getAndIncrement()
    }
}
