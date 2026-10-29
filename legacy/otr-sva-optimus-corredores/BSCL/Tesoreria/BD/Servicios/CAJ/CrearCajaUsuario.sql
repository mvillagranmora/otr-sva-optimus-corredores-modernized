SET SCHEMA "BSCL"; 
GO 
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.CAJ::CrearCajaUsuario";
GO
CREATE PROCEDURE "BSCL.Tesoreria.BD.Servicios.CAJ::CrearCajaUsuario" (
-- 
--  Proposito: Asocia Usuario a una Caja.
--  Autor: - BCS
--  Fecha Creacion: 27/01/2016
--  

-- Parámetros entrada comunes en todos los servicios públicos
	IN  p_IdEmpresa				BIGINT,						
	IN  p_IdUsuario				BIGINT,						--ID del usuario que solicita el servicio
	IN  p_IdModulo				NVARCHAR(3),  				
	IN  p_SimularOperacion		NVARCHAR(1) DEFAULT 'N',	-- S/N se usa para simular operacion (algunos cASos)
	IN  p_UsaReglasNegocio	 	NVARCHAR(1) DEFAULT 'N',	-- S/N se usa en cASo de servicios anidados.	
		  
-- Parámetros entrada del servicio público
          
	IN p_IdCaja                 BIGINT,						
	IN p_IdUsuarioCaja          BIGINT,						
	IN p_Sobregiro				NVARCHAR(1) DEFAULT 'N',	--S/N Indica si el cajero tiene la opcion de permitir que el Cliente se sobregire.
	IN p_FechaInicio		    NVARCHAR(10),
    IN p_FechaTermino      	    NVARCHAR(10),
    IN p_IdEstado       		NVARCHAR(3),      			
      			
      
-- Parámetros de salida del servicio público
	OUT p_out_IdCajaUsuario     BIGINT,    					--Correlativo
   
-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 	INT,
	OUT p_out_AdvertenciasDetalle 	TABLE ("Codigo" NVARCHAR(40),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),	
	OUT p_out_ErroresContador 		INT,
	OUT p_out_ErroresDetalle 		TABLE ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520))
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"

AS
BEGIN
	---------------------------
	--DECLARE Manejo de Errores
	---------------------------
	DECLARE SALIDA_CONTROLADA CONDITION; 	
	DECLARE SALIDA_CONTROLADA_NO_DATA CONDITION; 
	
	DECLARE v_Secuencia 		BIGINT;	  
	DECLARE v_resultadoRegla 	NVARCHAR(40);
	DECLARE v_MensajeRegla		NVARCHAR(520);
	 
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
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
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
		
		CALL "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosCrearCajaUsuario"(:p_IdEmpresa, :p_SimularOperacion,:p_IdCaja,:p_IdUsuarioCaja, :v_CajasRechazadas,:v_ErroresDetalle,:v_resultadoRegla);
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
		--* INSERTAR CAJA USUARIO *
		--*********************************	

		SELECT "BSCL.Tesoreria.BD.Secuencias::CajaUsuario".NEXTVAL INTO v_Secuencia FROM DUMMY;
			INSERT INTO "BSCL.Tesoreria.BD::TES.CajaUsuario"
		    (
				"IdCajaUsuario",                                                                
				"IdEmpresa",      
				"IdCaja",                                                                    
				"IdUsuario", 
				"Sobregiro",
				"FechaInicio",    
				"FechaTermino",        
				"IdEstado"                                 
			)
		    VALUES (		
				:v_Secuencia,
				p_IdEmpresa,
				p_IdCaja,    
				p_IdUsuarioCaja, 
				p_Sobregiro,         
				TO_DATE(p_FechaInicio, 'YYYY-MM-DD'), 
				TO_DATE(p_FechaTermino, 'YYYY-MM-DD'), 
				p_IdEstado                                                       	   	
		    );
		
		p_out_IdCajaUsuario := v_Secuencia;
		
		/* select 'IdCaja' as "Campo",
				"IdCaja" as "Id",
				'BSCL.Tesoreria.BD.Servicios::CrearCajaUsuario' as "Servicio",
				'Rech' as "Resultado"
		From :v_CajasRechazadas; */
		
	END;

	--LOG DE AUDITORIA
	 BEGIN
	 DECLARE p_Log TABLE(
		"IdCabeceraLog"    BIGINT,
		"IdTipoLog"     NVARCHAR(40),
		"IdModulo"     NVARCHAR(3),
		"IdUsuario"     BIGINT,
		"IdEmpresa"     BIGINT,
		"FechaSistemaLog"   NVARCHAR(10),
		"IdUsuarioAtendido"   BIGINT,
		"IdPersona" BIGINT,
		"IdCliente"     BIGINT,
		"EstadoCliente"    NVARCHAR(3),
		"NombreCliente"    NVARCHAR(100),
		"IdTipoDocumentoIdentidad" NVARCHAR(40),
		"IdDocumentoIdentidad"  NVARCHAR(40),
		"IdPortafolio"    BIGINT,
		"EstadoPortafolio"   NVARCHAR(3),
		"IdTipoRegistro"   NVARCHAR(40),
		"IdRegistro"    NVARCHAR(40),
		"EstadoRegistro"   NVARCHAR(3),
		"IdTipoOperacion"   NVARCHAR(40),
		"Folio"      BIGINT,
		"Cantidad"     DECIMAL(26,8),
		"PrecioTasa"    DECIMAL(26,8),
		"Monto"      DECIMAL(26,8),
		"IdMonedaOrigen"   NVARCHAR(3),
		"IdMonedaLiquidacion"  NVARCHAR(3),
		"TipoCambio"    NVARCHAR(40),
		"Nemo"      NVARCHAR(260),
		"IdProceso"     BIGINT,
		"EstadoProceso"    NVARCHAR(3),
		"NombreProceso"    NVARCHAR(100)
		);
	 	DECLARE v_FechaSistema  NVARCHAR(10);

		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema"(
				p_IdEmpresa
				, :v_FechaSistema
			);
		
		p_Log =
			SELECT
				"BSCL.Plataforma.BD.Secuencias::Log".NEXTVAL AS "IdCabeceraLog"
				,'INGRESO' as "IdTipoLog"
				,'TES' as "IdModulo"
				,p_IdUsuario as "IdUsuario"
				,p_IdEmpresa as "IdEmpresa"
				,TO_VARCHAR (v_FechaSistema, 'YYYY-MM-DD') as "FechaSistemaLog"
				,p_IdUsuarioCaja as "IdUsuarioAtendido"
				,0 AS "IdPersona"
				,0 as "IdCliente"
				,'' as "EstadoCliente"
				,'' as "NombreCliente"
				,'' as "IdTipoDocumentoIdentidad"
				,'' as "IdDocumentoIdentidad"
				,0 as "IdPortafolio"
				,'' as "EstadoPortafolio"
				,'CAJA' as "IdTipoRegistro"
				,0 as "IdRegistro"
				,'' as "EstadoRegistro"
				,'' as "IdTipoOperacion"
				,0 as "Folio"
				,0 as "Cantidad"
				,0 as "PrecioTasa"
				,0 as "Monto"
				,'' as "IdMonedaOrigen"
				,'' as "IdMonedaLiquidacion"
				,'' as "TipoCambio"
				,'' as "Nemo"
				,0 as "IdProceso"
				,'' as "EstadoProceso"
				,'Ingreso Usuario - Caja' as "NombreProceso"
			FROM DUMMY;
	 
	 	CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (
	 		p_Log   => :p_Log
	 	);
		 
	 END;
END;