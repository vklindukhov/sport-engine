package basketball

import basketball.actions.PlayerInfoDownloader
import basketball.entity.*
import groovy.json.JsonSlurper
import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.NodeChild
import service.dao.Dao
import utils.OtherUtils
import utils.XmlUtils

import java.time.LocalDate
import java.util.concurrent.CyclicBarrier

import static basketball.Constants.*
import static java.time.format.DateTimeFormatter.ofPattern
import static utils.OtherUtils.roundWithPrecision
import static utils.XmlUtils.findTagWithAttributeValue

class Constants {
    static String nbaUrl = 'http://www.nba.com'
    static String playerTail = '/players'
    static String statsUrl = 'http://stats.nba.com'
    static String gameLogsTail = '/player/#!/203076/gamelogs/'
    static String defensiveImpactUrlPrefix = 'http://stats.nba.com/stats/leaguedashptstats?College=&Conference=&Country=' +
            '&DateFrom=&DateTo=&Division=&DraftPick=&DraftYear=&GameScope=&Height=&LastNGames=0&LeagueID=00&Location=' +
            '&Month=0&OpponentTeamID=0&Outcome=&PORound=0&PerMode=PerGame&PlayerExperience=&PlayerOrTeam=Player' +
            '&PlayerPosition=&PtMeasureType=Defense&Season='
    static String defensiveImpactUrlSuffix = '&SeasonSegment=&SeasonType=Regular+Season&StarterBench=&TeamID=0&VsConference=&VsDivision=&Weight='
    static Map PLAYERS = new HashMap<Long, PlayerInfo>()
    static int NBA_PLAYERTS_AMOUNT = 450
    static int TAKE_AMOUNT = NBA_PLAYERTS_AMOUNT
    static String outputFileName = '/downloadNbaInfoDebugLog_' + LocalDate.now().format(ofPattern('dd-MM-yyyy')) + '.txt'
    static String errFileName = '/downloadNbaInfoErrorLog_' + LocalDate.now().format(ofPattern('dd-MM-yyyy')) + '.txt'
    static File outputFile = OtherUtils.getFile(outputFileName)
    static File errFile = OtherUtils.getFile(errFileName)
    static PrintStream outStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(outputFileName)))
    static PrintStream errStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(errFileName)))
    static byte FIRST_SEASON_START = 16
    static byte SEASONS_AMOUNT = 1
    static String schedulePrefixUrl = 'http://data.nba.com/data/10s/v2015/json/mobile_teams/nba/'
    static String scheduleSuffixUrl = '/league/00_full_schedule.json'
    static boolean IS_UPDATE = true
}

//System.setOut(outStream)
//System.setErr(errStream)

if(!IS_UPDATE) Dao.saveGamesSchedules(downloadGamesSchedules())
runActions()

static List downloadGamesSchedules() {
    List<GameSchedule> allGames = new ArrayList<>()
//    FIRST_SEASON_START.downto(FIRST_SEASON_START - SEASONS_AMOUNT + 1) {
        def season = 16
        def url = schedulePrefixUrl + 20 + season + scheduleSuffixUrl
        def seasonScheduleJsonData = new JsonSlurper().parseText(new URL(url).text)
        def lscd = seasonScheduleJsonData['lscd'] as List
        allGames.addAll(lscd.inject(new ArrayList<>()) { res, monthData ->
            res.addAll(monthData['mscd']['g'].collect {
                String gameId = it['gid'] as String
                LocalDate gameDate = LocalDate.parse(
                        (it['gdte'] as String).toLowerCase().capitalize(),
                        ofPattern('yyyy-MM-dd', Locale.US)
                ) as LocalDate
                Tuple2 matchup = new Tuple2<>(
                        Club.getClubByCode(it['h']['ta'] as String),
                        Club.getClubByCode(it['v']['ta'] as String)
                )
                new GameSchedule(seasonId: 22000 + season, gameId: gameId, gameDate: gameDate, matchup: matchup)
            })
            res
        } as List<GameSchedule>)
//    }
    def seasonStartDate = LocalDate.of(2016, 10, 25)
    def seasonEndDate = LocalDate.of(2017, 4, 12)
    allGames.findAll {
        seasonStartDate <= it.gameDate && it.gameDate <= seasonEndDate &&
                it.matchup.first != null && it.matchup.second != null
    }
}

private void runActions() {
    def overAllStartTime = System.nanoTime()
    def startSTr = "!!!STARTED!!! ..."
    println(startSTr)
    outputFile.append('\n' + startSTr)


    //  DOWNLOAD ALL PLAYERS IDS----------------------------------------------------------------------------------------
    def getPlayersStartTime = System.nanoTime()
    def getAllIdsStr = "\nGETTING ALL PLAYERS IDS ..."
    println(getAllIdsStr)
    outputFile.append('\n' + getAllIdsStr)
    getPlayers(nbaUrl + playerTail)
    def gotAllIdsStr = "ALL PLAYERS IDS ARE GOT"
    println(gotAllIdsStr)
    outputFile.append('\n' + gotAllIdsStr)
    def getPlayersEndTime = System.nanoTime()
    //  DOWNLOAD ALL PLAYERS IDS----------------------------------------------------------------------------------------
    //  DOWNLOAD ALL PLAYERS TIME REPORT--------------------------------------------------------------------------------
    def downloadPlayersTimeStr = "\nTo get all players - spent " +
            "${roundWithPrecision((getPlayersEndTime - getPlayersStartTime) / 1_000_000_000, 2)} sec"
    println(downloadPlayersTimeStr)
    outputFile.append('\n' + downloadPlayersTimeStr)
    //  DOWNLOAD ALL PLAYERS TIME REPORT--------------------------------------------------------------------------------

    //  DOWNLOAD DEFENSIVE IMPACTS -------------------------------------------------------------------------------------
    def getDefensiveImpactsStartTime = System.nanoTime()
    def getDefensiveImpactsStr = "\nGETTING DEFENSIVE IMPACTS ..."
    println(getDefensiveImpactsStr)
    outputFile.append('\n' + getDefensiveImpactsStr)
    List<DefensiveImpact> defensiveImpacts = getDefensiveImpactsForSeasons()
    def gotDefensiveImpactsStr = "DEFENSIVE IMPACTS ARE GOT"
    println(gotDefensiveImpactsStr)
    outputFile.append('\n' + gotDefensiveImpactsStr)
    def getDefensiveImpactsEndTime = System.nanoTime()
    //  DOWNLOAD DEFENSIVE IMPACTS -------------------------------------------------------------------------------------
    //  DOWNLOAD ALL DEFENSIVE IMPACT TIME REPORT-----------------------------------------------------------------------
    def downloadAllDIsTimeStr = "To download defensive impacts - spent " +
            "${roundWithPrecision((getDefensiveImpactsEndTime - getDefensiveImpactsStartTime) / 1_000_000_000, 2)} sec"
    println(downloadAllDIsTimeStr)
    outputFile.append('\n' + downloadAllDIsTimeStr)
    //  DOWNLOAD ALL DEFENSIVE IMPACT TIME REPORT-----------------------------------------------------------------------

    //  DOWNLOAD GAMES LOGS---------------------------------------------------------------------------------------------
    double counter = 1
    def downloadTimeStart = System.nanoTime()
    if (TAKE_AMOUNT == NBA_PLAYERTS_AMOUNT) TAKE_AMOUNT = PLAYERS.size()
    def cyclicBarrier = new CyclicBarrier(TAKE_AMOUNT, new Runnable() {
        @Override
        void run() {
            //  SAVE/UPDATE DATA------------------------------------------------------------------------------------------------
            def saveTimeStart = System.nanoTime()
            def sDStr = "\nUPDATING DATA ..."
            println(sDStr)
            outputFile.append('\n' + sDStr)
            try {
                if(!IS_UPDATE) Dao.savePlayers(PLAYERS.take(TAKE_AMOUNT).values() as List)
                Dao.saveDefensiveImpacts(defensiveImpacts)
                int addedNewLogsAmount = Dao.saveGamesLogs(PLAYERS, IS_UPDATE)
                if (addedNewLogsAmount > 0) {
                    def dSStr = "ADDED NEW ${addedNewLogsAmount} GAMES LOGS"
                    println(dSStr)
                    outputFile.append('\n' + dSStr)
                }
                def dSStr = "DATA HAS BEEN UPDATED"
                println(dSStr)
                outputFile.append('\n' + dSStr)
            } catch (Exception e) {
                def dSStr = "DATA HAS NOT BEEN UPDATED \n ${e.getMessage()} \n ${e.getStackTrace()}"
                println(dSStr)
                outputFile.append('\n' + dSStr)
            }
            def saveTimeEnd = System.nanoTime()
            //  SAVE/UPDATE DATA------------------------------------------------------------------------------------------------
            //  UPDATE DATA TIME REPORT-----------------------------------------------------------------------------------------
            def updateTimeStr = "To update data - spent ${roundWithPrecision((saveTimeEnd - saveTimeStart) / 1_000_000_000, 2)} sec"
            println(updateTimeStr)
            outputFile.append('\n' + updateTimeStr)
            //  UPDATE DATA TIME REPORT-----------------------------------------------------------------------------------------
        }
    })
    PLAYERS.take(TAKE_AMOUNT).each {
        new PlayerInfoDownloader(it.value, cyclicBarrier)
//        def strStart = "\n№ ${counter as int}.\nSTART FOR ${it.value} ..."
//        println(strStart)
//        outputFile.append('\n' + strStart)
//
//        def dStr = "\tDownloading logs ..."
//        println(dStr)
//        outputFile.append('\n' + strStart)
//        fillLogs(it.value, IS_UPDATE ? 1 : SEASONS_AMOUNT)
//        def dedStr = "\tLogs are downloaded"
//        println(dedStr)
//        outputFile.append('\n' + dedStr)
//        def endStr = "END FOR ${it.value}"
//        outputFile.append('\n' + endStr)
//        println(endStr)
//
//        def overDoneStr = "Overall done ${roundWithPrecision((counter++) / NBA_PLAYERTS_AMOUNT * 100, 2)}%"
//        println(overDoneStr)
//        outputFile.append('\n' + overDoneStr)
    }
    def downloadTimeEnd = System.nanoTime()
    //  DOWNLOAD GAMES LOGS---------------------------------------------------------------------------------------------

//    def overAllEndTime = System.nanoTime()
//
//    //  DOWNLOAD ALL PLAYERS TIME REPORT--------------------------------------------------------------------------------
//    def downloadPlayersTimeStr = "\nTo get all players - spent " +
//            "${roundWithPrecision((getPlayersEndTime - getPlayersStartTime) / 1_000_000_000, 2)} sec"
//    println(downloadPlayersTimeStr)
//    outputFile.append('\n' + downloadPlayersTimeStr)
//    //  DOWNLOAD ALL PLAYERS TIME REPORT--------------------------------------------------------------------------------
//
//    //  DOWNLOAD ALL DEFENSIVE IMPACT TIME REPORT-----------------------------------------------------------------------
//    def downloadAllDIsTimeStr = "To download defensive impacts - spent " +
//            "${roundWithPrecision((getDefensiveImpactsEndTime - getDefensiveImpactsStartTime) / 1_000_000_000, 2)} sec"
//    println(downloadAllDIsTimeStr)
//    outputFile.append('\n' + downloadAllDIsTimeStr)
//    //  DOWNLOAD ALL DEFENSIVE IMPACT TIME REPORT-----------------------------------------------------------------------
//
//    //  DOWNLOAD ALL GAMES LOGS TIME REPORT-----------------------------------------------------------------------------
//    def downloadAllGamesLogsTimeStr = "To download all games logs - spent " +
//            "${roundWithPrecision((downloadTimeEnd - downloadTimeStart) / 1_000_000_000, 2)} sec"
//    println(downloadAllGamesLogsTimeStr)
//    outputFile.append('\n' + downloadAllGamesLogsTimeStr)
//    //  DOWNLOAD ALL GAMES LOGS TIME REPORT-----------------------------------------------------------------------------
//
//    //  UPDATE DATA TIME REPORT-----------------------------------------------------------------------------------------
//    def updateTimeStr = "To update data - spent ${roundWithPrecision((saveTimeEnd - saveTimeStart) / 1_000_000_000, 2)} sec"
//    println(updateTimeStr)
//    outputFile.append('\n' + updateTimeStr)
//    //  UPDATE DATA TIME REPORT-----------------------------------------------------------------------------------------
//
//    //  OVERALL TIME REPORT---------------------------------------------------------------------------------------------
//    def overallTimeStr = "Overall - spent ${roundWithPrecision((overAllEndTime - overAllStartTime) / 1_000_000_000, 2)} sec"
//    println(overallTimeStr)
//    outputFile.append('\n' + overallTimeStr)
//    //  OVERALL TIME REPORT---------------------------------------------------------------------------------------------
//
////    def sum = PLAYERS.take(TAKE_AMOUNT).inject(0) { r, it ->
////        r + it.value.seasonsGamesLogs.inject(0) { res, entry ->
////            res + entry.value.size()
////        }
////    }
////    def overallGamesStr = "\nOverall got ${sum} game logs:"
////    println(overallGamesStr)
////    outputFile.append('\n' + overallGamesStr)
////
////    FIRST_SEASON_START.downto(FIRST_SEASON_START - SEASONS_AMOUNT + 1) {
////        def itSum = PLAYERS.take(TAKE_AMOUNT).inject(0) {
////            res, p ->
////                def gameLogs = p.value.seasonsGamesLogs["gamesLogs_20${it}-${it + 1}"]
////                res + (gameLogs == null ? 0 : gameLogs.size())
////        }
////        def logsForStr = "20${it}-${it + 1}: ${itSum} game logs"
////        println(logsForStr)
////        outputFile.append('\n' + logsForStr)
////    }
//
    def doneSTr = "!!!DONE!!!"
    println(doneSTr)
    outputFile.append('\n' + doneSTr)
}


static def fillLogs(PlayerInfo playerInfo, int seasonsAmount) {
    0.upto(seasonsAmount - 1) {
        def list = getGameslogUrl(playerInfo.id, it)
        def downloadingStr = '\t\tDownloading game logs for ' + list[0] + '-' + list[1] + ' season ...'
        println(downloadingStr)
        outputFile.append('\n' + downloadingStr)
        def gamesLogUrl = list[2]
//        HTTPBuilder http = new HTTPBuilder(gamesLogUrl)
//        def html = http.get([:])
//        http.shutdown()
//        same to the new JsonSlurper().parseText(new URL(gamesLogUrl).text)
        def gamesLogJsonData = new JsonSlurper().parseText(new URL(gamesLogUrl).text)
        def resultSets = gamesLogJsonData['resultSets']
        def matchesRows = resultSets['rowSet'] as List
        def logs = getSeasonGamesLogs(matchesRows, resultSets)
        playerInfo.seasonsGamesLogs['gamesLogs_' + list[0] + '-' + list[1]] = logs
        def downloadedNLogsStr = "\t\t\tDownloaded ${logs.size()} game logs"
        println(downloadedNLogsStr)
        outputFile.append('\n' + downloadedNLogsStr)
        def downloadedStr = '\t\tLogs for ' + list[0] + '-' + list[1] + ' season are downloaded'
        println(downloadedStr)
        outputFile.append('\n' + downloadedStr)
    }
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

private static List getGameslogUrl(id, ind) {
    int currentSeasonEnd = 2000 + FIRST_SEASON_START + 1
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

def getPlayers(url) {
    def html = XmlUtils.getHtmlByPath(url, null)

    GPathResult playersTable = findTagWithAttributeValue(html, 'div', 'class', 'small-12 columns')

    Iterator iterator = playersTable.children().iterator()
    while (iterator.hasNext()) {
        NodeChild child = iterator.next() as NodeChild
        if (XmlUtils.isTagWithAttributeValue(child, 'a', 'class', 'row playerList')) {
            String title = child.attributes()['title'] as String
            try {
                def playerUrlTail = child.attributes()['href'] as String
                def split = playerUrlTail.split('/')
                String name
                String surname
                long id
                if (split.size() == 5) {
                    name = (split[2] as String).capitalize() as String
                    surname = (split[3] as String).capitalize() as String
                    id = split[4] as long
                } else if (split.size() == 4) {
                    surname = (split[2] as String).capitalize() as String
                    id = split[3] as long
                }
                String playerUrl = nbaUrl + playerUrlTail as String
                PlayerInfo player = new PlayerInfo(id: id, url: playerUrl, title: title, name: name, surname: surname)
                if (PLAYERS[id] == null) PLAYERS[id] = player
            } catch (e) {
                println(title + ' - ' + e.getMessage())
            }
        }
    }
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

static DefensiveImpact createdDefensiveImpact(int seasonYear, List headers, List row) {
    def defensiveImpactFields = new HashMap()
    defensiveImpactFields['SEASON_ID'] = '220' + seasonYear
    defensiveImpactFields[headers[0]] = row[0] as int
    defensiveImpactFields[headers[3]] = row[3] as String
    defensiveImpactFields[headers[4]] = row[4] as byte
    defensiveImpactFields[headers[5]] = row[5] as byte
    defensiveImpactFields[headers[6]] = row[6] as byte
    defensiveImpactFields[headers[7]] = row[7] as float
    defensiveImpactFields[headers[8]] = row[8] as float
    defensiveImpactFields[headers[9]] = row[9] as float
    defensiveImpactFields[headers[10]] = row[10] as float
    defensiveImpactFields[headers[11]] = row[11] as float
    defensiveImpactFields[headers[12]] = row[12] as float
    defensiveImpactFields[headers[13]] = row[13] as float
    new DefensiveImpact(defensiveImpactFields)
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

static List<DefensiveImpact> getDefensiveImpactsForSeasons() {
    def list = new ArrayList<>()
    FIRST_SEASON_START.downto(FIRST_SEASON_START - SEASONS_AMOUNT + 1) {
        int seasonStartYear = it
        def seasonUrl = "20${it}-${it + 1}"
        def url = defensiveImpactUrlPrefix + seasonUrl + defensiveImpactUrlSuffix
        def defensiveImpactsJsonData = new JsonSlurper().parseText(new URL(url).text)

        def resultSets = defensiveImpactsJsonData['resultSets'][0]
        def headers = resultSets['headers'] as List
        def rowSet = resultSets['rowSet'] as List
        def defensiveImpactsForSeason = rowSet.collect { createdDefensiveImpact(seasonStartYear, headers, it as List) }
        list.addAll(defensiveImpactsForSeason)
    }
    list
}