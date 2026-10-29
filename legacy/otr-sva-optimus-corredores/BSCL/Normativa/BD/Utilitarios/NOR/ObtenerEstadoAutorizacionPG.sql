CREATE OR REPLACE FUNCTION "BSCL.Normativa.BD.Utilitarios::NOR.ObtenerEstadoAutorizacion"(
    p_IdTipoEntidad VARCHAR,
    p_IdEstado VARCHAR,
    p_IdEstadoAutorizacion VARCHAR
)
RETURNS VARCHAR
LANGUAGE plpgsql
IMMUTABLE
AS $$
BEGIN
    -- Lógica central de autorización
    IF p_IdEstado IN ('ACT', 'VIG', 'ACP') THEN 
        IF COALESCE(p_IdEstadoAutorizacion, '') IN ('PEN', '') THEN 
             RETURN 'PEN';
        ELSE
             RETURN p_IdEstado;
        END IF;  
    ELSE
        RETURN 'INA';
    END IF;
    
EXCEPTION
    -- Emulando el DECLARE EXIT HANDLER FOR SQLEXCEPTION de HANA
    WHEN OTHERS THEN
        RETURN 'INA';
END;
$$;