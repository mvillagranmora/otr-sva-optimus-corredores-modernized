UPDATE "BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
SET "MayorMenorValor" = :MayorMenorValor,
"MontoRetencionGananciaCap"=:MontoRetencionGananciaCap
WHERE "IdComprobanteDetalle" = :IdComprobanteDetalle;