CREATE TABLE defensive_impacts
(
    season_id INT(11) NOT NULL,
    player_id INT(11) NOT NULL,
    team ENUM('OKC', 'SAC', 'NOP', 'MIN', 'SAS', 'IND', 'MEM', 'POR', 'CLE', 'LAC', 'DAL', 'HOU', 'MIL', 'NYK', 'DEN', 'ORL', 'MIA', 'PHX', 'CHA', 'PHI', 'DET', 'ATL', 'WAS', 'BKN', 'LAL', 'UTA', 'BOS', 'CHI', 'TOR', 'GSW') NOT NULL,
    gp TINYINT(3) unsigned DEFAULT '0' NOT NULL,
    w TINYINT(3) unsigned DEFAULT '0' NOT NULL,
    l TINYINT(3) unsigned DEFAULT '0' NOT NULL,
    min FLOAT UNSIGNED DEFAULT '0' NOT NULL,
    stl FLOAT UNSIGNED DEFAULT '0' NOT NULL,
    blk FLOAT UNSIGNED DEFAULT '0',
    dreb FLOAT UNSIGNED DEFAULT '0' NOT NULL,
    dfgm FLOAT UNSIGNED DEFAULT '0',
    dfga FLOAT UNSIGNED DEFAULT '0' NOT NULL,
    dfg_pct FLOAT UNSIGNED DEFAULT '0' NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (season_id, player_id)
);
CREATE TABLE games_logs
(
    season_id INT(10) unsigned NOT NULL,
    player_id INT(10) unsigned NOT NULL,
    game_id VARCHAR(200) NOT NULL,
    game_date DATE NOT NULL,
    player_team ENUM('ATL', 'BOS', 'BKN', 'CHA', 'CHI', 'CLE', 'DAL', 'DEN', 'DET', 'GSW', 'HOU', 'IND', 'LAC', 'LAL', 'MEM', 'MIA', 'MIL', 'MIN', 'NOP', 'NYK', 'OKC', 'ORL', 'PHI', 'PHX', 'POR', 'SAC', 'SAS', 'TOR', 'UTA', 'WAS') NOT NULL,
    rival_team ENUM('ATL', 'BOS', 'BKN', 'CHA', 'CHI', 'CLE', 'DAL', 'DEN', 'DET', 'GSW', 'HOU', 'IND', 'LAC', 'LAL', 'MEM', 'MIA', 'MIL', 'MIN', 'NOP', 'NYK', 'OKC', 'ORL', 'PHI', 'PHX', 'POR', 'SAC', 'SAS', 'TOR', 'UTA', 'WAS') NOT NULL,
    is_at_home TINYINT(1) NOT NULL,
    wl CHAR(1) NOT NULL,
    min TINYINT(3) unsigned DEFAULT '0' NOT NULL,
    ftm TINYINT(3) unsigned DEFAULT '0' NOT NULL,
    fta TINYINT(3) unsigned DEFAULT '0' NOT NULL,
    ft_pct FLOAT UNSIGNED DEFAULT '0' NOT NULL,
    oreb TINYINT(3) unsigned DEFAULT '0' NOT NULL,
    dreb TINYINT(3) unsigned DEFAULT '0' NOT NULL,
    reb TINYINT(3) unsigned DEFAULT '0' NOT NULL,
    ast TINYINT(3) unsigned DEFAULT '0' NOT NULL,
    stl TINYINT(3) unsigned DEFAULT '0' NOT NULL,
    blk TINYINT(3) unsigned DEFAULT '0' NOT NULL,
    tov TINYINT(3) unsigned DEFAULT '0' NOT NULL,
    pf TINYINT(3) unsigned DEFAULT '0' NOT NULL,
    pts TINYINT(3) unsigned DEFAULT '0' NOT NULL,
    plus_minus TINYINT(4) DEFAULT '0' NOT NULL,
    url VARCHAR(200) NOT NULL,
    fgm TINYINT(3) unsigned DEFAULT '0' NOT NULL,
    fga TINYINT(3) unsigned DEFAULT '0' NOT NULL,
    fg_pct FLOAT UNSIGNED DEFAULT '0' NOT NULL,
    fg3m FLOAT UNSIGNED DEFAULT '0' NOT NULL,
    fg3a FLOAT UNSIGNED DEFAULT '0' NOT NULL,
    fg3_pct FLOAT UNSIGNED DEFAULT '0' NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (season_id, player_id, game_id)
);
CREATE TABLE games_schedules
(
    season_id INT(10) unsigned NOT NULL,
    game_id VARCHAR(20) PRIMARY KEY NOT NULL,
    game_date DATE NOT NULL,
    home_team ENUM('ATL', 'BOS', 'BKN', 'CHA', 'CHI', 'CLE', 'DAL', 'DEN', 'DET', 'GSW', 'HOU', 'IND', 'LAC', 'LAL', 'MEM', 'MIA', 'MIL', 'MIN', 'NOP', 'NYK', 'OKC', 'ORL', 'PHI', 'PHX', 'POR', 'SAC', 'SAS', 'TOR', 'UTA', 'WAS') NOT NULL,
    away_team ENUM('ATL', 'BOS', 'BKN', 'CHA', 'CHI', 'CLE', 'DAL', 'DEN', 'DET', 'GSW', 'HOU', 'IND', 'LAC', 'LAL', 'MEM', 'MIA', 'MIL', 'MIN', 'NOP', 'NYK', 'OKC', 'ORL', 'PHI', 'PHX', 'POR', 'SAC', 'SAS', 'TOR', 'UTA', 'WAS') NOT NULL
);
CREATE TABLE players
(
    id INT(11) unsigned PRIMARY KEY NOT NULL,
    name MEDIUMTEXT,
    surname VARCHAR(30) NOT NULL,
    position ENUM('PG', 'SG', 'SF', 'PF', 'C'),
    role ENUM('PG', 'SG', 'SF', 'PF', 'C'),
    nba_2k17_rating TINYINT(3) unsigned,
    url VARCHAR(200) NOT NULL,
    is_franchise VARCHAR(200) DEFAULT '0' NOT NULL,
    team ENUM('ATL', 'BOS', 'BKN', 'CHA', 'CHI', 'CLE', 'DAL', 'DEN', 'DET', 'GSW', 'HOU', 'IND', 'LAC', 'LAL', 'MEM', 'MIA', 'MIL', 'MIN', 'NOP', 'NYK', 'OKC', 'ORL', 'PHI', 'PHX', 'POR', 'SAC', 'SAS', 'TOR', 'UTA', 'WAS')
);
CREATE UNIQUE INDEX players_id_uindex ON players (id);
CREATE UNIQUE INDEX players_url_uindex ON players (url);
CREATE PROCEDURE best_players_amount_per_each_team();
CREATE PROCEDURE matches_pct_per_player();
CREATE PROCEDURE average_value_best_players();
CREATE PROCEDURE best_players_amount();
CREATE PROCEDURE best_players_grouped_by_roles_amount();
CREATE PROCEDURE team_matches_amount();
CREATE PROCEDURE best_players_one_on_position();
CREATE PROCEDURE each_team_has_one_best_player_on_position();
CREATE PROCEDURE each_team_has_many_best_players_on_position();
CREATE PROCEDURE each_team_best_players_on_position_and_sub();
CREATE PROCEDURE each_team_best_players_with_under_pts();
CREATE PROCEDURE under_pts_predictions_for_todays_matches();