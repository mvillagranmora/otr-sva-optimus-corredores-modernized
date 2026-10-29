SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::PAU.EntregarSistema";

CREATE PROCEDURE
--  Proposito: Entregar Sistema
--  Autor: - Gat-Blac 
--  Fecha Creacion: 31/05/2017
"BSCL.Plataforma.BD.Servicios::PAU.EntregarSistema" (
	 --VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 						BIGINT,
	IN p_IdUsuario 						BIGINT,
    IN p_IdModulo 						NVARCHAR(3),
    IN p_CodigoUsuario					NVARCHAR(40),
  	OUT p_out_Resultado			TABLE	(
  											"CodigoModulo"					NVARCHAR(40),
  											"IdEstado"						NVARCHAR(3)
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
	DECLARE v_out_AdvertenciasDetalle 		  	TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	DECLARE v_out_ErroresDetalle  			  	TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	BEGIN
		v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
		v_out_ErroresDetalle 	  = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
	END;
	BEGIN
		DECLARE SALIDA_CONTROLADA CONDITION;
		DECLARE v_FechaSistema NVARCHAR(10); 
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
			DECLARE v_count_modulos			  		BIGINT;
			DECLARE v_count_modulo_sistema	  		BIGINT;
			DECLARE v_count_modulos_estado	  		BIGINT;
			DECLARE v_count_procesos_no_ejecutados	BIGINT;
			DECLARE v_count_modulos_dependientes	BIGINT;
			DECLARE v_resultadoRegla 		  		NVARCHAR(40);
			DECLARE v_count					  		BIGINT;
			DECLARE v_count_familias_validacion 	BIGINT;
		
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CERRAR MODULO APERTURA CIERRE','INICIO','INICIO');
				    				
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
			
			if :v_resultadoRegla = 'RECH' then
				v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
				SIGNAL SALIDA_CONTROLADA; 	
			end if;
		
			v_modulo_validacion_estado = SELECT
											"Modulo"."CodigoModulo"
											,"Modulo"."IdEstado" as "Estado"		
									  		,"Modulo"."CodigoUsuario"	
									  		,:p_IdEmpresa AS "IdEmpresa"
									  		FROM "BSCL.Plataforma.BD::PAU.Modulo" AS "Modulo" 
									  	WHERE "Modulo"."CodigoModulo" = 'SIS'
  										AND "Modulo"."IdEmpresa" = :p_IdEmpresa;
			
			BEGIN
				v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'SISTEMA_YA_ENTREGADO' as "Codigo", "Modulos"."CodigoModulo" as "ValorParametro", 'CodigoModulo' AS "NombreParametro"
									   FROM :v_modulo_validacion_estado AS "Modulos" WHERE "Estado" = 'ENT';
				
				SELECT COUNT(1) as "Count" INTO v_count_modulos_estado
				FROM :v_modulo_validacion_estado WHERE "Estado" = 'ENT';

				IF (v_count_modulos_estado > 0) THEN
					SIGNAL SALIDA_CONTROLADA;
				END IF;
			END;
			
			v_modulos_dependientes = SELECT
											 "p_Modulo"."CodigoModulo"
									  		,:p_IdEmpresa AS "IdEmpresa"
									  		,CASE WHEN COALESCE("P1"."IdEstado",'CER') = 'CER' AND
									  				   COALESCE("P2"."IdEstado",'CER') = 'CER' AND
									  				   COALESCE("P3"."IdEstado",'CER') = 'CER' AND
									  				   COALESCE("P4"."IdEstado",'CER') = 'CER' AND
									    			   COALESCE("P5"."IdEstado",'CER') = 'CER' AND
									    			   COALESCE("P6"."IdEstado",'CER') = 'CER' AND
									    			   COALESCE("P7"."IdEstado",'CER') = 'CER' AND
									    			   COALESCE("P8"."IdEstado",'CER') = 'CER' AND
									    			   COALESCE("P9"."IdEstado",'CER') = 'CER' AND
									    			   COALESCE("P10"."IdEstado",'CER') = 'CER' AND
									    			   COALESCE("P11"."IdEstado",'CER') = 'CER' AND
									    			   COALESCE("P12"."IdEstado",'CER') = 'CER' AND
									    			   COALESCE("P13"."IdEstado",'CER') = 'CER' AND
									    			   COALESCE("P14"."IdEstado",'CER') = 'CER' AND
									    			   COALESCE("P15"."IdEstado",'CER') = 'CER' 
									    			   THEN 'OK' ELSE 'ERR' END AS "Resultado"
									   FROM "BSCL.Plataforma.BD::PAU.Modulo" "p_Modulo"
									   LEFT OUTER JOIN "BSCL.Plataforma.BD::PAU.Modulo" AS "P1" ON ("p_Modulo"."CodigoModuloDependenciaCierre1" = "P1"."CodigoModulo" AND
									  														           :p_IdEmpresa = "P1"."IdEmpresa")
									   LEFT OUTER JOIN "BSCL.Plataforma.BD::PAU.Modulo" AS "P2" ON ("p_Modulo"."CodigoModuloDependenciaCierre2" = "P2"."CodigoModulo" AND
									  														           :p_IdEmpresa = "P2"."IdEmpresa")
									   LEFT OUTER JOIN "BSCL.Plataforma.BD::PAU.Modulo" AS "P3" ON ("p_Modulo"."CodigoModuloDependenciaCierre3" = "P3"."CodigoModulo" AND
									  														           :p_IdEmpresa = "P3"."IdEmpresa")
									   LEFT OUTER JOIN "BSCL.Plataforma.BD::PAU.Modulo" AS "P4" ON ("p_Modulo"."CodigoModuloDependenciaCierre4" = "P4"."CodigoModulo" AND
									  														           :p_IdEmpresa = "P4"."IdEmpresa")
									   LEFT OUTER JOIN "BSCL.Plataforma.BD::PAU.Modulo" AS "P5" ON ("p_Modulo"."CodigoModuloDependenciaCierre5" = "P5"."CodigoModulo" AND
									  														           :p_IdEmpresa = "P5"."IdEmpresa")
									   LEFT OUTER JOIN "BSCL.Plataforma.BD::PAU.Modulo" AS "P6" ON ("p_Modulo"."CodigoModuloDependenciaCierre5" = "P6"."CodigoModulo" AND
									  														           :p_IdEmpresa = "P6"."IdEmpresa")
									   LEFT OUTER JOIN "BSCL.Plataforma.BD::PAU.Modulo" AS "P7" ON ("p_Modulo"."CodigoModuloDependenciaCierre5" = "P7"."CodigoModulo" AND
									  														           :p_IdEmpresa = "P7"."IdEmpresa")
									   LEFT OUTER JOIN "BSCL.Plataforma.BD::PAU.Modulo" AS "P8" ON ("p_Modulo"."CodigoModuloDependenciaCierre5" = "P8"."CodigoModulo" AND
									  														           :p_IdEmpresa = "P8"."IdEmpresa")
									   LEFT OUTER JOIN "BSCL.Plataforma.BD::PAU.Modulo" AS "P9" ON ("p_Modulo"."CodigoModuloDependenciaCierre5" = "P9"."CodigoModulo" AND
									  														           :p_IdEmpresa = "P9"."IdEmpresa")
									   LEFT OUTER JOIN "BSCL.Plataforma.BD::PAU.Modulo" AS "P10" ON ("p_Modulo"."CodigoModuloDependenciaCierre5" = "P10"."CodigoModulo" AND
									  														           :p_IdEmpresa = "P10"."IdEmpresa")
									   LEFT OUTER JOIN "BSCL.Plataforma.BD::PAU.Modulo" AS "P11" ON ("p_Modulo"."CodigoModuloDependenciaCierre5" = "P11"."CodigoModulo" AND
									  														           :p_IdEmpresa = "P11"."IdEmpresa")
									   LEFT OUTER JOIN "BSCL.Plataforma.BD::PAU.Modulo" AS "P12" ON ("p_Modulo"."CodigoModuloDependenciaCierre5" = "P12"."CodigoModulo" AND
									  														           :p_IdEmpresa = "P12"."IdEmpresa")
									   LEFT OUTER JOIN "BSCL.Plataforma.BD::PAU.Modulo" AS "P13" ON ("p_Modulo"."CodigoModuloDependenciaCierre5" = "P13"."CodigoModulo" AND
									  														           :p_IdEmpresa = "P13"."IdEmpresa")
									   LEFT OUTER JOIN "BSCL.Plataforma.BD::PAU.Modulo" AS "P14" ON ("p_Modulo"."CodigoModuloDependenciaCierre5" = "P14"."CodigoModulo" AND
									  														           :p_IdEmpresa = "P14"."IdEmpresa")
									   LEFT OUTER JOIN "BSCL.Plataforma.BD::PAU.Modulo" AS "P15" ON ("p_Modulo"."CodigoModuloDependenciaCierre5" = "P15"."CodigoModulo" AND
									  														           :p_IdEmpresa = "P15"."IdEmpresa")
									   
									   WHERE "p_Modulo"."CodigoModulo" = 'SIS';

			BEGIN
				v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'MODULOS_DEPENDIENTES_AUN_ABIERTOS' as "Codigo", "Modulo"."CodigoModulo" as "ValorParametro", 'CodigoModulo' AS "NombreParametro"
									   FROM :v_modulos_dependientes AS "Modulo" WHERE "Resultado" = 'ERR';
				
				SELECT COUNT(1) as "Count" INTO v_count_modulos_dependientes
				FROM :v_modulos_dependientes WHERE "Resultado" = 'ERR';

				IF (v_count_modulos_dependientes > 0) THEN
					SIGNAL SALIDA_CONTROLADA;
				END IF;
			END;
			
			UPDATE "Modulo"
			SET    "Modulo"."IdEstado" = 'ENT',
				   "Modulo"."CodigoUsuario" = :p_CodigoUsuario,
				   "Modulo"."FechaUltimaActualizacion" = CURRENT_UTCTIMESTAMP
		    FROM "BSCL.Plataforma.BD::PAU.Modulo" "Modulo"
		    WHERE "Modulo"."CodigoModulo" = 'SIS';
			
			p_out_Resultado = SELECT "Modulo"."CodigoModulo",
									 "Modulo"."IdEstado"
							  FROM "BSCL.Plataforma.BD::PAU.Modulo" "Modulo"
		   					  WHERE "Modulo"."CodigoModulo" = 'SIS';
								
			v_log = SELECT 
					0 AS "IdCabeceraLog"
					,'CERRAR' AS "IdTipoLog"
					,'PLA' AS "IdModulo"
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
					,'CERRAR_MODULO' AS "IdTipoRegistro"
					,0 AS "IdRegistro"
					,"Estado" AS "EstadoRegistro"
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
				FROM :v_modulo_validacion_estado;
			
			v_DETALLELOG = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
		
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_DETALLELOG);
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CERRAR MODULO APERTURA CIERRE','FIN','FIN');
								
			SIGNAL SALIDA_CONTROLADA;
		END;
	END;
	
END;
						
