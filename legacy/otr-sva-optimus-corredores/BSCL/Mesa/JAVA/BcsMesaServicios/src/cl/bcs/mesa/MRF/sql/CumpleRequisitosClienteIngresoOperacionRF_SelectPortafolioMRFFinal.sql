SELECT
	pr.*,
	CAST(0 AS DECIMAL(27,8)) as "FCA_Saldo",
	COALESCE(mrf."PRO_IdTipoProducto", 'PROINV') as "PRO_IdTipoProducto",
	COALESCE(mrf."PRO_IdTipoOperacion", 'OPERINV') AS "PRO_IdTipoOperacion"
FROM
	#CumpleRequisitosClienteIngresoOperacionRF_PortafolioMRF_Temp mrf
	RIGHT JOIN #CumpleRequisitosClienteIngresoOperacionRF_PortafoliosPreferencia_Temp pr ON mrf."POR_IdPortafolio"	= pr."POR_IdPortafolio" AND (mrf."PRO_IdTipoOperacion" = pr."PAR_IdTipoOperacion" OR mrf."PRO_IdTipoOperacion" = '0')
WHERE
	pr."REQ_IdCondicionOperativa" = 'PRODUCTOINVALIDO';