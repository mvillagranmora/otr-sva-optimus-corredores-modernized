SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::CAR.ConsultarCargaMasivaPlataforma";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::CAR.ConsultarCargaMasivaPlataforma" AS

SELECT 
	TCM."IdTipoCargaMasiva",
	TCM."Codigo" 	AS "CodigoTipoCargaMasiva",
	TCM."Nombre" 	AS "NombreTipoCargaMasiva",
	TCM."IdEstado" 	AS "IdEstadoTipoCargaMasiva",
	CM."IdCargaMasiva",
	CM."IdEmpresa",
	CM."IdUsuario",
	CM."NombreArchivo",
	USU."CodigoUsuario",
	CM."FechaCarga",            -- Fecha Sistema
 	CM."FechaProceso",         -- Fecha en que se procesó la CM 
  	CM."FechaIngresoRegistro", -- Fecha BD en ingreso
  	CM."CantidadRegistros",  	
	CM."IdEstado" AS "IdEstadoCargaMasiva",
	ECM."Nombre" AS "NombreEstadoCargaMasiva"	
		
FROM "BSCL.Plataforma.BD::PLA.CargaMasiva" AS CM
	INNER JOIN "BSCL.Plataforma.BD::PLA.TipoCargaMasiva" AS TCM 
		ON (TCM."IdTipoCargaMasiva" = CM."IdTipoCargaMasiva")
	LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario" AS USU 
		ON (USU."IdUsuario" = CM."IdUsuario")
	LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" AS ECM 
		ON (ECM."IdEstado" = CM."IdEstado")

WITH READ ONLY;
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::CAR.ConsultarCargaMasivaPlataforma" TO  BCS_DESARROLLADOR WITH GRANT OPTION;