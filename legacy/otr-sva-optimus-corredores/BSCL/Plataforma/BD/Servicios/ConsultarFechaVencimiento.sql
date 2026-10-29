SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::ConsultarFechaVencimiento";

CREATE PROCEDURE
-- 
--  Proposito: obtener la fecha de vencimiento de una operacion a partir de la fecha de ingreso + fecha de liquidacion + plazo en dias
--  Autor: dars
--  Fecha Creacion: 24/08/2015 

"BSCL.Plataforma.BD.Servicios::ConsultarFechaVencimiento" ( 
    --VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa              	BIGINT,
    IN p_IdUsuario              	BIGINT,
    IN p_IdModulo               	NVARCHAR(3),
    IN p_UsaReglasNegocio			NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
    
    --ENUMERAR VARIABLES DE ENTRADA
    IN  p_FechaIngreso				NVARCHAR(10),	
    IN  p_IdTipoOperacion			NVARCHAR(6),
    IN  P_IdCondicionDeLiquidacion	NVARCHAR(2),
	IN  p_IdPais					BIGINT,
	IN  p_Plazo						INTEGER,
	IN  p_DiasHabiles				NVARCHAR(1) default 'N',
    --VARIABLES DE SALIDA OBLIGATORIAS
    OUT p_out_FechaVencimiento		NVARCHAR(10),   
    OUT p_out_EsHabil				NVARCHAR(1),   


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
 		DECLARE v_out_FechaLiquidacion	NVARCHAR(10);
 		
    	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaLiquidacion" (:p_FechaIngreso,:p_IdTipoOperacion,:P_IdCondicionDeLiquidacion,:p_IdPais,v_out_FechaLiquidacion);		
    		
    	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerDiaSiguiente" ( 	
		    :v_out_FechaLiquidacion,						--IN  p_Fecha			NVARCHAR(10),
		    :p_Plazo,									--IN	p_NumeroDias	INTEGER,
		    :p_DiasHabiles,										--IN  p_EsHabil		NVARCHAR(1),
			:p_IdPais,									--IN  p_IdPais		BIGINT, 
			p_out_FechaVencimiento						--OUT p_out_FechaSiguente  NVARCHAR(10)
		);
		
		CALL "BSCL.Plataforma.BD.Utilitarios::DiaEsHabil" (:p_out_FechaVencimiento,:p_IdPais,:p_out_EsHabil); 
    		    	
	END;
END;
