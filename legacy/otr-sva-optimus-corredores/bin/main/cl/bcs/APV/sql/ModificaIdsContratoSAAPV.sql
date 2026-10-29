UPDATE "BSCL.APV.BD::SCS.ContratoSAAPV"
SET "IdPersona" = (CASE WHEN :IdPersona IS NOT NULL THEN :IdPersona ELSE "IdPersona" END)
  , "IdCliente" = (CASE WHEN :IdCliente IS NOT NULL THEN :IdCliente ELSE "IdCliente" END)
  , "IdPortafolio" = (CASE WHEN :IdPortafolio IS NOT NULL THEN :IdPortafolio ELSE "IdPortafolio" END)
  , "IdEmpleador" = (CASE WHEN :IdEmpleador IS NOT NULL THEN :IdEmpleador ELSE "IdEmpleador" END)
WHERE "FolioContratoSAAPV" = :FolioContratoSAAPV;