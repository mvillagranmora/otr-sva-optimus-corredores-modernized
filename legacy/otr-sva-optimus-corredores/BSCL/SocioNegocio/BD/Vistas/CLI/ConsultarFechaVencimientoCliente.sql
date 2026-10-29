SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarFechaVencimientoCliente";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarFechaVencimientoCliente" AS
SELECT CLI."IdCliente"                                                                  AS "IdCliente"
	 , TO_VARCHAR(ADD_DAYS(CLI."FechaUltimaModificacion", CONF."NumeroDiasFechaVencimiento")) AS "FechaVencimiento"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarFechaUltimaModificacionCliente" CLI
	 CROSS JOIN (
	SELECT IFNULL(PC."Valor", '365') AS "NumeroDiasFechaVencimiento"
	FROM "BSCL.Plataforma.BD::PLA.Configuracion" PC
	WHERE "CodigoParametro" = 'PLA.NumeroDiasFechaVencimiento'
) CONF;

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarFechaVencimientoCliente" TO BCS_DESARROLLADOR WITH GRANT OPTION;