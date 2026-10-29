SET SCHEMA "BSCL";

DROP FUNCTION "BSCL.Plataforma.BD.Utilitarios::EsNumeroEntero";

CREATE FUNCTION 
-- 
--  Proposito: Validar que dato sea un numero entero
--  Fecha Creacion: 31/07/2015
--  
"BSCL.Plataforma.BD.Utilitarios::EsNumeroEntero" (
    p_numero NVARCHAR(520)
) 
	RETURNS p_out_resultado NVARCHAR(1) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER AS
BEGIN
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		p_out_resultado := 'N';
	END;
	IF (FLOOR(:p_numero) = :p_numero) THEN 
	   p_out_resultado := 'S';
	ELSE
	   p_out_resultado := 'N';
	END IF;
END;
