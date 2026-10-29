SELECT
	 CON."IdContratoSAAPV"
    ,CON."FolioContratoSAAPV"
	,CON."IdPersona"
	,CON."IdEmpleador"
	,CON."IdCliente"
	,POR."IdPortafolio"
FROM "BSCL.APV.BD::SCS.ContratoSAAPV" CON
INNER JOIN "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" POR
	ON (CON."IdContratoSAAPV" = POR."IdContratoSAAPV")
WHERE CON."FolioContratoSAAPV" IN (:FolioContratoSAAPV)
  AND CON."IdEmpresa" = :IdEmpresa;
