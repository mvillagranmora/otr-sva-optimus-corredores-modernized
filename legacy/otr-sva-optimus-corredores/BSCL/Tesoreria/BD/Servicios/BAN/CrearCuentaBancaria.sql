SET SCHEMA "BSCL";   
GO 
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.BAN::CrearCuentaBancaria";
GO
-- 
--  Proposito: Crear cuentas bancarias para tesorerÃ­a
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 02/02/2016  
-- 
CREATE PROCEDURE "BSCL.Tesoreria.BD.Servicios.BAN::CrearCuentaBancaria"
  (
   -- Parametros de Validacion                                                                                            
   IN  p_IdEmpresa                     			BIGINT,                                                                            
   IN  p_IdUsuario                     			BIGINT,                                                                            
   IN  p_IdModulo                      			NVARCHAR(3),                                                                       
   IN  p_simularOperacion              			NVARCHAR(1),	-- S/N se usa para simular operacion (algunos casos)                  
   IN  p_UsaReglasNegocio              			NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.                          
   IN  p_FechaSistema              			    NVARCHAR(10),
   
	-- Parametros entrada
   IN  p_TablaCuentasBancarias             		TABLE
   (
       "IdCuentaBancaria"  						BIGINT,         -- Id Temporal(1,2..)
	   "IdBanco"  								BIGINT,
	   "NombreCuentaBancaria"  					NVARCHAR(20),
	   "IdTipoCuentaBancaria"  					NVARCHAR(4),
	   "IdMoneda"  								NVARCHAR(3),
	   "NumeroCuentaBancaria"   				NVARCHAR(100),
	   "Sucursal"								NVARCHAR(100),
	   "Direccion"								NVARCHAR(100),
  	   "CodigoCuentaInterbancario"			    NVARCHAR(40),  -- Solo para PERU (CCI)  
	   "CodigoAba"								NVARCHAR(50),  -- Codigo internacional para banco   	   
	   "OtroCodigoBancoExtranjero"				NVARCHAR(50),  -- Otro Codigo internacional para banco
	   "CodigoIban"								NVARCHAR(50),  -- Codigo internacional para cuenta bancaria
	   "OtroCodigoCuentaExtranjero"				NVARCHAR(50),  -- Otro Codigo internacional para cuenta bancaria
	   "IdPlaza"                                BIGINT, 
	   "Contacto"								NVARCHAR(100),
	   "TelefonoContacto"			 			NVARCHAR(100),
	   "EmailContacto"				 			NVARCHAR(100),
	   "SaldoInicial"				            DECIMAL(26,8),
	   "AfectaPosicionInicial"                  NVARCHAR(1),     -- S:Si   N:Especial
	   "GeneraNomina"                           NVARCHAR(1),
	   "GeneraTransferencia"                    NVARCHAR(1), 
	   "IdEmpleadoFirma1"   					BIGINT,   -- para carta de transferencias
	   "MontoSegundaFirma" 						DECIMAL(26,8),
	   "IdEmpleadoFirma2"   					BIGINT,
	   "IdEstado"								NVARCHAR(3),
	   "RecaudacionExclusivaPAC"				NVARCHAR(1)
   ),
   
   -- Salidas Tesoreria
   OUT p_out_ResultadoProceso			TABLE("Campo" VARCHAR(40),"IdCuentaBancaria" BIGINT, "Servicio" VARCHAR(250), "Resultado" VARCHAR(40)),
   OUT p_out_AdvertenciasContador       INT,
   OUT p_out_AdvertenciasDetalle        TABLE ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
   OUT p_out_ErroresContador            INT,   
   OUT p_out_ErroresDetalle             TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
) 
   
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
	
BEGIN  
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;
	DECLARE SALIDA_CONTROLADA 			CONDITION; 
	DECLARE v_errorcuenta 				INT DEFAULT 0;  
	DECLARE v_errorhashmsg 				table ("Codigo" nvarchar(100),"Valor" nvarchar(5000));
	DECLARE v_erroroutmsg  				nvarchar(520);	        	
	DECLARE v_ErrorNoData  				NVARCHAR(100);
	DECLARE v_CountMovimientoTesoreria  INT := 0;
	DECLARE v_out_InformacionContador   INT := 0;
	DECLARE v_out_AdvertenciasContador  INT := 0;
	DECLARE v_out_ErroresContador       INT := 0;
		
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
	END;	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN
		v_hash= SELECT '' as "Codigo",'' as "Valor" FROM DUMMY;	
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_ErrorNoData,:v_hash,:v_erroroutmsg);
		p_out_ErroresDetalle =  SELECT :v_ErrorNoData  as "Codigo",:v_erroroutmsg as "Descripcion" FROM dummy UNION ALL
								SELECT * FROM :p_out_ErroresDetalle;
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
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
	
	BEGIN --LOGICA

	    --------------------
		-- Declare Variables
		--------------------
		DECLARE v_Obligatorios					NVARCHAR(250);
		DECLARE v_resultadoRegla 				NVARCHAR(40);
		
		-- Para el Diccionario
        DECLARE p_TablaCodigosMensajes			TABLE ("CodigoMensaje" NVARCHAR(40),"CodigoDato" NVARCHAR(100),"ValorDato" NVARCHAR(520));
    	DECLARE p_out_TablaMensajes				TABLE ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520));    	
    	    	
	    DECLARE v_AdvertenciasContador 			INT;
	    DECLARE v_ErroresContador      			INT;
	    DECLARE v_AdvertenciasDetalle  			TABLE ("Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000));	  	
	    DECLARE v_ErroresDetalle       			TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000));    	
    	DECLARE v_RegistrosRech	                TABLE ("IdCuentaBancaria"      BIGINT);
    	DECLARE v_countAliasBancoMetadata       INT;
    	
    	DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;
    	
    	p_TablaCodigosMensajes  = SELECT '' as "CodigoMensaje", '' as "CodigoDato", '' as "ValorDato" from dummy where 1=2;
		p_out_TablaMensajes     = SELECT '' as "Codigo", '' as "Descripcion" from dummy where 1=2;
		p_out_ResultadoProceso	= SELECT '' as "Campo", 0 as "IdCuentaBancaria", '' as "Servicio", '' as "Resultado" FROM dummy where 1=2;
    	v_RegistrosRech	        = SELECT 0 as "IdCuentaBancaria" from dummy where 1=2;
		
		--------------------------------------------
		-- VALIDACIONES
		-------------------------------------------- 
		IF (p_UsaReglasNegocio = 'S') THEN
			BEGIN
				-- Validación básica de Empresa, Usuario, Módulo
				CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_Errores,:v_resultadoRegla);
				IF :v_resultadoRegla = 'RECH' THEN
					SIGNAL SALIDA_CONTROLADA;
				END IF;   
	
	                 
				CALL "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosEditarCuentaBancaria"('C', :p_TablaCuentasBancarias, :v_RegistrosRech, :v_ErroresDetalle, :v_resultadoRegla);
				
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
		
		
		-- Solo fue una simulacion
		If p_simularOperacion = 'S' then
			SIGNAL SALIDA_CONTROLADA;
		end if;



		-- Quita los registros que no pasaron las validaciones
	 	v_TablaCuentasBancarias =	 	
	 	SELECT 
	 	   "BSCL.Tesoreria.BD.Secuencias::CuentaBanco".NEXTVAL As "SecuenciaCuentaBanco",
	 		CB.* 
	 	FROM :p_TablaCuentasBancarias AS CB
	 	WHERE CB."IdCuentaBancaria" NOT IN (SELECT "IdCuentaBancaria" FROM :v_RegistrosRech);
	 	 
	 	INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoValor"("IdTipoDatoEvento","ValorVarchar","ValorDecimal","Indice","Estado","Descripcion")
	 	SELECT 'AliasBancoCaja',
	 	       "NombreCuentaBancaria",
	 	       0,
	 	       1,
	 	       'V',
	 	       "NombreCuentaBancaria"
	 	FROM :v_TablaCuentasBancarias
	 	WHERE "NombreCuentaBancaria" NOT IN (SELECT "ValorVarchar" FROM "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoValor" WHERE "IdTipoDatoEvento" = 'AliasBancoCaja');
	 	 
		------------------------------------------------------
		-- INSERTAR DATOS DE LA CUENTA BANCARIA TESORERIA
		------------------------------------------------------
		INSERT INTO "BSCL.Tesoreria.BD::TES.CuentaBanco" 
		(
			 "IdCuentaBanco",
			 "IdEmpresa",
			 "IdBanco",
			 "NombreCuentaBancaria",
			 "IdTipoCuentaBancaria",
			 "IdMoneda",
			 "NumeroCuentaBancaria",
			 "Sucursal",
			 "Direccion",
			 "CodigoCuentaInterbancario",
		     "CodigoAba",   	   
		     "OtroCodigoBancoExtranjero",
		     "CodigoIban",
		     "OtroCodigoCuentaExtranjero",
			 "IdPlaza",
			 "Contacto",
			 "TelefonoContacto",
			 "EmailContacto",
			 "IdEstado",
			 "SaldoInicial",
			 "AfectaPosicionInicial",			 
   		   	 "GeneraNomina",
   		   	 "GeneraTransferencia",   		   	    		   	 
			 "IdEmpleadoFirma1",
			 "MontoSegundaFirma",
			 "IdEmpleadoFirma2",
			 "APV"
		)
		SELECT
		    CB."SecuenciaCuentaBanco"
		   ,:p_IdEmpresa		   
		   ,CB."IdBanco"
		   ,CB."NombreCuentaBancaria"
		   ,CB."IdTipoCuentaBancaria"
		   ,CB."IdMoneda"		   
		   ,CB."NumeroCuentaBancaria"		   		   
		   ,COALESCE(CB."Sucursal",'')
		   ,COALESCE(CB."Direccion",'')
		   ,CB."CodigoCuentaInterbancario"
		   ,CB."CodigoAba"
		   ,CB."OtroCodigoBancoExtranjero"
		   ,CB."CodigoIban"
		   ,CB."OtroCodigoCuentaExtranjero"
		   ,COALESCE(CB."IdPlaza",0)
		   ,COALESCE(CB."Contacto",'')
		   ,COALESCE(CB."TelefonoContacto",'')
		   ,COALESCE(CB."EmailContacto",'')
		   ,CB."IdEstado"
		   ,COALESCE(CB."SaldoInicial",0)
   		   ,COALESCE(CB."AfectaPosicionInicial",'N')
   		   ,COALESCE(CB."GeneraNomina",'N')
   		   ,COALESCE(CB."GeneraTransferencia",'N')   		   
   		   ,COALESCE(CB."IdEmpleadoFirma1",0)
   		   ,COALESCE(CB."MontoSegundaFirma",0)
   		   ,COALESCE(CB."IdEmpleadoFirma2",0)
   		   ,CB."RecaudacionExclusivaPAC"
		FROM :v_TablaCuentasBancarias AS CB;
		
		
		
		------------------------------------------------------
		-- INFORMACION ADICIONAL
		------------------------------------------------------
		v_InformacionCorredora = 
		SELECT CSN."IdPersona",CC."NombreCompleto",CC."Tipo",CC."DocumentoIdentidad"
		FROM "BSCL.Plataforma.BD::PLA.Empresa" AS E
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS CSN ON CSN."IdSocioDeNegocio" = E."IdSocioDeNegocio"
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CC ON CC."IdCliente" = CSN."IdCliente"
	 	WHERE E."IdEmpresa" = :p_IdEmpresa;		
		
		

		-------------------------------------------------------------
		-- CREAR COMPROBANTE TESORERIA
		-- Si esiste Saldo Inicial, se crea comprobante y entra en el saldo banco
		-------------------------------------------------------------
		INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_IngresoEgresoGenerar"
		SELECT
			CB."SecuenciaCuentaBanco" As "NumeroReferencia",	  -- Id solo referencial
			'MOVING' As "IdTipoMovimiento",			
			'CNT'    As "IdTipoPersona",			
			IC."IdPersona",
			0 As "IdPortafolio",
			CB."IdMoneda",
			CB."SaldoInicial" As "Monto",
			:p_FechaSistema As "FechaLiquidacion",
			'SAINCT' As "IdConcepto",  -- SALDO INICIAL CUENTA CORRIENTE
			'CTN' As "IdTipoPersonaTitularPago",
			IC."IdPersona"    As "IdPersonaTitularPago",
			0 As "IdPortafolioTitularPago",
			0   As "IdCaja",
			''  As "SecuenciaAgrupacion",
			0   As "SecuenciaRelacionada",
			'SALDO INICIAL POR APERTURA CUENTA CORRIENTE' As "Glosa",
			0   As "IdEmpleadoDigitador",
			0   As "IdSucursalEmpleadoDigitador",
			''  As "IdTipoProducto",
			''  As "IdTipoOperacion",					           
			''  As "IdTipoComprobante",
			0   As "IdComprobante",
			'0001-01-01' As "FechaComprobante",
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
            'S' As "GeneraComprobante",
			'S' As "AgruparEnComprobante",
			'S' As "MueveCuentaInversion",
			'S' As "TitularPago"
		FROM  :v_TablaCuentasBancarias AS CB
		INNER JOIN :v_InformacionCorredora AS IC ON (1=1)
		WHERE CB."SaldoInicial" > 0;


	    INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_FormaDePago"
	    SELECT
            CB."SecuenciaCuentaBanco" As "NumeroReferencia",	  -- Id solo referencial
			COALESCE(PPG."IdMedioDePago",'TRA') AS "IdMedioDePago",            -- prefencias de pago del corredor
			COALESCE(PPG."IdModalidadDePago",'MODDEP') AS "IdModalidadDePago", -- prefencias de pago del corredor
			CB."IdMoneda",
			CB."SaldoInicial" As "Monto",
			CB."SecuenciaCuentaBanco" As "IdCuentaBancariaCorredora",
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
		FROM  :v_TablaCuentasBancarias AS CB
		LEFT JOIN  "BSCL.Tesoreria.BD.Vistas::ConsultarPreferenciaPagoCorredor" AS PPG ON -- Preferencias de pago para "SALDO INICIAL CUENTA CORRIENTE"
			(
				    PPG."IdTipoMovimiento"  = 'MOVING'
				AND PPG."IdConcepto"        = 'SAINCT' 
				AND PPG."IdEstado" 			= 'ACT'					
				AND PPG."IdEmpresa" 		= :p_IdEmpresa
			)		
		WHERE CB."SaldoInicial" > 0;


		
		SELECT Count(1)
		INTO v_CountMovimientoTesoreria
		FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_IngresoEgresoGenerar";
		
		IF (v_CountMovimientoTesoreria > 0) THEN
				CALL "BSCL.Tesoreria.BD.Servicios::CrearIngresoEgreso"(
					 p_IdEmpresa	  				=> :p_IdEmpresa
				   	,p_IdUsuario					=> :p_IdUsuario
				   	,p_IdModulo						=> :p_IdModulo
				   	,p_simularOperacion				=> 'N'
				   	,p_UsaReglasNegocio				=> 'S'
					,p_EsIngresoAutomatico   		=> 'S'
				    ,p_out_InformacionContador  	=>  v_out_InformacionContador
				   	,p_out_AdvertenciasContador		=>  v_out_AdvertenciasContador
				   	,p_out_ErroresContador			=>  v_out_ErroresContador
				);
		
				p_out_ErroresContador := p_out_ErroresContador + v_out_ErroresContador + v_out_AdvertenciasContador;
				    
				p_out_ErroresDetalle = SELECT "Codigo", "Descripcion" FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_ErroresDetalle"      UNION ALL
				                       SELECT "Codigo", "Descripcion" FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_AdvertenciasDetalle" UNION ALL
			                           SELECT "Codigo", "Descripcion" FROM :p_out_ErroresDetalle;
		
				IF v_out_ErroresContador > 0 THEN
				       SIGNAL SALIDA_CONTROLADA;
			    END IF;
	    		
		END IF;  -- IF (v_CountMovimientoTesoreria > 0)



		
		
		------------------------------------------------------
		-- RESULTADO DEL PROCESO
		------------------------------------------------------
		p_out_ResultadoProceso =
		SELECT  'IdCuentaBancaria' as "Campo",
				"IdCuentaBancaria",
				'BSCL.Tesoreria.BD.Servicios::CrearCuentaBancaria' as "Servicio",
				'ING' as "Resultado"
		FROM :v_TablaCuentasBancarias
		UNION ALL		
		SELECT  'IdCuentaBancaria' as "Campo",
				"IdCuentaBancaria",				
				'BSCL.Tesoreria.BD.Servicios::CrearCuentaBancaria' as "Servicio",
				'RECH' as "Resultado"
		FROM :v_RegistrosRech
		UNION ALL				
		SELECT "Campo","IdCuentaBancaria","Servicio","Resultado" FROM :p_out_ResultadoProceso;
				
    END; 
END;




