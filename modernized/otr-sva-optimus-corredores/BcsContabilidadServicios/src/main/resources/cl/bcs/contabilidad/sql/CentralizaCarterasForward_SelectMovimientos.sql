SELECT DISTINCT
TO_BIGINT(:IdTipoCentralizacion)					AS "IdTipoCentralizacion",
ConsultarCartera."IdCartera"						AS "IdMovimiento",
ConsultarCartera."IdTipoOperacion"					AS "IdTipoMovimiento",
ConsultarCartera."IdEmpresa"         				AS "IdEmpresa",									    
ConsultarCartera."FolioCartera"						AS "Numero",
ConsultarCartera."FolioCartera" 					AS "Linea",
YEAR(:FechaContable) ||'-'|| RIGHT('0' || MONTH(:FechaContable),2) ||'-'|| RIGHT('0' || DAYOFMONTH(:FechaContable),2) AS "Fecha",
ConsultarCartera."DocumentoIdentidad"				AS "DocumentoIdentidad",  
ConsultarCartera."IdPortafolio"						AS "IdPortafolio",
TO_BIGINT(YEAR(ConsultarCartera."INGFechaOperacion")) AS "Ejercicio",
RIGHT('0' || MONTH(:FechaContable),2) ||'-'|| YEAR(:FechaContable) 	AS "Periodo",
ABS(ConsultarCartera."INGMontoMonedaPrincipal")		AS "Monto",	
ConsultarCartera."IdMonedaPrincipal"				AS "IdMoneda",
ConsultarCartera."IdEstado"							AS "Estado"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
WHERE "IdEmpresa"			= :IdEmpresa
  AND "IdTipoProducto"		= 'MEFW'
  AND "INGFechaOperacion"	<= :FechaContable 
  AND "IdEstado"			= 'VIG'
UNION ALL 
SELECT DISTINCT
TO_BIGINT(:IdTipoCentralizacion)					AS "IdTipoCentralizacion",
ConsultarCartera."IdCartera"						AS "IdMovimiento",
CASE WHEN "IdEstado" = 'VCD' THEN 'VCDFWD' ELSE 'ANTFWD' END	AS "IdTipoMovimiento",
ConsultarCartera."IdEmpresa"         				AS "IdEmpresa",									    
ConsultarCartera."FolioCartera"						AS "Numero",
ConsultarCartera."FolioCartera" 					AS "Linea",
YEAR(:FechaContable) ||'-'|| RIGHT('0' || MONTH(:FechaContable),2) ||'-'|| RIGHT('0' || DAYOFMONTH(:FechaContable),2) AS "Fecha",
ConsultarCartera."DocumentoIdentidad"				AS "DocumentoIdentidad",  
ConsultarCartera."IdPortafolio"						AS "IdPortafolio",
TO_BIGINT(YEAR(ConsultarCartera."INGFechaOperacion"))			AS "Ejercicio",
RIGHT('0' || MONTH(:FechaContable),2) ||'-'|| YEAR(:FechaContable) 	AS "Periodo",
ABS(ConsultarCartera."EGRMontoMonedaPrincipal")		AS "Monto",	
ConsultarCartera."IdMonedaPrincipal"				AS "IdMoneda",
ConsultarCartera."IdEstado"							AS "Estado"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
WHERE "IdEmpresa"			= :IdEmpresa
  AND "IdTipoProducto"		= 'MEFW'
  AND "EGRFechaOperacion"	= :FechaContable 
  AND "IdEstado"			IN ('VCD','ANT')