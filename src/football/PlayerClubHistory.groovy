package football

import groovy.transform.EqualsAndHashCode

import java.time.LocalDate

@EqualsAndHashCode(includes = ['id'])
class PlayerClubHistory {
    long id;
    LocalDate start;
    LocalDate end;
    ClubInfo club;

    PlayerClubHistory(LocalDate start, ClubInfo club) {
        this.start = start
        this.club = club
    }

    PlayerClubHistory(LocalDate start, LocalDate end, ClubInfo club) {
        this.start = start
        this.end = end
        this.club = club
    }

    @Override
    String toString() {
        '' + start + ' - ' + end + ': ' + club.name
    }

}