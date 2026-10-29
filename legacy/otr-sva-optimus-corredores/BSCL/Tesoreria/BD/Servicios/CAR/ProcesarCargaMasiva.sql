SET SCHEMA "BSCL";
GO 
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.CAR::ProcesarCargaMasiva";
GO
CREATE PROCEDURE 
-- 
--  Proposito: Procesa las cargas masivas, genera movimientos de tesoreria, Cta. inversion, Partida de Ingresos
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 07/10/2016
--
"BSCL.Tesoreria.BD.Servicios.CAR::ProcesarCargaMasiva"(

    -- Parametros de Validacion                                                                                            
	IN  p_IdEmpresa					BIGINT,
   	IN  p_IdUsuario					BIGINT,
   	IN  p_IdModulo					NVARCHAR(3),                                                                       
   	IN  p_UsaReglasNegocio			NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.                          
   	IN  p_FechaProceso			    NVARCHAR(10),

   	IN  p_CargaMasivaProcesar       TABLE (
											  "IdCargaMasiva"        			BIGINT,
											  "CodigoTipoCargaMasiva"    		NVARCHAR(40),		     -- CARGA_RECAUDACION_SERVIPAG / CARGA_GENERICA_MOVIMIENTOS
											  "IdMoneda"			 			NVARCHAR(3),
											  "IdConcepto"           			NVARCHAR(6),
											  "IdMedioDePago"        			NVARCHAR(3),
										 	  "IdCuentaBanco"					BIGINT
										  ),


   	-- Parametros de Salida
   	OUT p_out_AdvertenciasContador	INT,
   	OUT p_out_AdvertenciasDetalle	TABLE ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
   	OUT p_out_ErroresContador		INT,   
   	OUT p_out_ErroresDetalle		TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
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
	DECLARE v_resultadoRegla 			NVARCHAR(40);	
	DECLARE v_ErroresDetalle            TABLE ("Codigo" nvarchar(100), "Descripcion" nvarchar(520));
	DECLARE v_AdvertenciasDetalle	    TABLE ("Codigo" nvarchar(100), "RequiereAutorizacion" nvarchar(40), "Descripcion" nvarchar(520));
	DECLARE v_RegistrosRech	            TABLE ("IdCargaMasiva"   BIGINT);	        	
	DECLARE v_out_InformacionContador   INT := 0;
	DECLARE v_out_ErroresContador       INT := 0;
	DECLARE v_out_AdvertenciasContador  INT := 0;

	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 	
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	END;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN
		v_hash= SELECT 0 as "IdInterno",'DATA_NO_EXISTE' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" FROM DUMMY;	
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_hash,:v_erroroutmsg);		
		p_out_ErroresDetalle =  SELECT "Codigo", "Descripcion" FROM :v_erroroutmsg;
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
	END;
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DECLARE v_errorExt 	NVARCHAR(40);	
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
	p_out_AdvertenciasDetalle		= SELECT '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" FROM dummy where 1=2;
	p_out_ErroresDetalle			= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;
	v_ErroresDetalle       			= SELECT '' as "Codigo", '' as "Descripcion" FROM DUMMY WHERE 1=2;	
	v_AdvertenciasDetalle  			= SELECT '' as "Codigo", '' as "RequiereAutorizacion", '' as "Descripcion" FROM DUMMY WHERE 1=2;
    v_RegistrosRech	       			= SELECT 0 as "IdCargaMasiva"  FROM DUMMY WHERE 1=2;

	--------------------
	-- VALIDACIONES
	--------------------	   
	IF p_UsaReglasNegocio = 'S' THEN
	BEGIN		

		-- Validaciones datos de entrada			
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_Errores,:v_resultadoRegla);
		
		IF :v_resultadoRegla = 'RECH' THEN
			 SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		CALL "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosProcesarCargaMasiva"(:p_IdEmpresa, :p_CargaMasivaProcesar, :v_RegistrosRech ,:v_ErroresDetalle, :v_AdvertenciasDetalle, :v_resultadoRegla);
		
		IF :v_resultadoRegla = 'RECH' THEN				
			p_out_ErroresDetalle = SELECT "Codigo","Descripcion" FROM :v_ErroresDetalle;
			SIGNAL SALIDA_CONTROLADA; 	
		ELSEIF :v_resultadoRegla = 'ADVE' THEN
			--LLamada al servicio de autorizacion si se requiere
			--p_out_AdvertenciasDetalle = SELECT "Codigo",'N' as "RequiereAutorizacion", "Descripcion" FROM :v_ErroresDetalle;
			p_out_AdvertenciasDetalle = SELECT "Codigo",'N' as "RequiereAutorizacion", "Descripcion" FROM :v_AdvertenciasDetalle;
			SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	
		END IF;

	END; 
	END IF;


	--------------------
	--LOGICA
	--------------------
	BEGIN
		DECLARE v_SecuenciaCargaMasiva BIGINT;

		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;

		-- Quita los registros que no pasaron la validación
		v_CargaMasivaProcesar =  
		SELECT *
		FROM :p_CargaMasivaProcesar
		WHERE "IdCargaMasiva" NOT IN (SELECT "IdCargaMasiva" FROM :v_RegistrosRech);
		
		
		
		-------------------------------------------------------------------
		-- OBTIENE PREFERENCIAS DE PAGO / COMPLEMENTO (puede haber + de 1 CodigotipoCarga+moneda)
		-------------------------------------------------------------------

		-- Toma los parámetros de entrada(preferencias de pago editada). Si son null, toma la preferencia original
		v_PreferenciaDePagoCargaMasiva =
		SELECT
			CMP."IdCargaMasiva",
			TCM."CodigoTipoCargaMasiva",
			PPC."IdPersonaEntidadRecaudadora",			
			COALESCE(PER."NombreCompleto",'') AS "NombreCompletoEntidadRecaudadora",
			COALESCE(PER."DocumentoIdentidad",'') AS "DocumentoIdentidadEntidadRecaudadora",
			COALESCE(PER."TipoDocumentoIdentidad",'') AS "TipoDocumentoIdentidadEntidadRecaudadora",
			COALESCE(CMP."IdMoneda",PPC."IdMoneda",'') AS "IdMoneda",
			COALESCE(CMP."IdConcepto",PPC."IdConcepto",'') AS "IdConcepto",
			COALESCE(CMP."IdMedioDePago",PPC."IdMedioDePago",'') AS "IdMedioDePago",
			COALESCE(CMP."IdCuentaBanco",PPC."IdCuentaBanco",0) AS "IdCuentaBanco",
			PPC."ProfundidadProceso",
			TCM."GeneraPartida"    -- indicador si se genera partida de ingreso/egreso
			
		FROM :v_CargaMasivaProcesar AS CMP
		INNER JOIN "BSCL.Tesoreria.BD::CAR.TipoCargaMasiva" AS TCM ON (TCM."CodigoTipoCargaMasiva" = CMP."CodigoTipoCargaMasiva")
		LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarPreferenciaPagoCorredor" AS PPC ON (
						    PPC."CodigoTipoCargaMasiva" = CMP."CodigoTipoCargaMasiva"						    
						AND PPC."NombreEntidad" 	= 'TES.CargaMasiva'
						AND PPC."IdEstado" 			= 'ACT'
						AND PPC."IdEmpresa" 		= :p_IdEmpresa)
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER ON (PER."IdPersona" = PPC."IdPersonaEntidadRecaudadora");
		
		--SELECT 'PreferenciaDePagoCargaMasiva', * FROM :v_PreferenciaDePagoCargaMasiva;  -- REVISAR



		--------------------------------------------------------
		-- OBTIENE DETALLE DE CARGA MASIVA A PROCESAR
		--------------------------------------------------------
		v_CargaMasivaDetalle = 
		SELECT
			CMD."IdCargaMasivaDetalle",
			CMD."IdCargaMasiva",
		    CMD."IdTipoMovimiento",
			CMD."DocumentoIdentidad",
			CMD."Monto",
			CMD."IdPersona",
			CMD."IdPortafolio",
			CMD."Glosa",

			-- Preferencias de pago
			PPC."CodigoTipoCargaMasiva",
			PPC."IdMoneda",
			PPC."IdConcepto",
			PPC."IdMedioDePago",
			PPC."IdCuentaBanco",
			PPC."ProfundidadProceso",
			PPC."GeneraPartida",
			MYM."IdModalidadDePago",
			
			-- Datos de carga "CONCILIACION_RECAUDACION_BCP"
			CMD."BCP_CON_FechaTransaccion",
			CMD."BCP_CON_NumeroOperacion",			
			(CASE WHEN COALESCE(IE."IdIngresoEgreso",0) = 0 THEN 'N' ELSE 'S' END) AS "ExisteMovimiento",
			COALESCE(IE."IdIngresoEgreso",0) AS "IdMovimientoExistente"

		FROM :v_CargaMasivaProcesar AS CMP
		INNER JOIN "BSCL.Tesoreria.BD::CAR.CargaMasivaDetalle" AS CMD ON (CMD."IdCargaMasiva" = CMP."IdCargaMasiva")
		LEFT JOIN :v_PreferenciaDePagoCargaMasiva AS PPC ON (PPC."CodigoTipoCargaMasiva" = CMP."CodigoTipoCargaMasiva" AND PPC."IdMoneda" = CMP."IdMoneda")
		LEFT JOIN "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" AS MYM ON (MYM."IdMedioDePago" = PPC."IdMedioDePago")
		-- Para CONCILIACION_RECAUDACION_BCP chequea si el movimiento ya fue creado 
		LEFT JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS IE ON 
		(		
				IE."IdComprobanteOrigen" = CMD."BCP_CON_NumeroOperacion"
			AND	COALESCE(IE."FechaComprobanteOrigen",'0001-01-01') = CAST(COALESCE(CMD."BCP_CON_FechaTransaccion",'0001-01-01') AS DATE)
			AND IE."IdConcepto" = 'RECBCP'
			AND IE."IdEstado" NOT IN ('ANU','BLO')
			AND PPC."CodigoTipoCargaMasiva" = 'CONCILIACION_RECAUDACION_BCP' --	PARA PERU
		);
		
		--SELECT 'CargaMasivaDetalle', * FROM :v_CargaMasivaDetalle; -- REVISAR

		-------------------------------------------------------------
		-- GENERA MOVIMIENTOS DE TESORERIA (Para cada cliente)
		-------------------------------------------------------------
		INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_IngresoEgresoGenerar"
		SELECT
			CMD."IdCargaMasivaDetalle" As "NumeroReferencia",
			CMD."IdTipoMovimiento",
			'CLI'    As "IdTipoPersona",      -- Siempre viene de un cliente
			CMD."IdPersona",
			CMD."IdPortafolio",
			CMD."IdMoneda",
			CMD."Monto",
			:p_FechaProceso As "FechaLiquidacion",
			CMD."IdConcepto",
			'CLI' As "IdTipoPersonaTitularPago",
			CMD."IdPersona"    As "IdPersonaTitularPago",
			CMD."IdPortafolio" As "IdPortafolioTitularPago",
			0 "IdCaja",
			''  As "SecuenciaAgrupacion",
			null   As "SecuenciaRelacionada",
			CMD."Glosa",
			0   As "IdEmpleadoDigitador",
			0   As "IdSucursalEmpleadoDigitador",
			''  As "IdTipoProducto",
			''  As "IdTipoOperacion",
			(CASE
				WHEN (CMD."CodigoTipoCargaMasiva" = 'CONCILIACION_RECAUDACION_BCP') THEN 'BAT'   -- BAT: Batch   WS: Online via WS
				ELSE ''
			END) As "IdTipoComprobante", 
			
			(CASE
				WHEN (CMD."CodigoTipoCargaMasiva" = 'CONCILIACION_RECAUDACION_BCP') THEN CMD."BCP_CON_NumeroOperacion"
				ELSE 0
			END) As "IdComprobante", 	
			
			(CASE
				WHEN (CMD."CodigoTipoCargaMasiva" = 'CONCILIACION_RECAUDACION_BCP') THEN CAST(CMD."BCP_CON_FechaTransaccion" AS DATE)
				ELSE '0001-01-01'
			END) As "FechaComprobante", 	
			
			'0001-01-01' As "FechaLiquidacionComprobante",
			0 As "IdOperacion",		           
			'0001-01-01' As "FechaOperacion",
			''  As "IdCondicionLiquidacion",
			''  As "IdBolsa",
			''  As "IdMonedaOperacion",
			0   As "MontoOperacion",
			''  As "IdTipoReajuste",
			'0001-01-01' As "FechaParidad",
			0   As "ValorParidad",
			0   As "FolioCartera",
			0   As "IdInstrumento",
			''  As "IdTipoInstrumento",
			''  As "IdClasificacionTipoInstrumento",
			'N' As "Bilateral",
			CASE
				WHEN (CMD."ProfundidadProceso" = 'COM') THEN 'S'  -- llegar hasta comprobante
				WHEN (CMD."ProfundidadProceso" = 'MOV') THEN 'N'  -- llegar hasta el movimiento
				WHEN (CMD."ProfundidadProceso" = 'LIQ') THEN 'S'  -- llega hasta comprobante(después se liquida)
				ELSE 'N'
			END	As "GeneraComprobante",
			
			'N' As "AgruparEnComprobante",
			'S' As "MueveCuentaInversion",
			'N' As "TitularPago"
		FROM  :v_CargaMasivaDetalle AS CMD
		WHERE CMD."ExisteMovimiento" = 'N';
		
		--SELECT 'GBL.CrearIngresoEgreso_Entrada_IngresoEgresoGenerar', * FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_IngresoEgresoGenerar"; -- REVISAR


		-- Asigna forma de pago a movimientos(si no existe configuración para forma de pago, no se crea)
		INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_FormaDePago"
	    SELECT
            CMD."IdCargaMasivaDetalle" As "NumeroReferencia",
			CMD."IdMedioDePago",
			CMD."IdModalidadDePago",
			CMD."IdMoneda",
			CMD."Monto",
			CMD."IdCuentaBanco" As "IdCuentaBancariaCorredora",
			0  As "IdTarjetaBancariaCorredora",
			0  As "IdChequera",
			0  As "NumeroDocumentoCorredora",
			'0001-01-01' As "FechaDocumentoPagoCorredora",
			0  As "IdCuentaBancariaSocio",
			0  As "IdBancoSocio",
			'' As "NombreCuentaBancariaSocio",
			'' As "NumeroCuentaBancariaSocio",
			'' As "IdTipoCuentaBancariaSocio",
			'' As "NombreTipoCuentaBancariaSocio",     
			0  As "IdTarjetaBancariaSocio",
			0  As "IdTipoTarjetaBancariaSocio",
			'' As "NombreTipoTarjetaBancariaSocio",
			'' As "NumeroTarjetaBancariaSocio",
			'' As "NumeroSerieSocio",
			0  As "NumeroDocumentoSocio",
			'0001-01-01' As "FechaDocumentoPagoSocio"
		FROM  :v_CargaMasivaDetalle AS CMD
		WHERE COALESCE(CMD."IdMedioDePago",'') <> '' -- si no existe preferencia de pago para forma de pago, no se crea
		AND   COALESCE(CMD."IdMoneda",'') <> ''
		AND   CMD."ExisteMovimiento" = 'N';
		
		--SELECT 'GBL.CrearIngresoEgreso_Entrada_FormaDePago', * FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_FormaDePago"; -- REVISAR


		IF (NOT IS_EMPTY("BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_IngresoEgresoGenerar")) THEN
		
				CALL "BSCL.Tesoreria.BD.Servicios::CrearIngresoEgreso"(
					 p_IdEmpresa	  				=> :p_IdEmpresa
				   	,p_IdUsuario					=> :p_IdUsuario
				   	,p_IdModulo						=> :p_IdModulo
				   	,p_simularOperacion				=> 'N'
				   	,p_UsaReglasNegocio				=> 'S'
					,p_EsIngresoAutomatico   		=> 'S'
				    ,p_out_InformacionContador  	=> v_out_InformacionContador
				   	,p_out_AdvertenciasContador		=> v_out_AdvertenciasContador
				   	,p_out_ErroresContador			=> v_out_ErroresContador
				);
		
				p_out_ErroresContador := p_out_ErroresContador + v_out_ErroresContador + v_out_AdvertenciasContador;
				    
				p_out_ErroresDetalle = 	SELECT "Codigo", "Descripcion"  FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_ErroresDetalle"      UNION ALL
				                        SELECT "Codigo", "Descripcion"  FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_AdvertenciasDetalle" UNION ALL
			                            SELECT "Codigo", "Descripcion"  FROM :p_out_ErroresDetalle;
		
		                                
				IF v_out_ErroresContador > 0 THEN
				       SIGNAL SALIDA_CONTROLADA;
			    END IF;

		END IF; -- IF (NOT IS_EMPTY("BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_IngresoEgresoGenerar"))	    


		
		-------------------------------------------------------------------------------
		-- GENERA PARTIDAS DE INGRESO/EGRESO  
		-------------------------------------------------------------------------------
		
		v_PartidaDetalle = 
		SELECT
			CMD."IdCargaMasiva",
			CMD."IdTipoMovimiento",
			CMD."IdMoneda",
			IEG."IdIngresoEgreso",
			CMD."IdMedioDePago",
			CMD."IdModalidadDePago",
			CMD."Monto",
			CMD."IdCuentaBanco"
		FROM  :v_CargaMasivaDetalle AS CMD
		INNER JOIN "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_IngresoEgresoGenerado" AS IEG ON (IEG."NumeroReferencia" = CMD."IdCargaMasivaDetalle")
		WHERE CMD."GeneraPartida" = 'S'; -- indicador de si se genera partida de ingreso/egreso
		--SELECT 'PartidaDetalle', * FROM :v_PartidaDetalle; -- REVISAR
				
				
		-- Arma grupos para generar partidas 
		v_Partida = 
		SELECT 
			ROW_NUMBER() over(ORDER BY PD."IdCargaMasiva",  PD."IdTipoMovimiento", PD."IdMoneda",PD."IdMedioDePago",PD."IdCuentaBanco") AS "IdPartidaReferencial",
			PD."IdCargaMasiva", PD."IdTipoMovimiento", PD."IdMoneda",PD."IdMedioDePago",PD."IdCuentaBanco", SUM(PD."Monto") As "Monto", MAX(PD."IdModalidadDePago") AS "IdModalidadDePago" 
		FROM  :v_PartidaDetalle AS PD
		GROUP BY PD."IdCargaMasiva", PD."IdTipoMovimiento", PD."IdMoneda",PD."IdMedioDePago",PD."IdCuentaBanco";
		--SELECT 'Partida', * FROM :v_Partida; -- REVISAR


		INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_IngresoEgresoGenerar"
		SELECT 
			PA."IdPartidaReferencial" AS "NumeroReferencia",
			PD."IdIngresoEgreso",			
			(CASE
	 	 		WHEN PD."IdTipoMovimiento" = 'MOVING' THEN 'PARING'
	 	 		WHEN PD."IdTipoMovimiento" = 'MOVEGR' THEN 'PAREGR'
			END) AS "TipoComprobante"
		FROM :v_Partida AS PA
		INNER JOIN :v_PartidaDetalle AS PD ON 
			(
				PD."IdCargaMasiva" 		= PA."IdCargaMasiva"
			AND PD."IdTipoMovimiento" 	= PA."IdTipoMovimiento"
			AND PD."IdMoneda" 			= PA."IdMoneda"
			AND PD."IdMedioDePago" 		= PA."IdMedioDePago"
			AND PD."IdCuentaBanco" 		= PA."IdCuentaBanco"
			);
		--SELECT 'GBL.CrearComprobante_Entrada_IngresoEgresoGenerar', * FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_IngresoEgresoGenerar"; -- REVISAR		
		
		

	    INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_FormaDePago"
	    SELECT
			"IdPartidaReferencial" AS "NumeroReferencia",		-- Id referencial
			"IdMedioDePago",
			"IdModalidadDePago",
			"IdMoneda",
			"Monto",
			"IdCuentaBanco" As "IdCuentaBancariaCorredora",
			0  As "IdTarjetaBancariaCorredora",
			0  As "IdChequera",
			0  As "NumeroDocumentoCorredora",
			'0001-01-01' As "FechaDocumentoPagoCorredora",
			0  As "IdCuentaBancariaSocio",
			0  As "IdBancoSocio",
			'' As "NombreCuentaBancariaSocio",
			'' As "NumeroCuentaBancariaSocio",
			'' As "IdTipoCuentaBancariaSocio",
			'' As "NombreTipoCuentaBancariaSocio",
			0  As "IdTarjetaBancariaSocio",
			0  As "IdTipoTarjetaBancariaSocio",
			'' As "NombreTipoTarjetaBancariaSocio",
			'' As "NumeroTarjetaBancariaSocio",
			'' As "NumeroSerieSocio",
			0  As "NumeroDocumentoSocio",
			'0001-01-01' As "FechaDocumentoPagoSocio"
		FROM  :v_Partida;
		--SELECT 'BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_FormaDePago', * FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_FormaDePago"; -- REVISAR
		
        
		IF (NOT IS_EMPTY("BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_IngresoEgresoGenerar")) THEN
		
				CALL "BSCL.Tesoreria.BD.Servicios::CrearComprobante"(
					 p_IdEmpresa	  						=> :p_IdEmpresa
				   	,p_IdUsuario							=> :p_IdUsuario
				   	,p_IdModulo								=> :p_IdModulo		
				   	,p_simularOperacion   					=> 'N'
				   	,p_UsaReglasNegocio						=> 'S'
					,p_FechaMovimiento        				=> :p_FechaProceso
					,p_IdCaja                 				=> 0
					,p_GenerarComprobanteDesdeMovimientos 	=> 'S'
					,p_EsIngresoAutomatico              	=> 'S'
					,p_out_InformacionContador  			=> v_out_InformacionContador
				    ,p_out_AdvertenciasContador 			=> v_out_AdvertenciasContador
				  	,p_out_ErroresContador      			=> v_out_ErroresContador
				);
				
		
				p_out_ErroresContador := p_out_ErroresContador + v_out_ErroresContador + v_out_AdvertenciasContador;
				    
				p_out_ErroresDetalle =    SELECT "Codigo", "Descripcion" FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_ErroresDetalle"      UNION ALL
				                          SELECT "Codigo", "Descripcion" FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_AdvertenciasDetalle" UNION ALL
			                              SELECT "Codigo", "Descripcion" FROM :p_out_ErroresDetalle;
		
				IF v_out_ErroresContador > 0 THEN
				       SIGNAL SALIDA_CONTROLADA;
			    END IF;
		
		END IF;  -- IF (NOT IS_EMPTY("BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_IngresoEgresoGenerar"))
		

		
		
		-----------------------------------------------------   
		-- ACTUALIZA CARGAS MASIVAS PROCESADAS
		-----------------------------------------------------
		v_ActualizarCargaMasiva =
		SELECT
			CMP."IdCargaMasiva",
			CASE
				WHEN COUNT(DISTINCT CMD."IdMoneda") > 0 THEN '' -- Multiples Monedas  
				ELSE MAX(CMD."IdMoneda")  						-- Una sola moneda
			END  AS "IdMoneda",			  
			MAX(CMD."IdConcepto") AS "IdConcepto"
		FROM "BSCL.Tesoreria.BD::CAR.CargaMasiva" AS CM
		INNER JOIN :v_CargaMasivaProcesar AS CMP ON (CMP."IdCargaMasiva" = CM."IdCargaMasiva")
		INNER JOIN :v_CargaMasivaDetalle AS CMD ON (CMD."IdCargaMasiva" = CM."IdCargaMasiva")
		GROUP BY CMP."IdCargaMasiva";
		--SELECT * FROM "BSCL.Tesoreria.BD::CAR.CargaMasivaDetalle";
		
		
		-- Cambia cargas masivas procesadas
		UPDATE CM
		SET 
			 CM."IdEstado"     = 'PRO'
			,CM."FechaProceso" = CAST(:p_FechaProceso AS SECONDDATE)
			,CM."IdMoneda"     = ACM."IdMoneda"
			,CM."IdConcepto"   = ACM."IdConcepto"
		FROM "BSCL.Tesoreria.BD::CAR.CargaMasiva" AS CM
		INNER JOIN :v_ActualizarCargaMasiva AS ACM ON (ACM."IdCargaMasiva" = CM."IdCargaMasiva");
		
		
		
		-- Cambia estado del detalle de cargas masivas
		UPDATE CMD
		SET 
			CMD."IdEstado" = (CASE 
								  WHEN (COALESCE(IEG."IdIngresoEgreso",0) <> 0) THEN 'PRO'
								  ELSE CMD."IdEstado" 
							  END),							  
			CMD."IdMovimientoComprobante" = COALESCE(IEG."IdIngresoEgreso",0),  -- Id del movimiento generado
			CMD."ObservacionValidacion" = (CASE 
											  WHEN (VCMD."CodigoTipoCargaMasiva" = 'CONCILIACION_RECAUDACION_BCP' AND VCMD."ExisteMovimiento" = 'S') THEN 'Movimiento Existe con Folio N° ' || CAST(VCMD."IdMovimientoExistente" AS NVARCHAR(50))
											  ELSE CMD."ObservacionValidacion" 
										   END)
			
		FROM "BSCL.Tesoreria.BD::CAR.CargaMasivaDetalle" AS CMD
		INNER JOIN :v_CargaMasivaProcesar AS CMP ON (CMP."IdCargaMasiva" = CMD."IdCargaMasiva")
		LEFT JOIN "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_IngresoEgresoGenerado" AS IEG ON (IEG."NumeroReferencia" = CMD."IdCargaMasivaDetalle")		
		LEFT JOIN :v_CargaMasivaDetalle AS VCMD ON (VCMD."IdCargaMasivaDetalle" = CMD."IdCargaMasivaDetalle");
		
		
		


  	END;    
END;