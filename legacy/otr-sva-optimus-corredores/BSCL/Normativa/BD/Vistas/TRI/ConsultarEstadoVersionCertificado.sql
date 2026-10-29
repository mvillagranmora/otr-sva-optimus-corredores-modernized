SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.ConsultarEstadoVersionCertificado";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.ConsultarEstadoVersionCertificado" (
 "IdEmpresa"
,"IdEstado"
,"IdTipoCertificado"
,"Version"
,"Cantidad"

)
AS 
SELECT x."IdEmpresa",x."IdEstado", x."IdTipoCertificado",x."Version",Count(1) as "Cantidad"
FROM 
 		(
			SELECT C."IdEmpresa",C."RutCliente",C."IdEstado", C."IdTipoCertificado",C."Version"
			FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" C
			GROUP BY  C."IdEmpresa",C."RutCliente",C."IdEstado", C."IdTipoCertificado",C."Version"
			
		)	AS x
GROUP BY  x."IdEmpresa",x."IdEstado", x."IdTipoCertificado",x."Version"

WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.ConsultarEstadoVersionCertificado" TO bcs_desarrollador WITH GRANT OPTION;