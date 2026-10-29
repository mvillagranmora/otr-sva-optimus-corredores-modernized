
SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1962.Origen";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1962.Origen" (
    "ID"
    ,"IdEmpresa"
    ,"IdDocumento"
    ,"VboUsuario"
    ,"Observaciones"
    ,"FuenteDatos"
   	,"IdOperacion"
    ,"IdEmisor"	
    ,"Fondo"
    ,"IdCliente"
    ,"IdPortafolio"				
	,"RutAdministradora"						
	,"RutFondoB"	
    ,"RunFondo"
    ,"TipoFondo"
    ,"TributaSa"
    ,"DetBenNetoPerPOS_C6"
    ,"DetBenNetoPerNEG_C7"
    ,"DetBenNetoUtilPer_C8"
    ,"DetBenNetoIntPer_C9"
    ,"DetBenNetoDivPer_C10"
    ,"DetBenNetoGanCapPer_C11"
    ,"DetBenNetoPerGastDev_C12"
    ,"Numero"
    ,"RutFondoC"
    ,"RutCliente"
    ,"NombreParticipe"
    ,"TaxId"
    ,"CodigoPais"
    ,"MonedaReajuste"
	,"MontoAporteNominal"
    ,"MontoNominalAportesActualizado"
    ,"CantidadCuotasParticipe"
    ,"ParticipeCumpleRequisitos"
    ,"CuotasCumplenRequisitos"
    ,"ValorCuotaPesos"
    ,"PorcentajeParticipacion31Dic"
    ,"TipoContribuyente"
    ,"FechaRespaldo"
	,"FolioCartera"
	,"FechaOperacion"
	,"IdCartera"
) AS

SELECT
    0                              AS "ID"
    ,LG."IdEmpresa"                 AS "IdEmpresa"	
    ,0                              AS "IdDocumento"
    ,''                             AS "VboUsuario"
    ,''                             AS "Observaciones"
    ,''                             AS "FuenteDatos"
    ,0                              AS "IdOperacion"
    ,COALESCE(emi."IdEmisor",'')    AS "IdEmisor"
    ,CASE WHEN COALESCE (fondo."NemoCMF",LG."Fondo",'') = '' THEN LG."Fondo" ELSE COALESCE (fondo."NemoCMF",LG."Fondo",'') END  AS "Fondo"--,LG."Fondo"                     AS "Fondo"
    ,LG."IdCliente" 		        AS "IdCliente"
    ,LG."IdPortafolio" 		        AS "IdPortafolio"

    -- COMIENZO SECCION B TODO 0 O VACIO

    ,''                             AS "RutAdministradora"
    ,''                             AS "RutFondoB"
    ,''                             AS "RunFondo"
    ,''                             AS "TipoFondo"
    ,''                             AS "TributaSa"
    ,0                            AS "DetBenNetoPerPOS_C6"
    ,0                            AS "DetBenNetoPerNEG_C7"
    ,0                            AS "DetBenNetoUtilPer_C8"
    ,0                            AS "DetBenNetoIntPer_C9"
    ,0                             AS "DetBenNetoDivPer_C10"
    ,0                            AS "DetBenNetoGanCapPer_C11"
    ,0                             AS "DetBenNetoPerGastDev_C12"
    -- FIN SECCION B
    
    -- COMIENZO SECCION C TipoContribuyente "1" "2" o "3" 

    ,0 AS "Numero"
    ,COALESCE(RutFondosFM."ValorDestino",Rutfondos."ValorDestino",'') AS "RutFondoC"-- BUSCA RUT DE FONDOS PARA FFMM, CFI,CFM
    ,cli."DocumentoIdentidad"                           AS "RutCliente"
    ,cli."NombreCompleto"                               AS "NombreParticipe"
    ,'0'                                                AS "TaxId"
    ,COALESCE (pais."CodigoPais", 'CL')                 AS "CodigoPais"
    ,LG."MonedaReajuste"                                AS "MonedaReajuste"
	,LG."MontoAporteNominal"							AS "MontoAporteNominal"
    ,LG."MontoActualizado"                              AS "MontoNominalAportesActualizado"	
    ,LG."NumCuotasSerieParticipe"                       AS "CantidadCuotasParticipe"
    ,LG."ParticipeCumpleReq"                            AS "ParticipeCumpleRequisitos"
    ,LG."CuotasCumplenReq"                              AS "CuotasCumplenRequisitos"
    ,round(LG."ValorCuotaPesos",0) 		            AS "ValorCuotaPesos"
	--CASE WHEN LG."IdMonedaInstrumento"='CLP' THEN COALESCE( PrecInst."PrecioCierre",PrecFM."ValorCuota" ,LG."ValorCuotaPesos",0)
    --ELSE COALESCE( round (PrecInst."PrecioCierre"*ReajFondo."Valor",6),PrecFM."ValorCuota" ,LG."ValorCuotaPesos",0) END AS "ValorCuotaPesos" -- PRIMERA OPCION TABLA DE VALOR DE FM HISTORICO O BOLSA INSTRUMENTO PRECIO
    ,0                                                  AS "PorcentajeParticipacion31Dic"
    ,'2'                                                AS "TipoContribuyente"

	-- CONTROL CUADRATURA CON CARTERA
    ,LG."FechaRespaldo"                                	AS "FechaRespaldo"
	,LG."FolioCartera"									AS "FolioCartera"
	,LG."FechaOperacion"								AS "FechaOperacion"
	,LG."IdCartera"										AS "IdCartera"

FROM 
(			-- CARGA DE CFI CFM
SELECT  
                    HIS."IdClasificacionTipoInstrumento",
                    HIS."IdEmpresa",
					HIS."IdCliente",
					HIS."IdPortafolio",
					HIS."Nemotecnico" AS "Fondo",--Fondo
					CASE 	WHEN HIS."IdMonedaInstrumento" = 'USD' THEN 2
							WHEN HIS."IdMonedaInstrumento" = 'EUR' THEN 3
							WHEN HIS."IdMonedaInstrumento" = 'UF'  THEN 4
							WHEN HIS."IdMonedaInstrumento" = 'IVP' THEN 5
							WHEN HIS."IdMonedaInstrumento" = 'CLP' THEN 1
							ELSE 6 -- Otros
					END AS "MonedaReajuste" , --MonedaReajuste
					HIS."MontoAporteNominal" AS "MontoAporteNominal",
					COALESCE(HIS."MtoNominalAporteActualizado", 0) AS "MontoActualizado",--MontoActualizado
					HIS."INGCantidad" as "NumCuotasSerieParticipe",--NumCuotasSerieParticipe
                    3 AS "ParticipeCumpleReq",--ParticipeCumpleReq
					3 AS "CuotasCumplenReq",--CuotasCumplenReq
					HIS."INGPrecioMercadoActual" as "ValorCuotaPesos",
					'CARGAINI' AS "Origen" , 
					'OPT_CFIM' AS "OrigenDet",
					'' AS "VBUsuario",--VBUsuario				varchar
					'' AS "Observacion",--Observacion			varchar          --- cantidad
					HIS."FechaRespaldo",
					HIS."IdInstrumento",
					HIS."FolioCartera",			-- Folio cartera
					HIS."FechaOperacion",
					HIS."IdMonedaInstrumento",
					HIS."IdCartera"
					FROM 
					(
						SELECT 
                        HISFEC."IdClasificacionTipoInstrumento",
                        HISFEC."FechaRespaldo",
                        HISFEC."FechaOperacionAporte" AS "FechaOperacion",
						HISFEC."IdEmpresa",
						HISFEC."IdCliente",
						HISFEC."IdPortafolio", 
						HISFEC."Nemotecnico",
                        HISFEC."INGCantidad",
						HISFEC."IdInstrumento",
						HISFEC."MtoNominalAporteActualizado",
						HISFEC."INGPrecioMercadoActual",
						HISFEC."IdMonedaInstrumento",
						HISFEC."MontoAporteNominal",
						HISFEC."FolioCartera",
						HISFEC."IdCartera"
								FROM (SELECT H."IdEmpresa",H."IdCliente", H."FechaOperacion", H."IdPortafolio",H."Nemotecnico",H."IdClasificacionTipoInstrumento",H."IdMonedaInstrumento", H."IdInstrumento",H."IdCartera",
											H."IdTipoInstrumento",H."IdEstado",COALESCE(H."IdRegimenFondoMutuo",'') AS "IdRegimenFondoMutuo",
											H."Cantidad",H."MontoCompra",H."FechaRespaldo",
											sum(round(H."INGMontoATasaMercadoActual",0)) AS "INGMontoATasaMercadoActual",
											sum(round(H."MontoCompra" * (H."INGCantidad" / NULLIF(H."Cantidad", 0)))) AS "MontoAporteNominal",
											sum(round(((H."INGCantidad" / NULLIF(H."Cantidad", 0)) * H."MontoCompra") * (NULLIF(MONREAJUSTEACT."Valor", 0) / NULLIF(MONREAJUSTEINV."Valor", 0)), 0)) as "MtoNominalAporteActualizado",
											sum(H."INGCantidad") AS "INGCantidad"
											,CASE WHEN H."IdMonedaInstrumento" = 'USD' THEN max(round((H."INGPrecioMercadoActual" * PAR."Valor"),2)) ELSE 	max(H."INGPrecioMercadoActual") END as "INGPrecioMercadoActual"
											,H."FolioCartera"
											,H."FechaOperacion" AS "FechaOperacionAporte"
											,RANK() over (partition by  H."FolioCartera" order by H."IdCartera" desc) as RANK ---Solo ultimo reg de cartera (Saldo final del periodo)
										FROM "BSCL.Custodia.BD::HIS.Cartera" H
										INNER JOIN
											(			
												SELECT TOP 5 "FechaRespaldoFinal" FROM ---Fechas finales de respaldo por periodo (acotado a 5 periodos 
												(
													SELECT  MAX("FechaRespaldo") AS "FechaRespaldoFinal" ,YEAR("FechaRespaldo") FROM  "BSCL.Custodia.BD::HIS.Cartera" 
													WHERE   "IdClasificacionTipoInstrumento"='RV' 
													AND "IdTipoInstrumento" IN ('CFI','ETN') 
													AND MONTH("FechaRespaldo")='12'
													AND "IdEstado" IN ('VIG','LIQ')
													AND "INGMontoATasaMercadoActual" > 0
													AND "IdRegimenFondoMutuo" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R','57B','57T')
													GROUP BY YEAR("FechaRespaldo")
													ORDER BY YEAR("FechaRespaldo") DESC
												)
											)GRFECHA	
										ON 	H."FechaRespaldo"=GRFECHA."FechaRespaldoFinal"
										AND H."IdClasificacionTipoInstrumento"='RV' 
										AND H."IdTipoInstrumento" IN ('CFI','ETN') 
										AND MONTH(H."FechaRespaldo")='12'
										--AND H."IdEstado" = 'VIG'
										AND H."INGMontoATasaMercadoActual" > 0
										AND H."IdRegimenFondoMutuo" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R','57B','57T')
										
										LEFT OUTER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" MONREAJUSTEINV ON MONREAJUSTEINV."IdParametroFinanciero" = (CASE "IdMonedaInstrumento" WHEN 'USD' THEN 'DO' WHEN 'CLP' THEN 'IPC' WHEN 'IVP' THEN 'IPC' ELSE "IdMonedaInstrumento" END) 
																													AND MONREAJUSTEINV."Fecha" = 
																									( CASE "IdMonedaInstrumento"
																											WHEN  'USD' THEN
																													TO_DATE("FechaOperacion", 'YYYY-MM-DD')
																											WHEN  'EUR' THEN
																													TO_DATE("FechaOperacion", 'YYYY-MM-DD')
																											WHEN  'UF' THEN
																													(YEAR(TO_DATE("FechaOperacion", 'YYYY-MM-DD')) ||'-12-31')
																											WHEN 'CLP' THEN
																												    TO_DATE(TO_VARCHAR(YEAR("FechaOperacion")) || '-' || LPAD(TO_VARCHAR(MONTH("FechaOperacion")), 2, '0') || '-01', 'YYYY-MM-DD')

																											ELSE
																												    (YEAR(TO_DATE("FechaOperacion", 'YYYY-MM-DD')) ||'-12-01')
																										END) 
									  
									    
									    		LEFT OUTER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" MONREAJUSTEACT ON MONREAJUSTEACT."IdParametroFinanciero" = (CASE "IdMonedaInstrumento" WHEN 'USD' THEN 'DO' WHEN 'CLP' THEN 'IPC' WHEN 'IVP' THEN 'IPC' ELSE "IdMonedaInstrumento" END)
									    													AND MONREAJUSTEACT."Fecha" = ( CASE "IdMonedaInstrumento"
																											WHEN  'USD' THEN
																													TO_DATE(TO_VARCHAR(YEAR("FechaRespaldo") + 1) || '-01-01', 'YYYY-MM-DD')
																											WHEN  'EUR' THEN
																													TO_DATE(TO_VARCHAR(YEAR("FechaRespaldo") + 1) || '-01-01', 'YYYY-MM-DD')
																											WHEN  'UF' THEN
																													TO_DATE( TO_VARCHAR(YEAR("FechaRespaldo")) || '-12-31', 'YYYY-MM-DD')

																											WHEN 'CLP' THEN
																												    TO_DATE( TO_VARCHAR(YEAR("FechaRespaldo")) || '-11-01', 'YYYY-MM-DD')

																											ELSE
																												    TO_DATE( TO_VARCHAR(YEAR("FechaRespaldo")) || '-12-01', 'YYYY-MM-DD')

																										END) 
										
									 LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" PAR ON H."FechaOperacion" = PAR."Fecha"     AND PAR."IdParametroFinanciero" = 'DO'
									 LEFT JOIN "BSCL.Custodia.BD::HIS.Garantia" GTA ON GTA."IdCarteraOrigen" = H."IdCartera" AND GTA."FechaRespaldo" = H."FechaRespaldo" 
									 WHERE H."IdEstado"='VIG' OR (H."IdEstado"='LIQ' AND H."EGRIdTipoOperacion" = 'INGGTA'  AND GTA."IdEstado" = 'VIG') 
									 GROUP BY H."IdCliente",H."IdPortafolio",H."Nemotecnico",H."IdClasificacionTipoInstrumento",H."IdMonedaInstrumento",H."IdInstrumento",H."IdTipoInstrumento",H."IdRegimenFondoMutuo",H."FechaRespaldo",H."IdEmpresa",H."FechaOperacion"
											,H."IdEstado",H."Cantidad",H."MontoCompra",H."FolioCartera",H."IdCartera",H."IdCliente"				  
									 )HISFEC WHERE HISFEC.RANK = 1

					UNION ALL
							
					-- Obtener Garantias RPA SOBRE CARTERAS VIGENTES
						SELECT 
                        HISFEC."IdClasificacionTipoInstrumento",
                        HISFEC."FechaRespaldo",
                        HISFEC."FechaOperacionAporte" AS "FechaOperacion",
						HISFEC."IdEmpresa",
						HISFEC."IdCliente",
						HISFEC."IdPortafolio", 
						HISFEC."Nemotecnico",
                        HISFEC."INGCantidad",
						HISFEC."IdInstrumento",
						HISFEC."MtoNominalAporteActualizado",
						HISFEC."INGPrecioMercadoActual",
						HISFEC."IdMonedaInstrumento",
						HISFEC."MontoAporteNominal",
						HISFEC."FolioCartera",
						HISFEC."IdCartera"
								FROM (SELECT H."IdEmpresa",H."IdCliente", H."FechaOperacion", H."IdPortafolio",H."Nemotecnico",H."IdClasificacionTipoInstrumento",H."IdMonedaInstrumento", H."IdInstrumento",H."IdCartera",
											H."IdTipoInstrumento",H."IdEstado",COALESCE(H."IdRegimenFondoMutuo",'') AS "IdRegimenFondoMutuo",
											H."Cantidad",H."MontoCompra",H."FechaRespaldo",
											sum(round(H."INGMontoATasaMercadoActual",0)) AS "INGMontoATasaMercadoActual",
											sum(round(H."MontoCompra" * (H."EGRCantidad" / NULLIF(H."Cantidad", 0)))) AS "MontoAporteNominal",
											sum(round(((H."EGRCantidad" / NULLIF(H."Cantidad", 0)) * H."MontoCompra") * (NULLIF(MONREAJUSTEACT."Valor", 0) / NULLIF(MONREAJUSTEINV."Valor", 0)), 0)) as "MtoNominalAporteActualizado",
											sum(CASE 
										        WHEN H."IdEstado" IN ('RPA') AND H."EGRIdTipoOperacion" = 'INGGTA' AND GTA."IdEstado" = 'VIG' THEN H."EGRCantidad"
										        ELSE 0 
										     END) AS "INGCantidad"
											,CASE WHEN H."IdMonedaInstrumento" = 'USD' THEN max(round((H."INGPrecioMercadoActual" * PAR."Valor"),2)) ELSE 	max(H."INGPrecioMercadoActual") END as "INGPrecioMercadoActual"
											,H."FolioCartera"
											,H."FechaOperacion" AS "FechaOperacionAporte"
											--,RANK() over (partition by  H."FolioCartera" order by H."IdCartera" desc) as RANK ---Solo ultimo reg de cartera (Saldo final del periodo)
										FROM "BSCL.Custodia.BD::HIS.Cartera" H
										INNER JOIN
											(			
												SELECT TOP 5 "FechaRespaldoFinal" FROM ---Fechas finales de respaldo por periodo (acotado a 5 periodos 
												(
													SELECT  MAX("FechaRespaldo") AS "FechaRespaldoFinal" ,YEAR("FechaRespaldo") FROM  "BSCL.Custodia.BD::HIS.Cartera" 
													WHERE   "IdClasificacionTipoInstrumento"='RV' 
													AND "IdTipoInstrumento" IN ('CFI','ETN') 
													AND MONTH("FechaRespaldo")='12'
													AND "IdEstado" = 'RPA'
													AND "INGMontoATasaMercadoActual" > 0
													AND "IdRegimenFondoMutuo" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R','57B','57T')
													GROUP BY YEAR("FechaRespaldo")
													ORDER BY YEAR("FechaRespaldo") DESC
												)
											)GRFECHA	
										ON 	H."FechaRespaldo"=GRFECHA."FechaRespaldoFinal"
										AND H."IdClasificacionTipoInstrumento"='RV' 
										AND H."IdTipoInstrumento" IN ('CFI','ETN') 
										AND MONTH(H."FechaRespaldo")='12'
										AND H."IdEstado" = 'RPA'
										AND H."INGMontoATasaMercadoActual" > 0
										AND H."IdRegimenFondoMutuo" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R','57B','57T')
										
										LEFT OUTER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" MONREAJUSTEINV ON MONREAJUSTEINV."IdParametroFinanciero" = (CASE "IdMonedaInstrumento" WHEN 'USD' THEN 'DO' WHEN 'CLP' THEN 'IPC' WHEN 'IVP' THEN 'IPC' ELSE "IdMonedaInstrumento" END) 
																													AND MONREAJUSTEINV."Fecha" = 
																									( CASE "IdMonedaInstrumento"
																											WHEN  'USD' THEN
																													TO_DATE("FechaOperacion", 'YYYY-MM-DD')
																											WHEN  'EUR' THEN
																													TO_DATE("FechaOperacion", 'YYYY-MM-DD')
																											WHEN  'UF' THEN
																													(YEAR(TO_DATE("FechaOperacion", 'YYYY-MM-DD')) ||'-12-31')
																											WHEN 'CLP' THEN
																												    TO_DATE(TO_VARCHAR(YEAR("FechaOperacion")) || '-' || LPAD(TO_VARCHAR(MONTH("FechaOperacion")), 2, '0') || '-01', 'YYYY-MM-DD')

																											ELSE
																												    (YEAR(TO_DATE("FechaOperacion", 'YYYY-MM-DD')) ||'-12-01')
																										END) 
									  
									    
									    		LEFT OUTER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" MONREAJUSTEACT ON MONREAJUSTEACT."IdParametroFinanciero" = (CASE "IdMonedaInstrumento" WHEN 'USD' THEN 'DO' WHEN 'CLP' THEN 'IPC' WHEN 'IVP' THEN 'IPC' ELSE "IdMonedaInstrumento" END)
									    													AND MONREAJUSTEACT."Fecha" = ( CASE "IdMonedaInstrumento"
																											WHEN  'USD' THEN
																													TO_DATE(TO_VARCHAR(YEAR("FechaRespaldo") + 1) || '-01-01', 'YYYY-MM-DD')
																											WHEN  'EUR' THEN
																													TO_DATE(TO_VARCHAR(YEAR("FechaRespaldo") + 1) || '-01-01', 'YYYY-MM-DD')
																											WHEN  'UF' THEN
																													TO_DATE( TO_VARCHAR(YEAR("FechaRespaldo")) || '-12-31', 'YYYY-MM-DD')

																											WHEN 'CLP' THEN
																												    TO_DATE( TO_VARCHAR(YEAR("FechaRespaldo")) || '-11-01', 'YYYY-MM-DD')

																											ELSE
																												    TO_DATE( TO_VARCHAR(YEAR("FechaRespaldo")) || '-12-01', 'YYYY-MM-DD')

																										END) 
										
									 LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" PAR ON H."FechaRespaldo" = PAR."Fecha"     AND PAR."IdParametroFinanciero" = 'DO'
									 LEFT JOIN "BSCL.Custodia.BD::HIS.Garantia" GTA ON GTA."IdCarteraOrigen" = H."IdCartera" AND GTA."FechaRespaldo" = H."FechaRespaldo" 
									 WHERE (H."IdEstado" IN ('RPA') AND H."EGRIdTipoOperacion" = 'INGGTA' AND GTA."IdEstado" = 'VIG') 
									 GROUP BY H."IdCliente",H."IdPortafolio",H."Nemotecnico",H."IdClasificacionTipoInstrumento",H."IdMonedaInstrumento",H."IdInstrumento",H."IdTipoInstrumento",H."IdRegimenFondoMutuo",H."FechaRespaldo",H."IdEmpresa",H."FechaOperacion"
											,H."IdEstado",H."Cantidad",H."MontoCompra",H."FolioCartera",H."IdCartera",H."IdCliente"				  
									 )HISFEC --WHERE HISFEC.RANK = 1

									
					)AS HIS 
				INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" por ON por."IdPortafolio"= HIS."IdPortafolio" 
 				WHERE  (por."CodigoPortafolio" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R','57B','57T') AND por."Proposito" NOT IN ('REGAPV'))
			UNION ALL
			
			
			-- CARGA DE FONDOS MUTUOS
			(SELECT  
                    HIS."IdClasificacionTipoInstrumento",
                    HIS."IdEmpresa",
					HIS."IdCliente",
					HIS."IdPortafolio",
					HIS."Nemotecnico" AS "Fondo",--Fondo
					CASE 	WHEN HIS."IdMonedaInstrumento" = 'USD' THEN 2
							WHEN HIS."IdMonedaInstrumento" = 'EUR' THEN 3
							WHEN HIS."IdMonedaInstrumento" = 'UF'  THEN 4
							WHEN HIS."IdMonedaInstrumento" = 'IVP' THEN 5
							WHEN HIS."IdMonedaInstrumento" = 'CLP' THEN 1
							ELSE 6 -- Otros
					END AS "MonedaReajuste" , --MonedaReajuste
					HIS."MontoAporteNominal" AS "MontoAporteNominal",
					COALESCE(HIS."MtoNominalAporteActualizado", 0) AS "MontoActualizado",--MontoActualizado
					HIS."INGCantidad" as "NumCuotasSerieParticipe",--NumCuotasSerieParticipe
                    3 AS "ParticipeCumpleReq",--ParticipeCumpleReq
					3 AS "CuotasCumplenReq",--CuotasCumplenReq
					HIS."INGPrecioMercadoActual" as "ValorCuotaPesos",
					'CARGAINI' AS "Origen" , 
					'OPT_FFM' AS "OrigenDet",
					'' AS "VBUsuario",--VBUsuario				varchar
					'' AS "Observacion",--Observacion			varchar          --- cantidad
					HIS."FechaRespaldo",
					HIS."IdInstrumento",
					HIS."FolioCartera",			-- Folio cartera
					HIS."FechaOperacion",
					HIS."IdMonedaInstrumento",
					HIS."IdCartera"
					FROM 
					(
						SELECT 
                        HISFEC."IdClasificacionTipoInstrumento",
                        HISFEC."FechaRespaldo",
                        HISFEC."FechaOperacionAporte" AS "FechaOperacion",
						HISFEC."IdEmpresa",
						HISFEC."IdCliente",
						HISFEC."IdPortafolio", 
						HISFEC."Nemotecnico",
                        HISFEC."INGCantidad",
						HISFEC."IdInstrumento",
						HISFEC."MtoNominalAporteActualizado",
						HISFEC."INGPrecioMercadoActual",
						HISFEC."IdMonedaInstrumento",
						HISFEC."MontoAporteNominal",
						HISFEC."FolioCartera",
						HISFEC."IdCartera"
								FROM (SELECT H."IdEmpresa",H."IdCliente", H."FechaOperacion", H."IdPortafolio",H."Nemotecnico",H."IdClasificacionTipoInstrumento",H."IdMonedaInstrumento", H."IdInstrumento",H."IdCartera",
											H."IdTipoInstrumento",H."IdEstado",COALESCE(H."IdRegimenFondoMutuo",'') AS "IdRegimenFondoMutuo",
											H."Cantidad",H."MontoCompra",H."FechaRespaldo",
											sum(round(H."INGMontoATasaMercadoActual",0)) AS "INGMontoATasaMercadoActual",
											sum(round(H."MontoCompra" * (H."INGCantidad" / NULLIF(H."Cantidad", 0)))) AS "MontoAporteNominal",
											sum(round(((H."INGCantidad" / NULLIF(H."Cantidad", 0)) * H."MontoCompra") * (NULLIF(MONREAJUSTEACT."Valor", 0) / NULLIF(MONREAJUSTEINV."Valor", 0)), 0)) as "MtoNominalAporteActualizado",
											sum(H."INGCantidad") AS "INGCantidad"
											--	Asi debiese ser pero esta mal cargado el valor del dolar en Optimus
											,CASE WHEN H."IdMonedaInstrumento" = 'USD' THEN max(round((H."INGPrecioMercadoActual" * PAR."Valor"),2)) ELSE 	max(H."INGPrecioMercadoActual") END as "INGPrecioMercadoActual"
											,H."FolioCartera"
											,H."FechaOperacion" AS "FechaOperacionAporte"
											,RANK() over (partition by  H."FolioCartera" order by H."IdCartera" desc) as RANK ---Solo ultimo reg de cartera (Saldo final del periodo)
										FROM "BSCL.Custodia.BD::HIS.Cartera" H
										INNER JOIN
											(			
												SELECT TOP 5 "FechaRespaldoFinal" FROM ---Fechas finales de respaldo por periodo (acotado a 5 periodos 
												(
													SELECT  MAX("FechaRespaldo") AS "FechaRespaldoFinal" ,YEAR("FechaRespaldo") FROM  "BSCL.Custodia.BD::HIS.Cartera" 
													WHERE   "IdClasificacionTipoInstrumento" IN ('RV','FM') 
													AND "IdTipoInstrumento" IN ('CFI','ETN','FFM') 
													AND MONTH("FechaRespaldo")='12'
													AND "IdEstado" IN ('VIG','LIQ')
													AND "INGMontoATasaMercadoActual" > 0
													AND "IdRegimenFondoMutuo" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R','57B','57T')
													GROUP BY YEAR("FechaRespaldo")
													ORDER BY YEAR("FechaRespaldo") DESC
												)
											)GRFECHA	
										ON 	H."FechaRespaldo"=GRFECHA."FechaRespaldoFinal"
										AND H."IdClasificacionTipoInstrumento" IN ('RV','FM')  
										AND H."IdTipoInstrumento" IN ('FFM') 
										AND MONTH(H."FechaRespaldo")='12'
										--AND H."IdEstado" = 'VIG'
										AND H."INGMontoATasaMercadoActual" > 0
										AND H."IdRegimenFondoMutuo" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R','57B','57T')
										
										LEFT OUTER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" MONREAJUSTEINV ON MONREAJUSTEINV."IdParametroFinanciero" = (CASE "IdMonedaInstrumento" WHEN 'USD' THEN 'DO' WHEN 'CLP' THEN 'IPC' WHEN 'IVP' THEN 'IPC' ELSE "IdMonedaInstrumento" END) 
																													AND MONREAJUSTEINV."Fecha" = 
																									( CASE "IdMonedaInstrumento"
																											WHEN  'USD' THEN
																													TO_DATE("FechaOperacion", 'YYYY-MM-DD')
																											WHEN  'EUR' THEN
																													TO_DATE("FechaOperacion", 'YYYY-MM-DD')
																											WHEN  'UF' THEN
																													(YEAR(TO_DATE("FechaOperacion", 'YYYY-MM-DD')) ||'-12-31')
																											WHEN 'CLP' THEN
																											ADD_MONTHS(
																												    TO_DATE(TO_VARCHAR(YEAR("FechaOperacion")) || '-' || LPAD(TO_VARCHAR(MONTH("FechaOperacion")), 2, '0') || '-01', 'YYYY-MM-DD'),-1)

																											ELSE
																												    (YEAR(TO_DATE("FechaOperacion", 'YYYY-MM-DD')) ||'-12-01')
																										END) 
									  
									    
									    		LEFT OUTER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" MONREAJUSTEACT ON MONREAJUSTEACT."IdParametroFinanciero" = (CASE "IdMonedaInstrumento" WHEN 'USD' THEN 'DO' WHEN 'CLP' THEN 'IPC' WHEN 'IVP' THEN 'IPC' ELSE "IdMonedaInstrumento" END)
									    													AND MONREAJUSTEACT."Fecha" = ( CASE "IdMonedaInstrumento"
																											WHEN  'USD' THEN
																													TO_DATE(TO_VARCHAR(YEAR("FechaRespaldo") + 1) || '-01-01', 'YYYY-MM-DD')
																											WHEN  'EUR' THEN
																													TO_DATE(TO_VARCHAR(YEAR("FechaRespaldo") + 1) || '-01-01', 'YYYY-MM-DD')
																											WHEN  'UF' THEN
																													TO_DATE( TO_VARCHAR(YEAR("FechaRespaldo")) || '-12-31', 'YYYY-MM-DD')

																											WHEN 'CLP' THEN
																												    TO_DATE( TO_VARCHAR(YEAR("FechaRespaldo")) || '-11-01', 'YYYY-MM-DD')

																											ELSE
																												    TO_DATE( TO_VARCHAR(YEAR("FechaRespaldo")) || '-12-01', 'YYYY-MM-DD')

																										END) 
										
									LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" PAR ON H."FechaOperacion" = PAR."Fecha"     AND PAR."IdParametroFinanciero" = 'DO'
									LEFT JOIN "BSCL.Custodia.BD::HIS.Garantia" GTA ON GTA."IdCarteraOrigen" = H."IdCartera" AND GTA."FechaRespaldo" = H."FechaRespaldo" 
									WHERE H."IdEstado"='VIG' OR (H."IdEstado"='LIQ' AND H."EGRIdTipoOperacion" = 'INGGTA'  AND GTA."IdEstado" = 'VIG')
									 GROUP BY H."IdCliente",H."IdPortafolio",H."Nemotecnico",H."IdClasificacionTipoInstrumento",H."IdMonedaInstrumento",H."IdInstrumento",H."IdTipoInstrumento",H."IdRegimenFondoMutuo",H."FechaRespaldo",H."IdEmpresa",H."FechaOperacion"
											,H."IdEstado",H."Cantidad",H."MontoCompra",H."FolioCartera",H."IdCartera",H."IdCliente"				  
									 )HISFEC WHERE HISFEC.RANK = 1

					UNION ALL 
					-- GARANTIAS RPA FM
					SELECT 
                        HISFEC."IdClasificacionTipoInstrumento",
                        HISFEC."FechaRespaldo",
                        HISFEC."FechaOperacionAporte" AS "FechaOperacion",
						HISFEC."IdEmpresa",
						HISFEC."IdCliente",
						HISFEC."IdPortafolio", 
						HISFEC."Nemotecnico",
                        HISFEC."INGCantidad",
						HISFEC."IdInstrumento",
						HISFEC."MtoNominalAporteActualizado",
						HISFEC."INGPrecioMercadoActual",
						HISFEC."IdMonedaInstrumento",
						HISFEC."MontoAporteNominal",
						HISFEC."FolioCartera",
						HISFEC."IdCartera"
								FROM (SELECT H."IdEmpresa",H."IdCliente", H."FechaOperacion", H."IdPortafolio",H."Nemotecnico",H."IdClasificacionTipoInstrumento",H."IdMonedaInstrumento", H."IdInstrumento",H."IdCartera",
											H."IdTipoInstrumento",H."IdEstado",COALESCE(H."IdRegimenFondoMutuo",'') AS "IdRegimenFondoMutuo",
											H."Cantidad",H."MontoCompra",H."FechaRespaldo",
											sum(round(H."INGMontoATasaMercadoActual",0)) AS "INGMontoATasaMercadoActual",
											sum(round(H."MontoCompra" * (H."EGRCantidad" / NULLIF(H."Cantidad", 0)))) AS "MontoAporteNominal",
											sum(round(((H."EGRCantidad" / NULLIF(H."Cantidad", 0)) * H."MontoCompra") * (NULLIF(MONREAJUSTEACT."Valor", 0) / NULLIF(MONREAJUSTEINV."Valor", 0)), 0)) as "MtoNominalAporteActualizado",
											sum(CASE 
										        WHEN H."IdEstado" IN ('RPA') AND H."EGRIdTipoOperacion" = 'INGGTA' AND GTA."IdEstado" = 'VIG' THEN H."EGRCantidad"
										        ELSE 0 
										     END) AS "INGCantidad"
											,CASE WHEN H."IdMonedaInstrumento" = 'USD' THEN max(round((H."INGPrecioMercadoActual" * PAR."Valor"),2)) ELSE 	max(H."INGPrecioMercadoActual") END as "INGPrecioMercadoActual"
											,H."FolioCartera"
											,H."FechaOperacion" AS "FechaOperacionAporte"
											--,RANK() over (partition by  H."FolioCartera" order by H."IdCartera" desc) as RANK ---Solo ultimo reg de cartera (Saldo final del periodo)
										FROM "BSCL.Custodia.BD::HIS.Cartera" H
										INNER JOIN
											(			
												SELECT TOP 5 "FechaRespaldoFinal" FROM ---Fechas finales de respaldo por periodo (acotado a 5 periodos 
												(
													SELECT  MAX("FechaRespaldo") AS "FechaRespaldoFinal" ,YEAR("FechaRespaldo") FROM  "BSCL.Custodia.BD::HIS.Cartera" 
													WHERE   "IdClasificacionTipoInstrumento" IN ('RV','FM') 
													AND "IdTipoInstrumento" IN ('FFM')
													AND MONTH("FechaRespaldo")='12'
													AND "IdEstado" = 'RPA'
													AND "INGMontoATasaMercadoActual" > 0
													AND "IdRegimenFondoMutuo" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R','57B','57T')
													GROUP BY YEAR("FechaRespaldo")
													ORDER BY YEAR("FechaRespaldo") DESC
												)
											)GRFECHA	
										ON 	H."FechaRespaldo"=GRFECHA."FechaRespaldoFinal"
										AND H."IdClasificacionTipoInstrumento" IN ('RV','FM')  
										AND H."IdTipoInstrumento" IN ('FFM') 
										AND MONTH(H."FechaRespaldo")='12'
										AND H."IdEstado" = 'RPA'
										AND H."INGMontoATasaMercadoActual" > 0
										AND H."IdRegimenFondoMutuo" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R','57B','57T')
										
										LEFT OUTER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" MONREAJUSTEINV ON MONREAJUSTEINV."IdParametroFinanciero" = (CASE "IdMonedaInstrumento" WHEN 'USD' THEN 'DO' WHEN 'CLP' THEN 'IPC' WHEN 'IVP' THEN 'IPC' ELSE "IdMonedaInstrumento" END) 
																													AND MONREAJUSTEINV."Fecha" = 
																									( CASE "IdMonedaInstrumento"
																											WHEN  'USD' THEN
																													TO_DATE("FechaOperacion", 'YYYY-MM-DD')
																											WHEN  'EUR' THEN
																													TO_DATE("FechaOperacion", 'YYYY-MM-DD')
																											WHEN  'UF' THEN
																													(YEAR(TO_DATE("FechaOperacion", 'YYYY-MM-DD')) ||'-12-31')
																											WHEN 'CLP' THEN
																												    TO_DATE(TO_VARCHAR(YEAR("FechaOperacion")) || '-' || LPAD(TO_VARCHAR(MONTH("FechaOperacion")), 2, '0') || '-01', 'YYYY-MM-DD')

																											ELSE
																												    (YEAR(TO_DATE("FechaOperacion", 'YYYY-MM-DD')) ||'-12-01')
																										END) 
									  
									    
									    		LEFT OUTER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" MONREAJUSTEACT ON MONREAJUSTEACT."IdParametroFinanciero" = (CASE "IdMonedaInstrumento" WHEN 'USD' THEN 'DO' WHEN 'CLP' THEN 'IPC' WHEN 'IVP' THEN 'IPC' ELSE "IdMonedaInstrumento" END)
									    													AND MONREAJUSTEACT."Fecha" = ( CASE "IdMonedaInstrumento"
																											WHEN  'USD' THEN
																													TO_DATE(TO_VARCHAR(YEAR("FechaRespaldo") + 1) || '-01-01', 'YYYY-MM-DD')
																											WHEN  'EUR' THEN
																													TO_DATE(TO_VARCHAR(YEAR("FechaRespaldo") + 1) || '-01-01', 'YYYY-MM-DD')
																											WHEN  'UF' THEN
																													TO_DATE( TO_VARCHAR(YEAR("FechaRespaldo")) || '-12-31', 'YYYY-MM-DD')

																											WHEN 'CLP' THEN
																												    TO_DATE( TO_VARCHAR(YEAR("FechaRespaldo")) || '-11-01', 'YYYY-MM-DD')

																											ELSE
																												    TO_DATE( TO_VARCHAR(YEAR("FechaRespaldo")) || '-12-01', 'YYYY-MM-DD')

																										END) 
										
									 LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" PAR ON H."FechaRespaldo" = PAR."Fecha"     AND PAR."IdParametroFinanciero" = 'DO'
									 LEFT JOIN "BSCL.Custodia.BD::HIS.Garantia" GTA ON GTA."IdCarteraOrigen" = H."IdCartera" AND GTA."FechaRespaldo" = H."FechaRespaldo" 
									 WHERE (H."IdEstado" = 'RPA' AND H."EGRIdTipoOperacion" = 'INGGTA' AND GTA."IdEstado" = 'VIG') 
									 GROUP BY H."IdCliente",H."IdPortafolio",H."Nemotecnico",H."IdClasificacionTipoInstrumento",H."IdMonedaInstrumento",H."IdInstrumento",H."IdTipoInstrumento",H."IdRegimenFondoMutuo",H."FechaRespaldo",H."IdEmpresa",H."FechaOperacion"
											,H."IdEstado",H."Cantidad",H."MontoCompra",H."FolioCartera",H."IdCartera",H."IdCliente"				  
									 )HISFEC
									
					)AS HIS 
				INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" por ON por."IdPortafolio"= HIS."IdPortafolio" 
 				WHERE --HIS."MtoNominalAporteActualizado" > 0 CARGA DE CRIAS/APODIV MONTO COMPRA=0
				(por."CodigoPortafolio" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R','57B','57T') AND por."Proposito" NOT IN ('REGAPV'))
                )) AS LG

		
         INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli ON cli."IdCliente" = LG."IdCliente"
        LEFT  JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" ins ON trim(ins."IdInstrumento") = trim(LG."IdInstrumento")
        LEFT  JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondo" fondo ON trim(fondo."IdSerieFondo") = trim(LG."Fondo")
        LEFT  JOIN "BSCL.Plataforma.BD::INS.Instrumento" inst ON TRIM(inst."IdInstrumento") = TRIM(ins."IdInstrumento")
        LEFT  JOIN "BSCL.Plataforma.BD::MER.Emisor" emi ON (ins."IdEmisor"=emi."IdEmisor")
        LEFT  JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" peremi ON (peremi."IdPersona"=emi."IdPersona")
        LEFT  JOIN "BSCL.Plataforma.BD::REG.Pais"  pais ON (pais."IdPais" = cli."IdPaisResidencia")
		LEFT  JOIN "BSCL.Normativa.BD::DOC.Homologacion"  Rutfondos on (Rutfondos."Esquema"='0' AND Rutfondos."Tipo"='RutFondo' AND inst."DocumentoIdentidadFondo"=Rutfondos."ValorOrigen"  AND LG."IdClasificacionTipoInstrumento" != 'FM')
		LEFT  JOIN "BSCL.Normativa.BD::DOC.Homologacion" RutfondosFM on (RutfondosFM."Esquema"='0' AND RutfondosFM."Tipo"='RutFondo' AND fondo."CodigoSVS"=RutfondosFM."ValorOrigen"  AND LG."IdClasificacionTipoInstrumento" = 'FM')


		--LEFT JOIN "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" PrecInst ON PrecInst."Fecha"=LG."FechaRespaldo" AND PrecInst."IdInstrumento"=LG."IdInstrumento" AND LG."IdClasificacionTipoInstrumento" != 'FM'
		--LEFT JOIN "BSCL.FondoMutuo.BD::FMM.ValorCuotaHistorico" PrecFM ON  PrecFM."FechaCierre"= ADD_DAYS(LG."FechaRespaldo",-1) AND COALESCE (fondo."NemoCMF",LG."Fondo")=PrecFM."IdSerieFondo" AND LG."IdClasificacionTipoInstrumento" = 'FM' AND PrecFM."EsUltimaCargaDiaria"='S' 

WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1962.Origen" TO bcs_desarrollador WITH GRANT OPTION;