SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.CuentaInversion.BD.Servicios::ActualizarSaldoCargoAbonoCargaSebra";
GO
CREATE PROCEDURE 
--  
--  Proposito: Actualizar el saldo de todos los movimientos a partir de un ingreso, certificación o anulación
--  Autor: - BCS
--  Fecha Creacion: 12/02/2016
--  
"BSCL.CuentaInversion.BD.Servicios::ActualizarSaldoCargoAbonoCargaSebra" (
	IN  p_IdEmpresa         BIGINT,
    IN  p_IdUsuario         BIGINT,
	IN  p_FechaMovimiento	NVARCHAR(10),
	IN  p_IdEjecucion		BIGINT
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
		DECLARE v_out_AdvertenciasContador,v_out_ErroresContador BIGINT;

		
 UPDATE CBM
  SET CBM."SaldoPorFechaMovimiento" = SM."SaldoMovimiento"
  FROM  "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" as CBM
  INNER JOIN 
      (

		SELECT  CBM."SecuenciaMovimientoMigracion",CBM."IdCuentaInversion",CBM."RankFechaMovimiento",MOVANT."RankMovimientoAnterior",MOVANT."SaldoMovimientoAnterior",CBM."Monto"
			   ,COALESCE(MOVANT."SaldoMovimientoAnterior",0) + 
				COALESCE(SUM(CBM."Monto") over (PARTITION BY CBM."IdCuentaInversion" 
						 	                        ORDER BY CBM."FechaSistema", CBM."FechaCertificacion", CBM."SecuenciaMovimientoMigracion" asc 
						 	                         ROWS BETWEEN unbounded preceding and current row),0) as "SaldoMovimiento"
		  FROM (
						 SELECT  B."IdCuentaInversion",B."SecuenciaMovimientoMigracion",B."IdTipoMovimiento"
				 				,(CASE WHEN B."IdTipoMovimiento" = 'MOVCAR' THEN -1*B."Monto" ELSE B."Monto" END) as "Monto"		 		
				 				,B."Saldo",B."SaldoPorFechaMovimiento",B."SaldoLibroInventario",B."FechaSistema",RANK() over (PARTITION BY B."IdCuentaInversion" ORDER BY B."FechaSistema",B."FechaCertificacion",B."SecuenciaMovimientoMigracion") as "RankFechaMovimiento"
					    		,B."FechaEfectivaMovimiento",RANK() over (PARTITION BY B."IdCuentaInversion" ORDER BY B."FechaEfectivaMovimiento",B."FechaCertificacion",B."SecuenciaMovimientoMigracion") as "RankFechaEfectiva"
					    		,B."FechaCertificacion",B."IdConcepto",B."IdComprobante",B."GeneroContraparteEnTesoreria",B."IdTipoMovimientoAsociado"
					    		,B."IdMovimientoAsociado",B."IdModulo"
			 	   		   FROM "BSCL.Homologacion.BD::GBL.ActualizarSaldoCargoAbono_Entrada_CuentaInversion" AS A
			  	  				INNER JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" as B 
			  	  						ON B."IdCuentaInversion" = A."IdCuentaInversion"
			 	  		  WHERE A."IdEjecucion" = 1
			 	    		AND B."IdEstado" = 'CRT'	
			 	    		--AND  B."IdCuentaInversion" =34087
		  
		  	   )  as CBM
		  LEFT JOIN (
						SELECT MOV."IdCuentaInversion", MOV."SaldoPorFechaMovimiento" as "SaldoMovimientoAnterior",FMAX."RankMovimientoAnterior"
						  FROM (
								 SELECT  B."IdCuentaInversion",B."SecuenciaMovimientoMigracion",B."IdTipoMovimiento"
						 				,(CASE WHEN B."IdTipoMovimiento" = 'MOVCAR' THEN -1*B."Monto" ELSE B."Monto" END) as "Monto"		 		
						 				,B."Saldo",B."SaldoPorFechaMovimiento",B."SaldoLibroInventario",B."FechaSistema",RANK() over (PARTITION BY B."IdCuentaInversion" ORDER BY B."FechaSistema",B."FechaCertificacion",B."SecuenciaMovimientoMigracion") as "RankFechaMovimiento"
							    		,B."FechaEfectivaMovimiento",RANK() over (PARTITION BY B."IdCuentaInversion" ORDER BY B."FechaEfectivaMovimiento",B."FechaCertificacion",B."SecuenciaMovimientoMigracion") as "RankFechaEfectiva"
							    		,B."FechaCertificacion",B."IdConcepto",B."IdComprobante",B."GeneroContraparteEnTesoreria",B."IdTipoMovimientoAsociado"
							    		,B."IdMovimientoAsociado",B."IdModulo"
					 	   		   FROM "BSCL.Homologacion.BD::GBL.ActualizarSaldoCargoAbono_Entrada_CuentaInversion" AS A
					  	  				INNER JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" as B 
					  	  						ON B."IdCuentaInversion" = A."IdCuentaInversion"
					 	  		  WHERE A."IdEjecucion" = 1
					 	    		AND B."IdEstado" = 'CRT'
									AND  B."IdCuentaInversion" =34087
					 	    		
						       ) as MOV,
							 	  
						  	  (
						  	  	SELECT "IdCuentaInversion", MAX("RankFechaMovimiento") as "RankMovimientoAnterior"
								  FROM (
										 SELECT  B."IdCuentaInversion",B."SecuenciaMovimientoMigracion",B."IdTipoMovimiento"
								 				,(CASE WHEN B."IdTipoMovimiento" = 'MOVCAR' THEN -1*B."Monto" ELSE B."Monto" END) as "Monto"		 		
								 				,B."Saldo",B."SaldoPorFechaMovimiento",B."SaldoLibroInventario",B."FechaSistema",RANK() over (PARTITION BY B."IdCuentaInversion" ORDER BY B."FechaSistema",B."FechaCertificacion",B."SecuenciaMovimientoMigracion") as "RankFechaMovimiento"
									    		,B."FechaEfectivaMovimiento",RANK() over (PARTITION BY B."IdCuentaInversion" ORDER BY B."FechaEfectivaMovimiento",B."FechaCertificacion",B."SecuenciaMovimientoMigracion") as "RankFechaEfectiva"
									    		,B."FechaCertificacion",B."IdConcepto",B."IdComprobante",B."GeneroContraparteEnTesoreria",B."IdTipoMovimientoAsociado"
									    		,B."IdMovimientoAsociado",B."IdModulo"
							 	   		   FROM "BSCL.Homologacion.BD::GBL.ActualizarSaldoCargoAbono_Entrada_CuentaInversion" AS A
							  	  				INNER JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" as B 
							  	  						ON B."IdCuentaInversion" = A."IdCuentaInversion"
							 	  		  WHERE A."IdEjecucion" = 1
							 	    		AND B."IdEstado" = 'CRT'	
							 	    		--AND  B."IdCuentaInversion" =34087
								       ) 
								 WHERE "FechaSistema" <  '2017-04-01'
								 GROUP BY "IdCuentaInversion"
							  ) FMAX
							 	 WHERE FMAX."IdCuentaInversion" = MOV."IdCuentaInversion"
							 	   AND FMAX."RankMovimientoAnterior" = MOV."RankFechaMovimiento"
		            )  as MOVANT
		 		  ON (MOVANT."IdCuentaInversion" = CBM."IdCuentaInversion")
		  WHERE CBM."FechaSistema" >=  '2017-04-01'
				 
	)   AS SM 
	ON SM."SecuenciaMovimientoMigracion" = CBM."SecuenciaMovimientoMigracion";
		  

--	v_SaldoEfectivo = 
  UPDATE CBM
  SET CBM."Saldo" = SM."SaldoEfectivo" 
  FROM  "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento"  as CBM
  INNER JOIN 
      (
		SELECT CBM."SecuenciaMovimientoMigracion",CBM."IdCuentaInversion",CBM."RankFechaEfectiva",MOVANT."RankEfectivoAnterior",MOVANT."SaldoEfectivoAnterior",CBM."Monto"
			   ,COALESCE(MOVANT."SaldoEfectivoAnterior",0) + 
				COALESCE(SUM(CBM."Monto") over (PARTITION BY CBM."IdCuentaInversion" 
						 	                        ORDER BY CBM."FechaEfectivaMovimiento", CBM."FechaCertificacion", CBM."SecuenciaMovimientoMigracion" asc 
						 	                         ROWS BETWEEN unbounded preceding and current row),0) as "SaldoEfectivo"
						 	                       
		FROM (
						 SELECT  B."IdCuentaInversion",B."SecuenciaMovimientoMigracion",B."IdTipoMovimiento"
				 				,(CASE WHEN B."IdTipoMovimiento" = 'MOVCAR' THEN -1*B."Monto" ELSE B."Monto" END) as "Monto"		 		
				 				,B."Saldo",B."SaldoPorFechaMovimiento",B."SaldoLibroInventario",B."FechaSistema",RANK() over (PARTITION BY B."IdCuentaInversion" ORDER BY B."FechaSistema",B."FechaCertificacion",B."SecuenciaMovimientoMigracion") as "RankFechaMovimiento"
					    		,B."FechaEfectivaMovimiento",RANK() over (PARTITION BY B."IdCuentaInversion" ORDER BY B."FechaEfectivaMovimiento",B."FechaCertificacion",B."SecuenciaMovimientoMigracion") as "RankFechaEfectiva"
					    		,B."FechaCertificacion",B."IdConcepto",B."IdComprobante",B."GeneroContraparteEnTesoreria",B."IdTipoMovimientoAsociado"
					    		,B."IdMovimientoAsociado",B."IdModulo"
			 	   		   FROM "BSCL.Homologacion.BD::GBL.ActualizarSaldoCargoAbono_Entrada_CuentaInversion" AS A
			  	  				INNER JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" as B 
			  	  						ON B."IdCuentaInversion" = A."IdCuentaInversion"
			 	  		  WHERE A."IdEjecucion" = 1
			 	    		AND B."IdEstado" = 'CRT'	
			 	    		--AND  B."IdCuentaInversion" =34087
		  
		  	   )  as CBM
		  LEFT JOIN (
		 				SELECT MOV."IdCuentaInversion", MOV."Saldo" as "SaldoEfectivoAnterior",FMAX."RankEfectivoAnterior"
						FROM (
								 SELECT  B."IdCuentaInversion",B."SecuenciaMovimientoMigracion",B."IdTipoMovimiento"
						 				,(CASE WHEN B."IdTipoMovimiento" = 'MOVCAR' THEN -1*B."Monto" ELSE B."Monto" END) as "Monto"		 		
						 				,B."Saldo",B."SaldoPorFechaMovimiento",B."SaldoLibroInventario",B."FechaSistema",RANK() over (PARTITION BY B."IdCuentaInversion" ORDER BY B."FechaSistema",B."FechaCertificacion",B."SecuenciaMovimientoMigracion") as "RankFechaMovimiento"
							    		,B."FechaEfectivaMovimiento",RANK() over (PARTITION BY B."IdCuentaInversion" ORDER BY B."FechaEfectivaMovimiento",B."FechaCertificacion",B."SecuenciaMovimientoMigracion") as "RankFechaEfectiva"
							    		,B."FechaCertificacion",B."IdConcepto",B."IdComprobante",B."GeneroContraparteEnTesoreria",B."IdTipoMovimientoAsociado"
							    		,B."IdMovimientoAsociado",B."IdModulo"
					 	   		   FROM "BSCL.Homologacion.BD::GBL.ActualizarSaldoCargoAbono_Entrada_CuentaInversion" AS A
					  	  				INNER JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" as B 
					  	  						ON B."IdCuentaInversion" = A."IdCuentaInversion"
					 	  		  WHERE A."IdEjecucion" = 1
					 	    		AND B."IdEstado" = 'CRT'
									--AND  B."IdCuentaInversion" =34087
					 	    		
						       ) as MOV,
							 	  
						  	  (
						  	  	SELECT "IdCuentaInversion", MAX("RankFechaEfectiva") as "RankEfectivoAnterior"
								  FROM (
										 SELECT  B."IdCuentaInversion",B."SecuenciaMovimientoMigracion",B."IdTipoMovimiento"
								 				,(CASE WHEN B."IdTipoMovimiento" = 'MOVCAR' THEN -1*B."Monto" ELSE B."Monto" END) as "Monto"		 		
								 				,B."Saldo",B."SaldoPorFechaMovimiento",B."SaldoLibroInventario",B."FechaSistema",RANK() over (PARTITION BY B."IdCuentaInversion" ORDER BY B."FechaSistema",B."FechaCertificacion",B."SecuenciaMovimientoMigracion") as "RankFechaMovimiento"
									    		,B."FechaEfectivaMovimiento",RANK() over (PARTITION BY B."IdCuentaInversion" ORDER BY B."FechaEfectivaMovimiento",B."FechaCertificacion",B."SecuenciaMovimientoMigracion") as "RankFechaEfectiva"
									    		,B."FechaCertificacion",B."IdConcepto",B."IdComprobante",B."GeneroContraparteEnTesoreria",B."IdTipoMovimientoAsociado"
									    		,B."IdMovimientoAsociado",B."IdModulo"
							 	   		   FROM "BSCL.Homologacion.BD::GBL.ActualizarSaldoCargoAbono_Entrada_CuentaInversion" AS A
							  	  				INNER JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" as B 
							  	  						ON B."IdCuentaInversion" = A."IdCuentaInversion"
							 	  		  WHERE A."IdEjecucion" = 1
							 	    		AND B."IdEstado" = 'CRT'	
							 	    		--AND  B."IdCuentaInversion" =34087
								       ) 
								 WHERE "FechaEfectivaMovimiento" <  '2017-04-01'
								 GROUP BY "IdCuentaInversion"
							  ) FMAX
			 	 				WHERE FMAX."IdCuentaInversion" = MOV."IdCuentaInversion"
			 	   				  AND FMAX."RankEfectivoAnterior" = MOV."RankFechaEfectiva"
		            )  as MOVANT
		 		  ON (MOVANT."IdCuentaInversion" = CBM."IdCuentaInversion")
		 WHERE CBM."FechaEfectivaMovimiento" >=  '2017-04-01'
				 
	)   AS SM 
	ON SM."SecuenciaMovimientoMigracion" = CBM."SecuenciaMovimientoMigracion";	
   	
	END;
END;