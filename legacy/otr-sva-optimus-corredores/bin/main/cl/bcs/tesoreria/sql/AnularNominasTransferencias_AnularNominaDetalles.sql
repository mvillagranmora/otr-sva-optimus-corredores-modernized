UPDATE NTD
SET NTD."IdEstado" = 'ANU'
FROM "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" as NTD
INNER JOIN #AnularNominasTransferencias_p_RegistrosAProcesar as RP ON RP."Id" = NTD."IdNominaTransferencia";

