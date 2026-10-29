SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::ILS.MantenedorInformeConfiguracion";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::ILS.MantenedorInformeConfiguracion" (
	-- Parametros entrada comunes
	IN p_IdTipoInforme		BIGINT,
	IN p_IdIndice			BIGINT,
	IN p_Orden	 			BIGINT,
	IN p_TipoOperacion		NVARCHAR(1),
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_IdUsuario			BIGINT,			--ID del usuario que solicita el servicio	  
	-- Salidas para mensajes de advertencias y errores
    
	OUT p_out_AdvertenciasContador	INT,
	OUT p_out_AdvertenciasDetalle	TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000))DEFAULT EMPTY,	
	OUT p_out_ErroresContador		INT,
	OUT p_out_ErroresDetalle		TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))DEFAULT EMPTY
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN

	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
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
	
	BEGIN
		--------------------
		--Declare Variables
		--------------------
	    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
	    DECLARE v_out_ErroresDetalle		TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
        DECLARE v_out_AdvertenciasDetalle	TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
        DECLARE v_countInfConfig BIGINT;

		--------------------
		--Validaciones
		--------------------
		--------------------
		--Reglas Negocio
		--------------------
		
		--------------------
		--Lógica de Negocio 
		--------------------
		BEGIN
		
		
			-----------------------------
			-- Elimina 
			-----------------------------
			IF :p_TipoOperacion = 'E' THEN
			
				DELETE FROM "BSCL.Normativa.BD::ILS.InformeConfiguracion"
				WHERE "IdTipoInforme" = :p_IdTipoInforme 
				AND	"IdIndice" = :p_IdIndice; 
				
			END IF;	
			
    	   
			-----------------------------
			-- Inserta 
			-----------------------------
			IF :p_TipoOperacion = 'I' THEN
			
				SELECT COUNT(*) INTO v_countInfConfig 
				FROM "BSCL.Normativa.BD::ILS.InformeConfiguracion" 
				WHERE "IdTipoInforme" = :p_IdTipoInforme AND "IdIndice" = :p_IdIndice;
			
				IF v_countInfConfig = 0 THEN
					INSERT INTO "BSCL.Normativa.BD::ILS.InformeConfiguracion"
					(
						 "IdTipoInforme" 
						,"IdIndice" 
						,"Orden"
					)
					VALUES
					(
						 :p_IdTipoInforme
						,:p_IdIndice
						,:p_Orden
					);
				ELSE
					p_out_ErroresContador	:= 1;
					p_out_ErroresDetalle = SELECT 
												'1' AS "Codigo"
												,'El Indice seleccionado ya existe' AS "Descripcion"
											FROM DUMMY;
				END IF;
				
			END IF;	
			
		END;		
	END;	
END;
