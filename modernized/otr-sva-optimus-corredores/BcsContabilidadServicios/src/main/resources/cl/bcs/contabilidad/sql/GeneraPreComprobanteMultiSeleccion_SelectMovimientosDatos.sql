SELECT
MOVD."IdMovimiento"
,COALESCE(EVT."TipoDato",'V') AS "TipoDato"
,MOVD."IdTipoDatoEvento"
,MOVD."ValorVarchar"
,MOVD."ValorDecimal"
,MOVD."Estado"
,MOVD."TipoEvento"
FROM "BSCL.Contabilidad.BD::ITZ.MovimientoDato" MOVD
INNER JOIN "BSCL.Contabilidad.BD::ITZ.Movimiento" MOV ON (MOVD."IdMovimiento" = MOV."IdMovimiento")
INNER JOIN "BSCL.Contabilidad.BD::ITZ.Centralizacion" C ON (C."IdCentralizacion" = MOV."IdCentralizacion")
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" EVT ON EVT."IdTipoDatoEvento" = MOVD."IdTipoDatoEvento"
WHERE C."FechaContable" = :FechaContable AND C."IdEmpresa" = :IdEmpresa AND C."Estado" = 'V'
AND EXISTS (SELECT 1 FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoCentralizacionTipo" CT WHERE CT."IdTipoCentralizacion" = C."IdTipoCentralizacion" AND CT."IdTipoContabilidad" IN (:IdTipoContabilidad))

