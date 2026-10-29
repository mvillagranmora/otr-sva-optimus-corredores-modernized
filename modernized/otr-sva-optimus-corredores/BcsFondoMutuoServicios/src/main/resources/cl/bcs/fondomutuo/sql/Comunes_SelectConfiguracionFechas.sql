SELECT 
"IdConfiguracionFechasFondoMutuo",
"IdEmpresa",
"IdModalidad",
"IdTipoFecha",
"TipoSolicitud",
"EstadoFondo",
"FondoExtranjero",
"Descripcion",
"CantidadDias",
"EsHabil",
"IdTipoFechaOrigen",
"DebeSerHabilFechaOrigen",
"ValutaCurse",
"ValutaValorCuota",
"ValutaCertificacion",
"ValutaRetencion",
"CalendarioLocal",
"CalendarioEmisor",
"CalendarioFondo",
"CalendarioMoneda",
"IdEstado",
FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionFechasFondoMutuo"
WHERE (:IdModalidad = "" AND ((c."IdModalidad" = 'UNITARIO' AND "SolicitudBatch" = 'N') OR (c."IdModalidad" = 'BATCH' AND "SolicitudBatch" = 'S')) OR c."IdModalidad" = :IdModalidad)
AND c."IdEmpresa" = :IdEmpresa
AND c."IdEstado" = 'ACT'