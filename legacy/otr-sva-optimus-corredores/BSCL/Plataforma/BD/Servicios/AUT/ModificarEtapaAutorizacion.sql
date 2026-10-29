SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::AUT.ModificarEtapaAutorizacion";

CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::AUT.ModificarEtapaAutorizacion" (
-- 
--  Proposito: Crea una etapa de un tipo de evento de autorizacion dado
--  Autor: - BCS
--  Fecha Creacion: 27/07/2015
--  
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa				BIGINT,			--ID Empresa destino
	IN  p_IdUsuario				BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo				NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_SimularOperacion		NVARCHAR(1) DEFAULT 'N',	-- S/N se usa para simular operacion (algunos cASos)
	IN  p_UsaReglasNegocio	 	NVARCHAR(1) DEFAULT 'N',	-- S/N se usa en cASo de servicios anidados.	
		  
	-- Parametros entrada del servicio publico

	IN p_IdEtapa          	   BIGINT,                 --//[FK]AUT.Etapa;IdTipoEvento;AUT.TipoEvento;IdTipoEvento// 
	IN p_Codigo                NVARCHAR(40),
	IN p_Nombre                NVARCHAR(100),
	IN p_Orden				   INT,
    IN p_TipoAutorizacion      NVARCHAR(1) DEFAULT  'T', --//(T)Deben autorizar todos (S)BASta solo uno (M)Un minimo de  
    IN p_MinimoAutorizan       INTEGER DEFAULT '0',      --//Si TipoAutorizacion = 'M' este campo debe ser distinto de cero
    IN p_IdEstado              NVARCHAR(3),       					--//[FK]AUT.Etapa;IdEstado;PLA.Estado;IdEstado//
      
	-- Parametros salida del servicio publico
	OUT p_out_IdEtapa		   BIGINT,
	
	-- SalidAS para mensajes de advertenciAS y errores
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
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE v_errorhAShmsg 	TABLE ("Codigo" NVARCHAR(40),"Valor" NVARCHAR(520));
	DECLARE v_erroroutmsg 	NVARCHAR(520);
	DECLARE v_Secuencia 	BIGINT;	  
	DECLARE v_out_Resultado CHAR(1);
    DECLARE v_out_Mensaje 	NVARCHAR(520); 
    DECLARE v_CodigoError 	INT;
    DECLARE v_MensajeError 	NVARCHAR(1000);
    DECLARE v_Descripcion 	NVARCHAR(100);
	 
	DECLARE SALIDA_CONTROLADA CONDITION;  	
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT COUNT(1) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;		 					 	
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
	
	BEGIN	
		--------------------
		--Declare Variables
		--------------------
		DECLARE v_count_Existe BIGINT :=0;
		-------------------------
		--Validaciones básicas 
		-------------------------
				
		--Validacion Codigo en blanco
		IF TRIM(p_Codigo) = '' THEN
			v_hash= SELECT 'Codigo' AS "Codigo", '' AS "Valor" FROM DUMMY;	        
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('CODIGO_EN_BLANCO',:v_hASh,:v_erroroutmsg);
			p_out_ErroresDetalle =  SELECT 'Codigo' AS "Codigo", :v_erroroutmsg AS "Descripcion" FROM dummy UNION ALL
									SELECT * FROM :p_out_ErroresDetalle; 		  
		END IF;		
		
		--Validacion nombre en blanco
		IF TRIM(p_Nombre) = '' THEN
			v_hash= SELECT 'Nombre' AS "Codigo", '' AS "Valor" FROM DUMMY;
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('NOMBRE_EN_BLANCO',:v_hASh,:v_erroroutmsg);
			p_out_ErroresDetalle =  SELECT 'Nombre' AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy UNION ALL
									SELECT * FROM :p_out_ErroresDetalle; 
		END IF;		

		--Validacion tipo de autorizacion
		IF  LOCATE('T U M', TRIM(p_TipoAutorizacion)) = 0 THEN
			v_hash= SELECT 'TipoAutorizacion' AS "Codigo", p_TipoAutorizacion AS "Valor" FROM DUMMY;
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('TIPOAUTORIZACION_NO_EXISTE',:v_hASh,:v_erroroutmsg);
			p_out_ErroresDetalle =  SELECT 'TipoAutorizacion' AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy UNION ALL
									SELECT * FROM :p_out_ErroresDetalle; 
		END IF;	

   		v_count_Existe := 0;
   		
			
		SELECT COUNT(1) INTO v_count_Existe FROM "BSCL.Plataforma.BD::PLA.Estado" WHERE "IdEstado" = :p_IdEstado;
			
		--IF  v_out_Resultado = 'N' THEN
		IF  v_count_Existe = 0 THEN
			v_hash= SELECT 'IdEstado' AS "Codigo", p_IdEstado AS "Valor" FROM DUMMY;
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('ESTADO_NO_EXISTE',:v_hash,:v_erroroutmsg);
			p_out_ErroresDetalle =  SELECT 'IdEstado' AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy UNION ALL
									SELECT * FROM :p_out_ErroresDetalle; 
		END IF;	
					
		--Si existen errores en las validaciones debe salir
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle; 
		IF :p_out_ErroresContador > 0 THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;		
		
		------------------------
		--Validaciones via Motor (solo complejAS)
		------------------------		
		--
	
		--------------------
		--ReglAS Negocio 
		--------------------		
		--
					
		--------------------
		--Negocio Aqui
		--------------------
		Begin
	      	--Se obtiene numero de secuencia que usaremos para el id del tipo de evento
    		
			UPDATE "BSCL.Plataforma.BD::AUT.Etapa"
		    	SET "Codigo" = TRIM(p_Codigo),                                                                   
				"Nombre" = TRIM(p_Nombre), --
				"Orden" = p_Orden,                                                                   
				"TipoAutorizacion" = p_TipoAutorizacion , 
				"MinimoAutorizan" = p_MinimoAutorizan, 
				"IdEstado" = p_IdEstado
			WHERE 
			"IdEtapa" = p_IdEtapa
			AND "IdEmpresa" = :p_IdEmpresa;
		    
		END;
		
		p_out_IdEtapa := v_Secuencia;
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;


