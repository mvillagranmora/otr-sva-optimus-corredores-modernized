SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::ILS.ItemConfiguracionMantiene";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::ILS.ItemConfiguracionMantiene" (
	-- Parametros entrada comunes
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_IdUsuario			BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo			NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN p_UsaReglasNegocio	NVARCHAR(1),	--S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada
	IN p_TipoOperacion		CHAR(01),
	IN p_tablaItemConfiguracion TABLE(
									"IdConfiguracionItem" 	BIGINT, 
									"IdItem" 				BIGINT, 
									"IdProcesoFuncion" 		BIGINT, 
									"CuentaContable" 		NVARCHAR(12),
									"CuentaOptimus" 		NVARCHAR(12),
									"SaldoDebeHaber" 		NVARCHAR(1), 
									"Condicion" 			NVARCHAR(1), 
									"Monto" 				DECIMAL(20,4), 
									"Operacion"				NVARCHAR(1), 
									"Factor" 				DECIMAL(20,4), 
									"MayorACero" 			NVARCHAR(1), 
									"MenorACero" 			NVARCHAR(1), 
									"PermiteCero" 			NVARCHAR(1), 
									"Estado" 				NVARCHAR(3)
									 ),
    
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
			
				DECLARE v_ExisteItemConfiguracion 		INT;
				DECLARE v_ExisteConceptoConfiguracion 	INT;
				DECLARE SALIDA_CONTROLADA_DELETE CONDITION;
				DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_DELETE	
				BEGIN
					--salida/error/advertencia controlado por este servicio
					select count(1) into p_out_ErroresContador 
					from :p_out_ErroresDetalle;
					select count(1) into p_out_AdvertenciasContador 
					from :p_out_AdvertenciasDetalle;		 					 	
				END;
				--------------------
				--VALIDACIONES
				--------------------
				v_ExisteItemConfiguracion := 0;				
				v_ExisteConceptoConfiguracion := 0;
				/*SELECT COUNT(1)
				  INTO v_ExisteConceptoConfiguracion
				  FROM "BSCL.Normativa.BD::ILS.ConceptoConfiguracion" AS Concepto
				  INNER JOIN :p_tablaItemConfiguracion AS TabConcepto
					ON TabConcepto."IdItem" = Concepto."IdItem";
				
				IF :v_ExisteConceptoConfiguracion > 0 THEN
					
					p_out_ErroresDetalle = SELECT '' AS "Codigo"
					                             , 'Debe eliminar Id Item de "Concepto configuración".' AS "Descripcion"
					                        FROM DUMMY;
					SIGNAL SALIDA_CONTROLADA_DELETE;
				ELSE */	
				
				DELETE FROM "BSCL.Normativa.BD::ILS.ItemConfiguracion" AS ItemConf
				WHERE (ItemConf."IdConfiguracionItem",ItemConf."IdItem") 
				IN (SELECT tabItemConf."IdConfiguracionItem"
						 , tabItemConf."IdItem"
				    FROM :p_tablaItemConfiguracion AS tabItemConf);
				--END IF;
				
			END IF;	
			-----------------------------
			-- Modifica
			-----------------------------
			IF :p_TipoOperacion = 'M' THEN
				UPDATE ItemConfiguracion
				   SET ItemConfiguracion."MayorACero" = Tab."MayorACero"
				     , ItemConfiguracion."MenorACero" = Tab."MenorACero"
				     , ItemConfiguracion."PermiteCero" = Tab."PermiteCero"
   					 , ItemConfiguracion."IdProcesoFuncion" = Tab."IdProcesoFuncion"
   					 , ItemConfiguracion."CuentaContable" = Tab."CuentaContable"
   					 , ItemConfiguracion."CuentaOptimus" = Tab."CuentaOptimus"	
					 , ItemConfiguracion."SaldoDebeHaber" = Tab."SaldoDebeHaber"
					 , ItemConfiguracion."Condicion" = Tab."Condicion"		
					 , ItemConfiguracion."Monto" = Tab."Monto"			
					 , ItemConfiguracion."Operacion" = Tab."Operacion"			
					 , ItemConfiguracion."Factor" = Tab."Factor"			
				  FROM "BSCL.Normativa.BD::ILS.ItemConfiguracion" AS ItemConfiguracion
				  INNER JOIN :p_tablaItemConfiguracion AS Tab
						  ON ItemConfiguracion."IdItem" = Tab."IdItem"
					  	 AND ItemConfiguracion."IdConfiguracionItem" = Tab."IdConfiguracionItem";
			END IF;
			-----------------------------
			-- Inserta 
			-----------------------------
			IF :p_TipoOperacion = 'I' THEN 	
				/*
				DELETE FROM "BSCL.Normativa.BD::ILS.ItemConfiguracion" AS ItemConf
				WHERE ItemConf."IdItem" = (SELECT tabItemConf."IdItem"
				    FROM :p_tablaItemConfiguracion AS tabItemConf);
				*/    
				INSERT INTO "BSCL.Normativa.BD::ILS.ItemConfiguracion"
				(
					 "IdConfiguracionItem"
					,"IdItem" 			
					,"IdProcesoFuncion" 	
					,"CuentaContable"
					,"CuentaOptimus" 	
					,"SaldoDebeHaber" 	
					,"Condicion" 		
					,"Monto" 			
					,"Operacion"			
					,"Factor" 			
					,"MayorACero" 		
					,"MenorACero" 		
					,"PermiteCero" 		
					,"Estado" 			
				) 
				SELECT "BSCL.Normativa.BD.Secuencias::ILS.IdItemConfiguracion".NEXTVAL AS "IdConfiguracionItem"
					, "IdItem" 			
					, "IdProcesoFuncion" 	
					, "CuentaContable"
					, "CuentaOptimus" 	
					, "SaldoDebeHaber" 	
					, "Condicion" 		
					, "Monto" 			
					, "Operacion"			
					, "Factor" 			
					, "MayorACero" 		
					, "MenorACero" 		
					, "PermiteCero" 		
					, "Estado"
				FROM :p_tablaItemConfiguracion;				
			END IF;			
						
		END;		
	END;	
END;
