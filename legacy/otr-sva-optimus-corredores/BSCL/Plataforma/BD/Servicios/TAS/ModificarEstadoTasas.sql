SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::TAS.ModificarEstadoTasas";

-- 
--  Proposito: Modificar el estado de una o mas Tasas. Aplica para Oficializar, Publicar y Eliminar
--  Autor: MMACHMAR - BCS
--  Fecha Creacion: 06/10/2016  
-- 
  
CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::TAS.ModificarEstadoTasas" (
	-- Parametros entrada comunes
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_IdUsuario			BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo			NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN p_UsaReglasNegocio	NVARCHAR(1),	--S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada
	IN p_Estado				NVARCHAR(3),
	IN p_TablaTasa			TABLE(	"IdPrecioTasa"	BIGINT,
									"CodDescFuente"	NVARCHAR(300),
									"Nemotecnico"	NVARCHAR(40)),
									
	IN p_OrigenWsCargarTasaNemo	NVARCHAR(1) DEFAULT 'N',--S/N se usa para detectar origen desde webservice cargarTasasNemo
	
	-- Parametros salida del servicio publico
	OUT p_out_RefId			BIGINT,
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador	INT,
	OUT p_out_AdvertenciasDetalle	TABLE ("Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000)),	
	OUT p_out_ErroresContador		INT,
	OUT p_out_ErroresDetalle		TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))
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
		select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador from :p_out_AdvertenciasDetalle;		 					 	
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
        
		DECLARE v_Count			BIGINT := 0;
		DECLARE v_CantRegistros	BIGINT := 0;
		DECLARE v_FechaSistema	SECONDDATE;	-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
		DECLARE v_IdTipoLog		NVARCHAR(40) := 'MODIFICACION';
		
		DECLARE v_FechaSistemaUTC   	NVARCHAR(40);
		DECLARE v_FechaSistemaLOCAL   	NVARCHAR(40);
		DECLARE v_FechaHoraSistemaUTC   NVARCHAR(40);
		DECLARE v_FechaHoraSistemaLOCAL SECONDDATE;
		DECLARE v_FechaHoraUTC   		NVARCHAR(40);
		DECLARE v_FechaHoraLOCAL   		NVARCHAR(40);
		
		p_out_RefId := 0;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaSistema);
		
		-- Se obtiene la misma FechaHoraSistema que se muestra en la UI
    	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechas"(
			p_IdEmpresa					=> p_IdEmpresa,
			p_out_FechaSistemaUTC		=> v_FechaSistemaUTC,
			p_out_FechaSistemaLOCAL		=> v_FechaSistemaLOCAL,
			p_out_FechaHoraSistemaUTC	=> v_FechaHoraSistemaUTC,
			p_out_FechaHoraSistemaLOCAL	=> v_FechaHoraSistemaLOCAL,
			p_out_FechaHoraUTC			=> v_FechaHoraUTC,
			p_out_FechaHoraLOCAL		=> v_FechaHoraLOCAL
		);
		
		
		-- Tasas que se van a modificar
		v_TablaTasa =
			SELECT
				 TAS."IdPrecioTasa"
				,TAS."Nemotecnico"
				,TAS."IdEstado"
				,TAS."Borrado"
				,CASE WHEN p_Estado = 'ELI' THEN 'Borrado' ELSE 'IdEstado' END						AS "CampoRegistro"
				,CASE WHEN p_Estado = 'ELI' THEN TAS."IdEstado"
					  ELSE (CASE WHEN p_Estado = 'OFI' AND TAS."IdTipoTasa" IN ('CURVA','PUNTO') THEN 'PUB' ELSE p_Estado END) END	AS "IdEstadoNuevo"
				,CASE WHEN p_Estado = 'ELI' THEN 'S' ELSE TAS."Borrado" END							AS "BorradoNuevo"
				,CASE WHEN p_Estado = 'OFI' OR (p_Estado = 'PUB' AND TAS."IdTipoTasa" IN ('CURVA','PUNTO')) THEN :v_FechaHoraSistemaLOCAL
					  ELSE TAS."FechaOficializacion" END											AS "FechaOficializacionNuevo"
				,CASE WHEN p_Estado = 'PUB' OR (p_Estado = 'OFI' AND TAS."IdTipoTasa" IN ('CURVA','PUNTO')) THEN :v_FechaHoraSistemaLOCAL 
					  ELSE TAS."FechaPublicacion" END												AS "FechaPublicacionNuevo"
				,CASE WHEN p_Estado = 'OFI' OR (p_Estado = 'PUB' AND TAS."IdTipoTasa" IN ('CURVA','PUNTO')) THEN :p_IdUsuario
					  ELSE TAS."IdUsuarioOficializador" END											AS "IdUsuarioOficializadorNuevo"
			FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarPrecioTasa" TAS
			INNER JOIN :p_TablaTasa TTA ON (TAS."IdPrecioTasa" = TTA."IdPrecioTasa" AND TAS."IdEmpresa" = p_IdEmpresa)
			WHERE TAS."Borrado" = 'N';
		
		--------------------
		--Validaciones
		--------------------
		BEGIN
			-- Cantidad registros de entrada
			SELECT COUNT(1) INTO v_CantRegistros FROM :p_TablaTasa;
			
			
			-- Verifica existen Tasas
			SELECT COUNT(1) INTO v_Count FROM :v_TablaTasa;
			IF(v_Count = 0) THEN
				p_out_ErroresDetalle = select 'TASAS NO EXISTEN' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			
			-- Verificaciones Oficializar
			IF p_Estado = 'OFI' THEN
				-- Verifica tasas ya estan Oficializadas
				SELECT COUNT(1) INTO v_Count FROM :v_TablaTasa WHERE "IdEstado" = 'OFI';
				IF(v_Count > 0 AND v_CantRegistros = v_Count) THEN
					p_out_ErroresDetalle = select 'TASAS YA ESTAN OFICIALIZADAS' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
					SIGNAL SALIDA_CONTROLADA;
				END IF;
				
				-- Verifica tasas están Publicadas
				SELECT COUNT(1) INTO v_Count FROM :v_TablaTasa WHERE "IdEstado" = 'PUB';
				IF(v_Count > 0 AND v_CantRegistros = v_Count) THEN
					p_out_ErroresDetalle = select 'TASAS ESTAN PUBLICADAS' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
					SIGNAL SALIDA_CONTROLADA;
				END IF;
				
				-- Verifica para Oficializar tasas, deben estar Cargadas y/o Creadas en Sistema
				SELECT COUNT(1) INTO v_Count FROM :v_TablaTasa WHERE "IdEstado" IN ('CRS','CAR','MOD');
				select v_CantRegistros as "CantReg", v_Count as "Count" from dummy;
				IF(v_Count = 0) THEN
					p_out_ErroresDetalle = select 'TASAS DEBEN ESTAR CARGADAS, CREADAS EN SISTEMA O MODIFICADAS' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
					SIGNAL SALIDA_CONTROLADA;
				END IF;
			END IF;
			
			
			-- Verificaciones Publicar
			-- Origen ws cargarTasasNemo no debe validar ya que debe publicar automaticamente
			IF p_Estado = 'PUB' AND p_OrigenWsCargarTasaNemo = 'N' THEN
				-- Verifica tasas ya estan Publicadas
				SELECT COUNT(1) INTO v_Count FROM :v_TablaTasa WHERE "IdEstado" = 'PUB';
				IF(v_Count > 0 AND v_CantRegistros = v_Count) THEN
					p_out_ErroresDetalle = select 'TASAS YA ESTAN PUBLICADAS' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
					SIGNAL SALIDA_CONTROLADA;
				END IF;
				
				-- Verifica para Publicar tasas, deben estar Oficializadas
				SELECT COUNT(1) INTO v_Count FROM :v_TablaTasa WHERE "IdEstado" = 'OFI';
				IF(v_Count = 0) THEN
					p_out_ErroresDetalle = select 'TASAS DEBEN ESTAR OFICIALIZADAS' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
					SIGNAL SALIDA_CONTROLADA;
				END IF;
			END IF;
			
			
			-- Verificaciones Eliminar (no deben estar Oficializadas ni Publicadas) 
			-- Origen ws cargarTasasNemo no debe validar ya que debe eliminar todos los registros anteriores asociado al nemo
			IF p_Estado = 'ELI' AND p_OrigenWsCargarTasaNemo = 'N' THEN
				SELECT COUNT(1) INTO v_Count 
				FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarPrecioTasa" TAS
				INNER JOIN :v_TablaTasa TTA ON (TAS."IdPrecioTasa" = TTA."IdPrecioTasa" AND TAS."IdEmpresa" = p_IdEmpresa)
				WHERE TAS."Borrado" = 'N' AND TTA."IdEstado" IN ('OFI','PUB') AND TAS."IdTipoTasa" != 'CURVA';
				--IF(v_Count > 0) THEN
				IF(v_CantRegistros = v_Count) THEN
					p_out_ErroresDetalle = select 'TASAS OFICIALIZADAS O PUBLICADAS NO SE PUEDEN ELIMINAR' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
					SIGNAL SALIDA_CONTROLADA;
				END IF;
			END IF;
			
		END;
		
		
		--------------------
		--Reglas Negocio 
		--------------------		
		
		--------------------
		--Lógica de Negocio 
		--------------------
		
		BEGIN
			IF p_OrigenWsCargarTasaNemo = 'N' THEN
				UPDATE TAS
				SET
					TAS."IdEstado"				= TTA."IdEstadoNuevo",
					TAS."Borrado"				= TTA."BorradoNuevo",
					TAS."IdUsuarioModificacion"	= p_IdUsuario,
					TAS."FechaModificacion"		= CURRENT_UTCTIMESTAMP,
					TAS."FechaOficializacion"	= TTA."FechaOficializacionNuevo",
					TAS."FechaPublicacion"		= TTA."FechaPublicacionNuevo",
					TAS."IdUsuarioOficializador"= TTA."IdUsuarioOficializadorNuevo"
				FROM "BSCL.Plataforma.BD::TAS.PrecioTasa" TAS
				INNER JOIN :v_TablaTasa TTA ON (TAS."IdPrecioTasa" = TTA."IdPrecioTasa")
				WHERE (p_Estado = 'OFI' AND TAS."IdEstado" IN ('CRS','CAR','MOD'))
				   OR (p_Estado = 'PUB' AND TAS."IdEstado" IN ('OFI'))
				   OR (p_Estado = 'PUB' AND TAS."IdEstado" IN ('CRS','CAR','MOD') AND TAS."IdTipoTasa" IN ('CURVA','PUNTO'))
				   OR (p_Estado = 'ELI' AND (TAS."IdEstado" IN ('SOL','CRS','CAR','MOD') OR TAS."IdTipoTasa" = 'CURVA'));
			ELSE
				-- Solo para origen ws cargarTasasNemo
				UPDATE TAS
				SET
					TAS."IdEstado"				= TTA."IdEstadoNuevo",
					TAS."Borrado"				= TTA."BorradoNuevo",
					TAS."IdUsuarioModificacion"	= p_IdUsuario,
					TAS."FechaModificacion"		= CURRENT_UTCTIMESTAMP,
					TAS."FechaOficializacion"	= TTA."FechaOficializacionNuevo", --Se usa la misma fecha de publicacion
					TAS."FechaPublicacion"		= TTA."FechaPublicacionNuevo",
					TAS."IdUsuarioOficializador"= TTA."IdUsuarioOficializadorNuevo"
				FROM "BSCL.Plataforma.BD::TAS.PrecioTasa" TAS
				INNER JOIN :v_TablaTasa TTA ON (TAS."IdPrecioTasa" = TTA."IdPrecioTasa")
				WHERE (p_Estado = 'OFI' AND TAS."IdEstado" IN ('CRS','CAR','MOD') AND TAS."IdTipoTasa" IN ('PRECIO','PUNTO','TASA'))
				   OR (p_Estado = 'PUB' AND TAS."IdEstado" IN ('OFI') AND TAS."IdTipoTasa" IN ('PRECIO','PUNTO','TASA'))
				   OR (p_Estado = 'ELI' AND TAS."IdEstado" IN ('SOL','CRS','CAR','MOD','PUB','OFI') AND TAS."IdTipoTasa" IN ('PRECIO','PUNTO','TASA'));
			END IF;
		
			p_out_RefId := 1;
			
			
			-- LOG DE AUDITORIA
			IF p_Estado = 'ELI' THEN v_IdTipoLog := 'ELIMINACION';
			END IF;
			
			p_DetalleLog =
				SELECT DISTINCT
					TTA."Nemotecnico" AS "SubRegistro",
					TTA."CampoRegistro",
					CASE WHEN p_Estado = 'ELI' THEN TTA."Borrado" ELSE TTA."IdEstado" END AS "ValorAntiguo",
					CASE WHEN p_Estado = 'ELI' THEN TTA."BorradoNuevo" ELSE TTA."IdEstadoNuevo" END AS "ValorNuevo"
				FROM :v_TablaTasa TTA;
			
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLog"(
				 p_IdTipoLog 		 => v_IdTipoLog
				,p_IdModulo 		 => :p_IdModulo
				,p_IdUsuario 		 => :p_IdUsuario
				,p_IdEmpresa 		 => :p_IdEmpresa
				,p_FechaSistemaLog 	 => TO_NVARCHAR(:v_FechaSistema,'YYYY-MM-DD')
				,p_IdUsuarioAtendido => :p_IdUsuario
				,p_IdTipoRegistro 	 => 'TASA'
				,p_NombreProceso 	 => v_IdTipoLog || ' TASA'
				,p_DetalleLog 		 => :p_DetalleLog
			);
			
		END;
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
