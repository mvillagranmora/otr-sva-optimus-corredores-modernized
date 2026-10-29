SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::ILS.ConceptoGrupoConfiguracionMantiene";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::ILS.ConceptoGrupoConfiguracionMantiene" (
	-- Parametros entrada comunes
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_IdUsuario			BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo			NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN p_UsaReglasNegocio	NVARCHAR(1),	--S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada
	IN p_TipoOperacion		CHAR(01),
	IN p_tablaConceptoConfiguracion TABLE(
									 "IdConceptoGrupo" 	BIGINT,
									 "IdConcepto" 		BIGINT,
									 "NombreConceptoGrupo" 	NVARCHAR(500),
									 "NombreCortoConceptoGrupo" NVARCHAR(20),
									 "Factor"			DECIMAL(20,4)
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
        DECLARE varExiste	INT;
		DECLARE v_IdConceptoGrupo			BIGINT;
		DECLARE v_IdConcepto				BIGINT;
		DECLARE v_NombreConceptoGrupo		NVARCHAR(500);
		DECLARE v_NombreCortoConceptoGrupo	NVARCHAR(20);
		DECLARE v_Factor					DECIMAL(20,4);

		
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
			
				v_ExisteConceptoConfiguracion := 0;	
				SELECT COUNT(1)
			  	INTO v_ExisteConceptoConfiguracion
			  	FROM "BSCL.Normativa.BD::ILS.ConceptoConfiguracion" AS Concepto
			 		INNER JOIN :p_tablaConceptoConfiguracion AS TabConcepto ON TabConcepto."IdConceptoGrupo" = Concepto."IdConceptoGrupo" 
			 			AND  TabConcepto."IdConcepto" = Concepto."IdConcepto";	
				
			 	IF :v_ExisteConceptoConfiguracion > 0 THEN
					
					p_out_ErroresDetalle = SELECT '' AS "Codigo"
					                             , 'Debe eliminar Id Item de "Concepto configuraci�n".' AS "Descripcion"
					                        FROM DUMMY;
					SIGNAL SALIDA_CONTROLADA_DELETE;
				ELSE 								
					DELETE 
					FROM "BSCL.Normativa.BD::ILS.ConceptoGrupoConfiguracion" AS ConceptoConf
					WHERE (ConceptoConf."IdConceptoGrupo", ConceptoConf."IdConcepto") 
						IN (SELECT tabConceptoConf."IdConceptoGrupo", tabConceptoConf."IdConcepto" 
				    		FROM :p_tablaConceptoConfiguracion AS tabConceptoConf);
				END IF;
				
				
			END IF;	
			-----------------------------
			-- Modifica 
			-----------------------------
			IF :p_TipoOperacion = 'M' THEN 	
			
				SELECT 
					"IdConceptoGrupo", "IdConcepto","NombreConceptoGrupo", "NombreCortoConceptoGrupo","Factor"	INTO 
					v_IdConceptoGrupo, v_IdConcepto,v_NombreConceptoGrupo,v_NombreCortoConceptoGrupo, v_Factor 
				FROM :p_tablaConceptoConfiguracion;	
				
				SELECT COUNT (*)INTO varExiste
				FROM "BSCL.Normativa.BD::ILS.ConceptoGrupoConfiguracion" AS ConceptoConf
				WHERE 
					ConceptoConf."IdConceptoGrupo"	= :v_IdConceptoGrupo	AND 
					ConceptoConf."IdConcepto"		= :v_IdConcepto;
				    
					IF (varExiste > 0)THEN    
					    
						UPDATE "BSCL.Normativa.BD::ILS.ConceptoGrupoConfiguracion" 	AS ConceptoConf 
						SET 
							ConceptoConf."Nombre" 		=	v_NombreConceptoGrupo, 
							ConceptoConf."NombreCorto"	=	v_NombreCortoConceptoGrupo, 
							ConceptoConf."Factor"		=	v_Factor
						
						WHERE 
							ConceptoConf."IdConceptoGrupo"	= :v_IdConceptoGrupo	AND 
							ConceptoConf."IdConcepto"		= :v_IdConcepto;

						    
					ELSE
						p_out_ErroresContador	:= 1;
						p_out_ErroresDetalle = SELECT '1' AS "Codigo"
													 ,'No existe grupo seleccionado' AS "Descripcion"
													FROM DUMMY;
										
				END IF;	
			END IF;			
			
			-----------------------------
			-- Inserta 
			-----------------------------
			
			IF :p_TipoOperacion = 'I' THEN 	
				
				SELECT COUNT (*)INTO varExiste
				FROM "BSCL.Normativa.BD::ILS.ConceptoGrupoConfiguracion" AS ConceptoConf
				WHERE (ConceptoConf."IdConcepto", ConceptoConf."Nombre", ConceptoConf."NombreCorto") 
				IN (SELECT tabConceptoConf."IdConcepto"
					     , tabConceptoConf."NombreConceptoGrupo"
						 , tabConceptoConf."NombreCortoConceptoGrupo"
					    FROM :p_tablaConceptoConfiguracion AS tabConceptoConf);
					    
						IF (varExiste = 0)THEN    
						    
							DELETE FROM "BSCL.Normativa.BD::ILS.ConceptoGrupoConfiguracion" AS ConceptoConf
							WHERE (ConceptoConf."IdConceptoGrupo", ConceptoConf."IdConcepto") 
							IN (SELECT tabConceptoConf."IdConceptoGrupo" 
								     , tabConceptoConf."IdConcepto" 
								     
							    FROM :p_tablaConceptoConfiguracion AS tabConceptoConf);
						
							INSERT INTO "BSCL.Normativa.BD::ILS.ConceptoGrupoConfiguracion"
							(
								  "IdConceptoGrupo"
								, "IdConcepto"
								, "Nombre"
								, "NombreCorto"
								, "Factor"
							) 
							SELECT "BSCL.Normativa.BD.Secuencias::ILS.IdConceptoGrupo".NEXTVAL 
								 , "IdConcepto"
								 , "NombreConceptoGrupo"
							     , "NombreCortoConceptoGrupo"
							     , "Factor"
							FROM :p_tablaConceptoConfiguracion;
						ELSE
							p_out_ErroresContador	:= 1;
							p_out_ErroresDetalle = SELECT '1' AS "Codigo"
														 ,'El grupo seleccionado ya existe' AS "Descripcion"
														FROM DUMMY;
											
					END IF;	
			END IF;
			
			IF :p_out_ErroresContador = 0 THEN
				--LOG DE AUDITORIA (Inicio)
				BEGIN
					DECLARE p_IdLog BIGINT;
					DECLARE v_fechaHoraSistema NVARCHAR(40);
		
					SELECT "BSCL.Plataforma.BD.Secuencias::Log".NEXTVAL INTO p_IdLog FROM dummy;
		
					call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
						p_IdEmpresa => p_IdEmpresa,
						p_out_FechaHoraSistema => v_fechaHoraSistema
					);
		
					v_log = SELECT :p_IdLog AS "IdCabeceraLog"
									,CASE WHEN :p_TipoOperacion = 'E' THEN 'ELIMINACION'
										WHEN :p_TipoOperacion = 'M' THEN 'MODIFICACION'
										WHEN :p_TipoOperacion = 'I' THEN 'INGRESO'
										ELSE ''
									END AS "IdTipoLog"
									,:p_IdModulo AS "IdModulo"
									,:p_IdUsuario AS "IdUsuario"
									,:p_IdEmpresa AS "IdEmpresa"
									,TO_VARCHAR (COALESCE(:v_fechaHoraSistema, '0001-01-01'), 'YYYY-MM-DD') as "FechaSistemaLog"
									,:p_IdUsuario AS "IdUsuarioAtendido"
									,0  AS "IdPersona"
									,0  AS "IdCliente"
									,'' AS "EstadoCliente"
									,'' AS "NombreCliente"
									,'' AS "IdTipoDocumentoIdentidad"
									,'' AS "IdDocumentoIdentidad"
									,0  AS "IdPortafolio"
									,'' AS "EstadoPortafolio"
									,'MANTENEDOR_CONCEPTO_ILS_GRUPO_CONCEPTO' AS "IdTipoRegistro"
									,'' AS "IdRegistro"
									,'' AS "EstadoRegistro"
									,'' AS "IdTipoOperacion"
									,0 AS "Folio"
									,0 AS "Cantidad"
									,0 AS "PrecioTasa"
									,0 AS "Monto"
									,'' AS "IdMonedaOrigen"
									,'' AS "IdMonedaLiquidacion"
									,'' AS "TipoCambio"
									,'' AS "Nemo"
									,0  AS "IdProceso" 
									,'' AS "EstadoProceso"
									,'ConceptoGrupoConfiguracionMantiene' AS "NombreProceso"
							FROM DUMMY;
		
					v_LogDetalle =
				 			SELECT
				 				:p_IdLog AS "IdCabeceraLog"
								,CASE WHEN :p_TipoOperacion = 'E' THEN 'ELIMINACION'
									WHEN :p_TipoOperacion = 'M' THEN 'MODIFICACION'
									WHEN :p_TipoOperacion = 'I' THEN 'INGRESO'
									ELSE ''
								END AS "SubRegistro"
								,'Nombre Concepto Grupo' AS "CampoRegistro"
								,'' AS "ValorAntiguo"
								,detalleLog1."NombreConceptoGrupo" AS "ValorNuevo"
				   			FROM :p_tablaConceptoConfiguracion as detalleLog1
				   			
				   			UNION ALL
				   			SELECT
								:p_IdLog AS "IdCabeceraLog"
								,CASE WHEN :p_TipoOperacion = 'E' THEN 'ELIMINACION'
									WHEN :p_TipoOperacion = 'M' THEN 'MODIFICACION'
									WHEN :p_TipoOperacion = 'I' THEN 'INGRESO'
									ELSE ''
								END AS "SubRegistro"
								,'Nombre Corto Concepto Grupo' AS "CampoRegistro"
								,'' AS "ValorAntiguo"
								,detalleLog2."NombreCortoConceptoGrupo" AS "ValorNuevo"
				   			FROM :p_tablaConceptoConfiguracion as detalleLog2
				   			
							UNION ALL
							SELECT
								:p_IdLog AS "IdCabeceraLog"
								,CASE WHEN :p_TipoOperacion = 'E' THEN 'ELIMINACION'
									WHEN :p_TipoOperacion = 'M' THEN 'MODIFICACION'
									WHEN :p_TipoOperacion = 'I' THEN 'INGRESO'
								ELSE ''
								END AS "SubRegistro"
								,'Factor' AS "CampoRegistro"
								,'' AS "ValorAntiguo"
								,COALESCE(TO_VARCHAR(detalleLog3."Factor"), '') AS "ValorNuevo"
							FROM :p_tablaConceptoConfiguracion as detalleLog3
							
							UNION ALL
							SELECT
								:p_IdLog AS "IdCabeceraLog"
								,CASE WHEN :p_TipoOperacion = 'E' THEN 'ELIMINACION'
									WHEN :p_TipoOperacion = 'M' THEN 'MODIFICACION'
									WHEN :p_TipoOperacion = 'I' THEN 'INGRESO'
								ELSE ''
								END AS "SubRegistro"
								,'IdConcepto' AS "CampoRegistro"
								,'' AS "ValorAntiguo"
								,COALESCE(TO_VARCHAR(detalleLog4."IdConcepto"), '') AS "ValorNuevo"
							FROM :p_tablaConceptoConfiguracion as detalleLog4;
							
					CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_LogDetalle);
				END;
				--LOG DE AUDITORIA (Fin)
			END IF;			
		END;		
	END;	
END;