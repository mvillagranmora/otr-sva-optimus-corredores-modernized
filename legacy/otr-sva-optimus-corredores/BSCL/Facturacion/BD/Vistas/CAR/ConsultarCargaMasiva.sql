SET SCHEMA "BSCL";
GO 

DROP VIEW "BSCL.Facturacion.BD.Vistas::CAR.ConsultarCargaMasiva";
GO  

CREATE VIEW "BSCL.Facturacion.BD.Vistas::CAR.ConsultarCargaMasiva" AS

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
 	CM."FechaProceso",          -- Fecha en que se procesó la CM (Se crearon los mov tes) 
  	CM."FechaIngresoRegistro",  -- Fecha BD en ingreso
  	CM."CantidadRegistros",
	CM."IdEstado" AS "IdEstadoCargaMasiva",
	EST."Nombre" AS "NombreEstadoCargaMasiva"

FROM "BSCL.Facturacion.BD::CAR.CargaMasiva" AS CM
INNER JOIN "BSCL.Facturacion.BD::CAR.TipoCargaMasiva" AS TCM ON (TCM."IdTipoCargaMasiva" = CM."IdTipoCargaMasiva")
LEFT OUTER JOIN "BSCL.Plataforma.BD::ACC.Usuario" 	AS USU ON (USU."IdUsuario" = CM."IdUsuario")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" 	AS EST ON (EST."IdEstado" = CM."IdEstado")

WITH READ ONLY;
GO 

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::CAR.ConsultarCargaMasiva" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
go
