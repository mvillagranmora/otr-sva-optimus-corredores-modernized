SET SCHEMA "BSCL";   

DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.CHE::CrearConfiguracionChequera";

-- 
--  Proposito: Crear configuracion para impresion de cheques
--  Autor: CESPINOSA - BCS
--  Fecha Creacion: 17/02/2016  
-- 
CREATE PROCEDURE "BSCL.Tesoreria.BD.Servicios.CHE::CrearConfiguracionChequera"
  (
   -- Parametros de Validacion                                                                                            
   IN  p_IdEmpresa                     			BIGINT,                                                                            
   IN  p_IdUsuario                     			BIGINT,                                                                            
   IN  p_IdModulo                      			NVARCHAR(3),                                                                       
   IN  p_simularOperacion              			NVARCHAR(1),	-- S/N se usa para simular operacion (algunos casos)                  
   IN  p_UsaReglasNegocio              			NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.                          

   -- Parámetros para lógica

   IN p_NombreConfChequera 						NVARCHAR(100),
   IN p_AnchoCheque								DECIMAL(10,4),
   IN p_AltoCheque								DECIMAL(10,4),
   IN p_SigloImpreso            				NVARCHAR(1),  -- S / N 

   IN p_MontoFila								DECIMAL(10,4),
   IN p_MontoColumna							DECIMAL(10,4),
   IN p_ProvinciaFila							DECIMAL(10,4),
   IN p_ProvinciaColumna						DECIMAL(10,4),
   IN p_MesFila									DECIMAL(10,4),
   IN p_MesColumna								DECIMAL(10,4),
   IN p_AnioFila								DECIMAL(10,4),
   IN p_AnioColumna								DECIMAL(10,4),
   IN p_ExtendidoAFila							DECIMAL(10,4),
   IN p_ExtendidoAColumna						DECIMAL(10,4),
   IN p_PrimeraCantidadFila						DECIMAL(10,4),
   IN p_PrimeraCantidadColumna					DECIMAL(10,4),
   IN p_SegundaCantidadFila						DECIMAL(10,4),
   IN p_SegundaCantidadColumna					DECIMAL(10,4),

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
	
	p_out_REF_ID				:= 0;
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
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_Errores,:v_resultadoRegla);
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

		
		--Solo fue una simulacion del servicio... move bitch,get out the way
		If p_simularOperacion = 'S' then
			SIGNAL SALIDA_CONTROLADA;
		end if;

   		 
   		------------------------------------------------------
		-- OBTENER NÚMERO DE SECUENCIA
		------------------------------------------------------
        SELECT "BSCL.Tesoreria.BD.Secuencias::ConfiguracionChequera".NEXTVAL INTO p_out_REF_ID FROM DUMMY;


		------------------------------------------------------
		-- INSERTAR DATOS DE LA CHEQUERA TESORERIA
		------------------------------------------------------

		INSERT INTO "BSCL.Tesoreria.BD::TES.ConfiguracionChequera" 
		(
			 "IdConfiguracionChequera",
			 "NombreConfChequera",
			 "AnchoCheque",
			 "AltoCheque",
			 "SigloImpreso",
			 "MontoFila",
			 "MontoColumna",
			 "ProvinciaFila",
			 "ProvinciaColumna",
			 "MesFila",
			 "MesColumna",
			 "AnioFila",
			 "AnioColumna",
			 "ExtendidoAFila",
			 "ExtendidoAColumna",
			 "PrimeraCantidadFila",
			 "PrimeraCantidadColumna",
			 "SegundaCantidadFila",
			 "SegundaCantidadColumna"
		)
		VALUES (
		   :p_out_REF_ID,
		   :p_NombreConfChequera,
		   :p_AnchoCheque,
		   :p_AltoCheque,
		   :p_SigloImpreso,
		   :p_MontoFila,
		   :p_MontoColumna,
		   :p_ProvinciaFila,
		   :p_ProvinciaColumna,
		   :p_MesFila,
		   :p_MesColumna,
		   :p_AnioFila,
		   :p_AnioColumna,
		   :p_ExtendidoAFila,
		   :p_ExtendidoAColumna,
		   :p_PrimeraCantidadFila,
		   :p_PrimeraCantidadColumna,
		   :p_SegundaCantidadFila,
		   :p_SegundaCantidadColumna
		);

    End; 
END;