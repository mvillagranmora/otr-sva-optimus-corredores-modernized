SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.LIQ::ProcesarClearingFinanciero";
GO
CREATE PROCEDURE 
-- 
--  Proposito: - Procesar Clearing Financiero 
--             - Genera comprobantes de tesoreria por grupo camara  
--             - Marca los movimientos(transacciones, dividendos) con folio de comprobante generado
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 01/03/2016
-- 
"BSCL.Tesoreria.BD.Servicios.LIQ::ProcesarClearingFinanciero"
  (
    -- VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa              BIGINT,
    IN p_IdUsuario              BIGINT,
    IN p_IdModulo               NVARCHAR(3),
    IN p_UsaReglasNegocio		NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
    
    -- VARIABLES DE ENTRADA
    IN  p_FechaLiquidacion		NVARCHAR(10),    
    IN  p_Camaras			    TABLE ("Camara"      NVARCHAR(2)),
    IN  p_IdPais                BIGINT,          -- pais de la camara de compensacion

	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
	
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA BSCL
AS	
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
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
	

	--------------------
	--Negocio Aqui
	--------------------
	BEGIN
		------------------------------------------------------
		-- Declaración de Variables
		------------------------------------------------------
		DECLARE v_out_InformacionContador  		 INTEGER := 0;
		DECLARE v_out_AdvertenciasContador  	 INTEGER := 0;
		DECLARE v_out_ErroresContador  			 INTEGER := 0;
		DECLARE v_LocalizacionSistema            NVARCHAR(2);
		DECLARE v_CodigoCamaraDeCompensacion     NVARCHAR(40);		
		DECLARE v_IdPersonaCamaraDeCompensacion  BIGINT  := 0;
		
		DECLARE v_TablaCodigosMensajes           TABLE (
														 "CodigoMensaje" NVARCHAR(520),
														 "CodigoDato"    NVARCHAR(100),
														 "ValorDato"	 NVARCHAR(100)
													   );


		v_TablaCodigosMensajes = SELECT '' As "CodigoMensaje", '' As "CodigoDato", '' As "ValorDato" 
		                         FROM DUMMY WHERE 1=2;


		------------------------------------------------
		-- Optiene localizacion sistema(CL|PE)
		------------------------------------------------		
		SELECT "Valor"
		INTO v_LocalizacionSistema
		FROM "BSCL.Plataforma.BD::PLA.Configuracion"
		WHERE "IdEmpresa" 		= :p_IdEmpresa
		AND "CodigoParametro" 	= 'PLA.LocalizacionSistema';

		
		
		IF (:v_LocalizacionSistema = 'PE') THEN
			v_CodigoCamaraDeCompensacion = 'CAVALI';
		ELSE
			v_CodigoCamaraDeCompensacion = 'CCLV';
		END IF;
		
	
		-----------------------------------------------------------------------------------------
		-- OBTIENE DATOS DE EMPRESA LIQUIDADORA (camara de compensación CCLV(CL), CAVALI(PE))
		-----------------------------------------------------------------------------------------
		v_CamaraDeCompensacion = 
		SELECT COALESCE("IdPersona",0) AS "IdPersona" 
		FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCamaraDeCompensacion"
		WHERE "Codigo"         = :v_CodigoCamaraDeCompensacion				
		AND   "IdPaisCamara"   = :p_IdPais
		AND   "IdEstadoCamara" = 'ACT';
		--SELECT 'v_CamaraDeCompensacion', * FROM :v_CamaraDeCompensacion;   -- REVISION
		
		IF IS_EMPTY(:v_CamaraDeCompensacion) THEN
		   v_TablaCodigosMensajes =
		   SELECT  'EMPRESA_LIQUIDADORA_NO_EXISTE' AS "CodigoMensaje" ,'Codigo' As "CodigoDato", :v_CodigoCamaraDeCompensacion As "ValorDato" FROM DUMMY
		   UNION ALL
		   SELECT * FROM :v_TablaCodigosMensajes;
		ELSE
			-- Verifica que la camara de compensación tenga IdPersona relacionado
		    SELECT "IdPersona" INTO v_IdPersonaCamaraDeCompensacion FROM :v_CamaraDeCompensacion;
		   
		    IF (:v_IdPersonaCamaraDeCompensacion = 0) THEN
			   v_TablaCodigosMensajes =
			   SELECT  'EMPRESA_LIQUIDADORA_SIN_PERSONA_RELACIONADA' AS "CodigoMensaje" ,'Codigo' As "CodigoDato", :v_CodigoCamaraDeCompensacion As "ValorDato" FROM DUMMY
			   UNION ALL
			   SELECT * FROM :v_TablaCodigosMensajes;		   
		    END IF;
		END IF;
		
		
		

		-----------------------------------------------------------
		-- OBTIENE LOS DATOS PARA PROCESAR EL CLEARING
		-----------------------------------------------------------

		IF (:v_LocalizacionSistema = 'CL') THEN
			CALL "BSCL.Tesoreria.BD.Servicios.LIQ::ConsultarClearingFinanciero"(
			    	 p_IdEmpresa         				=> :p_IdEmpresa
			    	,p_IdUsuario         				=> :p_IdUsuario
			    	,p_IdModulo          				=> :p_IdModulo
			    	,p_UsaReglasNegocio	 				=> :p_UsaReglasNegocio
	    			,p_FechaLiquidacion	   				=> :p_FechaLiquidacion
	    			,p_Camaras           				=> :p_Camaras
					,p_out_ClearingFinancieroResumen  	=> :v_ClearingFinancieroResumen
					,p_out_ClearingFinancieroDetalle  	=> :v_ClearingFinancieroDetalle				
					,p_out_ClearingFinancieroDividendos => :v_ClearingFinancieroDividendos
					,p_out_AdvertenciasContador 		=> :p_out_AdvertenciasContador
					,p_out_AdvertenciasDetalle  		=> :p_out_AdvertenciasDetalle
					,p_out_ErroresContador      		=> :p_out_ErroresContador
					,p_out_ErroresDetalle       		=> :p_out_ErroresDetalle
			);		
		END IF; -- IF (:v_LocalizacionSistema = 'CL')

		

		IF (:v_LocalizacionSistema = 'PE') THEN
			CALL "BSCL.Tesoreria.BD.Servicios.LIQ::ConsultarClearingFinancieroPE"(
			    	 p_IdEmpresa         				=> :p_IdEmpresa
			    	,p_IdUsuario         				=> :p_IdUsuario
			    	,p_IdModulo          				=> :p_IdModulo
			    	,p_UsaReglasNegocio	 				=> :p_UsaReglasNegocio
					,p_FechaLiquidacion	   				=> :p_FechaLiquidacion
					,p_Camaras           				=> :p_Camaras
					,p_out_ClearingFinancieroResumen  	=> :v_ClearingFinancieroResumen
					,p_out_ClearingFinancieroDetalle  	=> :v_ClearingFinancieroDetalle
					,p_out_AdvertenciasContador 		=> :p_out_AdvertenciasContador
					,p_out_AdvertenciasDetalle  		=> :p_out_AdvertenciasDetalle
					,p_out_ErroresContador      		=> :p_out_ErroresContador
					,p_out_ErroresDetalle       		=> :p_out_ErroresDetalle
			);		
		END IF; -- IF (:v_LocalizacionSistema = 'PE')

	
		
		-- Considera solo los movimientos(transacciones y dividendos) no procesados
		v_ClearingFinancieroResumen = 	SELECT * FROM :v_ClearingFinancieroResumen WHERE "IdComprobanteTesoreria" = 0;
												

		 
		IF IS_EMPTY(:v_ClearingFinancieroResumen) THEN
		        v_TablaCodigosMensajes =
				SELECT 	'NO_EXISTEN_MOVIMIENTOS_A_PROCESAR' AS "CodigoMensaje", '' As "CodigoDato", '' As "ValorDato" FROM DUMMY
			    UNION ALL
			    SELECT * FROM :v_TablaCodigosMensajes;				
		END IF;



		-----------------------------------------------------------
		-- LLAMADA A DICCIONARIO PARA ERRORRES(Hasta acá)
		-----------------------------------------------------------
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes, :v_out_ErroresDetalle);


	    p_out_ErroresDetalle = SELECT "Codigo", "Descripcion"  FROM :v_out_ErroresDetalle   UNION ALL
                               SELECT "Codigo", "Descripcion"  FROM :p_out_ErroresDetalle;
                              
	    Select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;
                              
	    If p_out_ErroresContador > 0 Then
	         SIGNAL SALIDA_CONTROLADA;
        End If;


		------------------------------------------------------------------------
		-- OBTIENE PREFERENCIAS DE PAGO DEL CORREDOR PARA CLEARING FINANCIERO
		------------------------------------------------------------------------
		v_PreferenciasPagoCorredor = 
		SELECT
					"IdTipoMovimiento",
					"IdMedioDePago",
					"IdModalidadDePago",
					"IdCuentaBanco",
					"IdMoneda",
					"IdConcepto"
		FROM "BSCL.Tesoreria.BD.Vistas::ConsultarPreferenciaPagoCorredor"
		WHERE  ("IdConcepto" 		IN ('CLFIPM','CFPHSM','CLFIRV') OR         -- para clearing financiero CHILE
				"IdConcepto" 		IN ('CLFIRV','CLFIRF','CLFIRP','CLFIRM'))  -- para clearing financiero PERU
		AND "IdTipoMovimiento" 	IN ('MOVING','MOVEGR')		
		AND "NombreEntidad" 	= 'TES.ClearingFinanciero'
		AND "IdEstado" 			= 'ACT'
		AND "IdEmpresa"			= :p_IdEmpresa;		
		--SELECT 'v_PreferenciasPagoCorredor', * FROM :v_PreferenciasPagoCorredor;		-- REVISAR				
				
		
		IF IS_EMPTY(:v_PreferenciasPagoCorredor) THEN
		        v_TablaCodigosMensajes =
				SELECT 	'PREFERENCIAS_DE_PAGO_CLEARING_NO_CONFIGURADAS' AS "CodigoMensaje", '' As "CodigoDato", '' As "ValorDato" FROM DUMMY
			    UNION ALL
			    SELECT * FROM :v_TablaCodigosMensajes;				
		END IF;
		
		
		--------------------------------------------------------------
		-- OBTIENE TOTALES POR CAMARA, MONEDA
		--------------------------------------------------------------	   
	    v_SaldoGrupoCamara = 
	    SELECT
	    	 ROW_NUMBER() over(ORDER BY "CodigoGrupoCamara", "NombreGrupoCamara","IdMonedaTransaccion") AS "NumeroReferencia"  -- Id Referencial
			,"CodigoGrupoCamara"    -- CHILE:   CambiosCCLV(S) -> 3(RV), 1(PM), 2(PH+SM)   -- CambiosCCLV(N) -> 1(RV+PM), 2(PH+SM)
			,"NombreGrupoCamara"
			,"IdMonedaTransaccion" AS "IdMoneda"
			

	   		,(COALESCE(SUM("SaldoAcreedor"),0)-COALESCE(SUM("SaldoDeudor"),0)) AS "SaldoCamara"
	   		
	   	FROM :v_ClearingFinancieroResumen	   		
	    GROUP BY "CodigoGrupoCamara", "NombreGrupoCamara","IdMonedaTransaccion";	    
	    --SELECT 'v_SaldoGrupoCamara', * FROM :v_SaldoGrupoCamara;  -- REVISAR


		--------------------------------------------------------------
		-- GENERAR COMPROBANTES DE TESORERIA
		--------------------------------------------------------------		
		INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_IngresoEgresoGenerar"
   		SELECT
			GRU."NumeroReferencia",  -- Id referencial
			CASE
				WHEN (GRU."SaldoCamara" < 0) THEN 'MOVEGR'
				ELSE 'MOVING'
			END As "IdTipoMovimiento",
            'CNT' AS "IdTipoPersona",
			CAM."IdPersona",
			0 AS "IdPortafolio",          -- Contraparte sin portafolio
           	GRU."IdMoneda",
		   	ABS("SaldoCamara") As "Monto",
		   	:p_FechaLiquidacion As "FechaLiquidacion",
		   
			CASE
				-- Conceptos para CHILE
				WHEN "CodigoGrupoCamara" = 1 AND :v_LocalizacionSistema = 'CL' THEN 'CLFIPM' -- 1: Camara PM
				WHEN "CodigoGrupoCamara" = 2 AND :v_LocalizacionSistema = 'CL' THEN 'CFPHSM' -- 2: Camara PH + SM
				WHEN "CodigoGrupoCamara" = 3 AND :v_LocalizacionSistema = 'CL' THEN 'CLFIRV' -- 3: Camara RV				
				-- Conceptos para PERU
				WHEN "CodigoGrupoCamara" = 1 AND :v_LocalizacionSistema = 'PE' THEN 'CLFIRV' -- 1: Camara RV --> CLEARING FINANCIERO CAMARA RV
				WHEN "CodigoGrupoCamara" = 2 AND :v_LocalizacionSistema = 'PE' THEN 'CLFIRF' -- 2: Camara RF --> CLEARING FINANCIERO CAMARA RF
				WHEN "CodigoGrupoCamara" = 3 AND :v_LocalizacionSistema = 'PE' THEN 'CLFIRP' -- 3: Camara RP --> CLEARING FINANCIERO CAMARA RP
				WHEN "CodigoGrupoCamara" = 4 AND :v_LocalizacionSistema = 'PE' THEN 'CLFIRM' -- 3: Camara RM --> CLEARING FINANCIERO CAMARA RM
			END As "IdConcepto",
			
           'CNT' AS "IdTipoPersonaTitularPago",					           
		   CAM."IdPersona" AS "IdPersonaTitularPago",
           0  AS "IdPortafolioTitularPago",
           0  As "IdCaja",
           '' As "SecuenciaAgrupacion",
           0  As "SecuenciaRelacionada",
		   'CLEARING FINANCIERO - CAMARA: ' || "NombreGrupoCamara" As "Glosa",			
           0  As "IdEmpleadoDigitador",
           0  As "IdSucursalEmpleadoDigitador",
           '' As "IdTipoProducto",
           '' As "IdTipoOperacion",
           '' As "IdTipoComprobante",
           0 As "IdComprobante",
           '0001-01-01' As "FechaComprobante",
           '0001-01-01' As "FechaLiquidacionComprobante",
           0 As "IdOperacion", 
           '0001-01-01' As "FechaOperacion",
           '' As "IdCondicionLiquidacion",
           '' As "IdBolsa",
           '' As "IdMonedaOperacion",
           0  As "MontoOperacion",
           '' As "IdTipoReajuste",
           '0001-01-01' As "FechaParidad",
           0   As "ValorParidad",
           0   As "FolioCartera",
           0   As "IdInstrumento",
           ''  As "IdTipoInstrumento",
           ''  As "IdClasificacionTipoInstrumento",
           'N' As "Bilateral",
           'S' As "GeneraComprobante",
           'S' As "AgruparEnComprobante",
           'S' As "MueveCuentaInversion",
           'S' As "TitularPago"					           
		FROM :v_SaldoGrupoCamara AS GRU		
		INNER JOIN :v_CamaraDeCompensacion AS CAM ON (1=1);
		
		--SELECT 'Entrada_IngresoEgresoGenerar', * FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_IngresoEgresoGenerar";   -- REVISAR
		

		-- Definir forma de pago de comprobantes
		            
		INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_FormaDePago"
		SELECT 	 IE."NumeroReferencia"
				,PRE."IdMedioDePago"
				,PRE."IdModalidadDePago"
				,IE."IdMoneda"
				,IE."Monto"					
				,PRE."IdCuentaBanco"          AS "IdCuentaBancariaCorredora"
				,0 							  AS "IdTarjetaBancariaCorredora"					
				,0 							  AS "IdChequera"
				,0 							  AS "NumeroDocumentoCorredora"
				,'0001-01-01' 				  AS "FechaDocumentoPagoCorredora"					
				,0 							  AS "IdCuentaBancariaSocio"					
				,0 							  AS "IdBancoSocio"
				,'' 						  AS "NombreCuentaBancariaSocio"
				,''			 				  AS "NumeroCuentaBancariaSocio"
				,'' 						  AS "IdTipoCuentaBancariaSocio"
				,'' 						  AS "NombreTipoCuentaBancariaSocio"															
				,0 							  AS "IdTarjetaBancariaSocio"					
				,'' 						  AS "IdTipoTarjetaBancariaSocio"
				,'' 						  AS "NombreTipoTarjetaBancariaSocio"
				,'' 						  AS "NumeroTarjetaBancariaSocio"
				,'' 						  AS "NumeroSerieSocio"
				,0 							  AS "NumeroDocumentoSocio"
				,'0001-01-01' 				  AS "FechaDocumentoPagoSocio"
		FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_IngresoEgresoGenerar" AS IE
		INNER JOIN :v_PreferenciasPagoCorredor AS PRE ON (
					    PRE."IdTipoMovimiento" = IE."IdTipoMovimiento"
					AND PRE."IdConcepto"       = IE."IdConcepto"
					AND PRE."IdMoneda"         = IE."IdMoneda");		
		--SELECT 'Entrada_FormaDePago', * FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_FormaDePago";   -- REVISAR	


		IF NOT IS_EMPTY("BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_IngresoEgresoGenerar") THEN	
		
	           CALL "BSCL.Tesoreria.BD.Servicios::CrearIngresoEgreso"
	           (
		   		  p_IdEmpresa 			 		=> :p_IdEmpresa
		   		 ,p_IdUsuario 			 		=> :p_IdUsuario
		   		 ,p_IdModulo 			 		=> 'TES'
		   		 ,p_simularOperacion 	 		=> 'N'
		   		 ,p_UsaReglasNegocio 	 		=> 'S'
		   		 ,p_EsIngresoAutomatico 		=> 'S'
		   		 ,p_out_InformacionContador 	=> v_out_InformacionContador
		   		 ,p_out_AdvertenciasContador 	=> v_out_AdvertenciasContador
		   		 ,p_out_ErroresContador      	=> v_out_ErroresContador
		       );
	           
		       p_out_ErroresContador := p_out_ErroresContador + v_out_ErroresContador + v_out_AdvertenciasContador;

			   p_out_ErroresDetalle = SELECT "Codigo", "Descripcion" FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_ErroresDetalle"      UNION ALL 
			                          SELECT "Codigo", "Descripcion" FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_AdvertenciasDetalle" UNION ALL
		                              SELECT "Codigo", "Descripcion" FROM :p_out_ErroresDetalle;

			   If v_out_ErroresContador > 0 Then
			      SIGNAL SALIDA_CONTROLADA;
		       End If;

				
			   	--------------------------------------------------------------				
				-- MARCA TRANSACCIONES CON EL FOLIO DEL COMPROBANTE GENERADO 
				--------------------------------------------------------------
				-- Obtener el Id de los comprobantes generados
				v_ComprobanteGenerado =
				SELECT
				   IEG."NumeroReferencia"
				  ,IEG."IdIngresoEgreso"
				  ,COALESCE(CD."IdComprobante", 0) AS "IdComprobante"  -- Secuencia Comprobante Tesoreria generado
				FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_IngresoEgresoGenerado" AS IEG
				LEFT OUTER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" AS CDO  ON (CDO."IdIngresoEgreso" = IEG."IdIngresoEgreso")
				LEFT OUTER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS CD  ON (CD."IdComprobanteDetalle" = CDO."IdComprobanteDetalle");
				-- SELECT 'ComprobanteGenerado:', * FROM :v_ComprobanteGenerado;   -- REVISAR
		
				-- Marca transacciones
				UPDATE TRA
				SET "IdComprobanteTesoreria" = CG."IdComprobante"
				FROM "BSCL.Orden.BD::ORD.Transaccion" AS TRA
				INNER JOIN :v_ClearingFinancieroDetalle AS DET ON (DET."IdTransaccion" = TRA."IdTransaccion")
				INNER JOIN :v_SaldoGrupoCamara AS GRU ON (GRU."CodigoGrupoCamara" = DET."CodigoGrupoCamara" AND  GRU."NombreGrupoCamara" = DET."NombreGrupoCamara"   AND GRU."IdMoneda" = DET."IdMonedaTransaccion")
				INNER JOIN :v_ComprobanteGenerado AS CG ON (CG."NumeroReferencia" = GRU."NumeroReferencia")
				WHERE TRA."IdComprobanteTesoreria" = 0;								
				
				
				
				--------------------------------------------------------------
				-- MARCA DIVIDENDOS CON EL FOLIO DEL COMPROBANTE GENERADO (CHILE)
				--------------------------------------------------------------
				IF (:v_LocalizacionSistema = 'CL') THEN
					UPDATE ECC
					SET "IdComprobanteTesoreria" = CG."IdComprobante"
					FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS ECC
					INNER JOIN :v_ClearingFinancieroDividendos AS DIV ON (DIV."IdEventoCapitalCliente" = ECC."IdEventoCapitalCliente")
					INNER JOIN :v_SaldoGrupoCamara AS GRU ON (GRU."CodigoGrupoCamara" = DIV."CodigoGrupoCamara" AND  GRU."NombreGrupoCamara" = DIV."NombreGrupoCamara"   AND GRU."IdMoneda" = DIV."IdMoneda")					
					INNER JOIN :v_ComprobanteGenerado AS CG ON (CG."NumeroReferencia" = GRU."NumeroReferencia")					
					WHERE ECC."IdComprobanteTesoreria" = 0;
				END IF;
				
		END IF; -- IF IS_EMPTY("BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_IngresoEgresoGenerar")

	
	END;


END;
