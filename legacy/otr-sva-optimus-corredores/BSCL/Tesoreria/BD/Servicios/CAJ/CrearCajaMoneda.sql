SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.CAJ::CrearCajaMoneda";

CREATE PROCEDURE "BSCL.Tesoreria.BD.Servicios.CAJ::CrearCajaMoneda" (
-- 
--  Proposito: Crea una moneda asociada a una Caja.
--  Autor: - BCS
--  Fecha Creacion: 15/01/2016
--  

-- Parámetros entrada comunes en todos los servicios públicos
	IN  p_IdEmpresa				BIGINT,						--ID Empresa destino
	IN  p_IdUsuario				BIGINT,						--ID del usuario que solicita el servicio
	IN  p_IdModulo				NVARCHAR(3),  				--ID del Modulo que lo invoca
	IN  p_SimularOperacion		NVARCHAR(1) DEFAULT 'N',	-- S/N se usa para simular operacion (algunos cASos)
	IN  p_UsaReglasNegocio	 	NVARCHAR(1) DEFAULT 'N',	-- S/N se usa en cASo de servicios anidados.	
		  
-- Parámetros entrada del servicio público
	          
	IN p_IdCaja                 BIGINT,						--Dato de entrada
	IN p_IdMoneda               NVARCHAR(3),				--Dato de entrada
	IN p_Saldo					DECIMAL(26,8),				--Saldo Caja Moneda
	IN p_IdTipoMovimiento	    NVARCHAR(6),				--Dato de entrada
    IN p_IdEstado       		NVARCHAR(3),      			--Dato de entrada
      			
      
-- Parámetros de salida del servicio público
	OUT p_out_IdCajaMoneda          BIGINT,    					--Correlativo
   
-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 	INT,
	OUT p_out_AdvertenciasDetalle 	TABLE ("Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000)),	
	OUT p_out_ErroresContador 		INT,
	OUT p_out_ErroresDetalle 		TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"

AS
BEGIN
	---------------------------
	--DECLARE Manejo de Errores
	---------------------------
	DECLARE SALIDA_CONTROLADA 			CONDITION; 
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;

	DECLARE v_Secuencia 			BIGINT;	  
	DECLARE v_resultadoRegla 		NVARCHAR(40);
	DECLARE v_MensajeRegla			NVARCHAR(520);
	
	DECLARE v_erroroutmsg  				NVARCHAR(5000);	
	DECLARE v_ErrorNoData  				NVARCHAR(100);
	
	DECLARE v_CajasRechazadas			TABLE ("IdCaja" BIGINT);
	DECLARE v_ErroresDetalle			TABLE ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520));	
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT COUNT(1) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;		 					 	
	END;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN
		v_hash= SELECT '' as "Codigo",'' as "Valor" FROM DUMMY;	
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_ErrorNoData,:v_hash,:v_erroroutmsg);
		p_out_ErroresDetalle =  SELECT :v_ErrorNoData  as "Codigo",:v_erroroutmsg as "Descripcion" FROM dummy UNION ALL
								SELECT * FROM :p_out_ErroresDetalle;
		SELECT COUNT(1) INTO p_out_ErroresContador
		FROM :p_out_ErroresDetalle;
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DECLARE v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT v_errorExt,SUBSTRING(::SQL_ERROR_MESSAGE, 0, 5000) FROM dummy;				
			SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= SELECT '' AS "Codigo",'' AS "RequiereAutorizacion",'' AS "Descripcion" FROM dummy WHERE 1=2;
	p_out_ErroresDetalle		= SELECT '' AS "Codigo",'' AS "Descripcion" FROM dummy WHERE 1=2;
	
	v_CajasRechazadas				= SELECT 0  as "IdCaja" FROM dummy where 1=2;
	v_ErroresDetalle				= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;
	
	--------------------
	--VALIDACIONES
	--------------------	
	IF p_UsaReglasNegocio = 'S' THEN
	BEGIN
		v_MensajeRegla := '';
		---Validaciones datos de entrada			
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_Errores,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN
			 SIGNAL SALIDA_CONTROLADA; 	
		END IF;
		
		CALL "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosCrearCajaMoneda"(:p_SimularOperacion,0,:p_IdCaja, :p_IdMoneda,:v_CajasRechazadas,:v_ErroresDetalle,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN				
			p_out_ErroresDetalle = SELECT "Codigo","Descripcion" FROM :v_ErroresDetalle;
			SIGNAL SALIDA_CONTROLADA; 	
		END IF;
	END;
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
		
		--*********************************
		--* INSERTAR CAJA MONEDA *
		--*********************************
		 SELECT "BSCL.Tesoreria.BD.Secuencias::CajaMoneda".NEXTVAL INTO v_Secuencia FROM DUMMY;
			INSERT INTO "BSCL.Tesoreria.BD::TES.CajaMoneda"
		    (
				"IdCajaMoneda",                                                                
				"IdEmpresa",      
				"IdCaja",                                                                    
				"IdMoneda", 
				"Saldo",                                                                   
				"IdTipoMovimiento",    
				"IdEstado"                                 
			)
		    VALUES (		
				:v_Secuencia,
				p_IdEmpresa,
				p_IdCaja,    
				p_IdMoneda, 
				p_Saldo,      
				p_IdTipoMovimiento,     
				p_IdEstado                                                       	   	
		    );
		
		p_out_IdCajaMoneda := v_Secuencia;
		
		/* select 'IdCaja' as "Campo",
				"IdCaja" as "Id",
				'BSCL.Tesoreria.BD.Servicios::CrearCajaMoneda' as "Servicio",
				'Rech' as "Resultado"
		From :v_CajasRechazadas; */
		
	END;

END;
