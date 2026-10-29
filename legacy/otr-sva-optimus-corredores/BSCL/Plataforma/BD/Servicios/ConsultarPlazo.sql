SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::ConsultarPlazo";

CREATE PROCEDURE
-- 
--  Proposito: Encapsular el servicio privado ObtenerPlazo y transformarlo en un servicio publico
--  Autor: Oscar Carreno Tolmo (OCT)
--  Fecha Creacion: 01/12/2015 
--    
"BSCL.Plataforma.BD.Servicios::ConsultarPlazo" ( 
    --VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          BIGINT,
    IN p_IdUsuario          BIGINT,
    IN p_IdModulo           NVARCHAR(3),
    IN p_UsaReglasNegocio	nvarchar(1),	-- S/N se usa en caso de servicios anidados.
    
    --ENUMERAR VARIABLES DE ENTRADA
    IN  p_FechaDesde	NVARCHAR(10),
    IN  p_FechaHasta	NVARCHAR(10),
    IN  p_DiasHabiles	NVARCHAR(1) default 'N',
 	IN  p_IdCondicionDeLiquidacion	NVARCHAR(2) default 'T0',
	IN  p_IdPais		BIGINT, 
	--ENUMERAR VARIABLES DE SALIDA	
	OUT p_out_PlazoDias	INTEGER,

	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
	
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
			--Error interno no manejado  (El log ser� captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;
		
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	

	--------------------
	--Negocio Aqui
	--------------------
	BEGIN
		DECLARE v_DiasCondicionDeLiquidacion INT;
		SELECT COALESCE("PlazoEnDias",0) INTO v_DiasCondicionDeLiquidacion from "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" where "IdCondicionDeLiquidacion" = p_IdCondicionDeLiquidacion;
	
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerPlazo" 
		(p_FechaDesde => :p_FechaDesde
		,p_FechaHasta => :p_FechaHasta
		,p_DiasHabiles => :p_DiasHabiles
		,p_DiasCondicionDeLiquidacion => :v_DiasCondicionDeLiquidacion
		,p_IdPais => :p_IdPais
		,p_out_PlazoDias => p_out_PlazoDias); 		
	END;
	

END;
