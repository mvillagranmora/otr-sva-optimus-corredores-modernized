SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarCuadraturaDiariaOperaciones";

CREATE VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarCuadraturaDiariaOperaciones"
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
FROM "BSCL.Mesa.BD::SPT.CuadraturaDiariaOperaciones";


GRANT SELECT ON "BSCL.Mesa.BD.Vistas::SPT.ConsultarCuadraturaDiariaOperaciones" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

