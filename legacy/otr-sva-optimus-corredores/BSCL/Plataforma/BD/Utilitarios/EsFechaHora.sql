SET SCHEMA "BSCL";
DROP FUNCTION "BSCL.Plataforma.BD.Utilitarios::EsFechaHora";

CREATE FUNCTION 
-- 
--  Proposito: Validar que el dato enviado sea una fecha
--  Autor: NRM :: BCS
--  Fecha Creacion: 23/09/2015
--  
"BSCL.Plataforma.BD.Utilitarios::EsFechaHora" (
    p_Fecha NVARCHAR(40)
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
	v_Fecha:= TO_TIMESTAMP (p_Fecha, 'YYYY-MM-DD HH24:MI:SS.FF7');
	p_out_resultado := 'S';
END;