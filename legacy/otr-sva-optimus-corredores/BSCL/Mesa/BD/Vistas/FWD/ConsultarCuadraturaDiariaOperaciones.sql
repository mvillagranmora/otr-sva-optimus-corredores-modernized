SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Mesa.BD.Vistas::FWD.ConsultarCuadraturaDiariaOperaciones";

CREATE VIEW "BSCL.Mesa.BD.Vistas::FWD.ConsultarCuadraturaDiariaOperaciones"
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
FROM "BSCL.Mesa.BD::MES.CuadraturaDiariaOperacionesFWD";

  		  
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::FWD.ConsultarCuadraturaDiariaOperaciones" TO BCS_DESARROLLADOR WITH GRANT OPTION; 

