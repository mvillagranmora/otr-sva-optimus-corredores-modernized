SELECT 
	  MPAC."IdMandatoPAC"
	 ,SAAPV."IdContratoSAAPV" 
	 ,SAAPV."FolioContratoSAAPV" 
	 ,MPAC."Estado"  
FROM "BSCL.Tesoreria.BD::PAC.MandatoPAC" AS MPAC 
LEFT JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" SAAPV ON (MPAC."FolioSAAPV" = SAAPV."FolioContratoSAAPV")
WHERE  SAAPV."FolioContratoSAAPV" IN (:FolioContratoSAAPV);