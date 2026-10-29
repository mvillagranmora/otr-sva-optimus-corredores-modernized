SELECT 
    TO_VARCHAR(
        ADD_SECONDS(
            TO_TIMESTAMP(:p_FechaSistema || ' 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
            SECONDS_BETWEEN(
                UTCTOLOCAL(:p_FechaSistema || ' 00:00:00', 'America/Santiago', 'platform'),
                :p_FechaSistema || ' 00:00:00'
            )
        ),
        'YYYY-MM-DD HH24:MI:SS'
    ) AS "Fecha_Inicio",
    TO_VARCHAR(
        ADD_SECONDS(
            ADD_DAYS(
                TO_TIMESTAMP(:p_FechaSistema || ' 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
                1
            ),
            SECONDS_BETWEEN(
                UTCTOLOCAL(:p_FechaSistema || ' 00:00:00', 'America/Santiago', 'platform'),
                :p_FechaSistema || ' 00:00:00'
            ) - 60
        ),
        'YYYY-MM-DD HH24:MI:SS'
    ) AS "Fecha_Fin"
FROM DUMMY;