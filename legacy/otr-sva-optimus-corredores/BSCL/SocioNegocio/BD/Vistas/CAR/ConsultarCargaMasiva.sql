SET SCHEMA "BSCL";
GO 

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CAR.ConsultarCargaMasiva";
GO  

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CAR.ConsultarCargaMasiva" AS

SELECT 
	TCM."IdTipoCargaMasiva",
	TCM."CodigoArchivo" 	AS "CodigoArchivoTipoCargaMasiva",
	TCM."Nombre" 			AS "NombreTipoCargaMasiva",
	TCM."IdEstado" 			AS "IdEstadoTipoCargaMasiva",
	CM."IdCargaMasiva",
	CM."IdEmpresa",
	CM."NombreArchivo",		
	CM."IdUsuario",
	USU."CodigoUsuario",
	CM."FechaCarga",            -- Fecha Sistema
 	CM."FechaProceso",          -- Fecha en que se procesó la CM
  	CM."FechaIngresoRegistro",  -- Fecha BD en ingreso
  	CM."CantidadRegistros",
	CM."IdEstado" AS "IdEstadoCargaMasiva",
	EST."Nombre" AS "NombreEstadoCargaMasiva"
FROM "BSCL.SocioNegocio.BD::CAR.CargaMasiva" AS CM
INNER JOIN "BSCL.SocioNegocio.BD::CAR.TipoCargaMasiva" AS TCM ON (TCM."IdTipoCargaMasiva" = CM."IdTipoCargaMasiva")
LEFT OUTER JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS USU ON (USU."IdUsuario" = CM."IdUsuario")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST ON (EST."IdEstado" = CM."IdEstado"); 
GO
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CAR.ConsultarCargaMasiva" TO BCS_DESARROLLADOR WITH GRANT OPTION;
GO