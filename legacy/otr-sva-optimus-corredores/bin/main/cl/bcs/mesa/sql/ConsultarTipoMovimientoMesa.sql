SELECT
    'TES' AS "TipoMov" 
    ,CASE 
        WHEN "Tipo"='IN' THEN 'R'
        WHEN "Tipo"='EG' THEN 'P'
        ELSE ''
    END as "RecibePaga"
    ,"IdTipoMovimiento"
FROM  "BSCL.Plataforma.BD::PLA.TipoMovimiento"--"BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoMovimiento" 
WHERE "IdModulo" = 'TES' AND "IdEstado"='ACT' UNION ALL
SELECT 
    'CTAINVCLI' AS "TipoMov" 
    ,CASE 
        WHEN "Tipo"='CA' THEN 'R'
        WHEN "Tipo"='AB' THEN 'P'
        ELSE ''
    END as "RecibePaga"
    ,"IdTipoMovimiento"
FROM  "BSCL.Plataforma.BD::PLA.TipoMovimiento"--"BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoMovimiento" 
WHERE "IdModulo" = 'CTA'  AND "IdEstado"='ACT' UNION ALL
SELECT
    'CTAINVCOR' AS "TipoMov" 
    ,CASE 
        WHEN "Tipo"='AB' THEN 'R'
        WHEN "Tipo"='CA' THEN 'P'
        ELSE ''
    END as "RecibePaga"
    ,"IdTipoMovimiento"
FROM  "BSCL.Plataforma.BD::PLA.TipoMovimiento"--"BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoMovimiento" 
WHERE "IdModulo" = 'CTA'  AND "IdEstado"='ACT' ;