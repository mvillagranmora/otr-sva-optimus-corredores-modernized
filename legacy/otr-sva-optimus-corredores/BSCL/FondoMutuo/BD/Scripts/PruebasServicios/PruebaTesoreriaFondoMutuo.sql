SET SCHEMA "BSCL";
DROP table #cargoAbono;
DROP table #RESULTADO_CertificacionSolicitudAporteRescateMasiva;
create local temporary table #cargoAbono(
											"NumeroCargoAbono"					BIGINT,
										    "IdPortafolio"						BIGINT,											    
										    "IdMoneda"							NVARCHAR(3),     
										    "IdTipoMovimiento"					NVARCHAR(6),    
										    "IdConcepto"						NVARCHAR(6),
										    "Descripcion"						NVARCHAR(520),
										    "IdCondicionDeLiquidacion"			NVARCHAR(2),
										    "FechaCondicionDeLiquidacion"		NVARCHAR(10),
										    "Monto"								DECIMAL(26,8),
										    
										    "IdEjecutivo" 						BIGINT,
 											"IdSucursal"						BIGINT,
										    "IdTipoProducto"					NVARCHAR(4),
										    "IdTipoOperacion"					NVARCHAR(6),
										    "IdOperacion"						BIGINT,
										    "FechaOperacion"					NVARCHAR(40),
										    "FechaLiquidacionOperacion"			NVARCHAR(40),
										    "FechaVencimientoOperacion"			NVARCHAR(40),
										    "IdMonedaOperacion"					NVARCHAR(3),
										    "MontoOperacion"					DECIMAL(26,8),
										    "TipoCambioOperacion"				DECIMAL(26,8),
										    "IdContraparte"						BIGINT,											    
										    "IdTipoComprobante"					NVARCHAR(3),
										    "IdComprobante"						BIGINT,
										    "IdComprobanteDetalle"				BIGINT,
										    "IdComprobanteFormaDePago"			BIGINT,											    
										    "IdTipoMovimientoAsociado"          NVARCHAR(6),
 											"IdMovimientoAsociado"              BIGINT,											    
										    "FechaComprobante"					NVARCHAR(40),											    
										    "IdPersonaPagoCobro"				BIGINT,
										    "IdInstrumento"						BIGINT,
										    "IdTipoInstrumento"					NVARCHAR(3),
										    "Nemo"								NVARCHAR(40),
										    "EsIndicadorBilateral"				NVARCHAR(1),
										    "EsCargoAbonoPorTraspaso"			NVARCHAR(1),
										    "IdPortafolioTraspaso"   			BIGINT,
										    "GeneroContraparteEnTesoreria"		NVARCHAR(1),
										    "CertificadoAutomatico"				NVARCHAR(1)
);

create local temporary table #RESULTADO_CertificacionSolicitudAporteRescateMasiva(
			"IdInterno"				   		BIGINT,
			"IdSolicitudAporteRescate" 		BIGINT,
			"Codigo" 			 	   		NVARCHAR(100),
			"Descripcion" 		       		NVARCHAR(520),
			"FechaCertificacion"       		DATE,
			"TipoSolicitud"			   		NVARCHAR(3),
			"IdSerieFondo"			   		NVARCHAR(40),
			"IdInstrumento"			   		BIGINT,
			"IdTipoInstrumento"		   		NVARCHAR(3),
			"IdTipoMovimientoFondoMutuo"	NVARCHAR(10),
			"IdEmisor"				   		NVARCHAR(40),
			"IdCliente"				   		BIGINT,
			"IdPortafolio"			   		BIGINT,
			"IdMoneda"				   		NVARCHAR(3),
			"IdMonedaFondo"			   		NVARCHAR(3),
			"CostoMonedaLocal"		   		DECIMAL(26,8),
			"Monto"					   		DECIMAL(26,8),
			"ValorCuota"			   		DECIMAL(26,8),
			"NumeroCuotas"			   		DECIMAL(26,8),
			"ParidadFechaInversion"	   		DECIMAL(26,8),
			"FechaLiquidacion"		   		DATE,
			"IdUsuario"				   		BIGINT,
			"IdPersona"				   		BIGINT,
			"IdMedioDePago"			   		NVARCHAR(3),
			"MueveCta"						NVARCHAR(1),
			"Rechazo"				   		BIGINT
);

INSERT INTO #RESULTADO_CertificacionSolicitudAporteRescateMasiva
SELECT 0 AS "IdInterno",
	   "Solicitudes"."IdSolicitudAporteRescate",
	   'OK'		AS "Codigo",
	   '' AS "Descripcion",
	   "Solicitudes"."FechaCertificacion",
	   "Solicitudes"."TipoSolicitud",
	   "Solicitudes"."IdSerieFondo",
	   "Solicitudes"."IdInstrumento",
	   "Solicitudes"."IdTipoInstrumento",
	   "Solicitudes"."IdTipoMovimientoFondoMutuo",
	   "Solicitudes"."IdEmisor",
	   "Solicitudes"."IdCliente",
	   "Solicitudes"."IdPortafolio",
	   "Solicitudes"."IdMoneda",
	   "Solicitudes"."IdMonedaFondo",
	   "Solicitudes"."CostoMonedaLocal",
	   "Solicitudes"."Monto",
	   "Solicitudes"."ValorCuota",
	   "Solicitudes"."NumeroCuotas",
	   "Solicitudes"."ParidadFechaInversion",
	   "Solicitudes"."FechaLiquidacion",
	   "Solicitudes"."IdUsuario",
	   "Solicitudes"."IdPersona",
	   "Solicitudes"."IdMedioDePago",
	   "Solicitudes"."MueveCta",
	   0 AS "Rechazo"
FROM "BSCL"."BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" AS "Solicitudes"
WHERE "Solicitudes"."FechaCertificacion" = '2016-09-06';

INSERT INTO #cargoAbono
SELECT ROW_NUMBER() OVER (ORDER BY "IdOperacion") AS "NumeroCargoAbono", * FROM(
		        					SELECT "IdPortafolio" AS "IdPortafolio",
		        					"IdMoneda" AS "IdMoneda"
		        					,'MOVCAR'"IdTipoMovimiento"
									,'APORFM' AS "IdConcepto"
									,'APORTE_FONDO_MUTUO' AS "Descripcion"
									,'T0' AS "IdCondicionDeLiquidacion"
									,"FechaCertificacion" AS "FechaCondicionDeLiquidacion"
									,"CostoMonedaLocal" AS "Monto"
									,"IdUsuario" AS "IdEjecutivo"
									,0 AS "IdSucursal"
									,'INFM' AS "IdTipoProducto"
									,'APORFM' AS "IdTipoOperacion"
									,"IdSolicitudAporteRescate" AS "IdOperacion"
									,"FechaCertificacion" AS "FechaOperacion"
									,"FechaLiquidacion" AS "FechaLiquidacionOperacion"
									,null AS "FechaVencimientoOperacion"
									,"IdMoneda" AS "IdMonedaOperacion"
									,"CostoMonedaLocal" AS "MontoOperacion"
									,"ParidadFechaInversion" AS "TipoCambioOperacion"
									,null AS "IdContraparte"
									,'FFM' AS "IdTipoComprobante"
									,"IdSolicitudAporteRescate" AS "IdComprobante"
									,"IdSolicitudAporteRescate" AS "IdComprobanteDetalle"
									,"IdSolicitudAporteRescate" AS "IdComprobanteFormaDePago"
									,'' AS "IdTipoMovimientoAsociado"
									,0 AS "IdMovimientoAsociado"
									,"FechaLiquidacion" AS "FechaComprobante"
									,null AS "IdPersonaPagoCobro"
									,"IdInstrumento" AS "IdInstrumento"
									,"IdTipoInstrumento" AS "IdTipoInstrumento"
									,"IdSerieFondo" AS "Nemo"
									,null AS "EsIndicadorBilateral"
									,'N' AS "EsCargoAbonoPorTraspaso"
									,null AS "IdPortafolioTraspaso"
									,'N' AS "GeneroContraparteEnTesoreria"
									,'S' AS "CertificadoAutomatico"
								FROM #RESULTADO_CertificacionSolicitudAporteRescateMasiva
								WHERE "Rechazo" = 0 AND ("TipoSolicitud" = 'APO' AND "MueveCta" = 'S') AND "IdMonedaFondo" != "IdMoneda"
								UNION ALL
		        					SELECT "IdPortafolio" AS "IdPortafolio",
		        					"IdMonedaFondo" AS "IdMoneda"
		        					,'MOVABN'"IdTipoMovimiento"
									,'APORFM' AS "IdConcepto"
									,'APORTE_FONDO_MUTUO' AS "Descripcion"
									,'T0' AS "IdCondicionDeLiquidacion"
									,"FechaCertificacion" AS "FechaCondicionDeLiquidacion"
									,"Monto" AS "Monto"
									,"IdUsuario" AS "IdEjecutivo"
									,0 AS "IdSucursal"
									,'INFM' AS "IdTipoProducto"
									,'APORFM' AS "IdTipoOperacion"
									,"IdSolicitudAporteRescate" AS "IdOperacion"
									,"FechaCertificacion" AS "FechaOperacion"
									,"FechaLiquidacion" AS "FechaLiquidacionOperacion"
									,null AS "FechaVencimientoOperacion"
									,"IdMonedaFondo" AS "IdMonedaOperacion"
									,"Monto" AS "MontoOperacion"
									,"ParidadFechaInversion" AS "TipoCambioOperacion"
									,null AS "IdContraparte"
									,'FFM' AS "IdTipoComprobante"
									,"IdSolicitudAporteRescate" AS "IdComprobante"
									,"IdSolicitudAporteRescate" AS "IdComprobanteDetalle"
									,"IdSolicitudAporteRescate" AS "IdComprobanteFormaDePago"
									,'' AS "IdTipoMovimientoAsociado"
									,0 AS "IdMovimientoAsociado"
									,"FechaLiquidacion" AS "FechaComprobante"
									,null AS "IdPersonaPagoCobro"
									,"IdInstrumento" AS "IdInstrumento"
									,"IdTipoInstrumento" AS "IdTipoInstrumento"
									,"IdSerieFondo" AS "Nemo"
									,null AS "EsIndicadorBilateral"
									,'N' AS "EsCargoAbonoPorTraspaso"
									,null AS "IdPortafolioTraspaso"
									,'N' AS "GeneroContraparteEnTesoreria"
									,'S' AS "CertificadoAutomatico"
								FROM #RESULTADO_CertificacionSolicitudAporteRescateMasiva
								WHERE "Rechazo" = 0 AND ("TipoSolicitud" = 'APO' AND "MueveCta" = 'S') AND "IdMonedaFondo" != "IdMoneda"
								UNION ALL
		        					SELECT "IdPortafolio" AS "IdPortafolio"
									,"IdMonedaFondo" AS "IdMoneda"
									,CASE "TipoSolicitud" WHEN 'APO' THEN 'MOVCAR' ELSE 'MOVABN' END AS "IdTipoMovimiento"
									,CASE "TipoSolicitud" WHEN 'APO' THEN 'APORFM' ELSE 'RESCFM' END AS "IdConcepto"
									,CASE "TipoSolicitud" WHEN 'APO' THEN 'APORTE_FONDO_MUTUO' ELSE 'RESCATE_FONDO_MUTUO' END AS "Descripcion"
									,'T0' AS "IdCondicionDeLiquidacion"
									,"FechaCertificacion" AS "FechaCondicionDeLiquidacion"
									,"Monto" AS "Monto"
									,"IdUsuario" AS "IdEjecutivo"
									,0 AS "IdSucursal"
									,'INFM' AS "IdTipoProducto"
									,'APORFM' AS "IdTipoOperacion"
									,"IdSolicitudAporteRescate" AS "IdOperacion"
									,"FechaCertificacion" AS "FechaOperacion"
									,"FechaLiquidacion" AS "FechaLiquidacionOperacion"
									,null AS "FechaVencimientoOperacion"
									,"IdMonedaFondo" AS "IdMonedaOperacion"
									,"Monto" AS "MontoOperacion"
									,"ParidadFechaInversion" AS "TipoCambioOperacion"
									,null AS "IdContraparte"
									,'FFM' AS "IdTipoComprobante"
									,"IdSolicitudAporteRescate" AS "IdComprobante"
									,"IdSolicitudAporteRescate" AS "IdComprobanteDetalle"
									,"IdSolicitudAporteRescate" AS "IdComprobanteFormaDePago"
									,'' AS "IdTipoMovimientoAsociado"
									,0 AS "IdMovimientoAsociado"
									,"FechaLiquidacion" AS "FechaComprobante"
									,null AS "IdPersonaPagoCobro"
									,"IdInstrumento" AS "IdInstrumento"
									,"IdTipoInstrumento" AS "IdTipoInstrumento"
									,"IdSerieFondo" AS "Nemo"
									,null AS "EsIndicadorBilateral"
									,'N' AS "EsCargoAbonoPorTraspaso"
									,null AS "IdPortafolioTraspaso"
									,'N' AS "GeneroContraparteEnTesoreria"
									,'S' AS "CertificadoAutomatico"
							FROM #RESULTADO_CertificacionSolicitudAporteRescateMasiva
		                    WHERE "Rechazo" = 0 AND (("IdMedioDePago" = 'CIN' AND "TipoSolicitud" = 'RES') OR ("TipoSolicitud" = 'APO' AND "MueveCta" = 'S'))
		                    UNION ALL
		                    	SELECT "IdPortafolio" AS "IdPortafolio"
									,"IdMonedaFondo" AS "IdMoneda"
									,'MOVCAR' AS "IdTipoMovimiento"
									,'RESCFM' AS "IdConcepto"
									,'RESCATE_FONDO_MUTUO' AS "Descripcion"
									,'T0' AS "IdCondicionDeLiquidacion"
									,"FechaCertificacion" AS "FechaCondicionDeLiquidacion"
									,"Monto" AS "Monto"
									,"IdUsuario" AS "IdEjecutivo"
									,0 AS "IdSucursal"
									,'INFM' AS "IdTipoProducto"
									,'APORFM' AS "IdTipoOperacion"
									,"IdSolicitudAporteRescate" AS "IdOperacion"
									,"FechaCertificacion" AS "FechaOperacion"
									,"FechaLiquidacion" AS "FechaLiquidacionOperacion"
									,null AS "FechaVencimientoOperacion"
									,"IdMonedaFondo" AS "IdMonedaOperacion"
									,"Monto" AS "MontoOperacion"
									,"ParidadFechaInversion" AS "TipoCambioOperacion"
									,null AS "IdContraparte"
									,'FFM' AS "IdTipoComprobante"
									,"IdSolicitudAporteRescate" AS "IdComprobante"
									,"IdSolicitudAporteRescate" AS "IdComprobanteDetalle"
									,"IdSolicitudAporteRescate" AS "IdComprobanteFormaDePago"
									,'' AS "IdTipoMovimientoAsociado"
									,0 AS "IdMovimientoAsociado"
									,"FechaLiquidacion" AS "FechaComprobante"
									,null AS "IdPersonaPagoCobro"
									,"IdInstrumento" AS "IdInstrumento"
									,"IdTipoInstrumento" AS "IdTipoInstrumento"
									,"IdSerieFondo" AS "Nemo"
									,null AS "EsIndicadorBilateral"
									,'N' AS "EsCargoAbonoPorTraspaso"
									,null AS "IdPortafolioTraspaso"
									,'N' AS "GeneroContraparteEnTesoreria"
									,'S' AS "CertificadoAutomatico"
							FROM #RESULTADO_CertificacionSolicitudAporteRescateMasiva
							WHERE "Rechazo" = 0 AND ("IdMedioDePago" = 'CIN' AND "TipoSolicitud" = 'RES') AND "IdMonedaFondo" != "IdMoneda"
							UNION ALL
								SELECT "IdPortafolio" AS "IdPortafolio"
									,"IdMoneda" AS "IdMoneda"
									,'MOVABN'   AS "IdTipoMovimiento"
									,'RESCFM'   AS "IdConcepto"
									,'RESCATE_FONDO_MUTUO' AS "Descripcion"
									,'T0' AS "IdCondicionDeLiquidacion"
									,"FechaCertificacion" AS "FechaCondicionDeLiquidacion"
									,"CostoMonedaLocal" AS "Monto"
									,"IdUsuario" AS "IdEjecutivo"
									,0 AS "IdSucursal"
									,'INFM' AS "IdTipoProducto"
									,'APORFM' AS "IdTipoOperacion"
									,"IdSolicitudAporteRescate" AS "IdOperacion"
									,"FechaCertificacion" AS "FechaOperacion"
									,"FechaLiquidacion" AS "FechaLiquidacionOperacion"
									,null AS "FechaVencimientoOperacion"
									,"IdMoneda" AS "IdMonedaOperacion"
									,"CostoMonedaLocal" AS "MontoOperacion"
									,"ParidadFechaInversion" AS "TipoCambioOperacion"
									,null AS "IdContraparte"
									,'FFM' AS "IdTipoComprobante"
									,"IdSolicitudAporteRescate" AS "IdComprobante"
									,"IdSolicitudAporteRescate" AS "FolioComprobante"
									,"IdSolicitudAporteRescate" AS "IdComprobanteDetalle"
									,"IdSolicitudAporteRescate" AS "IdComprobanteFormaDePago"
									,'' AS "IdTipoMovimientoAsociado"
									,0 AS "IdMovimientoAsociado"
									,"FechaLiquidacion" AS "FechaComprobante"
									,null AS "IdPersonaPagoCobro"
									,"IdInstrumento" AS "IdInstrumento"
									,"IdTipoInstrumento" AS "IdTipoInstrumento"
									,"IdSerieFondo" AS "Nemo"
									,null AS "EsIndicadorBilateral"
									,'N' AS "EsCargoAbonoPorTraspaso"
									,null AS "IdPortafolioTraspaso"
									,'N' AS "GeneroContraparteEnTesoreria"
									,'S' AS "CertificadoAutomatico"
							FROM #RESULTADO_CertificacionSolicitudAporteRescateMasiva
							WHERE "Rechazo" = 0 AND ("IdMedioDePago" = 'CIN' AND "TipoSolicitud" = 'RES') AND "IdMonedaFondo" != "IdMoneda");
	

CALL "BSCL.CuentaInversion.BD.Servicios::CrearCargoAbono" (
				 p_IdEmpresa				=> 1
			    ,p_IdUsuario				=> 21
			    ,p_IdModulo					=> 'FMM'
			    ,p_simularOperacion			=> 'N'
				,p_UsaReglasNegocio			=> 'S'
				,p_FechaSistema				=> '2016-07-25'
				,p_CargoAbonoAGenerar		=> #cargoAbono
				,p_EsIngresoManual			=> 'N'
				,p_out_CargoAbonoGenerado	=> ?
				,p_out_ResultadoProceso		=> ?
				,p_out_InformacionContador	=> ?
				,p_out_InformacionDetalle	=> ?
				,p_out_AdvertenciasContador	=> ?
				,p_out_AdvertenciasDetalle	=> ?
				,p_out_ErroresContador		=> ?
				,p_out_ErroresDetalle		=> ?
			);
