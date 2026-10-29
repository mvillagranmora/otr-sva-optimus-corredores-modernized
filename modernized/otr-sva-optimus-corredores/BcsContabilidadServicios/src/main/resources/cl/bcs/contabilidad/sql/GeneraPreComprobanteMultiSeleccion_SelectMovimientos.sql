SELECT
MOV."IdMovimiento"
,CT."IdTipoCentralizacion"
,MOV."IdTipoMovimiento"
,MOV."IdEmpresa"
,MOV."Numero"
,MOV."Linea"
,MOV."Fecha"
,MOV."DocumentoIdentidad"
,MOV."Cuenta"
,MOV."Ejercicio"
,MOV."Periodo"
,MOV."IdCentralizacion"
,MOV."Monto"
,MOV."Estado"
,MOV."IdMoneda"
FROM "BSCL.Contabilidad.BD::ITZ.Movimiento" MOV
INNER JOIN "BSCL.Contabilidad.BD::ITZ.Centralizacion" C ON (C."IdCentralizacion" = MOV."IdCentralizacion")
INNER JOIN "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoCentralizacionTipo" CT ON (CT."IdTipoCentralizacion" = C."IdTipoCentralizacion")
WHERE CT."IdTipoContabilidad" IN (:IdTipoContabilidad) AND C."FechaContable" = :FechaContable AND C."IdEmpresa" = :IdEmpresa AND C."Estado" = 'V'