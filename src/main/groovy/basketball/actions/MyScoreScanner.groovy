package basketball.actions

class MyScoreScanner {
    //MATCHES_IDS = new HashMap<>()
////workWithMyScore(path, MATCHES_IDS)
//private Map getTotals() {
//    MATCHES_IDS.each {
//        MyScoreMatchInfo match = it.value as MyScoreMatchInfo
//        def html = getHtmlByPath('http://d.myscore.com.ua/x/feed/d_od_' + it.key + '_ru_1_eu', 'X-Fsign': 'SW9D1eZo')
//        def GPathResult found = findTagWithAttributeValue(html, 'div', 'id', 'block-under-over-ft-include-ot')
//        if (found != null) {
//            def totalTablesIterator = found.children().iterator()
//            short total = 0
//            int ind = 1
//            while (totalTablesIterator.hasNext()) {
//                NodeChild nodeChild = totalTablesIterator.next() as NodeChild
//                if (nodeChild.attributes()['id'].contains('odds_ou_')) {
//                    total = nodeChild.attributes()['id'].split('_')[2] as double
//                    if (ind++ == 1) match.minTotal = total
//                }
//            }
//            if (total > 0) {
//                match.maxTotal = total + 1
//                match.total = match.minTotal + (match.maxTotal - match.minTotal) / 2
//            }
//        }
//    }
//}
//def getMatchesIds(String path) {
//    def html = getHtmlByPath(path)
//
//    GPathResult matches = findTagWithAttributeValue(html, 'div', 'class', 'table-main')
//
//    def tournamentsIterator = matches.children().iterator()
//    while (tournamentsIterator.hasNext()) {
//        def element = tournamentsIterator.next() as NodeChild
//        if (element.attributes().hasProperty('class') && element.attributes()['class'] == 'basketball') {
//            def iterator = element.children().iterator()
//            def (String countryName, String tournamentName) = getCountryAndTournament(++iterator.drop(2) as NodeChild)
//            def tbody = iterator.next() as NodeChild
//            def teamsIterator = tbody.children().iterator()
//            addMatches(teamsIterator, countryName, tournamentName)
//        }
//    }
//}
//private void addMatches(Iterator teamsIterator, String countryName, String tournamentName) {
//    while (teamsIterator.hasNext()) {
//        NodeChild trTeam = teamsIterator.next() as NodeChild
//        if (trTeam.attributes().hasProperty('class') && trTeam.attributes()["class"].contains("stage")) {
//            String elementId = trTeam.attributes()['id'] as String
//            String matchId = elementId.split("_")[2] as String
//            if (!MATCHES_IDS.containsKey(matchId)) {
//                def trIterator = trTeam.children().iterator()
//                def time = parse((++trIterator.drop(1)).toString(), ofPattern('HH:mm', ENGLISH))
//                def tdWithName = ++trIterator.drop(1)
//                MATCHES_IDS.put(matchId,
//                        new MyScoreMatchInfo(
//                                url: 'http://www.myscore.com.ua/match/' + matchId,
//                                dateTime: LocalDateTime.now().withHour(time.getHour()).withMinute(time.getMinute()),
//                                countryName: countryName.toLowerCase().capitalize(),
//                                tournamentName: tournamentName,
//                                team1Name: tdWithName.toString()
//                        )
//                )
//            } else MATCHES_IDS[matchId].setAwayTeamName(trTeam.children().iterator().next().toString())
//        }
//    }
//}
//private List getCountryAndTournament(NodeChild thead) {
////
////        <thead>
////          <tr class="tournament l_3_fR3khQta ">
////              <td colspan="1" class="head_aa ">
////                  <div class="dicons">
////                      <span class="icons left">
////                          <span id="latomyg_3_fR3khQta" class="tomyg"></span>
////                      </span>
////                  </div>
////              </td>
////              <td colspan="11" class="head_ab ">
////                  <span class="stats-link link-tables">
////                      <span class="stats" title="Таблица">Таблица</span>
////                  </span>
////                  <span class="country left">
////                      <span class="flag fl_28 left" title="Бахрейн"></span>
////                      <span class="name">
////                          <span class="country_part">БАХРЕЙН: </span>
////                          <span class="tournament_part">Премьер-Лига</span>
////                      </span>
////                  </span>
////                  <span class="toggleMyLeague 3_28_x0itGkel" title="Добавить эту лигу в раздел Мои лиги!" data-label-key="3_28_x0itGkel" onclick="cjs.myLeagues.toggleTop('3_28_x0itGkel', event); return false;"></span>
////              </td>
////          </tr>
////        </thead>
//    def tdIterator = (++thead.children().iterator()).children().iterator()
//    def spanIterator = tdIterator.drop(1).next().children().iterator()
//    spanIterator = spanIterator.drop(1).next().children().iterator()
//    spanIterator = spanIterator.drop(1).next().children().iterator()
//    def cN = spanIterator.next().toString()
//    String countryName = cN.split(":")[0]
//    String tournamentName = spanIterator.next().toString()
//    [countryName, tournamentName]
//}
//private void workWithMyScore(String path, HashMap MATCHES_IDS) {
//    getMatchesIds(path)
//    getTotals()
//
//    println('MATCHES ON ' + LocalDate.now())
//    println('-----------------------------------------------------------------------------------------------------------------------------------------------')
//    int COUNTER = 1
//    MATCHES_IDS.values() findAll { it.total > 0 } sort { x, y ->
//        if (x.dateTime == y.dateTime) x.tournamentName <=> y.tournamentName
//        else x.dateTime <=> y.dateTime
//    } each { println(COUNTER++ + '.\t' + it) }
//}
}