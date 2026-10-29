-- CALCULA SALDO PROYECTADO > T0 (t1, t2,t3) (para movimientos y comprobantes con FP)

 
WITH v_SaldoValuta AS 
(
     SELECT
    "IdCuentaBanco",
    "FechaSaldo",
    "Orden",
    SUM("Egreso") AS "SaldoValutaEgreso",
    SUM("Ingreso") AS "SaldoValutaIngreso",
    SUM("Ingreso") - SUM("Egreso")AS "SaldoValuta"
FROM (
    SELECT
        RF."IdCuentaBanco",
        RF."FechaSaldo",
        RF."Orden",
        CFP."MontoIngreso" AS "Ingreso",
        CFP."MontoEgreso" AS "Egreso"
        
    FROM "BSCL.Tesoreria.BD::GBL.ConsultarSaldoCuentaBanco_RangoFechaCuentaBanco" AS RF
    LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" AS CFP
        ON CFP."IdCuentaBancariaCorredora" = RF."IdCuentaBanco"
        AND (CFP."FechaLiquidacion" = RF."FechaSaldo" AND CFP."FechaAfectaSaldo" = '0001-01-01')
    WHERE RF."Orden" > 0
    AND CFP."IdEstadoComprobante" <> 'ANU'

    UNION ALL

    SELECT
        RF."IdCuentaBanco",
        RF."FechaSaldo",
        RF."Orden",
        IEFP."MontoIngreso" AS "Ingreso",
        IEFP."MontoEgreso" AS "Egreso"
    FROM "BSCL.Tesoreria.BD::GBL.ConsultarSaldoCuentaBanco_RangoFechaCuentaBanco" AS RF
    LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgresoFormaDePago" AS IEFP
        ON IEFP."IdCuentaBancariaCorredora" = RF."IdCuentaBanco"
        AND IEFP."FechaLiquidacion" = RF."FechaSaldo"
        AND IEFP."IdEstadoIngresoEgreso" = 'ING'
    WHERE RF."Orden" > 0
)
-- Filtro para excluir filas con todos los valores nulos
WHERE
    "Egreso" IS NOT NULL
    OR "Ingreso" IS NOT NULL
GROUP BY "IdCuentaBanco", "FechaSaldo", "Orden"
ORDER BY "IdCuentaBanco", "FechaSaldo"
)
 
  SELECT  "IdCuentaBanco"
   		 ,"FechaSaldo"
   		 ,MAX(CASE WHEN "Orden"=1 THEN "FechaSaldoValuta" 	ELSE NULL END) AS "FechaT1"
 		 ,MAX(CASE WHEN "Orden"=1 THEN "SaldoValutaEgreso" 	ELSE NULL END) AS "SaldoEgresoT1"
 		 ,MAX(CASE WHEN "Orden"=1 THEN "SaldoValutaIngreso" ELSE NULL END) AS "SaldoIngresoT1"
 		 ,MAX(CASE WHEN "Orden"=1 THEN "SaldoValuta" 		ELSE NULL END) AS "SaldoT1"		 		
 		 
 		 ,MAX(CASE WHEN "Orden"=2 THEN "FechaSaldoValuta" 	ELSE NULL END) AS "FechaT2"
 		 ,MAX(CASE WHEN "Orden"=2 THEN "SaldoValutaEgreso" 	ELSE NULL END) AS "SaldoEgresoT2"
 		 ,MAX(CASE WHEN "Orden"=2 THEN "SaldoValutaIngreso" ELSE NULL END) AS "SaldoIngresoT2"		 		 
 		 ,MAX(CASE WHEN "Orden"=2 THEN "SaldoValuta" 		ELSE NULL END) AS "SaldoT2"		 		
 		 
 		 ,MAX(CASE WHEN "Orden"=3 THEN "FechaSaldoValuta" 	ELSE NULL END) AS "FechaT3"
 		 ,MAX(CASE WHEN "Orden"=3 THEN "SaldoValutaEgreso" 	ELSE NULL END) AS "SaldoEgresoT3"
 		 ,MAX(CASE WHEN "Orden"=3 THEN "SaldoValutaIngreso" ELSE NULL END) AS "SaldoIngresoT3"
 		 ,MAX(CASE WHEN "Orden"=3 THEN "SaldoValuta" 		ELSE NULL END) AS "SaldoT3"

    FROM (SELECT  
   				 SD."IdCuentaBanco"
		 		,SD."FechaSaldo"
		 		,SV."FechaSaldo" as "FechaSaldoValuta"
		 		,SV."SaldoValutaEgreso"
		 		,SV."SaldoValutaIngreso"
		 		,SV."SaldoValuta"
		 		,SV."Orden"
		 FROM "BSCL.Tesoreria.BD::GBL.ConsultarSaldoCuentaBanco_RangoFechaCuentaBanco"   as SD				   
		 LEFT JOIN v_SaldoValuta as SV ON SV."IdCuentaBanco" = SD."IdCuentaBanco" AND SV."FechaSaldo" > SD."FechaSaldo"
		 WHERE SD."Orden" = 0 	-- no registro de proyecccion
		)
   GROUP BY "IdCuentaBanco", "FechaSaldo"
   ORDER BY "IdCuentaBanco","FechaSaldo";
		   
		   