-- HANA-SOURCE: cl/bcs/tesoreria/sql/QuitarDetalleNominasTransferencias_UpdateQuitarDetalleComprobante.sql
-- H2 rewrite (join -> EXISTS).
UPDATE "BSCL.Tesoreria.BD::TES.Comprobante" AS C SET "IdEstado" = 'ING' WHERE EXISTS (SELECT 1 FROM TT_QuitarDetalle_p_RegistrosAProcesar AS RP WHERE C."IdComprobante" = RP."IdComprobante")
