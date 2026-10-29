SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::ConsultarFechaLiquidacion";

CREATE PROCEDURE
-- 
--  Proposito: Encapsular el servicio privado ObtenerFechaLiquidacion y transdormalo en un servicio publico
--  Autor: Oscar Carreno Tolmo (OCT)
--  Fecha Creacion: 24/08/2015 
--    
"BSCL.Plataforma.BD.Servicios::ConsultarFechaLiquidacion" ( 
    --VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa              BIGINT,
    IN p_IdUsuario              BIGINT,
    IN p_IdModulo               NVARCHAR(3),
    IN p_UsaReglasNegocio		nvarchar(1),	-- S/N se usa en caso de servicios anidados.
    
    --ENUMERAR VARIABLES DE ENTRADA
    IN  p_FechaIngreso				NVARCHAR(10),	
    IN  p_IdTipoOperacion			NVARCHAR(6) DEFAULT NULL,
    IN  P_IdCondicionDeLiquidacion	NVARCHAR(2),
	IN  p_IdPais					BIGINT,
	    
    --VARIABLES DE SALIDA OBLIGATORIAS
    OUT p_out_FechaLiquidacion		NVARCHAR(10),   


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
    	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaLiquidacion" (:p_FechaIngreso,:p_IdTipoOperacion,:P_IdCondicionDeLiquidacion,:p_IdPais,p_out_FechaLiquidacion);		
	END;
	

END;
