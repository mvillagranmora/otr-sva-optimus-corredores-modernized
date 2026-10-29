SELECT
TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion"
,ConsultarCargosAbonos."IdCargoAbonoMovimiento" 	*10		AS "IdMovimiento" 
,CASE ConsultarCargosAbonos."IdTipoMovimiento" 		
							WHEN 'MOVABN' 
							THEN 'ABONO'
							ELSE 'CARGO' 			  END	AS "IdEvento"		
,CASE ConsultarCargosAbonos."IdTipoMovimiento" 		
							WHEN 'MOVABN' 
							THEN 'ABONO CTA INVERSION'
							ELSE 'CARGO CTA INVERSION' END	AS "Nombre"
,ConsultarCargosAbonos."Monto"	 							AS "MontoEvento"
,ConsultarCargosAbonos."IdMoneda" 							AS "CodigoMoneda"
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