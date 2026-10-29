SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Orden.BD.Reglas::TransaccionExiste";

CREATE PROCEDURE
-- 
--  Proposito: Regla para validar si la transaccion existe
--  Autor: - BCS
--  Fecha Creacion: 20/07/2015
--  
"BSCL.Orden.BD.Reglas::TransaccionExiste" (
	IN p_IdTransaccion  BIGINT,
	
	-- Parametros utilizados por el motor de reglas
	IN  p_IdEjecucion			 bigint,  --id entregado por el motor de reglas,0 = omite
    OUT p_out_ResultadoRegla 	 NVARCHAR(1), --'S' si se cumple la validacion xxExiste
    OUT p_out_MensajeRegla       NVARCHAR(520) --Solo de uso interno
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
--READS SQL DATA AS
AS
BEGIN
	--------------------
	--DECLARE LOGICA
	-------------------- 
 	DECLARE v_Quantity   BIGINT;

	--------------------
	--DECLARE SALIDA
	--------------------	
	DECLARE v_codigoMensaje NVARCHAR(40) :='REGLA_OK';
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		DECLARE v_hashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
		if v_codigoMensaje = 'REGLA_OK'	then
			p_out_ResultadoRegla := 'S';			
			p_out_MensajeRegla   := 'OK++';
		else
			--Arma el mensaje de salida de acuerdo a codigo de mensaje y los valores pasados
			p_out_ResultadoRegla := 'N';
			v_hashmsg = select 'IdTransaccion' as "Codigo",CAST (:p_IdTransaccion AS NVARCHAR) as "Valor"   from dummy;			    	
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_codigoMensaje,:v_hashmsg, :p_out_MensajeRegla);			
		end if;						 	
	END;	
	
	
	--------------------
	--LOGICA 
	--------------------	   
	v_Quantity := 0 ;	
	SELECT COUNT("IdTransaccion") INTO v_Quantity FROM "BSCL.Orden.BD::ORD.Transaccion"
	WHERE "IdTransaccion" = p_IdTransaccion;

 	IF (:v_Quantity > 0) THEN
	    v_codigoMensaje := 'REGLA_OK';
	    SIGNAL SALIDA_CONTROLADA;
 	END IF;
 	 	
	--------------------
	--TERMINO NoOK 
	--------------------	 	 	
    v_codigoMensaje := 'TRANSACCION_NO_EXISTE';
    SIGNAL SALIDA_CONTROLADA; 	
 	 	 	
END;
