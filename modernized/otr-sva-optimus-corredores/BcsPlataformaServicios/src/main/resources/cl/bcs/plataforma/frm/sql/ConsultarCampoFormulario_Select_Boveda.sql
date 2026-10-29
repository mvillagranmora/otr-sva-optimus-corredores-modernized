SELECT
"IdBoveda", 
"Nombre"
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBoveda"
WHERE "IdBoveda" in (:IdsBoveda);