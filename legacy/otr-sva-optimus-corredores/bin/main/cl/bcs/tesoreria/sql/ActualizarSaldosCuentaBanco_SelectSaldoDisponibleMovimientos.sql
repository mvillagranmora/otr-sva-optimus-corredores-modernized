SELECT
	VCFP."IdComprobanteFormaDePago"
	,CB."IdCuentaBanco"
	,to_varchar(VCFP."FechaHoraAfectaSaldo") AS "FechaHoraAfectaSaldo"
 	,to_varchar(VCFP."FechaHoraLiquidacion") AS "FechaHoraLiquidacion"
 	,CASE WHEN VCFP."IdEstado" <> 'ANU' THEN 
 		(CASE WHEN VCFP."IdTipoMovimiento" = 'MOVEGR' THEN -1*VCFP."Monto" ELSE VCFP."Monto" END) 
 	ELSE 0 END AS "Monto"
FROM "BSCL.Tesoreria.BD::GBL.ActualizarSaldosCuentaBanco_Proceso_Paso" AS CB
INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" 
	AS VCFP ON VCFP."IdCuentaBancariaCorredora" = CB."IdCuentaBanco"
WHERE VCFP."FechaAfectaSaldo" >= :p_FechaMovimiento
AND VCFP."AfectoSaldo" = 'S'
order by VCFP."FechaHoraAfectaSaldo", VCFP."IdComprobanteFormaDePago";