SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.ConsultarCertificadosDJs";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.ConsultarCertificadosDJs" (
	"Esquema"
	,"Version"
	,"UltimoFolio"
	,"CodigoCertificado"
	,"NombreCertificado"
	,"CodigoPrograma"
) AS
SELECT
	"Esquema"
	,"Version"
	,"UltimoFolio"
	,"CodigoCertificado"
	,"NombreCertificado"
	,"CodigoPrograma"
FROM "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.ConsultarCertificadosDJs" TO bcs_desarrollador WITH GRANT OPTION;
