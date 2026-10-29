WITH FechMax AS(
	SELECT MAX(VCFP."FechaAfectaSaldo") AS "FechaAfectaSaldoAnterior"
			  					,CB."IdCuentaBanco"
	FROM "BSCL.Tesoreria.BD::GBL.ActualizarSaldosCuentaBanco_Proceso_Paso" AS CB
	LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" AS VCFP ON VCFP."IdCuentaBancariaCorredora" = CB."IdCuentaBanco" AND VCFP."AfectoSaldo" = 'S'
	WHERE VCFP."FechaAfectaSaldo" < :p_FechaMovimiento 
	GROUP BY CB."IdCuentaBanco"
	 ),
v_SaldoDisponibleDetalladoInicial AS(
	SELECT  COUNT(VCFP."IdComprobanteFormaDePago") over (partition by VCFP."FechaAfectaSaldo",VCFP."IdCuentaBancariaCorredora" 
		order by VCFP."FechaHoraAfectaSaldo",VCFP."IdComprobanteFormaDePago"  
		ASC rows between unbounded preceding and current row) AS "Id"
					,VCFP."IdComprobanteFormaDePago"
					,COALESCE(VCFP."SaldoDisponible",0) AS "SaldoDisponible"
					,FechMax."IdCuentaBanco"
					,FechMax."FechaAfectaSaldoAnterior"
	FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" AS VCFP
	INNER JOIN FechMax 
	ON VCFP."FechaAfectaSaldo" = FechMax."FechaAfectaSaldoAnterior" 
	AND VCFP."IdCuentaBancariaCorredora" = FechMax."IdCuentaBanco"
	AND VCFP."AfectoSaldo" = 'S'
),
SCMax AS(
	SELECT MAX("Id") AS "MaxId", "IdCuentaBanco", "FechaAfectaSaldoAnterior" 
	FROM v_SaldoDisponibleDetalladoInicial 
	GROUP BY "FechaAfectaSaldoAnterior", "IdCuentaBanco"
)
SELECT 
	SC."IdCuentaBanco"
	,SC."IdComprobanteFormaDePago"
	,to_varchar(SC."FechaAfectaSaldoAnterior") AS "FechaAfectaSaldoAnterior"
	,CAST(SC."SaldoDisponible" AS decimal(26,8)) AS "SaldoAnterior"
FROM v_SaldoDisponibleDetalladoInicial AS SC
INNER JOIN SCMax
ON SCMax."IdCuentaBanco" = SC."IdCuentaBanco" 
AND SCMax."FechaAfectaSaldoAnterior" = SC."FechaAfectaSaldoAnterior" 
AND SCMax."MaxId" = SC."Id";