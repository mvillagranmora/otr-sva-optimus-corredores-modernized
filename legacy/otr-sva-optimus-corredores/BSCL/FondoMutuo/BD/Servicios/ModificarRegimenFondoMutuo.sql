SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::ModificarRegimenFondoMutuo";

CREATE PROCEDURE
--  Proposito: Modificar Regimen Fondo Mutuo
--  Autor: - Gat-Blac 
--  Fecha Creacion: 31/01/2016
"BSCL.FondoMutuo.BD.Servicios::ModificarRegimenFondoMutuo"(
	--VARIABLES OBLIGATORIAS
	IN p_IdEmpresa 						BIGINT,
	IN p_IdUsuario 						BIGINT,
    IN p_IdModulo 						NVARCHAR(3),
    IN p_UsaReglasNegocio 				NVARCHAR(1),
    IN p_RegimenFondoMutuo				TABLE(
    										"IdInterno"						BIGINT,
    										"IdRegimenFondoMutuo"			NVARCHAR(40),
											"Descripcion"					NVARCHAR(100),
											"PropositoSerieRelacionado"		NVARCHAR(520),				
											"PermiteOtrosGrupos"			NVARCHAR(1),
											"ValidaPortafolioCliente"		NVARCHAR(1),
											"ValidaPortafolioAGF"			NVARCHAR(1),
											"PermiteRescate"				NVARCHAR(1),
											"PermiteAporte"					NVARCHAR(1),
											"PermiteTraspaso"				NVARCHAR(1),
											"ValidaMontoMinimoInstrumento"	NVARCHAR(1),											
											---PARAMETROS PARA REINVERSIONES
											"PermiteCambioGrupo"			NVARCHAR(1),
											"RescateConMandato"				NVARCHAR(1), --Habilita Check del MLT En Aporte y Rescate REINVERSION
											"AceptaMandatoEnReinversion"	NVARCHAR(1), --Habilita Check del MLT En Rescate por reinversion al cambiar a un grupo distinto 				
											"IdEstado"						NVARCHAR(3),
											"CertificaSinAsociado"    		NVARCHAR(1),
											"HomologacionNCG365"			NVARCHAR(40)
											
    									),
    IN p_Regimen		               TABLE(
    										"IdRegimenFondoMutuo"           NVARCHAR(40)
    									),
   	IN p_PropositoPortafolio            TABLE(
    										"IdPropositoPortafolio"         NVARCHAR(40)
    									),
   	OUT p_out_Resultado					TABLE(
    										"IdInterno"						BIGINT,
    										"IdRegimenFondoMutuo"			NVARCHAR(40),
											"Descripcion"					NVARCHAR(100),
											"PropositoSerieRelacionado"		NVARCHAR(520),				
											"PermiteOtrosGrupos"			NVARCHAR(1),
											"ValidaPortafolioCliente"		NVARCHAR(1),
											"ValidaPortafolioAGF"			NVARCHAR(1),
											"PermiteRescate"				NVARCHAR(1),
											"PermiteTraspaso"				NVARCHAR(1),
											"ValidaMontoMinimoInstrumento"	NVARCHAR(1),
											---PARAMETROS PARA REINVERSIONES
											"PermiteCambioGrupo"			NVARCHAR(1),
											"RescateConMandato"				NVARCHAR(1), --Habilita Check del MLT En Aporte y Rescate REINVERSION
											"AceptaMandatoEnReinversion"	NVARCHAR(1), --Habilita Check del MLT En Rescate por reinversion al cambiar a un grupo distinto 				
											"IdEstado"						NVARCHAR(3),
											"CertificaSinAsociado"    		NVARCHAR(1),
											"HomologacionNCG365"			NVARCHAR(40)
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

	DECLARE v_out_AdvertenciasDetalle 		  	TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	DECLARE v_out_ErroresDetalle  			  	TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	BEGIN
		v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
		v_out_ErroresDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
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
			DECLARE v_count_regimenes		BIGINT;
			DECLARE v_resultadoRegla 		NVARCHAR(40);
			DECLARE v_count					BIGINT;
			DECLARE v_count_validaciones 	BIGINT;
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'MODIFICAR REGIMEN FONDO MUTUO MM','INICIO','INICIO');
				    				
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		
		if :v_resultadoRegla = 'RECH' then
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		end if;
		
		----------------------------
		---Validaciones
		----------------------------
		
		SELECT COUNT(1) AS "Count" into v_count_regimenes
		FROM :p_RegimenFondoMutuo;
		
		if (v_count_regimenes = 0) THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_INGRESARON_REGIMENES' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		SELECT TOP 1 COUNT("IdInterno") AS "Count" into v_count
		FROM :p_RegimenFondoMutuo
		GROUP BY "IdInterno"
		ORDER BY "Count" DESC;
		
		if (v_count > 1) THEN
			v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'CORRELATIVO_INCORRECTO' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		
		v_regimenes_validacion = SELECT "P_REGIMEN"."IdInterno"
										  ,"P_REGIMEN"."IdRegimenFondoMutuo"
										  ,"P_REGIMEN"."Descripcion"
										  ,"P_REGIMEN"."PropositoSerieRelacionado"
										  ,"P_REGIMEN"."PermiteOtrosGrupos"
										  ,"P_REGIMEN"."ValidaPortafolioCliente"
										  ,"P_REGIMEN"."ValidaPortafolioAGF"
										  ,"P_REGIMEN"."PermiteRescate"
										  ,"P_REGIMEN"."PermiteTraspaso"
										  ,"P_REGIMEN"."ValidaMontoMinimoInstrumento"
										  ,"P_REGIMEN"."PermiteCambioGrupo"
										  ,"P_REGIMEN"."RescateConMandato"
										  ,"P_REGIMEN"."AceptaMandatoEnReinversion"
										  ,"P_REGIMEN"."PermiteAporte"
										  ,"P_REGIMEN"."IdEstado"
										  ,"P_REGIMEN"."CertificaSinAsociado"
										  ,"P_REGIMEN"."HomologacionNCG365"
										  
										  ,CASE WHEN COALESCE("REGIMEN"."IdRegimenFondoMutuo",'') = '' THEN 'ERR' ELSE 'OK' END AS "Resultado"
									FROM :p_RegimenFondoMutuo AS "P_REGIMEN"
									LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS "REGIMEN" ON ("P_REGIMEN"."IdRegimenFondoMutuo" = "REGIMEN"."IdRegimenFondoMutuo");
		
		v_out_ErroresDetalle = SELECT "IdInterno", 'REGIMEN_NO_EXISTE' as "Codigo", "regimen"."IdRegimenFondoMutuo" as "ValorParametro", 'IdRegimenFondoMutuo' AS "NombreParametro"
							   FROM :v_regimenes_validacion AS "regimen" WHERE "Resultado" = 'ERR'
							   UNION ALL
							   SELECT * FROM :v_out_ErroresDetalle;
		
		v_regimenes_validados = SELECT "P_REGIMEN"."IdInterno"
									  ,"P_REGIMEN"."IdRegimenFondoMutuo"
									  ,"P_REGIMEN"."Descripcion"
									  ,"P_REGIMEN"."PropositoSerieRelacionado"
									  ,"P_REGIMEN"."PermiteOtrosGrupos"
									  ,"P_REGIMEN"."ValidaPortafolioCliente"
									  ,"P_REGIMEN"."ValidaPortafolioAGF"
									  ,"P_REGIMEN"."PermiteRescate"
									  ,"P_REGIMEN"."PermiteTraspaso"
									  ,"P_REGIMEN"."ValidaMontoMinimoInstrumento"
									  ,"P_REGIMEN"."PermiteCambioGrupo"
									  ,"P_REGIMEN"."RescateConMandato"
									  ,"P_REGIMEN"."AceptaMandatoEnReinversion"
									  ,"P_REGIMEN"."PermiteAporte"
									  ,"P_REGIMEN"."IdEstado"
									  ,"P_REGIMEN"."CertificaSinAsociado"
									  ,"P_REGIMEN"."HomologacionNCG365"
								FROM :v_regimenes_validacion AS "P_REGIMEN"
								WHERE "Resultado" = 'OK';
		
		SELECT COUNT(1) INTO v_count_validaciones
		FROM :v_regimenes_validados;
		
		IF v_count_validaciones = 0 THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		UPDATE REGIMENES
		SET "Descripcion" 				= P_REGIMENES."Descripcion",
			"PropositoSerieRelacionado" = P_REGIMENES."PropositoSerieRelacionado",
			"PermiteOtrosGrupos"		= P_REGIMENES."PermiteOtrosGrupos",
			"ValidaPortafolioCliente"	= P_REGIMENES."ValidaPortafolioCliente",
			"ValidaPortafolioAGF" 		= P_REGIMENES."ValidaPortafolioAGF",
			"PermiteRescate" 			= P_REGIMENES."PermiteRescate",
			"PermiteTraspaso" 			= P_REGIMENES."PermiteTraspaso",
			"ValidaMontoMinimoInstrumento" = P_REGIMENES."ValidaMontoMinimoInstrumento",
			"PermiteCambioGrupo" 		= P_REGIMENES."PermiteCambioGrupo",
			"RescateConMandato" 		= P_REGIMENES."RescateConMandato",
			"AceptaMandatoEnReinversion" = P_REGIMENES."AceptaMandatoEnReinversion",
			"PermiteAporte"				= P_REGIMENES."PermiteAporte",
			"IdEstado" 					= P_REGIMENES."IdEstado",
			"CertificaSinAsociado"      = P_REGIMENES."CertificaSinAsociado",
			"HomologacionNCG365"      = P_REGIMENES."HomologacionNCG365"
			
		FROM "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" REGIMENES
		
		INNER JOIN :v_regimenes_validados P_REGIMENES ON (REGIMENES."IdRegimenFondoMutuo" = P_REGIMENES."IdRegimenFondoMutuo");
		
		
		DELETE FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionRegimenFondoMutuo"
		WHERE "IdRegimenFondoMutuoOrigen" = (SELECT "IdRegimenFondoMutuo" FROM :v_regimenes_validados);
		
		regimenes = select * from :p_Regimen;
		
		INSERT INTO "BSCL.FondoMutuo.BD::FMM.ConfiguracionRegimenFondoMutuo" ("IdRegimenFondoMutuoOrigen","IdRegimenFondoMutuoDestino")
		SELECT a."IdRegimenFondoMutuo", b."IdRegimenFondoMutuo"
		from :v_regimenes_validados a
		CROSS JOIN :regimenes b;

		DELETE FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionPropositoPortafolio"
		WHERE "IdRegimenFondoMutuo" = (SELECT "IdRegimenFondoMutuo" FROM :v_regimenes_validados);
		
		propositos = select * from :p_PropositoPortafolio;
		
		INSERT INTO "BSCL.FondoMutuo.BD::FMM.ConfiguracionPropositoPortafolio" ("IdRegimenFondoMutuo","IdPropositoPortafolio")
		SELECT a."IdRegimenFondoMutuo", b."IdPropositoPortafolio"
		from :v_regimenes_validados a
		CROSS JOIN :p_PropositoPortafolio b;
		
		p_out_Resultado = SELECT "P_REGIMEN"."IdInterno"
								  ,"P_REGIMEN"."IdRegimenFondoMutuo"
								  ,"P_REGIMEN"."Descripcion"
								  ,"P_REGIMEN"."PropositoSerieRelacionado"
								  ,"P_REGIMEN"."PermiteOtrosGrupos"
								  ,"P_REGIMEN"."ValidaPortafolioCliente"
								  ,"P_REGIMEN"."ValidaPortafolioAGF"
								  ,"P_REGIMEN"."PermiteRescate"
								  ,"P_REGIMEN"."PermiteTraspaso"
								  ,"P_REGIMEN"."ValidaMontoMinimoInstrumento"
								  ,"P_REGIMEN"."PermiteCambioGrupo"
								  ,"P_REGIMEN"."RescateConMandato"
								  ,"P_REGIMEN"."AceptaMandatoEnReinversion"
								  ,"P_REGIMEN"."IdEstado"
								  ,"P_REGIMEN"."CertificaSinAsociado"
								  ,"P_REGIMEN"."HomologacionNCG365"
							FROM :v_regimenes_validados AS "P_REGIMEN";
		
		v_log = SELECT 
					"IdInterno" AS "IdCabeceraLog"
					,'MODIFICACION' AS "IdTipoLog"
					,'FMM' AS "IdModulo"
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
					,'MODIFICACION_REGIMEN' AS "IdTipoRegistro"
					,"IdInterno" AS "IdRegistro"
					,"IdEstado" AS "EstadoRegistro"
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
				FROM :v_regimenes_validados;
			
			v_DETALLELOG = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
		
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_DETALLELOG);
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'MODIFICAR REGIMEN FONDO MUTUO MM','FIN','FIN');
								
			SIGNAL SALIDA_CONTROLADA;
		
		END;
	END;
END;
