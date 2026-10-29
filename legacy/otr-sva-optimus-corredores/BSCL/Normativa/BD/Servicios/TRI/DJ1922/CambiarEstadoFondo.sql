SET SCHEMA BSCL;

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1922.CambiarEstadoFondo";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1922.CambiarEstadoFondo"
(
	-- ID empresa del login.
    IN p_IdEmpresa 	BIGINT		 ,
    -- ID usuario del login.
    IN p_IdUsuario 	BIGINT		 ,
    -- ID modulo invocador.
    IN p_IdModulo	NVARCHAR(3)	 ,
    
    IN p_Version	NVARCHAR(10),
    
    IN p_Fondos		TABLE(
    					"IdFondo"	NVARCHAR(40),
    					"IdEstado"	NVARCHAR(3)
    				),
    OUT p_out_AdvertenciasContador 		INT,
	OUT p_out_AdvertenciasDetalle 		TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),
	OUT p_out_ErroresContador 			INT,
	OUT p_out_ErroresDetalle 			TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
)
	LANGUAGE sqlscript
    SQL security invoker
    DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------
	DECLARE v_out_AdvertenciasDetalle 	TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	DECLARE v_out_ErroresDetalle  		TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
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
			DECLARE v_resultadoRegla 	NVARCHAR(40);
			DECLARE v_count				BIGINT;
			DECLARE v_count_correctos	BIGINT;
			DECLARE v_FechaSistema		NVARCHAR(10);
		
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'DJ1922.CambiarEstadoFondo','INICIO','INICIO');
				
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
			
			IF :v_resultadoRegla = 'RECH' THEN
				v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;
			
			SELECT COUNT(1) as "Count" into v_count
			FROM :p_Fondos;
			
			IF (v_count = 0) THEN
				v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_INGRESARON_FONDOS' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
			
			v_fondos_maestros = SELECT P_FONDOS."IdFondo"	AS "IdFondo",
									   'DJ1922'  			AS "Esquema",
									   :p_Version 	AS "Version",
									   P_FONDOS."IdEstado",
									   CASE WHEN COALESCE(FONDOS."IdFondo",'') = '' THEN 'ERR' ELSE 'OK' END AS "Resultado" 
								FROM :p_Fondos P_FONDOS 
								LEFT JOIN "BSCL.Normativa.BD::TRI.Fondos" FONDOS ON ('DJ1922' = FONDOS."Esquema" AND :p_Version = FONDOS."Version" 
																					AND FONDOS."IdFondo" = P_FONDOS."IdFondo");
			
			
			v_out_AdvertenciasDetalle = SELECT 0 AS "IdInterno", 'FONDO_NO_CONFIGURADO' AS "Codigo", 'N' AS "RequiereAutorizacion", "IdFondo" AS "ValorParametro", 'IdFondo' AS "NombreParametro"
										FROM :v_fondos_maestros 
										WHERE "Resultado" = 'ERR';
			
			v_fondos_correctos = SELECT "IdFondo",
										"Esquema",
										"Version",
										"IdEstado"
								 FROM :v_fondos_maestros
								 WHERE "Resultado" = 'OK';	
			
			SELECT COUNT(1) into v_count_correctos
			FROM :v_fondos_correctos;
			
			IF v_count_correctos = 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			UPDATE FONDOS
			SET "IdEstado" = P_FONDOS."IdEstado"
			FROM "BSCL.Normativa.BD::TRI.Fondos" FONDOS
			INNER JOIN :v_fondos_correctos P_FONDOS ON (P_FONDOS."Esquema" = FONDOS."Esquema" AND P_FONDOS."Version" = FONDOS."Version" 
														AND FONDOS."IdFondo" = P_FONDOS."IdFondo");
			
			v_log = SELECT	0 AS "IdCabeceraLog"
					,'MODIFICACION' AS "IdTipoLog"
					,:p_IdModulo  AS "IdModulo"
					,:p_IdUsuario AS "IdUsuario"
					,:p_IdEmpresa AS "IdEmpresa"
					,:v_FechaSistema AS "FechaSistemaLog"
					,:p_IdUsuario AS "IdUsuarioAtendido"
					,0 AS "IdPersona"
					,0 AS "IdCliente"
					,'' AS "EstadoCliente"
					,'' AS "NombreCliente"
					,'' AS "IdTipoDocumentoIdentidad"
					,'' AS "IdDocumentoIdentidad"
					,0 AS "IdPortafolio"
					,'' AS "EstadoPortafolio"
					,'ACTUALIZAR_FONDO_DJ1922' AS "IdTipoRegistro"
					,0 AS "IdRegistro"
					,"IdEstado" AS "EstadoRegistro"
					,'' AS "IdTipoOperacion"
					,0 AS "Folio"
					,0 AS "Cantidad"
					,0 AS "PrecioTasa"
					,0 AS "Monto"
					,'' AS "IdMonedaOrigen"
					,'' AS "IdMonedaLiquidacion"
					,'' AS "TipoCambio"
					,"IdFondo" AS "Nemo"
					,0 AS "IdProceso"
					,'' AS "EstadoProceso"
					,'' AS "NombreProceso"
				FROM :v_fondos_correctos;
			
			v_DETALLELOG = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
			
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_DETALLELOG);
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(1,CURRENT_UTCTIMESTAMP,1,'DJ1922.CambiarEstadoFondo','FIN','FIN');
		
			SIGNAL SALIDA_CONTROLADA;
		
		END;
	END;
END;
