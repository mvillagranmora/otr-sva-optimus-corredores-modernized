SET SCHEMA "BSCL";

DROP FUNCTION "BSCL.Normativa.BD.Utilitarios::NOR.ObtenerEstadoAutorizacion";

CREATE FUNCTION 
-- 
--  Proposito: Obtener el estado de la autorizacion segun combinatoria del campo IdEstado y IdEstadoAutorizacion 
--  Autor: CFLORES - BCS
--  Fecha Creacion: 28-09-2016
--  
"BSCL.Normativa.BD.Utilitarios::NOR.ObtenerEstadoAutorizacion" (
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

	
	IF ( p_IdEstado = 'ACT' OR p_IdEstado = 'VIG' OR p_IdEstado = 'ACP') THEN 
	
		IF ( P_IdEstadoAutorizacion = 'PEN' OR P_IdEstadoAutorizacion = '') THEN 
	 		 p_out_estado := 'PEN';
	 	ELSE
		 	 p_out_estado := p_IdEstado;--'ACT';
	 	END IF;	  
	 	
	ELSE
		p_out_estado := 'INA';
		--IF RTRIM(p_IdTipoEntidad)='EVALUACIONLIMITE' THEN  
		 --p_out_estado := 'RZD';
		--END IF;
	END IF;
	
	
END;
