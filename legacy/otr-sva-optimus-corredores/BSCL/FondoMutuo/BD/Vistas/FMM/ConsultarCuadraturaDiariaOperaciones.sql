SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarCuadraturaDiariaOperaciones";
GO
CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarCuadraturaDiariaOperaciones"
AS
SELECT
	"IdCuadraturaDiariaOperaciones"
	,"IdEmpresa"
	,"OperacionCuadraturaDiariaOperaciones"
	,"FechaEjecucion"
	,"TotalOperaciones"
	,"OperacionesConProblemas"
	,"TipoOperacion"
	,"Resultado"
FROM "BSCL.FondoMutuo.BD::FMM.CuadraturaDiariaOperaciones";
GO
GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarCuadraturaDiariaOperaciones" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO