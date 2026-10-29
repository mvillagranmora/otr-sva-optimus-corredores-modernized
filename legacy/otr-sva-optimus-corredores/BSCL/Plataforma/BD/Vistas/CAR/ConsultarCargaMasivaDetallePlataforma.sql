SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::CAR.ConsultarCargaMasivaDetallePlataforma";
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::CAR.ConsultarCargaMasivaDetallePlataforma" AS

SELECT
	CM."IdCargaMasiva",	
	CM."IdTipoCargaMasiva",
	CTCMC."Nombre"													AS "NombreTipoCargaMasiva",
	CM."IdEmpresa",
	CM."IdUsuario",
  	CM."FechaCarga",   			-- Fecha Sistema
 	CM."FechaProceso", 			-- Fecha en que se procesa CM
 	CM."FechaIngresoRegistro",	-- Fecha de BD al ingreso
 	CM."NombreArchivo",
	CM."IdEstado" 													AS "IdEstadoCargaMasiva",	
	CMD."IdCargaMasivaDetalle",
	CMD."Nemotecnico",
	CMD."ObservacionValidacion",
	CMD."IdEstado" 													AS "IdEstadoCargaMasivaDetalle",
	CMD."RF_PorcentajeDescuento",
	CMD."RV_IdEntidadClasificadoraRiesgo",
	CMD."RV_Valor"	
FROM "BSCL.Plataforma.BD::PLA.CargaMasiva" AS CM
	INNER JOIN "BSCL.Plataforma.BD::PLA.CargaMasivaDetalle" AS CMD 
		ON (CMD."IdCargaMasiva" = CM."IdCargaMasiva")
	LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" AS ECMD 
		ON (ECMD."IdEstado" = CMD."IdEstado")
	INNER JOIN "BSCL.Plataforma.BD.Vistas::CAR.ConsultarTipoCargaMasivaPlataforma"	AS CTCMC
		ON CM."IdTipoCargaMasiva" = CTCMC."IdTipoCargaMasiva"
	   AND CTCMC."IdEstado" = 'ACT'

WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::CAR.ConsultarCargaMasivaDetallePlataforma" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
