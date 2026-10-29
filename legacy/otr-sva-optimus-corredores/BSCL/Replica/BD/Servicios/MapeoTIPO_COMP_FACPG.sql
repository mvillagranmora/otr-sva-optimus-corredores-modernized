CREATE OR REPLACE FUNCTION "BSCL.Replica.BD.Servicios::MapeoTIPO_COMP_FAC"(IdTipoComprobante VARCHAR)
RETURNS VARCHAR
LANGUAGE sql
IMMUTABLE
AS $$
    SELECT CASE IdTipoComprobante
        WHEN 'FAC' THEN 'FA'
        WHEN 'FAE' THEN 'FD'
        WHEN 'FOC' THEN 'FO'
        WHEN 'FOE' THEN 'FT'
        WHEN 'FEE' THEN 'FX'
        WHEN 'FOX' THEN 'FX'
        WHEN 'NCR' THEN 'NC'
        WHEN 'NCE' THEN 'ND'
        WHEN 'NDR' THEN 'NB'
        WHEN 'NEO' THEN 'NT'
        WHEN 'NEE' THEN 'NT'
        ELSE 'FD'
    END;
$$;

-- Ejemplo de llamada en una sola línea
-- SELECT "BSCL.Replica.BD.Servicios::MapeoTIPO_COMP_FAC"('FAC');
