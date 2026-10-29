SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.ConsultarCertificadoICliente";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.ConsultarCertificadoICliente" (
	"IdTipoCertificado", 
	"IdCliente", 
	"IdPortafolio", 
	"Folio", 
	"TipoArchivo", 
	"IdArchivo", 
	"NombreArchivo", 
	"ServicioArchivo",
	"Version",
	"Descripcion",
	"RutCliente",
	"NombreCertificado", 
	"DescripcionCertificado",
	"Esquema"
) AS
select "IdTipoCertificado", "IdCliente", "IdPortafolio", "Folio", "TipoArchivo", "IdArchivo", "NombreArchivo", "ServicioArchivo", "Version", "Descripcion",
"RutCliente","NombreCertificado", "DescripcionCertificado","Esquema"
from (
	select cc."IdTipoCertificado", cc."IdCliente", cc."IdPortafolio", cc."Folio", an."TipoArchivo", an."IdArchivo", 
	an."NombreArchivo", an."ServicioArchivo", cc."Version", an."Descripcion",
	cc."RutCliente",tc."Nombre" AS "NombreCertificado",tc."Descripcion" as "DescripcionCertificado",an."Esquema",
	row_number() OVER (partition by cc."Esquema",cc."Version",cc."RutCliente",cc."IdTipoCertificado" order BY cc."Folio" DESC) AS "Row"
	from "BSCL.Normativa.BD::TRI.CertificadoCliente" cc
	 join "BSCL.Normativa.BD.Vistas::NOR.ConsultarArchivoNormativa" an on (an."IdTipoDocumento" = cc."IdTipoCertificado" AND cc."Version" = an."VersionArchivo")
	LEFT JOIN "BSCL.Normativa.BD::TRI.TipoCertificado" tc ON (tc."IdTipoCertificado" = cc."IdTipoCertificado")
) where "Row" = 1

WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.ConsultarCertificadoICliente" TO bcs_desarrollador WITH GRANT OPTION;
