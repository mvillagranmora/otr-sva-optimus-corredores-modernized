SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.ActivarPeriodo";

--  Proposito: Activa un periodo completo para todos los esquemas.
--  Autor: Gat-Blac 
--  Fecha Creacion: 14/02/2017

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.ActivarPeriodo"(
	IN p_IdEmpresa 								BIGINT,
	IN p_IdUsuario 								BIGINT,
	IN p_IdModulo 								NVARCHAR(3),
    IN p_SimularOperacion 						NVARCHAR(1),
    IN p_UsaReglasNegocio 						NVARCHAR(1),
	IN p_Version								NVARCHAR(10),
	IN p_Periodicidad							NVARCHAR(1) DEFAULT 'A',
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
		DECLARE v_countEsquemas				BIGINT;
		DECLARE v_FechaSistema				NVARCHAR(10);
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'BSCL.Normativa.BD.Servicios::TRI.ActivarPeriodo','INICIO','INICIO');
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		
		IF :v_resultadoRegla = 'RECH' THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		END IF;
				
		IF (p_Version = '') THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_INGRESARON_VERSIONES' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		
		/**
		********** INICIO VALIDACIONES **************
		**/
		v_esquemas = SELECT	ESQUEMA."Esquema",
							ESQUEMA."Version"
					FROM "BSCL.Normativa.BD::DOC.Esquema" ESQUEMA
					WHERE ESQUEMA."Modulo" = 'TRI' AND ESQUEMA."Version" = :p_Version;
		
		
		SELECT COUNT(1) into v_countEsquemas
		FROM :v_esquemas;
		
		IF v_countEsquemas = 0 THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_EXISTEN_ESQUEMAS_PERIODO' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		/**
		********** FIN VALIDACIONES **************
		**/
		
		/**
		********** INICIO ACTIVAR **************
		**/
		
		IF :p_Periodicidad = 'A' THEN
			--TODO UPDATE PARA ACTIVAR ESQUEMAS
			UPDATE CONFIGURACION
			SET  CONFIGURACION."Valor" = :p_Version
			FROM "BSCL.Normativa.BD::NOR.Configuracion" CONFIGURACION
			WHERE "CodigoParametro" = 'PERIODO_TRIBUTARIO' AND "IdNormativa" = '' AND "IdEmpresa" = :p_IdEmpresa;
		END IF;
		
		IF :p_Periodicidad = 'M' THEN
			--TODO UPDATE PARA ACTIVAR ESQUEMAS MESUALES
			UPDATE CONFIGURACION
			SET  CONFIGURACION."Valor" = :p_Version
			FROM "BSCL.Normativa.BD::NOR.Configuracion" CONFIGURACION
			WHERE "CodigoParametro" = 'PERIODO_TRIBUTARIO_MENSUAL' AND "IdNormativa" = '' AND "IdEmpresa" = :p_IdEmpresa;
		END IF;
		
		IF :p_Periodicidad = 'T' THEN
			--TODO UPDATE PARA ACTIVAR ESQUEMAS TRIMESTRAL
			UPDATE CONFIGURACION
			SET  CONFIGURACION."Valor" = :p_Version
			FROM "BSCL.Normativa.BD::NOR.Configuracion" CONFIGURACION
			WHERE "CodigoParametro" = 'PERIODO_TRIBUTARIO_TRIMESTRAL' AND "IdNormativa" = '' AND "IdEmpresa" = :p_IdEmpresa;
		END IF;
		
		DELETE 
		FROM "BSCL.Normativa.BD::NOR.Configuracion" CONFIGURACION
		WHERE "CodigoParametro" = 'PERIODO_TRIBUTARIO_' || "IdNormativa" AND "IdEmpresa" = :p_IdEmpresa;
		
		/**
		********** FIN ACTIVAR **************
		**/
		
		
		v_log = SELECT 
						:p_Version AS "IdCabeceraLog"
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
						,:p_Version AS "IdRegistro"
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
					FROM DUMMY;
			
		v_DETALLELOG = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
		
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_DETALLELOG);
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'BSCL.Normativa.BD.Servicios::TRI.ActivarPeriodo','FIN','FIN');
		
		SIGNAL SALIDA_CONTROLADA;
		
		END;
	END;	
END;
