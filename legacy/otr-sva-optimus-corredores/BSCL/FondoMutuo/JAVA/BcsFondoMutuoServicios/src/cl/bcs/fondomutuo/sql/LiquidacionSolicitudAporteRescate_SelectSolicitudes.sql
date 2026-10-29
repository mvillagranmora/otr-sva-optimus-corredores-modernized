SELECT
"Solicitudes"."IdSolicitudAporteRescate",
"Solicitudes"."TipoSolicitud",
"Solicitudes"."IdSerieFondo",
"Solicitudes"."IdInstrumento",
"Solicitudes"."IdTipoInstrumento",
"Solicitudes"."IdPortafolio",
"Solicitudes"."IdMoneda",
"Solicitudes"."IdMonedaFondo",
"Solicitudes"."CostoMonedaLocal",
"Solicitudes"."Monto",
"Solicitudes"."ParidadFechaCertificacion",
"Solicitudes"."FechaLiquidacion",
"Solicitudes"."FechaCertificacion",
"Solicitudes"."IdUsuario",
"Solicitudes"."IdPortafolioAGF",
"Solicitudes"."IdMedioDePago",
"Solicitudes"."IdPersonaAGF",
"Solicitudes"."IdMedioDePagoAbonoAGF",
"Solicitudes"."IdMedioDePagoCargoAGF",
"Solicitudes"."EstadoLiquidacion",
"Solicitudes"."MontoPago",
"Solicitudes"."MontoComision",
"Solicitudes"."MontoPagoLocal",
"Solicitudes"."MontoComisionLocal",
"Solicitudes"."IdOmnibus",
"Solicitudes"."IdPersona",
"Solicitudes"."CodigoPortafolio",
"Solicitudes"."IdTipoMovimientoFondoMutuo",
"Solicitudes"."IdMatrizAgrupadorAPV"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" AS "Solicitudes"
WHERE "Solicitudes"."IdEmpresa" = :p_IdEmpresa
AND "Solicitudes"."IdSolicitudAporteRescate" IN (:p_Movimientos)
AND "Solicitudes"."EstadoLiquidacion" = 'ING'
AND "Solicitudes"."IdTipoMovimientoFondoMutuo" IN ('APONORM','RESNORM','APOREINV','RESREINV','APORESPRO','APOOVER','RESTRAPOR','RESTRASP','APOTRASP', 'APODIVCFI', 'RESCNN');