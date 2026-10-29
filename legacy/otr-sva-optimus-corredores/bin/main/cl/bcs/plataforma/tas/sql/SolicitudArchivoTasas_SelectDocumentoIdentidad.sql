SELECT REPLACE(EMP."DocumentoIdentidad",'-','') as "documentoIdentidad"
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP
WHERE EMP."IdEmpresa" = :p_IdEmpresa;