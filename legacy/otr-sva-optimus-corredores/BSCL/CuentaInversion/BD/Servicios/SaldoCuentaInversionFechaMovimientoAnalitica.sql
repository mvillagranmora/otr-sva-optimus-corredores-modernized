SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.CuentaInversion.BD.Servicios::SaldoCuentaInversionFechaMovimientoAnalitica";

CREATE PROCEDURE 
--  
--  Proposito: Consultar el saldo de una cuentas de inversion Analitica.
--  El Procedimiento para analitica no puede tener insert por eso se crea uno nuevo, sin tablas globales
--  Autor: - BCS 
--  Fecha Creacion: 30/07/2018
-- 
--
"BSCL.CuentaInversion.BD.Servicios::SaldoCuentaInversionFechaMovimientoAnalitica" (
    IN  p_IdEmpresa                     BIGINT,
											 
	IN  p_FechaDesde  					NVARCHAR(10),
    IN  p_FechaHasta  					NVARCHAR(10),
   
	OUT p_out_Saldo					TABLE(
										"IdCuentaInversion"      	BIGINT, 
									  	"CodigoCuentaInversion"		NVARCHAR(40), 
									  	"IdCliente"               	BIGINT, 
									  	"IdPortafolio"            	BIGINT,  
									  	"FechaSaldo" 				NVARCHAR(10),
									  	"IdMoneda"                	NVARCHAR(3),
									  	"Saldo"                  	DECIMAL(26,8),
									  	"SaldoDisponible"          	DECIMAL(26,8),                                              	 
									  	"SaldoCargosPendientes"		DECIMAL(26,8),
									  	"SaldoAbonosPendientes"		DECIMAL(26,8),
									  	"UltimaOperacion"			BIGINT
										)
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	READS SQL DATA
	AS 
BEGIN
	--------------------
	--Negocio Aqui
	--------------------
	BEGIN 
		DECLARE v_TablaMovimientos TABLE("IdCuentaInversion" BIGINT,"IdPortafolio" BIGINT,"IdMoneda" NVARCHAR(3),"FechaSistema" NVARCHAR(10),
		"FechaCertificacion" TIMESTAMP, "IdCargoAbonoMovimiento" BIGINT,
		"IdTipoMovimiento" NVARCHAR(6), "IdEstado" NVARCHAR(3), "Saldo" DECIMAL(26,8), "SaldoPorFechaMovimiento" DECIMAL (26,8)
		, "Monto" DECIMAL(26,8), "MontoCargoPendiente" DECIMAL(26,8), "MontoAbonoPendiente" DECIMAL(26,8));
		
		DECLARE v_RangoFecha TABLE("IdCuentaInversion" BIGINT, "CodigoCuentaInversion" NVARCHAR(40), "IdCliente" BIGINT, "IdPortafolio" BIGINT,
		"IdMoneda" NVARCHAR(3), "Fecha" DATE, "MaxOperacion" BIGINT);
		
		DECLARE v_SaldoTotal TABLE("IdCuentaInversion" BIGINT,"FechaSistema" NVARCHAR(10), "Monto" DECIMAL(26,8),
		"MontoCargoPendiente" DECIMAL(26,8), "MontoAbonoPendiente" DECIMAL(26,8));
		
		DECLARE v_SaldoInicialFechaMovimiento TABLE ("IdCuentaInversion" BIGINT, "FechaMovimientoAnterior" NVARCHAR(10), "SaldoMovimientoAnterior" DECIMAL(26,8),
		"SaldoCargoPendienteAnterior" DECIMAL (26,8), "SaldoAbonoPendienteAnterior" DECIMAL(26,8));
		
		DECLARE v_SaldoMovimiento TABLE ("IdCuentaInversion" BIGINT,"CodigoCuentaInversion" NVARCHAR(40), "IdCliente" BIGINT, "IdPortafolio" BIGINT,
		"IdMoneda" NVARCHAR(3), "Fecha" DATE, "MaxOperacion" BIGINT, "Saldo" DECIMAL(26,8), "SaldoCargosPendientes" DECIMAL(26,8), "SaldoAbonosPendientes" DECIMAL(26,8));
		
		v_TablaMovimientos =
		SELECT   B."IdCuentaInversion"
				,B."IdPortafolio"
				,B."IdMoneda"
				,COALESCE(c."FechaSistema",:p_FechaHasta) as "FechaSistema"
				,COALESCE(c."FechaCertificacion",:p_FechaHasta) as "FechaCertificacion"
				,COALESCE(c."IdCargoAbonoMovimiento",0) as "IdCargoAbonoMovimiento"
				,c."IdTipoMovimiento"
				,c."IdEstado"
				,c."Saldo"
				,c."SaldoPorFechaMovimiento"
				,CASE WHEN c."IdEstado" = 'CRT' THEN (CASE WHEN c."IdTipoMovimiento" = 'MOVCAR' THEN  -1*c."Monto" ELSE c."Monto" END) ELSE 0 END AS "Monto"
				,CASE WHEN c."IdEstado" = 'PEN' AND c."IdTipoMovimiento" = 'MOVCAR' THEN c."Monto" ELSE 0 END AS "MontoCargoPendiente"
				,CASE WHEN c."IdEstado" = 'PEN' AND c."IdTipoMovimiento" = 'MOVABN' THEN c."Monto" ELSE 0 END AS "MontoAbonoPendiente"
		  FROM "BSCL.CuentaInversion.BD::CTA.CuentaInversion" as B 
		  LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" as c 
		         ON B."IdCuentaInversion" = c."IdCuentaInversion"
		   WHERE c."IdEstado" IN ('CRT','PEN');
		
		--------------------------
		-- Completar rango fecha
		--------------------------
		v_RangoFecha = 
		SELECT 	A."IdCuentaInversion", 
				A."Codigo" AS "CodigoCuentaInversion", 
				A."IdCliente", 
				A."IdPortafolio", 
				A."IdMoneda",
				GENERATED_PERIOD_START  as "Fecha",CAST(0 AS BIGINT) as "MaxOperacion"
		  FROM "BSCL.CuentaInversion.BD::CTA.CuentaInversion" as A
		  LEFT JOIN SERIES_GENERATE_DATE('INTERVAL 1 DAY', :p_FechaDesde, ADD_DAYS(TO_DATE(:p_FechaHasta,'YYYY-MM-DD'), 1) ) as B ON 1 = 1;
		
		v_RangoFecha = SELECT 
				RF."IdCuentaInversion", 
				RF."CodigoCuentaInversion", 
				RF."IdCliente", 
				RF."IdPortafolio", 
				RF."IdMoneda",
				RF."Fecha",
				COALESCE(MAX("IdCargoAbonoMovimiento"),0) AS "MaxOperacion" 
				FROM :v_RangoFecha RF
				LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" CA ON RF."IdCuentaInversion" = CA."IdCuentaInversion" and CA."FechaSistema" <= RF."Fecha"
				GROUP BY RF."IdCuentaInversion", RF."CodigoCuentaInversion",RF."IdCliente", RF."IdPortafolio", RF."IdMoneda",RF."Fecha";
		
		
		v_SaldoTotal = SELECT  MOVCTA."IdCuentaInversion"
   						,MOVCTA."FechaSistema"
   						,SUM( MOVCTA."Monto") as "Monto"
   						,SUM( MOVCTA."MontoCargoPendiente") as "MontoCargoPendiente"
   						,SUM( MOVCTA."MontoAbonoPendiente") as "MontoAbonoPendiente"
   						FROM :v_TablaMovimientos as MOVCTA
   						 GROUP BY MOVCTA."IdCuentaInversion",MOVCTA."FechaSistema";
   				 
		------------------------------------
		--Obtener Saldo Por Fecha Movimiento
		------------------------------------
		--) Obtener Saldo día Anterior (Inicial)
		v_SaldoDetalladoInicialFechaMovimiento =
		 SELECT  COUNT(CA."FechaSistema") over (PARTITION BY CA."IdCuentaInversion" 
		 										ORDER BY CA."FechaSistema", CA."IdCargoAbonoMovimiento" asc 
		 										ROWS BETWEEN unbounded preceding and CURRENT ROW) as "Id"
				,CA."IdCuentaInversion"
				,FechMax."FechaMovimientoAnterior"
				,CA."SaldoPorFechaMovimiento"	AS "SaldoMovimientoAnterior"
				,FechMax."SaldoCargoPendienteAnterior"
				,FechMax."SaldoAbonoPendienteAnterior"
		   FROM :v_TablaMovimientos as CA
		  INNER JOIN (SELECT B."IdCuentaInversion"
		   					,MAX(CASE WHEN B."IdEstado" = 'CRT' THEN B."FechaSistema" END) as "FechaMovimientoAnterior"
		   					,SUM(B."MontoCargoPendiente") as "SaldoCargoPendienteAnterior"
		   					,SUM(B."MontoAbonoPendiente") as "SaldoAbonoPendienteAnterior"
				 	   FROM :v_TablaMovimientos as B
				 	  WHERE B."FechaSistema" < :p_FechaDesde
				 	  GROUP BY B."IdCuentaInversion"
		  			 ) as FechMax 
		  		  ON CA."FechaSistema" 		= FechMax."FechaMovimientoAnterior" 
		  		 AND CA."IdCuentaInversion" = FechMax."IdCuentaInversion"
		  WHERE CA."IdEstado" = 'CRT';

		v_TablaMovimientos = SELECT * FROM :v_TablaMovimientos WHERE 1 = 2;

		v_SaldoInicialFechaMovimiento =
		 SELECT  SC."IdCuentaInversion"
		 		,SC."FechaMovimientoAnterior"
		 		,SC."SaldoMovimientoAnterior"
		 		,SC."SaldoCargoPendienteAnterior"
		 		,SC."SaldoAbonoPendienteAnterior"
   		   FROM :v_SaldoDetalladoInicialFechaMovimiento AS SC
   		  INNER JOIN (SELECT MAX("Id") as "MaxId","IdCuentaInversion","FechaMovimientoAnterior" 
   		  				FROM :v_SaldoDetalladoInicialFechaMovimiento
   		  			   GROUP BY "FechaMovimientoAnterior", "IdCuentaInversion") AS SCMax
   		 		  ON SCMax."IdCuentaInversion" = SC."IdCuentaInversion"
   		 		 AND SCMax."FechaMovimientoAnterior" = SC."FechaMovimientoAnterior" 
   		 		 AND SCMax."MaxId" = SC."Id";

		--) Obtener Saldo Movimiento
		v_SaldoMovimiento =
		 SELECT	 RF."IdCuentaInversion"
		 	  	,RF."CodigoCuentaInversion"
		 	  	,RF."IdCliente"
		 	  	,RF."IdPortafolio"
			  	,RF."IdMoneda"
			  	,RF."Fecha"
				,RF."MaxOperacion"
			  	,COALESCE(COALESCE(SI."SaldoMovimientoAnterior",0) + 
		 	  	 COALESCE(SUM( MOVCTA."Monto") over (PARTITION BY RF."IdCuentaInversion" ORDER BY RF."Fecha" asc
		 	  	 									 ROWS BETWEEN unbounded preceding and CURRENT ROW),0) ,0) as "Saldo"
				,COALESCE(COALESCE(SI."SaldoCargoPendienteAnterior",0) + 
		 	  	 COALESCE(SUM( MOVCTA."MontoCargoPendiente") over (PARTITION BY RF."IdCuentaInversion" ORDER BY RF."Fecha" asc
		 	  	 									 ROWS BETWEEN unbounded preceding and CURRENT ROW),0) ,0) as "SaldoCargosPendientes"
				,COALESCE(COALESCE(SI."SaldoAbonoPendienteAnterior",0) + 
		 	  	 COALESCE(SUM( MOVCTA."MontoAbonoPendiente") over (PARTITION BY RF."IdCuentaInversion" ORDER BY RF."Fecha" asc
		 	  	 									 ROWS BETWEEN unbounded preceding and CURRENT ROW),0) ,0) as "SaldoAbonosPendientes"
		   FROM :v_RangoFecha as RF
		   LEFT JOIN :v_SaldoTotal AS MOVCTA
		   		  ON MOVCTA."IdCuentaInversion" = RF."IdCuentaInversion" 
		   		 AND MOVCTA."FechaSistema"		= RF."Fecha"
		   LEFT JOIN :v_SaldoInicialFechaMovimiento as SI 
		   		  ON SI."IdCuentaInversion" = RF."IdCuentaInversion";

		p_out_Saldo = 
		SELECT 	 SF."IdCuentaInversion"
				,SF."CodigoCuentaInversion"
				,SF."IdCliente"
   				,SF."IdPortafolio"                						         
   				,SF."Fecha" as "FechaSaldo"
   				,SF."IdMoneda"   				
   				--,CAST(CASE 
   					--WHEN :p_IncluyeCargosPendientes = 'S' AND :p_IncluyeAbonosPendientes = 'S' THEN (SF."Saldo" - SF."SaldoCargosPendientes" + SF."SaldoAbonosPendientes")
   					--WHEN :p_IncluyeCargosPendientes = 'S' AND :p_IncluyeAbonosPendientes = 'N' THEN (SF."Saldo" - SF."SaldoCargosPendientes")
   					--WHEN :p_IncluyeCargosPendientes = 'N' AND :p_IncluyeAbonosPendientes = 'S' THEN (SF."Saldo" + SF."SaldoAbonosPendientes")
   					--WHEN :p_IncluyeCargosPendientes = 'N' AND :p_IncluyeAbonosPendientes = 'N' THEN (SF."Saldo")
   				--  END as DECIMAL(26,8)) as "Saldo"   	
				,(SF."Saldo") AS "Saldo"
   				,CAST(SF."Saldo" as DECIMAL(26,8)) as "SaldoDisponible"
   				,CAST(SF."SaldoCargosPendientes" as DECIMAL(26,8)) as "SaldoCargosPendientes"
   				,CAST(SF."SaldoAbonosPendientes" as DECIMAL(26,8)) as "SaldoAbonosPendientes"
				,"MaxOperacion" AS "UltimaOperacion"																	
   		  FROM :v_SaldoMovimiento AS SF;
	END;
END;
