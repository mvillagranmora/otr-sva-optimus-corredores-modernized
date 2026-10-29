SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::CrearInstrumentoFondoMutuo";

CREATE PROCEDURE
--  Proposito: Crear Instrumento de Fondos Mutuos (Fondo + Serie)
--  Autor: - Gat-Blac 
--  Fecha Creacion: 28/06/2016
"BSCL.FondoMutuo.BD.Servicios::CrearInstrumentoFondoMutuo"(
    --VARIABLES OBLIGATORIAS
    IN p_IdEmpresa                      BIGINT,
    IN p_IdUsuario                      BIGINT,
    IN p_IdModulo                       NVARCHAR(3),
    IN p_UsaReglasNegocio               NVARCHAR(1),
    
    IN p_Fondo                          TABLE   (
                                                "IdInterno"                   BIGINT,
                                                "IdFondo"                     NVARCHAR(40),
                                                "IdEmisor"                    NVARCHAR(40),
                                                "Nemotecnico"                 NVARCHAR(40),
                                                "NombreFondo"                 NVARCHAR(100),
                                                "IdTipoFondo"                 NVARCHAR(40),
                                                "IdPaisOrigen"                BIGINT,
                                                "CodigoSVS"                   NVARCHAR(40),
                                                "MoneyMarket"                 NVARCHAR(1),
                                                "Vigencia"                    NVARCHAR(1),
                                                "FondoExtranjero"             NVARCHAR(1),
                                                "IdMoneda"                    NVARCHAR(3),
                                                "IdTipoInstrumento"           NVARCHAR(3),
                                                "IdCategoriaPerfilFondoMutuo" NVARCHAR(40),
                                                "AperturaAutomaticaFondo"     NVARCHAR(1),
                                                "FondoNormativo"              NVARCHAR(1),
                                                "IdPaisEmisor"                BIGINT,
                                                "IdTipoDocumentoIdentidadFondo" NVARCHAR(3),
                                                "DocumentoIdentidadFondo"     NVARCHAR(40),
                                                "IdMercadoFondoMutuo"		  NVARCHAR(40)
                                                ),
    IN p_SerieFondo                     TABLE   (
                                                "IdInterno"                          BIGINT,
                                                "IdSerieFondo"                       NVARCHAR(40),
                                                "IdFondo"                            NVARCHAR(40),
                                                "NombreSerieFondo"                   NVARCHAR(100),
                                                "CodigoSerie"                        NVARCHAR(10),
                                                "Alias"                              NVARCHAR(40),
                                                "DecimalesValorCuota"                BIGINT     ,
                                                "DecimalesCuota"                     BIGINT     ,
                                                "MontoMinimo"                        DECIMAL(28,6),
                                                "MontoMaximo"                        DECIMAL(28,6),
                                                "DiasRetencionesAporte"              BIGINT,
                                                "DiasRetencionesRescate"             BIGINT,
                                                "ExcluyeGarantia"                    NVARCHAR(1),
                                                "CobraComision"                      NVARCHAR(1),
                                                "Diferido"                           NVARCHAR(1),
                                                "InstrumentoDCV"                     NVARCHAR(40),
                                                "Omnibus"                            NVARCHAR(1),
                                                "PatrimonioMinimo"                   DECIMAL(28,6),
                                                "IdSerieFondoOrigen"                 NVARCHAR(40),
                                                "IdEstado"                           NVARCHAR(3),
                                                "IdCliente"                          BIGINT,
                                                "IdPortafolio"                       BIGINT,
                                                "IdMedioDePagoAbono"                 NVARCHAR(3),
                                                "IdMedioDePagoCargo"                 NVARCHAR(3),
                                                "DiasValutaValorCuotaAporte"         BIGINT,
                                                "DiasValutaValorCuotaRescate"        BIGINT,
                                                "DiasValutaCertificacionAporte"      BIGINT,
                                                "DiasValutaCertificacionRescate"     BIGINT,
                                                "DiasValutaCurseAporte"              BIGINT,
                                                "DiasValutaCurseRescate"             BIGINT,
                                                "IdFamiliaFondo"                     NVARCHAR(40),
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
    IN p_RegimenSerieFondo              TABLE (
                                                "IdInterno" BIGINT,
                                                "IdRegimenFondoMutuo"   NVARCHAR(40),
                                                "IdSerieFondo"          NVARCHAR(40)
                                        ),
    OUT p_out_Resultado                 TABLE   (
                                                "IdInterno"                          BIGINT,
                                                "IdFondo"                            NVARCHAR(40),
                                                "IdEmisor"                           NVARCHAR(40),
                                                "Nemotecnico"                        NVARCHAR(40),
                                                "NombreFondo"                        NVARCHAR(100),
                                                "IdTipoFondo"                        NVARCHAR(40),
                                                "IdPaisOrigen"                       BIGINT,
                                                "CodigoSVS"                          NVARCHAR(40),
                                                "MoneyMarket"                        NVARCHAR(1),
                                                "FechaCreacion"                      DATE,
                                                "FechaIngresoRegistro"               TIMESTAMP,
                                                "FechaConstitucion"                  DATE,
                                                "Vigencia"                           NVARCHAR(1),
                                                "FondoExtranjero"                    NVARCHAR(1),
                                                "IdMoneda"                           NVARCHAR(3),
                                                "IdTipoInstrumento"                  NVARCHAR(3),
                                                "IdFamiliaFondo"                     NVARCHAR(40),              
                                                "IdSerieFondo"                       NVARCHAR(40),
                                                "IdInstrumento"                      BIGINT,
                                                "NombreSerieFondo"                   NVARCHAR(100),
                                                "CodigoSerie"                        NVARCHAR(10),
                                                "Alias"                              NVARCHAR(40),
                                                "DecimalesValorCuota"                BIGINT,
                                                "DecimalesCuota"                     BIGINT,
                                                "MontoMinimo"                        DECIMAL(28,6),
                                                "MontoMaximo"                        DECIMAL(28,6),
                                                "DiasRetencionesAporte"              BIGINT,
                                                "DiasRetencionesRescate"             BIGINT,
                                                "ExcluyeGarantia"                    NVARCHAR(1),
                                                "CobraComision"                      NVARCHAR(1),
                                                "Diferido"                           NVARCHAR(1),
                                                "InstrumentoDCV"                     NVARCHAR(40),
                                                "Omnibus"                            NVARCHAR(1),
                                                "PatrimonioMinimo"                   DECIMAL(28,6),
                                                "IdSerieFondoOrigen"                 NVARCHAR(40),
                                                "IdEstado"                           NVARCHAR(3),
                                                "FechaIngresoRegistroSerie"          TIMESTAMP,
                                                "IdCliente"                          BIGINT,
                                                "IdPortafolio"                       BIGINT,
                                                "IdMedioDePagoAbono"                 NVARCHAR(3),
                                                "IdMedioDePagoCargo"                 NVARCHAR(3),
                                                "DiasValutaValorCuotaAporte"         BIGINT,
                                                "DiasValutaValorCuotaRescate"        BIGINT,
                                                "DiasValutaCertificacionAporte"      BIGINT,
                                                "DiasValutaCertificacionRescate"     BIGINT,
                                                "DiasValutaCurseAporte"              BIGINT,
                                                "DiasValutaCurseRescate"             BIGINT,
                                                "CambioFondo"                        NVARCHAR(1),
				    					  		"DiasComision"      				 BIGINT,
										  		"PorcentajeComision"                 DECIMAL(26,8),
										  		"PorcentajeRemuneracion"             DECIMAL(26,8),
										  		"PorcentajeGastoOpera"          	 DECIMAL(26,8),
										  		"PorcentajeCostoAdm"          		 DECIMAL(26,8),
                                                "AperturaAutomaticaFondo"            NVARCHAR(1),
                                                "FondoNormativo"                     NVARCHAR(1),
                                                "IdEmpresa"                          BIGINT,
                                                "IdCategoriaPerfilFondoMutuo"        NVARCHAR(40),
                                                "IdPaisEmisor"                       BIGINT,
                                                "IdTipoDocumentoIdentidadFondo"      NVARCHAR(3),
                                                "DocumentoIdentidadFondo"            NVARCHAR(40),
                                                "IdMercadoFondoMutuo"		  		 NVARCHAR(40)
                                                
                                                
                                            ),
    OUT p_out_AdvertenciasContador      INT,
    OUT p_out_AdvertenciasDetalle       TABLE ("IdInterno"  BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),
    OUT p_out_ErroresContador           INT,
    OUT p_out_ErroresDetalle            TABLE ("IdInterno"  BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
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
    DECLARE v_out_AdvertenciasDetalle           TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
    DECLARE v_out_ErroresDetalle                TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
    BEGIN
        v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
        v_out_ErroresDetalle      = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
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
            
            p_out_ErroresDetalle = SELECT   "errores"."IdInterno",
                                            "errores"."Codigo",
                                            "msj"."Descripcion"
                                    FROM :v_out_ErroresDetalle AS "errores"
                                    INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "errores"."Codigo" = "msj"."Codigo";
            
            p_out_AdvertenciasDetalle = SELECT  "adv"."IdInterno",
                                                "adv"."Codigo",
                                                "adv"."RequiereAutorizacion",
                                                "msj"."Descripcion"
                                        FROM :v_out_AdvertenciasDetalle AS "adv"
                                        INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "adv"."Codigo" = "msj"."Codigo"; 
        END;
        
        p_out_AdvertenciasContador  := 0;
        p_out_ErroresContador       := 0;
        
        BEGIN
        DECLARE v_count_fondos                  BIGINT;
        DECLARE v_count_series                  BIGINT;
        DECLARE v_resultadoRegla                NVARCHAR(40);
        DECLARE v_count                         BIGINT;
        DECLARE v_count_series_validacion       BIGINT;
        DECLARE v_count_advertencia_ins         BIGINT;
        DECLARE v_count_errores_ins             BIGINT;
        DECLARE v_count_series_validacion_ins   BIGINT;
        DECLARE v_count_series_regimen          BIGINT;
        DECLARE v_count_regimenes               BIGINT;
        DECLARE v_valorCuota					DECIMAL(26,8);
        DECLARE v_IdCliente						BIGINT;
        DECLARE v_IdMoneda						NVARCHAR(3);
        DECLARE v_IdInstrumento					BIGINT;
        DECLARE v_IdConcepto					NVARCHAR(6);
        DECLARE v_RequiereAutorizacion			NVARCHAR(1);
        DECLARE v_RegistroSolicitudCambio 		"BSCL.Plataforma.BD::PLA.SolicitudCambio";
        DECLARE v_validar_ingreso				NVARCHAR(1) :='N';
        declare v_Glosa 			nvarchar(520) :='';
		declare v_out_idAuth 		bigint;
		declare v_out_AuthAdvertenciasContador 	int;
		declare v_out_AuthErroresContador 		int;
		declare v_out_VCAdvertenciasContador 	int;
		declare v_out_VCErroresContador 		int;
		v_out_AuthAdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
		v_out_AuthErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
        v_out_VCAdvertenciasDetalle		= select '' as "IdInterno", '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
		v_out_VCthErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
		
        CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CREAR INSTRUMENTO FONDO MUTUO MM','INICIO','INICIO');
                                    
        CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
        
        if :v_resultadoRegla = 'RECH' then
            v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
            SIGNAL SALIDA_CONTROLADA;   
        end if;
        
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
            
        
        
        v_series_regimen_validados  = SELECT "P_Serie"."IdInterno"              
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
        
        IF  v_count_series_regimen = 0 THEN
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
                                ,CASE WHEN COALESCE("Serie"."IdSerieFondo",'-1') = '-1' THEN 'OK' ELSE 'ERR' END AS "Resultado"
                                ,CASE WHEN COALESCE("Emisor"."IdEmisor",'-1') != COALESCE("P_Fondo"."IdEmisor",'-1') THEN 'ADV' else 'OK' END AS "ResultadoEmisor"
                                ,CASE WHEN COALESCE("TipoFondo"."IndicadorMoneyMarket",'N') != 'S' AND "P_Fondo"."MoneyMarket" = 'S' THEN 'ERR' ELSE 'OK' END AS "ResultadoMoneyMarket"
                            FROM :v_series_regimen_validados AS "P_Serie" 
                            INNER JOIN :p_fondo AS "P_Fondo" ON ("P_Fondo"."IdInterno" = "P_Serie"."IdInterno")  
                            LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "Serie" ON ("Serie"."IdSerieFondo" = "P_Serie"."IdSerieFondo" AND "P_Serie"."IdEmpresa" = "Serie"."IdEmpresa")
                            LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "Cliente" ON ("P_Serie"."IdCliente" = "Cliente"."IdCliente" AND "P_Serie"."IdEmpresa" = "Cliente"."IdEmpresa")
                            LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS "Emisor" ON ("Emisor"."DocumentoIdentidad" = "Cliente"."DocumentoIdentidad" AND "Emisor"."IdEmisor" = "P_Fondo"."IdEmisor")
                            LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.TipoFondo" AS "TipoFondo" ON ("TipoFondo"."IdTipoFondo" = "P_Fondo"."IdTipoFondo");
        
        BEGIN
            v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'SERIE_EXISTE' as "Codigo", "Series"."IdSerieFondo" as "ValorParametro", 'IdSerieFondo' AS "NombreParametro"
                                   FROM :v_series_validacion AS "Series" WHERE "Resultado" = 'ERR'
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

        
        v_series_antes_instrumento = SELECT "P_Serie"."IdInterno"               
                                            ,"P_Serie"."IdSerieFondo"                           
                                            ,"P_Serie"."IdFondo"
                                            ,COALESCE("Instrumento"."IdInstrumento",0) AS "IdInstrumento"               
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
                                    FROM  :v_series_validacion AS "P_Serie"
                                    LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS "Instrumento" ON ("Instrumento"."Nemotecnico" = "P_Serie"."IdSerieFondo"  AND "Instrumento"."IdClasificacionTipoInstrumento" = 'FM')
                                    WHERE "P_Serie"."Resultado" = 'OK';
        
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
                                ,0 AS "Tamano"
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
								,'S' AS "EsAutorizado"
                                ,'N' AS "AfectoArt107"
                                ,'N' AS "AfectoArt104"
                                ,'N' AS "DividendoOtraMoneda"
                                ,'S' AS "CobroComisionesCustodia"
                                ,0  AS "IdInstrumentoRaiz"
								,'' AS "IdCondicionLiquidacion"                                
                                ,'N' AS "EsInstrumentoCalificado"
                          FROM :v_series_antes_instrumento AS "Serie"
                          INNER JOIN :p_Fondo AS "Fondo" ON ("Fondo"."IdInterno" = "Serie"."IdInterno")
                          WHERE "Serie"."IdInstrumento" = 0;
        
        v_valores_riesgo = SELECT 0 AS "IdInterno", '' AS "IdEntidadClasificadoraRiesgo", '' AS "ValorClasificacionRiesgo" FROM DUMMY;
        v_bolsas = SELECT 0 AS "IdInterno",'' AS "IdBolsa" FROM DUMMY WHERE 1 = 2 ;
        
        CALL "BSCL.Plataforma.BD.Servicios::INS.CrearInstrumentos"(
            p_IdEmpresa => :p_IdEmpresa,
            p_IdUsuario => :p_IdUsuario,
            p_IdModulo => :p_IdModulo,
            p_simularOperacion => 'N',
            p_UsaReglasNegocio => 'N',
            p_Instrumentos => :v_instrumentos,
            p_ValoresRiesgo => :v_valores_riesgo,
            p_Bolsas => :v_Bolsas,
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
            v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'ERROR_INSTRUMENTO' as "Codigo", '' as "ValorParametro", 'IdInstrumento' AS "NombreParametro"
                                   FROM :v_series_validacion_instrumento AS "Series" WHERE "ResultadoInstrumento" = 'ERR'
                                   UNION ALL
                                   SELECT * FROM :v_out_ErroresDetalle;
            SELECT COUNT(1) as "Count" INTO v_count_series_validacion_ins
            FROM :v_series_validacion_instrumento WHERE "ResultadoInstrumento" = 'OK';
            IF (v_count_series_validacion_ins = 0) THEN
                SIGNAL SALIDA_CONTROLADA;
            END IF;
        END;
        
        v_series_final =    SELECT  "Serie"."IdInterno"                 
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
                                    ,"Serie"."IdEstado"
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
        
        v_regimen_final = SELECT "Regimen"."IdInterno"
                                 ,"Regimen"."IdRegimenFondoMutuo"
                                 ,"Regimen"."IdSerieFondo"
                          FROM :p_RegimenSerieFondo AS "Regimen"
                          INNER JOIN :v_series_final AS "Series" ON ("Regimen"."IdInterno" = "Series"."IdInterno" AND "Regimen"."IdSerieFondo" = "Series"."IdSerieFondo");
            
        CALL "BSCL.FondoMutuo.BD.Servicios::CrearFondo"(:p_IdEmpresa, :v_fondos_nuevos_maestro, v_fondos_nuevos);               
                
        CALL "BSCL.FondoMutuo.BD.Servicios::ModificarFondo"(:p_IdEmpresa, :v_fondos_actualizar_maestro, v_fondos_actualizados);
                
        CALL "BSCL.FondoMutuo.BD.Servicios::CrearSerieDeFondo"(:p_IdEmpresa, :v_series_final, v_series_fondos);
        
        CALL "BSCL.FondoMutuo.BD.Servicios::CrearRegimenSerieDeFondo"(:p_IdEmpresa, :v_regimen_final, v_regimenes_fondos);
                
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
            
            SELECT "ValorCuota" INTO v_valorCuota FROM :p_SerieFondo;
        	
        	IF(v_valorCuota > 0) THEN
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
										    SELECT 0 as "IdInterno",* FROM :p_out_AdvertenciasDetalle;			    
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
        	END IF;
                            
            CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
            
            v_log = SELECT 
                    "IdInterno" AS "IdCabeceraLog"
                    ,'INGRESO' AS "IdTipoLog"
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
            
            v_DETALLELOG = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
        
            CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log, p_LogDetalle => :v_DETALLELOG);
            
            CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CREAR INSTRUMENTO FONDO MUTUO MM','FIN','FIN');
            
        ------------------------------------------
		--Genera Evento de Autorizacion
		------------------------------------------
		if (v_RequiereAutorizacion='S') then
		
			SELECT "Instrumento"."IdInstrumento", "Instrumento"."Nemotecnico", "Serie"."IdCliente", "Resultado"."IdMoneda",
			"Resultado"."IdEmisor" || ' / Nemotecnico: ' || "Instrumento"."Nemotecnico" || ''
			INTO v_IdInstrumento,v_Glosa,v_IdCliente,v_IdMoneda,v_Glosa
          	FROM  :v_series_antes_instrumento AS "Serie"
          	LEFT OUTER JOIN :v_instrumentos_insertados AS "Instrumento" ON ("Serie"."IdInterno" = "Instrumento"."IdInterno")
          	LEFT OUTER JOIN :p_out_Resultado AS "Resultado" ON ("Serie"."IdInterno" = "Resultado"."IdInterno");
			
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
			SIGNAL SALIDA_CONTROLADA;
		end if;
            
            SIGNAL SALIDA_CONTROLADA;
        END;
        END;
END;

