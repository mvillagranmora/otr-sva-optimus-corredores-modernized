SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosCierreDiario";

CREATE PROCEDURE
-- 
--  Proposito: Validar condiciones para cerrar proceso del cierre diario
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 21/02/2017
--     
"BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosCierreDiario"(

     -- Parametros de Proceso 
	IN p_FechaCierre			NVARCHAR(10),	
	IN p_ProcesosACerrar		TABLE("IdModuloFuncionProceso"  BIGINT,
									  "NombreFuncionProceso"	NVARCHAR(40),
									  "TotalConProblemas"       INTEGER
								     ) DEFAULT EMPTY,
                                    		 
	OUT p_out_Resultado			TABLE("IdModuloFuncionProceso"  BIGINT,
									  "NombreFuncionProceso"	NVARCHAR(40),
									  "DescripcionFuncionProceso"	NVARCHAR(40),
	   								  "Codigo"					NVARCHAR(100),
	   								  "Resultado"				NVARCHAR(4))	--OK   = Cumple todos los requisitos,		
																				--ADVE = 
						                                                		--RECH = Alguna de las reglas detienen la operacion
																				--RAUT = Requiere autorizacion
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER
	DEFAULT SCHEMA BSCL
AS
BEGIN 


	v_MatrizValidaciones = 
	SELECT
			 PAC."NombreFuncionProceso"				 
			,COALESCE(MFP."Descripcion",PAC."NombreFuncionProceso") AS "DescripcionFuncionProceso"
			,PAC."IdModuloFuncionProceso"
			,PAC."TotalConProblemas"			
			,MFP."PermiteEjecucion"			
			,RCD."IdEstado" As "IdEstadoRevisionCierreDiario"
			,CASE WHEN COALESCE(CT."Valor",'')='N' THEN 1 ELSE 0 END as "PROCESO_DE_CIERRE_DIARIO_DESCUADRADO"  -- segun parametro
			,1 as "PROCESO_DE_CIERRE_DIARIO_NO_EXISTE"
			,1 as "PROCESO_DE_CIERRE_DIARIO_NO_CONFIGURADO"  -- Condicion ejecucion: el proceso solo se puede ejecutar si el modulo esta abierto
			,1 as "PROCESO_CERRADO"
    FROM :p_ProcesosACerrar AS PAC
	LEFT JOIN "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" AS MFP ON (MFP."NombreFuncionProceso" = PAC."NombreFuncionProceso")
	INNER JOIN "BSCL.Plataforma.BD::CRE.ModuloEmpresa" AS ME ON (ME."IdCREModuloEmpresa" = MFP."IdCREModuloEmpresa" AND ME."IdModulo" = 'TES')
   	LEFT JOIN "BSCL.Tesoreria.BD::CIE.RevisionCierreDiario" AS RCD ON (RCD."NombreFuncionProceso" = PAC."NombreFuncionProceso" AND RCD."FechaCierre" = :p_FechaCierre AND RCD."IdEstado" = 'CER')
	LEFT JOIN "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria" AS CT ON (CT."CodigoParametro" = 'PERMITECERRARPROCESODIARIODESCUADRADO');
	--SELECT 'MatrizValidaciones', * FROM :v_MatrizValidaciones; -- REVISAR


	v_Validaciones_v1 =
	SELECT   "NombreFuncionProceso"
			,"DescripcionFuncionProceso"
			,"IdModuloFuncionProceso"
			,CASE WHEN "PROCESO_DE_CIERRE_DIARIO_DESCUADRADO" = 1 AND COALESCE("TotalConProblemas",0) > 0 THEN 'RECH' ELSE 'OK' END "PROCESO_DE_CIERRE_DIARIO_DESCUADRADO"			
			,CASE WHEN "PROCESO_DE_CIERRE_DIARIO_NO_EXISTE" = 1 AND COALESCE("IdModuloFuncionProceso",0) = 0 THEN 'RECH' ELSE 'OK' END "PROCESO_DE_CIERRE_DIARIO_NO_EXISTE"
			,CASE WHEN "PROCESO_DE_CIERRE_DIARIO_NO_CONFIGURADO" = 1 AND (COALESCE("PermiteEjecucion",'') <> 'A' ) THEN 'RECH' ELSE 'OK' END "PROCESO_DE_CIERRE_DIARIO_NO_CONFIGURADO" -- Condicion ejecucion: el proceso solo se puede ejecutar si el modulo esta abierto
			,CASE WHEN "PROCESO_CERRADO" = 1 AND COALESCE("IdEstadoRevisionCierreDiario",'') = 'CER' THEN 'RECH' ELSE 'OK' END "PROCESO_CERRADO"			
	FROM :v_MatrizValidaciones;  	
	--SELECT 'Validaciones_v1', * FROM :v_Validaciones_v1; -- REVISAR

	p_out_Resultado =   
	 SELECT 
	 		 "IdModuloFuncionProceso"
	 		,"NombreFuncionProceso"
	 		,"DescripcionFuncionProceso"
	 		,"Codigo"
			,CAST("Resultado" as NVARCHAR(4)) as "Resultado"
	   FROM (
			 SELECT "IdModuloFuncionProceso", 
					"DescripcionFuncionProceso",
			 		"NombreFuncionProceso",
			 		COALESCE(			 		
			 			 "1" ||"PROCESO_DE_CIERRE_DIARIO_DESCUADRADO"			 			
			 			,"2" ||"PROCESO_DE_CIERRE_DIARIO_NO_EXISTE"
				 		,"3" ||"PROCESO_DE_CIERRE_DIARIO_NO_CONFIGURADO"
				 		,"4" ||"PROCESO_CERRADO"
			 		) AS "Resultado",
			 		COALESCE(			 			
				 		 "1" ||'PROCESO_DE_CIERRE_DIARIO_DESCUADRADO'
			 			,"2" ||'PROCESO_DE_CIERRE_DIARIO_NO_EXISTE'
				 		,"3" ||'PROCESO_DE_CIERRE_DIARIO_NO_CONFIGURADO'
				 		,"4" ||'PROCESO_CERRADO'
			 		) AS "Codigo"
			   FROM :v_Validaciones_v1,"BSCL.Plataforma.BD::PLA.Identidad")
	  WHERE "Resultado" IS NOT NULL;
END;
