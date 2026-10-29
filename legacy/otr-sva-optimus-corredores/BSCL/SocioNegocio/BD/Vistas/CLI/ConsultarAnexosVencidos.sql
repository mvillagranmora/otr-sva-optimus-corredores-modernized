SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarAnexosVencidos"; 
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarAnexosVencidos" AS
select
	DOC."IdTipoDocumento",
	TPO."NombreDocumento",
	ANX."NumeroFirmantesEmpresa",
	ANX."NumeroFirmantesCliente",
    COALESCE(ANX."NumeroFirmasMinimas", TPO."NumeroFirmasMinimas") AS "NumeroFirmasMinimas",
    COALESCE(ANX."NumeroFirmasMaximas", TPO."NumeroFirmasMaximas") AS "NumeroFirmasMaximas",
	ANX."IdAnexo" AS "IdAnexo",
	DOC."IdCliente",
	DOC."IdDocumento",
	IFNULL(ANX."IdEstado",'PEN') as "IdEstado",
	EST."Nombre" AS "NombreEstado",
	ANX."FechaVigencia",
	ANX."NombreFirmanteCliente1",
	ANX."NombreFirmanteCliente2",
	ANX."NombreFirmanteCliente3",
	ANX."NombreFirmanteEmpresa1",
	ANX."NombreFirmanteEmpresa2",
	ANX."NombreFirmanteEmpresa3",
	ANX."FirmaEmpresa1",
	ANX."FirmaEmpresa2",
	ANX."FirmaEmpresa3",
	ANX."FirmaCliente1",
	ANX."FirmaCliente2",
	ANX."FirmaCliente3",
    TPO."CodigoArchivo" AS "CodigoArchivo",
    AC."Descripcion" AS "NombrePlantillaContrato",
	ANX."NombreArchivoAdjunto"
	FROM "BSCL.Plataforma.BD::DOC.Documento" DOC 
    JOIN "BSCL.Plataforma.BD::DOC.Anexo" ANX ON ANX."IdDocumento" = DOC."IdDocumento" AND ANX."IdEstado" = 'VCD' 
    JOIN "BSCL.Plataforma.BD::DOC.TipoDocumento" TPO ON TPO."IdTipoDocumento" = DOC."IdTipoDocumento"
	JOIN "BSCL.Plataforma.BD::PLA.Estado" EST ON EST."IdEstado" = IFNULL(ANX."IdEstado",'PEN')
        LEFT JOIN "BSCL.Plataforma.BD::CTT.ArchivosContrato" AC ON (AC."CodigoArchivo" = TPO."CodigoArchivo" AND AC."IdEmpresa" = TPO."IdEmpresa")
	ORDER BY "IdTipoDocumento";

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarAnexosVencidos" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
