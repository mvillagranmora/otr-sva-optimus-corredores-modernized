
SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1922.PresenciaBursatil";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1922.PresenciaBursatil" (
    "NEMO"
    ,"FEC_DESDE"
    ,"FEC_HASTA"

) AS
SELECT DISTINCT
    "NEMO",
    "FEC_INI_MKT"  AS "FEC_DESDE",
    "FEC_TERM_MKT" AS "FEC_HASTA"
FROM (
    SELECT DISTINCT
        "NEMO",
        "FEC_INI_MKT",
        "FEC_TERM_MKT"
    FROM "TBRELMTK"
    WHERE "ESTADO" = 'V'
)

UNION ALL

-- JHI SOLO si no está contenido en MTK
SELECT DISTINCT
    PA."NEMO",
    PA."FECHA" AS "FEC_DESDE",
    PA."FECHA" AS "FEC_HASTA"
FROM "TBPRAJHI" PA
WHERE PA."PRESEN_AJU" >= 25
  AND PA."BOLSA" = 'TT'
  AND NOT EXISTS (
        SELECT 1
        FROM "TBRELMTK" TK
        WHERE TK."ESTADO" = 'V'
          AND TK."NEMO" = PA."NEMO"
          AND PA."FECHA" BETWEEN TK."FEC_INI_MKT"
                           AND TK."FEC_TERM_MKT"
  )
  
WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1922.PresenciaBursatil" TO bcs_desarrollador WITH GRANT OPTION;
