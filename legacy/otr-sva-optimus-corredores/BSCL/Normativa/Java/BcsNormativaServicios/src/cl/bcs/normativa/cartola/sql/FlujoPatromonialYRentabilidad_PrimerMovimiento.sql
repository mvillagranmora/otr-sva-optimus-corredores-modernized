SELECT por."IdCliente",
		por."IdPortafolio",
		por."FechaIngreso" AS "FechaCreacion",   -- Fecha se sistema de creacion del portafolio
		
	COALESCE(MIN(cab."FechaEfectivaMovimiento"),'1900-01-01') AS "FechaEfectivaMovimiento"
FROM "BSCL.Portafolio.BD::POR.Portafolio" por
INNER JOIN #tmpTblClientesPortafolios AS TEMP ON (por."IdCliente" = TEMP."IdCliente" AND por."IdPortafolio" = TEMP."IdPortafolio")
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" cta on por."IdPortafolio" = cta."IdPortafolio"
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" cab on cta."IdCuentaInversion" = cab."IdCuentaInversion"

GROUP BY por."IdCliente",por."IdPortafolio",por."FechaIngreso"
