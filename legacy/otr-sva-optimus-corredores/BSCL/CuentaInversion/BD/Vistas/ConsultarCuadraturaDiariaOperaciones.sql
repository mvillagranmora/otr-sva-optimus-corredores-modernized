SET SCHEMA "BSCL";
DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarCuadraturaDiariaOperaciones";

CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarCuadraturaDiariaOperaciones" AS
SELECT
	"IdCuadraturaDiariaOperaciones"
	,"IdEmpresa"
	,"OperacionCuadraturaDiariaOperaciones"
	,"FechaEjecucion"
	,"TotalOperaciones"
	,"OperacionesConProblemas"
	,"TipoOperacion"
	,"Resultado" 
  FROM "BSCL.CuentaInversion.BD::CTA.CuadraturaDiariaOperaciones";
  
GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarCuadraturaDiariaOperaciones" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
  
