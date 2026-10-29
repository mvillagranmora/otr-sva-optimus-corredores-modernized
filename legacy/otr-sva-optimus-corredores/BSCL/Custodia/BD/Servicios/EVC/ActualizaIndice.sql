SET SCHEMA "BSCL"; 
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ActualizaIndice";
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ActualizaIndice" (

--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          				BIGINT 			default 0,
    IN p_IdUsuario          				BIGINT 			default 0,
    IN p_IdModulo           				NVARCHAR(3) 	default '',  	

	IN p_IdEventoCapital					BIGINT     		DEFAULT 0,
	IN p_IdTipoEventoCapital				NVARCHAR(2) 	DEFAULT '',
	in p_Secuencia							BIGINT 			default 0,

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
	DECLARE abort 				INT default 0;
	DECLARE v_indice			INT DEFAULT 0;
	DECLARE v_descripcion		NVARCHAR(100);	   
	DECLARE v_val_acc 			DECIMAL(26,8) 	DEFAULT 0;	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta 		INT DEFAULT 0;  
	DECLARE v_errorhashmsg 		TABLE ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  		NVARCHAR(520);	        	
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

	v_indice:= 0;
	
	SELECT 	
				IFNULL(MAX("ACV_INDICE"),0) into v_indice
			FROM "BSCL.Custodia.BD::CUS.EventoCapitalBursatil" 
			WHERE 
				"ACF_SEC_VC" 	= p_Secuencia;

	IF (v_indice > 0) THEN

		SELECT IFNULL(BUR."ACV_INDICE",0),IFNULL(BUR."ACV_DESCRIP_VC",''), IFNULL(BUR."ACV_VAL_ACC",0) INTO v_indice,v_descripcion, v_val_acc 
		FROM  "BSCL.Custodia.BD::CUS.EventoCapitalBursatil" BUR
		INNER JOIN (SELECT 	
					"ACF_NEMO", 
					"ACF_SEC_VC", 
					MAX(ACV_INDICE) AS "ACV_INDICE"
				FROM "BSCL.Custodia.BD::CUS.EventoCapitalBursatil" 
				WHERE 
					"ACF_SEC_VC" 	= p_Secuencia
				GROUP BY "ACF_NEMO", "ACF_SEC_VC") 			AS 	TMP
				
			ON 	BUR."ACF_NEMO" 		= 	TMP."ACF_NEMO" 		AND 
				BUR."ACF_SEC_VC" 	= 	TMP."ACF_SEC_VC" 	AND
				BUR."ACV_INDICE" 	=  	TMP."ACV_INDICE"
		INNER JOIN
				"BSCL.Custodia.BD::CUS.EventoCapital"				ECV 
		ON (ECV."IdTipoEventoCapital" 			= BUR."ACF_TIPO_VC"					AND
			ECV."Secuencia"						= BUR."ACF_SEC_VC"					AND
			ECV."IdClasificacionTipoInstrumento"	= 	CASE BUR."ACF_MERCADO"
															WHEN 'AC' THEN 'RV'
															ELSE BUR."ACF_MERCADO" 
														END							AND
			LTRIM(RTRIM(ECV."Nemo"))		= 	LTRIM(RTRIM(BUR."ACF_NEMO")))
		WHERE
			
			ECV."IdEventoCapital"		= 	p_IdEventoCapital		AND
			ECV."IdEmpresa"				= 	p_IdEmpresa				AND		
			ECV."IdTipoEventoCapital"	=	p_IdTipoEventoCapital	AND
			ECV."Secuencia"				=	p_Secuencia;

	--------------------------------------------------------------
	-- SI EXISTE ACTUALZA EVENTO CAPITAL CLIENTE SOCIO NEGOCIO	--
	--------------------------------------------------------------




	

			UPDATE "BSCL.Custodia.BD::CUS.EventoCapital"
			SET 
				"Descripcion"	= v_descripcion,
				"Factor"		= v_val_acc,
				"Indice"		= v_indice,
				"IdEstado"		= 	'VIG'--'MAN'
--				"IdMoneda"		= 				
		WHERE
				"IdEventoCapital" 		= 	p_IdEventoCapital			and
				"IdEmpresa"				= 	p_IdEmpresa					and
				"IdTipoEventoCapital"	= 	p_IdTipoEventoCapital		and
				"Secuencia"				=	p_Secuencia;				
				
				
			v_TablaCodigosMensajes =	Select 	p_IdEventoCapital				As "IdInterno", 
												'UI_INDICE_OK'					As "Codigo",
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
												'UI_INDICE_NK'					As "Codigo",
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
