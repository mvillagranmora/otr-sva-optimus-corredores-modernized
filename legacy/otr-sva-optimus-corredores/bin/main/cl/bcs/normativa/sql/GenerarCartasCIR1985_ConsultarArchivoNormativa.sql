SELECT 
     "IdArchivo",
     "NombreArchivo",
     "ServicioArchivo",
     "Esquema"
FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarArchivoNormativa"
WHERE "IdTipoDocumento" = :IdTipoDocumento
  AND "TipoArchivo"     = :TipoArchivo
  AND "Version"         = :Version
  AND "IdEmpresa"       = :IdEmpresa
  AND "IdEstado"        = 'ACT'