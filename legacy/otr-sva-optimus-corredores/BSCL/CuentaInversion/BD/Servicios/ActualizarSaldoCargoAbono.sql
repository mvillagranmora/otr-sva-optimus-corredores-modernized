SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.CuentaInversion.BD.Servicios::ActualizarSaldoCargoAbono";

CREATE PROCEDURE 
--  
--  Proposito: Actualizar el saldo de todos los movimientos a partir de un ingreso, certificación o anulación
--  Autor: - BCS
--  Fecha Creacion: 12/02/2016
--  
"BSCL.CuentaInversion.BD.Servicios::ActualizarSaldoCargoAbono" (
	IN  p_IdEmpresa        				BIGINT,
    IN  p_IdUsuario         			BIGINT,
	IN  p_FechaMovimiento				NVARCHAR(10),
	IN  p_ActualizarTodosLosSaldos		NVARCHAR(1) DEFAULT 'N',
	IN  p_CuentaInversion				TABLE(
											"IdCuentaInversion" BIGINT
										) DEFAULT EMPTY
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
		DECLARE v_out_AdvertenciasContador,v_out_ErroresContador 	BIGINT;
		DECLARE v_IdEjecucion 										BIGINT;
		DECLARE v_TabCuentaInversion 								TABLE(
																		"IdCuentaInversion" BIGINT
																	);
		
		v_TabCuentaInversion = SELECT 0 AS "IdCuentaInversion" FROM DUMMY WHERE 1=2;
	
		IF (:p_ActualizarTodosLosSaldos = 'N') AND (NOT IS_EMPTY(:p_CuentaInversion)) THEN
			v_TabCuentaInversion =
				SELECT DISTINCT "IdCuentaInversion"
				FROM :p_CuentaInversion;
		END IF;
		
		IF (:p_ActualizarTodosLosSaldos = 'S') AND (IS_EMPTY(:p_CuentaInversion)) THEN
			v_TabCuentaInversion =
				SELECT "IdCuentaInversion"
				FROM "BSCL.CuentaInversion.BD::CTA.CuentaInversion"
				WHERE "IdEstado" = 'ACT';
		END IF;
		
		v_CargoAbonoMovimiento = 
		 SELECT  B."IdCuentaInversion"
		 		,B."IdCargoAbonoMovimiento"
		 		,B."IdTipoMovimiento"
		 		,(CASE WHEN B."IdTipoMovimiento" = 'MOVCAR' THEN -1*B."Monto" ELSE B."Monto" END) as "Monto"		 		
		 		,B."Saldo"
		 		,B."SaldoPorFechaMovimiento"
		 		,B."SaldoLibroInventario"
			    ,B."FechaSistema"
			    
			   
			    ,RANK() over (PARTITION BY B."IdCuentaInversion" ORDER BY B."FechaSistema",B."IdCargoAbonoMovimiento") as "RankFechaMovimiento"
			    
			    ,B."FechaEfectivaMovimiento"
			    ,RANK() over (PARTITION BY B."IdCuentaInversion" ORDER BY B."FechaEfectivaMovimiento",B."FechaCertificacion",B."IdCargoAbonoMovimiento") as "RankFechaEfectiva"
			    ,B."FechaCertificacion"
			    ,B."IdConcepto"
			    ,B."IdComprobante"
			    ,B."GeneroContraparteEnTesoreria"
			    ,B."IdTipoMovimientoAsociado"
			    ,B."IdMovimientoAsociado"
			    ,B."IdModulo"
	 	   FROM :v_TabCuentaInversion AS A
	  	  INNER JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" as B ON B."IdCuentaInversion" = A."IdCuentaInversion"
	 	  WHERE B."IdEstado" = 'CRT';
		
		------------------------------------
		--ACTUALIZAR SALDO FECHA MOVIMIENTO
		------------------------------------
		v_SaldoMovimiento = 
		WITH 
			 MovimientoAnterior as (
			 	SELECT MOV."IdCuentaInversion", MOV."SaldoPorFechaMovimiento" as "SaldoMovimientoAnterior",FMAX."RankMovimientoAnterior"
			 	  FROM :v_CargoAbonoMovimiento as MOV,			 	  
				  	  (SELECT "IdCuentaInversion", MAX("RankFechaMovimiento") as "RankMovimientoAnterior"
						 FROM :v_CargoAbonoMovimiento
						WHERE "FechaSistema" < :p_FechaMovimiento
						GROUP BY "IdCuentaInversion"
			 	 	   ) FMAX
			 	 WHERE FMAX."IdCuentaInversion" = MOV."IdCuentaInversion"
			 	   AND FMAX."RankMovimientoAnterior" = MOV."RankFechaMovimiento"
			 )		 
		SELECT  CBM."IdCargoAbonoMovimiento",CBM."IdCuentaInversion",CBM."RankFechaMovimiento",MOVANT."RankMovimientoAnterior",MOVANT."SaldoMovimientoAnterior",CBM."Monto"
			   ,COALESCE(MOVANT."SaldoMovimientoAnterior",0) + 
				COALESCE(SUM(CBM."Monto") over (PARTITION BY CBM."IdCuentaInversion" 
												
						 	                        
						 	                        ORDER BY CBM."FechaSistema", CBM."IdCargoAbonoMovimiento" ASC
						 	                        
						 	                         ROWS BETWEEN unbounded preceding and current row),0) as "SaldoMovimiento"
		  FROM :v_CargoAbonoMovimiento as CBM
		  LEFT JOIN MovimientoAnterior as MOVANT ON (MOVANT."IdCuentaInversion" = CBM."IdCuentaInversion")
		 WHERE CBM."FechaSistema" >= :p_FechaMovimiento;
				 
		

		--) Actualizar saldos movimientos
	    UPDATE CBM
		   SET CBM."SaldoPorFechaMovimiento" = SM."SaldoMovimiento"
		  FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" as CBM
		 INNER JOIN :v_SaldoMovimiento AS SM ON SM."IdCargoAbonoMovimiento" = CBM."IdCargoAbonoMovimiento";
		

		------------------------------------
		--ACTUALIZAR SALDO FECHA EFECTIVA
		------------------------------------
		v_SaldoEfectivo = 
		WITH 
			 MovimientoAnterior as (
			 	SELECT MOV."IdCuentaInversion", MOV."Saldo" as "SaldoEfectivoAnterior",FMAX."RankEfectivoAnterior"
			 	  FROM :v_CargoAbonoMovimiento as MOV,			 	  
				  	  (SELECT "IdCuentaInversion", MAX("RankFechaEfectiva") as "RankEfectivoAnterior"
						 FROM :v_CargoAbonoMovimiento
						WHERE "FechaEfectivaMovimiento" < :p_FechaMovimiento
						GROUP BY "IdCuentaInversion"
			 	 	   ) FMAX
			 	 WHERE FMAX."IdCuentaInversion" = MOV."IdCuentaInversion"
			 	   AND FMAX."RankEfectivoAnterior" = MOV."RankFechaEfectiva"
			 )
		SELECT CBM."IdCargoAbonoMovimiento",CBM."IdCuentaInversion",CBM."RankFechaEfectiva",MOVANT."RankEfectivoAnterior",MOVANT."SaldoEfectivoAnterior",CBM."Monto"
			   ,COALESCE(MOVANT."SaldoEfectivoAnterior",0) + 
				COALESCE(SUM(CBM."Monto") over (PARTITION BY CBM."IdCuentaInversion" 
						 	                        ORDER BY CBM."FechaEfectivaMovimiento", CBM."FechaCertificacion", CBM."IdCargoAbonoMovimiento" asc 
						 	                         ROWS BETWEEN unbounded preceding and current row),0) as "SaldoEfectivo"
		  FROM :v_CargoAbonoMovimiento as CBM
		  LEFT JOIN MovimientoAnterior as MOVANT ON (MOVANT."IdCuentaInversion" = CBM."IdCuentaInversion")
		 WHERE CBM."FechaEfectivaMovimiento" >= :p_FechaMovimiento;
		

		--) Actualizar saldos movimientos
	    UPDATE CBM
		   SET CBM."Saldo" = SM."SaldoEfectivo"
		  FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" as CBM
		 INNER JOIN :v_SaldoEfectivo AS SM ON SM."IdCargoAbonoMovimiento" = CBM."IdCargoAbonoMovimiento";
	   	
	END;
END;
