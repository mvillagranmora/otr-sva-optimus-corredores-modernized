SET SCHEMA "BSCL";
GO 
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.LIQ::ProcesarLiquidacionBilateral";

--  Proposito: Procesa las transacciones bilaterales(que no se liquidan por SCL) para el pago a las corredoras contrapartes.
--             NOTA: 
--             _ Genera movimientos y comprobantes de tesoreria para la contraparte
--             _ Marca transacciones(palos) con folios de comprobantes de tesoreria generados
--             _ Genera cargo/abono en cta inversion de las contrapartes(con parametro encendido)
--               Para esto las contrapartes debe estar creadas como cliente para que tengan portafolio
--  Autor: JVILLAGRAN - BCS 
--  Fecha Creacion: 25/08/2016
-- 
GO
CREATE PROCEDURE "BSCL.Tesoreria.BD.Servicios.LIQ::ProcesarLiquidacionBilateral"
  (
  
    IN p_IdEmpresa             				BIGINT,
    IN p_IdUsuario              			BIGINT,
    IN p_IdModulo               			NVARCHAR(3),
    IN p_UsaReglasNegocio					NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
    
    -- VARIABLES DE ENTRADA
    IN p_FechaSistema                       NVARCHAR(10),
    IN p_FechaLiquidacion			    	NVARCHAR(10),
    IN p_ClasificacionTipoInstrumento  		NVARCHAR(3)    DEFAULT '', -- RV,RF,IF
    IN p_TipoInstrumento               		NVARCHAR(3)    DEFAULT '', -- ACN
    IN p_CondicionDeLiquidacion        		NVARCHAR(2)    DEFAULT '', -- T0,T1,T2,T3
    IN p_IdAgrupacionAProcesar        		table ("IdAgrupacion" int),

	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
	
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA BSCL
AS	
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE v_LocalizacionSistema NVARCHAR(2);
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	  	
	DECLARE v_ErroresDetalle			TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000));
	DECLARE v_RechazadosPorValidacion	TABLE ("IdAgrupacion" BIGINT);
	DECLARE v_AdvertenciasContador		INT;
	DECLARE v_ErroresContador			INT;
	DECLARE v_InformacionContador	  	INT;
	DECLARE v_LiquidacionBilateralMueveCtaInversion NVARCHAR(1);
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;
		
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;

	v_InformacionContador		:= 0;
	v_AdvertenciasContador		:= 0;
	v_ErroresContador			:= 0;
	v_ErroresDetalle			= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;
	v_RechazadosPorValidacion	= SELECT 0 as "IdAgrupacion" FROM dummy where 1=2;


	
	
	-- Optiene localizacion sistema(CL|PE)
	SELECT "Valor"
	INTO v_LocalizacionSistema
	FROM "BSCL.Plataforma.BD::PLA.Configuracion"
	WHERE "IdEmpresa" 		= :p_IdEmpresa
	AND "CodigoParametro" 	= 'PLA.LocalizacionSistema';

	
	-----------------------------------------------------------------
	-- OBTIENE TRANSACCIONES BILATERALES (tiene que ir antes de las validaciones)
	-----------------------------------------------------------------
	
	IF (:v_LocalizacionSistema = 'CL') THEN
		CALL "BSCL.Tesoreria.BD.Servicios.LIQ::ConsultarLiquidacionBilateral"
		( 
			 p_IdEmpresa  						=> :p_IdEmpresa
			,p_IdUsuario 						=> :p_IdUsuario
			,p_IdModulo  						=> :p_IdModulo
		    ,p_FechaLiquidacion 				=> :p_FechaLiquidacion
		    ,p_ClasificacionTipoInstrumento 	=> :p_ClasificacionTipoInstrumento
		    ,p_TipoInstrumento       			=> :p_TipoInstrumento
		    ,p_CondicionDeLiquidacion       	=> :p_CondicionDeLiquidacion
		    ,p_out_LiquidacionBilateralDetalle 	=> v_LiquidacionBilateralDetalle
		    ,p_out_LiquidacionBilateralResumen  => v_LiquidacionBilateralResumen
			,p_out_ErroresContador 				=> v_ErroresContador
			,p_out_ErroresDetalle 				=> v_ErroresDetalle
		);
		--SELECT 'v_LiquidacionBilateralDetalle', * FROM :v_LiquidacionBilateralDetalle; -- REVISION
	END IF; -- IF (:v_LocalizacionSistema = 'CL')
	
	
	IF (:v_LocalizacionSistema = 'PE') THEN
		CALL "BSCL.Tesoreria.BD.Servicios.LIQ::ConsultarLiquidacionBilateralPE"
		( 
			 p_IdEmpresa  						=> :p_IdEmpresa
			,p_IdUsuario 						=> :p_IdUsuario
			,p_IdModulo  						=> :p_IdModulo
		    ,p_FechaLiquidacion 				=> :p_FechaLiquidacion
		    ,p_ClasificacionTipoInstrumento 	=> :p_ClasificacionTipoInstrumento
		    ,p_TipoInstrumento       			=> :p_TipoInstrumento
		    ,p_CondicionDeLiquidacion       	=> :p_CondicionDeLiquidacion
		    ,p_out_LiquidacionBilateralDetalle 	=> v_LiquidacionBilateralDetalle
		    ,p_out_LiquidacionBilateralResumen  => v_LiquidacionBilateralResumen
			,p_out_ErroresContador 				=> v_ErroresContador
			,p_out_ErroresDetalle 				=> v_ErroresDetalle
		);
		--SELECT 'v_LiquidacionBilateralDetalle', * FROM :v_LiquidacionBilateralDetalle; -- REVISION
	END IF; -- IF (:v_LocalizacionSistema = 'CL')
		


	
	
	
	-- Se considera solo aquellas transacciones que no han sido procesadas
		v_LiquidacionBilateralResumen =
		SELECT *
		FROM :v_LiquidacionBilateralResumen
		WHERE "IdComprobanteTesoreria" = 0 -- No se le ha asignado comprobante de tesoreria, por lo tanto no ha sido procesada
			  AND "IdAgrupacion" IN (SELECT "IdAgrupacion" FROM :p_IdAgrupacionAProcesar);
	
	--SELECT 'v_LiquidacionBilateralResumen', * FROM :v_LiquidacionBilateralResumen; -- REVISION

	
	-------------------------------------------------------------
	-- OBTIENE CONFIGURACION TESORERIA
	-------------------------------------------------------------
	SELECT  
	MAX(CASE WHEN "CodigoParametro" = 'LIQUIDACIONBILATEALMUEVECTAINVERSION' THEN "Valor" ELSE null END) AS "LIQUIDACIONBILATEALMUEVECTAINVERSION"
	INTO v_LiquidacionBilateralMueveCtaInversion
	FROM (
			  SELECT "CodigoParametro","Valor"
			  FROM "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria"
			  WHERE "IdEmpresa" = :p_IdEmpresa
			  AND "CodigoParametro" = 'LIQUIDACIONBILATEALMUEVECTAINVERSION'
			  ORDER BY "CodigoParametro"
		  );
	--SELECT 'LiquidacionBilateralMueveCtaInversion', :v_LiquidacionBilateralMueveCtaInversion FROM DUMMY;  -- REVISION



	-----------------------------------------
	-- VALIDACIONES
	-----------------------------------------

	IF p_UsaReglasNegocio = 'S' THEN
	BEGIN
		DECLARE v_resultadoRegla 	NVARCHAR(40);
		
		-- Validaciones datos de entrada			
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,'TES',v_Errores,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN
			 SIGNAL SALIDA_CONTROLADA;
		END IF;

		CALL "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosProcesarLiquidacionBilateral" (:p_IdEmpresa,:p_FechaSistema,:p_FechaLiquidacion,:v_LiquidacionBilateralMueveCtaInversion,:v_LiquidacionBilateralResumen,:v_RechazadosPorValidacion,:v_ErroresDetalle,:v_resultadoRegla);
		

		IF :v_resultadoRegla = 'RECH' THEN				
			p_out_ErroresDetalle = SELECT "Codigo","Descripcion" FROM :v_ErroresDetalle;
			SIGNAL SALIDA_CONTROLADA; 	
		ELSEIF :v_resultadoRegla = 'ADVE' THEN
			--LLamada al servicio de autorizacion si se requiere
			p_out_AdvertenciasDetalle = SELECT "Codigo",'N' as "RequiereAutorizacion", "Descripcion" FROM :v_ErroresDetalle;
			SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
		END IF;
	END; 
	END IF; 


								   	
	--------------------
	-- LOGICA
	--------------------
	BEGIN

		-- Se eliminan las transacciones bilaterales(grupos) que no pasaron las validaciones
		v_LiquidacionBilateralResumenValidos =
		SELECT *
		FROM :v_LiquidacionBilateralResumen
		WHERE "IdAgrupacion" NOT IN (SELECT "IdAgrupacion" FROM :v_RechazadosPorValidacion);
		
		--SELECT 'RechazadosPorValidacion', * FROM :v_RechazadosPorValidacion; -- REVISION
		--SELECT 'LiquidacionBilateralResumenValidos', * FROM :v_LiquidacionBilateralResumenValidos; -- REVISION

		
		IF (NOT IS_EMPTY(:v_LiquidacionBilateralResumenValidos)) THEN
				------------------------------------------------
				-- GENERAR COMPROBANTES DE TESORERIA				
				------------------------------------------------

				-- Prepara entrada para comprobante de tesoreria
				INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_IngresoEgresoGenerar"
				SELECT
						LBR."IdAgrupacion" AS "NumeroReferencia"  -- Id temporal de agrupacion de transacciones
						-- Desde pto de vista del pago a contrapartes, el TipodeMovimiento es inverso al pto de vista de operacion realizada por los cliente
						,(CASE
							WHEN (LBR."AccionTipoOperacion" = 'COMPRA') THEN 'MOVEGR'  -- COMPRA --> EGRESO  (Pagar a la contraparte)
							ELSE 'MOVING'                                              -- VENTA  --> INGRESO (Recibir pago de la contraparte)
						END) AS "IdTipoMovimiento"
					   ,'CNT'    AS "IdTipoPersona"			 -- CNT: Contraparte
					   ,LBR."IdPersonaCorredoraContraparte" AS "IdPersona"
					   ,0 AS "IdPortafolio" -- 0 ya que es contraparte 
					   ,LBR."IdMonedaLiquidacion"   AS "IdMoneda"
					   ,(CASE
							WHEN (LBR."AccionTipoOperacion" = 'COMPRA') THEN LBR."TotalMontoCompra"
							ELSE LBR."TotalMontoVenta"
						END) AS "Monto"
					   ,:p_FechaSistema AS "FechaLiquidacion" -- Se liquida a la fecha actual(PH)
					   ,'LIQBIL'	AS 	"IdConcepto"          -- Liquidacion Bilateral			   
					   ,'CNT' AS "IdTipoPersonaTitularPago"   -- CNT: Contraparte
					   ,LBR."IdPersonaCorredoraContraparte"   AS "IdPersonaTitularPago"
					   ,0     AS "IdPortafolioTitularPago" 
					   ,0     AS "IdCaja"
					   ,''    AS "SecuenciaAgrupacion"					   
					   ,0     AS "SecuenciaRelacionada"					   
					   ,'Liquidación Bilateral' AS "Glosa"					   
					   ,0	  AS "IdEmpleadoDigitador"
					   ,0     AS "IdSucursalEmpleadoDigitador"					   
					   ,''    AS "IdTipoProducto"
					   ,''    AS "IdTipoOperacion"
					   ,''    AS "IdTipoComprobante"
					   ,0     AS "IdComprobante"
					   ,'0001-01-01' AS "FechaComprobante"
					   ,'0001-01-01' AS "FechaLiquidacionComprobante"
					   ,0 	  AS "IdOperacion"
					   ,'0001-01-01' AS "FechaOperacion"
					   ,LBR."IdCondicionDeLiquidacion" AS "IdCondicionLiquidacion"
					   ,LBR."IdBolsa"
					   ,'' 	  AS "IdMonedaOperacion"
					   ,0 	  AS "MontoOperacion"
					   ,'' 	  AS "IdTipoReajuste"
					   ,'0001-01-01' AS "FechaParidad"
					   ,0 	  AS "ValorParidad"
					   ,0 	  AS "FolioCartera"
					   ,0 	  AS "IdInstrumento"
					   ,LBR."IdTipoInstrumento"
					   ,LBR."IdClasificacionTipoInstrumento"
					   ,'S'   AS "Bilateral"
					   ,'S'   AS "GeneraComprobante"
					   ,'N'	  AS "AgruparEnComprobante"
					   ,''    AS "MueveCuentaInversion"
					   ,'S'   AS "TitularPago"
				FROM :v_LiquidacionBilateralResumenValidos	AS LBR;
				--SELECT 'IngresoEgresoGenerar', * FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_IngresoEgresoGenerar"; -- REVISAR

				
				-- Obtiene las preferencias de pago del corredor para liquidacion bilateral(LIQBIL)
				v_PreferenciasPagoCorredor = 
				SELECT
					"IdTipoMovimiento",
					"IdMedioDePago",
					"IdModalidadDePago",
					"IdCuentaBanco",
					"IdMoneda"
				FROM "BSCL.Tesoreria.BD.Vistas::ConsultarPreferenciaPagoCorredor" 
				WHERE "IdConcepto" = 'LIQBIL'  -- para liquidacion bilateral
				AND "IdTipoMovimiento" IN ('MOVING','MOVEGR')
				AND "IdEmpresa" = :p_IdEmpresa
				AND "NombreEntidad" = 'TES.LiquidacionBilateral'
				AND "IdEstado" = 'ACT';
				--SELECT 'v_PreferenciasPagoCorredor', * FROM :v_PreferenciasPagoCorredor;		-- REVISAR
				



				-- Prepara forma de pago para comprobante de tesoreria                            
				INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_FormaDePago"
				SELECT 
					 LBR."IdAgrupacion" AS "NumeroReferencia"  -- Id temporal de agrupacion de transacciones
					,PRE."IdMedioDePago"
					,PRE."IdModalidadDePago"	
					,LBR."IdMonedaLiquidacion"   AS "IdMoneda"
				    ,(CASE
						WHEN (LBR."AccionTipoOperacion" = 'COMPRA') THEN LBR."TotalMontoCompra"
						ELSE LBR."TotalMontoVenta"
					END) AS "Monto"										
					,PRE."IdCuentaBanco"  AS "IdCuentaBancariaCorredora"
					,0 AS "IdTarjetaBancariaCorredora"
					,0 AS "IdChequera"
					,0 AS "NumeroDocumentoCorredora"
					,'0001-01-01' AS "FechaDocumentoPagoCorredora"	
					,0 AS "IdCuentaBancariaSocio"
					,0 AS "IdBancoSocio"
					,'' AS "NombreCuentaBancariaSocio"
					,'' AS "NumeroCuentaBancariaSocio"
					,'' AS "IdTipoCuentaBancariaSocio"
					,'' AS "NombreTipoCuentaBancariaSocio"
					,0  AS "IdTarjetaBancariaSocio"
					,'' AS "IdTipoTarjetaBancariaSocio"
					,'' AS "NombreTipoTarjetaBancariaSocio"
					,'' AS "NumeroTarjetaBancariaSocio"
					,'' AS "NumeroSerieSocio"
					,0  AS"NumeroDocumentoSocio"
					,'0001-01-01' AS "FechaDocumentoPagoSocio"	
				FROM :v_LiquidacionBilateralResumenValidos	AS LBR
				LEFT JOIN :v_PreferenciasPagoCorredor AS PRE ON 
						(
						    PRE."IdTipoMovimiento"    = (CASE WHEN (LBR."AccionTipoOperacion" = 'COMPRA') THEN 'MOVEGR' ELSE 'MOVING' END)
						AND LBR."IdMonedaLiquidacion" = PRE."IdMoneda" 
						);

				--SELECT 'FormaDePago', * FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_FormaDePago";		-- REVISAR


		    	CALL "BSCL.Tesoreria.BD.Servicios::CrearIngresoEgreso"
		    	(
					   p_IdEmpresa 			  	   => :p_IdEmpresa
					  ,p_IdUsuario	 		 	   => :p_IdUsuario
					  ,p_IdModulo 			  	   => :p_IdModulo
					  ,p_simularOperacion 	  	   => 'N'
					  ,p_UsaReglasNegocio 	  	   => 'S'			  
					  ,p_EsIngresoAutomatico   	   => 'S'
					  ,p_out_InformacionContador   => :v_InformacionContador
					  ,p_out_AdvertenciasContador  => :v_AdvertenciasContador
					  ,p_out_ErroresContador 	   => :v_ErroresContador
				);
				
				
	
					
				p_out_AdvertenciasDetalle 	= 	SELECT "Codigo", "RequiereAutorizacion", "Descripcion" FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_AdvertenciasDetalle" UNION ALL 
												SELECT * FROM :p_out_AdvertenciasDetalle;
					
				p_out_ErroresDetalle 		= 	SELECT "Codigo", "Descripcion" FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_ErroresDetalle" UNION ALL 
												SELECT * FROM :p_out_ErroresDetalle;
				
				SELECT COUNT(1) INTO p_out_ErroresContador FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_ErroresDetalle"; 
				IF :p_out_ErroresContador > 0 THEN
					SIGNAL SALIDA_CONTROLADA;
				END IF;	


				--------------------------------------------------------------------------
				-- MARCAR TRANSACCIONES(PALOS) CON FOLIOS DE COMPROBANTES GENERADOS
				--------------------------------------------------------------------------

				-- Obtiene folio de los comprobantes generados(de acuerdo a sus movimientos)
				v_ComprobanteTesoreria =
				SELECT IEG."NumeroReferencia", IEG."IdIngresoEgreso", IEG."IdTipoMovimiento", COALESCE(CD."IdComprobante",0) AS "IdComprobante"
				FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_IngresoEgresoGenerado" AS IEG
				LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" AS CDO ON (CDO."IdIngresoEgreso" = IEG."IdIngresoEgreso")
				LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS CD ON (CD."IdComprobanteDetalle" = CDO."IdComprobanteDetalle");
				--SELECT 'v_ComprobanteTesoreria', * FROM :v_ComprobanteTesoreria;  -- REVISAR

				-- Obtiene las transacciones asociadas a los comprobantes generados
				v_ComprobanteTransaccion = 
				SELECT LBD."IdTransaccion", COM."IdComprobante",COM."NumeroReferencia", COM."IdIngresoEgreso", COM."IdTipoMovimiento"
				FROM :v_ComprobanteTesoreria AS COM
				INNER JOIN :v_LiquidacionBilateralResumenValidos AS LBR  ON (LBR."IdAgrupacion" = COM."NumeroReferencia")  -- solo grupos que no han sido procesados y validos
				INNER JOIN :v_LiquidacionBilateralDetalle AS LBD ON (LBD."IdAgrupacion" = LBR."IdAgrupacion" AND LBD."IdComprobanteTesoreria" = 0);
				--SELECT 'v_ComprobanteTransaccion', * FROM :v_ComprobanteTransaccion;  -- REVISION
	
				-- Marca la transaccion con el folio del comprobante 
				UPDATE TRA
				SET TRA."IdComprobanteTesoreria" = CT."IdComprobante"
				FROM "BSCL.Orden.BD::ORD.Transaccion" AS TRA
				INNER JOIN :v_ComprobanteTransaccion CT ON (CT."IdTransaccion" = TRA."IdTransaccion")		 		
				WHERE   CT."IdComprobante" <> 0
				AND TRA."IdEmpresa" = :p_IdEmpresa;
				
		
				----------------------------------------------------------------------------------------------
				-- GENERA MOVIMIENTOS EN CUENTA INVERSION PARA LA CONTRAPARTE
				-- NOTA: Requiere que la corredora contraparte esé creada como cliente para obtener portafolio
		        -- Este movimiento será neteado cuando se liquide el comprobante de tesoreria
				-----------------------------------------------------------------------------------------------
				IF (v_LiquidacionBilateralMueveCtaInversion = 'S') THEN
						INSERT INTO "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_IN_CargoAbonoAGenerar" (
							"NumeroCargoAbono","IdPortafolio","IdMoneda","IdTipoMovimiento","IdConcepto","Descripcion","IdCondicionDeLiquidacion","FechaCondicionDeLiquidacion","Monto","IdEjecutivo","IdSucursal",
							"IdTipoProducto","IdTipoOperacion","IdOperacion","FechaOperacion","FechaLiquidacionOperacion","FechaVencimientoOperacion","IdMonedaOperacion","MontoOperacion","TipoCambioOperacion",
							"IdContraparte","IdTipoComprobante","IdComprobante","FolioComprobante","IdComprobanteDetalle","IdComprobanteFormaDePago","IdTipoMovimientoAsociado","IdMovimientoAsociado",
							"FechaComprobante","IdPersonaPagoCobro","IdInstrumento","IdTipoInstrumento","Nemo","EsIndicadorBilateral","EsCargoAbonoPorTraspaso","IdPortafolioTraspaso","GeneroContraparteEnTesoreria",
							"CertificadoAutomatico")
					 	SELECT
								ROW_NUMBER() over (Order by POR."IdPortafolio", LBR."IdMonedaLiquidacion") as "NumeroCargoAbono"
					 	        ,POR."IdPortafolio"					 	 		
					 	 		,LBR."IdMonedaLiquidacion" as "IdMoneda"
								,(CASE
									WHEN ("AccionTipoOperacion" = 'COMPRA') THEN 'MOVABN'  -- COMPRA --> ABONO  (Pagar a la contraparte)
									ELSE 'MOVCAR'                                          -- VENTA  --> CARGO  (Recibir pago de la contraparte)
								END) AS "IdTipoMovimiento"
					 	 		,'LIQBIL' AS "IdConcepto"
					 	 		,'Liquidación Bilateral' as "Descripcion"
								,LBR."IdCondicionDeLiquidacion" AS "IdCondicionDeLiquidacion"
					 	 		,:p_FechaSistema as "FechaCondicionDeLiquidacion"
							   ,(CASE
									WHEN ("AccionTipoOperacion" = 'COMPRA') THEN "TotalMontoCompra"
									ELSE "TotalMontoVenta"
								END) AS "Monto"
					 	 		,0 as "IdEjecutivo"
					 			,0 as "IdSucursal"
					 	 		,null as "IdTipoProducto"
							    ,null as "IdTipoOperacion"
							    ,null as "IdOperacion"
							    ,null as "FechaOperacion"
							    ,null as "FechaLiquidacionOperacion"
							    ,null as "FechaVencimientoOperacion"
							    ,null as "IdMonedaOperacion"
							    ,null as "MontoOperacion"
							    ,null as "TipoCambioOperacion"
								,0 as "IdContraparte"
								,(CASE
									WHEN (LBR."AccionTipoOperacion" = 'COMPRA') THEN 'EGR'
									ELSE 'ING'
								END) AS "IdTipoComprobante"
								,COALESCE(COM."IdComprobante",0)  as "IdComprobante"
								,null as "FolioComprobante"
							    ,null as "IdComprobanteDetalle"
							    ,null as "IdComprobanteFormaDePago"
							    ,0 as "IdTipoMovimientoAsociado"
							    ,0 as "IdMovimientoAsociado"
							    ,null as "FechaComprobante"
							    ,null as "IdPersonaPagoCobro"
							    ,null as "IdInstrumento"
							    ,null as "IdTipoInstrumento"
							    ,null as "Nemo"
							    ,'S' as "EsIndicadorBilateral"
							    ,'N' as "EsCargoAbonoPorTraspaso"
							    ,null as "IdPortafolioTraspaso"
							    ,'N' as "GeneroContraparteEnTesoreria"
							    ,'S' as "CertificadoAutomatico"		
						FROM :v_LiquidacionBilateralResumenValidos AS LBR
						INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS SN ON (SN."IdPersona" = LBR."IdPersonaCorredoraContraparte" )
						INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS POR ON (POR."IdCliente" = SN."IdCliente")
						LEFT JOIN :v_ComprobanteTesoreria AS COM ON (COM."NumeroReferencia" = LBR."IdAgrupacion") -- para rescatar folio de comprobante tesoreria		
						WHERE COALESCE(SN."IdCliente", 0) <> 0  -- Contraparte debe estar creada como cliente(para obtener portafolio)		
						AND POR."CodigoPortafolio" = 1          -- Portafolio por defecto
						AND POR."IdEstado" = 'ACT';		


						IF (NOT IS_EMPTY("BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_IN_CargoAbonoAGenerar")) THEN
						 	CALL "BSCL.CuentaInversion.BD.Servicios::CrearCargoAbono" (
								 p_IdEmpresa				=> :p_IdEmpresa
							    ,p_IdUsuario				=> :p_IdUsuario
							    ,p_IdModulo					=> :p_IdModulo
							    ,p_simularOperacion			=> 'N'
								,p_UsaReglasNegocio			=> 'S'
								,p_FechaSistema				=> :p_FechaSistema
								,p_EsIngresoManual			=> 'N'
								,p_out_InformacionContador	=> :v_InformacionContador
								,p_out_AdvertenciasContador	=> :v_AdvertenciasContador
								,p_out_ErroresContador		=> :v_ErroresContador
							);


							p_out_AdvertenciasDetalle 	= 	SELECT "Codigo", "RequiereAutorizacion", "Descripcion" FROM "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_AdvertenciasDetalle" UNION ALL 
															SELECT * FROM :p_out_AdvertenciasDetalle;

							p_out_ErroresDetalle 		= 	SELECT "Codigo", "Descripcion" FROM "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_ErroresDetalle" UNION ALL 
															SELECT * FROM :p_out_ErroresDetalle;

							IF (:v_AdvertenciasContador + :v_ErroresContador) > 0 THEN
								SIGNAL SALIDA_CONTROLADA;
							END IF;
						END IF; -- IF (NOT IS_EMPTY("BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_IN_CargoAbonoAGenerar"))
						
				END IF;  -- IF (v_LiquidacionBilateralMueveCtaInversion = 'S')

		END IF;   -- IF (NOT IS_EMPTY(:v_LiquidacionBilateralResumenValidos))

	END;


END;
