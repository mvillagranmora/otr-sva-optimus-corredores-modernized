SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarCuadraturaDiariaOperaciones"; 
GO 
CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarCuadraturaDiariaOperaciones" As 
SELECT
	"IdCuadraturaDiariaOperaciones"
	,"IdEmpresa"
	,"OperacionCuadraturaDiariaOperaciones"
	,"FechaEjecucion"
	,"TotalOperaciones"
	,"OperacionesConProblemas"
	,"TipoOperacion"
	,"Resultado"
FROM "BSCL.Tesoreria.BD::TES.CuadraturaDiariaOperaciones";
GO
GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarCuadraturaDiariaOperaciones" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
GO