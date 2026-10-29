CREATE OR REPLACE FUNCTION "BSCL.Replica.BD.Servicios::func_IsNumeric"(p_myVarChar VARCHAR)
RETURNS INTEGER
LANGUAGE plpgsql
IMMUTABLE
AS $$
/*
 * Proposito: Validar si una cadena es convertible a numerico (double)
 * Autor: JARVIS (Refactorización Optimus)
 */
DECLARE
    v_aux DOUBLE PRECISION;
BEGIN
    -- Manejo preventivo de nulos o cadenas vacías
    IF p_myVarChar IS NULL OR TRIM(p_myVarChar) = '' THEN
        RETURN 0;
    END IF;

    -- Intento de cast a DOUBLE PRECISION (equivalente a DOUBLE en HANA)
    v_aux := CAST(TRIM(p_myVarChar) AS DOUBLE PRECISION);
    
    RETURN 1;

EXCEPTION
    -- Captura cualquier error de cast (Data Exception: Invalid Text Representation)
    WHEN OTHERS THEN
        RETURN 0;
END;
$$;