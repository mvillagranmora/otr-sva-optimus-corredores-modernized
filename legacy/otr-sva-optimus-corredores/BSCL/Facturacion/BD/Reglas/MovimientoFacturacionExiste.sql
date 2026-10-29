SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Facturacion.BD.Reglas::MovimientoFacturacionExiste" ;

CREATE PROCEDURE "BSCL.Facturacion.BD.Reglas::MovimientoFacturacionExiste" 
    (
	  IN p_IdMovimientoComprobante       BIGINT, 
	     
      -- Parametros utilizados por el motor de reglas
	  IN  p_IdEjecucion		   BIGINT,       --id entregado por el motor de reglas,0 = omite
      OUT p_out_ResultadoRegla NVARCHAR(1), --'S' si se cumple la validacion xxExiste
      OUT p_out_MensajeRegla   NVARCHAR(520) --Solo de uso interno	
    ) 
    
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER   
	DEFAULT SCHEMA "BSCL"
	--READS SQL DATA     

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
		--Arma el mensaje de salida de acuerdo a codigo de mensaje y los valores pasados
		DECLARE v_hashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));	
		
		if v_codigoMensaje = 'REGLA_OK'	then
		   p_out_ResultadoRegla := 'S';
		   p_out_MensajeRegla := 'ok++';			
		else
		   p_out_ResultadoRegla := 'N';
		   v_hashmsg = select 'IdMovimientoComprobante' as "Codigo", p_IdMovimientoComprobante as "Valor" from dummy;		
						    	
		   CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_codigoMensaje,:v_hashmsg, :p_out_MensajeRegla);
		end if;
			
		if p_IdEjecucion <> 0 then
			insert into "BSCL.Plataforma.BD::MRE.ReglaEjecucion" ("Id","Resultado","Mensaje") 
			values (p_IdEjecucion,p_out_ResultadoRegla,p_out_MensajeRegla);
		end if;					 	
	END;	
	
	
	--------------------
	--LOGICA 
	--------------------
	v_Quantity := 0;
		   	
	SELECT COUNT("IdMovimientoComprobante")
	INTO v_Quantity
    FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
    WHERE "IdMovimientoComprobante" = p_IdMovimientoComprobante;

 	IF (:v_Quantity > 0) THEN
	    v_codigoMensaje := 'REGLA_OK';
	    SIGNAL SALIDA_CONTROLADA;
 	END IF;
 	 	
	--------------------
	--TERMINO NoOK 
	--------------------	 	 	
    v_codigoMensaje := 'MOVIMIENTO_FACTURACION_NO_EXISTE';
    SIGNAL SALIDA_CONTROLADA;

END;