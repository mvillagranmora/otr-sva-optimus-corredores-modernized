SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Orden.BD.Reglas::ServicioCumpleRequisitosCrearMovFacturarAsignacionMM";
CREATE PROCEDURE
-- 
--  Proposito: VALIDACIONES DEL SERVICIO DE ENVIO A FACTURAR ASIGNACIONES
--  Autor: TEAM ORDENES, F**K YEAH
--  Fecha Creacion: 15/01/2016
--     

"BSCL.Orden.BD.Reglas::ServicioCumpleRequisitosCrearMovFacturarAsignacionMM"(
	-- Parametros entrada
	IN p_IdEmpresa                        	BIGINT,
	
	IN p_Asignaciones	TABLE(
						"IdInterno" 			BIGINT,
						"IdAsignacion" 			BIGINT,
						"IdAsignacionReal" 		BIGINT,
						"IdEstadoAsignacion"	NVARCHAR(3),
						"IdEstadoModulo"		NVARCHAR(3),
						"NombreModulo"			NVARCHAR(100)
						),
	
	-- Salidas para mensajes de errores
	OUT p_out_SalidaDetalle		"BSCL.Orden.BD.Tipos::SalidaDetalle",
	OUT p_out_Salida        	"BSCL.Orden.BD.Tipos::SalidaResumen"



) 	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER   
	DEFAULT SCHEMA BSCL
	READS SQL DATA
	AS
BEGIN		
	--VALIDACIONES EN POSITIVO --PALABRAS CLAVE: COINCIDE, EXISTE, MAYOR_CERO, CORRECTO, DISPONIBLE
	
	--********************** VALIDACIONES *********************************** 	

	/*v_matriz_validaciones =	SELECT 	asig."IdAsignacion" ,
									1 	as "ASIGNACION_EXISTE",
									1 	as "ASIGNACION_NO_ENVIADA_A_FACTURAR",
									1 	as "ASIGNACION_NO_ANULADA"
									
							FROM :p_Asignaciones asig;*/
							
							
	v_val = SELECT asig."IdInterno",asig."IdAsignacion",
				CASE WHEN COALESCE(asig."IdAsignacionReal",-1) > 0 THEN 'OK' ELSE 'RECHAZO' END AS "ASIGNACION_EXISTE", 
				CASE WHEN COALESCE(asig."IdAsignacionReal",-1) > 0 AND "IdEstadoAsignacion" = 'EAF' THEN 'RECHAZO' ELSE 'OK' END AS "ASIGNACION_ENVIADA_A_FACTURAR",
				CASE WHEN COALESCE(asig."IdAsignacionReal",-1) > 0 AND "IdEstadoAsignacion" = 'ANU' THEN 'RECHAZO' ELSE 'OK' END AS "ASIGNACION_ANULADA",
				CASE "IdEstadoModulo" WHEN 'C' THEN 'RECHAZO' ELSE 'OK' END AS "MODULO_CERRADO"
			FROM :p_Asignaciones asig;
	
	
	v_TablaRequisitos = 
		SELECT "IdAsignacion","IdInterno","Resultado","CodigoValidacion"
		FROM (
			SELECT * ,
			
			COALESCE(
			"1" ||"ASIGNACION_EXISTE",	"2" ||"ASIGNACION_ENVIADA_A_FACTURAR",	"3" ||"ASIGNACION_ANULADA",	"4" ||"MODULO_CERRADO"
			)  	AS "Resultado",
			
			COALESCE(
			"1" ||'ASIGNACION_EXISTE',	"2" ||'ASIGNACION_ENVIADA_A_FACTURAR',	"3" ||'ASIGNACION_ANULADA',	"4" ||'MODULO_CERRADO'
			)  AS "CodigoValidacion" 
			FROM :v_val,"BSCL.Plataforma.BD::PLA.Identidad"
		) WHERE "Resultado" IS NOT NULL;
	
	
	
	
	
	p_out_SalidaDetalle =	SELECT 
								tb."IdInterno", tb."IdAsignacion" as "IdOperacion","Resultado" ,
								CASE "CodigoValidacion" 
									WHEN 'ASIGNACION_EXISTE' 					THEN 'ASIGNACION_NO_EXISTE'					--01
									WHEN 'ASIGNACION_ENVIADA_A_FACTURAR' 		THEN 'ASIGNACION_ENVIADA_A_FACTURAR'		--02
									WHEN 'ASIGNACION_ANULADA' 					THEN 'ASIGNACION_ANULADA'					--03
									WHEN 'MODULO_CERRADO' 						THEN 'MODULO_CERRADO'						--04
								END AS "Codigo",
								CASE "CodigoValidacion" 
									WHEN 'ASIGNACION_EXISTE' 					THEN 'IdAsignacion'							--01
									WHEN 'ASIGNACION_ENVIADA_A_FACTURAR' 		THEN 'IdAsignacion'							--02
									WHEN 'ASIGNACION_ANULADA' 					THEN 'IdAsignacion'							--03
									WHEN 'MODULO_CERRADO' 						THEN 'NombreModulo'							--04
								END AS "CodigoDato1",
								CASE "CodigoValidacion" 
									WHEN 'ASIGNACION_EXISTE' 					THEN CAST(asig."IdAsignacion" AS NVARCHAR(100))		--01
									WHEN 'ASIGNACION_ENVIADA_A_FACTURAR' 		THEN CAST(asig."IdAsignacion" AS NVARCHAR(100))		--02
									WHEN 'ASIGNACION_ANULADA' 					THEN CAST(asig."IdAsignacion" AS NVARCHAR(100))		--03
									WHEN 'MODULO_CERRADO' 						THEN asig."NombreModulo"							--04
								END AS "ValorDato1",
								'' as "CodigoDato2",
								'' as "ValorDato2"
							FROM :v_TablaRequisitos tb
							INNER JOIN :p_Asignaciones asig on tb."IdInterno" = asig."IdInterno"
							WHERE "Resultado" != 'OK'
							ORDER BY "IdInterno";
		
	p_out_Salida = SELECT * FROM "BSCL.Orden.BD.Utilitarios::ObtenerSalidaResumen"(:p_out_SalidaDetalle);
		

END;