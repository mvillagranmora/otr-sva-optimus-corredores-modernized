UPDATE "BSCL.Portafolio.BD::POR.Autorizado" 
SET "IdTipoAutorizado" = :IdTipoAutorizado, "FirmaOptimusCliente" = :FirmaOptimusClienteSN
WHERE "IdAutorizado" = :IdAutorizado;