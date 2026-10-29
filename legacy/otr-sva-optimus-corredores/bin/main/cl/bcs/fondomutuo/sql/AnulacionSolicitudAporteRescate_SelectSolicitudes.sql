SELECT sol."IdSolicitudAporteRescate",sol."IdSolicitudAporteRescateRelacionado",sol."EstadoSolicitud"
,sol."EstadoLiquidacion",sol."FolioCartaLiquidacion",sol."SolicitudBatch"
,sol."IdMedioDePago",sol."TipoSolicitud",sol."IdTipoMovimientoFondoMutuo",sol."FechaCertificacion",
MOV."TieneCertificacion", sol."IdPortafolio", sol."IdMonedaFondo", sol."MontoPago", sol."IdEmpleadoEjecutivo", sol."ParidadFechaCertificacion",
sol."IdInstrumento", sol."IdTipoInstrumento", sol."IdSerieFondo", sol."IdMoneda", sol."MontoPagoLocal", sol."IndicadorMLT", sol."MontoComisionLocal",
sol."NumeroCuotas", sol."IdCliente", sol."IdCartera", sol."IdTipoCanalIngreso", sol."FolioOmnibus"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" sol 
INNER JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS MOV ON sol."IdTipoMovimientoFondoMutuo" = MOV."IdTipoMovimientoFondoMutuo"
WHERE (sol."IdSolicitudAporteRescate" IN (:IdSolicitudAporteRescate) OR sol."IdSolicitudAporteRescateRelacionado" IN (:IdSolicitudAporteRescate)) AND sol."IdEmpresa" = :IdEmpresa
AND sol."EstadoSolicitud" <> 'ANU';