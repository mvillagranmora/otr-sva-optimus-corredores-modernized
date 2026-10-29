SELECT
	 CON."IdContratoSAAPV"
	,CON."FolioContratoSAAPV"
	,DIS."IdInstrumento"
	,DIS."Nemotecnico"
	,DIS."MontoPesos"
	,DIS."Porcentaje"
	,DIS."IdEstado"
FROM "BSCL.APV.BD::SCS.ContratoSAAPV" CON
INNER JOIN "BSCL.APV.BD::SCS.DistribucionInversion" DIS
	ON (CON."IdContratoSAAPV" = DIS."IdContratoSAAPV" AND
		CON."FolioContratoSAAPV" = DIS."FolioContratoSAAPV"
		--AND DIS."IdEstado" = 'VIG'
		)
WHERE CON."IdEmpresa" = :IdEmpresa
  AND CON."IdPlan" = :IdPlan
  AND CON."IdContratoSAAPV" IN (:IdContratoSAAPV)
  AND CON."IdEstado" IN ('VIG')
;
