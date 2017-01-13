package service.dao

import basketball.entity.DefensiveImpact
@Grab('mysql:mysql-connector-java:5.1.39')
@Grab('commons-dbcp:commons-dbcp:1.4')
@GrabConfig(systemClassLoader = true)
import basketball.entity.GameLog
import basketball.entity.GameSchedule
import basketball.entity.PlayerInfo
import com.mysql.jdbc.Driver
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.apache.commons.dbcp.BasicDataSource

import java.sql.Date
import java.sql.DriverManager
import java.sql.SQLException
import java.time.LocalDate

class Dao {
    def static url = 'jdbc:mysql://localhost:3306/basketball'
    def static user = 'root'
    def static password = 'Qwe123'
    def static driver = 'com.mysql.jdbc.Driver'
    def static dataSource = new BasicDataSource(driverClassName: driver, url: url, username: user, password: password)
    def static isConnectionOk

    public static boolean testConnection() {
        DriverManager.registerDriver(new Driver())
        def sql = new Sql(dataSource)
        sql.query('select * from players') { resultSet ->
            println('CONNECTION IS OK!')
            isConnectionOk = true
        }
        sql.close()
        isConnectionOk
    }

    public static savePlayer(PlayerInfo playerInfo) {
        def sql = new Sql(dataSource)
        def keys = savePlayerInfo(playerInfo, sql)
        playerInfo.seasonsGamesLogs.values().each { it.each { saveGameLog(it as GameLog, sql) } }
        sql.close()
    }

    public static void updatePlayers(Map<Long, PlayerInfo> playerInfos) {
        def sql = new Sql(dataSource)
        def results = sql.rows("SELECT player_id, max(game_date) FROM games_logs GROUP BY player_id") as List<GroovyRowResult>
        def resulSqlStr = 'INSERT INTO games_logs ' +
                '(SEASON_ID,Player_ID,Game_ID,GAME_DATE,PLAYER_TEAM,RIVAL_TEAM,IS_AT_HOME,WL,MIN,FGM,FGA,FG_PCT,FG3M,' +
                'FG3A,FG3_PCT,FTM,FTA,FT_PCT,OREB,DREB,REB,AST,STL,BLK,TOV,PF,PTS,PLUS_MINUS,URL) ' +
                'VALUES'
        def finalInsertString = results.inject('') { result, it ->
            def row = it as GroovyRowResult
            def id = row['player_id'] as long
            return result + getGamesLogsValuesStr(getGamesLogs(playerInfos, id, row))
        } as String
        resulSqlStr += finalInsertString.substring(0, finalInsertString.length() - 1) + ";"
        sql.execute resulSqlStr
    }

    public static int updatePlayer(PlayerInfo playerInfo) {
        def sql = new Sql(dataSource)
        def first = sql.firstRow("SELECT player_id, game_date FROM games_logs " +
                "where player_id=${playerInfo.id} " +
                "order by game_date desc"
        )
        if (first == null || first['player_id'] != null && first['player_id'] != playerInfo.id) {
            savePlayer(playerInfo)
        }
        def sqlDate = first['game_date'] as Date
        def lastDate = sqlDate.toLocalDate()

        def seasonsGamesLogs = playerInfo.seasonsGamesLogs
        int counter = 0
        if (seasonsGamesLogs.size() == 1) {
            def notDatabasesLogs = seasonsGamesLogs.values().first().findAll { if (lastDate < it.GAME_DATE) it }
            notDatabasesLogs.each { saveGameLog(it as GameLog, sql); counter++ }
        }
        sql.close()
        counter
    }

    public static def savePlayers(List<PlayerInfo> playerInfos) {
        def sql = new Sql(dataSource)
        def valuesStr = getPlayersValuesStr(playerInfos)
        String queryStr = "INSERT INTO players (ID,NAME,SURNAME,URL) " +
                "VALUES ${valuesStr.substring(0, valuesStr.length() - 1)} " +
                "ON DUPLICATE KEY UPDATE ID=VALUES(ID),NAME=VALUES(NAME),SURNAME=VALUES(SURNAME),URL=VALUES(URL);"
        try {
            sql.execute queryStr
        } catch (SQLException e) {
            System.err.println("!!!ERROR DURING UPDATIND PLAYERS DATA!!!\n ${e.getMessage()} \n ${e.getStackTrace()}")
        }
    }

    static String getPlayersValuesStr(List<PlayerInfo> playerInfos) {
        playerInfos.inject('') { res, p ->
            def playerInfo = p as PlayerInfo
            def strParams = [
                    playerInfo.id,
                    '\'' + playerInfo?.name?.replace('\'','\'\'') + '\'',
                    '\'' + playerInfo?.surname?.replace('\'','\'\'') + '\'',
                    '\'' + playerInfo?.url?.replace('\'','\'\'') + '\''
            ].collect { it.toString() }.inject('(') { r, e -> r + e + ',' } as String
            return res + strParams.substring(0, strParams.length() - 1) + '),'
        } as String
    }

    public static def saveDefensiveImpacts(List<DefensiveImpact> defensiveImpacts) {
        def sql = new Sql(dataSource)

        def insertStr = 'INSERT INTO defensive_impacts ' +
                '(SEASON_ID,PLAYER_ID,TEAM,GP,W,L,MIN,STL,BLK,DREB,DFGM,DFGA,DFG_PCT) ' +
                'VALUES'
        def valuesStr = getDefensiveImpactsValuesStr(defensiveImpacts)
        def insertCriteriaStr = 'ON DUPLICATE KEY UPDATE ' +
                'SEASON_ID=VALUES(SEASON_ID),PLAYER_ID=VALUES(PLAYER_ID),TEAM=VALUES(TEAM),GP=VALUES(GP),W=VALUES(W),' +
                'L=VALUES(L),MIN=VALUES(MIN),STL=VALUES(STL),BLK=VALUES(BLK),DREB=VALUES(DREB),DFGM=VALUES(DFGM),' +
                'DFGA=VALUES(DFGA),DFG_PCT=VALUES(DFG_PCT);'

        String queryStr1 = insertStr + valuesStr.substring(0, valuesStr.length() - 1) + insertCriteriaStr

        String queryStr2 = "INSERT INTO defensive_impacts " +
                "(SEASON_ID,PLAYER_ID,TEAM,GP,W,L,MIN,STL,BLK,DREB,DFGM,DFGA,DFG_PCT) " +
                "VALUES " +
                "${valuesStr.substring(0, valuesStr.length() - 1)} " +
                "ON DUPLICATE KEY UPDATE " +
                "SEASON_ID=VALUES(SEASON_ID),PLAYER_ID=VALUES(PLAYER_ID),TEAM=VALUES(TEAM),GP=VALUES(GP),W=VALUES(W)," +
                "L=VALUES(L),MIN=VALUES(MIN),STL=VALUES(STL),BLK=VALUES(BLK),DREB=VALUES(DREB),DFGM=VALUES(DFGM)," +
                "DFGA=VALUES(DFGA),DFG_PCT=VALUES(DFG_PCT);"

        String queryStr3 = "INSERT INTO defensive_impacts (SEASON_ID,PLAYER_ID,TEAM,GP,W,L,MIN,STL,BLK,DREB,DFGM,DFGA,DFG_PCT) " +
                "VALUES ${valuesStr.substring(0, valuesStr.length() - 1)} ON DUPLICATE KEY UPDATE SEASON_ID=VALUES(SEASON_ID)," +
                "PLAYER_ID=VALUES(PLAYER_ID),TEAM=VALUES(TEAM),GP=VALUES(GP),W=VALUES(W),L=VALUES(L),MIN=VALUES(MIN)," +
                "STL=VALUES(STL),BLK=VALUES(BLK),DREB=VALUES(DREB),DFGM=VALUES(DFGM),DFGA=VALUES(DFGA),DFG_PCT=VALUES(DFG_PCT);"

        try {
            sql.execute queryStr2
        } catch (SQLException e) {
            System.err.println("!!!ERROR DURING UPDATIND PLAYERS DATA!!!\n ${e.getMessage()} \n ${e.getStackTrace()}")
        }
    }

    public static int saveGamesLogs(Map<Long, PlayerInfo> playerInfos, isUpdate) {
        def sql = new Sql(dataSource)

        def insertStr = 'INSERT INTO games_logs ' +
                '(SEASON_ID,Player_ID,Game_ID,GAME_DATE,PLAYER_TEAM,RIVAL_TEAM,IS_AT_HOME,WL,MIN,FGM,FGA,FG_PCT,FG3M,' +
                'FG3A,FG3_PCT,FTM,FTA,FT_PCT,OREB,DREB,REB,AST,STL,BLK,TOV,PF,PTS,PLUS_MINUS,URL) ' +
                'VALUES'
        def valuesStr
        int counter = 0
        if (isUpdate) {
            def results = sql.rows("SELECT player_id, max(game_date) FROM games_logs GROUP BY player_id")
            valuesStr = results.inject('') { res, it ->
                def row = it as GroovyRowResult
                def playerId = row['player_id'] as long
                LocalDate lastGameDate = (row['max(game_date)'] as Date).toLocalDate()
                def playerInfo = playerInfos[playerId] as PlayerInfo
                if (playerInfo == null) res
                else {
                    def newGamesLogs = playerInfo.seasonsGamesLogs.values().first().findAll {
                        def gameLog = it as GameLog
                        lastGameDate < gameLog.GAME_DATE
                    }
                    counter += newGamesLogs.size()
                    res + getGamesLogsValuesStr(newGamesLogs)
                }
            }
        } else {
            valuesStr = playerInfos.values().inject('') { result, playerInfo ->
                return result + playerInfo.seasonsGamesLogs.collect().inject('') { res, entry ->
                    def gameLogs = (entry as Map.Entry<String, List<GameLog>>).value
                    counter += gameLogs.size()
                    return res + getGamesLogsValuesStr(gameLogs)
                }
            } as String
        }
        if (counter > 0) {
            def insertCriteriaStr = 'ON DUPLICATE KEY UPDATE ' +
                    '  SEASON_ID  = VALUES(SEASON_ID), Player_ID = VALUES(Player_ID), Game_ID = VALUES(Game_ID),' +
                    '  GAME_DATE  = VALUES(GAME_DATE), PLAYER_TEAM = VALUES(PLAYER_TEAM), RIVAL_TEAM = VALUES(RIVAL_TEAM),' +
                    '  IS_AT_HOME = VALUES(IS_AT_HOME), WL = VALUES(WL), MIN = VALUES(MIN), FGM = VALUES(FGM), FGA = VALUES(FGA),' +
                    '  FG_PCT     = VALUES(FG_PCT), FG3M = VALUES(FG3M), FG3A = VALUES(FG3A), FG3_PCT = VALUES(FG3_PCT),' +
                    '  FTM        = VALUES(FTM), FTA = VALUES(FTA), FT_PCT = VALUES(FT_PCT), OREB = VALUES(OREB),' +
                    '  DREB       = VALUES(DREB), REB = VALUES(REB), AST = VALUES(AST), STL = VALUES(STL), BLK = VALUES(BLK),' +
                    '  TOV        = VALUES(TOV), PF = VALUES(PF), PTS = VALUES(PTS), PLUS_MINUS = VALUES(PLUS_MINUS), URL = VALUES(URL);'

            String queryStr1 = insertStr + valuesStr.substring(0, valuesStr.length() - 1) + insertCriteriaStr

            String queryStr2 = "INSERT INTO games_logs " +
                    "(SEASON_ID,Player_ID,Game_ID,GAME_DATE,PLAYER_TEAM,RIVAL_TEAM,IS_AT_HOME,WL,MIN,FGM,FGA,FG_PCT,FG3M," +
                    "FG3A,FG3_PCT,FTM,FTA,FT_PCT,OREB,DREB,REB,AST,STL,BLK,TOV,PF,PTS,PLUS_MINUS,URL) " +
                    "VALUES " +
                    "${valuesStr.substring(0, valuesStr.length() - 1)} " +
                    "ON DUPLICATE KEY UPDATE " +
                    "SEASON_ID=VALUES(SEASON_ID),Player_ID=VALUES(Player_ID),Game_ID=VALUES(Game_ID),GAME_DATE=VALUES(GAME_DATE)," +
                    "PLAYER_TEAM=VALUES(PLAYER_TEAM),RIVAL_TEAM=VALUES(RIVAL_TEAM),IS_AT_HOME=VALUES(IS_AT_HOME),WL=VALUES(WL)," +
                    "MIN=VALUES(MIN),FGM=VALUES(FGM),FGA=VALUES(FGA),FG_PCT=VALUES(FG_PCT),FG3M=VALUES(FG3M),FG3A=VALUES(FG3A)," +
                    "FG3_PCT=VALUES(FG3_PCT),FTM=VALUES(FTM),FTA=VALUES(FTA),FT_PCT=VALUES(FT_PCT),OREB=VALUES(OREB)," +
                    "DREB=VALUES(DREB),REB=VALUES(REB),AST=VALUES(AST),STL=VALUES(STL),BLK=VALUES(BLK),TOV=VALUES(TOV)," +
                    "PF=VALUES(PF),PTS=VALUES(PTS),PLUS_MINUS=VALUES(PLUS_MINUS),URL=VALUES(URL);"

            String queryStr3 = "INSERT INTO games_logs (SEASON_ID,Player_ID,Game_ID,GAME_DATE,PLAYER_TEAM,RIVAL_TEAM," +
                    "IS_AT_HOME,WL,MIN,FGM,FGA,FG_PCT,FG3M,FG3A,FG3_PCT,FTM,FTA,FT_PCT,OREB,DREB,REB,AST,STL,BLK,TOV,PF,PTS," +
                    "PLUS_MINUS,URL) VALUES ${valuesStr.substring(0, valuesStr.length() - 1)} ON DUPLICATE KEY UPDATE " +
                    "SEASON_ID=VALUES(SEASON_ID),Player_ID=VALUES(Player_ID),Game_ID=VALUES(Game_ID),GAME_DATE=VALUES(GAME_DATE)," +
                    "PLAYER_TEAM=VALUES(PLAYER_TEAM),RIVAL_TEAM=VALUES(RIVAL_TEAM),IS_AT_HOME=VALUES(IS_AT_HOME),WL=VALUES(WL)," +
                    "MIN=VALUES(MIN),FGM=VALUES(FGM),FGA=VALUES(FGA),FG_PCT=VALUES(FG_PCT),FG3M=VALUES(FG3M),FG3A=VALUES(FG3A)," +
                    "FG3_PCT=VALUES(FG3_PCT),FTM=VALUES(FTM),FTA=VALUES(FTA),FT_PCT=VALUES(FT_PCT),OREB=VALUES(OREB)," +
                    "DREB=VALUES(DREB),REB=VALUES(REB),AST=VALUES(AST),STL=VALUES(STL),BLK=VALUES(BLK),TOV=VALUES(TOV)," +
                    "PF=VALUES(PF),PTS=VALUES(PTS),PLUS_MINUS=VALUES(PLUS_MINUS),URL=VALUES(URL);"
//        FROM THE DATABASE DELETED ERSAN ILYASOVA'S FIRST 3 GAME LOGS IN SEASON 2016-17 DUE TO THEY WAS FOR OKC
//        BUT HE IS CURRENTLY IN PHI
            try {
//                println(queryStr2)
                sql.execute queryStr2
                sql.execute "DELETE FROM games_logs WHERE season_id = 22016 AND player_team = \'OKC\' AND player_id = 101141;"
            } catch (SQLException e) {
                System.err.println("!!!ERROR DURING UPDATIND GAMES LOGS DATA!!!\n ${e.getMessage()} \n ${e.getStackTrace()}")
            }
        }
        counter
    }

    private static List savePlayerInfo(PlayerInfo playerInfo, Sql sql) {
        def insertSql = 'INSERT INTO players (id,url,name,surname) VALUES (?,?,?,?)'
        def params = [
                playerInfo.id,
                playerInfo.url,
                playerInfo.name,
                playerInfo.surname
        ]
        def keys
        try {
            keys = sql.executeInsert insertSql, params
        } catch (SQLException e) {
            def str = 'Player ' + playerInfo + ' is not saved!!!' + '\n' + e.getMessage() + '\n' + e.getStackTrace()
            System.err.println(str)
        }
        keys
    }

    private static List saveGameLog(GameLog gameLog, Sql sql) {
        def insertSql = 'INSERT INTO games_logs' +
                '(SEASON_ID,Player_ID,Game_ID,GAME_DATE,PLAYER_TEAM,RIVAL_TEAM,IS_AT_HOME,WL,MIN,FGM,FGA,FG_PCT,FG3M,FG3A,FG3_PCT,' +
                'FTM,FTA,FT_PCT,OREB,DREB,REB,AST,STL,BLK,TOV,PF,PTS,PLUS_MINUS,URL)' +
                'VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)'
        def keys
        try {
            def params = [
                    gameLog.SEASON_ID, gameLog.Player_ID, gameLog.Game_ID, gameLog.GAME_DATE,
                    gameLog.MATCHUP.first.name(), gameLog.MATCHUP.second.name(), gameLog.WL, gameLog.MIN, gameLog.FGM,
                    gameLog.FGA, gameLog.FG_PCT, gameLog.FG3M, gameLog.FG3A, gameLog.FG3_PCT, gameLog.FTM, gameLog.FTA,
                    gameLog.FT_PCT, gameLog.OREB, gameLog.DREB, gameLog.REB, gameLog.AST, gameLog.STL, gameLog.BLK,
                    gameLog.TOV, gameLog.PF, gameLog.PTS, gameLog.PLUS_MINUS, gameLog.url
            ]
            try {
                keys = sql.executeInsert insertSql, params
            } catch (SQLException e) {
                def str = 'Game log ' + gameLog + ' is not saved!!!' + '\n' + e.getMessage() + '\n' + e.getStackTrace()
                System.err.println(str)
                throw e
            }
        } catch (Throwable e) {
            def str = 'Error during saving ' + gameLog + ' is not saved!!!' + '\n' + e.getMessage() + '\n' + e.getStackTrace()
            System.err.println(str)
        }

        keys
    }

    private static List<GameLog> getGamesLogs(Map<Long, PlayerInfo> playerInfos, long id, row) {
        List<GameLog> newGamesLogs = playerInfos[id]?.seasonsGamesLogs?.values()?.first()?.findAll {
            def gameLog = it as GameLog
            def lastDate = (row['max(game_date)'] as Date).toLocalDate()
            lastDate < gameLog.GAME_DATE
        } as List<GameLog>
        newGamesLogs == null ? Collections.EMPTY_LIST : newGamesLogs
    }

    private static String getGamesLogsValuesStr(List<GameLog> newGamesLogs) {
        newGamesLogs.inject('') { res, gL ->
            def gameLog = gL as GameLog
            def strParams = [
                    gameLog.SEASON_ID, gameLog.Player_ID, '\'' + gameLog.Game_ID + '\'', '\'' + gameLog.GAME_DATE + '\'',
                    '\'' + gameLog.MATCHUP.first.name() + '\'', '\'' + gameLog.MATCHUP.second.name() + '\'',
                    gameLog.IS_AT_HOME, '\'' + gameLog.WL + '\'', gameLog.MIN, gameLog.FGM, gameLog.FGA, gameLog.FG_PCT,
                    gameLog.FG3M, gameLog.FG3A, gameLog.FG3_PCT, gameLog.FTM, gameLog.FTA, gameLog.FT_PCT, gameLog.OREB,
                    gameLog.DREB, gameLog.REB, gameLog.AST, gameLog.STL, gameLog.BLK, gameLog.TOV, gameLog.PF, gameLog.PTS,
                    gameLog.PLUS_MINUS, '\'' + gameLog.url + '\''
            ].collect { it.toString() }.inject('(') { r, e -> r + e + ',' } as String
            return res + strParams.substring(0, strParams.length() - 1) + '),'
        } as String
    }

    private static String getDefensiveImpactsValuesStr(List<DefensiveImpact> defensiveImpacts) {
        defensiveImpacts.inject('') { res, gL ->
            def impact = gL as DefensiveImpact
            def strParams = [
                    impact.SEASON_ID, impact.PLAYER_ID, '\'' + impact.club.name() + '\'',
                    impact.GP, impact.W, impact.L, impact.MIN, impact.STL, impact.BLK, impact.DREB,
                    impact.DFGM, impact.DFGA, impact.DFG_PCT
            ].collect { it.toString() }.inject('(') { r, e -> r + e + ',' } as String
            return res + strParams.substring(0, strParams.length() - 1) + '),'
        } as String
    }

    static def saveGamesSchedules(List<GameSchedule> gameSchedules) {
        def sql = new Sql(dataSource)
        def valuesStr = gameSchedules.inject('') { result, gameSchedule ->
            def strParams = [
                    gameSchedule.seasonId, '\'' + gameSchedule.gameId + '\'', '\'' + gameSchedule.gameDate + '\'',
                    '\'' + gameSchedule.matchup.first.name() + '\'', '\'' + gameSchedule.matchup.second.name() + '\''
            ].collect { it.toString() }.inject('(') { r, e -> r + e + ',' } as String
            return result + strParams.substring(0, strParams.length() - 1) + '),'
        } as String
        String queryStr = "INSERT INTO games_schedules " +
                "(SEASON_ID,GAME_ID,GAME_DATE,HOME_TEAM,AWAY_TEAM) VALUES ${valuesStr.substring(0, valuesStr.length() - 1)} " +
                "ON DUPLICATE KEY UPDATE SEASON_ID=VALUES(SEASON_ID),GAME_ID=VALUES(GAME_ID),GAME_DATE=VALUES(GAME_DATE)," +
                "HOME_TEAM=VALUES(HOME_TEAM),AWAY_TEAM=VALUES(AWAY_TEAM);"

        try {
            sql.execute queryStr
        } catch (SQLException e) {
            System.err.println("!!!ERROR DURING UPDATIND GAMES SCHEDULES DATA!!!\n ${e.getMessage()} \n ${e.getStackTrace()}")
        }
    }
}

//DELETED FIRST ERSAN ILYASOVA'S 3 GAME LOGS IN SEASON 2016-17 DUE TO THEY WAS FOR OKC BUT HE IS CURRENTLY IN PHI