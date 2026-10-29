SET SCHEMA "BSCL";

DROP FUNCTION "BSCL.SocioNegocio.BD.Utilitarios::SOC.ObtenerEstadoEnrolamiento";

CREATE FUNCTION 
-- 
--  Proposito: Obtener el estado de Enrolamiento segun combinatoria del campo IdEstado y EsEnrolado 
--  Autor: CFLORES - BCS
--  Fecha Creacion: 26-01-2016
--  


--NO USAR FUNCION, USAR ESTO
---CASE "XXX"."IdEstado" WHEN 'ACT' THEN CASE "XXX"."EsEnrolado" WHEN 'N' THEN 'PEN' WHEN 'N' THEN 'PEN' ELSE 'ACT' END ELSE 'BLO' END AS "IdEstado",
"BSCL.SocioNegocio.BD.Utilitarios::SOC.ObtenerEstadoEnrolamiento" (
    p_IdEstado 		NVARCHAR(3),
    P_EsEnrolado	NVARCHAR(1)
) 
	RETURNS p_out_estado NVARCHAR(3) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER AS
BEGIN
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	
	BEGIN
		p_out_estado := 'BLO';
	END;
	
/*
		Combinatoria			Salida
		IdEstado	EsEnrolado	Estado UI
		ACT			S			ACT
		ACT			N			PEN
		BLO			S			BLO
		BLO			N			BLO
*/

	
	IF ( p_IdEstado = 'ACT') THEN 
		IF ( P_EsEnrolado = 'N' OR P_EsEnrolado = 'P') THEN 
	 		 p_out_estado := 'PEN';
	 	ELSE
		 	 p_out_estado := 'ACT';
	 	END IF;	  
	ELSE
	   p_out_estado := 'BLO';
	END IF;
	
	
	
END;
