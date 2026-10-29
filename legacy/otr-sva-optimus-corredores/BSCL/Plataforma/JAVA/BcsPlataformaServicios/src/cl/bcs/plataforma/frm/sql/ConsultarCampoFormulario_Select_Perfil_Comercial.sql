SELECT
"IdPerfilComercialBase"
,"DescripcionPerfilComercialBase"
FROM "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilComercialBase"
WHERE "IdPerfilComercialBase" in (:IdsPerfilComercialBase);