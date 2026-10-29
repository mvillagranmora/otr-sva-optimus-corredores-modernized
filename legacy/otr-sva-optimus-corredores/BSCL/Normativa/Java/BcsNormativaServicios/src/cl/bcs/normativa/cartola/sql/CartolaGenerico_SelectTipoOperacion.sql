SELECT 
    COALESCE(t."IdTipoOperacion", c."IdConcepto") AS "IdTipoOperacion",
    CASE  
        WHEN t."IdTipoOperacion" = 'COMPAC' THEN 'VENTA CON PACTO'
        WHEN t."IdTipoOperacion" = 'VENPAC' THEN 'COMPRA CON PACTO'
        WHEN t."IdTipoOperacion" = 'COMTER' THEN 'VENTA A TERMINO'
        WHEN t."IdTipoOperacion" = 'VENTER' THEN 'COMPRA A TERMINO'
        WHEN t."IdTipoOperacion" = 'ANUCMT' THEN 'ANULACION VENTA A TERMINO'
        WHEN t."IdTipoOperacion" = 'ANUVNT' THEN 'ANULACION COMPRA A TERMINO'
        WHEN t."IdTipoOperacion" = 'COMFWD' THEN 'VENTA FORWARD'
        WHEN t."IdTipoOperacion" = 'VENFWD' THEN 'COMPRA FORWARD'
        WHEN t."IdTipoOperacion" IS NOT NULL THEN t."Nombre"
        WHEN c."IdConcepto" IS NOT NULL THEN c."Nombre"
        ELSE NULL
    END AS "Nombre"
FROM "BSCL.Plataforma.BD::INS.TipoOperacion" t
FULL OUTER JOIN "BSCL.Plataforma.BD::PLA.Concepto" c ON t."IdTipoOperacion" = c."IdConcepto"
WHERE COALESCE(t."IdTipoOperacion", c."IdConcepto") IN (:IdTipoOperacion);