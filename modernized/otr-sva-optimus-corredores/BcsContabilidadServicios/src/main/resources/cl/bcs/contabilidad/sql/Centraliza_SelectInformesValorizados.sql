SELECT
    "IdInforme", "IdCliente" 
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarInformesValorizados" INF
INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMP ON INF."IdSocioDeNegocio" = EMP."IdSocioDeNegocio" 
WHERE "Key" = :key
