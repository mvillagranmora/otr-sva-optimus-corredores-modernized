SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Portafolio.BD.Servicios::ConsultarPreferenciaPortafolio";

CREATE PROCEDURE
-------------------------------------------------------------------------------- 
--  Proposito: Servicio para entregar el valor de una preferencia determinada.
--  Autor: JGARCES - BCS
--  Fecha Creacion: 15/02/2016
------------------------------------------------------------------------------- 

"BSCL.Portafolio.BD.Servicios::ConsultarPreferenciaPortafolio" (

	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					BIGINT,			-- ID  Empresa destino
	IN  p_IdUsuario					BIGINT,			-- ID  del usuario que solicita el servicio
		  
	-- Parametros entrada del servicio publico
	IN  p_IdPortafolio				BIGINT,
	IN	p_NombrePreferencia			NVARCHAR(255),
													
	-- Parametros salida del servicio publico

	OUT p_outValorPreferencia		NVARCHAR(255),
	OUT p_out_DetallePreferencia	TABLE (	
				"IdAtributo"        	BIGINT,
				"IdTipoAtributo"  		BIGINT,
				"NombreTipoAtributo"    NVARCHAR(255),
				"ClaseDato"         	NVARCHAR(40),
				"ValorAtributo"      	NVARCHAR(255),
				"IdEstadoAutorizado" 	NVARCHAR(3),
				"GrupoAtributo"       	NVARCHAR(40)
				
			),

    OUT p_out_RefId bigint,
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 	INT,
	OUT p_out_AdvertenciasDetalle 	TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador 		INT,
	OUT p_out_ErroresDetalle 		TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
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
		

		-----------------------------------------------------
		--Validaciones básicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		
		--------------------
		--Negocio Aqui
		--------------------		

			p_out_DetallePreferencia = SELECT TOP 1
				IFNULL(ATR."IdAtributo",0) AS "IdAtributo",
				TPO."IdTipoAtributo",
				TPO."NombreTipoAtributo", 
				TPO."ClaseDato", 
				IFNULL(ATR."ValorAtributo",' ') as "ValorAtributo", 
				IFNULL(ATR."IdEstadoAutorizacion",' ') as "IdEstadoAutorizado",
				TPO."GrupoAtributo"
			FROM 
				"BSCL"."BSCL.Plataforma.BD::PLA.TipoAtributo" TPO
				LEFT JOIN "BSCL"."BSCL.Plataforma.BD::PLA.Atributo" ATR 
					ON  ATR."IdTipoAtributo" = TPO."IdTipoAtributo"
			WHERE 
				TPO."IdTipoEntidad" = 'PORTAFOLIO' AND TPO."EsPreferencia" = 'S'
				AND TPO."IdEmpresa" = :p_IdEmpresa
				AND ATR."IdEntidad" = :p_IdPortafolio
				AND TPO."NombreTipoAtributo" = :p_NombrePreferencia -- crear campo con codigo. consultar con CS
			ORDER BY
				ATR."FechaModificacion" DESC;
			
			SELECT "ValorAtributo" INTO p_outValorPreferencia FROM :p_out_DetallePreferencia;
			
			p_out_RefId := 1; --::ROWCOUNT;	
	
		--------------------
		-- Normativo 
		--------------------
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
