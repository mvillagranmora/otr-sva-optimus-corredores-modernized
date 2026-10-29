SELECT
"IdPropositoPortafolio", 
"Descripcion"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPropositoPortafolio"
WHERE "IdPropositoPortafolio" in (:IdsPropositoPortafolio);