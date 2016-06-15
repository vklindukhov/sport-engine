package football

import groovy.transform.EqualsAndHashCode
import jdk.nashorn.internal.ir.annotations.Immutable

import java.util.concurrent.atomic.AtomicInteger

@Immutable
@EqualsAndHashCode(includes = ['id'])
class EventReport {
    final static AtomicInteger COUNTER = new AtomicInteger(1)
    long id;
    byte atMinute;
    EventType eventType;
    PlayerInfo byPlayer

    EventReport(byte atMinute, EventType eventType, PlayerInfo byPlayer) {
        this.atMinute = atMinute
        this.eventType = eventType
        this.byPlayer = byPlayer
        id = COUNTER.getAndIncrement()
    }

    enum EventType {
        GOAL, PENALTY_GOAL, PENALTY_MISSED, AUTO_GOAL, ASSIST,
        YELLOW_CARD, YELLOW_SECOND_CARD, RED_CARD,
        SUBSTITUTION_OUT, SUBSTITUTION_IN
    }
}
