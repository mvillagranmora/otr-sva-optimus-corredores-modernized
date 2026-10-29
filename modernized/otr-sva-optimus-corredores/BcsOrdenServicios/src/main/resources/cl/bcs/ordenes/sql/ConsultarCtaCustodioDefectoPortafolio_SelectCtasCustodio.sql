SELECT 
	POR."IdPortafolio",
	POR."IdCuentaCustodioPortafolio",
	POR."IdModeloCustodia",
	MODELO."NombreModeloCustodia",
	(TIPOCUSTODIA."NombreTipoCustodia" || ' - ' || BOV."Nombre" || ' - ' || POR."Codigo" ) AS "CuentaCustodioPortafolio",
	POR."IdEstadoAutorizacion",
	POR."IdEstado",
	POR."IdBoveda", 
	BOV."Nombre" as "NombreBoveda",
	BOV."Nombre" || ' (' || POR."Codigo" || ')' as "NombreBovedaCodigo",
	POR."IdTipoCuentaCustodia",
	TIPOCUENTA."NombreTipoCuentaCustodia",
	CASE WHEN (POR."IdBoveda" = :bovedaSugerida) THEN 'DEF' ELSE 'NOD' END AS "Defecto"
	
FROM "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" POR
    INNER JOIN "BSCL.Plataforma.BD::PLA.Boveda" as BOV ON (BOV."IdBoveda" = POR."IdBoveda")
    LEFT JOIN "BSCL.Portafolio.BD::POR.TipoCustodia" TIPOCUSTODIA on TIPOCUSTODIA."IdTipoCustodia" = POR."IdTipoCustodia"
    LEFT JOIN "BSCL.Portafolio.BD::POR.TipoCuentaCustodia" TIPOCUENTA on TIPOCUENTA."IdTipoCuentaCustodia" = POR."IdTipoCuentaCustodia"
    LEFT JOIN "BSCL.Portafolio.BD::POR.ModeloCustodia" MODELO on MODELO."IdModeloCustodia" = POR."IdModeloCustodia"
WHERE "IdPortafolio" IN (:idsPortafolios) 
	AND POR."IdEstadoAutorizacion" != 'RZD'
	AND POR."IdEstado" = 'ACT'
	AND POR."IdModeloCustodia" = CASE WHEN :modeloCustodia <> '' THEN :modeloCustodia ELSE POR."IdModeloCustodia" END  
ORDER BY POR."IdPortafolio", CASE WHEN (POR."IdBoveda" = :bovedaSugerida) THEN 'DEF' ELSE 'NOD' END, POR."IdBoveda";
