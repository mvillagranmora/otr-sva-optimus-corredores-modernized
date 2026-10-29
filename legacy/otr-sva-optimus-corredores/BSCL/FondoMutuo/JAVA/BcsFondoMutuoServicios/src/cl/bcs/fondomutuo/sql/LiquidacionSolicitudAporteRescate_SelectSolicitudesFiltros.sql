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
WHERE "Solicitudes"."IdEmisor" = (CASE WHEN :IdEmisor <> '' THEN :IdEmisor ELSE "Solicitudes"."IdEmisor" END)
AND "Solicitudes"."TipoSolicitud" = (CASE WHEN :TipoSolicitud <> '' THEN :TipoSolicitud ELSE "Solicitudes"."TipoSolicitud" END)
AND (("Solicitudes"."EstadoSolicitud" = (CASE WHEN :IdEstado <> '' THEN :IdEstado ELSE "Solicitudes"."EstadoSolicitud" END) AND "Solicitudes"."TipoSolicitud" = 'APO') OR ("Solicitudes"."TipoSolicitud" = 'RES' AND "Solicitudes"."EstadoSolicitud" = 'CRT'))
AND "Solicitudes"."FechaLiquidacion" = (CASE WHEN :FechaLiquidacion <> '' THEN :FechaLiquidacion ELSE "Solicitudes"."FechaLiquidacion" END)
AND "Solicitudes"."FechaCertificacion" = (CASE WHEN :FechaCertificacion <> '' THEN :FechaCertificacion ELSE "Solicitudes"."FechaCertificacion" END)
AND "Solicitudes"."IdSerieFondo"  = (CASE WHEN :IdSerieFondo <> '' THEN :IdSerieFondo ELSE "Solicitudes"."IdSerieFondo" END)
AND "Solicitudes"."EstadoLiquidacion" = 'ING'
AND "Solicitudes"."IdTipoMovimientoFondoMutuo" IN ('APONORM','RESNORM','APOREINV','RESREINV','APORESPRO','APOOVER','RESTRAPOR','RESTRASP','APOTRASP','APODIVCFI','RESCNN')
AND "Solicitudes"."IdEmpresa" = :p_IdEmpresa;