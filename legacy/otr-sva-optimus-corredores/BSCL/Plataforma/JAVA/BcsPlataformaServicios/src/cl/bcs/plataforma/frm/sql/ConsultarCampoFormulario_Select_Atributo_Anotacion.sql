SELECT  
"IdAtributoAnotacion"
,"NombreAtributoAnotacion"
FROM "BSCL.Portafolio.BD::POR.AtributoAnotacion" 
WHERE "IdEmpresa" = :IdEmpresa AND  "IdAtributoAnotacion" IN (:IdAtributoAnotacion);