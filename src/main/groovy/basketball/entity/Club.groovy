package basketball.entity

enum Club {
    ATL('Atlanta'),
    BOS('Boston'),
    BKN('Brooklyn'),
    CHA('Charlotte'),
    CHI('Chicago'),
    CLE('Cleveland'),
    DAL('Dallas'),
    DEN('Denver'),
    DET('Detroit'),
    GSW('Golden State'),
    HOU('Houston'),
    IND('Indiana'),
    LAC('LA Clippers'),
    LAL('Los Angeles'),
    MEM('Memphis'),
    MIA('Miami'),
    MIL('Milwaukee'),
    MIN('Minnesota'),
    NOP('New Orleans'),
    NYK('New York'),
    OKC('Oklahoma City'),
    ORL('Orlando'),
    PHI('Philadelphia'),
    PHX('Phoenix'),
    POR('Portland'),
    SAC('Sacramento'),
    SAS('San Antonio'),
    TOR('Toronto'),
    UTA('Utah'),
    WAS('Washington')

    String displayName
    static Map<String, Club> all = new HashMap<>();
    static {
        for (Club club : values()) {
            all.put(club.name(), club)
        }
    }

    Club(String displayName) {
        this.displayName = displayName
    }

    static Club getClubByCode(String code) {
        all[code]
    }
}