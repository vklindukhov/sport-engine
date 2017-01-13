package basketball.entity

import groovy.transform.EqualsAndHashCode
import jdk.nashorn.internal.ir.annotations.Immutable

@Immutable
@EqualsAndHashCode(includes = ['SEASON_ID,PLAYER_ID'])
class DefensiveImpact {
    int SEASON_ID
    int PLAYER_ID
    Club club
    byte GP
    byte W
    byte L
    float MIN
    float STL
    float BLK
    float DREB
    float DFGM
    float DFGA
    float DFG_PCT

    DefensiveImpact(Map<String, Object> fields) {
        this.SEASON_ID = fields['SEASON_ID'] as int
        this.PLAYER_ID = fields['PLAYER_ID'] as int
        this.club = Club.getClubByCode(fields['TEAM_ABBREVIATION'] as String)
        this.GP = fields['GP'] as byte
        this.W = fields['W'] as byte
        this.L = fields['L'] as byte
        this.MIN = fields['MIN'] as float
        this.STL = fields['STL'] as float
        this.BLK = fields['BLK'] as float
        this.DREB = fields['DREB'] as float
        this.DFGM = fields['DEF_RIM_FGM'] as float
        this.DFGA = fields['DEF_RIM_FGA'] as float
        this.DFG_PCT = fields['DEF_RIM_FG_PCT'] as float
    }
}
