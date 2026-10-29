SELECT 
TO_BIGINT(:IdTipoCentralizacion)					AS "IdTipoCentralizacion",
MIN(ComprobanteFormaDePago."IdComprobanteFormaDePago")	AS "IdMovimiento",
CASE Comprobante."IdTipoMovimiento" WHEN 'MOVING' THEN 
													(CASE Comprobante."IdEstado" WHEN 'LIQ' THEN 
														CASE WHEN Comprobante."ComprobanteNeteado" = 'N' THEN 'SOLINGRE' ELSE 'SOLTRAINGR' END
														ELSE CASE WHEN Comprobante."ComprobanteNeteado" = 'N' THEN 'SOLINGANU' ELSE 'SOLTRAINGA' END 
													END) 
													ELSE 
													(CASE Comprobante."IdEstado" WHEN 'LIQ' THEN 
														CASE WHEN Comprobante."ComprobanteNeteado" = 'N' THEN 'SOLEGRE' ELSE 'SOLTRAEGRE' END
														ELSE CASE WHEN Comprobante."ComprobanteNeteado" = 'N' THEN 'SOLENGANU' ELSE 'SOLTRAEGRA' END 
													END)
													END	AS "IdTipoMovimiento",
Comprobante."IdEmpresa"         					AS "IdEmpresa",									    
Comprobante."IdComprobante"							AS "Numero",
MIN(ComprobanteFormaDePago."IdComprobanteFormaDePago") AS "Linea",
YEAR(Comprobante."FechaLiquidacion") ||'-'|| RIGHT('0' || MONTH(Comprobante."FechaLiquidacion"),2) ||'-'|| RIGHT('0' || DAYOFMONTH(Comprobante."FechaLiquidacion"),2) AS "Fecha",
Comprobante."DocumentoIdentidad"					AS "DocumentoIdentidad",
Comprobante."IdPortafolioTitularPago"				AS "IdPortafolio",
TO_BIGINT(YEAR(Comprobante."FechaLiquidacion"))		AS "Ejercicio",
RIGHT('0' || MONTH(Comprobante."FechaLiquidacion"),2) ||'-'|| YEAR(Comprobante."FechaLiquidacion") 	AS "Periodo",
Comprobante."IdMoneda"								AS "IdMoneda",
SUM(ComprobanteFormaDePago."Monto")					AS "Monto",                           
Comprobante."IdEstado"								AS "Estado"
FROM "BSCL.Tesoreria.BD::TES.Comprobante"   		AS	Comprobante
LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS ComprobanteFormaDePago  ON ( ComprobanteFormaDePago."IdComprobante" = Comprobante."IdComprobante" 	
  		AND ((ComprobanteFormaDePago."IdEstado" IN ('LIQ','CRT') AND :LocSistema = 'CL') OR (ComprobanteFormaDePago."IdEstado" IN ('LIQ','ANU') AND :LocSistema = 'PE'))	
  		and Comprobante."IdConcepto"  NOT IN ('PAREGR','PARING'))
WHERE Comprobante."IdEmpresa" =  :IdEmpresa AND
((Comprobante."IdEstado" = 'LIQ' AND  :LocSistema = 'CL') OR (Comprobante."IdEstado" IN ('LIQ','ANU') AND  :LocSistema = 'PE')) AND
  Comprobante."FechaLiquidacion" =  :FechaContable
   AND Comprobante."IdConcepto"  NOT IN ('PAREGR','PARING')
 GROUP BY 
  ComprobanteFormaDePago."NombreCuentaBancariaCorredora",
  Comprobante."IdTipoMovimiento",
  Comprobante."IdEmpresa" ,
  Comprobante."IdComprobante"	,
  Comprobante."IdPortafolioTitularPago",
  Comprobante."FechaLiquidacion",
  Comprobante."DocumentoIdentidad"	,
  Comprobante."IdMoneda",
  Comprobante."IdEstado",
  Comprobante."ComprobanteNeteado"
UNION ALL
SELECT 
TO_BIGINT(:IdTipoCentralizacion)	AS "IdTipoCentralizacion",
SUB."IdComprobanteFormaDePago"*-1	AS "IdMovimiento",
CASE SUB."IdTipoMovimiento" WHEN 'MOVING' THEN 
	   (CASE SUB."IdEstado" WHEN 'LIQ' THEN 
		CASE WHEN SUB."ComprobanteNeteado" = 'N' THEN 'DOCINGRE' ELSE 'DOCTRAINGR' END
		ELSE CASE WHEN SUB."ComprobanteNeteado" = 'N' THEN 'DOCINGANU' 
		ELSE 'DOCTRAINGA' END END) 
		ELSE (CASE SUB."IdEstado" WHEN 'LIQ' THEN 
			  CASE WHEN SUB."ComprobanteNeteado" = 'N' THEN 'DOCEGRE' ELSE 'DOCTRAEGRE' END
			   ELSE CASE WHEN SUB."ComprobanteNeteado" = 'N' THEN 'DOCENGANU' 
			   ELSE 'DOCTRAEGRA' END END) END AS "IdTipoMovimiento",
SUB."IdEmpresa"         		AS "IdEmpresa",									    
SUB."IdComprobante"				AS "Numero",
SUB."IdComprobanteFormaDePago" 	AS "Linea",
YEAR(SUB."FechaLiquidacion") ||'-'|| RIGHT('0' || MONTH(SUB."FechaLiquidacion"),2) ||'-'|| RIGHT('0' || DAYOFMONTH(SUB."FechaLiquidacion"),2) AS "Fecha",
SUB."DocumentoIdentidad"		AS "DocumentoIdentidad",  
SUB."IdPortafolioTitularPago"	AS "IdPortafolio",
TO_BIGINT(YEAR(SUB."FechaLiquidacion"))	AS "Ejercicio",
RIGHT('0' || MONTH(SUB."FechaLiquidacion"),2) ||'-'|| YEAR(SUB."FechaLiquidacion") 	AS "Periodo",
SUB."IdMoneda"					AS "IdMoneda",
SUB."Monto"						AS "Monto", 
SUB."IdEstado"					AS "Estado"	
FROM(
    SELECT 
        Comprobante."FechaMovimiento",
        Comprobante."IdTipoMovimiento" ,	
		Comprobante."ComprobanteNeteado" ,
		Comprobante."IdEmpresa" ,									    
		Comprobante."IdComprobante",
		ComprobanteFormaDePago."IdComprobanteFormaDePago",
		Comprobante."DocumentoIdentidad"	,  
		Comprobante."IdPortafolioTitularPago"	,
		ComprobanteFormaDePago."IdMoneda",
		ComprobanteFormaDePago."Monto", 
		Comprobante."IdEstado",	
		CAST((CASE WHEN ComprobanteFormaDePago."IdEstado" <> 'LIQ' THEN ADD_DAYS (TO_DATE (Comprobante."FechaLiquidacion",'YYYY-MM-DD'),ComprobanteFormaDePago."DiasRetencion")
		ELSE ComprobanteFormaDePago."FechaHoraLiquidacion"	END) AS DATE) AS "FechaLiquidacion" 	
		FROM "BSCL.Tesoreria.BD::TES.Comprobante" 	AS Comprobante
		INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS ComprobanteFormaDePago  ON ( ComprobanteFormaDePago."IdComprobante" = Comprobante."IdComprobante" 	
  		AND ((ComprobanteFormaDePago."IdEstado" IN ('LIQ','CRT') AND :LocSistema = 'CL') OR (ComprobanteFormaDePago."IdEstado" IN ('LIQ','ANU') AND :LocSistema = 'PE'))	
  		and Comprobante."IdConcepto"  NOT IN ('PAREGR','PARING')
  		)  	 	
  		GROUP BY 
	    Comprobante."FechaMovimiento",
        Comprobante."IdTipoMovimiento" ,	
		Comprobante."ComprobanteNeteado" ,
		Comprobante."IdEmpresa" ,									    
		Comprobante."IdComprobante",
		ComprobanteFormaDePago."IdComprobanteFormaDePago",
		Comprobante."DocumentoIdentidad"	,  
		Comprobante."IdPortafolioTitularPago"	,
		ComprobanteFormaDePago."IdMoneda",
		ComprobanteFormaDePago."Monto", 
		Comprobante."IdEstado",
		ComprobanteFormaDePago."IdEstado",
		Comprobante."FechaLiquidacion",
		ComprobanteFormaDePago."DiasRetencion",
		ComprobanteFormaDePago."FechaHoraLiquidacion"
  		)SUB
WHERE 
	SUB."IdEmpresa" = :IdEmpresa 			AND
((SUB."IdEstado" = 'LIQ' AND :LocSistema = 'CL') OR (SUB."IdEstado" IN ('LIQ','ANU') AND :LocSistema = 'PE')) AND
SUB."FechaLiquidacion" = :FechaContable ;