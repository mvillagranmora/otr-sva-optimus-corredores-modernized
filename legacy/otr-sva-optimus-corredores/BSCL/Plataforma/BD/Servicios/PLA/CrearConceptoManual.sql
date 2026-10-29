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
DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::PLA.CrearConceptoManual";
GO

CREATE PROCEDURE  "BSCL.Plataforma.BD.Servicios::PLA.CrearConceptoManual" (
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
												"GeneraComprobanteAutomaticamente"	NVARCHAR(1)
										 	 ),
	IN	p_configuracionCuentaInversion	TABLE(	"IdConcepto"						NVARCHAR(6),
												"DisponibleIngresoManual"			NVARCHAR(1),
												"ConceptoConfiguradoEnModulo"		NVARCHAR(1),
												"ConceptoConfiguradoOptimus"        NVARCHAR(1),
												"ConceptoConfiguradoFiori"	   	    NVARCHAR(1),
												"ConceptoConfiguradoWs"	   	        NVARCHAR(1),
--CELLZORRO-11 -MBI Mejoramiento Cartola - Clasificaci�n de cargos y abonos												
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
		IF (NOT IS_EMPTY("BSCL.Plataforma.BD.Servicios::PLA.CrearConceptoManual_SRV_TablaCodigosMensajes")) THEN
			
			v_Mensajes = SELECT "IdInterno","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2" FROM "BSCL.Plataforma.BD.Servicios::PLA.CrearConceptoManual_SRV_TablaCodigosMensajes";
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_Mensajes,v_erroroutmsg);
			
			INSERT INTO "BSCL.Plataforma.BD.Servicios::PLA.CrearConceptoManual_OUT_InformacionDetalle"						
			SELECT TCM."IdInterno",EM."Codigo",EM."Descripcion" 
			  FROM :v_erroroutmsg EM 
			 INNER JOIN "BSCL.Plataforma.BD.Servicios::PLA.CrearConceptoManual_SRV_TablaCodigosMensajes" TCM 
			 		 ON TCM."IdInterno" = EM."IdInterno" AND TCM."Codigo" = EM."Codigo" WHERE TCM."Resultado" = 'OK';
			
			INSERT INTO "BSCL.Plataforma.BD.Servicios::PLA.CrearConceptoManual_OUT_ErroresDetalle"
			SELECT TCM."IdInterno",EM."Codigo",EM."Descripcion" 
			  FROM :v_erroroutmsg EM 
			 INNER JOIN "BSCL.Plataforma.BD.Servicios::PLA.CrearConceptoManual_SRV_TablaCodigosMensajes" TCM 
			 		 ON TCM."IdInterno" = EM."IdInterno" AND TCM."Codigo" = EM."Codigo" WHERE TCM."Resultado" = 'RECH';
		END IF;
		
		SELECT count(1) into p_out_InformacionContador FROM "BSCL.Plataforma.BD.Servicios::PLA.CrearConceptoManual_OUT_InformacionDetalle";
		SELECT count(1) into p_out_ErroresContador FROM "BSCL.Plataforma.BD.Servicios::PLA.CrearConceptoManual_OUT_ErroresDetalle";		
	END;
	
	--------------------
	--VALIDACIONES
	--------------------	   
	IF p_UsaReglasNegocio = 'S' THEN
	BEGIN
		DECLARE v_resultadoValidacion 	NVARCHAR(40);
		DECLARE v_LargoNombre			NVARCHAR(6);
	
		
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_Errores,:v_resultadoValidacion);
		IF :v_resultadoValidacion = 'RECH' THEN
			INSERT INTO "BSCL.Plataforma.BD.Servicios::PLA.CrearConceptoManual_SRV_TablaCodigosMensajes"
			SELECT 0 as "IdInterno","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2",'RECH' as "Resultado" FROM :v_Errores;				
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		IF IS_EMPTY(:p_Conceptos) THEN
			INSERT INTO "BSCL.Plataforma.BD.Servicios::PLA.CrearConceptoManual_SRV_TablaCodigosMensajes"
			SELECT 0 as "IdInterno",'NO_VIENEN_MOVIMIENTOS' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2",'RECH' as "Resultado" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		v_ExisteConcepto =
		SELECT CNP."IdConcepto"	
		 FROM :p_Conceptos AS C
		INNER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS CNP ON CNP."IdConcepto" = C."IdConcepto";
		
		IF NOT IS_EMPTY(:v_ExisteConcepto) THEN
			INSERT INTO "BSCL.Plataforma.BD.Servicios::PLA.CrearConceptoManual_SRV_TablaCodigosMensajes"
			SELECT 0 as "IdInterno",'CODIGO_CONCEPTO_EXISTE' as "Codigo",'IdConcepto' as "CodigoDato1","IdConcepto" as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2",'RECH' as "Resultado" 
			  FROM :p_Conceptos;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		SELECT LENGTH("IdConcepto") as "LargoNombre" INTO v_LargoNombre FROM :p_Conceptos;
		IF :v_LargoNombre < 6 OR :v_LargoNombre > 6 THEN
			INSERT INTO "BSCL.Plataforma.BD.Servicios::PLA.CrearConceptoManual_SRV_TablaCodigosMensajes"
			SELECT 0 as "IdInterno",CASE WHEN :v_LargoNombre < 6 THEN 'CODIGO_CONCEPTO_MENOR_6' WHEN :v_LargoNombre > 6 THEN 'CODIGO_CONCEPTO_MAYOR_6' END as "Codigo",
				   'IdConcepto' as "CodigoDato1","IdConcepto" as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2",'RECH' as "Resultado" 
			  FROM :p_Conceptos;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
	END; 
	END IF;
	
	p_out_InformacionContador	:= 0;
	p_out_ErroresContador		:= 0;
		
	BEGIN
	
		declare v_pcli_out_AdvertenciasContador int;
		declare v_pcli_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));
		declare v_pcli_out_ErroresContador int;
		declare v_pcli_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
		DECLARE v_LocalizacionSistema NVARCHAR(2);
		
		SELECT "Valor" INTO v_LocalizacionSistema FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.LocalizacionSistema' AND "IdEmpresa" = :p_IdEmpresa;
		
		--PLATAFORMA
		INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto","Nombre","TipoConcepto","IdEstado","IdTipoCriterioNeteo","NombreConceptoCartola","ExclusivoCartola","ConceptoAbonoReplica","ConceptoCargoReplica")
		SELECT  "IdConcepto"
				,"Nombre"
				,'MANUAL' as "TipoConcepto"
				,"IdEstado"
				,"IdTipoCriterioNeteo"
				,"NombreConceptoCartola"
				,"ExclusivoCartola"
				,COALESCE("ConceptoAbonoReplica", NULL)
				,COALESCE("ConceptoCargoReplica", NULL)
		  FROM :p_Conceptos;
		
		--FACTURACION
		INSERT INTO "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" ("IdConceptoFacturacion", "IdConcepto", "IdTipoOperacion", "Origen", "IdEstado", "EsOtrosConceptos", 
																	"EsReembolso","ConfiguradoEnModulo","CentroCosto","VisibleOptimus","VisibleFiori","VisibleWS","CodigoProducto","InterfazSIG", "Contrapartida","AbonoClpContrapartida")
		SELECT "BSCL.Facturacion.BD.Secuencias::ConceptoFacturacion".NEXTVAL as "IdConceptoFacturacion"
				,CF."IdConcepto"					AS "IdConcepto"
				,''									AS "IdTipoOperacion"
				,'MAN'								AS "Origen"
				,'VIG'								AS "IdEstado"
				,CF."EsOtrosConceptos"
				,CF."EsReembolso"
				,CF."ConceptoConfiguradoEnModulo"	AS "ConfiguradoEnModulo"
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

		--CUSTODIA
		INSERT INTO "BSCL.Custodia.BD::CUS.ConceptoCustodia" ("IdConceptoCustodia","IdConcepto","NombreEntidad","IdClasificacionTipoInstrumento",
					"IdTipoInstrumento","IdInstrumento","IdTipoOperacion","IdContraparte","Descripcion","ConCertificadoAutomatico","GeneraMovimientoEnTransito",
					"GeneraTraspaso","EsGarantiaPrendaria","EsGarantiaVendible", "RetieneDividendos", "GeneraTransferenciaBoveda","EsTransferenciaPortafolio","EsTransferenciaCliente",
					"VisiblePorEjecutivo","IdTipoCuenta", "ConfiguradoEnModulo", "FolioObligatorio", "RetieneAmortizacion", "RetieneIntereses","VisibleOptimus","VisibleFiori","VisibleWS")
		SELECT "BSCL.Custodia.BD.Secuencias::ConceptoCustodia".NEXTVAL as "IdConceptoCustodia"
				,"IdConcepto"
				,"NombreEntidad"
				,"IdClasificacionTipoInstrumento"
				,"IdTipoInstrumento"
				,"IdInstrumento"
				,"IdTipoOperacion"
				,"IdContraparte"
				,"Descripcion"
				,"CertificadoAutomatico"
				,"GeneraMovimientoEnTransito"
				,"GeneraTraspaso"
				,"EsGarantiaPrendaria"
				,"EsGarantiaVendible"
				,"RetieneDividendos"
				,"GeneraTransferenciaBoveda"
				,"EsTransferenciaPortafolio"
				,"EsTransferenciaCliente"
				,"VisiblePorEjecutivo"
				,"IdTipoCuenta"
				,"ConfiguradoEnModulo"
				,"FolioObligatorio"
				,"RetieneAmortizacion"
				,"RetieneIntereses"
				,"ConceptoConfiguradoOptimus"
				,"ConceptoConfiguradoFiori"
				,"ConceptoConfiguradoWs"
		  FROM (
		  		SELECT   C."IdConcepto"
						,CASE WHEN CC."USO" = 'Titulo' 				THEN 'CUS.INGRESORETIROTITULO'						  
							  WHEN CC."USO" = 'Garantia' 			THEN 'CUS.GARANTIA'
							  WHEN CC."USO" = 'Prestamo' 			THEN 'CUS.PRESTAMO'
							  WHEN CC."USO" = 'SorteoLetras' 		THEN 'CUS.SORTEOLETRAS'
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
						,'*' AS "GeneraMovimientoEnTransito"	--CAMPO A ELIMINAR POST MIGRACION JAVA
						,'*' AS "GeneraTraspaso"				--CAMPO A ELIMINAR POST MIGRACION JAVA
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
				 INNER JOIN :p_configuracionCustodia as CC ON CC."IdConcepto" = C."IdConcepto"
		  	   );
		
		--TESORERIA
		INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria"("IdConcepto","NombreEntidad","MultipleConcepto","Tipo","DesplegarEnComboBox",
					"DisponibleIngresoManual","VisiblePorEjecutivo","Origen", "ConfiguradoEnModulo","VisibleOptimus","VisibleFiori","VisibleWS","GeneraComprobanteAutomaticamente")
		SELECT   C."IdConcepto"
				,CASE WHEN CT."USO" = 'Comprobante' THEN 'TES.IngresoEgreso' --'TES.Comprobante' 		
					  WHEN CT."USO" = 'Deposito' 	THEN 'TES.Deposito'			
					  WHEN CT."USO" = 'CargaMasiva' THEN 'TES.CargaMasiva'		
				  END AS "NombreEntidad"			  				               
				,'N' 								AS "MultipleConcepto"			
				,'AM'	 							AS "Tipo"
				,'S' 								AS "DesplegarEnComboBox"
				,CT."DisponibleIngresoManual"			
				,C."VisiblePorEjecutivo"			
				,'MAN' 								AS "Origen"
				,CT."ConceptoConfiguradoEnModulo"	AS "ConfiguradoEnModulo"
				,CT."ConceptoConfiguradoOptimus"
				,CT."ConceptoConfiguradoFiori"
				,CT."ConceptoConfiguradoWs"
				,CT."GeneraComprobanteAutomaticamente"
		  FROM :p_Conceptos as C
		 INNER JOIN :p_configuracionTesoreria as CT ON CT."IdConcepto" = C."IdConcepto";

		
		--CUENTA INVERSION
--CELLZORRO-11 -MBI Mejoramiento Cartola - Clasificaci�n de cargos y abonos se agrega campo 'AfectaRentabilidad'		
		INSERT INTO "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"("IdConcepto","TipoConcepto","DesplegarEnCombobox",
					"DisponibleIngresoManual","VisiblePorEjecutivo","NombreEntidad","Origen", "ConfiguradoEnModulo","VisibleOptimus","VisibleFiori","VisibleWS","AfectaRentabilidad")
		SELECT   C."IdConcepto"
				,'AM'	 							AS "TipoConcepto"
				,'S' 								AS "DesplegarEnCombobox"
				,CI."DisponibleIngresoManual"
				,C."VisiblePorEjecutivo"
				,'CTA.CARGOABONOMOVIMIENTO'			AS "NombreEntidad"
				,'MAN' 								AS "Origen"
				,CI."ConceptoConfiguradoEnModulo"	AS "ConfiguradoEnModulo"
				,CI."ConceptoConfiguradoOptimus"
				,CI."ConceptoConfiguradoFiori"
				,CI."ConceptoConfiguradoWs"
--CELLZORRO-11 -MBI Mejoramiento Cartola - Clasificaci�n de cargos y abonos se agrega campo 'AfectaRentabilidad'				
				,CI."AfectaRentabilidad"			AS "AfectaRentabilidad"  
 --CELLZORRO-11 -MBI Mejoramiento Cartola - Clasificaci�n de cargos y abonos se agrega campo 	"AfectaRentabilidad"				
		  FROM :p_Conceptos as C
		 INNER JOIN :p_configuracionCuentaInversion as CI ON CI."IdConcepto" = C."IdConcepto";
	
	
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
		INSERT INTO "BSCL.Plataforma.BD.Servicios::PLA.CrearConceptoManual_SRV_TablaCodigosMensajes"
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