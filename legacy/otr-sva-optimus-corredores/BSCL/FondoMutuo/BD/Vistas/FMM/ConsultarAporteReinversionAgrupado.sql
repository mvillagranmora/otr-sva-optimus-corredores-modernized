SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarAporteReinversionAgrupado";
GO
CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarAporteReinversionAgrupado"
AS
SELECT 
"IdSolicitudAporteRescate"
,"IdAgrupador"
,"IdSerieFondo"
,"IdMoneda"
,"Monto"
,"MontoMonedaSistema"
,"NumeroCuotas"
,"FechaCertificacion"
,"FechaLiquidacion"
,"FechaValorCuota"
,"FechaAceptacion"
,"FechaEmision"
,"ValorCuota"
,"IdRegimenTributario"
,"IdCliente"
,"IdPortafolio"
,"IdTipoMovimientoFondoMutuo"
,"IdEmpresa"
,"TipoSolicitud"
,"FolioContratoOperacion"
,"TipoContratoOperacion"
, CASE WHEN "IdEstadoAnulado" > 0 THEN 'ANU'
	   WHEN "IdEstadoPendiente" > 0 THEN 'PEN'
	   WHEN "IdEstadoIngresado" > 0 THEN 'ING'
	   WHEN "IdEstadoCertificado" > 0 THEN 'CRT'
END "Estado"
FROM (SELECT
				TO_BIGINT(0) AS "IdSolicitudAporteRescate"
				,SolicitudAporteRescate."IdAgrupador"
				,SolicitudAporteRescate."IdSerieFondo"
				,SolicitudAporteRescate."IdMoneda"
				,SolicitudAporteRescate."FechaCertificacion"
				,SolicitudAporteRescate."FechaLiquidacion"
				,SolicitudAporteRescate."FechaValorCuota"
				,SolicitudAporteRescate."FechaAceptacion"
				,SolicitudAporteRescate."FechaEmision"
				,SolicitudAporteRescate."ValorCuota"
				,SolicitudAporteRescate."IdRegimenTributario"
				,SolicitudAporteRescate."IdCliente"
				,SolicitudAporteRescate."IdPortafolio"
				,SUM(SolicitudAporteRescate."Monto") AS "Monto" 
				,SUM(CASE WHEN COALESCE(Spot."IdOperacionSpotOrigen",0) > 0 THEN Spot."MontoMonedaSecundaria" ELSE SolicitudAporteRescate."MontoMonedaSistema" END) AS "MontoMonedaSistema"
				,SUM(SolicitudAporteRescate."NumeroCuotas") AS "NumeroCuotas"
				,COUNT(CASE WHEN SolicitudAporteRescate."Estado" = 'ING' THEN 1 ELSE NULL END) AS "IdEstadoIngresado"
				,COUNT(CASE WHEN SolicitudAporteRescate."Estado" = 'PEN' THEN 1 ELSE NULL END) AS "IdEstadoPendiente"
				,COUNT(CASE WHEN SolicitudAporteRescate."Estado" = 'CRT' THEN 1 ELSE NULL END) AS "IdEstadoCertificado"
				,COUNT(CASE WHEN SolicitudAporteRescate."Estado" = 'ANU' THEN 1 ELSE NULL END) AS "IdEstadoAnulado"
				,SolicitudAporteRescate."IdTipoMovimientoFondoMutuo"
				,SolicitudAporteRescate."IdEmpresa"
				,SolicitudAporteRescate."TipoSolicitud"
				,SolicitudAporteRescate."FolioContratoOperacion"
				,SolicitudAporteRescate."TipoContratoOperacion"
				FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS SolicitudAporteRescate
				LEFT JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" Spot ON Spot."IdOperacionSpotOrigen"= SolicitudAporteRescate."IdOperacionSpot" AND Spot."Estado" != 'MOD'
				WHERE "IdTipoMovimientoFondoMutuo" = 'APOREINV' AND "IdAgrupador" != 0
				GROUP BY 
				SolicitudAporteRescate."IdAgrupador"
				,SolicitudAporteRescate."IdSerieFondo"
				,SolicitudAporteRescate."IdMoneda"
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
);
GO
GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarAporteReinversionAgrupado" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO