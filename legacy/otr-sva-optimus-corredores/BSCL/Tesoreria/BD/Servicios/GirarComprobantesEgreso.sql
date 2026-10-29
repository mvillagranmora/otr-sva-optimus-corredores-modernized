SET SCHEMA "BSCL";
GO

DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::GirarComprobantesEgreso";
GO

CREATE PROCEDURE 
--
--  Proposito: Girar comprobantes de egreso y genera una partida de egreso (PAREGR)
--  Autor: JVILLARAN - BCS
--  Fecha Creacion: 10/03/2016
-- 
"BSCL.Tesoreria.BD.Servicios::GirarComprobantesEgreso" 
(
    -- Parametros de Validacion
    IN p_IdEmpresa 						BIGINT,  
    IN p_IdUsuario 						BIGINT, 
    IN p_IdModulo 						NVARCHAR(3),
    IN p_simularOperacion 				NVARCHAR(1),				-- S/N se usa para simular operacion (algunos casos)
    IN p_UsaReglasNegocio 				NVARCHAR(1),				-- S/N se usa en caso de servicios anidados.   
   
     -- Parametros de Proceso
	IN p_IdCaja							BIGINT			DEFAULT 0, 	-- Solo en cero cuando se realice una simulación del proceso
    IN p_FechaMovimiento				NVARCHAR(10),   			-- Fecha Sistema	 
	IN p_IdMedioDePago 				    NVARCHAR(3),    			-- CHE, EFE, TRA, VVI, CIN
    IN p_IdCuentaBancariaCorredora 		BIGINT,
    IN p_IdModalidadDePago              NVARCHAR(6),    			-- MODCAJ: Por Caja   MODDEP: Depósito
    IN p_Monto 		                    DECIMAL(26,8),  			-- Monto total del giro
    IN p_UsaTalonarioCheque             NVARCHAR(1) 	DEFAULT 'N',
    IN p_GeneraNomina 					NVARCHAR(1),                -- S/N entra como parámetro ya que puede ser editado en el proceso  
	IN p_GeneraTransferencia 			NVARCHAR(1),				-- S/N entra como parámetro ya que puede ser editado en el proceso
	
    IN p_ComprobantesAGirar	            TABLE("IdComprobante" BIGINT),    
    IN p_FormasDePago					TABLE( -- Solo las formas de pago a editar
											  "IdComprobante" 					BIGINT,
											  "IdComprobanteFormaDePago" 		BIGINT,
											  "IdCuentaBancariaSocio" 			BIGINT,
											  "IdBancoSocio"					BIGINT,
											  "NombreCuentaBancariaSocio"		NVARCHAR(100),
											  "NumeroCuentaBancariaSocio"		NVARCHAR(100),
											  "IdTipoCuentaBancariaSocio"		NVARCHAR(4),
											  "NombreTipoCuentaBancariaSocio"	NVARCHAR(100),
											  "IdTarjetaBancariaSocio"			BIGINT,
											  "IdTipoTarjetaBancariaSocio"		NVARCHAR(40),
											  "NombreTipoTarjetaBancariaSocio"	NVARCHAR(100),
											  "NumeroTarjetaBancariaSocio"		NVARCHAR(100),
											  "NumeroSerieSocio"				NVARCHAR(40),
											  "NumeroDocumentoSocio"			BIGINT,
											  "FechaDocumentoPagoSocio"			SECONDDATE),


    
    -- Salidas Tesoreria (común en todos los comprobantes)
    OUT p_out_MediosDePagoAGirar        TABLE("IdMedioDePago" NVARCHAR(3), "MontoPorMedioDePago" DECIMAL(26,8)),
    OUT p_out_IdConcepto                NVARCHAR(6),
	OUT p_out_NombreConcepto	        NVARCHAR(100),
    OUT p_out_IdMoneda 				    NVARCHAR(3), 
    OUT p_out_NombreMoneda 			    NVARCHAR(100),    
    OUT p_out_FechaLiquidacion          NVARCHAR(10),
    OUT p_out_IdCaja                    BIGINT,
    OUT p_out_NombreCaja 			    NVARCHAR(100),
    OUT p_out_ResultadoProceso			TABLE("Campo" VARCHAR(40),"Id" BIGINT, "Servicio" VARCHAR(250), "Resultado" VARCHAR(40)), 
    
    --Salidas para mensajes de advertencias y errores
    OUT p_out_AdvertenciasContador 		INT,
    OUT p_out_AdvertenciasDetalle  		TABLE ("Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000)),
  	OUT p_out_ErroresContador      		INT,
    OUT p_out_ErroresDetalle       		TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))
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
    DECLARE v_ComprobantesRechazados	TABLE("IdComprobante" BIGINT);


	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT count("Codigo") into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT count("Codigo") into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	END;

	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN
		v_hash= SELECT '' as "Codigo",'' as "Valor" FROM DUMMY;	
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_ErrorNoData,:v_hash,:v_erroroutmsg);
		p_out_ErroresDetalle =  SELECT :v_ErrorNoData  as "Codigo",:v_erroroutmsg as "Descripcion" FROM dummy UNION ALL
								SELECT * FROM :p_out_ErroresDetalle;
		SIGNAL SALIDA_CONTROLADA;
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN	
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT count(1) into p_out_ErroresContador 
			FROM :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;

	DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
	BEGIN
		v_ErrorNoData := 'DATA_NO_EXISTE';
		SIGNAL SALIDA_CONTROLADA_NO_DATA;
	END;
	
	p_out_AdvertenciasContador		:= 0;
	p_out_ErroresContador			:= 0;
	p_out_AdvertenciasDetalle		= SELECT '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" FROM dummy where 1=2;
	p_out_ErroresDetalle			= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;  
	v_ComprobantesRechazados        = SELECT 0 AS "IdComprobante" FROM dummy WHERE 1=2;  
	
	p_out_MediosDePagoAGirar        = SELECT '' AS "IdMedioDePago",0 AS "MontoPorMedioDePago" FROM dummy WHERE 1=2;
	p_out_ResultadoProceso			= SELECT '' as "Campo", 0 as "Id", '' as "Servicio", '' as "Resultado" FROM dummy where 1=2;
    v_MediosDePagoAGirar            = SELECT '' AS "IdMedioDePago", 0 AS "MontoPorMedioDePago" FROM dummy WHERE 1=2;  
    	
	--------------------
	-- VALIDACIONES
	--------------------	   
	IF p_UsaReglasNegocio = 'S' THEN
	BEGIN
		DECLARE v_resultadoRegla 	NVARCHAR(40);
		DECLARE v_ErroresDetalle	TABLE ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520));
		
		v_ErroresDetalle			= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;

		-- Validaciones de datos básicos
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_Errores,:v_resultadoRegla);		
		IF :v_resultadoRegla = 'RECH' THEN
			 SIGNAL SALIDA_CONTROLADA; 	
		END IF;

		-- Validaciones de negocio de los comprobantes a girar
		CALL "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosGirarComprobantesEgreso"(
			:p_ComprobantesAGirar,
			:p_IdCuentaBancariaCorredora,
			:p_simularOperacion, 
			:p_out_MediosDePagoAGirar,
			:p_out_IdConcepto,
			:p_out_NombreConcepto,
			:p_out_IdMoneda,
			:p_out_NombreMoneda,
			:p_out_FechaLiquidacion,
			p_out_IdCaja,
			p_out_NombreCaja,
			:v_ComprobantesRechazados,
			:v_ErroresDetalle,
			:v_resultadoRegla
		);		
		--SELECT 'v_resultadoRegla', :v_resultadoRegla FROM DUMMY;   -- REVISAR
		
		IF :v_resultadoRegla = 'RECH' THEN
			p_out_ErroresDetalle = SELECT "Codigo","Descripcion" FROM :v_ErroresDetalle;
			SIGNAL SALIDA_CONTROLADA;

		ELSEIF :v_resultadoRegla = 'ADVE' THEN
			--LLamada al servicio de autorizacion si se requiere
			p_out_AdvertenciasDetalle = SELECT "Codigo",'N' as "RequiereAutorizacion", "Descripcion" FROM :v_ErroresDetalle;
		END IF;    

	END;
	END IF;	
	
	-- Solo para simulacion del servicio.
	IF p_simularOperacion = 'S' then
		SIGNAL SALIDA_CONTROLADA;
	END IF;


	--------------------
	--LOGICA
	--------------------
	BEGIN
		 DECLARE v_IdTipoCuentaBancariaCorredora  	    NVARCHAR(4);
		 DECLARE v_NombreTipoCuentaBancariaCorredora    NVARCHAR(100);
		 DECLARE v_NombreCuentaBancariaCorredora 		NVARCHAR(100);
		 DECLARE v_NumeroCuentaBancariaCorredora   	    NVARCHAR(100);		 
		 DECLARE v_IdNominaTransferencia 				BIGINT;
		 DECLARE v_IdPersonaContraparteBanco			BIGINT:=0;
		 DECLARE v_IdMovimientoIngresoEgreso 		    BIGINT;		 
	     DECLARE v_IdComprobantePartidaEgreso           BIGINT:=0;	     
	     DECLARE v_CountErroresDetalle                  INTEGER;
    	 DECLARE v_CountComprobantePartidaEgreso        INTEGER;
	     

    	 -- Creación de comprobante partida de egreso
	     DECLARE v_ResultadoProceso				    TABLE ("Campo" VARCHAR(40),"Id" BIGINT, "Servicio" VARCHAR(250), "Resultado" VARCHAR(40));
	     DECLARE v_InformacionContador				INT;
	     DECLARE v_AdvertenciasContador 			INT;
	     DECLARE v_ErroresContador      			INT;
	     DECLARE v_AdvertenciasDetalle  			TABLE ("Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000));	  	
	     DECLARE v_ErroresDetalle       			TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000));
		 DECLARE v_IngresoEgresoGenerado            TABLE ("IdIngresoEgreso" BIGINT);
		 


		 -- Tabla para el movimiento de egreso de la partida de egresos
		 DECLARE v_IngresoEgresoGenerar  			TABLE (
														"NumeroIngresoEgreso"						BIGINT, 
														"IdTipoMovimiento"  						NVARCHAR(6),
													   	"IdTipoPersona"								NVARCHAR(3),  -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)
													   	"IdPersona"    								BIGINT,   
													   	"IdMoneda"									NVARCHAR(3),
													   	"Monto" 									DECIMAL(26,8),
													   	"FechaLiquidacion"           				NVARCHAR(10),                                                                      
													   	"IdConcepto" 								NVARCHAR(6),
													   	"IdTipoPersonaTitularPago"					NVARCHAR(3),  -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)
													   	"IdPersonaTitularPago"    					BIGINT,
													   	"IdPortafolioTitularPago"					BIGINT,
													   	"IdCaja"   									BIGINT,
													   	"SecuenciaAgrupacion" 						NVARCHAR(40),
													   	"Glosa" 									NVARCHAR(260),
													   	"IdAgente" 									BIGINT,
													   	"IdSucursal" 								BIGINT,
													    "IdTipoComprobanteFacturacion"				NVARCHAR(3),
													   	"IdComprobanteFacturacion"					BIGINT,
													   	"FechaComprobanteFacturacion"				SECONDDATE,
													    "FechaLiquidacionComprobanteFacturacion"	SECONDDATE,
													    "FolioCartera" 								BIGINT,
													    "IdInstrumento" 							BIGINT,
													    "IdTipoInstrumento" 						NVARCHAR(3),
													    "IdClasificacionTipoInstrumento" 			NVARCHAR(3),
													    "IdTipoProducto"   							NVARCHAR(4),
													    "IdTipoOperacion" 							NVARCHAR(6),
													    "IdOperacion" 								BIGINT,
													    "FechaOperacion"							SECONDDATE,
													    "IdCondicionLiquidacion"					NVARCHAR(2),
													    "IdBolsa" 									NVARCHAR(3),
													    "IdMonedaOperacion" 						NVARCHAR(3),
													    "MontoOperacion" 							DECIMAL(26,8),
													    "IdTipoReajuste" 							NVARCHAR(3),
													    "FechaParidad" 								SECONDDATE,
													    "ValorParidad" 								DECIMAL(26,8),
													    "Bilateral"									NVARCHAR(1),  --Cleanimg
													    "GeneraComprobante"							NVARCHAR(1),  --S: genera comprobante automático									    
													    "AgruparEnComprobante"						NVARCHAR(1),  --S: permite agrupar varios mov. en un comprobante   
													    "TitularPago"								NVARCHAR(1)); --S: Socio que genera movimiento es quién cobra o paga


		DECLARE v_FormaDePago				        TABLE(
				   										"NumeroIngresoEgreso"				BIGINT,   
														"IdMedioDePago"						NVARCHAR(3), -- CHE, EFE, TRA, VVI, CIN
												 		"IdModalidadDePago"					NVARCHAR(6), -- MODCAJ: Por Caja   MODDEP: Depósito
														"IdMoneda"							NVARCHAR(3),
														"Monto"								DECIMAL(26,8),									
														"IdCuentaBancariaCorredora"			BIGINT,
														"IdTarjetaBancariaCorredora"		BIGINT,
														"IdChequera"						BIGINT,
														"NumeroDocumentoCorredora"			BIGINT,
														"FechaDocumentoPagoCorredora"		SECONDDATE,
														"IdCuentaBancariaSocio" 			BIGINT,
														"IdBancoSocio"						BIGINT,
														"NombreCuentaBancariaSocio"			NVARCHAR(100),
														"NumeroCuentaBancariaSocio"			NVARCHAR(100),
														"IdTipoCuentaBancariaSocio"			NVARCHAR(4),
														"NombreTipoCuentaBancariaSocio"		NVARCHAR(100),
														"IdTarjetaBancariaSocio"			BIGINT,
														"IdTipoTarjetaBancariaSocio"		NVARCHAR(40),
														"NombreTipoTarjetaBancariaSocio"	NVARCHAR(100),
														"NumeroTarjetaBancariaSocio"		NVARCHAR(100),
														"NumeroSerieSocio"					NVARCHAR(40),
														"NumeroDocumentoSocio"				BIGINT,
														"FechaDocumentoPagoSocio"			SECONDDATE																								
														);
		 
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;
		
		
		 -------------------------------------------------------------------
		 -- OBTIENE DATOS ADICIONALES	
		 -------------------------------------------------------------------
		 -- Obtiene los datos de la cuenta bancaria de la corredora con la cual se va a pagar
		 SELECT "IdTipoCuentaBancaria", "NombreTipoCuentaBancaria", "NombreCuentaBancaria", "NumeroCuentaBancaria", "IdPersona"
	     INTO v_IdTipoCuentaBancariaCorredora, v_NombreTipoCuentaBancariaCorredora, v_NombreCuentaBancariaCorredora, v_NumeroCuentaBancariaCorredora, v_IdPersonaContraparteBanco
		 FROM "BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias"
		 WHERE "IdCuentaBanco" = :p_IdCuentaBancariaCorredora;
		 -- SELECT :v_IdTipoCuentaBancariaCorredora, :v_NombreTipoCuentaBancariaCorredora, :v_NombreCuentaBancariaCorredora, :v_NumeroCuentaBancariaCorredora, :v_IdPersonaContraparteBanco FROM DUMMY;
		
		 
		-------------------------------------------------------------
		-- ACTUALIZA LAS FORMAS DE PAGO DEL COMPROBANTES
		-------------------------------------------------------------
		-- Modifica los datos de la corredora para las formas de pago giradas(actualiza todos los registros)
		UPDATE CFP
		SET
			  "IdCuentaBancariaCorredora" 			= :p_IdCuentaBancariaCorredora
			 ,"IdTipoCuentaBancariaCorredora"  	    = :v_IdTipoCuentaBancariaCorredora
			 ,"NombreTipoCuentaBancariaCorredora"   = :v_NombreTipoCuentaBancariaCorredora
			 ,"NombreCuentaBancariaCorredora" 		= :v_NombreCuentaBancariaCorredora
			 ,"NumeroCuentaBancariaCorredora"   	= :v_NumeroCuentaBancariaCorredora
		FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP
        INNER JOIN :p_ComprobantesAGirar AS GIR ON (CFP."IdComprobante" = GIR."IdComprobante")
        AND CFP."IdMedioDePago" = :p_IdMedioDePago     -- medio de pago que se está girando
        AND CFP."IdEstado" <> 'ANU';



		-- Modifica los datos del socio para las formas de pago giradas(actualiza sólo registros editados)
		UPDATE CFP
		SET 
			  "IdCuentaBancariaSocio" 		        = FormasDePago."IdCuentaBancariaSocio"			 
			 ,"IdBancoSocio"                        = FormasDePago."IdBancoSocio"
			 ,"NombreCuentaBancariaSocio" 		    = FormasDePago."NombreCuentaBancariaSocio"			 
			 ,"NumeroCuentaBancariaSocio"   	    = FormasDePago."NumeroCuentaBancariaSocio"
			 ,"IdTipoCuentaBancariaSocio"	        = FormasDePago."IdTipoCuentaBancariaSocio"
			 ,"NombreTipoCuentaBancariaSocio"	    = FormasDePago."NombreTipoCuentaBancariaSocio"
			 ,"IdTarjetaBancariaSocio" 			    = FormasDePago."IdTarjetaBancariaSocio"
			 ,"IdTipoTarjetaBancariaSocio" 		    = FormasDePago."IdTipoTarjetaBancariaSocio"			 
			 ,"NombreTipoTarjetaBancariaSocio"	    = FormasDePago."NombreTipoTarjetaBancariaSocio"			 
			 ,"NumeroTarjetaBancariaSocio"   	    = FormasDePago."NumeroTarjetaBancariaSocio"
			 ,"NumeroSerieSocio"		 			= FormasDePago."NumeroSerieSocio"
			 ,"NumeroDocumentoSocio"    			= FormasDePago."NumeroDocumentoSocio"
			 ,"FechaDocumentoPagoSocio" 			= FormasDePago."FechaDocumentoPagoSocio"
		FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP
		INNER JOIN  :p_FormasDePago AS FormasDePago  ON (CFP."IdComprobante" = FormasDePago."IdComprobante" AND CFP."IdComprobanteFormaDePago" = FormasDePago."IdComprobanteFormaDePago")
        AND CFP."IdMedioDePago" = :p_IdMedioDePago     -- medio de mago que se está girando
        AND CFP."IdEstado" <> 'ANU';

        
		-------------------------------------------------------------------
		-- GENERA COMPROBANTE DE PARTIDA DE EGRESO
		-------------------------------------------------------------------

		INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_IngresoEgresoGenerar"
		SELECT 0 as "NumeroReferencia",0 as "IdIngresoEgreso",'' as "TipoComprobante" FROM DUMMY WHERE 1=2;

		INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_ComprobanteGenerar"
		SELECT 1 as "NumeroReferencia","IdComprobante", 'PAREGR' as "TipoComprobante" FROM :p_ComprobantesAGirar;
		--SELECT 'Entrada_ComprobanteGenerar', * FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_ComprobanteGenerar"; -- REVISAR
		
		INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_FormaDePago"
		SELECT 	 1  						  AS "NumeroReferencia"
				,:p_IdMedioDePago 			  AS "IdMedioDePago"
				,:p_IdModalidadDePago 		  AS "IdModalidadDePago"
				,:p_out_IdMoneda 			  AS "IdMoneda"
				,:p_Monto 					  AS "Monto"
				,:p_IdCuentaBancariaCorredora AS "IdCuentaBancariaCorredora"
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
		FROM DUMMY;
		--SELECT 'Entrada_FormaDePago', * FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_FormaDePago";

		

		CALL "BSCL.Tesoreria.BD.Servicios::CrearComprobante" (
		     p_IdEmpresa 							=> p_IdEmpresa
		    ,p_IdUsuario							=> p_IdUsuario
		    ,p_IdModulo								=> p_IdModulo			    
		    ,p_simularOperacion						=> 'N'
		    ,p_UsaReglasNegocio						=> 'N'
		    ,p_FechaMovimiento						=> p_FechaMovimiento
			,p_IdCaja								=> p_IdCaja
			,p_GenerarComprobanteDesdeMovimientos	=> 'N'
			,p_GenerarComprobanteDesdeComprobantes	=> 'S'
			,p_EsIngresoAutomatico					=> 'S'
			,p_out_InformacionContador  			=> v_InformacionContador          
			,p_out_AdvertenciasContador    			=> :v_AdvertenciasContador
			,p_out_ErroresContador         			=> :v_ErroresContador
		);

		p_out_ErroresDetalle		=  SELECT "Codigo", "Descripcion" FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_ErroresDetalle" UNION ALL
									   SELECT "Codigo", "Descripcion" FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_AdvertenciasDetalle" UNION ALL		
									   SELECT "Codigo", "Descripcion" FROM :p_out_ErroresDetalle;
		p_out_ResultadoProceso		=  SELECT "Campo","Id","Servicio","Resultado" FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_ResultadoProceso" UNION ALL
									   SELECT "Campo","Id","Servicio","Resultado" FROM :p_out_ResultadoProceso;
		   
		--SELECT 'p_out_ErroresDetalle', *   FROM :p_out_ErroresDetalle;   -- REVISAR
		--SELECT 'p_out_ResultadoProceso', * FROM :p_out_ResultadoProceso; -- REVISAR



		-- Determina si hubieron errores en la generación del comprobante de PAREGR
		SELECT Count("Codigo") INTO v_CountErroresDetalle FROM :p_out_ErroresDetalle;								        
		IF (v_CountErroresDetalle > 0) THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		

		--Obtiene Id del comprobante de la PAREGR creada
		SELECT Count("Id")
		INTO v_CountComprobantePartidaEgreso
		FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_ResultadoProceso"
		WHERE "Campo" = 'IdComprobante' AND "Resultado" = 'ING';

		IF (v_CountComprobantePartidaEgreso > 0) THEN
			-- Se generó correctamente PAREGR
			SELECT "Id" 
			INTO v_IdComprobantePartidaEgreso
			FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_ResultadoProceso"
			WHERE "Campo" = 'IdComprobante' AND "Resultado" = 'ING';
		ELSE
			-- NO se generó PAREGR
			p_out_ErroresDetalle		=  SELECT 'PARTIDA_EGRESO_NO_GENERADA' As "Codigo", 'La partida de egreso no fue generada' As "Descripcion" FROM DUMMY   UNION ALL
										   SELECT "Codigo", "Descripcion" FROM :p_out_ErroresDetalle;


			SIGNAL SALIDA_CONTROLADA;
		END IF;			

		
		-------------------------------------------------------------------
		-- GIRA LOS COMPROBANTES Y FORMAS DE PAGO
		-------------------------------------------------------------------
		-- Modifica estado de las formas de pago
		UPDATE CFP
		SET
		   	  "IdUsuarioGiro"        = :p_IdUsuario  
			 ,"FechaGiro"         	 = CURRENT_UTCTIMESTAMP
		 	 ,"IdEstado"          	 = 'GIR'
		 	 ,"IdComprobantePartida" = :v_IdComprobantePartidaEgreso
		FROM  "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP
		INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS COM ON (COM."IdComprobante" = CFP."IdComprobante")
		INNER JOIN :p_ComprobantesAGirar AS GIR ON (COM."IdComprobante" = GIR."IdComprobante")
		WHERE CFP."IdMedioDePago" = :p_IdMedioDePago
		AND CFP."IdEstado" NOT IN ('ANU','MOD');
		
		-- Calcula si el comprobante fue girado total o parcialmnete.
		v_EstadoComprobantes =		 
		SELECT  
			  CFP."IdComprobante"
			 ,(COUNT(CFP."IdComprobanteFormaDePago") - COUNT(CASE WHEN CFP."IdEstado" = 'GIR' THEN 1 END)) as "GiroParcial"
		FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP
		INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS COM ON (COM."IdComprobante" = CFP."IdComprobante")
		INNER JOIN :p_ComprobantesAGirar AS GIR ON (COM."IdComprobante" = GIR."IdComprobante")
		WHERE (COM."IdEstado" = 'ING' OR COM."IdEstado" = 'GIP') -- Ingresado / Girado Parcialmente		 
		AND CFP."IdEstado" NOT IN ('ANU','MOD','LIQ')		
		GROUP BY CFP."IdComprobante";


		-- Actualiza estado de los comprobantes 	
		UPDATE COM
		SET "IdEstado" = (CASE  WHEN (EST."GiroParcial" = 0) THEN 'GIR' ELSE 'GIP' END)
		FROM "BSCL.Tesoreria.BD::TES.Comprobante" COM
		INNER JOIN :v_EstadoComprobantes AS EST ON (COM."IdComprobante" = EST."IdComprobante");

		
		---------------------------------------------------
		-- GENERA NOMINA
		---------------------------------------------------
		IF (:p_GeneraNomina = 'S') THEN    
				-- Genera Secuencia para Nomina
				SELECT "BSCL.Tesoreria.BD.Secuencias::NominaTransferencia".NEXTVAL
				INTO v_IdNominaTransferencia
				FROM DUMMY;
				
				-- Crea el encabezado de la Nomina
				INSERT INTO "BSCL.Tesoreria.BD::TES.NominaTransferencia"
				(
					  "IdNominaTransferencia"
					 ,"IdEmpresa"
					 ,"IdComprobantePartidaEgreso"
					 ,"FechaMovimiento"
					 ,"FechaIngresoRegistro"
					 ,"FechaLiquidacion"
					 ,"FechaRecepcion"				-- Fecha en que el banco recibio con exito la nomina
					 ,"IdMedioDePago"
					 ,"IdModalidadDePago"
					 ,"IdCuentaBanco"
					 ,"IdTipoCuentaBanco"
					 ,"IdMoneda"
					 ,"Monto"
					 ,"NombreArchivo"
					 ,"IdFormatoArchivo"
					 ,"IdFormatoNomina"
					 ,"EsNomina"
					 ,"EsTransferencia"
					 ,"IdUsuario"
					 ,"IdEstado"
				     ,"UsaTalonarioCheque"
				)
				VALUES
				(
					  :v_IdNominaTransferencia
					 ,:p_IdEmpresa
					 ,:v_IdComprobantePartidaEgreso
					 ,:p_FechaMovimiento
					 ,CURRENT_UTCTIMESTAMP	 
					 ,p_out_FechaLiquidacion
					 ,'0001-01-01'
					 ,:p_IdMedioDePago
					 ,:p_IdModalidadDePago
					 ,:p_IdCuentaBancariaCorredora
					 ,:v_IdTipoCuentaBancariaCorredora
					 ,:p_out_IdMoneda
					 ,:p_Monto
					 -- Estos campos se deben borrar(deprecados)
					 ,''	   -- NombreArchivo
					 ,1		   -- IdFormatoArchivo
					 ,1		   -- IdFormatoNomina
					 ,'S'      -- GeneraNomina
					 ,'N'  	   -- GeneraTransferencia
					 ,:p_IdUsuario
					 ,'ING'
					 ,:p_UsaTalonarioCheque
				);
				
				-- GENERA DETALLE NOMINA
				INSERT INTO "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle"
				(
					  "IdNominaTransferenciaDetalle"
					 ,"IdNominaTransferencia"
					 ,"IdComprobanteFormaDePago"
					 ,"IdUsuario"
					 ,"IdEstado"
				)
				SELECT 
					 "BSCL.Tesoreria.BD.Secuencias::NominaTransferenciaDetalle".NEXTVAL
					 ,:v_IdNominaTransferencia
					 ,CFP."IdComprobanteFormaDePago"
					 ,:p_IdUsuario
					 ,'ING'
				FROM :p_ComprobantesAGirar AS GIR
				INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS COM ON (COM."IdComprobante" = GIR."IdComprobante")
				INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP ON (CFP."IdComprobante" = COM."IdComprobante")
				WHERE CFP."IdMedioDePago" = :p_IdMedioDePago
				AND   CFP."IdEstado"      = 'GIR';
				
		END IF;


		---------------------------------------------------
		-- GENERA TRANSFERENCIA
		---------------------------------------------------		
		IF (:p_GeneraTransferencia = 'S') THEN
				-- Genera Secuencia para Transferencia
				SELECT "BSCL.Tesoreria.BD.Secuencias::NominaTransferencia".NEXTVAL
				INTO v_IdNominaTransferencia
				FROM DUMMY;
				
				-- Crea el encabezado de la Transferencia
				INSERT INTO "BSCL.Tesoreria.BD::TES.NominaTransferencia"
				(
					  "IdNominaTransferencia"
					 ,"IdEmpresa"
					 ,"IdComprobantePartidaEgreso"
					 ,"FechaMovimiento"
					 ,"FechaIngresoRegistro"
					 ,"FechaLiquidacion"
					 ,"FechaRecepcion"				-- Fecha en que el banco recibio con exito la nomina
					 ,"IdMedioDePago"
					 ,"IdModalidadDePago"
					 ,"IdCuentaBanco"
					 ,"IdTipoCuentaBanco"
					 ,"IdMoneda"
					 ,"Monto"
					 ,"NombreArchivo"
					 ,"IdFormatoArchivo"
					 ,"IdFormatoNomina"
					 ,"EsNomina"
					 ,"EsTransferencia"
					 ,"IdUsuario"
					 ,"IdEstado"
				     ,"UsaTalonarioCheque"
				)
				VALUES
				(
					  :v_IdNominaTransferencia
					 ,:p_IdEmpresa
					 ,:v_IdComprobantePartidaEgreso
					 ,:p_FechaMovimiento
					 ,CURRENT_UTCTIMESTAMP	 
					 ,p_out_FechaLiquidacion
					 ,'0001-01-01'
					 ,:p_IdMedioDePago
					 ,:p_IdModalidadDePago
					 ,:p_IdCuentaBancariaCorredora
					 ,:v_IdTipoCuentaBancariaCorredora
					 ,:p_out_IdMoneda
					 ,:p_Monto
					 -- Estos campos se deben borrar(deprecados)
					 ,''	   -- NombreArchivo
					 ,1		   -- IdFormatoArchivo
					 ,1		   -- IdFormatoNomina
					 ,'N'      -- GeneraNomina
					 ,'S'      -- GeneraTransferencia
					 ,:p_IdUsuario
					 ,'ING'
					 ,:p_UsaTalonarioCheque
				);
				
				-- GENERA DETALLE TRANSFERENCIA
				INSERT INTO "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle"
				(
					  "IdNominaTransferenciaDetalle"
					 ,"IdNominaTransferencia"
					 ,"IdComprobanteFormaDePago"
					 ,"IdUsuario"
					 ,"IdEstado"
				)
				SELECT 
					 "BSCL.Tesoreria.BD.Secuencias::NominaTransferenciaDetalle".NEXTVAL
					 ,:v_IdNominaTransferencia
					 ,CFP."IdComprobanteFormaDePago"
					 ,:p_IdUsuario
					 ,'ING'
				FROM :p_ComprobantesAGirar AS GIR
				INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS COM ON (COM."IdComprobante" = GIR."IdComprobante")
				INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP ON (CFP."IdComprobante" = COM."IdComprobante")
				WHERE CFP."IdMedioDePago" = :p_IdMedioDePago
				AND   CFP."IdEstado"      = 'GIR';
		END IF;
		
				

		
		-------------------------------------------------------------------
		-- LIQUIDA LOS COMPROBANTES Y FORMAS DE PAGO
		-- NOTA: ahora los comprobantes se liquidan en blotter de Nomninas y Transferencias
		-------------------------------------------------------------------
		/********
		--IF (:p_GeneraNomina = 'S') THEN
			-- Liquida los comprobantes girados que generaron la PE(en realidad las formas de pago giradas)
			v_TablaComprobante = 
				SELECT 
				  	"IdComprobante", 
				  	'MOVEGR' AS "IdTipoMovimiento", 
				  	'CLI'    AS "AfectaCuentaInversion"  -- Afecta Cta. Inv. solo del cliente
				FROM :p_ComprobantesAGirar;
			
			v_TablaComprobanteFormaDePago = 
				SELECT 
					CFP."IdComprobante", 
					CFP."IdComprobanteFormaDePago",
					'CLI' AS "AfectaCuentaInversion"
				FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP
		        INNER JOIN :p_ComprobantesAGirar AS GIR ON (CFP."IdComprobante" = GIR."IdComprobante")
		        AND CFP."IdMedioDePago" = :p_IdMedioDePago     -- medio de pago que se está girando
		        AND CFP."IdEstado" <> 'ANU';
			
			CALL "BSCL.Tesoreria.BD.Servicios::LiquidarComprobantes" 
			(
			      p_IdEmpresa 					=> :p_IdEmpresa
			     ,p_IdUsuario 					=> :p_IdUsuario
			     ,p_IdModulo 					=> :p_IdModulo
			     ,p_UsaReglasNegocio 			=> 'S'
			     ,p_FechaMovimiento			    => p_FechaMovimiento
			     ,p_TablaComprobante 			=> :v_TablaComprobante
			     ,p_TablaComprobanteFormaDePago	=> :v_TablaComprobanteFormaDePago
			     ,p_out_ResultadoProceso		=> :v_ResultadoProceso
			     ,p_out_InformacionContador  	=> v_InformacionContador
			     ,p_out_AdvertenciasContador 	=> :v_AdvertenciasContador
			  	 ,p_out_ErroresContador      	=> :v_ErroresContador
			);
			
			p_out_ErroresDetalle		=  SELECT "Codigo", "Descripcion" FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_AdvertenciasDetalle" UNION ALL
										   SELECT "Codigo", "Descripcion" FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_ErroresDetalle" UNION ALL		
										   SELECT "Codigo", "Descripcion" FROM :p_out_ErroresDetalle;
			p_out_ResultadoProceso		=  SELECT "Campo","Id","Servicio","Resultado" FROM :v_ResultadoProceso UNION ALL
										   SELECT "Campo","Id","Servicio","Resultado" FROM :p_out_ResultadoProceso;
			
			
			-- Determina si hubieron errores en la liquidación de los comprobantes que generaron la PE
			SELECT Count("Codigo")
			INTO v_CountErroresDetalle
			FROM :p_out_ErroresDetalle;

			IF (v_CountErroresDetalle > 0) THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;
		--END IF;
		********/
		
  	END;    
END;

