package football.entity

import basketball.entity.Club
import groovy.transform.EqualsAndHashCode

import java.time.LocalDate

@EqualsAndHashCode(includes = ['id'])
class PlayerClubHistory {
    long id;
    LocalDate start;
    LocalDate end;
    Club club;

    PlayerClubHistory(Club club) {
        this.club = club
    }

    PlayerClubHistory(LocalDate start, Club club) {
        this.start = start
        this.club = club
    }

    PlayerClubHistory(LocalDate start, LocalDate end, Club club) {
        this.start = start
        this.end = end
        this.club = club
    }

    @Override
    String toString() {
        '' + start + ' - ' + end + ': ' + club.displayName
    }

}