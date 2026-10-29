SET SCHEMA "BSCL";
DROP table #ingresoEgreso;
DROP table #tipoPago;
create local temporary table #ingresoEgreso(
										"NumeroReferencia"						BIGINT, 
										"IdTipoMovimiento"  						NVARCHAR(6),  -- MOVING, MOVEGR
									   	"IdTipoPersona"								NVARCHAR(3),  -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)
									   	"IdPersona"    								BIGINT,   
									   	"IdMoneda"									NVARCHAR(3),
									   	"Monto" 									DECIMAL(26,8),
									   	"FechaLiquidacion"           				NVARCHAR(10),                                                                      
									   	"IdConcepto" 								NVARCHAR(6),
									   	"IdTipoPersonaTitularPago"					NVARCHAR(3),  -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)
									   	"IdPersonaTitularPago"    					BIGINT,
									   	"IdPortafolioTitularPago"					BIGINT,
									   	"IdCaja"   									BIGINT,
									   	"SecuenciaAgrupacion" 						NVARCHAR(40),
									   	"Glosa" 									NVARCHAR(260),
									   	
									   	-- Parámetros Opcionales(Referenciales)  
									   	"IdAgente" 									BIGINT,
									   	"IdSucursal" 								BIGINT,
									   	"IdTipoProducto"   							NVARCHAR(4),
									    "IdTipoOperacion" 							NVARCHAR(6),
									    
									    "IdTipoComprobanteFacturacion"				NVARCHAR(3),
									   	"IdComprobanteFacturacion"					BIGINT,
									   	"FechaComprobanteFacturacion"				NVARCHAR(40),
									    "FechaLiquidacionComprobanteFacturacion"	NVARCHAR(40),
									    
									    "IdOperacion" 								BIGINT,			
									    "FechaOperacion"							NVARCHAR(40),
									    "IdCondicionLiquidacion"					NVARCHAR(2),
									    "IdBolsa" 									NVARCHAR(3),
									    "IdMonedaOperacion" 						NVARCHAR(3),
									    "MontoOperacion" 							DECIMAL(26,8),
									    "IdTipoReajuste" 							NVARCHAR(3),
									    "FechaParidad" 								NVARCHAR(40),
									    "ValorParidad" 								DECIMAL(26,8),
									    "FolioCartera" 								BIGINT,
									    "IdInstrumento" 							BIGINT,
									    "IdTipoInstrumento" 						NVARCHAR(3),
									    "IdClasificacionTipoInstrumento" 			NVARCHAR(3),
									  
									   -- Indicarores
									    "Bilateral"									NVARCHAR(1), --Cleanimg
									    "GeneraComprobante"							NVARCHAR(1), --S: genera comprobante automático									    
									    "AgruparEnComprobante"						NVARCHAR(1), --S: permite agrupar varios mov. en un comprobante
										"MueveCuentaInversion"						NVARCHAR(1), --S: mueve cuenta inversion N: No mueve cuenta inversion
									    "TitularPago"								NVARCHAR(1)  --S: Socio que genera movimiento es quién cobra o paga
);

create local temporary table #tipoPago(
	"NumeroReferencia"				BIGINT,   
	"IdMedioDePago"						NVARCHAR(3), -- CHE, EFE, TRA, VVI, CIN
 	"IdModalidadDePago"					NVARCHAR(6), -- MODCAJ: Por Caja   MODDEP: Depósito
	"IdMoneda"							NVARCHAR(3),
	"Monto"								DECIMAL(26,8),									
	"IdCuentaBancariaCorredora"			BIGINT,
	"IdTarjetaBancariaCorredora"		BIGINT,
	"IdChequera"						BIGINT,
	"NumeroDocumentoCorredora"			BIGINT,
	"FechaDocumentoPagoCorredora"		NVARCHAR(40),
	"IdCuentaBancariaSocio" 			BIGINT,
	"IdBancoSocio"						BIGINT,
	"NombreCuentaBancariaSocio"			NVARCHAR(100),
	"NumeroCuentaBancariaSocio"			NVARCHAR(100),
	"IdTipoCuentaBancariaSocio"			NVARCHAR(4),
	"NombreTipoCuentaBancariaSocio"		NVARCHAR(100),
	"IdTarjetaBancariaSocio"			BIGINT,
	"IdTipoTarjetaBancariaSocio"		NVARCHAR(40),
	"NombreTipoTarjetaBancariaSocio"	NVARCHAR(100),
	"NumeroTarjetaBancariaSocio"		NVARCHAR(100),
	"NumeroSerieSocio"					NVARCHAR(40),
	"NumeroDocumentoSocio"				BIGINT,
	"FechaDocumentoPagoSocio"			NVARCHAR(40)
);

INSERT INTO #ingresoEgreso
SELECT ROW_NUMBER() OVER(ORDER BY "IdOperacion") AS "NumeroReferencia", * FROM(
							SELECT 
									CASE "TipoSolicitud" WHEN 'APO' THEN 'MOVEGR' ELSE 'MOVING' END AS "IdTipoMovimiento",
									'CLI' AS "IdTipoPersona",
									"IdPersona" AS "IdPersona",
									"IdMoneda" AS "IdMoneda",
									"Monto" AS "Monto",
									"FechaLiquidacion" AS "FechaLiquidacion",
									CASE "TipoSolicitud" WHEN 'APO' THEN 'APORFM' ELSE 'RESCFM' END AS "IdConcepto",
									'CLI'  AS "IdTipoPersonaTitularPago",
									"IdPersona" AS "IdPersonaTitularPago",
									"IdPortafolio" AS "IdPortafolioTitularPago",
									0 AS "IdCaja",
									'' AS "SecuenciaAgrupacion",
									'' AS "Glosa",
									0 AS "IdAgente",
									0 AS "IdSucursal",
									'INFM' AS "IdTipoProducto",
									CASE "TipoSolicitud" WHEN 'APO' THEN 'APORFM' ELSE 'RESCFM' END AS "IdTipoOperacion",
									'' AS "IdTipoComprobanteFacturacion",
									0 AS "IdComprobanteFacturacion",
									'0001-01-01' AS "FechaComprobanteFacturacion",
									'0001-01-01' AS "FechaLiquidacionComprobanteFacturacion",
									"IdSolicitudAporteRescate" AS "IdOperacion",
									"FechaCertificacion" AS "FechaOperacion",
									'T0' AS "IdCondicionLiquidacion",
									'BCS'	AS "IdBolsa",
									"IdMoneda" AS "IdMonedaOperacion",
									"Monto" AS "MontoOperacion",
									'' AS "IdTipoReajuste",
									"FechaCertificacion" AS "FechaParidad",
									"ParidadFechaInversion" AS "ValorParidad",
									0 AS "FolioCartera",
									"IdInstrumento" AS "IdInstrumento",
									"IdTipoInstrumento" AS "IdTipoInstrumento",
									'' AS "IdClasificacionTipoInstrumento",
									'N' AS "Bilateral",
									'N' AS "GeneraComprobante",
									'N' AS "AgruparEnComprobante",
									'N' AS "MueveCuentaInversion",
									'N' AS "TitularPago" 
        					FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate"
                    		WHERE "EstadoSolicitud" = 'ING' AND "FormaPago" != 0 AND "FechaCertificacion" = '2016-08-19'
                    		UNION ALL
                    		SELECT
                    				CASE "TipoSolicitud" WHEN 'APO' THEN 'MOVEGR' ELSE 'MOVING' END AS "IdTipoMovimiento",
									'CLI' AS "IdTipoPersona",
									"IdPersonaAGF" AS "IdPersona",
									"IdMoneda" AS "IdMoneda",
									"Monto" AS "Monto",
									"FechaLiquidacion" AS "FechaLiquidacion",
									CASE "TipoSolicitud" WHEN 'APO' THEN 'APORFM' ELSE 'RESCFM' END AS "IdConcepto",
									'CLI'  AS "IdTipoPersonaTitularPago",
									"IdPersonaAGF" AS "IdPersonaTitularPago",
									"IdPortafolioAGF" AS "IdPortafolioTitularPago",
									0 AS "IdCaja",
									'' AS "SecuenciaAgrupacion",
									'' AS "Glosa",
									0 AS "IdAgente",
									0 AS "IdSucursal",
									'INFM' AS "IdTipoProducto",
									CASE "TipoSolicitud" WHEN 'APO' THEN 'APORFM' ELSE 'RESCFM' END AS "IdTipoOperacion",
									'' AS "IdTipoComprobanteFacturacion",
									0 AS "IdComprobanteFacturacion",
									'0001-01-01' AS "FechaComprobanteFacturacion",
									'0001-01-01' AS "FechaLiquidacionComprobanteFacturacion",
									"IdSolicitudAporteRescate" AS "IdOperacion",
									"FechaCertificacion" AS "FechaOperacion",
									'T0' AS "IdCondicionLiquidacion",
									'BCS'	AS "IdBolsa",
									"IdMoneda" AS "IdMonedaOperacion",
									"Monto" AS "MontoOperacion",
									'' AS "IdTipoReajuste",
									"FechaCertificacion" AS "FechaParidad",
									"ParidadFechaInversion" AS "ValorParidad",
									0 AS "FolioCartera",
									"IdInstrumento" AS "IdInstrumento",
									"IdTipoInstrumento" AS "IdTipoInstrumento",
									'' AS "IdClasificacionTipoInstrumento",
									'N' AS "Bilateral",
									'N' AS "GeneraComprobante",
									'N' AS "AgruparEnComprobante",
									'N' AS "MueveCuentaInversion",
									'N' AS "TitularPago"
							FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate"
                    		WHERE "EstadoSolicitud" = 'ING' AND "FechaCertificacion" = '2016-08-19');

INSERT INTO #tipoPago
SELECT
							   	"IngresoEgreso"."NumeroReferencia",
							   	CASE WHEN "Solicitudes"."IdPersonaAGF" = "IngresoEgreso"."IdPersona" THEN 'CIN' ELSE "IdMedioDePago" END AS "IdMedioDePago",
							   	'MODCAJ'   AS "IdModalidadDePago",
							   	"Solicitudes"."IdMoneda" AS "IdMoneda",
							   	"Solicitudes"."Monto" AS "Monto",
								0 AS "IdCuentaBancariaCorredora",
								0 AS "IdTarjetaBancariaCorredora",
								0 AS "IdChequera",
								0 AS "NumeroDocumentoCorredora",
								'0001-01-01' AS "FechaDocumentoPagoCorredora",
								0 AS "IdCuentaBancariaSocio",
								0 AS "IdBancoSocio",
								'' AS "NombreCuentaBancariaSocio",
								'' AS "NumeroCuentaBancariaSocio",
								'' AS "IdTipoCuentaBancariaSocio",
								'' AS "NombreTipoCuentaBancariaSocio",
								0 AS "IdTarjetaBancariaSocio",
								'' AS "IdTipoTarjetaBancariaSocio",
								'' AS "NombreTipoTarjetaBancariaSocio",
								'' AS "NumeroTarjetaBancariaSocio",
								'' AS "NumeroSerieSocio",
								0 AS "NumeroDocumentoSocio",
								'0001-01-01' AS "FechaDocumentoPagoSocio"
        				FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" AS "Solicitudes"
        				INNER JOIN #ingresoEgreso AS "IngresoEgreso" ON ("Solicitudes"."IdSolicitudAporteRescate" = "IngresoEgreso"."IdOperacion");
SELECT "IdOperacion", * FROM #ingresoEgreso;
SELECT * FROM #tipoPago;
	
CALL "BSCL.Tesoreria.BD.Servicios::CrearIngresoEgreso" (
				 p_IdEmpresa				=> 1
			    ,p_IdUsuario				=> 21
			    ,p_IdModulo					=> 'FMM'
			    ,p_simularOperacion			=> 'N'
				,p_UsaReglasNegocio			=> 'S'
				,p_IngresoEgresoGenerar		=> #ingresoEgreso
				,p_TableFormaPago			=> #tipoPago
				,p_EsIngresoAutomatico		=> 'N'
				,p_out_IngresoEgresoGenerado => ?
				,p_out_ResultadoProceso		=> ?
				,p_out_AdvertenciasContador	=> ?
				,p_out_AdvertenciasDetalle	=> ?
				,p_out_ErroresContador		=> ?
				,p_out_ErroresDetalle		=> ?
			);
