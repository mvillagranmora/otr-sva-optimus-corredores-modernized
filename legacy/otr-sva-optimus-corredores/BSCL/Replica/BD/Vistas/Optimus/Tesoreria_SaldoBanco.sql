SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Replica.BD.Vistas::Optimus.Tesoreria_SaldoBanco"; 


CREATE VIEW "BSCL.Replica.BD.Vistas::Optimus.Tesoreria_SaldoBanco"
AS

WITH
v_SaldoDisponibleDetalladoInicial AS
(
		 SELECT  
		 		COUNT(VCFP."IdComprobanteFormaDePago") over (partition by VCFP."FechaAfectaSaldo" order by VCFP."FechaAfectaSaldo",VCFP."FechaHoraLiquidacion",VCFP."IdComprobanteFormaDePago"  asc rows between unbounded preceding and current row) as "Id"
				,VCFP."IdComprobanteFormaDePago"
				,VCFP."SaldoDisponible"
				,FechMax."IdCuentaBanco"
				,FechMax."FechaAfectaSaldoAnterior"
		   FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" as VCFP
		  INNER JOIN (
		  			   SELECT MAX(VCFP."FechaAfectaSaldo") as "FechaAfectaSaldoAnterior"
		  					,VCFP."IdCuentaBancariaCorredora" AS "IdCuentaBanco"
		  				FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" AS VCFP
		  				INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS fec ON (fec."IdEmpresa" = 1 and "CodigoParametro" = 'PLA.FechaSistema')
		  				WHERE VCFP."IdCuentaBancariaCorredora" IN (SELECT  
	 		 CB."IdCuentaBanco"		
	FROM "BSCL.Tesoreria.BD::TES.CuentaBanco" as CB 	 		 	 	 		 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON ON (MON."IdMoneda" = CB."IdMoneda")
	LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco"  AS BAN ON (BAN."IdBanco"  = CB."IdBanco"))
				 	    AND   VCFP."FechaAfectaSaldo" < fec."Valor"
						AND   VCFP."IdEstado" <> 'ANU'
				 	    GROUP BY VCFP."IdCuentaBancariaCorredora"
		  			 ) as FechMax 
		  		  ON VCFP."FechaAfectaSaldo" = FechMax."FechaAfectaSaldoAnterior" 
		  		 AND VCFP."IdCuentaBancariaCorredora" = FechMax."IdCuentaBanco"
		  		
),
v_SaldoDisponibleInicial AS 
(
		SELECT   SC."IdCuentaBanco"
		 		,SC."FechaAfectaSaldoAnterior"
		 		,CAST(SC."SaldoDisponible" as decimal(26,8)) as "SaldoDisponible"
   		FROM v_SaldoDisponibleDetalladoInicial AS SC
   		INNER JOIN (
		   				SELECT 
		   					MAX("Id") as "MaxId", 
		   					"IdCuentaBanco", 
		   					"FechaAfectaSaldoAnterior" 
		   				FROM v_SaldoDisponibleDetalladoInicial 
		   				GROUP BY "FechaAfectaSaldoAnterior", "IdCuentaBanco"
   					) AS SCMax
   		
   		 		  ON SCMax."IdCuentaBanco" = SC."IdCuentaBanco" AND SCMax."FechaAfectaSaldoAnterior" = SC."FechaAfectaSaldoAnterior" AND SCMax."MaxId" = SC."Id"
),
v_ConsultarSaldoCuentaBanco_RangoFechaCuentaBanco AS 
(
	SELECT  CB."IdCuentaBanco", (SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema') AS "FechaSaldo", 0 AS "Orden"
			,CB."IdMoneda" AS "Moneda"
			,BAN."IdBanco"
			,BAN."NombreBanco"
			,CB."NombreCuentaBancaria" AS "NombreCuenta"
			,CB."NumeroCuentaBancaria" AS "NumeroCuenta"
	FROM "BSCL.Tesoreria.BD::TES.CuentaBanco" as CB 	 		 	 	 		 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON ON (MON."IdMoneda" = CB."IdMoneda")
	LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco"  AS BAN ON (BAN."IdBanco"  = CB."IdBanco") 
),
v_SaldoDisponibleDetallado AS
(
				 SELECT	 COUNT(RF."FechaSaldo") over (partition by RF."FechaSaldo" order by RF."FechaSaldo", VCFP."FechaHoraAfectaSaldo",VCFP."FechaHoraLiquidacion",VCFP."IdComprobanteFormaDePago"  asc rows between unbounded preceding and current row) as "Id"
		 		,VCFP."IdComprobanteFormaDePago"
		 		,RF."IdCuentaBanco"
		 		,RF."FechaSaldo"
		 		,VCFP."FechaAfectaSaldo"
		 		,VCFP."FechaHoraAfectaSaldo"
		 		,COALESCE(SI."SaldoDisponible",0) as "SaldoDisponibleInicial"
		 		,COALESCE(SUM(VCFP."MontoEgreso") over (partition by RF."IdCuentaBanco" order by RF."FechaSaldo", VCFP."FechaHoraAfectaSaldo", VCFP."IdComprobanteFormaDePago" asc rows between unbounded preceding and current row),0) as "SaldoDisponibleEgreso"
		 		,COALESCE(SUM(VCFP."MontoIngreso") over (partition by RF."IdCuentaBanco" order by RF."FechaSaldo", VCFP."FechaHoraAfectaSaldo", VCFP."IdComprobanteFormaDePago" asc rows between unbounded preceding and current row),0) as "SaldoDisponibleIngreso"		 				 		
		 		,COALESCE(SI."SaldoDisponible",0) + COALESCE(SUM(VCFP."MontoConSigno") over (partition by RF."IdCuentaBanco" order by RF."FechaSaldo", VCFP."FechaHoraAfectaSaldo", VCFP."IdComprobanteFormaDePago" asc rows between unbounded preceding and current row),0) as "SaldoDisponible"
				,RF."Moneda"
				,RF."IdBanco"
				,RF."NombreBanco"
				,RF."NombreCuenta"
				,RF."NumeroCuenta"
		 		FROM v_ConsultarSaldoCuentaBanco_RangoFechaCuentaBanco as RF
		   LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" as VCFP 
		   		  ON VCFP."IdCuentaBancariaCorredora"	= RF."IdCuentaBanco"
		   		 AND VCFP."FechaAfectaSaldo"	= RF."FechaSaldo" 
		   		 AND VCFP."AfectoSaldo"			= 'S'
		   LEFT JOIN v_SaldoDisponibleInicial as SI
		   		  ON SI."IdCuentaBanco" = RF."IdCuentaBanco"
),
v_SaldoDisponibleFinal AS
(
	     SELECT  SC."IdCuentaBanco"
		 		,SC."FechaSaldo"
		 		,SC."Moneda"
		 		,SC."IdBanco"
		 		,SC."NombreBanco"
		 		,SC."NombreCuenta"
		 		,SC."NumeroCuenta"
		 		,SC."SaldoDisponibleInicial"
		 		,SC."SaldoDisponibleEgreso"
		 		,SC."SaldoDisponibleIngreso"
		 		,CAST(SC."SaldoDisponible" as decimal(26,8)) as "SaldoDisponible"
   		   FROM v_SaldoDisponibleDetallado AS SC
   		  INNER JOIN (SELECT MAX("Id") as "MaxId", "FechaSaldo", "IdCuentaBanco" FROM v_SaldoDisponibleDetallado GROUP BY "FechaSaldo", "IdCuentaBanco") AS SCMax
   		 		  ON SCMax."IdCuentaBanco" = SC."IdCuentaBanco" AND SCMax."FechaSaldo" = SC."FechaSaldo" AND SCMax."MaxId" = SC."Id"	
),
v_SaldoPendienteDetallado AS
(
		 SELECT  COUNT(TB."FechaSaldo") over (partition by TB."FechaSaldo"  order by TB."FechaSaldo" asc rows between unbounded preceding and current row) as "Id"
		 		,TB."IdCuentaBanco"
		 		,TB."FechaSaldo"
		 		,COALESCE(SUM(FP."MontoEgreso") over (partition by TB."IdCuentaBanco",TB."FechaSaldo"),0) AS "SaldoPendienteEgreso"
		 		,COALESCE(SUM(FP."MontoIngreso") over (partition by TB."IdCuentaBanco",TB."FechaSaldo"),0) AS "SaldoPendienteIngreso"
		 		,COALESCE(SUM(FP."MontoConSigno") over (partition by TB."IdCuentaBanco",TB."FechaSaldo"),0) AS "SaldoPendiente"

           FROM v_ConsultarSaldoCuentaBanco_RangoFechaCuentaBanco  AS TB
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
),
v_SaldoPendienteFinal AS
(
SELECT  SC."IdCuentaBanco"
 		,SC."FechaSaldo"
 		,SC."SaldoPendienteEgreso"
 		,SC."SaldoPendienteIngreso"
 		,SC."SaldoPendiente"
FROM v_SaldoPendienteDetallado AS SC
INNER JOIN (SELECT MAX("Id") as "MaxId", "FechaSaldo", "IdCuentaBanco" FROM v_SaldoPendienteDetallado GROUP BY "FechaSaldo", "IdCuentaBanco") AS SCMax
   		 ON SCMax."IdCuentaBanco" = SC."IdCuentaBanco" AND SCMax."FechaSaldo" = SC."FechaSaldo" AND SCMax."MaxId" = SC."Id"
)

SELECT SDF."IdBanco"
	,SDF."NombreBanco" 
	,SDF."NombreCuenta"
	,SDF."NumeroCuenta"
	,SDF."FechaSaldo" AS "FechaConsulta"
	,SDF."Moneda"
	,SDF."SaldoDisponibleInicial"
	,SDF."SaldoDisponibleEgreso"
	,SDF."SaldoDisponibleIngreso"
	,SDF."SaldoDisponible"
	,COALESCE (SPF."SaldoPendienteEgreso", 0) AS "SaldoPendienteEgreso"
 	,COALESCE(SPF."SaldoPendienteIngreso", 0) AS "SaldoPendienteIngreso"
 	,COALESCE(SPF."SaldoPendiente",0) AS "SaldoPendiente"
FROM v_SaldoDisponibleFinal SDF
LEFT JOIN v_SaldoPendienteFinal SPF ON SDF."IdCuentaBanco" = SPF."IdCuentaBanco" AND SDF."FechaSaldo" = SPF."FechaSaldo";


GRANT SELECT ON "BSCL.Replica.BD.Vistas::Optimus.Tesoreria_SaldoBanco" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 