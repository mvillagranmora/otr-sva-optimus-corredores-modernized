SELECT
TO_BIGINT(:IdTipoCentralizacion)					AS "IdTipoCentralizacion",
ConsultarCartera."IdCartera"						AS "IdMovimiento",
CONCAT('V',ConsultarCartera."IdTipoOperacion")		AS "IdTipoMovimiento",
ConsultarCartera."IdEmpresa"         				AS "IdEmpresa",									    
ConsultarCartera."FolioCartera"						AS "Numero",
ConsultarCartera."FolioCartera" 					AS "Linea",
YEAR(:FechaContable) ||'-'|| RIGHT('0' || MONTH(:FechaContable),2) ||'-'|| RIGHT('0' || DAYOFMONTH(:FechaContable),2) AS "Fecha",
Cli."DocumentoIdentidad"				AS "DocumentoIdentidad",  
ConsultarCartera."IdPortafolio"						AS "IdPortafolio",
TO_BIGINT(YEAR(ConsultarCartera."INGFechaOperacion")) AS "Ejercicio",
RIGHT('0' || MONTH(:FechaContable),2) ||'-'|| YEAR(:FechaContable) 	AS "Periodo",
ABS(ConsultarCartera."INGMontoMonedaPrincipal")		AS "Monto",	
ConsultarCartera."IdMonedaPrincipal"				AS "IdMoneda",
ConsultarCartera."IdEstado"							AS "Estado"
FROM "BSCL.Custodia.BD::HIS.Cartera" ConsultarCartera
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as Cli ON Cli."IdCliente" = ConsultarCartera."IdCliente" 
WHERE ConsultarCartera."IdEmpresa"			= :IdEmpresa
  AND ConsultarCartera."FechaRespaldo" = :FechaContable
  AND "IdTipoProducto"		= 'MEFW'
  AND "INGFechaOperacion"	<= :FechaContable 
  AND "FechaVencimientoOperacion" > :FechaContable
  AND ConsultarCartera."IdEstado"			= 'VIG';