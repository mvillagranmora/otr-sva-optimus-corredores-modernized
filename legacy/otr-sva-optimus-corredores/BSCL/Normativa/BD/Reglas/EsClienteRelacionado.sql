SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Reglas::EsClienteRelacionado";


--call "BSCL.Normativa.BD.Reglas::EsClienteRelacionado"(179627,0,?,?)


CREATE PROCEDURE "BSCL.Normativa.BD.Reglas::EsClienteRelacionado" (  
	IN p_IdPersona BIGINT,
	
     -- Parametros utilizados por el motor de reglas
	 IN  p_IdEjecucion		  BIGINT,       --id entregado por el motor de reglas,0 = omite
     OUT p_out_ResultadoRegla NVARCHAR(1), --'S' si se cumple la validacion xxExiste
     OUT p_out_MensajeRegla   NVARCHAR(520) --Solo de uso interno	
	
	) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA BSCL
	AS
BEGIN

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
		   v_hashmsg = select 'IdPersona' as "Codigo", p_IdPersona as "Valor" from dummy;		
				    	
   		   CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_codigoMensaje,:v_hashmsg, :p_out_MensajeRegla);
		end if;
						 	
	END;	

	BEGIN

		DECLARE v_Quantity   BIGINT;
	   
		DECLARE v_IdPersonaCorredora BIGINT;
		
		--v_tablaIdPersonaRel = select * from "BSCL.SocioNegocio.BD::REL.PersonasRelacionadas" where 1=2;
		
		v_IdPersonaCorredora := 0;

		Select "IdPersona" into v_IdPersonaCorredora FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" where "Nombre"='HANA CORREDORA';
		
	
		
		--rescatar IdPersona e igualar al idpersona entrante de este sp
		
		CALL "BSCL.SocioNegocio.BD.Servicios::REL.ConsultarRelacionPersona" (p_IdEmpresa => 1,p_IdPersona => v_IdPersonaCorredora ,p_out_PersonasRelacionadas => v_tablaIdPersonaRel);
	
	 select count("IdPersona") into v_Quantity from :v_tablaIdPersonaRel where "IdPersona" = p_IdPersona;
	
	 	IF (:v_Quantity > 0) THEN
	 		v_codigoMensaje := 'REGLA_OK';
	    	SIGNAL SALIDA_CONTROLADA;
	 	END IF;

	--------------------
	--TERMINO NoOK 
	--------------------	 	 	
    v_codigoMensaje := 'REGLA_NO_OK';
    SIGNAL SALIDA_CONTROLADA;

	END;
 
END;


