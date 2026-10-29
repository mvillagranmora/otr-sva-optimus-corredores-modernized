SET SCHEMA BSCL;

DROP VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarEsquemaHabil";

CREATE VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarEsquemaHabil" AS
SELECT 
    EsquemaHabil."Esquema",
    EsquemaHabil."Version",
    EsquemaHabil."FechaDesde",
    EsquemaHabil."Fechas" AS "FechaHasta"
FROM (
    SELECT 
        RANK() OVER (
            PARTITION BY DIAESQUEMA."Esquema", DIAESQUEMA."Version"
            ORDER BY DIAESQUEMA."Fechas" DESC
        ) AS "RankNum",
        DIAESQUEMA."Esquema",
        DIAESQUEMA."Version",
        DIAESQUEMA."FechaDatosDesde" AS "FechaDesde",
        DIAESQUEMA."Fechas",
        COALESCE(FERIADO."Tipo", 'HABIL') AS "TipoDia"
    FROM (
        SELECT 
            ESQ."FechaDatosDesde",
            ADD_DAYS(TO_DATE(ESQ."FechaDatosDesde", 'YYYY-MM-DD'), n - 1) AS "Fechas",
            ESQ."Esquema",
            ESQ."Version"
        FROM "BSCL.Normativa.BD::DOC.Esquema" ESQ
        CROSS JOIN (
            SELECT 1 AS n FROM DUMMY
            UNION ALL SELECT 2 FROM DUMMY
            UNION ALL SELECT 3 FROM DUMMY
            UNION ALL SELECT 4 FROM DUMMY
            UNION ALL SELECT 5 FROM DUMMY
            UNION ALL SELECT 6 FROM DUMMY
            UNION ALL SELECT 7 FROM DUMMY
            UNION ALL SELECT 8 FROM DUMMY
            UNION ALL SELECT 9 FROM DUMMY
            UNION ALL SELECT 10 FROM DUMMY
            UNION ALL SELECT 11 FROM DUMMY
            UNION ALL SELECT 12 FROM DUMMY
            UNION ALL SELECT 13 FROM DUMMY
            UNION ALL SELECT 14 FROM DUMMY
            UNION ALL SELECT 15 FROM DUMMY
            UNION ALL SELECT 16 FROM DUMMY
            UNION ALL SELECT 17 FROM DUMMY
            UNION ALL SELECT 18 FROM DUMMY
            UNION ALL SELECT 19 FROM DUMMY
            UNION ALL SELECT 20 FROM DUMMY
            UNION ALL SELECT 21 FROM DUMMY
            UNION ALL SELECT 22 FROM DUMMY
            UNION ALL SELECT 23 FROM DUMMY
            UNION ALL SELECT 24 FROM DUMMY
            UNION ALL SELECT 25 FROM DUMMY
            UNION ALL SELECT 26 FROM DUMMY
            UNION ALL SELECT 27 FROM DUMMY
            UNION ALL SELECT 28 FROM DUMMY
            UNION ALL SELECT 29 FROM DUMMY
            UNION ALL SELECT 30 FROM DUMMY
            UNION ALL SELECT 31 FROM DUMMY
        ) AS DIAS
    ) DIAESQUEMA
    LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.DiaFeriado" FERIADO 
        ON (
            (
                ("Tipo" = 'FERIADO' AND FERIADO."Fecha" = DIAESQUEMA."Fechas") 
                OR ("Tipo" = 'INHABIL' AND FERIADO."Dia" = WEEKDAY(TO_DATE(DIAESQUEMA."Fechas", 'YYYY-MM-DD')))
            )
            AND FERIADO."AgnoCarga" = YEAR(DIAESQUEMA."Fechas")
            AND FERIADO."IdPais" = (
                SELECT P."IdPais"
                FROM "BSCL.Plataforma.BD::REG.Pais" P
                WHERE P."CodigoPais" = 'CL'
            )
        )
    WHERE 
        MONTH(DIAESQUEMA."Fechas") = MONTH(DIAESQUEMA."FechaDatosDesde")
        AND COALESCE(FERIADO."Tipo", 'HABIL') = 'HABIL'
) EsquemaHabil
WHERE EsquemaHabil."RankNum" = 1;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::DOC.ConsultarEsquemaHabil" TO BCS_DESARROLLADOR WITH GRANT OPTION;
