SELECT
"IdCategoriaInversionista", 
"NombreCategoriaInversionista"
FROM "BSCL.Plataforma.BD.Vistas::COM.ConsultarCategoriaInversionista"
WHERE 
"IdEmpresa" = :IdsEmpresa AND
"IdCategoriaInversionista" in (:IdsCategoriaInversionista);