SET SCHEMA "BSCL";
                                             
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::ModificarIngresoEgreso";

CREATE PROCEDURE 
-- 
--  Proposito: Modificar movimiento de ingreso/egreso de tesoreria
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 12/09/2016
-- 

"BSCL.Tesoreria.BD.Servicios::ModificarIngresoEgreso"(                                                    
   -- Parametros de Validacion                                                                                            
	IN  p_IdEmpresa					BIGINT,
   	IN  p_IdUsuario					BIGINT,
   	IN  p_IdModulo					NVARCHAR(3),                                                                       
   	IN  p_simularOperacion			NVARCHAR(1),	-- S/N se usa para simular operacion (algunos casos)                  
   	IN  p_UsaReglasNegocio			NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.                          
	IN  p_IngresoEgresoModificar    "BSCL.Tesoreria.BD.Tipos::TES.IngresoEgresoEditar",
	IN  p_FormaDePago               "BSCL.Tesoreria.BD.Tipos::TES.FormaDePago",	

   	-- Parametros de Salida
	OUT p_out_IngresoEgresoModificado	TABLE("NumeroReferencia"	BIGINT, "IdTipoMovimiento" NVARCHAR(6)),	
   	OUT p_out_ResultadoProceso			TABLE("Campo" VARCHAR(40),"Id" BIGINT, "Servicio" VARCHAR(250), "Resultado" VARCHAR(40)),   	   
   	OUT p_out_AdvertenciasContador		INT,
   	OUT p_out_AdvertenciasDetalle		TABLE ("NumeroReferencia"	BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
   	OUT p_out_ErroresContador			INT,
   	OUT p_out_ErroresDetalle			TABLE ("NumeroReferencia"	BIGINT, "Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
) 
   
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
  	--------------------
	--DECLARE Manejo de Errores
	--------------------	  
	DECLARE SALIDA_CONTROLADA 			CONDITION; 
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;
	DECLARE v_errorcuenta 				INT DEFAULT 0;  
	DECLARE v_errorhashmsg 				TABLE ("Codigo" NVARCHAR(100),"Valor" NVARCHAR(5000));
	DECLARE v_erroroutmsg  				NVARCHAR(5000);		
	DECLARE v_ErrorNoData  				NVARCHAR(100);
	DECLARE v_resultadoRegla 			NVARCHAR(40);
	DECLARE v_ErroresDetalle			TABLE ("NumeroReferencia" BIGINT,"Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520)); 	

	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	END;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN
		v_hash= SELECT '' as "Codigo",'' as "Valor" FROM DUMMY;	
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_ErrorNoData,:v_hash,:v_erroroutmsg);
		p_out_ErroresDetalle =  SELECT 0 as "NumeroReferencia", :v_ErrorNoData  as "Codigo",:v_erroroutmsg as "Descripcion" FROM dummy
								UNION ALL  
								SELECT * FROM :p_out_ErroresDetalle;
		SELECT COUNT(1) INTO p_out_ErroresContador
		FROM :p_out_ErroresDetalle;
	END;
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL
								   SELECT 0 as "NumeroReferencia", v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;
			SELECT count(1) into p_out_ErroresContador 
			FROM :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;
	
	p_out_AdvertenciasContador		:= 0;
	p_out_ErroresContador			:= 0;
	p_out_AdvertenciasDetalle		= SELECT 0 as "NumeroReferencia", '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" FROM dummy where 1=2;
	p_out_ErroresDetalle			= SELECT 0 as "NumeroReferencia",'' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;
	p_out_ResultadoProceso			= SELECT '' as "Campo", 0 as "Id", '' as "Servicio", '' as "Resultado" FROM dummy where 1=2;
	p_out_IngresoEgresoModificado	= SELECT 0 as "NumeroReferencia", '' as "IdTipoMovimiento" FROM dummy where 1=2;
	v_ErroresDetalle				= SELECT 0 as "NumeroReferencia",'' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;


	--------------------------------------------------
	-- VALIDACIONES
	--------------------------------------------------
	IF p_UsaReglasNegocio = 'S' THEN
	BEGIN
	
		---Validaciones datos de entrada			
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_out_ErroresDetalle,:v_resultadoRegla);		
		p_out_ErroresDetalle = SELECT 0 As "NumeroReferencia", "Codigo","Descripcion" FROM :v_out_ErroresDetalle;					
					
		IF :v_resultadoRegla = 'RECH' THEN
			 SIGNAL SALIDA_CONTROLADA; 	
		END IF;

		-- Se llenan tablas temporales globales para Servicio Cumple Requisito
		INSERT INTO "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Entrada_IngresoEgresoEditar"
		SELECT * FROM :p_IngresoEgresoModificar;
	
		INSERT INTO "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Entrada_FormaDePago"
		SELECT * FROM :p_FormaDePago;

		CALL "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosEditarIngresoEgreso"(:p_IdEmpresa,:p_IdModulo,'MODIFICAR',:v_resultadoRegla);
		
		IF :v_resultadoRegla = 'RECH' THEN			
			p_out_ResultadoProceso =
			SELECT  'IdIngresoEgreso' as "Campo",
					"NumeroReferencia" as "Id",
					'BSCL.Tesoreria.BD.Servicios::ModificarIngresoEgreso' as "Servicio",
					'RECH' as "Resultado"
			  FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Salida_MovimientosRechazados";

			p_out_ErroresDetalle = SELECT "NumeroReferencia", "Codigo","Descripcion" FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Salida_ErroresDetalle";			
			
			SIGNAL SALIDA_CONTROLADA; 	
		ELSEIF :v_resultadoRegla = 'ADVE' THEN
			--LLamada al servicio de autorizacion si se requiere
			p_out_AdvertenciasDetalle = SELECT "NumeroReferencia", "Codigo",'N' as "RequiereAutorizacion", "Descripcion" FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Salida_ErroresDetalle";
			SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
			
		END IF;  -- IF :v_resultadoRegla = 'RECH'
			
	END; 
	END IF;  -- IF p_UsaReglasNegocio = 'S'


	--------------------------------------------------
	-- SIMULACION
	--------------------------------------------------
	IF (:p_simularOperacion = 'S') THEN
		SIGNAL SALIDA_CONTROLADA;
	END IF;
	

	--------------------
	--LOGICA
	--------------------
	BEGIN

		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;
		
		
		--------------------------------------------   
		-- OBTENER DATOS ADICIONALES
		--------------------------------------------
		
		
		-- Se eliminan los movimientos que no pasaron la validacion
		v_IngresoEgresoModificar =
	 	SELECT * 
	 	FROM :p_IngresoEgresoModificar
	 	WHERE "NumeroReferencia" NOT IN (SELECT "NumeroReferencia" FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Salida_MovimientosRechazados");
	 	 
		-- Se eliminan las FP de los movimientos que no pasaron la validacion
	 	v_TableFormaPago =
	 	SELECT * 
	 	FROM :p_FormaDePago 
	 	WHERE "NumeroReferencia" NOT IN (SELECT "NumeroReferencia" FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Salida_MovimientosRechazados");

		
		
		------------------------------------------------------------
		-- MODIFICA EL MOVIMIENTO DE INGRESO/EGRESO
		------------------------------------------------------------

		UPDATE IE
		SET
			 "IdTipoMovimiento" 				= IEM."IdTipoMovimiento"
			,"IdConcepto" 						= IEM."IdConcepto"
			,"IdCaja" 							= IEM."IdCaja"
			,"IdMoneda" 						= IEM."IdMoneda"
			,"Monto" 							= IEM."Monto"
			,"FechaLiquidacion"  				= IEM."FechaLiquidacion"
			,"IdTipoPersona" 					= IEM."IdTipoPersona"
			,"IdPersona" 						= IEM."IdPersona"			
			,"NombrePersona" 					= PER."NombreCompleto"
			,"IdTipoDocumentoIdentidad" 		= PER."TipoDocumentoIdentidad"
			,"DocumentoIdentidad" 				= PER."DocumentoIdentidad"
			,"IdTipoPersonaTitularPago" 		= IEM."IdTipoPersonaTitularPago"
 			,"IdPersonaTitularPago" 			= IEM."IdPersonaTitularPago"
 			,"NombrePersonaTitularPago" 		= PTP."NombreCompleto"
 			,"IdPortafolioTitularPago" 			= IEM."IdPortafolioTitularPago"
			,"SecuenciaAgrupacion" 				= IEM."SecuenciaAgrupacion"
			,"Glosa" 							= IEM."Glosa"
			,"IdEmpleadoDigitador"              = IEM."IdEmpleadoDigitador"
			,"IdSucursalEmpleadoDigitador"      = IEM."IdSucursalEmpleadoDigitador"
			,"IdTipoComprobanteOrigen" 			= IEM."IdTipoComprobante"
			,"IdComprobanteOrigen" 				= IEM."IdComprobante"
			,"FechaComprobanteOrigen" 			= IEM."FechaComprobante"
			,"FechaLiquidacionComprobanteOrigen" = IEM."FechaLiquidacionComprobante"
			,"IdTipoProducto" 					= IEM."IdTipoProducto"
			,"IdTipoOperacion" 					= IEM."IdTipoOperacion"
			,"IdOperacion" 						= IEM."IdOperacion"
 			,"FechaOperacion" 					= IEM."FechaOperacion"
 			,"IdCondicionLiquidacion" 			= IEM."IdCondicionLiquidacion"
			,"IdBolsa" 							= IEM."IdBolsa"
			,"IdMonedaOperacion" 				= IEM."IdMonedaOperacion"
			,"MontoOperacion"					= IEM."MontoOperacion"
			,"FechaParidad"						= IEM."FechaParidad"
			,"IdTipoReajuste"					= IEM."IdTipoReajuste"
			,"ValorParidad"						= IEM."ValorParidad"			    
			,"FolioCartera"						= IEM."FolioCartera"
			,"IdInstrumento"					= IEM."IdInstrumento"
			,"IdTipoInstrumento"				= IEM."IdTipoInstrumento"
			,"IdClasificacionTipoInstrumento"	= IEM."IdClasificacionTipoInstrumento"			
			,"EsBilateral"						= IEM."Bilateral"
			,"TitularPago"						= IEM."TitularPago"
			,"AgrupaComprobante"				= IEM."AgruparEnComprobante"
			,"MueveCuentaInversion"				= IEM."MueveCuentaInversion"
			,"IdUsuarioModificacion" 			= :p_IdUsuario
			,"FechaHoraModificacion" 			= CURRENT_UTCTIMESTAMP

		FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS IE
		INNER JOIN :v_IngresoEgresoModificar AS IEM ON (IEM."NumeroReferencia" = IE."IdIngresoEgreso")
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER ON (PER."IdPersona" = IEM."IdPersona")
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PTP ON (PTP."IdPersona" = IEM."IdPersonaTitularPago");




		------------------------------------------------------------
		-- MODIFICACION FORMA DE PAGO INGRESO/EGRESO
		------------------------------------------------------------
		-- Anula FP actual
	    UPDATE IEFP
		SET "IdEstado" = 'MOD'
		FROM "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago" AS IEFP
		INNER JOIN :v_IngresoEgresoModificar  AS IEM  ON (IEM."NumeroReferencia" = IEFP."IdIngresoEgreso");

		
		-- Inserta nueva FP			
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
			,IEM."NumeroReferencia" AS "IdIngresoEgreso"
			,IEM."IdTipoMovimiento"			
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
			,FP."Monto"
			,CURRENT_UTCTIMESTAMP
			,'ING'			
		FROM :v_TableFormaPago AS FP		
		INNER JOIN :v_IngresoEgresoModificar AS IEM ON (IEM."NumeroReferencia" = FP."NumeroReferencia")
	 	LEFT OUTER JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" AS MeP ON (MeP."IdMedioDePago" = FP."IdMedioDePago")
		LEFT OUTER JOIN "BSCL.Plataforma.BD::PAG.ModalidadDePago" AS MoP ON (MoP."IdModalidadDePago" = FP."IdModalidadDePago")
		LEFT OUTER JOIN "BSCL.Tesoreria.BD::TES.CuentaBanco" AS CBC ON (CBC."IdCuentaBanco" = FP."IdCuentaBancariaCorredora")
		LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTarjetasBancarias" AS TBC ON (TBC."IdTarjetaBancaria" = FP."IdTarjetaBancariaCorredora")
		LEFT OUTER JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" AS TCBC ON (TCBC."IdTipoCuentaBancaria" = CBC."IdTipoCuentaBancaria");


		------------------------------------------------------------
		-- RESULTADO PROCESO
		------------------------------------------------------------
		p_out_ResultadoProceso =
		SELECT  'IdIngresoEgreso' as "Campo",
				"NumeroReferencia" as "Id",
				'BSCL.Tesoreria.BD.Servicios::ModificarIngresoEgreso' as "Servicio",
				'MOD' as "Resultado"	
		FROM :v_IngresoEgresoModificar
		UNION ALL
		SELECT  'IdIngresoEgreso' as "Campo",
				"NumeroReferencia" as "Id",
				'BSCL.Tesoreria.BD.Servicios::ModificarIngresoEgreso' as "Servicio",
				'RECH' as "Resultado"
		FROM "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Salida_MovimientosRechazados"
		UNION ALL
		
		SELECT "Campo","Id","Servicio","Resultado" 
		FROM :p_out_ResultadoProceso;

		
		 
		p_out_IngresoEgresoModificado =
		SELECT IEM."NumeroReferencia",IEM."IdTipoMovimiento"
		FROM :v_IngresoEgresoModificar AS IEM;
		
		 
  	END;
	--LOG DE AUDITORIA
	BEGIN
	DECLARE p_Log TABLE(
	"IdCabeceraLog"    BIGINT,
	"IdTipoLog"     NVARCHAR(40),
	"IdModulo"     NVARCHAR(3),
	"IdUsuario"     BIGINT,
	"IdEmpresa"     BIGINT,
	"FechaSistemaLog"   NVARCHAR(10),
	"IdUsuarioAtendido"   BIGINT,
	"IdPersona"     BIGINT,
    "IdCliente"     BIGINT,
	"EstadoCliente"    NVARCHAR(3),
	"NombreCliente"    NVARCHAR(100),
	"IdTipoDocumentoIdentidad" NVARCHAR(40),
	"IdDocumentoIdentidad"  NVARCHAR(40),
	"IdPortafolio"    BIGINT,
	"EstadoPortafolio"   NVARCHAR(3),
	"IdTipoRegistro"   NVARCHAR(40),
	"IdRegistro"    NVARCHAR(40),
	"EstadoRegistro"   NVARCHAR(3),
	"IdTipoOperacion"   NVARCHAR(40),
	"Folio"      BIGINT,
	"Cantidad"     DECIMAL(26,8),
	"PrecioTasa"    DECIMAL(26,8),
	"Monto"      DECIMAL(26,8),
	"IdMonedaOrigen"   NVARCHAR(3),
	"IdMonedaLiquidacion"  NVARCHAR(3),
	"TipoCambio"    NVARCHAR(40),
	"Nemo"      NVARCHAR(260),
	"IdProceso"     BIGINT,
	"EstadoProceso"    NVARCHAR(3),
	"NombreProceso"    NVARCHAR(100)
	);
	DECLARE v_fechaHoraSistema NVARCHAR(40);
	call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
	p_IdEmpresa => p_IdEmpresa,
	p_out_FechaHoraSistema => v_fechaHoraSistema
	);
	p_Log =
	Select
	"BSCL.Plataforma.BD.Secuencias::Log".NEXTVAL as "IdCabeceraLog"
	,'MODIFICACION' as "IdTipoLog"
	,p_IdModulo as "IdModulo"
	,p_IdUsuario as "IdUsuario"
	,p_IdEmpresa as "IdEmpresa"
	,TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD') as "FechaSistemaLog"
	,p_IdUsuario as "IdUsuarioAtendido"
	,COALESCE(entrada."IdPersona", 0) as "IdPersona"
    ,0 as "IdCliente"
	,'' as "EstadoCliente"
	,'' as "NombreCliente"
	,'' as "IdTipoDocumentoIdentidad"
	,'' as "IdDocumentoIdentidad"
	,COALESCE(entrada."IdPortafolio", 0) as "IdPortafolio"
	,'' as "EstadoPortafolio"
	,'EDITAR_TESORERIA' as "IdTipoRegistro"
	,'' as "IdRegistro"
	,'' as "EstadoRegistro"
	,COALESCE(entrada."IdTipoOperacion", '') as "IdTipoOperacion"
	,COALESCE(entrada."IdComprobante", 0) as "Folio"
	,0 as "Cantidad"
	,0 as "PrecioTasa" 
	,COALESCE(entrada."Monto", 0) as "Monto"
	,COALESCE(entrada."IdMoneda", '') as "IdMonedaOrigen"
	,'' as "IdMonedaLiquidacion"
	,'' as "TipoCambio"
	,'' as "Nemo"
	,0 as "IdProceso"
	,'' as "EstadoProceso"
	,'ModificarEngresoIngreso Tesoreria' as "NombreProceso"
	FROM :p_IngresoEgresoModificar as entrada; 
	CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (
	p_Log   => :p_Log
	);
	END;
 --LOG DE AUDITORIA (Fin)
END;