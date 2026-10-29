SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateUI";
GO
CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateUI" AS
SELECT
	 Consulta."IdSolicitudAporteRescate"
	,Consulta."IdCliente"
	,Consulta."IdPortafolio"
	,Consulta."FechaEmision"
	,Consulta."FechaIngresoRegistro"
	,Consulta."IdTipoMovimientoFondoMutuo"
	,Consulta."TipoSolicitud"
	,Consulta."DescripcionTipoMovimiento"
	,Consulta."CalculoMontoCuotaUI"
	,Consulta."EstadoSolicitud"
	,Consulta."NombreEstado"
	,Consulta."Nemotecnico" AS "NombreFondo"
	,Consulta."NombreFondo" AS "NombreFondoReal"
	,Consulta."IdSerieFondo"
	,Consulta."NombreSerieFondo"
	,Consulta."CalculoPorMontoCuotaUI"
	,Consulta."NumeroCuotas"
	,CASE WHEN Consulta."EstadoSolicitud" != 'CRT' THEN Consulta."ValorCuota" ELSE Consulta."ValorCuotaSolicitud" END AS "ValorCuota"
	,Consulta."FechaValorCuota"
	,Consulta."Monto"
	,Consulta."DocumentoIdentidad"
	,Consulta."NombreCompleto"
	--
	,Consulta."Nombres"
	,Consulta."ApellidoPaterno"
	,Consulta."ApellidoMaterno"
	,Consulta."IdInstrumento"
	,Consulta."IdRegimenTributario"
	--
	,Consulta."DescripcionPortafolio"
	,Consulta."FechaCertificacion"
	,Consulta."FechaLiquidacion"
	,Consulta."FechaAceptacion"
	,Consulta."FechaAnulacion"
	,Consulta."FechaAceptacionRegistro"
	,Consulta."FechaCertificacionRegistro"
	,Consulta."FechaLiquidacionRegistro"
	,Consulta."FechaAnulacionRegistro"
	,Consulta."NombreEmpleado"
	,Consulta."IdEmisor"
	,Consulta."IdEmpresa"
	,Consulta."IdMoneda"
	,Consulta."TextoPreferenciaPago"
	,Consulta."Glosa"
	,Consulta."DecimalesValorCuota"
	,Consulta."DecimalesCuota"
	,Consulta."EsOvernightUI"
	,Consulta."IdSolicitudAporteRescateRelacionado"
	,Consulta."DescripcionPortafolioDestino"
	,Consulta."IdPortafolioDestino"
	,Consulta."IdClienteDestino"
	,Consulta."DocumentoIdentidadDestino"
	,Consulta."NombreCompletoDestino"
	,CASE Consulta."IndicadorMLT" WHEN 'N' THEN 'UI_NO' ELSE 'UI_SI' END AS "EsIndicadorMLTUI"
	,Consulta."MontoComision"
	,Consulta."MontoComisionLocal"
	,Consulta."IdTipoMovimientoFondoMutuoRelacionado"
	,Consulta."DescripcionTipoMovimientoRelacionado"
	,Consulta."IdMonedaFondo"
	,Consulta."EsDiferido"
	,Consulta."EsDiferidoUI"
	,Consulta."CostoMonedaLocal"
	,Consulta."ParidadFechaInversion"
	,Consulta."MontoInversion"
	,Consulta."NumeroCuotasInversion"
	,Consulta."ValorCuotaInversion"
	,Consulta."CostoMonedaLocalInversion"
	,Consulta."ParidadFechaCertificacion"
	,Consulta."CalculoPorMontoCuota"
	,Consulta."IdSerieFondoDestino"
	,Consulta."IdEmisorDestino"
	,Consulta."SolicitudBatch"
	,Consulta."OrigenOperacion"
	,Consulta."ComisionInformada"
	,Consulta."EstadoLiquidacion"
	,Consulta."NombreEstadoLiquidacion"
	,Consulta."FolioCartaLiquidacion"
	,Consulta."CodigoPortafolioDestino"
	--,Consulta."CostoUF" 
	,Consulta."ValorUFFechaInversion"
	,Consulta."Spread"
	,Consulta."FechaOriginalInversion"
	,Consulta."MontoMovimientoAsociado"
	,Consulta."NumeroCuotasMovimientoAsociado"
	,Consulta."IndicadorMLT"
	,Consulta."IdEmisorOrigen"
	,Consulta."CodigoPortafolio"
	,Consulta."CodigoSerie"
	,Consulta."IdFondoRelacionado"
	,Consulta."CodigoSerieRelacionado"
	,Consulta."DescripcionRegimenTributario"
	,Consulta."IdFondo"
	,Consulta."DescripcionTipoSolicitud"
	,Consulta."MontoMonedaSistema"
	,Consulta."IdDividendo"
	,Consulta."Switch"
	,CASE Consulta."Switch" WHEN 'N' THEN 'UI_NO' ELSE 'UI_SI' END AS "SwitchUI"
	,Consulta."IdTipoCanalIngreso","FolioContrato"
	,MatrizAgrupador."Producto"
	,MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") AS "IdProductoAgrupador"
	,Consulta."CodigoUsuario"
	---SPOT
 	,Consulta."IdIngresoEgreso"
	,Consulta."NombreEstadoTesoreria" 	
	,TO_NVARCHAR(Consulta."FechaLiquidacionTesoreria",'YYYY-MM-DD') AS "FechaLiquidacionTesoreria"
	,Consulta."IdOperacionSpot"
	,Consulta."NombreEstadoSpot"
	,Consulta."NombreEstadoConfirmacionSpot"
	,Consulta."FechaLiquidacionSpot"
	,Consulta."IdTipoOperacionSpot"
	,Consulta."NombreTipoOperacionSpot"
	--OMNIBUS
	,Consulta."IdOmnibus"
	--REINVERSION 1N
	,Consulta."IdAgrupador"
	--
	,Consulta."RentabilidadReal"
	,Consulta."CostoUF"
	,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
    ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MatrizAgrupador."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
    

	,CASE 
		WHEN Consulta."TipoSolicitud" = 'RES' AND (Consulta."IdRegimenTributario" = 'APV-B' OR Consulta."IdRegimenTributario" = 'APV-AN') AND (COALESCE(OrdenTraspaso."IdTraspaso",0) = 0) THEN (Consulta."Monto" * 0.15) 
		ELSE NULL 
	 END AS "MontoRetencion"
    ,Consulta."FolioContratoOperacion"
    ,Consulta."TipoContratoOperacion"
	
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" AS Consulta
LEFT OUTER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
			 ON Portafolio."IdPortafolio" = Consulta."IdPortafolio"
			AND Portafolio."IdCliente" = Consulta."IdCliente"
LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupador" AS MatrizAgrupador
   			 ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0


LEFT JOIN "BSCL.APV.BD::TRA.OrdenTraspaso" AS OrdenTraspaso ON (OrdenTraspaso."IdSolicitudAporteRescate" = Consulta."IdSolicitudAporteRescate")


;
GO
GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateUI" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

