SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::ModificarInstrumentoFondoMutuo";

CREATE PROCEDURE
--  Proposito: Modificar Instrumento de Fondos Mutuos (Fondo + Serie)
--  Autor: - Gat-Blac 
--  Fecha Creacion: 28/06/2016
"BSCL.FondoMutuo.BD.Servicios::ModificarInstrumentoFondoMutuo"(
--VARIABLES OBLIGATORIAS
	IN p_IdEmpresa 						BIGINT,
	IN p_IdUsuario 						BIGINT,
    IN p_IdModulo 						NVARCHAR(3),
    IN p_UsaReglasNegocio 				NVARCHAR(1),
    
    IN p_Fondo							TABLE	(
		    									"IdInterno" 					BIGINT,
		    									"IdFondo"						NVARCHAR(40),
		    									"IdEmisor"						NVARCHAR(40),
		    									"Nemotecnico"					NVARCHAR(40),
		    									"NombreFondo"					NVARCHAR(100),
		    									"IdTipoFondo"					NVARCHAR(40),
		    									"IdPaisOrigen"					BIGINT,
		    									"CodigoSVS"						NVARCHAR(40),
		    									"MoneyMarket"   				NVARCHAR(1),
		    									"Vigencia"						NVARCHAR(1),
		    									"FondoExtranjero"				NVARCHAR(1),
		    									"IdMoneda"						NVARCHAR(3),
		    									"IdTipoInstrumento"				NVARCHAR(3),
		    									"IdCategoriaPerfilFondoMutuo"	NVARCHAR(40),
		    									"AperturaAutomaticaFondo"	  	NVARCHAR(1),
		    									"FondoNormativo"				NVARCHAR(1),
                                                "IdPaisEmisor"                  BIGINT,
                                                "IdTipoDocumentoIdentidadFondo" NVARCHAR(3),
                                                "DocumentoIdentidadFondo"       NVARCHAR(40),
                                                "IdMercadoFondoMutuo"		  	NVARCHAR(40)
		    							   		),
	IN p_SerieFondo 					TABLE	(
				    					  		"IdInterno" 						 BIGINT,
				    					  		"IdSerieFondo"						 NVARCHAR(40),
				    					  		"IdFondo"							 NVARCHAR(40),
				    					  		"NombreSerieFondo"					 NVARCHAR(100),
				    					  		"CodigoSerie"						 NVARCHAR(10),
				    					  		"Alias"								 NVARCHAR(40),
				    					  		"DecimalesValorCuota"		  		 BIGINT		,
		 										"DecimalesCuota"			  		 BIGINT		,
				    					  		"MontoMinimo"						 DECIMAL(28,6),
				    					  		"MontoMaximo"						 DECIMAL(28,6),
				    					  		"DiasRetencionesAporte"              BIGINT,
				    					  		"DiasRetencionesRescate"			 BIGINT,
				    					  		"ExcluyeGarantia"					 NVARCHAR(1),
				    					  		"CobraComision"			  			 NVARCHAR(1),
				    					  		"Diferido"					  		 NVARCHAR(1),
				    					  		"InstrumentoDCV"			  		 NVARCHAR(40),
				    					  		"Omnibus"					  		 NVARCHAR(1),
				    					  		"PatrimonioMinimo"					 DECIMAL(28,6),
				    					  		"IdSerieFondoOrigen"				 NVARCHAR(40),
				    					  		"IdEstado"							 NVARCHAR(3),
				    					  		"IdCliente"							 BIGINT,
				    					  		"IdPortafolio"						 BIGINT,
				    					  		"IdMedioDePagoAbono"				 NVARCHAR(3),
				    					  		"IdMedioDePagoCargo"				 NVARCHAR(3),
				    					  		"DiasValutaValorCuotaAporte"         BIGINT,
                                                "DiasValutaValorCuotaRescate"        BIGINT,
				    					  		"DiasValutaCertificacionAporte" 	 BIGINT,
				    					  		"DiasValutaCertificacionRescate" 	 BIGINT,
				    					  		"DiasValutaCurseAporte"				 BIGINT,
	 											"DiasValutaCurseRescate"			 BIGINT,
	 											"IdFamiliaFondo"					 NVARCHAR(40),
	 											"CambioFondo"                        NVARCHAR(1),
				    					  		"DiasComision"      				 BIGINT,
										  		"PorcentajeComision"                 DECIMAL(26,8),
										  		"PorcentajeRemuneracion"             DECIMAL(26,8),
										  		"PorcentajeGastoOpera"          	 DECIMAL(26,8),
										  		"PorcentajeCostoAdm"          		 DECIMAL(26,8),
										  		"ValorCuota"						 DECIMAL(26,8),
										  		"FechaValorCuota"					 DATE,
										  		"AportesAltos"						 NVARCHAR(1),
										  		"PatrimonioAltos"					 NVARCHAR(1),
										  		"MontoPatrimonioEmisor"				 DECIMAL(26,8),
										  		"InstrumentoOWP"					 NVARCHAR(1),
										  		"EsAPV"								 NVARCHAR(1),
										  		"RestringeAporte"					 NVARCHAR(1),
										  		"RestringeRescate"					 NVARCHAR(1),
												"NemoCMF"							 NVARCHAR(40)
				    					  		),
	IN p_RegimenSerieFondo				TABLE (
												"IdInterno"	BIGINT,
												"IdRegimenFondoMutuo" 	NVARCHAR(40),
												"IdSerieFondo"			NVARCHAR(40)
										),
	OUT p_out_Resultado					TABLE	(
		 										"IdInterno"							 BIGINT,
		 										"IdFondo"							 NVARCHAR(40),
		 										"IdEmisor"							 NVARCHAR(40),
		 										"Nemotecnico"						 NVARCHAR(40),
		    									"NombreFondo"						 NVARCHAR(100),
		    									"IdTipoFondo"						 NVARCHAR(40),
		    									"IdPaisOrigen"						 BIGINT,
		    									"CodigoSVS"							 NVARCHAR(40),
		    									"MoneyMarket"   					 NVARCHAR(1),
		    									"FechaCreacion"						 DATE,
		    									"FechaIngresoRegistro"				 TIMESTAMP,
		    									"FechaConstitucion" 				 DATE,
		    									"Vigencia"							 NVARCHAR(1),
		    									"FondoExtranjero"					 NVARCHAR(1),
		    									"IdMoneda"							 NVARCHAR(3),
		    									"IdTipoInstrumento"					 NVARCHAR(3),
		    									"IdFamiliaFondo"					 NVARCHAR(40),
		    									"IdSerieFondo"						 NVARCHAR(40),
	  											"IdInstrumento"						 BIGINT,
	  											"NombreSerieFondo"					 NVARCHAR(100),
	  											"CodigoSerie"						 NVARCHAR(10),
	  											"Alias"								 NVARCHAR(40),
	  											"DecimalesValorCuota"		  		 BIGINT,
		 										"DecimalesCuota"			  		 BIGINT,
	  											"MontoMinimo"						 DECIMAL(28,6),
	  											"MontoMaximo"						 DECIMAL(28,6),
	  											"DiasRetencionesAporte"              BIGINT,
	  											"DiasRetencionesRescate"			 BIGINT,
	  											"ExcluyeGarantia"					 NVARCHAR(1),
	  											"CobraComision"			  			 NVARCHAR(1),
				    					  		"Diferido"					  		 NVARCHAR(1),
				    					  		"InstrumentoDCV"			  		 NVARCHAR(40),
				    					  		"Omnibus"					  		 NVARCHAR(1),
	  											"PatrimonioMinimo"					 DECIMAL(28,6),
	  											"IdSerieFondoOrigen"				 NVARCHAR(40),
	  											"IdEstado"							 NVARCHAR(3),
	  											"FechaIngresoRegistroSerie"			 TIMESTAMP,
	  											"IdCliente"							 BIGINT,
				    					  		"IdPortafolio"						 BIGINT,
				    					  		"IdMedioDePagoAbono"				 NVARCHAR(3),
				    					  		"IdMedioDePagoCargo"				 NVARCHAR(3),
				    					  		"DiasValutaValorCuotaAporte"         BIGINT,
                                                "DiasValutaValorCuotaRescate"        BIGINT,
				    					  		"DiasValutaCertificacionAporte" 	 BIGINT,
				    					  		"DiasValutaCertificacionRescate" 	 BIGINT,
				    					  		"DiasValutaCurseAporte"				 BIGINT,
	 											"DiasValutaCurseRescate"			 BIGINT,
	 											"CambioFondo"                        NVARCHAR(1),
	 											"DiasComision"      				 BIGINT,
										  		"PorcentajeComision"                 DECIMAL(26,8),
										  		"PorcentajeRemuneracion"             DECIMAL(26,8),
										  		"PorcentajeGastoOpera"          	 DECIMAL(26,8),
										  		"PorcentajeCostoAdm"          		 DECIMAL(26,8),
	 											"AperturaAutomaticaFondo"			 NVARCHAR(1),
	 											"FondoNormativo"					 NVARCHAR(1),
				    					  		"IdEmpresa"							 BIGINT,
				    					  		"IdCategoriaPerfilFondoMutuo"        NVARCHAR(40),
                                                "IdPaisEmisor"                       BIGINT,
                                                "IdTipoDocumentoIdentidadFondo"      NVARCHAR(3),
                                                "DocumentoIdentidadFondo"            NVARCHAR(40),
                                                "IdMercadoFondoMutuo"		  		 NVARCHAR(40)
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
	DECLARE v_erroroutmsg  nvarchar(520);
	DECLARE v_out_AdvertenciasDetalle 		  	TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	DECLARE v_out_ErroresDetalle  			  	TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	----------------------------
	--DECLARE Detalle Log
	-----------------------------
	DECLARE v_count_nuevo_instrumento BIGINT;
	DECLARE v_count_fondos_nuevos BIGINT;
	DECLARE v_count_fondos_actualizar BIGINT;
	DECLARE v_count_fondos_actualizar_real BIGINT;
	DECLARE v_count_serie_fondo_modificado BIGINT;
	DECLARE v_count_serie_fondo_modificado_real BIGINT;
	DECLARE v_count_regimen_nuevo BIGINT;
	DECLARE v_count_regimen_actualizado BIGINT;
	DECLARE v_count_regimen_actualizado_real BIGINT;
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
		DECLARE v_count_fondos 			  		BIGINT;
		DECLARE v_count_series			  		BIGINT;
		DECLARE v_resultadoRegla 		  		NVARCHAR(40);
		DECLARE v_count					  		BIGINT;
		DECLARE v_count_series_validacion 		BIGINT;
		DECLARE v_count_advertencia_ins	  		BIGINT;
		DECLARE v_count_errores_ins		  		BIGINT;
		DECLARE v_count_series_validacion_ins	BIGINT;
		DECLARE v_count_regimenes				BIGINT;
		DECLARE v_count_series_regimen			BIGINT;
		DECLARE v_count_carteras				BIGINT;
		DECLARE v_IdEstado						NVARCHAR(3);
		DECLARE v_IdCliente						BIGINT;
        DECLARE v_IdMoneda						NVARCHAR(3);
        DECLARE v_IdInstrumentoInactivo			BIGINT;
        DECLARE v_IdInstrumento					BIGINT;
		DECLARE v_IdConcepto					NVARCHAR(6);
		DECLARE v_RequiereAutorizacion			NVARCHAR(1);
		DECLARE v_RegistroSolicitudCambio 		"BSCL.Plataforma.BD::PLA.SolicitudCambio";
		DECLARE v_validar_ingreso				NVARCHAR(1) :='N';
		declare v_Glosa 						NVARCHAR(520) :='';
		declare v_out_idAuth 		bigint;
		declare v_out_AuthAdvertenciasContador 	int;
		declare v_out_AuthErroresContador 		int;
		declare v_out_VCAdvertenciasContador 	int;
		declare v_out_VCErroresContador 		int;
		v_out_AuthAdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
		v_out_AuthErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
		v_out_VCAdvertenciasDetalle		= select '' as "IdInterno", '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
		v_out_VCthErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'MODIFICAR INSTRUMENTO FONDO MUTUO MM','INICIO','INICIO');
				    				
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		
		if :v_resultadoRegla = 'RECH' then
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		end if;
		
		SELECT TOP 1 "IdEstado" AS "IdEstado" INTO v_IdEstado
		FROM :p_SerieFondo;
		
		IF (v_IdEstado = 'INA') THEN
			
			SELECT TOP 1 "Instrumento"."IdInstrumento" INTO v_IdInstrumentoInactivo
			FROM :p_SerieFondo "Serie"
			LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo" ON ("SerieFondo"."IdSerieFondo" = "Serie"."IdSerieFondo")
			LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS "Instrumento" ON ("Instrumento"."Nemotecnico" = "Serie"."IdSerieFondo" AND (COALESCE("SerieFondo"."IdInstrumento", 0) = 0 OR "SerieFondo"."IdInstrumento" = "Instrumento"."IdInstrumento") );
			
			SELECT COUNT(1) AS "Count" INTO v_count_carteras 
			from "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSaldos"
			WHERE "IdEmpresa" = p_IdEmpresa
			and "IdInstrumento" = v_IdInstrumentoInactivo;
			
			IF (v_count_carteras > 0) THEN
				v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'INSTRUMENTO_EN_CUSTODIA' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
		END IF;
		
		CALL "BSCL.FondoMutuo.BD.Utilitarios::ConsultarParametroFondoMutuo" (:p_IdEmpresa, 'REQUIERE_AUTORIZACION_INGRESAR_FFMM', v_validar_ingreso); --oct
		------------------------------------------
		--Pregunta Si requiere autorizacion
		------------------------------------------
		IF (v_validar_ingreso = 'S') THEN
			v_IdConcepto := 'INGFFM';
			v_RegistroSolicitudCambio =SELECT  * FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" WHERE 1=2;
			
			call "BSCL.Plataforma.BD.Servicios::AUT.RequiereEventoAutorizacion" (
				:p_IdEmpresa,
				:p_IdUsuario,
				:p_IdModulo,
				:v_RegistroSolicitudCambio,
				:v_IdConcepto,
				:v_RequiereAutorizacion
				);
		END IF;
		----------------------------
		---Validaciones
		----------------------------
		
		SELECT COUNT(1) AS "Count" into v_count_fondos
		FROM :p_Fondo;
		
		if (v_count_fondos = 0) THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_INGRESARON_FONDOS' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		SELECT TOP 1 COUNT("IdInterno") AS "Count" into v_count
		FROM :p_Fondo
		GROUP BY "IdInterno"
		ORDER BY "Count" DESC;
		
		if (v_count > 1) THEN
			v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'CORRELATIVO_INCORRECTO' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		SELECT COUNT(1) AS "Count" into v_count_series
		FROM :p_SerieFondo "Serie"
		INNER JOIN :p_Fondo AS "Fondo" ON ("Serie"."IdInterno" = "Fondo"."IdInterno" AND "Serie"."IdFondo" = "Fondo"."IdFondo");
		
		if (v_count_series = 0) THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_INGRESARON_SERIES' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		SELECT TOP 1 COUNT("IdInterno") AS "Count" into v_count
		FROM :p_SerieFondo
		GROUP BY "IdInterno"
		ORDER BY "Count" DESC;
		
		if (v_count > 1) THEN
			v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'CORRELATIVO_INCORRECTO' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		SELECT COUNT(1) AS "Count" into v_count_regimenes
		FROM :p_RegimenSerieFondo;
		
		if (v_count_regimenes = 0) THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_INGRESARON_REGIMENES' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		v_series_prevalidacion = SELECT "IdInterno" 				
										,"IdSerieFondo"							
										,"IdFondo"					
										,"NombreSerieFondo"			
										,"CodigoSerie"						
										,"Alias"						
										,"DecimalesValorCuota"		
										,"DecimalesCuota"			
										,"MontoMinimo"				
										,"MontoMaximo"
										,"DiasRetencionesAporte"
										,"DiasRetencionesRescate"	
										,"ExcluyeGarantia"			
										,"CobraComision"			  	
										,"Diferido"					
										,"InstrumentoDCV"			
										,"Omnibus"					
										,"PatrimonioMinimo"			
										,"IdSerieFondoOrigen"		
										,CASE :v_RequiereAutorizacion WHEN 'S' THEN 'PDA' ELSE "IdEstado" END AS "IdEstado"
										,"IdCliente"
										,"IdPortafolio"
										,"IdMedioDePagoAbono"
										,"IdMedioDePagoCargo"
										,"DiasValutaValorCuotaAporte"
                                        ,"DiasValutaValorCuotaRescate"
										,"DiasValutaCertificacionAporte"
										,"DiasValutaCertificacionRescate"
										,"DiasValutaCurseAporte"
										,"DiasValutaCurseRescate"
										,"IdFamiliaFondo"
										,"CambioFondo"
										,:p_IdEmpresa AS "IdEmpresa"
										,"DiasComision"
										,"PorcentajeComision"
										,"PorcentajeRemuneracion"
										,"PorcentajeGastoOpera"
										,"PorcentajeCostoAdm"
										,"AportesAltos"
										,"PatrimonioAltos"
										,"MontoPatrimonioEmisor"
										,"InstrumentoOWP"
										,"EsAPV"
										,"RestringeAporte"
										,"RestringeRescate"
										,"NemoCMF"
								 FROM :p_SerieFondo;
		
		v_regimen_validacion = SELECT "P_REGIMEN"."IdInterno",
									  "P_REGIMEN"."IdRegimenFondoMutuo",
									  "REGIMEN"."GrupoRegimen",
								 	  "REGIMEN"."PermiteOtrosGrupos",
								 	  "REGIMEN"."ValidaPortafolioAGF",
								 	  "REGIMEN"."PropositoSerieRelacionado",
								 	  "P_REGIMEN"."IdSerieFondo",
									  CASE WHEN COALESCE("REGIMEN"."IdRegimenFondoMutuo",'') = '' THEN 'ERR' ELSE 'OK' END AS "ResultadoRegimen"
							   FROM :p_RegimenSerieFondo AS "P_REGIMEN"
							   LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS "REGIMEN" ON ("P_REGIMEN"."IdRegimenFondoMutuo" = "REGIMEN"."IdRegimenFondoMutuo" AND "IdEstado" = 'ACT');

		v_validacion_portafolio = SELECT "REGIMEN"."IdInterno"
									     ,"REGIMEN"."IdRegimenFondoMutuo"
								  FROM :v_regimen_validacion AS "REGIMEN"
								  INNER JOIN :p_SerieFondo AS "SERIE" ON ("SERIE"."IdSerieFondo" = "REGIMEN"."IdSerieFondo")
								  INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS "ConsultarPortafolio" ON  ( "ConsultarPortafolio"."IdPortafolio" = "SERIE"."IdPortafolio" )
								  WHERE "REGIMEN"."ValidaPortafolioAGF" = 'S' AND "ConsultarPortafolio"."Proposito" != "REGIMEN"."PropositoSerieRelacionado";
										  
		v_out_ErroresDetalle = SELECT "IdInterno", 'REGIMEN_NO_EXISTE' as "Codigo", "regimen"."IdRegimenFondoMutuo" as "ValorParametro", 'IdRegimenFondoMutuo' AS "NombreParametro"
							   FROM :v_regimen_validacion AS "regimen" WHERE "ResultadoRegimen" = 'ERR'
							   UNION ALL
							   SELECT "IdInterno", 'PORTAFOLIO_NO_COINCIDE_REGIMEN' AS "Codigo","regimen"."IdRegimenFondoMutuo" as "ValorParametro", 'IdRegimenFondoMutuo' AS "NombreParametro"
							   FROM :v_validacion_portafolio AS "regimen"
							   UNION ALL
							   SELECT * FROM :v_out_ErroresDetalle;
				
		v_regimen_errores = SELECT DISTINCT "IdInterno" FROM 
							(SELECT "IdInterno"
					  		FROM :v_regimen_validacion AS "regimen" WHERE "ResultadoRegimen" = 'ERR'
					  		UNION ALL
					  		SELECT "IdInterno"
					  		FROM :v_validacion_portafolio
					  		) "validaciones";
					  		
		v_series_regimen_validados	= SELECT "P_Serie"."IdInterno" 				
											,"P_Serie"."IdSerieFondo"							
											,"P_Serie"."IdFondo"					
											,"P_Serie"."NombreSerieFondo"			
											,"P_Serie"."CodigoSerie"						
											,"P_Serie"."Alias"						
											,"P_Serie"."DecimalesValorCuota"		
											,"P_Serie"."DecimalesCuota"			
											,"P_Serie"."MontoMinimo"				
											,"P_Serie"."MontoMaximo"
											,"P_Serie"."DiasRetencionesAporte"
											,"P_Serie"."DiasRetencionesRescate"	
											,"P_Serie"."ExcluyeGarantia"			
											,"P_Serie"."CobraComision"			  	
											,"P_Serie"."Diferido"					
											,"P_Serie"."InstrumentoDCV"			
											,"P_Serie"."Omnibus"					
											,"P_Serie"."PatrimonioMinimo"			
											,"P_Serie"."IdSerieFondoOrigen"		
											,"P_Serie"."IdEstado"
											,"P_Serie"."IdCliente"
											,"P_Serie"."IdPortafolio"
											,"P_Serie"."IdMedioDePagoAbono"
											,"P_Serie"."IdMedioDePagoCargo"
											,"P_Serie"."DiasValutaValorCuotaAporte"
                                            ,"P_Serie"."DiasValutaValorCuotaRescate"
											,"P_Serie"."DiasValutaCertificacionAporte"
											,"P_Serie"."DiasValutaCertificacionRescate"
											,"P_Serie"."DiasValutaCurseAporte"
											,"P_Serie"."DiasValutaCurseRescate"
											,"P_Serie"."IdFamiliaFondo"
											,"P_Serie"."CambioFondo"
											,"P_Serie"."IdEmpresa"
											,"P_Serie"."DiasComision"
											,"P_Serie"."PorcentajeComision"
											,"P_Serie"."PorcentajeRemuneracion"
											,"P_Serie"."PorcentajeGastoOpera"
											,"P_Serie"."PorcentajeCostoAdm"
											,"P_Serie"."AportesAltos"
											,"P_Serie"."PatrimonioAltos"
											,"P_Serie"."MontoPatrimonioEmisor"
											,"P_Serie"."InstrumentoOWP"
											,"P_Serie"."EsAPV"
											,"P_Serie"."RestringeAporte"
											,"P_Serie"."RestringeRescate"
											,"P_Serie"."NemoCMF"
									 FROM :v_series_prevalidacion AS "P_Serie"
									 LEFT OUTER JOIN :v_regimen_errores AS "REGIMEN" ON ("REGIMEN"."IdInterno" = "P_Serie"."IdInterno")
									 WHERE COALESCE("REGIMEN"."IdInterno",-1) = -1;
									 
					  	
		SELECT COUNT(1) into v_count_series_regimen
		FROM :v_series_regimen_validados;
		
		IF 	v_count_series_regimen = 0 THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		
		v_series_validacion = SELECT "P_Serie"."IdInterno" 				
								,"P_Serie"."IdSerieFondo"						
								,"P_Serie"."IdFondo"					
								,"P_Serie"."NombreSerieFondo"			
								,"P_Serie"."CodigoSerie"						
								,"P_Serie"."Alias"						
								,"P_Serie"."DecimalesValorCuota"		
								,"P_Serie"."DecimalesCuota"			
								,"P_Serie"."MontoMinimo"				
								,"P_Serie"."MontoMaximo"
								,"P_Serie"."DiasRetencionesAporte"
								,"P_Serie"."DiasRetencionesRescate"	
								,"P_Serie"."ExcluyeGarantia"			
								,"P_Serie"."CobraComision"			  	
								,"P_Serie"."Diferido"					
								,"P_Serie"."InstrumentoDCV"			
								,"P_Serie"."Omnibus"					
								,"P_Serie"."PatrimonioMinimo"			
								,"P_Serie"."IdSerieFondoOrigen"		
								,"P_Serie"."IdEstado"
								,"P_Serie"."IdCliente"
								,"P_Serie"."IdPortafolio"
								,"P_Serie"."IdMedioDePagoAbono"
								,"P_Serie"."IdMedioDePagoCargo"
                                ,"P_Serie"."DiasValutaValorCuotaAporte"
                                ,"P_Serie"."DiasValutaValorCuotaRescate"
								,"P_Serie"."DiasValutaCertificacionAporte"
								,"P_Serie"."DiasValutaCertificacionRescate"
								,"P_Serie"."DiasValutaCurseAporte"
								,"P_Serie"."DiasValutaCurseRescate"
								,"P_Serie"."IdFamiliaFondo"
								,"P_Serie"."CambioFondo"
								,"P_Serie"."DiasComision"
								,"P_Serie"."PorcentajeComision"
								,"P_Serie"."PorcentajeRemuneracion"
								,"P_Serie"."PorcentajeGastoOpera"
								,"P_Serie"."PorcentajeCostoAdm"
								,"P_Serie"."AportesAltos"
								,"P_Serie"."PatrimonioAltos"
								,"P_Serie"."MontoPatrimonioEmisor"
								,"P_Serie"."InstrumentoOWP"
								,"P_Serie"."EsAPV"
								,"P_Serie"."RestringeAporte"
								,"P_Serie"."RestringeRescate"
								,"P_Serie"."NemoCMF"
								,COALESCE("TipoFondo"."Descripcion",'') AS "DescripcionTipoFondo"
								,CASE WHEN COALESCE("Serie"."IdSerieFondo",'-1') != '-1' THEN 'OK' ELSE 'ERR' END AS "Resultado"
								,CASE WHEN COALESCE("Emisor"."IdEmisor",'-1') != COALESCE("P_Fondo"."IdEmisor",'-1') THEN 'ADV' else 'OK' END AS "ResultadoEmisor"
								,CASE WHEN COALESCE("TipoFondo"."IndicadorMoneyMarket",'N') != 'S' AND "P_Fondo"."MoneyMarket" = 'S' THEN 'ERR' ELSE 'OK' END AS "ResultadoMoneyMarket"
								,COALESCE("Serie"."IdInstrumento", 0) AS "IdInstrumento"
							FROM :v_series_regimen_validados AS "P_Serie" 
							INNER JOIN :p_fondo AS "P_Fondo" ON ("P_Fondo"."IdInterno" = "P_Serie"."IdInterno")  
					        LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "P_Serie"."IdEmpresa" = "Serie"."IdEmpresa")
							LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "Cliente" ON ("P_Serie"."IdCliente" = "Cliente"."IdCliente" AND "P_Serie"."IdEmpresa" = "Cliente"."IdEmpresa")
							LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS "Emisor" ON ("Emisor"."DocumentoIdentidad" = "Cliente"."DocumentoIdentidad" AND "Emisor"."IdEmisor" = "P_Fondo"."IdEmisor")
							LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.TipoFondo" AS "TipoFondo" ON ("TipoFondo"."IdTipoFondo" = "P_Fondo"."IdTipoFondo");
		
		BEGIN
			v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'SERIE_NO_EXISTE' as "Codigo", "ValoresCuotaMaestro"."IdSerieFondo" as "ValorParametro", 'IdSerieFondo' AS "NombreParametro"
								   FROM :v_series_validacion AS "ValoresCuotaMaestro" WHERE "Resultado" = 'ERR'
								   UNION ALL 
								   SELECT 0 AS "IdInterno",'TIPO_FONDO_NO_MONEY_MARKET' AS "Codigo", "DescripcionTipoFondo" AS "ValorParametro", 'DescripcionTipoFondo'AS "NombreParametro"
								   FROM :v_series_validacion AS "Series" WHERE "ResultadoMoneyMarket" = 'ERR'
								   UNION ALL
								   SELECT * FROM :v_out_ErroresDetalle;
			
			SELECT COUNT(1) as "Count" INTO v_count_series_validacion
			FROM :v_series_validacion WHERE "Resultado" = 'OK' AND "ResultadoMoneyMarket" = 'OK';
			IF (v_count_series_validacion = 0) THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;
		END;
		
		v_out_AdvertenciasDetalle = SELECT 0 AS "IdInterno", 'RUT_EMISOR_DISTINTO_RUT_AGF' as "Codigo", 'N' as "RequiereAutorizacion", "Series"."IdSerieFondo" as "ValorParametro", 'IdSerieFondo' AS "NombreParametro"
							  	    FROM :v_series_validacion AS "Series" WHERE "ResultadoEmisor" = 'ADV';
		
		v_series_maestro = SELECT "P_Serie"."IdInterno" 				
								,"P_Serie"."IdSerieFondo"							
								,"P_Serie"."IdFondo"					
								,"P_Serie"."NombreSerieFondo"			
								,"P_Serie"."CodigoSerie"						
								,"P_Serie"."Alias"						
								,"P_Serie"."DecimalesValorCuota"		
								,"P_Serie"."DecimalesCuota"			
								,"P_Serie"."MontoMinimo"				
								,"P_Serie"."MontoMaximo"
								,"P_Serie"."DiasRetencionesAporte"
								,"P_Serie"."DiasRetencionesRescate"	
								,"P_Serie"."ExcluyeGarantia"			
								,"P_Serie"."CobraComision"			  	
								,"P_Serie"."Diferido"					
								,"P_Serie"."InstrumentoDCV"			
								,"P_Serie"."Omnibus"					
								,"P_Serie"."PatrimonioMinimo"			
								,"P_Serie"."IdSerieFondoOrigen"		
								,"P_Serie"."IdEstado"
								,"P_Serie"."IdCliente"
								,"P_Serie"."IdPortafolio"
								,"P_Serie"."IdMedioDePagoAbono"
								,"P_Serie"."IdMedioDePagoCargo"
                                ,"P_Serie"."DiasValutaValorCuotaAporte"
                                ,"P_Serie"."DiasValutaValorCuotaRescate"
							    ,"P_Serie"."DiasValutaCertificacionAporte"
								,"P_Serie"."DiasValutaCertificacionRescate"
								,"P_Serie"."DiasValutaCurseAporte"
								,"P_Serie"."DiasValutaCurseRescate"
								,"P_Serie"."IdFamiliaFondo"
								,"P_Serie"."CambioFondo"
								,"P_Serie"."DiasComision"
								,"P_Serie"."PorcentajeComision"
								,"P_Serie"."PorcentajeRemuneracion"
								,"P_Serie"."PorcentajeGastoOpera"
								,"P_Serie"."PorcentajeCostoAdm"	
								,"P_Serie"."AportesAltos"
								,"P_Serie"."PatrimonioAltos"
								,"P_Serie"."MontoPatrimonioEmisor"
								,"P_Serie"."InstrumentoOWP"
								,"P_Serie"."EsAPV"
								,"P_Serie"."RestringeAporte"
								,"P_Serie"."RestringeRescate"
								,"P_Serie"."NemoCMF"
								,"P_Serie"."IdInstrumento"
							FROM  :v_series_validacion AS "P_Serie"
							WHERE "P_Serie"."Resultado" = 'OK';
		
		v_series_antes_instrumento =  SELECT "Serie"."IdInterno" 				
											,"Serie"."IdSerieFondo"							
											,"Serie"."IdFondo"
											,COALESCE("Instrumento"."IdInstrumento",0) AS "IdInstrumento"					
											,"Serie"."NombreSerieFondo"			
											,"Serie"."CodigoSerie"							
											,"Serie"."Alias"						
											,"Serie"."DecimalesValorCuota"		
											,"Serie"."DecimalesCuota"			
											,"Serie"."MontoMinimo"				
											,"Serie"."MontoMaximo"
											,"Serie"."DiasRetencionesAporte"
											,"Serie"."DiasRetencionesRescate"	
											,"Serie"."ExcluyeGarantia"			
											,"Serie"."CobraComision"			  	
											,"Serie"."Diferido"					
											,"Serie"."InstrumentoDCV"			
											,"Serie"."Omnibus"					
											,"Serie"."PatrimonioMinimo"			
											,"Serie"."IdSerieFondoOrigen"		
											,"Serie"."IdCliente"
											,"Serie"."IdPortafolio"
											,"Serie"."IdMedioDePagoAbono"
											,"Serie"."IdMedioDePagoCargo"
                                            ,"Serie"."DiasValutaValorCuotaAporte"
                                            ,"Serie"."DiasValutaValorCuotaRescate"
											,"Serie"."DiasValutaCertificacionAporte"
											,"Serie"."DiasValutaCertificacionRescate"
											,"Serie"."DiasValutaCurseAporte"
											,"Serie"."DiasValutaCurseRescate"
											,"Serie"."IdFamiliaFondo"
											,"Serie"."CambioFondo"
											,"Serie"."IdEstado"
											,"Serie"."DiasComision"
											,"Serie"."PorcentajeComision"
											,"Serie"."PorcentajeRemuneracion"
											,"Serie"."PorcentajeGastoOpera"
											,"Serie"."PorcentajeCostoAdm"
											,"Serie"."AportesAltos"
											,"Serie"."PatrimonioAltos"
											,"Serie"."MontoPatrimonioEmisor"
											,"Serie"."InstrumentoOWP"
											,"Serie"."EsAPV"
											,"Serie"."RestringeAporte"
											,"Serie"."RestringeRescate"
											,"Serie"."NemoCMF"
										FROM :v_series_maestro "Serie"
										LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS "Instrumento" ON ("Instrumento"."Nemotecnico" = "Serie"."IdSerieFondo" AND "Serie"."IdInstrumento" = "Instrumento"."IdInstrumento");
		
										
		--VALIDAMOS QUE NO SE ESTE CAMBIANDO LA MONEDA CUANDO YA EXISTE CARTERA
		v_out_ErroresDetalle = SELECT "P_Serie"."IdInterno", 'CAMBIO_MONEDA_NEMO_CARTERA' AS "Codigo", "P_Serie"."IdSerieFondo" as "ValorParametro", 'IdSerieFondo' AS "NombreParametro" 
							   FROM :v_series_antes_instrumento AS "P_Serie"
							   INNER JOIN :p_Fondo AS "P_Fondo" ON ("P_Fondo"."IdFondo" = "P_Serie"."IdFondo")
							   INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON "Fondo"."IdFondo" = "P_Serie"."IdFondo"
							   WHERE "P_Fondo"."IdMoneda" != "Fondo"."IdMoneda" AND "P_Serie"."IdInstrumento" != 0 
							   AND (EXISTS(SELECT 1 FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "Solicitud" WHERE "Solicitud"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Solicitud"."Estado" IN ('ING','CRT')) OR EXISTS(SELECT 1 FROM "BSCL.Custodia.BD::CUS.Cartera" AS "Cartera" WHERE "Cartera"."IdInstrumento" = "P_Serie"."IdInstrumento"))
							   UNION ALL
							   SELECT * FROM :v_out_ErroresDetalle;
		
		
		v_series_antes_instrumento = SELECT "Serie"."IdInterno" 				
											,"Serie"."IdSerieFondo"							
											,"Serie"."IdFondo"
											,"Serie"."IdInstrumento"					
											,"Serie"."NombreSerieFondo"			
											,"Serie"."CodigoSerie"							
											,"Serie"."Alias"						
											,"Serie"."DecimalesValorCuota"		
											,"Serie"."DecimalesCuota"			
											,"Serie"."MontoMinimo"				
											,"Serie"."MontoMaximo"
											,"Serie"."DiasRetencionesAporte"
											,"Serie"."DiasRetencionesRescate"	
											,"Serie"."ExcluyeGarantia"			
											,"Serie"."CobraComision"			  	
											,"Serie"."Diferido"					
											,"Serie"."InstrumentoDCV"			
											,"Serie"."Omnibus"					
											,"Serie"."PatrimonioMinimo"			
											,"Serie"."IdSerieFondoOrigen"		
											,"Serie"."IdCliente"
											,"Serie"."IdPortafolio"
											,"Serie"."IdMedioDePagoAbono"
											,"Serie"."IdMedioDePagoCargo"
                                            ,"Serie"."DiasValutaValorCuotaAporte"
                                            ,"Serie"."DiasValutaValorCuotaRescate"
											,"Serie"."DiasValutaCertificacionAporte"
											,"Serie"."DiasValutaCertificacionRescate"
											,"Serie"."DiasValutaCurseAporte"
											,"Serie"."DiasValutaCurseRescate"
											,"Serie"."IdFamiliaFondo"
											,"Serie"."CambioFondo"
											,"Serie"."IdEstado"
											,"Serie"."DiasComision"
											,"Serie"."PorcentajeComision"
											,"Serie"."PorcentajeRemuneracion"
											,"Serie"."PorcentajeGastoOpera"
											,"Serie"."PorcentajeCostoAdm"
											,"Serie"."AportesAltos"
											,"Serie"."PatrimonioAltos"
											,"Serie"."MontoPatrimonioEmisor"
											,"Serie"."InstrumentoOWP"
											,"Serie"."EsAPV"
											,"Serie"."RestringeAporte"
											,"Serie"."RestringeRescate"
											,"Serie"."NemoCMF"
											FROM :v_series_antes_instrumento AS "Serie"
											LEFT JOIN :v_out_ErroresDetalle AS "Error" ON ("Error"."IdInterno" = "Serie"."IdInterno")
											WHERE COALESCE("Error"."IdInterno",-1) = -1;
		
		SELECT COUNT(1) INTO v_count_series_validacion
		FROM :v_series_antes_instrumento;
		
		IF(v_count_series_validacion = 0) THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		v_instrumentos = SELECT "Serie"."IdInterno"
								,"Serie"."IdSerieFondo" AS "Nemotecnico"
								,''   AS "ISIN"
								,0    AS "CorteMinimo"
								,'N'  AS "EstaInscrito"
								,"Serie"."NombreSerieFondo" AS "RazonSocial"
								,'0001-01-01' AS "FechaEmision"
								,'0001-01-01' AS "FechaVencimiento"
								,"Fondo"."IdEmisor"
								,'' AS "IdTipoReajuste"
								,"Fondo"."IdMoneda" AS "IdMoneda"
								,"Fondo"."IdTipoInstrumento"
								,"Serie"."IdEstado"
								,0  AS "Tamano"
								,'' AS "IdEntidadClasificadoraRiesgo"
								,'' AS "ValorClasificacionRiesgo"
								,0  AS "PrecioBase"
								,0  AS "PrecioReferencia"
								,'0001-01-01' AS "FechaReferencia"
								,0 AS "FrecuenciaCupon"
								,0 AS "NumeroCupones"
								,NULL AS "InstrumentoAcreditado"
								,0 AS "IdInstrumentoRelacionado"
								,'' AS "EsPutEsCall"
								,0 AS "PrecioStrike"
								,'' AS "TipoBonoIRF"
								,'' AS "OrigenInstrumento"
								,'' AS "TipoRenta"
								,'' AS "RelacionadoGrupo"
								,'' AS "FormadorMercado"
								,'' AS "PactoAndino"
								,0 AS "FactorExposicion"
								,'' AS "LargoPlazo"
								,0 AS "Tasa"
								,'N' AS "AfectoArt107"
								,'N' AS "AfectoArt104"
								,'N' AS "DividendoOtraMoneda"
								,'S' AS "EsAutorizado"
								,'S' AS "CobroComisionesCustodia"
								,0  AS "IdInstrumentoRaiz"
								,'' AS "IdCondicionLiquidacion"								
								,'N' AS "EsInstrumentoCalificado"
						  FROM :v_series_antes_instrumento AS "Serie"
						  INNER JOIN :p_Fondo AS "Fondo" ON ("Fondo"."IdInterno" = "Serie"."IdInterno")
						  WHERE "Serie"."IdInstrumento" = 0;
		
		v_valores_riesgo = SELECT 0 AS "IdInterno", '' AS "IdEntidadClasificadoraRiesgo", '' AS "ValorClasificacionRiesgo" FROM DUMMY;
		v_bolsas = SELECT 0 AS "IdInterno",'' AS "IdBolsa" FROM DUMMY WHERE 1 = 2 ;
		--------------------------------
--Informacion Log instrumento nuevo
--------------------------------
SELECT count(1) INTO v_count_nuevo_instrumento FROM :v_instrumentos;

IF(:v_count_nuevo_instrumento > 0) THEN 

	v_instrumento_nuevo =
		SELECT  'Nemotecnico' AS "Campo",
				"Nemotecnico" As "ValorActual" 
				FROM :v_instrumentos
				
		UNION ALL
		
		SELECT 'ISIN' AS "Campo",
				"ISIN" As "ValorActual"
				FROM :v_instrumentos		
		
		UNION ALL
		
		SELECT 'CorteMinimo' AS "Campo",
				CAST("CorteMinimo" as varchar) As "ValorActual"
				FROM :v_instrumentos
				
		UNION ALL
		
		SELECT 'EstaInscrito' AS "Campo",
				"EstaInscrito" As "ValorActual"
				FROM :v_instrumentos
				
		UNION ALL
		
		SELECT 'RazonSocial' AS "Campo",
				"RazonSocial" As "ValorActual"
				FROM :v_instrumentos
				
		UNION ALL
		
		SELECT 'FechaEmision' AS "Campo",
				CAST("FechaEmision" As varchar) As "ValorActual"
				FROM :v_instrumentos
				
		UNION ALL
		
		SELECT 'FechaVencimiento' AS "Campo",
				CAST("FechaVencimiento" As varchar) As "ValorActual"
				FROM :v_instrumentos
				
		UNION ALL
		
		SELECT 'IdEmisor' AS "Campo",
				"IdEmisor" As "ValorActual"
				FROM :v_instrumentos
				
		UNION ALL
		
		SELECT 'IdTipoReajuste' AS "Campo",
				"IdTipoReajuste" As "ValorActual"
				FROM :v_instrumentos
		
		UNION ALL
		
		SELECT 'IdMoneda' AS "Campo",
				"IdMoneda" As "ValorActual"
				FROM :v_instrumentos
		
		UNION ALL
		
		SELECT 'IdTipoInstrumento' AS "Campo",
				"IdTipoInstrumento" As "ValorActual"
				FROM :v_instrumentos
				
		UNION ALL
		
		SELECT 'IdEstado' AS "Campo",
				"IdEstado" As "ValorActual"
				FROM :v_instrumentos
				
		UNION ALL
		
		SELECT 'Tamano' AS "Campo",
				CAST("Tamano" As varchar) As "ValorActual"
				FROM :v_instrumentos
		
		UNION ALL
		
		SELECT 'IdEntidadClasificadoraRiesgo' AS "Campo",
				"IdEntidadClasificadoraRiesgo" As "ValorActual"
				FROM :v_instrumentos	
		
		UNION ALL
		
		SELECT 'ValorClasificacionRiesgo' AS "Campo",
				"ValorClasificacionRiesgo" As "ValorActual"
				FROM :v_instrumentos
				
		UNION ALL
		
		SELECT 'PrecioBase' AS "Campo",
				CAST("PrecioBase" As varchar)As "ValorActual"
				FROM :v_instrumentos	
				
		UNION ALL
		
		SELECT 'PrecioReferencia' AS "Campo",
				"PrecioReferencia" As "ValorActual"
				FROM :v_instrumentos	
				
		UNION ALL
		
		SELECT 'FechaReferencia' AS "Campo",
				CAST("FechaReferencia" As varchar) As "ValorActual"
				FROM :v_instrumentos
				
		UNION ALL
		
		SELECT 'FrecuenciaCupon' AS "Campo",
				CAST("FrecuenciaCupon" As varchar)As "ValorActual"
				FROM :v_instrumentos
				
		UNION ALL
		
		SELECT 'NumeroCupones' AS "Campo",
				CAST("NumeroCupones" As varchar) As "ValorActual"
				FROM :v_instrumentos	
				
		UNION ALL
		
		SELECT 'InstrumentoAcreditado' AS "Campo",
				COALESCE("InstrumentoAcreditado",'') As "ValorActual"
				FROM :v_instrumentos
				
		UNION ALL
		
		SELECT 'IdInstrumentoRelacionado' AS "Campo",
				CAST("IdInstrumentoRelacionado" As varchar) As "ValorActual"
				FROM :v_instrumentos
		
		UNION ALL
		
		SELECT 'EsPutEsCall' AS "Campo",
				"EsPutEsCall" As "ValorActual"
				FROM :v_instrumentos
		
		UNION ALL
		
		SELECT 'PrecioStrike' AS "Campo",
				CAST("PrecioStrike" As varchar) As "ValorActual"
				FROM :v_instrumentos
				
		UNION ALL
		
		SELECT 'TipoBonoIRF' AS "Campo",
				"TipoBonoIRF" As "ValorActual"
				FROM :v_instrumentos
				
		UNION ALL
		
		SELECT 'OrigenInstrumento' AS "Campo",
				"OrigenInstrumento" As "ValorActual"
				FROM :v_instrumentos
				
		UNION ALL
		
		SELECT 'TipoRenta' AS "Campo",
				"TipoRenta" As "ValorActual"
				FROM :v_instrumentos
				
		UNION ALL
		
		SELECT 'RelacionadoGrupo' AS "Campo",
				"RelacionadoGrupo" As "ValorActual"
				FROM :v_instrumentos

		UNION ALL
				
		SELECT 'FormadorMercado' AS "Campo",
				"FormadorMercado" As "ValorActual"
				FROM :v_instrumentos
		
		UNION ALL
				
		SELECT 'PactoAndino' AS "Campo",
				"PactoAndino" As "ValorActual"
				FROM :v_instrumentos
				
		UNION ALL
				
		SELECT 'FactorExposicion' AS "Campo",
				CAST("FactorExposicion" As varchar) As "ValorActual"
				FROM :v_instrumentos
				
		UNION ALL
				
		SELECT 'LargoPlazo' AS "Campo",
				"LargoPlazo" As "ValorActual"
				FROM :v_instrumentos
				
		UNION ALL
				
		SELECT 'Tasa' AS "Campo",
				CAST("Tasa" As varchar) As "ValorActual"
				FROM :v_instrumentos
				
		UNION ALL
				
		SELECT 'AfectoArt107' AS "Campo",
				"AfectoArt107" As "ValorActual"
				FROM :v_instrumentos
		UNION ALL
				
		SELECT 'AfectoArt104' AS "Campo",
				"AfectoArt104" As "ValorActual"
				FROM :v_instrumentos

		UNION ALL
				
		SELECT '"DividendoOtraMoneda"' AS "Campo",
				"DividendoOtraMoneda" As "ValorActual"
				FROM :v_instrumentos

		UNION ALL
				
		SELECT '"CobroComisionesCustodia"' AS "Campo",
				"CobroComisionesCustodia" As "ValorActual"
				FROM :v_instrumentos;

	END IF;
		CALL "BSCL.Plataforma.BD.Servicios::INS.CrearInstrumentos"(
			p_IdEmpresa => :p_IdEmpresa,
			p_IdUsuario => :p_IdUsuario,
			p_IdModulo => :p_IdModulo,
			p_simularOperacion => 'N',
			p_UsaReglasNegocio => 'N',
			p_Instrumentos => :v_instrumentos,
			p_Bolsas => :v_Bolsas,
			p_ValoresRiesgo => :v_valores_riesgo,
			p_out_Resultado => v_instrumentos_insertados,
			p_out_AdvertenciasContador => v_count_advertencia_ins,
			p_out_AdvertenciasDetalle => v_advertencias_instrumentos,
			p_out_ErroresContador => v_count_errores_ins,
			p_out_ErroresDetalle => v_errores_instrumentos
		); 
		
		v_series_validacion_instrumento = SELECT "Serie"."IdInterno" 							
								    			,"Instrumento"."IdInstrumento"		
												,CASE WHEN COALESCE("Instrumento"."IdInstrumento",0) = 0 AND COALESCE("Serie"."IdInstrumento",0) = 0 THEN 'ERR' ELSE 'OK' END AS "ResultadoInstrumento"
							 			  FROM  :v_series_antes_instrumento AS "Serie"
							 			  LEFT OUTER JOIN :v_instrumentos_insertados AS "Instrumento" ON ("Serie"."IdInterno" = "Instrumento"."IdInterno");
		
		BEGIN
			v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'ERROR_INSTRUMENTO' as "Codigo", "Series"."IdInterno" as "ValorParametro", 'IdInstrumento' AS "NombreParametro"
								   FROM :v_series_validacion_instrumento AS "Series" WHERE "ResultadoInstrumento" = 'ERR'
								   UNION ALL
								   SELECT * FROM :v_out_ErroresDetalle;
			SELECT COUNT(1) as "Count" INTO v_count_series_validacion_ins
			FROM :v_series_validacion_instrumento WHERE "ResultadoInstrumento" = 'OK';
			IF (v_count_series_validacion_ins = 0) THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;
		END;
		
		v_series_final = 	SELECT	"Serie"."IdInterno" 				
									,"Serie"."IdSerieFondo"
									,CASE WHEN "Serie"."IdInstrumento" = 0 THEN "Instrumento"."IdInstrumento" ELSE "Serie"."IdInstrumento" END AS "IdInstrumento"							
								    ,"Serie"."IdFondo"					
								    ,"Serie"."NombreSerieFondo"			
									,"Serie"."CodigoSerie"						
									,"Serie"."Alias"						
									,"Serie"."DecimalesValorCuota"		
									,"Serie"."DecimalesCuota"			
									,"Serie"."MontoMinimo"				
									,"Serie"."MontoMaximo"
									,"Serie"."DiasRetencionesAporte"
									,"Serie"."DiasRetencionesRescate"	
									,"Serie"."ExcluyeGarantia"			
									,"Serie"."CobraComision"			  	
									,"Serie"."Diferido"					
									,"Serie"."InstrumentoDCV"			
									,"Serie"."Omnibus"					
									,"Serie"."PatrimonioMinimo"			
									,"Serie"."IdSerieFondoOrigen"		
									,"Serie"."IdCliente"
									,"Serie"."IdPortafolio"
									,"Serie"."IdMedioDePagoAbono"
									,"Serie"."IdMedioDePagoCargo"
                                    ,"Serie"."DiasValutaValorCuotaAporte"
                                    ,"Serie"."DiasValutaValorCuotaRescate"
									,"Serie"."DiasValutaCertificacionAporte"
									,"Serie"."DiasValutaCertificacionRescate"
									,"Serie"."DiasValutaCurseAporte"
									,"Serie"."DiasValutaCurseRescate"
									,"Serie"."IdFamiliaFondo"
									,"Serie"."IdEstado"
									,"Serie"."CambioFondo"
									,"Serie"."DiasComision"
									,"Serie"."PorcentajeComision"
									,"Serie"."PorcentajeRemuneracion"
									,"Serie"."PorcentajeGastoOpera"
									,"Serie"."PorcentajeCostoAdm"
									,"Serie"."AportesAltos"
									,"Serie"."PatrimonioAltos"
									,"Serie"."MontoPatrimonioEmisor"
									,"Serie"."InstrumentoOWP"
									,"Serie"."EsAPV"
									,"Serie"."RestringeAporte"
									,"Serie"."RestringeRescate"
									,"Serie"."NemoCMF"
							FROM  :v_series_antes_instrumento AS "Serie"
							INNER JOIN :v_series_validacion_instrumento AS "Instrumento" ON ("Serie"."IdInterno" = "Instrumento"."IdInterno")
							WHERE "Instrumento"."ResultadoInstrumento" = 'OK';
		
		v_fondos_prevalicacion = SELECT "P_Fondo"."IdInterno",
									"P_Fondo"."IdFondo",
									"P_Fondo"."IdEmisor",
									"P_Fondo"."Nemotecnico",
									"P_Fondo"."NombreFondo",
									"P_Fondo"."IdTipoFondo",
									"P_Fondo"."IdPaisOrigen",
									"P_Fondo"."CodigoSVS",
									"P_Fondo"."MoneyMarket",
									"P_Fondo"."Vigencia",
									"P_Fondo"."FondoExtranjero",
									"P_Fondo"."IdMoneda",
									"P_Fondo"."IdTipoInstrumento",
									:p_IdEmpresa AS "IdEmpresa",
									"P_Fondo"."IdCategoriaPerfilFondoMutuo",
									"P_Fondo"."AperturaAutomaticaFondo",
									"P_Fondo"."FondoNormativo",
                                    "P_Fondo"."IdPaisEmisor",
                                    "P_Fondo"."IdTipoDocumentoIdentidadFondo",
                                    "P_Fondo"."DocumentoIdentidadFondo",
                                    "P_Fondo"."IdMercadoFondoMutuo"
								FROM :p_Fondo AS "P_Fondo" ;
		
		v_fondos_actualizar_maestro = SELECT
									"P_Fondo"."IdInterno",
									"P_Fondo"."IdFondo",
									"P_Fondo"."IdEmisor",
									"P_Fondo"."Nemotecnico",
									"P_Fondo"."NombreFondo",
									"P_Fondo"."IdTipoFondo",
									"P_Fondo"."IdPaisOrigen",
									"P_Fondo"."CodigoSVS",
									"P_Fondo"."MoneyMarket",
									"P_Fondo"."Vigencia",
									"P_Fondo"."FondoExtranjero",
									"P_Fondo"."IdMoneda",
									"P_Fondo"."IdTipoInstrumento",
									"P_Fondo"."IdCategoriaPerfilFondoMutuo",
									"P_Fondo"."AperturaAutomaticaFondo",
									"P_Fondo"."FondoNormativo",
                                    "P_Fondo"."IdPaisEmisor",
                                    "P_Fondo"."IdTipoDocumentoIdentidadFondo",
                                    "P_Fondo"."DocumentoIdentidadFondo",
                                    "P_Fondo"."IdMercadoFondoMutuo"
								FROM :v_fondos_prevalicacion AS "P_Fondo"
								INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("P_Fondo"."IdFondo" = "Fondo"."IdFondo" AND "P_Fondo"."IdEmpresa" = "Fondo"."IdEmpresa");
		
		v_fondos_nuevos_maestro = SELECT
									"P_Fondo"."IdInterno",
									"P_Fondo"."IdFondo",
									"P_Fondo"."IdEmisor",
									"P_Fondo"."Nemotecnico",
									"P_Fondo"."NombreFondo",
									"P_Fondo"."IdTipoFondo",
									"P_Fondo"."IdPaisOrigen",
									"P_Fondo"."CodigoSVS",
									"P_Fondo"."MoneyMarket",
									"P_Fondo"."Vigencia",
									"P_Fondo"."FondoExtranjero",
									"P_Fondo"."IdMoneda",
									"P_Fondo"."IdTipoInstrumento",
									"P_Fondo"."IdCategoriaPerfilFondoMutuo",
									"P_Fondo"."AperturaAutomaticaFondo",
									"P_Fondo"."FondoNormativo",
                                    "P_Fondo"."IdPaisEmisor",
                                    "P_Fondo"."IdTipoDocumentoIdentidadFondo",
                                    "P_Fondo"."DocumentoIdentidadFondo",
                                    "P_Fondo"."IdMercadoFondoMutuo"
								FROM :v_fondos_prevalicacion AS "P_Fondo"
								LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("P_Fondo"."IdFondo" = "Fondo"."IdFondo" AND "P_Fondo"."IdEmpresa" = "Fondo"."IdEmpresa")
								WHERE COALESCE("Fondo"."IdFondo",'-1') = '-1';
		
		v_regimenes_nuevos = SELECT "REGIMEN"."IdInterno"
									,"REGIMEN"."IdRegimenFondoMutuo"
									,"REGIMEN"."IdSerieFondo"
							  FROM :p_RegimenSerieFondo AS "REGIMEN"
							  INNER JOIN :v_series_final "SERIE" ON ("SERIE"."IdInterno" = "REGIMEN"."IdInterno" AND "SERIE"."IdSerieFondo" = "REGIMEN"."IdSerieFondo")
							  LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo" AS "REGIMEN_SERIE_FONDO" ON ("REGIMEN_SERIE_FONDO"."IdRegimenFondoMutuo" = "REGIMEN"."IdRegimenFondoMutuo"
							  																							  AND "SERIE"."IdSerieFondo" = "REGIMEN_SERIE_FONDO"."IdSerieFondo"
							  																							  AND "REGIMEN_SERIE_FONDO"."IdEmpresa" = :p_IdEmpresa
							  																							)
							  WHERE COALESCE("REGIMEN_SERIE_FONDO"."IdRegimenFondoMutuo",'') = '';
		
		
		
		v_regimenes_actualizar = SELECT "REGIMEN"."IdInterno"
										,"REGIMEN"."IdRegimenFondoMutuo"
										,"REGIMEN"."IdSerieFondo"
										,'ACT' AS "IdEstado"
								 FROM :p_RegimenSerieFondo AS "REGIMEN"
							  	 INNER JOIN :v_series_final "SERIE" ON ("SERIE"."IdInterno" = "REGIMEN"."IdInterno" AND "SERIE"."IdSerieFondo" = "REGIMEN"."IdSerieFondo")
							  	 INNER JOIN "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo" AS "REGIMEN_SERIE_FONDO" ON ("REGIMEN_SERIE_FONDO"."IdRegimenFondoMutuo" = "REGIMEN"."IdRegimenFondoMutuo"
							  																							  AND "SERIE"."IdSerieFondo" = "REGIMEN_SERIE_FONDO"."IdSerieFondo"
							  																							  AND "REGIMEN_SERIE_FONDO"."IdEmpresa" = :p_IdEmpresa
							  																							)
							  	UNION ALL 
							  	SELECT "SERIE"."IdInterno"
										,"REGIMEN_SERIE_FONDO"."IdRegimenFondoMutuo"
										,"REGIMEN_SERIE_FONDO"."IdSerieFondo"
										,'INA' AS "IdEstado"
								FROM "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo" AS "REGIMEN_SERIE_FONDO" 
								INNER JOIN :v_series_final "SERIE" ON ("SERIE"."IdSerieFondo" = "REGIMEN_SERIE_FONDO"."IdSerieFondo" AND "REGIMEN_SERIE_FONDO"."IdEmpresa" = :p_IdEmpresa)
								LEFT OUTER JOIN :p_RegimenSerieFondo AS "REGIMEN" ON ("REGIMEN_SERIE_FONDO"."IdRegimenFondoMutuo" = "REGIMEN"."IdRegimenFondoMutuo"
																					  AND "SERIE"."IdSerieFondo" = "REGIMEN"."IdSerieFondo"
																					  AND "SERIE"."IdInterno" = "REGIMEN"."IdInterno"
																					 )
								WHERE COALESCE("REGIMEN"."IdRegimenFondoMutuo",'') = '';
		---------------------------------------------------------------
		--Informacion Log fondo nuevo
		---------------------------------------------------------------
		SELECT count(1) INTO v_count_fondos_nuevos FROM :v_fondos_nuevos_maestro;

		IF (:v_count_fondos_nuevos > 0) THEN

		v_fondos_nuevos_real =
			SELECT		'IdFondo' AS "Campo",
						"P_Fondo"."IdFondo" AS "ValorActual"
						FROM :v_fondos_nuevos_maestro AS "P_Fondo"
						
			UNION ALL
			
			SELECT		'IdEmisor' AS "Campo",
						"P_Fondo"."IdEmisor" AS "ValorActual"
						FROM :v_fondos_nuevos_maestro AS "P_Fondo"            
			
			UNION ALL  	    
			
			SELECT		'Nemotecnico' AS "Campo",
						"P_Fondo"."Nemotecnico" AS "ValorActual"
						FROM :v_fondos_nuevos_maestro AS "P_Fondo"

			UNION ALL       
			
			SELECT		'NombreFondo' AS "Campo",
						"P_Fondo"."NombreFondo" AS "ValorActual"
						FROM :v_fondos_nuevos_maestro AS "P_Fondo"
						
			UNION ALL
			
			SELECT 		'IdTipoFondo' AS "Campo",
						COALESCE ("TFondoAct"."NombreCorto","P_Fondo"."IdTipoFondo") AS "ValorActual"
						FROM :v_fondos_nuevos_maestro AS "P_Fondo"
						LEFT JOIN "BSCL.FondoMutuo.BD::FMM.TipoFondo" AS "TFondoAct" ON ("P_Fondo"."IdTipoFondo" = "TFondoAct"."IdTipoFondo")

			UNION ALL 
			
			SELECT 		'CodigoSVS' AS "Campo",
						"P_Fondo"."CodigoSVS" AS "ValorActual"
						FROM :v_fondos_nuevos_maestro AS "P_Fondo"
						
			UNION ALL 
			
			SELECT 	
						'MoneyMarket' AS "Campo",
						"P_Fondo"."MoneyMarket" AS "ValorActual"
						FROM :v_fondos_nuevos_maestro AS "P_Fondo"
					
			UNION ALL 
			
			SELECT 		'Vigencia' AS "Campo",
						"P_Fondo"."Vigencia" AS "ValorActual"
						FROM :v_fondos_nuevos_maestro AS "P_Fondo"
			
			UNION ALL 
			
			SELECT 		'FondoExtranjero' AS "Campo",
						"P_Fondo"."FondoExtranjero" AS "ValorActual"
						FROM :v_fondos_nuevos_maestro AS "P_Fondo"
						
			UNION ALL 
			
			SELECT 		'IdMoneda' AS "Campo",
						"P_Fondo"."IdMoneda" AS "ValorActual"
						FROM :v_fondos_nuevos_maestro AS "P_Fondo"
						
			UNION ALL 
			
			SELECT 		'IdTipoInstrumento' AS "Campo",
						"P_Fondo"."IdTipoInstrumento" AS "ValorActual"
						FROM :v_fondos_nuevos_maestro AS "P_Fondo"
						
			UNION ALL 
			
			SELECT 		'IdCategoriaPerfilFondoMutuo' AS "Campo",
						"P_Fondo"."IdCategoriaPerfilFondoMutuo" AS "ValorActual"
						FROM :v_fondos_nuevos_maestro AS "P_Fondo"
						
			UNION ALL

			SELECT 		'AperturaAutomaticaFondo' AS "Campo",
						"P_Fondo"."AperturaAutomaticaFondo" AS "ValorActual"
						FROM :v_fondos_nuevos_maestro AS "P_Fondo"

			UNION ALL

			SELECT 		'FondoNormativo' AS "Campo",
						"P_Fondo"."FondoNormativo" AS "ValorActual"
						FROM :v_fondos_nuevos_maestro AS "P_Fondo"

			UNION ALL

			SELECT 		'IdTipoDocumentoIdentidadFondo' AS "Campo",
						"P_Fondo"."IdTipoDocumentoIdentidadFondo" AS "ValorActual"
						FROM :v_fondos_nuevos_maestro AS "P_Fondo"

			UNION ALL

			SELECT 		'DocumentoIdentidadFondo' AS "Campo",
						"P_Fondo"."DocumentoIdentidadFondo" AS "ValorActual"
						FROM :v_fondos_nuevos_maestro AS "P_Fondo"
						
			UNION ALL

			SELECT 		'IdMercadoFondoMutuo' AS "Campo",
						"P_Fondo"."IdMercadoFondoMutuo" AS "ValorActual"
						FROM :v_fondos_nuevos_maestro AS "P_Fondo"
						
			UNION ALL 
			
			SELECT	 	'IdPaisEmisor' AS "Campo",
						COALESCE ("POrigenAct"."NombrePais",CAST("P_Fondo"."IdPaisEmisor" AS varchar)) AS "ValorActual"
						FROM :v_fondos_nuevos_maestro AS "P_Fondo"
						LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" AS "POrigenAct" ON ("POrigenAct"."IdPais" = "P_Fondo"."IdPaisEmisor")
						
			UNION ALL 
			
			SELECT 		'IdPaisOrigen' AS "Campo",
						COALESCE ("POrigenAct"."NombrePais",CAST("P_Fondo"."IdPaisOrigen" AS varchar)) AS "ValorActual"
						FROM :v_fondos_nuevos_maestro AS "P_Fondo"
						LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" AS "POrigenAct" ON ("POrigenAct"."IdPais" = "P_Fondo"."IdPaisOrigen");

		END IF;
		-----------------------------------------------------------------------
		--Informacion Log fondo modificado
		-----------------------------------------------------------------------
		SELECT count(1) INTO v_count_fondos_actualizar FROM :v_fondos_actualizar_maestro;

		IF (:v_count_fondos_actualizar > 0) THEN 

		v_tabla_fondo =
			SELECT 		* FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo"
						WHERE "Fondo"."IdFondo" IN (SELECT "P_Fondo"."IdFondo" FROM :v_fondos_prevalicacion AS "P_Fondo") AND "Fondo"."IdEmpresa"= :p_IdEmpresa;

		v_fondos_actualizar_real =
			SELECT		'IdEmisor' AS "Campo",
						"P_Fondo"."IdEmisor" AS "ValorActual",
						"Fondo"."IdEmisor" AS "ValorAnterior",
						CASE
							WHEN "P_Fondo"."IdEmisor" != "Fondo"."IdEmisor" THEN 'S'
						END AS "Modificado"
						FROM :v_fondos_prevalicacion AS "P_Fondo"
						INNER JOIN :v_tabla_fondo  AS "Fondo" ON ("P_Fondo"."IdFondo" = "Fondo"."IdFondo")
					
			UNION ALL  	    
			
			SELECT		'Nemotecnico' AS "Campo",
						"P_Fondo"."Nemotecnico" AS "ValorActual",
						"Fondo"."Nemotecnico" AS "ValorAnterior",
						CASE
							WHEN "P_Fondo"."Nemotecnico" != "Fondo"."Nemotecnico" THEN 'S'
						END AS "Modificado"
						FROM :v_fondos_prevalicacion AS "P_Fondo"
						INNER JOIN :v_tabla_fondo  AS "Fondo" ON ("P_Fondo"."IdFondo" = "Fondo"."IdFondo")
						
			UNION ALL       
			
			SELECT		'NombreFondo' AS "Campo",
						"P_Fondo"."NombreFondo" AS "ValorActual",
						"Fondo"."NombreFondo" AS "ValorAnterior",
						CASE
							WHEN "P_Fondo"."NombreFondo" != "Fondo"."NombreFondo" THEN 'S'
						END AS "Modificado"
						FROM :v_fondos_prevalicacion AS "P_Fondo"
						INNER JOIN :v_tabla_fondo  AS "Fondo" ON ("P_Fondo"."IdFondo" = "Fondo"."IdFondo")
						
			UNION ALL
			
			SELECT 		'IdTipoFondo' AS "Campo",
						COALESCE ("TFondoAct"."NombreCorto","P_Fondo"."IdTipoFondo") AS "ValorActual",
						COALESCE ("TFondoAnt"."NombreCorto","Fondo"."IdTipoFondo") AS "ValorAnterior",
						CASE
							WHEN "P_Fondo"."IdTipoFondo" != "Fondo"."IdTipoFondo" THEN 'S'
						END AS "Modificado"
						FROM :v_fondos_prevalicacion AS "P_Fondo"
						INNER JOIN :v_tabla_fondo  AS "Fondo" ON ("P_Fondo"."IdFondo" = "Fondo"."IdFondo")
						LEFT JOIN "BSCL.FondoMutuo.BD::FMM.TipoFondo" AS "TFondoAct" ON ("P_Fondo"."IdTipoFondo" = "TFondoAct"."IdTipoFondo")
						LEFT JOIN "BSCL.FondoMutuo.BD::FMM.TipoFondo" AS "TFondoAnt" ON ("Fondo"."IdTipoFondo" = "TFondoAnt"."IdTipoFondo")
									
			UNION ALL 
			
			SELECT 		'CodigoSVS' AS "Campo",
						"P_Fondo"."CodigoSVS" AS "ValorActual",
						"Fondo"."CodigoSVS" AS "ValorAnterior",
						CASE
							WHEN "P_Fondo"."CodigoSVS" != "Fondo"."CodigoSVS" THEN 'S'
						END AS "Modificado"
						FROM :v_fondos_prevalicacion AS "P_Fondo"
						INNER JOIN :v_tabla_fondo  AS "Fondo" ON ("P_Fondo"."IdFondo" = "Fondo"."IdFondo")
						
			UNION ALL 
			
			SELECT 	
						'MoneyMarket' AS "Campo",
						"P_Fondo"."MoneyMarket" AS "ValorActual",
						"Fondo"."MoneyMarket" AS "ValorAnterior",
						CASE
							WHEN "P_Fondo"."MoneyMarket" != "Fondo"."MoneyMarket" THEN 'S'
						END AS "Modificado"
						FROM :v_fondos_prevalicacion AS "P_Fondo"
						INNER JOIN :v_tabla_fondo  AS "Fondo" ON ("P_Fondo"."IdFondo" = "Fondo"."IdFondo")
					
			UNION ALL 
			
			SELECT 		'Vigencia' AS "Campo",
						"P_Fondo"."Vigencia" AS "ValorActual",
						"Fondo"."Vigencia" AS "ValorAnterior",
						CASE
							WHEN "P_Fondo"."Vigencia" != "Fondo"."Vigencia" THEN 'S'
						END AS "Modificado"
						FROM :v_fondos_prevalicacion AS "P_Fondo"
						INNER JOIN :v_tabla_fondo  AS "Fondo" ON ("P_Fondo"."IdFondo" = "Fondo"."IdFondo")
			
			UNION ALL 
			
			SELECT 		'FondoExtranjero' AS "Campo",
						"P_Fondo"."FondoExtranjero" AS "ValorActual",
						"Fondo"."FondoExtranjero" AS "ValorAnterior",
						CASE
							WHEN "P_Fondo"."FondoExtranjero" != "Fondo"."FondoExtranjero" THEN 'S'
						END AS "Modificado"
						FROM :v_fondos_prevalicacion AS "P_Fondo"
						INNER JOIN :v_tabla_fondo  AS "Fondo" ON ("P_Fondo"."IdFondo" = "Fondo"."IdFondo")
						
			UNION ALL 
			
			SELECT 		'IdMoneda' AS "Campo",
						"P_Fondo"."IdMoneda" AS "ValorActual",
						"Fondo"."IdMoneda" AS "ValorAnterior",
						CASE
							WHEN "P_Fondo"."IdMoneda" != "Fondo"."IdMoneda" THEN 'S'
						END AS "Modificado"
						FROM :v_fondos_prevalicacion AS "P_Fondo"
						INNER JOIN :v_tabla_fondo  AS "Fondo" ON ("P_Fondo"."IdFondo" = "Fondo"."IdFondo")
						
			UNION ALL 
			
			SELECT 		'IdTipoInstrumento' AS "Campo",
						"P_Fondo"."IdTipoInstrumento" AS "ValorActual",
						"Fondo"."IdTipoInstrumento" AS "ValorAnterior",
						CASE
							WHEN "P_Fondo"."IdTipoInstrumento" != "Fondo"."IdTipoInstrumento" THEN 'S'
						END AS "Modificado"
						FROM :v_fondos_prevalicacion AS "P_Fondo"
						INNER JOIN :v_tabla_fondo  AS "Fondo" ON ("P_Fondo"."IdFondo" = "Fondo"."IdFondo")
						
			UNION ALL 
			
			SELECT 		'IdCategoriaPerfilFondoMutuo' AS "Campo",
						"P_Fondo"."IdCategoriaPerfilFondoMutuo" AS "ValorActual",
						"Fondo"."IdCategoriaPerfilFondoMutuo" AS "ValorAnterior",
						CASE
							WHEN "P_Fondo"."IdCategoriaPerfilFondoMutuo" != "Fondo"."IdCategoriaPerfilFondoMutuo" THEN 'S'
						END AS "Modificado"
						FROM :v_fondos_prevalicacion AS "P_Fondo"
						INNER JOIN :v_tabla_fondo  AS "Fondo" ON ("P_Fondo"."IdFondo" = "Fondo"."IdFondo")
						
			UNION ALL

			SELECT 		'AperturaAutomaticaFondo' AS "Campo",
						"P_Fondo"."AperturaAutomaticaFondo" AS "ValorActual",
						"Fondo"."AperturaAutomaticaFondo" AS "ValorAnterior",
						CASE
							WHEN "P_Fondo"."AperturaAutomaticaFondo" != "Fondo"."AperturaAutomaticaFondo" THEN 'S'
						END AS "Modificado"
						FROM :v_fondos_prevalicacion AS "P_Fondo"
						INNER JOIN :v_tabla_fondo  AS "Fondo" ON ("P_Fondo"."IdFondo" = "Fondo"."IdFondo")

			UNION ALL

			SELECT 		'FondoNormativo' AS "Campo",
						"P_Fondo"."FondoNormativo" AS "ValorActual",
						"Fondo"."FondoNormativo" AS "ValorAnterior",
						CASE
							WHEN "P_Fondo"."FondoNormativo" != "Fondo"."FondoNormativo" THEN 'S'
						END AS "Modificado"
						FROM :v_fondos_prevalicacion AS "P_Fondo"
						INNER JOIN :v_tabla_fondo  AS "Fondo" ON ("P_Fondo"."IdFondo" = "Fondo"."IdFondo")

			UNION ALL

			SELECT 		'IdTipoDocumentoIdentidadFondo' AS "Campo",
						"P_Fondo"."IdTipoDocumentoIdentidadFondo" AS "ValorActual",
						"Fondo"."IdTipoDocumentoIdentidadFondo" AS "ValorAnterior",
						CASE
							WHEN "P_Fondo"."IdTipoDocumentoIdentidadFondo" != "Fondo"."IdTipoDocumentoIdentidadFondo" THEN 'S'
						END AS "Modificado"
						FROM :v_fondos_prevalicacion AS "P_Fondo"
						INNER JOIN :v_tabla_fondo  AS "Fondo" ON ("P_Fondo"."IdFondo" = "Fondo"."IdFondo")

			UNION ALL

			SELECT 		'DocumentoIdentidadFondo' AS "Campo",
						"P_Fondo"."DocumentoIdentidadFondo" AS "ValorActual",
						"Fondo"."DocumentoIdentidadFondo" AS "ValorAnterior",
						CASE
							WHEN "P_Fondo"."DocumentoIdentidadFondo" != "Fondo"."DocumentoIdentidadFondo" THEN 'S'
						END AS "Modificado"
						FROM :v_fondos_prevalicacion AS "P_Fondo"
						INNER JOIN :v_tabla_fondo  AS "Fondo" ON ("P_Fondo"."IdFondo" = "Fondo"."IdFondo")
						
			UNION ALL

			SELECT 		'IdMercadoFondoMutuo' AS "Campo",
						"P_Fondo"."IdMercadoFondoMutuo" AS "ValorActual",
						"Fondo"."IdMercadoFondoMutuo" AS "ValorAnterior",
						CASE
							WHEN "P_Fondo"."IdMercadoFondoMutuo" != "Fondo"."IdMercadoFondoMutuo" THEN 'S'
						END AS "Modificado"
						FROM :v_fondos_prevalicacion AS "P_Fondo"
						INNER JOIN :v_tabla_fondo  AS "Fondo" ON ("P_Fondo"."IdFondo" = "Fondo"."IdFondo")
									
			UNION ALL 
			
				SELECT 	'IdPaisEmisor' AS "Campo",
						COALESCE ("POrigenAct"."NombrePais",CAST( "P_Fondo"."IdPaisEmisor" AS varchar ) ) AS "ValorActual",
						COALESCE ("POrigenAnt"."NombrePais",CAST ("Fondo"."IdPaisEmisor" AS varchar )) AS "ValorAnterior",
						CASE
							WHEN "P_Fondo"."IdPaisEmisor" != "Fondo"."IdPaisEmisor" THEN 'S'
						END AS "Modificado"
						FROM :v_fondos_prevalicacion AS "P_Fondo"
						INNER JOIN :v_tabla_fondo  AS "Fondo" ON ("P_Fondo"."IdFondo" = "Fondo"."IdFondo")
						LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" AS "POrigenAct" ON ("POrigenAct"."IdPais" = "P_Fondo"."IdPaisEmisor")
						LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" AS "POrigenAnt" ON ("POrigenAnt"."IdPais" = "Fondo"."IdPaisEmisor")
						
			UNION ALL 
			
			SELECT 		'IdPaisOrigen' AS "Campo",
						COALESCE ("POrigenAct"."NombrePais",CAST("P_Fondo"."IdPaisOrigen" AS varchar)) AS "ValorActual",
						COALESCE ("POrigenAnt"."NombrePais",CAST("Fondo"."IdPaisOrigen" AS varchar)) AS "ValorAnterior",         
						CASE
							WHEN "P_Fondo"."IdPaisOrigen" != "Fondo"."IdPaisOrigen" THEN 'S'
						END AS "Modificado"
						FROM :v_fondos_prevalicacion AS "P_Fondo"
						INNER JOIN :v_tabla_fondo  AS "Fondo" ON ("P_Fondo"."IdFondo" = "Fondo"."IdFondo")
						LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" AS "POrigenAct" ON ("POrigenAct"."IdPais" = "P_Fondo"."IdPaisOrigen")
						LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" AS "POrigenAnt" ON ("POrigenAnt"."IdPais" = "Fondo"."IdPaisOrigen");

		END IF;
		--------------------------------------------------------------------------
		--Informacion Log serie modificada
		--------------------------------------------------------------------------
		SELECT count(1) INTO v_count_serie_fondo_modificado FROM :v_series_final;

		IF (:v_count_serie_fondo_modificado > 0) THEN 

		v_tabla_serie_fondo=
			SELECT 	* FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "Serie" 
					WHERE "Serie"."IdSerieFondo" IN (SELECT "P_Serie"."IdSerieFondo" FROM :v_series_final AS "P_Serie") AND "Serie"."IdEmpresa" = :p_IdEmpresa;

		v_serie_fondo_actualizados =
			SELECT  'IdSerieFondo' AS "Campo",
					"P_Serie"."IdSerieFondo" AS "ValorActual",
					"Serie"."IdSerieFondo" AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."IdSerieFondo" != "Serie"."IdSerieFondo" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
			
			UNION ALL
			
			SELECT  'IdInstrumento' AS "Campo",
					CAST("P_Serie"."IdInstrumento" AS varchar) AS "ValorActual",
					CAST("Serie"."IdInstrumento" AS varchar) AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."IdInstrumento" != "Serie"."IdInstrumento" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
			
			UNION ALL
			
			SELECT  'IdFondo' AS "Campo",
					CAST("P_Serie"."IdFondo" AS varchar) AS "ValorActual",
					CAST("Serie"."IdFondo" AS varchar) AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."IdFondo" != "Serie"."IdFondo" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
			
			UNION ALL
			
			SELECT  'NombreSerieFondo' AS "Campo",
					"P_Serie"."NombreSerieFondo" AS "ValorActual",
					"Serie"."NombreSerieFondo" AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."NombreSerieFondo" != "Serie"."NombreSerieFondo" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
				
			UNION ALL
			
			SELECT  'CodigoSerie' AS "Campo",
					"P_Serie"."CodigoSerie" AS "ValorActual",
					"Serie"."CodigoSerie" AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."CodigoSerie" != "Serie"."CodigoSerie" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
					
			UNION ALL
			
			SELECT  'Alias' AS "Campo",
					"P_Serie"."Alias" AS "ValorActual",
					"Serie"."Alias" AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."Alias" != "Serie"."Alias" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
					
			UNION ALL
			
			SELECT  'DecimalesValorCuota' AS "Campo",
					CAST("P_Serie"."DecimalesValorCuota" AS varchar) AS "ValorActual",
					CAST("Serie"."DecimalesValorCuota" AS varchar) AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."DecimalesValorCuota" != "Serie"."DecimalesValorCuota" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
					
			UNION ALL
			
			SELECT  'DecimalesCuota' AS "Campo",
					CAST("P_Serie"."DecimalesCuota" AS varchar) AS "ValorActual",
					CAST("Serie"."DecimalesCuota" AS varchar) AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."DecimalesCuota" != "Serie"."DecimalesCuota" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
					
			UNION ALL
			
			SELECT  'MontoMinimo' AS "Campo",
					CAST("P_Serie"."MontoMinimo" AS varchar) AS "ValorActual",
					CAST("Serie"."MontoMinimo" AS varchar) AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."MontoMinimo" != "Serie"."MontoMinimo" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
					
			UNION ALL
			
			SELECT  'MontoMaximo' AS "Campo",
					CAST("P_Serie"."MontoMaximo" AS varchar) AS "ValorActual",
					CAST("Serie"."MontoMaximo" AS varchar) AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."MontoMaximo" != "Serie"."MontoMaximo" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)      
					
			UNION ALL
			
			SELECT  'DiasRetencionesAporte' AS "Campo",
					CAST("P_Serie"."DiasRetencionesAporte" AS varchar) AS "ValorActual",
					CAST("Serie"."DiasRetencionesAporte" AS varchar) AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."DiasRetencionesAporte" != "Serie"."DiasRetencionesAporte" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
					
			UNION ALL
			
			SELECT  'DiasRetencionesRescate' AS "Campo",
					CAST("P_Serie"."DiasRetencionesRescate" AS varchar) AS "ValorActual",
					CAST("Serie"."DiasRetencionesRescate" AS varchar) AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."DiasRetencionesRescate" != "Serie"."DiasRetencionesRescate" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
					
			UNION ALL
			
			SELECT  'ExcluyeGarantia' AS "Campo",
					"P_Serie"."ExcluyeGarantia" AS "ValorActual",
					"Serie"."ExcluyeGarantia" AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."ExcluyeGarantia" != "Serie"."ExcluyeGarantia" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
					
			UNION ALL
			
			SELECT  'CobraComision' AS "Campo",
					"P_Serie"."CobraComision" AS "ValorActual",
					"Serie"."CobraComision" AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."CobraComision" != "Serie"."CobraComision" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
					
			UNION ALL
			
			SELECT  'Diferido' AS "Campo",
					"P_Serie"."Diferido" AS "ValorActual",
					"Serie"."Diferido" AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."Diferido" != "Serie"."Diferido" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
					
			UNION ALL
			
			SELECT  'InstrumentoDCV' AS "Campo",
					"P_Serie"."InstrumentoDCV" AS "ValorActual",
					"Serie"."InstrumentoDCV" AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."InstrumentoDCV" != "Serie"."InstrumentoDCV" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
					
			UNION ALL
			
			SELECT  'Omnibus' AS "Campo",
					"P_Serie"."Omnibus" AS "ValorActual",
					"Serie"."Omnibus" AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."Omnibus" != "Serie"."Omnibus" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
					
			UNION ALL
			
			SELECT  'PatrimonioMinimo' AS "Campo",
					CAST("P_Serie"."PatrimonioMinimo" AS varchar) AS "ValorActual",
					CAST("Serie"."PatrimonioMinimo" AS varchar) AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."PatrimonioMinimo" != "Serie"."PatrimonioMinimo" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
					
			UNION ALL
			
			SELECT  'IdSerieFondoOrigen' AS "Campo",
					"P_Serie"."IdSerieFondoOrigen" AS "ValorActual",
					"Serie"."IdSerieFondoOrigen" AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."IdSerieFondoOrigen" != "Serie"."IdSerieFondoOrigen" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
					
			UNION ALL
			
			SELECT  'IdCliente' AS "Campo",
					CAST("P_Serie"."IdCliente" AS varchar) AS "ValorActual",
					CAST("Serie"."IdCliente" AS varchar) AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."IdCliente" != "Serie"."IdCliente" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)      
			
			UNION ALL
			
			SELECT  'IdPortafolio' AS "Campo",
					CAST("P_Serie"."IdPortafolio" AS varchar) AS "ValorActual",
					CAST("Serie"."IdPortafolio" AS varchar) AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."IdPortafolio" != "Serie"."IdPortafolio" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
			
			UNION ALL
			
			SELECT  'IdMedioDePagoAbono' AS "Campo",
					"P_Serie"."IdMedioDePagoAbono" AS "ValorActual",
					"Serie"."IdMedioDePagoAbono" AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."IdMedioDePagoAbono" != "Serie"."IdMedioDePagoAbono" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
					
			UNION ALL
			
			SELECT  'IdMedioDePagoCargo' AS "Campo",
					"P_Serie"."IdMedioDePagoCargo" AS "ValorActual",
					"Serie"."IdMedioDePagoCargo" AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."IdMedioDePagoCargo" != "Serie"."IdMedioDePagoCargo" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
					
			UNION ALL
			
			SELECT  'DiasValutaValorCuotaAporte' AS "Campo",
					CAST("P_Serie"."DiasValutaValorCuotaAporte" AS varchar) AS "ValorActual",
					CAST("Serie"."DiasValutaValorCuotaAporte" AS varchar) AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."DiasValutaValorCuotaAporte" != "Serie"."DiasValutaValorCuotaAporte" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
					
			UNION ALL
			
			SELECT  'DiasValutaValorCuotaRescate' AS "Campo",
					CAST("P_Serie"."DiasValutaValorCuotaRescate" AS varchar) AS "ValorActual",
					CAST("Serie"."DiasValutaValorCuotaRescate" AS varchar) AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."DiasValutaValorCuotaRescate" != "Serie"."DiasValutaValorCuotaRescate" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
					
			UNION ALL
			
			SELECT  'DiasValutaCertificacionAporte' AS "Campo",
					CAST("P_Serie"."DiasValutaCertificacionAporte" AS varchar) AS "ValorActual",
					CAST("Serie"."DiasValutaCertificacionAporte" AS varchar) AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."DiasValutaCertificacionAporte" != "Serie"."DiasValutaCertificacionAporte" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
				
			UNION ALL
			
			SELECT  'DiasValutaCertificacionRescate' AS "Campo",
					CAST("P_Serie"."DiasValutaCertificacionRescate" AS varchar) AS "ValorActual",
					CAST("Serie"."DiasValutaCertificacionRescate" AS varchar) AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."DiasValutaCertificacionRescate" != "Serie"."DiasValutaCertificacionRescate" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
					
			UNION ALL
			
			SELECT  'DiasValutaCurseAporte' AS "Campo",
					CAST("P_Serie"."DiasValutaCurseAporte" AS varchar) AS "ValorActual",
					CAST("Serie"."DiasValutaCurseAporte" AS varchar) AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."DiasValutaCurseAporte" != "Serie"."DiasValutaCurseAporte" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
					
			UNION ALL
			
			SELECT  'DiasValutaCurseRescate' AS "Campo",
					CAST("P_Serie"."DiasValutaCurseRescate" AS varchar) AS "ValorActual",
					CAST("Serie"."DiasValutaCurseRescate" AS varchar) AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."DiasValutaCurseRescate" != "Serie"."DiasValutaCurseRescate" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
					
			UNION ALL
			
			SELECT  'IdFamiliaFondo' AS "Campo",
					"P_Serie"."IdFamiliaFondo" AS "ValorActual",
					"Serie"."IdFamiliaFondo" AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."IdFamiliaFondo" != "Serie"."IdFamiliaFondo" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
					
			UNION ALL
			
			SELECT  'IdEstado' AS "Campo",
					"P_Serie"."IdEstado" AS "ValorActual",
					"Serie"."IdEstado" AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."IdEstado" != "Serie"."IdEstado" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
					
			UNION ALL
			
			SELECT  'CambioFondo' AS "Campo",
					"P_Serie"."CambioFondo" AS "ValorActual",
					"Serie"."CambioFondo" AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."CambioFondo" != "Serie"."CambioFondo" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
					
			UNION ALL
			
			SELECT  'DiasComision' AS "Campo",
					CAST("P_Serie"."DiasComision" AS varchar) AS "ValorActual",
					CAST("Serie"."DiasComision" AS varchar) AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."DiasComision" != "Serie"."DiasComision" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
					
			UNION ALL
			
			SELECT  'PorcentajeComision' AS "Campo",
					CAST("P_Serie"."PorcentajeComision" AS varchar) AS "ValorActual",
					CAST("Serie"."PorcentajeComision" AS varchar) AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."PorcentajeComision" != "Serie"."PorcentajeComision" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
					
			UNION ALL
			
			SELECT  'PorcentajeRemuneracion' AS "Campo",
					CAST("P_Serie"."PorcentajeRemuneracion" AS varchar) AS "ValorActual",
					CAST("Serie"."PorcentajeRemuneracion" AS varchar) AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."PorcentajeRemuneracion" != "Serie"."PorcentajeRemuneracion" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
			
			UNION ALL
					
			SELECT  'PorcentajeGastoOpera' AS "Campo",
					CAST("P_Serie"."PorcentajeGastoOpera" AS varchar) AS "ValorActual",
					CAST("Serie"."PorcentajeGastoOpera" AS varchar) AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."PorcentajeGastoOpera" != "Serie"."PorcentajeGastoOpera" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
					
			UNION ALL
			
			SELECT  'PorcentajeCostoAdm' AS "Campo",
					CAST("P_Serie"."PorcentajeCostoAdm" AS varchar) AS "ValorActual",
					CAST("Serie"."PorcentajeCostoAdm" AS varchar) AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."PorcentajeCostoAdm" != "Serie"."PorcentajeCostoAdm" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
					
			UNION ALL
			
			SELECT  'AportesAltos' AS "Campo",
					"P_Serie"."AportesAltos" AS "ValorActual",
					"Serie"."AportesAltos" AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."AportesAltos" != "Serie"."AportesAltos" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
			
			UNION ALL
			
			SELECT  'PatrimonioAltos' AS "Campo",
					"P_Serie"."PatrimonioAltos" AS "ValorActual",
					"Serie"."PatrimonioAltos" AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."PatrimonioAltos" != "Serie"."PatrimonioAltos" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
					
			UNION ALL
			
			SELECT  'MontoPatrimonioEmisor' AS "Campo",
					CAST("P_Serie"."MontoPatrimonioEmisor" AS varchar) AS "ValorActual",
					CAST("Serie"."MontoPatrimonioEmisor" AS varchar) AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."MontoPatrimonioEmisor" != "Serie"."MontoPatrimonioEmisor" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
					
			UNION ALL
			
			SELECT  'InstrumentoOWP' AS "Campo",
					"P_Serie"."InstrumentoOWP" AS "ValorActual",
					"Serie"."InstrumentoOWP" AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."InstrumentoOWP" != "Serie"."InstrumentoOWP" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
					
			UNION ALL
			
			SELECT  'EsAPV' AS "Campo",
					"P_Serie"."EsAPV" AS "ValorActual",
					"Serie"."EsAPV" AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."EsAPV" != "Serie"."EsAPV" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
					
			UNION ALL
			
			SELECT  'RestringeAporte' AS "Campo",
					"P_Serie"."RestringeAporte" AS "ValorActual",
					"Serie"."RestringeAporte" AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."RestringeAporte" != "Serie"."RestringeAporte" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
					
			UNION ALL
			
			SELECT  'RestringeRescate' AS "Campo",
					"P_Serie"."RestringeRescate" AS "ValorActual",
					"Serie"."RestringeRescate" AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."RestringeRescate" != "Serie"."RestringeRescate" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa)
					
			UNION ALL
			
			SELECT  'NemoCMF' AS "Campo",
					"P_Serie"."NemoCMF" AS "ValorActual",
					"Serie"."NemoCMF" AS "ValorAnterior",
					CASE
						WHEN "P_Serie"."NemoCMF" != "Serie"."NemoCMF" THEN 'S'
					END AS "Modificado"
					FROM :v_series_final AS "P_Serie"
					INNER JOIN :v_tabla_serie_fondo AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "Serie"."IdEmpresa" = :p_IdEmpresa);
				
		END IF;
		------------------------------------------------------------------------
		--Informacion Log regimen nuevo 
		------------------------------------------------------------------------
		SELECT count(1) INTO v_count_regimen_nuevo FROM :v_regimenes_nuevos;

		IF (:v_count_regimen_nuevo > 0) THEN 
		v_regimenes_nuevos_log=
			SELECT 	"IdRegimenFondoMutuo" AS "IdRegimenFondoMutuo",
					'IdEstado' AS "Campo",
					'ACT' AS "ValorActual"
					FROM :v_regimenes_nuevos;
		END IF;

		---------------------------------------------------------------------
		--Informacion Log regimen modificado
		---------------------------------------------------------------------
		SELECT count(1) INTO v_count_regimen_actualizado FROM :v_regimenes_actualizar;

		IF (:v_count_regimen_actualizado > 0) THEN 
		v_regimenes_modificados=
		SELECT  
				"Regimen"."IdSerieFondo" AS "IdSerieFondo",
				"Regimen"."IdRegimenFondoMutuo" AS "IdRegimenFondoMutuo",
				'IdEstado' AS "Campo",
				"P_Regimen"."IdEstado" AS "ValorActual",
				"Regimen"."IdEstado" AS "ValorAnterior",
				CASE
					WHEN "P_Regimen"."IdEstado" != "Regimen"."IdEstado" THEN 'S'
				END AS "Modificado"
				FROM :v_regimenes_actualizar AS "P_Regimen"
				INNER JOIN "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo" AS "Regimen" ON ("Regimen"."IdRegimenFondoMutuo" = "P_Regimen"."IdRegimenFondoMutuo" AND "Regimen"."IdSerieFondo"  = "P_Regimen"."IdSerieFondo"AND "Regimen"."IdEmpresa" = :p_IdEmpresa);
		END IF;


		
		CALL "BSCL.FondoMutuo.BD.Servicios::CrearFondo"(:p_IdEmpresa, :v_fondos_nuevos_maestro, v_fondos_nuevos);				
		
		CALL "BSCL.FondoMutuo.BD.Servicios::ModificarFondo"(:p_IdEmpresa, :v_fondos_actualizar_maestro, v_fondos_actualizados);
		
		CALL "BSCL.FondoMutuo.BD.Servicios::ModificarSerieDeFondo"(:p_IdEmpresa, :v_series_final, v_series_fondos);
		
		CALL "BSCL.FondoMutuo.BD.Servicios::CrearRegimenSerieDeFondo"(:p_IdEmpresa, :v_regimenes_nuevos, v_regimenes_fondos);
		
		CALL "BSCL.FondoMutuo.BD.Servicios::ModificarRegimenSerieDeFondo"(:p_IdEmpresa, :v_regimenes_actualizar, v_regimenes_fondos_actualizados);
		
		v_target_estado = 	SELECT DISTINCT S."IdInstrumento", S."IdEstado"
		  					FROM :v_series_final S
		  					WHERE S."IdEstado" IN ('INA','ACT');
		  					
  		MERGE INTO "BSCL.Plataforma.BD::INS.Instrumento" AS I
		USING :v_target_estado AS T
		   ON (I."IdInstrumento" = T."IdInstrumento")
		WHEN MATCHED AND COALESCE(I."IdEstado",'') <> COALESCE(T."IdEstado",'')
		THEN UPDATE SET I."IdEstado" = T."IdEstado";
				
		p_out_Resultado = SELECT 	
									"Fondo"."IdInterno"					
									,"Fondo"."IdFondo"					
									,"Fondo"."IdEmisor"					
									,"Fondo"."Nemotecnico"				
									,"Fondo"."NombreFondo"				
									,"Fondo"."IdTipoFondo"				
									,"Fondo"."IdPaisOrigen"				
									,"Fondo"."CodigoSVS"								
									,"Fondo"."MoneyMarket"   			 
									,"Fondo"."FechaCreacion"				
									,"Fondo"."FechaIngresoRegistro"		
									,"Fondo"."FechaConstitucion" 		
									,"Fondo"."Vigencia"					
									,"Fondo"."FondoExtranjero"
									,"Fondo"."IdMoneda"
									,"Fondo"."IdTipoInstrumento"
									,"SerieFondo"."IdFamiliaFondo"	
									,"SerieFondo"."IdSerieFondo"				
									,"SerieFondo"."IdInstrumento"				
									,"SerieFondo"."NombreSerieFondo"			
									,"SerieFondo"."CodigoSerie"						
									,"SerieFondo"."Alias"	
									,"SerieFondo"."DecimalesValorCuota"	
									,"SerieFondo"."DecimalesCuota"				
									,"SerieFondo"."MontoMinimo"				
									,"SerieFondo"."MontoMaximo"
									,"SerieFondo"."DiasRetencionesAporte"
									,"SerieFondo"."DiasRetencionesRescate"	
									,"SerieFondo"."ExcluyeGarantia"
									,"SerieFondo"."CobraComision"
									,"SerieFondo"."Diferido"
									,"SerieFondo"."InstrumentoDCV"
									,"SerieFondo"."Omnibus"				
									,"SerieFondo"."PatrimonioMinimo"			
									,"SerieFondo"."IdSerieFondoOrigen"		
									,"SerieFondo"."IdEstado"
									,"SerieFondo"."FechaIngresoRegistro" AS "FechaIngresoRegistroSerie"
									,"SerieFondo"."IdCliente"
									,"SerieFondo"."IdPortafolio"
									,"SerieFondo"."IdMedioDePagoAbono"
									,"SerieFondo"."IdMedioDePagoCargo"
									,"SerieFondo"."DiasValutaValorCuotaAporte"
                                    ,"SerieFondo"."DiasValutaValorCuotaRescate"
									,"SerieFondo"."DiasValutaCertificacionAporte"
									,"SerieFondo"."DiasValutaCertificacionRescate"
									,"SerieFondo"."DiasValutaCurseAporte"
									,"SerieFondo"."DiasValutaCurseRescate"
									,"SerieFondo"."CambioFondo"
									,"SerieFondo"."DiasComision"
									,"SerieFondo"."PorcentajeComision"
									,"SerieFondo"."PorcentajeRemuneracion"
									,"SerieFondo"."PorcentajeGastoOpera"
									,"SerieFondo"."PorcentajeCostoAdm"									
									,"Fondo"."AperturaAutomaticaFondo"		
									,"Fondo"."FondoNormativo"		
									,"Fondo"."IdEmpresa"	
									,"Fondo"."IdCategoriaPerfilFondoMutuo"
                                    ,"Fondo"."IdPaisEmisor"
                                    ,"Fondo"."IdTipoDocumentoIdentidadFondo"
                                    ,"Fondo"."DocumentoIdentidadFondo"
                                    ,"Fondo"."IdMercadoFondoMutuo"
							FROM :v_fondos_nuevos AS "Fondo"
							INNER JOIN :v_series_fondos AS "SerieFondo" ON ("Fondo"."IdInterno" = "SerieFondo"."IdInterno")
							UNION ALL
							SELECT 	
									"Fondo"."IdInterno"					
									,"Fondo"."IdFondo"					
									,"Fondo"."IdEmisor"					
									,"Fondo"."Nemotecnico"				
									,"Fondo"."NombreFondo"				
									,"Fondo"."IdTipoFondo"				
									,"Fondo"."IdPaisOrigen"				
									,"Fondo"."CodigoSVS"								
									,"Fondo"."MoneyMarket"   			  
									,"Fondo"."FechaCreacion"				
									,"Fondo"."FechaIngresoRegistro"		
									,"Fondo"."FechaConstitucion" 		
									,"Fondo"."Vigencia"					
									,"Fondo"."FondoExtranjero"
									,"Fondo"."IdMoneda"
									,"Fondo"."IdTipoInstrumento"
									,"SerieFondo"."IdFamiliaFondo"
									,"SerieFondo"."IdSerieFondo"				
									,"SerieFondo"."IdInstrumento"				
									,"SerieFondo"."NombreSerieFondo"			
									,"SerieFondo"."CodigoSerie"							
									,"SerieFondo"."Alias"	
									,"SerieFondo"."DecimalesValorCuota"	
									,"SerieFondo"."DecimalesCuota"				
									,"SerieFondo"."MontoMinimo"				
									,"SerieFondo"."MontoMaximo"
									,"SerieFondo"."DiasRetencionesAporte"
									,"SerieFondo"."DiasRetencionesRescate"	
									,"SerieFondo"."ExcluyeGarantia"
									,"SerieFondo"."CobraComision"
									,"SerieFondo"."Diferido"
									,"SerieFondo"."InstrumentoDCV"
									,"SerieFondo"."Omnibus"				
									,"SerieFondo"."PatrimonioMinimo"			
									,"SerieFondo"."IdSerieFondoOrigen"		
									,"SerieFondo"."IdEstado"
									,"SerieFondo"."FechaIngresoRegistro" AS "FechaIngresoRegistroSerie"	
									,"SerieFondo"."IdCliente"
									,"SerieFondo"."IdPortafolio"
									,"SerieFondo"."IdMedioDePagoAbono"
									,"SerieFondo"."IdMedioDePagoCargo"
									,"SerieFondo"."DiasValutaValorCuotaAporte"
                                    ,"SerieFondo"."DiasValutaValorCuotaRescate"
									,"SerieFondo"."DiasValutaCertificacionAporte"
									,"SerieFondo"."DiasValutaCertificacionRescate"
									,"SerieFondo"."DiasValutaCurseAporte"
									,"SerieFondo"."DiasValutaCurseRescate"
									,"SerieFondo"."CambioFondo"
									,"SerieFondo"."DiasComision"
									,"SerieFondo"."PorcentajeComision"
									,"SerieFondo"."PorcentajeRemuneracion"
									,"SerieFondo"."PorcentajeGastoOpera"
									,"SerieFondo"."PorcentajeCostoAdm"									
									,"Fondo"."AperturaAutomaticaFondo"
									,"Fondo"."FondoNormativo"					
									,"Fondo"."IdEmpresa"	
									,"Fondo"."IdCategoriaPerfilFondoMutuo"
                                    ,"Fondo"."IdPaisEmisor"
                                    ,"Fondo"."IdTipoDocumentoIdentidadFondo"
                                    ,"Fondo"."DocumentoIdentidadFondo"
                                    ,"Fondo"."IdMercadoFondoMutuo"
							FROM :v_fondos_actualizados AS "Fondo"
							INNER JOIN :v_series_fondos AS "SerieFondo" ON ("Fondo"."IdInterno" = "SerieFondo"."IdInterno");
			
			--agregamos valor cuota
			v_valor_cuota = SELECT "Resultado"."IdSerieFondo" as "IDSERIEFONDO", 
				"Resultado"."IdMoneda" as "IDMONEDA", "P_Serie"."FechaValorCuota" as "FECHACIERRE",
				"P_Serie"."ValorCuota" as "VALORCUOTA", '' as "OBSERVACION", 'OK' as "MENSAJEVALIDACION"
				FROM :p_out_Resultado "Resultado"
				INNER JOIN :p_SerieFondo AS "P_Serie" ON ("P_Serie"."IdInterno" = "Resultado"."IdInterno");
	
	        CALL "BSCL.FondoMutuo.BD.Servicios::CargarValorCuota"(:p_IdEmpresa, :p_IdUsuario, :p_IdModulo, :p_UsaReglasNegocio, 'N',
		        :v_valor_cuota, 
		        v_out_VCResultado, 
		        v_out_VCAdvertenciasContador, 
		        v_out_VCAdvertenciasDetalle, 
		        v_out_VCErroresContador, 
		        v_out_VCErroresDetalle);
		        
		    --adjunta mensajes
			v_out_AdvertenciasDetalle =  select 0 as "IdInterno","Codigo","RequiereAutorizacion",'' as "ValorParametro", '' as "NombreParametro" 
										from :v_out_VCAdvertenciasDetalle UNION ALL
									    SELECT 0 as "IdInterno","Codigo","RequiereAutorizacion",'' as "ValorParametro", '' as "NombreParametro" FROM :p_out_AdvertenciasDetalle;			    
			v_out_ErroresDetalle      =  select 0 as "IdInterno", "Codigo", '' as "ValorParametro", '' as "NombreParametro" from :v_out_VCErroresDetalle UNION ALL
									     select 0 as "IdInterno", "Codigo", '' as "ValorParametro", '' as "NombreParametro" from :p_out_ErroresDetalle;
			--SIGNAL SALIDA_CONTROLADA;
			--No se pudo agregar valor cuota
			if 	:v_out_VCErroresContador > 0 then
				SIGNAL SALIDA_CONTROLADA;
			end if;								
			--Mensaje de Salida
			if 	:v_out_VCAdvertenciasContador > 0 then
				v_hash= select '' as "Codigo",'' as "Valor" FROM DUMMY;
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_OPERACION_INGRESO_ADVERTENCIA',:v_hash,:v_erroroutmsg);
				v_out_AdvertenciasDetalle =  select 0 as "IdInterno", 'UI_OPERACION_INGRESO_ADVERTENCIA' as "Codigo",'S' as "RequiereAutorizacion",:v_erroroutmsg as "ValorParametro", '' as "NombreParametro" from dummy UNION ALL
										     SELECT 0 as "IdInterno","Codigo","RequiereAutorizacion",'' as "ValorParametro", '' as "NombreParametro"
			                                    FROM :p_out_AdvertenciasDetalle;
			end if;
	        --fin agregar valor cuota
							
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
			
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
					,'INSTRUMENTO_FONDO_MUTUO' AS "IdTipoRegistro"
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
					,"IdSerieFondo" AS "Nemo"
					,0 AS "IdProceso"
					,'' AS "EstadoProceso"
					,'' AS "NombreProceso"
				FROM :v_series_fondos;
			
			--------------------------------------------------------------------
			--Detalle Log
			---------------------------------------------------------------------
			BEGIN
				DECLARE v_DETALLELOG 						TABLE ("IdCabeceraLog" BIGINT,"SubRegistro" NVARCHAR(40),"CampoRegistro" NVARCHAR(500),"ValorAntiguo" NVARCHAR(250),"ValorNuevo" NVARCHAR(250));

				SELECT count(1) INTO v_count_fondos_actualizar_real FROM :v_fondos_actualizar_real WHERE "Modificado" IS NOT NULL;
				SELECT count(1) INTO v_count_serie_fondo_modificado_real FROM :v_serie_fondo_actualizados WHERE "Modificado" IS NOT NULL;
				SELECT count(1) INTO v_count_regimen_actualizado_real FROM :v_regimenes_modificados WHERE "Modificado" IS NOT NULL;

				v_DETALLELOG = (SELECT 0 AS "IdCabeceraLog", 'INIT' AS "SubRegistro", '' AS "CampoRegistro", '' AS "ValorAntiguo", '' AS "ValorNuevo"  FROM dummy);
				--Detalle Log instrumento nuevo
				IF(:v_count_nuevo_instrumento > 0) THEN 	

					v_DETALLELOG = 
						(SELECT * FROM :v_DETALLELOG
						
						UNION ALL
						
						SELECT 0 AS "IdCabeceraLog", 'Crear Instrumento' as "SubRegistro", "Campo" as "CampoRegistro", '' as "ValorAntiguo", "ValorActual" as "ValorNuevo" 
						FROM :v_instrumento_nuevo ORDER BY "CampoRegistro" ASC); 
							
				END IF;

				--Detalle Log Fondo Nuevo
				IF (:v_count_fondos_nuevos > 0) THEN

					v_DETALLELOG = 
						(SELECT * FROM :v_DETALLELOG
						
						UNION ALL 
						
						SELECT 0 AS "IdCabeceraLog", 'Crear Fondo' as "SubRegistro", "Campo" as "CampoRegistro", '' as "ValorAntiguo", "ValorActual" as "ValorNuevo" 
						FROM :v_fondos_nuevos_real ORDER BY "CampoRegistro" ASC);

				END IF;

				--Log Fondo Modificado
				IF (:v_count_fondos_actualizar_real > 0) THEN 

					v_DETALLELOG = 
					(SELECT * FROM :v_DETALLELOG
					
					UNION ALL 
					
					SELECT 0 AS "IdCabeceraLog", 'Actualizar Fondo' as "SubRegistro", "Campo" as "CampoRegistro", "ValorAnterior" as "ValorAntiguo", "ValorActual" as "ValorNuevo" 
					FROM :v_fondos_actualizar_real WHERE "Modificado" IS NOT NULL ORDER BY "CampoRegistro" ASC);

				END IF;

				--Log Serie Fondo Modificado     
				IF (:v_count_serie_fondo_modificado_real > 0) THEN 

					v_DETALLELOG = 
					(SELECT * FROM :v_DETALLELOG
					
					UNION ALL 
					
					SELECT 0 AS "IdCabeceraLog", 'Actualizar Serie Fondo' as "SubRegistro", "Campo" as "CampoRegistro", "ValorAnterior" as "ValorAntiguo", "ValorActual" as "ValorNuevo" 
					FROM :v_serie_fondo_actualizados WHERE "Modificado" IS NOT NULL ORDER BY "CampoRegistro" ASC);

				END IF;

				--Log Regimen Nuevo
				IF (:v_count_regimen_nuevo > 0) THEN 

					v_DETALLELOG = 
					(SELECT * FROM :v_DETALLELOG
					
					UNION ALL 
					
					SELECT 0 AS "IdCabeceraLog", CONCAT('Crear regimen ',"IdRegimenFondoMutuo") as "SubRegistro", "Campo" as "CampoRegistro", '' as "ValorAntiguo", "ValorActual" as "ValorNuevo" 
					FROM :v_regimenes_nuevos_log ORDER BY "CampoRegistro" ASC);

				END IF;

				--Log Regimen Actualizado
				IF (:v_count_regimen_actualizado_real > 0) THEN 

					v_DETALLELOG = 
					(SELECT * FROM :v_DETALLELOG
					
					UNION ALL 
					
					SELECT 0 AS "IdCabeceraLog", CONCAT('Actualizar regimen ',"IdRegimenFondoMutuo") as "SubRegistro", "Campo" as "CampoRegistro", "ValorAnterior" as "ValorAntiguo", "ValorActual" as "ValorNuevo" 
					FROM :v_regimenes_modificados WHERE "Modificado" IS NOT NULL ORDER BY "CampoRegistro" ASC);

				END IF;

				p_LogDetalle =(SELECT * FROM  :v_DETALLELOG WHERE "SubRegistro" <> 'INIT');
			
				CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle =>:p_LogDetalle);
				
				CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'MODIFICAR INSTRUMENTO FONDO MUTUO MM','FIN','FIN');
			END;
			------------------------------------------
			--Genera Evento de Autorizacion
			------------------------------------------
			if (v_RequiereAutorizacion='S') then
			
				SELECT "Resultado"."IdInstrumento", "Resultado"."Nemotecnico", "Serie"."IdCliente", "Resultado"."IdMoneda",
				"Resultado"."IdEmisor" || ' / Nemotecnico: ' || "Resultado"."Nemotecnico" || ''
				INTO v_IdInstrumento,v_Glosa,v_IdCliente,v_IdMoneda,v_Glosa
	          	FROM  :v_series_antes_instrumento AS "Serie"
	          	LEFT OUTER JOIN :p_out_Resultado AS "Resultado" ON ("Serie"."IdInterno" = "Resultado"."IdInterno");
				
	          	--cambiamos el estado del instrumento a PDA
				UPDATE inst
				SET inst."IdEstado" = 'PDA'
				FROM "BSCL.Plataforma.BD::INS.Instrumento" inst
				INNER JOIN :p_out_Resultado res ON inst."IdInstrumento" = res."IdInstrumento"
				INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" sf ON sf."IdInstrumento" = inst."IdInstrumento"
				WHERE inst."IdInstrumento" = v_IdInstrumento;
	          	
				CALL "BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacion" (
					p_IdEmpresa => p_IdEmpresa,
					p_IdUsuario => p_IdUsuario,
					p_IdModulo => p_IdModulo,
					p_UsaReglasNegocio => 'N',								
					p_IdTipoEvento => 'INGFFM',
					p_IdModuloSolicita => p_IdModulo,
					p_IdUsuarioSolicita => p_IdUsuario,
					p_Fecha => TO_VARCHAR (v_FechaSistema, 'YYYY-MM-DD'), 
					p_IdConcepto => v_IdConcepto,
					p_NumeroOperacionConcepto =>  CAST (v_IdInstrumento AS NVARCHAR(40)),
					p_FechaOperacionConcepto => TO_VARCHAR (CURRENT_UTCTIMESTAMP, 'YYYY-MM-DD'),
					p_IdCliente => v_IdCliente,
					p_IdMoneda => v_IdMoneda,
					p_Glosa => v_Glosa,
					--salida
					p_out_IdEvento => v_out_idAuth,
					p_out_AdvertenciasContador => v_out_AuthAdvertenciasContador,
					p_out_AdvertenciasDetalle =>  v_out_AuthAdvertenciasDetalle,
					p_out_ErroresContador =>  v_out_AuthErroresContador,
					p_out_ErroresDetalle =>  v_out_AuthErroresDetalle);			
				--adjunta mensajes
				v_out_AdvertenciasDetalle =  select 0 as "IdInterno","Codigo","RequiereAutorizacion",'' as "ValorParametro", '' as "NombreParametro" 
											from :v_out_AuthAdvertenciasDetalle UNION ALL
										    SELECT 0 as "IdInterno",* FROM :p_out_AdvertenciasDetalle;			    
				v_out_ErroresDetalle      =  select 0 as "IdInterno", "Codigo", '' as "ValorParametro", '' as "NombreParametro" from :v_out_AuthErroresDetalle UNION ALL
										     select 0 as "IdInterno", "Codigo", '' as "ValorParametro", '' as "NombreParametro" from :p_out_ErroresDetalle;
				--SIGNAL SALIDA_CONTROLADA;
				--No se pudo crear aut
				if 	:v_out_AuthErroresContador > 0 then
					SIGNAL SALIDA_CONTROLADA;
				end if;								
				--Mensaje de Salida
				v_hash= select '' as "Codigo",'' as "Valor" FROM DUMMY;
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('REQUIERE_AUTORIZACION_INGRESAR_FFMM',:v_hash,:v_erroroutmsg);
				v_out_AdvertenciasDetalle =  select 0 as "IdInterno", 'REQUIERE_AUTORIZACION_INGRESAR_FFMM' as "Codigo",'S' as "RequiereAutorizacion",:v_erroroutmsg as "ValorParametro", '' as "NombreParametro" from dummy UNION ALL
										     SELECT 0 as "IdInterno","Codigo","RequiereAutorizacion",'' as "ValorParametro", '' as "NombreParametro"
			                                    FROM :p_out_AdvertenciasDetalle;
			end if;
			
			SIGNAL SALIDA_CONTROLADA;
		
		END;
	END;
END;

