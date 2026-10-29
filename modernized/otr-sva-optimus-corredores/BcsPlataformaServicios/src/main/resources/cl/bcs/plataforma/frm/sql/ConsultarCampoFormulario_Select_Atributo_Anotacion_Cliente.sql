SELECT
"IdAtributoAnotacion"
,"NombreAtributoAnotacion"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarAtributoAnotacion"
WHERE "IdAtributoAnotacion" in (:IdsAtributoAnotacion) ;