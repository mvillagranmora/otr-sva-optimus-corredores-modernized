
-- OBTENER SALDO DISPONIBLE T0(Saldo real)

WITH v_SaldoDisponibleDetalladoInicial AS
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
		  				WHERE VCFP."IdCuentaBancariaCorredora" IN (:IdCuentaBanco)
				 	    AND   VCFP."FechaAfectaSaldo" < :FechaDesde
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
           FROM "BSCL.Tesoreria.BD::GBL.ConsultarSaldoCuentaBanco_RangoFechaCuentaBanco" as RF
		   LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" as VCFP 
		   		  ON VCFP."IdCuentaBancariaCorredora"	= RF."IdCuentaBanco"
		   		 AND VCFP."FechaAfectaSaldo"	= RF."FechaSaldo" 
		   		 AND VCFP."AfectoSaldo"			= 'S'
		   LEFT JOIN v_SaldoDisponibleInicial as SI
		   		  ON SI."IdCuentaBanco" = RF."IdCuentaBanco"
)	
 
	     SELECT  SC."IdCuentaBanco"
		 		,SC."FechaSaldo"
		 		,SC."SaldoDisponibleInicial"
		 		,SC."SaldoDisponibleEgreso"
		 		,SC."SaldoDisponibleIngreso"
		 		,CAST(SC."SaldoDisponible" as decimal(26,8)) as "SaldoDisponible"
   		   FROM v_SaldoDisponibleDetallado AS SC
   		  INNER JOIN (SELECT MAX("Id") as "MaxId", "FechaSaldo", "IdCuentaBanco" FROM v_SaldoDisponibleDetallado GROUP BY "FechaSaldo", "IdCuentaBanco") AS SCMax
   		 		  ON SCMax."IdCuentaBanco" = SC."IdCuentaBanco" AND SCMax."FechaSaldo" = SC."FechaSaldo" AND SCMax."MaxId" = SC."Id";	
   		 		  

   		 		  
