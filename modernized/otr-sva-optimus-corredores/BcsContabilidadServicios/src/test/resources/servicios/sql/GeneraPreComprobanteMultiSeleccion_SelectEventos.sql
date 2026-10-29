SELECT
EVT."IdEvento"
,EVT."IdEmpresa"
,EVT."IdTipoEvento"
,EVT."IdCentralizacion"
,EVT."IdMovimiento"
,EVT."FechaContable"
,EVT."Monto"
,EVT."CodigoMoneda"
,EVT."CodigoUsuario"
,EVT."FechaProceso"
,EVT."HoraProceso"
,EVT."Estado"
FROM "BSCL.Contabilidad.BD::ITZ.Evento" EVT
INNER JOIN "BSCL.Contabilidad.BD::ITZ.Movimiento" MOV ON (EVT."IdMovimiento" = MOV."IdMovimiento")
INNER JOIN "BSCL.Contabilidad.BD::ITZ.Centralizacion" C ON (C."IdCentralizacion" = MOV."IdCentralizacion")
WHERE C."FechaContable" = :FechaContable AND C."IdEmpresa" = :IdEmpresa AND C."Estado" = 'V'
AND EXISTS (SELECT 1 FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoCentralizacionTipo" CT WHERE CT."IdTipoCentralizacion" = C."IdTipoCentralizacion" AND CT."IdTipoContabilidad" IN (:IdTipoContabilidad))
AND EVT."Monto" != 0