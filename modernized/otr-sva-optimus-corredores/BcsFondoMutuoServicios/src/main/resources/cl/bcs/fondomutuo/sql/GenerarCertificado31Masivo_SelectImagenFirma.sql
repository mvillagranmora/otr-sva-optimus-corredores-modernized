SELECT TOP 1 "Binario" AS "Contenido"
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
WHERE "NombreImagen" = 'FIRMA_CORREDORA' ORDER BY "Version" DESC;