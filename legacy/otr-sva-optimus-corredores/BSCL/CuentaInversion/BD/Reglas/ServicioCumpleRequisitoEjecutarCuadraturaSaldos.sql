SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.CuentaInversion.BD.Reglas::ServicioCumpleRequisitoEjecutarCuadraturaSaldos";

CREATE PROCEDURE
-- 
--  Proposito: Realizar el conjunto de validaciones asociadas al servicio
--  Autor: NRM - BCS 
--  Fecha Creacion: 13/01/2017
-- 
--
"BSCL.CuentaInversion.BD.Reglas::ServicioCumpleRequisitoEjecutarCuadraturaSaldos" ()
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" AS
BEGIN
	
	v_MatrizValidaciones_v1 =
	 SELECT "IdInterno"
			,"IdCuentaInversion"
			,"IdPortafolio"
			,"IdEstadoPortafolio"
			,"CodigoPortafolio"
			,"IdMoneda"
			,"IdEstadoMoneda"
			,1 as "CUENTA_INVERSION_NO_EXISTE"
			,1 as "PORTAFOLIO_NO_EXISTE"
			,1 as "MONEDA_NO_EXISTE"			
	   FROM "BSCL.CuentaInversion.BD::CTA.ServicioCumpleRequisitoEjecutarCuadraturaSaldos_IN_CuentasACuadrar"; 
	  
	  
	v_Validaciones_V1 =
	 SELECT  "IdInterno"			
			,CASE WHEN "CUENTA_INVERSION_NO_EXISTE" = 1 AND COALESCE("IdCuentaInversion",0) = 0 THEN 'RECH' ELSE 'OK' END AS "CUENTA_INVERSION_NO_EXISTE"
			,CASE WHEN "PORTAFOLIO_NO_EXISTE" = 1 AND COALESCE("IdEstadoPortafolio",'') = '' THEN 'RECH' ELSE 'OK' END AS "PORTAFOLIO_NO_EXISTE"
			,CASE WHEN "MONEDA_NO_EXISTE" = 1 AND COALESCE("IdEstadoMoneda",'') = '' THEN 'RECH' ELSE 'OK' END AS "MONEDA_NO_EXISTE"
	  FROM :v_MatrizValidaciones_v1; 
	
	
	INSERT INTO "BSCL.CuentaInversion.BD::CTA.ServicioCumpleRequisitoEjecutarCuadraturaSaldos_OUT_Resultado"
	SELECT DISTINCT R."IdInterno", "Codigo", "Resultado"
		   ,CASE WHEN "Codigo" = 'CUENTA_INVERSION_NO_EXISTE' THEN 'IdCuentaInversion'
		   		 WHEN "Codigo" = 'PORTAFOLIO_NO_EXISTE' THEN 'IdPortafolio'
		   		 WHEN "Codigo" = 'MONEDA_NO_EXISTE' THEN 'IdMoneda'
		     END as "CodigoDato1"
		   ,CASE WHEN "Codigo" = 'CUENTA_INVERSION_NO_EXISTE' THEN CAST("IdCuentaInversion" as NVARCHAR(520))
		   		 WHEN "Codigo" = 'PORTAFOLIO_NO_EXISTE' THEN CAST("IdPortafolio" as NVARCHAR(520))
		   		 WHEN "Codigo" = 'MONEDA_NO_EXISTE' THEN "IdMoneda"
		     END as "ValorDato1"
		   ,'' as "CodigoDato2", '' as "ValorDato2"
	  FROM (
	  		SELECT "IdInterno",
	  			   COALESCE(
	  			   		 "1" || 'CUENTA_INVERSION_NO_EXISTE'
	  			   		,"2" || 'PORTAFOLIO_NO_EXISTE'
	  			   		,"3" || 'MONEDA_NO_EXISTE'
	  			   ) AS "Codigo",
	  			   COALESCE(
	  			   		 "1" || "CUENTA_INVERSION_NO_EXISTE"
	  			   		,"2" || "PORTAFOLIO_NO_EXISTE"
	  			   		,"3" || "MONEDA_NO_EXISTE"
	  			   ) AS "Resultado"
	  		  FROM :v_Validaciones_V1,"BSCL.Plataforma.BD::PLA.Identidad"
	  	   ) as R
	 INNER JOIN :v_MatrizValidaciones_v1 as M ON M."IdInterno" = R."IdInterno"
	 WHERE R."Resultado" IS NOT NULL;
	
END;
