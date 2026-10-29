SELECT 
Direccion."IdPersona"
,Direccion."IdEstado"
,Direccion."Predeterminado"
,Direccion."CodigoProposito"
,Proposito."Descripcion"
,Direccion."Direccion"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" AS Direccion
INNER JOIN "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto" AS Proposito ON Proposito."IdPropositoMedioContacto" = Direccion."CodigoProposito"
WHERE "IdPersona" = :p_IdPersona AND "IdEstado" = 'ACT' AND "Predeterminado" = 'UI_SI' AND Direccion."CodigoProposito" = 'PART'