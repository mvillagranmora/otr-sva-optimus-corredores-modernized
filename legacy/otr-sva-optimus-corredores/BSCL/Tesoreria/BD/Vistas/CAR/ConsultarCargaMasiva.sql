SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Tesoreria.BD.Vistas::CAR.ConsultarCargaMasiva";
GO
CREATE VIEW "BSCL.Tesoreria.BD.Vistas::CAR.ConsultarCargaMasiva" AS

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
 	CM."FechaProceso",          -- Fecha en que se procesó la CM (Se crearon los mov tes) 
  	CM."FechaIngresoRegistro",  -- Fecha BD en ingreso
  	CM."CantidadRegistros",
  	CM."MontoTotal",
	CM."IdEstado" AS "IdEstadoCargaMasiva",
	EST."Nombre" AS "NombreEstadoCargaMasiva",
	(CASE 
		WHEN COALESCE(CM."IdMoneda",'') <> '' THEN CM."IdMoneda"
		WHEN (COALESCE(CM."IdMoneda",'') = '' AND CM."IdEstado" = 'PRO') THEN 'MULTIPLES' 
		ELSE ''
	END) AS "IdMoneda",
	CM."IdConcepto",
	CON."Nombre" AS "NombreConcepto"

FROM "BSCL.Tesoreria.BD::CAR.CargaMasiva" AS CM
INNER JOIN "BSCL.Tesoreria.BD::CAR.TipoCargaMasiva" AS TCM ON (TCM."CodigoTipoCargaMasiva" = CM."CodigoTipoCargaMasiva")
LEFT OUTER JOIN "BSCL.Plataforma.BD::ACC.Usuario" 	AS USU ON (USU."IdUsuario" = CM."IdUsuario")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" 	AS EST ON (EST."IdEstado" = CM."IdEstado")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Concepto" 	AS CON ON (CON."IdConcepto" = CM."IdConcepto")

WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::CAR.ConsultarCargaMasiva" TO  BCS_DESARROLLADOR WITH GRANT OPTION;



