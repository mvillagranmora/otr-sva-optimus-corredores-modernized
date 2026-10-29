SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1870.Origen";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1870.Origen" (
	"IdEmpresa"
    ,"ID"
    ,"IdDocumento"
    ,"VboUsuario"
    ,"Observaciones"
    ,"FuenteDatos"
   	,"Numero"									
	,"DatosPersonaRut"							
	,"DatosPersonaDV"							
	,"DatosPersonaIndentificadorExtranjero"		
	,"DatosPersonaPaisOtorganteIdentificador"	
	,"DatosPersonaNombreRazonSocial"				
	,"TipoTransaccionDJ"							
	,"ValoresTransados"							
	,"FechaTransaccionDia"						
	,"FechaTransaccionMes"						
	,"FechaTransaccionAnio"						
	,"DocumentoTipoDocumento"					
	,"DocumentoNumero"							
	,"CodigoMonedaTransada"						
	,"MontoCompras"								
	,"MontoVentas"								
	,"TipoCambio"																				
	,"IdCliente"
	,"IdPortafolio"
	,"FechaDocumento"
	,"IdComprobante"
	,"IndNacionalidad"
	,"Paridad"
	,"LineaFactura"
) AS

SELECT DISTINCT	 

     MASTER."IdEmpresa" 																										AS "IdEmpresa"
    ,0 																															AS ID
	,(SELECT TOP 1 "IdDocumento" FROM "BSCL.Normativa.BD::DOC.Documento" doc WHERE "Esquema"='DJ1870') 							AS "IdDocumento"    
    ,'N' 																														AS "VboUsuario"
    ,'' 																														AS "Observaciones"
    ,'INICIAL' 																													AS "FuenteDatos"
    ,0 																															AS "Numero"
	,COALESCE(CLIENTE."DocumentoIdentidad",'00000000-0')																		AS "DatosPersonaRut"							
	,SUBSTRING(CLIENTE."DocumentoIdentidad",LENGTH(CLIENTE."DocumentoIdentidad"),1) 											AS "DatosPersonaDV"							
	,CASE CLIENTE."TipoDocumentoIdentidad" WHEN 'TAX' THEN CLIENTE."DocumentoIdentidad" ELSE '' END 							AS "DatosPersonaIndentificadorExtranjero"		
	,CASE CLIENTE."TipoDocumentoIdentidad" WHEN 'TAX' THEN COALESCE(PAIS."CodigoPais",'') ELSE '' END 							AS "DatosPersonaPaisOtorganteIdentificador"	-- CGO
	,CLIENTE."NombreCompleto" 																									AS "DatosPersonaNombreRazonSocial"											
	,b."ValorDestino"																											AS "TipoTransaccionDJ"
	,CASE COALESCE(CPAGO."IdMedioDePago",'1')			
		WHEN 'EFE' THEN '1'			
		WHEN 'CHE' THEN '2'			
		ELSE '3'			
	END 																														AS "ValoresTransados" --solo se transan monedas CGO									
	,DAYOFMONTH(MASTER."FechaComprobante")	 																					AS "FechaTransaccionDia"
	,MONTH(MASTER."FechaComprobante") 																							AS "FechaTransaccionMes"
	,YEAR(MASTER."FechaComprobante") 																							AS "FechaTransaccionAnio"
	,c."ValorDestino"																											AS "DocumentoTipoDocumento"
	,MASTER."FolioComprobante" 																									AS "DocumentoNumero"
	,CASE WHEN DETALLE."NemotecnicoInstrumento" IN ('DOLAR','EURO') THEN 'USD' ELSE MASTER."IdMonedaSpot" END  					AS "CodigoMonedaTransada"	
	,CASE 
		WHEN MASTER."IdTipoOperacion"  IN ('COMSPT','ANVSPT')    THEN 
			CASE WHEN DETALLE."NemotecnicoInstrumento" = 'INTERB' THEN DETALLE."Cantidad"									-- SPOT
				 WHEN DETALLE."NemotecnicoInstrumento" = 'DISTRI' THEN (CASE WHEN MASTER."IdMonedaSpot" = 'CLP' THEN DETALLE."MontoLiquidacion"
                                                                        ELSE DETALLE."Cantidad" END)		-- SEMI-ARBITRAJE
				 WHEN DETALLE."NemotecnicoInstrumento" = 'ARBINT' THEN (CASE WHEN MASTER."IdMonedaSpot" = 'CLP' THEN DETALLE."MontoLiquidacion"
                                                                        ELSE DETALLE."Cantidad" END)		-- ARBITRAJE
				WHEN DETALLE."NemotecnicoInstrumento" = 'ARBDIS' THEN (CASE WHEN MASTER."IdMonedaSpot" = 'CLP' THEN DETALLE."MontoLiquidacion"
                                                                        ELSE DETALLE."Cantidad" END)		-- ARBITRAJE DISTRIBUCION
				 ELSE 0		
			END 
		WHEN MASTER."IdTipoOperacion"  = 'COMMON'  THEN 
			CASE WHEN DETALLE."NemotecnicoInstrumento" IN ('DOLAR','EURO') THEN DETALLE."MontoLiquidacion"/(CASE WHEN DETALLE."ValorTipoCambio" = 0 THEN TIPOCAMBIO."Valor" 
                                                                                         						 ELSE DETALLE."ValorTipoCambio" 
                                                                                         					END)	-- MONETARIO
				 ELSE 0
			END 
		ELSE 0
	END																															AS "MontoCompras"	
	,CASE 
		WHEN MASTER."IdTipoOperacion" IN ('VENSPT','ANCSPT')   THEN 
			CASE WHEN DETALLE."NemotecnicoInstrumento" = 'INTERB' THEN DETALLE."Cantidad" 	-- DOLAR."Valor"
				 WHEN DETALLE."NemotecnicoInstrumento" = 'DISTRI' THEN (CASE WHEN MASTER."IdMonedaSpot" = 'CLP' THEN DETALLE."MontoLiquidacion"
                                                                        ELSE DETALLE."Cantidad" END)
				 WHEN DETALLE."NemotecnicoInstrumento" = 'ARBINT' THEN (CASE WHEN MASTER."IdMonedaSpot" = 'CLP' THEN DETALLE."MontoLiquidacion"
                                                                        ELSE DETALLE."Cantidad" END)
                 WHEN DETALLE."NemotecnicoInstrumento" = 'ARBDIS' THEN (CASE WHEN MASTER."IdMonedaSpot" = 'CLP' THEN DETALLE."MontoLiquidacion"
                                                                        ELSE DETALLE."Cantidad" END) --ARBITRAJE DISTRIBUCION
				 ELSE 0
			END 		
		WHEN MASTER."IdTipoOperacion" = 'VENMON'   THEN 
			CASE WHEN DETALLE."NemotecnicoInstrumento" IN ('DOLAR','EURO') THEN DETALLE."MontoLiquidacion"/(CASE WHEN DETALLE."ValorTipoCambio" = 0 THEN TIPOCAMBIO."Valor" 
                                                                                         						 ELSE DETALLE."ValorTipoCambio" 
                                                                                         					END)   -- MONETARIO
			ELSE 0
			END 
		ELSE 0 
	END 																														AS "MontoVentas"								
	,DETALLE."Precio"                                                                                                           AS "TipoCambio" -- por entregar CGO
	,MASTER."IdCliente" 																										AS "IdCliente"
	,MASTER."IdPortafolio" 																										AS "IdPortafolio"
	,MASTER."FechaComprobante" 																									AS "FechaComprobante"
	,MASTER."IdComprobante" 																									AS "IdComprobante"	
	,'N'																														AS "IndNacionalidad" -- 'N'
	,DOLAR."Valor"		                                                                                        				AS "Paridad"																								
	,DETALLE."NroLinea"																											AS "LineaFactura"
	FROM 		"BSCL.Facturacion.BD::FAC.Comprobante"			 			AS MASTER
	INNER JOIN  "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalle"  	AS DETALLE 		ON DETALLE."IdComprobante" 			= MASTER."IdComprobante"
	LEFT JOIN  	"BSCL.Facturacion.BD::FAC.MovimientoComprobante" 			AS MOVCOM 		ON MOVCOM."IdComprobante" 			= MASTER."IdComprobante"	
	LEFT JOIN   "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago"	AS CPAGO 		ON CPAGO."IdMovimientoComprobante" 	= MOVCOM."IdMovimientoComprobante"
	LEFT JOIN 	"BSCL.Plataforma.BD::MER.ParametroFinanciero"				AS DOLAR		ON DOLAR."Fecha"					= MASTER."FechaComprobante"         AND DOLAR."IdParametroFinanciero" = 'DO'
	LEFT JOIN 	"BSCL.Plataforma.BD::MER.ParametroFinanciero"				AS TIPOCAMBIO	ON TIPOCAMBIO."Fecha"				= MASTER."FechaComprobante" 		AND TIPOCAMBIO."IdParametroFinanciero" = CASE 
																																																				WHEN MASTER."NemotecnicoInstrumento" IN ('INTERB','ARBINT','DISTRI','ARBDIS') AND  MASTER."IdMonedaSpot"='USD' THEN 'DO'
																																																				WHEN MASTER."NemotecnicoInstrumento" IN ('INTERB','ARBINT','DISTRI','ARBDIS') AND  MASTER."IdMonedaSpot"='EUR' THEN 'EUR'
																																																				WHEN DETALLE."NemotecnicoInstrumento" ='DOLAR' THEN 'DO' 
																																																				WHEN DETALLE."NemotecnicoInstrumento" ='EURO' THEN 'EUR' 
																																																			 ELSE 'DO' END       
	INNER JOIN 	"BSCL.SocioNegocio.BD::CLI.Cliente" 						AS CLI   		ON CLI."IdCliente" 	  				= MASTER."IdCliente"
	INNER JOIN 	"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" 		AS CLIENTE   	ON CLIENTE."IdPersona" 	  			= CLI."IdPersona"
	LEFT  JOIN  "BSCL.Plataforma.BD::INS.Instrumento"						AS INSTRUMENTO 	ON INSTRUMENTO."IdInstrumento" 		= DETALLE."IdInstrumento"
	LEFT  JOIN  "BSCL.Plataforma.BD::REG.Pais"								AS PAIS 		ON PAIS."IdPais" 					= CLIENTE."IdPaisResidencia"
	INNER JOIN  "BSCL.Normativa.BD::NOR.Configuracion"						AS PARAMETRO	ON MASTER."IdEmpresa" 				= PARAMETRO."IdEmpresa" 				
	--Homologacion 
	LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion"  b ON (CONCAT(MASTER."IdTipoComprobante",DETALLE."AccionTipoOperacion") = b."ValorOrigen" AND b."Esquema"='DJ1870' AND b."Tipo" = 'TIPOTRANSACCION' AND MASTER."FechaComprobante" BETWEEN b."FechaDatosDesde" AND b."FechaDatosHasta")
	LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion"  c ON (CONCAT(MASTER."IdTipoComprobante",DETALLE."AccionTipoOperacion") = c."ValorOrigen" AND c."Esquema"='DJ1870' AND c."Tipo" = 'TIPODOCUMENTO' AND MASTER."FechaComprobante" BETWEEN c."FechaDatosDesde" AND c."FechaDatosHasta")
	LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion"  d ON (PAIS."CodigoPais" = d."ValorOrigen" AND d."Esquema"='DJ1870' AND d."Tipo" = 'PAISOTORGANTE' AND MASTER."FechaComprobante" BETWEEN d."FechaDatosDesde" AND d."FechaDatosHasta")
	LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion"  e 
	ON (CASE COALESCE(CPAGO."IdMedioDePago",'1') WHEN 'EFE' THEN '1' WHEN 'CHE' THEN '2' ELSE '3' END  = e."ValorOrigen"
	 	AND e."Esquema"='DJ1870' AND e."Tipo" = 'VALORESTRANSADOS' AND MASTER."FechaComprobante" BETWEEN e."FechaDatosDesde" AND e."FechaDatosHasta")
	LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as PORT ON PORT."IdPortafolio" = MASTER."IdPortafolio"
	WHERE 
	MASTER."IdEstadoSII" IN  ('ACP','REC') -- deja afuera los de estado 'ENV'
	AND  PORT."Proposito"  NOT IN ('REGAPV')
    AND 	((MASTER."IdTipoProducto" = 'MEME' AND (Case 
	          When MASTER."IdTipoInstrumento" in ('PRT', 'EMP', 'SIM','RRV','RRF') then MASTER."NemotecnicoSubyacente" 
	        Else MASTER."NemotecnicoInstrumento"
			end) IN ('INTERB','ARBINT','DISTRI','ARBDIS')) OR	-- SPOT	
			 (MASTER."IdTipoProducto" = 'INMO' AND (Case 
	          When MASTER."IdTipoInstrumento" in ('PRT', 'EMP', 'SIM','RRV','RRF') then MASTER."NemotecnicoSubyacente" 
	        Else MASTER."NemotecnicoInstrumento"
			end) IN ('DOLAR','EURO')))				-- MONETARIO	
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
	-- Notas de crédito
	AND (
			CASE
				WHEN MASTER."IdTipoOperacion" IN ('ANCSPT','ANVSPT') THEN MASTER."FolioComprobanteReferencia" 
				ELSE MASTER."FolioComprobante"
			END IN (SELECT DJ1870."DocumentoNumero" FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1870.PreOrigen" AS DJ1870)
		)
	-- MONTO > 10.000 USD para operaciones fuera de notas de crédito			
	AND (
			CASE
				WHEN MASTER."IdTipoOperacion" IN ('COMSPT','COMMON','VENSPT','VENMON') THEN DETALLE."IdMoneda"
				ELSE ''
			END NOT IN (CASE WHEN DETALLE."NemotecnicoInstrumento" = 'ARBINT' THEN 'USD' ELSE 'XXX' END )
		)		
	AND  (
			CASE
				WHEN MASTER."IdTipoOperacion" IN ('COMSPT','COMMON','VENSPT','VENMON') THEN PARAMETRO."CodigoParametro"
				ELSE 'MontoMinimoTributarioDJ1870'
			END = 'MontoMinimoTributarioDJ1870'
		)
	AND (
			CASE
				WHEN MASTER."IdTipoOperacion" IN ('COMSPT','COMMON','VENSPT','VENMON')  THEN INSTRUMENTO."IdEstado"
				ELSE 'ACT'
			END = 'ACT'
		)
	AND (CASE 
			WHEN MASTER."IdTipoOperacion"  = 'COMSPT'  THEN 
            -- SPOT, SEMI-ARBITRAJE, ARBITRAJE
				CASE WHEN DETALLE."NemotecnicoInstrumento" IN ('INTERB','DISTRI','ARBINT','ARBDIS') THEN (CASE WHEN MASTER."IdMonedaSpot" IN ('USD','EUR') THEN DETALLE."Cantidad"
                                                                                ELSE (DETALLE."MontoLiquidacion"/DOLAR."Valor") END)    -- ARBITRAJE
					 ELSE 0
				END 
			WHEN MASTER."IdTipoOperacion"  = 'COMMON'  THEN 
				CASE WHEN DETALLE."NemotecnicoInstrumento" IN ('DOLAR','EURO') THEN (DETALLE."MontoLiquidacion"/DOLAR."Valor")	-- MONETARIO
				ELSE 0
				END
			WHEN MASTER."IdTipoOperacion" IN ('ANVSPT','ANCSPT') THEN 20000
			ELSE 0 
		  END >= (CASE WHEN PARAMETRO."Valor" <> '10000' THEN 10000 ELSE PARAMETRO."Valor" END)		  
		OR		
		CASE 
			WHEN MASTER."IdTipoOperacion" = 'VENSPT'   THEN 
            -- SPOT, SEMI-ARBITRAJE, ARBITRAJE
				CASE WHEN DETALLE."NemotecnicoInstrumento" IN ('INTERB','DISTRI','ARBINT','ARBDIS') THEN (CASE WHEN MASTER."IdMonedaSpot" IN ('USD','EUR') THEN DETALLE."Cantidad"
                                                                                ELSE (DETALLE."MontoLiquidacion"/DOLAR."Valor") END)
					 ELSE 0
				END
			WHEN MASTER."IdTipoOperacion" = 'VENMON'   THEN 
				CASE WHEN DETALLE."NemotecnicoInstrumento" IN ('DOLAR','EURO') THEN (DETALLE."MontoLiquidacion"/DOLAR."Valor")    -- MONETARIO
				ELSE 0
				END
			WHEN MASTER."IdTipoOperacion" IN ('ANVSPT','ANCSPT') THEN 20000	
		ELSE 0  
		END >= (CASE WHEN PARAMETRO."Valor" <> '10000' THEN 10000 ELSE PARAMETRO."Valor" END)
		)