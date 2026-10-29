SET SCHEMA "BSCL";
DROP PROCEDURE  "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosEdicionMultiple";

CREATE PROCEDURE
-- 
--  Proposito: Validar si movimientos cumplen con los requisitos solicitados
--  Autor: NRM - BCS
--  Fecha Creacion: 26/01/2016
--     
"BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosEdicionMultiple"(
	-- Parametros entrada
	IN p_FechaMovimiento			NVARCHAR(10),
    IN p_RegistrosAProcesar 		TABLE ("IdInterno"		BIGINT,
    								   	   "Id" 			BIGINT, 
	    								   "Entidad" 		NVARCHAR(300), 
	    								   "BLO_DES" 		NVARCHAR(3),	--Bloquea o desbloquea registro
	    								   "AGRUPA" 		NVARCHAR(3),	--Agrupa o no resgistros
	    								   "NETEA" 			NVARCHAR(3)),	--Netea o no resgistros
	OUT p_out_ResultadoRequisitos	TABLE ("IdInterno"		BIGINT,
										   "Codigo"			NVARCHAR(100),
										   "Resultado"		NVARCHAR(4),
										   "CodigoDato1"	NVARCHAR(100),
										   "ValorDato1"		NVARCHAR(520),
										   "CodigoDato2"	NVARCHAR(100),
										   "ValorDato2"		NVARCHAR(520)) DEFAULT EMPTY
										   									--OK	= Cumple todos los requisitos,
																			--ADVE	= Alguna de lAS reglas posee advertencias, no detiene la operacion
																			--RAUT 	= Alguna de las reglas requiere autorizacion	 								  	   
					                                                   		--RECH	= Alguna de las reglas detienen la operacion
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER   
	DEFAULT SCHEMA BSCL
AS
BEGIN
	
	v_MatrizValidaciones_v1 = 
	SELECT  "IdInterno"
	   	   ,"Id"
		   ,"Entidad"
		   ,"BLO_DES"
		   ,"AGRUPA"
		   ,"NETEA"
		   ,CASE WHEN "Entidad" = 'TES.IngresoEgreso' THEN 1 ELSE 0 END as "MOVIMIENTO_TESORERIA_NO_EXISTE"
		   ,CASE WHEN "Entidad" = 'TES.IngresoEgreso' THEN 1 ELSE 0 END as "MOVIMIENTO_TESORERIA_EMITIDO"
		   ,CASE WHEN "Entidad" = 'TES.IngresoEgreso' THEN 1 ELSE 0 END as "MOVIMIENTO_TESORERIA_ANULADO"
		   ,CASE WHEN "Entidad" = 'TES.IngresoEgreso' THEN 1 ELSE 0 END as "MOVIMIENTO_TESORERIA_DESBLOQUEADO"
		   ,CASE WHEN "Entidad" = 'TES.IngresoEgreso' THEN 1 ELSE 0 END as "MOVIMIENTO_TESORERIA_BLOQUEADO"
		   ,CASE WHEN "Entidad" = 'TES.Comprobante' THEN 1 ELSE 0 END as "COMPROBANTE_TESORERIA_NO_EXISTE"
		   ,CASE WHEN "Entidad" = 'TES.Comprobante' THEN 1 ELSE 0 END as "COMPROBANTE_TESORERIA_LIQUIDADO"
		   ,CASE WHEN "Entidad" = 'TES.Comprobante' THEN 1 ELSE 0 END as "COMPROBANTE_TESORERIA_DESBLOQUEADO"
	  FROM :p_RegistrosAProcesar;
	
	
	v_Validaciones_IngresoEgreso =
	SELECT  "IdInterno","Id"
		   ,CASE WHEN "MOVIMIENTO_TESORERIA_NO_EXISTE" = 1 AND COALESCE(IE."IdIngresoEgreso",0) = 0 THEN 'RECH' ELSE 'OK' END AS "MOVIMIENTO_TESORERIA_NO_EXISTE"
		   ,CASE WHEN "MOVIMIENTO_TESORERIA_EMITIDO" = 1 AND IE."IdEstado" = 'EMI' AND ("BLO_DES" = 'BLO' OR "AGRUPA" = 'S' OR "NETEA" = 'S') THEN 'RECH' ELSE 'OK' END as "MOVIMIENTO_TESORERIA_EMITIDO"
		   ,CASE WHEN "MOVIMIENTO_TESORERIA_ANULADO" = 1 AND IE."IdEstado" = 'ANU' AND ("BLO_DES" = 'BLO' OR "AGRUPA" = 'S' OR "NETEA" = 'S') THEN 'RECH' ELSE 'OK' END as "MOVIMIENTO_TESORERIA_ANULADO"
		   ,CASE WHEN "MOVIMIENTO_TESORERIA_DESBLOQUEADO" = 1 AND IE."IdEstado" <> 'BLO' AND "BLO_DES" = 'ING' THEN 'RECH' ELSE 'OK' END as "MOVIMIENTO_TESORERIA_DESBLOQUEADO"
		   ,CASE WHEN "MOVIMIENTO_TESORERIA_BLOQUEADO" = 1 AND IE."IdEstado" = 'BLO' AND ("AGRUPA" = 'S' OR "NETEA" = 'S') THEN 'RECH' ELSE 'OK' END as "MOVIMIENTO_TESORERIA_BLOQUEADO"
	  FROM :v_MatrizValidaciones_v1 as  MTZ
	  LEFT JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" as IE ON IE."IdIngresoEgreso" = MTZ."Id"
	 WHERE MTZ."Entidad" = 'TES.IngresoEgreso';
	
	
	v_Validaciones_Comprobante =
	SELECT  "IdInterno","Id"
		   ,CASE WHEN "COMPROBANTE_TESORERIA_NO_EXISTE" = 1 AND COALESCE(C."IdComprobante",0) = 0 THEN 'RECH' ELSE 'OK' END as "COMPROBANTE_TESORERIA_NO_EXISTE"
		   ,CASE WHEN "COMPROBANTE_TESORERIA_LIQUIDADO" = 1 AND "BLO_DES" = 'BLO' AND C."IdEstado" = 'LIQ' THEN 'RECH' ELSE 'OK' END as "COMPROBANTE_TESORERIA_LIQUIDADO"
		   ,CASE WHEN "COMPROBANTE_TESORERIA_DESBLOQUEADO" = 1 AND "BLO_DES" = 'DES' AND C."IdEstado" <> 'BLO' THEN 'RECH' ELSE 'OK' END as "COMPROBANTE_TESORERIA_DESBLOQUEADO"
	  FROM :v_MatrizValidaciones_v1 as  MTZ
	  LEFT JOIN "BSCL.Tesoreria.BD::TES.Comprobante" as C ON C."IdComprobante" = MTZ."Id"
	 WHERE MTZ."Entidad" = 'TES.Comprobante';
	
	
	
	P_OUT_RESULTADOREQUISITOS = 
	SELECT  DISTINCT R."IdInterno"
		   	,R."Codigo"
	 		,R."Resultado"
	 		,'Id' as "CodigoDato1"
	 		,CAST(R."Id" as NVARCHAR(520)) as "ValorDato1"
	 		,'' as "CodigoDato2"
	 		,'' as "ValorDato2"
	   FROM (
				SELECT "IdInterno","Id","Codigo","Resultado"
			 	  FROM (
					 SELECT "IdInterno","Id",
					 		COALESCE(			 			
								 "1" ||'MOVIMIENTO_TESORERIA_NO_EXISTE'
								,"2" ||'MOVIMIENTO_TESORERIA_EMITIDO'
								,"3" ||'MOVIMIENTO_TESORERIA_ANULADO'
								,"4" ||'MOVIMIENTO_TESORERIA_DESBLOQUEADO'
								,"5" ||'MOVIMIENTO_TESORERIA_BLOQUEADO'
					 		) AS "Codigo",
					 		COALESCE(			 			
					 			 "1" ||"MOVIMIENTO_TESORERIA_NO_EXISTE"
								,"2" ||"MOVIMIENTO_TESORERIA_EMITIDO"
								,"3" ||"MOVIMIENTO_TESORERIA_ANULADO"
								,"4" ||"MOVIMIENTO_TESORERIA_DESBLOQUEADO"
								,"5" ||"MOVIMIENTO_TESORERIA_BLOQUEADO"
					 		) AS "Resultado"
					   FROM :v_Validaciones_IngresoEgreso,"BSCL.Plataforma.BD::PLA.Identidad")
	  		  	 WHERE "Resultado" IS NOT NULL
	  		  	 UNION ALL
	  		  	SELECT "IdInterno","Id","Codigo","Resultado"
			 	  FROM (
					 SELECT "IdInterno","Id",
					 		COALESCE(			 			
								 "1" ||'COMPROBANTE_TESORERIA_NO_EXISTE'
								,"2" ||'COMPROBANTE_TESORERIA_LIQUIDADO'
								,"3" ||'COMPROBANTE_TESORERIA_DESBLOQUEADO'
					 		) AS "Codigo",
					 		COALESCE(			 			
					 			 "1" ||"COMPROBANTE_TESORERIA_NO_EXISTE"
								,"2" ||"COMPROBANTE_TESORERIA_LIQUIDADO"
								,"3" ||"COMPROBANTE_TESORERIA_DESBLOQUEADO"
					 		) AS "Resultado"
					   FROM :v_Validaciones_Comprobante,"BSCL.Plataforma.BD::PLA.Identidad")
	  		  	 WHERE "Resultado" IS NOT NULL
	   		) as R;
END;
