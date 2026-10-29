SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Mesa.BD.Servicios::ModificarEventoPosicion";

-- 
--  Proposito: Modificar Evento Posicion
--  Autor: MMACHMAR - BCS
--  Fecha Creacion: 29/12/2016
-- 
  
CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::ModificarEventoPosicion" (
	-- Parametros entrada comunes
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_IdUsuario			BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo			NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN p_UsaReglasNegocio	NVARCHAR(1),	--S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada
	IN p_IdEvento			BIGINT,
	IN p_IdOperacion		NVARCHAR(4),
	IN p_IdTipoEvento		NVARCHAR(3),
	IN p_IndAccion			NVARCHAR(1),
	IN p_IdEstado			NVARCHAR(3),
	
	-- Parametro para registro en Log
	IN p_DetalleLog			TABLE (	"SubRegistro"	NVARCHAR(40),
									"CampoRegistro"	NVARCHAR(40),
									"ValorAntiguo"	NVARCHAR(250),
									"ValorNuevo"	NVARCHAR(250)),
	
	-- Parametros salida del servicio publico
	OUT p_out_RefId			BIGINT,	
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador	INT,
	OUT p_out_AdvertenciasDetalle	TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador		INT,
	OUT p_out_ErroresDetalle		TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
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
	
	DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
	DECLARE v_out_ErroresDetalle		TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
	DECLARE v_out_AdvertenciasDetalle	TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));
	
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
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle
								   UNION ALL
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;
			select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;
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
	    
		DECLARE v_Count			BIGINT:=0;
		DECLARE v_FechaSistema	SECONDDATE;-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
		
		p_out_RefId := 0;
		
		--------------------
		--Validaciones
		--------------------
			
		---Verifica que Evento Posicion exista
		SELECT COUNT(1) INTO v_Count
		FROM "BSCL.Mesa.BD::MES.EventoPosicion"
		WHERE "IdEvento" = :p_IdEvento;
		
		IF(v_Count = 0) THEN
			p_out_ErroresDetalle = select 'EVENTO POSICION NO EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		
		-- Validacion existe Evento Posicion con mismo IdOperacion, IdTipoEvento y IndAccion
		SELECT COUNT(1) INTO v_Count
		FROM "BSCL.Mesa.BD::MES.EventoPosicion"
		WHERE "IdOperacion"	= p_IdOperacion
		  AND "IdTipoEvento"= p_IdTipoEvento
		  AND "IndAccion"	= p_IndAccion
		  AND "IdEstado"	= 'VIG'
		  AND "IdEvento"	<> :p_IdEvento;
		
		IF(v_Count > 0) THEN
			p_out_ErroresDetalle = select 'EVENTO POSICION YA EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		--------------------
		--Reglas Negocio 
		--------------------		
		
		--------------------
		--Lógica de Negocio 
		--------------------
				
		BEGIN
			
			UPDATE EVE
			SET
				EVE."IdOperacion"	= p_IdOperacion,
				EVE."IdTipoEvento"	= p_IdTipoEvento,
				EVE."IndAccion"		= p_IndAccion,
				EVE."IdEstado"		= p_IdEstado
			FROM "BSCL.Mesa.BD::MES.EventoPosicion" EVE
			WHERE EVE."IdEvento"= p_IdEvento;
			
			p_out_RefId := 1;
			
			
			-- LOG DE AUDITORIA
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaSistema);
			
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLog"(
				 p_IdTipoLog 		 => 'MODIFICACION'
				,p_IdModulo 		 => :p_IdModulo
				,p_IdUsuario 		 => :p_IdUsuario
				,p_IdEmpresa 		 => :p_IdEmpresa
				,p_FechaSistemaLog 	 => TO_NVARCHAR(:v_FechaSistema,'YYYY-MM-DD')
				,p_IdUsuarioAtendido => :p_IdUsuario
				,p_IdTipoRegistro 	 => 'EVENTOPOSICION'
				,p_NombreProceso 	 => 'MODIFICACION EVENTO POSICION'
				,p_DetalleLog 		 => :p_DetalleLog
			);
		
		END;
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
