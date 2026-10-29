SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::REQ.CrearRequisitoOperativo"  ;

CREATE PROCEDURE
-- 
--  Proposito: Servicio para la creacion de Requisito Operativo
--  Autor: JG - BCS
--  Fecha Creacion: 08/04/2016
-- 

 "BSCL.Plataforma.BD.Servicios::REQ.CrearRequisitoOperativo"  (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						BIGINT,			-- ID  Empresa destino
	IN  p_IdUsuario						BIGINT,			-- ID  del usuario que solicita el servicio
	IN  p_IdModulo						NVARCHAR(3),  	-- ID  del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 			NVARCHAR(1)	,	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
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
		--DECLARE v_FechaSistema	SECONDDATE;-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'

		--DECLARE v_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));
		DECLARE v_CountModeloRequisitoOperativo	BIGINT := 0;
		DECLARE v_IdModeloRequisitoOperativo	NVARCHAR(40);
		
		-----------------------------------------------------
		--Validaciones básicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		
		-- Se valida exista el ModeloRequisitoOperativo por empresa y este activo
		BEGIN
			SELECT COUNT(1) INTO v_CountModeloRequisitoOperativo 
			  FROM "BSCL.Plataforma.BD::REQ.ModeloRequisitoOperativo"
			 WHERE "IdEmpresa"	= :p_IdEmpresa
			   AND "IdEstado"	= 'ACT';
			
			IF (v_CountModeloRequisitoOperativo < 1) THEN
				p_out_ErroresDetalle = select 'MODELO_REQUISITO_OPERATIVO_NO_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			IF (v_CountModeloRequisitoOperativo > 1) THEN
				p_out_ErroresDetalle = select 'MODELO_REQUISITO_OPERATIVO_INCORRECTO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
		END;
		--------------------
		--Negocio Aqui
		--------------------

		BEGIN
		--CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaSistema);
		
		SELECT "BSCL.Plataforma.BD.Secuencias::IdRequisitoOperativo".NEXTVAL INTO p_out_RefId FROM DUMMY;
		
		SELECT "IdModeloRequisitoOperativo" INTO v_IdModeloRequisitoOperativo
		  FROM "BSCL.Plataforma.BD::REQ.ModeloRequisitoOperativo"
		 WHERE "IdEmpresa"	= :p_IdEmpresa
		   AND "IdEstado"	= 'ACT';
				   
		INSERT INTO "BSCL.Plataforma.BD::REQ.RequisitoOperativo" (
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
		) 	
		VALUES(
			:p_out_RefId,
			:v_IdModeloRequisitoOperativo,
			:p_IdCondicionOperativa,
			:p_IdTipoEtapa,
			:p_IdTipoComportamiento,
			:p_IdTipoCanalIngreso,
			:p_IdMoneda,
			:p_ValorAtributo1,
			:p_ValorAtributo2,
			:p_IdUsuario,
			CURRENT_UTCTIMESTAMP,
			CURRENT_UTCTIMESTAMP,
			:p_IdEmpresa,
			:p_IdEstado
		);
		
		--p_out_RefId:=1;
		  
	-- Parametros entrada del servicio publico				 

		END;		
	
		--------------------
		-- LOG 
		--------------------
		
		/*v_DetalleLog = Select 'IdRequisitoOperativo' as "SubRegistro", 'RequisitoOperativo' as "CampoRegistro", 
						'' as "ValorAntiguo", '' as "p_IdRequisitoOperativo" from DUMMY WHERE 1=2;
						
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
			p_IdTipoLog => 'INGRESO' 
			,p_IdModulo => 'REQ' 
			,p_IdEmpresa => p_IdEmpresa
			,p_IdUsuario => p_IdUsuario 
			,p_FechaSistemaLog => v_FechaSistema
			,p_IdUsuarioAtendido => p_IdUsuario
			,p_IdTipoRegistro => 'REQUISITOOPERATIVO'
			,p_IdRegistro => p_IdRequisitoOperativo
			,p_DetalleLog => :v_DetalleLog
			,p_NombreProceso => 'CrearRequisitoOperativo'
		);*/
				
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;