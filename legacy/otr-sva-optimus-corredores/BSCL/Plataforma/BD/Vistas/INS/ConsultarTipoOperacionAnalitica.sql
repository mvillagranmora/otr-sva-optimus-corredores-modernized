SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacionAnalitica"; 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacionAnalitica" AS
SELECT 
	"IdTipoOperacion",
	CAST("Codigo" AS NVARCHAR(40)) AS "Codigo",
	CAST("Nombre" AS NVARCHAR(100)) AS "Nombre",
	"IdTipoProducto",
	case when "IdTipoOperacionRelacionada" = '' then null else "IdTipoOperacionRelacionada" end as "IdTipoOperacionRelacionada",
	CAST("Accion" AS NVARCHAR (40)) AS "Accion" 
	 	  		FROM  "BSCL.Plataforma.BD::INS.TipoOperacion"
	UNION 
				SELECT 
	'0',
	'0',
	'TODAS LAS OPERACIONES',
	'0',
	NULL,
	''
FROM DUMMY;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacionAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;