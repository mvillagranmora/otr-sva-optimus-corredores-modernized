SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.BAN::ActualizarSaldosCuentaBanco";

CREATE PROCEDURE 
--  
--  Proposito: Actualizar los saldos de la cuenta banco
--  Autor: - BCS
--  Fecha Creacion: 05/05/2016
--  
"BSCL.Tesoreria.BD.Servicios.BAN::ActualizarSaldosCuentaBanco" (
	IN  p_FechaMovimiento		NVARCHAR(10),
	IN	p_CuentaBanco			TABLE("IdCuentaBanco"	BIGINT)
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER
	DEFAULT SCHEMA "BSCL" AS
BEGIN 
  
  	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		RESIGNAL; 
	END;
	 
	--------------------
	--Negocio Aqui
	--------------------
	BEGIN

		-----------------------------
		--Actualizar Saldo Disponible
		-----------------------------
		
		--) Obtener Saldo disponible Inicial
		v_SaldoDisponibleDetalladoInicial =
		 SELECT  COUNT(VCFP."IdComprobanteFormaDePago") over (partition by VCFP."FechaAfectaSaldo" order by VCFP."FechaHoraAfectaSaldo",VCFP."IdComprobanteFormaDePago"  asc rows between unbounded preceding and current row) as "Id"
				,VCFP."IdComprobanteFormaDePago"
				,VCFP."SaldoDisponible"
				,FechMax."IdCuentaBanco"
				,FechMax."FechaAfectaSaldoAnterior"
		   FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" as VCFP
		  INNER JOIN (SELECT MAX(VCFP."FechaAfectaSaldo") as "FechaAfectaSaldoAnterior"
		  					,CB."IdCuentaBanco"
				 	   FROM :p_CuentaBanco AS CB
				  	   LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" as VCFP ON VCFP."IdCuentaBancariaCorredora" = CB."IdCuentaBanco"
				 	  WHERE VCFP."FechaAfectaSaldo" < :p_FechaMovimiento
				 	  GROUP BY CB."IdCuentaBanco"
		  			 ) as FechMax 
		  		  ON VCFP."FechaAfectaSaldo" = FechMax."FechaAfectaSaldoAnterior" 
		  		 AND VCFP."IdCuentaBancariaCorredora" = FechMax."IdCuentaBanco"
		  		 AND VCFP."AfectoSaldo"	= 'S';
		  		  
		v_SaldoDisponibleInicial =
		 SELECT  SC."IdCuentaBanco"
		 		,SC."FechaAfectaSaldoAnterior"
		 		,CAST(SC."SaldoDisponible" as decimal(26,8)) as "SaldoAnterior"
   		   FROM :v_SaldoDisponibleDetalladoInicial AS SC
   		  INNER JOIN (SELECT MAX("Id") as "MaxId", "IdCuentaBanco", "FechaAfectaSaldoAnterior" FROM :v_SaldoDisponibleDetalladoInicial GROUP BY "FechaAfectaSaldoAnterior", "IdCuentaBanco") AS SCMax
   		 		  ON SCMax."IdCuentaBanco" = SC."IdCuentaBanco" AND SCMax."FechaAfectaSaldoAnterior" = SC."FechaAfectaSaldoAnterior" AND SCMax."MaxId" = SC."Id";	
		
	
	
		--) Obtener Movimientos
		v_Movimientos =
		 SELECT  VCFP."IdComprobanteFormaDePago"
		 		,CB."IdCuentaBanco"
		 		,VCFP."FechaHoraAfectaSaldo"
		 		,VCFP."FechaHoraLiquidacion"
		 		,CASE WHEN VCFP."IdEstado" <> 'ANU' THEN (CASE WHEN VCFP."IdTipoMovimiento" = 'MOVEGR' THEN -1*VCFP."Monto" ELSE VCFP."Monto" END) 
		 		      ELSE 0 
		 		  END AS "Monto"
		 		,SDI."SaldoAnterior"
		   FROM :p_CuentaBanco as CB
		  INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" as VCFP ON VCFP."IdCuentaBancariaCorredora" = CB."IdCuentaBanco"
		   LEFT JOIN :v_SaldoDisponibleInicial as SDI ON VCFP."IdCuentaBancariaCorredora" = SDI."IdCuentaBanco"
		  WHERE VCFP."FechaAfectaSaldo" >= :p_FechaMovimiento
		    AND VCFP."AfectoSaldo" = 'S';
		

		--) Actualizar Saldo Disponible
		UPDATE CFP
		   SET CFP."SaldoDisponible" = SALDO."SaldoDisponible"
		  FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" as CFP
		 INNER JOIN ( SELECT  MOV."IdComprobanteFormaDePago"
						 	 ,COALESCE(MOV."SaldoAnterior",0) + 
						 	  COALESCE(SUM(MOV."Monto") 
						 	  		over (partition by MOV."IdCuentaBanco" 
									order by MOV."FechaHoraAfectaSaldo",MOV."FechaHoraLiquidacion",MOV."IdComprobanteFormaDePago" asc 
									rows between unbounded preceding and current row),0) as "SaldoDisponible"		 	
						FROM :v_Movimientos as MOV
		 			) AS SALDO 
		 		 ON SALDO."IdComprobanteFormaDePago" = CFP."IdComprobanteFormaDePago";
		
		-----------------------------------------------------------------------------------------------
		--Actualizar Saldo Contable (Saldo disponible + Saldo Pendiente + Tn (que no han afectado saldo))
		------------------------------------------------------------------------------------------------
	
		--) Obtener Saldo Pendiente
		v_SaldoPendiente =
		 SELECT  TB."IdCuentaBanco"
		 		,COALESCE(SUM(VCFP."MontoIngreso"),0) - COALESCE(SUM(VCFP."MontoEgreso"),0) AS "SaldoPendiente"
		   FROM :p_CuentaBanco AS TB
		   LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" as VCFP
		  		  ON VCFP."IdCuentaBancariaCorredora" = TB."IdCuentaBanco"
		  		 AND VCFP."AfectoSaldo" = 'N'
		         AND VCFP."FechaLiquidacion" < :p_FechaMovimiento
		  GROUP BY TB."IdCuentaBanco";
	
		
		--) Obtener Movimientos
		v_Movimientos2 =	
		 SELECT	 VCFP."IdComprobanteFormaDePago"
		 		,CB."IdCuentaBanco"
		 		,VCFP."FechaHoraAfectaSaldo"
		 		,VCFP."FechaHoraLiquidacion"
		 		,CASE WHEN VCFP."IdEstado" <> 'ANU' THEN (CASE WHEN VCFP."IdTipoMovimiento" = 'MOVEGR' THEN -1*VCFP."Monto" ELSE VCFP."Monto" END) 
		 		      ELSE 0 
		 		  END AS "Monto"
		 		,SP."SaldoPendiente"
		 		,VCFP."SaldoDisponible"
		   FROM :p_CuentaBanco as CB
		   LEFT JOIN :v_SaldoPendiente as SP
		   		  ON SP."IdCuentaBanco" = CB."IdCuentaBanco"
		   		-- AND SP."FechaSaldo" = RF."FechaSaldo"
		   LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" as VCFP 
		   		  ON VCFP."IdCuentaBancariaCorredora" = CB."IdCuentaBanco"
		   		 AND VCFP."FechaLiquidacion" >= :p_FechaMovimiento;		


		--) Actualizar Saldo Contable
		UPDATE CFP
		   SET CFP."SaldoContable" = SALDO."SaldoContable"
		  FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" as CFP
		 INNER JOIN ( SELECT  MOV."IdComprobanteFormaDePago"
						 	 ,COALESCE(MOV."SaldoPendiente",0) + 
						 	  COALESCE(SUM(MOV."Monto") 
						 	  		over (partition by MOV."IdCuentaBanco" 
									order by MOV."FechaHoraAfectaSaldo" desc,MOV."FechaHoraLiquidacion",MOV."IdComprobanteFormaDePago" asc 
									rows between unbounded preceding and current row),0) as "SaldoContable"		 	
						FROM :v_Movimientos2 as MOV
		 			) AS SALDO 
		 		 ON SALDO."IdComprobanteFormaDePago" = CFP."IdComprobanteFormaDePago";
	END;
END;
