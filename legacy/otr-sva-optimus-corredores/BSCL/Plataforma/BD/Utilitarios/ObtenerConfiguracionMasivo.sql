SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracionMasivo";

CREATE PROCEDURE 
-- 
--  Proposito: permite obtener el valor de un parametro de configuracion
--  Autor: - CHERNANDEZ - BCS
--  Fecha Creacion: 19/04/2016
--   
"BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracionMasivo" (
	IN  p_IdEmpresa			             	 BIGINT,
	IN  p_CodigosParametros                  TABLE("CodigoParametro" NVARCHAR(40)),	
	OUT p_out_Valores                        TABLE("CodigoParametro" NVARCHAR(40), "Valor" NVARCHAR(520))
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
READS SQL DATA
AS
BEGIN
	DECLARE PARAMETRO_DE_CONFIGURACION_NO_EXISTE CONDITION FOR SQL_ERROR_CODE 10007;  
	declare v_cantidad int := 0;

 	p_out_Valores = SELECT C."CodigoParametro", C."Valor"
					FROM "BSCL.Plataforma.BD::PLA.Configuracion" AS C
					INNER JOIN :p_CodigosParametros AS CP 
					ON C."CodigoParametro" = CP."CodigoParametro"
					WHERE  "IdEmpresa" = p_IdEmpresa;
	
	select count("Valor") into v_cantidad from :p_out_Valores;
	
	IF (:v_cantidad = 0) THEN
		 SIGNAL PARAMETRO_DE_CONFIGURACION_NO_EXISTE; 
 	END IF;	 	 	
    		 	
END;







