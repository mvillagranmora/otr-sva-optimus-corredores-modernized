--partidas
SELECT 
TO_BIGINT(:IdTipoCentralizacion)					AS "IdTipoCentralizacion",
ConsultarComprobanteDetalle."IdComprobanteDetalle"	AS "IdMovimiento",
CASE ConsultarComprobanteDetalle."IdTipoMovimiento" WHEN 'MOVING' THEN 
													(
													CASE Comprobante1."IdEstado" WHEN  'GIR' 
													THEN 		CASE WHEN Comprobante1."ComprobanteNeteado" = 'NO' THEN 'SOLINGRE' ELSE 'SOLTRAINGR' END
														ELSE CASE WHEN Comprobante1."ComprobanteNeteado" = 'NO' THEN 'SOLINGANU' ELSE 'SOLTRAINGA' END 
													END) 
													ELSE 
													(CASE Comprobante1."IdEstado" WHEN  'GIR' THEN 
														CASE WHEN Comprobante1."ComprobanteNeteado" = 'NO' THEN 'SOLEGRE' ELSE 'SOLTRAEGRE' END
														ELSE CASE WHEN Comprobante1."ComprobanteNeteado" = 'NO' THEN 'SOLENGANU' ELSE 'SOLTRAEGRA' END 
													END)
													END	AS "IdTipoMovimiento",
Comprobante1."IdEmpresa"         					AS "IdEmpresa",									    
Comprobante1."IdComprobante"							AS "Numero",
ConsultarComprobanteDetalle."IdComprobanteDetalle"  AS "Linea",
YEAR(Comprobante1."FechaLiquidacion") ||'-'|| RIGHT('0' || MONTH(Comprobante1."FechaLiquidacion"),2) ||'-'|| RIGHT('0' || DAYOFMONTH(Comprobante1."FechaLiquidacion"),2) AS "Fecha",
Comprobante1."DocumentoIdentidad"					AS "DocumentoIdentidad",
Comprobante1."IdPortafolioTitularPago"				AS "IdPortafolio",
TO_BIGINT(YEAR(Comprobante1."FechaLiquidacion"))		AS "Ejercicio",
RIGHT('0' || MONTH(Comprobante1."FechaLiquidacion"),2) ||'-'|| YEAR(Comprobante1."FechaLiquidacion") 	AS "Periodo",
Comprobante1."IdMoneda"								AS "IdMoneda",
ConsultarComprobanteDetalle."Monto"					AS "Monto",                                     
Comprobante1."IdEstado"								AS "Estado"


FROM (
  SELECT comp.* FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobante" comp WHERE comp."IdComprobante" IN 
  
		  (SELECT x."IdComprobanteAsociado" FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteOrigen" x
			WHERE x."IdComprobante" IN ( 
										SELECT z."IdComprobante" FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobante" z
										  WHERE z."IdConcepto" IN ('PAREGR' ,'PARING')  and z."IdEstado" IN ('GIR','LIQ')
			  							) 
		  )
     )   AS	Comprobante1
INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle"  ConsultarComprobanteDetalle
	ON  (ConsultarComprobanteDetalle."IdComprobante" = Comprobante1."IdComprobante" 
	AND ConsultarComprobanteDetalle."IdEstado" IN ('LIQ','ING'))
WHERE 
Comprobante1."IdEmpresa" = :IdEmpresa AND 
(:LocSistema = 'CL' OR (Comprobante1."IdEstado" IN ('LIQ','ANU') AND :LocSistema = 'PE')) AND

Comprobante1."FechaLiquidacion" = :FechaContable 

  GROUP BY ConsultarComprobanteDetalle."IdComprobanteDetalle",
  ConsultarComprobanteDetalle."IdTipoMovimiento",
  Comprobante1."IdEmpresa" ,
  Comprobante1."IdComprobante"	,
  ConsultarComprobanteDetalle."IdComprobanteDetalle",
  Comprobante1."IdPortafolioTitularPago",
  Comprobante1."FechaLiquidacion",
  Comprobante1."DocumentoIdentidad"	,
  Comprobante1."IdMoneda",
  ConsultarComprobanteDetalle."Monto"	,
  Comprobante1."IdEstado",
  Comprobante1."ComprobanteNeteado"



UNION ALL 
--documentos montos egreso ingreso partidas
SELECT 
TO_BIGINT(:IdTipoCentralizacion)	AS "IdTipoCentralizacion",
ComprobanteFormaDePago."IdComprobanteFormaDePago"*-1	AS "IdMovimiento",
CASE ConsultarComprobanteDetalle."IdTipoMovimiento" WHEN 'MOVING' THEN 
	   (CASE Comprobante2."IdEstado" WHEN 'GIR' THEN 
		CASE WHEN Comprobante2."ComprobanteNeteado" = 'NO' THEN 'DOCINGRE' ELSE 'DOCTRAINGR' END
		ELSE CASE WHEN Comprobante2."ComprobanteNeteado" = 'NO' THEN 'DOCINGANU' 
		ELSE 'DOCTRAINGA' END END) 
		ELSE (CASE Comprobante2."IdEstado" WHEN 'GIR' THEN 
			  CASE WHEN Comprobante2."ComprobanteNeteado" = 'NO' THEN 'DOCEGRE' ELSE 'DOCTRAEGRE' END
			   ELSE CASE WHEN Comprobante2."ComprobanteNeteado" = 'NO' THEN 'DOCENGANU' 
			   ELSE 'DOCTRAEGRA' END END) END AS "IdTipoMovimiento",
Comprobante2."IdEmpresa"         		AS "IdEmpresa",									    
Comprobante2."IdComprobante"				AS "Numero",
ConsultarComprobanteDetalle."IdComprobanteDetalle"	AS "Linea",
YEAR(Comprobante2."FechaLiquidacion") ||'-'|| RIGHT('0' || MONTH(Comprobante2."FechaLiquidacion"),2) ||'-'|| RIGHT('0' || DAYOFMONTH(Comprobante2."FechaLiquidacion"),2) AS "Fecha",
Comprobante2."DocumentoIdentidad"		AS "DocumentoIdentidad",  
Comprobante2."IdPortafolioTitularPago"	AS "IdPortafolio",
TO_BIGINT(YEAR(Comprobante2."FechaLiquidacion"))	AS "Ejercicio",
RIGHT('0' || MONTH(Comprobante2."FechaLiquidacion"),2) ||'-'|| YEAR(Comprobante2."FechaLiquidacion") 	AS "Periodo",
Comprobante2."IdMoneda"					AS "IdMoneda",
ConsultarComprobanteDetalle."Monto"							AS "Monto", 
Comprobante2."IdEstado"					AS "Estado"	

--SELECT Comprobante2.*
FROM (
  SELECT comp.* FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobante" comp WHERE comp."IdComprobante" IN 
  
	  (
	  SELECT  x."IdComprobanteAsociado" FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteOrigen" x
		WHERE x."IdComprobante" IN (  SELECT z."IdComprobante" FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobante" z
	  		WHERE z."IdConcepto" IN ('PAREGR' ,'PARING')  and z."IdEstado" IN ('LIQ', 'GIR') ) 
	  )
  	 )   AS	Comprobante2
  
  
INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS ComprobanteFormaDePago  ON ( ComprobanteFormaDePago."IdComprobante" = Comprobante2."IdComprobante" 	
AND ((ComprobanteFormaDePago."IdEstado" IN ('LIQ','GIR') AND :LocSistema = 'CL') OR (ComprobanteFormaDePago."IdEstado" IN ('LIQ','ANU') AND :LocSistema = 'PE'))	  		
)	
INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle"  ConsultarComprobanteDetalle
	ON  ConsultarComprobanteDetalle."IdComprobante" = Comprobante2."IdComprobante" 
	AND ConsultarComprobanteDetalle."IdEstado" IN ('LIQ','ING')
WHERE 
Comprobante2."IdEmpresa" = :IdEmpresa 
AND 
((:LocSistema = 'CL' AND Comprobante2."IdEstado" IN ('LIQ','GIR')) 
or  (Comprobante2."IdEstado" IN ('LIQ','ANU') AND :LocSistema = 'PE')) 
AND

  Comprobante2."FechaLiquidacion" = :FechaContable  
  GROUP BY ConsultarComprobanteDetalle."IdComprobanteDetalle",
  ConsultarComprobanteDetalle."IdTipoMovimiento",
  ComprobanteFormaDePago."IdComprobanteFormaDePago",
  Comprobante2."IdEmpresa" ,
  Comprobante2."IdComprobante"	,
  ConsultarComprobanteDetalle."IdComprobanteDetalle",
  Comprobante2."IdPortafolioTitularPago",
  Comprobante2."FechaLiquidacion",
  Comprobante2."DocumentoIdentidad"	,
  Comprobante2."IdMoneda",
  ConsultarComprobanteDetalle."Monto"	,
  Comprobante2."IdEstado",
  Comprobante2."ComprobanteNeteado"