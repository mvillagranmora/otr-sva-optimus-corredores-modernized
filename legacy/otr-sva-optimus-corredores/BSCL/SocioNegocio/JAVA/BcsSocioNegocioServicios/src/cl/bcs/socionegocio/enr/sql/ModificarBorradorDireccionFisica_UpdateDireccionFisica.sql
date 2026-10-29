UPDATE "BSCL.SocioNegocio.BD::ENR.DireccionFisica"
SET "Calle" = :Calle
,"Numero" = :Numero
,"Departamento" = :Departamento
,"Sector" = :Sector
,"CodigoPostal" = :CodigoPostal
,"IdCiudad" = :IdCiudad
,"CodigoProposito" = :CodigoProposito
,"Comentario" = :Comentario
,"IdDivisionPais" = :IdDivisionPais
,"Predeterminado" = :Predeterminado
,"CodigoCompuesto" = :CodigoCompuesto
WHERE "IdDireccionFisica" = :IdDireccionFisica;