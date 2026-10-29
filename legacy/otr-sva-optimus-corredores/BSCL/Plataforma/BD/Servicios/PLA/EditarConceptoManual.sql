----------------------------------------------------------------
--  Fecha Creacion: 22/03/2017
--  Proposito: Crear los conceptos definidos por el cliente
--  Autor: NRM - BCS
----------------------------------------------------------------
--  Fecha Modificacion: 15/05/2019
--  Autor: RAM - BCS
--  Proposito: Se agrega campo "AfectaRentabilidad" a la tabla "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"
----------------------------------------------------------------
SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::PLA.EditarConceptoManual";
GO
CREATE PROCEDURE  "BSCL.Plataforma.BD.Servicios::PLA.EditarConceptoManual" (
	IN  p_IdEmpresa                 	BIGINT,
    IN  p_IdUsuario                 	BIGINT,
    IN  p_IdModulo                 	 	NVARCHAR(3),    
	IN  p_UsaReglasNegocio	 			NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
	IN  p_Conceptos						TABLE(	"IdConcepto"						NVARCHAR(6), 
							  					"Nombre"							NVARCHAR(100),
							  					"VisiblePorEjecutivo"				NVARCHAR(1),
							  					"IdEstado"							NVARCHAR(3),
							  					"IdTipoCriterioNeteo"				BIGINT,
							  					"NombreConceptoCartola"             NVARCHAR(100),
							  					"ExclusivoCartola"				    NVARCHAR(1),
							  					"ConceptoAbonoReplica"				NVARCHAR(2),
												"ConceptoCargoReplica"				NVARCHAR(2)
							  				 ),
	-- OPTIMUSPE-324 Implementación Requerimientos de Desarrollo Contabilidad Perú										 	 
	IN	p_configuracionFacturacion		TABLE(	"IdConcepto"						NVARCHAR(6),
												"CentroCosto"						NVARCHAR(10),
												"CodigoProducto"					NVARCHAR(4),
												"EsOtrosConceptos"					NVARCHAR(1),												
												"EsReembolso"						NVARCHAR(1),
												"EsInterfazSIG"						NVARCHAR(1),
												"Contrapartida"						NVARCHAR(1),
												"AbonoClpContrapartida"				NVARCHAR(1),
												"ConceptoConfiguradoEnModulo"		NVARCHAR(1),
												"ConceptoConfiguradoOptimus"		NVARCHAR(1),
												"ConceptoConfiguradoFiori"	   	    NVARCHAR(1),
												"ConceptoConfiguradoWs"	   	        NVARCHAR(1)
										 	 ),	
	IN	p_configuracionCustodia			TABLE(	"IdConcepto"						NVARCHAR(6),
												"IdConceptoCustodia"				BIGINT,
												"USO"								NVARCHAR(40),
											   	"CertificadoAutomatico"				NVARCHAR(1),											   	
											   	"GeneraTransferenciaBoveda"			NVARCHAR(1),
											   	"EsTransferenciaPortafolio"			NVARCHAR(1),
											   	"EsTransferenciaCliente"			NVARCHAR(1),
											   	"EsGarantiaPrendaria"				NVARCHAR(1),
											   	"EsGarantiaVendible"				NVARCHAR(1),											   	
											   	"RetieneDividendos"					NVARCHAR(1),											   	
											   	"RetieneAmortizacion"				NVARCHAR(1),
											   	"RetieneIntereses"					NVARCHAR(1),
											   	"FolioObligatorio"					NVARCHAR(1),											   	
											   	"IdClasificacionTipoInstrumento"	NVARCHAR(2),
											   	"IdTipoInstrumento"					NVARCHAR(3),
											   	"IdInstrumento"						BIGINT,
											   	"IdContraparte"						BIGINT,
											   	"IdTipoCuenta"						NVARCHAR(6),											   	
											   	"ConceptoConfiguradoEnModulo"		NVARCHAR(1),
											   	"ConceptoConfiguradoOptimus"        NVARCHAR(1),
												"ConceptoConfiguradoFiori"	   	    NVARCHAR(1),
												"ConceptoConfiguradoWs"	   	        NVARCHAR(1)
												
										 	 ),
	IN	p_configuracionTesoreria		TABLE(	"IdConcepto"						NVARCHAR(6),
												"USO"								NVARCHAR(40),
												"DisponibleIngresoManual"			NVARCHAR(1),												
												"ConceptoConfiguradoEnModulo"		NVARCHAR(1),
												"ConceptoConfiguradoOptimus"		NVARCHAR(1),
												"ConceptoConfiguradoFiori"	   	    NVARCHAR(1),
												"ConceptoConfiguradoWs"	   	        NVARCHAR(1),
												"NoValidaSaldoCaja"					NVARCHAR(1),
												"GeneraComprobanteAutomaticamente"	NVARCHAR(1)
										 	 ),
	IN	p_configuracionCuentaInversion	TABLE(	"IdConcepto"						NVARCHAR(6),
												"DisponibleIngresoManual"			NVARCHAR(1),
												"ConceptoConfiguradoEnModulo"		NVARCHAR(1),
												"ConceptoConfiguradoOptimus"        NVARCHAR(1),
												"ConceptoConfiguradoFiori"	   	    NVARCHAR(1),
												"ConceptoConfiguradoWs"	   	        NVARCHAR(1),
--CELLZORRO-11 -MBI Mejoramiento Cartola - Clasificación de cargos y abonos												
												"AfectaRentabilidad"				NVARCHAR(1)
										 	 ),
									 										 
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_InformacionContador  	INT,
	OUT p_out_ErroresContador       INT
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	  
	DECLARE SALIDA_CONTROLADA  CONDITION;	       	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN		
		IF (NOT IS_EMPTY("BSCL.Plataforma.BD.Servicios::PLA.EditarConceptoManual_SRV_TablaCodigosMensajes")) THEN
			
			v_Mensajes = SELECT "IdInterno","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2" FROM "BSCL.Plataforma.BD.Servicios::PLA.EditarConceptoManual_SRV_TablaCodigosMensajes";
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_Mensajes,v_erroroutmsg);
			
			INSERT INTO "BSCL.Plataforma.BD.Servicios::PLA.EditarConceptoManual_OUT_InformacionDetalle"							
			SELECT TCM."IdInterno",EM."Codigo",EM."Descripcion" 
			  FROM :v_erroroutmsg EM 
			 INNER JOIN "BSCL.Plataforma.BD.Servicios::PLA.EditarConceptoManual_SRV_TablaCodigosMensajes" TCM 
			 		 ON TCM."IdInterno" = EM."IdInterno" AND TCM."Codigo" = EM."Codigo" WHERE TCM."Resultado" = 'OK';
			
			INSERT INTO "BSCL.Plataforma.BD.Servicios::PLA.EditarConceptoManual_OUT_ErroresDetalle"
			SELECT TCM."IdInterno",EM."Codigo",EM."Descripcion" 
			  FROM :v_erroroutmsg EM 
			 INNER JOIN "BSCL.Plataforma.BD.Servicios::PLA.EditarConceptoManual_SRV_TablaCodigosMensajes" TCM 
			 		 ON TCM."IdInterno" = EM."IdInterno" AND TCM."Codigo" = EM."Codigo" WHERE TCM."Resultado" = 'RECH';
		END IF;
		
		SELECT count(1) into p_out_InformacionContador FROM "BSCL.Plataforma.BD.Servicios::PLA.EditarConceptoManual_OUT_InformacionDetalle";
		SELECT count(1) into p_out_ErroresContador FROM "BSCL.Plataforma.BD.Servicios::PLA.EditarConceptoManual_OUT_ErroresDetalle";		
	END;
	
	--------------------
	--VALIDACIONES
	--------------------	   
	IF p_UsaReglasNegocio = 'S' THEN
	BEGIN
		DECLARE v_resultadoValidacion 	NVARCHAR(40);
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_Errores,:v_resultadoValidacion);
		IF :v_resultadoValidacion = 'RECH' THEN
			INSERT INTO "BSCL.Plataforma.BD.Servicios::PLA.EditarConceptoManual_SRV_TablaCodigosMensajes"
			SELECT 0 as "IdInterno","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2",'RECH' as "Resultado" FROM :v_Errores;				
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		IF IS_EMPTY(:p_Conceptos) THEN
			INSERT INTO "BSCL.Plataforma.BD.Servicios::PLA.EditarConceptoManual_SRV_TablaCodigosMensajes"
			SELECT 0 as "IdInterno",'NO_VIENEN_MOVIMIENTOS' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2",'RECH' as "Resultado" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;		
	END; 
	END IF;
	
	p_out_InformacionContador	:= 0;
	p_out_ErroresContador		:= 0;
	
	
	--------------------
	--LOGICA
	--------------------
	BEGIN
		DECLARE v_Count BiGINT :=0;
		
		declare v_pcli_out_AdvertenciasContador int;
		declare v_pcli_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));
		declare v_pcli_out_ErroresContador int;
		declare v_pcli_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
		declare v_existe_conceptoCustodia int;
		declare v_existe_conceptoTesoreria int;
		declare v_existe_conceptoCuentaInversion int;
		declare v_existe_conceptoFacturacion int;
		
		--PLATAFORMA
		UPDATE CNP
		   SET  CNP."Nombre" 	= C."Nombre",
		   		CNP."IdEstado" 	= CASE WHEN C."IdEstado" = 'ACT' THEN 'VIG' ELSE C."IdEstado" END,
		   		CNP."IdTipoCriterioNeteo" = C."IdTipoCriterioNeteo",
		   		CNP."NombreConceptoCartola" = C."NombreConceptoCartola",
		   		CNP."ExclusivoCartola"	 = C."ExclusivoCartola",
		   		CNP."ConceptoAbonoReplica" = COALESCE(C."ConceptoAbonoReplica", NULL),
				CNP."ConceptoCargoReplica" = COALESCE(C."ConceptoCargoReplica", NULL)
		  FROM :p_Conceptos AS C
		 INNER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS CNP ON CNP."IdConcepto" = C."IdConcepto";
	SELECT '4444',* FROM :p_configuracionCuentaInversion;
	   
	   --CUSTODIA
	   SELECT COUNT("IdConcepto") INTO v_existe_conceptoCustodia FROM "BSCL.Custodia.BD::CUS.ConceptoCustodia" 
	   WHERE "IdConcepto" IN (SELECT "IdConcepto" FROM :p_configuracionCustodia);
	   
	   IF(v_existe_conceptoCustodia > 0) THEN
		SELECT COUNT(1) INTO v_Count FROM :p_configuracionCustodia;
		UPDATE CNP
		   SET  CNP."NombreEntidad" 					= CASE WHEN v_Count >= 1 THEN (CASE WHEN CC."USO" = 'Titulo' THEN 'CUS.INGRESORETIROTITULO'
																						   WHEN CC."USO" = 'Garantia' THEN 'CUS.GARANTIA'
																						   WHEN CC."USO" = 'Prestamo' THEN 'CUS.PRESTAMO'
																						   WHEN CC."USO" = 'SorteoLetras' THEN 'CUS.SORTEOLETRAS'
																						   WHEN CC."USO" = 'CargaMasivaCustodia' THEN 'CUS.CARGAMASIVACUSTODIA'
																						   ELSE ''
																					  END)
														  ELSE  CNP."NombreEntidad" END,					  				  				
		   		CNP."IdClasificacionTipoInstrumento"	= COALESCE(CC."IdClasificacionTipoInstrumento",'-'),
				CNP."IdTipoInstrumento"					= COALESCE(CC."IdTipoInstrumento",''),
				CNP."IdInstrumento"						= COALESCE(CC."IdInstrumento",0),
				CNP."IdContraparte"						= COALESCE(CC."IdContraparte",0),
				CNP."ConCertificadoAutomatico"			= CC."CertificadoAutomatico",
				CNP."EsGarantiaVendible"				= CC."EsGarantiaVendible",
				CNP."RetieneDividendos"					= CC."RetieneDividendos",
				CNP."GeneraTransferenciaBoveda"			= CC."GeneraTransferenciaBoveda",
				CNP."EsTransferenciaPortafolio"			= CC."EsTransferenciaPortafolio",
				CNP."EsTransferenciaCliente"			= CC."EsTransferenciaCliente",
				CNP."VisiblePorEjecutivo"				= C."VisiblePorEjecutivo",
				CNP."IdTipoCuenta"						= COALESCE(CC."IdTipoCuenta",''),
				CNP."IdTipoOperacion"					= CASE 
															  WHEN CC."USO" = 'Titulo' THEN '-'
															  WHEN CC."USO" = 'Garantia' THEN '-'
															  WHEN CC."USO" = 'Prestamo' THEN '-'						  
															  WHEN CC."USO" = 'SorteoLetras' THEN '-'
															  WHEN CC."USO" = 'CargaMasivaCustodia' THEN '-'
															  ELSE ''
														  END,
				CNP."ConfiguradoEnModulo"				= COALESCE(CC."ConceptoConfiguradoEnModulo",'N'),
				CNP."FolioObligatorio"					= COALESCE(CC."FolioObligatorio",'N'),
				CNP."RetieneAmortizacion"				= CC."RetieneAmortizacion",
				CNP."RetieneIntereses"					= CC."RetieneIntereses",
				CNP."VisibleOptimus"            		= CC."ConceptoConfiguradoOptimus",
				CNP."VisibleFiori"              		= CC."ConceptoConfiguradoFiori",
				CNP."VisibleWS"                 		= CC."ConceptoConfiguradoWs",
				CNP."EsGarantiaPrendaria"           	= CC."EsGarantiaPrendaria"
		  FROM :p_Conceptos AS C
		 INNER JOIN :p_configuracionCustodia as CC ON CC."IdConcepto" = C."IdConcepto" 
		 INNER JOIN "BSCL.Custodia.BD::CUS.ConceptoCustodia" AS CNP ON 
		 		CNP."IdConcepto" = C."IdConcepto" 
		 		AND CNP."IdConceptoCustodia" = CC."IdConceptoCustodia"
		 		AND (CNP."NombreEntidad" = (CASE WHEN CC."USO" = 'Titulo' THEN 'CUS.INGRESORETIROTITULO'
												 WHEN CC."USO" = 'Garantia' THEN 'CUS.GARANTIA'
												 WHEN CC."USO" = 'Prestamo' THEN 'CUS.PRESTAMO'
												 WHEN CC."USO" = 'SorteoLetras' THEN 'CUS.SORTEOLETRAS'
												 WHEN CC."USO" = 'CargaMasivaCustodia' THEN 'CUS.CARGAMASIVACUSTODIA'
										  	END) 
										  	OR CNP."NombreEntidad" = '' OR CC."USO" = '' OR CNP."NombreEntidad" <>  (CASE WHEN CC."USO" = 'Titulo' THEN 'CUS.INGRESORETIROTITULO'
                                                 WHEN CC."USO" = 'Garantia' THEN 'CUS.GARANTIA'
                                                 WHEN CC."USO" = 'Prestamo' THEN 'CUS.PRESTAMO'
                                                 WHEN CC."USO" = 'SorteoLetras' THEN 'CUS.SORTEOLETRAS'
                                                 WHEN CC."USO" = 'CargaMasivaCustodia' THEN 'CUS.CARGAMASIVACUSTODIA'
                                            END)			  	
					) ;
	   ELSE 
        INSERT INTO "BSCL.Custodia.BD::CUS.ConceptoCustodia" ("IdConceptoCustodia","IdConcepto","NombreEntidad","IdClasificacionTipoInstrumento",
                    "IdTipoInstrumento","IdInstrumento","IdTipoOperacion","IdContraparte","Descripcion","ConCertificadoAutomatico","GeneraMovimientoEnTransito",
                    "GeneraTraspaso","EsGarantiaPrendaria","EsGarantiaVendible", "RetieneDividendos", "GeneraTransferenciaBoveda","EsTransferenciaPortafolio","EsTransferenciaCliente",
                    "VisiblePorEjecutivo","IdTipoCuenta", "ConfiguradoEnModulo", "FolioObligatorio", "RetieneAmortizacion", "RetieneIntereses","VisibleOptimus","VisibleFiori","VisibleWS")
        SELECT "BSCL.Custodia.BD.Secuencias::ConceptoCustodia".NEXTVAL as "IdConceptoCustodia","IdConcepto","NombreEntidad","IdClasificacionTipoInstrumento","IdTipoInstrumento"
                ,"IdInstrumento","IdTipoOperacion","IdContraparte","Descripcion","CertificadoAutomatico","GeneraMovimientoEnTransito","GeneraTraspaso","EsGarantiaPrendaria"
                ,"EsGarantiaVendible","RetieneDividendos","GeneraTransferenciaBoveda","EsTransferenciaPortafolio","EsTransferenciaCliente","VisiblePorEjecutivo","IdTipoCuenta"
                ,"ConfiguradoEnModulo","FolioObligatorio","RetieneAmortizacion","RetieneIntereses","ConceptoConfiguradoOptimus","ConceptoConfiguradoFiori","ConceptoConfiguradoWs"
            FROM (
                SELECT  C."IdConcepto"
                        ,CASE WHEN CC."USO" = 'Titulo'              THEN 'CUS.INGRESORETIROTITULO'                        
                              WHEN CC."USO" = 'Garantia'            THEN 'CUS.GARANTIA'
                              WHEN CC."USO" = 'Prestamo'            THEN 'CUS.PRESTAMO'
                              WHEN CC."USO" = 'SorteoLetras'        THEN 'CUS.SORTEOLETRAS'
                              WHEN CC."USO" = 'CargaMasivaCustodia' THEN 'CUS.CARGAMASIVACUSTODIA'
                              ELSE ''
                          END AS "NombreEntidad"
                        ,COALESCE(CC."IdClasificacionTipoInstrumento",'-') AS "IdClasificacionTipoInstrumento"
                        ,COALESCE(CC."IdTipoInstrumento",'') AS "IdTipoInstrumento"
                        ,COALESCE(CC."IdInstrumento",0) AS "IdInstrumento"
                        ,'-' AS "IdTipoOperacion"
                        ,COALESCE(CC."IdContraparte",0) AS "IdContraparte"
                        ,'' as "Descripcion"
                        ,CC."CertificadoAutomatico"
                        ,'*' AS "GeneraMovimientoEnTransito"    --CAMPO A ELIMINAR POST MIGRACION JAVA
                        ,'*' AS "GeneraTraspaso"                --CAMPO A ELIMINAR POST MIGRACION JAVA
                        ,CC."EsGarantiaPrendaria"
                        ,CC."EsGarantiaVendible"
                        ,CC."RetieneDividendos"
                        ,CC."GeneraTransferenciaBoveda"
                        ,CC."EsTransferenciaPortafolio"
                        ,CC."EsTransferenciaCliente"
                        ,C."VisiblePorEjecutivo"
                        ,COALESCE(CC."IdTipoCuenta",'')AS "IdTipoCuenta"
                        ,COALESCE(CC."ConceptoConfiguradoEnModulo",'N') AS "ConfiguradoEnModulo"
                        ,COALESCE(CC."FolioObligatorio",'N') AS "FolioObligatorio" 
                        ,CC."RetieneAmortizacion"
                        ,CC."RetieneIntereses"
                        ,CC."ConceptoConfiguradoOptimus"
                        ,CC."ConceptoConfiguradoFiori"
                        ,CC."ConceptoConfiguradoWs"
                  FROM :p_Conceptos as C
                 INNER JOIN :p_configuracionCustodia as CC ON CC."IdConcepto" = C."IdConcepto");
	   END IF;
	
	   --TESORERIA
	   SELECT COUNT("IdConcepto") INTO v_existe_conceptoTesoreria FROM "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" 
	   WHERE "IdConcepto" IN (SELECT "IdConcepto" FROM :p_configuracionTesoreria);
	   
	   IF (v_existe_conceptoTesoreria > 0) THEN 
		  UPDATE CNP
		   SET	CNP."NombreEntidad"				= CASE WHEN CT."USO" = 'Comprobante' 	THEN 'TES.IngresoEgreso'
													   WHEN CT."USO" = 'Deposito' 		THEN 'TES.Deposito'
													   WHEN CT."USO" = 'CargaMasiva' 	THEN 'TES.CargaMasiva'
												   END,
				CNP."VisibleOptimus"            = CT."ConceptoConfiguradoOptimus",
				CNP."VisibleFiori"              = CT."ConceptoConfiguradoFiori",
				CNP."VisibleWS"                 = CT."ConceptoConfiguradoWs",
				CNP."DisponibleIngresoManual"	= CT."DisponibleIngresoManual",
				CNP."VisiblePorEjecutivo"		= C."VisiblePorEjecutivo",
				CNP."ConfiguradoEnModulo"		= COALESCE(CT."ConceptoConfiguradoEnModulo",'N'),
				CNP."NoValidaSaldoCaja"			= COALESCE(CT."NoValidaSaldoCaja",'N'),
				CNP."GeneraComprobanteAutomaticamente" = COALESCE(CT."GeneraComprobanteAutomaticamente",'N')
		  FROM :p_Conceptos as C
		  INNER JOIN :p_configuracionTesoreria as CT ON CT."IdConcepto" = C."IdConcepto"
		  INNER JOIN "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" AS CNP ON CNP."IdConcepto" = C."IdConcepto";
	   ELSE
	       INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria"("IdConcepto","NombreEntidad","MultipleConcepto","Tipo","DesplegarEnComboBox",
                    "DisponibleIngresoManual","VisiblePorEjecutivo","Origen", "ConfiguradoEnModulo","VisibleOptimus","VisibleFiori","VisibleWS","NoValidaSaldoCaja","GeneraComprobanteAutomaticamente")
            SELECT   C."IdConcepto"
                ,CASE WHEN CT."USO" = 'Comprobante' THEN 'TES.IngresoEgreso' --'TES.Comprobante'        
                      WHEN CT."USO" = 'Deposito'    THEN 'TES.Deposito'         
                      WHEN CT."USO" = 'CargaMasiva' THEN 'TES.CargaMasiva'      
                  END AS "NombreEntidad"
                ,'N'                                AS "MultipleConcepto"           
                ,'AM'                               AS "Tipo"
                ,'S'                                AS "DesplegarEnComboBox"
                ,CT."DisponibleIngresoManual"           
                ,C."VisiblePorEjecutivo"            
                ,'MAN'                              AS "Origen"
                ,CT."ConceptoConfiguradoEnModulo"   AS "ConfiguradoEnModulo"
                ,CT."ConceptoConfiguradoOptimus"
                ,CT."ConceptoConfiguradoFiori"
                ,CT."ConceptoConfiguradoWs"
                ,CT."NoValidaSaldoCaja"				AS "NoValidaSaldoCaja"
                ,CT."GeneraComprobanteAutomaticamente"
            FROM :p_Conceptos as C
            INNER JOIN :p_configuracionTesoreria as CT ON CT."IdConcepto" = C."IdConcepto";
	   END IF;
		
	   --CUENTA INVERSION
	   SELECT COUNT("IdConcepto") INTO v_existe_conceptoCuentaInversion FROM "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion" 
	   WHERE "IdConcepto" IN (SELECT "IdConcepto" FROM :p_configuracionCuentaInversion);
	   
	   IF (v_existe_conceptoCuentaInversion > 0) THEN
	       UPDATE CNP
		      SET  CNP."DisponibleIngresoManual"	= CI."DisponibleIngresoManual",
				    CNP."VisiblePorEjecutivo"		= C."VisiblePorEjecutivo",
				    CNP."VisibleOptimus"            = CI."ConceptoConfiguradoOptimus",
				    CNP."VisibleFiori"              = CI."ConceptoConfiguradoFiori",
				    CNP."VisibleWS"                 = CI."ConceptoConfiguradoWs",
				    CNP."ConfiguradoEnModulo"		= COALESCE(CI."ConceptoConfiguradoEnModulo",'N'),
--CELLZORRO-11 -MBI Mejoramiento Cartola - Clasificación de cargos y abonos
   			        CNP."AfectaRentabilidad"        = CI."AfectaRentabilidad"
				    
		      FROM :p_Conceptos as C
		      INNER JOIN :p_configuracionCuentaInversion as CI ON CI."IdConcepto" = C."IdConcepto"
		      INNER JOIN "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion" AS CNP ON CNP."IdConcepto" = C."IdConcepto";
		 
		 SELECT * FROM :p_configuracionCuentaInversion;
	   ELSE 
	       INSERT INTO "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConcepto","TipoConcepto","DesplegarEnCombobox",
                    "DisponibleIngresoManual","VisiblePorEjecutivo","NombreEntidad","Origen", "ConfiguradoEnModulo","VisibleOptimus","VisibleFiori","VisibleWS","AfectaRentabilidad")
            SELECT   C."IdConcepto"
                ,'AM'                               AS "TipoConcepto"
                ,'S'                                AS "DesplegarEnCombobox"
                ,CI."DisponibleIngresoManual"
                ,C."VisiblePorEjecutivo"
                ,'CTA.CARGOABONOMOVIMIENTO'         AS "NombreEntidad"
                ,'MAN'                              AS "Origen"
                ,CI."ConceptoConfiguradoEnModulo"   AS "ConfiguradoEnModulo"
                ,CI."ConceptoConfiguradoOptimus"
                ,CI."ConceptoConfiguradoFiori"
                ,CI."ConceptoConfiguradoWs"
 --CELLZORRO-11 -MBI Mejoramiento Cartola - Clasificación de cargos y abonos se agrega campo 	"AfectaRentabilidad"                  
                ,CI."AfectaRentabilidad"			AS "AfectaRentabilidad"
                
            FROM :p_Conceptos as C
            INNER JOIN :p_configuracionCuentaInversion as CI ON CI."IdConcepto" = C."IdConcepto";
	   END IF;
       -- OPTIMUSPE-324 Implementación Requerimientos de Desarrollo Contabilidad Perú

	   --FACTURACION
	   SELECT COUNT("IdConcepto") INTO v_existe_conceptoFacturacion FROM "BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
	   WHERE "IdConcepto" IN (SELECT "IdConcepto" FROM :p_configuracionFacturacion);
	   
	   IF (v_existe_conceptoFacturacion > 0) THEN
		  UPDATE CFA
		      SET CFA."EsOtrosConceptos"	= FA."EsOtrosConceptos",
		   		  CFA."EsReembolso"		  = FA."EsReembolso",
		   	      CFA."VisibleOptimus"    = FA."ConceptoConfiguradoOptimus",
				  CFA."VisibleFiori"      = FA."ConceptoConfiguradoFiori",
				  CFA."VisibleWS"         = FA."ConceptoConfiguradoWs",
				  CFA."ConfiguradoEnModulo" = FA."ConceptoConfiguradoEnModulo",
				  CFA."CentroCosto"		= FA."CentroCosto",
				  CFA."InterfazSIG"  	= FA."EsInterfazSIG",
				  CFA."CodigoProducto"  = FA."CodigoProducto",
				  CFA."Contrapartida"	= FA."Contrapartida",
				  CFA."AbonoClpContrapartida"	= FA."AbonoClpContrapartida"

		      FROM :p_Conceptos as C
		      INNER JOIN :p_configuracionFacturacion as FA ON FA."IdConcepto" = C."IdConcepto"
		      INNER JOIN "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" AS CFA ON CFA."IdConcepto" = C."IdConcepto";
		      --SELECT * FROM "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" WHERE "IdConcepto" = 'TEST03'
		 ELSE 
		   INSERT INTO "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion", "Origen", "IdEstado", "EsOtrosConceptos", 
                                                                    "EsReembolso","ConfiguradoEnModulo","CentroCosto","VisibleOptimus","VisibleFiori","VisibleWS","CodigoProducto","InterfazSIG","Contrapartida","AbonoClpContrapartida")
            SELECT "BSCL.Facturacion.BD.Secuencias::ConceptoFacturacion".NEXTVAL as "IdConceptoFacturacion"
                ,CF."IdConcepto"                    AS "IdConcepto"
                ,''                                 AS "IdTipoOperacion"
                ,'MAN'                              AS "Origen"
                ,'VIG'                              AS "IdEstado"
                ,CF."EsOtrosConceptos"
                ,CF."EsReembolso"
                ,CF."ConceptoConfiguradoEnModulo"   AS "ConfiguradoEnModulo"
                ,CF."CentroCosto"
                ,CF."ConceptoConfiguradoOptimus"
                ,CF."ConceptoConfiguradoFiori"
                ,CF."ConceptoConfiguradoWs"
                ,CF."CodigoProducto"
				,CF."EsInterfazSIG"
				,CF."Contrapartida"
				,CF."AbonoClpContrapartida"
            FROM :p_Conceptos as C
            INNER JOIN :p_configuracionFacturacion as CF ON CF."IdConcepto" = C."IdConcepto";
		 END IF;
		 
		--CADA VEZ QUE SE CREA O MODIFICA CONCEPTO SE RECALCULA PREFERENCIA CALCE
		call "BSCL.Plataforma.BD.Servicios::CalculaPreferenciaCalceLibroInventario"(
			p_IdEmpresa => :p_IdEmpresa,
		    p_IdUsuario => :p_IdUsuario,
		    p_IdModulo  => :p_IdModulo,
		    p_out_AdvertenciasContador => :v_pcli_out_AdvertenciasContador,
			p_out_AdvertenciasDetalle => :v_pcli_out_AdvertenciasDetalle,
			p_out_ErroresContador => :v_pcli_out_ErroresContador,
			p_out_ErroresDetalle => :v_pcli_out_ErroresDetalle
		);

		--RESULTADO PROCESO
		INSERT INTO "BSCL.Plataforma.BD.Servicios::PLA.EditarConceptoManual_SRV_TablaCodigosMensajes"
		SELECT	 0 as "IdInterno",
				'UI_EDITADO_CONCEPTO_MANUAL' as "Codigo",
				'Nombre' as "CodigoDato1",
				"Nombre" as "ValorDato1",
				'' as "CodigoDato2",
				'' as "ValorDato2",
				'OK' as "Resultado" 
		  FROM :p_Conceptos;
		SIGNAL SALIDA_CONTROLADA;
	END;
END;
--GO
