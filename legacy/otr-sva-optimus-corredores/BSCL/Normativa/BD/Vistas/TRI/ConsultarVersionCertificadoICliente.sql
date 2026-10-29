SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.ConsultarVersionCertificadoICliente";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.ConsultarVersionCertificadoICliente" (
	"Version"
	,"IdCliente"
	,"DocumentoIdentidad"
) AS
select distinct "Version", "IdCliente",  "RutCliente" AS "DocumentoIdentidad"
from "BSCL.Normativa.BD::TRI.CertificadoCliente"
group by "Version", "IdCliente","RutCliente"

WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.ConsultarVersionCertificadoICliente" TO bcs_desarrollador WITH GRANT OPTION;
