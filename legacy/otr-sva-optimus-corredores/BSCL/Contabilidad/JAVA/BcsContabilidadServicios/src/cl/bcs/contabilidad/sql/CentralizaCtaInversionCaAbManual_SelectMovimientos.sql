SELECT 
TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion"
,ConsultarCargosAbonos."IdCargoAbonoMovimiento"	* 10	AS  "IdMovimiento" --¿?
,CASE ConsultarCargosAbonos."IdTipoMovimiento" 		
								WHEN 'MOVABN' 
 								THEN 'ABNMANUAL'
 								ELSE 'CARMANUAL' END	AS "IdTipoMovimiento"
,ConsultarCargosAbonos."IdEmpresa"					AS "IdEmpresa"
,ConsultarCargosAbonos."IdCargoAbonoMovimiento"		AS "Numero"
,TO_BIGINT(0)								AS "Linea"
,YEAR(ConsultarCargosAbonos."FechaSistema") ||'-'|| 
 RIGHT('0' || MONTH(ConsultarCargosAbonos."FechaSistema"),2) ||'-'|| 
 RIGHT('0' || DAYOFMONTH(ConsultarCargosAbonos."FechaSistema"),2) 
 													AS "Fecha"
,ConsultarPersona."DocumentoIdentidad"				AS "DocumentoIdentidad"
,ConsultarPortafolio."IdPortafolio"			AS "IdPortafolio"
,TO_BIGINT(YEAR(ConsultarCargosAbonos."FechaSistema"))	AS "Ejercicio"
,RIGHT('0' || MONTH(ConsultarCargosAbonos."FechaSistema"),2) ||'-'|| YEAR(ConsultarCargosAbonos."FechaSistema") 	AS "Periodo"
,ConsultarCargosAbonos."Monto"						AS "Monto"
,ConsultarCargosAbonos."IdMoneda"					AS "IdMoneda"
,ConsultarCargosAbonos."IdEstado"					AS "Estado"
FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos" ConsultarCargosAbonos
LEFT JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" ConsultarCuentaInversion
ON ConsultarCuentaInversion."IdCuentaInversion" = ConsultarCargosAbonos."IdCuentaInversion" 
LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" ConsultarPortafolio 
ON ConsultarPortafolio."IdPortafolio" = ConsultarCuentaInversion."IdPortafolio" 
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" ConsultarCliente
ON ConsultarCliente."IdCliente" =  ConsultarPortafolio."IdCliente" 
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" ConsultarPersona
ON ConsultarPersona."IdPersona" = ConsultarCliente."IdPersona"
WHERE "EsIngresoManual" = 'S' 
AND CAST(ConsultarCargosAbonos."FechaSistema" AS DATE) = :FechaContable
AND ConsultarCargosAbonos."IdEstado" IN ('CRT');