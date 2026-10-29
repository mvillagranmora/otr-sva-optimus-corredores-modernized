CREATE OR REPLACE FUNCTION "BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor"(
    p_IdEmisor VARCHAR,
    p_IdClaseInstrumento VARCHAR,
    p_TipoInstrumento VARCHAR,
    p_Nemotecnico VARCHAR,
    p_IdTipoReajuste VARCHAR
)
RETURNS VARCHAR
LANGUAGE plpgsql
IMMUTABLE
AS $$
DECLARE
    p_out_Clasificacion VARCHAR(2) := NULL;
BEGIN
    -- LÓGICA PARA CLASE 'IF'
    IF COALESCE(p_IdClaseInstrumento, '') = 'IF' THEN
        p_out_Clasificacion := CASE
            WHEN p_Nemotecnico = 'PDBC' THEN '1'
            WHEN strpos(p_Nemotecnico, 'PDBC') > 0 AND p_IdEmisor = 'CENTRAL' THEN '1'
            WHEN p_Nemotecnico = 'PRBC' THEN '2'
            WHEN p_IdEmisor = 'TESORERIA' THEN '13'
            WHEN p_Nemotecnico = 'PDT' THEN '12'
            WHEN p_Nemotecnico = 'PAGARE NR' AND p_IdEmisor = 'CENTRAL' THEN '1'
            WHEN p_Nemotecnico = 'PAGARE NR' AND p_IdEmisor <> 'CENTRAL' THEN '15'
            WHEN p_Nemotecnico = 'PAGARE R'  AND p_IdEmisor = 'CENTRAL' THEN '2'
            WHEN p_Nemotecnico = 'PAGARE R'  AND p_IdEmisor <> 'CENTRAL' AND p_IdTipoReajuste = 'USD' THEN '16'
            WHEN p_Nemotecnico = 'PAGARE R'  AND p_IdEmisor <> 'CENTRAL' AND p_IdTipoReajuste = 'UF' THEN '17'
            WHEN p_Nemotecnico = 'PAGARE R'  AND p_IdTipoReajuste = 'ICP' THEN '22'
            ELSE '21'
        END;
    END IF;

    -- LÓGICA PARA CLASE 'RF'
    IF COALESCE(p_IdClaseInstrumento, '') = 'RF' THEN 
        IF COALESCE(p_IdEmisor, '') = 'CENTRAL' OR COALESCE(p_IdEmisor, '') = 'TESORERIA' THEN 
            p_out_Clasificacion := CASE COALESCE(p_TipoInstrumento, '')
                WHEN 'PPBC' THEN '3'  
                WHEN 'PCC'  THEN '4'
                WHEN 'PCD'  THEN
                    CASE COALESCE(p_IdTipoReajuste, '')   
                        WHEN 'UF'  THEN '25'   
                        WHEN 'USD' THEN '26'   
                        ELSE '25'   
                    END     
                WHEN 'PTF'  THEN '6'  
                WHEN 'PDP'  THEN '7' 
                WHEN 'PPS'  THEN '8'                                
                WHEN 'PRC'  THEN '9'  
                WHEN 'CAR'  THEN '10'                                 
                WHEN 'PRD'  THEN '11'                                 
                WHEN 'PRT'  THEN '13'                                 
                WHEN 'BCX'  THEN '19'   
                WHEN 'PEP'  THEN '24'   
                WHEN 'BR'   THEN '27'  
                WHEN 'ZER'  THEN '28'  
                WHEN 'BCD'  THEN '29'  
                WHEN 'BCP'  THEN '30'  
                WHEN 'BCU'  THEN '31'  
                WHEN 'CER'  THEN '33'   
                ELSE '14'  
            END;
        ELSIF COALESCE(p_IdEmisor, '') = 'BTU' OR COALESCE(p_IdEmisor, '') = 'BTP' THEN
            p_out_Clasificacion := '14';
        ELSIF COALESCE(p_TipoInstrumento, '') = 'LTH' THEN
            p_out_Clasificacion := CASE COALESCE(p_IdEmisor, '')  
                WHEN 'MVU' THEN '23'  
                ELSE '18'  
            END;
        ELSIF COALESCE(p_TipoInstrumento, '') IN ('BEM', 'BBC') THEN
            p_out_Clasificacion := CASE  
                WHEN strpos(LEFT(p_IdEmisor, 2) || LEFT(p_Nemotecnico, 2), 'BB') > 0 AND COALESCE(p_TipoInstrumento, '') = 'BBC' THEN '20'
                WHEN p_IdEmisor = 'FFCC' THEN '22'  
                ELSE '34'    
            END;  
        ELSE   
            p_out_Clasificacion := '21';
        END IF;   
    END IF;

    -- RETORNO POR DEFECTO
    RETURN COALESCE(p_out_Clasificacion, '21');  
END;
$$;