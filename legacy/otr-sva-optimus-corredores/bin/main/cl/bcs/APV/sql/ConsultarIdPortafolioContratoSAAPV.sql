SELECT "IdPortafolio"
FROM "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" 
WHERE 
    "IdPortafolio" = :IdPortafolio AND 
    "IdContratoSAAPV" <> :IdContratoSAAPV;