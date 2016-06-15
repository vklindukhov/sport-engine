package football

import java.time.LocalDate

class PlayerAgent {
    String name

    PlayerAgent(String name) {
        this.name = name
    }

    void transferPlayer(LocalDate date, PlayerInfo player, ClubInfo fromClub, ClubInfo toClub) {
        if(fromClub != null) {
            fromClub.removePlayer player, date
        }
        toClub.addPlayer player, date
    }
}

