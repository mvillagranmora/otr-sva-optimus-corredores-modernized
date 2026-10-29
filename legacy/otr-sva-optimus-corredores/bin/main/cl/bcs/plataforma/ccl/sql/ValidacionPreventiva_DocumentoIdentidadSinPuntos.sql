SELECT rtrim(ltrim("DocumentoIdentidad")) AS "Valor1"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona"
WHERE rtrim(ltrim("DocumentoIdentidad")) LIKE '%.%';