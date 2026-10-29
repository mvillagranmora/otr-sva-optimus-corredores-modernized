-- HANA-SOURCE: cl/bcs/tesoreria/sql/AnularNominasTransferencias_AnularNominaDetalles.sql
-- H2 rewrite (join -> EXISTS).
UPDATE "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" AS NTD SET "IdEstado" = 'ANU' WHERE EXISTS (SELECT 1 FROM TT_AnularNominasTransferencias_p_RegistrosAProcesar AS RP WHERE RP."Id" = NTD."IdNominaTransferencia")
