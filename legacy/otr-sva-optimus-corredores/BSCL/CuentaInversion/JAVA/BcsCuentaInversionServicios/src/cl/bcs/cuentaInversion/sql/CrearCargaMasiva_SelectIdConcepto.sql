SELECT CTA."IdConcepto"
FROM "BSCL.CuentaInversion.BD::CAR.TipoCargaMasiva" as CTA
WHERE CTA."CodigoTipoCargaMasiva"=:p_CodigoTipoCargaMasiva;