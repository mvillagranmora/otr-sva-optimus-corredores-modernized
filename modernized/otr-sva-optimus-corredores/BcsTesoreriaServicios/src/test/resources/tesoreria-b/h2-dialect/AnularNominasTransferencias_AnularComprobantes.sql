-- HANA-SOURCE: cl/bcs/tesoreria/sql/AnularNominasTransferencias_AnularComprobantes.sql
-- H2 rewrite (joins -> EXISTS).
UPDATE "BSCL.Tesoreria.BD::TES.Comprobante" AS C SET "IdEstado" = 'ANU' WHERE EXISTS (SELECT 1 FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP INNER JOIN "BSCL.Tesoreria.BD::TES.NominaTransferencia" AS NT ON CFP."IdComprobantePartida" = NT."IdComprobantePartidaEgreso" INNER JOIN TT_AnularNominasTransferencias_p_RegistrosAProcesar AS RP ON RP."Id" = NT."IdNominaTransferencia" WHERE C."IdComprobante" = CFP."IdComprobante")
