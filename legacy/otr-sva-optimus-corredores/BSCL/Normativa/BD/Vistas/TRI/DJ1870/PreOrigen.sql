SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1870.PreOrigen";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1870.PreOrigen" (
	"IdEmpresa"
    ,"ID"
    ,"IdDocumento"												
	,"DocumentoNumero"							
) AS

SELECT DISTINCT
     MASTER."IdEmpresa" 																										AS "IdEmpresa"
    ,0 																															AS "ID"
    ,0 																															AS "IdDocumento"						
	,MASTER."FolioComprobante" 																									AS "DocumentoNumero"
	FROM 		"BSCL.Facturacion.BD::FAC.Comprobante"						AS MASTER
	INNER JOIN  "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalle"  	AS DETALLE 		ON DETALLE."IdComprobante" 			= MASTER."IdComprobante"
	LEFT JOIN  	"BSCL.Facturacion.BD::FAC.MovimientoComprobante" 			AS MOVCOM 		ON MOVCOM."IdComprobante" 			= MASTER."IdComprobante"	
	LEFT JOIN   "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago"	AS CPAGO 		ON CPAGO."IdMovimientoComprobante" 	= MOVCOM."IdMovimientoComprobante"
	LEFT JOIN 	"BSCL.Plataforma.BD::MER.ParametroFinanciero"				AS DOLAR		ON DOLAR."Fecha"					= MASTER."FechaComprobante"		
	LEFT  JOIN  "BSCL.Plataforma.BD::INS.Instrumento"						AS INSTRUMENTO 	ON INSTRUMENTO."IdInstrumento" 		= DETALLE."IdInstrumento"
	INNER JOIN  "BSCL.Normativa.BD::NOR.Configuracion"						AS PARAMETRO	ON MASTER."IdEmpresa" 				= PARAMETRO."IdEmpresa"	
	WHERE 	MASTER."IdEstadoSII" IN  ('ACP','REC')
	AND 	((MASTER."IdTipoProducto" = 'MEME' AND 
			(Case 
	          When MASTER."IdTipoInstrumento" in ('PRT', 'EMP', 'SIM','RRV','RRF') then MASTER."NemotecnicoSubyacente" 
	        Else MASTER."NemotecnicoInstrumento"
			end) IN ('INTERB','ARBINT','DISTRI','ARBDIS')) OR	-- SPOT	
			 (MASTER."IdTipoProducto" = 'INMO' AND 
			 (Case 
	          When MASTER."IdTipoInstrumento" in ('PRT', 'EMP', 'SIM','RRV','RRF') then MASTER."NemotecnicoSubyacente" 
	        Else MASTER."NemotecnicoInstrumento"
			end) IN ('DOLAR','EURO')))				-- MONETARIO	
	AND 	DOLAR."IdParametroFinanciero" IN ('DO','EUR')
    -- FORMA DE PAGO	
    AND 	IFNULL(CPAGO."IdTipoMovimientoSpot",'') = CASE  
														WHEN DETALLE."AccionTipoOperacion" = 'COMPRA' THEN  
															CASE WHEN  DETALLE."NemotecnicoInstrumento"  = 'INTERB'  THEN 'MOVEGR'  -- SPOT
																 WHEN  DETALLE."NemotecnicoInstrumento"  = 'ARBINT'  THEN 'MOVING'	-- ARBITRAJE
																 WHEN  DETALLE."NemotecnicoInstrumento"  = 'DISTRI'  THEN 'MOVEGR' 	-- SEMI-ARBITRAJE
																 WHEN  DETALLE."NemotecnicoInstrumento"  = 'ARBDIS'  THEN 'MOVING' 	-- ARBITRAJE DISTRIBUCION
																 ELSE ''
															END
														WHEN DETALLE."AccionTipoOperacion" = 'VENTA' THEN	
															CASE WHEN DETALLE."NemotecnicoInstrumento"  = 'INTERB'  THEN 'MOVING'	-- SPOT
																 WHEN DETALLE."NemotecnicoInstrumento"  = 'ARBINT'  THEN 'MOVEGR'	-- ARBITRAJE
																 WHEN DETALLE."NemotecnicoInstrumento"	= 'DISTRI'  THEN 'MOVING'	-- SEMI-ARBITRAJE
																 WHEN DETALLE."NemotecnicoInstrumento"	= 'ARBDIS'  THEN 'MOVEGR'	-- ARBITRAJE DISTRIBUCION
																 ELSE ''
															END 
														ELSE ''
													  END  
-- MONTO > 10.000 USD Validación de Facturas
	AND (CASE 
			WHEN MASTER."IdTipoOperacion"  = 'COMSPT'  THEN 
				CASE WHEN DETALLE."NemotecnicoInstrumento" = 'INTERB' THEN DETALLE."Cantidad"							-- SPOT
					 WHEN DETALLE."NemotecnicoInstrumento" = 'DISTRI' THEN (DETALLE."MontoLiquidacion"/(CASE WHEN MASTER."ValorTipoCambio" = 0 THEN DOLAR."Valor" ELSE MASTER."ValorTipoCambio" END))	-- SEMI-ARBITRAJE
					 WHEN DETALLE."NemotecnicoInstrumento" = 'ARBINT' THEN (DETALLE."MontoLiquidacion"/(CASE WHEN MASTER."ValorTipoCambio" = 0 THEN DOLAR."Valor" ELSE MASTER."ValorTipoCambio" END))	-- ARBITRAJE
					 WHEN DETALLE."NemotecnicoInstrumento" = 'ARBDIS' THEN (DETALLE."MontoLiquidacion"/(CASE WHEN MASTER."ValorTipoCambio" = 0 THEN DOLAR."Valor" ELSE MASTER."ValorTipoCambio" END))	-- ARBITRAJE DISTRIBUCION					 
					 ELSE 0
				END 
			WHEN MASTER."IdTipoOperacion"  = 'COMMON'  THEN 
				CASE WHEN DETALLE."NemotecnicoInstrumento" IN ('DOLAR','EURO')THEN (DETALLE."MontoLiquidacion"/(CASE WHEN MASTER."ValorTipoCambio" = 0 THEN DOLAR."Valor" ELSE MASTER."ValorTipoCambio" END))	-- MONETARIO
				ELSE 0
				END 
			ELSE 0 
		  END >= PARAMETRO."Valor"		  
		OR		
		CASE 
			WHEN MASTER."IdTipoOperacion" = 'VENSPT'   THEN 
				CASE WHEN DETALLE."NemotecnicoInstrumento" = 'INTERB' THEN DETALLE."Cantidad" 	-- DOLAR."Valor"				-- SPOT
					 WHEN DETALLE."NemotecnicoInstrumento" = 'DISTRI' THEN (DETALLE."MontoLiquidacion"/(CASE WHEN MASTER."ValorTipoCambio" = 0 THEN DOLAR."Valor" ELSE MASTER."ValorTipoCambio" END))			-- SEMI-ARBITRAJE 
					 WHEN DETALLE."NemotecnicoInstrumento" = 'ARBINT' THEN (DETALLE."MontoLiquidacion"/(CASE WHEN MASTER."ValorTipoCambio" = 0 THEN DOLAR."Valor" ELSE MASTER."ValorTipoCambio" END))			-- ARBITRAJE
					 WHEN DETALLE."NemotecnicoInstrumento" = 'ARBDIS' THEN (DETALLE."MontoLiquidacion"/(CASE WHEN MASTER."ValorTipoCambio" = 0 THEN DOLAR."Valor" ELSE MASTER."ValorTipoCambio" END))			-- ARBITRAJE DISTRIBUCION
					 ELSE 0
				END 
			WHEN MASTER."IdTipoOperacion" = 'VENMON'   THEN 
				CASE WHEN DETALLE."NemotecnicoInstrumento" IN ('DOLAR','EURO') THEN (DETALLE."MontoLiquidacion"/(CASE WHEN MASTER."ValorTipoCambio" = 0 THEN DOLAR."Valor" ELSE MASTER."ValorTipoCambio" END))	-- MONETARIO
				ELSE 0
				END 	
		ELSE 0  
		END >= PARAMETRO."Valor"
		)		
	AND DETALLE."IdMoneda" NOT IN (CASE WHEN DETALLE."NemotecnicoInstrumento" IN ('ARBINT','ARBDIS') THEN 'USD' ELSE 'XXX' END )	
	AND PARAMETRO."CodigoParametro" = 'MontoMinimoTributarioDJ1870'	
	
