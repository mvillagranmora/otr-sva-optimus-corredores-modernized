SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Orden.BD.Servicios::BloquearDesbloquearTransacciones";

CREATE PROCEDURE 
-- 
--  Proposito: bloqueo masivo de Transacciones
--  Autor: - BCS
--  Fecha Creacion: 20/07/2015
--  
"BSCL.Orden.BD.Servicios::BloquearDesbloquearTransacciones"(
    --VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          		BIGINT,
    IN p_IdUsuario          		BIGINT,
    IN p_IdModulo           		NVARCHAR(3),
 
    IN p_Accion						NVARCHAR(1), --B (BLOQUEAR), D(DESBLOQUEAR)
	IN p_Motivo						NVARCHAR(40),
	IN p_transacciones				TABLE ( "IdTransaccion" BIGINT),


	OUT p_out_transacciones				TABLE ( "IdTransaccion" BIGINT, "EstadoBloqueo" NVARCHAR(1), "Comentario" NVARCHAR(300)),

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
	    
	    UPDATE  tran
	    SET tran."Bloqueado" = CASE  
									WHEN p_accion = 'B' THEN 'S'
									WHEN p_accion = 'D' THEN 'N'
	    						END,
	    	tran."IdUsuarioBloqueo" = :p_IdUsuario,
	    	tran."MotivoBloqueo" = :p_Motivo
	    FROM :p_transacciones p_tran
	    INNER JOIN "BSCL.Orden.BD::ORD.Transaccion" tran ON p_tran."IdTransaccion" = tran."IdTransaccion"
	    WHERE tran."Bloqueado" = CASE  
	    	WHEN p_accion = 'B' THEN 'N'
		    WHEN p_accion = 'D' THEN 'S'
	    END;--'N' ;


		
		p_out_transacciones = SELECT TR."IdTransaccion","Bloqueado" as "EstadoBloqueo","MotivoBloqueo" as "Comentario" 
		from "BSCL.Orden.BD::ORD.Transaccion" as TR inner join :p_transacciones as p_TR on (TR."IdTransaccion" = p_TR."IdTransaccion");

	END ;	
END;
