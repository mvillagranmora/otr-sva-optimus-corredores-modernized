UPDATE "BSCL.Facturacion.BD::FAC.Comprobante"
SET "MayorMenorValor" = :MayorMenorValor,
"MontoRetencionGananciaCap"=:MontoRetencionGananciaCap
WHERE "IdComprobante" = :IdComprobante;