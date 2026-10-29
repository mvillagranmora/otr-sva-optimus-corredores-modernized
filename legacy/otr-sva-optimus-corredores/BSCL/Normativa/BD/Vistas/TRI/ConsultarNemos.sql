SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.ConsultarNemos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.ConsultarNemos" AS
SELECT DISTINCT
		"IdCliente"														AS "IdCliente",
		CASE WHEN "IdEstadoInstrumento" = '' OR "IdEstadoInstrumento" IS NULL THEN 'NO PROCESAR'   ELSE "IdEstadoInstrumento"  END 											AS "IdEstado",
		"NemotecnicoSVS"												AS "Nemotecnico",
		CAST(YEAR("FechaAdquisicion") AS VARCHAR)						AS "Version",
		'DJ1874'														AS "Esquema"
		
FROM "BSCL.Normativa.BD::TRI.DatosDJ1874" WHERE "ValResultado" = 'OK'
WITH read ONLY;



