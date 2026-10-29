SELECT "IdDetalleContratoComision"
FROM "BSCL.Plataforma.BD::COM.DetalleContratoComision"
WHERE "IdContratoComision" = :IdContratoComision 
AND "IdCobroClasificacion" = :IdCobroClasificacion
AND "IdCobro" = :IdCobro
AND "IdConcepto" = :IdConcepto;


