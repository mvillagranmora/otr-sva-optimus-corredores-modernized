SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarOmnibusDetalle";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarOmnibusDetalle"
AS
SELECT 
	O."IdOmnibus"
	,O."FechaOmnibus"
	,O."FechaIngresoRegistro"
	,O."IdEstado" AS "IdEstadoArchivo"
	,OD."IdOmnibusDetalle"
	,OD."IdSerieFondo"
	,OD."CantidadOperaciones"
	,OD."ValorCuota"
	,OD."UltimoValorCuota"
	,OD."Monto"
	,OD."CantidadCuotas"
	,OD."ValorCuotaAGF"
	,OD."MontoAGF"
	,OD."CantidadCuotasAGF"
	,OD."DiferenciaValorCuota"
	,OD."DiferenciaValorCuotaAGF"
	,OD."IdEstado"
	,S."DecimalesCuota"
	,S."DecimalesValorCuota"
	,E."Nombre" AS "NombreEstado"
	,F."IdMoneda"
	,F."IdEmisor"
FROM "BSCL.FondoMutuo.BD::FMM.OmnibusDetalle" AS OD
INNER JOIN "BSCL.FondoMutuo.BD::FMM.Omnibus" AS O ON (OD."IdOmnibus" = O."IdOmnibus")
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS S ON (S."IdSerieFondo" = OD."IdSerieFondo")
INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS F ON (F."IdFondo" = S."IdFondo")
LEFT OUTER  JOIN "BSCL.Plataforma.BD::PLA.Estado" AS E ON (E."IdEstado" =  OD."IdEstado");

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarOmnibusDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
