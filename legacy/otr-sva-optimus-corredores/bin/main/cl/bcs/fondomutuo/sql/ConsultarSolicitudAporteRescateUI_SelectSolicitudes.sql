SELECT
	 SolicitudAporteRescate."IdSolicitudAporteRescate"
	,SolicitudAporteRescate."IdCliente"
	,SolicitudAporteRescate."IdPortafolio"
	,SolicitudAporteRescate."FechaEmision"
	,SolicitudAporteRescate."FechaIngresoRegistro"
	,SolicitudAporteRescate."IdTipoMovimientoFondoMutuo"
	,SolicitudAporteRescate."TipoSolicitud"
	,TipoMovimientoFondoMutuo."Descripcion" AS "DescripcionTipoMovimiento"
	,CASE
	    WHEN SolicitudAporteRescate."CalculoPorMontoCuota" = 'C' Then 'CUOTA'
	    WHEN SolicitudAporteRescate."CalculoPorMontoCuota" = 'M' Then 'MONTO'
	    WHEN SolicitudAporteRescate."CalculoPorMontoCuota" = 'T' Then 'TOTAL'
	  ELSE ''
	  END AS "CalculoMontoCuotaUI"	
	,SolicitudAporteRescate."Estado" AS "EstadoSolicitud"
	,EstadoSolicitud."Nombre" AS "NombreEstado"
	,Fondo."Nemotecnico" AS "NombreFondo"
	,Fondo."NombreFondo" AS "NombreFondoReal"
	,SolicitudAporteRescate."IdSerieFondo"
	,SerieDeFondo."NombreSerieFondo"
	,CASE SolicitudAporteRescate."CalculoPorMontoCuota" 
		WHEN 'C' THEN 'UI_NUMERO_CUOTAS' 
		WHEN 'T' THEN 'UI_TOTAL' 
		WHEN 'P' THEN 'UI_PORCENTAJE' 
		ELSE 'UI_MONTO' 
	END AS "CalculoPorMontoCuotaUI"
	,SolicitudAporteRescate."NumeroCuotas"
	,CASE 
		WHEN SolicitudAporteRescate."Estado" IN ('PEN','ING') THEN TO_DECIMAL(0,26,8) 
		ELSE SolicitudAporteRescate."ValorCuota" 
	END AS "ValorCuota"
	,SolicitudAporteRescate."FechaValorCuota"
	,SolicitudAporteRescate."Monto"
	,Cliente."DocumentoIdentidad"
	,Cliente."NombreCompleto"
	,Cliente."Nombres"
	,Cliente."ApellidoPaterno"
	,Cliente."ApellidoMaterno"
	,SerieDeFondo."IdInstrumento"
	,SolicitudAporteRescate."IdRegimenTributario"
	,CASE Portafolio."Proposito"
		WHEN 'PROPOSITOAPV' THEN
			CASE SolicitudAporteRescate."IdRegimenTributario"
				WHEN '' THEN Portafolio."CodigoPortafolio"
				ELSE SolicitudAporteRescate."IdRegimenTributario"
			END
		ELSE ''
	END AS "IdRegimenTributarioAPV"
	,Portafolio."DescripcionPortafolio"
	,SolicitudAporteRescate."FechaCertificacion"
	,SolicitudAporteRescate."FechaLiquidacion"
	,SolicitudAporteRescate."FechaAceptacion"
	,SolicitudAporteRescate."FechaAnulacion"
	,SolicitudAporteRescate."FechaAceptacionRegistro"
	,SolicitudAporteRescate."FechaCertificacionRegistro"
	,SolicitudAporteRescate."FechaLiquidacionRegistro"
	,SolicitudAporteRescate."FechaAnulacionRegistro"
	,Empleado."NombreCompleto" AS "NombreEmpleado"
	,Fondo."IdEmisor"
	,SolicitudAporteRescate."IdEmpresa"
	,SolicitudAporteRescate."IdMoneda"
	,CASE 
		WHEN COALESCE(ComprobanteFormaDePago."IdMedioDePago",'') = '' THEN MedioDePago."Nombre" 
		ELSE ComprobanteFormaDePago."NombreMedioDePago" 
	END AS "TextoPreferenciaPago"
	,SolicitudAporteRescate."Glosa"
	,SerieDeFondo."DecimalesValorCuota"
	,SerieDeFondo."DecimalesCuota"
	,CASE 
		SolicitudAporteRescate."Overnight" WHEN 'S' THEN 'UI_SI' 
		ELSE 'UI_NO' 
	END AS "EsOvernightUI"
	,SolicitudAporteRescate."IdSolicitudAporteRescateRelacionado"
	,PortafolioDestino."DescripcionPortafolio" AS "DescripcionPortafolioDestino"
	,SolicitudAporteRescate."IdPortafolioDestino"
	,SolicitudAporteRescate."IdClienteDestino"
	,ClienteDestino."DocumentoIdentidad" AS "DocumentoIdentidadDestino"
	,ClienteDestino."NombreCompleto" AS "NombreCompletoDestino"
	,CASE 
		SolicitudAporteRescate."IndicadorMLT" WHEN 'N' THEN 'UI_NO' 
		ELSE 'UI_SI' 
	END AS "EsIndicadorMLTUI"
	,SolicitudAporteRescate."MontoComision"
	,SolicitudAporteRescate."MontoComisionLocal"
	,SolicitudAporteRescateRelacionado."IdTipoMovimientoFondoMutuo" AS "IdTipoMovimientoFondoMutuoRelacionado"
	,TipoMovimientoFondoMutuoRelacionado."Descripcion" AS "DescripcionTipoMovimientoRelacionado"
	,Fondo."IdMoneda" AS "IdMonedaFondo"
	,CASE 
		WHEN SolicitudAporteRescate."IdMoneda" != Fondo."IdMoneda" THEN 'S' 
		ELSE 'N' 
	END AS "EsDiferido"
	,CASE 
		WHEN SolicitudAporteRescate."IdMoneda" != Fondo."IdMoneda" THEN 'UI_SI' 
		ELSE 'UI_NO' 
	END AS "EsDiferidoUI"
	,SolicitudAporteRescate."CostoMonedaLocal"
	,SolicitudAporteRescate."ParidadFechaInversion"
	,SolicitudAporteRescate."MontoInversion"
	,SolicitudAporteRescate."NumeroCuotasInversion"
	,SolicitudAporteRescate."ValorCuotaInversion"
	,SolicitudAporteRescate."CostoMonedaLocalInversion"
	,SolicitudAporteRescate."ParidadFechaCertificacion"
	,SolicitudAporteRescate."CalculoPorMontoCuota"
	,SolicitudAporteRescate."IdSerieFondoDestino"
	,SolicitudAporteRescate."IdEmisorDestino"
	,SolicitudAporteRescate."SolicitudBatch"
	,SolicitudAporteRescate."OrigenOperacion"
	,SolicitudAporteRescate."ComisionInformada"
	,SolicitudAporteRescate."EstadoLiquidacion"
	,CASE 
		WHEN SolicitudAporteRescate."EstadoLiquidacion" = 'ING' THEN 'INGRESADO' 
	   	WHEN SolicitudAporteRescate."EstadoLiquidacion" = 'LIQ' THEN 'LIQUIDADO'
	   	ELSE '' 
   	END AS "NombreEstadoLiquidacion"
	,SolicitudAporteRescate."FolioCartaLiquidacion"
	,PortafolioDestino."CodigoPortafolio" AS "CodigoPortafolioDestino"
	,SolicitudAporteRescate."ValorUFFechaInversion"
	,SolicitudAporteRescate."Spread"
	,SolicitudAporteRescate."FechaOriginalInversion"
	,SolicitudAporteRescateRelacionado."Monto" AS "MontoMovimientoAsociado"
	,SolicitudAporteRescateRelacionado."NumeroCuotas" AS "NumeroCuotasMovimientoAsociado"
	,SolicitudAporteRescate."IndicadorMLT"
	,SolicitudAporteRescate."IdEmisorOrigen"
	,Portafolio."CodigoPortafolio"
	,SerieDeFondo."CodigoSerie"
	,FondoRelacionado."IdFondo" AS "IdFondoRelacionado"
	,SerieDeFondoRelacionado."CodigoSerie" AS "CodigoSerieRelacionado"
	,COALESCE(RegimenFondoMutuo."Descripcion",'') AS "DescripcionRegimenTributario"
	,Fondo."IdFondo"
	,CASE 
		WHEN TipoMovimientoFondoMutuo."IndicadorAporteRescate" = 'APO' THEN 'APORTE'
       	WHEN TipoMovimientoFondoMutuo."IndicadorAporteRescate" = 'RES' THEN 'RESCATE'
       	ELSE '' 
   	END AS "DescripcionTipoSolicitud"
	,SolicitudAporteRescate."MontoMonedaSistema"
	,SolicitudAporteRescate."IdDividendo"
	,SolicitudAporteRescate."Switch"
	,CASE SolicitudAporteRescate."Switch" 
		WHEN 'N' THEN 'UI_NO' 
		ELSE 'UI_SI' 
	END AS "SwitchUI"
	,SolicitudAporteRescate."IdTipoCanalIngreso"
	,SolicitudAporteRescate."FolioContrato"
	,ProductoAgrupador."NombreProductoAgrupador" AS "Producto"
	,MAP(MatrizAgrupador."IdProductoAgrupador",NULL,TO_BIGINT(0),MatrizAgrupador."IdProductoAgrupador") AS "IdProductoAgrupador"
	,Usuario."CodigoUsuario"
 	,CASE WHEN  SolicitudAporteRescate."IdIngresoEgreso" = TesoreriaSpot."IdIngresoEgreso" THEN SolicitudAporteRescate."IdIngresoEgreso"
		  ELSE MovComprobante."IdIngresoEgreso"
	END	 AS "IdIngresoEgreso" 
	,CASE WHEN  SolicitudAporteRescate."IdIngresoEgreso" = TesoreriaSpot."IdIngresoEgreso" THEN EstadoTesoreriaSpot."Nombre"
		  ELSE EstadoTesoreriaSpot1."Nombre"
	END	 AS "NombreEstadoTesoreria" 
	,CASE WHEN SolicitudAporteRescate."IdIngresoEgreso" = TesoreriaSpot."IdIngresoEgreso" THEN TO_NVARCHAR(TesoreriaSpot."FechaLiquidacion",'YYYY-MM-DD')
		  ELSE  TO_NVARCHAR(MovComprobante."FechaLiquidacion",'YYYY-MM-DD')
	END  AS "FechaLiquidacionTesoreria"
	,SolicitudAporteRescate."IdOperacionSpot"
	,COALESCE(EstadoSpot."Nombre",'') AS "NombreEstadoSpot"
	,COALESCE(EstadoConfirmacionSpot."Nombre",'') AS "NombreEstadoConfirmacionSpot"
	,Spot."FechaLiquidacion" AS "FechaLiquidacionSpot"
	,CASE 
		WHEN SolicitudAporteRescate."IdOperacionSpot" > 0 THEN  Spot."IdTipoOperacion"
		ELSE '' 
	END AS "IdTipoOperacionSpot" 
	,CASE 
		WHEN SolicitudAporteRescate."IdOperacionSpot" > 0 THEN  TipoOperacionSpot."Nombre" 
		ELSE '' 
	END AS "NombreTipoOperacionSpot"
	,OmnibusDetalle."IdOmnibus"
	--REINVERSION 1N
	,SolicitudAporteRescate."IdAgrupador"
	--
	,SolicitudAporteRescate."RentabilidadReal"
	,SolicitudAporteRescate."CostoUF"
	,CASE 
		WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",TO_BIGINT(0)) 
		ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",TO_BIGINT(0)) 
	END AS "IdMatrizAgrupador"
    ,CASE 
    	WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MatrizAgrupador."EsADC",'N') 
    	ELSE COALESCE(MAT_NOAPV."EsADC",'N') 
    END AS "EsADC"
    ,SolicitudAporteRescate."FolioContratoOperacion"
    ,SolicitudAporteRescate."TipoContratoOperacion"
    ,OrdenTraspaso."IdTraspaso"
    ,SolicitudAporteRescate."IdMedioDePago"
    ,CASE 
    	WHEN Spot."IdOperacionSpot" IS NOT NULL THEN Spot."MontoMonedaSecundaria"
    	ELSE SolicitudAporteRescate."MontoMonedaSistema"
    END AS "MontoMonedaSistemaInicial"
    ,SolicitudAporteRescate."IdMonedaPago"
    ,SolicitudAporteRescate."IdMonedaSistema"
    ,SolicitudAporteRescate."FolioCarteraAsociadoComision" AS "FolioCarteraAsociadoComision"
	,CASE 
		WHEN SolicitudAporteRescate."Modificaciones"='N' THEN 'No modificado' 
		ELSE 'Modificado' END AS "Modificaciones"
	,COALESCE(SerieDeFondo."ISIN",'') AS "ISIN"
	,CASE 
		WHEN COALESCE(SolicitudAporteRescate."IdModulo",'') = 'FMM' AND COALESCE(Traspaso."IdTipoMovimientoTraspaso",'') = 'TRAING' THEN EntidadRecaudadora."Nombre"
		WHEN COALESCE(SolicitudAporteRescate."IdModulo",'') = 'APV' AND COALESCE(TraspasoAPV."TipoMovimiento",'') = 'INGRESO' THEN TraspasoAPV."NombreEntidad"
		ELSE '' END AS "EntidadOrigen"
	,CASE
		WHEN COALESCE(SolicitudAporteRescate."IdModulo",'') = 'FMM' AND COALESCE(Traspaso."IdTipoMovimientoTraspaso",'') = 'TRAEGR' THEN EntidadRecaudadora."Nombre"
		WHEN COALESCE(SolicitudAporteRescate."IdModulo",'') = 'APV' AND COALESCE(TraspasoAPV."TipoMovimiento",'') = 'EGRESO' THEN TraspasoAPV."NombreEntidad"
		ELSE '' END AS "EntidadDestino"
	,COALESCE(RecEmpleador."PeriodoPago",'') AS "PeriodoPago"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SolicitudAporteRescate
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS TipoMovimientoFondoMutuo ON (TipoMovimientoFondoMutuo."IdTipoMovimientoFondoMutuo" = SolicitudAporteRescate."IdTipoMovimientoFondoMutuo")
LEFT OUTER  JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EstadoSolicitud ON (EstadoSolicitud."IdEstado" = SolicitudAporteRescate."Estado")
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SerieDeFondo ON (SerieDeFondo."IdSerieFondo" = SolicitudAporteRescate."IdSerieFondo" )
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS Fondo ON (Fondo."IdFondo" = SerieDeFondo."IdFondo" )
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS RegimenFondoMutuo ON (RegimenFondoMutuo."IdRegimenFondoMutuo" = SolicitudAporteRescate."IdRegimenTributario")
LEFT OUTER  JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS Usuario ON (Usuario."IdUsuario" = SolicitudAporteRescate."IdUsuario")
LEFT OUTER  JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON  (Portafolio."IdPortafolio" = SolicitudAporteRescate."IdPortafolio" )
LEFT OUTER 	JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
LEFT OUTER 	JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador ON MatrizAgrupador."IdProductoAgrupador" = ProductoAgrupador."IdProductoAgrupador"
LEFT OUTER	JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
LEFT OUTER  JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS Cliente ON  (Cliente."IdCliente" = Portafolio."IdCliente" )
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.OmnibusDetalle" AS OmnibusDetalle ON (SolicitudAporteRescate."FolioOmnibus" = OmnibusDetalle."IdOmnibusDetalle")
LEFT OUTER  JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PortafolioDestino ON  (PortafolioDestino."IdPortafolio" = SolicitudAporteRescate."IdPortafolioDestino")
LEFT OUTER  JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS ClienteDestino ON  (ClienteDestino."IdCliente" = PortafolioDestino."IdCliente" )
LEFT OUTER  JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" AS Empleado ON (Empleado."IdEmpleado" = SolicitudAporteRescate."IdEmpleadoEjecutivo")
LEFT OUTER  JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" AS MedioDePago ON (MedioDePago."IdMedioDePago" = SolicitudAporteRescate."IdMedioDePago")
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS SolicitudAporteRescateRelacionado ON (SolicitudAporteRescateRelacionado."IdSolicitudAporteRescate" = SolicitudAporteRescate."IdSolicitudAporteRescateRelacionado")
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS TipoMovimientoFondoMutuoRelacionado ON (TipoMovimientoFondoMutuoRelacionado."IdTipoMovimientoFondoMutuo" = SolicitudAporteRescateRelacionado."IdTipoMovimientoFondoMutuo")
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SerieDeFondoRelacionado ON (SerieDeFondoRelacionado."IdSerieFondo" = SolicitudAporteRescate."IdSerieFondoDestino" )
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS FondoRelacionado ON (FondoRelacionado."IdFondo" = SerieDeFondoRelacionado."IdFondo" )
LEFT OUTER  JOIN (SELECT top 1 "IdTipoComprobanteOrigen", "IdComprobanteOrigen", ComprobanteDetalle."IdEstado",Comprobante."IdComprobante"  FROM
					"BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS ComprobanteDetalle
					INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS Comprobante ON Comprobante."IdComprobante" = ComprobanteDetalle."IdComprobante"
				) AS ComprobanteDetalle ON (ComprobanteDetalle."IdTipoComprobanteOrigen" = 'FMM' AND ComprobanteDetalle."IdComprobanteOrigen" = SolicitudAporteRescate."IdSolicitudAporteRescate" AND ComprobanteDetalle."IdEstado" IN ('ING','LIQ','CRT'))
LEFT OUTER  JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS ComprobanteFormaDePago ON (ComprobanteFormaDePago."IdComprobante" = ComprobanteDetalle."IdComprobante" AND ComprobanteFormaDePago."IdEstado" IN ('CRT','EBV','EMI','GIR','ING','LIQ'))
LEFT OUTER  JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS TesoreriaSpot ON (SolicitudAporteRescate."IdIngresoEgreso" = TesoreriaSpot."IdIngresoEgreso")
LEFT OUTER  JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EstadoTesoreriaSpot ON (EstadoTesoreriaSpot."IdEstado" =  TesoreriaSpot."IdEstado")
LEFT OUTER  JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" AS Spot ON (Spot."IdOperacionSpotOrigen" = SolicitudAporteRescate."IdOperacionSpot" AND Spot."Estado" != 'MOD' AND Spot."IdOperacionSpotOrigen" != 0)
LEFT OUTER  JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" AS OperSpotPrin ON (Spot."IdOperacionSpot"=OperSpotPrin."IdOperacionSpot")				  
LEFT OUTER  JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EstadoSpot ON (EstadoSpot."IdEstado" = Spot."Estado")
LEFT OUTER  JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EstadoConfirmacionSpot ON (EstadoConfirmacionSpot."IdEstado" = OperSpotPrin."EstadoConfirmacion")
LEFT OUTER  JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TipoOperacionSpot ON (TipoOperacionSpot."IdTipoOperacion" = Spot."IdTipoOperacion")
--
LEFT OUTER  JOIN (SELECT MovComprobante."IdTransaccion", MovComprobante."FolioTransaccion", MovComprobante."IdTipoCanalIngreso", 
					MovComprobante."IdComprobante",TesoreriaSpot1."IdIngresoEgreso",MovComprobante."IdTipoProducto", 
					TesoreriaSpot1."IdEstado",TesoreriaSpot1."FechaLiquidacion", ROW_NUMBER() OVER(PARTITION BY TesoreriaSpot1."IdOperacion") AS rownumber
					FROM  "BSCL.Facturacion.BD::FAC.MovimientoComprobante"   AS MovComprobante 
					INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS TesoreriaSpot1 ON (TesoreriaSpot1."IdComprobanteOrigen"= MovComprobante."IdComprobante" AND TesoreriaSpot1."IdModulo"='FAC' AND TesoreriaSpot1."IdTipoProducto"= 'MEME')
				)AS MovComprobante
ON (MovComprobante."IdTipoCanalIngreso"='FMM'  AND  MovComprobante."IdTransaccion" = Spot."IdOperacionSpot" AND MovComprobante.rownumber=1 )
LEFT OUTER  JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EstadoTesoreriaSpot1 ON (EstadoTesoreriaSpot1."IdEstado" =  MovComprobante."IdEstado")
--
LEFT OUTER	JOIN "BSCL.APV.BD::TRA.OrdenTraspaso" AS OrdenTraspaso ON (OrdenTraspaso."IdSolicitudAporteRescate" = SolicitudAporteRescate."IdSolicitudAporteRescate")
LEFT OUTER JOIN "BSCL.APV.BD::TRA.Traspaso" AS TraspasoAPV ON (OrdenTraspaso."IdTraspaso" = TraspasoAPV."IdTraspaso")
LEFT OUTER JOIN "BSCL.APV.BD::REC.Cliente" AS RecCliente ON (RecCliente."IdRecaudacionCliente" = SolicitudAporteRescate."IdOrigenRecaCliente")
LEFT OUTER JOIN "BSCL.APV.BD::REC.Empleador" AS RecEmpleador ON (RecEmpleador."IdRecaudacionEmpleador"  = RecCliente."IdRecaudacionEmpleador")
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::TRA.TraspasoDetalleDestino" AS TraspasoDestino ON (TraspasoDestino."IdSolicitudAporteRescate" <> 0 AND SolicitudAporteRescate."IdSolicitudAporteRescate" = TraspasoDestino."IdSolicitudAporteRescate")
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::TRA.TraspasoDetalleOrigen" AS TraspasoOrigen ON (TraspasoOrigen."IdSolicitudAporteRescate" <> 0 AND SolicitudAporteRescate."IdSolicitudAporteRescate" = TraspasoOrigen."IdSolicitudAporteRescate")
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::TRA.Traspaso" AS Traspaso ON (TraspasoOrigen."IdTraspaso" = Traspaso."IdTraspaso" OR TraspasoDestino."IdTraspaso" = Traspaso."IdTraspaso")
LEFT OUTER JOIN "BSCL.APV.BD::SCS.EntidadRecaudadora" AS EntidadRecaudadora ON (Traspaso."IdEntidadRecaudadora" = EntidadRecaudadora."IdEntidadRecaudadora")
WHERE
SolicitudAporteRescate."IdEmpresa" = :IdEmpresa
AND (:FiltraPorFechaAceptacionDesde = 0 OR SolicitudAporteRescate."FechaAceptacion" >= :FechaAceptacionDesde)
AND (:FiltraPorFechaAceptacionHasta = 0 OR SolicitudAporteRescate."FechaAceptacion" <= :FechaAceptacionHasta)
AND (:FiltraPorIdTipoMovimientoFondoMutuo = 0 OR SolicitudAporteRescate."IdTipoMovimientoFondoMutuo" = :IdTipoMovimientoFondoMutuo)
AND (:FiltraPorEstado = 0 OR SolicitudAporteRescate."Estado" = :Estado)
AND (:FiltraPorIdEmisor = 0 OR Fondo."IdEmisor" = :IdEmisor)
AND (:FiltraPorIdSerieFondo = 0 OR SolicitudAporteRescate."IdSerieFondo" = :IdSerieFondo)
AND (:FiltraPorIdCliente = 0 OR SolicitudAporteRescate."IdCliente" = :IdCliente)
AND (:FiltraPorIdProductoAgrupador = 0 OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :IdProductoAgrupador)
AND (:FiltraPorIdPortafolio = 0 OR SolicitudAporteRescate."IdPortafolio" = :IdPortafolio)
AND (:FiltraPorTipoSolicitud = 0 OR SolicitudAporteRescate."TipoSolicitud" = :TipoSolicitud)
AND (:FiltraPorIdMoneda = 0 OR SolicitudAporteRescate."IdMoneda" = :IdMoneda)
AND (:FiltraPorIdSolicitudAporteRescate = 0 OR SolicitudAporteRescate."IdSolicitudAporteRescate" = :IdSolicitudAporteRescate)