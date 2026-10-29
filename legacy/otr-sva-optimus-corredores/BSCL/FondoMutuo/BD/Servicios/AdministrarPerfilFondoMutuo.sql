SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::AdministrarPerfilFondoMutuo";

CREATE PROCEDURE
--  Proposito: Administra que perfil es backoffice y cual es comercial para las operaciones Fondo Mutuo
--  Autor: - Gat-Blac 
--  Fecha Creacion: 13/09/2016
"BSCL.FondoMutuo.BD.Servicios::AdministrarPerfilFondoMutuo"(
	--VARIABLES OBLIGATORIAS
	IN 	p_IdEmpresa 						BIGINT,
	IN 	p_IdUsuario 						BIGINT,
    IN 	p_IdModulo 							NVARCHAR(3),
    IN 	p_UsaReglasNegocio 					NVARCHAR(1),
    
    IN 	p_PerfilFondo						TABLE (
    											"IdInterno"		BIGINT,
    											"TipoPerfil"	NVARCHAR(40),
    											"IdPerfil"		BIGINT
    										),
    OUT p_out_Resultado						TABLE (
    											"IdInterno"		BIGINT,
    											"TipoPerfil"	NVARCHAR(40),
    											"IdPerfil"		BIGINT,
    											"IdEmpresa"		BIGINT
    										),
    OUT p_out_AdvertenciasContador 		INT,
   	OUT p_out_AdvertenciasDetalle 		TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),
   	OUT p_out_ErroresContador 			INT,
	OUT p_out_ErroresDetalle 			TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
)
LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------
	
	DECLARE v_out_AdvertenciasDetalle 	TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(5000),"NombreParametro" NVARCHAR(5000));
	DECLARE v_out_ErroresDetalle  		TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(5000),"NombreParametro" NVARCHAR(5000));
	BEGIN
		v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
		v_out_ErroresDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
	END;
	
	BEGIN
		DECLARE SALIDA_CONTROLADA CONDITION;
		DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA  
		BEGIN
			select count(1) into p_out_ErroresContador 
			from :v_out_ErroresDetalle;
			select count(1) into p_out_AdvertenciasContador 
			from :v_out_AdvertenciasDetalle;
			
			v_TablaCodigosMensajes = SELECT "Codigo" AS "CodigoMensaje",
									"NombreParametro" as "CodigoDato",
									"ValorParametro" as "ValorDato" 
									FROM :v_out_ErroresDetalle
									UNION ALL
									SELECT "Codigo" AS "CodigoMensaje",
									"NombreParametro" as "CodigoDato",
									"ValorParametro" as "ValorDato" 
									FROM :v_out_AdvertenciasDetalle;
									
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
			
			p_out_ErroresDetalle = SELECT 	"errores"."IdInterno",
											"errores"."Codigo",
											"msj"."Descripcion"
									FROM :v_out_ErroresDetalle AS "errores"
									INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "errores"."Codigo" = "msj"."Codigo";
			
			p_out_AdvertenciasDetalle = SELECT 	"adv"."IdInterno",
												"adv"."Codigo",
												"adv"."RequiereAutorizacion",
												"msj"."Descripcion"
										FROM :v_out_AdvertenciasDetalle AS "adv"
										INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "adv"."Codigo" = "msj"."Codigo"; 
		END;
		
		p_out_AdvertenciasContador	:= 0;
		p_out_ErroresContador		:= 0;
		
		BEGIN
		
		DECLARE v_FechaSistema 				NVARCHAR(10);
		DECLARE v_resultadoRegla 			NVARCHAR(40);
		DECLARE v_count_perfiles			BIGINT;
		DECLARE v_count_perfiles_maestro	BIGINT;
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'ADMINISTRAR PERFIL FM','INICIO','INICIO');
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		
		if :v_resultadoRegla = 'RECH' then
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		end if;
		
		SELECT COUNT(1) AS "Count" into v_count_perfiles
		FROM :p_PerfilFondo;
		
		if (v_count_perfiles = 0) THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_INGRESARON_PERFILES' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		v_perfiles_maestro = SELECT "PerfilFondo"."IdInterno",
									"PerfilFondo"."TipoPerfil",
									"PerfilFondo"."IdPerfil",
									:p_IdEmpresa AS "IdEmpresa",
									CASE WHEN COALESCE("Configuracion"."Key",'-1') = '-1' THEN 'ERR' ELSE 'OK' END AS "ResultadoTipoPerfil",
									CASE WHEN COALESCE("ConsultarPerfil"."IdPerfil",'-1') = '-1' THEN 'ERR' ELSE 'OK' END AS "ResultadoPerfil"
							FROM :p_PerfilFondo AS "PerfilFondo"
							LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" AS "Configuracion" ON ("Configuracion"."Key" = "PerfilFondo"."TipoPerfil")
							LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarPerfiles" AS "ConsultarPerfil" ON ("ConsultarPerfil"."IdPerfil" = "PerfilFondo"."IdPerfil");
		
		
		BEGIN
			v_out_ErroresDetalle = SELECT "IdInterno", 'TIPO_PERFIL_INVALIDO' as "Codigo", '' as "ValorParametro", '' AS "NombreParametro"
								   		FROM :v_perfiles_maestro AS "PerfilMaestro" WHERE "ResultadoTipoPerfil" = 'ERR'
								   UNION ALL
								   SELECT "IdInterno", 'PERFIL_NO_EXISTE' as "Codigo", '' as "ValorParametro", '' AS "NombreParametro"
								   		FROM :v_perfiles_maestro AS "PerfilMaestro" WHERE "ResultadoPerfil" = 'ERR';
								   		
			SELECT COUNT(1) as "Count" INTO v_count_perfiles_maestro
			FROM  :v_perfiles_maestro WHERE "ResultadoTipoPerfil" = 'OK' AND "ResultadoPerfil" = 'OK';
			IF (v_count_perfiles_maestro = 0) THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;
		END;
		
		UPDATE PERFIL
		SET "Value" = PERFIL_MAESTRO."IdPerfil"
		FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" AS PERFIL
		INNER JOIN :v_perfiles_maestro AS PERFIL_MAESTRO ON (PERFIL_MAESTRO."TipoPerfil" = PERFIL."Key")
		WHERE PERFIL_MAESTRO."ResultadoTipoPerfil" = 'OK' AND PERFIL_MAESTRO."ResultadoPerfil" = 'OK';
		
		p_out_Resultado = SELECT "IdInterno",
								 "TipoPerfil",
								 "IdPerfil",
								 "IdEmpresa"
						  FROM :v_perfiles_maestro
						  WHERE "ResultadoTipoPerfil" = 'OK' AND "ResultadoPerfil" = 'OK';
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		
		v_log = SELECT 
				"IdInterno" AS "IdCabeceraLog"
				,'MODIFICACION' AS "IdTipoLog"
				,'FMM' AS "IdModulo"
				,:p_IdUsuario AS "IdUsuario"
				,:p_IdEmpresa AS "IdEmpresa"
				,:v_FechaSistema AS "FechaSistemaLog"
				,:p_IdUsuario AS "IdUsuarioAtendido"
				,0  AS "IdPersona"
				,0  AS "IdCliente"
				,'' AS "EstadoCliente"
				,'' AS "NombreCliente"
				,'' AS "IdTipoDocumentoIdentidad"
				,'' AS "IdDocumentoIdentidad"
				,0 AS "IdPortafolio"
				,'' AS "EstadoPortafolio"
				,'ADMINISTRAR_PERFIL_FMM' AS "IdTipoRegistro"
				,"IdInterno" AS "IdRegistro"
				,'' AS "EstadoRegistro"
				,'' AS "IdTipoOperacion"
				,0 AS "Folio"
				,0 AS "Cantidad"
				,0 AS "PrecioTasa"
				,0 AS "Monto"
				,'' AS "IdMonedaOrigen"
				,'' AS "IdMonedaLiquidacion"
				,'' AS "TipoCambio"
				,'' AS "Nemo"
				,0 AS "IdProceso"
				,'' AS "EstadoProceso"
				,'' AS "NombreProceso"
			FROM :v_perfiles_maestro;
		
		v_DETALLELOG = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
		
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_DETALLELOG);
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'ADMINISTRAR PERFIL FM','FIN','FIN');
		
		SIGNAL SALIDA_CONTROLADA;
		
		END;
	END;
END;
		
		
		
