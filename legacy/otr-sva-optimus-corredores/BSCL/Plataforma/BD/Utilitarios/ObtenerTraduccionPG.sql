CREATE OR REPLACE FUNCTION "BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"(
    p_palabra VARCHAR
)
RETURNS VARCHAR
LANGUAGE sql
IMMUTABLE
AS $$
    SELECT CASE p_palabra
        WHEN 'S' THEN 'SI'
        WHEN 'N' THEN 'NO'
        WHEN 'F' THEN 'FIJO'
        WHEN 'V' THEN 'VARIABLE'
        ELSE ''
    END;
$$;