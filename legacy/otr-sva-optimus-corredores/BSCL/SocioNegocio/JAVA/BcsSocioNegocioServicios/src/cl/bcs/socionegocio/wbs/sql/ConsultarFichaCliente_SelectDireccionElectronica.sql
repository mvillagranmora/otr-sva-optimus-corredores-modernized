SELECT 
Direccion."IdPersona"
,COALESCE(Direccion."IdDireccionElectronica",0) AS "IdDireccionElectronica"
,COALESCE(Direccion."CodigoProposito",'')  AS "IdProposito"
,COALESCE(Proposito."Descripcion",'')  AS "Proposito"
,COALESCE(Direccion."Direccion",'') AS "Direccion"
,COALESCE(Direccion."IdEstado",'') AS "IdEstado"
,COALESCE(Estado."Nombre",'') AS "Estado"
FROM "BSCL.SocioNegocio.BD::PER.DireccionElectronica" Direccion
LEFT JOIN "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto" AS Proposito ON Proposito."IdPropositoMedioContacto" = Direccion."CodigoProposito"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS Estado ON Estado."IdEstado" = Direccion."IdEstado"
WHERE "IdPersona" IN (:p_IdPersonas)
AND Direccion."Borrado" = 'N';