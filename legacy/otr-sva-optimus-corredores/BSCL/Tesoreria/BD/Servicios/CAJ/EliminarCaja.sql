SET SCHEMA "BSCL";  
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.CAJ::EliminarCaja";

CREATE PROCEDURE 
-- 
--  Proposito: Bloquear o desbloquear movientos o comprobantes de tesoreria
--  Autor: CES - BCS
--  Fecha Creacion: 04/02/2016
--   
"BSCL.Tesoreria.BD.Servicios.CAJ::EliminarCaja" (
    -- Parametros de Validacion
    IN p_IdEmpresa 						BIGINT,  
    IN p_IdUsuario 						BIGINT, 
    IN p_IdModulo 						NVARCHAR(3), 
   
    IN p_UsaReglasNegocio 				NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.   
   
    -- Parametros de Entrada 
	IN p_IdCaja							BIGINT,
	IN p_FechaModificacion				NVARCHAR(10),

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
	--DECLARE v_errorcuenta 				INT DEFAULT 0;  
	--DECLARE v_errorhashmsg 				TABLE ("Codigo" NVARCHAR(100),"Valor" NVARCHAR(5000));
	DECLARE v_erroroutmsg  				NVARCHAR(5000);		
	DECLARE v_ErrorNoData  				NVARCHAR(100);
	DECLARE v_CajasRechazadas			TABLE ("IdCaja" BIGINT);
	
	DECLARE v_resultadoRegla 			NVARCHAR(40);
	DECLARE v_resultadoReglaMsg			NVARCHAR(520);
	DECLARE v_ErroresDetalle			TABLE ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520));	        	
	        	
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
		p_out_ErroresDetalle =  SELECT :v_ErrorNoData  as "Codigo",:v_erroroutmsg as "Descripcion" FROM dummy UNION ALL
								SELECT * FROM :p_out_ErroresDetalle;
		SELECT COUNT(1) INTO p_out_ErroresContador
		FROM :p_out_ErroresDetalle;
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
	
	p_out_AdvertenciasContador		:= 0;
	p_out_ErroresContador			:= 0;
	p_out_AdvertenciasDetalle		= SELECT '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" FROM dummy where 1=2;
	p_out_ErroresDetalle			= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2; 	 	
    p_out_ResultadoProceso			= SELECT '' as "Campo", 0 as "Id", '' as "Servicio", '' as "Resultado" FROM dummy where 1=2;
    
	v_ErroresDetalle				= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;
	v_CajasRechazadas				= SELECT 0  as "IdCaja" FROM dummy where 1=2;
	
	--------------------
	--VALIDACIONES
	--------------------	   
	IF p_UsaReglasNegocio = 'S' THEN
	BEGIN		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_Errores,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN
			 SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		CALL "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosEliminarCaja"('N', :p_IdCaja, :v_CajasRechazadas, :v_ErroresDetalle, :v_resultadoRegla, :v_resultadoReglaMsg);
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
		--* Eliminar Caja-ProductoEmpresa *
		--*********************************
		DELETE FROM "BSCL.Tesoreria.BD::TES.CajaProductoEmpresa"
		WHERE "IdCaja" = p_IdCaja;

		--*********************************
		--**** Eliminar Caja-Usuarios *****
		--*********************************
		DELETE FROM "BSCL.Tesoreria.BD::TES.CajaUsuario"
		WHERE "IdCaja" = p_IdCaja;

		--*********************************
		--***** Eliminar Caja-Moneda ******
		--*********************************
		DELETE FROM "BSCL.Tesoreria.BD::TES.CajaMoneda"
		WHERE "IdCaja" = p_IdCaja;
		
		--*********************************
		--********* Eliminar Caja *********
		--*********************************
		DELETE FROM "BSCL.Tesoreria.BD::TES.Caja"
		WHERE "IdCaja" = p_IdCaja;
		
		v_DatoLog = Select '' AS "SubRegistro", '' AS "CampoRegistro" , '' AS "ValorAntiguo", '' AS "ValorNuevo" 
							From DUMMY Where 1 = 2;
		
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
				p_IdTipoLog => 'ELIMINACION' 
				,p_IdModulo => 'TES' 
				,p_IdUsuario => :p_IdUsuario 
				,p_IdEmpresa => :p_IdEmpresa
				,p_FechaSistemaLog => :p_FechaModificacion
				,p_IdUsuarioAtendido => :p_IdUsuario
				,p_IdTipoRegistro => 'CAJA'
				,p_IdProceso => p_IdCaja
				--,p_EstadoProceso => ''
				,p_NombreProceso => 'Eliminacion Caja'
				,p_DetalleLog => :v_DatoLog
				);
		
		/* select 'IdCaja' as "Campo",
				"IdCaja" as "Id",
				'BSCL.Tesoreria.BD.Servicios::EliminarCaja' as "Servicio",
				'Rech' as "Resultado"
		From :v_CajasRechazadas; */
		
	END;
END;