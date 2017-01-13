package football2.actions

import football2.entity.ForkInfoLog
import groovy.json.JsonSlurper
import groovy.util.slurpersupport.NodeChild

import java.time.LocalDateTime
import java.util.concurrent.BrokenBarrierException
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.atomic.AtomicInteger

import static utils.XmlUtils.findTagWithAttributeValue
import static utils.XmlUtils.getHtmlByPath

class MatchParser extends Thread {
    static AtomicInteger COUNTER = new AtomicInteger(0);
    static def SINCE_MINUTE_TO_ODD = [
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
    CyclicBarrier waitPoint
    ForkInfoLog forkInfoLog
    Map matched
    Map todaysMatchesToScan
    static def KICK_OFF_MAX_DAY_SHIFT = 7

    MatchParser(CyclicBarrier waitPoint, ForkInfoLog forkInfoLog, Map todaysMatchesToScan, Map matched) {
        this.waitPoint = waitPoint
        this.todaysMatchesToScan = todaysMatchesToScan
        this.forkInfoLog = forkInfoLog
        this.matched = matched
        this.start()
    }

    public void run() {
//        System.err.println("${COUNTER.incrementAndGet()}. \nPROCESS ${forkInfoLog}");
        try {
            def CG1 = findTagWithAttributeValue(getHtmlByPath(forkInfoLog.url, null), 'div', 'class', 'CG1') as NodeChild
            if (CG1 != null && forkInfoLog.kickOffDateTime <= LocalDateTime.now().plusDays(KICK_OFF_MAX_DAY_SHIFT)) {
                // and match will start more than 7 days
                def td = CG1.children().iterator().next()
                        .children().iterator().next()
                        .children().iterator().drop(1)
                        .children().last() as NodeChild
                def jsonData = new JsonSlurper().parseText(td.attributes()['data-sel'] as String)
                forkInfoLog.sinceMin = jsonData['sn'].split(' ')[0].split('-')[0] as byte
                forkInfoLog.odd = jsonData['prices']['1'] as double
                if (SINCE_MINUTE_TO_ODD.containsKey(forkInfoLog.sinceMin)
                        && SINCE_MINUTE_TO_ODD[forkInfoLog.sinceMin] <= forkInfoLog.odd) {
                    matched[forkInfoLog.id] = forkInfoLog
                } else {
                    todaysMatchesToScan.remove(forkInfoLog.id)
                }
            } else todaysMatchesToScan[forkInfoLog.id] = forkInfoLog
//            System.err.println("DONE FOR ${forkInfoLog}");
            waitPoint.await(); // await for all four players to arrive
        } catch (BrokenBarrierException | InterruptedException exception) {
//            System.err.println("An exception occurred while waiting... " + exception);
        }
    }
}
