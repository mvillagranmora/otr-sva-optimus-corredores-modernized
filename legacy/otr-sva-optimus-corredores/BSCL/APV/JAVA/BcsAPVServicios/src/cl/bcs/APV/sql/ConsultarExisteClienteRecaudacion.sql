SELECT 
	"CLIR"."IdRecaudacionCliente",
	"CLIR"."IdCliente"
FROM "BSCL.APV.BD::REC.Cliente" AS "CLIR"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "CLI"
ON "CLIR"."IdCliente" = "CLI"."IdCliente"
WHERE "CLI"."DocumentoIdentidad" = :p_DoctoIdentidad;
