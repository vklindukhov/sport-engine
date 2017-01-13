package basketball.entity

import football.entity.PlayerClubHistory
import groovy.transform.EqualsAndHashCode
import jdk.nashorn.internal.ir.annotations.Immutable

import java.time.LocalDate

@Immutable
@EqualsAndHashCode(includes = ['url'])
class PlayerInfo {
    long id
    String url
    String title
    String name
    String surname
    LocalDate birthday
    Position position
    short height
    int weight
    List<PlayerClubHistory> clubs;
    Map<String, List> seasonsGamesLogs = new HashMap()

    PlayerInfo(Map<String, Object> fields) {
        id = fields['id'] as long
        url = fields['url']
        title = fields['title']
        name = fields['name']
        surname = fields['surname']
        birthday = fields['birthday'] as LocalDate
        position = fields['position'] as Position
        height = fields['height'] == null ? 0 : fields['height'] as short
        weight = fields['weight'] == null ? 0 : fields['weight'] as short
    }


    @Override
    public String toString() {
        'id=' + id + ' - ' + url + ' - ' + name + ' ' + surname
//        name + ' ' + surname + ' ' + ' ' + position?.displayName + ' ' + birthday + ' ' + clubs?.last()?.club?.displayName
    }

    public enum Position {
        PG('Point Guard'), SG('Shooting Guard'), SF('Small Forward'), PF('Power Forward'), C('Center')

        String displayName

        Position(String displayName) {
            this.displayName = displayName
        }

        public static Position getByDisplayName(String displayName) {
            values().find { it.displayName == displayName }
        }
    }
}