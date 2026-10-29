SELECT
"IdMoneda", 
"NombreLargo"
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda"
WHERE "IdMoneda" in (:IdsMoneda);