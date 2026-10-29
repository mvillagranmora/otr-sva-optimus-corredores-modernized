SET SCHEMA "BSCL"; 

GO 

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::PAU.CerrarModulo";

GO

CREATE PROCEDURE
--  Proposito: Cerrar Modulo
--  Autor: - Gat-Blac 
--  Fecha Creacion: 31/05/2017
"BSCL.Plataforma.BD.Servicios::PAU.CerrarModulo" (
	 --VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 						BIGINT,
	IN p_IdUsuario 						BIGINT,
    IN p_IdModulo 						NVARCHAR(3),
	IN p_Modulo 				TABLE	(
			    					  		"CodigoModulo" 					NVARCHAR(40),
			    					  		"Estado"						NVARCHAR(3),
			    					  		"CodigoUsuario"					NVARCHAR(40)
		    					  		),
  	OUT p_out_Resultado			TABLE	(
  											"CodigoModulo"					NVARCHAR(40),
  											"IdEstado"						NVARCHAR(3),
  											"NombreModulo"                  NVARCHAR(40)
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
	DECLARE v_out_AdvertenciasDetalle 		  	TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520));
	DECLARE v_out_ErroresDetalle  			  	TABLE ("IdInterno" BIGINT,"Codigo" NVARCHAR(100),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520));
	BEGIN
		v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" from DUMMY where 1=2;
		v_out_ErroresDetalle 	  = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" from DUMMY where 1=2;
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
			
			v_TablaCodigosMensajes = SELECT 0 AS "IdInterno", 
									"Codigo" AS "Codigo",
									"CodigoDato1" as "CodigoDato1",
									"ValorDato1" as "ValorDato1",
									"CodigoDato2" as "CodigoDato2",
									"ValorDato2" as "ValorDato2"
									FROM :v_out_ErroresDetalle
									UNION ALL
									SELECT 0 AS "IdInterno", 
									"Codigo" AS "Codigo",
									"CodigoDato1" as "CodigoDato1",
									"ValorDato1" as "ValorDato1",
									"CodigoDato2" as "CodigoDato2",
									"ValorDato2" as "ValorDato2"
									FROM :v_out_AdvertenciasDetalle;
									
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
			
			p_out_ErroresDetalle = SELECT 	DISTINCT "errores"."IdInterno",
											"errores"."Codigo",
											"msj"."Descripcion"
									FROM :v_out_ErroresDetalle AS "errores"
									INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "errores"."Codigo" = "msj"."Codigo"
									GROUP BY "errores"."IdInterno","errores"."Codigo","msj"."Descripcion";
			
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
			DECLARE v_count_sistema_entregado       BIGINT;
			DECLARE v_count_mesa_abierta            BIGINT;
			DECLARE v_count_mesa_modulo            BIGINT;
			DECLARE v_codigoModulo					NVARCHAR(40);
		
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CERRAR MODULO APERTURA CIERRE','INICIO','INICIO');
				    				
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
			
			if :v_resultadoRegla = 'RECH' then
				v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo",'' as "CodigoDato1",
											  '' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" FROM :v_out_ErroresDetalle_Basicos;
				SIGNAL SALIDA_CONTROLADA; 	
			end if;
		
			SELECT COUNT(1) AS "Count" into v_count_modulos
			FROM :p_Modulo;
			
			if (v_count_modulos = 0) THEN
				v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_INGRESARON_MODULOS' AS "Codigo",'' as "CodigoDato1",
											  '' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2"  FROM DUMMY;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			SELECT COUNT(1) AS "Count" into v_count_modulo_sistema
			FROM :p_Modulo "P"
			WHERE "P"."CodigoModulo" = 'SIS';
			
			if (v_count_modulo_sistema > 0) THEN
				v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'MODULO_SISTEMA_NO_SE_PUEDE_CERRAR_DEBE_ENTREGAR' AS "Codigo",'' as "CodigoDato1",
											  '' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2"  FROM DUMMY;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			SELECT COUNT(1) AS "Count" into v_count_sistema_entregado
			FROM "BSCL.Plataforma.BD::PAU.Modulo" "M"
			WHERE "M"."CodigoModulo" = 'ENT'
			AND "M"."IdEstado" = 'SIS';
			
			if (v_count_sistema_entregado > 0) THEN
				v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_PUEDE_CERRAR_SISTEMA_ENTREGADO' AS "Codigo",'' as "CodigoDato1",
											  '' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2"  FROM DUMMY;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
		
			--*
			-- Condicion para Cierre de mesas
			--*
			------------------------------------------------------------
			SELECT COUNT(1) INTO v_count_mesa_modulo FROM :p_Modulo WHERE "CodigoModulo"  IN('MSP', 'MRF', 'MFW');
            
		      IF(v_count_mesa_modulo > 0) THEN 
		      
		      	  	-- Valida que mesa (spot o renta fija o forward) no este abierta
		      	  
				 	SELECT "CodigoModulo" INTO v_codigoModulo FROM :p_Modulo WHERE "CodigoModulo" IN('MSP', 'MRF', 'MFW');
		      	  
				 	SELECT COUNT("IdMesa") INTO v_count_mesa_abierta
				 		FROM "BSCL.Mesa.BD.Vistas::ConsultarMesas" MES
					  	INNER JOIN "BSCL.Plataforma.BD.Vistas::PAU.ConsultarModulosDetalle" MDL 
					  		ON (MDL."CodigoModulo" = MES."CodigoModulo") 
						INNER JOIN  :p_Modulo P 
					  		ON (P."CodigoModulo" = MES."CodigoModulo" AND MDL."IdEmpresa" = :p_IdEmpresa)
					 			WHERE MES."IdEstadoMesa" = 'A' AND MES."CodigoModulo" = :v_codigoModulo AND Mes."IdEstadoRegistro" = 'ACT';
				 	
				 	IF (v_count_mesa_abierta > 0) THEN
				        v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'MODULO_CON_MESA_ABIERTA' as "Codigo",'' as "CodigoDato1",
				                                '' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" FROM dummy;
				        SIGNAL SALIDA_CONTROLADA;
				    END IF;
		     END IF;
   		----------------------------------------------------------------
			
			v_modulo_validacion_estado = SELECT
											"p_Modulo"."CodigoModulo"
											,"p_Modulo"."Estado"		
									  		,"p_Modulo"."CodigoUsuario"	
									  		,:p_IdEmpresa AS "IdEmpresa"
									  		,CASE WHEN COALESCE("Modulo"."CodigoModulo",'-1') = '-1' THEN 'OK' ELSE 'ERR' END AS "Resultado"
									  	FROM :p_Modulo AS "p_Modulo"
									  	LEFT OUTER JOIN "BSCL.Plataforma.BD::PAU.Modulo" AS "Modulo" ON ("Modulo"."CodigoModulo" = "p_Modulo"."CodigoModulo" 
									  														  AND :p_IdEmpresa = "Modulo"."IdEmpresa"
									  														  AND "Modulo"."IdEstado" = "p_Modulo"."Estado");
			
			BEGIN
				v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'MODULO_YA_CERRADO' as "Codigo", 'CodigoModulo' as "CodigoDato1",
											  "Modulos"."CodigoModulo" as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" 
									   FROM :v_modulo_validacion_estado AS "Modulos" WHERE "Resultado" = 'ERR';
				
				SELECT COUNT(1) as "Count" INTO v_count_modulos_estado
				FROM :v_modulo_validacion_estado WHERE "Resultado" = 'OK';

				IF (v_count_modulos_estado = 0) THEN
					SIGNAL SALIDA_CONTROLADA;
				END IF;
			END;
			
			v_procesos_ejecutados = SELECT
										 "p_Modulo"."CodigoModulo"	
								  		,"P"."CodigoProceso"
								  		,"P"."DescripcionProceso"
								  		,:p_IdEmpresa AS "IdEmpresa"
								  		,"ModuloPla"."Nombre" AS "NombreModulo"
								  		,CASE WHEN COALESCE("CP"."IdControlProceso",'-1') = '-1' THEN 'ERR' ELSE 'OK' END AS "Resultado"
								    FROM :p_Modulo AS "p_Modulo"
								    LEFT OUTER JOIN "BSCL.Plataforma.BD::PAU.Proceso" AS "P" ON ("P"."CodigoModulo" = "p_Modulo"."CodigoModulo" AND
								  														        :p_IdEmpresa = "P"."IdEmpresa" AND "P"."EsApertura" = 'N' AND "P"."IdEstado" = 'ACT')
								    LEFT OUTER JOIN "BSCL.Plataforma.BD::PAU.ControlProceso" AS "CP" ON ("CP"."CodigoModulo" = "P"."CodigoModulo" AND
								  	              														 "CP"."CodigoProceso" = "P"."CodigoProceso" AND
								  	              														 "CP"."FechaSistema" = :v_FechaSistema AND
								  	              														 "P"."IdEmpresa" = :p_IdEmpresa )
								  	LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Modulo" AS "ModuloPla" ON ("ModuloPla"."IdModulo" = "p_Modulo"."CodigoModulo");

								  	
			BEGIN

				v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'MODULO_CON_PROCESOS_NO_EJECUTADOS' as "Codigo",'Modulo' as "CodigoDato1",
											  "Procesos"."NombreModulo" as "ValorDato1",'Proceso' as "CodigoDato2","Procesos"."DescripcionProceso" as "ValorDato2"
									   FROM :v_procesos_ejecutados AS "Procesos" WHERE "Resultado" = 'ERR'
									   AND COALESCE("Procesos"."CodigoProceso",'') != '';

				SELECT COUNT(1) as "Count" INTO v_count_procesos_no_ejecutados
				FROM :v_procesos_ejecutados WHERE "Resultado" = 'ERR'
				AND COALESCE("CodigoProceso",'') != '';

				IF (v_count_procesos_no_ejecutados > 0) THEN
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
									    			   COALESCE("P5"."IdEstado",'CER') = 'CER' THEN 'OK' ELSE 'ERR' END AS "Resultado"
									   FROM :p_Modulo AS "P"
									   JOIN "BSCL.Plataforma.BD::PAU.Modulo" "p_Modulo" ON ("p_Modulo"."CodigoModulo" = "P"."CodigoModulo")
									   LEFT OUTER JOIN "BSCL.Plataforma.BD::PAU.Modulo" AS "P1" ON ("p_Modulo"."CodigoModuloDependenciaCierre1" = "P1"."CodigoModulo" AND
									  														           :p_IdEmpresa = "P1"."IdEmpresa")
									   LEFT OUTER JOIN "BSCL.Plataforma.BD::PAU.Modulo" AS "P2" ON ("p_Modulo"."CodigoModuloDependenciaCierre2" = "P2"."CodigoModulo" AND
									  														           :p_IdEmpresa = "P2"."IdEmpresa")
									   LEFT OUTER JOIN "BSCL.Plataforma.BD::PAU.Modulo" AS "P3" ON ("p_Modulo"."CodigoModuloDependenciaCierre3" = "P3"."CodigoModulo" AND
									  														           :p_IdEmpresa = "P3"."IdEmpresa")
									   LEFT OUTER JOIN "BSCL.Plataforma.BD::PAU.Modulo" AS "P4" ON ("p_Modulo"."CodigoModuloDependenciaCierre4" = "P4"."CodigoModulo" AND
									  														           :p_IdEmpresa = "P4"."IdEmpresa")
									   LEFT OUTER JOIN "BSCL.Plataforma.BD::PAU.Modulo" AS "P5" ON ("p_Modulo"."CodigoModuloDependenciaCierre5" = "P5"."CodigoModulo" AND
									  														           :p_IdEmpresa = "P5"."IdEmpresa");

			BEGIN

				v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'MODULOS_DEPENDIENTES_AUN_ABIERTOS' as "Codigo", 'CodigoModulo' as "CodigoDato1",
											  "Modulo"."CodigoModulo" as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2"
									   FROM :v_modulos_dependientes AS "Modulo" WHERE "Resultado" = 'ERR';
				
				SELECT COUNT(1) as "Count" INTO v_count_modulos_dependientes
				FROM :v_modulos_dependientes WHERE "Resultado" = 'ERR';

				IF (v_count_modulos_dependientes > 0) THEN
					SIGNAL SALIDA_CONTROLADA;
				END IF;
			END;
			
			UPDATE "Modulo"
			SET    "Modulo"."IdEstado" = "PM"."Estado",
				   "Modulo"."CodigoUsuario" = "PM"."CodigoUsuario",
				   "Modulo"."FechaUltimaActualizacion" = CURRENT_UTCTIMESTAMP
		    FROM "BSCL.Plataforma.BD::PAU.Modulo" "Modulo"
		    JOIN :p_Modulo "PM" ON ("PM"."CodigoModulo" = "Modulo"."CodigoModulo");
			
			p_out_Resultado = SELECT "Modulo"."CodigoModulo",
									 "Modulo"."IdEstado",
									 "ModuloPla"."Nombre" AS "NombreModulo"
							  FROM "BSCL.Plataforma.BD::PAU.Modulo" "Modulo"
		   					  JOIN :p_Modulo "P" ON ("P"."CodigoModulo" = "Modulo"."CodigoModulo")
		   					  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Modulo" AS "ModuloPla" ON ("ModuloPla"."IdModulo" = "Modulo"."CodigoModulo");
		   					  
		   	--* -----------------------------------------------
	        --* ACTUALIZAR TABLA TBBASIPA
	        --* -----------------------------------------------
	        
	        SELECT COUNT(1) INTO v_Count   
	        FROM :p_Modulo
	        WHERE "CodigoModulo" = 'MFW' AND "Estado" = 'CER';
	            
	        IF v_Count > 0 THEN
	        	CALL "BSCL.Mesa.BD.Servicios::FWD.GrabarReporteCarteraFWDBasilea" (  
	     			p_IdEmpresa                => :p_IdEmpresa
				    ,p_IdUsuario               => :p_IdUsuario
				    ,p_FechaOperacion          => :v_FechaSistema
				);
			END IF;
			
			INSERT INTO "BSCL.Plataforma.BD::PLA.MensajeWebsocket"(
			  "IdEmpresa", 
			  "FechaRegistro",
			  "Evento",
			  "DATA",
			  "EsProcesado" 
			)
			SELECT :p_IdEmpresa,
				   CURRENT_UTCTIMESTAMP,
				   'AVISO',
				   'El modulo ' || "ModuloPla"."Nombre" || ' ha sido cerrado por el usuario ' || "p_Modulo"."CodigoUsuario",
				   'N'
			FROM :p_Modulo AS "p_Modulo"
			LEFT OUTER JOIN "BSCL.Plataforma.BD::PAU.Modulo" AS "Modulo" ON ("Modulo"."CodigoModulo" = "p_Modulo"."CodigoModulo" 
				  														     AND :p_IdEmpresa = "Modulo"."IdEmpresa"
				  														     AND "Modulo"."IdEstado" = "p_Modulo"."Estado")
		    LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Modulo" AS "ModuloPla" ON ("ModuloPla"."IdModulo" = "Modulo"."CodigoModulo");
							
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