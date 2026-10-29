SET SCHEMA "BSCL";


DROP PROCEDURE  "BSCL.Portafolio.BD.Servicios::ModificarAnotacionPortafolio";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para la modificacion de anotaciones de portafolio.
--  Autor: CAPONTE - BCS
--  Fecha Creacion: 07/02/2016
-- 

"BSCL.Portafolio.BD.Servicios::ModificarAnotacionPortafolio" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					bigint,			-- ID  Empresa destino
	IN  p_IdUsuario					bigint,			-- ID  del usuario que solicita el servicio
	IN  p_IdModulo					nvarchar(3),  	-- ID  del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 		nvarchar(1)	,	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
   	IN  p_IdPortafolio					BIGINT,
   	IN  p_IdAnotacion					BIGINT,
   	IN  p_Titulo						NVARCHAR(100),
   	IN  p_Detalle						NVARCHAR(500),
   	IN  p_IdMotivo						NVARCHAR(6),
   	IN  p_IdAmbito						NVARCHAR(6),
   	IN  p_IdRelevancia					NVARCHAR(6),
   	IN  p_Borrado						NVARCHAR(1) DEFAULT 'N',
   	IN  p_FechaDesde					NVARCHAR(10),
   	IN  p_FechaHasta					NVARCHAR(10),

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
			call "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
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
		
		DECLARE v_FechaIngreso					SECONDDATE;-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
		
		--PRONETIT
		DECLARE v_countIdPortafolio 			INT:=0;
		DECLARE v_IdEntidad						BIGINT;
		DECLARE v_TipoEntidadReplica			NVARCHAR(50):= 'ANOTACION';
		DECLARE v_TipoOperacion					NVARCHAR(1):= 'U';
		-----------------------------------------------------
		--Validaciones básicas (Codigos,Existencias,etc)
		---------------------------------------------------

					
		--------------------
		--Negocio Aqui
		--------------------

		
		BEGIN
			
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaIngreso);
		
				
			UPDATE 	"BSCL.Portafolio.BD::POR.Anotacion"
			SET "Titulo" 				= p_Titulo, 
				"Detalle"				= p_Detalle,
				"IdMotivoAnotacion" 	= p_IdMotivo,
				"FechaModificacion" 	= v_FechaIngreso,
				"IdAmbitoAnotacion" 	= p_IdAmbito,
				"IdRelevanciaAnotacion" = p_IdRelevancia,
				"Borrado"				= p_Borrado,
				"ValidoDesde"			= p_FechaDesde,
				"ValidoHasta"			= p_FechaHasta
			WHERE 	"IdAnotacion" 		= p_IdAnotacion
			AND 	"IdPortafolio" 		= p_IdPortafolio;
			
			p_out_RefId = :p_IdAnotacion;
			
			
			--PRONETIT
			
			v_IdEntidad	:= :p_out_RefId;

			SELECT COUNT("IdPortafolio") INTO v_countIdPortafolio
			FROM "BSCL.Portafolio.BD::POR.Portafolio"
			WHERE "IdPortafolio" = :p_IdPortafolio
			AND "Proposito" = 'PROPOSITOAPV';
			
			IF(v_countIdPortafolio > 0)THEN
				CALL "BSCL.APV.BD.Servicios::ReplicaPortafolioAPV" (
					:p_IdEmpresa,
					:p_IdUsuario,
					:p_IdModulo,
					:p_UsaReglasNegocio,
					:p_IdPortafolio, 
					v_IdEntidad,
					v_TipoEntidadReplica, 
					v_TipoOperacion,
					p_out_AdvertenciasContador,
					p_out_AdvertenciasDetalle,
					p_out_ErroresContador,
					p_out_ErroresDetalle
				);
	                
	            IF	p_out_ErroresContador > 0 then
					SIGNAL SALIDA_CONTROLADA;
			  	END if;
            END IF;   
		END;		
	
		--------------------
		-- Normativo 
		--------------------
		
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
