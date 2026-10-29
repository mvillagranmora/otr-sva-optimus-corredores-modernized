SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilCobroAnalitica"; 
 		
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilCobroAnalitica" AS
SELECT
	dpc."IdPerfilCobros",
	pc."DescripcionPerfilCobros",
	dpc."IdEstado",
	dpc."IdValorCobro",
	dpc."IdBolsa",
	dpc."IdTipoProducto",
	dpc."IdTipoOperacion",
	dpc."IdCanalIngreso",
	dpc."IdMoneda",
	pc."IdEstado" AS "IdEstadoPerfilCobro",
	TO_TIMESTAMP(dpc."FechaCreacion") AS "FechaCreacion",
	dpc."FechaIngreso",
	dpc."FechaModificacion",
	TO_TIMESTAMP(pc."FechaCreacion") AS "FechaCreacionPerfilCobro",
	TO_TIMESTAMP(pc."FechaModificacion") AS "FechaModificacionPerfilCobro",
	pc."FechaIngreso" AS "FechaIngresoPerfilCobro",
	pc."IdEmpresa",
	dpc."IdDetallePerfilCobros",
	dpc."DiasPlazo",
	dpc."Prioridad"
FROM "BSCL.Plataforma.BD::COM.PerfilCobros" pc
INNER JOIN "BSCL.Plataforma.BD::COM.DetallePerfilCobros" dpc ON pc."IdPerfilCobros" = dpc."IdPerfilCobros";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilCobroAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 