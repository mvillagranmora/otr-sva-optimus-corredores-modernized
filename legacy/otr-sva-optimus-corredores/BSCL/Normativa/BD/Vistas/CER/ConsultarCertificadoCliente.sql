SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Normativa.BD.Vistas::CER.ConsultarCertificadoCliente"; 
 
CREATE VIEW "BSCL.Normativa.BD.Vistas::CER.ConsultarCertificadoCliente"  
AS
SELECT 
cc."IdCertificadoCliente",
cc."IdTipoDocumento",
cc."IdEmpresa",
cc."Esquema",
cc."Anio",
cc."Periodo",
cc."IdCliente",
cc."IdPortafolio",
cc."Version",
cc."JsonData",
an."IdArchivo",
an."NombreArchivo",
an."TipoArchivo",
an."ServicioArchivo"
FROM "BSCL.Normativa.BD::CER.CertificadoCliente" cc
INNER JOIN "BSCL.Normativa.BD.Vistas::NOR.ConsultarArchivoNormativa" an ON cc."Esquema" = an."Esquema" AND cc."Version" = an."Version" AND cc."IdTipoDocumento" = an."IdTipoDocumento";

--INNER JOIN "BSCL.Report::Template" t ON an."IdArchivo" = t."Id";	
		
GRANT SELECT ON "BSCL.Normativa.BD.Vistas::CER.ConsultarCertificadoCliente" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
