SET SCHEMA "BSCL";
DROP PROCEDURE  "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosGestionDeDeposito";

CREATE PROCEDURE
-- 
--  Proposito: Validar 
--  Autor: NRM - BCS
--  Fecha Creacion: 31/03/2016
--     
"BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosGestionDeDeposito"(
	IN  p_Registros				TABLE ("IdInterno"					BIGINT,
									   "TipoEjecucion"				NVARCHAR(3),
									   "IdConcepto"					NVARCHAR(6),
									   "Descripcion"				NVARCHAR(520),
									   "Id" 						BIGINT, 
									   "Entidad" 					NVARCHAR(300),	
									   "IdDeposito"					BIGINT,
									   "IdEstadoDeposito"			NVARCHAR(3),
	 								   "IdDepositoDetalle"			BIGINT,									   
									   "IdEstadoDepositoDetalle"	NVARCHAR(3)),

	OUT p_out_Resultado			TABLE ("IdInterno"					BIGINT,
									   "Id" 						BIGINT,								   
	   								   "Codigo"						NVARCHAR(100),
	   								   "Resultado"					NVARCHAR(4))	--OK   = Cumple todos los requisitos,		
																					--ADVE = Una regla no cumple requisito pero NO detiene ejecución 
						                                                			--RECH = Alguna de lAS reglAS detienen la operacion
																					--RAUT = Requiere autorizacion
) 	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER   
	DEFAULT SCHEMA BSCL
AS
BEGIN

	v_MatrizValidaciones = 
	SELECT  DISTINCT
			 "IdInterno"
			,"TipoEjecucion"
			,"IdConcepto"
			,"Descripcion"
			,"Id"
			,"Entidad"
			,"IdDeposito"
			,"IdEstadoDeposito"
	 		,"IdDepositoDetalle"			
			,"IdEstadoDepositoDetalle"
			,CASE WHEN "Entidad" = 'TES.Deposito' THEN 1 ELSE 0 END as "DEPOSITO_NO_EXISTE"
			,CASE WHEN "Entidad" = 'TES.DepositoDetalle' THEN 1 ELSE 0 END as "DETALLE_DEPOSITO_NO_EXISTE"
			,CASE WHEN "Entidad" = 'TES.Deposito' THEN 1 ELSE 0 END as "DEPOSITO_LIQUIDADO"
			,CASE WHEN "Entidad" = 'TES.DepositoDetalle' THEN 1 ELSE 0 END as "DETALLE_DEPOSITO_LIQUIDADO"
			,CASE WHEN "Entidad" = 'TES.Deposito' THEN 1 ELSE 0 END as "DEPOSITO_ANULADO"
			,CASE WHEN "Entidad" = 'TES.DepositoDetalle' THEN 1 ELSE 0 END as "DETALLE_DEPOSITO_ANULADO"
			
			,CASE WHEN "TipoEjecucion" = 'ANU' THEN 1 ELSE 0 END as "NOMINA_DEPOSITADA"
			,CASE WHEN "TipoEjecucion" = 'LIQ' THEN 1 ELSE 0 END as "NOMINA_SIN_DEPOSITAR"
	  FROM :p_Registros as M;
	 
	v_Validaciones = 
	SELECT   "IdInterno"
			,"Id"
			,CASE WHEN "DEPOSITO_NO_EXISTE" = 1 AND COALESCE("IdDeposito",0) = 0 THEN 'RECH' ELSE 'OK' END AS "DEPOSITO_NO_EXISTE"
			,CASE WHEN "DETALLE_DEPOSITO_NO_EXISTE" = 1 AND COALESCE("IdDepositoDetalle",0) = 0 THEN 'RECH' ELSE 'OK' END AS "DETALLE_DEPOSITO_NO_EXISTE"
			
			,CASE WHEN "DEPOSITO_LIQUIDADO" = 1 AND ("IdEstadoDeposito" = 'LIQ') THEN 'RECH' ELSE 'OK' END AS "DEPOSITO_LIQUIDADO"
			,CASE WHEN "DETALLE_DEPOSITO_LIQUIDADO" = 1 AND ("IdEstadoDepositoDetalle" = 'LIQ') THEN 'RECH' ELSE 'OK' END AS "DETALLE_DEPOSITO_LIQUIDADO"
			
			,CASE WHEN "DEPOSITO_ANULADO" = 1 AND ("IdEstadoDeposito" = 'ANU') THEN 'RECH' ELSE 'OK' END AS "DEPOSITO_ANULADO"			
			,CASE WHEN "DETALLE_DEPOSITO_ANULADO" = 1 AND ("IdEstadoDepositoDetalle" = 'ANU') THEN 'RECH' ELSE 'OK' END AS "DETALLE_DEPOSITO_ANULADO"
			
			,CASE WHEN "NOMINA_DEPOSITADA" = 1 AND ("IdEstadoDeposito" = 'EDP') THEN 'RECH' ELSE 'OK' END AS "NOMINA_DEPOSITADA"
			,CASE WHEN "NOMINA_SIN_DEPOSITAR" = 1 AND ("IdEstadoDeposito" = 'ING') THEN 'RECH' ELSE 'OK' END AS "NOMINA_SIN_DEPOSITAR"
	  FROM :v_MatrizValidaciones;


	p_out_Resultado =
	 SELECT "IdInterno",
	 		"Id",
			"Codigo",
			"Resultado"
	   FROM (
			 SELECT "IdInterno",
			 		"Id",
			 		COALESCE(
			 			"1" ||"DEPOSITO_NO_EXISTE",
			 			"2" ||"DETALLE_DEPOSITO_NO_EXISTE",
			 			"3" ||"DEPOSITO_LIQUIDADO",
			 			"4" ||"DETALLE_DEPOSITO_LIQUIDADO",
			 			"5" ||"DEPOSITO_ANULADO",
			 			"6" ||"DETALLE_DEPOSITO_ANULADO",
			 			"7" ||"NOMINA_DEPOSITADA",
			 			"8" ||"NOMINA_SIN_DEPOSITAR"
			 		) AS "Resultado",
			 		COALESCE(
			 			"1" ||'DEPOSITO_NO_EXISTE',
			 			"2" ||'DETALLE_DEPOSITO_NO_EXISTE',
			 			"3" ||'DEPOSITO_LIQUIDADO',
			 			"4" ||'DETALLE_DEPOSITO_LIQUIDADO',
			 			"5" ||'DEPOSITO_ANULADO',
			 			"6" ||'DETALLE_DEPOSITO_ANULADO',
			 			"7" ||'NOMINA_DEPOSITADA',
			 			"8" ||'NOMINA_SIN_DEPOSITAR'
			 		) AS "Codigo"
			   FROM :v_Validaciones,"BSCL.Plataforma.BD::PLA.Identidad")
	  WHERE "Resultado" IS NOT NULL;	
END;
