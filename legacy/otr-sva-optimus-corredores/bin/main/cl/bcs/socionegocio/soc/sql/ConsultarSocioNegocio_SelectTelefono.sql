SELECT 
Telefono."IdTelefono"
,Telefono."CodigoProposito" AS "IdProposito"
,Proposito."Descripcion" AS "Proposito"
,Telefono."Numero"
,Telefono."IdPais"
,Pais."NombrePais" AS "Pais"
,Telefono."IdTipoTelefono" AS "Tipo"
,Telefono."Anexo"
FROM "BSCL.SocioNegocio.BD::PER.Telefono" AS Telefono
LEFT JOIN "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto" AS Proposito ON Proposito."IdPropositoMedioContacto" = Telefono."CodigoProposito"
LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" AS Pais ON Pais."IdPais" = Telefono."IdPais"
WHERE "IdPersona" = :p_IdPersona AND Telefono."Borrado"='N'