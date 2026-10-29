SELECT COALESCE("IdEstadoEnvioNotificacion", '') "IdEstadoEnvioNotificacion", COALESCE("IdEstadoSolicitud", '') "IdEstadoSolicitud", "IdEstado", COALESCE("IdGlosa", 0) "IdGlosa", COALESCE("DescripcionGlosa", '') "DescripcionGlosa"
FROM "BSCL.APV.BD::SCS.ContratoSAAPV"
WHERE "FolioContratoSAAPV" = :IdFolioContratoSAAPV