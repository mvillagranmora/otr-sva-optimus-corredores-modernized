SET SCHEMA "BSCL";

DROP FUNCTION "BSCL.SocioNegocio.BD.Utilitarios::SOC.ObtenerEstadoAutorizacion";

CREATE FUNCTION 
-- 
--  Proposito: Obtener el estado de la autorizacion segun combinatoria del campo IdEstado y IdEstadoAutorizacion 
--  Autor: CFLORES - BCS
--  Fecha Creacion: 27-01-2016
--  
"BSCL.SocioNegocio.BD.Utilitarios::SOC.ObtenerEstadoAutorizacion" (
	p_IdTipoEntidad			NVARCHAR(40),
    p_IdEstado 				NVARCHAR(3),
    P_IdEstadoAutorizacion	NVARCHAR(3)
) 
	RETURNS p_out_estado NVARCHAR(3) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER AS
BEGIN
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	
	BEGIN
		p_out_estado := 'INA';
	END;
	
/*
IdEstado	IdEstadoAutorizacion	UI
ACT			PEN						PEN 
INA			PEN						PEN 
ACT			ING						ACT
INA			ING						INA


Post Autorizacion			
1)		IdEstado	IdEstadoAutorizacion	UI
AUT ->	ACT			AUT						ACT
RCZ ->	INA			RZD						INA
2)			
AUT->	INA			AUT						INA
RZD ->	INA			RZD						INA

*/

	
	IF ( p_IdEstado = 'ACT' OR p_IdEstado = 'VIG') THEN 
	
		IF ( P_IdEstadoAutorizacion = 'PEN'  OR P_IdEstadoAutorizacion = '') THEN 
	 		 p_out_estado := 'PEN';
	 	ELSE
		 	 p_out_estado := p_IdEstado;--'ACT';
	 	END IF;	  
	 	
	ELSE
		p_out_estado := 'INA';
		IF RTRIM(p_IdTipoEntidad)='DOCUMENTOIDENTIDAD' THEN
		   p_out_estado := 'VCD';
	   	END IF;
		IF RTRIM(p_IdTipoEntidad)='PORTAFOLIO' THEN
		   p_out_estado := 'BLO';
	   	END IF;
	   	
	   	
	END IF;
	
	
	
END;