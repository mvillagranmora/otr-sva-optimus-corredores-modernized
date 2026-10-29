SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.CuentaInversion.BD.Servicios::ActualizarSaldoLibroInventario";

CREATE PROCEDURE 
--  
--  Proposito: Actualizar el saldo del libro inventario según los calces realizados
--  Autor: - BCS
--  Fecha Creacion: 12/02/2016
--  
"BSCL.CuentaInversion.BD.Servicios::ActualizarSaldoLibroInventario" (
	IN  p_FechaMovimiento	NVARCHAR(10),
	IN  p_IdEjecucion		BIGINT,
	OUT p_out_Count			BIGINT
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
		DECLARE v_CuentaInversionAProcesar TABLE ("IdCuentaInversion" BIGINT);
		DECLARE indexCargo,indexAbono,indexMovCargo,indexMovAbono,indexCalce,indexPreferencia	BIGINT := 1;
		DECLARE idCtaInvCargo,idCtaInvAbono,idCargoMov,idAbonoMov,idCargo,idAbono				BIGINT ARRAY;
		DECLARE movAsociadoCargo,movAsociadoAbono,idPreferencia									BIGINT ARRAY;
		DECLARE calceCtaInv,calceIdCargo,calceIdAbono,calceIdPreferencia						BIGINT ARRAY;
		DECLARE saldoInvCargo,saldoInvAbono,calceMontoCargo,calceMonto,calceSaldo				DECIMAL(26,8) ARRAY;
		DECLARE conceptoAbono,conceptoCargo,idConceptoCargo,idConceptoAbono						NVARCHAR(6) ARRAY;


		p_out_Count := 0;
		--INSERT INTO "BSCL.CuentaInversion.BD::CTA.LogProcesos" VALUES(:p_IdEjecucion,'ActualizarSaldoLibroInventario','Inicio Proceso');
		
		v_CuentaInversionAProcesar = 
		SELECT "IdCuentaInversion" FROM "BSCL.CuentaInversion.BD::CTA.ActualizarSaldoLibroInventario_IN_CuentaInversion" WHERE "IdEjecucion" = :p_IdEjecucion;
				
		
		v_CargoAbonoMovimiento = 
		 SELECT  B."IdCuentaInversion"
		 		,B."IdCargoAbonoMovimiento"
		 		,B."IdTipoMovimiento"
		 		,(CASE WHEN B."IdTipoMovimiento" = 'MOVCAR' THEN -1*B."Monto" ELSE B."Monto" END) as "Monto"		 		
		 		,B."Saldo"
		 		,B."SaldoPorFechaMovimiento"
		 		,B."SaldoLibroInventario"
			    ,B."FechaSistema"
			    ,RANK() over (PARTITION BY B."IdCuentaInversion" ORDER BY B."FechaSistema",B."FechaCertificacion") as "RankFechaMovimiento"
			    ,B."FechaEfectivaMovimiento"
			    ,RANK() over (PARTITION BY B."IdCuentaInversion" ORDER BY B."FechaSistema",B."FechaCertificacion") as "RankFechaEfectiva"
			    ,B."FechaCertificacion"
			    ,B."IdConcepto"
			    ,B."IdComprobante"
			    ,B."GeneroContraparteEnTesoreria"
			    ,B."IdTipoMovimientoAsociado"
			    ,B."IdMovimientoAsociado"
			    ,B."IdModulo"
	 	   FROM :v_CuentaInversionAProcesar AS A
	  	  INNER JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" as B ON B."IdCuentaInversion" = A."IdCuentaInversion"
	 	  WHERE B."IdEstado" = 'CRT';	 	  
		
		------------------------------------
		--ACTUALIZAR SALDO LIBRO INVENTARIO
		------------------------------------
		v_movimientos =
		SELECT  DISTINCT
				CAM."IdCuentaInversion",
				CAM."IdCargoAbonoMovimiento",
				CAM."IdTipoMovimiento",
				CAM."Monto",
				CAM."SaldoLibroInventario",
				CAM."IdConcepto",
				CO."IdConcepto" AS "IdConceptoMovimiento",
				CASE WHEN CAM."IdMovimientoAsociado" > 0 THEN CAM."IdMovimientoAsociado"
					 WHEN COALESCE(CO."IdIngresoEgreso",0) > 0 THEN CO."IdIngresoEgreso"	 	   
					 ELSE 0
				 END as "IdMovimientoAsociado",
				CASE WHEN CAM."IdMovimientoAsociado" > 0 THEN CAM."IdTipoMovimientoAsociado"
					 WHEN COALESCE(CO."IdIngresoEgreso",0) > 0 THEN COALESCE(CO."IdTipoMovimientoIngresoEgreso",'')
					 ELSE ''
				 END as "IdTipoMovimientoAsociado",
				CASE WHEN CAM."IdMovimientoAsociado" > 0 THEN COALESCE(IE."Monto",0)
					 WHEN COALESCE(CO."IdIngresoEgreso",0) > 0 AND CAM."Monto" = CO."MontoIngresoEgreso" THEN COALESCE(CO."MontoIngresoEgreso",0)
					 ELSE 0
				 END as "MontoIngresoEgreso",
				 CAM."GeneroContraparteEnTesoreria"
		  FROM :v_CargoAbonoMovimiento as CAM
		  LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteOrigen" as CO 
		  		 ON CO."IdComprobante" = CAM."IdComprobante" AND CAM."IdConcepto" = CO."IdConcepto"
		  		AND CAM."IdModulo" = 'TES' AND CO."EsIngresoAutomatico" = 'S'
		  LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgreso" as IE
		  		 ON IE."IdIngresoEgreso" = CAM."IdMovimientoAsociado" AND IE."EsIngresoAutomatico" = 'S'
		 WHERE CAM."SaldoLibroInventario" > 0;
		

		v_MovCargos	= 
		 SELECT DISTINCT "IdCuentaInversion","IdCargoAbonoMovimiento","Monto","SaldoLibroInventario","IdConcepto" 
		   FROM :v_movimientos 
		  WHERE "IdTipoMovimiento" = 'MOVCAR';
		  
		v_MovAbonos	= 
		 SELECT DISTINCT "IdCuentaInversion","IdCargoAbonoMovimiento","Monto","SaldoLibroInventario","IdConcepto" 
		   FROM :v_movimientos 
		  WHERE "IdTipoMovimiento" = 'MOVABN';
			
		v_saldosCargos	= 
		 SELECT 'v_saldos_ca',* 
		   FROM :v_movimientos 
		  WHERE "IdTipoMovimiento" = 'MOVCAR';
		  
		v_saldosAbonos 	= 
		 SELECT 'v_saldos_AB',* 
		   FROM :v_movimientos 
		  WHERE "IdTipoMovimiento" = 'MOVABN';
		
		v_preferenciasCalce =	
		SELECT DISTINCT Mov."IdConcepto",PCLI."EsPagadoCon",PCLI."Preferencia",PCLI."IdPreferenciaCalceLibroInventario"
		  FROM :v_saldosCargos as Mov
		 INNER JOIN "BSCL.CuentaInversion.BD::CTA.PreferenciaCalceLibroInventario" as PCLI 
		  		 ON PCLI."IdConcepto" = Mov."IdConcepto"; -- AND Mov."IdMovimientoAsociado" = 0;
			
		
		--Cargo
		idCtaInvCargo 		= ARRAY_AGG(:v_MovCargos."IdCuentaInversion" order by "IdCuentaInversion");
		idCargo  			= ARRAY_AGG(:v_MovCargos."IdCargoAbonoMovimiento" order by "IdCuentaInversion","IdCargoAbonoMovimiento");
		saldoInvCargo		= ARRAY_AGG(:v_MovCargos."SaldoLibroInventario" order by "IdCuentaInversion","IdCargoAbonoMovimiento");
		idConceptoCargo		= ARRAY_AGG(:v_MovCargos."IdConcepto" order by "IdCuentaInversion","IdCargoAbonoMovimiento");
		--Abono
		idCtaInvAbono 		= ARRAY_AGG(:v_MovAbonos."IdCuentaInversion" order by "IdCuentaInversion");
		idAbono  			= ARRAY_AGG(:v_MovAbonos."IdCargoAbonoMovimiento" order by "IdCuentaInversion","IdCargoAbonoMovimiento");
		saldoInvAbono		= ARRAY_AGG(:v_MovAbonos."SaldoLibroInventario" order by "IdCuentaInversion","IdCargoAbonoMovimiento");
		idConceptoAbono		= ARRAY_AGG(:v_MovAbonos."IdConcepto" order by "IdCuentaInversion","IdCargoAbonoMovimiento");
		--Movimientos
		idCargoMov			= ARRAY_AGG(:v_saldosCargos."IdCargoAbonoMovimiento" order by "IdCuentaInversion","IdCargoAbonoMovimiento","IdMovimientoAsociado");
		movAsociadoCargo	= ARRAY_AGG(:v_saldosCargos."IdMovimientoAsociado" order by "IdCuentaInversion","IdCargoAbonoMovimiento","IdMovimientoAsociado");		
		idAbonoMov			= ARRAY_AGG(:v_saldosAbonos."IdCargoAbonoMovimiento" order by "IdCuentaInversion","IdCargoAbonoMovimiento","IdMovimientoAsociado");
		movAsociadoAbono	= ARRAY_AGG(:v_saldosAbonos."IdMovimientoAsociado" order by "IdCuentaInversion","IdCargoAbonoMovimiento","IdMovimientoAsociado");		
		--Preferencias
		conceptoCargo		= ARRAY_AGG(:v_preferenciasCalce."IdConcepto" order by "IdConcepto","Preferencia");
		conceptoAbono		= ARRAY_AGG(:v_preferenciasCalce."EsPagadoCon" order by "IdConcepto","Preferencia");
		idPreferencia		= ARRAY_AGG(:v_preferenciasCalce."IdPreferenciaCalceLibroInventario" order by "IdConcepto","Preferencia");
		
		FOR indexCargo IN 1 .. CARDINALITY(:idCargo) DO
			FOR indexMovCargo IN 1 .. CARDINALITY(:movAsociadoCargo) DO
				IF (:idCargo[:indexCargo] = :idCargoMov[:indexMovCargo] AND :saldoInvCargo[:indexCargo] > 0) THEN
		
					FOR indexPreferencia IN 1 .. CARDINALITY(:conceptoCargo) DO
						IF (:idConceptoCargo[:indexCargo] = :conceptoCargo[:indexPreferencia]) THEN
												
							FOR indexAbono IN 1 .. CARDINALITY(:idAbono) DO
								IF (:idCtaInvCargo[:indexCargo] = :idCtaInvAbono[:indexAbono] AND :saldoInvAbono[:indexAbono] > 0) THEN									
									FOR indexMovAbono IN 1 .. CARDINALITY(:movAsociadoAbono) DO
										IF (:idAbono[:indexAbono] = :idAbonoMov[:indexMovAbono] AND 
											:movAsociadoCargo[:indexMovCargo] = :movAsociadoAbono[:indexMovAbono]) THEN
																					
											IF (:saldoInvAbono[:indexAbono] = 0 OR :saldoInvCargo[:indexCargo] = 0) THEN 
												CONTINUE;
											END IF;
											
											IF ((:movAsociadoCargo[:indexMovCargo] > 0) OR
												(:movAsociadoCargo[:indexMovCargo] = 0 AND :idConceptoAbono[:indexAbono] = :conceptoAbono[:indexPreferencia])) THEN
				   												   
												calceIdCargo[:indexCalce]		= :idCargo[:indexCargo];
												calceIdAbono[:indexCalce]		= :idAbono[:indexAbono];								
												calceIdPreferencia[:indexCalce]	= :idPreferencia[:indexPreferencia];
												
												IF (:saldoInvCargo[:indexCargo] < :saldoInvAbono[:indexAbono]) THEN
													calceMonto[:indexCalce] 	= :saldoInvCargo[:indexCargo];
													calceSaldo[:indexCalce]		= 0;
													saldoInvAbono[:indexAbono]	= :saldoInvAbono[:indexAbono] - :saldoInvCargo[:indexCargo];
													saldoInvCargo[:indexCargo]	= 0;
													indexCalce := indexCalce + 1;
													CONTINUE;
												END IF;
												
												IF (:saldoInvCargo[:indexCargo] >= :saldoInvAbono[:indexAbono]) THEN
													calceMonto[:indexCalce] 	= :saldoInvAbono[:indexAbono];
													calceSaldo[:indexCalce]		= :saldoInvCargo[:indexCargo] - :saldoInvAbono[:indexAbono];
													saldoInvCargo[:indexCargo]	= :saldoInvCargo[:indexCargo] - :saldoInvAbono[:indexAbono];
													saldoInvAbono[:indexAbono]	= 0;
													indexCalce := indexCalce + 1;
												END IF;																					   
											END IF; --FIN LOGICA CALCE
											
										END IF;	-- IF IDABONO/MOVIMENTO
									END FOR; 	-- FOR MOVIMIENTO ABONO
								END IF;			-- SOLO RECORRE SI ES LA MISMA CTA INV Y SI EXISTE SALDO
							END FOR; 			-- FOR ABONO
							
						END IF;	--IF CONCEPTOCARGO/PREFERENCIA
					END FOR; 	--FOR PREFERENCIA
					
				END IF;	-- IF IDCARGO/MOVIMIENTO
			END FOR; 	-- FOR MOVIMIENTO CARGO
		END FOR; 		-- FOR CARGO
		rst = UNNEST(:calceIdCargo,:calceIdAbono,:calceMonto,:calceSaldo,:calceIdPreferencia) as ("CalceIdCargo","CalceIdAbono","CalceMonto","CalceSaldo","CalceIdPreferencia");
		--SELECT 'rst', * FROM :rst ORDER BY "CalceIdCargo","CalceIdAbono"; --BORRAR
		
		
		--LIBRO INVENTARIO DETALLE
		INSERT INTO "BSCL.CuentaInversion.BD::CTA.LibroInventarioDeSaldos" (
	   		 "IdLibroInventarioDeSaldos"
			,"IdCuentaInversion"
			,"IdCargoAbonoMovimiento"
			,"Monto"
			,"FechaCalce"
			,"IdPreferenciaCalceLibroInventario"
			,"IdCargoAbonoMovimientoCalce"
			,"MontoCalce"
			,"Saldo"
			,"IdEstado"
	   	)
	   	SELECT 	"BSCL.CuentaInversion.BD.Secuencias::LibroInventarioDeSaldos".NEXTVAL,
	   			SCA."IdCuentaInversion",
	   			R."CalceIdCargo",
	   			SCA."Monto",
	   			CURRENT_UTCTIMESTAMP,
	   			R."CalceIdPreferencia",
	   			R."CalceIdAbono",
	   			R."CalceMonto",
	   			R."CalceSaldo",
	   			'-' as "IdEstado"
	   	  FROM :rst as R
	   	 INNER JOIN :v_MovCargos as SCA ON SCA."IdCargoAbonoMovimiento" = R."CalceIdCargo";
		
		v_MovimientosVigentes =
		SELECT T."IdCuentaInversion",
			   T."IdMax",
			   'VIG' as "IdEstado"
		  FROM (SELECT LIS."IdCuentaInversion",
					   MAX(LIS."IdLibroInventarioDeSaldos") over (partition by LIS."IdCuentaInversion",LIS."IdCargoAbonoMovimiento") as "IdMax"
				  FROM :v_CuentaInversionAProcesar as CI 
				 INNER JOIN "BSCL.CuentaInversion.BD::CTA.LibroInventarioDeSaldos" as LIS
				 	ON LIS."IdCuentaInversion" = CI."IdCuentaInversion"				 
			   ) as T
		 GROUP BY T."IdCuentaInversion", T."IdMax";
		
		UPDATE LIS
		   SET LIS."IdEstado" = COALESCE(MV."IdEstado",'MOD')
		  FROM "BSCL.CuentaInversion.BD::CTA.LibroInventarioDeSaldos" as LIS 
		  LEFT JOIN :v_MovimientosVigentes as MV
		 	ON MV."IdCuentaInversion" = LIS."IdCuentaInversion"
		   AND MV."IdMax" = LIS."IdLibroInventarioDeSaldos"
		 WHERE LIS."IdEstado" <> 'ANU';
		
		
		--CARGO ABONO MOVIMIENTO
		v_SaldoLibroInventario = 
		SELECT Mov."IdCargoAbonoMovimiento", ("SaldoLibroInventario" - "Calce") as "SaldoLibroInventario"
		  FROM :v_movimientos as Mov
		 INNER JOIN (SELECT "CalceIdCargo" as "ID", SUM("CalceMonto") as "Calce" FROM :rst GROUP BY "CalceIdCargo" UNION ALL
		 			 SELECT "CalceIdAbono" as "ID", SUM("CalceMonto") as "Calce" FROM :rst GROUP BY "CalceIdAbono") as CA
		 		 ON CA."ID"= Mov."IdCargoAbonoMovimiento";

		UPDATE CAM
		   SET CAM."SaldoLibroInventario" = S."SaldoLibroInventario"
		  FROM :v_SaldoLibroInventario as S
		 INNER JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" as CAM ON CAM."IdCargoAbonoMovimiento" = S."IdCargoAbonoMovimiento";
		 
		
		SELECT COUNT(CAM."IdCargoAbonoMovimiento") INTO p_out_Count
		  FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" as CAM 
		 INNER JOIN :v_SaldoLibroInventario as SLI ON SLI."IdCargoAbonoMovimiento" = CAM."IdCargoAbonoMovimiento";
		
		------------------------------------
		--FIN DEL PROCESO
		------------------------------------		
		DELETE FROM "BSCL.CuentaInversion.BD::CTA.ActualizarSaldoLibroInventario_IN_CuentaInversion" WHERE "IdEjecucion" = :p_IdEjecucion;
		--INSERT INTO "BSCL.CuentaInversion.BD::CTA.LogProcesos" VALUES(:p_IdEjecucion,'ActualizarSaldoLibroInventario','Fin Proceso '||p_out_Count);
	END;
END;
