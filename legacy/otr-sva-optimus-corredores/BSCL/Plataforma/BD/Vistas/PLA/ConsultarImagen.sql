SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"  
AS
SELECT 
	"IdEmpresa",
	"IdImagen",
	"NombreImagen",
	"Tipo",
	"Extension",
	"Version",
	"Binario",
	"RutaPublica"
From "BSCL.Plataforma.BD::PLA.Imagenes";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 