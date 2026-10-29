SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::CRE.ConsultarCierreProcesosFunciones"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::CRE.ConsultarCierreProcesosFunciones" AS
SELECT  
	"IdModuloFuncionProceso",
	 "IdCREModuloEmpresa",
	 "Descripcion",
	 "NombreFuncionProceso",
	 "PermiteEjecucion",
	 "IdEstado"
FROM "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::CRE.ConsultarCierreProcesosFunciones" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
