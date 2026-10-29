SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::NOR.FatcaResumen";
GO 
--
--  Proposito: Servicio para la generacion del archivo por JOB Manager .
--  Autor: GAT-BLAC
--  Fecha Creacion: 05/12/2017
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::NOR.FatcaResumen"
(
	--VARIABLES DEL PROCESO
 	IN p_IdSolicitudCircular 		  BIGINT DEFAULT 0, 
	IN p_TablaParametros    		  TABLE("TipoParametro" nvarchar(100) , "Valor" nvarchar(100)) 
    --VARIABLES DE SALIDA 
	,OUT p_out_TablaSalida	"BSCL.Normativa.BD::NOR.SALIDA_FATCARESUMEN"
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
	DECLARE v_IdDocumento BIGINT;
	DECLARE v_IdTrabajoParametros BIGINT;
	DECLARE v_IdTipoDocumento NVARCHAR(10):= 'FATCAD';
	DECLARE v_IdEmpresa BIGINT;
	DECLARE v_Count BIGINT;
	DECLARE v_out_IdInfoTran BIGINT;
	DECLARE v_FINI NVARCHAR(10);
	DECLARE v_IDCLI BIGINT;
	DECLARE v_MMIN DECIMAL(26,8);
	DECLARE v_MMAX DECIMAL(26,8);
	DECLARE v_FEJEC NVARCHAR(10);
	DECLARE v_FENR NVARCHAR(10);
	DECLARE v_FEDES NVARCHAR(10);
	DECLARE v_FEHAS NVARCHAR(10);
	DECLARE v_CFAT NVARCHAR(20);
	DECLARE v_IFAT NVARCHAR(20);
	DECLARE v_TP NVARCHAR(20);
	DECLARE v_RIND NVARCHAR(20);
	
	IF p_IdSolicitudCircular>0 THEN
	
		SELECT TOP 1 "IdDocumento" INTO v_IdDocumento 
		FROM "BSCL.Normativa.BD::NOR.SolicitudCircular"
		WHERE "IdSolicitudCircular" = p_IdSolicitudCircular;
		
		SELECT TOP 1 "IdEmpresa" INTO v_IdEmpresa 
		FROM "BSCL.Normativa.BD::DOC.Documento"
		WHERE "IdDocumento" = v_IdDocumento;
		
		v_Datos = SELECT 
			  CASE WHEN SOL."TipoParametro"='IDCLI'	THEN "Valor" ELSE NULL END AS "IDCLI"
			, CASE WHEN SOL."TipoParametro"='MMIN'	THEN "Valor" ELSE NULL END AS "MMIN"
			, CASE WHEN SOL."TipoParametro"='MMAX'	THEN "Valor" ELSE NULL END AS "MMAX"
			, CASE WHEN SOL."TipoParametro"='FEJEC'	THEN "Valor" ELSE NULL END AS "FEJEC"
			, CASE WHEN SOL."TipoParametro"='FENR'	THEN "Valor" ELSE NULL END AS "FENR"
			, CASE WHEN SOL."TipoParametro"='CFAT'	THEN "Valor" ELSE NULL END AS "CFAT"
			, CASE WHEN SOL."TipoParametro"='IFAT'	THEN "Valor" ELSE NULL END AS "IFAT"
			, CASE WHEN SOL."TipoParametro"='TP'	THEN "Valor" ELSE NULL END AS "TP"
			, CASE WHEN SOL."TipoParametro"='RIND'	THEN "Valor" ELSE NULL END AS "RIND"
			FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" SOL
		 	WHERE SOL."IdSolicitudCircular" = p_IdSolicitudCircular;
		 	
		v_DatosParametros= SELECT PAR."TipoParametro" AS "TipoParametro" , PAR."Valor" AS "Valor"
						 FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" PAR 
						WHERE PAR."IdSolicitudCircular" = p_IdSolicitudCircular;
		
	END IF;
	
	SELECT  COUNT(1) INTO v_Count FROM :p_TablaParametros;
	
	if v_Count>0 THEN
		SELECT TOP 1  "Valor" INTO v_IdEmpresa
			FROM :p_TablaParametros SOL WHERE SOL."TipoParametro"='IdEmpresa';
			
		v_Datos = SELECT 
			  CASE WHEN SOL."TipoParametro"='IDCLI'	THEN "Valor" ELSE NULL END AS "IDCLI"
			, CASE WHEN SOL."TipoParametro"='MMIN'	THEN "Valor" ELSE NULL END AS "MMIN"
			, CASE WHEN SOL."TipoParametro"='MMAX'	THEN "Valor" ELSE NULL END AS "MMAX"
			, CASE WHEN SOL."TipoParametro"='FEJEC'	THEN "Valor" ELSE NULL END AS "FEJEC"
			, CASE WHEN SOL."TipoParametro"='FENR'	THEN "Valor" ELSE NULL END AS "FENR"
			, CASE WHEN SOL."TipoParametro"='CFAT'	THEN "Valor" ELSE NULL END AS "CFAT"
			, CASE WHEN SOL."TipoParametro"='IFAT'	THEN "Valor" ELSE NULL END AS "IFAT"
			, CASE WHEN SOL."TipoParametro"='TP'	THEN "Valor" ELSE NULL END AS "TP"
			, CASE WHEN SOL."TipoParametro"='RIND'	THEN "Valor" ELSE NULL END AS "RIND"
			FROM :p_TablaParametros SOL;
			
		 v_DatosParametros= SELECT PAR."TipoParametro" AS "TipoParametro" , PAR."Valor" AS "Valor"
						 FROM :p_TablaParametros PAR UNION ALL
						 SELECT 'IdTipoDocumento' AS "TipoParametro" , :v_IdTipoDocumento AS "Valor"
						 FROM DUMMY;		
			
	END IF;
	
	v_DatosAgrupado =SELECT  
					  MAX(TRIM(SOL."IDCLI"))		AS "IDCLI"
					, MAX(TRIM(SOL."MMIN"))			AS "MMIN"
					, MAX(TRIM(SOL."MMAX"))			AS "MMAX"
					, MAX(TRIM(SOL."FEJEC"))		AS "FEJEC"
					, MAX(TRIM(SOL."FENR"))			AS "FENR"
					, MAX(TRIM(SOL."CFAT"))			AS "CFAT"
					, MAX(TRIM(SOL."IFAT"))			AS "IFAT"
					, MAX(TRIM(SOL."TP"))			AS "TP"
					, MAX(TRIM(SOL."RIND"))			AS "RIND"
					FROM :v_Datos SOL;
	
   SELECT TOP 1 "IDCLI","MMIN","MMAX","FEJEC","FENR","CFAT","IFAT","TP","RIND" 
   INTO v_IDCLI,v_MMIN, v_MMAX, v_FEJEC, v_FENR, v_CFAT, v_IFAT, v_TP, v_RIND 
   FROM :v_DatosAgrupado;
   
   SELECT "BSCL.Normativa.BD.Secuencias::GBL.IdTrabajoParametros".NEXTVAL INTO v_IdTrabajoParametros FROM DUMMY;
	
   INSERT INTO "BSCL.Normativa.BD::GBL.Parametros" ("IdInterno","Tipo","NombreParametro","Valor")
   SELECT :v_IdTrabajoParametros AS "IdInterno",'SOLICITUD', PAR."TipoParametro", PAR."Valor"
   FROM :v_DatosParametros PAR;
   
   CALL "BSCL.Normativa.BD.Servicios::FATCA.ConsultarFatcaResumen"(
   		p_IdEmpresa => v_IdEmpresa,
 	
	 	--VARIABLES DEL PROCESO
		p_IdCliente => v_IDCLI,
		p_MontoPatrimonioMinimo	=> v_MMIN,
		p_MontoPatrimonioMaximo	=> v_MMAX,
		p_FechaEjecucion => v_FEJEC,
		p_FechaEnrolamiento	=> v_FENR,
		p_ClasificacionFatca => v_CFAT,
		p_IndicadorFatca => v_IFAT,
		p_TipoPersona => v_TP,
		p_Indicios	=> v_RIND,
		
		p_IdTrabajoParametros => v_IdTrabajoParametros,
		
		-- VARIABLES DE SALIDA
		p_out_IdInfoTran => v_out_IdInfoTran
   );
   
   p_out_TablaSalida = SELECT "Rut" 
							,"TipoPersona"    
							,"Indicios"
							,"ClasificacionFatca"   
							,"ClienteFatca"   
							,"AutorizaOrgFatca"    
							,"PatrimonioClp"    
							,"PatrimonioUsd"   
							,"FechaEnrolamiento"    
							,"FechaModIndicios"   
							,"FechaModFacta"   
							,"Residencia"
							,"LugarNacimiento"
							,"PaisConstitucion"   
							,"CasaMatriz"  
							,"Nacionalidad"
							,"CodPostal"
							,"NumTelefono"
							,"OrdenPermanente"   
							,"PoderEfectivo"   
							,"Beneficiario" 
							,"FechaCargaInformacion" 
					FROM "BSCL.Normativa.BD::GBL.SALIDA_FATCARESUMEN"
					WHERE "IdInterno"=v_out_IdInfoTran;
	
	SELECT * FROM :p_out_TablaSalida;
	
   
   
END;
