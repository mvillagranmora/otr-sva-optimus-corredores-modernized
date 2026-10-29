SELECT 
"IdCliente"
,"IdPortafolio"
,"fecha"
,"abonosClp"
,"retirosClp"
FROM(
	SELECT row_number() OVER (partition by B."IdCliente", B."IdPortafolio" order BY B."fecha" DESC) AS "Row"
	,"IdCliente"
	,"IdPortafolio"
	,"fecha"
	,"abonosClp"
	,"retirosClp"
	FROM(
		SELECT
		POR."IdCliente",
		COM."IdPortafolioTitularPago" AS "IdPortafolio",
		CAST(COM."FechaLiquidacion" as TIMESTAMP) as "fecha",
		SUM(CASE COM."IdTipoMovimiento" WHEN 'MOVEGR' THEN COM."Monto" ELSE 0 END)  as "retirosClp",
		SUM(CASE COM."IdTipoMovimiento" WHEN 'MOVING' THEN COM."Monto" ELSE 0 END)  as "abonosClp"
		FROM "BSCL.Tesoreria.BD::TES.Comprobante" COM
		INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON COM."IdPortafolioTitularPago" = P."IdPortafolio"
		INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = COM."IdPortafolioTitularPago"
		WHERE  COM."IdEstado" = 'LIQ'
		AND COM."FechaLiquidacion" IS NOT NULL
		AND COM."FechaLiquidacion" BETWEEN :FechaDesde AND :FechaHasta
		AND COM."IdMoneda" = :IdMoneda
		GROUP BY POR."IdCliente", COM."IdPortafolioTitularPago", COM."FechaLiquidacion"
	) B
)
WHERE "Row" < 6
ORDER BY "fecha" desc;