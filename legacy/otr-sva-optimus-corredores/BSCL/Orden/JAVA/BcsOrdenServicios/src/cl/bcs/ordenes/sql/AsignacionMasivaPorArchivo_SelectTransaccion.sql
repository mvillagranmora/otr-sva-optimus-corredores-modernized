
-- Obtiene datos de la transaccion
SELECT DISTINCT TRA."IdTransaccion", TRA."FolioTransaccion", TRA."CantidadDisponibleTransaccion", TRA."MontoTransaccion", TRA."ValorRescateTransaccion", TRA."MarcaNegociacion"
FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccionPorAsignar" AS TRA
INNER JOIN "BSCL.Orden.BD::GBL.AsignacionMasivaPorArchivo_Entrada" AS GBL ON (
									GBL."FolioTransaccion" = TRA."FolioTransaccion" AND 
									GBL."FechaTransaccion" = TRA."FechaOperacion"  AND 
									GBL."Nemotecnico"      = TRA."Instrumento")
WHERE TRA."MarcaNegociacion" = :Marca
AND   TRA."IdEmpresa" = :IdEmpresa;