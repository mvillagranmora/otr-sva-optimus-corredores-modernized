SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.CuentaInversion.BD.Servicios::LiquidarSaldo";

CREATE PROCEDURE   
-- 
--  Proposito: Liquidar Saldo de Cuenta Inversión
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 10/06/2016 
--  
"BSCL.CuentaInversion.BD.Servicios::LiquidarSaldo" (
    IN p_IdEmpresa						BIGINT,
    IN p_IdUsuario						BIGINT,
    IN p_IdModulo						NVARCHAR(3),
    IN p_simularOperacion				NVARCHAR(1),	-- S/N se usa para simular operacion (algunos casos) 
	IN p_UsaReglasNegocio				NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.	
    IN p_FechaSistema              		NVARCHAR(40),
    
    IN p_CuentaInversionALiquidar 		TABLE(	
    											"NumeroReferencia"					BIGINT,
    											"IdCuentaInversion"					BIGINT,
    											"IdPersona"    						BIGINT,
											    "IdPortafolio"						BIGINT,
											    "IdMoneda"							NVARCHAR(3),
											    "MontoALiquidar"					DECIMAL(26,8) -- Total o Parcial
											 ),
							   			 
	IN p_TableFormaPago  				"BSCL.Tesoreria.BD.Tipos::TES.FormaDePago",									   			 

	-- Parametros salida del servicio publico
	OUT p_out_ResultadoProceso			TABLE("Campo" VARCHAR(40),"Id" BIGINT, "Servicio" VARCHAR(250), "Resultado" VARCHAR(40)),
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador		INT,
	OUT p_out_AdvertenciasDetalle		TABLE ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador			INT,
	OUT p_out_ErroresDetalle			TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
	
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
	DECLARE v_erroroutmsg  				NVARCHAR(5000);		
	DECLARE v_ErrorNoData  				NVARCHAR(100);
	DECLARE v_errorExt 					NVARCHAR(40);
	DECLARE v_resultadoRegla 			NVARCHAR(40) DEFAULT 'OK';
	DECLARE v_errorcuenta 				INT DEFAULT 0;
	DECLARE v_errorhashmsg 				TABLE ("Codigo" NVARCHAR(100),"Valor" NVARCHAR(5000));
	DECLARE v_RechazadosPorValidacion	TABLE ("NumeroReferencia" BIGINT, "IdPortafolio" BIGINT, "IdMoneda" NVARCHAR(3), "IdCuentaInversion" BIGINT);
	DECLARE v_InformacionContador		INT;
	DECLARE v_AdvertenciasContador		INT;
	DECLARE v_ErroresContador			INT;	
	
	
		        	
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
		p_out_ErroresDetalle =  SELECT :v_ErrorNoData  as "Codigo",:v_erroroutmsg as "Descripcion" FROM dummy;
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
	END;
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN		
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL;
		END IF;		
	END;
	
	p_out_AdvertenciasContador		:= 0;
	p_out_ErroresContador			:= 0;
	p_out_AdvertenciasDetalle		= SELECT '' AS "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" FROM dummy where 1=2;
	p_out_ErroresDetalle			= SELECT '' AS "Codigo",'' as "Descripcion" FROM dummy where 1=2;
	p_out_ResultadoProceso			= SELECT '' AS "Campo", 0 as "Id", '' as "Servicio", '' as "Resultado" FROM dummy where 1=2;
	v_RechazadosPorValidacion		= SELECT 0 as "NumeroReferencia", 0 as "IdPortafolio", '' as "IdMoneda",	0 as "IdCuentaInversion" FROM dummy where 1=2;
										   
	v_AdvertenciasContador			:= 0;
	v_ErroresContador				:= 0;

	
	--------------------
	--VALIDACIONES
	--------------------	   
	IF p_UsaReglasNegocio = 'S' THEN
	BEGIN
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_Errores,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN
				 SIGNAL SALIDA_CONTROLADA; 	
		END IF;


		CALL "BSCL.CuentaInversion.BD.Reglas::ServicioCumpleRequisitoLiquidarSaldo"(
			 p_IdEmpresa				=>	:p_IdEmpresa
    		,p_IdUsuario				=>	:p_IdUsuario
    		,p_IdModulo					=>	:p_IdModulo
			,p_FechaSistema				=>	:p_FechaSistema
    		,p_CuentaInversionALiquidar =>	:p_CuentaInversionALiquidar
    		,p_out_MovimientosRech		=>	:v_RechazadosPorValidacion
			,p_out_ErroresDetalle		=>	:v_ErroresDetalleValidacion
			,p_out_resultadoRegla		=>	:v_resultadoRegla
		);
		
		SELECT 'ResultadoRegla', :v_resultadoRegla FROM DUMMY;   -- REVISAR
		
		IF :v_resultadoRegla = 'RECH' THEN
			p_out_ErroresDetalle = SELECT "Codigo","Descripcion" FROM :v_ErroresDetalleValidacion;
			SIGNAL SALIDA_CONTROLADA;
		ELSEIF :v_resultadoRegla = 'ADVE' THEN
			--LLamada al servicio de autorizacion si se requiere
			p_out_AdvertenciasDetalle = SELECT "Codigo",'N' as "RequiereAutorizacion", "Descripcion" FROM :v_ErroresDetalleValidacion;
			SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
		END IF;
	END; 
	END IF;

	--------------------
	--SIMULACION
	--------------------
	IF (:p_simularOperacion = 'S') THEN
		SIGNAL SALIDA_CONTROLADA;
	END IF;

	--------------------
	--LOGICA
	--------------------
	BEGIN
		DECLARE v_ErroresContador, v_AdvertenciasContador	BIGINT;
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;
		
				
		v_ConfiguracionCuentaInversion =
		SELECT "LIQUIDASALDOGENERACOMPROBANTETESORERIA" FROM "BSCL.CuentaInversion.BD.Vistas::ConsultaConfiguracionCuentaInversion";

		
		-- Elimina registros rechazados por validacion
		v_CuentaInversionALiquidar =
	 	SELECT * 
	 	FROM :p_CuentaInversionALiquidar
		WHERE "NumeroReferencia" NOT IN (SELECT "NumeroReferencia" FROM :v_RechazadosPorValidacion);
		-- SELECT 'CuentaInversionALiquidar', * FROM :v_CuentaInversionALiquidar;   -- REVISAR


		------------------------------------------------
		-- GENERA MOVIMIENTO EGRESO DE TESORERIA
		------------------------------------------------
		INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_IngresoEgresoGenerar"
		SELECT
				"CIL"."IdCuentaInversion"  AS "NumeroReferencia"
			   ,'MOVEGR' AS "IdTipoMovimiento"
			   ,'CLI'    AS "IdTipoPersona"
			   ,"CIL"."IdPersona"
			   ,"CIL"."IdPortafolio"
			   ,"CIL"."IdMoneda"
			   ,"CIL"."MontoALiquidar" AS "Monto"
               ,CAST(:p_FechaSistema as NVARCHAR(10)) AS "FechaLiquidacion"
			   ,'LQSDCI'	AS 	"IdConcepto"				               
			   ,'CLI' AS "IdTipoPersonaTitularPago"
			   ,"CIL"."IdPersona"    AS "IdPersonaTitularPago"
			   ,"CIL"."IdPortafolio" AS "IdPortafolioTitularPago"
			   ,0     AS "IdCaja"
			   ,''    AS "SecuenciaAgrupacion"
			   ,null  AS "SecuenciaRelacionada"
			   ,'Liquidación de Saldos Cuenta Inversión' AS "Glosa"
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
			   ,'' 	  AS "IdCondicionLiquidacion"
			   ,'' 	  AS "IdBolsa"
			   ,'' 	  AS "IdMonedaOperacion"
			   ,0 	  AS "MontoOperacion"
			   ,'' 	  AS "IdTipoReajuste"
			   ,'0001-01-01' AS "FechaParidad"
			   ,0 	  AS "ValorParidad"
			   ,0 	  AS "FolioCartera"
			   ,0 	  AS "IdInstrumento"
			   ,'' 	  AS "IdTipoInstrumento"
			   ,'' 	  AS "IdClasificacionTipoInstrumento"
			   ,'N'   AS "Bilateral"
			   ,"CON"."LIQUIDASALDOGENERACOMPROBANTETESORERIA" AS "GeneraComprobante" -- S/N			   
			   ,'N'	  AS "AgruparEnComprobante"
			   ,'S'   AS "MueveCuentaInversion"   -- Debe ir en S para la liquidación de saldo
			   ,'S'   AS "TitularPago"
		FROM :v_CuentaInversionALiquidar AS "CIL"
		INNER JOIN :v_ConfiguracionCuentaInversion  AS "CON" ON (1=1);


		INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_FormaDePago"
		SELECT * FROM :p_TableFormaPago;

    	CALL "BSCL.Tesoreria.BD.Servicios::CrearIngresoEgreso"
    	(
			   p_IdEmpresa 			  	   => :p_IdEmpresa
			  ,p_IdUsuario	 		 	   => :p_IdUsuario
			  ,p_IdModulo 			  	   => 'CTA'
			  ,p_simularOperacion 	  	   => 'N'
			  ,p_UsaReglasNegocio 	  	   => 'S'
			  ,p_EsIngresoAutomatico   	   => 'S'
			  ,p_out_InformacionContador   => :v_InformacionContador
			  ,p_out_AdvertenciasContador  => :v_AdvertenciasContador
			  ,p_out_ErroresContador 	   => :v_ErroresContador
		);
		
		p_out_AdvertenciasDetalle 	= SELECT "Codigo", "RequiereAutorizacion", "Descripcion" FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_AdvertenciasDetalle" UNION ALL SELECT * FROM :p_out_AdvertenciasDetalle;
		p_out_ErroresDetalle 		= SELECT "Codigo", "Descripcion" FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_ErroresDetalle" UNION ALL SELECT * FROM :p_out_ErroresDetalle;
		p_out_ResultadoProceso 	    = SELECT * FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_ResultadoProceso" UNION ALL SELECT * FROM :p_out_ResultadoProceso;

	
			
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle; 
		IF :p_out_ErroresContador > 0 THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;		

		
		------------------------------------------------
		-- RESULTADO DEL PROCESO
		------------------------------------------------
		p_out_ResultadoProceso =
		SELECT  'IdCuentaInversion' as "Campo",
				"IdCuentaInversion" as "Id",				
				'BSCL.CuentaInversion.BD.Servicios::LiquidarSaldo' as "Servicio",
				'LIQ' as "Resultado"
		FROM :v_CuentaInversionALiquidar
		UNION ALL
		SELECT  'IdCuentaInversion' as "Campo",
				"IdCuentaInversion" as "Id",				
				'BSCL.CuentaInversion.BD.Servicios::LiquidarSaldo' as "Servicio",
				'RECH' as "Resultado"
		FROM :v_RechazadosPorValidacion
		UNION ALL
		SELECT "Campo","Id","Servicio","Resultado" FROM :p_out_ResultadoProceso;
		
				
	END;
END;