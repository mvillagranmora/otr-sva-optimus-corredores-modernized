SET SCHEMA "BSCL";
DROP table #cargoAbono;
DROP table #RESULTADO_LiquidacionSolicitudAporteRescateMasiva;
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

create local temporary table #RESULTADO_LiquidacionSolicitudAporteRescateMasiva(
			"IdInterno"				   		BIGINT,
			"IdSolicitudAporteRescate" 		BIGINT,
			"Codigo" 			 	   		NVARCHAR(100),
			"Descripcion" 		       		NVARCHAR(520),
			"TipoSolicitud"			   		NVARCHAR(3),
			"IdSerieFondo"			   		NVARCHAR(40),
			"IdInstrumento"			   		BIGINT,
			"IdTipoInstrumento"		   		NVARCHAR(3),
			"IdPortafolio"			   		BIGINT,
			"IdMoneda"				   		NVARCHAR(3),
			"IdMonedaFondo"			   		NVARCHAR(3),
			"CostoMonedaLocal"		   		DECIMAL(26,8),
			"Monto"					   		DECIMAL(26,8),
			"ParidadFechaInversion"	   		DECIMAL(26,8),
			"FechaLiquidacion"		   		DATE,
			"IdUsuario"				   		BIGINT,
			"IdPortafolioAGF"		   		BIGINT,
			"IdMedioDePago"			   		NVARCHAR(3),
			"IdPersonaAGF"			   		BIGINT,
			"IdMedioDePagoAbonoAGF"			NVARCHAR(3),
			"IdMedioDePagoCargoAGF"			NVARCHAR(3),
			"EstadoLiquidacion"				NVARCHAR(3),
			"MueveCta"						NVARCHAR(1),
			"Rechazo"				   		BIGINT
);

INSERT INTO #RESULTADO_LiquidacionSolicitudAporteRescateMasiva
SELECT 0 AS "IdInterno",
				sol."IdSolicitudAporteRescate", 
				'UI_EXITO' AS "Codigo",
				'' as "Descripcion",
				sol."TipoSolicitud",
				sol."IdSerieFondo",
				sol."IdInstrumento",
				sol."IdTipoInstrumento",
				sol."IdPortafolio",
				sol."IdMoneda",
				sol."IdMonedaFondo",
				sol."CostoMonedaLocal",
				sol."Monto",
				sol."ParidadFechaInversion",
				sol."FechaLiquidacion",
				sol."IdUsuario",
				sol."IdPortafolioAGF",
				sol."IdMedioDePago",
				sol."IdPersonaAGF",
				sol."IdMedioDePagoAbonoAGF",
				sol."IdMedioDePagoCargoAGF",
				sol."EstadoLiquidacion",
				sol."MueveCta",
				0 AS "Rechazo"
FROM "BSCL"."BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" AS sol
WHERE sol."FechaLiquidacion" = '2016-09-07';

SELECT * FROM #RESULTADO_LiquidacionSolicitudAporteRescateMasiva;

INSERT INTO #cargoAbono
SELECT ROW_NUMBER() OVER (ORDER BY "IdOperacion") AS "NumeroCargoAbono", * FROM(
	        					SELECT "IdPortafolio" AS "IdPortafolio",
		        					"IdMoneda" AS "IdMoneda"
		        					,'MOVCAR'"IdTipoMovimiento"
									,'APORFM' AS "IdConcepto"
									,'APORTE_FONDO_MUTUO' AS "Descripcion"
									,'T0' AS "IdCondicionDeLiquidacion"
									,"FechaLiquidacion" AS "FechaCondicionDeLiquidacion"
									,"CostoMonedaLocal" AS "Monto"
									,"IdUsuario" AS "IdEjecutivo"
									,0 AS "IdSucursal"
									,'INFM' AS "IdTipoProducto"
									,'APORFM' AS "IdTipoOperacion"
									,"IdSolicitudAporteRescate" AS "IdOperacion"
									,"FechaLiquidacion" AS "FechaOperacion"
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
								FROM #RESULTADO_LiquidacionSolicitudAporteRescateMasiva
								WHERE "Rechazo" = 0 AND "TipoSolicitud" = 'APO' AND "MueveCta" = 'S' AND "IdMonedaFondo" != "IdMoneda"
								UNION ALL
								SELECT "IdPortafolio" AS "IdPortafolio",
		        					"IdMonedaFondo" AS "IdMoneda"
		        					,'MOVABN'"IdTipoMovimiento"
									,'APORFM' AS "IdConcepto"
									,'APORTE_FONDO_MUTUO' AS "Descripcion"
									,'T0' AS "IdCondicionDeLiquidacion"
									,"FechaLiquidacion" AS "FechaCondicionDeLiquidacion"
									,"Monto" AS "Monto"
									,"IdUsuario" AS "IdEjecutivo"
									,0 AS "IdSucursal"
									,'INFM' AS "IdTipoProducto"
									,'APORFM' AS "IdTipoOperacion"
									,"IdSolicitudAporteRescate" AS "IdOperacion"
									,"FechaLiquidacion" AS "FechaOperacion"
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
								FROM #RESULTADO_LiquidacionSolicitudAporteRescateMasiva
								WHERE "Rechazo" = 0 AND "TipoSolicitud" = 'APO' AND "MueveCta" = 'S' AND "IdMonedaFondo" != "IdMoneda"
								UNION ALL
	        					SELECT "IdPortafolio" AS "IdPortafolio"
								,"IdMonedaFondo" AS "IdMoneda"
								,'MOVCAR' AS "IdTipoMovimiento"
								,'APORFM' AS "IdConcepto"
								,'APORTE_FONDO_MUTUO' AS "Descripcion"
								,'T0' AS "IdCondicionDeLiquidacion"
								,"FechaLiquidacion" AS "FechaCondicionDeLiquidacion"
								,"Monto" AS "Monto"
								,"IdUsuario" AS "IdEjecutivo"
								,0 AS "IdSucursal"
								,'INFM' AS "IdTipoProducto"
								,'APORFM' AS "IdTipoOperacion"
								,"IdSolicitudAporteRescate" AS "IdOperacion"
								,"FechaLiquidacion" AS "FechaOperacion"
								,"FechaLiquidacion" AS "FechaLiquidacionOperacion"
								,null AS "FechaVencimientoOperacion"
								,"IdMonedaFondo" AS "IdMonedaOperacion"
								,"Monto" AS "MontoOperacion"
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
						FROM #RESULTADO_LiquidacionSolicitudAporteRescateMasiva
	                    WHERE "Rechazo" = 0 AND "TipoSolicitud" = 'APO' AND "MueveCta" = 'S');
	

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
