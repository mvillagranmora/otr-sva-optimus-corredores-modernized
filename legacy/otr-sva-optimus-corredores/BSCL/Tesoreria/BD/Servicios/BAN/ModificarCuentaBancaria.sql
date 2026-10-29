SET SCHEMA "BSCL";
GO 
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.BAN::ModificarCuentaBancaria";
GO
-- 
--  Proposito: Modifica los datos de una cuenta bancaria de la corredora(tesorería).
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 03/02/2016  
-- 
CREATE PROCEDURE "BSCL.Tesoreria.BD.Servicios.BAN::ModificarCuentaBancaria"
  (
   -- Parametros de Validacion                                                                                            
   IN  p_IdEmpresa                     			BIGINT,                                                                            
   IN  p_IdUsuario                     			BIGINT,                                                                            
   IN  p_IdModulo                      			NVARCHAR(3),                                                                       
   IN  p_simularOperacion              			NVARCHAR(1),	-- S/N se usa para simular operacion (algunos casos)                  
   IN  p_UsaReglasNegocio              			NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.                          

   IN  p_TablaCuentasBancarias             		TABLE
   (
       "IdCuentaBancaria"  						BIGINT,
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
	   "AfectaPosicionInicial"                  NVARCHAR(1),      -- S:Si   N:Especial
	   "GeneraNomina"                           NVARCHAR(1),  
	   "GeneraTransferencia"                    NVARCHAR(1),  
	   "IdEmpleadoFirma1"   					BIGINT,   -- para carta de transferencias
	   "MontoSegundaFirma" 						DECIMAL(26,8),
	   "IdEmpleadoFirma2"   					BIGINT,
	   "IdEstado"								NVARCHAR(3),
	   "RecaudacionExclusivaPAC"                NVARCHAR(1)
   ),
   
   --IN  p_CamposModificados		       TABLE    ("Campo" NVARCHAR(40),    "ValorNuevo" NVARCHAR(520),    "ValorAntiguo" NVARCHAR(520)),
   
   -- Salidas Tesoreria
   OUT p_out_ResultadoProceso			TABLE("Campo" VARCHAR(40),"IdCuentaBancaria" BIGINT, "Servicio" VARCHAR(250), "Resultado" VARCHAR(40)),
   
   -- Parametros de Salida
   OUT p_out_AdvertenciasContador      INT,
   OUT p_out_AdvertenciasDetalle       TABLE ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
   OUT p_out_ErroresContador           INT,   
   OUT p_out_ErroresDetalle            TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
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
		DECLARE v_FechaSistema                  NVARCHAR(10);
		
		-- Para el Diccionario
        DECLARE p_TablaCodigosMensajes			TABLE ("CodigoMensaje" NVARCHAR(40),"CodigoDato" NVARCHAR(100),"ValorDato" NVARCHAR(520));
    	DECLARE p_out_TablaMensajes				TABLE ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520));    	
    	    	
	    DECLARE v_AdvertenciasContador 			INT;
	    DECLARE v_ErroresContador      			INT;
	    DECLARE v_AdvertenciasDetalle  			TABLE ("Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000));	  	
	    DECLARE v_ErroresDetalle       			TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000));
    	DECLARE v_RegistrosRech	                TABLE ("IdCuentaBancaria"      BIGINT);
   		DECLARE v_TablaCuentasBancarias         TABLE ("IdCuentaBancaria"      BIGINT);		
    			
    	DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;
    	
    	p_TablaCodigosMensajes 	= SELECT '' as "CodigoMensaje", '' as "CodigoDato", '' as "ValorDato" from dummy where 1=2;
		p_out_TablaMensajes 	= SELECT '' as "Codigo", '' as "Descripcion" from dummy where 1=2;				
		p_out_ResultadoProceso	= SELECT '' as "Campo", 0 as "IdCuentaBancaria", '' as "Servicio", '' as "Resultado" FROM dummy where 1=2;		
		v_RegistrosRech	        = SELECT 0 as "IdCuentaBancaria" from dummy where 1=2;	 			
		v_TablaCuentasBancarias	= SELECT 0 as "IdCuentaBancaria" from dummy where 1=2;	 	
 
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
	
	                 
				CALL "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosEditarCuentaBancaria"('M', :p_TablaCuentasBancarias, :v_RegistrosRech, :v_ErroresDetalle, :v_resultadoRegla);
				
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
		
		
		-- Solo fue una simulacion del servicio.
		If p_simularOperacion = 'S' then
			SIGNAL SALIDA_CONTROLADA;
		end if;
	   	
	   	
		-- Quita los registros que no pasaron las validaciones
	 	v_TablaCuentasBancarias =	 	
	 	SELECT "IdCuentaBancaria" FROM :p_TablaCuentasBancarias
	 	EXCEPT	 	
	 	SELECT "IdCuentaBancaria" FROM :v_RegistrosRech;
	 	
	 	UPDATE ETDV
		SET    "ValorVarchar" = TCB2."NombreCuentaBancaria", "Descripcion" = TCB2."NombreCuentaBancaria"
		FROM   "BSCL.Tesoreria.BD::TES.CuentaBanco" AS CB
		INNER JOIN :v_TablaCuentasBancarias AS TCB1 ON (CB."IdCuentaBanco" = TCB1."IdCuentaBancaria")		
		INNER JOIN :p_TablaCuentasBancarias AS TCB2 ON (TCB1."IdCuentaBancaria" = TCB2."IdCuentaBancaria")
		INNER JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoValor" ETDV ON (ETDV."ValorVarchar" = CB."NombreCuentaBancaria" AND ETDV."IdTipoDatoEvento" = 'AliasBancoCaja');
	 		    	
		--------------------------------------------------------------
		-- MODIFICA DATOS DE LA CUENTA BANCARIA DE LA CORREDORA
		--------------------------------------------------------------
		UPDATE CB
		SET
			 "IdBanco" 				 		= COALESCE(TCB2."IdBanco",0)
			 ,"NombreCuentaBancaria"  		= COALESCE(TCB2."NombreCuentaBancaria",'')
			 ,"IdTipoCuentaBancaria"  		= COALESCE(TCB2."IdTipoCuentaBancaria",'')
			 ,"IdMoneda" 			 		= COALESCE(TCB2."IdMoneda",'')
			 ,"NumeroCuentaBancaria"  		= COALESCE(TCB2."NumeroCuentaBancaria",'')
			 ,"Sucursal"					= COALESCE(TCB2."Sucursal",'')
			 ,"Direccion"			 		= COALESCE(TCB2."Direccion",'')
			 ,"CodigoCuentaInterbancario"	= TCB2."CodigoCuentaInterbancario"
			 ,"CodigoAba"					= TCB2."CodigoAba"
			 ,"OtroCodigoBancoExtranjero"	= TCB2."OtroCodigoBancoExtranjero"
			 ,"CodigoIban"					= TCB2."CodigoIban"
			 ,"OtroCodigoCuentaExtranjero"	= TCB2."OtroCodigoCuentaExtranjero"
			 ,"IdPlaza"                 = COALESCE(TCB2."IdPlaza",0)
			 ,"Contacto"				= COALESCE(TCB2."Contacto",'')
			 ,"TelefonoContacto"		= COALESCE(TCB2."TelefonoContacto",'')
			 ,"EmailContacto"		 	= COALESCE(TCB2."EmailContacto",'')
			 ,"IdEstado"              	= COALESCE(TCB2."IdEstado",'')
			 ,"SaldoInicial"          	= COALESCE(TCB2."SaldoInicial",0)
		     ,"AfectaPosicionInicial" 	= COALESCE(TCB2."AfectaPosicionInicial",'N')
		   	 ,"GeneraNomina"         	= COALESCE(TCB2."GeneraNomina",'N')
		   	 ,"GeneraTransferencia"   	= COALESCE(TCB2."GeneraTransferencia",'N')		   		   	 
	 	     ,"IdEmpleadoFirma1"  		= COALESCE(TCB2."IdEmpleadoFirma1",0)
	   		 ,"MontoSegundaFirma" 		= COALESCE(TCB2."MontoSegundaFirma",0)
	   		 ,"IdEmpleadoFirma2"  		= COALESCE(TCB2."IdEmpleadoFirma2",0)
	   		 ,"APV"                     = TCB2."RecaudacionExclusivaPAC"
		   	 
		FROM  "BSCL.Tesoreria.BD::TES.CuentaBanco" AS CB
		INNER JOIN :v_TablaCuentasBancarias AS TCB1 ON (CB."IdCuentaBanco" = TCB1."IdCuentaBancaria")		
		INNER JOIN :p_TablaCuentasBancarias AS TCB2 ON (TCB1."IdCuentaBancaria" = TCB2."IdCuentaBancaria")
		WHERE CB."IdEmpresa"  = :p_IdEmpresa;
		
		------------------------------------------------------
		-- REGISTRO DE LOG 
		------------------------------------------------------				
		/**********
		-- Obtiene Fecha de Sistema
	   	Call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema"(:p_IdEmpresa, v_FechaSistema);

		v_DetalleLog = Select 'Modificación Cuenta Bancaria' AS "SubRegistro", 		
							  "Campo" AS "CampoRegistro" , 
							  "ValorAntiguo" AS "ValorAntiguo", 
							  "ValorNuevo" AS "ValorNuevo" 
					   From :p_CamposModificados;


		CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
					 p_IdTipoLog 		 => 'MODIFICACION' 
					,p_IdModulo 		 => 'TES' 
					,p_IdUsuario 		 => :p_IdUsuario 
					,p_IdEmpresa 		 => :p_IdEmpresa
					,p_FechaSistemaLog 	 => :v_FechaSistema
					,p_IdUsuarioAtendido => :p_IdUsuario
					,p_IdTipoRegistro 	 => 'CUENTASBANCARIA'
					,p_IdProceso 		 => p_IdCuentaBanco
					,p_NombreProceso 	 => 'Modificación de Cuenta Bancaria'
					,p_DetalleLog 		 => :v_DetalleLog
		);	
		*********/
			
			
		------------------------------------------------------
		-- RESULTADO DEL PROCESO
		------------------------------------------------------
		p_out_ResultadoProceso =
		SELECT  'IdCuentaBancaria' as "Campo",
				"IdCuentaBancaria",
				'BSCL.Tesoreria.BD.Servicios::CrearCuentaBancaria' as "Servicio",
				'EDI' as "Resultado"
		FROM :v_TablaCuentasBancarias
		UNION ALL		
		SELECT  'IdCuentaBancaria' as "Campo",
				"IdCuentaBancaria",				
				'BSCL.Tesoreria.BD.Servicios::CrearCuentaBancaria' as "Servicio",
				'RECH' as "Resultado"
		FROM :v_RegistrosRech
		UNION ALL				
		SELECT "Campo","IdCuentaBancaria","Servicio","Resultado" FROM :p_out_ResultadoProceso;				
		
    End; 
END;




