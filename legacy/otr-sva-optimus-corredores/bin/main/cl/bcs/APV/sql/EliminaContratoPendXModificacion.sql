DELETE FROM "BSCL.APV.BD::SCS.ContratoSAAPV" 
WHERE	"FolioContratoSAAPV" = :IdFolioContratoSAAPV 
	AND "IdEstado"			 = 'PDI'
	AND	"IdEstado"			 = 'PDS';