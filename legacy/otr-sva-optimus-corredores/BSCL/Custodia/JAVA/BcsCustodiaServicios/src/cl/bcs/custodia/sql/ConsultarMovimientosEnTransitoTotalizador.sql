SELECT
  COALESCE(SUM("Cantidad"), 0) AS "CantidadAgrupada", 
  COALESCE(SUM("MontoCompraMonedaInstrumento"), 0) AS "MontoAgrupadoMonedaInstrumento"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraEnTransito"
WHERE "IdEstado" IN ('PDA', 'PEN', 'MAU')
/* AND */