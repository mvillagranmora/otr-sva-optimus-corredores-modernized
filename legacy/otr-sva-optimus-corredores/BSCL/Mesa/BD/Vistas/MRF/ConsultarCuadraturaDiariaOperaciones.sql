SET SCHEMA "BSCL";
DROP VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultarCuadraturaDiariaOperaciones";

CREATE VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultarCuadraturaDiariaOperaciones" AS
SELECT
	"IdCuadraturaDiariaOperaciones"
	,"IdEmpresa"
	,"OperacionCuadraturaDiariaOperaciones"
	,"FechaEjecucion"
	,"TotalOperaciones"
	,"OperacionesConProblemas"
	,"TipoOperacion"
	,"Resultado"
FROM "BSCL.Mesa.BD::MES.CuadraturaDiariaOperacionesMRF";

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::MRF.ConsultarCuadraturaDiariaOperaciones" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
