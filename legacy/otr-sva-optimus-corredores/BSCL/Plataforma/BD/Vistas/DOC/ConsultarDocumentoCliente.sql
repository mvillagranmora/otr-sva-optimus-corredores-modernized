SET SCHEMA "BSCL";
GO
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::DOC.ConsultarDocumentoCliente";
GO

CREATE VIEW "BSCL.Plataforma.BD.Vistas::DOC.ConsultarDocumentoCliente" as
SELECT
	Documento."IdDocumento"
   ,Documento."IdTipoDocumento"
   ,Documento."IdEmpresa"
   ,Documento."IdCliente"
   ,Documento."IdUltimoAnexo"
   ,Documento."IdCarga" AS "IdCargaDocumento"
   ,Anexo."IdAnexo"
   ,Anexo."IdEstado"
   ,Anexo."NumeroFirmantesEmpresa"
   ,Anexo."NumeroFirmantesCliente"
   ,Anexo."NombreFirmanteEmpresa1"
   ,Anexo."NombreFirmanteEmpresa2"
   ,Anexo."NombreFirmanteEmpresa3"
   ,Anexo."NombreFirmanteCliente1"
   ,Anexo."NombreFirmanteCliente2"
   ,Anexo."NombreFirmanteCliente3"
   ,Anexo."FirmaEmpresa1"
   ,Anexo."FirmaEmpresa2"
   ,Anexo."FirmaEmpresa3"
   ,Anexo."FirmaCliente1"
   ,Anexo."FirmaCliente2"
   ,Anexo."FirmaCliente3"
   ,Anexo."IdUsuarioModifica"
   ,Anexo."FechaIngreso"
   ,Anexo."FechaModificacion"
   ,Anexo."FechaVigencia"
   ,Anexo."DocumentoFisicoReferencia"
   ,Anexo."ArchivoAdjunto"
   ,Anexo."IdCarga" AS "IdCargaAnexo"
   ,TipoDocumento."NombreDocumento"
   ,TipoDocumento."Glosa"
   ,TipoDocumento."ValidaFechaVigencia"
   ,TipoDocumento."CodigoTipoIndicador"
   ,TipoDocumento."IdTipoPersona"
FROM "BSCL.Plataforma.BD::DOC.Documento" AS Documento
INNER JOIN "BSCL.Plataforma.BD::DOC.Anexo" AS Anexo ON Anexo."IdDocumento" = Documento."IdDocumento"
LEFT JOIN "BSCL.Plataforma.BD::DOC.TipoDocumento" AS TipoDocumento ON TipoDocumento."IdTipoDocumento" = Documento."IdTipoDocumento";
GO

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::DOC.ConsultarDocumentoCliente" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO
