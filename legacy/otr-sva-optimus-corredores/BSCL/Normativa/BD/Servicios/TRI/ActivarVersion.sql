SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.ActivarVersion";

--  Proposito: Activa una version los esquemas seleccionados.
--  Autor: Gat-Blac 
--  Fecha Creacion: 14/02/2017

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.ActivarVersion"(
	IN p_IdEmpresa 								BIGINT,
	IN p_IdUsuario 								BIGINT,
	IN p_IdModulo 								NVARCHAR(3),
    IN p_SimularOperacion 						NVARCHAR(1),
    IN p_UsaReglasNegocio 						NVARCHAR(1),
	IN p_Versiones								TABLE (
					    							"IdInterno"					BIGINT,
					    							"Esquema"					NVARCHAR(10),
					    							"Version"					NVARCHAR(10),
					    							"FechaPresentacionLimite"	NVARCHAR(10)
					    						),
   	OUT p_out_AdvertenciasContador 				INT,
	OUT p_out_AdvertenciasDetalle 				TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),
	OUT p_out_ErroresContador 					INT,
	OUT p_out_ErroresDetalle 					TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
)

	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
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
		v_out_ErroresDetalle 	  = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
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
		DECLARE v_resultadoRegla 			NVARCHAR(40);
		DECLARE v_count						BIGINT;
		DECLARE v_countVersiones			BIGINT;
		DECLARE v_countRepetidos 			BIGINT;
		DECLARE v_FechaSistema				NVARCHAR(10);
		DECLARE v_countEsquemasValidados	BIGINT;
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'BSCL.Normativa.BD.Servicios::TRI.ActivarVersion','INICIO','INICIO');
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		
		IF :v_resultadoRegla = 'RECH' THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		END IF;
		
		SELECT COUNT(1) AS "Count" INTO v_countVersiones
		FROM :p_Versiones;
		
		IF (v_countVersiones = 0) THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_INGRESARON_VERSIONES' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		SELECT TOP 1 COUNT("IdInterno") AS "Count" INTO v_count
		FROM :p_Versiones
		GROUP BY "IdInterno"
		ORDER BY "Count" DESC;
		
		IF (v_count > 1) THEN
			v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'CORRELATIVO_INCORRECTO' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		SELECT TOP 1 COUNT("IdInterno") AS "Count" INTO v_countRepetidos
		FROM :p_Versiones
		GROUP BY "Esquema"
		ORDER BY "Count" DESC;
		
		IF (v_count > 1) THEN
			v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'ESQUEMA_REPETIDO' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		
		/**
		********** INICIO VALIDACIONES **************
		**/
		v_validaciones_esquemas = SELECT	VERSION."IdInterno",
											VERSION."Esquema",
											VERSION."Version",
											VERSION."FechaPresentacionLimite",
											CASE WHEN COALESCE(ESQUEMA."Esquema",'') = '' THEN 'ERR' ELSE 'OK' END AS "Resultado"
									FROM :p_Versiones VERSION 
									LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Esquema" ESQUEMA ON (ESQUEMA."Esquema" = VERSION."Esquema" AND VERSION."Version" = ESQUEMA."Version" AND ESQUEMA."Modulo" = 'TRI');
		
		
		v_out_ErroresDetalle = SELECT "IdInterno",'ESQUEMA_VERSION_NO_EXISTE' AS "Codigo","Esquema" AS "ValorParametro", 'Esquema' AS "NombreParametro"
							   FROM :v_validaciones_esquemas
							   WHERE "Resultado" = 'ERR'
							   UNION ALL
							   SELECT * FROM :v_out_ErroresDetalle;
		
		v_esquemas_validados = SELECT ESQUEMA."IdInterno",
									  ESQUEMA."Esquema",
									  ESQUEMA."Version",
									  ESQUEMA."FechaPresentacionLimite"
								FROM :v_validaciones_esquemas ESQUEMA
								WHERE "Resultado" = 'OK';
		
		SELECT COUNT(1) into v_countEsquemasValidados
		FROM :v_esquemas_validados;
		
		IF v_countEsquemasValidados = 0 THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		/**
		********** FIN VALIDACIONES **************
		**/
		
		/**
		********** INICIO ACTIVAR **************
		**/
		
		v_configuraciones_existentes = SELECT 	ESQUEMA."IdInterno",
												ESQUEMA."Esquema",
												ESQUEMA."Version",
												CONFIGURACION."IdNormativa"
										FROM :v_esquemas_validados AS ESQUEMA 
										INNER JOIN "BSCL.Normativa.BD::NOR.Configuracion" AS CONFIGURACION ON (CONFIGURACION."IdNormativa" = ESQUEMA."Esquema" AND CONFIGURACION."IdEmpresa" = :p_IdEmpresa)
										WHERE  CONFIGURACION."CodigoParametro" = 'PERIODO_TRIBUTARIO_' || ESQUEMA."Esquema";
		
		v_configuraciones_nuevas = SELECT 	ESQUEMA."IdInterno",
											ESQUEMA."Esquema",
											ESQUEMA."Version"
									FROM :v_esquemas_validados AS ESQUEMA
									LEFT OUTER JOIN "BSCL.Normativa.BD::NOR.Configuracion" AS CONFIGURACION ON (CONFIGURACION."IdNormativa" = ESQUEMA."Esquema" AND CONFIGURACION."CodigoParametro" = 'PERIODO_TRIBUTARIO_' ||  ESQUEMA."Esquema" AND CONFIGURACION."IdEmpresa" = :p_IdEmpresa)
									WHERE COALESCE(CONFIGURACION."CodigoParametro",'') = '';
									
		UPDATE CONFIGURACION
		SET  CONFIGURACION."Valor" = CONFIGURACIONES_EXISTENTES."Version"
		FROM "BSCL.Normativa.BD::NOR.Configuracion" CONFIGURACION
		INNER JOIN :v_configuraciones_existentes AS CONFIGURACIONES_EXISTENTES ON (CONFIGURACION."IdNormativa" = CONFIGURACIONES_EXISTENTES."IdNormativa" AND CONFIGURACION."IdEmpresa" = :p_IdEmpresa)
		WHERE "CodigoParametro" = 'PERIODO_TRIBUTARIO_' || "Esquema" AND "IdEmpresa" = :p_IdEmpresa;
		
		INSERT INTO "BSCL.Normativa.BD::NOR.Configuracion" 
		("IdEmpresa","CodigoParametro","Descripcion","Valor","IdNormativa")
		SELECT :p_IdEmpresa,'PERIODO_TRIBUTARIO_' || "Esquema",'Periodo Tributario para las DJ',"Version","Esquema"
		FROM :v_configuraciones_nuevas;
		
		UPDATE ESQUEMA
		SET  ESQUEMA."FechaPresentacionLimite" = ESQUEMAS_VALIDADOS."FechaPresentacionLimite"
		FROM "BSCL.Normativa.BD::DOC.Esquema" ESQUEMA
		INNER JOIN :v_esquemas_validados ESQUEMAS_VALIDADOS ON (ESQUEMAS_VALIDADOS."Esquema" = ESQUEMA."Esquema" AND ESQUEMA."Version" = ESQUEMAS_VALIDADOS."Version");
		
		/**
		********** FIN ACTIVAR **************
		**/
		
		
		v_log = SELECT 
						0 AS "IdCabeceraLog"
						,'MODIFICACION' AS "IdTipoLog"
						,'NOR' AS "IdModulo"
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
						,'ACTIVAR_VERSION' AS "IdTipoRegistro"
						,0 AS "IdRegistro"
						,'ACT' AS "EstadoRegistro"
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
					FROM :p_Versiones;
			
		v_DETALLELOG = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
		
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_DETALLELOG);
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'BSCL.Normativa.BD.Servicios::TRI.ActivarVersion','FIN','FIN');
		
		SIGNAL SALIDA_CONTROLADA;
		
		END;
	END;	
END;
