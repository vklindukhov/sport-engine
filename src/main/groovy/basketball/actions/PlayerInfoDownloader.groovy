package basketball.actions

import basketball.entity.Club
import basketball.entity.GameLog
import basketball.entity.PlayerInfo
import groovy.json.JsonSlurper
import utils.OtherUtils

import java.time.LocalDate
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.atomic.AtomicInteger

import static basketball.Constants.*
import static java.time.format.DateTimeFormatter.ofPattern

class PlayerInfoDownloader extends Thread {
    CyclicBarrier waitPoint
    private PlayerInfo playerInfo
    static AtomicInteger COUNTER = new AtomicInteger(0)

    PlayerInfoDownloader(PlayerInfo playerInfo, CyclicBarrier waitPoint) {
        this.playerInfo = playerInfo
        this.waitPoint = waitPoint
        this.start()
    }

    @Override
    void run() {
        0.upto(SEASONS_AMOUNT - 1) {
            def list = getGameslogUrl(playerInfo.id, it)
//            def downloadingStr = '\t\tDownloading game logs for ' + list[0] + '-' + list[1] + ' season ...'
//            println(downloadingStr)
//            outputFile.append('\n' + downloadingStr)
            def gamesLogUrl = list[2]
            def gamesLogJsonData = new JsonSlurper().parseText(new URL(gamesLogUrl).text)
            def resultSets = gamesLogJsonData['resultSets']
            def matchesRows = resultSets['rowSet'] as List
            def logs = getSeasonGamesLogs(matchesRows, resultSets)
            playerInfo.seasonsGamesLogs['gamesLogs_' + list[0] + '-' + list[1]] = logs
//            def downloadedNLogsStr = "\t\t\t ${COUNTER.incrementAndGet()} Downloaded ${logs.size()} game logs for ${playerInfo}"
//            println(downloadedNLogsStr)
//            outputFile.append('\n' + downloadedNLogsStr)
        }
        waitPoint.await()
    }

    private static List getGameslogUrl(id, ind) {
        int currentSeasonEnd = 2017
        int startAt = currentSeasonEnd - ind - 1
        int endAt = currentSeasonEnd - ind
        String urlStartAt = startAt
        String urlEndAt = endAt % 1000

//    String gameLogsUrl = statsUrl + '/player/#!/' + id + '/gamelogs/' + '/?Season=' + urlStartAt + '-' + urlEndAt + '&SeasonType=Regular%20Season'
        String gameLogsUrl = statsUrl + '/stats/playergameLog?DateFrom=&DateTo=&LeagueID=00&PlayerID=' + id +
                '&Season=' + urlStartAt + '-' + urlEndAt + '&SeasonType=Regular+Season'
        //    http://stats.nba.com/stats/playergamelog?DateFrom=&DateTo=&LeagueID=00&PlayerID=203076&Season=2015-16&SeasonType=Regular+Season
        [urlStartAt, urlEndAt, gameLogsUrl]
    }


    private static List getSeasonGamesLogs(List matchesRows, resultSets) {
        if (matchesRows != null && !matchesRows.isEmpty()) {
            if (matchesRows.size() == 1) return matchesRows[0].findAll { (it as List)[5] != null }.collect {
                createGameLog(resultSets['headers'][0] as List, it as List)
            } else {
                System.err.println('!!!! MATCHES_ROWS SIZE GREATER THAN 1 !!!!')
                OtherUtils.playEmergency()
            }
        }
        Collections.EMPTY_LIST
    }

    static GameLog createGameLog(List headers, List row) {
        def gameLogFields = new HashMap()
        gameLogFields[headers[0]] = row[0] as String
        gameLogFields[headers[1]] = row[1] as String
        gameLogFields[headers[2]] = row[2] as String
        gameLogFields[headers[3]] = LocalDate.parse((row[3] as String).toLowerCase().capitalize(), ofPattern('MMM dd, yyyy', Locale.US)) as LocalDate
        gameLogFields[headers[4]] = getClubs(row[4] as String)
        gameLogFields['IS_AT_HOME'] = (row[4] as String).contains(' vs. ')
        gameLogFields[headers[5]] = row[5] as String
        gameLogFields[headers[6]] = row[6] as byte
        gameLogFields[headers[7]] = row[7] as byte
        gameLogFields[headers[8]] = row[8] as byte
        gameLogFields[headers[9]] = row[9] as float
        gameLogFields[headers[10]] = row[10] as byte
        gameLogFields[headers[11]] = row[11] as byte
        gameLogFields[headers[12]] = row[12] as float
        gameLogFields[headers[13]] = row[13] as byte
        gameLogFields[headers[14]] = row[14] as byte
        gameLogFields[headers[15]] = row[15] as float
        gameLogFields[headers[16]] = row[16] as byte
        gameLogFields[headers[17]] = row[17] as byte
        gameLogFields[headers[18]] = row[18] as byte
//    gameLogFields[headers[0][18]] = row[18] as byte = row[16] + row[17] - offensive + defensive rebounds
        gameLogFields[headers[19]] = row[19] as byte
        gameLogFields[headers[20]] = row[20] as byte
        gameLogFields[headers[21]] = row[21] as byte
        gameLogFields[headers[22]] = row[22] as byte
        gameLogFields[headers[23]] = row[23] as byte
        gameLogFields[headers[24]] = row[24] as byte
        gameLogFields[headers[25]] = row[25] as byte
        gameLogFields[headers[26]] = row[26] as byte
        new GameLog(gameLogFields)
    }

    private static Tuple2 getClubs(String clubsCodesTitle) {
        boolean isHomeGame = clubsCodesTitle.contains(' vs. ')
        def teamsCodes = isHomeGame ? clubsCodesTitle.split(' vs. ') : clubsCodesTitle.split(' @ ')
        String thisClubCode = teamsCodes[0] as String
        String rivalClubCode = teamsCodes[1] as String
        Club thisClub = Club.getClubByCode(thisClubCode)
        Club rivalClub = Club.getClubByCode(rivalClubCode)
        new Tuple2(thisClub, rivalClub)
    }

}
