SET SCHEMA "BSCL";

DROP VIEW "BSCL.Custodia.BD.Vistas::CAR.ConsultarCargaMasivaCustodia";
CREATE VIEW "BSCL.Custodia.BD.Vistas::CAR.ConsultarCargaMasivaCustodia" AS

SELECT
	TCM."IdTipoCargaMasiva",
	TCM."Codigo" AS "CodigoTipoCargaMasiva",
	TCM."Nombre" AS "NombreTipoCargaMasiva",
	TCM."IdEstado" AS "IdEstadoTipoCargaMasiva",
	CM."IdCargaMasiva",
	CM."IdEmpresa",
	CM."IdUsuario",
	CM."NombreArchivo",
	USU."CodigoUsuario",
	CM."FechaCarga",            -- Fecha Sistema
 	CM."FechaProceso",         -- Fecha en que se procesó la CM 
  	CM."FechaIngresoRegistro", -- Fecha BD en ingreso
	CM."CantidadRegistros",
	CMD."RegistroConErrores",
	CM."IdEstado" AS "IdEstadoCargaMasiva",
	ECM."Nombre" AS "NombreEstadoCargaMasiva"
FROM
	"BSCL.Custodia.BD::CUS.CargaMasiva" AS CM
INNER JOIN "BSCL.Custodia.BD::CUS.TipoCargaMasiva" AS TCM ON
	(TCM."IdTipoCargaMasiva" = CM."IdTipoCargaMasiva")
LEFT JOIN (
		SELECT
		CM."IdCargaMasiva",
		COUNT(CASE WHEN CMD."IdEstado" IN ('ERR','RZD','INV') THEN 0 END) AS "RegistroConErrores"
	FROM
		"BSCL.Custodia.BD::CUS.CargaMasivaDetalle" AS CMD
	LEFT JOIN "BSCL.Custodia.BD::CUS.CargaMasiva" AS CM ON
		CM."IdCargaMasiva" = CMD."IdCargaMasiva"
	GROUP BY
		CM."IdCargaMasiva",
		CM."CantidadRegistros") AS CMD ON
	(CMD."IdCargaMasiva" = CM."IdCargaMasiva")
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario" AS USU ON
	(USU."IdUsuario" = CM."IdUsuario")
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" AS ECM ON
	(ECM."IdEstado" = CM."IdEstado")

WITH READ ONLY;
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CAR.ConsultarCargaMasivaCustodia" TO  BCS_DESARROLLADOR WITH GRANT OPTION;