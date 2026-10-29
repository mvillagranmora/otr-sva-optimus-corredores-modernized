SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.ConsultarClientesComplemento4Porciento";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.ConsultarClientesComplemento4Porciento" AS
SELECT DISTINCT cli."DocumentoIdentidad"					AS "RutCliente"
	   			,cli."IdCliente"							AS "IdCliente"
				,cli."NombreCompleto"							AS "NombreCompleto"

FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS cli
JOIN "BSCL.Normativa.BD.Vistas::TRI.DJ1874.PreDatos" AS datos ON datos."IdCliente" = cli."IdCliente"
WHERE datos."ValResultado" = 'OK'
WITH read ONLY;

