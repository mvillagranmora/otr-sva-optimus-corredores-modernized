SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::NCG380Instrucciones";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::NCG380Instrucciones"
( 
    -- Parametros de Entrada
 	IN p_IdSolicitudCircular 		  BIGINT DEFAULT 0, 
	IN p_TablaParametros    		  TABLE("TipoParametro" nvarchar(100) , "Valor" nvarchar(100))
    --VARIABLES DE SALIDA 
	,OUT p_out_TablaSalida	 		  "BSCL.Normativa.BD::NOR.SALIDA_NCG380Instrucciones" --Se debe llamar siemte p_out_TablaSalida
      
)  	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
  BEGIN
  	  
  	declare v_ParametrosSolicitudFecRel DATE DEFAULT '0001-01-01';

	declare v_IdSolicitudCircular int;
	
	declare v_TRut		 int DEFAULT 0;	
	declare v_TNemo		 int DEFAULT 0;	
	
	declare v_Count				 int DEFAULT 0;	
	declare v_IdEmpresa			 BIGINT DEFAULT 0;
	
    --------------------
	--DECLARE Manejo de Errores
	-------------------- 
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		RESIGNAL;
	END;

	--------------------
	--LOGICA
	--------------------
	
	select top 1 "Valor" into v_IdEmpresa from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = :p_IdSolicitudCircular and "SP"."TipoParametro" = 'EMP';
	select top 1 "Valor" into v_ParametrosSolicitudFecRel from "BSCL.Normativa.BD::NOR.SolicitudParametros" as "SP" where "SP"."IdSolicitudCircular" = :p_IdSolicitudCircular and "SP"."TipoParametro" ='FTER'; -- = 'FREA';
	
	CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(0,CURRENT_UTCTIMESTAMP,0,'BSCL.Normativa.BD.Servicios.NOR::NCG380Instrucciones','INICIO','INICIO');
	
		--* FILTRO POR CLIENTES (Asume que la info viene como 338-4)
		--* ------------------------------------------------------------------------------	
		v_TablaClientes = 
			SELECT 	"Valor", "IdSistema" 
			FROM 	"BSCL.Normativa.BD::NOR.SolicitudParametros" 
			WHERE 	"IdSolicitudCircular" = p_IdSolicitudCircular AND "TipoParametro" = 'RUT';
	
		SELECT COUNT(1) INTO v_Count FROM :v_TablaClientes;
		IF v_Count = 0 THEN
			v_TablaClientes = 
				SELECT	NULL AS "Valor", NULL AS "IdSistema"
				FROM	DUMMY;
		END IF;
		
		--* FILTRO POR NEMO
		--* ------------------------------------------------------------------------------	
		v_TablaNemo = 
			SELECT 	"Valor", "IdSistema" 
			FROM 	"BSCL.Normativa.BD::NOR.SolicitudParametros" 
			WHERE 	"IdSolicitudCircular" = p_IdSolicitudCircular AND "TipoParametro" = 'NEMO';
	
		SELECT COUNT(1) INTO v_Count FROM :v_TablaNemo;
		IF v_Count = 0 THEN
			v_TablaNemo = 
				SELECT	NULL AS "Valor", NULL AS "IdSistema"
				FROM	DUMMY;
		END IF;	
		

		p_out_TablaSalida = select 
			 EVT."DocumentoIdentidad"
			,EVT."CodigoISIN"
			,EVT."Nemotecnico"
			,EVT."IdEmisor"
			,EVT."TipoJunta" 
			,EVT."Lugar"
			,EVT."FechaRealizacion"
			,EVT."Materias"
			,EVT."Instruccion"
			,EVT."CantidadDerecho"
		FROM "BSCL.Normativa.BD.Vistas::NCG380.ConsultarInstruccionesJuntaAccionista" AS EVT
		INNER JOIN :v_TablaNemo AS PrmNemo ON (PrmNemo."Valor" IS NULL OR PrmNemo."Valor" = EVT."Nemotecnico")
		INNER JOIN :v_TablaClientes AS PrmCli ON (PrmCli."Valor" IS NULL OR PrmCli."Valor" = EVT."DocumentoIdentidad")	
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoEnrolamientoCliente" AS CLIENTE ON CLIENTE."DocumentoIdentidad" = EVT."DocumentoIdentidad"
		WHERE EVT."FechaRealizacion"  = v_ParametrosSolicitudFecRel
		AND CLIENTE."TipoEnrolamiento" = 'FULL';
		
		SELECT COUNT(1) INTO v_Count FROM :p_out_TablaSalida;
		
	IF v_Count=0 THEN
		p_out_TablaSalida = select 
				 '' AS "DocumentoIdentidad"
				,'' AS "CodigoISIN"
				,'' AS "Nemotecnico"
				,'' AS "IdEmisor"
				,'' AS "TipoJunta" 
				,'' AS "Lugar"
				,'0001-01-01' AS "FechaRealizacion"
				,'' AS "Materias"
				,'' AS "Instruccion"
				,0 AS "CantidadDerecho"
				from DUMMY WHERE	1=2;
	END IF;
										

	
	CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(0,CURRENT_UTCTIMESTAMP,0,'BSCL.Normativa.BD.Servicios.NOR::NCG380Instrucciones','FIN','FIN');

END;