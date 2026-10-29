SET SCHEMA "BSCL";  
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.CAJ::CrearCaja";

CREATE PROCEDURE 
-- 
--  Proposito: Crear Caja
--  Autor: CE - BCS
--  Fecha Creacion: 26/01/2016 
--   
"BSCL.Tesoreria.BD.Servicios.CAJ::CrearCaja" (
    -- Parametros de Validacion
    IN p_IdEmpresa 						BIGINT,  
    IN p_Codigo							NVARCHAR(40),
    IN p_Nombre							NVARCHAR(100),
    IN p_IdEstado						NVARCHAR(3),
          
    -- Salidas Tesoreria
    OUT p_out_IdCaja					BIGINT,
          
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

	--------------------
	--VALIDACIONES
	--------------------	   
	
	--------------------
	--LOGICA
	--------------------
	BEGIN
		DECLARE v_Secuencia BIGINT;
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;
		
		SELECT "BSCL.Tesoreria.BD.Secuencias::Caja".NEXTVAL INTO v_Secuencia FROM DUMMY;
		
		INSERT INTO "BSCL.Tesoreria.BD::TES.Caja" (
			"IdCaja",
			"IdEmpresa",
			"Codigo",
			"Nombre",
			"IdEstado"
		)
		VALUES (
			:v_Secuencia,
			p_IdEmpresa,
			p_Codigo,
			p_Nombre,
			p_IdEstado
		);
		--SELECT v_Secuencia INTO p_out_IdCaja FROM DUMMY;
		p_out_IdCaja := v_Secuencia;
  	END;
END;