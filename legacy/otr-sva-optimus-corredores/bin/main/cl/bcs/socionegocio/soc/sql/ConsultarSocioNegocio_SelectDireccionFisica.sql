SELECT 
Direccion."IdDireccionFisica"
,Direccion."CodigoProposito" AS "IdProposito"
,Proposito."Descripcion" AS "Proposito"
,Direccion."Sector"
,Direccion."CodigoPostal"
,Direccion."IdDivisionPais" AS "IdComuna"
,DivisionPais."NombreDivisionPais" AS "Comuna"
,Direccion."Calle"
,Direccion."Numero"
,Direccion."Departamento"
,Direccion."Comentario" AS "Complemento"
,Direccion."IdCiudad"
,Ciudad."NombreCiudad" AS "Ciudad"
FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS Direccion
LEFT JOIN "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto" AS Proposito ON Proposito."IdPropositoMedioContacto" = Direccion."CodigoProposito"
LEFT JOIN "BSCL.Plataforma.BD::REG.DivisionPais" AS DivisionPais ON DivisionPais."IdDivisionPais" = Direccion."IdDivisionPais"
LEFT JOIN "BSCL.Plataforma.BD::REG.Ciudad" AS Ciudad ON Ciudad."IdCiudad" = Direccion."IdCiudad"
WHERE "IdPersona" = :p_IdPersona AND Direccion."Borrado"='N'