SELECT
"Solicitudes"."IdSolicitudAporteRescate",
"Solicitudes"."TipoSolicitud",
"Solicitudes"."IdSerieFondo",
"Solicitudes"."IdPortafolio",
"Solicitudes"."IdMoneda",
"Solicitudes"."CostoMonedaLocal",
"Solicitudes"."Monto",
"Solicitudes"."FechaLiquidacion",
"Solicitudes"."FechaCertificacion",
"Solicitudes"."IdMedioDePago",
"Solicitudes"."EstadoLiquidacion",
"Solicitudes"."MontoPago",
"Solicitudes"."MontoComision",
"Solicitudes"."MontoPagoLocal",
"Solicitudes"."MontoComisionLocal",
"Solicitudes"."CodigoPortafolio",
"Solicitudes"."IdTipoMovimientoFondoMutuo"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" AS "Solicitudes"
WHERE "Solicitudes"."IdEmpresa" = :IdEmpresa
AND "Solicitudes"."IdSolicitudAporteRescate" IN (:IdSolicitudAporteRescate)
AND "Solicitudes"."IdTipoMovimientoFondoMutuo" = 'RESNORM';