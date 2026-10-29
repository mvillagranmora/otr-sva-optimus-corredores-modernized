SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.FondoMutuo.BD.Vistas::TRA.ConsultarTraspasoDetalleOrigenUI";
GO
CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::TRA.ConsultarTraspasoDetalleOrigenUI"
AS
SELECT 
	 ORIGEN."IdDetalleTraspaso"
	,ORIGEN."IdTraspaso"
	,ORIGEN."IdSerieFondo"
	,ORIGEN."Cantidad"
	,ORIGEN."IdEstado"
	,ORIGEN."FechaIngreso"
    ,ORIGEN."FechaModificacion"
    ,ORIGEN."FechaIngresoRegistro"
    ,ORIGEN."FechaModificacionRegistro"
    ,ORIGEN."IdRegimenTributario"
	,COALESCE(ESTADO."Nombre",'')			 				AS "Estado"
	,COALESCE(SERIEFONDO."NombreSerieFondo",'')				AS "NombreSerieFondo"
	,COALESCE(SERIEFONDO."CodigoSerie",'')					AS "CodigoSerie"
	,COALESCE(SERIEFONDO."DecimalesValorCuota",0)			AS "DecimalesValorCuota"
	,COALESCE(SERIEFONDO."DecimalesCuota",0)				AS "DecimalesCuota"
	
	,COALESCE(SOLICITUD."IdSolicitudAporteRescate",0)		AS "IdSolicitudAporteRescate"
	,COALESCE(SOLICITUD."IdTipoMovimientoFondoMutuo",'')	AS "IdTipoMovimientoFondoMutuo"
	,COALESCE(SOLICITUD."FechaCertificacion",'')			AS "FechaCertificacion"
	,COALESCE(SOLICITUD."FechaLiquidacion",'')				AS "FechaLiquidacion"
	,COALESCE(SOLICITUD."IdMoneda",'')						AS "IdMoneda"
	,COALESCE(SOLICITUD."Monto",0)							AS "Monto"
	,COALESCE(SOLICITUD."NumeroCuotas",0)					AS "NumeroCuotas"
	,COALESCE(SOLICITUD."ValorCuota",0)						AS "ValorCuota"
	,COALESCE(SOLICITUD."CostoUF",0)						AS "CostoUF"
	
	,COALESCE(SOLICITUD."IdMonedaSistema",'')				AS "IdMonedaSistema"
	,COALESCE(SOLICITUD."MontoMonedaSistema",0)				AS "MontoMonedaSistema"
	,COALESCE(SOLICITUD."Estado",'')						AS "EstadoSolicitud"
	,COALESCE(SOLICITUD."EstadoLiquidacion",'')				AS "EstadoLiquidacion"
	,COALESCE(ESTADOSOL."Nombre",'')						AS "NombreEstadoSolicitud"
	,COALESCE(ESTADOLIQ."Nombre",'')						AS "NombreEstadoLiquidacion"
	,COALESCE(TIPOMOV."Descripcion",'')						AS "DescripcionTipoMovimiento"
	
	
	,COALESCE(MOVTESO."IdEstado",'') 						AS "IdEstadoIngresoEgreso"
	,COALESCE(MOVTESO."IdIngresoEgreso",0) 		 			AS "IdIngresoEgreso"
	,COALESCE(MOVTESO."FechaMovimiento",'') 				AS "FechaIngresoEgreso"
	,COALESCE(TIPOTES."Nombre",'') 							AS "TipoMovimientoIngresoEgreso"
	,COALESCE(ESTADOTES."Nombre",'')						AS "NombreEstadoIngresoEgreso"
	
	
	
	,COALESCE(EMISOR."NombreCorto",'')   		AS "NombreCortoEmisor"
	,COALESCE(EMISOR."NombreCompleto",'')   	AS "NombreCompletoEmisor"
	
	,COALESCE(EMISOR."DocumentoIdentidad",'') 	AS "DocumentoIdentidadEmisor"
	,COALESCE(REGIMEN."Descripcion",'') 		AS "RegimenTributario"
	,COALESCE(FONDO."IdMoneda", 'CLP') AS "IdMonedaFondo"
	
FROM "BSCL.FondoMutuo.BD::TRA.TraspasoDetalleOrigen" 					AS ORIGEN
LEFT OUTER  JOIN "BSCL.Plataforma.BD::PLA.Estado" 						AS ESTADO		ON  ( ESTADO."IdEstado" 					=  ORIGEN."IdEstado")
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" 				AS SERIEFONDO   ON  ( SERIEFONDO."IdSerieFondo" 			=  ORIGEN."IdSerieFondo")
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"   	AS SOLICITUD 	ON  ( SOLICITUD."IdSolicitudAporteRescate" 	=  ORIGEN."IdSolicitudAporteRescate")
LEFT OUTER  JOIN "BSCL.Plataforma.BD::PLA.Estado" 						AS ESTADOSOL	ON  ( ESTADOSOL."IdEstado" 					=  SOLICITUD."Estado")
LEFT OUTER  JOIN "BSCL.Plataforma.BD::PLA.Estado" 						AS ESTADOLIQ	ON  ( ESTADOLIQ."IdEstado" 					=  SOLICITUD."EstadoLiquidacion")
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" 	AS TIPOMOV 		ON  ( TIPOMOV."IdTipoMovimientoFondoMutuo"  =  SOLICITUD."IdTipoMovimientoFondoMutuo")
LEFT OUTER  JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" 				AS MOVTESO 		ON  ( SOLICITUD."IdIngresoEgreso"			=  MOVTESO."IdIngresoEgreso")
LEFT OUTER  JOIN "BSCL.Plataforma.BD::PLA.TipoMovimiento" 				AS TIPOTES		ON  ( TIPOTES."IdTipoMovimiento" 			=  MOVTESO."IdTipoMovimiento" AND TIPOTES."IdModulo"='TES')
LEFT OUTER  JOIN "BSCL.Plataforma.BD::PLA.Estado" 						AS ESTADOTES 	ON  ( ESTADOTES."IdEstado" 					=  MOVTESO."IdEstado")




LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" 						AS FONDO 		ON  ( FONDO."IdFondo" 						=  SERIEFONDO."IdFondo")
LEFT OUTER  JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor"		AS EMISOR		ON  ( EMISOR."IdEmisor" 					=  FONDO."IdEmisor")
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" 			AS REGIMEN 		ON  ( REGIMEN."IdRegimenFondoMutuo" 		=  ORIGEN."IdRegimenTributario");


GO
GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::TRA.ConsultarTraspasoDetalleOrigenUI" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO