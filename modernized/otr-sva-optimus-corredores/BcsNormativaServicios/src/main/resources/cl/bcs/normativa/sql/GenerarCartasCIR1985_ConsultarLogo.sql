SELECT "Binario" AS "Contenido"
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
WHERE "NombreImagen" = 'LOGO_CORREDOR'
  AND "IdEmpresa"   = :IdEmpresa
ORDER BY "Version" DESC
LIMIT 1