SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::CTT.ModificarContratoDatosGenerales";

CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::CTT.ModificarContratoDatosGenerales" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						BIGINT,			--ID Empresa destino
	IN  p_IdUsuario						BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						NVARCHAR(3),  		--ID del Modulo que lo invoca	
	IN  p_SimularOperacion				NVARCHAR(1) DEFAULT 'N',	-- S/N se usa para simular operacion (algunos casos)
	IN  p_UsaReglasNegocio	 			NVARCHAR(1) DEFAULT 'N',	-- S/N se usa en caso de servicios anidados.
    	
    IN p_RazonSocial					NVARCHAR(200),
    IN p_RUT							NVARCHAR(40),
    IN p_Direccion						NVARCHAR(200),
    IN p_Telefono						NVARCHAR(20),
    IN p_Email							NVARCHAR(100),
    IN p_Fax							NVARCHAR(20),    
    IN p_Apoderado1						NVARCHAR(1000),
    IN p_DocumentoApoderado1			NVARCHAR(1000),
    IN p_Apoderado2						NVARCHAR(1000),
    IN p_DocumentoApoderado2			NVARCHAR(1000),
    IN p_IdPersonaApoderado1			BIGINT DEFAULT 0,
    IN p_IdPersonaApoderado2			BIGINT DEFAULT 0,

	-- Salidas para mensajes de advertencias y errores
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
 
	DECLARE v_errorhashmsgtable TABLE ("Codigo" NVARCHAR(40),"Valor" NVARCHAR(520));
	DECLARE v_erroroutmsg 		NVARCHAR(520);
	--DECLARE v_SecuenciaEvento	BIGINT;	  
	DECLARE v_out_Resultado 	NVARCHAR(1);
    DECLARE v_out_Mensaje 		NVARCHAR(520);
	
	DECLARE v_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));

	DECLARE SALIDA_CONTROLADA CONDITION; 	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT COUNT(1) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;		 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;										
		ELSE
			p_out_ErroresDetalle = SELECT '' AS "Codigo", ::SQL_ERROR_MESSAGE AS "Descripcion" FROM dummy;
			--Error INTerno no manejado  (El log ser� captado por la capa de XS control)		
			--RESIGNAL; v_MensajeError
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
		DECLARE v_count_datosentrada BIGINT;
		DECLARE v_count_empresa BIGINT;
		DECLARE v_fechaHoraSistema NVARCHAR(40);
		
		
		-------------------------
		--Validaciones basicas 
		-------------------------
		

		--Si existen errores en las validaciones debe salir
		---------------------------------------------------
	
		------------------------
		--Validaciones via Motor (solo complejas)
		------------------------		
		--
	
		--------------------
		--Reglas Negocio 
		--------------------		
		--
					
		-------------------- 
		--Negocio Aqui
		--------------------
		BEGIN
			
			SELECT COUNT(1) into v_count_empresa 
			FROM "BSCL.Plataforma.BD::CTT.ContratoDatosGenerales"
			WHERE "IdEmpresa" = p_IdEmpresa;
			
			IF(v_count_empresa = 0) THEN --IdEmpresa no existe, se debe insertar 
				
				INSERT INTO "BSCL.Plataforma.BD::CTT.ContratoDatosGenerales"
			    ("IdEmpresa","RazonSocial","RUT","Direccion","Telefono","Email","Fax","Apoderado1","DocumentoApoderado1","Apoderado2","DocumentoApoderado2","IdPersonaApoderado1","IdPersonaApoderado2") VALUES
				(p_IdEmpresa, p_RazonSocial, p_RUT, p_Direccion, p_Telefono, p_Email, p_Fax, p_Apoderado1, p_DocumentoApoderado1, p_Apoderado2, p_DocumentoApoderado2, p_IdPersonaApoderado1, p_IdPersonaApoderado2);
			
			ELSE --Empresa existe, se debe actualizar la info
				
				p_DetalleLog = 
				SELECT 	L."SubRegistro",
				L."CampoRegistro",
				CASE L."CampoRegistro"  WHEN 'RUT' THEN CDG."RUT"
										WHEN 'RazonSocial' THEN CDG."RazonSocial"
										WHEN 'Direccion' THEN CDG."Direccion"
										WHEN 'Telefono' THEN CDG."Telefono"
										WHEN 'Email' 	THEN CDG."Email"
										WHEN 'Fax'		THEN CDG."Fax"
										WHEN 'Apoderado1'	THEN CDG."Apoderado1"
										WHEN 'DocumentoApoderado1'	THEN CDG."DocumentoApoderado1"
										WHEN 'Apoderado2'	THEN CDG."Apoderado2"
										WHEN 'DocumentoApoderado2'	THEN CDG."DocumentoApoderado2" 
										WHEN 'IdPersonaApoderado1' THEN CAST(CDG."IdPersonaApoderado1" AS NVARCHAR(500)) 
										WHEN 'IdPersonaApoderado2' THEN CAST(CDG."IdPersonaApoderado2" AS NVARCHAR(500)) END AS "ValorAntiguo",
				L."ValorNuevo"	
				FROM	
				(Select p_IdEmpresa	as "SubRegistro", 'RazonSocial' as "CampoRegistro", p_RazonSocial as "ValorNuevo" FROM DUMMY
				UNION ALL
				Select p_IdEmpresa	as "SubRegistro", 'RUT' as "CampoRegistro",  p_RUT as "ValorNuevo" FROM DUMMY
				UNION ALL
				Select p_IdEmpresa	as "SubRegistro", 'Direccion' as "CampoRegistro",  p_Direccion as "ValorNuevo" FROM DUMMY
				UNION ALL
				Select p_IdEmpresa	as "SubRegistro", 'Telefono' as "CampoRegistro",  p_Telefono as "ValorNuevo" FROM DUMMY
				UNION ALL
				Select p_IdEmpresa	as "SubRegistro", 'Email' as "CampoRegistro",  p_Email as "ValorNuevo" FROM DUMMY
				UNION ALL
				Select p_IdEmpresa	as "SubRegistro", 'Fax' as "CampoRegistro",  p_Fax as "ValorNuevo" FROM DUMMY
				UNION ALL
				Select p_IdEmpresa	as "SubRegistro", 'Apoderado1' as "CampoRegistro",  p_Apoderado1 as "ValorNuevo" FROM DUMMY
				UNION ALL
				Select p_IdEmpresa	as "SubRegistro", 'DocumentoApoderado1' as "CampoRegistro",  p_DocumentoApoderado1 as "ValorNuevo" FROM DUMMY
				UNION ALL
				Select p_IdEmpresa	as "SubRegistro", 'Apoderado2' as "CampoRegistro",  p_Apoderado2 as "ValorNuevo" FROM DUMMY
				UNION ALL
				Select p_IdEmpresa	as "SubRegistro", 'DocumentoApoderado2' as "CampoRegistro",  p_DocumentoApoderado2 as "ValorNuevo" FROM DUMMY
				UNION ALL
				Select p_IdEmpresa	as "SubRegistro", 'IdPersonaApoderado1' as "CampoRegistro",  CAST(p_IdPersonaApoderado1 AS NVARCHAR(500)) as "ValorNuevo" FROM DUMMY
				UNION ALL
				Select p_IdEmpresa	as "SubRegistro", 'IdPersonaApoderado2' as "CampoRegistro",  CAST(p_IdPersonaApoderado2 AS NVARCHAR(500)) as "ValorNuevo" FROM DUMMY
				) L,
				"BSCL.Plataforma.BD::CTT.ContratoDatosGenerales" AS CDG
				WHERE CDG."IdEmpresa" = p_IdEmpresa ;
								
				UPDATE "BSCL.Plataforma.BD::CTT.ContratoDatosGenerales"
				SET "RazonSocial" = 	p_RazonSocial,
				"RUT" =				p_RUT,
				"Direccion" = 		p_Direccion,
				"Telefono" = 		p_Telefono,
				"Email" = 			p_Email,
				"Fax" =				p_Fax,
				"Apoderado1" = 		p_Apoderado1,
				"DocumentoApoderado1" = p_DocumentoApoderado1,
				"Apoderado2" = 		p_Apoderado2,
				"DocumentoApoderado2" = p_DocumentoApoderado2,
				"IdPersonaApoderado1" = p_IdPersonaApoderado1,
				"IdPersonaApoderado2" = p_IdPersonaApoderado2
				WHERE "IdEmpresa" = p_IdEmpresa;
			END IF; 
			
			
			
			call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
					p_IdEmpresa => p_IdEmpresa,
					p_out_FechaHoraSistema => v_fechaHoraSistema
				);
			
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
					p_IdTipoLog => 'MODIFICACION' 
					,p_IdModulo => 'PLA' 
					,p_IdUsuario => p_IdUsuario 
					,p_IdEmpresa => p_IdEmpresa 
					,p_FechaSistemaLog => TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD')
					,p_IdUsuarioAtendido => p_IdUsuario
					,p_IdTipoRegistro => 'DATOS_CONTRATOS'--'CLIENTE'
					,p_DetalleLog => :p_DetalleLog	-- hacia arriba obligatorios
					--,p_NombreProceso => :v_NombreProceso-- hacia abajo opcionales
									
					);
			
			--------------------
			--FIN SP 
			--------------------
		END;	
	END;
END;

