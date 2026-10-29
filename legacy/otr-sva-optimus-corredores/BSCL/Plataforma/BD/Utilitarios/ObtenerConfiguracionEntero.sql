SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracionEntero";

CREATE PROCEDURE 
-- 
--  Proposito: permite obtener el valor de un parametro de configuracion
--  Autor: CSI - BCS
--  Fecha Creacion: 20/07/2015
--   
"BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracionEntero" (
	IN  p_IdEmpresa			             	 BIGINT,
	IN  p_CodigoParametro                    NVARCHAR(40),	
	OUT p_out_Valor                          INT
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
READS SQL DATA
AS
BEGIN
	DECLARE PARAMETRO_DE_CONFIGURACION_NO_EXISTE CONDITION FOR SQL_ERROR_CODE 10007;  
	declare v_cantidad int := 0;
	

	
    SELECT COUNT("CodigoParametro") INTO v_cantidad FROM "BSCL.Plataforma.BD::PLA.Configuracion"
	WHERE  "IdEmpresa" = p_IdEmpresa and "CodigoParametro" = p_CodigoParametro;

 	IF (:v_cantidad = 0) THEN
		 SIGNAL PARAMETRO_DE_CONFIGURACION_NO_EXISTE; 
 	END IF;

	SELECT CAST("Valor" as INT) INTO p_out_Valor
	FROM "BSCL.Plataforma.BD::PLA.Configuracion"
	WHERE  "IdEmpresa" = p_IdEmpresa and "CodigoParametro" = :p_CodigoParametro;
		 	 	
    		 	
END;







