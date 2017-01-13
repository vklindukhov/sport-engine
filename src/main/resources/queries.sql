DELETE FROM players;
DELETE FROM defensive_impacts;
DELETE FROM games_logs;
DELETE FROM games_schedules;

SET @SEASON_ID = 22016, @MIN_PLAYED_MIN = 16, @MATCHES_PCT = 0.45, @COMPARE_PCT_DELTA = 0.04, @MAX_DFG_PCT = 0.5,
@MIN_AVG_PTS = 10, @PTS_UNDER_MIN_AMOUNT = 0.5, @MATCH_DATE = CURDATE();
# RESET
SET @SEASON_ID = 22016, @MIN_PLAYED_MIN = 0, @MATCHES_PCT = 0;
# RESET

# COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
DELIMITER //
CREATE PROCEDURE team_matches_amount
  ()
  BEGIN
    SELECT
      gl.player_team               AS TEAM,
      count(DISTINCT gl.game_date) AS MATCHES_AMOUNT
    FROM games_logs gl
    WHERE gl.season_id = @SEASON_ID
    GROUP BY gl.player_team;
  END //
DELIMITER ;
# COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @ID_SEASON
CALL team_matches_amount;

# COUNTS MATCHES_PCT AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
DELIMITER //
CREATE PROCEDURE matches_pct_per_player
  ()
  BEGIN
    SELECT
      gl.PLAYER_TEAM,
      p.NAME,
      p.SURNAME,
      p.ROLE,
      count(gl.game_date) / TEAMS_MATCHES_AMOUNT.MATCHES_AMOUNT AS MATCHES_PCT
    FROM games_logs gl
      JOIN players p ON gl.player_id = p.id
      JOIN (
             # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
             SELECT
               gl.player_team               AS TEAM,
               count(DISTINCT gl.game_date) AS MATCHES_AMOUNT
             FROM games_logs gl
             WHERE gl.season_id = @SEASON_ID
             GROUP BY gl.player_team
             # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
           ) AS TEAMS_MATCHES_AMOUNT ON gl.player_team = TEAMS_MATCHES_AMOUNT.TEAM
    WHERE gl.season_id = @SEASON_ID AND @MIN_PLAYED_MIN <= gl.min
    GROUP BY gl.player_team, gl.player_id
    HAVING @MATCHES_PCT <= MATCHES_PCT
    ORDER BY gl.PLAYER_TEAM, MATCHES_PCT DESC;
  END //
DELIMITER ;
# COUNTS MATCHES_PCT AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
CALL matches_pct_per_player;

# CALCULATES BEST PLAYERS FOR NBA
DELIMITER //
CREATE PROCEDURE best_players_amount
  ()
  BEGIN
    SELECT COUNT(TEAMS_BEST_PLAYERS.MATCHES_PCT) AS PLAYERS_AMOUNT
    FROM
      (
        SELECT
          gl.PLAYER_TEAM,
          p.NAME,
          p.SURNAME,
          count(gl.game_date) / TEAMS_MATCHES_AMOUNT.MATCHES_AMOUNT AS MATCHES_PCT
        FROM games_logs gl
          JOIN players p ON gl.player_id = p.id
          JOIN (
                 # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                 SELECT
                   gl.player_team               AS TEAM,
                   count(DISTINCT gl.game_date) AS MATCHES_AMOUNT
                 FROM games_logs gl
                 WHERE gl.season_id = @SEASON_ID
                 GROUP BY gl.player_team
                 # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
               ) AS TEAMS_MATCHES_AMOUNT ON gl.player_team = TEAMS_MATCHES_AMOUNT.TEAM
        WHERE gl.season_id = @SEASON_ID AND @MIN_PLAYED_MIN <= gl.min
        GROUP BY gl.player_team, gl.player_id
        HAVING @MATCHES_PCT <= MATCHES_PCT
      ) AS TEAMS_BEST_PLAYERS;
  END //
DELIMITER ;
# CALCULATES BEST PLAYERS FOR NBA
CALL best_players_amount;

# CALCULATES BEST PLAYERS FOR EACH TEAM
DELIMITER //
CREATE PROCEDURE best_players_amount_per_each_team
  ()
  BEGIN
    SELECT
      TEAMS_BEST_PLAYERS.player_team    AS TEAM,
      COUNT(TEAMS_BEST_PLAYERS.surname) AS PLAYERS_AMOUNT
    FROM
      (
        SELECT
          gl.PLAYER_TEAM,
          p.NAME,
          p.SURNAME,
          count(gl.game_date) / TEAMS_MATCHES_AMOUNT.MATCHES_AMOUNT AS MATCHES_PCT
        FROM games_logs gl
          JOIN players p ON gl.player_id = p.id
          JOIN (
                 # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                 SELECT
                   gl.player_team               AS TEAM,
                   count(DISTINCT gl.game_date) AS MATCHES_AMOUNT
                 FROM games_logs gl
                 WHERE gl.season_id = @SEASON_ID
                 GROUP BY gl.player_team
                 # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
               ) AS TEAMS_MATCHES_AMOUNT ON gl.player_team = TEAMS_MATCHES_AMOUNT.TEAM
        WHERE gl.season_id = @SEASON_ID AND @MIN_PLAYED_MIN <= gl.min
        GROUP BY gl.player_team, gl.player_id
        HAVING @MATCHES_PCT <= MATCHES_PCT
      ) AS TEAMS_BEST_PLAYERS
    GROUP BY TEAMS_BEST_PLAYERS.player_team;
  END //
DELIMITER ;
# CALCULATES BEST PLAYERS FOR EACH TEAM
CALL best_players_amount_per_each_team;

DELIMITER //
CREATE PROCEDURE average_value_best_players
  ()
  BEGIN
    SELECT AVG(TEAMS_BEST_PLAYERS.PLAYERS_AMOUNT)
    FROM (
           SELECT
             TEAMS_BEST_PLAYERS.player_team    AS TEAM,
             COUNT(TEAMS_BEST_PLAYERS.surname) AS PLAYERS_AMOUNT
           FROM
             (
               SELECT
                 gl.PLAYER_TEAM,
                 p.NAME,
                 p.SURNAME,
                 count(gl.game_date) / TEAMS_MATCHES_AMOUNT.MATCHES_AMOUNT AS MATCHES_PCT
               FROM games_logs gl
                 JOIN players p ON gl.player_id = p.id
                 JOIN (
                        # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                        SELECT
                          gl.player_team               AS TEAM,
                          count(DISTINCT gl.game_date) AS MATCHES_AMOUNT
                        FROM games_logs gl
                        WHERE gl.season_id = @SEASON_ID
                        GROUP BY gl.player_team
                        # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                      ) AS TEAMS_MATCHES_AMOUNT ON gl.player_team = TEAMS_MATCHES_AMOUNT.TEAM
               WHERE gl.season_id = @SEASON_ID AND @MIN_PLAYED_MIN <= gl.min
               GROUP BY gl.player_team, gl.player_id
               HAVING @MATCHES_PCT <= MATCHES_PCT
             ) AS TEAMS_BEST_PLAYERS
           GROUP BY TEAMS_BEST_PLAYERS.player_team
         ) AS TEAMS_BEST_PLAYERS;
  END //
DELIMITER ;
# CALCULATES AVERAGE BEST PLAYERS FOR NBA
CALL average_value_best_players;

# CALCULATES BEST PLAYERS FOR EACH TEAM GROUPED BY ROLES AMOUNT
DELIMITER //
CREATE PROCEDURE best_players_grouped_by_roles_amount
  ()
  BEGIN
    SELECT
      PGS.TEAM,
      PGS.PG,
      SGS.SG,
      SFS.SF,
      PFS.PF,
      CS.C,
      ALL_PLAYERS.OVERALL
    FROM (SELECT
            TEAMS_BEST_PGS.player_team        AS TEAM,
            COUNT(TEAMS_BEST_PGS.MATCHES_PCT) AS PG
          FROM
            (
              SELECT
                gl.PLAYER_TEAM,
                p.NAME,
                p.SURNAME,
                count(gl.game_date) / TEAMS_MATCHES_AMOUNT.MATCHES_AMOUNT AS MATCHES_PCT
              FROM games_logs gl
                JOIN players p ON gl.player_id = p.id
                JOIN (
                       # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                       SELECT
                         gl.player_team               AS TEAM,
                         count(DISTINCT gl.game_date) AS MATCHES_AMOUNT
                       FROM games_logs gl
                       WHERE gl.season_id = @SEASON_ID
                       GROUP BY gl.player_team
                       # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                     ) AS TEAMS_MATCHES_AMOUNT ON gl.player_team = TEAMS_MATCHES_AMOUNT.TEAM
              WHERE gl.season_id = @SEASON_ID AND @MIN_PLAYED_MIN <= gl.min AND p.role = 'PG'
              GROUP BY gl.player_team, gl.player_id
              HAVING @MATCHES_PCT <= MATCHES_PCT
            ) AS TEAMS_BEST_PGS
          GROUP BY TEAMS_BEST_PGS.player_team) AS PGS
      JOIN (SELECT
              TEAMS_BEST_SGS.player_team        AS TEAM,
              COUNT(TEAMS_BEST_SGS.MATCHES_PCT) AS SG
            FROM
              (
                SELECT
                  gl.PLAYER_TEAM,
                  p.NAME,
                  p.SURNAME,
                  count(gl.game_date) / TEAMS_MATCHES_AMOUNT.MATCHES_AMOUNT AS MATCHES_PCT
                FROM games_logs gl
                  JOIN players p ON gl.player_id = p.id
                  JOIN (
                         # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                         SELECT
                           gl.player_team               AS TEAM,
                           count(DISTINCT gl.game_date) AS MATCHES_AMOUNT
                         FROM games_logs gl
                         WHERE gl.season_id = @SEASON_ID
                         GROUP BY gl.player_team
                         # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                       ) AS TEAMS_MATCHES_AMOUNT ON gl.player_team = TEAMS_MATCHES_AMOUNT.TEAM
                WHERE gl.season_id = @SEASON_ID AND @MIN_PLAYED_MIN <= gl.min AND p.role = 'SG'
                GROUP BY gl.player_team, gl.player_id
                HAVING @MATCHES_PCT <= MATCHES_PCT
              ) AS TEAMS_BEST_SGS
            GROUP BY TEAMS_BEST_SGS.player_team) AS SGS ON PGS.TEAM = SGS.TEAM
      JOIN (SELECT
              TEAMS_BEST_SFS.player_team        AS TEAM,
              COUNT(TEAMS_BEST_SFS.MATCHES_PCT) AS SF
            FROM
              (
                SELECT
                  gl.PLAYER_TEAM,
                  p.NAME,
                  p.SURNAME,
                  count(gl.game_date) / TEAMS_MATCHES_AMOUNT.MATCHES_AMOUNT AS MATCHES_PCT
                FROM games_logs gl
                  JOIN players p ON gl.player_id = p.id
                  JOIN (
                         # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                         SELECT
                           gl.player_team               AS TEAM,
                           count(DISTINCT gl.game_date) AS MATCHES_AMOUNT
                         FROM games_logs gl
                         WHERE gl.season_id = @SEASON_ID
                         GROUP BY gl.player_team
                         # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                       ) AS TEAMS_MATCHES_AMOUNT ON gl.player_team = TEAMS_MATCHES_AMOUNT.TEAM
                WHERE gl.season_id = @SEASON_ID AND @MIN_PLAYED_MIN <= gl.min AND p.role = 'SF'
                GROUP BY gl.player_team, gl.player_id
                HAVING @MATCHES_PCT <= MATCHES_PCT
              ) AS TEAMS_BEST_SFS
            GROUP BY TEAMS_BEST_SFS.player_team) AS SFS ON PGS.TEAM = SFS.TEAM
      JOIN (SELECT
              TEAMS_BEST_PFS.player_team        AS TEAM,
              COUNT(TEAMS_BEST_PFS.MATCHES_PCT) AS PF
            FROM
              (
                SELECT
                  gl.PLAYER_TEAM,
                  p.NAME,
                  p.SURNAME,
                  count(gl.game_date) / TEAMS_MATCHES_AMOUNT.MATCHES_AMOUNT AS MATCHES_PCT
                FROM games_logs gl
                  JOIN players p ON gl.player_id = p.id
                  JOIN (
                         # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                         SELECT
                           gl.player_team               AS TEAM,
                           count(DISTINCT gl.game_date) AS MATCHES_AMOUNT
                         FROM games_logs gl
                         WHERE gl.season_id = @SEASON_ID
                         GROUP BY gl.player_team
                         # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                       ) AS TEAMS_MATCHES_AMOUNT ON gl.player_team = TEAMS_MATCHES_AMOUNT.TEAM
                WHERE gl.season_id = @SEASON_ID AND @MIN_PLAYED_MIN <= gl.min AND p.role = 'PF'
                GROUP BY gl.player_team, gl.player_id
                HAVING @MATCHES_PCT <= MATCHES_PCT
              ) AS TEAMS_BEST_PFS
            GROUP BY TEAMS_BEST_PFS.player_team) AS PFS ON PGS.TEAM = PFS.TEAM
      JOIN (SELECT
              TEAMS_BEST_CS.player_team        AS TEAM,
              COUNT(TEAMS_BEST_CS.MATCHES_PCT) AS C
            FROM
              (
                SELECT
                  gl.PLAYER_TEAM,
                  p.NAME,
                  p.SURNAME,
                  p.ROLE,
                  count(gl.game_date) / TEAMS_MATCHES_AMOUNT.MATCHES_AMOUNT AS MATCHES_PCT
                FROM games_logs gl
                  JOIN players p ON gl.player_id = p.id
                  JOIN (
                         # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                         SELECT
                           gl.player_team               AS TEAM,
                           count(DISTINCT gl.game_date) AS MATCHES_AMOUNT
                         FROM games_logs gl
                         WHERE gl.season_id = @SEASON_ID
                         GROUP BY gl.player_team
                         # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                       ) AS TEAMS_MATCHES_AMOUNT ON gl.player_team = TEAMS_MATCHES_AMOUNT.TEAM
                WHERE gl.season_id = @SEASON_ID AND @MIN_PLAYED_MIN <= gl.min AND p.role = 'C'
                GROUP BY gl.player_team, gl.player_id
                HAVING @MATCHES_PCT <= MATCHES_PCT
              ) AS TEAMS_BEST_CS
            GROUP BY TEAMS_BEST_CS.player_team) AS CS ON PGS.TEAM = CS.TEAM
      JOIN (SELECT
              TEAMS_BEST.player_team        AS TEAM,
              COUNT(TEAMS_BEST.MATCHES_PCT) AS OVERALL
            FROM
              (
                SELECT
                  gl.PLAYER_TEAM,
                  p.NAME,
                  p.SURNAME,
                  p.ROLE,
                  count(gl.game_date) / TEAMS_MATCHES_AMOUNT.MATCHES_AMOUNT AS MATCHES_PCT
                FROM games_logs gl
                  JOIN players p ON gl.player_id = p.id
                  JOIN (
                         # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                         SELECT
                           gl.player_team               AS TEAM,
                           count(DISTINCT gl.game_date) AS MATCHES_AMOUNT
                         FROM games_logs gl
                         WHERE gl.season_id = @SEASON_ID
                         GROUP BY gl.player_team
                         # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                       ) AS TEAMS_MATCHES_AMOUNT ON gl.player_team = TEAMS_MATCHES_AMOUNT.TEAM
                WHERE gl.season_id = @SEASON_ID AND @MIN_PLAYED_MIN <= gl.min
                GROUP BY gl.player_team, gl.player_id
                HAVING @MATCHES_PCT <= MATCHES_PCT
                ORDER BY gl.PLAYER_TEAM, p.ROLE
              ) AS TEAMS_BEST
            GROUP BY TEAMS_BEST.player_team) AS ALL_PLAYERS ON PGS.TEAM = ALL_PLAYERS.TEAM;
  END //
DELIMITER ;
# CALCULATES BEST PLAYERS FOR EACH TEAM GROUPED BY ROLES AMOUNT
CALL best_players_grouped_by_roles_amount;

# CALCULATES BEST PLAYERS FOR EACH TEAM WHERE ONLY ONE PLAYER ON A POSITION
DELIMITER //
CREATE PROCEDURE each_team_has_one_best_player_on_position
  ()
  BEGIN
    SELECT *
    FROM (
           SELECT
             gl.PLAYER_TEAM,
             p.NAME,
             p.SURNAME,
             count(gl.game_date) / TEAMS_MATCHES_AMOUNT.MATCHES_AMOUNT AS MATCHES_PCT,
             di.DFG_PCT,
             p.ROLE,
             ''                                                        AS SUB_NAME,
             ''                                                        AS SUB_SURNAME,
             ''                                                        AS SUB_MATCHES_PCT,
             ''                                                        AS SUB_DFG_PCT
           FROM games_logs gl
             JOIN players p ON gl.player_id = p.id
             JOIN (
                    # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                    SELECT
                      gl.player_team               AS TEAM,
                      count(DISTINCT gl.game_date) AS MATCHES_AMOUNT
                    FROM games_logs gl
                    WHERE gl.season_id = @SEASON_ID
                    GROUP BY gl.player_team
                    # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                  ) AS TEAMS_MATCHES_AMOUNT ON gl.player_team = TEAMS_MATCHES_AMOUNT.TEAM
             JOIN defensive_impacts di ON di.season_id = gl.season_id AND di.player_id = p.id
           WHERE gl.season_id = @SEASON_ID AND @MIN_PLAYED_MIN <= gl.min
           GROUP BY gl.player_team, gl.player_id
           HAVING @MATCHES_PCT <= MATCHES_PCT
           ORDER BY gl.PLAYER_TEAM, MATCHES_PCT DESC
         ) AS BEST_PLAYERS
    GROUP BY BEST_PLAYERS.PLAYER_TEAM, BEST_PLAYERS.ROLE
    HAVING COUNT(MATCHES_PCT) = 1
    ORDER BY BEST_PLAYERS.PLAYER_TEAM, BEST_PLAYERS.MATCHES_PCT DESC, BEST_PLAYERS.DFG_PCT;
  END //
DELIMITER ;
# CALCULATES BEST PLAYERS FOR EACH TEAM WHERE ONLY ONE PLAYER ON A POSITION
CALL each_team_has_one_best_player_on_position;

# CALCULATES BEST PLAYERS FOR EACH TEAM WHERE MORE THAN ONE PLAYER ON A POSITION
DELIMITER //
CREATE PROCEDURE each_team_has_many_best_players_on_position
  ()
  BEGIN
    SELECT
      BEST_PLAYERS.PLAYER_TEAM,
      BEST_PLAYERS.NAME,
      BEST_PLAYERS.SURNAME,
      BEST_PLAYERS.MATCHES_PCT,
      BEST_PLAYERS.DFG_PCT,
      BEST_PLAYERS.ROLE,
      BEST_PLAYERS2.NAME        AS SUB_NAME,
      BEST_PLAYERS2.SURNAME     AS SUB_SURNAME,
      BEST_PLAYERS2.MATCHES_PCT AS SUB_MATCHES_PCT,
      BEST_PLAYERS2.DFG_PCT     AS SUB_DFG_PCT
    FROM (
           SELECT
             gl.PLAYER_TEAM,
             p.id,
             p.NAME,
             p.SURNAME,
             p.IS_FRANCHISE,
             count(gl.game_date) / TEAMS_MATCHES_AMOUNT.MATCHES_AMOUNT AS MATCHES_PCT,
             di.DFG_PCT,
             p.ROLE
           FROM games_logs gl
             JOIN players p ON gl.player_id = p.id
             JOIN (
                    # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                    SELECT
                      gl.player_team               AS TEAM,
                      count(DISTINCT gl.game_date) AS MATCHES_AMOUNT
                    FROM games_logs gl
                    WHERE gl.season_id = @SEASON_ID
                    GROUP BY gl.player_team
                    # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                  ) AS TEAMS_MATCHES_AMOUNT ON gl.player_team = TEAMS_MATCHES_AMOUNT.TEAM
             JOIN defensive_impacts di ON di.season_id = gl.season_id AND di.player_id = p.id
           WHERE gl.season_id = @SEASON_ID AND @MIN_PLAYED_MIN <= gl.min
           GROUP BY gl.player_team, gl.player_id
           HAVING @MATCHES_PCT <= MATCHES_PCT
           ORDER BY gl.PLAYER_TEAM, MATCHES_PCT DESC
         ) AS BEST_PLAYERS
      JOIN
      (
        SELECT
          gl.PLAYER_TEAM,
          p.id,
          p.NAME,
          p.SURNAME,
          p.IS_FRANCHISE,
          count(gl.game_date) / TEAMS_MATCHES_AMOUNT.MATCHES_AMOUNT AS MATCHES_PCT,
          di.DFG_PCT,
          p.ROLE
        FROM games_logs gl
          JOIN players p ON gl.player_id = p.id
          JOIN (
                 # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                 SELECT
                   gl.player_team               AS TEAM,
                   count(DISTINCT gl.game_date) AS MATCHES_AMOUNT
                 FROM games_logs gl
                 WHERE gl.season_id = @SEASON_ID
                 GROUP BY gl.player_team
                 # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
               ) AS TEAMS_MATCHES_AMOUNT ON gl.player_team = TEAMS_MATCHES_AMOUNT.TEAM
          JOIN defensive_impacts di ON di.season_id = gl.season_id AND di.player_id = p.id
        WHERE gl.season_id = @SEASON_ID AND @MIN_PLAYED_MIN <= gl.min
        GROUP BY gl.player_team, gl.player_id
        HAVING @MATCHES_PCT <= MATCHES_PCT
        ORDER BY gl.PLAYER_TEAM, MATCHES_PCT DESC
      ) AS BEST_PLAYERS2
        ON BEST_PLAYERS.player_team = BEST_PLAYERS2.player_team AND BEST_PLAYERS.id <> BEST_PLAYERS2.id AND
           BEST_PLAYERS.role = BEST_PLAYERS2.role
    WHERE BEST_PLAYERS.is_franchise AND NOT BEST_PLAYERS2.is_franchise
          OR (BEST_PLAYERS.MATCHES_PCT >= BEST_PLAYERS2.MATCHES_PCT + @COMPARE_PCT_DELTA)
          OR (BEST_PLAYERS.dfg_pct + @COMPARE_PCT_DELTA <= BEST_PLAYERS2.dfg_pct)
          OR TRUE
    GROUP BY BEST_PLAYERS.player_team, BEST_PLAYERS.role
    ORDER BY BEST_PLAYERS.PLAYER_TEAM, BEST_PLAYERS.MATCHES_PCT DESC, BEST_PLAYERS.DFG_PCT;
  END //
DELIMITER ;
# CALCULATES BEST PLAYERS FOR EACH TEAM WHERE MORE THAN ONE PLAYER ON A POSITION
CALL each_team_has_many_best_players_on_position;

# FOR EACH TEAM CALCULATES BEST PLAYERS ON A POSITION AND THEIR BEST SUBSTITUTION
DELIMITER //
CREATE PROCEDURE each_team_best_players_on_position_and_sub
  ()
  BEGIN
    SELECT *
    FROM (
           SELECT
             gl.PLAYER_TEAM,
             p.NAME,
             p.SURNAME,
             count(gl.game_date) / TEAMS_MATCHES_AMOUNT.MATCHES_AMOUNT AS MATCHES_PCT,
             di.DFG_PCT,
             p.ROLE,
             ''                                                        AS SUB_NAME,
             ''                                                        AS SUB_SURNAME,
             ''                                                        AS SUB_MATCHES_PCT,
             ''                                                        AS SUB_DFG_PCT
           FROM games_logs gl
             JOIN players p ON gl.player_id = p.id
             JOIN (
                    # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                    SELECT
                      gl.player_team               AS TEAM,
                      count(DISTINCT gl.game_date) AS MATCHES_AMOUNT
                    FROM games_logs gl
                    WHERE gl.season_id = @SEASON_ID
                    GROUP BY gl.player_team
                    # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                  ) AS TEAMS_MATCHES_AMOUNT ON gl.player_team = TEAMS_MATCHES_AMOUNT.TEAM
             JOIN defensive_impacts di ON di.season_id = gl.season_id AND di.player_id = p.id
           WHERE gl.season_id = @SEASON_ID AND @MIN_PLAYED_MIN <= gl.min
           GROUP BY gl.player_team, gl.player_id
           HAVING @MATCHES_PCT <= MATCHES_PCT
           ORDER BY gl.PLAYER_TEAM, MATCHES_PCT DESC
         ) AS BEST_PLAYERS
    GROUP BY BEST_PLAYERS.PLAYER_TEAM, BEST_PLAYERS.ROLE
    HAVING COUNT(MATCHES_PCT) = 1
    UNION
    SELECT
      BEST_PLAYERS.PLAYER_TEAM,
      BEST_PLAYERS.NAME,
      BEST_PLAYERS.SURNAME,
      BEST_PLAYERS.MATCHES_PCT,
      BEST_PLAYERS.DFG_PCT,
      BEST_PLAYERS.ROLE,
      BEST_PLAYERS2.NAME        AS SUB_NAME,
      BEST_PLAYERS2.SURNAME     AS SUB_SURNAME,
      BEST_PLAYERS2.MATCHES_PCT AS SUB_MATCHES_PCT,
      BEST_PLAYERS2.DFG_PCT     AS SUB_DFG_PCT
    FROM (
           SELECT
             gl.PLAYER_TEAM,
             p.id,
             p.NAME,
             p.SURNAME,
             p.IS_FRANCHISE,
             count(gl.game_date) / TEAMS_MATCHES_AMOUNT.MATCHES_AMOUNT AS MATCHES_PCT,
             di.DFG_PCT,
             p.ROLE
           FROM games_logs gl
             JOIN players p ON gl.player_id = p.id
             JOIN (
                    # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                    SELECT
                      gl.player_team               AS TEAM,
                      count(DISTINCT gl.game_date) AS MATCHES_AMOUNT
                    FROM games_logs gl
                    WHERE gl.season_id = @SEASON_ID
                    GROUP BY gl.player_team
                    # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                  ) AS TEAMS_MATCHES_AMOUNT ON gl.player_team = TEAMS_MATCHES_AMOUNT.TEAM
             JOIN defensive_impacts di ON di.season_id = gl.season_id AND di.player_id = p.id
           WHERE gl.season_id = @SEASON_ID AND @MIN_PLAYED_MIN <= gl.min
           GROUP BY gl.player_team, gl.player_id
           HAVING @MATCHES_PCT <= MATCHES_PCT
           ORDER BY gl.PLAYER_TEAM, MATCHES_PCT DESC
         ) AS BEST_PLAYERS
      JOIN
      (
        SELECT
          gl.PLAYER_TEAM,
          p.id,
          p.NAME,
          p.SURNAME,
          p.IS_FRANCHISE,
          count(gl.game_date) / TEAMS_MATCHES_AMOUNT.MATCHES_AMOUNT AS MATCHES_PCT,
          di.DFG_PCT,
          p.ROLE
        FROM games_logs gl
          JOIN players p ON gl.player_id = p.id
          JOIN (
                 # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                 SELECT
                   gl.player_team               AS TEAM,
                   count(DISTINCT gl.game_date) AS MATCHES_AMOUNT
                 FROM games_logs gl
                 WHERE gl.season_id = @SEASON_ID
                 GROUP BY gl.player_team
                 # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
               ) AS TEAMS_MATCHES_AMOUNT ON gl.player_team = TEAMS_MATCHES_AMOUNT.TEAM
          JOIN defensive_impacts di ON di.season_id = gl.season_id AND di.player_id = p.id
        WHERE gl.season_id = @SEASON_ID AND @MIN_PLAYED_MIN <= gl.min
        GROUP BY gl.player_team, gl.player_id
        HAVING @MATCHES_PCT <= MATCHES_PCT
        ORDER BY gl.PLAYER_TEAM, MATCHES_PCT DESC
      ) AS BEST_PLAYERS2
        ON BEST_PLAYERS.player_team = BEST_PLAYERS2.player_team AND BEST_PLAYERS.id <> BEST_PLAYERS2.id AND
           BEST_PLAYERS.role = BEST_PLAYERS2.role
    WHERE BEST_PLAYERS.is_franchise AND NOT BEST_PLAYERS2.is_franchise
          OR (BEST_PLAYERS.MATCHES_PCT >= BEST_PLAYERS2.MATCHES_PCT + @COMPARE_PCT_DELTA)
          OR (BEST_PLAYERS.dfg_pct + @COMPARE_PCT_DELTA <= BEST_PLAYERS2.dfg_pct)
          OR TRUE
    GROUP BY BEST_PLAYERS.player_team, BEST_PLAYERS.role
    ORDER BY player_team, MATCHES_PCT DESC, dfg_pct;
  END //
DELIMITER ;
# FOR EACH TEAM CALCULATES BEST PLAYERS ON A POSITION AND THEIR BEST SUBSTITUTION
CALL each_team_best_players_on_position_and_sub;

# FINDS MATCHED PLAYERS BY POINTS UNDER CRITERIA
DELIMITER //
CREATE PROCEDURE each_team_best_players_with_under_pts
  ()
  BEGIN
    SELECT
      BEST_PLAYERS.PLAYER_TEAM,
      BEST_PLAYERS.NAME,
      BEST_PLAYERS.SURNAME,
      BEST_PLAYERS.MATCHES_PCT,
      PTS_STATS.AVG_PTS,
      count(gl.game_date) / PTS_STATS.MATCHES_AMOUNT AS U_PCT
    FROM (SELECT
            gl.PLAYER_TEAM,
            p.id,
            p.NAME,
            p.SURNAME,
            p.ROLE,
            count(gl.game_date)                                          MATCHES_AMOUNT,
            count(gl.game_date) / TEAMS_MATCHES_AMOUNT.MATCHES_AMOUNT AS MATCHES_PCT,
            AVG(gl.pts)                                               AS AVG_PTS
          FROM games_logs gl
            JOIN players p ON gl.player_id = p.id
            JOIN (
                   # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                   SELECT
                     gl.player_team               AS TEAM,
                     count(DISTINCT gl.game_date) AS MATCHES_AMOUNT
                   FROM games_logs gl
                   WHERE gl.season_id = @SEASON_ID
                   GROUP BY gl.player_team
                   # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                 ) AS TEAMS_MATCHES_AMOUNT ON gl.player_team = TEAMS_MATCHES_AMOUNT.TEAM
          WHERE gl.season_id = @SEASON_ID AND @MIN_PLAYED_MIN <= gl.min
          GROUP BY gl.player_team, gl.player_id
          HAVING @MATCHES_PCT <= MATCHES_PCT AND @MIN_AVG_PTS <= AVG_PTS) AS BEST_PLAYERS
      JOIN (SELECT
              p.id,
              count(gl.game_date)                                          MATCHES_AMOUNT,
              count(gl.game_date) / TEAMS_MATCHES_AMOUNT.MATCHES_AMOUNT AS MATCHES_PCT,
              AVG(gl.pts)                                               AS AVG_PTS
            FROM games_logs gl
              JOIN players p ON gl.player_id = p.id
              JOIN (
                     # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                     SELECT
                       gl.player_team               AS TEAM,
                       count(DISTINCT gl.game_date) AS MATCHES_AMOUNT
                     FROM games_logs gl
                     WHERE gl.season_id = @SEASON_ID
                     GROUP BY gl.player_team
                     # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                   ) AS TEAMS_MATCHES_AMOUNT ON gl.player_team = TEAMS_MATCHES_AMOUNT.TEAM
            WHERE gl.season_id = @SEASON_ID AND @MIN_PLAYED_MIN <= gl.min
            GROUP BY gl.player_team, gl.player_id
            HAVING @MATCHES_PCT <= MATCHES_PCT AND @MIN_AVG_PTS <= AVG_PTS
            ORDER BY gl.PLAYER_TEAM, MATCHES_PCT DESC) AS PTS_STATS ON PTS_STATS.id = BEST_PLAYERS.id
      JOIN games_logs gl ON gl.player_id = BEST_PLAYERS.id
    WHERE gl.season_id = @SEASON_ID AND @MIN_PLAYED_MIN <= gl.min AND gl.pts < PTS_STATS.AVG_PTS
    GROUP BY BEST_PLAYERS.player_team, BEST_PLAYERS.id
    HAVING @PTS_UNDER_MIN_AMOUNT <= U_PCT
    ORDER BY BEST_PLAYERS.PLAYER_TEAM, MATCHES_PCT DESC, U_PCT DESC;
  END //
DELIMITER ;
# FINDS MATCHED PLAYERS BY POINTS UNDER CRITERIA
CALL each_team_best_players_with_under_pts;

SET @SEASON_ID = 22016, @MIN_PLAYED_MIN = 16, @MATCHES_PCT = 0.45, @COMPARE_PCT_DELTA = 0.04, @MAX_DFG_PCT = 0.5,
@MIN_AVG_PTS = 10, @PTS_UNDER_MIN_AMOUNT = 0.5, @MATCH_DATE = CURDATE();
DROP PROCEDURE under_pts_predictions_for_todays_matches;
# DOES UNDER POINTS PREDICTIONS FOR TODAY'S MATCHES
DELIMITER //
CREATE PROCEDURE under_pts_predictions_for_todays_matches
  ()
  BEGIN
    SELECT
      gs.GAME_DATE,
      gs.HOME_TEAM,
      gs.AWAY_TEAM,
      TARGET_PLAYERS.PLAYER_TEAM,
      TARGET_PLAYERS.NAME,
      TARGET_PLAYERS.SURNAME,
      TARGET_PLAYERS.AVG_PTS,
      TARGET_PLAYERS.U_PCT,
      TARGET_PLAYERS.MATCHES_PCT,
      TARGET_PLAYERS.ROLE AS POSITION,
      #       VSS.PLAYER_TEAM     AS VS_TEAM,
      VSS.NAME            AS VS_NAME,
      VSS.SURNAME         AS VS_SURNAME,
      VSS.MATCHES_PCT     AS VS_MATCHES_PCT,
      VSS.DFG_PCT         AS VS_DFG_PCT,
      VSS.SUB_NAME        AS VS_SUB_NAME,
      VSS.SUB_SURNAME     AS VS_SUB_SURNAME,
      VSS.SUB_MATCHES_PCT AS VS_SUB_MATCHES_PCT,
      VSS.SUB_DFG_PCT     AS VS_SUB_DFG_PCT
    FROM games_schedules gs
      JOIN (SELECT
              BEST_PLAYERS.PLAYER_TEAM,
              BEST_PLAYERS.NAME,
              BEST_PLAYERS.SURNAME,
              BEST_PLAYERS.MATCHES_PCT,
              PTS_STATS.AVG_PTS,
              count(gl.game_date) / PTS_STATS.MATCHES_AMOUNT AS U_PCT,
              BEST_PLAYERS.ROLE
            FROM (SELECT
                    gl.PLAYER_TEAM,
                    p.id,
                    p.NAME,
                    p.SURNAME,
                    p.ROLE,
                    count(gl.game_date)                                          MATCHES_AMOUNT,
                    count(gl.game_date) / TEAMS_MATCHES_AMOUNT.MATCHES_AMOUNT AS MATCHES_PCT,
                    AVG(gl.pts)                                               AS AVG_PTS
                  FROM games_logs gl
                    JOIN players p ON gl.player_id = p.id
                    JOIN (
                           # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                           SELECT
                             gl.player_team               AS TEAM,
                             count(DISTINCT gl.game_date) AS MATCHES_AMOUNT
                           FROM games_logs gl
                           WHERE gl.season_id = @SEASON_ID
                           GROUP BY gl.player_team
                           # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                         ) AS TEAMS_MATCHES_AMOUNT ON gl.player_team = TEAMS_MATCHES_AMOUNT.TEAM
                  WHERE gl.season_id = @SEASON_ID AND @MIN_PLAYED_MIN <= gl.min
                  GROUP BY gl.player_team, gl.player_id
                  HAVING @MATCHES_PCT <= MATCHES_PCT AND @MIN_AVG_PTS <= AVG_PTS
                 ) AS BEST_PLAYERS
              JOIN (SELECT
                      p.id,
                      count(gl.game_date)                                          MATCHES_AMOUNT,
                      count(gl.game_date) / TEAMS_MATCHES_AMOUNT.MATCHES_AMOUNT AS MATCHES_PCT,
                      AVG(gl.pts)                                               AS AVG_PTS
                    FROM games_logs gl
                      JOIN players p ON gl.player_id = p.id
                      JOIN (
                             # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                             SELECT
                               gl.player_team               AS TEAM,
                               count(DISTINCT gl.game_date) AS MATCHES_AMOUNT
                             FROM games_logs gl
                             WHERE gl.season_id = @SEASON_ID
                             GROUP BY gl.player_team
                             # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                           ) AS TEAMS_MATCHES_AMOUNT ON gl.player_team = TEAMS_MATCHES_AMOUNT.TEAM
                    WHERE gl.season_id = @SEASON_ID AND @MIN_PLAYED_MIN <= gl.min
                    GROUP BY gl.player_team, gl.player_id
                    HAVING @MATCHES_PCT <= MATCHES_PCT AND @MIN_AVG_PTS <= AVG_PTS
                    ORDER BY gl.PLAYER_TEAM, MATCHES_PCT DESC) AS PTS_STATS ON PTS_STATS.id = BEST_PLAYERS.id
              JOIN games_logs gl ON gl.player_id = BEST_PLAYERS.id
            WHERE gl.season_id = @SEASON_ID AND @MIN_PLAYED_MIN <= gl.min AND gl.pts < PTS_STATS.AVG_PTS
            GROUP BY BEST_PLAYERS.player_team, BEST_PLAYERS.id
            HAVING @PTS_UNDER_MIN_AMOUNT <= U_PCT
            ORDER BY BEST_PLAYERS.PLAYER_TEAM, MATCHES_PCT DESC, U_PCT DESC) AS TARGET_PLAYERS
        ON TARGET_PLAYERS.player_team = gs.home_team OR TARGET_PLAYERS.player_team = gs.away_team
      JOIN (SELECT *
            FROM (
                   SELECT
                     gl.PLAYER_TEAM,
                     p.NAME,
                     p.SURNAME,
                     count(gl.game_date) / TEAMS_MATCHES_AMOUNT.MATCHES_AMOUNT AS MATCHES_PCT,
                     di.DFG_PCT,
                     p.ROLE,
                     ''                                                        AS SUB_NAME,
                     ''                                                        AS SUB_SURNAME,
                     ''                                                        AS SUB_MATCHES_PCT,
                     ''                                                        AS SUB_DFG_PCT
                   FROM games_logs gl
                     JOIN players p ON gl.player_id = p.id
                     JOIN (
                            # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                            SELECT
                              gl.player_team               AS TEAM,
                              count(DISTINCT gl.game_date) AS MATCHES_AMOUNT
                            FROM games_logs gl
                            WHERE gl.season_id = @SEASON_ID
                            GROUP BY gl.player_team
                            # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                          ) AS TEAMS_MATCHES_AMOUNT ON gl.player_team = TEAMS_MATCHES_AMOUNT.TEAM
                     JOIN defensive_impacts di ON di.season_id = gl.season_id AND di.player_id = p.id
                   WHERE gl.season_id = @SEASON_ID AND @MIN_PLAYED_MIN <= gl.min
                   GROUP BY gl.player_team, gl.player_id
                   HAVING @MATCHES_PCT <= MATCHES_PCT
                   ORDER BY gl.PLAYER_TEAM, MATCHES_PCT DESC
                 ) AS BEST_PLAYERS
            GROUP BY BEST_PLAYERS.PLAYER_TEAM, BEST_PLAYERS.ROLE
            HAVING COUNT(MATCHES_PCT) = 1
            UNION
            SELECT
              BEST_PLAYERS.PLAYER_TEAM,
              BEST_PLAYERS.NAME,
              BEST_PLAYERS.SURNAME,
              BEST_PLAYERS.MATCHES_PCT,
              BEST_PLAYERS.DFG_PCT,
              BEST_PLAYERS.ROLE,
              BEST_PLAYERS2.NAME        AS SUB_NAME,
              BEST_PLAYERS2.SURNAME     AS SUB_SURNAME,
              BEST_PLAYERS2.MATCHES_PCT AS SUB_MATCHES_PCT,
              BEST_PLAYERS2.DFG_PCT     AS SUB_DFG_PCT
            FROM (
                   SELECT
                     gl.PLAYER_TEAM,
                     p.id,
                     p.NAME,
                     p.SURNAME,
                     p.IS_FRANCHISE,
                     count(gl.game_date) / TEAMS_MATCHES_AMOUNT.MATCHES_AMOUNT AS MATCHES_PCT,
                     di.DFG_PCT,
                     p.ROLE
                   FROM games_logs gl
                     JOIN players p ON gl.player_id = p.id
                     JOIN (
                            # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                            SELECT
                              gl.player_team               AS TEAM,
                              count(DISTINCT gl.game_date) AS MATCHES_AMOUNT
                            FROM games_logs gl
                            WHERE gl.season_id = @SEASON_ID
                            GROUP BY gl.player_team
                            # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                          ) AS TEAMS_MATCHES_AMOUNT ON gl.player_team = TEAMS_MATCHES_AMOUNT.TEAM
                     JOIN defensive_impacts di ON di.season_id = gl.season_id AND di.player_id = p.id
                   WHERE gl.season_id = @SEASON_ID AND @MIN_PLAYED_MIN <= gl.min
                   GROUP BY gl.player_team, gl.player_id
                   HAVING @MATCHES_PCT <= MATCHES_PCT
                   ORDER BY gl.PLAYER_TEAM, MATCHES_PCT DESC
                 ) AS BEST_PLAYERS
              JOIN
              (
                SELECT
                  gl.PLAYER_TEAM,
                  p.id,
                  p.NAME,
                  p.SURNAME,
                  p.IS_FRANCHISE,
                  count(gl.game_date) / TEAMS_MATCHES_AMOUNT.MATCHES_AMOUNT AS MATCHES_PCT,
                  di.DFG_PCT,
                  p.ROLE
                FROM games_logs gl
                  JOIN players p ON gl.player_id = p.id
                  JOIN (
                         # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                         SELECT
                           gl.player_team               AS TEAM,
                           count(DISTINCT gl.game_date) AS MATCHES_AMOUNT
                         FROM games_logs gl
                         WHERE gl.season_id = @SEASON_ID
                         GROUP BY gl.player_team
                         # COUNTS MATCHES AMOUNT PLAYED BY EACH TEAM IN @SEASON_ID
                       ) AS TEAMS_MATCHES_AMOUNT ON gl.player_team = TEAMS_MATCHES_AMOUNT.TEAM
                  JOIN defensive_impacts di ON di.season_id = gl.season_id AND di.player_id = p.id
                WHERE gl.season_id = @SEASON_ID AND @MIN_PLAYED_MIN <= gl.min
                GROUP BY gl.player_team, gl.player_id
                HAVING @MATCHES_PCT <= MATCHES_PCT
                ORDER BY gl.PLAYER_TEAM, MATCHES_PCT DESC
              ) AS BEST_PLAYERS2
                ON BEST_PLAYERS.player_team = BEST_PLAYERS2.player_team AND BEST_PLAYERS.id <> BEST_PLAYERS2.id AND
                   BEST_PLAYERS.role = BEST_PLAYERS2.role
            WHERE BEST_PLAYERS.is_franchise AND NOT BEST_PLAYERS2.is_franchise
                  OR (BEST_PLAYERS.MATCHES_PCT >= BEST_PLAYERS2.MATCHES_PCT + @COMPARE_PCT_DELTA)
                  OR (BEST_PLAYERS.dfg_pct + @COMPARE_PCT_DELTA <= BEST_PLAYERS2.dfg_pct)
                  OR TRUE
            GROUP BY BEST_PLAYERS.player_team, BEST_PLAYERS.role
            ORDER BY player_team, MATCHES_PCT DESC, dfg_pct) AS VSS
        ON VSS.player_team = gs.home_team OR VSS.player_team = gs.away_team
    WHERE gs.game_date = CURDATE() AND VSS.player_team <> TARGET_PLAYERS.player_team AND VSS.role = TARGET_PLAYERS.role
          AND (VSS.dfg_pct < @MAX_DFG_PCT OR VSS.SUB_DFG_PCT <> 0 AND VSS.SUB_DFG_PCT < @MAX_DFG_PCT)
    ORDER BY gs.home_team, TARGET_PLAYERS.U_PCT DESC, TARGET_PLAYERS.MATCHES_PCT DESC, VSS.DFG_PCT, VSS.SUB_DFG_PCT;
  END //
DELIMITER ;
# DOES UNDER POINTS PREDICTIONS FOR TODAY'S MATCHES
CALL under_pts_predictions_for_todays_matches;

DROP PROCEDURE team_matches_amount;
DROP PROCEDURE matches_pct_per_player;
DROP PROCEDURE best_players_amount_per_each_team;
DROP PROCEDURE average_value_best_players;
DROP PROCEDURE best_players_grouped_by_roles_amount;
DROP PROCEDURE each_team_has_one_best_player_on_position;
DROP PROCEDURE each_team_has_many_best_players_on_position;
DROP PROCEDURE each_team_best_players_on_position_and_sub;
DROP PROCEDURE each_team_best_players_with_under_pts;
DROP PROCEDURE under_pts_predictions_for_todays_matches;


UPDATE players p
SET p.role = 'PG'
WHERE MOD(p.id, 5) = 0;
UPDATE players p
SET p.role = 'SG'
WHERE MOD(p.id, 5) = 1;
UPDATE players p
SET p.role = 'SF'
WHERE MOD(p.id, 5) = 2;
UPDATE players p
SET p.role = 'PF'
WHERE MOD(p.id, 5) = 3;
UPDATE players p
SET p.role = 'C'
WHERE MOD(p.id, 5) = 4;
SELECT
  p.role,
  count(p.role)
FROM players p
GROUP BY p.role
ORDER BY p.role;

# UPDATE players AS p
#   INNER JOIN (
#                SELECT
#                  gl.player_team,
#                  p.id,
#                  p.name,
#                  p.surname
#                FROM games_logs gl
#                  JOIN players p ON gl.player_id = p.id
#                WHERE season_id = @SEASON_ID
#                GROUP BY gl.player_id
#                ORDER BY gl.player_team
#              ) AS players_teams
#     ON p.id = players_teams.id
# SET p.team = players_teams.player_team
# WHERE p.name = players_teams.name AND p.surname = players_teams.surname

UPDATE players p
SET p.role = p.position

