SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.FondoMutuo.BD.Vistas::TRA.ConsultarTraspasoDetalleDestinoUI";
GO
CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::TRA.ConsultarTraspasoDetalleDestinoUI"
AS
SELECT

	 DESTINO."IdDetalleTraspaso"
	,DESTINO."IdTraspaso"
	,DESTINO."IdSerieFondo"
	,DESTINO."Porcentaje"
	,DESTINO."IdEstado"
    ,DESTINO."FechaIngreso"
    ,DESTINO."FechaModificacion"
    ,DESTINO."FechaIngresoRegistro"
    ,DESTINO."FechaModificacionRegistro"
    ,DESTINO."IdRegimenFondoMutuo"
	,COALESCE(ESTADO."Nombre",'')			 		AS "Estado"
	,COALESCE(SERIEFONDO."NombreSerieFondo",'')		AS "NombreSerieFondo"
	,COALESCE(SERIEFONDO."CodigoSerie",'')			AS "CodigoSerie"
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
	
	
	,COALESCE(EMISOR."NombreCorto",'')   		AS "NombreCortoEmisor"
	,COALESCE(EMISOR."DocumentoIdentidad",'') 	AS "DocumentoIdentidadEmisor"
FROM "BSCL.FondoMutuo.BD::TRA.TraspasoDetalleDestino" AS DESTINO
LEFT OUTER  JOIN "BSCL.Plataforma.BD::PLA.Estado" 						AS ESTADO			ON  ( ESTADO."IdEstado" 		=  DESTINO."IdEstado")
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" 				AS SERIEFONDO   	ON  ( SERIEFONDO."IdSerieFondo" =  DESTINO."IdSerieFondo")
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"   	AS SOLICITUD 		ON  ( SOLICITUD."IdSolicitudAporteRescate" 	=  DESTINO."IdSolicitudAporteRescate")
LEFT OUTER  JOIN "BSCL.Plataforma.BD::PLA.Estado" 						AS ESTADOSOL		ON  ( ESTADOSOL."IdEstado" 					=  SOLICITUD."Estado")
LEFT OUTER  JOIN "BSCL.Plataforma.BD::PLA.Estado" 						AS ESTADOLIQ		ON  ( ESTADOLIQ."IdEstado" 					=  SOLICITUD."EstadoLiquidacion")
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" 	AS TIPOMOV 			ON  ( TIPOMOV."IdTipoMovimientoFondoMutuo"  =  SOLICITUD."IdTipoMovimientoFondoMutuo")

LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" 						AS FONDO 			ON  ( FONDO."IdFondo" 			=  SERIEFONDO."IdFondo")
LEFT OUTER  JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor"		AS EMISOR			ON  ( EMISOR."IdEmisor" =  FONDO."IdEmisor");
GO
GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::TRA.ConsultarTraspasoDetalleDestinoUI" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO