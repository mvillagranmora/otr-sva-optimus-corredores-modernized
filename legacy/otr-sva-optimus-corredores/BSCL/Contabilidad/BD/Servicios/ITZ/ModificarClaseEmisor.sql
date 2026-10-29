SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Contabilidad.BD.Servicios::ITZ.ModificarClaseEmisor" ;
GO
CREATE PROCEDURE
-- 
--  Proposito: Servicio para la modificacion de Boveda
--  Autor: BCS
--  Fecha Creacion: 02/04/2018
-- 

"BSCL.Contabilidad.BD.Servicios::ITZ.ModificarClaseEmisor" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					bigint,			-- ID  Empresa destino
	IN  p_IdUsuario					bigint,			-- ID  del usuario que solicita el servicio
	IN  p_IdModulo					nvarchar(3),  	-- ID  del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 		nvarchar(1)	,	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN p_TipoOperacion 		NVARCHAR(6) ,
	IN p_ClaseInstrumento 	NVARCHAR(60) ,
	IN p_TipoBonoIRF 		NVARCHAR(3) ,
	IN p_OrigenInstrumento 	NVARCHAR(1) ,
	IN p_Plaza 				NVARCHAR(1) ,
	IN p_CodigoIFRS 		NVARCHAR(1) ,
	IN p_RelacionadoGrupo 	NVARCHAR(1) ,
	IN p_PresenciaBursatil 	NVARCHAR(1) ,
	IN p_FormadorMercado 	NVARCHAR(1) ,
	IN p_PactoAndino 		NVARCHAR(1) ,
	IN p_ClaseEmisor 		NVARCHAR(3) ,
	IN p_LargoPlazo 		NVARCHAR(1) , 
		
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
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaIngreso);
			
			------------------------------------------
			--Actualizar CLASE EMISOR
			------------------------------------------
	
			UPDATE "BSCL.Contabilidad.BD::ITZ.ClaseEmisor"
			SET
				"TipoOperacion"		= :p_TipoOperacion	  	,			
				"ClaseInstrumento"	= :p_ClaseInstrumento 	,
				"TipoBonoIRF"		= :p_TipoBonoIRF		, 
				"OrigenInstrumento"	= :p_OrigenInstrumento	,
				"Plaza"				= :p_Plaza				,
				"CodigoIFRS"		= :p_CodigoIFRS 		,
				"RelacionadoGrupo"	= :p_RelacionadoGrupo 	,
				"PresenciaBursatil"	= :p_PresenciaBursatil  ,
				"FormadorMercado"	= :p_FormadorMercado	, 		
				"PactoAndino"		= :p_PactoAndino		, 
				"ClaseEmisor"		= :p_ClaseEmisor 		,
				"LargoPlazo"		= :p_LargoPlazo			
			WHERE 
				"IdEmpresa"			= :p_IdEmpresa			AND 
				"TipoOperacion"		= :p_TipoOperacion	  	AND 			
				"ClaseInstrumento"	= :p_ClaseInstrumento 	AND
				"TipoBonoIRF"		= :p_TipoBonoIRF		AND
				"OrigenInstrumento"	= :p_OrigenInstrumento	AND
				"Plaza"				= :p_Plaza				AND
				"CodigoIFRS"		= :p_CodigoIFRS 		AND
				"RelacionadoGrupo"	= :p_RelacionadoGrupo 	AND
				"PresenciaBursatil"	= :p_PresenciaBursatil  AND
				"FormadorMercado"	= :p_FormadorMercado	AND	
				"ClaseEmisor"		= :p_ClaseEmisor		AND
				"PactoAndino"		= :p_PactoAndino					
			
			;

			p_out_RefId:=1;	
			
		END;		
	
		--------------------
		-- Normativo 
		--------------------
		
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
GO