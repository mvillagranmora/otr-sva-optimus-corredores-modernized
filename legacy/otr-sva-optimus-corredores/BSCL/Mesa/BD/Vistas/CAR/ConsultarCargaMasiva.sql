SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Mesa.BD.Vistas::CAR.ConsultarCargaMasiva";
GO
CREATE VIEW "BSCL.Mesa.BD.Vistas::CAR.ConsultarCargaMasiva" AS
SELECT
	TCM."IdTipoCargaMasiva",
	TCM."CodigoTipoCargaMasiva",
	TCM."Nombre" 			    AS "NombreTipoCargaMasiva",
	TCM."IdEstado" 			    AS "IdEstadoTipoCargaMasiva",
	CM."IdCargaMasiva",
	CM."IdEmpresa",
	CM."NombreArchivo",
	CM."IdUsuario",
	USU."CodigoUsuario",
	CM."FechaCarga",
 	CM."FechaProceso",
  	CM."FechaIngresoRegistro",
  	CM."CantidadRegistros",
	CM."IdEstado" AS "IdEstadoCargaMasiva",
	EST."Nombre" AS "NombreEstadoCargaMasiva",
	CM."IdMesa",
	MESA."Nombre" AS "NombreMesa"
FROM "BSCL.Mesa.BD::CAR.CargaMasiva" AS CM
INNER JOIN "BSCL.Mesa.BD::CAR.TipoCargaMasivaRF" AS TCM ON (TCM."CodigoTipoCargaMasiva" = CM."CodigoTipoCargaMasiva")
LEFT OUTER JOIN "BSCL.Plataforma.BD::ACC.Usuario" 	AS USU ON (USU."IdUsuario" = CM."IdUsuario")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" 	AS EST ON (EST."IdEstado" = CM."IdEstado")
LEFT OUTER JOIN "BSCL.Mesa.BD::MES.Mesa" AS MESA ON MESA."IdMesa" = CM."IdMesa";
GO
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::CAR.ConsultarCargaMasiva" TO  BCS_DESARROLLADOR WITH GRANT OPTION;