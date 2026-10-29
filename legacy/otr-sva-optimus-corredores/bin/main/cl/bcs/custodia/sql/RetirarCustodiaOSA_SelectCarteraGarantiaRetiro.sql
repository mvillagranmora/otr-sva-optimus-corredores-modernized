SELECT 
	CAR."IdCartera"								AS "IdGenerarMovimientoCustodia"
	,''                                         AS "IdOrigenGarantia"
	,'CARTERA'									AS "TablaAfectada"
	,'N'										AS "EsNominalIngreso"
	,CAR."IdCliente"                            AS "IdCliente"
	,CAR."IdPortafolio"                         AS "IdPortafolio"
	,CAR."IdMonedaSistema"                      AS "IdMonedaSistema"
	,'SUECOS'                                   AS "IdConcepto"       -- Retiro por Vencimiento de OSA	
	,'RETOSA'                                   AS "IdTipoOperacion"  -- Retiro de Osa
	,CAR."IdBolsa"                              AS "IdBolsa"
	,'T0'            							AS "IdCondicionLiquidacion"
	,CAR."INGGlosa"                             AS "Glosa"
	,CAR."IdClasificacionTipoInstrumento"       AS "IdClasificacionTipoInstrumento"
	,CAR."IdTipoInstrumento"                    AS "IdTipoInstrumento"
	,CAR."IdInstrumento"						AS "IdInstrumento"
	,CAR."Nemotecnico"                          AS "Nemotecnico"
	,CAR."IdEmisor"                             AS "IdEmisor"			
	,CAR."IdBoveda"                             AS "IdBoveda"			
	,CAR."INGCantidad"           				AS "Cantidad"
	,CAR."IdMonedaInstrumento"                  AS "IdMonedaInstrumento"
FROM "BSCL.Custodia.BD::CUS.Cartera"			AS CAR
WHERE 	CAR."IdInstrumento" = :IdInstrumentoRelacionado	AND
		CAR."IdEstado"		= 'VIG'							AND
		CAR."EGRCantidad"	= 0
UNION ALL
SELECT  
	GAR."IdGarantia"							AS "IdGenerarMovimientoCustodia"
	,GAR."IdOrigenGarantia"                      AS "IdOrigenGarantia"
	,'GARANTIA'									AS "TablaAfectada"
	,'N'										AS "EsNominalIngreso"
	,GAR."IdCliente"                            AS "IdCliente"
	,GAR."IdPortafolio"                         AS "IdPortafolio"
	,GAR."IdMonedaSistema"                      AS "IdMonedaSistema"
	,GAR."IdConcepto"                           AS "IdConcepto"         -- Retiro por Vencimiento de OSA
	,'RETOSA'                                   AS "IdTipoOperacion"    -- Retiro de Osa
	,car."IdBolsa"								AS "IdBolsa"
	,'T0'							            AS "IdCondicionLiquidacion"			
	,''											AS "Glosa"			
	,GAR."IdClasificacionTipoInstrumento"       AS "IdClasificacionTipoInstrumento"
	,GAR."IdTipoInstrumento"                    AS "IdTipoInstrumento"
	,GAR."IdInstrumento" 	                    AS "IdInstrumento"
	,GAR."Nemotecnico"                          AS "Nemotecnico"
	,GAR."IdEmisor"                             AS "IdEmisor"
	,car."IdBoveda"								AS "IdBoveda"
	,GAR."INGCantidad"                    		AS "Cantidad"
	,car."IdMonedaInstrumento"                  AS "IdMonedaInstrumento"
FROM "BSCL.Custodia.BD::GTA.Garantia"			AS GAR
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" as car on car."IdCartera" = GAR."IdCarteraOrigen"
WHERE
	GAR."IdInstrumento" = :IdInstrumentoRelacionado			AND
	GAR."IdEstado"		= 'VIG'									AND
	GAR."EGRCantidad"	= 0;