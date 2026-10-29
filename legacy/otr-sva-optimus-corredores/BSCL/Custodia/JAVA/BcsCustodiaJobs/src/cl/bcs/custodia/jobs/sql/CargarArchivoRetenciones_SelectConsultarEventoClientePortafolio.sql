SELECT 	 EventoCliente."IdSocioNegocio" 		AS "IdSocioNegocio"
		,EventoCliente."IdPortafolio" 			AS "IdPortafolio"  
		,SUM(EventoCliente."Total")				AS "Total"
FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EventoCliente
WHERE EventoCliente."IdEventoCapital" = :IdEventoCapital
  AND EventoCliente."CantidadDerecho" > 0
  AND EventoCliente."TipoCartera" = 'CUS'
  AND EventoCliente."IdBoveda" = 'CAV'
GROUP BY	EventoCliente."IdSocioNegocio", 
			EventoCliente."IdPortafolio";