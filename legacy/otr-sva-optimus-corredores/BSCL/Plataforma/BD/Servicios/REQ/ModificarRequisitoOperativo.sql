SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::REQ.ModificarRequisitoOperativo"  ;

CREATE PROCEDURE
-- 
--  Proposito: Servicio para la Modificación de Requisito Operativo
--  Autor: JG - BCS
--  Fecha Creacion: 08/04/2016
-- 

 "BSCL.Plataforma.BD.Servicios::REQ.ModificarRequisitoOperativo"  (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						BIGINT,			-- ID  Empresa destino
	IN  p_IdUsuario						BIGINT,			-- ID  del usuario que solicita el servicio
	IN  p_IdModulo						NVARCHAR(3),  	-- ID  del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 			NVARCHAR(1)	,	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN  p_IdRequisitoOperativo			BIGINT			,
	IN  p_IdModeloRequisitoOperativo	NVARCHAR(40)	DEFAULT 'CORREDORA', -- POR EL MOMENTO SOLO UNO, EL BASE DE LA CORREDORA.
	IN  p_IdCondicionOperativa			NVARCHAR(40)	,
	IN  p_IdTipoEtapa					NVARCHAR(40)	,
	IN  p_IdTipoComportamiento			NVARCHAR(40)	,
	IN  p_IdTipoCanalIngreso			VARCHAR(3)      DEFAULT '0', -- 0 todos.
	IN  p_IdMoneda						NVARCHAR(3)    	DEFAULT '0', -- 0 todos.
	IN  p_ValorAtributo1				NVARCHAR(255)	DEFAULT '',
	IN  p_ValorAtributo2				NVARCHAR(255)	DEFAULT '',	
	IN  p_IdEstado 						NVARCHAR(3) 	DEFAULT 'ACT',
	
	-- Parametros salida del servicio publico
    OUT p_out_RefId bigint,
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 	int,
	OUT p_out_AdvertenciasDetalle 	table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador 		int,
	OUT p_out_ErroresDetalle 		table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
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
		DECLARE v_FechaSistema DATE;
		
		DECLARE v_CountCantLog BIGINT;
		
		DECLARE v_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));
		
		
		DECLARE v_RequisitoOperativo TABLE (
			"IdRequisitoOperativo"			BIGINT,
			"IdModeloRequisitoOperativo"	NVARCHAR(40),
			"IdCondicionOperativa"			NVARCHAR(40),
			"IdTipoEtapa"					NVARCHAR(40),
			"IdTipoComportamiento"			NVARCHAR(40),
			"IdTipoCanalIngreso"			VARCHAR(3),
			"IdMoneda"						NVARCHAR(3),
			"ValorAtributo1"				NVARCHAR(255),
			"ValorAtributo2"				NVARCHAR(255),	
			"IdUsuarioCreador"				BIGINT,
			"FechaCreacion" 				SECONDDATE,
			"FechaModificacion" 			SECONDDATE,  
			"IdEmpresa"         			BIGINT,  
			"IdEstado"						NVARCHAR(3) );

		-----------------------------------------------------
		--Validaciones básicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		
		
		--------------------
		--Negocio Aqui
		--------------------

		BEGIN
		
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema"(p_IdEmpresa,:v_FechaSistema);
	
	
			--RESPALDO DATOS ANTES PARA EL LOG!
			v_RequisitoOperativo = SELECT
				"IdRequisitoOperativo",
				"IdModeloRequisitoOperativo",
				"IdCondicionOperativa",
				"IdTipoEtapa",
				"IdTipoComportamiento",
				"IdTipoCanalIngreso",
				"IdMoneda",
				"ValorAtributo1",
				"ValorAtributo2",
				"IdUsuarioCreador",
				"FechaCreacion",
				"FechaModificacion",
				"IdEmpresa",
				"IdEstado"		
			from "BSCL.Plataforma.BD::REQ.RequisitoOperativo" 
			WHERE "IdRequisitoOperativo" = :p_IdRequisitoOperativo;
			
		
			UPDATE "BSCL.Plataforma.BD::REQ.RequisitoOperativo" SET 
				--"IdModeloRequisitoOperativo" 	= :p_IdModeloRequisitoOperativo,
				"IdCondicionOperativa"			= :p_IdCondicionOperativa,
				"IdTipoEtapa"					= :p_IdTipoEtapa,
				"IdTipoComportamiento"			= :p_IdTipoComportamiento,
				"IdTipoCanalIngreso"			= :p_IdTipoCanalIngreso,
				"IdMoneda"						= :p_IdMoneda,
				"ValorAtributo1"				= :p_ValorAtributo1,
				"ValorAtributo2"				= :p_ValorAtributo2,
				"FechaModificacion"				= CURRENT_UTCTIMESTAMP,
				"IdEmpresa"						= :p_IdEmpresa,
				"IdEstado"						= :p_IdEstado				
			WHERE 
				"IdRequisitoOperativo" = :p_IdRequisitoOperativo;
	
			p_out_RefId:=1;
		
		END;		
	
		--------------------
		-- LOG 
		--------------------
		
		v_DetalleLog = 
			Select 'IdModeloRequisitoOperativo' as "SubRegistro", 'REQUISITO OPERATIVO' as "CampoRegistro", 
			CAST("IdModeloRequisitoOperativo" AS VARCHAR) as "ValorAntiguo", CAST(:p_IdModeloRequisitoOperativo AS VARCHAR) as "ValorNuevo" from :v_RequisitoOperativo
			UNION ALL
			Select 'IdCondicionOperativa' as "SubRegistro", 'REQUISITO OPERATIVO' as "CampoRegistro", 
			CAST("IdCondicionOperativa" AS VARCHAR) as "ValorAntiguo", CAST(:p_IdCondicionOperativa AS VARCHAR) as "ValorNuevo" from :v_RequisitoOperativo
			UNION ALL		
			Select 'IdTipoEtapa' as "SubRegistro", 'REQUISITO OPERATIVO' as "CampoRegistro", 
			CAST("IdTipoEtapa" AS VARCHAR) as "ValorAntiguo", CAST(:p_IdTipoEtapa AS VARCHAR) as "ValorNuevo" from :v_RequisitoOperativo
			UNION ALL	
			Select 'IdTipoComportamiento' as "SubRegistro", 'REQUISITO OPERATIVO' as "CampoRegistro", 
			CAST("IdTipoComportamiento" AS VARCHAR) as "ValorAntiguo", CAST(:p_IdTipoComportamiento AS VARCHAR) as "ValorNuevo" from :v_RequisitoOperativo
			UNION ALL
			Select 'IdTipoCanalIngreso' as "SubRegistro", 'REQUISITO OPERATIVO' as "CampoRegistro", 
			CAST("IdTipoCanalIngreso" AS VARCHAR) as "ValorAntiguo", CAST(:p_IdTipoCanalIngreso AS VARCHAR) as "ValorNuevo" from :v_RequisitoOperativo
			UNION ALL	
			Select 'IdMoneda' as "SubRegistro", 'REQUISITO OPERATIVO' as "CampoRegistro", 
			CAST("IdMoneda" AS VARCHAR) as "ValorAntiguo", CAST(:p_IdMoneda AS VARCHAR) as "ValorNuevo" from :v_RequisitoOperativo
			UNION ALL	
			Select 'ValorAtributo1' as "SubRegistro", 'REQUISITO OPERATIVO' as "CampoRegistro", 
			CAST("ValorAtributo1" AS VARCHAR) as "ValorAntiguo", CAST(:p_ValorAtributo1 AS VARCHAR) as "ValorNuevo" from :v_RequisitoOperativo
			UNION ALL
			Select 'ValorAtributo2' as "SubRegistro", 'REQUISITO OPERATIVO' as "CampoRegistro", 
			CAST("ValorAtributo2" AS VARCHAR) as "ValorAntiguo", CAST(:p_ValorAtributo2 AS VARCHAR) as "ValorNuevo" from :v_RequisitoOperativo
			--UNION ALL
			--Select 'IdEmpresa' as "SubRegistro", 'REQUISITO OPERATIVO' as "CampoRegistro", 
			--CAST("IdEmpresa" AS VARCHAR) as "ValorAntiguo", :p_IdEmpresa as "ValorNuevo" from :v_RequisitoOperativo
			UNION ALL
			Select 'IdEstado' as "SubRegistro", 'REQUISITO OPERATIVO' as "CampoRegistro", 
			CAST("IdEstado" AS VARCHAR) as "ValorAntiguo", CAST(:p_IdEstado AS VARCHAR) as "ValorNuevo" from :v_RequisitoOperativo
			;
		
		SELECT COUNT("SubRegistro") INTO v_CountCantLog FROM :v_DetalleLog;
		
		IF(v_CountCantLog>0) THEN	
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
				p_IdTipoLog => 'MODIFICACION' 
				,p_IdModulo => 'REQ' 
				,p_IdEmpresa => p_IdEmpresa
				,p_IdUsuario => p_IdUsuario 
				,p_FechaSistemaLog => v_FechaSistema
				,p_IdUsuarioAtendido => p_IdUsuario
				,p_IdTipoRegistro => 'REQUISITOOPERATIVO'
				,p_IdRegistro => p_IdRequisitoOperativo
				,p_DetalleLog => :v_DetalleLog
				,p_NombreProceso => 'ModificarRequisitoOperativo'
			);
		END IF;
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;