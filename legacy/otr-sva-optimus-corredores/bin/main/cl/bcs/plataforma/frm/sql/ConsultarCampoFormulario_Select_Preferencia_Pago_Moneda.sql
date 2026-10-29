SELECT TOP 1
"IdMoneda"
,"NombreMoneda"
FROM "BSCL.Plataforma.BD.Vistas::PAG.ConsultarPreferenciaPago"
WHERE "IdMoneda" in (:IdsMoneda);