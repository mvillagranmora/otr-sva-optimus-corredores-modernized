SET SCHEMA "BSCL"; 
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::OPE.CrearEnvioOperacionesPendientes";
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::OPE.CrearEnvioOperacionesPendientes" (

--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          				BIGINT 			default 0,
    IN p_IdUsuario          				BIGINT 			default 0,
    IN p_IdModulo           				NVARCHAR(3) 	default '',  


	-- Parametros de Salida
	OUT p_out_InformacionContador  			INT,
	OUT p_out_InformacionDetalle   			TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,
	
	OUT p_out_AdvertenciasContador 			INT,
	OUT p_out_AdvertenciasDetalle 			table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)) DEFAULT EMPTY,	
	
	OUT p_out_ErroresContador 				INT,   
	OUT p_out_ErroresDetalle 				table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000)) DEFAULT EMPTY,

	OUT p_out_IdInterno 					bigint
)   
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
	
BEGIN SEQUENTIAL EXECUTION
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE v_FechaSistema		TIMESTAMP;
	DECLARE SALIDA_CONTROLADA  	CONDITION;	       	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		IF (NOT IS_EMPTY("BSCL.Custodia.BD::GBL.CrearEventoCapital_SRV_TablaCodigosMensajes")) THEN
			
			v_Mensajes = SELECT "IdInterno","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2" FROM "BSCL.Custodia.BD::GBL.CrearEventoCapital_SRV_TablaCodigosMensajes";
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_Mensajes,v_erroroutmsg);
												
			INSERT INTO "BSCL.Custodia.BD::GBL.CrearEnvioOperacionesPendientes_OUT_InformacionDetalle"							
			SELECT TCM."IdInterno",EM."Codigo",EM."Descripcion" 
			  FROM :v_erroroutmsg EM 
			 INNER JOIN "BSCL.Custodia.BD::GBL.CrearEnvioOperacionesPendientes_SRV_TablaCodigosMensajes" TCM 
			 		 ON TCM."IdInterno" = EM."IdInterno" AND TCM."Codigo" = EM."Codigo" WHERE TCM."Resultado" = 'OK';		
			
			INSERT INTO "BSCL.Custodia.BD::GBL.CrearEnvioOperacionesPendientes_OUT_AdvertenciasDetalle"
			SELECT TCM."IdInterno",EM."Codigo",TCM."Resultado" as "RequiereAutorizacion",EM."Descripcion" 
			  FROM :v_erroroutmsg EM 
			 INNER JOIN "BSCL.Custodia.BD::GBL.CrearEnvioOperacionesPendientes_SRV_TablaCodigosMensajes" TCM 
			 		 ON TCM."IdInterno" = EM."IdInterno" AND TCM."Codigo" = EM."Codigo" WHERE TCM."Resultado" IN ('ADVE','RAUT');
						
			INSERT INTO "BSCL.Custodia.BD::GBL.CrearEnvioOperacionesPendientes_OUT_ErroresDetalle"
			SELECT TCM."IdInterno",EM."Codigo",EM."Descripcion" 
			  FROM :v_erroroutmsg EM 
			 INNER JOIN "BSCL.Custodia.BD::GBL.CrearEnvioOperacionesPendientes_SRV_TablaCodigosMensajes" TCM 
			 		 ON TCM."IdInterno" = EM."IdInterno" AND TCM."Codigo" = EM."Codigo" WHERE TCM."Resultado" = 'RECH';
		END IF;
		
		SELECT count(1) into p_out_InformacionContador FROM "BSCL.Custodia.BD::GBL.CrearEnvioOperacionesPendientes_OUT_InformacionDetalle";
		SELECT count(1) into p_out_AdvertenciasContador FROM "BSCL.Custodia.BD::GBL.CrearEnvioOperacionesPendientes_OUT_AdvertenciasDetalle";
		SELECT count(1) into p_out_ErroresContador FROM "BSCL.Custodia.BD::GBL.CrearEnvioOperacionesPendientes_OUT_ErroresDetalle";
	END;
	/*
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DECLARE v_errorExt 	NVARCHAR(40);	
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);
			INSERT INTO "BSCL.Custodia.BD::GBL.CrearOperacionesPendientesCCLV_OUT_ErroresDetalle" SELECT 0,v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;
			SIGNAL SALIDA_CONTROLADA;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL;
		END IF;	
	END;
	*/
	p_out_InformacionContador	:= 0;
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
		
	BEGIN
	
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
		DECLARE v_FechaSistema 			TIMESTAMP;
		DECLARE v_Secuencia				BIGINT := 0;
		DECLARE v_Valor					BIGINT := 0;
		
	    CALL  "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (p_IdEmpresa,v_FechaSistema);
	             
		-----------------------------------------------------
		--Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		BEGIN
			DECLARE v_resultadoValidacion 	NVARCHAR(40);
	
		END;
		-----------------------------------------------------
		--fin Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------


		-----------------------------------------------------
		--proceso de Negocio
		---------------------------------------------------
		BEGIN
		
--	    	SELECT "BSCL.Custodia.BD.IdInterno::OperacionesPendientesCCLV".NEXTVAL INTO p_out_IdInterno FROM DUMMY;
		
			INSERT INTO "BSCL.Custodia.BD::CUS.EnvioOperacionesPendientes"
			    (
				 "IdInterno"
				,"FolioOperacion"
				,"IdTipo"
				,"FechaIngresoRegistro"
				,"IdTipoOperacion"
				,"TipoOperacion"
				,"NombreOperacion"
				,"IdTipoInstrumento"
				,"Nemo"
				,"IdClasificacionTipoInstrumento"
				,"IdEmpresa"
				,"IdCondicionLiquidacion"
				,"IdOperacion"
				,"OpDirecto"
				,"FechaMovimientoComprobante"
				,"FechaVencimiento"
				,"DocumentoIdentidad"
				,"IdPortafolio"
				,"FolioComprobante"
				,"FolioComprobanteReferencia"
				,"IdOrden"
				,"Cantidad"
				,"Precio"
				,"Tasa"
				,"Monto"
				,"MontoMovimientoComprobante"
				,"IdEstado"
				,"IdAgente"
				,"IdMoneda"
				,"IdTipoGarantia"
				,"FondoAfp"
				,"IdTipoComprobante"
				,"IdTipoProducto"
				,"Producto"
				,"IdBolsa"
				,"DocumentoIdentidadEntidad"
				,"IndDcv"
				,"FolioTransaccion"
				,"UsarioEnvio"
				,"EstadoCclv"
				,"EstadoCclvDesc"
				,"FechaEjecucion"
				,"NemoSVS"
				,"Categoria"
				)
				
			SELECT 
			"BSCL.Custodia.BD.IdInterno::EnvioOperacionesPendientes".NEXTVAL,
			"FolioOperacion",
			"IdTipo",
			"FechaIngresoRegistro",
			"IdTipoOperacion",
			"TipoOperacion",
			"NombreOperacion",
			"IdTipoInstrumento",
			"Nemo",
			"IdClasificacionTipoInstrumento",
			p_IdEmpresa,
			"IdCondicionLiquidacion",
			"IdOperacion",
			"OpDirecto",
			"FechaMovimientoComprobante",
			"FechaVencimiento",
			"DocumentoIdentidad",
			"IdPortafolio",
			"FolioComprobante",
			"FolioComprobanteReferencia",
			"IdOrden",
			"Cantidad",
			"Precio",
			"Tasa",
			"Monto",
			"MontoMovimientoComprobante",
			"IdEstado",
			"IdAgente",
			"IdMoneda",
			"IdTipoGarantia",
			"FondoAfp",
			"IdTipoComprobante",
			"IdTipoProducto",
			"Producto",
			"IdBolsa",	
			"DocumentoIdentidadEntidad",
			"IndDcv",
			"FolioTransaccion",
			"UsarioEnvio",
			"EstadoCclv",
			"EstadoCclvDesc",
			"FechaEjecucion",
			"NemoSVS",
			"Categoria"
			FROM 
			"BSCL.Custodia.BD::GBL.EnvioOperacionesPendientes_Entrada_EnvioOperacionesPendientes";

			
		END;
		
		---------------------------------------------------
		--fin proceso de Negocio
		---------------------------------------------------
	END;
END; 
