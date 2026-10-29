SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::NOR.NCG380Aut";
GO 

CREATE PROCEDURE 
--
--  Proposito: Servicio para la generacion del archivo por JOB Manager .
--  Autor: CFLORES - BCS
--  Fecha Creacion: 06/09/2017
"BSCL.Normativa.BD.Servicios::NOR.NCG380Aut" (
	--VARIABLES DEL PROCESO
 	IN p_IdSolicitudCircular 		  BIGINT DEFAULT 0, 
	IN p_TablaParametros    		  TABLE("TipoParametro" nvarchar(100) , "Valor" nvarchar(100)) 
    --VARIABLES DE SALIDA 
	,OUT p_out_TablaSalida	"BSCL.Normativa.BD::NOR.SALIDA_NCG380AUT"
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
	
	DECLARE v_IdDocumento,v_IdEmpresa, v_Count,v_IdTrabajoParametros,v_IdCliente,
v_IdPersonaAutorizado,v_out_IdInfoTran INT:=0;

	DECLARE v_RELA	NVARCHAR(6):='';
	
	DECLARE v_FechaOperacionDesde, v_FechaOperacionHasta DATE DEFAULT '0001-01-01';
	DECLARE v_Esquema,v_Version, v_FechaVersion			 NVARCHAR(10) DEFAULT '';
	
	DECLARE v_IdTipoDocumento				 NVARCHAR(10):= 'NCG380AUT';
	DECLARE v_TODOS_CLI,v_TODOS_NEMO		 NVARCHAR(2):='N';
	DECLARE v_NEMO 							 NVARCHAR(40):='';
	DECLARE v_IDCLI, v_IDAUTORIZADO	 NVARCHAR(40):='0';

	
	
	IF p_IdSolicitudCircular>0 THEN
		
		SELECT TOP 1 "IdDocumento" INTO v_IdDocumento 
		FROM "BSCL.Normativa.BD::NOR.SolicitudCircular"
		WHERE "IdSolicitudCircular" = p_IdSolicitudCircular;
		
		SELECT TOP 1 "IdEmpresa" INTO v_IdEmpresa 
		FROM "BSCL.Normativa.BD::DOC.Documento"
		WHERE "IdDocumento" = v_IdDocumento;
	
		 				
		
		v_Datos = SELECT 
			  CASE WHEN SOL."TipoParametro"='FINI'	THEN "Valor" ELSE NULL END AS "FINI"
			, CASE WHEN SOL."TipoParametro"='FTER'	THEN "Valor" ELSE NULL END AS "FTER"
			, CASE WHEN SOL."TipoParametro"='IDCLI'			THEN "Valor" ELSE NULL END AS "IDCLI"
			, CASE WHEN SOL."TipoParametro"='IDAUTORIZADO'	THEN "Valor" ELSE NULL END AS "IDAUTORIZADO"
			, CASE WHEN SOL."TipoParametro"='RELA'		THEN "Valor" ELSE '' END AS "RELA"
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
			  CASE WHEN SOL."TipoParametro"='fechaDesde'	THEN "Valor" ELSE NULL END AS "FINI"
			, CASE WHEN SOL."TipoParametro"='fechaHasta'	THEN "Valor" ELSE NULL END AS "FTER"
			, CASE WHEN SOL."TipoParametro"='IDCLI'			THEN "Valor" ELSE NULL END AS "IDCLI"
			, CASE WHEN SOL."TipoParametro"='IDAUTORIZADO'	THEN "Valor" ELSE NULL END AS "IDAUTORIZADO"
			--, CASE WHEN SOL."TipoParametro"='NEMO'			THEN "Valor" ELSE NULL END AS "NEMO"		
			, CASE WHEN SOL."TipoParametro"='RELA'		THEN "Valor" ELSE '' END AS "RELA"
			FROM :p_TablaParametros SOL;
			
		 v_DatosParametros= SELECT PAR."TipoParametro" AS "TipoParametro" , PAR."Valor" AS "Valor"
						 FROM :p_TablaParametros PAR UNION ALL
						 SELECT 'IdTipoDocumento' AS "TipoParametro" , v_IdTipoDocumento AS "Valor"
						 FROM DUMMY;		
			
	END IF;


	v_DatosAgrupado =SELECT  
					  MAX(TRIM(SOL."FINI")) 		AS "FINI"
					, MAX(TRIM(SOL."FTER"))			AS "FTER"
					, MAX(TRIM(SOL."IDCLI"))		AS "IDCLI"
					, MAX(TRIM(SOL."IDAUTORIZADO"))	AS "IDAUTORIZADO"
					--, MAX(TRIM(SOL."NEMO"))			AS "NEMO"
					, MAX(TRIM(SOL."RELA"))		AS "RELA"
					
					FROM :v_Datos SOL;
					

	SELECT TOP 1 "FINI","FTER", "IDCLI", "IDAUTORIZADO" , "RELA" INTO v_FechaOperacionDesde,v_FechaOperacionHasta, v_IDCLI, v_IDAUTORIZADO, v_RELA FROM :v_DatosAgrupado;
	
	
	SELECT "BSCL.Normativa.BD.Secuencias::GBL.IdTrabajoParametros".NEXTVAL INTO v_IdTrabajoParametros FROM DUMMY;
			
	INSERT INTO "BSCL.Normativa.BD::GBL.Parametros" ("IdInterno","Tipo","NombreParametro","Valor")
	SELECT :v_IdTrabajoParametros AS "IdInterno",'SOLICITUD', PAR."TipoParametro", PAR."Valor"
		FROM :v_DatosParametros PAR;
			
	v_IdCliente = CAST (TRIM(v_IDCLI) AS BIGINT); 	
	v_IdPersonaAutorizado= CAST (TRIM(v_IDAUTORIZADO) AS BIGINT);
	SELECT COALESCE(v_RELA,'')INTO v_RELA FROM DUMMY;
	--v_RELA:='';
	CALL "BSCL.Normativa.BD.Servicios::NCG380.ConsultarAutorizados"
	(
		p_IdEmpresa							=> v_IdEmpresa,
		p_FechaOperacionDesde				=> v_FechaOperacionDesde,
		p_FechaOperacionHasta				=> v_FechaOperacionHasta,
		p_IdCliente 						=> v_IdCliente,
		p_IdAutorizado 						=> v_IdPersonaAutorizado,
	    p_IdTipoRelacion 					=> v_RELA,
		p_IdTrabajoParametros				=> :v_IdTrabajoParametros,
		p_out_IdInfoTran					=> v_out_IdInfoTran
	);
	
	p_out_TablaSalida = SELECT
						OPE."NumeroFolioCliente",
						OPE."RutCLI",
						OPE."DigCLI",
						OPE."NumeroPersona", ---AUTORIZADOS
						OPE."Rut",
						OPE."Dig",
						OPE."Pasaporte",
						OPE."Glei",
						OPE."ApellidoPaterno",
						OPE."ApellidoMaterno",
						OPE."PrimerNombre",
						OPE."SegundoNombre",
						OPE."RazonSocial",
						OPE."Telefono",
						OPE."Email",
						OPE."DireccionTipo",
						OPE."DireccionNombre",
						OPE."DireccionNumero",
						OPE."DireccionDepto",
						OPE."DireccionComuna",
						OPE."DireccionCiudad",
						COALESCE("REG"."NombreClasificacionDivisionPais1",'') AS "DireccionEstado",
						OPE."DireccionPais",
						OPE."DireccionCodigoPostal",
						OPE."Nacionalidad",
						--CASE WHEN COALESCE(OPE."RelacionIntermediario",'')= '' THEN 'OTRA RELACION' ELSE SUBSTR_AFTER  (OPE."RelacionIntermediario",' ') END  AS "RelacionIntermediario"
						OPE."RelacionIntermediario"  AS "RelacionIntermediario",
						OPE."TipoRelacion"
					FROM "BSCL.Normativa.BD::GBL.SALIDA_NCG380AUT" OPE
					LEFT JOIN "BSCL.Plataforma.BD.Vistas::REG.ConsultarPaisRegionCiudad" AS REG ON (REG."NombreCiudad" = OPE."DireccionCiudad") AND (REG."NombreDivisionPais" = OPE."DireccionComuna")
					WHERE "IdInterno"=v_out_IdInfoTran
					ORDER BY "NumeroFolioCliente";
	
	SELECT * FROM :p_out_TablaSalida;

END
