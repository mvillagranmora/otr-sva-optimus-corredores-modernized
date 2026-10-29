SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarCuadraturaDiariaOperaciones";
GO
CREATE VIEW "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarCuadraturaDiariaOperaciones"
AS
SELECT
	"IdCuadraturaDiariaOperaciones"
	,"IdEmpresa"
	,"IdTipoCentralizacion"
	,"OperacionCuadraturaDiariaOperaciones"
	,"FechaEjecucion"
	,"TotalOperaciones"
	,"OperacionesConProblemas"
	,"TipoOperacion"
	,"Resultado"
	,"Orden"
FROM "BSCL.Contabilidad.BD::ITZ.CuadraturaDiariaOperaciones";
GO
GRANT SELECT ON "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarCuadraturaDiariaOperaciones" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO