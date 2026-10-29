SELECT
	TB."IdCuentaBanco"
	,COALESCE(SUM(VCFP."MontoIngreso"),0) - COALESCE(SUM(VCFP."MontoEgreso"),0) AS "SaldoPendiente"
FROM "BSCL.Tesoreria.BD::GBL.ActualizarSaldosCuentaBanco_Proceso_Paso" AS TB
LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" as VCFP
	ON VCFP."IdCuentaBancariaCorredora" = TB."IdCuentaBanco"
AND VCFP."AfectoSaldo" = 'N'
AND VCFP."FechaLiquidacion" < :p_FechaMovimiento
GROUP BY TB."IdCuentaBanco";