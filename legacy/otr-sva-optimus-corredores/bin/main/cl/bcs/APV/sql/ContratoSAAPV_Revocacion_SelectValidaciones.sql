SELECT top 1 "IdContratoSAAPV", "IdEmpleador"  FROM "BSCL.APV.BD::SCS.ContratoSAAPV"
WHERE "FolioContratoSAAPV" in (:FolioSAAPV) AND "IdEstado" = 'VIG'






