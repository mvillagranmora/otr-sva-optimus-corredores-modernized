SELECT  TO_VARCHAR(SIM."INGFechaOperacion" , 'YYYYMMDD')        	AS "FechaCompra"
        ,RPAD(COALESCE(REPLACE(SIM."suby_Nemotecnico",'','A'),''),14,' ')  AS "Nemotecnico"
        ,SIM."suby_IdClasificacionTipoInstrumento" 					AS "IdTipoInstrumento"
        ,'1'                             							AS "TipoCartera"      
    ,COALESCE(SIM."INGCantidad", 0)                 				AS "NumeroInstrMant"
        ,CASE WHEN SIM."IdMonedaInstrumento" = 'EUR' THEN '142'
            WHEN SIM."IdMonedaInstrumento" IN ('USD', 'DO') THEN '994'  -- Dolar Observado
            WHEN SIM."IdMonedaInstrumento" = 'IV'  THEN '997'
            WHEN SIM."IdMonedaInstrumento" = 'UF'  THEN '998'
            WHEN COALESCE(SIM."IdMonedaInstrumento",'CLP') IN ('CLP','NR')  THEN '999'
            ELSE '880'
         END                             							AS "Moneda"     
    ,COALESCE(SIM."PrecioAPlazo" ,0)                				AS "PrecioCompra"
    ,COALESCE(SIM."INGMontoATasaCompraActual",0)         			AS "ValorRazonable"
    ,TO_VARCHAR(SIM."FechaLiquidacion", 'YYYYMMDD')       			AS "FechaInicioCondicion"
    ,TO_VARCHAR(SIM."FechaVencimientoOperacion", 'YYYYMMDD')    	AS "FechaFinCondicion"
FROM   "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea" AS SIM 
WHERE SIM."IdEmpresa"                           = :IdEmpresa
AND SIM."IdEstado"                             	= 'VIG'
AND SIM."suby_IdClasificacionTipoInstrumento"  	= 'RV'
AND SIM."IdTipoInstrumentoOperacion"           	= 'SIM'
AND SIM."IdCliente"                            	= :IdCliente