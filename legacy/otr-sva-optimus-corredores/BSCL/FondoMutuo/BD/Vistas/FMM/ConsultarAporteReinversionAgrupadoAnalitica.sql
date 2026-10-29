SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarAporteReinversionAgrupadoAnalitica";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarAporteReinversionAgrupadoAnalitica"
AS
SELECT
	SolicitudAporteRescate."IdSolicitudAporteRescate"
	,SolicitudAporteRescate."IdAgrupador"
	,SolicitudAporteRescate."IdSerieFondo"
	,SolicitudAporteRescate."IdMoneda"
	,SolicitudAporteRescate."Monto" AS "Monto" 
	,CASE WHEN COALESCE(Spot."IdOperacionSpotOrigen",0) > 0 THEN Spot."MontoMonedaSecundaria" ELSE SolicitudAporteRescate."MontoMonedaSistema" END AS "MontoMonedaSistema"
	,SolicitudAporteRescate."NumeroCuotas" AS "NumeroCuotas"
	,SolicitudAporteRescate."FechaCertificacion"
	,SolicitudAporteRescate."FechaLiquidacion"
	,SolicitudAporteRescate."FechaValorCuota"
	,SolicitudAporteRescate."FechaAceptacion"
	,SolicitudAporteRescate."FechaEmision"
	,SolicitudAporteRescate."ValorCuota"
	,SolicitudAporteRescate."IdRegimenTributario"
	,SolicitudAporteRescate."IdCliente"
	,SolicitudAporteRescate."IdPortafolio"
	,SolicitudAporteRescate."IdTipoMovimientoFondoMutuo"
	,SolicitudAporteRescate."IdEmpresa"
	,SolicitudAporteRescate."TipoSolicitud"
	,SolicitudAporteRescate."FolioContratoOperacion"
	,SolicitudAporteRescate."TipoContratoOperacion"
	,SolicitudAporteRescate."Estado"
	,doc."DocumentoIdentidad"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS SolicitudAporteRescate
LEFT JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" Spot ON Spot."IdOperacionSpotOrigen"= SolicitudAporteRescate."IdOperacionSpot" AND Spot."Estado" != 'MOD'
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" cli ON cli."IdCliente" = SolicitudAporteRescate."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" doc ON doc."IdPersona" = cli."IdPersona" and doc."Predeterminado" = 'S'
WHERE "IdTipoMovimientoFondoMutuo" = 'APOREINV' AND "IdAgrupador" != 0;

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarAporteReinversionAgrupadoAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;