SET SCHEMA "BSCL";

DROP FUNCTION "BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion";

CREATE FUNCTION 
-- 
--  Proposito: traduce campos simples
--  Fecha Creacion: 31/07/2015
--  
"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion" (
    p_palabra NVARCHAR(100)
) 
	RETURNS p_out_resultado NVARCHAR(100) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER AS
BEGIN
	if (p_palabra = 'S') THEN
		p_out_resultado = 'SI';
	ELSEIF (p_palabra = 'N') THEN
		p_out_resultado = 'NO';
	ELSEIF (p_palabra = 'F') THEN
		p_out_resultado = 'FIJO';
	ELSEIF (p_palabra = 'V') THEN
		p_out_resultado = 'VARIABLE';
	ELSE
		p_out_resultado = '';
	END IF;
END;
