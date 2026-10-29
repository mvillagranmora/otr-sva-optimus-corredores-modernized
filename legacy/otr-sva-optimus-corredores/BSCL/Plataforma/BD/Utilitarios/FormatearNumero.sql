SET SCHEMA "BSCL";

DROP FUNCTION "BSCL.Plataforma.BD.Utilitarios::FormatearNumero";

CREATE FUNCTION "BSCL.Plataforma.BD.Utilitarios::FormatearNumero" ( 
	in  p_Numero			DECIMAL(26,8),
	in	p_Decimales			INT DEFAULT 0,
	IN  p_RedondeaTrunca	NVARCHAR(1)	DEFAULT 'R'--R  o T
) 
	RETURNS resultado DECIMAL(26,8)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER AS
BEGIN
	
	IF (p_RedondeaTrunca = 'R') THEN
		resultado = ROUND( p_Numero, p_Decimales, ROUND_HALF_UP);
	END IF;
	IF (p_RedondeaTrunca = 'T') THEN
		resultado = ROUND( p_Numero, p_Decimales, ROUND_FLOOR);
	END IF;
	
END;





