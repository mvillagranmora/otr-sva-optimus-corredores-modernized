SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarCampos";
 
CREATE VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarCampos" AS
SELECT 
	"Esquema",
	"Version",
	"Nombre",
	"CodigoSII",
	"TipoDiccionario",
	"Orden",
	"Titulo",
	"Ayuda",
	"TipoDato",
	"Largo",
	"ValorDefecto",
	"Obligatorio",
	"Editable",
	"ModoMasivo",
	"Exportable",
	"OcultoEnGrilla",
	"OcultoEnMantenedor",
	"Comun",
	"Clave",
	"Grupo",
	"OrdenCargaExterna",
	"CantidadDecimales"
FROM "BSCL.Normativa.BD::DOC.Campo"
ORDER BY "Orden" ASC;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::DOC.ConsultarCampos" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 