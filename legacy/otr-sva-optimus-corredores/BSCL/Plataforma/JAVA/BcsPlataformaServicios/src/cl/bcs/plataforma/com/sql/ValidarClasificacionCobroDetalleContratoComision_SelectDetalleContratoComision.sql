SELECT "IdDetalleContratoComision" 
FROM "BSCL.Plataforma.BD::COM.DetalleContratoComision"
WHERE "IdContratoComision" = :IdContratoComision 
AND "IdCobroClasificacion" = :IdCobroClasificacion
AND "IdDetalleContratoComision" != (CASE
    WHEN :IdDetalleContratoComision IS NULL THEN 0
    ELSE :IdDetalleContratoComision
END);
