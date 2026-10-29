SET SCHEMA "BSCL"; 
GO
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ActualizaContraparte";
GO
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ActualizaContraparte" (

--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          				BIGINT 			default 0,
    IN p_IdUsuario          				BIGINT 			default 0,
    IN p_IdModulo           				NVARCHAR(3) 	default '',  	

    IN p_IdInstrumento						BIGINT 			default 0,
	IN p_IdEventoCapital					BIGINT     		DEFAULT 0,
	IN p_IdTipoEventoCapital				NVARCHAR(2) 	DEFAULT '',
	IN p_IdEventoCapitalCliente				BIGINT     		default 0,


-- Parametros de Salida
-- Salidas para mensajes de advertencias y errores
	OUT p_out_InformacionContador  				INT,
	OUT p_out_InformacionDetalle   				TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)),

	OUT p_out_ErroresContador       INT,
	OUT p_out_ErroresDetalle        TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
	 )   
	 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
	
BEGIN 
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE abort int default 0;
	DECLARE v_existecontraparte	INT DEFAULT 0; 
	DECLARE v_pendientes BIGINT DEFAULT 0;
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
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
			--Error interno no manejado  (El log sera captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;

	p_out_ErroresContador		:= 0;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	BEGIN
	
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
		--variables de proceso
		DECLARE v_DocumentoIdentidad	NVARCHAR(40);
		DECLARE v_RazonSocial			NVARCHAR(100);
		
	
		BEGIN
	
			p_out_ErroresContador			:= 0;
			p_out_ErroresDetalle			= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;

	--------------------------------------------------------------
	-- CONSULTA SI EXISTE CONTRAPARTE SOCIO DE NEGOCIO			--
	--------------------------------------------------------------


		SELECT count(1) INTO v_existecontraparte
		FROM "BSCL.Plataforma.BD::INS.Instrumento" AS INS
		INNER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS EMI
		ON INS."IdEmisor" = EMI."IdEmisor"
		
		WHERE  INS."IdInstrumento" = p_IdInstrumento;
		
		IF (v_existecontraparte > 0) THEN				
			SELECT IFNULL(INS."RazonSocial",''), IFNULL(EMI."DocumentoIdentidad",'') into  v_RazonSocial, v_DocumentoIdentidad
			
			FROM "BSCL.Plataforma.BD::INS.Instrumento" AS INS
			INNER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS EMI
			ON INS."IdEmisor" = EMI."IdEmisor"

			
			WHERE  INS."IdInstrumento" = p_IdInstrumento;
		
	--------------------------------------------------------------
	-- SI EXISTE ACTUALZA EVENTO CAPITAL CLIENTE SOCIO NEGOCIO	--
	--------------------------------------------------------------

			UPDATE "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
			SET 
				"DocumentoIdentidad"	= v_DocumentoIdentidad,
				"NombreCliente"			= v_RazonSocial,
				"IdEstado"				= 'VIG'
			WHERE
				"IdEventoCapitalCliente"	= 	p_IdEventoCapitalCliente	and
				"IdEventoCapital" 			= 	p_IdEventoCapital;
			
			SELECT COUNT(1) INTO v_pendientes
			FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
			WHERE "IdEventoCapital" = 	p_IdEventoCapital
			AND "IdEstado" = 'PEN';
			
			IF v_pendientes = 0 THEN
				UPDATE "BSCL.Custodia.BD::CUS.EventoCapital"
				SET "IdEstado" = 'PRO'
				WHERE "IdEventoCapital" = p_IdEventoCapital;
			END IF;
				
			v_TablaCodigosMensajes =	Select 	p_IdEventoCapital				As "IdInterno", 
												'UI_CONTRAPARTE_OK'				As "Codigo",
												'' 								As "CodigoDato1", 
												'' 								as "ValorDato1",
												'' 								As "CodigoDato2", 
												'' 								As "ValorDato2" 
							From dummy;
					                        
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_out_hash);
				
			p_out_InformacionContador = 1;
			p_out_InformacionDetalle = 	Select "Codigo", "Descripcion" 
										From :v_out_hash;	
				
		ELSE
			v_TablaCodigosMensajes =	Select 	p_IdEventoCapital				As "IdInterno", 
												'UI_CONTRAPARTE_NK'				As "Codigo",
												'' 								As "CodigoDato1", 
												'' 								as "ValorDato1",
												'' 								As "CodigoDato2", 
												'' 								As "ValorDato2" 
											From dummy;
					                        
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_out_hash);
				
			p_out_ErroresContador 	= 1;
			p_out_ErroresDetalle 	= 	Select "Codigo", "Descripcion" 
										From :v_out_hash;			
										
		END IF;
		
		
		IF p_out_ErroresContador > 0 then 
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		

		END;
		-----------------------------------------------------
		--fin proceso de Negocio
		---------------------------------------------------
	END;
END;
GO
