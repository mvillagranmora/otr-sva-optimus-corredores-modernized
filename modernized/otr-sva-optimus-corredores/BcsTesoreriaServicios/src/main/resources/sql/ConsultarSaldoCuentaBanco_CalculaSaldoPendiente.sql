
-- OBTIENE SALDOS PENDIENTES (Suma movimientos y comprobantes en transito que aun no se han certificado/liquidado)
 
WITH v_SaldoPendienteDetallado AS
(
		 SELECT  COUNT(TB."FechaSaldo") over (partition by TB."FechaSaldo"  order by TB."FechaSaldo" asc rows between unbounded preceding and current row) as "Id"
		 		,TB."IdCuentaBanco"
		 		,TB."FechaSaldo"
		 		,COALESCE(SUM(FP."MontoEgreso") over (partition by TB."IdCuentaBanco",TB."FechaSaldo"),0) AS "SaldoPendienteEgreso"
		 		,COALESCE(SUM(FP."MontoIngreso") over (partition by TB."IdCuentaBanco",TB."FechaSaldo"),0) AS "SaldoPendienteIngreso"
		 		,COALESCE(SUM(FP."MontoConSigno") over (partition by TB."IdCuentaBanco",TB."FechaSaldo"),0) AS "SaldoPendiente"

           FROM "BSCL.Tesoreria.BD::GBL.ConsultarSaldoCuentaBanco_RangoFechaCuentaBanco"  AS TB
           LEFT JOIN 
           (
           		SELECT CFP."MontoEgreso", CFP."MontoIngreso", CFP."MontoConSigno", CFP."IdCuentaBancariaCorredora", CFP."FechaLiquidacion"
		   		FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" AS CFP
		  		WHERE CFP."AfectoSaldo" = 'N'
				AND CFP."IdEstadoComprobante" <> 'ANU'

				UNION ALL

				SELECT IEFP."MontoEgreso", IEFP."MontoIngreso", IEFP."MontoConSigno", IEFP."IdCuentaBancariaCorredora", IEFP."FechaLiquidacion"
		        FROM "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgresoFormaDePago" AS IEFP
				WHERE IEFP."IdEstadoIngresoEgreso" = 'ING'
				AND IEFP."IdEstado" <> 'ANU'
           ) AS FP ON (FP."IdCuentaBancariaCorredora" = TB."IdCuentaBanco" AND FP."FechaLiquidacion" <= TB."FechaSaldo")
)           

SELECT  SC."IdCuentaBanco"
 		,SC."FechaSaldo"
 		,SC."SaldoPendienteEgreso"
 		,SC."SaldoPendienteIngreso"
 		,SC."SaldoPendiente"
FROM v_SaldoPendienteDetallado AS SC
INNER JOIN (SELECT MAX("Id") as "MaxId", "FechaSaldo", "IdCuentaBanco" FROM v_SaldoPendienteDetallado GROUP BY "FechaSaldo", "IdCuentaBanco") AS SCMax
   		 ON SCMax."IdCuentaBanco" = SC."IdCuentaBanco" AND SCMax."FechaSaldo" = SC."FechaSaldo" AND SCMax."MaxId" = SC."Id";
   		 		  
   		 		  