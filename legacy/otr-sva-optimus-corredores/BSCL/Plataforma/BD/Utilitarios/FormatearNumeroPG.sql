CREATE OR REPLACE FUNCTION "BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(
    p_Numero DECIMAL(26,8),
    p_Decimales INT DEFAULT 0,
    p_RedondeaTrunca VARCHAR(1) DEFAULT 'R'
)
RETURNS DECIMAL(26,8)
LANGUAGE plpgsql
IMMUTABLE
AS $$
DECLARE
    resultado DECIMAL(26,8);
BEGIN
    IF p_RedondeaTrunca = 'R' THEN
        -- En PostgreSQL, ROUND(numeric, int) aplica implícitamente la lógica "Half Up"
        resultado := ROUND(p_Numero, p_Decimales);
    ELSIF p_RedondeaTrunca = 'T' THEN
        -- TRUNC corta los decimales sobrantes sin redondear, equivalente a ROUND_FLOOR para valores positivos
        resultado := TRUNC(p_Numero, p_Decimales);
    ELSE
        -- Comportamiento por defecto a prueba de fallos
        resultado := ROUND(p_Numero, p_Decimales);
    END IF;

    RETURN resultado;
END;
$$;