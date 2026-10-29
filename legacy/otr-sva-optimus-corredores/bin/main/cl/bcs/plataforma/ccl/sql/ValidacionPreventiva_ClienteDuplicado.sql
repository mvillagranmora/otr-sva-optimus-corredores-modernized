SELECT rtrim(ltrim("DocumentoIdentidad")) AS "Valor1"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona"
GROUP BY rtrim(ltrim("DocumentoIdentidad"))
HAVING COUNT(1)>1;