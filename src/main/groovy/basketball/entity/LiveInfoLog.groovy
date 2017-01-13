package basketball.entity

import groovy.transform.EqualsAndHashCode
import jdk.nashorn.internal.ir.annotations.Immutable

@Immutable
@EqualsAndHashCode(includes = ['url'])
class LiveInfoLog {
    SportType sportType
    String url
    String tournamentTitle
    String homeTeamName
    String awayTeamName
    double total
    double overOdd
    double underOdd
    List<Tuple2> score

    LiveInfoLog(Map<String, Object> fields) {
        sportType = fields['sportType'] as SportType
        url = fields['url'] as String
        tournamentTitle = fields['tournamentTitle'] as String
        homeTeamName = fields['homeTeamName'] as String
        awayTeamName = fields['awayTeamName'] as String
        total = fields['total'] as double
        overOdd = fields['overOdd'] as double
        underOdd = fields['underOdd'] as double
        score = fields['score'] as List<Tuple2>
    }

    boolean isBeginningOfMatch() {
        0 < total && (score.size() == 1 && score.get(0).first < 5 && score.get(0).second < 5 || score.isEmpty())
    }

    boolean isMatchOver() {
        score.size() == 4 && (total == 0 || overOdd == 0 || underOdd == 0)
    }

    boolean isStartOfQuarter(int n) {
        !isMatchOver() && n <= score.size() && isQuarterStartByScore(n)
    }

    private boolean isEndByAverageTotals() {
        0.1 * getQuartersAverageTotal(3, 1) > getQuarterTotal(score.get(3))
    }

    double getQuartersAverageTotal(int amount, int precision) {
        if (score.size() > 0 && score.size() >= amount) {
            def average = score.take(amount).sum { getQuarterTotal(it) } / amount
            Math.round(average * Math.pow(10, precision)) / Math.pow(10, precision)
        } else -1
    }

    private double getQuarterTotal(Tuple2 quarterScore) {
        (quarterScore.first + quarterScore.second)
    }

    private boolean isQuarterStartByScore(int n) {
        n <= score.size() && score.get(n - 1).first < 5 && score.get(n - 1).second < 5
    }

    private boolean isQuarterStartByTotal(int n) {
        n <= score.size() && score.get(n - 1).first + score.get(n - 1).second < 9
    }

    boolean isFinishingOfMatch() {
        !isMatchOver() && 4 <= score.size() && isStartOfQuarter(4)
    }


    @Override
    public String toString() {
        url + ' - ' + tournamentTitle + '. ' + homeTeamName + ' vs ' + awayTeamName/* + ', total=' + total + ", overOdd=" + overOdd + ", underOdd=" + underOdd*/
    }
}
