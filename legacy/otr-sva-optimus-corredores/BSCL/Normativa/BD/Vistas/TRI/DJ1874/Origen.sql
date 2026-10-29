SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1874.Origen";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1874.Origen"

(
	"IdEmpresa"
	,"IdOperacion"
	,"Numero"
	,"RutEmisor"
	,"RutInversionista"
	,"TipoContribuyente"
	,"NemotecnicoSVS"
	,"Nemotecnico"
	,"FechaOperacion"
	,"MonedaUnidadReajuste"
	,"NumeroCuentaDCV"
	,"MovimientoSaldoInicial"
	,"MovimientoAdquision"
	,"MovimientoEnajenacion"
	,"MovimientoRescateAnticipado"
	,"MovimientoSaldoFinal"
	,"SaldoFinalNominal"
	,"ResultadoEnajenacionMonedaUnidadReajuste"
	,"ResultadoEnajenacionPesos"
	,"RetencionMonedaUnidadReajuste"
	,"RetencionPesos"
	,"IdCliente"
	,"IdPortafolio"
	,"IdTipoOperacion"
	,"IdTipoProducto"
	,"FechaAdquisicion"
	,"TipoDocumento"
	,"FolioCartera"
	,"IdClasificacionTipoInstrumento"
	,"IdTipoInstrumento"
	,"IdInstrumento"
	,"InsFechaVencimiento"
	,"InsFechaEmision"
	,"IdMonedaInstrumento"
	,"IdMonedaEmisionInstrumento"
	,"IdTipoReajusteInstrumento"
	,"IdEmisor"
	--,"IdCondicionDeLiquidacion"
	,"FechaLiquidacion"
	,"Cantidad"
	--,"ValorRescate"
	,"Tasa"
	,"Precio"
	--,"IdMoneda"
	,"InteresDevengadoMonedaUnidadReajuste"
	,"InteresDevengadoPesos"
	,"ValIndicadorValorizado"
	,"ValResultado"
	,"ValMontoMonedaInstrumento"
	,"ValMontoMonedaSistema"
	,"ValMontoMonedaInstrumentoFechaVenta"
	,"ValMontoMonedaSistemaFechaVenta"
	,"ValMontoDevengadoAMonedaInstrumento"
	,"ValMontoDevengadoAMonedaSistema"
	,"RetencionMonedaUnidadReajuste4porciento" -- complemento 4%
	,"RetencionPesos4porciento"
)
AS 

SELECT
   MAX(a."IdEmpresa")         AS "IdEmpresa"
  ,MAX(a."IdOperacion")         AS "IdOperacion"
  ,MAX(a."Numero")          AS "Numero"
  ,MAX(a."RutEmisor")         AS "RutEmisor"
  ,a."RutInversionista" 
  ,MAX(a."TipoContribuyente")       AS "TipoContribuyente"
  ,MAX(a."NemotecnicoSVS" ) AS "NemotecnicoSVS"
  ,a."Nemotecnico"      
  ,CASE WHEN SUM(a."MovimientoSaldoInicial") > 0  THEN YEAR(a."FechaAdquisicion") -1 || '-12-31' ELSE MAX(a."FechaOperacion") END AS "FechaOperacion"
  ,MAX(a."MonedaUnidadReajuste")       AS "MonedaUnidadReajuste"
  ,MAX(a."NumeroCuentaDCV")       AS "NumeroCuentaDCV"
  ,SUM(a."MovimientoSaldoInicial")      AS "MovimientoSaldoInicial"
  ,SUM(a."MovimientoAdquision")       AS "MovimientoAdquision"
  ,SUM(a."MovimientoEnajenacion")      AS "MovimientoEnajenacion"
  ,SUM(a."MovimientoRescateAnticipado")     AS "MovimientoRescateAnticipado"
  ,ABS(SUM(a."MovimientoSaldoFinal"))     AS "MovimientoSaldoFinal"
  ,ABS(SUM(a."SaldoFinalNominal"))       AS "SaldoFinalNominal"
  ,ABS(SUM(a."ResultadoEnajenacionMonedaUnidadReajuste"))  AS "ResultadoEnajenacionMonedaUnidadReajuste"
  ,ABS(SUM(a."ResultadoEnajenacionPesos"))     AS "ResultadoEnajenacionPesos"
  ,SUM(a."RetencionMonedaUnidadReajuste")    AS "RetencionMonedaUnidadReajuste"
  ,SUM(a."RetencionPesos")        AS "RetencionPesos"
  ,MAX(a."IdCliente")         AS "IdCliente"
  ,MAX(a."IdPortafolio")         AS "IdPortafolio"
  ,MAX(a."IdTipoOperacion")        AS "IdTipoOperacion"
  ,MAX(a."IdTipoProducto")        AS "IdTipoProducto"
  ,MAX(a."FechaAdquisicion")        AS "FechaAdquisicion"
  ,MAX(a."TipoDocumento")        AS "TipoDocumento"
  ,MAX(a."FolioCartera") AS "FolioCartera"
  ,MAX(a."IdClasificacionTipoInstrumento")    AS "IdClasificacionTipoInstrumento"
  ,MAX(a."IdTipoInstrumento")       AS "IdTipoInstrumento"
  ,MAX(a."IdInstrumento")        AS "IdInstrumento"
  ,MAX(a."InsFechaVencimiento")       AS "InsFechaVencimiento"
  ,MAX(a."InsFechaEmision")        AS "InsFechaEmision"
  ,MAX(a."IdMonedaInstrumento")       AS "IdMonedaInstrumento"
  ,MAX(a."IdMonedaEmisionInstrumento")     AS "IdMonedaEmisionInstrumento"
  ,MAX(a."IdTipoReajusteInstrumento")     AS "IdTipoReajusteInstrumento"
  ,MAX(a."IdEmisor")          AS "IdEmisor"
  ,MAX(a."FechaLiquidacion")        AS "FechaLiquidacion"
  ,MAX(a."Cantidad")          AS "Cantidad"
  ,MAX(a."Tasa")           AS "Tasa"
  ,MAX(a."Precio")          AS "Precio"
  ,ABS(SUM(a."InteresDevengadoMonedaUnidadReajuste"))   AS "InteresDevengadoMonedaUnidadReajuste"
  ,ABS(SUM(a."InteresDevengadoPesos"))      AS "InteresDevengadoPesos"
  ,MAX(a."ValIndicadorValorizado")      AS "ValIndicadorValorizado"
  ,MAX(a."ValResultado")         AS "ValResultado"
  ,SUM(a."ValMontoMonedaInstrumento")     AS "ValMontoMonedaInstrumento"
  ,SUM(a."ValMontoMonedaSistema")      AS "ValMontoMonedaSistema"
  ,SUM(a."ValMontoMonedaInstrumentoFechaVenta")   AS "ValMontoMonedaInstrumentoFechaVenta"
  ,SUM(a."ValMontoMonedaSistemaFechaVenta")    AS "ValMontoMonedaSistemaFechaVenta"
  ,SUM(a."ValMontoDevengadoAMonedaInstrumento")   AS "ValMontoDevengadoAMonedaInstrumento"
  ,SUM(a."ValMontoDevengadoAMonedaSistema")    AS "ValMontoDevengadoAMonedaSistema"
  ,SUM(a."RetencionMonedaUnidadReajuste4porciento")  AS "RetencionMonedaUnidadReajuste4porciento"
  ,SUM(a."RetencionPesos4porciento")
  FROM (
SELECT
	origen."IdEmpresa"
	,origen."IdOperacion"
	,origen."Numero"
	,origen."RutEmisor"
	,origen."RutInversionista"
	,origen."TipoContribuyente"
	,origen."NemotecnicoSVS"
	,origen."Nemotecnico"
	,origen."FechaOperacion"
	,origen."MonedaUnidadReajuste"
	,origen."NumeroCuentaDCV"
	,origen."MovimientoSaldoInicial"
	,origen."MovimientoAdquision"
	,origen."MovimientoEnajenacion"
	,origen."MovimientoRescateAnticipado"
	,origen."MovimientoSaldoFinal"
	,origen."SaldoFinalNominal"
	,origen."ResultadoEnajenacionMonedaUnidadReajuste"
	,origen."ResultadoEnajenacionPesos"
	,origen."RetencionMonedaUnidadReajuste"
	,origen."RetencionPesos"
	,origen."IdCliente"
	,origen."IdPortafolio"
	,origen."IdTipoOperacion"
	,origen."IdTipoProducto"
	,origen."FechaAdquisicion"
	,origen."TipoDocumento"
	,origen."FolioCartera"
	,origen."IdClasificacionTipoInstrumento"
	,origen."IdTipoInstrumento"
	,origen."IdInstrumento"
	,origen."InsFechaVencimiento"
	,origen."InsFechaEmision"
	,origen."IdMonedaInstrumento"
	,origen."IdMonedaEmisionInstrumento"
	,origen."IdTipoReajusteInstrumento"
	,origen."IdEmisor"
	,origen."FechaLiquidacion"
	,origen."Cantidad"
	,origen."Tasa"
	,origen."Precio"
	--,ORI."IdMoneda"
	,origen."InteresDevengadoMonedaUnidadReajuste"
	,origen."InteresDevengadoPesos"
	,'' AS "ValIndicadorValorizado"
	,''   AS "ValResultado"
	,NULL AS "ValMontoMonedaInstrumento"
	,NULL AS "ValMontoMonedaSistema"
	,NULL AS "ValMontoMonedaInstrumentoFechaVenta"
	,NULL AS "ValMontoMonedaSistemaFechaVenta"
	,NULL AS "ValMontoDevengadoAMonedaInstrumento"
	,NULL AS "ValMontoDevengadoAMonedaSistema"
	,NULL AS "RetencionMonedaUnidadReajuste4porciento"
	,NULL AS "RetencionPesos4porciento"

FROM (
---vista origen 
SELECT 

	
	ORI."IdEmpresa"
	,ORI."IdOperacion"
	,ORI."Numero"
	,ORI."RutEmisor"
	,ORI."RutInversionista"
	,ORI."TipoContribuyente"
	,ORI."NemotecnicoSVS"
	,ORI."Nemotecnico"
	,ORI."FechaOperacion"
	,ORI."MonedaUnidadReajuste"
	,CASE  ORI."NumeroCuentaDCV" WHEN '0' THEN COALESCE(DCVCOR."Codigo",'0')   ELSE ORI."NumeroCuentaDCV" END AS "NumeroCuentaDCV"
	,ORI."MovimientoSaldoInicial"
	,ORI."MovimientoAdquision"
	,ORI."MovimientoEnajenacion"
	,ORI."MovimientoRescateAnticipado"
	,ORI."MovimientoSaldoFinal"
	,ORI."SaldoFinalNominal"
	,ORI."ResultadoEnajenacionMonedaUnidadReajuste"
	,ORI."ResultadoEnajenacionPesos"
	,ORI."RetencionMonedaUnidadReajuste"
	,ORI."RetencionPesos"
	,ORI."IdCliente"
	,ORI."IdPortafolio"
	,ORI."IdTipoOperacion"
	,ORI."IdTipoProducto"
	,ORI."FechaAdquisicion"
	,ORI."TipoDocumento"
	,ORI."FolioCartera"
	,ORI."IdClasificacionTipoInstrumento"
	,ORI."IdTipoInstrumento"
	,ORI."IdInstrumento"
	,ORI."InsFechaVencimiento"
	,ORI."InsFechaEmision"
	,ORI."IdMonedaInstrumento"
	,ORI."IdMonedaEmisionInstrumento"
	,ORI."IdTipoReajusteInstrumento"
	,ORI."IdEmisor"
	,ORI."FechaLiquidacion"
	,ORI."Cantidad"
	,ORI."Tasa"
	,ORI."Precio"
	--,ORI."IdMoneda"
	,ORI."InteresDevengadoMonedaUnidadReajuste"
	,ORI."InteresDevengadoPesos"
	,ORI."RetencionMonedaUnidadReajuste4porciento"
	,ORI."RetencionPesos4porciento"
FROM

(
		/*TIPO 3 Y 4 */
		-- 3 - ADQUISICION - OPERACIONES DE COMPRAS (COMPRA DE CARTERAS EN EL PERIODO)            	-- FECHA ADQUISICION CARTERA						// (FECHA_OPERACION = FECHA ADQUISICION)
		--SELECT TIPOOPERACION."Accion", CAR."INGIdTipoOperacion"	,CAR."EGRIdOperacion" ,  CAR."EGRFolioComprobante",CAR."EGRIdOperacion",CAR."FolioComprobante" ,CAR."Nemotecnico",
		SELECT
			CAR."IdEmpresa" AS "IdEmpresa"
			,0 AS "IdOperacion"
			--	,CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN CAR."FolioComprobante"   ELSE CAR."EGRFolioComprobante"   END 	AS "Numero"	
			,CAR."FolioComprobante"	AS "Numero"	
			,COALESCE(EMI."DocumentoIdentidad",'') AS "RutEmisor"
			,COALESCE(IDE."DocumentoIdentidad",'') AS "RutInversionista"
			,CASE COALESCE(PER."ResidenciaTributariaExtranjera",'N') WHEN 'S' THEN 2 ELSE 1 END AS "TipoContribuyente" --CASE para cuando vive o no en chile
			,CAR."NemoSVS"  AS "NemotecnicoSVS"
			,CAR."Nemotecnico"  AS "Nemotecnico"
		--	,CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN TO_DATE(CAR."INGFechaOperacion")   ELSE TO_DATE(CAR."EGRFechaOperacion")  END 	AS "FechaOperacion"	
			,TO_DATE(CAR."INGFechaOperacion")	AS "FechaOperacion"
			,COALESCE(HMONREAJ."ValorDestino", TO_VARCHAR('9')) AS  "MonedaUnidadReajuste" --HOMOLOGAR
			,COALESCE(CTA."Codigo",'0') AS "NumeroCuentaDCV"
		--	,CASE TIPOOPERACION."Accion" WHEN 'VENTA'  THEN CAR."INGMontoATasaMercadoActual" ELSE 0 END AS "MovimientoSaldoInicial"
			,0 AS "MovimientoSaldoInicial"
			,ROUND(CASE WHEN HMONREAJ."ValorOrigen" IN ('CLP', '$', 'PESO', '1') THEN CAR."MontoCompra" ELSE (CAR."MontoCompra" / COALESCE(NULLIF(TC."TipoCambioMercado", 0),1))  END,0) AS "MovimientoAdquision"
			,0 AS "MovimientoEnajenacion"
			,0 AS "MovimientoRescateAnticipado"
			,0 AS "MovimientoSaldoFinal"
			,0 AS "SaldoFinalNominal"
			,0 AS "ResultadoEnajenacionMonedaUnidadReajuste"
			,0 AS "ResultadoEnajenacionPesos"
			,CASE 
				WHEN PER."ResidenciaTributariaExtranjera" = 'S' THEN
					CASE
						WHEN INS."IdMoneda" <> 'CLP' THEN
							CASE
								WHEN MONVNT."Valor" IS NULL OR MONCOM."Valor" IS NULL
								THEN
									CASE 
									WHEN DAYS_BETWEEN(TO_DATE(CAR."INGFechaOperacion"),TO_DATE(YEAR(TO_DATE(CAR."INGFechaOperacion")) || '-12-31')) > 0 
										THEN ROUND( ((B."Tasa_Facial" * CAR."INGCantidad") / 100 / 360) * DAYS_BETWEEN(TO_DATE(CAR."INGFechaOperacion"), TO_DATE(YEAR(TO_DATE(CAR."INGFechaOperacion")) || '-12-31')), 4)*0.04*1
										ELSE 0
									END
								ELSE
									CASE 
									WHEN DAYS_BETWEEN(TO_DATE(CAR."INGFechaOperacion"),TO_DATE(YEAR(TO_DATE(CAR."INGFechaOperacion")) || '-12-31')) > 0 
										THEN ROUND( ((B."Tasa_Facial" * CAR."INGCantidad") / 100 / 360) * DAYS_BETWEEN(TO_DATE(CAR."INGFechaOperacion"), TO_DATE(YEAR(TO_DATE(CAR."INGFechaOperacion")) || '-12-31')), 4)*0.04*(MONVNT."Valor"/MONCOM."Valor")
										ELSE 0
									END
								END
						ELSE
							CASE 
							WHEN DAYS_BETWEEN(TO_DATE(CAR."INGFechaOperacion"),TO_DATE(YEAR(TO_DATE(CAR."INGFechaOperacion")) || '-12-31')) > 0 
								THEN ROUND( ((B."Tasa_Facial" * CAR."INGCantidad") / 100 / 360) * DAYS_BETWEEN(TO_DATE(CAR."INGFechaOperacion"), TO_DATE(YEAR(TO_DATE(CAR."INGFechaOperacion")) || '-12-31')), 4)*0.04*(IPCVNT."Valor"/IPCCOM."Valor")
								ELSE 0
							END
						END 
				ELSE 0 
				END AS "RetencionMonedaUnidadReajuste"
			,CASE 
				WHEN DAYS_BETWEEN(TO_DATE(CAR."INGFechaOperacion"), 
					TO_DATE(YEAR(TO_DATE(CAR."INGFechaOperacion")) || '-12-31')) > 0  AND PER."ResidenciaTributariaExtranjera" = 'S'
				THEN 
					CASE 
						WHEN CAR."IdMonedaAdicional" IN ('CLP', '$', 'PESO') THEN
							ROUND(
								((B."Tasa_Facial" * CAR."INGCantidad") / 100 / 360)
								* DAYS_BETWEEN(TO_DATE(CAR."INGFechaOperacion"), 
								TO_DATE(YEAR(TO_DATE(CAR."INGFechaOperacion")) || '-12-31')) * 0.04 * (IPCVNT."Valor"/IPCCOM."Valor")
							, 0)
						ELSE
							ROUND(
								((B."Tasa_Facial" * CAR."INGCantidad") / 100 / 360)
								* DAYS_BETWEEN(TO_DATE(CAR."INGFechaOperacion"), 
								TO_DATE(YEAR(TO_DATE(CAR."INGFechaOperacion")) || '-12-31'))
								* COALESCE(NULLIF(TC."TipoCambioMercado", 0), 1) * 0.04 *(IPCVNT."Valor"/IPCCOM."Valor")
							, 0)
					END
				ELSE 0
			END AS  "RetencionPesos" 
			,CAR."IdCliente" AS "IdCliente"
			,CAR."IdPortafolio" AS "IdPortafolio"
			,CAR."INGIdTipoOperacion" AS "IdTipoOperacion"
			,CAR."IdTipoProducto"
			,TO_DATE(CAR."FechaOperacion") AS "FechaAdquisicion"
			--,CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN 3 ELSE 4 END AS "TipoDocumento"
			,3 AS "TipoDocumento"
			,CAR."FolioCartera"
			-----------------------------------------------------------------------------------------categoria
			,CAR."IdClasificacionTipoInstrumento" 			--RV/RF/IF
			,CAR."IdTipoInstrumento"						--PAC/AUT/DEP
			,CAR."IdInstrumento"
			-----------------------------------------------------------------------------------------instrumento
			,TO_VARCHAR (TO_date(CAR."FechaVencimientoInstrumento")) AS "InsFechaVencimiento"
			,TO_VARCHAR (TO_date(CAR."FechaEmisionInstrumento")) 	AS "InsFechaEmision"
			,INS."IdMoneda" AS "IdMonedaInstrumento"
			,INS."IdMonedaEmision" AS "IdMonedaEmisionInstrumento"
			,INS."IdTipoReajuste" AS "IdTipoReajusteInstrumento" 
			,INS."IdEmisor"  AS "IdEmisor"								--valorizacionCL
			-----------------------------------------------------------------------------------------operacion
			
		--	,CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN TO_VARCHAR (TO_date(CAR."INGFechaLiquidacion")) ELSE TO_VARCHAR (TO_date(CAR."EGRFechaLiquidacion")) END 	AS "FechaLiquidacion" 
			,TO_VARCHAR (TO_date(CAR."INGFechaLiquidacion")) 	AS "FechaLiquidacion"
		--	,CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN CAR."INGCantidad" ELSE CAR."EGRCantidad"   END 	AS "Cantidad"							               
			,CAR."INGCantidad" 	AS "Cantidad"
			,B."Tasa_Facial" AS "Tasa"	
		--	,CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN CAR."INGPrecioCompra" ELSE CAR."EGRPrecio" END 	AS "Precio"
			,CAR."INGPrecioCompra"AS "Precio"
			,CAR."IdMonedaAdicional"
			,0 AS "InteresDevengadoMonedaUnidadReajuste"
			,0 AS "InteresDevengadoPesos"
			,CASE 
				WHEN DAYS_BETWEEN(TO_DATE(CAR."INGFechaOperacion"), 
					TO_DATE(YEAR(TO_DATE(CAR."INGFechaOperacion")) || '-12-31')) > 0  AND PER."ResidenciaTributariaExtranjera" = 'S'
				THEN 
					CASE 
						WHEN CAR."IdMonedaAdicional" IN ('CLP', '$', 'PESO') THEN
							ROUND(
								((B."Tasa_Facial" * CAR."INGCantidad") / 100 / 360)
								* DAYS_BETWEEN(TO_DATE(CAR."INGFechaOperacion"), 
								TO_DATE(YEAR(TO_DATE(CAR."INGFechaOperacion")) || '-12-31')) * 0.04 * (IPCVNT."Valor"/IPCCOM."Valor")
							, 0)
						ELSE
							ROUND(
								((B."Tasa_Facial" * CAR."INGCantidad") / 100 / 360)
								* DAYS_BETWEEN(TO_DATE(CAR."INGFechaOperacion"), 
								TO_DATE(YEAR(TO_DATE(CAR."INGFechaOperacion")) || '-12-31'))
								* COALESCE(NULLIF(TC."TipoCambioMercado", 0), 1) * 0.04 *(IPCVNT."Valor"/IPCCOM."Valor")
							, 0)
					END
				ELSE 0
			END AS  "RetencionPesos4porciento" 
			,CASE 
				WHEN PER."ResidenciaTributariaExtranjera" = 'S' THEN
					CASE
						WHEN INS."IdMoneda" <> 'CLP' THEN
							CASE
								WHEN MONVNT."Valor" IS NULL OR MONCOM."Valor" IS NULL
								THEN
									CASE 
									WHEN DAYS_BETWEEN(TO_DATE(CAR."INGFechaOperacion"),TO_DATE(YEAR(TO_DATE(CAR."INGFechaOperacion")) || '-12-31')) > 0 
										THEN ROUND( ((B."Tasa_Facial" * CAR."INGCantidad") / 100 / 360) * DAYS_BETWEEN(TO_DATE(CAR."INGFechaOperacion"), TO_DATE(YEAR(TO_DATE(CAR."INGFechaOperacion")) || '-12-31')), 4)*0.04*1
										ELSE 0
									END
								ELSE
									CASE 
									WHEN DAYS_BETWEEN(TO_DATE(CAR."INGFechaOperacion"),TO_DATE(YEAR(TO_DATE(CAR."INGFechaOperacion")) || '-12-31')) > 0 
										THEN ROUND( ((B."Tasa_Facial" * CAR."INGCantidad") / 100 / 360) * DAYS_BETWEEN(TO_DATE(CAR."INGFechaOperacion"), TO_DATE(YEAR(TO_DATE(CAR."INGFechaOperacion")) || '-12-31')), 4)*0.04*(MONVNT."Valor"/MONCOM."Valor")
										ELSE 0
									END
								END
						ELSE
							CASE 
							WHEN DAYS_BETWEEN(TO_DATE(CAR."INGFechaOperacion"),TO_DATE(YEAR(TO_DATE(CAR."INGFechaOperacion")) || '-12-31')) > 0 
								THEN ROUND( ((B."Tasa_Facial" * CAR."INGCantidad") / 100 / 360) * DAYS_BETWEEN(TO_DATE(CAR."INGFechaOperacion"), TO_DATE(YEAR(TO_DATE(CAR."INGFechaOperacion")) || '-12-31')), 4)*0.04*(IPCVNT."Valor"/IPCCOM."Valor")
								ELSE 0
							END
						END 
				ELSE 0 
				END AS "RetencionMonedaUnidadReajuste4porciento"
		FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
		INNER JOIN (SELECT DISTINCT "Instrumento", MAX("Tasa_Facial") "Tasa_Facial"
					FROM "BSCL.Normativa.BD::TRI.InstrumentosArticulo104" WHERE "Periodo" >= 2019 GROUP BY "Instrumento"
					) B ON trim(CAR."NemoSVS") = trim(B."Instrumento")
		INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON  CAR."IdCliente" = CLI."IdCliente"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = CAR."IdInstrumento"
		LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCCOM  ON
			(substring(ADD_MONTHS (TO_DATE (CAR."FechaOperacion", 'YYYY-MM-DD'), -1),0,7) = IPCCOM."AgnoMes")  AND IPCCOM."IdParametroFinanciero" = 'IPC'
		LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCVNT  ON
			(substring(ADD_MONTHS (TO_DATE (CAR."INGFechaOperacion", 'YYYY-MM-DD'), -1),0,7) = IPCVNT."AgnoMes")  AND IPCVNT."IdParametroFinanciero" = 'IPC'
		LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" MONCOM  ON
			(substring(ADD_MONTHS (TO_DATE (CAR."FechaOperacion", 'YYYY-MM-DD'), -1),0,7) = MONCOM."AgnoMes")  
				AND	((CASE WHEN INS."IdMoneda" <> 'USD' THEN INS."IdMoneda" ELSE 'DO' END)  = MONCOM."IdParametroFinanciero")
		LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" MONVNT  ON 
															(CONCAT(substring(ADD_MONTHS (TO_DATE (CAR."FechaOperacion", 'YYYY-MM-DD'), -1),0,4),'-12') = MONVNT."AgnoMes")  AND 
															((CASE
															WHEN INS."IdMoneda" <> 'USD' THEN
																INS."IdMoneda"
															ELSE
																'DO'
															END)  = MONVNT."IdParametroFinanciero") 
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOOPERACION	ON TIPOOPERACION."IdTipoOperacion" = CAR."INGIdTipoOperacion"	
		
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" EMI ON INS."IdEmisor" = EMI."IdEmisor"
		LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio"  SOC ON CAR."IdCliente" = SOC."IdCliente"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona"  PER ON SOC."IdPersona" = PER."IdPersona"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDE ON PER."IdDocumentoIdentidad" = IDE."IdDocumentoIdentidad"
		LEFT JOIN (SELECT "IdPortafolio", MAX("Codigo") AS "Codigo"  FROM "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" WHERE "IdBoveda" ='DCV' GROUP BY "IdPortafolio") AS CTA ON CTA."IdPortafolio"=CAR."IdPortafolio"
		LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" TC ON TC."IdMoneda" = INS."IdTipoReajuste" AND TC."Fecha" = TO_VARCHAR(TO_DATE(CAR."INGFechaOperacion"), 'YYYY-MM-DD')
		LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HMONREAJ
				ON (HMONREAJ."Esquema"='DJ1874' AND TRIM(INS."IdTipoReajuste")= TRIM(HMONREAJ."ValorOrigen") AND HMONREAJ."Tipo"='MONEDAREAJUSTE' AND  TO_DATE(CAR."FechaOperacion") BETWEEN TO_DATE(HMONREAJ."FechaDatosDesde") AND TO_DATE(HMONREAJ."FechaDatosHasta"))
		
		WHERE 
			CAR."IdClasificacionTipoInstrumento"='RF'
		--AND  YEAR((CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN TO_DATE(CAR."INGFechaOperacion")   ELSE TO_DATE(CAR."EGRFechaOperacion")  END)) =YEAR(TO_DATE(CAR."FechaOperacion"))
		--AND  YEAR(TO_DATE(CAR."INGFechaOperacion")) =YEAR(TO_DATE(CAR."FechaOperacion"))   
		AND  CAR."EsPrimeraCartera"='S'
		AND  YEAR(TO_DATE(CAR."FechaOperacion")) = YEAR(TO_DATE(CAR."FechaOperacion"))   
		AND  CAR."INGIdTipoOperacion"  NOT IN ('VCDPAC','ANUCPA','ANUVPA','ANTPAC','COMPAC','VENPAC','ANUPAC' )
		--AND TIPOOPERACION."Accion" IN ('COMPRA','INGRESO')
		AND CAR."IdEstado"<>'ANU' AND CAR."MontoCompra">0 
		--AND "FolioCartera"=566421
			--AND CAR."FolioComprobante" IN (138142)
		
		
		
		-- 4 - ENAJENACION - OPERACIONES DE VENTAS  (VENTA DE CARTERAS COMPRADAS EN EL PERIODO)   	-- FECHA ADQUISICION CARTERA (AÑO VTA = AÑO COM)	// (FECHA_OPERACION = FECHA VENTA) 
		UNION ALL
		--SELECT TIPOOPERACION."Accion", CAR."INGIdTipoOperacion"	,CAR."EGRIdOperacion" ,  CAR."EGRFolioComprobante",CAR."EGRIdOperacion",CAR."FolioComprobante" ,CAR."Nemotecnico",
		SELECT
			CAR."IdEmpresa" AS "IdEmpresa"
			,0 AS "IdOperacion"
			--	,CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN CAR."FolioComprobante"   ELSE CAR."EGRFolioComprobante"   END 	AS "Numero"	
			,CAR."EGRFolioComprobante"	AS "Numero"	
			,COALESCE(EMI."DocumentoIdentidad",'') AS "RutEmisor"
			,COALESCE(IDE."DocumentoIdentidad",'') AS "RutInversionista"
			,CASE COALESCE("ResidenciaTributariaExtranjera",'N') WHEN 'S' THEN 2 ELSE 1 END AS "TipoContribuyente" --CASE para cuando vive o no en chile
			,CAR."NemoSVS"  AS "NemotecnicoSVS"
			,CAR."Nemotecnico"  AS "Nemotecnico"
		--	,CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN TO_DATE(CAR."INGFechaOperacion")   ELSE TO_DATE(CAR."EGRFechaOperacion")  END 	AS "FechaOperacion"	
			,TO_DATE(CAR."EGRFechaOperacion")	AS "FechaOperacion"
			,COALESCE(HMONREAJ."ValorDestino", TO_VARCHAR('9')) AS  "MonedaUnidadReajuste" --HOMOLOGAR
			,COALESCE(CTA."Codigo",'0') AS "NumeroCuentaDCV"
		--	,CASE TIPOOPERACION."Accion" WHEN 'VENTA'  THEN CAR."INGMontoATasaMercadoActual" ELSE 0 END AS "MovimientoSaldoInicial"
			,0 AS "MovimientoSaldoInicial" 
			,0 AS "MovimientoAdquision"
			--,CAR."EGRCantidad" AS "MovimientoEnajenacion"
			,ROUND(CASE WHEN HMONREAJ."ValorOrigen" IN ('CLP', '$', 'PESO', '1') THEN CAR."EGRMonto" ELSE (CAR."EGRMonto" / COALESCE(NULLIF(TC."TipoCambioMercado", 0),1))  END,0 ) AS "MovimientoEnajenacion"
			,0 AS "MovimientoRescateAnticipado"
			,0 AS "MovimientoSaldoFinal"
			,0 AS "SaldoFinalNominal"			
			,CASE 
				WHEN HMONREAJ."ValorOrigen" IN ('CLP', '$', 'PESO', '1') THEN 
					ROUND(COALESCE ((CAR."EGRMonto") - ((CAR."EGRCantidad" / CAR."Cantidad") * CAR."MontoCompra"),0), 0)
				ELSE 
					ROUND(
						COALESCE ((CAR."EGRMonto" / COALESCE(NULLIF(TC."TipoCambioMercado", 0), 1)) - ((CAR."EGRCantidad" / CAR."Cantidad") * (CAR."MontoCompra" / COALESCE(NULLIF(TC."TipoCambioMercado", 0), 1) )),0)
					, 0)
			END AS "ResultadoEnajenacionMonedaUnidadReajuste"
			,ROUND(COALESCE ((CAR."EGRMonto") - ((CAR."EGRCantidad" / CAR."Cantidad") * CAR."MontoCompra"),0), 0) AS "ResultadoEnajenacionPesos" 
			,0 AS "RetencionMonedaUnidadReajuste"
			,0 AS "RetencionPesos"
			,CAR."IdCliente" AS "IdCliente"
			,CAR."IdPortafolio" AS "IdPortafolio"
			,CAR."EGRIdTipoOperacion" AS "IdTipoOperacion"
			,CAR."IdTipoProducto"
			,TO_DATE(CAR."EGRFechaOperacion") AS "FechaAdquisicion"
			--,CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN 3 ELSE 4 END AS "TipoDocumento"
			,4 AS "TipoDocumento"
			,CAR."FolioCartera"
			-----------------------------------------------------------------------------------------categoria
			,CAR."IdClasificacionTipoInstrumento" 			--RV/RF/IF
			,CAR."IdTipoInstrumento"						--PAC/AUT/DEP
			,CAR."IdInstrumento"
			-----------------------------------------------------------------------------------------instrumento
			,TO_VARCHAR (TO_date(CAR."FechaVencimientoInstrumento")) AS "InsFechaVencimiento"
			,TO_VARCHAR (TO_date(CAR."FechaEmisionInstrumento")) 	AS "InsFechaEmision"
			,INS."IdMoneda" AS "IdMonedaInstrumento"
			,INS."IdMonedaEmision" AS "IdMonedaEmisionInstrumento"
			,INS."IdTipoReajuste" AS "IdTipoReajusteInstrumento" 
			,INS."IdEmisor"  AS "IdEmisor"								--valorizacionCL
			-----------------------------------------------------------------------------------------operacion
			
		--	,CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN TO_VARCHAR (TO_date(CAR."INGFechaLiquidacion")) ELSE TO_VARCHAR (TO_date(CAR."EGRFechaLiquidacion")) END 	AS "FechaLiquidacion" 
			,TO_VARCHAR (TO_date(CAR."EGRFechaLiquidacion")) 	AS "FechaLiquidacion"
		--	,CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN CAR."INGCantidad" ELSE CAR."EGRCantidad"   END 	AS "Cantidad"							               
			,CAR."EGRCantidad" 	AS "Cantidad"
			,B."Tasa_Facial" AS "Tasa"	
		--	,CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN CAR."INGPrecioCompra" ELSE CAR."EGRPrecio" END 	AS "Precio"
			,CAR."EGRPrecio" AS "Precio"
			,CAR."IdMonedaAdicional"
			,CASE 
				WHEN CAR."FechaOperacion" = CAR."EGRFechaOperacion" THEN 0
			    WHEN TO_DATE(CAR."FechaOperacion") < TO_DATE(YEAR(TO_DATE(CAR."EGRFechaOperacion")) || '-01-01')
			    
			    THEN ROUND(((CAR."INGCantidad" * B."Tasa_Facial") / 100) / 360 
			               * DAYS_BETWEEN(TO_DATE(YEAR(TO_DATE(CAR."EGRFechaOperacion")) || '-01-01'), ADD_DAYS(TO_DATE(CAR."EGRFechaOperacion"), -1)), 0)
			               
			    ELSE ROUND(((CAR."INGCantidad" * B."Tasa_Facial") / 100) / 360 
			               * DAYS_BETWEEN(TO_DATE(CAR."FechaOperacion"), ADD_DAYS(TO_DATE(CAR."EGRFechaOperacion"), -1)), 0)
			END AS "InteresDevengadoMonedaUnidadReajuste"
			,CASE 
				WHEN CAR."FechaOperacion" = CAR."EGRFechaOperacion" THEN 0
			    WHEN TO_DATE(CAR."FechaOperacion") < TO_DATE(YEAR(TO_DATE(CAR."EGRFechaOperacion")) || '-01-01')
			    THEN 
			        CASE 
			            WHEN INS."IdTipoReajuste" IN ('CLP', '$', 'PESO') THEN
			                ROUND(((CAR."INGCantidad" * B."Tasa_Facial") / 100) / 360 
			                      * DAYS_BETWEEN(TO_DATE(YEAR(TO_DATE(CAR."EGRFechaOperacion")) || '-01-01'), ADD_DAYS(TO_DATE(CAR."EGRFechaOperacion"), -1)), 0)
			            ELSE
			                ROUND(((CAR."INGCantidad" * B."Tasa_Facial") / 100) / 360 
			                      * DAYS_BETWEEN(TO_DATE(YEAR(TO_DATE(CAR."EGRFechaOperacion")) || '-01-01'), ADD_DAYS(TO_DATE(CAR."EGRFechaOperacion"), -1))
			                      * COALESCE(NULLIF(TC."TipoCambioMercado", 0), 1), 0)
			        END
			    ELSE 
			        CASE 
			            WHEN INS."IdTipoReajuste" IN ('CLP', '$', 'PESO') THEN
			                ROUND(((CAR."INGCantidad" * B."Tasa_Facial") / 100) / 360 
			                      * DAYS_BETWEEN(TO_DATE(CAR."FechaOperacion"), ADD_DAYS(TO_DATE(CAR."EGRFechaOperacion"), -1)), 0)
			            ELSE
			                ROUND(((CAR."INGCantidad" * B."Tasa_Facial") / 100) / 360 
			                      * DAYS_BETWEEN(TO_DATE(CAR."FechaOperacion"), ADD_DAYS(TO_DATE(CAR."EGRFechaOperacion"), -1))
			                      * COALESCE(NULLIF(TC."TipoCambioMercado", 0), 1), 0)
			        END
			END AS "InteresDevengadoPesos"
			,NULL AS "RetencionMonedaUnidadReajuste4porciento"
			,NULL AS  "RetencionPesos4porciento"
		FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
		INNER JOIN (SELECT DISTINCT "Instrumento", MAX("Tasa_Facial") "Tasa_Facial"
					FROM "BSCL.Normativa.BD::TRI.InstrumentosArticulo104" WHERE "Periodo" >= 2019 GROUP BY "Instrumento"
					) B ON trim(CAR."NemoSVS") = trim(B."Instrumento")
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOOPERACION	ON TIPOOPERACION."IdTipoOperacion" = CAR."EGRIdTipoOperacion"	
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = CAR."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" EMI ON INS."IdEmisor" = EMI."IdEmisor"
		LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio"  SOC ON CAR ."IdCliente" = SOC."IdCliente"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona"  PER ON SOC ."IdPersona" = PER."IdPersona"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDE ON PER."IdDocumentoIdentidad" = IDE."IdDocumentoIdentidad"
		LEFT JOIN (SELECT "IdPortafolio", MAX("Codigo") AS "Codigo"  FROM "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" WHERE "IdBoveda" ='DCV' GROUP BY "IdPortafolio") AS CTA ON CTA."IdPortafolio"=CAR."IdPortafolio"
		LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HMONREAJ
				ON (HMONREAJ."Esquema"='DJ1874' AND TRIM(INS."IdTipoReajuste")= TRIM(HMONREAJ."ValorOrigen") AND HMONREAJ."Tipo"='MONEDAREAJUSTE' AND  TO_DATE(CAR."FechaOperacion") BETWEEN TO_DATE(HMONREAJ."FechaDatosDesde") AND TO_DATE(HMONREAJ."FechaDatosHasta"))
		LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" TC ON TC."IdMoneda" = HMONREAJ."ValorOrigen"  AND TC."Fecha" = TO_VARCHAR(TO_DATE(CAR."EGRFechaOperacion"), 'YYYY-MM-DD')
		WHERE 
			CAR."IdClasificacionTipoInstrumento"='RF'
		--AND  YEAR((CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN TO_DATE(CAR."INGFechaOperacion")   ELSE TO_DATE(CAR."EGRFechaOperacion")  END)) =YEAR(TO_DATE(CAR."FechaOperacion"))
		--AND  YEAR(TO_DATE(CAR."EGRFechaOperacion")) = YEAR(TO_DATE(CAR."FechaOperacion"))   
		AND  (CAR."EGRIdOperacion">0 OR  CAR."EGRFolioComprobante">0)
		AND  CAR."EGRIdTipoOperacion"  NOT IN ('VCDPAC','ANUCPA','ANUVPA','ANTPAC','COMPAC','VENPAC','ANUPAC' )
		--AND TIPOOPERACION."Accion" IN ('VENTA','RETIRO')
		AND CAR."IdEstado" <>'ANU' AND CAR."EGRMonto">0
		--AND CAR."FolioComprobante" IN (138142)
		
		UNION ALL
		--CARTERAS VIGENTES
		--SELECT TIPOOPERACION."Accion", CAR."INGIdTipoOperacion"	,CAR."EGRIdOperacion" ,  CAR."EGRFolioComprobante",CAR."EGRIdOperacion",CAR."FolioComprobante" ,CAR."Nemotecnico",
		SELECT
			CAR."IdEmpresa" AS "IdEmpresa"
			,0 AS "IdOperacion"
			--	,CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN CAR."FolioComprobante"   ELSE CAR."EGRFolioComprobante"   END 	AS "Numero"	
			,CAR."EGRFolioComprobante"	AS "Numero"	
			,COALESCE(EMI."DocumentoIdentidad",'') AS "RutEmisor"
			,COALESCE(IDE."DocumentoIdentidad",'') AS "RutInversionista"
			,CASE COALESCE("ResidenciaTributariaExtranjera",'N') WHEN 'S' THEN 2 ELSE 1 END AS "TipoContribuyente" --CASE para cuando vive o no en chile
			,CAR."NemoSVS"  AS "NemotecnicoSVS"
			,CAR."Nemotecnico"  AS "Nemotecnico"
		--	,CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN TO_DATE(CAR."INGFechaOperacion")   ELSE TO_DATE(CAR."EGRFechaOperacion")  END 	AS "FechaOperacion"	
			,TO_DATE(CAR."FechaOperacion")	AS "FechaOperacion"
			,COALESCE(HMONREAJ."ValorDestino", TO_VARCHAR('9')) AS  "MonedaUnidadReajuste" --HOMOLOGAR
			,COALESCE(CTA."Codigo",'0') AS "NumeroCuentaDCV"
		--	,CASE TIPOOPERACION."Accion" WHEN 'VENTA'  THEN CAR."INGMontoATasaMercadoActual" ELSE 0 END AS "MovimientoSaldoInicial"
			,0 AS "MovimientoSaldoInicial" 
			,0 AS "MovimientoAdquision"
			--,CAR."EGRCantidad" AS "MovimientoEnajenacion"
			,0 AS "MovimientoEnajenacion"
			,0 AS "MovimientoRescateAnticipado"
			--,0 AS "MovimientoSaldoFinal"
		    ,ROUND(CASE WHEN HMONREAJ."ValorOrigen" IN ('CLP', '$', 'PESO', '1') THEN ((CAR."INGCantidad" / CAR."Cantidad") * CAR."MontoCompra") ELSE (((CAR."INGCantidad" / CAR."Cantidad") * CAR."MontoCompra") / COALESCE(NULLIF(TC."TipoCambioMercado", 0),1))  END,0) AS "MovimientoSaldoFinal"
			,ROUND(CAST(COALESCE(CAR."INGCantidad", 0) AS DECIMAL(26,8)),0) AS "SaldoFinalNominal"
			,0 AS "ResultadoEnajenacionMonedaUnidadReajuste"
			,0 AS "ResultadoEnajenacionPesos" 
			,0 AS "RetencionMonedaUnidadReajuste"
			,0 AS "RetencionPesos"
			,CAR."IdCliente" AS "IdCliente"
			,CAR."IdPortafolio" AS "IdPortafolio"
			,CAR."EGRIdTipoOperacion" AS "IdTipoOperacion"
			,CAR."IdTipoProducto"
			,YEAR((CAR."FechaRespaldo")) || '-12-31' AS "FechaAdquisicion"

			--,CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN 3 ELSE 4 END AS "TipoDocumento"
			,4 AS "TipoDocumento"
			,CAR."FolioCartera"
			-----------------------------------------------------------------------------------------categoria
			,CAR."IdClasificacionTipoInstrumento" 			--RV/RF/IF
			,CAR."IdTipoInstrumento"						--PAC/AUT/DEP
			,CAR."IdInstrumento"
			-----------------------------------------------------------------------------------------instrumento
			,TO_VARCHAR (TO_date(CAR."FechaVencimientoInstrumento")) AS "InsFechaVencimiento"
			,TO_VARCHAR (TO_date(CAR."FechaEmisionInstrumento")) 	AS "InsFechaEmision"
			,INS."IdMoneda" AS "IdMonedaInstrumento"
			,INS."IdMonedaEmision" AS "IdMonedaEmisionInstrumento"
			,INS."IdTipoReajuste" AS "IdTipoReajusteInstrumento" 
			,INS."IdEmisor"  AS "IdEmisor"								--valorizacionCL
			-----------------------------------------------------------------------------------------operacion
			
		--	,CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN TO_VARCHAR (TO_date(CAR."INGFechaLiquidacion")) ELSE TO_VARCHAR (TO_date(CAR."EGRFechaLiquidacion")) END 	AS "FechaLiquidacion" 
			,TO_VARCHAR (TO_date(CAR."EGRFechaLiquidacion")) 	AS "FechaLiquidacion"
		--	,CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN CAR."INGCantidad" ELSE CAR."EGRCantidad"   END 	AS "Cantidad"							               
			,CAR."INGCantidad" 	AS "Cantidad"
			,B."Tasa_Facial" AS "Tasa"	
		--	,CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN CAR."INGPrecioCompra" ELSE CAR."EGRPrecio" END 	AS "Precio"
			,CAR."EGRPrecio"AS "Precio"
			,CAR."IdMonedaAdicional"
			,CASE 
			    WHEN TO_DATE(CAR."FechaOperacion") < TO_DATE(YEAR(TO_DATE(CAR."FechaOperacion")) || '-01-01')
			    THEN ROUND(((CAR."INGCantidad" * B."Tasa_Facial") / 100) / 360 
			               * DAYS_BETWEEN(TO_DATE(YEAR(TO_DATE(CAR."FechaOperacion")) || '-01-01'), ADD_DAYS(TO_DATE(YEAR(TO_DATE(CAR."FechaOperacion")) || '-12-31'), -1)), 0)
			    ELSE ROUND(((CAR."INGCantidad" * B."Tasa_Facial") / 100) / 360 
			               * DAYS_BETWEEN(TO_DATE((SELECT CASE 
			                        WHEN MAX(CASE WHEN CAR2."EGRFechaOperacion" > TO_DATE(YEAR(TO_DATE(CAR2."FechaOperacion")) || '-12-31') THEN '0001-01-01' ELSE CAR2."EGRFechaOperacion" END) = '0001-01-01' THEN CAR."FechaOperacion"
			                        ELSE MAX(CASE WHEN CAR2."EGRFechaOperacion" > TO_DATE(YEAR(TO_DATE(CAR2."FechaOperacion")) || '-12-31') THEN '0001-01-01' ELSE CAR2."EGRFechaOperacion" END) END FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR2 WHERE CAR2."FolioCartera" = CAR."FolioCartera")), ADD_DAYS(TO_DATE(YEAR(TO_DATE(CAR."FechaOperacion")) || '-12-31'), -1)), 0)
			 END AS "InteresDevengadoMonedaUnidadReajuste"
			,CASE 
			    WHEN TO_DATE(CAR."FechaOperacion") < TO_DATE(YEAR(TO_DATE(CAR."FechaOperacion")) || '-01-01')
			    THEN 
			        CASE 
			            WHEN INS."IdTipoReajuste" IN ('CLP', '$', 'PESO') THEN
			                ROUND(((CAR."INGCantidad" * B."Tasa_Facial") / 100) / 360 
			                      * DAYS_BETWEEN(TO_DATE(YEAR(TO_DATE(CAR."FechaOperacion")) || '-01-01'), ADD_DAYS(TO_DATE(YEAR(TO_DATE(CAR."FechaOperacion")) || '-12-31'), -1)), 0)
			            ELSE
			                ROUND(((CAR."INGCantidad" * B."Tasa_Facial") / 100) / 360 
			                      * DAYS_BETWEEN(TO_DATE(YEAR(TO_DATE(CAR."FechaOperacion")) || '-01-01'), ADD_DAYS(TO_DATE(YEAR(TO_DATE(CAR."FechaOperacion")) || '-12-31'), -1))
			                      * COALESCE(NULLIF(TC."TipoCambioMercado", 0), 1), 0)
			        END
			    ELSE 
			        CASE 
			            WHEN INS."IdTipoReajuste" IN ('CLP', '$', 'PESO') THEN
			                ROUND(((CAR."INGCantidad" * B."Tasa_Facial") / 100) / 360 
			                      * DAYS_BETWEEN(TO_DATE((SELECT CASE 
			                        WHEN MAX(CASE WHEN CAR2."EGRFechaOperacion" > TO_DATE(YEAR(TO_DATE(CAR2."FechaOperacion")) || '-12-31') THEN '0001-01-01' ELSE CAR2."EGRFechaOperacion" END) = '0001-01-01' THEN CAR."FechaOperacion"
			                        ELSE MAX(CASE WHEN CAR2."EGRFechaOperacion" > TO_DATE(YEAR(TO_DATE(CAR2."FechaOperacion")) || '-12-31') THEN '0001-01-01' ELSE CAR2."EGRFechaOperacion" END) END FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR2 WHERE CAR2."FolioCartera" = CAR."FolioCartera")), ADD_DAYS(TO_DATE(YEAR(TO_DATE(CAR."FechaOperacion")) || '-12-31'), -1)), 0)
			            ELSE
			                ROUND(((CAR."INGCantidad" * B."Tasa_Facial") / 100) / 360 
			                      * DAYS_BETWEEN(TO_DATE((SELECT CASE 
			                        WHEN MAX(CASE WHEN CAR2."EGRFechaOperacion" > TO_DATE(YEAR(TO_DATE(CAR2."FechaOperacion")) || '-12-31') THEN '0001-01-01' ELSE CAR2."EGRFechaOperacion" END) = '0001-01-01' THEN CAR."FechaOperacion"
			                        ELSE MAX(CASE WHEN CAR2."EGRFechaOperacion" > TO_DATE(YEAR(TO_DATE(CAR2."FechaOperacion")) || '-12-31') THEN '0001-01-01' ELSE CAR2."EGRFechaOperacion" END) END FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR2 WHERE CAR2."FolioCartera" = CAR."FolioCartera")), ADD_DAYS(TO_DATE(YEAR(TO_DATE(CAR."FechaOperacion")) || '-12-31'), -1))
			                      * COALESCE(NULLIF(TC."TipoCambioMercado", 0), 1), 0)
			        END
			END AS "InteresDevengadoPesos"
			,NULL AS "RetencionMonedaUnidadReajuste4porciento"
			,NULL AS  "RetencionPesos4porciento"
		FROM "BSCL.Custodia.BD::HIS.Cartera" AS CAR
		INNER JOIN (SELECT DISTINCT "Instrumento", MAX("Tasa_Facial") "Tasa_Facial"
					FROM "BSCL.Normativa.BD::TRI.InstrumentosArticulo104" WHERE "Periodo" >= 2019 GROUP BY "Instrumento"
					) B ON trim(CAR."NemoSVS") = trim(B."Instrumento")
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOOPERACION	ON TIPOOPERACION."IdTipoOperacion" = CAR."EGRIdTipoOperacion"	
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = CAR."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" EMI ON INS."IdEmisor" = EMI."IdEmisor"
		LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio"  SOC ON CAR ."IdCliente" = SOC."IdCliente"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona"  PER ON SOC ."IdPersona" = PER."IdPersona"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDE ON PER."IdDocumentoIdentidad" = IDE."IdDocumentoIdentidad"
		LEFT JOIN (SELECT "IdPortafolio", MAX("Codigo") AS "Codigo"  FROM "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" WHERE "IdBoveda" ='DCV' GROUP BY "IdPortafolio") AS CTA ON CTA."IdPortafolio"=CAR."IdPortafolio"
		LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HMONREAJ
				ON (HMONREAJ."Esquema"='DJ1874' AND TRIM(INS."IdTipoReajuste")= TRIM(HMONREAJ."ValorOrigen") AND HMONREAJ."Tipo"='MONEDAREAJUSTE' AND  TO_DATE(CAR."FechaOperacion") BETWEEN TO_DATE(HMONREAJ."FechaDatosDesde") AND TO_DATE(HMONREAJ."FechaDatosHasta"))
		LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" TC ON TC."IdMoneda" = HMONREAJ."ValorOrigen"  AND TC."Fecha" = TO_VARCHAR(TO_DATE(CAR."FechaOperacion"), 'YYYY-MM-DD')
		INNER JOIN(SELECT "FechaRespaldoFinal" FROM (
				SELECT  MAX("FechaRespaldo") AS "FechaRespaldoFinal" ,YEAR("FechaRespaldo") FROM  "BSCL.Custodia.BD::HIS.Cartera" 
				WHERE   "IdClasificacionTipoInstrumento"='RF' AND MONTH("FechaRespaldo")='12'
				GROUP BY YEAR("FechaRespaldo")
				ORDER BY YEAR("FechaRespaldo") DESC
			))GRFECHA	ON CAR."FechaRespaldo"=GRFECHA."FechaRespaldoFinal"
		WHERE CAR."IdClasificacionTipoInstrumento"='RF'
		AND  CAR."IdTipoOperacion"  NOT IN ('VCDPAC','ANUCPA','ANUVPA','ANTPAC','COMPAC','VENPAC','ANUPAC' )
		AND CAR."IdEstado"='VIG'
		
		
		UNION ALL
		/*
		TIPO 5 Y 6
		---PACTOS
		-- 3(5) - ADQUISICION - VP VENTA PACTOS 			(VENTA DE CARTERAS COMPRADAS EN EL PERIODO)   	-- FECHA ADQUISICION CARTERA (AÑO VTA = AÑO COM
		-- 4(6) - ENAJENACION - CP COMPRA PACTOS			(COMPRA DE CARTERAS EN EL PERIODO)				-- FECHA ADQUISICION CARTERA	
		SELECT 
			CAR."IdEmpresa" AS "IdEmpresa"
			,0 AS "IdOperacion"
			,CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN CAR."FolioComprobante"   ELSE CAR."EGRFolioComprobante"   END 	AS "Numero"	
			,COALESCE(EMI."DocumentoIdentidad",'') AS "RutEmisor"
			,COALESCE(IDE."DocumentoIdentidad",'') AS "RutInversionista"
			,CASE COALESCE("ResidenciaTributariaExtranjera",'N') WHEN 'S' THEN 2 ELSE 1 END AS "TipoContribuyente" --CASE para cuando vive o no en chile
			,CAR."NemoSVS"  AS "NemotecnicoSVS"
			,CAR."Nemotecnico"  AS "Nemotecnico"
			,CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN TO_DATE(CAR."INGFechaOperacion")   ELSE TO_DATE(CAR."EGRFechaOperacion")  END 	AS "FechaOperacion"	
			,COALESCE(HMONREAJ."ValorDestino", TO_VARCHAR('9')) AS  "MonedaUnidadReajuste" --HOMOLOGAR
			,COALESCE(CTA."Codigo",'0') AS "NumeroCuentaDCV"
			,CASE TIPOOPERACION."Accion" WHEN 'VENTA'  THEN CAR."INGMontoATasaMercadoActual" ELSE 0 END AS "MovimientoSaldoInicial"
			,CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN CAR."INGMontoATasaMercadoActual" ELSE 0 END AS "MovimientoAdquision"
			,CASE TIPOOPERACION."Accion" WHEN 'VENTA'  THEN CAR."INGMontoATasaMercadoActual" ELSE 0 END AS "MovimientoEnajenacion"
			,0 AS "MovimientoRescateAnticipado"
			,0 AS "MovimientoSaldoFinal"
			,0 AS "SaldoFinalNominal"
			,0 AS "ResultadoEnajenacionMonedaUnidadReajuste"
			,0 AS "ResultadoEnajenacionPesos"
			,0 AS "RetencionMonedaUnidadReajuste"
			,0 AS "RetencionPesos"
			,CAR."IdCliente" AS "IdCliente"
			,CAR."IdPortafolio" AS "IdPortafolio"
			,CAR."IdTipoOperacion"
			,CAR."IdTipoProducto"
			,TO_DATE(CAR."FechaOperacion") AS "FechaAdquisicion"
			,CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN 6 ELSE 5 END AS "TipoDocumento"
			,CAR."FolioCartera"
			-----------------------------------------------------------------------------------------categoria
			,CAR."IdClasificacionTipoInstrumento" 			--RV/RF/IF
			,CAR."IdTipoInstrumento"						--PAC/AUT/DEP
			,CAR."IdInstrumento"
			-----------------------------------------------------------------------------------------instrumento
			,TO_VARCHAR (TO_date(CAR."FechaVencimientoInstrumento")) AS "InsFechaVencimiento"
			,TO_VARCHAR (TO_date(CAR."FechaEmisionInstrumento")) 	AS "InsFechaEmision"
			,INS."IdMoneda" AS "IdMonedaInstrumento"
			,INS."IdMonedaEmision" AS "IdMonedaEmisionInstrumento"
			,INS."IdTipoReajuste" AS "IdTipoReajusteInstrumento" 
			,INS."IdEmisor"  AS "IdEmisor"								--valorizacionCL
			-----------------------------------------------------------------------------------------operacion
			--,CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN CAR."INGIdCondicionLiquidacion" ELSE CAR."EGRIdCondicionLiquidacion" END 	AS "IdCondicionDeLiquidacion" --valorizacionCL
			,CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN TO_VARCHAR (TO_date(CAR."INGFechaLiquidacion")) ELSE TO_VARCHAR (TO_date(CAR."EGRFechaLiquidacion")) END 	AS "FechaLiquidacion" 
			,CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN CAR."INGCantidad" ELSE CAR."EGRCantidad"   END 	AS "Cantidad"						               
			--,CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN CAR."INGValorRescate" ELSE CAR."EGRValorRescate" END 	AS "ValorRescate"
			,B."Tasa_Facial" AS "Tasa"	
			,CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN CAR."INGPrecioCompra" ELSE CAR."EGRPrecio" 		END 	AS "Precio"
			,CAR."IdMonedaAdicional"
			,0 AS "InteresDevengadoMonedaUnidadReajuste"
			,0 AS "InteresDevengadoPesos"
			,NULL AS "RetencionMonedaUnidadReajuste4porciento"
			,NULL AS  "RetencionPesos4porciento"
		FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
		INNER JOIN (SELECT DISTINCT "Instrumento", MAX("Tasa_Facial") "Tasa_Facial"
					FROM "BSCL.Normativa.BD::TRI.InstrumentosArticulo104" WHERE "Periodo" >= 2019 GROUP BY "Instrumento"
					) B ON trim(CAR."Nemotecnico") = trim(B."Instrumento") 
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOOPERACION	ON TIPOOPERACION."IdTipoOperacion" = CAR."IdTipoOperacion"	
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = CAR."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" EMI ON INS."IdEmisor" = EMI."IdEmisor"
		LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio"  SOC ON CAR ."IdCliente" = SOC."IdCliente"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona"  PER ON SOC ."IdPersona" = PER."IdPersona"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDE ON PER."IdDocumentoIdentidad" = IDE."IdDocumentoIdentidad"
		LEFT JOIN (SELECT "IdPortafolio", MAX("Codigo") AS "Codigo"  FROM "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" WHERE "IdBoveda" ='DCV' GROUP BY "IdPortafolio") AS CTA ON CTA."IdPortafolio"=CAR."IdPortafolio"
		LEFT JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HMONREAJ
				ON (HMONREAJ."Esquema"='DJ1874' AND TRIM(INS."IdTipoReajuste")= TRIM(HMONREAJ."ValorOrigen") AND HMONREAJ."Tipo"='MONEDAREAJUSTE' AND  TO_DATE(CAR."FechaOperacion") BETWEEN TO_DATE(HMONREAJ."FechaDatosDesde") AND TO_DATE(HMONREAJ."FechaDatosHasta"))
		WHERE CAR."IdTipoOperacion"  IN ('COMPAC', 'VENPAC')
		AND  CAR."IdClasificacionTipoInstrumento"='RF'
		AND YEAR((CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN TO_DATE(CAR."INGFechaOperacion")   ELSE TO_DATE(CAR."EGRFechaOperacion")  END)) =YEAR(TO_DATE(CAR."FechaOperacion"))
		AND TO_DATE(CAR."FechaLiquidacion") <>'0001-01-01'
		AND EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::NOR.Configuracion" N WHERE N."CodigoParametro"='DJ1874_INCLUIR_PACTOS' AND UPPER(N."Valor")='S')
		UNION ALL
		*/
		
		/**CARTERAS PERIODO ANTERIOR *****/
		
		/* TIPO 1*/
		-- 1     - ADQUISICION - OPERACIONES DE COMPRAS (CARTERAS AÑO ANTERIOR SIN VENTAS) 				-- FECHA ADQUISICION CARTERA AÑO-1 						 		// (FECHA_OPERACION = AÑO-1 - 01-01)
		/*TIPO 2*/
		-- 4(2)  - ENAJENACION - OPERACIONES DE VENTAS  (CARTERAS AÑO ANTERIOR CON VENTA TOTAL O PARCIAL) -- FECHA ADQUISICION CARTERA AÑO-1 (AÑO VTA  - 1 = AÑO COM) 	// (FECHA_OPERACION = AÑO-1 - 12-31)
		SELECT 
			CAR."IdEmpresa" AS "IdEmpresa"
			,0 AS "IdOperacion"
			,CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN CAR."FolioComprobante"   ELSE CAR."EGRFolioComprobante"   END 	AS "Numero"	 
			,COALESCE(EMI."DocumentoIdentidad",'') AS "RutEmisor"
			,COALESCE(IDE."DocumentoIdentidad",'') AS "RutInversionista"
			,CASE COALESCE("ResidenciaTributariaExtranjera",'N') WHEN 'S' THEN 2 ELSE 1 END AS "TipoContribuyente" --CASE para cuando vive o no en chile
			,CAR."NemoSVS"  AS "NemotecnicoSVS"
			,CAR."Nemotecnico"  AS "Nemotecnico"
--			,CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN YEAR(TO_DATE(CAR."INGFechaOperacion", 'YYYY-MM-DD')) || '-01-01'   ELSE TO_DATE(CAR."EGRFechaOperacion")  END 	AS "FechaOperacion"	
			,CAR."FechaOperacion" AS "FechaOperacion"
			,COALESCE(HMONREAJ."ValorDestino", TO_VARCHAR('9')) AS  "MonedaUnidadReajuste" --HOMOLOGAR
			,COALESCE(CTA."Codigo",'0') AS "NumeroCuentaDCV"
			,ROUND (CASE WHEN HMONREAJ."ValorOrigen" IN ('CLP', '$', 'PESO', '1') THEN ((CAR."INGCantidad" / CAR."Cantidad") * CAR."MontoCompra") ELSE (((CAR."INGCantidad" / CAR."Cantidad") * CAR."MontoCompra") / COALESCE(NULLIF(TC."TipoCambioMercado", 0),1))  END,0) AS "MovimientoSaldoInicial" 
			,0 AS "MovimientoAdquision" 
			,0 AS "MovimientoEnajenacion" 
			,0 AS "MovimientoRescateAnticipado"
			,0 AS "MovimientoSaldoFinal" 
			,0 AS "SaldoFinalNominal" 
			,0 AS "ResultadoEnajenacionMonedaUnidadReajuste" 
			,0 AS "ResultadoEnajenacionPesos" 
			,0 AS "RetencionMonedaUnidadReajuste"
			,0 AS "RetencionPesos"
			,CAR."IdCliente" AS "IdCliente"
			,CAR."IdPortafolio" AS "IdPortafolio"
			,CAR."IdTipoOperacion"
			,CAR."IdTipoProducto"
			
			--, YEAR(TO_DATE(HIS."FechaOperacion", 'YYYY-MM-DD')) +1 || RIGHT(TO_DATE(HIS."FechaOperacion", 'YYYY-MM-DD'),6) AS "FechaAdquisicion"
			, YEAR((CAR."FechaRespaldo"))+1 || '-12-31' AS "FechaAdquisicion"
			,CASE WHEN CAR."INGCantidad" = CAR."INGCantidad" THEN 1 ELSE 2 END  AS "TipoDocumento"
			,CAR."FolioCartera"
			-----------------------------------------------------------------------------------------categoria
			,CAR."IdClasificacionTipoInstrumento" 			--RV/RF/IF
			,CAR."IdTipoInstrumento"						--PAC/AUT/DEP
			,CAR."IdInstrumento"
			-----------------------------------------------------------------------------------------instrumento
			,TO_VARCHAR (TO_date(CAR."FechaVencimientoInstrumento")) AS "InsFechaVencimiento"
			,TO_VARCHAR (TO_date(CAR."FechaEmisionInstrumento")) 	AS "InsFechaEmision"
			,INS."IdMoneda" AS "IdMonedaInstrumento"
			,INS."IdMonedaEmision" AS "IdMonedaEmisionInstrumento"
			,INS."IdTipoReajuste" AS "MonedaUnidadReajuste" 
			,INS."IdEmisor"  AS"IdEmisor"								--valorizacionCL
			-----------------------------------------------------------------------------------------operacion
			--,CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN CAR."INGIdCondicionLiquidacion" ELSE CAR."EGRIdCondicionLiquidacion" END 	AS "IdCondicionDeLiquidacion" --valorizacionCL
			,CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN TO_VARCHAR (TO_date(CAR."INGFechaLiquidacion")) ELSE TO_VARCHAR (TO_date(CAR."EGRFechaLiquidacion")) END 	AS "FechaLiquidacion" 
			,CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN CAR."INGCantidad" ELSE CAR."EGRCantidad"   END 	AS "Cantidad"					               
			--,CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN CAR."INGValorRescate" ELSE CAR."EGRValorRescate" END 	AS "ValorRescate"
			,B."Tasa_Facial" AS "Tasa"	
			,CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN CAR."INGPrecioCompra" ELSE CAR."EGRPrecio" END 	AS "Precio"
			,CAR."IdMonedaAdicional"
			,0 AS "InteresDevengadoMonedaUnidadReajuste"
			,0 AS "InteresDevengadoPesos"
			,NULL AS "RetencionMonedaUnidadReajuste4porciento"
			,NULL AS  "RetencionPesos4porciento"
		
		--SELECT CAR.*	
				--FROM  "BSCL.Custodia.BD::HIS.Cartera" AS CAR
		FROM (
					SELECT HISFEC.*
					FROM (
							SELECT H.*
							,RANK() over (partition by H."FolioCartera", YEAR(H."FechaRespaldo") order by H."IdCartera" desc) as RANK ---Solo ultimo reg de cartera (Saldo final del periodo)
							FROM "BSCL.Custodia.BD::HIS.Cartera" H
							INNER JOIN
								(			
									SELECT TOP 5 "FechaRespaldoFinal" FROM ---Fechas finales de respaldo por periodo (acotado a 5 periodos 
									(
										SELECT  MAX("FechaRespaldo") AS "FechaRespaldoFinal" ,YEAR("FechaRespaldo") FROM  "BSCL.Custodia.BD::HIS.Cartera" 
										WHERE   "IdClasificacionTipoInstrumento"='RF'-- AND MONTH("FechaRespaldo")='12'
										GROUP BY YEAR("FechaRespaldo")
										ORDER BY YEAR("FechaRespaldo") DESC
									)
								)GRFECHA	
							ON 	H."FechaRespaldo"=GRFECHA."FechaRespaldoFinal"
							)HISFEC WHERE HISFEC.RANK = 1
					)CAR -- ON CAR."FolioCartera"=HIS."FolioCartera" AND YEAR(TO_DATE(CAR."FechaRespaldo")) = YEAR(TO_DATE(HIS."FechaRespaldo"))+1	
		/*
		INNER JOIN (
					SELECT HISFEC."FechaRespaldo",HISFEC."FolioCartera",HISFEC."FechaOperacion",HISFEC."INGCantidad",HISFEC."INGMontoATasaMercadoActual",HISFEC."MontoCompra", HISFEC."EGRMonto",HISFEC."EGRFechaOperacion"
					FROM (
							SELECT H."FechaRespaldo",H."FolioCartera",H."FechaOperacion",H."INGCantidad",H."INGMontoATasaMercadoActual",H."MontoCompra",H."EGRMonto",H."EGRFechaOperacion"
							,RANK() over (partition by H."FolioCartera", YEAR(H."FechaRespaldo") order by H."IdCartera" desc) as RANK ---Solo ultimo reg de cartera (Saldo final del periodo)
							FROM "BSCL.Custodia.BD::HIS.Cartera" H
							       INNER JOIN
							        (   
							         SELECT TOP 5 "FechaRespaldoFinal" FROM ---Fechas finales de respaldo por periodo (acotado a 5 periodos 
							         (
							          SELECT  MAX("FechaRespaldo") AS "FechaRespaldoFinal" ,YEAR("FechaRespaldo") FROM  "BSCL.Custodia.BD::HIS.Cartera" 
							          WHERE   "IdClasificacionTipoInstrumento"='RF' AND MONTH("FechaRespaldo")='12'
							          GROUP BY YEAR("FechaRespaldo")
							          ORDER BY YEAR("FechaRespaldo") DESC
							         )
							        )GRFECHA 
							       ON  H."FechaRespaldo"=GRFECHA."FechaRespaldoFinal"
							)HISFEC WHERE HISFEC.RANK = 1
					)HIS_ANT ON CAR."FolioCartera"=HIS_ANT."FolioCartera" AND YEAR(TO_DATE(CAR."FechaRespaldo")) = YEAR(TO_DATE(HIS_ANT."FechaRespaldo"))+1
				*/	
		INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOOPERACION	ON TIPOOPERACION."IdTipoOperacion" = CAR."IdTipoOperacion"	
		INNER JOIN (SELECT DISTINCT "Instrumento", MAX("Tasa_Facial") "Tasa_Facial"
					FROM "BSCL.Normativa.BD::TRI.InstrumentosArticulo104" WHERE "Periodo" >= 2019 GROUP BY "Instrumento"
					) B ON trim(CAR."Nemotecnico") = trim(B."Instrumento")
					
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = CAR."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" EMI ON INS."IdEmisor" = EMI."IdEmisor"
		LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio"  SOC ON CAR ."IdCliente" = SOC."IdCliente"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona"  PER ON SOC."IdPersona" = PER."IdPersona"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDE ON PER."IdDocumentoIdentidad" = IDE."IdDocumentoIdentidad"
		LEFT JOIN (SELECT "IdPortafolio", MAX("Codigo") AS "Codigo"  FROM "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" WHERE "IdBoveda" ='DCV' GROUP BY "IdPortafolio") AS CTA ON CTA."IdPortafolio"=CAR."IdPortafolio"
		LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" TC ON TC."IdMoneda" = INS."IdTipoReajuste" AND TC."Fecha" = TO_VARCHAR(TO_DATE(CAR."FechaOperacion"), 'YYYY-MM-DD')
		LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HMONREAJ
				ON (HMONREAJ."Esquema"='DJ1874' AND TRIM(INS."IdTipoReajuste")= TRIM(HMONREAJ."ValorOrigen") AND HMONREAJ."Tipo"='MONEDAREAJUSTE' AND  TO_DATE(CAR."FechaOperacion") BETWEEN TO_DATE(HMONREAJ."FechaDatosDesde") AND TO_DATE(HMONREAJ."FechaDatosHasta"))
		WHERE CAR."IdTipoOperacion"  NOT IN ('COMPAC', 'VENPAC')
		AND  CAR."IdClasificacionTipoInstrumento"='RF'
		AND CAR."IdEstado" = 'VIG'
)  ORI

LEFT JOIN 
(
		SELECT CTCORR."IdPortafolio",PO."IdEmpresa",CTCORR."Codigo" 
		FROM (
			SELECT MIN(POR."IdPortafolio") AS "IdPortafolio"  ,EMP."IdEmpresa" FROM "BSCL.Plataforma.BD::PLA.Empresa" EMP
			INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON (SOC."IdSocioDeNegocio"=EMP."IdSocioDeNegocio")
			INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON (POR."IdCliente"=SOC."IdCliente")
		GROUP  BY EMP."IdEmpresa"
		)AS PO LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" CTCORR ON (PO."IdPortafolio" =CTCORR."IdPortafolio" AND CTCORR."IdBoveda" ='DCV')
) DCVCOR ON (DCVCOR."IdEmpresa"=ORI."IdEmpresa")

) AS origen ) AS a
WHERE (a."MovimientoSaldoInicial"+a."MovimientoAdquision"+a."MovimientoEnajenacion"+a."InteresDevengadoPesos"+a."InteresDevengadoMonedaUnidadReajuste"+a."ResultadoEnajenacionPesos"+a."ResultadoEnajenacionMonedaUnidadReajuste"+a."MovimientoSaldoFinal") != 0
GROUP BY				
	a."RutInversionista", 
	a."Nemotecnico",
	YEAR(a."FechaAdquisicion")

WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1874.Origen" TO bcs_desarrollador WITH GRANT OPTION;