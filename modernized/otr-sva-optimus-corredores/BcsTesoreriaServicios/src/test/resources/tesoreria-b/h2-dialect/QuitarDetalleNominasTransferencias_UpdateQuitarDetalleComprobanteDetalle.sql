-- HANA-SOURCE: cl/bcs/tesoreria/sql/QuitarDetalleNominasTransferencias_UpdateQuitarDetalleComprobanteDetalle.sql
-- H2 rewrite (join -> EXISTS).
UPDATE "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS CD SET "IdEstado" = 'ING' WHERE EXISTS (SELECT 1 FROM TT_QuitarDetalle_p_RegistrosAProcesar AS RP WHERE CD."IdComprobante" = RP."IdComprobante")
