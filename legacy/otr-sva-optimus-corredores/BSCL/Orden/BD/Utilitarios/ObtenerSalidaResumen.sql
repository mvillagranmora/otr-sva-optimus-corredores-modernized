SET SCHEMA "BSCL";

DROP FUNCTION "BSCL.Orden.BD.Utilitarios::ObtenerSalidaResumen";

CREATE FUNCTION
-- 
--  Proposito: Obtiene el resumen de errores
--  Autor: - BCS
--  Fecha Creacion: 27/07/2015 
--     
"BSCL.Orden.BD.Utilitarios::ObtenerSalidaResumen" ( 
	IN p_SalidaDetalle	"BSCL.Orden.BD.Tipos::SalidaDetalle"
) RETURNS "BSCL.Orden.BD.Tipos::SalidaResumen"
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA BSCL 
	READS SQL DATA AS
BEGIN
	
	v_count2 = SELECT "IdInterno", "IdOperacion",
		count(CASE WHEN "Resultado" = 'OK' THEN 1 ELSE NULL END) as "OK",
		count(CASE WHEN "Resultado" = 'ADVERTENCIA' THEN 1 ELSE NULL END) as "ADVERTENCIA",
		count(CASE WHEN "Resultado" = 'AUTORIZACION' THEN 1 ELSE NULL END) as "AUTORIZACION",
		count(CASE WHEN "Resultado" = 'RECHAZO' THEN 1 ELSE NULL END) as "RECHAZO"
		FROM :p_SalidaDetalle 
		group by "IdInterno","IdOperacion";
	
			
	RETURN	SELECT "IdInterno","IdOperacion",
						CASE 
							WHEN "RECHAZO" > 0 THEN 'RECHAZO'
							WHEN "AUTORIZACION" > 0 THEN 'AUTORIZACION'
							WHEN "ADVERTENCIA" > 0 THEN 'ADVERTENCIA'
							WHEN "OK" > 0 THEN 'OK'
							ELSE 'OK'
						END as "Resultado","OK","ADVERTENCIA","AUTORIZACION","RECHAZO"
					FROM :v_count2
					ORDER BY "IdInterno";
  
END;
 
