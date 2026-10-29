SELECT DISTINCT "Solicitudes"."IdSolicitudAporteRescate"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" AS "Solicitudes"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.DetalleCargaSolicitudesAporteRescate" AS "Detalle" ON ("Solicitudes"."IdSolicitudAporteRescate" = "Detalle"."IdSolicitudAporteRescate")
WHERE 
(COALESCE(:IdEmisor,'') = '' OR :IdEmisor = "Solicitudes"."IdEmisor")
 AND (COALESCE(:IdMoneda,'') = '' OR :IdMoneda = "Solicitudes"."IdMoneda")
 AND (COALESCE(:IdSerieFondo,'') = '' OR :IdSerieFondo = "Solicitudes"."IdSerieFondo")
 AND (COALESCE(:Nacional,'N') = 'N' OR :Nacional = "Solicitudes"."EsFondoNacional")
 AND (COALESCE(:TipoSolicitud,'') = '' OR :TipoSolicitud = "Solicitudes"."TipoSolicitud")
 AND (COALESCE(:IdTipoMovimientoFondoMutuo,'') = '' OR :IdTipoMovimientoFondoMutuo = "Solicitudes"."IdTipoMovimientoFondoMutuo")
 AND (COALESCE(:MoneyMarket,'N') = 'N' OR :MoneyMarket = "Solicitudes"."MoneyMarket")
 AND "Solicitudes"."EstadoSolicitud" IN ('ING','ACP')
 AND (:IdCarga = 0 OR ("Detalle"."IdCargaSolicitudesAporteRescate" = :IdCarga AND "Detalle"."EstadoProceso" = 'PEN'))
 AND "Solicitudes"."FechaCertificacion" = :FechaCertificacion
 AND "Solicitudes"."IdEmpresa" = :IdEmpresa;