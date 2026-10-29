-- HANA-SOURCE: cl/bcs/tesoreria/sql/AnularNominasTransferencias_AnularComprobantePartidaEgreso.sql
-- H2 rewrite (joins -> EXISTS).
UPDATE "BSCL.Tesoreria.BD::TES.Comprobante" AS C SET "IdEstado" = 'ANU' WHERE EXISTS (SELECT 1 FROM "BSCL.Tesoreria.BD::TES.NominaTransferencia" AS NT INNER JOIN TT_AnularNominasTransferencias_p_RegistrosAProcesar AS RP ON RP."Id" = NT."IdNominaTransferencia" WHERE NT."IdComprobantePartidaEgreso" = C."IdComprobante")
