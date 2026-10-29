SET SCHEMA "BSCL";
go
DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::BLO.BloqueoMasivoClientesPortafolios";
go
CREATE PROCEDURE
-- 
--  Proposito: Servicio para bloquear clientes y sus portafolios de forma masiva.
--  Autor: CHERNANDEZ - BCS
--  Fecha Creacion: 17-08-2016
--  
"BSCL.SocioNegocio.BD.Servicios::BLO.BloqueoMasivoClientesPortafolios" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	
	
		  
	-- Parametros entrada del servicio publico
	IN  p_BloqueoClientes	 						TABLE(
														DOCUMENTOIDENTIDAD	NVARCHAR(40),	
														MOTIVOBLOCLI		NVARCHAR(40),
														MOTIVOBLOPOR 		NVARCHAR(40),
														PORTAFOLIO 			BIGINT,
														TIPODOCUMENTO 		NVARCHAR(1)
													),
	
	-- Parametros salida del servicio publico
	OUT p_out_operaciones_realizadas				TABLE(
													"Operacion" NVARCHAR(20),
													"Entidad"	NVARCHAR(10),
													"Id"		NVARCHAR(40),
													"IdEstado"	NVARCHAR(500)),

	-- Salidas para mensajes de advertencias y errores
	OUT p_out_InformacionContador  	INT,
	OUT p_out_InformacionDetalle   	TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)),
	OUT p_out_AdvertenciasContador 	int,
	OUT p_out_AdvertenciasDetalle 	table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador 		int,
	OUT p_out_ErroresDetalle 		table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
	
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA 		CONDITION; 
	DECLARE v_errorcuenta 			INT DEFAULT 0;  
	DECLARE v_errorhashmsg 			table ("Codigo" nvarchar(40),"Valor" nvarchar(5000));
	DECLARE v_erroroutmsg  			nvarchar(5000);	        	
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
	p_out_InformacionContador	:= 0;
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_InformacionDetalle	= SELECT '' as "Codigo",'' as "Descripcion" from dummy where 1=2;	
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	
	BEGIN	
		--------------------
		--Declare Variables
		--------------------
		DECLARE v_bloqueo_clientes		TABLE ("IdPersona" BIGINT, "DocumentoIdentidad" NVARCHAR(40), "TipoBloqueoCliente" NVARCHAR(40), "DescripcionBloqueoCliente" NVARCHAR(40),"TipoBloqueoPortafolio" NVARCHAR(40), "IdPortafolio" BIGINT, "IdTipoDocumentoIdentidad" NVARCHAR(1), "IdEstado" NVARCHAR(3));		
		DECLARE v_bloqueo_portafolios	TABLE ("IdPortafolio" BIGINT, "DocumentoIdentidad" NVARCHAR(40), "TipoBloqueoCliente" NVARCHAR(40), "TipoBloqueoPortafolio" NVARCHAR(40), "IdTipoDocumentoIdentidad" NVARCHAR(1), "IdEstado" NVARCHAR(3));
		DECLARE v_count_clientes BIGINT;
		
		DECLARE v_count_pf_all BIGINT;
		DECLARE v_count_cl_all BIGINT;
		
		DECLARE v_count_pf BIGINT;
		DECLARE v_count_cl BIGINT;

		
		p_out_InformacionDetalle = SELECT '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
		--------------------
		--Validaciones
		--------------------
			--Verificar que todos los documentos de identidad de entrada esten asociados a una persona.
			--Validar que las personas sean clientes y tengan portafolios
			
			-- Validacion datos portafolio completo
			
			select COUNT(*) INTO v_count_pf_all
			FROM :p_BloqueoClientes 
			WHERE MOTIVOBLOPOR <> '' OR PORTAFOLIO <> 0 ;
			
			select COUNT(*) INTO v_count_pf
			FROM :p_BloqueoClientes 
			WHERE MOTIVOBLOPOR <> '' AND PORTAFOLIO <> 0 ;
			
			SELECT 'PF_ALL',v_count_pf_all FROM dummy;
			SELECT 'PF',v_count_pf FROM dummy;
			
			
			IF (v_count_pf_all <> v_count_pf) THEN 				
				
				p_out_ErroresDetalle=SELECT 'ERROR_CANTIDAD_PORTAFOLIO' as "Codigo",'Datos Portafolios Incompleto' as "Descripcion" from dummy;
				
			
			END IF;
			
		   -- Validacion datos cliente completo
			select COUNT(*) INTO v_count_cl_all
			FROM :p_BloqueoClientes 
			WHERE  DOCUMENTOIDENTIDAD <> '' OR TIPODOCUMENTO <> '' OR  MOTIVOBLOCLI <> '';
			
			select COUNT(*) INTO v_count_cl
			FROM :p_BloqueoClientes 
			WHERE DOCUMENTOIDENTIDAD <> '' AND TIPODOCUMENTO <> '' AND  MOTIVOBLOCLI <> '';
			
			IF (v_count_cl_all <> v_count_cl) THEN 
			
				p_out_ErroresDetalle =
				SELECT "Codigo", "Descripcion" FROM :p_out_ErroresDetalle
				UNION ALL
				SELECT 'ERROR_CANTIDAD_CLIENTES' as "Codigo",'Datos Clientes Incompleto' as "Descripcion" from dummy;	
				
			END IF;
			
			SELECT COUNT (*) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
			
			--PRUEBAS
					
			SELECT 'Errores',p_out_ErroresContador FROM dummy;
			SELECT 'TABLA ENTRADA',* FROM :p_BloqueoClientes;
			
			-- Comprueba que no existan incosistencias antes de realizar los bloqueos
			IF p_out_ErroresContador = 0 THEN 
			
			select COUNT(*) INTO v_count_clientes
				FROM :p_BloqueoClientes bc
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" di on bc.DOCUMENTOIDENTIDAD = di."DocumentoIdentidad" AND bc.TIPODOCUMENTO = di."IdTipoDocumentoIdentidad"
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC ON di."IdPersona" = CC."IdPersona"
				WHERE COALESCE(di."DocumentoIdentidad",'') = '' AND bc.DOCUMENTOIDENTIDAD <> '' AND bc.PORTAFOLIO = 0;
				
				IF v_count_clientes > 0  THEN
					p_out_InformacionDetalle = 	
					SELECT 
					'UI_ERROR' AS "Codigo",
					REPLACE('NO EXISTE UN CLIENTE CON DOCUMENTO: #DOCUMENTO#','#DOCUMENTO#', bc.DOCUMENTOIDENTIDAD) AS "Descripcion"
					FROM :p_BloqueoClientes bc
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" di on bc.DOCUMENTOIDENTIDAD = di."DocumentoIdentidad" AND bc.TIPODOCUMENTO = di."IdTipoDocumentoIdentidad"
					LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC ON di."IdPersona" = CC."IdPersona"
					WHERE COALESCE(di."DocumentoIdentidad",'') = '' AND bc.DOCUMENTOIDENTIDAD <> '' AND bc.PORTAFOLIO = 0;
				END IF;
				
				select COUNT(*) INTO v_count_clientes
				FROM :p_BloqueoClientes bc
				LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" p ON bc.PORTAFOLIO = p."IdPortafolio"
				WHERE COALESCE(p."CodigoPortafolio",'') = '' AND bc.PORTAFOLIO <> 0 AND bc.DOCUMENTOIDENTIDAD = '';
				
				IF v_count_clientes > 0 THEN
					p_out_InformacionDetalle = 	
					SELECT 
					'UI_ERROR' AS "Codigo",
					REPLACE('NO EXISTE EL PORTAFOLIO: #PORTAFOLIO#','#PORTAFOLIO#', bc.PORTAFOLIO) AS "Descripcion"
					FROM :p_BloqueoClientes bc
					LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" p ON bc.PORTAFOLIO = p."IdPortafolio"
					WHERE COALESCE(p."CodigoPortafolio",'') = '' AND bc.PORTAFOLIO <> 0  AND bc.DOCUMENTOIDENTIDAD = '' 
					UNION ALL SELECT * FROM :p_out_InformacionDetalle;
				END IF;
				
				select COUNT(*) INTO v_count_clientes
				FROM :p_BloqueoClientes bc
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" di on bc.DOCUMENTOIDENTIDAD = di."DocumentoIdentidad" AND bc.TIPODOCUMENTO = di."IdTipoDocumentoIdentidad"
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC ON di."IdPersona" = CC."IdPersona"
				LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" p ON bc.PORTAFOLIO = p."IdPortafolio" AND CC."IdCliente" = p."IdCliente"
				WHERE COALESCE(di."DocumentoIdentidad",'') = '' AND COALESCE(p."CodigoPortafolio",'') = ''
				AND bc.DOCUMENTOIDENTIDAD<>'' AND bc.PORTAFOLIO <> 0;
				
				IF v_count_clientes > 0 THEN
					p_out_InformacionDetalle = 	
					SELECT 
					'UI_ERROR' AS "Codigo",
					REPLACE(REPLACE('EL PORTAFOLIO:#PORTAFOLIO# NO ESTA ASOCIADO AL CLIENTE: #DOCUMENTO#','#DOCUMENTO#', bc.DOCUMENTOIDENTIDAD),'#PORTAFOLIO#',bc.PORTAFOLIO) AS "Descripcion"
					FROM :p_BloqueoClientes bc
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" di on bc.DOCUMENTOIDENTIDAD = di."DocumentoIdentidad" AND bc.TIPODOCUMENTO = di."IdTipoDocumentoIdentidad"
					LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC ON di."IdPersona" = CC."IdPersona"
					LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" p ON bc.PORTAFOLIO = p."IdPortafolio" AND CC."IdCliente" = p."IdCliente"
					WHERE COALESCE(di."DocumentoIdentidad",'') = '' AND COALESCE(p."CodigoPortafolio",'') = ''
					AND bc.DOCUMENTOIDENTIDAD <> '' AND bc.PORTAFOLIO <> 0
					UNION ALL SELECT * FROM :p_out_InformacionDetalle;
				END IF;
	
				SELECT COUNT(*) INTO v_count_clientes FROM :p_out_InformacionDetalle;
				IF v_count_clientes > 0 THEN
					SIGNAL SALIDA_CONTROLADA;
				END IF;
				
				
				--------------------
		--Logica Negocio
		--------------------
			BEGIN
			--Obtener Clientes a bloquear IdTipoBloqueo, FechaBloqueo, ComentarioBloqueo, "IdEstado"
			v_bloqueo_clientes = select 
				di."IdPersona", 
				di."DocumentoIdentidad",
				bc.MOTIVOBLOCLI as "TipoBloqueoCliente",				
				TBS."NombreTipoBloqueo" AS "DescripcionBloqueoCliente",
				bc.MOTIVOBLOPOR as "TipoBloqueoPortafolio",				
				bc.PORTAFOLIO as "IdPortafolio",
				di."IdTipoDocumentoIdentidad",
				'BLO' AS "IdEstado"
			FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" di
			INNER JOIN :p_BloqueoClientes bc on bc.DOCUMENTOIDENTIDAD = di."DocumentoIdentidad" AND bc.TIPODOCUMENTO = di."IdTipoDocumentoIdentidad"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarTipoBloqueoSocioDeNegocio" TBS ON TBS."IdTipoBloqueo" = bc.MOTIVOBLOCLI AND TBS."IdTipoEntidad"='CLIENTE';
			
			SELECT 'TABLA BLOQUEO', * FROM :v_bloqueo_clientes;
			
			--Aplicar Bloqueo		
			UPDATE "BSCL.SocioNegocio.BD::CLI.Cliente" c
			SET 
				c."IdTipoBloqueo" = bc."TipoBloqueoCliente", 
				c."FechaBloqueo" = CURRENT_UTCTIMESTAMP,
				c."FechaModificacion" = CURRENT_UTCTIMESTAMP,
				c."ComentarioBloqueo" = bc."DescripcionBloqueoCliente",									
				c."IdEstado" = bc."IdEstado"
			FROM "BSCL.SocioNegocio.BD::CLI.Cliente" c, :v_bloqueo_clientes bc
			WHERE c."IdPersona" = bc."IdPersona";
			
			p_out_operaciones_realizadas = 	SELECT 
											CASE
												WHEN c."IdEstado" = 'BLO' THEN
												'BLOQUEADO'
												ELSE
												'NO BLOQUEADO'
											END AS "Operacion", 
											'CLIENTE' AS "Entidad", bc."DocumentoIdentidad" AS "Id", c."IdEstado" FROM :v_bloqueo_clientes bc
											INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" c ON c."IdPersona" = bc."IdPersona";
											
			--("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)),
			p_out_InformacionDetalle = 	SELECT 
										'UI_BLOQUEO_CLIENTE' AS "Codigo",
										CASE
											WHEN c."IdEstado" = 'BLO' THEN
												REPLACE('EL CLIENTE CON DOCUMENTO: #DOCUMENTO#, FUE BLOQUEADO CORRECTAMENTE','#DOCUMENTO#', bc."DocumentoIdentidad")
											ELSE
												REPLACE('EL CLIENTE CON DOCUMENTO: #DOCUMENTO#, NO FUE BLOQUEADO CORRECTAMENTE', '#DOCUMENTO#', bc."DocumentoIdentidad")
											END AS "Descripcion"
										FROM :v_bloqueo_clientes bc
										INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" c ON c."IdPersona" = bc."IdPersona";				

			--Obtener los portafolios que esten asociados a los clientes/portafolio recibidos como entrada.
			--Solo se bloquean los Portafolios que vienen en la entrada. IdEstado, FechaBloqueo, ComentarioBloqueo, IdTipoBloqueo
			v_bloqueo_clientes = select 
				c."IdPersona", 
				di."DocumentoIdentidad",
				bc.MOTIVOBLOCLI as "TipoBloqueoCliente",
				TBS."NombreTipoBloqueo" AS "DescripcionBloqueoCliente",
				bc.MOTIVOBLOPOR as "TipoBloqueoPortafolio",
				p."IdPortafolio",
				bc.TIPODOCUMENTO AS "IdTipoDocumentoIdentidad",
				'BLO' AS "IdEstado"
			FROM :p_BloqueoClientes bc
			INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" di ON bc.DOCUMENTOIDENTIDAD = di."DocumentoIdentidad" AND bc.TIPODOCUMENTO = di."IdTipoDocumentoIdentidad"
			INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" c ON c."IdPersona" = di."IdPersona"
			INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" p ON p."IdCliente" = c."IdCliente" AND bc.PORTAFOLIO = p."IdPortafolio"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarTipoBloqueoSocioDeNegocio" TBS ON TBS."IdTipoBloqueo" = bc.MOTIVOBLOCLI AND TBS."IdTipoEntidad"='CLIENTE';
			
			--Aplicar Bloqueo
			UPDATE "BSCL.Portafolio.BD::POR.Portafolio" p
			SET
				p."IdEstado" = bc."IdEstado",
				p."FechaBloqueo" = CURRENT_UTCTIMESTAMP,
				p."ComentarioBloqueo" = 'BLOQUEO  MASIVO',
				p."IdTipoBloqueo" = bc."TipoBloqueoPortafolio"
			FROM "BSCL.Portafolio.BD::POR.Portafolio" p , :v_bloqueo_clientes bc
			WHERE p."IdPortafolio" = bc."IdPortafolio";
			
			--este bloqueo no funciona porque no existe el id de la persona, obtener
			--Obtener los portafolios que vienen solos.
			v_bloqueo_clientes = select 
				c."IdPersona", 
				di."DocumentoIdentidad",
				bc.MOTIVOBLOCLI as "TipoBloqueoCliente",
				TBS."NombreTipoBloqueo" AS "DescripcionBloqueoCliente",
				bc.MOTIVOBLOPOR as "TipoBloqueoPortafolio",
				p."IdPortafolio",
				di."IdTipoDocumentoIdentidad",
				'BLO' AS "IdEstado"
			FROM :p_BloqueoClientes bc
			INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" p ON bc.PORTAFOLIO = p."IdPortafolio"
			INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" c ON c."IdCliente" = p."IdCliente"
			INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" di ON di."IdPersona" = c."IdPersona"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarTipoBloqueoSocioDeNegocio" TBS ON TBS."IdTipoBloqueo" = bc.MOTIVOBLOCLI AND TBS."IdTipoEntidad"='CLIENTE';
			--Aplicar Bloqueo
			UPDATE "BSCL.Portafolio.BD::POR.Portafolio" p
			SET
				p."IdEstado" = bc."IdEstado",
				p."FechaBloqueo" = CURRENT_UTCTIMESTAMP,
				p."ComentarioBloqueo" = 'BLOQUEO  MASIVO',
				p."IdTipoBloqueo" = bc."TipoBloqueoPortafolio"
			FROM "BSCL.Portafolio.BD::POR.Portafolio" p , :v_bloqueo_clientes bc
			WHERE p."IdPortafolio" = bc."IdPortafolio";
			
			p_out_operaciones_realizadas = 	SELECT 
											CASE
												WHEN p."IdEstado" = 'BLO' THEN
												'BLOQUEADO'
												ELSE
												'NO BLOQUEADO'
												END AS "Operacion", 
											'PORTAFOLIO' AS "Entidad", CAST(bc."IdPortafolio" AS VARCHAR) AS "Id", p."IdEstado" FROM :v_bloqueo_clientes bc
											INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" p ON bc."IdPortafolio" = p."IdPortafolio"
											UNION ALL SELECT * FROM :p_out_operaciones_realizadas;
			
			p_out_InformacionDetalle = 	SELECT 
										'UI_BLOQUEO_PORTAFOLIO' AS "Codigo",
										CASE
											WHEN p."IdEstado" = 'BLO' THEN
												REPLACE(REPLACE('EL PORTAFOLIO:#PORTAFOLIO# FUE BLOQUEADO CORRECTAMENTE','#DOCUMENTO#', bc."DocumentoIdentidad"),'#PORTAFOLIO#',bc."IdPortafolio")
											ELSE
												REPLACE(REPLACE('EL PORTAFOLIO:#PORTAFOLIO# NO FUE BLOQUEADO CORRECTAMENTE', '#DOCUMENTO#', bc."DocumentoIdentidad"),'#PORTAFOLIO#',bc."IdPortafolio")
											END AS "Descripcion"
										FROM :v_bloqueo_clientes bc
										INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" p ON bc."IdPortafolio" = p."IdPortafolio"
										UNION ALL SELECT * FROM :p_out_InformacionDetalle;

		END;
				
			
		END IF;
		--------------------
		--FIN SP 
		--------------------
	END;
END;