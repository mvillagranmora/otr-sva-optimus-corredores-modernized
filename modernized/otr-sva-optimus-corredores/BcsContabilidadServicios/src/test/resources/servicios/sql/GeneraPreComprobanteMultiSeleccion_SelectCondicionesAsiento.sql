SELECT
CON."IdCondicion"
,CON."IdDefinicionAsiento"
,CON."IdTipoContabilidad"
,CON."IdTipoDatoEvento"
,CON."ValorVarchar"
,CON."ValorDecimal"
,CON."Condicion"
,CON."Conector"
,COALESCE(EVT."TipoDato",'V') AS "TipoDato"
,CON."TipoCondicion"
,CON."Estado"
FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoAsientoCondicion" CON
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" EVT ON EVT."IdTipoDatoEvento" = CON."IdTipoDatoEvento"
WHERE CON."IdTipoContabilidad" IN (:IdTipoContabilidad) AND CON."IdDefinicionAsiento" = 0