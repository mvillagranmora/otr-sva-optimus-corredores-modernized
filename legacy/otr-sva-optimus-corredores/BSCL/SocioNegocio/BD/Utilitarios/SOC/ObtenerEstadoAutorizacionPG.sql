CREATE OR REPLACE FUNCTION "BSCL.SocioNegocio.BD.Utilitarios::SOC.ObtenerEstadoAutorizacion"(
    p_IdTipoEntidad VARCHAR,
    p_IdEstado VARCHAR,
    p_IdEstadoAutorizacion VARCHAR
)
RETURNS VARCHAR
LANGUAGE plpgsql
IMMUTABLE
AS $$
/*
 * Proposito: Obtener el estado de la autorizacion (SocioNegocio)
 * Autor: JARVIS (Refactorización Optimus)
 */
DECLARE
    v_out_estado VARCHAR(3);
BEGIN
    -- Evaluación de estados activos
    IF p_IdEstado IN ('ACT', 'VIG') THEN 
    
        IF COALESCE(p_IdEstadoAutorizacion, '') IN ('PEN', '') THEN 
             v_out_estado := 'PEN';
        ELSE
             v_out_estado := p_IdEstado;
        END IF;      
        
    ELSE
        -- Lógica para estados no vigentes/inactivos
        v_out_estado := 'INA';
        
        -- Ramificaciones específicas por Tipo de Entidad
        IF RTRIM(p_IdTipoEntidad) = 'DOCUMENTOIDENTIDAD' THEN
            v_out_estado := 'VCD';
        ELSIF RTRIM(p_IdTipoEntidad) = 'PORTAFOLIO' THEN
            v_out_estado := 'BLO';
        END IF;
        
    END IF;

    RETURN v_out_estado;

EXCEPTION
    -- Emulación del EXIT HANDLER de HANA
    WHEN OTHERS THEN
        RETURN 'INA';
END;
$$;