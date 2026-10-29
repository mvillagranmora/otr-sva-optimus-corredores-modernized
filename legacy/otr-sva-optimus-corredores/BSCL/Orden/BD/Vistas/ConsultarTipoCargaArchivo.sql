SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarTipoCargaArchivo"; 
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarTipoCargaArchivo"  
AS SELECT 
	"Codigo",
	"Descripcion",
	"IdEmpresa",
	"Estado",
	"Formato",
	"EjemploLinea"
FROM "BSCL.Orden.BD::ORD.TipoCargaArchivo";
	
GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarTipoCargaArchivo" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
