package basketball.entity

import groovy.transform.EqualsAndHashCode
import jdk.nashorn.internal.ir.annotations.Immutable

@Immutable
@EqualsAndHashCode(includes = ['url'])
class PlayerStatisticsInfo {
    long id
    Map info = new HashMap()

    PlayerStatisticsInfo(Map<String, Object> fields) {
        id = fields['id'] as long
    }

}
