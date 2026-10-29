SELECT 
"NombreImagen",
"Binario" 
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
WHERE "NombreImagen" IN (:Recurso);
