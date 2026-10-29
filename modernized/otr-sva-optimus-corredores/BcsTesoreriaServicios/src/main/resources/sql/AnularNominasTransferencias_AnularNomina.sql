UPDATE NT
SET NT."IdEstado" = 'ANU'
FROM "BSCL.Tesoreria.BD::TES.NominaTransferencia" as NT
INNER JOIN #AnularNominasTransferencias_p_RegistrosAProcesar as RP ON RP."Id" = NT."IdNominaTransferencia";