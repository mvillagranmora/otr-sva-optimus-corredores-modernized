SELECT 
Direccion."IdPersona"
,Direccion."IdEstado"
,Direccion."Predeterminado"
,Direccion."CodigoProposito"
,Proposito."Descripcion"
,Direccion."NombreLargo"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS Direccion
INNER JOIN "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto" AS Proposito ON Proposito."IdPropositoMedioContacto" = Direccion."CodigoProposito"
WHERE "IdPersona" = :p_IdPersona AND "IdEstado" = 'ACT' AND Direccion."CodigoProposito" = 'PART'