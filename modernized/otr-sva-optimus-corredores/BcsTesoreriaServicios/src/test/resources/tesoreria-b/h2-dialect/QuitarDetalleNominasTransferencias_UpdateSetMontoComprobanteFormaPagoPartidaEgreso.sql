-- HANA-SOURCE: cl/bcs/tesoreria/sql/QuitarDetalleNominasTransferencias_UpdateSetMontoComprobanteFormaPagoPartidaEgreso.sql
-- H2 rewrite (joins -> EXISTS).
UPDATE "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP SET "Monto" = (SELECT NT2."Monto" FROM "BSCL.Tesoreria.BD::TES.NominaTransferencia" AS NT2 WHERE NT2."IdNominaTransferencia" = :IdNominaTransferencia) WHERE EXISTS (SELECT 1 FROM "BSCL.Tesoreria.BD::TES.NominaTransferencia" AS NT WHERE CFP."IdComprobante" = NT."IdComprobantePartidaEgreso" AND NT."IdNominaTransferencia" = :IdNominaTransferencia)
