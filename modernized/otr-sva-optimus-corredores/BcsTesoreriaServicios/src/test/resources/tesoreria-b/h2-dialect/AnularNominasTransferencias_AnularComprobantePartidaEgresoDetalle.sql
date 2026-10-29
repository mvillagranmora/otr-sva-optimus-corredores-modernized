-- HANA-SOURCE: cl/bcs/tesoreria/sql/AnularNominasTransferencias_AnularComprobantePartidaEgresoDetalle.sql
-- H2 rewrite (joins -> EXISTS).
UPDATE "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS CD SET "IdEstado" = 'ANU' WHERE EXISTS (SELECT 1 FROM "BSCL.Tesoreria.BD::TES.NominaTransferencia" AS NT INNER JOIN TT_AnularNominasTransferencias_p_RegistrosAProcesar AS RP ON RP."Id" = NT."IdNominaTransferencia" WHERE NT."IdComprobantePartidaEgreso" = CD."IdComprobante")
