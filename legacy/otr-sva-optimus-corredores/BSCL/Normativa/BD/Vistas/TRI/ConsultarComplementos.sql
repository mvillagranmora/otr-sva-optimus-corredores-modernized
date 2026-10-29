SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.ConsultarComplementos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.ConsultarComplementos" (
	"Esquema"
	,"CodigoComplemento"
	,"Descripcion"
) AS
SELECT
	"Esquema"
	,"CodigoComplemento"
	,"Descripcion"
FROM "BSCL.Normativa.BD::TRI.Complemento"
WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.ConsultarComplementos" TO bcs_desarrollador WITH GRANT OPTION;
