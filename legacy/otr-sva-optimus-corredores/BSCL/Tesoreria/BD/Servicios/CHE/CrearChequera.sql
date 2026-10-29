SET SCHEMA "BSCL";   

DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.CHE::CrearChequera";

-- 
--  Proposito: Crear cuentas bancarias para tesorería
--  Autor: CESPINOSA - BCS
--  Fecha Creacion: 02/02/2016  
-- 
CREATE PROCEDURE "BSCL.Tesoreria.BD.Servicios.CHE::CrearChequera"
  (
   -- Parametros de Validacion                                                                                            
   IN  p_IdEmpresa                     			BIGINT,                                                                            
   IN  p_IdUsuario                     			BIGINT,                                                                            
   IN  p_IdModulo                      			NVARCHAR(3),                                                                       
   IN  p_simularOperacion              			NVARCHAR(1),	-- S/N se usa para simular operacion (algunos casos)                  
   IN  p_UsaReglasNegocio              			NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.                          

   -- Parametros para logica
   IN  p_IdCuentaBanco							BIGINT,
   IN  p_FechaRetiro		  					NVARCHAR(10),
   IN  p_Serie				  					NVARCHAR(40),
   IN  p_CantidadCheques						BIGINT,
   IN  p_Remanente								BIGINT,
   IN  p_NumeroChequeDesde						BIGINT,
   IN  p_NumeroChequeHasta						BIGINT,
   IN  p_NumeroChequeActual						BIGINT,
   IN  p_IdEstado								NVARCHAR(3),
   IN  p_IdConfiguracionChequera				BIGINT,
   
   
   -- Parametros de Salida
   OUT p_out_REF_ID                    BIGINT,  
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
	
	DECLARE v_erroroutmsg  				nvarchar(520);	        	
	DECLARE v_ErrorNoData  				NVARCHAR(100);
	
	DECLARE v_CuentaBancariasRechazadas	TABLE ("IdCuentaBanco" BIGINT);
	DECLARE v_ErroresDetalle			TABLE ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520));	
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador from :p_out_AdvertenciasDetalle;		 					 	
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
	
	p_out_REF_ID				:= 0;
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	v_CajasRechazadas				= SELECT 0  as "IdCuentaBanco" FROM dummy where 1=2;
	v_ErroresDetalle				= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;
	
	BEGIN --LOGICA

	    --------------------
		-- Declare Variables
		--------------------
		DECLARE v_resultadoRegla 				NVARCHAR(40);
		
		-- Para el Diccionario
        DECLARE p_TablaCodigosMensajes			TABLE ("CodigoMensaje" NVARCHAR(40),"CodigoDato" NVARCHAR(100),"ValorDato" NVARCHAR(520));
    	DECLARE p_out_TablaMensajes				TABLE ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520));    	
    	    	
	    DECLARE v_AdvertenciasContador 			INT;
	    DECLARE v_ErroresContador      			INT;
	    DECLARE v_AdvertenciasDetalle  			TABLE ("Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000));	  	
	    DECLARE v_ErroresDetalle       			TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000));
    	
    	
    	DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;
    	
    	p_TablaCodigosMensajes = Select '' as "CodigoMensaje", '' as "CodigoDato", '' as "ValorDato" from dummy where 1=2;
		p_out_TablaMensajes = Select '' as "Codigo", '' as "Descripcion" from dummy where 1=2;		
			 	
		--------------------------------------------
		-- VALIDACIONES BASICAS
		-------------------------------------------- 

		BEGIN
			-- Validación básica de Empresa, Usuario, Módulo
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_Errores,v_resultadoRegla);
			IF :v_resultadoRegla = 'RECH' THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;   
			
		END;

		--------------------------------------------------------
		--- VALIDACIONES DE NEGOCIO
		--------------------------------------------------------
		BEGIN
			-- Validación de Banco
			v_resultadoRegla:='S';
 		    /*CALL "BSCL.Tesoreria.BD.Reglas::CuentaBancariaExiste"(:p_IdCuentaBanco, 0,:v_resultadoRegla,:v_erroroutmsg); 
			IF :v_resultadoRegla <> 'S' THEN				  				  
			   p_TablaCodigosMensajes = Select 'CUENTA_BANCO_NO_EXISTE' AS "CodigoMensaje", 'IdCuentaBanco' AS "CodigoDato", :p_IdCuentaBanco AS "ValorDato" 
				                        From DUMMY Union All				                         
				                        Select "CodigoMensaje", "CodigoDato", "ValorDato"
				                        From :p_TablaCodigosMensajes;

			END IF;*/
			
			IF :p_NumeroChequeDesde > :p_NumeroChequeHasta THEN
			   p_TablaCodigosMensajes = Select 'NUMEROS_CHEQUES_ERROR' AS "CodigoMensaje", 'NumeroChequeDesde' AS "CodigoDato", :p_NumeroChequeDesde AS "ValorDato" 
				                        From DUMMY Union All				                         
				                        Select "CodigoMensaje", "CodigoDato", "ValorDato"
				                        From :p_TablaCodigosMensajes;
			END IF;
			
			IF (:p_NumeroChequeHasta - :p_NumeroChequeDesde +1) <> :p_CantidadCheques THEN
			   p_TablaCodigosMensajes = Select 'CANTIDAD_CHEQUES_ERROR' AS "CodigoMensaje", 'CantidadCheques' AS "CodigoDato", :p_CantidadCheques AS "ValorDato" 
				                        From DUMMY Union All				                         
				                        Select "CodigoMensaje", "CodigoDato", "ValorDato"
				                        From :p_TablaCodigosMensajes;
			END IF;
		END;

		--------------------------------------------   
		-- DICCIONARIO
		--------------------------------------------
        CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:p_TablaCodigosMensajes, :p_out_TablaMensajes);

		p_out_ErroresDetalle =  Select "Codigo", "Descripcion" 
		                        From :p_out_TablaMensajes Union All
								Select "Codigo", "Descripcion" 
								From :p_out_ErroresDetalle;		


		--Si existen errores en las validaciones debe salir
		select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle; 
		IF :p_out_ErroresContador > 0 THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;					

		
		--Solo fue una simulacion del servicio
		If p_simularOperacion = 'S' then
			SIGNAL SALIDA_CONTROLADA;
		end if;

   		 
   		------------------------------------------------------
		-- OBTENER NÚMERO DE SECUENCIA
		------------------------------------------------------
        SELECT "BSCL.Tesoreria.BD.Secuencias::Chequera".NEXTVAL INTO p_out_REF_ID FROM DUMMY;


		------------------------------------------------------
		-- INSERTAR DATOS DE LA CHEQUERA TESORERIA
		------------------------------------------------------

		INSERT INTO "BSCL.Tesoreria.BD::TES.Chequera" 
		(
			 "IdChequera",
			 "IdCuentaBanco",
			 "FechaRetiro",
			 "Serie",
			 "CantidadCheques",
			 "Remanente",
			 "NumeroChequeDesde",
			 "NumeroChequeHasta",
			 "NumeroChequeActual",
			 "IdEstado",
			 "IdConfiguracionChequera"
		)
		VALUES (
		   :p_out_REF_ID,
		   :p_IdCuentaBanco,   
		    TO_DATE(:p_FechaRetiro, 'YYYY-MM-DD'),
		   :p_Serie,
		   :p_CantidadCheques,
		   :p_Remanente,
		   :p_NumeroChequeDesde,
		   :p_NumeroChequeHasta,
		   :p_NumeroChequeActual,
		   :p_IdEstado,
		   :p_IdConfiguracionChequera
		);

    End; 
END;




