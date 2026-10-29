SET SCHEMA "BSCL"; 
GO
DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::CrearSolicitudAGF";
GO
CREATE PROCEDURE
--  Proposito: Crear Solicitud para informar a la AGF
--  Autor: - Gat-Blac 
--  Fecha Creacion: 08/11/2016
"BSCL.FondoMutuo.BD.Servicios::CrearSolicitudAGF" (
	--VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 						BIGINT,
    IN p_IdUsuario						BIGINT,
    IN p_IdModulo						NVARCHAR(3),
    IN p_EsManual						NVARCHAR(1) DEFAULT 'N', -- Parametro para evitar hacer de nuevo la validaciones basicas por si ya se hicieron en otra llamada.    
	IN p_Solicitudes					TABLE	(
		    									"IdSolicitudAporteRescate"	BIGINT
		    							   		),
	OUT p_out_Resultado					TABLE	(
												"IdSolicitudAporteRescate"				BIGINT			,
		 										"IdSolicitudAporteRescateRelacionado"	BIGINT			,
		 										"DocumentoIdentidad" 					NVARCHAR(40)	,
												"CodigoPortafolio" 						NVARCHAR(100)	,
												"FechaLiquidacion"						DATE         	,
												"FechaAceptacion"   					DATE         	,
												"TipoSolicitud"							NVARCHAR(3)	 	,
												"IdEstadoSolicitud"						NVARCHAR(3)  	,
												"IdSerieFondo"          				NVARCHAR(40) 	,
												"IdUsuario"             				BIGINT          ,
												"NumeroCuotas"          				DECIMAL(26,8) 	,
												"ValorCuota"            				DECIMAL(26,8)   ,
												"Monto"									DECIMAL(26,8)	,
												"CalculoPorMontoCuota"					NVARCHAR(1)		,
												"IdMoneda"								NVARCHAR(3)		,
												"PropositoSerie"        				NVARCHAR(30)    ,
												"IdSerieFondoDestino"   				NVARCHAR(40)  	,
												"IdEstadoSerieFondo"					NVARCHAR(3)		,
												"IndicadorMLT"          				NVARCHAR(1)     ,
												"IdEstado"								NVARCHAR(3)		,
												"IdEmpresa"             				BIGINT          
		 										),
	OUT p_out_AdvertenciasContador 				INT,
	OUT p_out_AdvertenciasDetalle 				TABLE ("IdSolicitudAporteRescate"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000)),
	OUT p_out_ErroresContador 					INT,
	OUT p_out_ErroresDetalle 					TABLE ("IdSolicitudAporteRescate"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))
	)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS 
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------
	DECLARE v_out_AdvertenciasDetalle 	TABLE ("IdSolicitudAporteRescate" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	DECLARE v_out_ErroresDetalle  		TABLE ("IdSolicitudAporteRescate" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	BEGIN
		v_out_AdvertenciasDetalle = SELECT 0 as "IdSolicitudAporteRescate" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
		v_out_ErroresDetalle 	  = SELECT 0 as "IdSolicitudAporteRescate" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
	END;
	BEGIN
		DECLARE SALIDA_CONTROLADA CONDITION;		
		DECLARE EXIT HANDLER FOR SQLEXCEPTION
		BEGIN
			RESIGNAL;
		END; 
		
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
			
			p_out_ErroresDetalle = SELECT 	"errores"."IdSolicitudAporteRescate",
											"errores"."Codigo",
											"msj"."Descripcion"
									FROM :v_out_ErroresDetalle AS "errores"
									INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "errores"."Codigo" = "msj"."Codigo";
			
			p_out_AdvertenciasDetalle = SELECT 	"adv"."IdSolicitudAporteRescate",
												"adv"."Codigo",
												"adv"."RequiereAutorizacion",
												"msj"."Descripcion"
										FROM :v_out_AdvertenciasDetalle AS "adv"
										INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "adv"."Codigo" = "msj"."Codigo";
										
		END;
		
		p_out_AdvertenciasContador	:= 0;
		p_out_ErroresContador		:= 0;
		
		BEGIN
			DECLARE v_resultadoRegla 		NVARCHAR(40);
			DECLARE v_FechaSistema			NVARCHAR(10);
			DECLARE v_count_solicitudes		BIGINT;
			DECLARE v_solicitudes_repetidas	BIGINT;
			DECLARE v_count_correctas		BIGINT;
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CREAR SOLICITUD AGF','INICIO','INICIO');
			
			IF :p_EsManual = 'S' THEN
				
				CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
				
				IF :v_resultadoRegla = 'RECH' THEN
					v_out_ErroresDetalle = SELECT 0 as "IdSolicitudAporteRescate", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
					SIGNAL SALIDA_CONTROLADA; 	
				END IF;
			
			END IF;
			
			SELECT COUNT(1) as "Count" into v_count_solicitudes
			FROM :p_Solicitudes;
			
			IF v_count_solicitudes = 0 THEN
				v_out_ErroresDetalle = SELECT 0 as "IdSolicitudAporteRescate", 'NO_SE_INGRESARON_SOLICITUDES' AS "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM DUMMY;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			SELECT TOP 1 COUNT("IdSolicitudAporteRescate") as "Count" into v_solicitudes_repetidas
			FROM :p_Solicitudes
			GROUP BY "IdSolicitudAporteRescate"
			ORDER BY "Count" DESC;
			
			IF v_solicitudes_repetidas > 1 THEN
				v_out_ErroresDetalle = SELECT 0 as "IdSolicitudAporteRescate", 'SOLICITUDES_REPETIDAS' AS "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM DUMMY;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
			
			v_beneficiarios = SELECT  DISTINCT "SolicitudAporteRescate"."DocumentoIdentidad",
									  "SolicitudAporteRescate"."CodigoPortafolio",
									  "SolicitudAporteRescate"."NombreCompleto",
									  "SolicitudAporteRescate"."IdCliente",
									  "SolicitudAporteRescate"."IdPortafolio" 
							  FROM :p_Solicitudes AS "SolicitudesEntrada"
							  INNER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" AS "SolicitudAporteRescate" ON ("SolicitudesEntrada"."IdSolicitudAporteRescate" = "SolicitudAporteRescate"."IdSolicitudAporteRescate")
							  LEFT JOIN "BSCL.FondoMutuo.BD::FMM.InterfazAgf" AS "Interfaz" ON ("Interfaz"."DocumentoIdentidad" = "SolicitudAporteRescate"."DocumentoIdentidad" AND "Interfaz"."CodigoPortafolio" = "SolicitudAporteRescate"."CodigoPortafolio")
							  WHERE COALESCE("Interfaz"."IdInterfazAgf",0) = 0;
			
			v_beneficiarios_insertar = SELECT "BSCL.FondoMutuo.BD.Secuencias::InterfazAgf".NEXTVAL as "IdInterfazAgf",
											  "DocumentoIdentidad",
											  "CodigoPortafolio",
											  "NombreCompleto",
											  :v_FechaSistema AS "FechaIngreso",
											  CURRENT_UTCTIMESTAMP AS "FechaIngresoRegistro",
											  'PEN' AS "IdEstado",
										   	  'PA'  AS "TipoRegistro",
										   	  "IdCliente",
										   	  "IdPortafolio",
										   	  :p_IdEmpresa AS "IdEmpresa"
										FROM :v_beneficiarios;
			
			v_solicitudes_validacion = SELECT "SolicitudesEntrada"."IdSolicitudAporteRescate",
									   "SolicitudAporteRescate"."IdSolicitudAporteRescateRelacionado",
									   "SolicitudAporteRescate"."DocumentoIdentidad",
								       "SolicitudAporteRescate"."CodigoPortafolio",
								       "SolicitudAporteRescate"."FechaLiquidacion",
								       "SolicitudAporteRescate"."FechaAceptacion",
								       "SolicitudAporteRescate"."TipoSolicitud",
								       "SolicitudAporteRescate"."EstadoSolicitud" as "IdEstadoSolicitud",
								       "SolicitudAporteRescate"."IdSerieFondo",
								       "SolicitudAporteRescate"."IdUsuario",
								       "SolicitudAporteRescate"."NumeroCuotas",
								       "SolicitudAporteRescate"."ValorCuota",
								       "SolicitudAporteRescate"."Monto",
								       "SolicitudAporteRescate"."CalculoPorMontoCuota",
								       "SolicitudAporteRescate"."IdMoneda",
								       "SolicitudAporteRescate"."IdRegimenTributario" AS "PropositoSerie",
								       "SolicitudAporteRescate"."IdSerieFondoDestino",
								       "SolicitudAporteRescate"."IdEstadoSerieFondo" AS "IdEstadoSerieFondo",
									   "SolicitudAporteRescate"."IndicadorMLT",
									   "SolicitudAporteRescate"."NombreCompleto",
									   :v_FechaSistema AS "FechaIngreso",
										CURRENT_UTCTIMESTAMP AS "FechaIngresoRegistro",
									   'PEN' AS "IdEstado",
									   CASE WHEN "SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo" IN ('RESTRANSF','RESTRASP') THEN 'TR' ELSE 'AR' END  AS "TipoRegistro",
										CASE WHEN COALESCE("SolicitudAporteRescate"."IdSolicitudAporteRescate",0) = 0 THEN 'ERR' ELSE 'OK' END AS "Resultado",   
									   :p_IdEmpresa AS "IdEmpresa"
								    FROM :p_Solicitudes AS "SolicitudesEntrada"
								    LEFT JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" AS "SolicitudAporteRescate" ON ("SolicitudesEntrada"."IdSolicitudAporteRescate" = "SolicitudAporteRescate"."IdSolicitudAporteRescate");
			
			SELECT COUNT(1) into v_count_correctas
			FROM :v_solicitudes_validacion
			WHERE "Resultado" = 'OK';
			
			v_out_ErroresDetalle = SELECT "IdSolicitudAporteRescate" as "IdSolicitudAporteRescate", 'NO_SE_ENCONTRO_SOLICITUD' AS "Codigo", 
								   "IdSolicitudAporteRescate" as "ValorParametro",'IdSolicitudAporteRescate' as "NombreParametro" 
								   FROM :v_solicitudes_validacion
								   WHERE "Resultado" = 'ERR';
			
			IF v_count_correctas = 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			v_solicitudes_maestro = SELECT "BSCL.FondoMutuo.BD.Secuencias::InterfazAgf".NEXTVAL as "IdInterfazAgf",
										   "IdSolicitudAporteRescate",
										   "IdSolicitudAporteRescateRelacionado",
										   "DocumentoIdentidad",
									       "CodigoPortafolio",
									       "FechaLiquidacion",
									       "FechaAceptacion",
									       "TipoSolicitud",
									       "IdEstadoSolicitud",
									       "IdSerieFondo",
									       "IdUsuario",
									       "NumeroCuotas",
									       "ValorCuota",
									       "Monto",
									       "CalculoPorMontoCuota",
									       "IdMoneda",
									       "PropositoSerie",
									       "IdSerieFondoDestino",
									       "IdEstadoSerieFondo",
										   "IndicadorMLT",
										   "NombreCompleto",
										   "FechaIngreso",
										   "FechaIngresoRegistro",
										   "IdEstado",
										   "TipoRegistro",
										   "IdEmpresa"
								    FROM :v_solicitudes_validacion
								    WHERE "Resultado" = 'OK';
								    
			INSERT INTO "BSCL.FondoMutuo.BD::FMM.InterfazAgf" (
				"IdInterfazAgf"			 ,	"DocumentoIdentidad" ,	"CodigoPortafolio"	,	"NombreCompleto"			,
				"IdEstado"				 ,  "TipoRegistro"		 , 	"FechaIngreso"		, 	"FechaIngresoRegistro"		,
				"IdEmpresa"				 ,	"IdCliente"			 ,	"IdPortafolio"
			)
			SELECT "IdInterfazAgf"			 ,	"DocumentoIdentidad" ,	"CodigoPortafolio"	,	"NombreCompleto"			,
				   "IdEstado"				 ,  "TipoRegistro"		 , 	"FechaIngreso"		, 	"FechaIngresoRegistro"		,
				   "IdEmpresa"				 , 	"IdCliente"		 	 ,	"IdPortafolio"
			FROM :v_beneficiarios_insertar;
								
			INSERT INTO "BSCL.FondoMutuo.BD::FMM.InterfazAgf" (
					"IdInterfazAgf"       , "DocumentoIdentidad" , "CodigoPortafolio"   , "FechaLiquidacion"			,
			        "FechaAceptacion"     , "TipoSolicitud"      , "IdEstadoSolicitud"  , "IdSerieFondo"				,
			        "IdUsuario"           , "NumeroCuotas"       , "ValorCuota"         , "Monto"						,
			        "CalculoPorMontoCuota", "IdMoneda"           , "PropositoSerie"		, "IdSerieFondoDestino"			, "IdEstadoSerieFondo", 
			        "IndicadorMLT"        , "IdEstado"			 , "IdEmpresa"			, "IdSolicitudAporteRescate"	, "IdSolicitudAporteRescateRelacionado"	,
					"TipoRegistro"		  , "NombreCompleto"	 , "FechaIngreso"		, "FechaIngresoRegistro")
			SELECT "IdInterfazAgf"        , "DocumentoIdentidad" , "CodigoPortafolio"   , "FechaLiquidacion"			,
			        "FechaAceptacion"     , "TipoSolicitud"      , "IdEstadoSolicitud"  , "IdSerieFondo"				,
			        "IdUsuario"           , "NumeroCuotas"       , "ValorCuota"         , "Monto"						,
			        "CalculoPorMontoCuota", "IdMoneda"           , "PropositoSerie"		, "IdSerieFondoDestino"			, "IdEstadoSerieFondo", 
			        "IndicadorMLT"        , "IdEstado"			 , "IdEmpresa"			, "IdSolicitudAporteRescate"	, "IdSolicitudAporteRescateRelacionado"	,
			        "TipoRegistro"		  , "NombreCompleto"	 , "FechaIngreso"		, "FechaIngresoRegistro"		
			FROM :v_solicitudes_maestro;

			p_out_Resultado = SELECT
								"IdSolicitudAporteRescate" 
								,"IdSolicitudAporteRescateRelacionado"
								,"DocumentoIdentidad"
								,"CodigoPortafolio"
								,"FechaLiquidacion"
								,"FechaAceptacion"
								,"TipoSolicitud"
								,"IdEstadoSolicitud"
								,"IdSerieFondo"
								,"IdUsuario"
								,"NumeroCuotas"
								,"ValorCuota"
								,"Monto"
								,"CalculoPorMontoCuota"
								,"IdMoneda"
								,"PropositoSerie"
								,"IdSerieFondoDestino"
								,"IdEstadoSerieFondo"
								,"IndicadorMLT"
								,"IdEstado"
								,"IdEmpresa"
							  FROM :v_solicitudes_maestro;
							  
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CREAR SOLICITUD AGF','FIN','FIN');
			
			SIGNAL SALIDA_CONTROLADA;
		
		END;
		
	END;
	
END;
						
GO