SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.CuentaInversion.BD.Reglas::ServicioCumpleRequisitoCertificarCargoAbono";

CREATE PROCEDURE 
-- 
--  Proposito: Validar si el conjunto de movimientos recibidos de cargos o abonos pueden ser certificados
--  Autor: NRM - BCS
--  Fecha Creacion: 20/08/2015
--  
--
"BSCL.CuentaInversion.BD.Reglas::ServicioCumpleRequisitoCertificarCargoAbono" (
	IN  p_IdEmpresa         BIGINT,
    IN  p_IdUsuario         BIGINT,
    IN  p_IdModulo          NVARCHAR(3), 
	IN  p_FechaMovimiento   NVARCHAR(40),
	IN  p_IdSesion			INT
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
BEGIN
	DECLARE v_ErroresContador			BIGINT;
	DECLARE v_FechaDesde,v_FechaHasta	NVARCHAR(10);
	
	DECLARE v_Validaciones_V1  TABLE  ("IdInterno"										BIGINT,
									   "FECHA_SISTEMA_INVALIDA"							NVARCHAR(4),
									   "MOVIMIENTO_CARGO_ABONO_NO_EXISTE"				NVARCHAR(4),
									   "MOVIMIENTO_CARGO_ABONO_CERTIFICADO"				NVARCHAR(4),
									   "MOVIMIENTO_CARGO_ABONO_ANULADO"					NVARCHAR(4),
									   "MOVIMIENTO_ABONO_ES_PARTE_TRANSFERENCIA"		NVARCHAR(4));
	DECLARE v_Validaciones_V2  TABLE  ("IdInterno" 										BIGINT,
									   "SaldoFinal"										NVARCHAR(100),
									   "ERROR_CONSULTA_FLUJO_CAJA"						NVARCHAR(4),
									   "SALDO_CAJA_INSUFICIENTE"						NVARCHAR(4),
									   "CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO"			NVARCHAR(4));
	
	
	v_Validaciones_V2 = SELECT 0 as "IdInterno",0 as "SaldoFinal",'' as "ERROR_CONSULTA_FLUJO_CAJA",'' as "SALDO_CAJA_INSUFICIENTE",'' as "CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO"
						  FROM DUMMY WHERE 1=2; 
	
	
	v_MatrizValidaciones_v1 = 	
	 SELECT  CAC."IdSesion"
	        ,CAC."IdInterno"
	        ,CAC."IdCuentaInversion"
	        ,CAC."IdPortafolio"
	        ,CAC."IdMoneda"
	 		,CAC."IdCargoAbonoMovimiento"
			,CAC."IdCondicionDeLiquidacion"
			,CAC."PlazoEnDias"
			,CAC."FechaEfectivaMovimiento"
			,CAC."FechaSistema"
			,CAC."FechaIngresoRegistro"
			,CAC."FechaCalculada"
			,CAC."IdTipoMovimiento"
			,CAC."EsCargoAbonoPorTraspaso"
			,CAC."Monto"
			,CAC."IdEstado"
			,CAC."Informado"
			,IndPor."OperaLineaSobregiro"
			,1 as "FECHA_SISTEMA_INVALIDA"
			,1 as "NO_VIENEN_MOVIMIENTOS"
			--,1 as "MOVIMIENTO_CARGO_ABONO_INVALIDO"
			,1 as "MOVIMIENTO_CARGO_ABONO_NO_EXISTE"
			,1 as "MOVIMIENTO_CARGO_ABONO_CERTIFICADO"
			,1 as "MOVIMIENTO_CARGO_ABONO_ANULADO"
			,1 as "CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO"
			,1 as "SALDO_CAJA_INSUFICIENTE"
			,1 as "ERROR_CONSULTA_FLUJO_CAJA"
			,CASE WHEN CAC."IdTipoMovimiento" = 'MOVABN' THEN 1 ELSE 0 END as "MOVIMIENTO_ABONO_ES_PARTE_TRANSFERENCIA"
	   FROM "BSCL.CuentaInversion.BD::CTA.ServicioCumpleRequisitoCertificarCargoAbono_IN_CargosAbonosACertificar" AS CAC
	   LEFT JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarIndicadoresPortafolioCuentaInversion" AS IndPor 
	  		  ON IndPor."IdPortafolio" = CAC."IdPortafolio"
	  WHERE CAC."IdSesion" = :p_IdSesion;


	v_Validaciones_V1 =
	 SELECT  "IdInterno"
	 		,CASE WHEN "FECHA_SISTEMA_INVALIDA" = 1 AND ("BSCL.Plataforma.BD.Utilitarios::EsFecha"(:p_FechaMovimiento) = 'N') OR (:p_FechaMovimiento = '0001-01-01') THEN 'RECH' ELSE 'OK' END AS "FECHA_SISTEMA_INVALIDA"
	 		,CASE WHEN "MOVIMIENTO_CARGO_ABONO_NO_EXISTE" = 1 AND COALESCE("IdCargoAbonoMovimiento",0) = 0 THEN 'RECH' ELSE 'OK' END AS "MOVIMIENTO_CARGO_ABONO_NO_EXISTE"
	 		,CASE WHEN "MOVIMIENTO_CARGO_ABONO_CERTIFICADO" = 1 AND "IdEstado" = 'CRT' THEN 'RECH' ELSE 'OK' END AS "MOVIMIENTO_CARGO_ABONO_CERTIFICADO"
	 		,CASE WHEN "MOVIMIENTO_CARGO_ABONO_ANULADO" = 1 AND "IdEstado" = 'ANU' THEN 'RECH' ELSE 'OK' END AS "MOVIMIENTO_CARGO_ABONO_ANULADO"	 		
	 		,CASE WHEN "MOVIMIENTO_ABONO_ES_PARTE_TRANSFERENCIA" = 1 AND "EsCargoAbonoPorTraspaso" = 'S' AND "Informado" = 'S' THEN 'RECH' ELSE 'OK' END AS "MOVIMIENTO_ABONO_ES_PARTE_TRANSFERENCIA"
	  FROM :v_MatrizValidaciones_v1
	 WHERE "IdSesion" = 0;
	 
	
	---FLUJO CAJA
	v_PortafolioAConsultar = SELECT DISTINCT "IdPortafolio","IdMoneda" FROM :v_MatrizValidaciones_v1 WHERE "IdTipoMovimiento" = 'MOVCAR' AND "IdSesion" = 1;		
	SELECT MIN("FechaCalculada") INTO v_FechaDesde FROM :v_MatrizValidaciones_v1 WHERE "IdTipoMovimiento" = 'MOVCAR' AND "IdSesion" = 1;
	SELECT MAX("FechaCalculada") INTO v_FechaHasta FROM :v_MatrizValidaciones_v1 WHERE "IdTipoMovimiento" = 'MOVCAR' AND "IdSesion" = 1;
	
	IF NOT IS_EMPTY(:v_PortafolioAConsultar) THEN 					
		CALL "BSCL.Plataforma.BD.Servicios::SaldoFlujoCaja" (
		     p_IdEmpresa				=> :p_IdEmpresa				    
			,p_FechaDesde				=> :v_FechaDesde
		    ,p_FechaHasta				=> :v_FechaHasta
		    ,p_uso						=> 'CUENTA_INVERSION_CERTIFICACION'
		    ,p_PortafolioAConsultar		=> :v_PortafolioAConsultar
		    ,p_out_FlujoCaja			=> :v_FlujoCaja
		    ,p_out_FlujoCajaDetallado	=> :v_FlujoCajaDetallado
			,p_out_ErroresContador		=> :v_ErroresContador
			,p_out_ErroresDetalle		=> :v_ErroresDetalle
		);
		
		v_Validaciones_V2 =
		SELECT "IdInterno"
			   ,CAST("SaldoFinal" as NVARCHAR(100)) as "SaldoFinal"
			   ,CASE WHEN "ERROR_CONSULTA_FLUJO_CAJA" = 1 AND :v_ErroresContador > 0 THEN 'RECH' ELSE 'OK' END AS "ERROR_CONSULTA_FLUJO_CAJA"
		       ,CASE WHEN "SALDO_CAJA_INSUFICIENTE" = 1 AND :v_ErroresContador = 0 AND "SaldoFinal" < 0 AND "OperaLineaSobregiro" = 'N' THEN 'RECH' ELSE 'OK' END AS "SALDO_CAJA_INSUFICIENTE"
		       ,CASE WHEN "CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO" = 1 AND :v_ErroresContador = 0 AND "SaldoFinal" < 0 AND "OperaLineaSobregiro" = 'N' THEN 'RECH' ELSE 'OK' END AS "CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO"			       
		  FROM (SELECT  MTZ."IdInterno"
					   ,MTZ."OperaLineaSobregiro"
					   ,MTZ."CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO"
					   ,MTZ."SALDO_CAJA_INSUFICIENTE"
					   ,MTZ."ERROR_CONSULTA_FLUJO_CAJA"
					   ,FC."Saldo" - SUM(MTZ."Monto") over (partition by MTZ."IdPortafolio",MTZ."IdMoneda" order by MTZ."IdInterno" asc 
								 	                     		 rows between unbounded preceding and current row) as "SaldoFinal"
				  FROM :v_FlujoCaja as FC
				 INNER JOIN :v_MatrizValidaciones_v1 as MTZ 
				     	 ON MTZ."IdPortafolio" 		= FC."IdPortafolio" 
				     	AND MTZ."IdMoneda" 			= FC."IdMoneda"
						AND MTZ."FechaCalculada"  	= FC."FechaSaldo"
						AND MTZ."IdSesion" = 1
			   ) as SF;	
	END IF;	
	
	------RESULTADO	
	v_Resultado = 
	 SELECT "IdInterno","Codigo","Resultado",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2"
	   FROM (
			 SELECT "IdInterno",
			 		COALESCE(			 			
			 			 --"1" ||'FECHA_SISTEMA_INVALIDA'
						 "2" ||'MOVIMIENTO_CARGO_ABONO_NO_EXISTE'
						,"3" ||'MOVIMIENTO_CARGO_ABONO_CERTIFICADO'
						,"4" ||'MOVIMIENTO_CARGO_ABONO_ANULADO'
						,"5" ||'MOVIMIENTO_ABONO_ES_PARTE_TRANSFERENCIA'
			 		) AS "Codigo",
			 		COALESCE(			 			
			 			 --"1" ||"FECHA_SISTEMA_INVALIDA"
						 "2" ||"MOVIMIENTO_CARGO_ABONO_NO_EXISTE"
						,"3" ||"MOVIMIENTO_CARGO_ABONO_CERTIFICADO"
						,"4" ||"MOVIMIENTO_CARGO_ABONO_ANULADO"
						,"5" ||"MOVIMIENTO_ABONO_ES_PARTE_TRANSFERENCIA"
			 		) AS "Resultado"
			   FROM :v_Validaciones_V1,"BSCL.Plataforma.BD::PLA.Identidad")
	  WHERE "Resultado" IS NOT NULL			   
	  UNION ALL 
	 SELECT "IdInterno","Codigo","Resultado",'' as "CodigoDato1","SaldoFinal" as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2"
	   FROM (
			 SELECT "IdInterno","SaldoFinal",
			 		COALESCE(			 			
			 			 "1" ||'CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO'
						,"2" ||'SALDO_CAJA_INSUFICIENTE'
						,"3" ||'ERROR_CONSULTA_FLUJO_CAJA'
			 		) AS "Codigo",
			 		COALESCE(			 			
			 			 "1" ||"CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO"
						,"2" ||"SALDO_CAJA_INSUFICIENTE"
						,"3" ||"ERROR_CONSULTA_FLUJO_CAJA"
			 		) AS "Resultado"
			   FROM :v_Validaciones_V2,"BSCL.Plataforma.BD::PLA.Identidad")			   
	  WHERE "Resultado" IS NOT NULL;
	
	
	
	INSERT INTO "BSCL.CuentaInversion.BD::CTA.ServicioCumpleRequisitoCertificarCargoAbono_OUT_Resultado"
	 SELECT  DISTINCT R."IdInterno"
		   	,R."Codigo"
	 		,R."Resultado"
	 		,M."IdSesion"
	 		,CASE WHEN "Codigo" = 'FECHA_SISTEMA_INVALIDA' THEN 'Fecha'
	 		      WHEN "Codigo" = 'MOVIMIENTO_CARGO_ABONO_NO_EXISTE' THEN 'IdCargoAbonoMovimiento'
	 			  WHEN "Codigo" = 'MOVIMIENTO_CARGO_ABONO_CERTIFICADO' THEN 'IdCargoAbonoMovimiento'
	 			  WHEN "Codigo" = 'MOVIMIENTO_CARGO_ABONO_ANULADO' THEN 'IdCargoAbonoMovimiento'
	 			  WHEN "Codigo" = 'MOVIMIENTO_ABONO_ES_PARTE_TRANSFERENCIA' THEN 'IdCargoAbonoMovimiento'
	 			  WHEN "Codigo" = 'SALDO_CAJA_INSUFICIENTE' THEN 'SALDO_CAJA_INSUFICIENTE'
	 			  ELSE ''
	 		  END "CodigoDato1"
	 		,CASE WHEN "Codigo" = 'FECHA_SISTEMA_INVALIDA' THEN :p_FechaMovimiento
	 			  WHEN "Codigo" = 'MOVIMIENTO_CARGO_ABONO_NO_EXISTE' THEN CAST("IdCargoAbonoMovimiento" as NVARCHAR)
	 			  WHEN "Codigo" = 'MOVIMIENTO_CARGO_ABONO_CERTIFICADO' THEN CAST("IdCargoAbonoMovimiento" as NVARCHAR)
	 			  WHEN "Codigo" = 'MOVIMIENTO_CARGO_ABONO_ANULADO' THEN CAST("IdCargoAbonoMovimiento" as NVARCHAR)
	 			  WHEN "Codigo" = 'MOVIMIENTO_ABONO_ES_PARTE_TRANSFERENCIA' THEN CAST("IdCargoAbonoMovimiento" as NVARCHAR)
	 			  WHEN "Codigo" = 'SALDO_CAJA_INSUFICIENTE' THEN "ValorDato1"
	 			  ELSE ''
	 		  END "ValorDato1"
	 		,"CodigoDato2"
	 		,"ValorDato2"
	   FROM :v_Resultado as R
	  INNER JOIN :v_MatrizValidaciones_v1 as M ON M."IdInterno" = R."IdInterno" AND M."IdSesion" = :p_IdSesion;
END;
