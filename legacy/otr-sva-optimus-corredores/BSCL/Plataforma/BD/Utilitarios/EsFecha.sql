SET SCHEMA "BSCL";

DROP FUNCTION "BSCL.Plataforma.BD.Utilitarios::EsFecha";

CREATE FUNCTION 
-- 
--  Proposito: Validar que el dato enviado sea una fecha
--  Autor: Oscar Carreno Tolmo :: OCT
--  Fecha Creacion: 31/07/2015
--  
"BSCL.Plataforma.BD.Utilitarios::EsFecha" (
    p_Fecha NVARCHAR(10)
) 
	RETURNS p_out_resultado NVARCHAR(1) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER AS
BEGIN
	DECLARE v_Fecha DATE;
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		p_out_resultado := 'N';
	END;
	v_Fecha:= TO_DATE(p_Fecha, 'YYYY-MM-DD');

	p_out_resultado := 'S';
END;