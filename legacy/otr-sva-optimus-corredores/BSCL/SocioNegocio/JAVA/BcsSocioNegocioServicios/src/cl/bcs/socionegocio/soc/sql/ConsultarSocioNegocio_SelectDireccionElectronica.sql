SELECT 
Direccion."IdDireccionElectronica"
,Direccion."CodigoProposito" AS "IdProposito"
,Proposito."Descripcion" AS "Proposito"
,Direccion."Direccion"
FROM "BSCL.SocioNegocio.BD::PER.DireccionElectronica" Direccion
LEFT JOIN "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto" AS Proposito ON Proposito."IdPropositoMedioContacto" = Direccion."CodigoProposito"
WHERE "IdPersona" = :p_IdPersona
AND Direccion."Borrado" = 'N';