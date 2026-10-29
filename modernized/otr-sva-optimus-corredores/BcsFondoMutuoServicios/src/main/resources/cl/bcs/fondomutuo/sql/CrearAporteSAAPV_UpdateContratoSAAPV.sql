UPDATE "BSCL.APV.BD::SCS.ContratoSAAPV"  
SET "IdEstado" = 'CER' 
,"FechaCreacion" = :FechaCreacion
,"FechaSuscripcion" = :FechaSuscripcion

WHERE "IdContratoSAAPV" = :IdContratoSAAPV;