SELECT
	CON."IdContratoSAAPV",
	CON."FolioContratoSAAPV"
FROM "BSCL.APV.BD::SCS.ContratoSAAPV" AS CON
WHERE CON."IdEmpresa" = :IdEmpresa
  AND CON."FolioContratoSAAPV" IN (:FolioContratoSAAPV)
;
