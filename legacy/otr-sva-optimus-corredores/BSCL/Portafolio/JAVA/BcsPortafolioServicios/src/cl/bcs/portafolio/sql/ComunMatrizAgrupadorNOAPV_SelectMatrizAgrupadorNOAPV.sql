SELECT
NOAPV."IdMatrizAgrupadorNOAPV"
,NOAPV."CodigoPortafolio"
,NOAPV."EsADC"
FROM "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" NOAPV
WHERE NOAPV."CodigoPortafolio" = :CodigoPortafolio OR NOAPV."IdMatrizAgrupadorNOAPV" IN (:IdMatrizAgrupadorNOAPV);