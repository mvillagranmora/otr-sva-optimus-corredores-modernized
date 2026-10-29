SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::CrearIngresoEgreso";

CREATE PROCEDURE "BSCL.Tesoreria.BD.Servicios::CrearIngresoEgreso"(
		IN  p_IdEmpresa						BIGINT,
	   	IN  p_IdUsuario						BIGINT,
	   	IN  p_IdModulo						NVARCHAR(3),                                                                       
	   	IN  p_simularOperacion				NVARCHAR(1),			 -- S/N se usa para simular operacion (algunos casos)
	   	IN  p_UsaReglasNegocio				NVARCHAR(1),			 -- S/N se usa en caso de servicios anidados.
		IN  p_EsIngresoAutomatico			NVARCHAR(1)  DEFAULT 'S', -- S: ingreso automatico por servicio, N: ingreso Manual	
		IN  p_FechaMovimiento				NVARCHAR(10) DEFAULT '0001-01-01', -- Fecha de Sistema(si no viene le asigna fecha sistema)
		
	    OUT p_out_InformacionContador  		INT,
	   	OUT p_out_AdvertenciasContador		INT,
	   	OUT p_out_ErroresContador			INT
	   	
		-- TABLAS ENTRADA
	    --"BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_IngresoEgresoGenerar"
		--"BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_FormaDePago"
		
		-- TABLAS SALIDA
		-- "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_IngresoEgresoGenerado"
		-- "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_ResultadoProceso"
		-- "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_InformacionDetalle"
		-- "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_AdvertenciasDetalle"
		-- "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_ErroresDetalle"
	) 
	   
		LANGUAGE SQLSCRIPT
		SQL SECURITY INVOKER 
		DEFAULT SCHEMA "BSCL"
	AS
	BEGIN
	
		--------------------
		--DECLARE Manejo de Errores
		--------------------	  
		DECLARE v_IdEvento                  TABLE ("IdEvento" BIGINT, "NumeroOperacionConcepto"  NVARCHAR(40));   -- Evento de Autorización
		DECLARE SALIDA_CONTROLADA  CONDITION;	       	
		DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
		BEGIN
			IF (NOT IS_EMPTY("BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Proceso_TablaCodigosMensajes")) THEN
				
				v_Mensajes = SELECT "IdInterno","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2" 
							   FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Proceso_TablaCodigosMensajes";
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_Mensajes,v_erroroutmsg);
					
				INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_InformacionDetalle"							
				SELECT TCM."IdInterno" as "NumeroReferencia", 0 as "Id", EM."Codigo",EM."Descripcion" 
				  FROM :v_erroroutmsg EM 
				 INNER JOIN "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Proceso_TablaCodigosMensajes" TCM 
				 		 ON TCM."IdInterno" = EM."IdInterno" AND TCM."Codigo" = EM."Codigo" WHERE TCM."Resultado" = 'OK';		
				
				INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_AdvertenciasDetalle"
				SELECT TCM."IdInterno" as "NumeroReferencia", 0 as "Id", EM."Codigo",TCM."Resultado" as "RequiereAutorizacion",EM."Descripcion" 
				  FROM :v_erroroutmsg EM 
				 INNER JOIN "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Proceso_TablaCodigosMensajes" TCM 
				 		 ON TCM."IdInterno" = EM."IdInterno" AND TCM."Codigo" = EM."Codigo" WHERE TCM."Resultado" IN ('ADVE','RAUT');
							
				INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_ErroresDetalle"
				SELECT TCM."IdInterno" as "NumeroReferencia", 0 as "Id", EM."Codigo",EM."Descripcion" 
				  FROM :v_erroroutmsg EM 
				 INNER JOIN "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Proceso_TablaCodigosMensajes" TCM 
				 		 ON TCM."IdInterno" = EM."IdInterno" AND TCM."Codigo" = EM."Codigo" WHERE TCM."Resultado" = 'RECH';			 		 
			END IF;
			
			SELECT count(1) into p_out_InformacionContador FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_InformacionDetalle";
			SELECT count(1) into p_out_AdvertenciasContador FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_AdvertenciasDetalle";
			SELECT count(1) into p_out_ErroresContador FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_ErroresDetalle";
		END;
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN		
			INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Proceso_TablaCodigosMensajes"
			SELECT 0 as "IdInterno",'DATA_NO_EXISTE' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2",'RECH' as "Resultado" FROM DUMMY;			
			SIGNAL SALIDA_CONTROLADA;
		END;
		DECLARE EXIT HANDLER FOR SQLEXCEPTION
		BEGIN
			DECLARE v_errorExt 	NVARCHAR(40);	
			IF ::SQL_ERROR_CODE > 10000 THEN 
				--error pseudo-controlado pero desde un Sub-SP
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);
				INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_ErroresDetalle" SELECT 0 as "NumeroReferencia",0 as "Id",v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;
				SIGNAL SALIDA_CONTROLADA;										
			ELSE
				--Error interno no manejado  (El log será captado por la capa de XS control)		
				RESIGNAL;
			END IF;
		END;	
		
	
		
		
		p_out_InformacionContador	:= 0;
		p_out_AdvertenciasContador	:= 0;
		p_out_ErroresContador		:= 0;
		
		v_IngresoEgresoGenerar 	= SELECT * FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_IngresoEgresoGenerar";
		v_FormaDePago			= SELECT * FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_FormaDePago";
		
		-- Proceso
		DELETE FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Proceso_TablaCodigosMensajes";
		
		-- Salida
		DELETE FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_IngresoEgresoGenerado";
		DELETE FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_ResultadoProceso";
		DELETE FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_InformacionDetalle";
		DELETE FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_AdvertenciasDetalle";
		DELETE FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_ErroresDetalle";
	
		v_IdEvento    					= SELECT 0 as "IdEvento", '' as "NumeroOperacionConcepto" FROM dummy where 1=2;   
		
		--------------------
		--VALIDACIONES
		--------------------	   
		IF p_UsaReglasNegocio = 'S' THEN
		BEGIN	
			DECLARE v_resultadoValidacion 	NVARCHAR(40);
			
			IF p_EsIngresoAutomatico = 'N' THEN
				CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_Errores,:v_resultadoValidacion);
				IF :v_resultadoValidacion = 'RECH' THEN
					INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Proceso_TablaCodigosMensajes"
					SELECT 0 as "IdInterno","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2",'RECH' as "Resultado" FROM :v_Errores;				
					SIGNAL SALIDA_CONTROLADA;
				END IF;
			END IF;
			
		  	IF IS_EMPTY(:v_IngresoEgresoGenerar) THEN
		  		INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Proceso_TablaCodigosMensajes"
				SELECT 0 as "IdInterno",'NO_LLEGAN_MOVIMIENTOS_TESORERIA' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2",'RECH' as "Resultado" FROM DUMMY;			
				SIGNAL SALIDA_CONTROLADA;
		  	END IF;
		
			
			INSERT INTO "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearIngresoEgreso_Entrada_IngresoEgreso"
			SELECT * FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_IngresoEgresoGenerar";
	
			INSERT INTO "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearIngresoEgreso_Entrada_FormaDePago"
			SELECT * FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_FormaDePago";
			
			CALL "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosCrearIngresoEgreso"(
				p_IdEmpresa => :p_IdEmpresa,
				p_IdModulo 	=> :p_IdModulo
			);
			
			INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Proceso_TablaCodigosMensajes"
			SELECT "IdInterno","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2","Resultado" 
			FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearIngresoEgreso_Salida_Resultado"
			WHERE "Resultado" <> 'OK';
			 
			-- Obtiene movimientos que requieren ser autorizados
			v_RequierenAutorizacion =  
			SELECT "IdInterno","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2","Resultado" 
			FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearIngresoEgreso_Salida_Resultado"
			WHERE "Resultado" = 'RAUT';  -- RAUT: requiere autorizacion
	
	
			--Se eliminan los movimientos rechazados
			v_IngresoEgresoGenerar = 
			SELECT IEG.* 
			FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_IngresoEgresoGenerar" as IEG
			INNER JOIN (SELECT DISTINCT "IdInterno" FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearIngresoEgreso_Salida_Resultado" WHERE "Resultado" = 'OK'
			 			EXCEPT 
			 			SELECT DISTINCT "IdInterno" FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearIngresoEgreso_Salida_Resultado" WHERE "Resultado" = 'RECH') as R
			 	    ON R."IdInterno" = IEG."NumeroReferencia";
			
			
			v_FormaDePago = 
			SELECT FPG.* 
			FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_FormaDePago" as FPG
			INNER JOIN (SELECT DISTINCT "IdInterno" FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearIngresoEgreso_Salida_Resultado" WHERE "Resultado" = 'OK'
			 			EXCEPT 
			 			SELECT DISTINCT "IdInterno" FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearIngresoEgreso_Salida_Resultado" WHERE "Resultado" = 'RECH') as R
			 	    ON R."IdInterno" = FPG."NumeroReferencia";
			
			IF IS_EMPTY(:v_IngresoEgresoGenerar) THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF; 
		END;
		END IF;
		
		--------------------
		--LOGICA
		--------------------
		BEGIN
			
			DECLARE v_FechaSistema			NVARCHAR(10);
			DECLARE v_FechaMovimiento		NVARCHAR(10);
			DECLARE v_IdPersonaRemitente 	bigint;
			DECLARE v_IdCliente 			bigint;
			DECLARE v_IdDireccionPersonaRemitente bigint;
			DECLARE v_fechaHoraSistema NVARCHAR(40);
			DECLARE v_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(5000), "ValorNuevo" nvarchar(5000));
			DECLARE v_IdIngresoEgreso BIGINT;
			--------------------------------------------   
			-- OBTENER DATOS ADICIONALES
			--------------------------------------------
			
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema"(:p_IdEmpresa, v_FechaSistema);
			
			-- Si no viene FechaMovimiento desde fuera del SP, le asigna la fecha de sistema
			IF (:p_FechaMovimiento <> '0001-01-01' AND :p_FechaMovimiento <> '') THEN
				v_FechaMovimiento = :p_FechaMovimiento;
			ELSE
				v_FechaMovimiento = v_FechaSistema;
			END IF;
			
	
			v_SecuenciaIngresoEgreso = 
			SELECT 	IEG."NumeroReferencia"
				   ,"BSCL.Tesoreria.BD.Secuencias::IngresoEgreso".NEXTVAL as "IdIngresoEgreso"
			FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_IngresoEgresoGenerar" as IEG;
			
			
			----------------------------------------------------
			-- GENERAR MOVIMIENTOS DE TESORERIA
			----------------------------------------------------
	
			IF (:p_simularOperacion = 'N') THEN	 
				INSERT INTO "BSCL.Tesoreria.BD::TES.IngresoEgreso" (
					"IdIngresoEgreso",
					"IdEmpresa",
					"IdModulo",
					"IdUsuario",
					"IdTipoMovimiento",
					"IdConcepto",
					"IdCaja",
					"IdMoneda",
					"Monto",
					"FechaLiquidacion",
					"FechaIngresoRegistro",   -- CURRENT_UTCTIMESTAMP
					"FechaMovimiento",        
					"IdTipoPersona",
					"IdPersona",
					"IdPortafolio",
					"NombrePersona",
					"IdTipoDocumentoIdentidad",
					"DocumentoIdentidad",
					"IdTipoPersonaTitularPago",
		 			"IdPersonaTitularPago",
		 			"NombrePersonaTitularPago",
		 			"IdPortafolioTitularPago",
		 			"IdTipoDocumentoIdentidadTitularPago",
		 			"DocumentoIdentidadTitularPago",
					"SecuenciaAgrupacion",
					"SecuenciaRelacionada",
					"Glosa",					
					"IdEmpleadoDigitador",
					"IdSucursalEmpleadoDigitador",			
					"IdTipoComprobanteOrigen",
					"IdComprobanteOrigen",
					"FechaComprobanteOrigen",
					"FechaLiquidacionComprobanteOrigen",
					"IdTipoProducto",
					"IdTipoOperacion",
					"IdOperacion",
		 			"FechaOperacion",
		 			"IdCondicionLiquidacion",
					"IdBolsa",
					"IdMonedaOperacion",
					"MontoOperacion",
					"FechaParidad",
					"IdTipoReajuste",
					"ValorParidad",
					"FolioCartera",
					"IdInstrumento",
					"IdTipoInstrumento",
					"IdClasificacionTipoInstrumento",				
					"EsBilateral",
					"TitularPago",
					"EsIngresoAutomatico",
					"AgrupaComprobante",
					"NeteaComprobante",
					"MueveCuentaInversion",
					"IdEstado"
				)
				SELECT  
					 SIE."IdIngresoEgreso"
					,:p_IdEmpresa
					,:p_IdModulo
					,:p_IdUsuario
					,IEG."IdTipoMovimiento"
					,IEG."IdConcepto"
					,IEG."IdCaja"
					,IEG."IdMoneda"
				   	,"BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(IEG."Monto",MF."MontoDecimales",MF."MontoTrucaRedondea") AS "Monto" 
				   	,IEG."FechaLiquidacion"
				   	,CURRENT_UTCTIMESTAMP     -- FechaIngresoRegistro
				   	,:v_FechaMovimiento       -- FechaMovimiento
				   	,IEG."IdTipoPersona"
				   	,IEG."IdPersona"
				   	,IEG."IdPortafolio"
				   	,P."NombreCompleto"
				   	,P."TipoDocumentoIdentidad"
				   	,P."DocumentoIdentidad"
				   	,IEG."IdTipoPersonaTitularPago"
				   	,IEG."IdPersonaTitularPago"
				   	,PTP."NombreCompleto"
				   	,IEG."IdPortafolioTitularPago"			  
				   	,PTP."TipoDocumentoIdentidad"
				   	,PTP."DocumentoIdentidad"	   	
				   	,IEG."SecuenciaAgrupacion"
				   	,IEG."SecuenciaRelacionada"
				   	,IEG."Glosa"		   			   	
				   	,IEG."IdEmpleadoDigitador"
				   	,IEG."IdSucursalEmpleadoDigitador"
				    ,coalesce(IEG."IdTipoComprobante",'')
				   	,coalesce(IEG."IdComprobante",0)
				   	,coalesce(IEG."FechaComprobante",'0001-01-01')
				    ,coalesce(IEG."FechaLiquidacionComprobante",'0001-01-01')
				    ,IEG."IdTipoProducto"
				    ,IEG."IdTipoOperacion"
				    ,IEG."IdOperacion"
		 			,IEG."FechaOperacion"
		 			,IEG."IdCondicionLiquidacion"
				    ,IEG."IdBolsa"
				    ,IEG."IdMonedaOperacion"
				    ,IEG."MontoOperacion"
				    ,IEG."FechaParidad"
				    ,IEG."IdTipoReajuste"
				    ,IEG."ValorParidad"			    
				    ,IEG."FolioCartera"
				    ,IEG."IdInstrumento"
				    ,IEG."IdTipoInstrumento"
				    ,IEG."IdClasificacionTipoInstrumento"
				    ,IEG."Bilateral"
				    ,IEG."TitularPago"
				    ,:p_EsIngresoAutomatico
				    ,IEG."AgruparEnComprobante"
				    ,COALESCE(PrefTes."TES_NIE",'N')		--Netea movimientos de EGR e ING según preferencias del portafolio
				    ,IEG."MueveCuentaInversion"
				    ,'ING'
				  FROM :v_IngresoEgresoGenerar as IEG
				 INNER JOIN :v_SecuenciaIngresoEgreso as SIE 
				 		 ON SIE."NumeroReferencia" = IEG."NumeroReferencia"
				 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" as P 
				 		 ON P."IdPersona" = IEG."IdPersona"
				 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" as PTP 
				 		 ON PTP."IdPersona" = IEG."IdPersonaTitularPago"
				 INNER JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MF 
				 		 ON MF."IdMoneda" 			= IEG."IdMoneda" 
				 		AND MF."IdUsoMonedaFormato" = 'GEN'
				 		AND MF."IdTipoInstrumento" 	= ''
				 		AND MF."IdTipoOperacion" 	= ''
				  LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciasTesoreria" as PrefTes 
				  		 ON PrefTes."IdPortafolio" = IEG."IdPortafolioTitularPago";
				
				SELECT MAX("IdIngresoEgreso") INTO v_IdIngresoEgreso FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso";
				
				CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa => p_IdEmpresa, p_out_FechaHoraSistema => :v_fechaHoraSistema);
		        
				v_DetalleLog = 	SELECT 'INGRESO' AS "SubRegistro", 
		        		'IdIngresoEgreso' AS "CampoRegistro", 
		        		'' AS "ValorAntiguo", 
		        		:v_IdIngresoEgreso AS "ValorNuevo" FROM dummy;
		        		

				CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
					 p_IdTipoLog => 'INGRESO' 
					,p_IdModulo => 'TES' 
					,p_IdUsuario => p_IdUsuario 
					,p_IdEmpresa => p_IdEmpresa
					,p_FechaSistemaLog => TO_VARCHAR (:v_fechaHoraSistema, 'YYYY-MM-DD')
					,p_IdUsuarioAtendido => p_IdUsuario
					,p_IdTipoRegistro => 'CREARINGRESOEGRESO'
					,p_IdRegistro => :v_IdIngresoEgreso 
					,p_DetalleLog => :v_DetalleLog  --hacia arriba obligatorios
					,p_NombreProceso => 'BSCL.Tesoreria.BD.Servicios::CrearIngresoEgreso' -- hacia abajo opcionales
					,p_EstadoProceso => ''
					,p_Cantidad => 0
					,p_Nemo => '');
				
				-- Crea Forma de Pago para movimiento
				INSERT INTO  "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago"(
					 "IdIngresoEgresoFormaDePago"
					,"IdIngresoEgreso"
					,"IdTipoMovimiento"
					,"IdMedioDePago"
					,"NombreMedioDePago"
					,"IdModalidadDePago"
					,"NombreModalidadDePago"
					,"IdCuentaBancariaCorredora"
					,"IdTipoCuentaBancariaCorredora"
					,"NombreTipoCuentaBancariaCorredora"
					,"NombreCuentaBancariaCorredora"
					,"NumeroCuentaBancariaCorredora"
					,"IdTarjetaBancariaCorredora"
					,"IdTipoTarjetaBancariaCorredora"
					,"NombreTipoTarjetaBancariaCorredora"
					,"NumeroTarjetaBancariaCorredora"			
					,"IdChequera"
		 			,"NumeroDocumentoCorredora"
		 			,"FechaDocumentoPagoCorredora"
					,"IdCuentaBancariaSocio"
					,"IdTipoCuentaBancariaSocio"
					,"IdBancoSocio"
					,"NombreTipoCuentaBancariaSocio"
					,"NombreCuentaBancariaSocio"
					,"NumeroCuentaBancariaSocio"
					,"IdTarjetaBancariaSocio"
					,"IdTipoTarjetaBancariaSocio"
					,"NombreTipoTarjetaBancariaSocio"
					,"NumeroTarjetaBancariaSocio"
					,"FechaVctoTarjetaBancariaSocio"
					,"NumeroSerieSocio"
		 			,"NumeroDocumentoSocio"
		 			,"FechaDocumentoPagoSocio"
					,"IdMoneda"
					,"Monto"
					,"FechaIngresoRegistro"
					,"IdEstado"
				) 
				SELECT 	 
					 "BSCL.Tesoreria.BD.Secuencias::IngresoEgresoFormaDePago".NEXTVAL
					,SIE."IdIngresoEgreso"
					,IEG."IdTipoMovimiento"			
					,FP."IdMedioDePago"
				   	,COALESCE(MeP."Nombre",'')
				   	,FP."IdModalidadDePago"
				   	,COALESCE(MoP."Nombre",'')		   	
				   	,FP."IdCuentaBancariaCorredora"
				   	,COALESCE(CBC."IdTipoCuentaBancaria",'')
				   	,COALESCE(TCBC."NombreTipoCuentaBancaria",'')
				   	,COALESCE(CBC."NombreCuentaBancaria",'')
				   	,COALESCE(CBC."NumeroCuentaBancaria",'')		   	
				   	,FP."IdTarjetaBancariaCorredora"
				   	,COALESCE(TBC."IdTipoTarjetaBancaria",'')
				   	,COALESCE(TBC."NombreTipoTarjetaBancaria",'')
				   	,COALESCE(TBC."NumeroTarjetaBancaria",'')
					,COALESCE(FP."IdChequera",0)
					,COALESCE(FP."NumeroDocumentoCorredora",0)
					,COALESCE(FP."FechaDocumentoPagoCorredora",'0001-01-01')
					,FP."IdCuentaBancariaSocio"
					,COALESCE(FP."IdTipoCuentaBancariaSocio",'')
					,COALESCE(FP."IdBancoSocio",0)
					,COALESCE(FP."NombreTipoCuentaBancariaSocio",'')
					,COALESCE(FP."NombreCuentaBancariaSocio",'')
					,COALESCE(FP."NumeroCuentaBancariaSocio",'')
				   	,FP."IdTarjetaBancariaSocio"
				   	,COALESCE(FP."IdTipoTarjetaBancariaSocio",'')
				   	,COALESCE(FP."NombreTipoTarjetaBancariaSocio",'')
				   	,COALESCE(FP."NumeroTarjetaBancariaSocio",'')
				   	,'0001-01-01'		   	
				   	,COALESCE(FP."NumeroSerieSocio",'')
					,COALESCE(FP."NumeroDocumentoSocio",0)
		 			,COALESCE(FP."FechaDocumentoPagoSocio",'0001-01-01')
					,FP."IdMoneda"
					,"BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(FP."Monto",MF."MontoDecimales",MF."MontoTrucaRedondea") AS "MontoOperacion"
					,CURRENT_UTCTIMESTAMP
					,'ING'
				FROM :v_FormaDePago as FP
				INNER JOIN :v_IngresoEgresoGenerar as IEG ON IEG."NumeroReferencia" = FP."NumeroReferencia"
				INNER JOIN :v_SecuenciaIngresoEgreso as SIE ON SIE."NumeroReferencia" = IEG."NumeroReferencia"
				INNER JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MF 
				 		 ON MF."IdMoneda" 			= FP."IdMoneda" 
				 		AND MF."IdUsoMonedaFormato" = 'GEN'
				 		AND MF."IdTipoInstrumento" 	= ''
				 		AND MF."IdTipoOperacion" 	= ''
				 LEFT OUTER JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" AS MeP ON (MeP."IdMedioDePago" = FP."IdMedioDePago")
				 LEFT OUTER JOIN "BSCL.Plataforma.BD::PAG.ModalidadDePago" AS MoP ON (MoP."IdModalidadDePago" = FP."IdModalidadDePago")
				 LEFT OUTER JOIN "BSCL.Tesoreria.BD::TES.CuentaBanco" AS CBC ON (CBC."IdCuentaBanco" = FP."IdCuentaBancariaCorredora")		 
				 LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTarjetasBancarias" AS TBC ON TBC."IdTarjetaBancaria" = FP."IdTarjetaBancariaCorredora"		 
				 LEFT OUTER JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" AS TCBC ON (TCBC."IdTipoCuentaBancaria" = CBC."IdTipoCuentaBancaria");
				
	
				INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_ResultadoProceso"					
				SELECT  'IdIngresoEgreso' as "Campo",
						"IdIngresoEgreso" as "Id",
						'BSCL.Tesoreria.BD.Servicios::CrearIngresoEgreso' as "Servicio",
						'ING' as "Resultado"
				FROM :v_SecuenciaIngresoEgreso;
							
				INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_IngresoEgresoGenerado"
				SELECT IEG."NumeroReferencia", SIE."IdIngresoEgreso", IEG."IdTipoMovimiento"
				FROM :v_IngresoEgresoGenerar AS IEG
				INNER JOIN :v_SecuenciaIngresoEgreso as SIE ON SIE."NumeroReferencia" = IEG."NumeroReferencia";
				
	
				-- Actualiza chequera de la corredora (GestionFormaDePago también lo hace, pero para Comprobantes sin FP)
				v_ChequesUtilizados =
				SELECT DISTINCT 
						FP."IdChequera",
						MIN(COALESCE(FP."NumeroDocumentoCorredora",0)) as "ChequeDesde",
						MAX(COALESCE(FP."NumeroDocumentoCorredora",0)) as "ChequeHasta",
						COALESCE(C."NumeroChequeActual",0) as "NumeroChequeActual",
						(MAX(FP."NumeroDocumentoCorredora") - MIN(FP."NumeroDocumentoCorredora") + 1) as "CantidadCheques"
			    FROM :v_FormaDePago as FP	  
				LEFT JOIN "BSCL.Tesoreria.BD::TES.Chequera" as C ON C."IdChequera" = FP."IdChequera"
				GROUP BY FP."IdChequera",C."NumeroChequeActual";			
	
	
				UPDATE CHE
				SET	 CHE."NumeroChequeActual" = (CU."ChequeHasta" + 1),
					 CHE."Remanente"		  = (CHE."Remanente" - CU."CantidadCheques")
				FROM "BSCL.Tesoreria.BD::TES.Chequera" as CHE
				INNER JOIN :v_ChequesUtilizados as CU ON CU."IdChequera" = CHE."IdChequera"
				WHERE CU."IdChequera"  > 0
				AND   CU."ChequeDesde" > 0
				AND   CU."ChequeHasta" > 0;
	
				
				----------------------------------------------------
				-- GENERAR COMPROBANTES DE LOS MOVIMIENTOS
				----------------------------------------------------
				
				
				 SELECT "IdInterno" AS "NumeroReferencia" FROM :v_RequierenAutorizacion;
				
				
				-- Obtiene movimientos para generar comprobante (Descarta los que requieren autorizacion)
				INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_IngresoEgresoGenerar"
				SELECT  SIE."NumeroReferencia", SIE."IdIngresoEgreso", '' as "TipoComprobante"
				FROM :v_SecuenciaIngresoEgreso as SIE
				INNER JOIN (
							 SELECT "NumeroReferencia" FROM :v_IngresoEgresoGenerar WHERE "GeneraComprobante" = 'S'
							 EXCEPT
							 SELECT "IdInterno" AS "NumeroReferencia" FROM :v_RequierenAutorizacion
						   ) AS IEG ON (IEG."NumeroReferencia" = SIE."NumeroReferencia");	
							   
				
				IF (NOT IS_EMPTY("BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_IngresoEgresoGenerar")) THEN
					
					CALL "BSCL.Tesoreria.BD.Servicios::CrearComprobante" (
					     p_IdEmpresa 							=> p_IdEmpresa
					    ,p_IdUsuario							=> p_IdUsuario
					    ,p_IdModulo								=> p_IdModulo
					    ,p_simularOperacion						=> p_simularOperacion
					    ,p_UsaReglasNegocio						=> 'N'
					    ,p_FechaMovimiento						=> :v_FechaMovimiento
						,p_IdCaja								=> 0
						,p_GenerarComprobanteDesdeMovimientos	=> 'S'
						,p_GenerarComprobanteDesdeComprobantes	=> 'N'				
						,p_EsIngresoAutomatico					=> 'S'
						,p_out_InformacionContador				=> p_out_InformacionContador
						,p_out_AdvertenciasContador				=> p_out_AdvertenciasContador
						,p_out_ErroresContador      			=> p_out_ErroresContador
					);
					
					INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_InformacionDetalle"							
					SELECT 0 as "NumeroReferencia", 0 as "Id","Codigo","Descripcion"
					  FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_InformacionDetalle";		
					
					INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_AdvertenciasDetalle"
					SELECT 0 as "NumeroReferencia", 0 as "Id","Codigo","RequiereAutorizacion","Descripcion" 
					  FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_AdvertenciasDetalle";
								
					INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_ErroresDetalle"
					SELECT 0 as "NumeroReferencia", 0 as "Id","Codigo","Descripcion" 
					  FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_ErroresDetalle";
					
					INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_ResultadoProceso"					
					SELECT "Campo","Id",'BSCL.Tesoreria.BD.Servicios::CrearComprobante' as "Servicio","Resultado"
					FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_ResultadoProceso";
					
					IF NOT (IS_EMPTY("BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_ErroresDetalle")) THEN
						SIGNAL SALIDA_CONTROLADA;
					END IF;								
				END IF;   -- IF (NOT IS_EMPTY("BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_IngresoEgresoGenerar"))
				
				
				
	
				------------------------------------------------------------------------------
				-- GENERA EVENTOS DE AUTORIZACION (una vez generado los movimientos) 
				------------------------------------------------------------------------------
				
				IF (p_UsaReglasNegocio = 'S') THEN
					
					IF (NOT IS_EMPTY(:v_RequierenAutorizacion)) THEN
						v_tablaEventos =				
						SELECT 
							'TESOCU' 				AS "IdTipoEvento"            --Movimiento tesoreria sobregira cuenta inversion
							,:p_IdModulo 	 		AS "IdModuloSolicita"
						    ,:p_IdUsuario	 		AS "IdUsuarioSolicita"
							,:v_FechaSistema		AS "Fecha"
							,'' 					AS "IdTipoOperacion"
							,IE."IdConcepto" 		AS "IdConcepto"
							,IE."IdIngresoEgreso" 	AS "NumeroOperacionConcepto"
							,IE."FechaMovimiento" 	AS "FechaOperacionConcepto"								    
							,0  AS "IdCliente"
							,0  AS "IdFuncionario"
							,0  AS "IdProveedor"
							,'' AS "IdTipoProducto"
							,'' AS "IdClasificacionTipoInstrumento"
							,'' AS "IdTipoInstrumento"
							,0  AS "IdInstrumento"
							,'' AS "Nemo"
							,0  AS "Cantidad"
							,IE."Monto"  AS "Monto"
							,0  AS "Precio"
							,0  AS "Tasa"
							,IE."IdMoneda" AS "IdMoneda"
							,'MOVIMIENTO TESORERIA SOBREGIRA CUENTA INVERSION (' || CAST (IE."IdIngresoEgreso" AS NVARCHAR(15)) ||')' AS "Glosa"
							,0 as "IdUnion" 
						FROM :v_RequierenAutorizacion AS RA
						INNER JOIN :v_SecuenciaIngresoEgreso AS SIE ON (SIE."NumeroReferencia" = RA."IdInterno")
						INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS IE ON (IE."IdIngresoEgreso" = SIE."IdIngresoEgreso");
						
						----------------------------------------------------------------------------------------------------------------							
				        v_tablaEventosDetalle = select  "IdEventoDetalle" as "IdUnion","NombreCampo","TipoDato","Valor","Clasificacion"
				   									from "BSCL.Plataforma.BD::AUT.EventoDetalle" 
				   									where "IdEventoDetalle" = 0;
				   		-----------------------------------------------------------------------------------------------------------------
						IF (NOT IS_EMPTY(:v_tablaEventos)) THEN
								CALL "BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacionMasivo"(
										 p_IdEmpresa 					=> p_IdEmpresa
										,p_IdUsuario 					=> p_IdUsuario
										,p_IdModulo 					=> 'TES'
										,p_tablaEventos					=> :v_tablaEventos
										,p_tablaDetalle	                => :v_tablaEventosDetalle
										,p_out_IdEvento 				=> :v_IdEvento
										,p_out_AdvertenciasContador 	=> p_out_AdvertenciasContador
										,p_out_AdvertenciasDetalle		=> v_AdvertenciasDetalle
										,p_out_ErroresContador 			=> p_out_ErroresContador
										,p_out_ErroresDetalle 			=> v_out_ErroresDetalle
								);
						
								INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_AdvertenciasDetalle"
								SELECT 0 as "NumeroReferencia", 0 as "Id","Codigo","RequiereAutorizacion","Descripcion" 
								FROM :v_AdvertenciasDetalle;
					  
								INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_ErroresDetalle"
								SELECT 0 as "NumeroReferencia", 0 as "Id","Codigo","Descripcion" 
					  			FROM :v_out_ErroresDetalle;
					  
															   
								-- Actualiza IE con Evento de solicitud de autorización
						 		UPDATE IE 
						 		SET  "IdEvento" = EVE."IdEvento",
						 			 "IdEstado" = 'PDA' -- PDA: pendiente de autorizacion
						 		FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS IE
						 		INNER JOIN :v_IdEvento AS EVE ON (IE."IdIngresoEgreso" = CAST(EVE."NumeroOperacionConcepto" AS BIGINT))
						 		AND EVE."IdEvento" > 0;
						 		
						END IF; -- IF (NOT IS_EMPLY(:v_tablaEventos))
						
					END IF; -- IF (NOT IS_EMPLY(:v_RequierenAutorizacion))
					
				END IF; -- IF (p_UsaReglasNegocio = 'S')
			
						
			ELSE   -- IF (:p_simularOperacion = 'N')
				-- Si simularOperacion=S solo se valida y se entrega la secuencia con el cual se habría generado el movimiento
				INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_ResultadoProceso"					
				SELECT  'IdIngresoEgreso' as "Campo",
						"IdIngresoEgreso" as "Id",
						'BSCL.Tesoreria.BD.Servicios::CrearIngresoEgreso' as "Servicio",
						'ING' as "Resultado"
				FROM :v_SecuenciaIngresoEgreso;
			END IF;   -- IF (:p_simularOperacion = 'N')
	
			----------------------------------------------------
			-- RESULTADO DEL PROCESO
			----------------------------------------------------
			INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Proceso_TablaCodigosMensajes"
			SELECT  SIE."NumeroReferencia" AS "IdInterno"
				   ,CASE WHEN IEG."IdTipoMovimiento" = 'MOVEGR' THEN 'UI_CREADO_MOVIMIENTO_EGRESO' 
				   		 WHEN IEG."IdTipoMovimiento" = 'MOVING' THEN 'UI_CREADO_MOVIMIENTO_INGRESO'
				   	 END as "Codigo"
				   ,'IdIngresoEgreso' as "CodigoDato1"
				   ,CAST(SIE."IdIngresoEgreso" as NVARCHAR(520)) as "ValorDato1"
				   ,'' as "CodigoDato2"
				   ,'' as "ValorDato2"
				   ,'OK' as "Resultado" 
			FROM :v_IngresoEgresoGenerar as IEG
			INNER JOIN :v_SecuenciaIngresoEgreso as SIE ON SIE."NumeroReferencia" = IEG."NumeroReferencia";
	
			DELETE FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_IngresoEgresoGenerar";
			DELETE FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_FormaDePago";
		
			---------------NOTIFICACION
	
			--INFORMACION DE REMITENTE
			SELECT CE."IdPersona", 
			CE."IdDireccionElectronica" 
			INTO v_IdPersonaRemitente, v_IdDireccionPersonaRemitente
			FROM "BSCL.Plataforma.BD::ACC.Usuario" U
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado"  CE ON CE."IdEmpleado" = U."IdEntidad"  
			WHERE "IdUsuario" = :p_IdUsuario; 
			
			--INFORMACION DE DESTINATARIO: 	
			
			t_cliente  = 
				SELECT 
				SIE."IdIngresoEgreso" AS "IdSecuencia", 
				CCl."NombreCompleto",
				CCP."IdPortafolio",
				DI."DocumentoIdentidadAsociado" AS "DNI",
				Coalesce("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(ECC."Monto", FormMvto."MontoDecimales", FormMvto."MontoTrucaRedondea"), ECC."Monto") As "Monto",
				TIM."Nombre" AS "NombreOp",
				ECC."IdMoneda",
				:v_FechaSistema AS "FechaSistema",
				COALESCE(CPT."Numero",'') AS "NumeroTelefono",
				COALESCE(CPGDEG."Direccion",'') AS "Correo",
				DI."IdTipoDocumentoIdentidadAsociado" AS "TipoDoc",
				DI."DocumentoIdentidadAsociado" AS "DI",
				CEMP."Nombre" AS "Corredora",
				CC."Nombre" AS "NombreConcepto",
				ECC."FechaLiquidacion",
				CCl."IdPersona", 
				CCl."IdDireccionElectronicaPrincipal", 
				CASE ECC."IdTipoMovimiento"
					WHEN 'MOVEGR' THEN 'EGRESO'
					ELSE 'INGRESO'
				END AS "TipoMovimiento"
				FROM "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" CCP
				INNER JOIN :v_IngresoEgresoGenerar ECC ON CCP."IdPortafolio"= ECC."IdPortafolio"
				INNER JOIN :v_SecuenciaIngresoEgreso as SIE ON SIE."NumeroReferencia" = ECC."NumeroReferencia"
				INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CCl ON CCP."IdCliente" = CCl."IdCliente"
				INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI ON DI."IdDocumentoIdentidad" = CCl."IdDocumentoIdentidad"
				LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoMovimiento" AS TIM ON (TIM."IdTipoMovimiento" = ECC."IdTipoMovimiento")
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConcepto" AS CC ON CC."IdConcepto" = ECC."IdConcepto"
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" CPT ON CPT."IdPersona"=CCl."IdPersona" AND CCl."IdTelefonoPrincipal" = CPT."IdTelefono"
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaGrupoDireccionElectronicaGlobal" CPGDEG ON CPGDEG."IdGrupo" = CCl."IdDireccionElectronicaPrincipal"
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" CEMP ON CEMP."IdEmpresa" = :p_IdEmpresa
				LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" As FormMvto
                    On FormMvto."IdMoneda" = ECC."IdMoneda" And FormMvto."IdTipoOperacion" = '' And FormMvto."IdTipoInstrumento" = '' And FormMvto."IdUsoMonedaFormato" = 'GEN';
                   
                   -- SELECT * FROM "BSCL.Plataforma.BD::PLA.MonedaFormato"
			--construir notificacion
			p_Eventos = 
				SELECT 
				'MOV_TESORERIA' AS "CodigoTipoEvento", 
				"IdSecuencia", 
				CURRENT_UTCTIMESTAMP AS "FechaCreacionSistema", 
				:v_IdPersonaRemitente AS "IdPersonaRemitente", 
				"IdPersona" AS "IdPersonaDestinatario",
				0 AS "IdPortafolio"
				FROM :t_cliente;
	
			p_IdDirecciones = 
				SELECT 
				E."IdSecuencia",
				C."IdDireccionElectronicaPrincipal" AS "IdDireccion",
				'TO' AS "TipoDestinatario"
				FROM :p_Eventos E
				INNER JOIN :t_cliente C ON E."IdSecuencia" = C."IdSecuencia"
			UNION ALL
				SELECT 
				E."IdSecuencia",
				v_IdDireccionPersonaRemitente AS "IdDireccion",
				'CC' AS "TipoDestinatario"
				FROM :p_Eventos E;
			
			p_ArchivosAdjuntos = SELECT 0 AS "IdSecuencia", null AS "ArchivoAdjunto", '' AS "Nombre", '' AS "Contrasenia" FROM DUMMY WHERE 1=2;
			
			p_EventoDetalle = SELECT "IdSecuencia", "Llave", "Valor"
		    FROM (
			SELECT * ,
			        
    			COALESCE(
    			"1" || "IdSecuencia",
                "2" || "NombreCompleto",
                "3" || "IdPortafolio",
                "4" || "DNI",
                "5" || "Monto",
                "6" || "NombreOp",
                "7" || "IdMoneda",
                "8" || "FechaSistema",
                "9" || "NumeroTelefono",
                "10" || "Correo",
                "11" || "TipoDoc",
                "12" || "DI",
                "13" || "Corredora",
                "14" || "NombreConcepto",
                "15" || "FechaLiquidacion",
                "16" || "IdPersona",
                "17" || "IdDireccionElectronicaPrincipal",
                "18" || "TipoMovimiento"
    			) AS "Valor",
    			        
    			COALESCE(
    			"1"  || 'IdSecuencia',
                "2"  || 'NombreCompleto',
                "3"  || 'IdPortafolio',
                "4"  || 'DNI',
                "5"  || 'Monto',
                "6"  || 'NombreOp',
                "7"  || 'IdMoneda',
                "8"  || 'FechaSistema',
                "9"  || 'NumeroTelefono',
                "10"  || 'Correo',
                "11"  || 'TipoDoc',
                "12"  || 'DI',
                "13"  || 'Corredora',
                "14"  || 'NombreConcepto',
                "15"  || 'FechaLiquidacion',
                "16"  || 'IdPersona',
                "17"  || 'IdDireccionElectronicaPrincipal',
                "18"  || 'TipoMovimiento'
    			) AS "Llave" 
    			FROM :t_cliente T, "BSCL.Plataforma.BD::PLA.Identidad"     
    		)
    		WHERE "Valor" IS NOT NULL;
			
	
			--Guardar eventos
			CALL "BSCL.Plataforma.BD.Servicios::EVT.AgregarEvento"(
				p_IdEmpresa => p_IdEmpresa,
				p_IdUsuario => p_IdUsuario,
				p_IdModulo => p_IdModulo,
				p_Eventos => :p_Eventos,
				p_Valores => :p_EventoDetalle,
				p_ArchivosAdjuntos => :p_ArchivosAdjuntos,
				p_IdDirecciones => :p_IdDirecciones,
				p_out_IdEvento => p_out_IdEvento
			);
						
			SIGNAL SALIDA_CONTROLADA;
			
			
		END;	
	END;
