SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarClienteEstado"; 

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarClienteEstado" AS
SELECT
			"CLI"."IdCliente",
			CASE "CLI"."IdEstado" WHEN 'ACT' THEN CASE "CLI"."EsEnrolado" WHEN 'P' THEN 'PEN' WHEN 'N' THEN 'PEN' ELSE 'ACT' END ELSE 'BLO' END AS "IdEstado"
	FROM "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLI";

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarClienteEstado" TO  BCS_DESARROLLADOR WITH GRANT OPTION;