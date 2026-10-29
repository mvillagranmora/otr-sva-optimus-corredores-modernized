SELECT
C."IdTipoDatoEvento"
,C."ValorVarchar"
,C."ValorDecimal"
,C."Estado"
FROM "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoValor" C
INNER JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" E ON C."IdTipoDatoEvento" = E."IdTipoDatoEvento"
INNER JOIN "BSCL.Contabilidad.BD::GBL.CentralizaGenerico_ProcesoDatoValor_Paso" P ON (C."IdTipoDatoEvento" = P."TipoDato" AND (C."ValorVarchar" = P."ValorVarChar" OR C."ValorDecimal" = P."ValorDecimal"))
WHERE E."DatosPreDefinidos" = 'S'