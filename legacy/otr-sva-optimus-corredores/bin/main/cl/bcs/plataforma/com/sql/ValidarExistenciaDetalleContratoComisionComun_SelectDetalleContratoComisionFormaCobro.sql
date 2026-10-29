SELECT dc."IdDetalleContratoComision"
FROM "BSCL.Plataforma.BD::COM.DetalleContratoComision" dc
LEFT JOIN "BSCL.Plataforma.BD::COM.Cobro" c ON c."IdCobro" = dc."IdCobro" 
LEFT JOIN "BSCL.Plataforma.BD::COM.Cobro" nc ON nc."IdCobro" = :IdCobro
WHERE dc."IdContratoComision" = :IdContratoComision 
AND c."FormaCobro" <> nc."FormaCobro" 
AND dc."IdDetalleContratoComision" != (CASE
    WHEN :IdDetalleContratoComision IS NULL THEN 0
    ELSE :IdDetalleContratoComision
END);

