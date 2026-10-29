SELECT 
Telefono."IdPersona"
,COALESCE(Telefono."IdTelefono",0) AS "IdTelefono"
,COALESCE(Telefono."CodigoProposito",'') AS "IdProposito"
,COALESCE(Proposito."Descripcion",'') AS "Proposito"
,COALESCE(Telefono."Numero",'') AS "Numero"
,COALESCE(Telefono."IdPais",0) AS "IdPais"
,COALESCE(Pais."NombrePais",'') AS "Pais"
,COALESCE(Telefono."IdTipoTelefono",'') AS "Tipo"
,COALESCE(Telefono."Anexo",'') AS "Anexo"
FROM "BSCL.SocioNegocio.BD::PER.Telefono" AS Telefono
LEFT JOIN "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto" AS Proposito ON Proposito."IdPropositoMedioContacto" = Telefono."CodigoProposito"
LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" AS Pais ON Pais."IdPais" = Telefono."IdPais"
WHERE "IdPersona" in (:p_IdPersonas) AND Telefono."Borrado"='N'