SET SCHEMA "BSCL";
go
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ReprocesarEventoCapital";
go
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ReprocesarEventoCapital" (


--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          				BIGINT 			default 0,
    IN p_IdUsuario          				BIGINT 			default 0,
    IN p_IdModulo           				NVARCHAR(3) 	default '',  
	
	IN p_IdEventoCapital					BIGINT     		default 0,
	IN p_IdTipoEventoCapital				NVARCHAR(2) 	default '',

-- Parametros de Salida
-- Salidas para mensajes de advertencias y errores

	OUT p_out_InformacionContador  	INT,
	OUT p_out_InformacionDetalle   	TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) default empty,

	OUT p_out_AdvertenciasContador  INT,
	OUT p_out_AdvertenciasDetalle	TABLE ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)) default empty,	
										
	OUT p_out_ErroresContador       INT,
	OUT p_out_ErroresDetalle        TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000)) default empty)
	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
	
BEGIN SEQUENTIAL EXECUTION
	--------------------
--DECLARE Manejo de Errores
	--------------------	
	DECLARE abort 						INT DEFAULT 0;	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta 				INT DEFAULT 0;  
	DECLARE v_errorhashmsg 				TABLE ("Codigo" NVARCHAR(40),"Valor" NVARCHAR(520));
	DECLARE v_erroroutmsg  				NVARCHAR(520);	        	
	DECLARE v_errorcliente 				INT DEFAULT 0;  
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
--salida/error/advertencia controlado por este servicio
		SELECT COUNT(1) INTO p_out_ErroresContador 
		FROM :p_out_ErroresDetalle;
		SELECT COUNT(1) INTO p_out_AdvertenciasContador 
		FROM :p_out_AdvertenciasDetalle;		 					 	
	END;


	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DECLARE v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP

			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT COUNT(1) INTO p_out_ErroresContador 
			FROM :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log sera captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;

	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= SELECT '' AS "Codigo",'' AS "RequiereAutorizacion",'' AS "Descripcion" FROM dummy WHERE 1=2;
	p_out_ErroresDetalle		= SELECT '' AS "Codigo",'' AS "Descripcion" 	FROM dummy WHERE 1=2;
	
	BEGIN
	
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
		DECLARE v_ResultadoRegla        NVARCHAR(40);

--variables de proceso
		DECLARE v_FechaSistema 			TIMESTAMP;
		DECLARE v_RowCount				BIGINT := 0;
		DECLARE v_Factor				DECIMAL(26,8) 	DEFAULT 0;
		DECLARE v_FactorNuevo			DECIMAL(26,8) 	DEFAULT 0;
		DECLARE v_FactorMayor			NVARCHAR		DEFAULT 'S';
		DECLARE v_FechaPago				DATE;
		
		
		p_out_TblId = SELECT 0 as "Ids" FROM dummy WHERE 1=2;

-- Campos segun linea de Negocio (no se obtienen desde la tabla movimiento en transito)
		p_TblErrores 	= SELECT '' as "Codigo", '' as "Descripcion" FROM DUMMY WHERE 1=2;
		v_TablaErrores 	= SELECT '' as "Codigo", '' as "Descripcion" FROM DUMMY WHERE 1=2;
		   
	    CALL  "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (p_IdEmpresa,v_FechaSistema);
	             
		-----------------------------------------------------
		--Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		BEGIN
			DECLARE v_resultadoRegla nvarchar(40);

			--Si existen errores en las validaciones debe salir
			SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle; 
			IF :p_out_ErroresContador > 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;				
		END;
		-----------------------------------------------------
		--fin Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------

		-----------------------------------------------------
		-- Proceso de Negocio
		---------------------------------------------------
		BEGIN

		-----------------------------------------------------
		-- Obtiene Factores y FechaPago
		---------------------------------------------------
		
			SELECT "Factor", "FactorNuevo", "FechaPago" INTO  v_Factor, v_FactorNuevo, v_FechaPago
			FROM 
				"BSCL.Custodia.BD::CUS.EventoCapital"
			WHERE  
					"IdTipoEventoCapital"	= P_IdTipoEventoCapital	AND
					"IdEventoCapital" 		= p_IdEventoCapital;
					
					
			IF v_Factor > v_FactorNuevo THEN
				v_FactorMayor 	:= 'N';			
			END IF;

		-----------------------------------------------------
		-- Actualiza clientes
		---------------------------------------------------

		-----------------------------------------------------
		-- Dividendo en acciones
		---------------------------------------------------
			

			IF 	P_IdTipoEventoCapital = 'DA' THEN
				UPDATE "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
				SET
					"CantidadNueva"	= 	CASE "IdTipoEventoCapital"										 
											WHEN 'DA' THEN 
											CASE WHEN "CantidadNueva" > 0 THEN "CantidadDerecho" * v_Factor
												ELSE ("CantidadDerecho" * v_Factor) * -1
											END
										END,
					"FechaPago" = v_FechaPago
				WHERE  
					"IdTipoEventoCapital"	= P_IdTipoEventoCapital	AND
					"IdEventoCapital" 		= p_IdEventoCapital;
			END IF;
			

		-----------------------------------------------------
		-- Dividendo en dinero
		---------------------------------------------------

			IF 	P_IdTipoEventoCapital = 'DV' THEN
				UPDATE "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
				SET
					"Total"			= 	CASE "IdTipoEventoCapital"										 
											WHEN 'DV' THEN 
											CASE WHEN "Total" > 0 THEN "CantidadDerecho" * v_Factor
												ELSE ("CantidadDerecho" * v_Factor) * -1
											END
										END,
					"FechaPago" = v_FechaPago				
				WHERE  
					"IdTipoEventoCapital"	= P_IdTipoEventoCapital	AND
					"IdEventoCapital" 		= p_IdEventoCapital;
			END IF;
			
		-----------------------------------------------------
		-- Canjes
		---------------------------------------------------
			
			IF 	P_IdTipoEventoCapital = 'CA' THEN
				UPDATE "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
				SET				
					"CantidadNueva"	=	CASE v_FactorMayor
											WHEN 'N' THEN 	("CantidadDerecho" / v_Factor)
											ELSE 		  	("CantidadDerecho" * v_FactorNuevo)
										END,
										
					"PrecioNuevo"	= CASE v_FactorMayor
											WHEN 'N' THEN 	("Precio"  * v_Factor)
											ELSE 			("Precio"  / v_FactorNuevo)
										END,
				
					"Total"			= 	CASE v_FactorMayor
											WHEN 'N' THEN 	("CantidadDerecho" / v_Factor) 		* ("Precio" * v_Factor)
											ELSE 			("CantidadDerecho" * v_FactorNuevo) * ("Precio" / v_FactorNuevo)
										END,
					"FechaPago" = v_FechaPago
				WHERE  
					"IdTipoEventoCapital"	= P_IdTipoEventoCapital	AND
					"IdEventoCapital" 		= p_IdEventoCapital;
			END IF;

		-----------------------------------------------------
		-- Division de Sociedad
		---------------------------------------------------
			
			IF 	P_IdTipoEventoCapital = 'DS' THEN
				UPDATE "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
				SET				
					"CantidadNueva"	=	("CantidadDerecho" * v_FactorNuevo),
										
					"PrecioNuevo"	= 	("Precio" * v_FactorNuevo),
				
					"Total"			= 	("CantidadDerecho" * v_FactorNuevo) * ("Precio" * v_FactorNuevo),
					"FechaPago" = v_FechaPago
				WHERE  
					"IdTipoEventoCapital"	= P_IdTipoEventoCapital	AND
					"IdEventoCapital" 		= p_IdEventoCapital;
			END IF;			


		-----------------------------------------------------
		-- Actualiza Estado Evento Capital 
		---------------------------------------------------
				
			UPDATE "BSCL.Custodia.BD::CUS.EventoCapital"
			SET "IdEstado"				= 'PRO'
			WHERE  
				"IdTipoEventoCapital"	= P_IdTipoEventoCapital	AND
				"IdEventoCapital" 		= p_IdEventoCapital;	
				
				

				v_TablaCodigosMensajes =	Select 	p_IdEventoCapital 					As "IdInterno", 
														'UI_OPERACION_PROCESAR_EXITO'	As "Codigo",
														'' 								As "CodigoDato1", 
														'' 								as "ValorDato1",
														'' 								As "CodigoDato2", 
														'' 								As "ValorDato2" 
												From dummy;
					                        
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_out_hash);
				
				p_out_InformacionContador = 1;
				p_out_InformacionDetalle = 	Select "Codigo", "Descripcion" 
											From :v_out_hash;	
	
		    IF p_out_ErroresContador > 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;
    
	END;
		-----------------------------------------------------
		--fin proceso de Negocio
		---------------------------------------------------
	END;
END; 


go