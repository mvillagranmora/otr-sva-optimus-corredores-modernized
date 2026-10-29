SET SCHEMA BSCL;

DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarTodosLosMercados";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarTodosLosMercados" AS
SELECT ARB."IdArbolClasificacionActivo",
	       ARB."IdArbolClasificacionActivo" As "IdArbolClasificacionActivo2",
		   ARB."Descripcion", 
		   ARB."Padre",
		   ARB."Indice"
	FROM "BSCL.Plataforma.BD::INS.ArbolClasificacionActivos" AS ARB
	WHERE "Padre"=0;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarTodosLosMercados"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;