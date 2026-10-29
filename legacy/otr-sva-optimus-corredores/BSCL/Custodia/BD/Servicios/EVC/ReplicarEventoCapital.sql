SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ReplicarEventoCapital";
GO
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ReplicarEventoCapital" (

--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          				BIGINT 			default 0,
    IN p_IdUsuario          				BIGINT 			default 0,
    IN p_IdModulo           				NVARCHAR(3) 	default '',  
    IN p_IdPais								BIGINT			default 0,
	
	IN p_IdEventoCapital					BIGINT     		default 0,
	IN p_IdTipoEventoCapital				NVARCHAR(2) 	default '',

-- Parametros de Salida
	OUT p_out_AdvertenciasContador 			INT,
	OUT p_out_ErroresContador 				INT)   
	 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
	
BEGIN 
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE abort int default 0;	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		select count(1) into p_out_ErroresContador 
		from "BSCL.Custodia.BD::GBL.ReplicarEventoCapital_OUT_ErroresDetalle";
		select count(1) into p_out_AdvertenciasContador 
		from "BSCL.Custodia.BD::GBL.ReplicarEventoCapital_OUT_AdvertenciasDetalle";
	END;


	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			INSERT INTO "BSCL.Custodia.BD::GBL.CerrarEventoCapitalSorteoLetras_Salida_ErroresDetalle"
				SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM DUMMY;				
				SELECT COUNT(1) INTO p_out_ErroresContador 
				FROM "BSCL.Custodia.BD::GBL.ReplicarEventoCapital_OUT_ErroresDetalle";
		ELSE
			--Error interno no manejado  (El log sera captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;

	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	BEGIN

		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
		DECLARE v_out_REF_ID			BIGINT;
		
		--variables de proceso
		DECLARE v_FechaSistema 			NVARCHAR(10);
		DECLARE v_Secuencia				BIGINT := 0;
		
		p_out_TblId = SELECT 0 as "Ids" from dummy where 1=2;

		-- Campos segun linea de Negocio (no se obtienen desde la tabla movimiento en transito)
		p_TblErrores = SELECT '' as "Codigo", '' as "Descripcion" FROM DUMMY WHERE 1=2;
		v_TablaErrores = SELECT '' as "Codigo", '' as "Descripcion" FROM DUMMY WHERE 1=2;
		   
	    CALL  "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (p_IdEmpresa,v_FechaSistema);
	             
		-----------------------------------------------------
		--Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		BEGIN
			-- Se valida que el evento capital debe encontrarse "PRO" para poder realizar el cierre.
			
			SELECT count(1) INTO p_out_ErroresContador
		    FROM "BSCL.Custodia.BD::CUS.EventoCapital"
			WHERE "IdEventoCapital" = p_IdEventoCapital
			  and "IdEstado" != 'PRO' AND "IdEstado" != 'PEN';
			  
			IF (p_out_ErroresContador > 0) THEN

	           	v_TablaCodigosMensajes =	Select 	p_IdEventoCapital 									As "IdInterno", 
												'UI_EVENTO_DEBE_ESTAR_PROCESADO_PARA_REPLICAR'			As "Codigo",
												'' 														As "CodigoDato1", 
												'' 														as "ValorDato1",
												''			 											As "CodigoDato2", 
												''														As "ValorDato2" 
										From dummy;
			                        
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_out_hash);
				
				INSERT INTO "BSCL.Custodia.BD::GBL.ReplicarEventoCapital_OUT_ErroresDetalle"
				("Codigo","Descripcion")
				SELECT "Codigo", "Descripcion"  FROM :v_out_hash;
				
		 	END IF;
		 	
			--Si existen errores en las validaciones debe salir
			SELECT COUNT(1) INTO p_out_ErroresContador FROM "BSCL.Custodia.BD::GBL.ReplicarEventoCapital_OUT_ErroresDetalle"; 
			
			IF :p_out_ErroresContador > 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;				
		END;
		-----------------------------------------------------
		--fin Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------

		-----------------------------------------------------
		--proceso de Negocio
		---------------------------------------------------
		BEGIN

			--********************************
			--* REPLICAR DIVIDENDO EN DINERO *
			--********************************
			IF :p_IdTipoEventoCapital = 'DV' THEN
			
				UPDATE "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
				SET 	"CantidadDerecho"			= 	CASE 
															WHEN "CantidadDerechoArchivo" = 0 THEN "CantidadDerecho" 
															ELSE "CantidadDerechoArchivo"
														END,
						"Total"						= 	CASE
															WHEN "MontoBeneficioArchivo" = 0 THEN "Total"
															ELSE "MontoBeneficioArchivo"
														END,
						"DiferenciaCantidadDerecho"	= 0,
						"DiferenciaMontoBeneficio"	= 0
				WHERE "IdEventoCapital" 	= :p_IdEventoCapital
				  AND "IdTipoEventoCapital" = :p_IdTipoEventoCapital;
				
			END IF;

			--**********************************
			--* REPLICAR DIVIDENDO EN ACCIONES *
			--**********************************
			IF :p_IdTipoEventoCapital = 'DA' THEN
			
				UPDATE "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
				SET 	"CantidadDerecho"					= CASE 
																WHEN "CantidadDerechoArchivo" = 0 THEN "CantidadDerecho"
																ELSE "CantidadDerechoArchivo" 
															  END,
						"CantidadNueva"						= CASE 
																WHEN "AccionesDisponiblesArchivo" = 0 THEN "CantidadDerecho"
																ELSE "AccionesDisponiblesArchivo" 
															  END,
						"DiferenciaCantidadDerecho"			= 0,
						"DiferenciaAccionesCantidadNueva"	= 0
				WHERE "IdEventoCapital" 	= :p_IdEventoCapital
				  AND "IdTipoEventoCapital" = :p_IdTipoEventoCapital;

			END IF;
			
			--*******************************************************************************************************************************
			--* REPLICAR CUPONES																											*
			--******************																											*
			--* ESTO NO ESTA TERMINADO DEBIDO A QUE NO HAY AUN RESPUESTA DESDE PERÚ RESPECTO AL ARCHIVO DE CARGA, POR QUE ESTE ARCHIVO		*
			--* TIENE DISTINTOS FORMATOS PARA LOS NUMÉRICOS																					*
			--*******************************************************************************************************************************
/*			IF :p_IdTipoEventoCapital = 'DA' THEN
			
				UPDATE "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
				SET 	"CantidadDerecho"					= "CantidadDerechoArchivo", 
						"CantidadNueva"						= "AccionesDisponiblesArchivo",
						"DiferenciaCantidadDerecho"			= 0,
						"DiferenciaAccionesCantidadNueva"	= 0
				WHERE "IdEventoCapital" 	= :p_IdEventoCapital
				  AND "IdTipoEventoCapital" = :p_IdTipoEventoCapital

			END IF;*/

		END;
		-----------------------------------------------------
		--fin proceso de Negocio
		---------------------------------------------------
	END;
END;
