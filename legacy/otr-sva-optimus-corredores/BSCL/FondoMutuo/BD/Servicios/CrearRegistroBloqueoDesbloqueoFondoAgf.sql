SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::CrearRegistroBloqueoDesbloqueoFondoAgf";

CREATE PROCEDURE
--  Proposito: Crear Registro de Bloqueo y Desbloqueo de Fondo AGF.
--  Autor: - Gat-Blac 
--  Fecha Creacion: 16/11/2016
"BSCL.FondoMutuo.BD.Servicios::CrearRegistroBloqueoDesbloqueoFondoAgf" (
	--VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 						BIGINT,
    IN p_IdUsuario						BIGINT,
    IN p_IdModulo						NVARCHAR(3),
    IN p_EsManual						NVARCHAR(1) DEFAULT 'N', -- Parametro para evitar hacer de nuevo la validaciones basicas por si ya se hicieron en otra llamada.    
	IN p_Fondo							TABLE	(
		    									"IdFondo"				NVARCHAR(40)
		    							   		),
	OUT p_out_Resultado					TABLE	(
												"IdFondo"				NVARCHAR(40)	,
		 										"EstadoBloqueoFondo"	NVARCHAR(1)		,
		 										"FechaIngreso" 			DATE			,
												"IdEstado"				NVARCHAR(3)		,
												"IdEmpresa" 			BIGINT			     
		 										),
	OUT p_out_AdvertenciasContador 				INT,
	OUT p_out_AdvertenciasDetalle 				TABLE ("IdFondo"	NVARCHAR(40),"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000)),
	OUT p_out_ErroresContador 					INT,
	OUT p_out_ErroresDetalle 					TABLE ("IdFondo"	NVARCHAR(40),"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS 
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------
	DECLARE v_out_AdvertenciasDetalle 	TABLE ("IdFondo" NVARCHAR(40), "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	DECLARE v_out_ErroresDetalle  		TABLE ("IdFondo" NVARCHAR(40), "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	BEGIN
		v_out_AdvertenciasDetalle = SELECT '' as "IdFondo" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
		v_out_ErroresDetalle 	  = SELECT '' as "IdFondo" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
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
			
			p_out_ErroresDetalle = SELECT 	"errores"."IdFondo",
											"errores"."Codigo",
											"msj"."Descripcion"
									FROM :v_out_ErroresDetalle AS "errores"
									INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "errores"."Codigo" = "msj"."Codigo";
			
			p_out_AdvertenciasDetalle = SELECT 	"adv"."IdFondo",
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
			DECLARE v_count_series			BIGINT;
			DECLARE v_count_correctas		BIGINT;
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CREAR REGISTRO BLOQ DES FONDO AGF','INICIO','INICIO');
			
			IF :p_EsManual = 'S' THEN
				
				CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
				
				IF :v_resultadoRegla = 'RECH' THEN
					v_out_ErroresDetalle = SELECT '' as "IdFondo", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
					SIGNAL SALIDA_CONTROLADA; 	
				END IF;
			
			END IF;
			
			SELECT COUNT(1) as "Count" into v_count_series
			FROM :p_Fondo;
			
			IF v_count_series = 0 THEN
				v_out_ErroresDetalle = SELECT '' as "IdFondo", 'NO_SE_INGRESARON_FONDOS' AS "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM DUMMY;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
						
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
			
			v_fondos_validacion = SELECT "FONDOS"."IdFondo",
										 CASE WHEN "Fondo"."BloqueoAporte" = 'S' OR  "Fondo"."BloqueoRescate" = 'S' THEN 'B' ELSE 'D' END AS "EstadoBloqueoFondo",
										 :v_FechaSistema AS "FechaIngreso",
										 CURRENT_UTCTIMESTAMP AS "FechaIngresoRegistro",
										 'PEN' AS "IdEstado",
										 'BD'  AS "TipoRegistro",
										 :p_IdEmpresa AS "IdEmpresa",
										 CASE WHEN COALESCE("Fondo"."IdFondo",'') = '' THEN 'ERR' ELSE 'OK' END AS "Resultado"
								  FROM :p_Fondo AS "FONDOS"
								  INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("FONDOS"."IdFondo" = "Fondo"."IdFondo");
			
			SELECT COUNT(1) into v_count_correctas
			FROM :v_fondos_validacion
			WHERE "Resultado" = 'OK';
			
			v_out_ErroresDetalle = SELECT "IdFondo" as "IdFondo", 'NO_SE_ENCONTRO_FONDO' AS "Codigo", 
								   "IdFondo" as "ValorParametro",'IdFondo' as "NombreParametro" 
								   FROM :v_fondos_validacion
								   WHERE "Resultado" = 'ERR';
			
			IF v_count_correctas = 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			v_fondos_maestro = SELECT "BSCL.FondoMutuo.BD.Secuencias::InterfazAgf".NEXTVAL as "IdInterfazAgf",
									  "IdFondo",
									  "EstadoBloqueoFondo",
									  "FechaIngresoRegistro",
									  "FechaIngreso",
									  "IdEstado",
									  "TipoRegistro",
									  "IdEmpresa"
								FROM :v_fondos_validacion
								WHERE "Resultado" = 'OK';
			
			INSERT INTO "BSCL.FondoMutuo.BD::FMM.InterfazAgf"(
				"IdInterfazAgf", "IdSerieFondo", "EstadoBloqueoFondo", "FechaIngresoRegistro", "FechaIngreso",
				"IdEstado", "TipoRegistro", "IdEmpresa"
			)
			SELECT "IdInterfazAgf", "IdFondo", "EstadoBloqueoFondo", "FechaIngresoRegistro", "FechaIngreso",
					"IdEstado", "TipoRegistro", "IdEmpresa"
			FROM :v_fondos_maestro;
			
			p_out_Resultado = SELECT "IdFondo",
									 "EstadoBloqueoFondo",
									 "FechaIngreso",
									 "IdEstado",
									 "IdEmpresa"
							  FROM :v_fondos_maestro;
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CREAR REGISTRO BLOQ DES FONDO AGF','FIN','FIN');
			SIGNAL SALIDA_CONTROLADA;
		
		END;
	END;
END;
