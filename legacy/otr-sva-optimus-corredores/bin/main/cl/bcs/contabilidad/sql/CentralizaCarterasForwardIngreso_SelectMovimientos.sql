SELECT
TO_BIGINT(:IdTipoCentralizacion)					AS "IdTipoCentralizacion",
ConsultarCartera."IdCartera"						AS "IdMovimiento",
CONCAT('I',ConsultarCartera."IdTipoOperacion")		AS "IdTipoMovimiento",
ConsultarCartera."IdEmpresa"         				AS "IdEmpresa",									    
ConsultarCartera."FolioCartera"						AS "Numero",
ConsultarCartera."FolioCartera" 					AS "Linea",
YEAR(ConsultarCartera."INGFechaOperacion" ) ||'-'|| RIGHT('0' || MONTH(ConsultarCartera."INGFechaOperacion"),2) ||'-'|| RIGHT('0' || DAYOFMONTH(ConsultarCartera."INGFechaOperacion"),2) AS "Fecha",
ConsultarCartera."DocumentoIdentidad"				AS "DocumentoIdentidad",  
ConsultarCartera."IdPortafolio"						AS "IdPortafolio",
TO_BIGINT(YEAR(ConsultarCartera."INGFechaOperacion")) AS "Ejercicio",
RIGHT('0' || MONTH(ConsultarCartera."INGFechaOperacion"),2) ||'-'|| YEAR(ConsultarCartera."INGFechaOperacion") 	AS "Periodo",
ABS(ConsultarCartera."INGMontoMonedaPrincipal")		AS "Monto",	
ConsultarCartera."IdMonedaPrincipal"				AS "IdMoneda",
ConsultarCartera."IdEstado"							AS "Estado"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
INNER JOIN "BSCL.Mesa.BD::MES.Operacion" AS op
    ON op."IdOperacion" = ConsultarCartera."IdOperacion" AND op."IdEstado" != 'ANU'
WHERE ConsultarCartera."IdEmpresa" = :IdEmpresa
AND ConsultarCartera."IdTipoProducto" = 'MEFW'
AND ConsultarCartera."INGFechaOperacion"	= :FechaContable 
AND ConsultarCartera."IdEstado" IN  ('VIG','LIQ');