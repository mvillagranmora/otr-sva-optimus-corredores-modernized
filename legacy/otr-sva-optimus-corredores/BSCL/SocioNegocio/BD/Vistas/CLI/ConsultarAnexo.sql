SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarAnexo"; 
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarAnexo" AS
SELECT 
DOC."IdCliente",
ANX."IdAnexo",
TPO."IdTipoDocumento",
TPO."NombreDocumento",
ANX."IdEstado",
ANX."FechaModificacion",
EST."Nombre" as "NombreEstado",
ANX."IdDocumento",
ANX."NumeroFirmantesEmpresa",
ANX."NumeroFirmantesCliente",
--ANX."IdUsuarioEmpresa1",
ANX."NombreFirmanteEmpresa1" as "NombreFirmanteEmpresa1",
ANX."FirmaEmpresa1",
ANX."FechaFirmaEmpresa1",
--ANX."IdUsuarioEmpresa2",
ANX."NombreFirmanteEmpresa2" as "NombreFirmanteEmpresa2",
ANX."FirmaEmpresa2",
ANX."FechaFirmaEmpresa2",
--ANX."IdUsuarioEmpresa3",
ANX."NombreFirmanteEmpresa3" as "NombreFirmanteEmpresa3",
ANX."FirmaEmpresa3",
ANX."FechaFirmaEmpresa3",
--ANX."IdUsuarioCliente1",
ANX."NombreFirmanteCliente1" as "NombreFirmanteCliente1",
ANX."FirmaCliente1",
ANX."FechaFirmaCliente1",
--ANX."IdUsuarioCliente2",
ANX."NombreFirmanteCliente2" as "NombreFirmanteCliente2",
ANX."FirmaCliente2",
ANX."FechaFirmaCliente2",
--ANX."IdUsuarioCliente3",
ANX."NombreFirmanteCliente3" as "NombreFirmanteCliente3",
ANX."FirmaCliente3",
ANX."FechaFirmaCliente3",
ANX."FechaVigencia"
from "BSCL.Plataforma.BD::DOC.TipoDocumento" TPO 
INNER JOIN "BSCL.Plataforma.BD::DOC.Documento" DOC ON TPO."IdTipoDocumento" = DOC."IdTipoDocumento"
INNER JOIN "BSCL.Plataforma.BD::DOC.Anexo" ANX ON ANX."IdDocumento" = DOC."IdDocumento" AND ANX."IdAnexo" = DOC."IdUltimoAnexo" AND ANX."IdEstado" != 'VCD'
INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" EST ON EST."IdEstado" = IFNULL(ANX."IdEstado",'PEN');

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarAnexo" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 