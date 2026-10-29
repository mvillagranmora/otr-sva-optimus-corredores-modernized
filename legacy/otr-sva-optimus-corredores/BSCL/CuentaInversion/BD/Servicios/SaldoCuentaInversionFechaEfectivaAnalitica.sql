SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.CuentaInversion.BD.Servicios::SaldoCuentaInversionFechaEfectivaAnalitica";

CREATE PROCEDURE 
--  
--  Proposito: Consultar el saldo de una cuentas de inversion Analitica.
--  El Procedimiento para analitica no puede tener insert por eso se crea uno nuevo, sin tablas globales
--  Autor: - BCS 
--  Fecha Creacion: 30/07/2018
-- 
--
"BSCL.CuentaInversion.BD.Servicios::SaldoCuentaInversionFechaEfectivaAnalitica" (
    IN  p_IdEmpresa                     BIGINT,
											 
	IN  p_FechaDesde  					NVARCHAR(10),
    IN  p_FechaHasta  					NVARCHAR(10),
    IN  p_IncluyeCargosPendientes       NVARCHAR(1)		DEFAULT 'N', --N: Solo incluye cargos certificados, S:Incluye cargos pendientes y certificados
    IN  p_IncluyeAbonosPendientes       NVARCHAR(1)		DEFAULT 'N', --N: Solo incluye abonos certificados, S:Incluye abonos pendientes y certificados
	
	OUT p_out_Saldo		TABLE(
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

		v_TablaMovimientos =
		SELECT   B."IdCuentaInversion"
				,B."IdPortafolio"
				,B."IdMoneda"
				,COALESCE(c."FechaSistema",:p_FechaHasta) as "FechaSistema"
				,COALESCE(c."FechaCertificacion",:p_FechaHasta) as "FechaCertificacion"
				,COALESCE(c."FechaIngresoRegistro",:p_FechaHasta) as "FechaIngresoRegistro"
				,COALESCE(c."FechaEfectivaMovimiento",:p_FechaHasta) as "FechaEfectivaMovimiento"
				,COALESCE(c."IdCargoAbonoMovimiento",0) as "IdCargoAbonoMovimiento"
				,c."IdTipoMovimiento"
				,c."IdEstado"
				,c."Saldo"
				,c."SaldoPorFechaMovimiento"
				,CASE WHEN c."IdEstado" = 'CRT' THEN (CASE WHEN c."IdTipoMovimiento" = 'MOVCAR' THEN  -1*c."Monto" ELSE c."Monto" END) ELSE 0 END AS "Monto"
				,CASE WHEN c."IdEstado" = 'PEN' AND c."IdTipoMovimiento" = 'MOVCAR' THEN c."Monto" ELSE 0 END AS "MontoCagoPendiente"
				,CASE WHEN c."IdEstado" = 'PEN' AND c."IdTipoMovimiento" = 'MOVABN' THEN c."Monto" ELSE 0 END AS "MontoAbonoPendiente"
		  FROM "BSCL.CuentaInversion.BD::CTA.CuentaInversion" as B 
		  LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" as c 
		         ON B."IdCuentaInversion" = c."IdCuentaInversion"
		  WHERE c."IdEstado" IN ('CRT','PEN');
		
		--SELECT 'v_TablaMovimientos',* FROM :v_TablaMovimientos;
		
		--------------------------
		-- Completar rango fecha
		--------------------------
		v_RangoFecha = 
		SELECT 	A."IdCuentaInversion", 
				A."Codigo" AS "CodigoCuentaInversion", 
				A."IdCliente", 
				A."IdPortafolio", 
				A."IdMoneda",
				A."CuentaInversionPrincipal",
				GENERATED_PERIOD_START  as "Fecha",CAST(0 AS BIGINT) as "MaxOperacion"
		  FROM "BSCL.CuentaInversion.BD::CTA.CuentaInversion" as A
		  LEFT JOIN SERIES_GENERATE_DATE('INTERVAL 1 DAY', :p_FechaDesde, ADD_DAYS(TO_DATE(:p_FechaHasta,'YYYY-MM-DD'), 1) ) as B ON 1 = 1;
		
		v_RangoFecha = SELECT 
				RF."IdCuentaInversion", 
				RF."CodigoCuentaInversion", 
				RF."IdCliente", 
				RF."IdPortafolio", 
				RF."IdMoneda",
				RF."CuentaInversionPrincipal",
				RF."Fecha",
				COALESCE(MAX("IdCargoAbonoMovimiento"),0) AS "MaxOperacion" 
				FROM :v_RangoFecha RF
				LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" CA ON RF."IdCuentaInversion" = CA."IdCuentaInversion" and CA."FechaEfectivaMovimiento" <= RF."Fecha"
				GROUP BY RF."IdCuentaInversion", RF."CodigoCuentaInversion",RF."IdCliente", RF."IdPortafolio", RF."IdMoneda",RF."CuentaInversionPrincipal",RF."Fecha";
		
		--select 'v_RangoFecha',* from :v_RangoFecha;
		------------------------------------
		--Obtener Saldo Por Fecha Efectiva
		------------------------------------
		--) Obtener Saldo día Anterior (Inicial)
		v_SaldoDetalladoInicialFechaEfectiva =
		 SELECT  COUNT(CA."FechaEfectivaMovimiento") over (PARTITION BY CA."IdCuentaInversion"
		 												   ORDER BY CA."FechaEfectivaMovimiento", CA."FechaCertificacion", CA."IdCargoAbonoMovimiento" asc 
		 												   ROWS BETWEEN unbounded preceding and CURRENT ROW) as "Id"
				,CA."IdCuentaInversion"
				,FechMax."FechaEfectivaAnterior" AS "FechaEfectivaAnterior"
				,CA."Saldo" AS "SaldoEfectivoAnterior"
				,FechMax."SaldoCargoPendienteAnterior"
				,FechMax."SaldoAbonoPendienteAnterior"
		   FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" as CA
		  INNER JOIN (SELECT B."IdCuentaInversion"
		   					,MAX(CASE WHEN B."IdEstado" = 'CRT' THEN B."FechaEfectivaMovimiento" END) as "FechaEfectivaAnterior"	   					
		   					,SUM(B."MontoCagoPendiente") as "SaldoCargoPendienteAnterior"
		   					,SUM(B."MontoAbonoPendiente") as "SaldoAbonoPendienteAnterior"
				 	   FROM :v_TablaMovimientos as B
				 	  WHERE B."FechaEfectivaMovimiento" < :p_FechaDesde
				 	  GROUP BY B."IdCuentaInversion"
		  			 ) as FechMax 
				  ON CA."FechaEfectivaMovimiento" = FechMax."FechaEfectivaAnterior"
				 AND CA."IdCuentaInversion" = FechMax."IdCuentaInversion"
		  WHERE CA."IdEstado" = 'CRT';
		
		--select 'v_SaldoDetalladoInicialFechaEfectiva',* from :v_SaldoDetalladoInicialFechaEfectiva;
					
		v_SaldoInicialFechaEfectiva =
		 SELECT  SC."IdCuentaInversion"
		 		,SC."FechaEfectivaAnterior"
		 		,SC."SaldoEfectivoAnterior"
		 		,SC."SaldoCargoPendienteAnterior"
		 		,SC."SaldoAbonoPendienteAnterior"
   		   FROM :v_SaldoDetalladoInicialFechaEfectiva AS SC
   		  INNER JOIN (SELECT MAX("Id") as "MaxId","IdCuentaInversion","FechaEfectivaAnterior" 
   		  				FROM :v_SaldoDetalladoInicialFechaEfectiva 
   		  			   GROUP BY "FechaEfectivaAnterior", "IdCuentaInversion") AS SCMax
   		 		  ON SCMax."IdCuentaInversion" = SC."IdCuentaInversion" 
   		 		 AND SCMax."FechaEfectivaAnterior" = SC."FechaEfectivaAnterior" 
   		 		 AND SCMax."MaxId" = SC."Id";

		--select 'v_SaldoInicialFechaEfectiva',* from :v_SaldoInicialFechaEfectiva;
		--) Obtener Saldo Efectivo
		v_SaldoEfectivo =
		 SELECT	 RF."IdCuentaInversion"
		 	  	,RF."CodigoCuentaInversion"
		 	  	,RF."IdCliente"
		 	  	,RF."IdPortafolio"
			  	,RF."IdMoneda"
			  	,RF."Fecha"
			  	,RF."MaxOperacion"
			  	,COALESCE(COALESCE(SI."SaldoEfectivoAnterior",0) + 
		 	  	 COALESCE(SUM( MOVCTA."Monto") over (PARTITION BY RF."IdCuentaInversion" ORDER BY RF."Fecha" asc
		 	  	 									 ROWS BETWEEN unbounded preceding and CURRENT ROW),0) ,0) as "Saldo"
				,COALESCE(COALESCE(SI."SaldoCargoPendienteAnterior",0) + 
		 	  	 COALESCE(SUM( MOVCTA."MontoCagoPendiente") over (PARTITION BY RF."IdCuentaInversion" ORDER BY RF."Fecha" asc
		 	  	 									 ROWS BETWEEN unbounded preceding and CURRENT ROW),0) ,0) as "SaldoCargosPendientes"
				,COALESCE(COALESCE(SI."SaldoAbonoPendienteAnterior",0) + 
		 	  	 COALESCE(SUM( MOVCTA."MontoAbonoPendiente") over (PARTITION BY RF."IdCuentaInversion" ORDER BY RF."Fecha" asc
		 	  	 									 ROWS BETWEEN unbounded preceding and CURRENT ROW),0) ,0) as "SaldoAbonosPendientes"
		   FROM :v_RangoFecha as RF
		   LEFT JOIN (
		   				SELECT  MOVCTA."IdCuentaInversion"
		   					   ,MOVCTA."FechaEfectivaMovimiento"
		   					   ,SUM( MOVCTA."Monto") as "Monto"
		   					   ,SUM( MOVCTA."MontoCagoPendiente") as "MontoCagoPendiente"
		   					   ,SUM( MOVCTA."MontoAbonoPendiente") as "MontoAbonoPendiente"
		   				  FROM :v_TablaMovimientos as MOVCTA
		   				 GROUP BY MOVCTA."IdCuentaInversion",MOVCTA."FechaEfectivaMovimiento"
		   			 ) AS MOVCTA
		   		  ON MOVCTA."IdCuentaInversion" 		= RF."IdCuentaInversion" 
		   		 AND MOVCTA."FechaEfectivaMovimiento"	= RF."Fecha"
		   LEFT JOIN :v_SaldoInicialFechaEfectiva as SI 
		   		  ON SI."IdCuentaInversion" = RF."IdCuentaInversion";
  
		--select 'v_SaldoEfectivo',* from :v_SaldoEfectivo;

		------------------------------------------------------
		--Retorna informacion del saldo de la cuenta inversion 
		------------------------------------------------------
		p_out_Saldo =
		SELECT 	 SF."IdCuentaInversion"
				,SF."CodigoCuentaInversion"
				,SF."IdCliente"
   				,SF."IdPortafolio"
   				,SF."Fecha" as "FechaSaldo"
   				,SF."IdMoneda"
   				,CAST(CASE 
   					WHEN :p_IncluyeCargosPendientes = 'S' AND :p_IncluyeAbonosPendientes = 'S' THEN (SF."Saldo" - SF."SaldoCargosPendientes" + SF."SaldoAbonosPendientes")
   					WHEN :p_IncluyeCargosPendientes = 'S' AND :p_IncluyeAbonosPendientes = 'N' THEN (SF."Saldo" - SF."SaldoCargosPendientes")
   					WHEN :p_IncluyeCargosPendientes = 'N' AND :p_IncluyeAbonosPendientes = 'S' THEN (SF."Saldo" + SF."SaldoAbonosPendientes")
   					WHEN :p_IncluyeCargosPendientes = 'N' AND :p_IncluyeAbonosPendientes = 'N' THEN (SF."Saldo")
   				  END as DECIMAL(26,8)) as "Saldo"   				
   				,CAST(SF."Saldo" as DECIMAL(26,8)) as "SaldoDisponible"
   				,CAST(SF."SaldoCargosPendientes" as DECIMAL(26,8)) as "SaldoCargosPendientes"
   				,CAST(SF."SaldoAbonosPendientes" as DECIMAL(26,8)) as "SaldoAbonosPendientes"
   				,"MaxOperacion" AS "UltimaOperacion"
   		  FROM :v_SaldoEfectivo AS SF;
		
		
		 
		 
	END;
END;
