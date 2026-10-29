SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::ILS.MantenedorIndice";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::ILS.MantenedorIndice" (
	-- Parametros entrada comunes
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_IdUsuario			BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo			NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN p_UsaReglasNegocio	NVARCHAR(1),	--S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada
	IN p_TipoOperacion		NVARCHAR(1),
	IN p_tablaIndice TABLE(
										 "IdIndice" 	BIGINT,
										 "Nombre" 		NVARCHAR(200),
										 "NombreCorto" 	NVARCHAR(20),
										 "Tipo" 		NVARCHAR(1),
										 "Estado" 		NVARCHAR(3)
									 ),
    
	-- Salidas para mensajes de advertencias y errores
    OUT p_out_IdIndice TABLE (
    								     "IdIndice" BIGINT,
									     "Estado" NVARCHAR(3)
									 ),
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
				
				DECLARE v_ExisteIndiceConfiguracion 	INT;
				
				--------------------
				--VALIDACIONES
				--------------------			 								
				v_ExisteIndiceConfiguracion := 0;	
				
				SELECT COUNT(*) INTO v_ExisteIndiceConfiguracion
				FROM "BSCL.Normativa.BD::ILS.IndiceConfiguracion" AS Indice
				INNER JOIN :p_tablaIndice AS TabIndice
				ON TabIndice."IdIndice" = Indice."IdIndice";					 
			
				IF :v_ExisteIndiceConfiguracion > 0 THEN
					
					p_out_ErroresDetalle = SELECT '' AS "Codigo"
					                             , 'Debe eliminar IdConcepto de "Indice configuración".' AS "Descripcion"
					                        FROM DUMMY;
					SIGNAL SALIDA_CONTROLADA;
				ELSE 								
					DELETE FROM "BSCL.Normativa.BD::ILS.Indice" AS Indice
					WHERE Indice."IdIndice"
					IN (SELECT tabIndice."IdIndice"
				    FROM :p_tablaIndice AS tabIndice);
				END IF;
			END IF;	
		   
			-----------------------------
			-- Inserta 
			-----------------------------
			IF :p_TipoOperacion = 'I' THEN
			   --**Crear tabla de paso
			   	v_tblIndice_ = 
			   	SELECT '0' as "IdIndice"
					 ,"IdEmpresa" 	 
					 ,"Nombre" 	     	
					 ,"NombreCorto" 
					 ,"Tipo"
					 ,"Estado" 	         
			    FROM "BSCL.Normativa.BD::ILS.Indice" WHERE 1=2;
		
			
		     	v_tblIndice_ = 
				SELECT  "BSCL.Normativa.BD.Secuencias::ILS.IdIndice".NEXTVAL as "IdIndice" 
						 , p_IdEmpresa    as   "IdEmpresa"
						 ,"Nombre" 	     		
						 ,"NombreCorto"
						 ,"Tipo"
						 ,"Estado"	   
				FROM :p_tablaIndice; 
		
			
				INSERT INTO "BSCL.Normativa.BD::ILS.Indice"
				(
					  "IdIndice" 
					 ,"IdEmpresa" 	 
					 ,"Nombre" 	     
					 ,"NombreCorto"
					 ,"Tipo"
					 ,"Estado" 	     		
				)
				SELECT
				  "IdIndice"
				 ,"IdEmpresa"
				 ,"Nombre" 	     
				 ,"NombreCorto"
				 ,"Tipo"
				 ,"Estado" 	    
				FROM :v_tblIndice_;
				
				
			   --DEVUELVE MOVIMIENTOS AGREGADOS
				p_out_IdIndice = 
				SELECT 
				       "BSCL.Normativa.BD::ILS.Indice"."IdIndice",  
				       "BSCL.Normativa.BD::ILS.Indice"."Estado"    
				FROM :v_tblIndice_ Indice
				INNER JOIN  "BSCL.Normativa.BD::ILS.Indice" 
				ON Indice."IdIndice"  = "BSCL.Normativa.BD::ILS.Indice"."IdIndice"  ;
				  			
			END IF;	
			
			-----------------------------
			-- Modifica 
			-----------------------------
			IF :p_TipoOperacion = 'M' THEN
			
				UPDATE Indice
				SET "Nombre" 		= TabIndice."Nombre",  
				    "NombreCorto" 	= TabIndice."NombreCorto",
				    "Tipo" 			= TabIndice."Tipo"  
				FROM "BSCL.Normativa.BD::ILS.Indice" AS Indice
				INNER JOIN :p_tablaIndice AS TabIndice
						ON Indice."IdIndice" = TabIndice."IdIndice";

			END IF;			
		END;		
	END;	
END;
