package basketball.actions

import basketball.entity.LiveInfoLog
import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.NodeChild
import utils.OtherUtils

import java.time.LocalDate
import java.time.LocalTime

import static basketball.entity.SportType.BASKETBALL
import static java.time.format.DateTimeFormatter.ofPattern
import static utils.OtherUtils.playSound
import static utils.OtherUtils.roundWithPrecision
import static utils.XmlUtils.findTagWithAttributeValue
import static utils.XmlUtils.getHtmlByPath

class PariMatchLiveScanner extends TimerTask {
    int counter = 0
    def pariMathUrl = 'https://www.parimatch.com'
    def pariMatchLiveUrl = pariMathUrl + '/en/live.html'
    def TODAYS_LIVE_MATCHES = new HashMap<>()
    def TODAYS_MATCHED_LIVE_MATCHES = new HashMap<>()
    def borderShiftCoefficient = 0.1
    def NBA_DIFF = 0.75
    def static OUTPUT_FILENAME = '/liveLog_' + LocalDate.now().format(ofPattern('dd-MM-yyyy')) + '.txt'
    static final def OUTPUT_FILE = OtherUtils.getFile(OUTPUT_FILENAME)


    @Override
    public void run() {
        try {
            println('START SCANNING № ' + ++counter + ' AT ' + LocalTime.now())
            Map<LiveInfoLog, LiveInfoLog> matched = new HashMap<>()
            Map<String, LiveInfoLog> liveMatches = parseMatches()

            liveMatches.each {
                if (isMatched(it)) {
                    LiveInfoLog startInfo = TODAYS_LIVE_MATCHES[it.key] as LiveInfoLog
                    if (isOutOfBound(startInfo.tournamentTitle, startInfo.total, it.value.total)
                            && !TODAYS_MATCHED_LIVE_MATCHES.containsKey(it.key)) {
                        TODAYS_MATCHED_LIVE_MATCHES[it.key] = it.value
                        matched[startInfo] = it.value
                    }
                } else if (it.value.isBeginningOfMatch()) {
                    TODAYS_LIVE_MATCHES[it.key] = it.value
                    def str = 'ADDED NEW MATCH FOR MONITORING ' + it.value
                    OUTPUT_FILE.append('\n' + str)
                    println(str)
                }
            }

            if (!matched.isEmpty()) {
                matched.each { k, v ->
                    def coefficient = v.tournamentTitle.contains('NBA') ? NBA_DIFF * borderShiftCoefficient : borderShiftCoefficient
                    def str = (roundWithPrecision(k.total * (1 - coefficient) as int, 2)) +
                            '/' + k.total + '/' +
                            roundWithPrecision(k.total * (1 + coefficient) as int, 2) +
                            ' out of ' + (v.total < k.total ? 'UNDER' : 'OVER') +
                            ', total =  ' + v.total + ', over odd = ' + v.overOdd + ', under odd = ' + v.underOdd
                    str = '' + k + ' ' + str
                    println(str)
                    OUTPUT_FILE.append('\n' + str)
                }
                playSound("/Money.mp3")
            }

            println('END SCANNING № ' + counter + ' AT ' + LocalTime.now())
        } catch (Throwable t) {
            playSound('/emergency.mp3')
            print(t)
            print(t.getStackTrace())
        }
    }


    private Map<String, LiveInfoLog> parseMatches() {
        def matches = new HashMap()
        def html = getHtmlByPath(pariMatchLiveUrl, null)

        GPathResult basketballContainer = findTagWithAttributeValue(html, 'div', 'class', 'sport basketball')
        if (basketballContainer == null) return matches

        def tournamentsWrapper = basketballContainer.children().iterator().collect()
        def tournaments = basketballContainer.children().iterator().collect()
        Iterator tournamentsIterator = tournaments.iterator()
        def mainTable = tournamentsIterator.next() as NodeChild

        while (tournamentsIterator.hasNext()) {
            def tournamentWrapper = tournamentsIterator.next() as NodeChild
            Iterator tournamentWrapperChildrenIterator = tournamentWrapper.children().iterator()
            NodeChild p = tournamentWrapperChildrenIterator.next() as NodeChild
            def pChildrenList = p.children().iterator().collect()
            def a = pChildrenList[1]
            def childrenList = (tournamentWrapperChildrenIterator.next() as NodeChild).children().iterator().collect()
            childrenList.each {
                Collection tds = it.children().iterator().collect()[6].children().iterator().collect()[0].children().iterator().collect()
                LiveInfoLog info = createLiveInfo(tds, a.toString())
                matches[info.url] = info
            }

        }
        matches
    }

    private LiveInfoLog createLiveInfo(Collection tds, String tournamentName) {
        NodeChild titleWrapper = tds[1].children().iterator().next() as NodeChild
        String matchPath = titleWrapper.attributes()['href']
        String teamsTitle = titleWrapper.localText().iterator().next().toString().trim()
        def split = teamsTitle.split(' - ')
        String homeTeamName = split.length > 0 ? split[0] as String : ''
        String awayTeamName = split.length > 1 ? split[1] as String : ''

        def tdWithTotal = tds[9].toString().trim()
        double total = tdWithTotal.length() < 5 ? 0 : tdWithTotal as double

        def tdWithOverOdd = tds[10].toString().trim()
        double overOdd = tdWithOverOdd.length() < 4 ? 0 : tdWithOverOdd as double

        def tdWithUnderOdd = tds[11].toString().trim()
        double underOdd = tdWithUnderOdd.length() < 4 ? 0 : tdWithUnderOdd as double

        LiveInfoLog liveInfo = new LiveInfoLog(sportType: BASKETBALL, url: pariMathUrl + '/' + matchPath,
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

    private boolean isMatched(Map.Entry<String, LiveInfoLog> it) {
        TODAYS_LIVE_MATCHES.containsKey(it.key) && !it.value.isMatchOver() && !it.value.isFinishingOfMatch()
    }

    private boolean isOutOfBound(String tournamentTitle, double startInfo, double currentTotal) {
        def coefficient = tournamentTitle.contains('NBA') ? NBA_DIFF * borderShiftCoefficient : borderShiftCoefficient
        currentTotal <= startInfo * (1 - coefficient) || startInfo * (1 + coefficient) <= currentTotal
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
