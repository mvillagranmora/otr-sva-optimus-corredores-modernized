SELECT "IdEmpresa", "DocumentoIdentidad", "RazonSocial"
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa"
WHERE "IdEmpresa" = :IdEmpresa;