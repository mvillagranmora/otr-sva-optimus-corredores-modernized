UPDATE "BSCL.SocioNegocio.BD::ENR.PreferenciaPagoPortafolio"
SET "PorDefecto" = 'N'
WHERE "IdEnrolamiento" = :IdEnrolamiento AND "IdPreferenciaPago" = :IdPreferenciaPago AND "IdPreferenciaPagoPortafolio" != :IdPreferenciaPagoPortafolio