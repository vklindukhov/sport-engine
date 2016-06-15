package football

import groovy.transform.EqualsAndHashCode
import jdk.nashorn.internal.ir.annotations.Immutable

import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger

@Immutable
@EqualsAndHashCode(includes = ['url'])
class PlayerInfo {
    final static AtomicInteger COUNTER = new AtomicInteger(1)
    long id
    String url
    String name
    String surname
    String nationality
    LocalDate birthday
    List<Position> positions
    short height
    int weight
    List<PlayerClubHistory> clubs

    PlayerInfo(Map<String, Object> fields) {
        url = fields['url']
        name = fields['name']
        surname = fields['surname']
        nationality = fields['nationality']
        birthday = fields['birthday'] as LocalDate
        positions = fields['positions'] as List<Position>
        height = fields['height'] == null ? 0 : fields['height'] as short
        weight = fields['weight'] == null ? 0 : fields['weight'] as short
        clubs = fields['clubs'] as List<PlayerClubHistory>
        id = COUNTER.getAndIncrement()
    }

    void addPosition(Position newPosition) {
        positions << newPosition
    }

    void transferToClub(PlayerClubHistory newClubHistory) {
        clubs << newClubHistory
    }

    @Override
    public String toString() {
        return name + ' ' + surname + ' ' + nationality + ' ' + positions?.displayName + ' ' + birthday + ' ' + clubs?.last()?.club?.name
    }

    public enum Position {
        GK('Goalkeeper'), D('Defender'), MD('Midfielder'), STRIKER('Attacker')

        String displayName

        Position(String displayName) {
            this.displayName = displayName
        }

        public static Position getByDisplayName(String displayName) {
            values().find { it.displayName == displayName }
        }
    }
}
