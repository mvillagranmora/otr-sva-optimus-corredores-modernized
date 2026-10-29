SELECT
	por.*,
	COALESCE(fCaja."Saldo", 0) as "FCA_Saldo",
	'0' as "PRO_IdTipoProducto",
	'0' AS "PRO_IdTipoOperacion"
FROM
	#CumpleRequisitosClienteIngresoOperacionRF_PortafoliosPreferencia_Temp por
	LEFT JOIN #CumpleRequisitosClienteIngresoOperacionRF_FlujoCaja_Temp fCaja ON (por."POR_IdPortafolio" = fCaja."IdPortafolio" and por."PAR_IdMoneda" = fCaja."IdMoneda" and por."PAR_FechaLiquidacion" = fCaja."FechaSaldo")
WHERE
	"REQ_IdCondicionOperativa" <> 'PRODUCTOINVALIDO'
UNION
ALL
SELECT
	*
FROM
	#CumpleRequisitosClienteIngresoOperacionRF_PortafolioMRF_Temp;