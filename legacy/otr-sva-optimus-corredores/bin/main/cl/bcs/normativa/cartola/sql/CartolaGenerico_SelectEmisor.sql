SELECT
"IdEmisor"
,"NombreCompleto"
,"NombreCorto"
FROM "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor"
WHERE "IdEmisor" IN (:IdEmisor)