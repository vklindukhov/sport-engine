package football2

import basketball.entity.LiveInfoLog
import football2.actions.MatchParser
import football2.entity.ForkInfoLog
import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.NodeChild
import utils.OtherUtils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CyclicBarrier

import static basketball.entity.SportType.BASKETBALL
import static basketball.entity.SportType.FOOTBALL
import static java.time.format.DateTimeFormatter.ofPattern
import static utils.OtherUtils.playSound
import static utils.XmlUtils.findTagWithAttributeValue
import static utils.XmlUtils.getHtmlByPath

class MarathonForkScanner extends TimerTask {
    int counter = 0
    def marathonUrl = 'https://www.marathonbet.com'
    def marathonFootballUrl = marathonUrl + '/en/popular/Football/?menu=11'
    def TODAYS_MATCHES = new ConcurrentHashMap<>()
    def TODAY_MATCHED_MATCHES_WITH_GOAL_AT_END_OF_MATCH = new ConcurrentHashMap()
    def TODAY_SCANNED_MATCHES_WITH_GOAL_AT_END_OF_MATCH = new ConcurrentHashMap<>()
    def TODAYS_MATCHES_TO_SCAN = new ConcurrentHashMap<>()
    def borderShiftCoefficient = 0.1
    def static OUTPUT_FILENAME = '/forkLog_' + LocalDate.now().format(ofPattern('dd-MM-yyyy')) + '.txt'
    static final def OUTPUT_FILE = OtherUtils.getFile(OUTPUT_FILENAME)
    static int KICK_OFF_MAX_DAY_SHIFT = 7
    def SINCE_MINUTE_TO_ODD = [
            60: 2.00,
            61: 2.03,
            62: 2.06,
            63: 2.09,
            64: 2.12,
            65: 2.15,
            66: 2.18,
            67: 2.21,
            68: 2.24,
            69: 2.29,
            70: 2.34,
            71: 2.39,
            72: 2.44,
            73: 2.49,
            74: 2.54,
            75: 2.59,
            76: 2.64,
            77: 2.69,
            78: 2.74,
            79: 2.79,
            80: 2.80
    ]


    @Override
    public void run() {
        try {
            println('START SCANNING № ' + ++counter + ' AT ' + LocalTime.now())
            Map<Long, ForkInfoLog> matched = new ConcurrentHashMap<>()

            def start = System.nanoTime()
            def newMatches = getNewMatches()
            def end = System.nanoTime()
            def spent = OtherUtils.roundWithPrecision(((end - start) / 1000000000), 2)
            println("TO DOWNLOAD AND PARSE INFO FOR ${newMatches.isEmpty() ? '' : newMatches.size()} MATCHES SPENT ${spent} SEC")


            if (!newMatches.isEmpty()) {
                start = System.nanoTime()
                CyclicBarrier barrier = new CyclicBarrier(newMatches.size())
                1.upto(newMatches.size()) {
                    new MatchParser(barrier, newMatches[it - 1], TODAYS_MATCHES_TO_SCAN, matched)
                }
//                newMatches.each { forkInfoLog ->
//                    def html = getHtmlByPath(forkInfoLog.url, null)
//                    def CG1 = findTagWithAttributeValue(html, 'div', 'data-mutable-id', 'CG1') as NodeChild
//                    if (CG1 != null && forkInfoLog.kickOffDateTime <= LocalDateTime.now().plusDays(KICK_OFF_MAX_DAY_SHIFT)) {
//                        // and match will start more than 7 days
//                        def next = CG1.children().iterator().next() as NodeChild
//                        def next1 = next.children().iterator().next() as NodeChild
//                        def next2 = next1.children().iterator().drop(1).next() as NodeChild
//                        def td = next2.children().last() as NodeChild
//                        def jsonData = new JsonSlurper().parseText(td.attributes()['data-sel'] as String)
//                        forkInfoLog.sinceMin = jsonData['sn'].split(' ')[0].split('-')[0] as byte
//                        forkInfoLog.odd = jsonData['prices']['1'] as double
//                        if (SINCE_MINUTE_TO_ODD.containsKey(forkInfoLog.sinceMin)
//                                && SINCE_MINUTE_TO_ODD[forkInfoLog.sinceMin] <= forkInfoLog.odd) {
//                            matched[forkInfoLog.id] = forkInfoLog
//                        } else {
//                            TODAYS_MATCHES_TO_SCAN.remove(forkInfoLog.id)
//                        }
//                    } else TODAYS_MATCHES_TO_SCAN[forkInfoLog.id] = forkInfoLog
//                }
                end = System.nanoTime()
                spent = OtherUtils.roundWithPrecision(((end - start) / 1000000000), 2)
                println("TO DOWNLOAD AND PARSE ${newMatches.size()} MATCHES SPENT ${spent} SEC")
            }

            TODAYS_MATCHES_TO_SCAN.each { key, value ->
                def log = value as ForkInfoLog
                if (SINCE_MINUTE_TO_ODD.containsKey(log.sinceMin) &&
                        SINCE_MINUTE_TO_ODD[log.sinceMin] <= log.odd) matched[key as Long] = log
            }
            if (!matched.isEmpty()) {
                TODAY_MATCHED_MATCHES_WITH_GOAL_AT_END_OF_MATCH.putAll(matched)
                matched.each { k, v ->
                    v.matchedAt = LocalDateTime.now()
                    println(v)
                    OUTPUT_FILE.append('\n' + v)
                }
                playSound("/Money.mp3")
            }

            println('END SCANNING № ' + counter + ' AT ' + LocalTime.now())
        } catch (Throwable t) {
            playSound('/emergency.mp3')
            println(t)
            println(t.getStackTrace())
        }
    }

    private List<ForkInfoLog> getNewMatches() {
        def matches = new CopyOnWriteArrayList()
        GPathResult footballContainer = findTagWithAttributeValue(getHtmlByPath(marathonFootballUrl, null), 'div',
                'class', 'sport-category-content')
        if (footballContainer != null) {
            footballContainer.children().iterator().collect().each {
                try {
                    Collection collection = it.children().iterator().collect()
                    NodeChild matchesWrapper = collection[1] as NodeChild
                    def next = matchesWrapper.iterator().next()
                    def collect = next.children().iterator().collect()
                    def object = collect[1]
                    def collect1 = object.children().iterator().collect()
                    def object1 = collect1[0]
                    def collect2 = object1.children().iterator().drop(1).collect()
                    collect2.each {
                        try {
                            def tbody = it as NodeChild
                            def tableWithDateTime = findTagWithAttributeValue(tbody, 'table', 'class', 'member-area-content-table')
                            String dateTime = tableWithDateTime.children().iterator().next()
                                    .children().iterator().next()
                                    .children().iterator().drop(1).next().toString().trim()
                            def splitDateTime = dateTime.split(' ')
                            LocalDateTime matchDateTime
                            if (splitDateTime.length == 1) {
                                matchDateTime = LocalDateTime.now().with(LocalTime.parse(dateTime, ofPattern('HH:mm')))
                            } else if (splitDateTime.length == 3) {
                                matchDateTime = LocalDateTime.parse(
                                        "${splitDateTime[0]} ${splitDateTime[1]} ${LocalDate.now().getYear()} ${splitDateTime[2]}",
                                        ofPattern('dd MMM yyyy HH:mm', Locale.US)
                                )
                            } else if (splitDateTime.length == 4) matchDateTime = LocalDateTime.parse(dateTime, ofPattern('dd MMM yyyy HH:mm', Locale.US))

                            def matchId = tbody.attributes()['data-event-treeid'] as Long
                            if (!TODAYS_MATCHES.containsKey(matchId)) {
                                def matchTitle = tbody.attributes()['data-event-name'] as String
                                def split = matchTitle.split(" vs ")
                                def forkInfoLog = new ForkInfoLog(
                                        id: matchId,
                                        sportType: FOOTBALL,
                                        url: marathonUrl + "/en/events.htm?id=${matchId}",
                                        tournamentTitle: findTagWithAttributeValue(collection[0], 'h2', 'class', 'category-label').toString(),
                                        team1Name: split[0] as String,
                                        team2Name: split[1] as String,
                                        kickOffDateTime: matchDateTime
                                )
                                TODAYS_MATCHES[matchId] = forkInfoLog
                                matches.add(forkInfoLog)
                            }
                        } catch (Exception e) {
                            System.err.println('ERROR PROCESSING ' + it)
                            System.err.println(e.getMessage())
                            System.err.println(e.getStackTrace())
                        }
                    }
                } catch (Exception e) {
                    System.err.println('ERROR PROCESSING ' + it)
                    System.err.println(e.getMessage())
                    System.err.println(e.getStackTrace())
                }
            }
        }
        matches
    }

    private LiveInfoLog createLiveInfo(Collection tds, String tournamentName) {
        NodeChild titleWrapper = tds[1].children().iterator().next() as NodeChild
        String matchPath = titleWrapper.attributes()['href']
        String teamsTitle = titleWrapper.localText().iterator().next().toString()
        def split = teamsTitle.split(' - ')
        String homeTeamName = split.length > 0 ? split[0] as String : ''
        String awayTeamName = split.length > 1 ? split[1] as String : ''

        def tdWithTotal = tds[9].toString().trim()
        double total = tdWithTotal.length() < 5 ? 0 : tdWithTotal as double

        def tdWithOverOdd = tds[10].toString().trim()
        double overOdd = tdWithOverOdd.length() < 4 ? 0 : tdWithOverOdd as double

        def tdWithUnderOdd = tds[11].toString().trim()
        double underOdd = tdWithUnderOdd.length() < 4 ? 0 : tdWithUnderOdd as double

        LiveInfoLog liveInfo = new LiveInfoLog(sportType: BASKETBALL, url: maratathonUrl + '/' + matchPath,
                tournamentTitle: tournamentName,
                homeTeamName: homeTeamName.trim(), awayTeamName: awayTeamName.trim(),
                total: total, overOdd: overOdd, underOdd: underOdd, score: getScore(titleWrapper)
        )
        liveInfo
    }

    private static List<Tuple2> getScore(NodeChild titleWrapper) {
        def score = new ArrayList<Tuple2>()
        def span = findTagWithAttributeValue(titleWrapper, 'span', 'class', 'score')
        def scoreSplit = span.toString().split("\\(")
        if (scoreSplit.length == 1) return score

        def overallScoreSplit = scoreSplit[0].split("-")
        def homeScored = overallScoreSplit[0] as int
        def awayScored = overallScoreSplit[1] as int
        def scoreSplitByQuarters = scoreSplit[1].substring(0, scoreSplit[1].length() - 1).split(",")
        scoreSplitByQuarters.each {
            def splitQuarterScore = it.split("-")
            homeScored = splitQuarterScore[0] as int
            awayScored = splitQuarterScore[1] as int
            score.add(new Tuple2(homeScored, awayScored))
        }
        score
    }

    private boolean isOutOfBound(double startInfo, double currentTotal) {
        currentTotal <= startInfo * (1 - borderShiftCoefficient) || startInfo * (1 + borderShiftCoefficient) <= currentTotal
    }

    private static Collection getTds(it) {
        it.children().iterator().collect().drop(6)
        [0].children().iterator().collect()
        [0].children().iterator().collect()
    }

    private static File getOutputFile(String fileName) {
        def outputFileName = '/liveLog_' + LocalDate.now().format(ofPattern('dd-MM-yyyy')) + '.txt'
        def defLocationFileName = '/path.txt'
        def pathToResourceFolder = this.getClass().getResource('/path.txt').getPath().split(defLocationFileName)[0]
        def outputFile = new File(pathToResourceFolder + outputFileName)
        if (!outputFile.exists()) {
            outputFile.createNewFile()
        }
        new File(outputFileName)
    }

}

//            matches.each { forkInfoLog ->
//                def CG1 = findTagWithAttributeValue(getHtmlByPath(forkInfoLog.url, null), 'div', 'class', 'CG1') as NodeChild
//                if (CG1 != null && forkInfoLog.kickOffDateTime <= LocalDateTime.now().plusDays(KICK_OFF_MAX_DAY_SHIFT)) {
//                    // and match will start more than 7 days
//                    def td = CG1.children().iterator().next()
//                            .children().iterator().next()
//                            .children().iterator().drop(1)
//                            .children().last() as NodeChild
//                    def jsonData = new JsonSlurper().parseText(td.attributes()['data-sel'] as String)
//                    forkInfoLog.sinceMin = jsonData['sn'].split(' ')[0].split('-')[0] as byte
//                    forkInfoLog.odd = jsonData['prices']['1'] as double
//                    if (SINCE_MINUTE_TO_ODD[forkInfoLog.sinceMin] <= forkInfoLog.odd) {
//                        matched[forkInfoLog.id] = forkInfoLog
//                    } else {
//                        TODAYS_MATCHES_TO_SCAN.remove(forkInfoLog.id)
//                    }
//                } else TODAYS_MATCHES_TO_SCAN[forkInfoLog.id] = forkInfoLog
//            }