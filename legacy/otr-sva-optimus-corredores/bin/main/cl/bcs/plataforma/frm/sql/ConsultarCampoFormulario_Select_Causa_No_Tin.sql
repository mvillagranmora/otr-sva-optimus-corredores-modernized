SELECT
"IdCausaNoTin", 
"Descripcion"
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCausaNoTin"
WHERE "IdCausaNoTin" in (:IdsCausaNoTin);