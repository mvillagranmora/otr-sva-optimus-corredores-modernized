select tran."IdInstrumento",tran."IdInstrumentoSubyacente",tran."IdTipoOperacion",tran."EsVentaCorta",tran."IdTipoInstrumento",tran."IdBolsa",
IFNULL(sum(CASE tran_tipope."AccionOrden" WHEN 'COMPRA' THEN (CASE WHEN tran."EsFungible" = 'S' THEN tran."ValorRescateTransaccion" ELSE tran."CantidadTransaccion" END) WHEN 'VENTA' THEN 0 ELSE 0 END),0) AS "TransadoCompra",
IFNULL(sum(CASE tran_tipope."AccionOrden" WHEN 'VENTA' THEN (CASE WHEN tran."EsFungible" = 'S' THEN tran."ValorRescateTransaccion" ELSE tran."CantidadTransaccion" END) WHEN 'COMPRA' THEN 0 ELSE 0 END),0) AS "TransadoVenta"
from "BSCL.Orden.BD.Vistas::ConsultarTransaccion"tran
inner join "BSCL.Plataforma.BD::INS.TipoOperacion" tran_tipope on tran."IdTipoOperacion" = tran_tipope."IdTipoOperacion"
WHERE tran."EsAPlazo" = 'N' AND tran."IdEstadoTransaccion" = 'ING' and tran."FechaSistemaCreacion" = :FechaConsulta
group by tran."IdInstrumento",tran."IdInstrumentoSubyacente",tran."IdTipoOperacion",tran."EsVentaCorta",tran."IdTipoInstrumento",tran."IdBolsa"