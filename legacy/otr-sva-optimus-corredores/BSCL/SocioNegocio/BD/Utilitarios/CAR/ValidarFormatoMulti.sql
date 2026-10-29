SET SCHEMA "BSCL";
GO
DROP FUNCTION "BSCL.SocioNegocio.BD.Utilitarios::CAR.ValidarFormatoMulti";
GO
-- 
--  Proposito: Valida formato "MULTI" de String principalmente para Indicador ORDMODALIDAD
--  Autor: GSALINI
--  Fecha Creacion: 11/04/2023
--  
CREATE FUNCTION 
"BSCL.SocioNegocio.BD.Utilitarios::CAR.ValidarFormatoMulti" (
	p_string NVARCHAR(100),
	p_patron NVARCHAR(100)
)
	RETURNS p_out_resultado INT
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER
AS
BEGIN
	DECLARE v_delimit NVARCHAR(1);
	DECLARE v_string NVARCHAR(100);
	DECLARE v_patr NVARCHAR(100);
	DECLARE v_temp NVARCHAR(100);
	DECLARE v_cod NVARCHAR(2);
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	
	p_out_resultado := 1;
	v_patr := CONCAT('^(',p_patron);
	v_patr := CONCAT(v_patr,')$');
	v_string := p_string;

	BEGIN
	IF v_string LIKE '{%}' AND v_string!='{}' THEN
		v_string := SUBSTR_AFTER(v_string,'{');
		v_string := SUBSTR_BEFORE(v_string,'}');
	
		IF v_string LIKE_REGEXPR '^("([A-Z]{2})":(true|false))+(,"([A-Z]{2})":(true|false))*$' THEN
			v_delimit := ',';
			v_string := CONCAT(v_string,v_delimit);

			WHILE v_string != SUBSTR_BEFORE(v_string,v_delimit) DO
				p_out_resultado := 1;
				v_temp := SUBSTR_BEFORE(v_string,v_delimit);
				v_string := SUBSTR_AFTER(v_string,v_delimit);
				v_cod := SUBSTRING(v_temp,2,2);
			
				IF v_cod LIKE_REGEXPR v_patr THEN
					v_patr := CONCAT(SUBSTR_BEFORE(v_patr,v_cod),SUBSTR_AFTER(v_patr,v_cod));
					CONTINUE;
				ELSE
					p_out_resultado := 0;
					BREAK;
				END IF;
			
			END WHILE;
		ELSE p_out_resultado := 0;
		END IF;
	ELSE p_out_resultado := 0;
	END IF;
	END;
END;
GO
