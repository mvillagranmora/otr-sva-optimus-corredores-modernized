CREATE OR REPLACE FUNCTION "BSCL.Plataforma.BD.Utilitarios::ObtenerEstadoAutorizacion"(
    p_IdTipoEntidad VARCHAR,
    p_IdEstado VARCHAR,
    p_IdEstadoAutorizacion VARCHAR
)
RETURNS VARCHAR
LANGUAGE plpgsql
IMMUTABLE
AS $$
/*
 * Proposito: Obtener el estado de la autorizacion segun combinatoria del campo IdEstado y IdEstadoAutorizacion 
 * Autor: JARVIS (Refactorización Optimus)
 */
DECLARE
    v_out_estado VARCHAR(3);
BEGIN
    -- Lógica central de validación
    IF p_IdEstado IN ('ACT', 'VIG', 'ACP') THEN 
    
        IF COALESCE(p_IdEstadoAutorizacion, '') IN ('PEN', '') THEN 
             v_out_estado := 'PEN';
        ELSE
             v_out_estado := p_IdEstado;
        END IF;      
        
    ELSE
        -- Estado por defecto para casos no activos
        v_out_estado := 'INA';
        
        -- Regla específica para Evaluación de Límites
        IF RTRIM(p_IdTipoEntidad) = 'EVALUACIONLIMITE' THEN  
            v_out_estado := 'RZD';
        END IF;
        
    END IF;

    RETURN v_out_estado;

EXCEPTION
    -- Emulación del EXIT HANDLER FOR SQLEXCEPTION de HANA
    WHEN OTHERS THEN
        RETURN 'INA';
END;
$$;