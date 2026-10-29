SET SCHEMA "BSCL";
DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::INS.ModificarRelacionInstrumento" ;

--  Proposito: Servicio para la modificacion de relación Instrumento
--  Autor: JPZ - BCS
--  Fecha Creacion: Mayo-2016

CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::INS.ModificarRelacionInstrumento" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					bigint,			-- ID  Empresa destino
	IN  p_IdUsuario					bigint,			-- ID  del usuario que solicita el servicio
	IN  p_IdModulo					nvarchar(3),  	-- ID  del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 		nvarchar(1)	,	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN p_IdRelacionInstrumento 						BIGINT 				,
	IN p_IdTipoRelacionInstrumento					NVARCHAR(6) 	default '' 	,			
	IN p_IdClasificacionTipoInstrumento				NVARCHAR(6) 	default '' 	,
	IN p_IdTipoInstrumento  		 				NVARCHAR(3)   	default ''	,			
	IN p_IdInstrumento								BIGINT			default 0 	,		
	IN p_Nemo 										NVARCHAR(40)	default '' 	,			
	IN p_IdClasificacionTipoInstrumentoRelacionado	NVARCHAR(6) 	default '' 	,			
	IN p_IdTipoInstrumentoRelacionado  				NVARCHAR(3)   	default ''	,			
	IN p_IdInstrumentoRelacionado					BIGINT			default 0 	,		
	IN p_NemoRelacionado							NVARCHAR(40)	default '' 	,			
	IN p_Descripcion								NVARCHAR(100)	default '' 	,			
	IN p_FechaInicioRelacion						NVARCHAR(10) 	default '0001-01-01',     
	IN p_FechaFinRelacion							NVARCHAR(10) 	default '0001-01-01',     
	IN p_IdEstado 									NVARCHAR(3) 	default '' 	,	
		
	-- Parametros salida del servicio publico
--    OUT p_out_RefId 						bigint,
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
	BEGIN	
		--------------------
		--Declare Variables
		--------------------
		--DECLARE v_IdPerfilCobros			 	bigint:=0;
		DECLARE v_FechaIngreso					SECONDDATE;-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
		DECLARE v_CountIdPersona				BIGINT:=0;
		DECLARE	v_IdPersona						BIGINT:=0;

		-----------------------------------------------------
		--Validaciones básicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		
		--------------------
		--Negocio Aqui
		--------------------

		
		BEGIN
	
			p_out_AdvertenciasContador	:= 0;
			p_out_ErroresContador		:= 0;
			p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
			p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
			
			------------------------------------------
			--Actualizar BOVEDA
			------------------------------------------

			UPDATE "BSCL.Plataforma.BD::INS.RelacionInstrumento"
			SET
				"IdTipoRelacionInstrumento"					= :p_IdTipoRelacionInstrumento,				
				"IdClasificacionTipoInstrumento"			= :p_IdClasificacionTipoInstrumento,
				"IdTipoInstrumento"							= :p_IdTipoInstrumento,
				"IdInstrumento"								= :p_IdInstrumento,
				"Nemo"										= :p_Nemo,
				"IdClasificacionTipoInstrumentoRelacionado"	= :p_IdClasificacionTipoInstrumentoRelacionado,
				"IdTipoInstrumentoRelacionado"				= :p_IdTipoInstrumentoRelacionado,  
				"IdInstrumentoRelacionado"					= :p_IdInstrumentoRelacionado,		
				"NemoRelacionado"							= :p_NemoRelacionado,
				"Descripcion"								= :p_Descripcion,
				"FechaInicioRelacion"						= :p_FechaInicioRelacion,
				"FechaFinRelacion"							= :p_FechaFinRelacion,
				"IdUsuario"									= :p_IdUsuario,
				"IdEstado"									= :p_IdEstado
			WHERE "IdRelacionInstrumento" = :p_IdRelacionInstrumento;


			--p_out_RefId:=1;	
			
		END;		
	
		--------------------
		-- Normativo 
		--------------------
		
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;