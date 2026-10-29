SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::CrearComprobante";

CREATE PROCEDURE 
-- 
--  Proposito: Crear comprobantes de tesoreria a partir de:
--				1-	Movimientos de tesoreria
--				2-	Comprobantes de tesoreria (Ingreso, Egresos o Ambos)
--			   Permite generar Partidas de Ingreso y Partida de Egresos	
--  Autor: NRM - BCS
--  Fecha Creacion: 01/12/2015
--
-- TABLAS DE ENTRADA
-- "BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_IngresoEgresoGenerar"   cascade;
-- "BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_ComprobanteGenerar"     cascade;
-- "BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_FormaDePago"     		cascade;

-- TABLAS DE SALIDA
-- "BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_ResultadoProceso"     	cascade;
-- "BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_InformacionDetalle"     	cascade;
-- "BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_AdvertenciasDetalle"     cascade;
-- "BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_ErroresDetalle"     		cascade;
--
"BSCL.Tesoreria.BD.Servicios::CrearComprobante" (
    -- Parametros de Validacion
    IN p_IdEmpresa 								BIGINT,  
    IN p_IdUsuario 								BIGINT, 
    IN p_IdModulo 								NVARCHAR(3),    
    IN p_simularOperacion 						NVARCHAR(1)		DEFAULT 'N',	-- S/N se usa para simular operacion (algunos casos)
    IN p_UsaReglasNegocio 						NVARCHAR(1)		DEFAULT 'S',	-- S/N se usa en caso de servicios anidados.      
	IN p_FechaMovimiento						NVARCHAR(10),  
	IN p_IdCaja   								BIGINT			DEFAULT 0,      -- Caja donde se procesaran los comprobantes		
	IN p_GenerarComprobanteDesdeMovimientos		NVARCHAR(1)		DEFAULT 'N',
	IN p_GenerarComprobanteDesdeComprobantes	NVARCHAR(1)		DEFAULT 'N',
	IN p_EsIngresoAutomatico					NVARCHAR(1) 	DEFAULT 'N',    -- S: Es ingreso automatico, N: Es ingreso Manual

	OUT p_out_InformacionContador  				INT,
    OUT p_out_AdvertenciasContador 				INT,  
  	OUT p_out_ErroresContador      				INT
)  	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
  BEGIN
  	--------------------
	--DECLARE Manejo de Errores
	--------------------									
	DECLARE v_CountFormaDePago		INTEGER;
	DECLARE SALIDA_CONTROLADA 		CONDITION;	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
		IF (NOT IS_EMPTY("BSCL.Tesoreria.BD::GBL.CrearComprobante_TablaCodigosMensajes")) THEN	
			v_Mensajes = SELECT "IdInterno","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2" FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_TablaCodigosMensajes";
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_Mensajes,v_erroroutmsg);
	
			INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_InformacionDetalle"						
			SELECT EM."Codigo",EM."Descripcion" FROM :v_erroroutmsg EM INNER JOIN "BSCL.Tesoreria.BD::GBL.CrearComprobante_TablaCodigosMensajes" TCM ON TCM."IdInterno" = EM."IdInterno" AND TCM."Codigo" = EM."Codigo" WHERE TCM."Resultado" = 'OK';
			
			INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_ErroresDetalle"
			SELECT EM."Codigo",EM."Descripcion" FROM :v_erroroutmsg EM INNER JOIN "BSCL.Tesoreria.BD::GBL.CrearComprobante_TablaCodigosMensajes" TCM ON TCM."IdInterno" = EM."IdInterno" AND TCM."Codigo" = EM."Codigo" WHERE TCM."Resultado" = 'RECH';
	
			INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_AdvertenciasDetalle"
			SELECT EM."Codigo",TCM."Resultado" as "RequiereAutorizacion",EM."Descripcion" FROM :v_erroroutmsg EM INNER JOIN "BSCL.Tesoreria.BD::GBL.CrearComprobante_TablaCodigosMensajes" TCM ON TCM."IdInterno" = EM."IdInterno" AND TCM."Codigo" = EM."Codigo" WHERE TCM."Resultado" IN ('ADVE','RAUT');
		END IF;

		SELECT count(1) into p_out_InformacionContador  FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_InformacionDetalle";
		SELECT count(1) into p_out_AdvertenciasContador FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_AdvertenciasDetalle";
		SELECT count(1) into p_out_ErroresContador      FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_ErroresDetalle";
	END;
	DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299		
	BEGIN
		INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_TablaCodigosMensajes"
		SELECT 0 as "IdInterno",'DATA_NO_EXISTE' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2",'RECH' as "Resultado" FROM DUMMY;			

		SIGNAL SALIDA_CONTROLADA;
	END;
	
	p_out_InformacionContador		:= 0;
	p_out_AdvertenciasContador		:= 0;
	p_out_ErroresContador			:= 0;

	--*******************************
	--* Inicialización de variables *
	--*******************************
	
	v_IngresoEgresoGenerar = 
	SELECT  IngEgrAGenerar."NumeroReferencia",IngEgrAGenerar."IdIngresoEgreso" as "IdIngresoEgresoInformado",IngEgrAGenerar."TipoComprobante",
			IngrEgr."IdIngresoEgreso",IngrEgr."IdTipoMovimiento",IngrEgr."IdConcepto",IngrEgr."IdCaja",IngrEgr."IdMoneda",IngrEgr."Monto",
			IngrEgr."FechaLiquidacion",IngrEgr."FechaIngresoRegistro",IngrEgr."FechaMovimiento",IngrEgr."IdTipoPersona",IngrEgr."IdPersona",
			IngrEgr."IdTipoDocumentoIdentidad",IngrEgr."IdTipoPersonaTitularPago",IngrEgr."IdPersonaTitularPago",
			IngrEgr."IdPortafolioTitularPago",IngrEgr."SecuenciaAgrupacion",
			IngrEgr."SecuenciaRelacionada",IngrEgr."Glosa",IngrEgr."IdEmpleadoDigitador",IngrEgr."IdSucursalEmpleadoDigitador",IngrEgr."IdTipoComprobanteOrigen",
			IngrEgr."IdComprobanteOrigen",IngrEgr."FechaComprobanteOrigen",IngrEgr."FechaLiquidacionComprobanteOrigen",IngrEgr."IdTipoProducto",
			IngrEgr."IdTipoOperacion",IngrEgr."IdBolsa",IngrEgr."IdMonedaOperacion",IngrEgr."MontoOperacion",IngrEgr."FechaParidad",IngrEgr."IdTipoReajuste",
			IngrEgr."ValorParidad",IngrEgr."FolioCartera",IngrEgr."IdInstrumento",IngrEgr."IdTipoInstrumento",IngrEgr."IdClasificacionTipoInstrumento",
			IngrEgr."EsBilateral",IngrEgr."TitularPago",IngrEgr."EsIngresoAutomatico",IngrEgr."NeteaComprobante",IngrEgr."AgrupaComprobante",
			IngrEgr."MueveCuentaInversion",IngrEgr."IdEstado",IngrEgr."IdCondicionLiquidacion"
	  FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_IngresoEgresoGenerar" As IngEgrAGenerar
	  LEFT JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" As IngrEgr ON IngEgrAGenerar."IdIngresoEgreso" = IngrEgr."IdIngresoEgreso"
	 WHERE IngrEgr."IdEmpresa" = :p_IdEmpresa
	   FOR UPDATE OF IngrEgr."IdEstado";
	
	
	v_ComprobanteAGenerar = 
	SELECT	 COALESCE (CompAGenerar."NumeroReferencia",0) AS "NumeroReferencia" ,CompAGenerar."IdComprobante" as "IdComprobanteInformado",COALESCE (CompAGenerar."TipoComprobante",'') as "TipoComprobanteInformado"
			,Comp."IdComprobante",Comp."TipoComprobante",Comp."IdEmpresa",Comp."IdTipoMovimiento",Comp."IdConcepto",Comp."IdCaja",Comp."IdMoneda"
			,Comp."FechaLiquidacion",Comp."FechaMovimiento",Comp."IdTipoPersona",Comp."IdPersona",Comp."IdTipoDocumentoIdentidad"
			,Comp."TitularPago",Comp."IdTipoPersonaTitularPago",Comp."IdPersonaTitularPago",Comp."IdPortafolioTitularPago",Comp."EsBilateral"
			,Comp."MueveCuentaInversion",Comp."ComprobanteAgrupado",Comp."ComprobanteNeteado",Comp."SeparadoPorConcepto", Comp."IdEstado"
			,CASE WHEN Comp."IdTipoMovimiento" = 'MOVING' THEN Comp."Monto" ELSE -1*Comp."Monto" END AS "Monto", COALESCE(Comp."IdCondicionDeLiquidacion",'')  AS "IdCondicionDeLiquidacion"
			, COALESCE(Comp."Glosa", '') AS "Glosa"
	  FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_ComprobanteGenerar" As CompAGenerar
	  LEFT JOIN "BSCL.Tesoreria.BD::TES.Comprobante" As Comp ON CompAGenerar."IdComprobante" = Comp."IdComprobante"
	 WHERE Comp."IdEmpresa" = :p_IdEmpresa
	   FOR UPDATE OF Comp."IdEstado";
	
	
	v_FormasDePago	=
	SELECT  FP.*, MeP."NombreMedioDePago",MeP."NombreModalidadDePago",
			IbC."IdPersona",IbC."NombreCompleto",IbC."DocumentoIdentidadDefecto",IbC."CodigoTipoPersona",
			IbC."IdCuentaBanco",IbC."IdBanco",IbC."IdPlaza",IbC."PlazoRetencionMismoBanco",IbC."PlazoRetencionOtroBanco",
			IbC."NombreCuentaBancaria",IbC."NumeroCuentaBancaria",IbC."IdTipoCuentaBancaria",IbC."NombreTipoCuentaBancaria",
			TBC."IdTipoTarjetaBancaria",TBC."NombreTipoTarjetaBancaria",TBC."NumeroTarjetaBancaria"
	  FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_FormaDePago" AS FP
	  LEFT JOIN (SELECT MeP."IdMedioDePago",MeP."Nombre" as "NombreMedioDePago",MoP."IdModalidadDePago", MoP."Nombre" as "NombreModalidadDePago"
				  FROM "BSCL.Plataforma.BD::PAG.MedioDePago" AS MeP
				 INNER JOIN "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" AS MePMoP ON (MePMoP."IdMedioDePago" = MeP."IdMedioDePago")
				 INNER JOIN "BSCL.Plataforma.BD::PAG.ModalidadDePago" AS MoP ON (MoP."IdModalidadDePago" = MePMoP."IdModalidadDePago")
	  			) AS MeP
	  		 ON MeP."IdMedioDePago" = FP."IdMedioDePago"
	  LEFT JOIN (SELECT CB."IdPersona",SN."NombreCompleto",SN."DocumentoIdentidadDefecto",SN."CodigoTipoPersona",
					    CB."IdCuentaBanco",CB."IdBanco",CB."IdPlaza",CB."PlazoRetencionMismoBanco",CB."PlazoRetencionOtroBanco",
					    CB."NombreCuentaBancaria",CB."NumeroCuentaBancaria",CB."IdTipoCuentaBancaria",TCBC."NombreTipoCuentaBancaria"
				   FROM "BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias" as CB
				  INNER JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" AS TCBC ON (TCBC."IdTipoCuentaBancaria" = CB."IdTipoCuentaBancaria")
				  INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.UIConsultarSocioDeNegocio" as SN ON SN."IdPersona" = CB."IdPersona"
				  WHERE SN."IdEmpresa" = :p_IdEmpresa
	  			) AS IbC
	  		 ON IbC."IdCuentaBanco" = FP."IdCuentaBancariaCorredora"
	  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTarjetasBancarias" AS TBC 
			 ON TBC."IdTarjetaBancaria" = FP."IdTarjetaBancariaCorredora";	  
	SELECT COUNT(1) INTO v_CountFormaDePago FROM :v_FormasDePago;
	
	--------------------
	--VALIDACIONES
	--------------------	   
	IF p_UsaReglasNegocio = 'S' THEN
	BEGIN
		DECLARE v_resultadoValidacion	NVARCHAR(40);
		
		IF p_EsIngresoAutomatico = 'N' THEN		
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_out_ErroresDetalle,:v_resultadoValidacion);
			IF :v_resultadoValidacion = 'RECH' THEN			
				INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_TablaCodigosMensajes"
				SELECT 0 as "IdInterno","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2",'RECH' as "Resultado" FROM :v_out_ErroresDetalle;				
				SIGNAL SALIDA_CONTROLADA;
			END IF;
		END IF;

		IF (:p_GenerarComprobanteDesdeMovimientos = 'S' AND IS_EMPTY(:v_IngresoEgresoGenerar)) OR (:p_GenerarComprobanteDesdeComprobantes = 'S' AND IS_EMPTY(:v_ComprobanteAGenerar)) THEN
			INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_TablaCodigosMensajes"
			SELECT 0 as "IdInterno",'NO_VIENEN_MOVIMIENTOS' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2",'RECH' as "Resultado" 
			  FROM :v_out_ErroresDetalle;				
			SIGNAL SALIDA_CONTROLADA;
		END IF;

				
		INSERT INTO "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Entrada_IngresoEgresoGenerar"
		("NumeroReferencia","IdIngresoEgresoInformado","TipoComprobante",
		"IdIngresoEgreso","IdTipoMovimiento","IdConcepto","IdCaja","IdMoneda","Monto",
		"FechaLiquidacion","FechaIngresoRegistro","FechaMovimiento","IdTipoPersona","IdPersona",
		"IdTipoDocumentoIdentidad","IdTipoPersonaTitularPago","IdPersonaTitularPago",
		"IdPortafolioTitularPago","SecuenciaAgrupacion",
		"SecuenciaRelacionada","Glosa","IdEmpleadoDigitador","IdSucursalEmpleadoDigitador","IdTipoComprobanteOrigen",
		"IdComprobanteOrigen","FechaComprobanteOrigen","FechaLiquidacionComprobanteOrigen","IdTipoProducto",
		"IdTipoOperacion","IdBolsa","IdMonedaOperacion","MontoOperacion","FechaParidad","IdTipoReajuste",
		"ValorParidad","FolioCartera","IdInstrumento","IdTipoInstrumento","IdClasificacionTipoInstrumento",
		"EsBilateral","TitularPago","EsIngresoAutomatico","NeteaComprobante","AgrupaComprobante",
		"MueveCuentaInversion","IdEstado") 
		SELECT "NumeroReferencia","IdIngresoEgresoInformado","TipoComprobante",
		"IdIngresoEgreso","IdTipoMovimiento","IdConcepto","IdCaja","IdMoneda","Monto",
		"FechaLiquidacion","FechaIngresoRegistro","FechaMovimiento","IdTipoPersona","IdPersona",
		"IdTipoDocumentoIdentidad","IdTipoPersonaTitularPago","IdPersonaTitularPago",
		"IdPortafolioTitularPago","SecuenciaAgrupacion",
		"SecuenciaRelacionada","Glosa","IdEmpleadoDigitador","IdSucursalEmpleadoDigitador","IdTipoComprobanteOrigen",
		"IdComprobanteOrigen","FechaComprobanteOrigen","FechaLiquidacionComprobanteOrigen","IdTipoProducto",
		"IdTipoOperacion","IdBolsa","IdMonedaOperacion","MontoOperacion","FechaParidad","IdTipoReajuste",
		"ValorParidad","FolioCartera","IdInstrumento","IdTipoInstrumento","IdClasificacionTipoInstrumento",
		"EsBilateral","TitularPago","EsIngresoAutomatico","NeteaComprobante","AgrupaComprobante",
		"MueveCuentaInversion","IdEstado" FROM :v_IngresoEgresoGenerar;
		INSERT INTO "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Entrada_ComprobanteGenerar"(
		     "NumeroReferencia","IdComprobanteInformado","TipoComprobanteInformado"
			,"IdComprobante","TipoComprobante","IdEmpresa","IdTipoMovimiento","IdConcepto","IdCaja","IdMoneda"
			,"FechaLiquidacion","FechaMovimiento","IdTipoPersona","IdPersona","IdTipoDocumentoIdentidad"
			,"TitularPago","IdTipoPersonaTitularPago","IdPersonaTitularPago","IdPortafolioTitularPago","EsBilateral"
			,"MueveCuentaInversion","ComprobanteAgrupado","ComprobanteNeteado","SeparadoPorConcepto", "IdEstado"
			,"Monto") SELECT "NumeroReferencia","IdComprobanteInformado","TipoComprobanteInformado"
			,"IdComprobante","TipoComprobante","IdEmpresa","IdTipoMovimiento","IdConcepto","IdCaja","IdMoneda"
			,"FechaLiquidacion","FechaMovimiento","IdTipoPersona","IdPersona","IdTipoDocumentoIdentidad"
			,"TitularPago","IdTipoPersonaTitularPago","IdPersonaTitularPago","IdPortafolioTitularPago","EsBilateral"
			,"MueveCuentaInversion","ComprobanteAgrupado","ComprobanteNeteado","SeparadoPorConcepto", "IdEstado"
			,"Monto" FROM :v_ComprobanteAGenerar;
		INSERT INTO "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Entrada_FormasDePagoGenerar"(
			"NumeroReferencia","IdMedioDePago","IdModalidadDePago","IdMoneda","Monto","IdCuentaBancariaCorredora","IdTarjetaBancariaCorredora","IdChequera","NumeroDocumentoCorredora","FechaDocumentoPagoCorredora",
			"IdCuentaBancariaSocio","IdBancoSocio","NombreCuentaBancariaSocio","NumeroCuentaBancariaSocio","IdTipoCuentaBancariaSocio","NombreTipoCuentaBancariaSocio","IdTarjetaBancariaSocio","IdTipoTarjetaBancariaSocio",
			"NombreTipoTarjetaBancariaSocio","NumeroTarjetaBancariaSocio","NumeroSerieSocio","NumeroDocumentoSocio","FechaDocumentoPagoSocio","NombreMedioDePago","NombreModalidadDePago",
			"IdPersona","NombreCompleto","DocumentoIdentidadDefecto","CodigoTipoPersona",
			"IdCuentaBanco","IdBanco","IdPlaza","PlazoRetencionMismoBanco","PlazoRetencionOtroBanco",
			"NombreCuentaBancaria","NumeroCuentaBancaria","IdTipoCuentaBancaria","NombreTipoCuentaBancaria",
			"IdTipoTarjetaBancaria","NombreTipoTarjetaBancaria","NumeroTarjetaBancaria") 
		SELECT "NumeroReferencia","IdMedioDePago","IdModalidadDePago","IdMoneda","Monto","IdCuentaBancariaCorredora","IdTarjetaBancariaCorredora","IdChequera","NumeroDocumentoCorredora","FechaDocumentoPagoCorredora",
			"IdCuentaBancariaSocio","IdBancoSocio","NombreCuentaBancariaSocio","NumeroCuentaBancariaSocio","IdTipoCuentaBancariaSocio","NombreTipoCuentaBancariaSocio","IdTarjetaBancariaSocio","IdTipoTarjetaBancariaSocio",
			"NombreTipoTarjetaBancariaSocio","NumeroTarjetaBancariaSocio","NumeroSerieSocio","NumeroDocumentoSocio","FechaDocumentoPagoSocio","NombreMedioDePago","NombreModalidadDePago",
			"IdPersona","NombreCompleto","DocumentoIdentidadDefecto","CodigoTipoPersona",
			"IdCuentaBanco","IdBanco","IdPlaza","PlazoRetencionMismoBanco","PlazoRetencionOtroBanco",
			"NombreCuentaBancaria","NumeroCuentaBancaria","IdTipoCuentaBancaria","NombreTipoCuentaBancaria",
			"IdTipoTarjetaBancaria","NombreTipoTarjetaBancaria","NumeroTarjetaBancaria" FROM :v_FormasDePago;
			
		CALL "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosCrearComprobantes"(
			p_FechaMovimiento						=>	:p_FechaMovimiento,
			p_IdCaja								=>	:p_IdCaja,
			p_GenerarComprobanteDesdeMovimientos	=>	:p_GenerarComprobanteDesdeMovimientos,
			p_GenerarComprobanteDesdeComprobantes	=>	:p_GenerarComprobanteDesdeComprobantes,
			p_ValidaComprobanteGenerado				=>	'N'
		);
		INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_TablaCodigosMensajes"
		SELECT "NumeroReferencia" AS "IdInterno","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2","Resultado" 
		  FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Salida_Resultado"
		 WHERE "Resultado" <> 'OK';
		
		--***************************************************************
		--* Se eliminan los movimientos que no pasaron las validaciones *
		--***************************************************************
		v_IngresoEgresoGenerar =
	 	SELECT IEG.* 
	 	  FROM :v_IngresoEgresoGenerar as IEG
	 	 INNER JOIN (SELECT "NumeroReferencia" FROM :v_IngresoEgresoGenerar EXCEPT SELECT "NumeroReferencia" FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Salida_Resultado" WHERE "Resultado" = 'RECH') as R
	 	 		 ON R."NumeroReferencia" = IEG."NumeroReferencia";
	 	
	 	v_ComprobanteAGenerar =
	 	SELECT CG.* 
	 	  FROM :v_ComprobanteAGenerar as CG
	 	 INNER JOIN (SELECT "NumeroReferencia" FROM :v_ComprobanteAGenerar EXCEPT SELECT "NumeroReferencia" FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Salida_Resultado" WHERE "Resultado" = 'RECH') as R
	 	 		 ON R."NumeroReferencia" = CG."NumeroReferencia";
	 	 	
	 	v_FormasDePago =
	 	SELECT FPG.* 
	 	  FROM :v_FormasDePago as FPG
	 	 INNER JOIN (SELECT "NumeroReferencia" FROM :v_FormasDePago EXCEPT SELECT "NumeroReferencia" FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Salida_Resultado" WHERE "Resultado" = 'RECH') as R
	 	 		 ON R."NumeroReferencia" = FPG."NumeroReferencia";
		
		IF (:p_GenerarComprobanteDesdeMovimientos = 'S' AND IS_EMPTY(:v_IngresoEgresoGenerar)) OR (:p_GenerarComprobanteDesdeComprobantes = 'S' AND IS_EMPTY(:v_ComprobanteAGenerar)) THEN
	  		SIGNAL SALIDA_CONTROLADA;
	  	END IF;		
	END; 
	END IF; 
	
	--------------------
	--LOGICA
	--------------------
	BEGIN
		DECLARE v_CountError BIGINT;

		--*****************************************
		--* Recupera Informacion base del proceso *
		--*****************************************
		v_ConfiguracionTesoreria =
		SELECT "SEPARAPORCONCEPTO","SEPARAPORAGENTE","SEPARAPORSUCURSAL","SEPARAPORCORREDORACONTRAPARTE","SEPARAPORTIPOCOMPROBANTE",
			   "SEPARAPORTIPOPRODUCTO","SEPARAPORTIPOOPERACION","SEPARAPORBOLSA","SEPARAPORCONDICIONLIQUIDACION"
		  FROM "BSCL.Tesoreria.BD.Vistas::ConsultarConfiguracionTesoreriaPivot"
		 WHERE "IdEmpresa" = :p_IdEmpresa;

		v_InformacionPersona =
		SELECT "IdPersona","NombreCompleto" as "NombrePersona","DocumentoIdentidad"
		  FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona";
	
		v_InformacionCorredora = 
		SELECT CSN."IdPersona",CC."NombreCompleto",CC."Tipo",CC."DocumentoIdentidad" --,CP."IdPortafolio"
		  FROM "BSCL.Plataforma.BD::PLA.Empresa" AS E
		 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS CSN ON CSN."IdSocioDeNegocio" = E."IdSocioDeNegocio"
		 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CC ON CC."IdCliente" = CSN."IdCliente"
	 	 WHERE E."IdEmpresa" = :p_IdEmpresa;
	 	 		 			
		v_InformacionBancoCorredora = 
		SELECT DISTINCT CB."IdPersona",SN."NombreCompleto",SN."DocumentoIdentidadDefecto",SN."CodigoTipoPersona",
			   CB."IdCuentaBanco",CB."IdBanco",CB."IdPlaza",CB."PlazoRetencionMismoBanco",CB."PlazoRetencionOtroBanco",
			   CB."NombreCuentaBancaria",CB."NumeroCuentaBancaria",CB."IdTipoCuentaBancaria",TCBC."NombreTipoCuentaBancaria"
		  FROM "BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias" as CB
		 INNER JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" AS TCBC ON (TCBC."IdTipoCuentaBancaria" = CB."IdTipoCuentaBancaria")
		 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.UIConsultarSocioDeNegocio" as SN ON SN."IdPersona" = CB."IdPersona"
		 WHERE SN."IdEmpresa" = :p_IdEmpresa;
			 
		v_CajaMonedaUsuario =
		SELECT "IdCaja","IdMoneda","IdTipoMovimiento"
		  FROM ( 
				SELECT MAX("IdCaja") over (partition by "IdMoneda") as "MaxIdCaja","IdCaja","IdMoneda","IdTipoMovimiento"
			      FROM "BSCL.Tesoreria.BD.Vistas::ConsultarCajaMonedaUsuario"
			     WHERE "IdEmpresa" = :p_IdEmpresa 		       
			       AND "IdUsuario" = :p_IdUsuario
			       AND "IdEstadoCaja" = 'ABT'
			       AND "IdEstadoCajaMoneda" = 'ACT'
			       AND "IdEstadoCajaUsuario" = 'ACT'
			       AND "FechaInicio" <= :p_FechaMovimiento
				   AND "FechaTermino" >= :p_FechaMovimiento ) AS T
		 WHERE "MaxIdCaja" = "IdCaja";
		
		
		IF (upper(:p_GenerarComprobanteDesdeMovimientos) = 'S') THEN
			--***************************************
			--* Recupera los movimientos a procesar *
			--***************************************
			v_MovimientosAProcesar = 
			SELECT  IngEgrAGenerar."IdIngresoEgreso",IngEgrAGenerar."IdTipoMovimiento",IngEgrAGenerar."IdConcepto",IngEgrAGenerar."IdCaja",IngEgrAGenerar."IdMoneda",
					IngEgrAGenerar."Monto",IngEgrAGenerar."FechaLiquidacion",IngEgrAGenerar."FechaIngresoRegistro",IngEgrAGenerar."FechaMovimiento",IngEgrAGenerar."IdTipoPersona",
					IngEgrAGenerar."IdPersona",Per."NombrePersona",IngEgrAGenerar."IdTipoDocumentoIdentidad",Per."DocumentoIdentidad",IngEgrAGenerar."IdTipoPersonaTitularPago",
					IngEgrAGenerar."IdPersonaTitularPago",PerTP."NombrePersona" as "NombrePersonaTitularPago",IngEgrAGenerar."IdPortafolioTitularPago",IngEgrAGenerar."SecuenciaAgrupacion",
					IngEgrAGenerar."SecuenciaRelacionada",IngEgrAGenerar."Glosa",IngEgrAGenerar."IdEmpleadoDigitador",IngEgrAGenerar."IdSucursalEmpleadoDigitador",
					IngEgrAGenerar."IdTipoComprobanteOrigen",IngEgrAGenerar."IdComprobanteOrigen",IngEgrAGenerar."FechaComprobanteOrigen",IngEgrAGenerar."FechaLiquidacionComprobanteOrigen",
					IngEgrAGenerar."IdTipoProducto",IngEgrAGenerar."IdTipoOperacion",IngEgrAGenerar."IdBolsa",IngEgrAGenerar."IdMonedaOperacion",IngEgrAGenerar."MontoOperacion",
					IngEgrAGenerar."FechaParidad",IngEgrAGenerar."IdTipoReajuste",IngEgrAGenerar."ValorParidad",IngEgrAGenerar."FolioCartera",IngEgrAGenerar."IdInstrumento",
					IngEgrAGenerar."IdTipoInstrumento",IngEgrAGenerar."IdClasificacionTipoInstrumento",IngEgrAGenerar."EsBilateral",IngEgrAGenerar."TitularPago",IngEgrAGenerar."EsIngresoAutomatico",
					IngEgrAGenerar."NeteaComprobante",IngEgrAGenerar."AgrupaComprobante",IngEgrAGenerar."MueveCuentaInversion",IngEgrAGenerar."NumeroReferencia",IngEgrAGenerar."TipoComprobante",IngEgrAGenerar."IdCondicionLiquidacion"
			  FROM :v_IngresoEgresoGenerar As IngEgrAGenerar
			 INNER JOIN :v_InformacionPersona AS Per ON Per."IdPersona" = IngEgrAGenerar."IdPersona"
			 INNER JOIN :v_InformacionPersona AS PerTP ON PerTP."IdPersona" = IngEgrAGenerar."IdPersonaTitularPago"
			 WHERE IngEgrAGenerar."IdEstado" = 'ING';
			   
				   
			--********************************************************************************************************
			--* Relaciona cada movimiento con la configuracion de tesoreria, para generar los grupos de comprobantes *
			--********************************************************************************************************
			
			v_TABLAPASO = SELECT  IngrEgr."NumeroReferencia",
			  				IngrEgr."TipoComprobante",
							CASE WHEN LENGTH(IngrEgr."TipoComprobante") = 0 THEN IngrEgr."IdPersonaTitularPago" END AS "IdPersonaTitularPago",
							CASE WHEN LENGTH(IngrEgr."TipoComprobante") = 0 THEN IngrEgr."IdPortafolioTitularPago" END AS "IdPortafolioTitularPago",       
			                IngrEgr."FechaLiquidacion",
			                IngrEgr."IdMoneda",			                                    
			                IngrEgr."SecuenciaAgrupacion",
			                IngrEgr."MueveCuentaInversion",
			                CASE WHEN p_IdCaja = 0 THEN IngrEgr."IdCaja" End  as "IdCaja",
			                CASE WHEN (LENGTH(IngrEgr."SecuenciaAgrupacion") = 0 AND LENGTH(IngrEgr."TipoComprobante") = 0) 											THEN IngrEgr."NeteaComprobante" 			END "NeteaComprobante",
							CASE WHEN (LENGTH(IngrEgr."SecuenciaAgrupacion") = 0 AND LENGTH(IngrEgr."TipoComprobante") = 0 AND IngrEgr."NeteaComprobante" = 'N') 		THEN IngrEgr."IdTipoMovimiento" 			END "IdTipoMovimiento",
							CASE WHEN (LENGTH(IngrEgr."SecuenciaAgrupacion") = 0 AND LENGTH(IngrEgr."TipoComprobante") = 0 AND IngrEgr."AgrupaComprobante" = 'N') 		THEN IngrEgr."AgrupaComprobante" 			END "AgrupaComprobante",
							CASE WHEN (LENGTH(IngrEgr."SecuenciaAgrupacion") = 0 AND LENGTH(IngrEgr."TipoComprobante") = 0 AND IngrEgr."AgrupaComprobante" = 'N') 		THEN IngrEgr."IdIngresoEgreso" 				END "IdInternoTes",												
							CASE WHEN (LENGTH(IngrEgr."SecuenciaAgrupacion") = 0 AND LENGTH(IngrEgr."TipoComprobante") = 0 AND Config."SEPARAPORCONCEPTO"  = 'S') 		THEN IngrEgr."IdConcepto" 					END "IdConcepto",
							CASE WHEN (LENGTH(IngrEgr."SecuenciaAgrupacion") = 0 AND LENGTH(IngrEgr."TipoComprobante") = 0 AND Config."SEPARAPORAGENTE" = 'S') 			THEN IngrEgr."IdEmpleadoDigitador" 			END "IdEmpleadoDigitador",
							CASE WHEN (LENGTH(IngrEgr."SecuenciaAgrupacion") = 0 AND LENGTH(IngrEgr."TipoComprobante") = 0 AND Config."SEPARAPORSUCURSAL" = 'S') 		THEN IngrEgr."IdSucursalEmpleadoDigitador"	END "IdSucursalEmpleadoDigitador",							
					 		CASE WHEN (LENGTH(IngrEgr."SecuenciaAgrupacion") = 0 AND LENGTH(IngrEgr."TipoComprobante") = 0 AND Config."SEPARAPORTIPOCOMPROBANTE" = 'S') THEN IngrEgr."IdTipoComprobanteOrigen"	 	END "IdTipoComprobanteOrigen",
					 		CASE WHEN (LENGTH(IngrEgr."SecuenciaAgrupacion") = 0 AND LENGTH(IngrEgr."TipoComprobante") = 0 AND Config."SEPARAPORTIPOPRODUCTO" = 'S') 	THEN IngrEgr."IdTipoProducto" 				END "IdTipoProducto",
					 		CASE WHEN (LENGTH(IngrEgr."SecuenciaAgrupacion") = 0 AND LENGTH(IngrEgr."TipoComprobante") = 0 AND Config."SEPARAPORTIPOOPERACION" = 'S') 	THEN IngrEgr."IdTipoOperacion" 				END "IdTipoOperacion",
					 		CASE WHEN (LENGTH(IngrEgr."SecuenciaAgrupacion") = 0 AND LENGTH(IngrEgr."TipoComprobante") = 0 AND Config."SEPARAPORBOLSA" = 'S') 			THEN IngrEgr."IdBolsa" 						END "IdBolsa",
                            IngrEgr."IdCondicionLiquidacion" as "IdCondicionDeLiquidacion",
                            IngrEgr."Glosa"
					  FROM :v_MovimientosAProcesar AS IngrEgr
					 INNER JOIN :v_ConfiguracionTesoreria AS Config ON 1=1;
			
			INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_GruposDeComprobantes"
			SELECT  RANK() over 
						(order by	"NumeroReferencia",
									"TipoComprobante",
									"IdPersonaTitularPago",
									"IdPortafolioTitularPago",
									"FechaLiquidacion",
									"IdMoneda", 
					                "IdCaja", 					                
					                "SecuenciaAgrupacion",
					                "MueveCuentaInversion",
					                "NeteaComprobante",
					                "IdTipoMovimiento",
					                "AgrupaComprobante",	
					                "IdInternoTes",
					                "IdConcepto",
					                "IdEmpleadoDigitador",
					                "IdSucursalEmpleadoDigitador",
					                "IdTipoComprobanteOrigen",
					                "IdTipoProducto",
					                "IdTipoOperacion",
					                "IdBolsa",
                                    "IdCondicionDeLiquidacion",
                                    "Glosa"
                                   ) AS "Grupo",
					"BSCL.Tesoreria.BD.Secuencias::Comprobante".NEXTVAL AS "IdComprobante",
					"NumeroReferencia",
					"TipoComprobante",
					"IdPersonaTitularPago",
					"IdPortafolioTitularPago",
					"IdMoneda", 
	                "IdCaja", 
	                "FechaLiquidacion",
	                "SecuenciaAgrupacion",
	                "MueveCuentaInversion",
	                "NeteaComprobante",
					"IdTipoMovimiento",
					"AgrupaComprobante",
					"IdInternoTes",
					"IdConcepto",
					"IdEmpleadoDigitador",
					"IdSucursalEmpleadoDigitador",
			 		"IdTipoComprobanteOrigen",
			 		"IdTipoProducto",
			 		"IdTipoOperacion",
			 		"IdBolsa",
			 		"IdCondicionDeLiquidacion",
			 		COALESCE("Glosa", '') AS "Glosa"
			  FROM :v_TABLAPASO
			 GROUP BY "NumeroReferencia","TipoComprobante","IdPersonaTitularPago","IdPortafolioTitularPago","IdMoneda","IdCaja","FechaLiquidacion",
			 	"SecuenciaAgrupacion","MueveCuentaInversion","NeteaComprobante","IdConcepto","IdTipoMovimiento","AgrupaComprobante","IdInternoTes",
			 	"IdEmpleadoDigitador","IdSucursalEmpleadoDigitador","IdTipoComprobanteOrigen","IdTipoProducto","IdTipoOperacion","IdBolsa","IdCondicionDeLiquidacion", "Glosa";
			
			v_MovimientosNoAgrupables = 
			SELECT "IdInternoTes","Grupo","IdComprobante" 
			  FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_GruposDeComprobantes" WHERE COALESCE("IdInternoTes",0) > 0;
			
			v_MovimientosAgrupables = 
			SELECT * FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_GruposDeComprobantes" WHERE "IdInternoTes" IS NULL;		 	
		 	
		 	v_MovimientoEnGruposComprobantes =
			SELECT  IEA."Grupo",IEA."IdComprobante",
					ROW_NUMBER() over (Partition by
											IE."NumeroReferencia",											
					 						IE."IdPersonaTitularPago",
					 						IE."IdPortafolioTitularPago",
											IE."IdMoneda",
							                IE."FechaLiquidacion",
							                IE."SecuenciaAgrupacion",
							                IE."MueveCuentaInversion",
							                IEA."TipoComprobante",
							                IEA."IdCaja",
							                IEA."NeteaComprobante",
							                IEA."IdTipoMovimiento",
							                IEA."AgrupaComprobante",	
					                		IEA."IdInternoTes",					                	
							                IEA."IdConcepto",
							                IEA."IdEmpleadoDigitador",
							                IEA."IdSucursalEmpleadoDigitador",
							                IEA."IdTipoComprobanteOrigen",
							                IEA."IdTipoProducto",
							                IEA."IdTipoOperacion",
							                IEA."IdBolsa",
                                            IE."IdCondicionLiquidacion"
                                            
							    		) as "NroLinea",
					IE."IdIngresoEgreso",CASE WHEN IEA."NeteaComprobante" = 'S' THEN '' ELSE IE."IdTipoMovimiento" END AS "IdTipoMovimiento",
					IE."IdTipoMovimiento" AS "IdTipoMovimientoDetalle", IE."IdConcepto",
					CASE WHEN p_IdCaja = 0 THEN (CASE WHEN COALESCE(IE."IdCaja",0) = 0 THEN CMU."IdCaja" ELSE IE."IdCaja" END) ELSE p_IdCaja END as "IdCaja",
					IE."IdMoneda",CASE WHEN IE."IdTipoMovimiento" = 'MOVEGR' THEN CAST((-1*IE."Monto") AS DECIMAL(26,8)) ELSE IE."Monto" END AS "Monto",IE."FechaLiquidacion",
					IE."FechaIngresoRegistro",IE."FechaMovimiento",

					CASE WHEN IE."TipoComprobante" = 'PARING' THEN '' 							 WHEN IE."TipoComprobante" = 'PAREGR' THEN 'CNT' ELSE IE."IdTipoPersona" END AS "IdTipoPersona",
					CASE WHEN IE."TipoComprobante" = 'PARING' THEN InfoCorr."IdPersona" 		 WHEN IE."TipoComprobante" = 'PAREGR' THEN InfoBanco."IdPersona" ELSE IE."IdPersona" END AS "IdPersona",					
					CASE WHEN IE."TipoComprobante" = 'PARING' THEN InfoCorr."NombreCompleto" 	 WHEN IE."TipoComprobante" = 'PAREGR' THEN InfoBanco."NombreCompleto" ELSE IE."NombrePersona" END AS "NombrePersona",
					CASE WHEN IE."TipoComprobante" = 'PARING' THEN InfoCorr."Tipo" 				 WHEN IE."TipoComprobante" = 'PAREGR' THEN '' ELSE IE."IdTipoDocumentoIdentidad" END AS "IdTipoDocumentoIdentidad",
					CASE WHEN IE."TipoComprobante" = 'PARING' THEN InfoCorr."DocumentoIdentidad" WHEN IE."TipoComprobante" = 'PAREGR' THEN InfoBanco."DocumentoIdentidadDefecto" ELSE IE."DocumentoIdentidad" END AS "DocumentoIdentidad",
					CASE WHEN IE."TipoComprobante" = 'PARING' THEN '' 							 WHEN IE."TipoComprobante" = 'PAREGR' THEN 'CNT' ELSE IE."IdTipoPersonaTitularPago" END AS "IdTipoPersonaTitularPago",
					CASE WHEN IE."TipoComprobante" = 'PARING' THEN InfoCorr."IdPersona" 		 WHEN IE."TipoComprobante" = 'PAREGR' THEN InfoBanco."IdPersona" ELSE IE."IdPersonaTitularPago" END AS "IdPersonaTitularPago",
					CASE WHEN IE."TipoComprobante" = 'PARING' THEN InfoCorr."NombreCompleto" 	 WHEN IE."TipoComprobante" = 'PAREGR' THEN InfoBanco."NombreCompleto" ELSE IE."NombrePersonaTitularPago" END AS "NombrePersonaTitularPago",
					CASE WHEN IE."TipoComprobante" = 'PARING' THEN 0 							 WHEN IE."TipoComprobante" = 'PAREGR' THEN 0 ELSE IE."IdPortafolioTitularPago" END AS "IdPortafolioTitularPago",
					
					IE."SecuenciaAgrupacion",IE."SecuenciaRelacionada",IE."MueveCuentaInversion",IE."IdEmpleadoDigitador",IE."IdSucursalEmpleadoDigitador",IE."EsBilateral",					
					CASE WHEN IE."TipoComprobante" = 'PAREGR' THEN 'S' ELSE IE."TitularPago" END AS "TitularPago",
					IE."IdTipoComprobanteOrigen",IE."IdComprobanteOrigen",IE."FechaComprobanteOrigen",IE."FechaLiquidacionComprobanteOrigen",
					IE."IdTipoProducto",IE."IdTipoOperacion",IE."IdBolsa",IE."IdMonedaOperacion",IE."MontoOperacion",IE."FechaParidad",IE."IdTipoReajuste",IE."ValorParidad",
					IE."FolioCartera",IE."IdInstrumento",IE."IdTipoInstrumento",IE."IdClasificacionTipoInstrumento",IE."AgrupaComprobante" AS "ComprobanteAgrupado",
					IE."NeteaComprobante" AS "ComprobanteNeteado",IE."NumeroReferencia",IE."TipoComprobante",
                    IE."IdCondicionLiquidacion",
                    COALESCE(IE."Glosa", '') AS "Glosa"
			  FROM :v_MovimientosAProcesar AS IE
			 INNER JOIN :v_MovimientosAgrupables AS IEA ON (   IE."NumeroReferencia"		= IEA."NumeroReferencia"
														   AND IE."IdMoneda" 				= IEA."IdMoneda"
														   AND IE."FechaLiquidacion"		= IEA."FechaLiquidacion"
														   AND IE."MueveCuentaInversion"	= IEA."MueveCuentaInversion"
														   AND IE."SecuenciaAgrupacion" 	= IEA."SecuenciaAgrupacion")
			  LEFT JOIN :v_CajaMonedaUsuario AS CMU ON CMU."IdMoneda" = IE."IdMoneda" 
			  									   AND (CMU."IdTipoMovimiento" = IE."IdTipoMovimiento" OR  CMU."IdTipoMovimiento" = 'TODOS')
			  									   
		      LEFT JOIN (SELECT DISTINCT FP."NumeroReferencia", IB.* -- Solo para partidas de egreso
		      			 FROM :v_FormasDePago AS FP 
		      			 LEFT JOIN :v_InformacionBancoCorredora AS IB ON IB."IdCuentaBanco" = FP."IdCuentaBancariaCorredora"
		      			) AS InfoBanco
		      		 ON  (InfoBanco."NumeroReferencia" = IE."NumeroReferencia" AND IE."TipoComprobante" = 'PAREGR')
		      		 
		     INNER JOIN :v_InformacionCorredora AS InfoCorr ON 1=1
			 WHERE IE."IdIngresoEgreso" NOT IN (select "IdIngresoEgreso" FROM :v_MovimientosNoAgrupables)
			   AND IE."IdPersonaTitularPago" 			= COALESCE(IEA."IdPersonaTitularPago",IE."IdPersonaTitularPago")
			   AND IE."IdPortafolioTitularPago" 		= COALESCE(IEA."IdPortafolioTitularPago",IE."IdPortafolioTitularPago")
			   AND IE."IdCaja" 							= COALESCE(IEA."IdCaja",IE."IdCaja")
			   AND IE."NeteaComprobante"				= COALESCE(IEA."NeteaComprobante",IE."NeteaComprobante")
			   AND IE."IdConcepto" 						= COALESCE(IEA."IdConcepto",IE."IdConcepto")
			   AND IFNULL(IE."IdEmpleadoDigitador" , -1) = COALESCE(IEA."IdEmpleadoDigitador", IE."IdEmpleadoDigitador", -1)
			   AND IE."IdSucursalEmpleadoDigitador" 	= COALESCE(IEA."IdSucursalEmpleadoDigitador",IE."IdSucursalEmpleadoDigitador")
			   AND IE."IdTipoComprobanteOrigen" 		= COALESCE(IEA."IdTipoComprobanteOrigen",IE."IdTipoComprobanteOrigen")
			   AND IE."IdTipoProducto" 					= COALESCE(IEA."IdTipoProducto",IE."IdTipoProducto")
			   AND IE."IdTipoOperacion" 				= COALESCE(IEA."IdTipoOperacion",IE."IdTipoOperacion")
			   AND IE."IdBolsa" 						= COALESCE(IEA."IdBolsa",IE."IdBolsa")
			   AND IE."IdTipoMovimiento" 				= COALESCE(IEA."IdTipoMovimiento",IE."IdTipoMovimiento");
	
						   
			   
			v_MovimientosUnicoPorComprobante =
		    SELECT  IEA."Grupo",IEA."IdComprobante",1 as "NroLinea",
		    		IE."IdIngresoEgreso",IE."IdTipoMovimiento",IE."IdTipoMovimiento" AS "IdTipoMovimientoDetalle",IE."IdConcepto",
					CASE WHEN p_IdCaja = 0 THEN (CASE WHEN COALESCE(IE."IdCaja",0) = 0 THEN CMU."IdCaja" ELSE IE."IdCaja" END) ELSE p_IdCaja END as "IdCaja",
					IE."IdMoneda",CASE WHEN IE."IdTipoMovimiento" = 'MOVEGR' THEN CAST((-1*IE."Monto" ) AS DECIMAL(26,8)) ELSE IE."Monto" END AS "Monto",
					IE."FechaLiquidacion",IE."FechaIngresoRegistro",IE."FechaMovimiento",IE."IdTipoPersona",IE."IdPersona",IE."NombrePersona",
					IE."IdTipoDocumentoIdentidad",IE."DocumentoIdentidad",IE."IdTipoPersonaTitularPago",IE."IdPersonaTitularPago",IE."NombrePersonaTitularPago",
					IE."IdPortafolioTitularPago",IE."SecuenciaAgrupacion",IE."SecuenciaRelacionada",IE."MueveCuentaInversion",IE."IdEmpleadoDigitador",
					IE."IdSucursalEmpleadoDigitador",IE."EsBilateral",IE."TitularPago",IE."IdTipoComprobanteOrigen",IE."IdComprobanteOrigen",IE."FechaComprobanteOrigen",
					IE."FechaLiquidacionComprobanteOrigen",IE."IdTipoProducto",IE."IdTipoOperacion",IE."IdBolsa",IE."IdMonedaOperacion",IE."MontoOperacion",
					IE."FechaParidad",IE."IdTipoReajuste",IE."ValorParidad",IE."FolioCartera",IE."IdInstrumento",IE."IdTipoInstrumento",IE."IdClasificacionTipoInstrumento",
					'N' AS "ComprobanteAgrupado",'N' AS "ComprobanteNeteado",IE."NumeroReferencia",IE."TipoComprobante",IE."IdCondicionLiquidacion",
					COALESCE(IE."Glosa", '') AS "Glosa"
		      FROM :v_MovimientosAProcesar AS IE
		     INNER JOIN :v_MovimientosNoAgrupables AS IEA ON (IE."IdIngresoEgreso" = IEA."IdInternoTes")
		      LEFT JOIN :v_CajaMonedaUsuario AS CMU ON (CMU."IdMoneda" = IE."IdMoneda")
		      									   AND (CMU."IdTipoMovimiento" = IE."IdTipoMovimiento" OR  CMU."IdTipoMovimiento" = 'TODOS');
			
			
			--********************************************************
			--* Procesamiento de los datos a ingresar en comprobante *
			--********************************************************
			v_MovimientoDetalle =
			SELECT MGC.* FROM :v_MovimientoEnGruposComprobantes AS MGC
			 UNION ALL
		    SELECT MUC.* FROM :v_MovimientosUnicoPorComprobante AS MUC;
		    


			INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_Comprobante"
			SELECT  MI."NumeroReferencia",MI."IdComprobante",MI."TipoComprobante",
					CASE WHEN MI."IdTipoMovimiento" = '' THEN CASE WHEN SUM(MI."Monto") > 0 THEN 'MOVING' ELSE 'MOVEGR' END ELSE MI."IdTipoMovimiento" END as "IdTipoMovimiento",
					CASE WHEN LENGTH(MI."TipoComprobante") > 0 THEN MI."TipoComprobante" 
						 WHEN LENGTH(MI."TipoComprobante") = 0 AND COUNT(Distinct MI."IdConcepto") > 1 THEN 'MULCPT' 
						 ELSE MAX(MI."IdConcepto") 
					 END as "IdConcepto",
		            MI."IdCaja",  
		            MI."IdMoneda",
		            CAST(ABS(SUM(MI."Monto")) AS DECIMAL(26,8)) as "Monto",
		            MI."FechaLiquidacion",
		            MI."IdTipoPersona",
		            MI."IdPersona",
		            MI."NombrePersona",
		            CAST(MI."IdTipoDocumentoIdentidad"  as NVARCHAR(1)) as "IdTipoDocumentoIdentidad",
		            MI."DocumentoIdentidad",
		            MI."IdTipoPersonaTitularPago",
		            MI."IdPersonaTitularPago",
		            MI."NombrePersonaTitularPago",
		            MI."IdPortafolioTitularPago",		            
		            CASE WHEN LENGTH(MI."TipoComprobante") = 0 THEN 'S' ELSE MI."TitularPago" END AS "TitularPago",
		            MI."SecuenciaAgrupacion",
		            MI."EsBilateral",
		            MI."ComprobanteAgrupado",
					MI."ComprobanteNeteado",
					CASE WHEN COUNT(Distinct MI."IdConcepto") > 1 THEN 'N' ELSE 'S' END as "SeparaPorConcepto",
					'S' AS "GeneradoDesdeMovimientos",
					MI."MueveCuentaInversion",
                    MI."IdCondicionLiquidacion" AS "IdCondicionDeLiquidacion",
                    CASE 
						WHEN COUNT(DISTINCT NULLIF("Glosa", '')) = 1 THEN MAX(NULLIF("Glosa", ''))    
						WHEN COUNT(DISTINCT NULLIF("Glosa", '')) > 1 THEN ''   WHEN COUNT("Glosa") = 1 THEN MAX("Glosa")
						ELSE ''
					END AS "Glosa"
			 FROM :v_MovimientoDetalle as MI
			GROUP BY MI."NumeroReferencia",MI."IdComprobante",MI."TipoComprobante",MI."IdPersona",MI."NombrePersona",MI."IdTipoPersona",
					 MI."IdTipoDocumentoIdentidad",MI."DocumentoIdentidad",MI."TitularPago",MI."IdTipoPersonaTitularPago",MI."IdPersonaTitularPago",
					 MI."NombrePersonaTitularPago",MI."IdPortafolioTitularPago",MI."IdMoneda",MI."IdCaja",MI."IdTipoMovimiento",
					 MI."FechaLiquidacion", MI."SecuenciaAgrupacion",MI."EsBilateral",MI."ComprobanteAgrupado",MI."ComprobanteNeteado",MI."MueveCuentaInversion",MI."IdCondicionLiquidacion";
	
								 

					 
			INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteDetalle"
			SELECT "BSCL.Tesoreria.BD.Secuencias::ComprobanteDetalle".NEXTVAL AS "IdComprobanteDetalle",MI."IdComprobante",
				   MI."IdTipoMovimientoDetalle" as "IdTipoMovimiento",MI."IdConcepto",
				   ROW_NUMBER() over () as "NroLinea", -- no necesita particion ya que está agrupado 
				   MI."IdMoneda",SUM(MI."Monto") AS "Monto",
				   MI."SecuenciaAgrupacion",MI."SecuenciaRelacionada",MI."IdEmpleadoDigitador",MI."IdSucursalEmpleadoDigitador",MI."IdTipoComprobanteOrigen",
				   MI."IdComprobanteOrigen",MI."FechaComprobanteOrigen",MI."FechaLiquidacionComprobanteOrigen",MI."IdTipoProducto",MI."IdTipoOperacion",
				   MI."IdBolsa",MI."IdMonedaOperacion",MI."MontoOperacion",MI."FechaParidad",MI."IdTipoReajuste",MI."ValorParidad",
				   MI."FolioCartera",MI."IdInstrumento",MI."IdTipoInstrumento",MI."IdClasificacionTipoInstrumento",MI."IdCondicionLiquidacion" AS "IdCondicionDeLiquidacion"
			  FROM :v_MovimientoDetalle as MI
			 GROUP BY MI."IdComprobante",MI."IdTipoMovimientoDetalle",MI."IdConcepto",MI."IdMoneda",MI."SecuenciaAgrupacion",MI."SecuenciaRelacionada",
			 	   MI."IdEmpleadoDigitador",MI."IdSucursalEmpleadoDigitador",MI."IdTipoComprobanteOrigen",MI."IdComprobanteOrigen",
				   MI."FechaComprobanteOrigen",MI."FechaLiquidacionComprobanteOrigen",MI."IdTipoProducto",MI."IdTipoOperacion",
				   MI."IdBolsa",MI."IdMonedaOperacion",MI."MontoOperacion",MI."FechaParidad",MI."IdTipoReajuste",MI."ValorParidad",
				   MI."FolioCartera",MI."IdInstrumento",MI."IdTipoInstrumento",MI."IdClasificacionTipoInstrumento",MI."IdCondicionLiquidacion";
				   
		
		
			INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteDetalleOrigen"
			SELECT CD."IdComprobanteDetalle", MI."IdIngresoEgreso", CAST(null AS BIGINT) AS "IdComprobante"
			  FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteDetalle" as CD
			 INNER JOIN :v_MovimientoDetalle as MI 
			 		 ON MI."IdComprobante" = CD."IdComprobante"
			 		AND MI."IdTipoMovimientoDetalle" = CD."IdTipoMovimiento"
			 		AND MI."IdConcepto" = CD."IdConcepto"
					AND COALESCE(MI."SecuenciaAgrupacion",'') = COALESCE(CD."SecuenciaAgrupacion",'')
			 		AND COALESCE(MI."SecuenciaRelacionada",0) = COALESCE(CD."SecuenciaRelacionada",0);			 		
			  			
			
			INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteFormaDePago"
			SELECT 	MI."IdComprobante",MI."NumeroReferencia",IEFP."IdTipoMovimiento",IEFP."IdMedioDePago",IEFP."NombreMedioDePago",IEFP."IdModalidadDePago",IEFP."NombreModalidadDePago",				
					IEFP."IdCuentaBancariaCorredora",IEFP."NombreCuentaBancariaCorredora",COALESCE(IEFP."NumeroCuentaBancariaCorredora",''),COALESCE(IEFP."IdTipoCuentaBancariaCorredora",''),
					COALESCE(IEFP."NombreTipoCuentaBancariaCorredora",''),IEFP."IdTarjetaBancariaCorredora",IEFP."IdTipoTarjetaBancariaCorredora",IEFP."NombreTipoTarjetaBancariaCorredora",
					IEFP."NumeroTarjetaBancariaCorredora",IEFP."IdChequera",IEFP."NumeroDocumentoCorredora", IEFP."FechaDocumentoPagoCorredora", 
					IEFP."IdCuentaBancariaSocio",IEFP."IdBancoSocio",IEFP."NombreCuentaBancariaSocio",IEFP."NumeroCuentaBancariaSocio",IEFP."IdTipoCuentaBancariaSocio",
					IEFP."NombreTipoCuentaBancariaSocio",IEFP."IdTarjetaBancariaSocio",IEFP."IdTipoTarjetaBancariaSocio",IEFP."NombreTipoTarjetaBancariaSocio",
					IEFP."NumeroTarjetaBancariaSocio",IEFP."NumeroSerieSocio",IEFP."NumeroDocumentoSocio", IEFP."FechaDocumentoPagoSocio",
					IEFP."IdMoneda",CAST(SUM(IEFP."Monto") AS DECIMAL(26,8)) AS "Monto",COALESCE(IbC."IdPlaza",0) as "IdPlaza",
					CASE WHEN (IEFP."IdBancoSocio" = IbC."IdBanco" AND IEFP."IdBancoSocio" > 0) THEN IbC."PlazoRetencionMismoBanco"
						 WHEN (IEFP."IdBancoSocio" <> IbC."IdBanco" AND IEFP."IdBancoSocio" > 0) THEN IbC."PlazoRetencionOtroBanco"
						 ELSE 0
					 END AS "DiasRetencion",
					'0001-01-01' as "FechaAfectaSaldo"
			  FROM :v_MovimientoDetalle AS MI
			 INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago" AS IEFP 
			 		 ON IEFP."IdIngresoEgreso"  = MI."IdIngresoEgreso"
			  LEFT JOIN :v_InformacionBancoCorredora AS IbC 
			 		 ON IbC."IdCuentaBanco" 	= IEFP."IdCuentaBancariaCorredora"
			 WHERE IEFP."IdEstado" = 'ING' AND LENGTH(MI."TipoComprobante") = 0 
			 GROUP BY MI."Grupo",MI."IdComprobante",MI."NumeroReferencia",IEFP."IdTipoMovimiento",IEFP."IdMedioDePago",IEFP."NombreMedioDePago",IEFP."IdModalidadDePago",
					IEFP."NombreModalidadDePago",IEFP."IdCuentaBancariaCorredora",IEFP."IdTipoCuentaBancariaCorredora",IEFP."NombreTipoCuentaBancariaCorredora",
					IEFP."NombreCuentaBancariaCorredora",IEFP."NumeroCuentaBancariaCorredora",IEFP."IdTarjetaBancariaCorredora",IEFP."IdTipoTarjetaBancariaCorredora",
					IEFP."NombreTipoTarjetaBancariaCorredora",IEFP."NumeroTarjetaBancariaCorredora",IEFP."IdChequera",IEFP."IdBancoSocio",
					IEFP."NumeroDocumentoCorredora",IEFP."FechaDocumentoPagoCorredora",IEFP."IdCuentaBancariaSocio",IEFP."IdTipoCuentaBancariaSocio",
					IEFP."NombreTipoCuentaBancariaSocio",IEFP."NombreCuentaBancariaSocio",IEFP."NumeroCuentaBancariaSocio",IEFP."IdTarjetaBancariaSocio",
					IEFP."IdTipoTarjetaBancariaSocio",IEFP."NombreTipoTarjetaBancariaSocio",IEFP."NumeroTarjetaBancariaSocio",
					IEFP."FechaVctoTarjetaBancariaSocio",IEFP."NumeroSerieSocio",IEFP."NumeroDocumentoSocio",IEFP."FechaDocumentoPagoSocio",IEFP."IdMoneda",
					IbC."IdPlaza",IbC."IdBanco",IbC."PlazoRetencionMismoBanco",IbC."PlazoRetencionOtroBanco";
							
			 
			INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteFormaDePago"
			SELECT DISTINCT MI."IdComprobante",MI."NumeroReferencia",CASE WHEN MI."TipoComprobante" = 'PARING' THEN 'MOVING' WHEN MI."TipoComprobante" = 'PAREGR' THEN 'MOVEGR' END as "IdTipoMovimiento",				
					FP."IdMedioDePago",COALESCE(FP."NombreMedioDePago",'') as "NombreMedioDePago",FP."IdModalidadDePago",COALESCE(FP."NombreModalidadDePago",'') as "NombreModalidadDePago",
					FP."IdCuentaBancariaCorredora",COALESCE(FP."NombreCuentaBancaria",'') as "NombreCuentaBancariaCorredora",COALESCE(FP."NumeroCuentaBancaria",'') as "NumeroCuentaBancariaCorredora",
		 			COALESCE(FP."IdTipoCuentaBancaria",'') as "IdTipoCuentaBancariaCorredora",COALESCE(FP."NombreTipoCuentaBancaria",'') as "NombreTipoCuentaBancariaCorredora",
					FP."IdTarjetaBancariaCorredora",COALESCE(FP."IdTipoTarjetaBancaria",'') as "IdTipoTarjetaBancariaCorredora",COALESCE(FP."NombreTipoTarjetaBancaria",'') as "NombreTipoTarjetaBancariaCorredora",
					COALESCE(FP."NumeroTarjetaBancaria",'') as "NumeroTarjetaBancariaCorredora",FP."IdChequera",FP."NumeroDocumentoCorredora",FP."FechaDocumentoPagoCorredora",
					FP."IdCuentaBancariaSocio",FP."IdBancoSocio",FP."NombreCuentaBancariaSocio",FP."NumeroCuentaBancariaSocio",FP."IdTipoCuentaBancariaSocio",
	 				FP."NombreTipoCuentaBancariaSocio",FP."IdTarjetaBancariaSocio",FP."IdTipoTarjetaBancariaSocio",FP."NombreTipoTarjetaBancariaSocio",
	 				FP."NumeroTarjetaBancariaSocio",FP."NumeroSerieSocio",FP."NumeroDocumentoSocio",FP."FechaDocumentoPagoSocio",FP."IdMoneda",FP."Monto",
	 				COALESCE(FP."IdPlaza",0) as "IdPlaza",
					CASE WHEN (FP."IdBancoSocio" = FP."IdBanco" AND FP."IdBancoSocio" > 0) THEN FP."PlazoRetencionMismoBanco"
						 WHEN (FP."IdBancoSocio" <> FP."IdBanco" AND FP."IdBancoSocio" > 0) THEN FP."PlazoRetencionOtroBanco"
						 ELSE 0
					 END AS "DiasRetencion",
					'0001-01-01' as "FechaAfectaSaldo"
			  FROM :v_MovimientoDetalle AS MI
			 INNER JOIN :v_FormasDePago AS FP 
			 		 ON FP."NumeroReferencia" = MI."NumeroReferencia"
			 WHERE LENGTH(MI."TipoComprobante") > 0;
			 

			UPDATE CFP
			   SET 	 "DiasRetencion"	 	= CASE WHEN TMMP."AfectoADiasRetencion" = 'S' THEN CFP."DiasRetencion" ELSE 0 END			   
			   		,"FechaAfectaSaldo"		= CASE WHEN TMMP."AfectoADiasRetencion" = 'S' AND instr(TMMP."AfectaSaldoEnEstado",'ING')> 0 AND COALESCE(CFP."IdCuentaBancariaCorredora",0) > 0
												   THEN ADD_DAYS(TO_DATE(:p_FechaMovimiento, 'YYYY-MM-DD'), CFP."DiasRetencion") || ' ' || TO_VARCHAR (CURRENT_UTCTIMESTAMP,'HH24:MI:SS.FF7')												   
												   WHEN TMMP."AfectoADiasRetencion" = 'N' AND instr(TMMP."AfectaSaldoEnEstado",'ING')> 0 AND COALESCE(CFP."IdCuentaBancariaCorredora",0) > 0
												   THEN TO_DATE(:p_FechaMovimiento, 'YYYY-MM-DD') || ' ' || TO_VARCHAR (CURRENT_UTCTIMESTAMP,'HH24:MI:SS.FF7')
									               ELSE '0001-01-01'
											   END
			  FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteFormaDePago" as CFP
			 INNER JOIN "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago" AS TMMP 
				 	 ON TMMP."IdTipoMovimiento" = CFP."IdTipoMovimiento"
			    	AND TMMP."IdMedioDePago" 	= CFP."IdMedioDePago";
		END IF; ---upper(:p_GenerarComprobanteDesdeMovimientos) = 'S'

		
		IF (upper(:p_GenerarComprobanteDesdeComprobantes) = 'S') THEN
			--*********************************************************
			--* Recupera informacion asociado al comprobante original *
			--*********************************************************
			v_ComprobantesAProcesar =
			SELECT	 CompAGenerar."NumeroReferencia",CompAGenerar."TipoComprobanteInformado",CompAGenerar."IdComprobanteInformado"
					,CompAGenerar."IdComprobante",CompAGenerar."TipoComprobante",CompAGenerar."IdEmpresa",CompAGenerar."IdTipoMovimiento"
					,CompAGenerar."IdConcepto",CompAGenerar."IdCaja",CompAGenerar."IdMoneda",CompAGenerar."FechaLiquidacion",CompAGenerar."FechaMovimiento"
					,CompAGenerar."IdTipoPersona",CompAGenerar."IdPersona",Per."NombrePersona",CompAGenerar."IdTipoDocumentoIdentidad"
					,Per."DocumentoIdentidad",CompAGenerar."TitularPago",CompAGenerar."IdTipoPersonaTitularPago",CompAGenerar."IdPersonaTitularPago"
					,PerTP."NombrePersona" as "NombrePersonaTitularPago",CompAGenerar."IdPortafolioTitularPago",CompAGenerar."EsBilateral"
					,CompAGenerar."MueveCuentaInversion",CompAGenerar."ComprobanteAgrupado",CompAGenerar."ComprobanteNeteado",CompAGenerar."SeparadoPorConcepto"
					,CASE WHEN CompAGenerar."IdTipoMovimiento" = 'MOVING' THEN CompAGenerar."Monto" ELSE -1*CompAGenerar."Monto" END AS "Monto",CompAGenerar."IdCondicionDeLiquidacion"
					,COALESCE(CompAGenerar."Glosa", '') AS "Glosa"
			  FROM :v_ComprobanteAGenerar As CompAGenerar
			 INNER JOIN :v_InformacionPersona AS Per ON Per."IdPersona" = CompAGenerar."IdPersona"
			 INNER JOIN :v_InformacionPersona AS PerTP ON PerTP."IdPersona" = CompAGenerar."IdPersonaTitularPago"
			 WHERE			    (
				        (CompAGenerar."TipoComprobanteInformado" = 'PAREGR' AND CompAGenerar."IdEstado" = 'LIQ')
				        OR
				        CompAGenerar."TipoComprobanteInformado" <> 'PAREGR' AND CompAGenerar."IdEstado" IN ('ING', 'LIP', 'GIP')
				    );
								   		   
			v_ComprobanteDetalleAProcesar = 
			SELECT   CompAProcesar."NumeroReferencia",CompAProcesar."TipoComprobanteInformado",CompDet."IdConcepto",CompDet."IdMoneda",CompDet."SecuenciaAgrupacion"
					,CompDet."SecuenciaRelacionada",CompDet."IdEmpleadoDigitador",CompDet."IdSucursalEmpleadoDigitador",CompDet."IdTipoComprobanteOrigen"
					,CompDet."IdComprobanteOrigen",CompDet."FechaComprobanteOrigen",CompDet."FechaLiquidacionComprobanteOrigen",CompDet."IdTipoProducto"
					,CompDet."IdTipoOperacion",CompDet."IdBolsa",CompDet."IdMonedaOperacion",CompDet."MontoOperacion",CompDet."IdComprobanteDetalle"
					,CompDet."FechaParidad",CompDet."IdTipoReajuste",CompDet."ValorParidad",CompDet."FolioCartera",CompDet."IdInstrumento"
					,CompDet."IdTipoInstrumento",CompDet."IdClasificacionTipoInstrumento",CompDet."IdComprobante"
					,CASE WHEN CompAProcesar."IdTipoMovimiento" = 'MOVING' THEN CompDet."Monto" ELSE -1*CompDet."Monto" END AS "Monto"
					,CompDet."IdTipoMovimiento",CompDet."IdCondicionDeLiquidacion"
					,COALESCE(CompAProcesar."Glosa", '') AS "Glosa"
			  FROM :v_ComprobantesAProcesar As CompAProcesar
			 INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" As CompDet ON CompAProcesar."IdComprobante" = CompDet."IdComprobante"
			 WHERE
				    (
				        (CompAProcesar."TipoComprobanteInformado" = 'PAREGR' AND CompDet."IdEstado" = 'LIQ')
				        OR
				        (CompAProcesar."TipoComprobanteInformado" <> 'PAREGR' AND CompDet."IdEstado" = 'ING')
				    )
			   FOR UPDATE OF CompDet."IdEstado";		
		   		   
			v_ComprobanteFormaDePagoAProcesar =
			SELECT   CompAProcesar."NumeroReferencia",CompAProcesar."TipoComprobanteInformado",CompFP."IdTipoMovimiento"
					,CompFP."IdMedioDePago",CompFP."NombreMedioDePago",CompFP."IdModalidadDePago",CompFP."NombreModalidadDePago"
					,CompFP."IdCuentaBancariaCorredora",CompFP."NombreCuentaBancariaCorredora",CompFP."NumeroCuentaBancariaCorredora"
					,CompFP."IdTipoCuentaBancariaCorredora",CompFP."NombreTipoCuentaBancariaCorredora",CompFP."IdTarjetaBancariaCorredora"
					,CompFP."IdTipoTarjetaBancariaCorredora",CompFP."NombreTipoTarjetaBancariaCorredora",CompFP."NumeroTarjetaBancariaCorredora"
					,CompFP."IdChequera",CompFP."NumeroDocumentoCorredora",CompFP."FechaDocumentoPagoCorredora",CompFP."IdCuentaBancariaSocio"
					,CompFP."IdBancoSocio",CompFP."NombreCuentaBancariaSocio",CompFP."NumeroCuentaBancariaSocio",CompFP."IdTipoCuentaBancariaSocio"
					,CompFP."NombreTipoCuentaBancariaSocio",CompFP."IdTarjetaBancariaSocio",CompFP."IdTipoTarjetaBancariaSocio"
					,CompFP."NombreTipoTarjetaBancariaSocio",CompFP."NumeroTarjetaBancariaSocio",CompFP."NumeroSerieSocio",CompFP."NumeroDocumentoSocio"
					,CompFP."FechaDocumentoPagoSocio",CompFP."IdMoneda",CompFP."IdComprobante"
					,CASE WHEN CompAProcesar."IdTipoMovimiento" = 'MOVING' THEN CompFP."Monto" ELSE -1*CompFP."Monto" END AS "Monto"
					,COALESCE(CompAProcesar."Glosa", '') AS "Glosa"
			  FROM :v_ComprobantesAProcesar As CompAProcesar
			 INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" As CompFP ON CompAProcesar."IdComprobante" = CompFP."IdComprobante"
			 WHERE
				    (
				        (CompAProcesar."TipoComprobanteInformado" = 'PAREGR' AND CompFP."IdEstado" = 'LIQ')
				        OR
				        (CompAProcesar."TipoComprobanteInformado" <> 'PAREGR' AND CompFP."IdEstado" = 'ING')
				    )
			   FOR UPDATE OF CompFP."IdEstado";
			
			v_InformacionPartida =
			SELECT 	FP."NumeroReferencia", Comp."TipoComprobanteInformado", Comp."CantComprobantes", Comp."CantComprobantesDetalle",FP."IdMoneda",
					CAST(SUM(FP."Monto") AS DECIMAL(26,8)) AS "Monto"
			  FROM :v_FormasDePago as FP
			 INNER JOIN (SELECT  CompDet."NumeroReferencia"
			 					,CompDet."TipoComprobanteInformado"
			 					,COUNT(CompDet."IdComprobante") AS "CantComprobantes"
			 					,COUNT(CompDet."IdComprobanteDetalle") AS "CantComprobantesDetalle"
			 			   FROM :v_ComprobanteDetalleAProcesar as CompDet
			 			  GROUP BY CompDet."NumeroReferencia",CompDet."TipoComprobanteInformado"
			 			) AS Comp ON Comp."NumeroReferencia" = FP."NumeroReferencia"
			 WHERE LENGTH(Comp."TipoComprobanteInformado") > 0
			 GROUP BY FP."NumeroReferencia", FP."IdMoneda",Comp."TipoComprobanteInformado",Comp."CantComprobantes",Comp."CantComprobantesDetalle";
			
			--********************************************************************************************************
			--* Relaciona cada movimiento con la configuracion de tesoreria, para generar los grupos de comprobantes *
			--********************************************************************************************************
			INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_GruposDeComprobantes"
			SELECT  RANK() over 
						(order by	"NumeroReferencia",
									"TipoComprobanteInformado",
									"IdPersonaTitularPago",
									"IdPortafolioTitularPago",
									"IdMoneda", 
					                "IdCaja", 
					                "FechaLiquidacion",
					                "SecuenciaAgrupacion",
					                "MueveCuentaInversion",
					                "NeteaComprobante",
					                "IdTipoMovimiento",
					                "AgrupaComprobante",	
					                "IdInternoTes",		                	
					                "IdConcepto",
					                "IdEmpleadoDigitador",
					                "IdSucursalEmpleadoDigitador",
					                "IdTipoComprobanteOrigen",
					                "IdTipoProducto",
					                "IdTipoOperacion",
					                "IdBolsa",
                                    "IdCondicionDeLiquidacion",
									"Glosa"
						) as "Grupo",
					"BSCL.Tesoreria.BD.Secuencias::Comprobante".NEXTVAL AS "IdComprobante",
					"NumeroReferencia",
					COALESCE ("TipoComprobanteInformado",'') AS "TipoComprobante",
					"IdPersonaTitularPago",
					"IdPortafolioTitularPago",
					"IdMoneda", 
	                "IdCaja", 
	                "FechaLiquidacion",
	                "SecuenciaAgrupacion",
	                "MueveCuentaInversion",
	                "NeteaComprobante",
					"IdTipoMovimiento",
					"AgrupaComprobante",
					"IdInternoTes",
					"IdConcepto",
					"IdEmpleadoDigitador",
					"IdSucursalEmpleadoDigitador",
			 		"IdTipoComprobanteOrigen",
			 		"IdTipoProducto",
			 		"IdTipoOperacion",
			 		"IdBolsa",
			 		COALESCE ("IdCondicionDeLiquidacion",'') AS "IdCondicionDeLiquidacion",
					CASE 
						WHEN COUNT(DISTINCT NULLIF("Glosa", '')) = 1 THEN MAX(NULLIF("Glosa", ''))    
						WHEN COUNT(DISTINCT NULLIF("Glosa", '')) > 1 THEN ''   WHEN COUNT("Glosa") = 1 THEN MAX("Glosa")
						ELSE ''
					END AS "Glosa"
			  FROM (SELECT  CompPro."NumeroReferencia",
			  				CompPro."TipoComprobanteInformado",
							CASE WHEN LENGTH(CompPro."TipoComprobanteInformado") = 0 THEN CompPro."IdPersonaTitularPago" END AS "IdPersonaTitularPago",
							CASE WHEN LENGTH(CompPro."TipoComprobanteInformado") = 0 THEN CompPro."IdPortafolioTitularPago" END AS "IdPortafolioTitularPago",
			                CompPro."FechaLiquidacion",
			                CompPro."IdMoneda",
			                '' as "SecuenciaAgrupacion",
			                CompPro."MueveCuentaInversion",
			                CASE WHEN p_IdCaja = 0 THEN CompPro."IdCaja" ELSE :p_IdCaja END as "IdCaja",			                
	                		CompPro."ComprobanteNeteado" as "NeteaComprobante",
	                		CASE WHEN (LENGTH(CompPro."TipoComprobanteInformado") = 0 AND CompPro."ComprobanteNeteado" = 'N') 	THEN CompPro."IdTipoMovimiento" END "IdTipoMovimiento",
	                		CompPro."ComprobanteAgrupado" as "AgrupaComprobante",
	                		CASE WHEN (LENGTH(CompPro."TipoComprobanteInformado") = 0 AND CompPro."ComprobanteAgrupado" = 'N') 	THEN CompPro."IdComprobante" 	END "IdInternoTes",
	                		CASE WHEN (LENGTH(CompPro."TipoComprobanteInformado") = 0 AND Config."SEPARAPORCONCEPTO"  = 'S') 	THEN CompPro."IdConcepto" 		END "IdConcepto",
	                		0 as "IdEmpleadoDigitador",
							0 as "IdSucursalEmpleadoDigitador",
							'' as "IdTipoComprobanteOrigen",
					 		'' as "IdTipoProducto",
					 		'' as "IdTipoOperacion",
					 		'' as "IdBolsa",
							 CASE WHEN (LENGTH(CompPro."TipoComprobanteInformado") = 0 AND Config."SEPARAPORCONDICIONLIQUIDACION"  = 'S') 	THEN  CompPro."IdCondicionDeLiquidacion" END "IdCondicionDeLiquidacion",
							COALESCE(CompPro."Glosa", '') AS "Glosa"
					  FROM :v_ComprobantesAProcesar AS CompPro
					 INNER JOIN :v_ConfiguracionTesoreria AS Config ON 1=1)
			 GROUP BY "NumeroReferencia","TipoComprobanteInformado","IdPersonaTitularPago","IdPortafolioTitularPago","IdMoneda","MueveCuentaInversion","IdCaja",
			 	"FechaLiquidacion","SecuenciaAgrupacion","NeteaComprobante","IdConcepto","IdTipoMovimiento","AgrupaComprobante","IdInternoTes",
			 	"IdEmpleadoDigitador","IdSucursalEmpleadoDigitador","IdTipoComprobanteOrigen","IdTipoProducto","IdTipoOperacion","IdBolsa","IdCondicionDeLiquidacion","Glosa";
			 
			
			--*********Comprobante normal
			INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_Comprobante"
			SELECT   Comp."NumeroReferencia"
			,Grupos."IdComprobante"
			,Comp."TipoComprobanteInformado" AS "TipoComprobante"
					,CASE WHEN SUM(Comp."Monto") > 0 THEN 'MOVING' ELSE 'MOVEGR' END AS "IdTipoMovimiento"
					,CASE WHEN COUNT(Distinct Comp."IdConcepto") > 1 THEN 'MULCPT' ELSE MAX(Comp."IdConcepto") END as "IdConcepto"
					,CASE WHEN p_IdCaja = 0 THEN (CASE WHEN COALESCE(Comp."IdCaja",0) = 0 THEN MAX(CMU."IdCaja") ELSE Comp."IdCaja" END) ELSE p_IdCaja END as "IdCaja"
					,Comp."IdMoneda"
					,CAST(ABS(SUM(Comp."Monto")) AS DECIMAL(26,8)) AS "Monto"
					,Comp."FechaLiquidacion"
					,Comp."IdTipoPersona"
					,Comp."IdPersona"
					,Comp."NombrePersona"
					,CAST(Comp."IdTipoDocumentoIdentidad" as NVARCHAR(1)) as "IdTipoDocumentoIdentidad"
					,Comp."DocumentoIdentidad"
					,Comp."IdTipoPersonaTitularPago"
					,Comp."IdPersonaTitularPago"
					,Comp."NombrePersonaTitularPago",Comp."IdPortafolioTitularPago"
					,Comp."TitularPago"
					,CAST('' AS NVARCHAR(40)) AS "SecuenciaAgrupacion"
					,Comp."EsBilateral"
					,Comp."ComprobanteAgrupado" AS "ComprobanteAgrupado"
					,Comp."ComprobanteNeteado"
					,CASE WHEN COUNT(Distinct Comp."IdConcepto") > 1 THEN 'N' ELSE 'S' END as "SeparaPorConcepto"
					,'N' AS "GeneradoDesdeMovimientos"
					,Comp."MueveCuentaInversion"
					,Grupos."IdCondicionDeLiquidacion"
					,COALESCE(Grupos."Glosa", '') AS "Glosa"
			  FROM :v_ComprobantesAProcesar AS Comp
			 INNER JOIN "BSCL.Tesoreria.BD::GBL.CrearComprobante_GruposDeComprobantes" AS Grupos ON (  Grupos."NumeroReferencia" 		= Comp."NumeroReferencia"
															AND Grupos."FechaLiquidacion"		= Comp."FechaLiquidacion"
															AND Grupos."MueveCuentaInversion"	= Comp."MueveCuentaInversion"
															AND Grupos."IdMoneda"				= Comp."IdMoneda")
			  LEFT JOIN :v_CajaMonedaUsuario AS CMU ON (CMU."IdMoneda" = Comp."IdMoneda")
			  									   AND (CMU."IdTipoMovimiento" = Comp."IdTipoMovimiento" OR  CMU."IdTipoMovimiento" = 'TODOS')
			 WHERE Comp."IdPersonaTitularPago" 		= COALESCE(Grupos."IdPersonaTitularPago",Comp."IdPersonaTitularPago")
			   AND Comp."IdPortafolioTitularPago"	= COALESCE(Grupos."IdPortafolioTitularPago",Comp."IdPortafolioTitularPago") 
			   AND Comp."IdCaja" 					= COALESCE(Grupos."IdCaja",Comp."IdCaja")
			   AND Comp."IdComprobante"				= COALESCE(Grupos."IdInternoTes",Comp."IdComprobante")
			   AND Comp."TipoComprobanteInformado" 	= COALESCE(Grupos."TipoComprobante",Comp."TipoComprobanteInformado")
			   AND Comp."IdConcepto"				= COALESCE(Grupos."IdConcepto",Comp."IdConcepto")
			   AND LENGTH(Grupos."TipoComprobante") = 0
			 GROUP BY  Grupos."Grupo",Comp."NumeroReferencia",Grupos."IdComprobante",Comp."IdEmpresa",Comp."TipoComprobanteInformado",Comp."IdMoneda"
			 		,Comp."FechaLiquidacion",Comp."IdTipoPersona",Comp."IdPersona",Comp."NombrePersona",Comp."IdTipoDocumentoIdentidad"
			 		,Comp."DocumentoIdentidad",Comp."TitularPago",Comp."IdTipoPersonaTitularPago",Comp."IdPersonaTitularPago",Comp."NombrePersonaTitularPago"
					,Comp."IdPortafolioTitularPago",Comp."EsBilateral",Comp."MueveCuentaInversion",Comp."IdCaja",Comp."ComprobanteNeteado",Comp."ComprobanteAgrupado",Grupos."IdCondicionDeLiquidacion", Grupos."Glosa";
			
			--*********Comprobante partida egreso
			INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_Comprobante"
			SELECT   Grupos."NumeroReferencia",Grupos."IdComprobante",Grupos."TipoComprobante",'MOVEGR' AS "IdTipoMovimiento",'PAREGR' AS "IdConcepto"
					,Grupos."IdCaja",InfoPartida."IdMoneda",InfoPartida."Monto",Grupos."FechaLiquidacion",'CNT' AS "IdTipoPersona",InfoBanco."IdPersona"
					,InfoBanco."NombreCompleto" AS "NombrePersona",CAST(InfoBanco."CodigoTipoPersona" as NVARCHAR(1)) AS "IdTipoDocumentoIdentidad"
					,InfoBanco."DocumentoIdentidadDefecto" AS "DocumentoIdentidad",'CNT' AS "IdTipoPersonaTitularPago",InfoBanco."IdPersona" AS "IdPersonaTitularPago"
					,InfoBanco."NombreCompleto" AS "NombrePersonaTitularPago",0 AS "IdPortafolioTitularPago",'S' AS "TitularPago"
					,CAST('' AS NVARCHAR(40)) AS "SecuenciaAgrupacion",'N' as "EsBilateral",'S' AS "ComprobanteAgrupado"
					,'N' AS "ComprobanteNeteado",'N' AS "SeparaPorConcepto",'N' AS "GeneradoDesdeMovimientos",Grupos."MueveCuentaInversion",Grupos."IdCondicionDeLiquidacion" AS "IdCondicionDeLiquidacion"
					,COALESCE(Grupos."Glosa", '') AS "Glosa"
			  FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_GruposDeComprobantes" AS Grupos
		     INNER JOIN :v_InformacionPartida AS InfoPartida ON InfoPartida."NumeroReferencia" = Grupos."NumeroReferencia"
		      LEFT JOIN (SELECT DISTINCT FP."NumeroReferencia", IB.*
		      			   FROM :v_FormasDePago AS FP 
		      			   LEFT JOIN :v_InformacionBancoCorredora AS IB ON IB."IdCuentaBanco" = FP."IdCuentaBancariaCorredora"
		      			) AS InfoBanco
		      		 ON  InfoBanco."NumeroReferencia" = Grupos."NumeroReferencia"
			 WHERE Grupos."TipoComprobante" = 'PAREGR'
			 GROUP BY Grupos."NumeroReferencia",Grupos."IdComprobante",Grupos."TipoComprobante",Grupos."IdCaja",InfoPartida."IdMoneda",InfoPartida."Monto",
			 		  Grupos."FechaLiquidacion",InfoBanco."IdPersona",InfoBanco."NombreCompleto",InfoBanco."CodigoTipoPersona",InfoBanco."DocumentoIdentidadDefecto",
		    		  Grupos."MueveCuentaInversion",Grupos."AgrupaComprobante",Grupos."IdCondicionDeLiquidacion", Grupos."Glosa";			
								
			 --*********Comprobante partida ingreso
			INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_Comprobante"
			SELECT   Grupos."NumeroReferencia",Grupos."IdComprobante",Grupos."TipoComprobante",'MOVING' AS "IdTipoMovimiento",'PARING' AS "IdConcepto"
					,Grupos."IdCaja",InfoPartida."IdMoneda",InfoPartida."Monto",Grupos."FechaLiquidacion",'CNT' AS "IdTipoPersona",InfoCorr."IdPersona"
					,InfoCorr."NombreCompleto" AS "NombrePersona",CAST(InfoCorr."Tipo" as NVARCHAR(1)) AS "IdTipoDocumentoIdentidad"
					,InfoCorr."DocumentoIdentidad" AS "DocumentoIdentidad",'CNT' AS "IdTipoPersonaTitularPago",InfoCorr."IdPersona" AS "IdPersonaTitularPago"
					,InfoCorr."NombreCompleto" AS "NombrePersonaTitularPago",0 AS "IdPortafolioTitularPago",'S' AS "TitularPago"
					,CAST('' AS NVARCHAR(40)) AS "SecuenciaAgrupacion",'N' as "EsBilateral",'S' AS "ComprobanteAgrupado"
					,'N' AS "ComprobanteNeteado",'N' AS "SeparaPorConcepto",'N' AS "GeneradoDesdeMovimientos",Grupos."MueveCuentaInversion",Grupos."IdCondicionDeLiquidacion" AS "IdCondicionDeLiquidacion"
					,COALESCE(Grupos."Glosa", '') AS "Glosa"
			  FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_GruposDeComprobantes" AS Grupos
		     INNER JOIN :v_InformacionPartida AS InfoPartida ON InfoPartida."NumeroReferencia" = Grupos."NumeroReferencia",
		     :v_InformacionCorredora as InfoCorr
			 WHERE Grupos."TipoComprobante" = 'PARING'
			 GROUP BY Grupos."NumeroReferencia",Grupos."IdComprobante",Grupos."TipoComprobante",Grupos."IdCaja",InfoPartida."IdMoneda",InfoPartida."Monto",
			 		  Grupos."FechaLiquidacion",InfoCorr."IdPersona",InfoCorr."NombreCompleto",InfoCorr."Tipo",InfoCorr."DocumentoIdentidad",
			 		  Grupos."MueveCuentaInversion",Grupos."AgrupaComprobante",Grupos."IdCondicionDeLiquidacion", Grupos."Glosa";
			 		  
			
				  
			v_ComprobantesDetalleOriginal =
			SELECT 	 Grupos."Grupo"
					,DENSE_RANK() over (order by 
									 Grupos."Grupo", CompDet."NumeroReferencia",Grupos."IdComprobante",CompDet."IdTipoMovimiento",CompDet."IdConcepto",CompDet."IdMoneda"
							 		,CompDet."SecuenciaAgrupacion",CompDet."IdEmpleadoDigitador",CompDet."IdSucursalEmpleadoDigitador",CompDet."IdTipoComprobanteOrigen"
									,CompDet."IdComprobanteOrigen",CompDet."FechaComprobanteOrigen",CompDet."FechaLiquidacionComprobanteOrigen",CompDet."IdTipoProducto"
									,CompDet."IdTipoOperacion",CompDet."IdBolsa",CompDet."IdMonedaOperacion",CompDet."MontoOperacion",CompDet."FechaParidad"
									,CompDet."IdTipoReajuste",CompDet."ValorParidad",CompDet."FolioCartera",CompDet."IdInstrumento",CompDet."IdTipoInstrumento"
									,CompDet."IdClasificacionTipoInstrumento",CompDet."IdCondicionDeLiquidacion") AS "GrupoDetalle"
					,Grupos."IdComprobante",CompDet."IdTipoMovimiento",CompDet."IdConcepto",CompDet."TipoComprobanteInformado",CompDet."IdMoneda",CompDet."Monto"
					,CompDet."SecuenciaAgrupacion",CompDet."SecuenciaRelacionada",CompDet."IdEmpleadoDigitador",CompDet."IdSucursalEmpleadoDigitador"
					,CompDet."IdTipoComprobanteOrigen",CompDet."IdComprobanteOrigen",CompDet."FechaComprobanteOrigen",CompDet."FechaLiquidacionComprobanteOrigen"
					,CompDet."IdTipoProducto",CompDet."IdTipoOperacion",CompDet."IdBolsa",CompDet."IdMonedaOperacion",CompDet."MontoOperacion",CompDet."FechaParidad"
					,CompDet."IdTipoReajuste",CompDet."ValorParidad",CompDet."FolioCartera",CompDet."IdInstrumento",CompDet."IdTipoInstrumento",CompDet."IdClasificacionTipoInstrumento"
					,CompDet."IdComprobante" as "IdComprobanteOriginal",CompDet."IdComprobanteDetalle" as "IdComprobanteDetalleOriginal",CompDet."IdCondicionDeLiquidacion"
			  FROM :v_ComprobanteDetalleAProcesar As CompDet
			 INNER JOIN :v_ComprobantesAProcesar AS Comp ON (Comp."IdComprobante" 			= CompDet."IdComprobante")
			 INNER JOIN "BSCL.Tesoreria.BD::GBL.CrearComprobante_GruposDeComprobantes" AS Grupos  ON (Grupos."NumeroReferencia" 		= Comp."NumeroReferencia"
															AND Grupos."FechaLiquidacion"	= Comp."FechaLiquidacion"
															AND Grupos."IdMoneda"			= Comp."IdMoneda")
			 WHERE Comp."IdPersonaTitularPago" 		= COALESCE(Grupos."IdPersonaTitularPago",Comp."IdPersonaTitularPago")
			   AND Comp."IdPortafolioTitularPago"	= COALESCE(Grupos."IdPortafolioTitularPago",Comp."IdPortafolioTitularPago") 
			   AND Comp."IdCaja" 					= COALESCE(Grupos."IdCaja",Comp."IdCaja")
			   AND Comp."IdComprobante"				= COALESCE(Grupos."IdInternoTes",Comp."IdComprobante")
			   AND Comp."IdTipoMovimiento" 			= COALESCE(Grupos."IdTipoMovimiento",Comp."IdTipoMovimiento")
			   AND Comp."IdConcepto"				= COALESCE(Grupos."IdConcepto",Comp."IdConcepto")
			   AND LENGTH(CompDet."TipoComprobanteInformado") = 0
			UNION ALL
			SELECT	 Grupos."Grupo",1 AS "GrupoDetalle",Grupos."IdComprobante",CompDet."IdTipoMovimiento"
					,Grupos."TipoComprobante" as "IdConcepto",Grupos."TipoComprobante",InfoPartida."IdMoneda"
					,CAST((InfoPartida."Monto" / InfoPartida."CantComprobantesDetalle") AS DECIMAL(26,8)) AS "Monto"
					,'' AS "SecuenciaAgrupacion",0 AS "SecuenciaRelacionada",0  AS "IdEmpleadoDigitador",0  AS "IdSucursalEmpleadoDigitador"
					,'' AS "IdTipoComprobanteOrigen",0  AS "IdComprobanteOrigen",'0001-01-01' AS "FechaComprobanteOrigen",'0001-01-01' AS "FechaLiquidacionComprobanteOrigen"
					,'' AS "IdTipoProducto",'' AS "IdTipoOperacion",'' AS "IdBolsa",'' AS "IdMonedaOperacion",0  AS "MontoOperacion",'0001-01-01' AS "FechaParidad"
					,'' AS "IdTipoReajuste",0  AS "ValorParidad",0  AS "FolioCartera",0  AS "IdInstrumento",'' AS "IdTipoInstrumento",'' AS "IdClasificacionTipoInstrumento"
					,CompDet."IdComprobante" as "IdComprobanteOriginal",CompDet."IdComprobanteDetalle" as "IdComprobanteDetalleOriginal",Grupos."IdCondicionDeLiquidacion" AS "IdCondicionDeLiquidacion"
			  FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_GruposDeComprobantes" AS Grupos
			 INNER JOIN :v_InformacionPartida AS InfoPartida ON InfoPartida."NumeroReferencia" = Grupos."NumeroReferencia"
			 INNER JOIN :v_ComprobanteDetalleAProcesar AS CompDet ON CompDet."NumeroReferencia" = Grupos."NumeroReferencia"
			 WHERE LENGTH(Grupos."TipoComprobante") > 0;
			 
						 
			v_SecuenciaDetalle = 
			SELECT 	 "GrupoDetalle"
					,"BSCL.Tesoreria.BD.Secuencias::ComprobanteDetalle".NEXTVAL AS "IdComprobanteDetalle"
			  FROM :v_ComprobantesDetalleOriginal
			 GROUP BY "GrupoDetalle";
			 
			
			INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteDetalle"
			SELECT   SecDet."IdComprobanteDetalle",CompDet."IdComprobante",CompDet."IdTipoMovimiento",CompDet."IdConcepto",ROW_NUMBER() over (Partition by CompDet."IdComprobante") as "NroLinea"
					,CompDet."IdMoneda",CAST(ABS(SUM(CompDet."Monto")) AS DECIMAL(26,8)) AS "Monto",CompDet."SecuenciaAgrupacion",CompDet."SecuenciaRelacionada"
					,CompDet."IdEmpleadoDigitador",CompDet."IdSucursalEmpleadoDigitador",CompDet."IdTipoComprobanteOrigen",CompDet."IdComprobanteOrigen",CompDet."FechaComprobanteOrigen"
					,CompDet."FechaLiquidacionComprobanteOrigen",CompDet."IdTipoProducto",CompDet."IdTipoOperacion",CompDet."IdBolsa",CompDet."IdMonedaOperacion",CompDet."MontoOperacion"
					,CompDet."FechaParidad",CompDet."IdTipoReajuste",CompDet."ValorParidad",CompDet."FolioCartera",CompDet."IdInstrumento",CompDet."IdTipoInstrumento"
					,CompDet."IdClasificacionTipoInstrumento",CompDet."IdCondicionDeLiquidacion"
			  FROM :v_ComprobantesDetalleOriginal as CompDet
			 INNER JOIN :v_SecuenciaDetalle as SecDet ON SecDet."GrupoDetalle" = CompDet."GrupoDetalle"
			 GROUP BY  SecDet."IdComprobanteDetalle",CompDet."IdComprobante",CompDet."IdTipoMovimiento",CompDet."IdConcepto",CompDet."IdMoneda"
			 		,CompDet."SecuenciaAgrupacion",CompDet."SecuenciaRelacionada",CompDet."IdEmpleadoDigitador",CompDet."IdSucursalEmpleadoDigitador"
			 		,CompDet."IdTipoComprobanteOrigen",CompDet."IdComprobanteOrigen",CompDet."FechaComprobanteOrigen",CompDet."FechaLiquidacionComprobanteOrigen"
					,CompDet."IdTipoProducto",CompDet."IdTipoOperacion",CompDet."IdBolsa",CompDet."IdMonedaOperacion",CompDet."MontoOperacion"
					,CompDet."FechaParidad",CompDet."IdTipoReajuste",CompDet."ValorParidad",CompDet."FolioCartera",CompDet."IdInstrumento"
					,CompDet."IdTipoInstrumento",CompDet."IdClasificacionTipoInstrumento",CompDet."IdCondicionDeLiquidacion";
					  
			INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteDetalleOrigen"
			SELECT	SecDet."IdComprobanteDetalle", CDO."IdIngresoEgreso", CompDet."IdComprobanteOriginal" AS "IdComprobante"
			  FROM :v_ComprobantesDetalleOriginal as CompDet
			 INNER JOIN :v_SecuenciaDetalle as SecDet ON SecDet."GrupoDetalle" = CompDet."GrupoDetalle"
			  LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" as CDO ON CDO."IdComprobanteDetalle" = CompDet."IdComprobanteDetalleOriginal"; 			 
			 
			
			INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteFormaDePago"
			SELECT  Grupos."IdComprobante",CompFP."NumeroReferencia",CompFP."IdTipoMovimiento"
					,CompFP."IdMedioDePago",CompFP."NombreMedioDePago",CompFP."IdModalidadDePago" as "IdModalidadDePago"
					,CompFP."NombreModalidadDePago",CompFP."IdCuentaBancariaCorredora",CompFP."NombreCuentaBancariaCorredora",CompFP."NumeroCuentaBancariaCorredora"
					,CompFP."IdTipoCuentaBancariaCorredora",CompFP."NombreTipoCuentaBancariaCorredora",CompFP."IdTarjetaBancariaCorredora"
					,CompFP."IdTipoTarjetaBancariaCorredora",CompFP."NombreTipoTarjetaBancariaCorredora",CompFP."NumeroTarjetaBancariaCorredora",CompFP."IdChequera"
					,CompFP."NumeroDocumentoCorredora",CompFP."FechaDocumentoPagoCorredora",CompFP."IdCuentaBancariaSocio",CompFP."IdBancoSocio"
					,CompFP."NombreCuentaBancariaSocio",CompFP."NumeroCuentaBancariaSocio",CompFP."IdTipoCuentaBancariaSocio",CompFP."NombreTipoCuentaBancariaSocio"
					,CompFP."IdTarjetaBancariaSocio",CompFP."IdTipoTarjetaBancariaSocio",CompFP."NombreTipoTarjetaBancariaSocio",CompFP."NumeroTarjetaBancariaSocio"
					,CompFP."NumeroSerieSocio",CompFP."NumeroDocumentoSocio",CURRENT_UTCTIMESTAMP AS "FechaDocumentoPagoSocio",CompFP."IdMoneda"
					,CAST(ABS(SUM(CompFP."Monto")) AS DECIMAL(26,8)) AS "Monto",COALESCE(IbC."IdPlaza",0) as "IdPlaza"
					,CASE WHEN (CompFP."IdBancoSocio" = IbC."IdBanco" AND CompFP."IdBancoSocio" > 0) THEN IbC."PlazoRetencionMismoBanco"
						  WHEN (CompFP."IdBancoSocio" <> IbC."IdBanco" AND CompFP."IdBancoSocio" > 0) THEN IbC."PlazoRetencionOtroBanco"
						  ELSE 0
					  END AS "DiasRetencion"
					,'0001-01-01' as "FechaAfectaSaldo"
			  FROM :v_ComprobanteFormaDePagoAProcesar As CompFP
			 INNER JOIN :v_ComprobantesAProcesar AS Comp ON (Comp."IdComprobante" 			= CompFP."IdComprobante")
			 INNER JOIN "BSCL.Tesoreria.BD::GBL.CrearComprobante_GruposDeComprobantes" AS Grupos  ON (Grupos."NumeroReferencia" 		= Comp."NumeroReferencia"
															AND Grupos."FechaLiquidacion"	= Comp."FechaLiquidacion"
															AND Grupos."IdMoneda"			= Comp."IdMoneda")
			  LEFT JOIN :v_InformacionBancoCorredora AS IbC ON (IbC."IdCuentaBanco" = CompFP."IdCuentaBancariaCorredora")
			 WHERE Comp."IdPersonaTitularPago" 		= COALESCE(Grupos."IdPersonaTitularPago",Comp."IdPersonaTitularPago")
			   AND Comp."IdPortafolioTitularPago"	= COALESCE(Grupos."IdPortafolioTitularPago",Comp."IdPortafolioTitularPago") 
			   AND Comp."IdCaja" 					= COALESCE(Grupos."IdCaja",Grupos."IdCaja") 
			   AND Comp."IdTipoMovimiento" 			= COALESCE(Grupos."IdTipoMovimiento",Comp."IdTipoMovimiento")
			   AND Comp."IdConcepto"				= COALESCE(Grupos."IdConcepto",Comp."IdConcepto")
			   AND :v_CountFormaDePago 				= 0
			 GROUP BY  Grupos."Grupo",CompFP."NumeroReferencia",Grupos."IdComprobante",CompFP."IdTipoMovimiento",CompFP."IdMedioDePago",CompFP."NombreMedioDePago"
			 		,CompFP."IdModalidadDePago",CompFP."NombreModalidadDePago",CompFP."IdCuentaBancariaCorredora",CompFP."NombreCuentaBancariaCorredora"
			 		,CompFP."NumeroCuentaBancariaCorredora",CompFP."IdTipoCuentaBancariaCorredora",CompFP."NombreTipoCuentaBancariaCorredora"
			 		,CompFP."IdTarjetaBancariaCorredora",CompFP."IdTipoTarjetaBancariaCorredora",CompFP."NombreTipoTarjetaBancariaCorredora"
					,CompFP."NumeroTarjetaBancariaCorredora",CompFP."IdChequera",CompFP."NumeroDocumentoCorredora",CompFP."FechaDocumentoPagoCorredora"
					,CompFP."IdCuentaBancariaSocio",CompFP."IdBancoSocio",CompFP."NombreCuentaBancariaSocio",CompFP."NumeroCuentaBancariaSocio"
					,CompFP."IdTipoCuentaBancariaSocio",CompFP."NombreTipoCuentaBancariaSocio",CompFP."IdTarjetaBancariaSocio",CompFP."IdTipoTarjetaBancariaSocio"
					,CompFP."NombreTipoTarjetaBancariaSocio",CompFP."NumeroTarjetaBancariaSocio",CompFP."NumeroSerieSocio"
					,CompFP."NumeroDocumentoSocio",CompFP."IdMoneda",IbC."IdPlaza",IbC."IdBanco",IbC."PlazoRetencionMismoBanco",IbC."PlazoRetencionOtroBanco";
			
			INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteFormaDePago"
			SELECT DISTINCT COMP."IdComprobante",Comp."NumeroReferencia",
					CASE WHEN COMP."TipoComprobante" = 'PARING' THEN 'MOVING' WHEN COMP."TipoComprobante" = 'PAREGR' THEN 'MOVEGR' END as "IdTipoMovimiento",				
					FP."IdMedioDePago",FP."NombreMedioDePago",FP."IdModalidadDePago",FP."NombreModalidadDePago",
					FP."IdCuentaBancariaCorredora",COALESCE(FP."NombreCuentaBancaria",'') as "NombreCuentaBancariaCorredora",
					COALESCE(FP."NumeroCuentaBancaria",'') as "NumeroCuentaBancariaCorredora",COALESCE(FP."IdTipoCuentaBancaria",'') as "IdTipoCuentaBancariaCorredora",
	 				COALESCE(FP."NombreTipoCuentaBancaria",'') as "NombreTipoCuentaBancariaCorredora",FP."IdTarjetaBancariaCorredora",
					COALESCE(FP."IdTipoTarjetaBancaria",'') as "IdTipoTarjetaBancariaCorredora",COALESCE(FP."NombreTipoTarjetaBancaria",'') as "NombreTipoTarjetaBancariaCorredora",
					COALESCE(FP."NumeroTarjetaBancaria",'') as "NumeroTarjetaBancariaCorredora",FP."IdChequera",FP."NumeroDocumentoCorredora",FP."FechaDocumentoPagoCorredora",
					FP."IdCuentaBancariaSocio",FP."IdBancoSocio",FP."NombreCuentaBancariaSocio",FP."NumeroCuentaBancariaSocio",FP."IdTipoCuentaBancariaSocio",
					FP."NombreTipoCuentaBancariaSocio",FP."IdTarjetaBancariaSocio",FP."IdTipoTarjetaBancariaSocio",FP."NombreTipoTarjetaBancariaSocio",
					FP."NumeroTarjetaBancariaSocio",FP."NumeroSerieSocio",FP."NumeroDocumentoSocio",FP."FechaDocumentoPagoSocio",FP."IdMoneda",FP."Monto",
					COALESCE(FP."IdPlaza",0) as "IdPlaza", 
					CASE WHEN (FP."IdBancoSocio" = FP."IdBanco" AND FP."IdBancoSocio" > 0) THEN FP."PlazoRetencionMismoBanco"
						 WHEN (FP."IdBancoSocio" <> FP."IdBanco" AND FP."IdBancoSocio" > 0) THEN FP."PlazoRetencionOtroBanco"
						 ELSE 0
					 END AS "DiasRetencion",
					'0001-01-01' as "FechaAfectaSaldo"
			  FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_Comprobante" AS COMP
			 INNER JOIN :v_FormasDePago AS FP 
			 		 ON FP."NumeroReferencia" = COMP."NumeroReferencia"
			 WHERE LENGTH(COMP."TipoComprobante") > 0;
			 
	
			UPDATE CFP
			   SET  "DiasRetencion"	 	= CASE WHEN TMMP."AfectoADiasRetencion" = 'S' THEN CFP."DiasRetencion" ELSE 0 END
			   	   ,"FechaAfectaSaldo"	= CASE WHEN TMMP."AfectoADiasRetencion" = 'S' AND instr(TMMP."AfectaSaldoEnEstado",'ING')> 0 AND COALESCE(CFP."IdCuentaBancariaCorredora",0) > 0
											   THEN ADD_DAYS(TO_DATE(:p_FechaMovimiento, 'YYYY-MM-DD'), CFP."DiasRetencion") || ' ' || TO_VARCHAR (CURRENT_UTCTIMESTAMP,'HH24:MI:SS.FF7')
											   WHEN TMMP."AfectoADiasRetencion" = 'N' AND instr(TMMP."AfectaSaldoEnEstado",'ING')> 0 AND COALESCE(CFP."IdCuentaBancariaCorredora",0) > 0
											   THEN TO_DATE(:p_FechaMovimiento, 'YYYY-MM-DD') || ' ' || TO_VARCHAR (CURRENT_UTCTIMESTAMP,'HH24:MI:SS.FF7')
									           ELSE '0001-01-01'
										   END										   										   
			  FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteFormaDePago" as CFP
			 INNER JOIN "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago" AS TMMP 
				 	 ON TMMP."IdTipoMovimiento" = CFP."IdTipoMovimiento"
			    	AND TMMP."IdMedioDePago" 	= CFP."IdMedioDePago";

		END IF; ---upper(:p_GenerarComprobanteDesdeComprobantes) = 'S')	
		
		
		--SELECT 'v_GruposDeComprobantes', * FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_GruposDeComprobantes"; 			--borrar
		--SELECT 'v_Comprobante', * FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_Comprobante";  							--borrar
		--SELECT 'v_ComprobanteDetalle', * FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteDetalle"; 				--borrar
		--SELECT 'v_ComprobanteDetalleOrigen', * FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteDetalleOrigen"; 	--borrar
		--SELECT 'v_ComprobanteFormaPago', * FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteFormaDePago"; 		--borrar
		
		--***********************************************
		--*VALIDA RESULTADO PROCESO ANTES DE INSERTAR*
		--***********************************************
		INSERT INTO "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Entrada_Comprobante"  
		SELECT "NumeroReferencia","IdComprobante","TipoComprobante","IdTipoMovimiento","IdConcepto","IdCaja","IdMoneda","Monto",
			    Cast("FechaLiquidacion" As NVARCHAR(10)),:p_FechaMovimiento as "FechaMovimiento","IdTipoPersona"
		  FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_Comprobante";
		
		INSERT INTO "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Entrada_ComprobanteFormaDePago" 
		SELECT "NumeroReferencia","IdComprobante","IdMoneda","Monto","IdModalidadDePago" FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteFormaDePago";
						
		CALL "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosCrearComprobantes"(
			p_FechaMovimiento						=>	:p_FechaMovimiento,
			p_IdCaja								=>	:p_IdCaja,
			p_GenerarComprobanteDesdeMovimientos	=>	:p_GenerarComprobanteDesdeMovimientos,
			p_GenerarComprobanteDesdeComprobantes	=>	:p_GenerarComprobanteDesdeComprobantes,
			p_ValidaComprobanteGenerado				=>	'S'
		);	
		SELECT COUNT("NumeroReferencia") INTO v_CountError 
		  FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Salida_Resultado" WHERE "Resultado" = 'RECH' AND "ValidaComprobanteGenerado" = 'S';
		
		IF ( :v_CountError > 0 ) THEN
			INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_TablaCodigosMensajes"
			SELECT "NumeroReferencia" as "IdInterno","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2","Resultado" 
			  FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosCrearComprobantes_Salida_Resultado" WHERE "Resultado" <> 'OK';			
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		
		--*******************************
		--*INSERT TABLAS DE COMPROBANTES*
		--*******************************				
		IF (:p_simularOperacion = 'N') THEN			
			--1)
			INSERT INTO "BSCL.Tesoreria.BD::TES.Comprobante" (
				"IdComprobante",
				"IdEmpresa",
				"IdUsuario",
				"TipoComprobante",
				"IdTipoMovimiento",
				"IdConcepto",
				"IdCaja",
				"IdMoneda",
				"Monto",
				"FechaLiquidacion",
				"FechaIngresoRegistro", 
				"FechaMovimiento",
				"IdTipoPersona",
				"IdPersona",
				"NombrePersona",
				"IdTipoDocumentoIdentidad",
				"DocumentoIdentidad",
				"TitularPago",			
				"IdTipoPersonaTitularPago",
				"IdPersonaTitularPago",
				"NombrePersonaTitularPago",			
				"IdPortafolioTitularPago",			
				"SecuenciaAgrupacion",		
				"EsBilateral",
				"ComprobanteAgrupado",
				"ComprobanteNeteado",
				"SeparadoPorConcepto",
				"GeneradoDesdeMovimientos",
				"MueveCuentaInversion",
				"IdEstado",
                "IdCondicionDeLiquidacion",
                "SecuenciaMovimiento"
	        )
			SELECT 
				MI."IdComprobante",
				:p_IdEmpresa,
	            :p_IdUsuario,
	            MI."TipoComprobante",
				MI."IdTipoMovimiento",
				MI."IdConcepto",
				COALESCE(MI."IdCaja",0),
				MI."IdMoneda",
				"BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MI."Monto",MF."MontoDecimales",MF."MontoTrucaRedondea"),
				MI."FechaLiquidacion",
				CURRENT_UTCTIMESTAMP,
		        :p_FechaMovimiento,
				MI."IdTipoPersona",
				MI."IdPersona",
				MI."NombrePersona",
				MI."IdTipoDocumentoIdentidad",
				MI."DocumentoIdentidad",
				MI."TitularPago",			
				MI."IdTipoPersonaTitularPago",
				MI."IdPersonaTitularPago",
				MI."NombrePersonaTitularPago",
				MI."IdPortafolioTitularPago",			
				MI."SecuenciaAgrupacion",			
				MI."EsBilateral",
				MI."ComprobanteAgrupado",
				MI."ComprobanteNeteado",
				MI."SeparaPorConcepto",
				MI."GeneradoDesdeMovimientos",
				MI."MueveCuentaInversion",
				CASE WHEN MI."TipoComprobante" = 'PAREGR' THEN 'LIQ' ELSE 'ING' END,
				MI."IdCondicionDeLiquidacion",
				"BSCL.Plataforma.BD.Secuencias::SecuenciaMovimiento".NEXTVAL
			FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_Comprobante" as MI
			INNER JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MF 
			 		 ON MF."IdMoneda" 			= MI."IdMoneda" 
			 		AND MF."IdUsoMonedaFormato" = 'GEN'
			 		AND MF."IdTipoInstrumento" 	= ''
			 		AND MF."IdTipoOperacion" 	= '';

			 		
			 		
			--2)
			INSERT INTO "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" (
				"IdComprobanteDetalle",
				"IdComprobante",
				"IdTipoMovimiento",
				"IdConcepto",
				"NroLinea",
				"IdMoneda",
				"Monto",
				"SecuenciaAgrupacion",
				"SecuenciaRelacionada",
				"IdEmpleadoDigitador",
				"IdSucursalEmpleadoDigitador",
				"IdTipoComprobanteOrigen",
				"IdComprobanteOrigen",
				"FechaComprobanteOrigen",
				"FechaLiquidacionComprobanteOrigen",
				"IdTipoProducto",
				"IdTipoOperacion",
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
				"IdEstado",
                "IdCondicionDeLiquidacion"
			)
			SELECT  
				MI."IdComprobanteDetalle",
				MI."IdComprobante",
				MI."IdTipoMovimiento",
				MI."IdConcepto",
				MI."NroLinea",
				MI."IdMoneda",
				"BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(ABS(MI."Monto"),MF."MontoDecimales",MF."MontoTrucaRedondea"),
				MI."SecuenciaAgrupacion",
				MI."SecuenciaRelacionada",
		        MI."IdEmpleadoDigitador",
		        MI."IdSucursalEmpleadoDigitador",
				MI."IdTipoComprobanteOrigen",
				MI."IdComprobanteOrigen",
				MI."FechaComprobanteOrigen",
				MI."FechaLiquidacionComprobanteOrigen",
				MI."IdTipoProducto",
				MI."IdTipoOperacion",
				MI."IdBolsa",
				MI."IdMonedaOperacion",
				MI."MontoOperacion",				
				MI."FechaParidad",
				MI."IdTipoReajuste",
				MI."ValorParidad",
				MI."FolioCartera",
				MI."IdInstrumento",
				MI."IdTipoInstrumento",
				MI."IdClasificacionTipoInstrumento",
	            CASE WHEN MI."IdConcepto" = 'PAREGR' THEN 'LIQ' ELSE 'ING' END,
                MI."IdCondicionDeLiquidacion"
			 FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteDetalle" as MI
			 INNER JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MF 
			 		 ON MF."IdMoneda" 			= MI."IdMoneda" 
			 		AND MF."IdUsoMonedaFormato" = 'GEN'
			 		AND MF."IdTipoInstrumento" 	= ''
			 		AND MF."IdTipoOperacion" 	= '';
			
			
			--3)
			INSERT INTO "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" (
				"IdComprobanteFormaDePago",
				"IdComprobante",
				"IdTipoMovimiento",
				"IdMedioDePago",
				"NombreMedioDePago",
				"IdModalidadDePago",
	
				"NombreModalidadDePago",			 
				"IdCuentaBancariaCorredora",
				"IdTipoCuentaBancariaCorredora",
				"NombreTipoCuentaBancariaCorredora",
				"NombreCuentaBancariaCorredora",
				"NumeroCuentaBancariaCorredora",
				"IdTarjetaBancariaCorredora",
				"IdTipoTarjetaBancariaCorredora",
				"NombreTipoTarjetaBancariaCorredora",
				"NumeroTarjetaBancariaCorredora",
				"IdChequera",
				"NumeroDocumentoCorredora",
				"FechaDocumentoPagoCorredora",	
		 
				"IdCuentaBancariaSocio",
				"IdBancoSocio",
				"NombreCuentaBancariaSocio",
				"NumeroCuentaBancariaSocio",
				"IdTipoCuentaBancariaSocio",
				"NombreTipoCuentaBancariaSocio",			
				"IdTarjetaBancariaSocio",
				"IdTipoTarjetaBancariaSocio",
				"NombreTipoTarjetaBancariaSocio",
				"NumeroTarjetaBancariaSocio",
				"NumeroSerieSocio",
				"NumeroDocumentoSocio",
				"FechaDocumentoPagoSocio",
	
				"IdMoneda",	
				"Monto",
				"IdPlaza",
				"DiasRetencion",
				"FechaIngresoRegistro",
				"FechaAfectaSaldo",		 	
				"IdEstado"
			)
			SELECT	
				"BSCL.Tesoreria.BD.Secuencias::ComprobanteFormaDePago".NEXTVAL,
				MI."IdComprobante",
				MI."IdTipoMovimiento",
				MI."IdMedioDePago",
				MI."NombreMedioDePago",
				MI."IdModalidadDePago",
				MI."NombreModalidadDePago",
	
				MI."IdCuentaBancariaCorredora",
				MI."IdTipoCuentaBancariaCorredora",
				MI."NombreTipoCuentaBancariaCorredora",
				MI."NombreCuentaBancariaCorredora",
				MI."NumeroCuentaBancariaCorredora",
				MI."IdTarjetaBancariaCorredora",
				MI."IdTipoTarjetaBancariaCorredora",
				MI."NombreTipoTarjetaBancariaCorredora",
				MI."NumeroTarjetaBancariaCorredora",
				MI."IdChequera",
				MI."NumeroDocumentoCorredora",
				MI."FechaDocumentoPagoCorredora",
				MI."IdCuentaBancariaSocio",
				MI."IdBancoSocio",
				MI."NombreCuentaBancariaSocio",
				MI."NumeroCuentaBancariaSocio",
				MI."IdTipoCuentaBancariaSocio",
				MI."NombreTipoCuentaBancariaSocio",			
				MI."IdTarjetaBancariaSocio",
				MI."IdTipoTarjetaBancariaSocio",
				MI."NombreTipoTarjetaBancariaSocio",
				MI."NumeroTarjetaBancariaSocio",			
				MI."NumeroSerieSocio",
				MI."NumeroDocumentoSocio",
				MI."FechaDocumentoPagoSocio",
	
				MI."IdMoneda",
				"BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MI."Monto",MF."MontoDecimales",MF."MontoTrucaRedondea"),
				MI."IdPlaza",
				MI."DiasRetencion",
				CURRENT_UTCTIMESTAMP,
				MI."FechaAfectaSaldo",
				CASE WHEN CO."TipoComprobante" = 'PAREGR' THEN 'LIQ' ELSE 'ING' END
			  FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteFormaDePago" as MI
			  INNER JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MF 
			 		 ON MF."IdMoneda" 			= MI."IdMoneda" 
			 		AND MF."IdUsoMonedaFormato" = 'GEN'
			 		AND MF."IdTipoInstrumento" 	= ''
			 		AND MF."IdTipoOperacion" 	= ''
			  INNER JOIN "BSCL.Tesoreria.BD::GBL.CrearComprobante_Comprobante" as CO
			  		ON MI."IdComprobante" = CO."IdComprobante";
			
			--4)
			INSERT INTO "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen"( 
						"IdComprobanteDetalleOrigen",
						"IdComprobanteDetalle",
						"IdIngresoEgreso",
						"IdComprobante"
		    		  )
			SELECT  "BSCL.Tesoreria.BD.Secuencias::ComprobanteDetalleIngresoEgreso".NEXTVAL,
					MI."IdComprobanteDetalle",
					MI."IdIngresoEgreso",
					MI."IdComprobante"
			 FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteDetalleOrigen" as MI;
			
		    
		    --********************************
			--* CAMBIO DE ESTADO MOVIMIENTOS *
			--********************************
			UPDATE IE    
			   SET "IdEstado" 			= 'EMI', 
			   	   "IdUsuarioEmision"	= :p_IdUsuario,
			   	   "FechaHoraEmision"	= TO_DATE(:p_FechaMovimiento, 'YYYY-MM-DD') || ' ' || TO_VARCHAR (CURRENT_UTCTIMESTAMP,'HH24:MI:SS.FF7')
			  FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" IE
			 INNER JOIN :v_IngresoEgresoGenerar IEG ON IE."IdIngresoEgreso" = IEG."IdIngresoEgreso";
			
			UPDATE IEFP    
			   SET "IdEstado" = 'EMI'
			  FROM "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago" IEFP
			 INNER JOIN :v_IngresoEgresoGenerar IEG ON IEFP."IdIngresoEgreso" = IEG."IdIngresoEgreso"
			 WHERE IEFP."IdEstado" <> 'MOD';
		

			--********************************
			--* CAMBIO DE ESTADO COMPROBANTES *
			--********************************
			--Nota validar los estados con que queda el comprobante
			--Solo se actualizaran aquellos comprobantes que provienen de movimientos
			IF (upper(:p_GenerarComprobanteDesdeMovimientos) = 'S') THEN
				UPDATE Comp
				SET "IdEstado" = 'MOD'
				FROM "BSCL.Tesoreria.BD::TES.Comprobante" Comp
				INNER JOIN :v_ComprobanteAGenerar CompG ON Comp."IdComprobante" = CompG."IdComprobante"			             
				WHERE LENGTH(CompG."TipoComprobante") = 0			 
				AND CompG."TipoComprobanteInformado" <> 'PAREGR';  -- Para PAREGR quedan con estado girado 
						
				UPDATE CompDet
				SET "IdEstado" = 'MOD'
				FROM "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" CompDet
				INNER JOIN :v_ComprobanteAGenerar CompG ON CompDet."IdComprobante" = CompG."IdComprobante"
				WHERE LENGTH(CompG."TipoComprobante") = 0			
				AND CompG."TipoComprobanteInformado" <> 'PAREGR';
				
			
				UPDATE CompFP
				SET "IdEstado" = 'MOD'
				FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" CompFP
				INNER JOIN :v_ComprobanteAGenerar CompG ON CompFP."IdComprobante" = CompG."IdComprobante"
				WHERE LENGTH(CompG."TipoComprobante") = 0
				AND CompG."TipoComprobanteInformado" <> 'PAREGR';
			END IF;
	
			--********************************
			--* ACTUALIZAR SALDOS            *
			--********************************
			v_CuentaBanco =
			SELECT DISTINCT "IdCuentaBancariaCorredora" as "IdCuentaBanco"
			  FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteFormaDePago"
			 WHERE COALESCE("IdCuentaBancariaCorredora",0) > 0;
	
			CALL "BSCL.Tesoreria.BD.Servicios.BAN::ActualizarSaldosCuentaBanco" (
				p_FechaMovimiento 	=> :p_FechaMovimiento,
				p_CuentaBanco		=> :v_CuentaBanco
			);
		END IF;
		
		--********************************
		--* RESULTADO DEL PROCESO *
		--********************************
		INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_ResultadoProceso"
		SELECT  'IdComprobante' as "Campo",
				"IdComprobante" as "Id",
				'BSCL.Tesoreria.BD.Servicios::CrearComprobante' as "Servicio",
				'ING' as "Resultado"
		  FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_Comprobante"		--> Comprobantes generados desde movimientos o comprobantes
		 UNION ALL
		 SELECT 'IdIngresoEgreso' as "Campo",
				"IdIngresoEgreso" as "Id",
				'BSCL.Tesoreria.BD.Servicios::CrearComprobante' as "Servicio",
				'EMI' as "Resultado"
		  FROM :v_IngresoEgresoGenerar
		 UNION ALL
		 SELECT 'IdComprobante' as "Campo",
				"IdComprobante" as "Id",
				'BSCL.Tesoreria.BD.Servicios::CrearComprobante' as "Servicio",
				'MOD' as "Resultado"
		  FROM :v_ComprobanteAGenerar;


		INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_TablaCodigosMensajes"
		SELECT  "IdComprobante" AS "IdInterno"
			   ,CASE WHEN "IdTipoMovimiento" = 'MOVEGR' THEN 'UI_CREADO_COMPROBANTE_EGRESO' 
			   		 WHEN "IdTipoMovimiento" = 'MOVING' THEN 'UI_CREADO_COMPROBANTE_INGRESO'
			   	 END as "Codigo"
			   ,'IdComprobante' as "CodigoDato1"
			   ,CAST("IdComprobante" as NVARCHAR(520)) as "ValorDato1"
			   ,'' as "CodigoDato2"
			   ,'' as "ValorDato2"
			   ,'OK' as "Resultado" 
		  FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_Comprobante";
		SIGNAL SALIDA_CONTROLADA;
  	END;
END;
