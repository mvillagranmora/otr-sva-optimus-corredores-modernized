SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Orden.BD.Servicios::BloquearDesbloquearOrdenes";

CREATE PROCEDURE 
-- 
--  Proposito: bloqueo masivo de ordenes
--  Autor: - BCS
--  Fecha Creacion: 20/07/2015
--  
"BSCL.Orden.BD.Servicios::BloquearDesbloquearOrdenes"(
    --VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          		BIGINT,
    IN p_IdUsuario          		BIGINT,
    IN p_IdModulo           		NVARCHAR(3),
 
    IN p_Accion						NVARCHAR(1), --B (BLOQUEAR), D(DESBLOQUEAR)
	IN p_Motivo						NVARCHAR(40),
	IN p_ordenes					TABLE ( "IdOrden" BIGINT),


	OUT p_out_ordenes				TABLE ( "IdOrden" BIGINT, "EstadoBloqueo" NVARCHAR(1), "Comentario" NVARCHAR(300)),

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
	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	p_out_ordenes				= select 0 as "IdOrden" , '' as "EstadoBloqueo", '' as "Comentario" from dummy where 1=2;
	
	BEGIN	    
	    
	    UPDATE ord
	    SET ord."Bloqueado" = CASE  
									WHEN p_accion = 'B' THEN 'S'
									WHEN p_accion = 'D' THEN 'N'
	    						END,
	    	ord."IdUsuarioBloqueo" = :p_IdUsuario,
	    	ord."MotivoBloqueo" = :p_Motivo
	    FROM :p_ordenes p_ord INNER JOIN "BSCL.Orden.BD::ORD.Orden" ord ON ord."IdOrden" = p_ord."IdOrden"
	    WHERE ord."Bloqueado" = CASE  
	    	WHEN p_accion = 'B' THEN 'N'
		    WHEN p_accion = 'D' THEN 'S'
		END;--'N' ;
	    
	    		
		p_out_ordenes = SELECT ORD."IdOrden","Bloqueado" as "EstadoBloqueo","MotivoBloqueo" as "Comentario" 
		from "BSCL.Orden.BD::ORD.Orden" as ORD inner join :p_ordenes as p_ORD on (ORD."IdOrden" = p_ORD."IdOrden");

	END ;	
END;
