SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::ILS.ConceptoMantiene";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::ILS.ConceptoMantiene" (
	-- Parametros entrada comunes
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_IdUsuario			BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo			NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN p_UsaReglasNegocio	NVARCHAR(1),	--S/N se usa en caso de servicios anidados.
	IN p_TipoOperacion		NVARCHAR(1),
	-- Parametros entrada
	IN p_TablaConcepto		TABLE("IdConcepto"	BIGINT
								 ,"Nombre"		NVARCHAR(200)
								 ,"NombreCorto"	NVARCHAR(20)
							),
	
	-- Parametros salida del servicio
	OUT p_out_TablaConcepto	TABLE(
								  "IdConcepto"	BIGINT
								 ,"Nombre"		NVARCHAR(200)
								 ,"NombreCorto"	NVARCHAR(20)
							),
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador	INT,
	OUT p_out_AdvertenciasDetalle	TABLE ("Codigo" nvarchar(40)
										  ,"RequiereAutorizacion" nvarchar(40)
										  ,"Descripcion" nvarchar(5000)
									)DEFAULT EMPTY,	
	OUT p_out_ErroresContador		INT,
	OUT p_out_ErroresDetalle		TABLE ("Codigo" nvarchar(40)
	                                      ,"Descripcion" nvarchar(5000)
	                                )DEFAULT EMPTY
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
			p_out_ErroresDetalle = select * 
			                       from :p_out_ErroresDetalle 
			                       UNION ALL  
								   select v_errorExt
								        ,::SQL_ERROR_MESSAGE 
								   from dummy;				
			select count(1) 
			into p_out_ErroresContador 
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
				
				SELECT COUNT(1)
				  INTO v_ExisteConceptoConfiguracion
				FROM "BSCL.Normativa.BD::ILS.ConceptoGrupoConfiguracion" AS Concepto
				 	INNER JOIN :p_TablaConcepto AS TabConcepto
						 ON TabConcepto."IdConcepto" = Concepto."IdConcepto";	
			
				IF :v_ExisteConceptoConfiguracion > 0 THEN
					
					p_out_ErroresDetalle = SELECT '' AS "Codigo"
					                             , 'Debe eliminar Id Item de "Concepto configuración".' AS "Descripcion"
					                        FROM DUMMY;
					SIGNAL SALIDA_CONTROLADA_DELETE;
				ELSE 								
					DELETE FROM "BSCL.Normativa.BD::ILS.Concepto" 
					WHERE "IdConcepto" IN (SELECT "IdConcepto" FROM :p_TablaConcepto );
				END IF;
				
			END IF;	
       
			-----------------------------
			-- Inserta 
			-----------------------------
			IF :p_TipoOperacion = 'I' THEN 	
				
				DELETE FROM "BSCL.Normativa.BD::ILS.Concepto" 
				WHERE "IdConcepto" IN (SELECT "IdConcepto" FROM :p_TablaConcepto );
					
				v_aux_tablaConcepto = SELECT "BSCL.Normativa.BD.Secuencias::ILS.IdConcepto".NEXTVAL AS "IdConcepto"
										   , "Nombre"
										   , "NombreCorto"
										   , 'VIG' AS "Estado"
									  FROM :p_TablaConcepto;
					
				INSERT INTO "BSCL.Normativa.BD::ILS.Concepto"
				(
						"IdConcepto",
						"Nombre",
						"NombreCorto",
						"Estado"
				)
				SELECT "IdConcepto"
				   , "Nombre"
				   , "NombreCorto"
				   , "Estado"
				FROM :v_aux_tablaConcepto;
				
				p_out_TablaConcepto =
				SELECT "IdConcepto"
				   , "Nombre"
				   , "NombreCorto"
				FROM :v_aux_tablaConcepto;
								
			END IF;			
		
			-----------------------------
			-- Modifica 
			-----------------------------
			IF :p_TipoOperacion = 'M' THEN
				/*
				DECLARE v_ExisteConcepto INT:=0;
			
				SELECT	COUNT(1)
				  INTO	v_ExisteConcepto
				  FROM	"BSCL.Normativa.BD::ILS.Concepto" AS Concepto
				INNER JOIN :p_TablaConcepto AS TabConcepto
						ON TabConcepto."IdConcepto" = Concepto."IdConcepto";
		
				IF :v_ExisteConcepto > 0 THEN				
					DELETE FROM "BSCL.Normativa.BD::ILS.ConceptoConfiguracion" 
					WHERE "IdConcepto" IN (SELECT "IdConcepto" FROM :p_TablaConcepto );
				END IF;
				*/
				UPDATE Concepto
				SET "Nombre" 		= TabConcepto."Nombre"
				  , "NombreCorto"	= TabConcepto."NombreCorto"
				FROM "BSCL.Normativa.BD::ILS.Concepto" AS Concepto
				INNER JOIN :p_TablaConcepto AS TabConcepto
						ON TabConcepto."IdConcepto" = Concepto."IdConcepto";

			END IF;
			
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
								,'MANTENEDOR_CONCEPTO_ILS' AS "IdTipoRegistro"
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
								,'ConceptoMantiene' AS "NombreProceso"
						FROM DUMMY;
	
				v_LogDetalle =
			 			SELECT
			 				:p_IdLog AS "IdCabeceraLog"
							,CASE WHEN :p_TipoOperacion = 'E' THEN 'ELIMINACION'
								WHEN :p_TipoOperacion = 'M' THEN 'MODIFICACION'
								WHEN :p_TipoOperacion = 'I' THEN 'INGRESO'
								ELSE ''
							END AS "SubRegistro"
							,'Nombre Concepto' AS "CampoRegistro"
							,'' AS "ValorAntiguo"
							,detalleLog1."Nombre" AS "ValorNuevo"
			   			FROM :p_TablaConcepto as detalleLog1
			   			
			   			UNION ALL
			   			SELECT
							:p_IdLog AS "IdCabeceraLog"
							,CASE WHEN :p_TipoOperacion = 'E' THEN 'ELIMINACION'
								WHEN :p_TipoOperacion = 'M' THEN 'MODIFICACION'
								WHEN :p_TipoOperacion = 'I' THEN 'INGRESO'
								ELSE ''
							END AS "SubRegistro"
							,'Nombre Corto' AS "CampoRegistro"
							,'' AS "ValorAntiguo"
							,detalleLog2."NombreCorto" AS "ValorNuevo"
			   			FROM :p_TablaConcepto as detalleLog2;

				CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_LogDetalle);
			END;
			--LOG DE AUDITORIA (Fin)
		END;		
	END;	
END;