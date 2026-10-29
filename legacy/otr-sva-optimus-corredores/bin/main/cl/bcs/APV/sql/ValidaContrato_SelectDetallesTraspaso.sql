SELECT "IdPortafolio", "IdContratoSAAPV", "Codigo", "EsReapertura"
FROM "BSCL.APV.BD.Vistas::TRA.ConsultarDetalleTraspasosAPV"
WHERE "IdDetalleTraspaso" IN (:IdDetalleTraspaso); 