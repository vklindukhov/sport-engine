package basketball.entity

import basketball.Constants
import groovy.transform.EqualsAndHashCode
import jdk.nashorn.internal.ir.annotations.Immutable

import java.time.LocalDate

@Immutable
@EqualsAndHashCode(includes = ['SEASON_ID,Game_ID,Player_ID'])
class GameLog {
    String url
    int SEASON_ID
    int Player_ID
    String Game_ID
    LocalDate GAME_DATE
    Tuple2<Club, Club> MATCHUP
    boolean IS_AT_HOME
    String WL
    byte MIN
    byte FGM
    byte FGA
    float FG_PCT
    byte FG3M
    byte FG3A
    float FG3_PCT
    byte FTM
    byte FTA
    float FT_PCT
    byte OREB
    byte DREB
    byte REB
    byte AST
    byte STL
    byte BLK
    byte TOV
    byte PF
    byte PTS
    byte PLUS_MINUS

    GameLog(Map<String, Object> fields) {
        this.SEASON_ID = fields['SEASON_ID'] as int
        this.Player_ID = fields['Player_ID'] as int
        this.Game_ID = fields['Game_ID'] as String
        this.GAME_DATE = fields['GAME_DATE'] as LocalDate
        this.MATCHUP = fields['MATCHUP'] as Tuple2
        this.IS_AT_HOME = fields['IS_AT_HOME'] as boolean
        this.WL = fields['WL'] as String
        this.MIN = fields['MIN'] as byte
        this.FGM = fields['FGM'] as byte
        this.FGA = fields['FGA'] as byte
        this.FG_PCT = fields['FG_PCT'] as float
        this.FG3M = fields['FG3M'] as byte
        this.FG3A = fields['FG3A'] as byte
        this.FG3_PCT = fields['FG3_PCT'] as float
        this.FTM = fields['FTM'] as byte
        this.FTA = fields['FTA'] as byte
        this.FT_PCT = fields['FT_PCT'] as float
        this.OREB = fields['OREB'] as byte
        this.DREB = fields['DREB'] as byte
        this.REB = fields['REB'] as byte
        this.AST = fields['AST'] as byte
        this.STL = fields['STL'] as byte
        this.BLK = fields['BLK'] as byte
        this.TOV = fields['TOV'] as byte
        this.PF = fields['PF'] as byte
        this.PTS = fields['PTS'] as byte
        this.PLUS_MINUS = fields['PLUS_MINUS'] as byte
        this.url = Constants.statsUrl + '/game/#!/' + Game_ID + '/'
    }


    @Override
    public String toString() {
        return "GameLog{" +
                "url='" + url + '\'' +
                ", Player_ID=" + Player_ID +
                ", Game_ID='" + Game_ID + '\'' +
                ", matchup=" + MATCHUP +
                '}';
    }
}
