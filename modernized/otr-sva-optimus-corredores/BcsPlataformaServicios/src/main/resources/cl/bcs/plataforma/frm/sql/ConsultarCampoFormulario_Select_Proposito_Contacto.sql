SELECT
"IdPropositoMedioContacto", 
"Descripcion"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPropositoMedioContacto"
WHERE "IdPropositoMedioContacto" in (:IdsPropositoMedioContacto);