SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Orden.BD.Vistas::CAR.ConsultarCargaMasiva";
GO
CREATE VIEW "BSCL.Orden.BD.Vistas::CAR.ConsultarCargaMasiva" AS

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
	CM."FechaCarga",            -- Fecha Sistema
 	CM."FechaProceso",          -- Fecha en que se proceso la carga masiva
  	CM."FechaIngresoRegistro",  -- Fecha BD en ingreso
  	CM."CantidadRegistros",
	CM."IdEstado" AS "IdEstadoCargaMasiva",
	EST."Nombre" AS "NombreEstadoCargaMasiva"
	
FROM "BSCL.Orden.BD::CAR.CargaMasiva" AS CM
INNER JOIN "BSCL.Orden.BD::CAR.TipoCargaMasiva"    AS TCM ON (TCM."CodigoTipoCargaMasiva" = CM."CodigoTipoCargaMasiva")
LEFT OUTER JOIN "BSCL.Plataforma.BD::ACC.Usuario" 	AS USU ON (USU."IdUsuario" = CM."IdUsuario")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" 	AS EST ON (EST."IdEstado" = CM."IdEstado")


WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Orden.BD.Vistas::CAR.ConsultarCargaMasiva" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
