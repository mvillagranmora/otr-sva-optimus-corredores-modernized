SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::ILS.ConceptoConfiguracionMantiene";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::ILS.ConceptoConfiguracionMantiene" (
	-- Parametros entrada comunes
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_IdUsuario			BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo			NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN p_UsaReglasNegocio	NVARCHAR(1),	--S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada
	IN p_TipoOperacion		CHAR(01),
	IN p_tablaConceptoConfiguracion TABLE(
									 "IdConcepto" 	BIGINT,
									 "IdConceptoGrupo" 	BIGINT,
									 "IdItem" 		BIGINT,
									 "Operacion" 	NVARCHAR(1),
									 "Factor" 		DECIMAL(20,4)
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
			
				DELETE FROM "BSCL.Normativa.BD::ILS.ConceptoConfiguracion" AS ConceptoConf
				WHERE (ConceptoConf."IdConcepto", ConceptoConf."IdConceptoGrupo", ConceptoConf."IdItem", ConceptoConf."Operacion", ConceptoConf."Factor") 
				IN (SELECT tabConceptoConf."IdConcepto" 
						 , tabConceptoConf."IdConceptoGrupo"
					     , tabConceptoConf."IdItem" 
					     , tabConceptoConf."Operacion"
					     , tabConceptoConf."Factor"
				    FROM :p_tablaConceptoConfiguracion AS tabConceptoConf);
				
			END IF;	
			
			-----------------------------
			-- Inserta 
			-----------------------------
			
			IF :p_TipoOperacion = 'I' THEN 	
				
				SELECT COUNT (*)INTO varExiste
				FROM "BSCL.Normativa.BD::ILS.ConceptoConfiguracion" AS ConceptoConf
				WHERE (ConceptoConf."IdConcepto", ConceptoConf."IdConceptoGrupo", ConceptoConf."IdItem") 
				IN (SELECT tabConceptoConf."IdConcepto"
						 , tabConceptoConf."IdConceptoGrupo"
					     , tabConceptoConf."IdItem" 
					    FROM :p_tablaConceptoConfiguracion AS tabConceptoConf);
					    
						IF (varExiste = 0)THEN    
						    
							DELETE FROM "BSCL.Normativa.BD::ILS.ConceptoConfiguracion" AS ConceptoConf
							WHERE (ConceptoConf."IdConcepto", ConceptoConf."IdConceptoGrupo", ConceptoConf."IdItem", ConceptoConf."Operacion", ConceptoConf."Factor") 
							IN (SELECT tabConceptoConf."IdConcepto"
									 ,tabConceptoConf."IdConceptoGrupo" 
								     , tabConceptoConf."IdItem" 
								     , tabConceptoConf."Operacion"
								     , tabConceptoConf."Factor"
							    FROM :p_tablaConceptoConfiguracion AS tabConceptoConf);
						
							INSERT INTO "BSCL.Normativa.BD::ILS.ConceptoConfiguracion"
							(	  "IdConcepto"
								, "IdConceptoGrupo"
								, "IdItem"
								, "Operacion"
								, "Factor"
							) 
							SELECT "IdConcepto"
								 , "IdConceptoGrupo" 
								 , "IdItem" 
								 , "Operacion"
								 , "Factor"
							FROM :p_tablaConceptoConfiguracion;
						ELSE
							p_out_ErroresContador	:= 1;
							p_out_ErroresDetalle = SELECT '1' AS "Codigo"
														 ,'El concepto seleccionado ya existe' AS "Descripcion"
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
									,'MANTENEDOR_CONCEPTO_ILS_ITEM' AS "IdTipoRegistro"
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
									WHEN :p_TipoOperacion = 'I' THEN 'INGRESO'
									ELSE ''
								END AS "SubRegistro"
								,'Operacion' AS "CampoRegistro"
								,'' AS "ValorAntiguo"
								,detalleLog1."Operacion" AS "ValorNuevo"
				   			FROM :p_tablaConceptoConfiguracion as detalleLog1
				   			
				   			UNION ALL
				   			SELECT
								:p_IdLog AS "IdCabeceraLog"
								,CASE WHEN :p_TipoOperacion = 'E' THEN 'ELIMINACION'
									WHEN :p_TipoOperacion = 'I' THEN 'INGRESO'
									ELSE ''
								END AS "SubRegistro"
								,'IdConcepto' AS "CampoRegistro"
								,'' AS "ValorAntiguo"
								,COALESCE(TO_VARCHAR(detalleLog2."IdConcepto"), '') AS "ValorNuevo"
				   			FROM :p_tablaConceptoConfiguracion as detalleLog2
				   			
							UNION ALL
							SELECT
								:p_IdLog AS "IdCabeceraLog"
								,CASE WHEN :p_TipoOperacion = 'E' THEN 'ELIMINACION'
									WHEN :p_TipoOperacion = 'I' THEN 'INGRESO'
								ELSE ''
								END AS "SubRegistro"
								,'IdConceptoGrupo' AS "CampoRegistro"
								,'' AS "ValorAntiguo"
								,COALESCE(TO_VARCHAR(detalleLog3."IdConceptoGrupo"), '') AS "ValorNuevo"
							FROM :p_tablaConceptoConfiguracion as detalleLog3
							
							UNION ALL
							SELECT
								:p_IdLog AS "IdCabeceraLog"
								,CASE WHEN :p_TipoOperacion = 'E' THEN 'ELIMINACION'
									WHEN :p_TipoOperacion = 'I' THEN 'INGRESO'
								ELSE ''
								END AS "SubRegistro"
								,'IdItem' AS "CampoRegistro"
								,'' AS "ValorAntiguo"
								,COALESCE(TO_VARCHAR(detalleLog4."IdItem"), '') AS "ValorNuevo"
							FROM :p_tablaConceptoConfiguracion as detalleLog4;
							
					CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_LogDetalle);
				END;
				--LOG DE AUDITORIA (Fin)
			END IF;	
		END;		
	END;	
END;