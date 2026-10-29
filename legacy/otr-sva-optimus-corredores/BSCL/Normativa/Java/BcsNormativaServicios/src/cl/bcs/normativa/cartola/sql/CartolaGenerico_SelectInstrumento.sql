SELECT 
"IdInstrumento"
,"FechaVencimiento"
,"FechaEmision"
,"IdTipoReajuste"
,"IdEmisor"
,"RazonSocial"
,"CorteMinimo"
,"Tasa"
,"IdTipoInstrumento"
,"CodigoRubro"
,"TieneCupon"
FROM "BSCL.Plataforma.BD::INS.Instrumento"
WHERE "IdInstrumento" IN (:IdInstrumento)