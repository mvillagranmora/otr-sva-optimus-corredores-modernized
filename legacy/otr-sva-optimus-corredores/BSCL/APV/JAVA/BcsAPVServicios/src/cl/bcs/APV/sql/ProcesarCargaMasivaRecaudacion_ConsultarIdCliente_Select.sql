SELECT 
"IdCliente","DocumentoIdentidad"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"
WHERE "DocumentoIdentidad" IN (:DocumentoIdentidad);