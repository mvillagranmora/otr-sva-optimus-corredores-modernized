-- PACTOS
 SELECT 
     OPER."FolioOperacion"                              AS "IdOperacion"
    ,CCliente."NombreCompleto"                          AS "NombreCliente" 
    ,CCliente."DocumentoIdentidad"                      AS "RutCliente"
    ,TO_NVARCHAR(OPER."FechaOperacion",'DD-MM-YYYY')    AS "FechaInicio"
    ,TO_NVARCHAR(OPER."FechaVencimiento",'DD-MM-YYYY')	AS "FechaVencimiento"
    ,CCliente."CodigoPortafolio"                        AS "CodigoPortafolio"
    ,CASE WHEN OPER."IdTipoOperacion" = 'COMPAC' THEN
    	CASE WHEN OPER."IdMonedaSecundaria" = 'USD' THEN    	
    		ABS(OPER."MontoOperacionFinalMoneda") *-1
    	ELSE 
    		ABS(OPER."MontoOperacionFinal") *-1
    	END
    ELSE
    	CASE WHEN OPER."IdMonedaSecundaria" = 'USD' THEN    	
    		ABS(OPER."MontoOperacionFinalMoneda")
    	ELSE 
    		ABS(OPER."MontoOperacionFinal")
    	END
    END 												AS "MontoPagarRecibir"
    ,OPER."IdMonedaPrincipal"                           AS "IdMonedaOrigen"
    ,OPER."IdMonedaSecundaria"                          AS "IdMonedaPago" 
    ,'PAC'						                        AS "IdTipoInstrumento"
    ,IFNULL(CIndicadores."ValorIndicador",'N')			AS "EsAdministradorCartera"
    ,CASE WHEN OPER."IdMonedaPrincipal" != 'CLP' THEN
    	CASE WHEN IFNULL(CIndicadores."ValorIndicador",'N') = 'S' THEN
    		'PAC_MONEDA_EXTRANJERA_CON_ADMIN_CARTERA'
    	ELSE
    		'PAC_MONEDA_EXTRANJERA_SIN_ADMIN_CARTERA'
    	END
     ELSE
    	CASE WHEN IFNULL(CIndicadores."ValorIndicador",'N') = 'S' THEN
    		'PAC_MONEDA_NACIONAL_CON_ADMIN_CARTERA'
    	ELSE
    		'PAC_MONEDA_NACIONAL_SIN_ADMIN_CARTERA'
    	END     
     END												AS "AgruparSeccion"	-- Campos para agrupar por seccion en hoja de Excel
    
 FROM "BSCL.Mesa.BD::MES.Operacion" AS OPER
 LEFT JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS CCliente ON OPER."IdCliente" = CCliente."IdCliente" AND OPER."IdPortafolio" = CCliente."IdPortafolio" 
 LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS CIndicadores ON (OPER."IdCliente" = CIndicadores."IdCliente" AND OPER."IdPortafolio" = CIndicadores."IdPortafolio" AND CIndicadores."CodigoTipoIndicador" = 'ADMCART')
 LEFT JOIN "BSCL.Mesa.BD::MES.ConfiguracionMesa" AS PARAM ON CCliente."IdEmpresa" = PARAM."IdEmpresa" AND PARAM."CodigoParametro" = 'CONFIRMA_OPERACIONES_MESARF'						
WHERE CCliente."IdEmpresa"	= :p_IdEmpresa 
AND	OPER."FechaVencimiento" = :p_FechaVencimiento
AND OPER."IdTipoOperacion" 	IN ('COMPAC', 'VENPAC')
AND OPER."IdEstado" NOT IN ('ANU', 'PEN', 'ANT')

UNION ALL

-- SIMULTANEAS
SELECT  
     SIM."FolioComprobante"                                     AS "IdOperacion"
    ,SIM."NombreLargoCliente"                                   AS "NombreCliente"
    ,SIM."DocumentoIdentidad"                                  	AS "RutCliente"
    ,TO_NVARCHAR(SIM."INGFechaOperacion",'DD-MM-YYYY')			AS "FechaInicio"
    ,TO_NVARCHAR(SIM."FechaVencimientoOperacion",'DD-MM-YYYY')	AS "FechaVencimiento"
    ,SIM."CodigoPortafolio"                                    	AS "CodigoPortafolio"
    ,CASE
		WHEN "Cantidad" = "INGCantidad" AND COALESCE("MontoAPlazoComprobante", 0) <> 0 THEN ROUND(ABS("MontoAPlazoComprobante"), 0)
		WHEN "Cantidad" <> "INGCantidad" THEN ROUND(ABS("INGCantidad" * COALESCE("PrecioAPlazo",0)),0)
		ELSE ROUND(ABS("MontoAPlazo"), 0) END 					AS "MontoPagarRecibir"
    ,SIM."IdMonedaSistema"                                      AS "IdMonedaOrigen"
    ,SIM."IdMonedaInstrumento"                                	AS "IdMonedaPago"
    ,SIM."IdTipoInstrumentoOperacion"                           AS "IdTipoInstrumento"
    ,'N'														AS "EsAdministradorCartera"
	,'SIM'	 													AS "AgruparSeccion"	-- Campos para agrupar por seccion en hoja de Excel    
    
 FROM "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea"	AS SIM
WHERE SIM."IdEmpresa"					= :p_IdEmpresa
  AND SIM."FechaVencimientoOperacion"	= :p_FechaVencimiento
  AND SIM."IdEstado"					NOT IN ('ANT','ANU')
ORDER BY "AgruparSeccion"
  