SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarCuadraturaDiariaOperaciones";
GO
CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarCuadraturaDiariaOperaciones"
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
FROM "BSCL.Custodia.BD::CUS.CuadraturaDiariaOperaciones";
GO
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarCuadraturaDiariaOperaciones" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO