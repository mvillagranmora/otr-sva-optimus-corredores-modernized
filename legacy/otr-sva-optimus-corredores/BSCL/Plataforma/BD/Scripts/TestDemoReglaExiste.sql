SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL"."BSCL.Plataforma.BD.Reglas::DemoReglaExiste";

CREATE PROCEDURE 
-- 
--  Proposito: Esqueleto para una validacion/regla de negocio
--  Autor: - BCS
--  Fecha Creacion: 20/07/2015
--  
"BSCL"."BSCL.Plataforma.BD.Reglas::DemoReglaExiste" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  IdValida           		 NVARCHAR(1), 
	
	-- Parametros utilizados por el motor de reglas
	IN  p_IdEjecucion			 bigint,  --id entregado por el motor de reglas,0 = omite
    OUT p_out_ResultadoRegla 	 NVARCHAR(1), --'S' si se cumple la validacion xxExiste
    OUT p_out_MensajeRegla       NVARCHAR(520) --Solo de uso interno
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE LOGICA
	--------------------	
	declare v_RutCliente nvarchar(100) := '12106100-2';
	declare v_NombreCliente nvarchar(100) := 'Juanito Perez';
	

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
			v_hashmsg = select 'REGLA_NOMBRE' as "Codigo",'DemoReglaExiste'  as "Valor" from dummy UNION ALL 
						select 'CLIENTE_RUT',v_RutCliente          from dummy UNION ALL 
						select	'CLIENTE_NOMBRE',v_NombreCliente   from dummy;			    	
			CALL "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_codigoMensaje,:v_hashmsg, :p_out_MensajeRegla);
		end if;	
		if p_IdEjecucion <> 0 then
			insert into "BSCL"."BSCL.Plataforma.BD::MRE.ReglaEjecucion" ("Id","Resultado","Mensaje") 
			values (p_IdEjecucion,p_out_ResultadoRegla,p_out_MensajeRegla);
		end if;	 					 	
	END;	
	--------------------
	--LOGICA 
	--------------------	
	
	if IdValida='N' then
	    v_codigoMensaje := 'RUT_INVALIDO'; --Codigo de mensaje debe estar en la tabla Diccionario
	    SIGNAL SALIDA_CONTROLADA;		
	end if;
	
	--------------------
	--TERMINO OK 
	--------------------
	v_codigoMensaje := 'REGLA_OK';
	SIGNAL SALIDA_CONTROLADA;
	
END;
