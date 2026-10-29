SET SCHEMA "BSCL";
DROP PROCEDURE  "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosGestionDeMonedas";

CREATE PROCEDURE
-- 
--  Proposito: Validar 
--  Autor: NRM - BCS
--  Fecha Creacion: 23/09/2016
--     
"BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosGestionDeMonedas"(
	-- Parametros entrada
	IN 	p_IdEmpresa				BIGINT,
    IN 	p_Monedas				TABLE ("IdInterno"		BIGINT,
    								   "IdMoneda"  		NVARCHAR(3),
   									   "TipoEjecucion" 	NVARCHAR(3)),
   									   
	OUT p_out_Resultado			TABLE ("IdInterno"		BIGINT,
									   "IdMoneda"  		NVARCHAR(3),
   									   "TipoEjecucion" 	NVARCHAR(3),   									   
   									   "Codigo"			NVARCHAR(100),
   									   "Resultado"		NVARCHAR(4)) 	--OK   = Cumple todos los requisitos,		
																		--ADVE = 
					                                                   	--RECH = Alguna de lAS reglAS detienen la operacion
																		--RAUT = Requiere autorizacion
   									      									   
) 	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER   
	DEFAULT SCHEMA BSCL
AS
BEGIN

	--**************************************************************************************************
	--********************************* VALIDACIONES Y REGLAS  *****************************************
	--**************************************************************************************************
	v_MatrizValidaciones = 
	SELECT  DISTINCT
			 M."IdInterno"
			,M."IdMoneda"
			,MT."IdEstado" as "IdEstadoMoneda"
			,MN."IdEstado" as "IdEstadoMonedaSistema"
			,M."TipoEjecucion"
			,1 as "MONEDA_NO_EXISTE"
			,1 as "MONEDA_NO_ACTIVA"
	  FROM :p_Monedas as M
	  LEFT JOIN "BSCL.Tesoreria.BD::TES.MonedaTesoreria" as MT ON MT."IdMoneda" = M."IdMoneda"
	  LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" as MN ON MN."IdMoneda" = M."IdMoneda";
	  
	
	v_Validaciones =  
	SELECT   "IdInterno"
			,"IdMoneda"
			,"TipoEjecucion"
			,CASE WHEN "MONEDA_NO_EXISTE" = 1 AND COALESCE("IdEstadoMonedaSistema",'') = '' THEN 'RECH' ELSE 'OK' END AS "MONEDA_NO_EXISTE"
			,CASE WHEN "MONEDA_NO_ACTIVA" = 1 AND "IdEstadoMonedaSistema" = 'INA' THEN 'RECH' ELSE (CASE WHEN COALESCE("IdEstadoMonedaSistema",'') = '' THEN null ELSE 'OK' END) END AS "MONEDA_NO_ACTIVA"
	  FROM :v_MatrizValidaciones;
		

	p_out_Resultado =
	 SELECT "IdInterno",
	 		"IdMoneda",
			"TipoEjecucion",			
			"Codigo",
			"Resultado"
	   FROM (
			 SELECT "IdInterno",
			 		"IdMoneda",
					"TipoEjecucion",
			 		COALESCE(
			 			"1" ||"MONEDA_NO_EXISTE",
			 			"2" ||"MONEDA_NO_ACTIVA"			 			
			 		) AS "Resultado",
			 		COALESCE(
			 			"1" ||'MONEDA_NO_EXISTE',
			 			"2" ||'MONEDA_NO_ACTIVA'
			 		) AS "Codigo"
			   FROM :v_Validaciones,"BSCL.Plataforma.BD::PLA.Identidad")
	  WHERE "Resultado" IS NOT NULL;
	  
END;
