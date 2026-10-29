SET SCHEMA "BSCL";

DROP TABLE "BSCL.Normativa.BD::TRI.DatosDJ1839";

create column table "BSCL.Normativa.BD::TRI.DatosDJ1839" (
    "ID" BIGINT NOT NULL ,
	"IdDocumento" BIGINT NOT NULL ,
	"VboUsuario" CHAR(1) NOT NULL ,
	"Observaciones" NVARCHAR(250) NOT NULL ,
	"FuenteDatos" NVARCHAR(40) NOT NULL ,
	"Numero" BIGINT  NOT NULL ,
	"RutCliente" NVARCHAR(40) NOT NULL ,
	"TaxID" NVARCHAR(40) NOT NULL ,
	"RutClienteB" NVARCHAR(40) NOT NULL ,
	"TaxIDB" NVARCHAR(40) NOT NULL ,
	"CodigoPaisA" NVARCHAR(10) NOT NULL ,
	"CodigoPaisB" NVARCHAR(10) NOT NULL ,
	"AcuerdoMarcoTipo" BIGINT NOT NULL ,
	"AcuerdoMarcoNumero" BIGINT  NOT NULL ,
	"AcuerdoMarcoFechaSuscripcion" DATE NOT NULL ,
	"ContratoNumero" BIGINT NOT NULL ,
	"ContratoFechaSuscripcion" DATE NOT NULL ,
	"ContratoTipo" BIGINT NOT NULL ,
	"ContratoNombreInstrumento" NVARCHAR(100) NOT NULL ,
	"FechaSuscripcion" DATE,
	"IdCliente" BIGINT,
	"IdPortafolio" BIGINT, 
    primary key ("ID")
);

alter table "BSCL.Normativa.BD::TRI.DatosDJ1839"
    add constraint "FK_Documento_DatosDJ1839" foreign key ("IdDocumento")
        references "BSCL.Normativa.BD::DOC.Documento" ("IdDocumento")
        on delete restrict on update restrict;
        
--Limpiar todas las versiones        
DELETE FROM "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1839";
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1839';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema"='DJ1839';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1839';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Documento" where "Esquema" = 'DJ1839';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1839';
DELETE FROM "BSCL"."BSCL.Normativa.BD::TRI.ControlProceso" WHERE  "Esquema" = 'DJ1839';


--ESQUEMA
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Esquema"
("Esquema", "Version", "Modulo", "Periodicidad", "Objetivo", "PeriodoInicio", "IdEntidadReguladora", "FechaPresentacionLimite", "FechaDatosDesde", "FechaDatosHasta", "CodigoVersionSII", "HabilitadoMantenedor", "TieneCertificado")
VALUES('DJ1839', '2020/01', 'TRI', 'A', '', '2020-01-01', 'SII', '2021-01-01', '2020-01-01', '2020-01-31', 0, 'S', 'N');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Esquema"
("Esquema", "Version", "Modulo", "Periodicidad", "Objetivo", "PeriodoInicio", "IdEntidadReguladora", "FechaPresentacionLimite", "FechaDatosDesde", "FechaDatosHasta", "CodigoVersionSII", "HabilitadoMantenedor", "TieneCertificado")
VALUES('DJ1839', '2020/02', 'TRI', 'A', '', '2020-01-01', 'SII', '2021-01-01', '2020-02-01', '2020-03-02', 0, 'S', 'N');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Esquema"
("Esquema", "Version", "Modulo", "Periodicidad", "Objetivo", "PeriodoInicio", "IdEntidadReguladora", "FechaPresentacionLimite", "FechaDatosDesde", "FechaDatosHasta", "CodigoVersionSII", "HabilitadoMantenedor", "TieneCertificado")
VALUES('DJ1839', '2020/03', 'TRI', 'A', '', '2020-01-01', 'SII', '2021-01-01', '2020-03-01', '2020-03-31', 0, 'S', 'N');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Esquema"
("Esquema", "Version", "Modulo", "Periodicidad", "Objetivo", "PeriodoInicio", "IdEntidadReguladora", "FechaPresentacionLimite", "FechaDatosDesde", "FechaDatosHasta", "CodigoVersionSII", "HabilitadoMantenedor", "TieneCertificado")
VALUES('DJ1839', '2020/04', 'TRI', 'A', '', '2020-01-01', 'SII', '2021-01-01', '2020-04-01', '2020-05-01', 0, 'S', 'N');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Esquema"
("Esquema", "Version", "Modulo", "Periodicidad", "Objetivo", "PeriodoInicio", "IdEntidadReguladora", "FechaPresentacionLimite", "FechaDatosDesde", "FechaDatosHasta", "CodigoVersionSII", "HabilitadoMantenedor", "TieneCertificado")
VALUES('DJ1839', '2020/05', 'TRI', 'A', '', '2020-01-01', 'SII', '2021-01-01', '2020-05-01', '2020-05-31', 0, 'S', 'N');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Esquema"
("Esquema", "Version", "Modulo", "Periodicidad", "Objetivo", "PeriodoInicio", "IdEntidadReguladora", "FechaPresentacionLimite", "FechaDatosDesde", "FechaDatosHasta", "CodigoVersionSII", "HabilitadoMantenedor", "TieneCertificado")
VALUES('DJ1839', '2020/06', 'TRI', 'A', '', '2020-01-01', 'SII', '2021-01-01', '2020-06-01', '2020-07-01', 0, 'S', 'N');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Esquema"
("Esquema", "Version", "Modulo", "Periodicidad", "Objetivo", "PeriodoInicio", "IdEntidadReguladora", "FechaPresentacionLimite", "FechaDatosDesde", "FechaDatosHasta", "CodigoVersionSII", "HabilitadoMantenedor", "TieneCertificado")
VALUES('DJ1839', '2020/07', 'TRI', 'A', '', '2020-01-01', 'SII', '2021-01-01', '2020-07-01', '2020-07-31', 0, 'S', 'N');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Esquema"
("Esquema", "Version", "Modulo", "Periodicidad", "Objetivo", "PeriodoInicio", "IdEntidadReguladora", "FechaPresentacionLimite", "FechaDatosDesde", "FechaDatosHasta", "CodigoVersionSII", "HabilitadoMantenedor", "TieneCertificado")
VALUES('DJ1839', '2020/08', 'TRI', 'A', '', '2020-01-01', 'SII', '2021-01-01', '2020-08-01', '2020-08-31', 0, 'S', 'N');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Esquema"
("Esquema", "Version", "Modulo", "Periodicidad", "Objetivo", "PeriodoInicio", "IdEntidadReguladora", "FechaPresentacionLimite", "FechaDatosDesde", "FechaDatosHasta", "CodigoVersionSII", "HabilitadoMantenedor", "TieneCertificado")
VALUES('DJ1839', '2020/09', 'TRI', 'A', '', '2020-01-01', 'SII', '2021-01-01', '2020-09-01', '2020-10-01', 0, 'S', 'N');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Esquema"
("Esquema", "Version", "Modulo", "Periodicidad", "Objetivo", "PeriodoInicio", "IdEntidadReguladora", "FechaPresentacionLimite", "FechaDatosDesde", "FechaDatosHasta", "CodigoVersionSII", "HabilitadoMantenedor", "TieneCertificado")
VALUES('DJ1839', '2020/10', 'TRI', 'A', '', '2020-01-01', 'SII', '2021-01-01', '2020-10-01', '2020-10-31', 0, 'S', 'N');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Esquema"
("Esquema", "Version", "Modulo", "Periodicidad", "Objetivo", "PeriodoInicio", "IdEntidadReguladora", "FechaPresentacionLimite", "FechaDatosDesde", "FechaDatosHasta", "CodigoVersionSII", "HabilitadoMantenedor", "TieneCertificado")
VALUES('DJ1839', '2020/11', 'TRI', 'A', '', '2020-01-01', 'SII', '2021-01-01', '2020-11-01', '2020-12-01', 0, 'S', 'N');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Esquema"
("Esquema", "Version", "Modulo", "Periodicidad", "Objetivo", "PeriodoInicio", "IdEntidadReguladora", "FechaPresentacionLimite", "FechaDatosDesde", "FechaDatosHasta", "CodigoVersionSII", "HabilitadoMantenedor", "TieneCertificado")
VALUES('DJ1839', '2020/12', 'TRI', 'A', '', '2020-01-01', 'SII', '2021-01-01', '2020-12-01', '2020-12-31', 0, 'S', 'N');


--DOCUMENTO
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Documento"
("IdDocumento", "IdTipoDocumento", "Esquema", "Version", "IdEmpresa", "FechaLimite", "IdEstadoDocumento", "FechaCreacion", "FechaPresentacion", "DocumentoIdentidadDeclarante", "DocumentoIdentidadRepresentante", "DireccionElectronicaDeclaracion", "DireccionFisicaDeclaracion", "ComunaDeclaracion", "TelefonoDeclaracion", "FolioDeclaracion", "EnProceso", "FechaInicioUltimoProceso", "AnoMes")
VALUES(256, 'DECLACORR', 'DJ1839', '2020/01', 1, '2020-12-31', 'ACT', '2020-01-01 00:00:00.000', '2020-12-31 00:00:00.000', '96489000-5', '56362550-3', 'BLOCKCHAINUSR2@BCSBLOCK.COM', 'ADDQE 1234', 'SANTIAGO', '+56111222334', 'I9153103', 'N', '2022-02-07 00:47:45.517', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Documento"
("IdDocumento", "IdTipoDocumento", "Esquema", "Version", "IdEmpresa", "FechaLimite", "IdEstadoDocumento", "FechaCreacion", "FechaPresentacion", "DocumentoIdentidadDeclarante", "DocumentoIdentidadRepresentante", "DireccionElectronicaDeclaracion", "DireccionFisicaDeclaracion", "ComunaDeclaracion", "TelefonoDeclaracion", "FolioDeclaracion", "EnProceso", "FechaInicioUltimoProceso", "AnoMes")
VALUES(257, 'DECLACORR', 'DJ1839', '2020/02', 1, '2020-12-31', 'ACT', '2020-01-01 00:00:00.000', '2020-12-31 00:00:00.000', '96489000-5', '56362550-3', 'BLOCKCHAINUSR2@BCSBLOCK.COM', 'ADDQE 1234', 'SANTIAGO', '+56111222334', 'I9153103', 'N', '2022-02-07 00:47:45.517', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Documento"
("IdDocumento", "IdTipoDocumento", "Esquema", "Version", "IdEmpresa", "FechaLimite", "IdEstadoDocumento", "FechaCreacion", "FechaPresentacion", "DocumentoIdentidadDeclarante", "DocumentoIdentidadRepresentante", "DireccionElectronicaDeclaracion", "DireccionFisicaDeclaracion", "ComunaDeclaracion", "TelefonoDeclaracion", "FolioDeclaracion", "EnProceso", "FechaInicioUltimoProceso", "AnoMes")
VALUES(258, 'DECLACORR', 'DJ1839', '2020/03', 1, '2020-12-31', 'ACT', '2020-01-01 00:00:00.000', '2020-12-31 00:00:00.000', '96489000-5', '56362550-3', 'BLOCKCHAINUSR2@BCSBLOCK.COM', 'ADDQE 1234', 'SANTIAGO', '+56111222334', 'I9153103', 'N', '2022-02-07 00:47:45.517', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Documento"
("IdDocumento", "IdTipoDocumento", "Esquema", "Version", "IdEmpresa", "FechaLimite", "IdEstadoDocumento", "FechaCreacion", "FechaPresentacion", "DocumentoIdentidadDeclarante", "DocumentoIdentidadRepresentante", "DireccionElectronicaDeclaracion", "DireccionFisicaDeclaracion", "ComunaDeclaracion", "TelefonoDeclaracion", "FolioDeclaracion", "EnProceso", "FechaInicioUltimoProceso", "AnoMes")
VALUES(259, 'DECLACORR', 'DJ1839', '2020/04', 1, '2020-12-31', 'ACT', '2020-01-01 00:00:00.000', '2020-12-31 00:00:00.000', '96489000-5', '56362550-3', 'BLOCKCHAINUSR2@BCSBLOCK.COM', 'ADDQE 1234', 'SANTIAGO', '+56111222334', 'I9153103', 'N', '2022-02-07 00:47:45.517', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Documento"
("IdDocumento", "IdTipoDocumento", "Esquema", "Version", "IdEmpresa", "FechaLimite", "IdEstadoDocumento", "FechaCreacion", "FechaPresentacion", "DocumentoIdentidadDeclarante", "DocumentoIdentidadRepresentante", "DireccionElectronicaDeclaracion", "DireccionFisicaDeclaracion", "ComunaDeclaracion", "TelefonoDeclaracion", "FolioDeclaracion", "EnProceso", "FechaInicioUltimoProceso", "AnoMes")
VALUES(260, 'DECLACORR', 'DJ1839', '2020/05', 1, '2020-12-31', 'ACT', '2020-01-01 00:00:00.000', '2020-12-31 00:00:00.000', '96489000-5', '56362550-3', 'BLOCKCHAINUSR2@BCSBLOCK.COM', 'ADDQE 1234', 'SANTIAGO', '+56111222334', 'I9153103', 'N', '2022-02-07 00:47:45.517', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Documento"
("IdDocumento", "IdTipoDocumento", "Esquema", "Version", "IdEmpresa", "FechaLimite", "IdEstadoDocumento", "FechaCreacion", "FechaPresentacion", "DocumentoIdentidadDeclarante", "DocumentoIdentidadRepresentante", "DireccionElectronicaDeclaracion", "DireccionFisicaDeclaracion", "ComunaDeclaracion", "TelefonoDeclaracion", "FolioDeclaracion", "EnProceso", "FechaInicioUltimoProceso", "AnoMes")
VALUES(261, 'DECLACORR', 'DJ1839', '2020/06', 1, '2020-12-31', 'ACT', '2020-01-01 00:00:00.000', '2020-12-31 00:00:00.000', '96489000-5', '56362550-3', 'BLOCKCHAINUSR2@BCSBLOCK.COM', 'ADDQE 1234', 'SANTIAGO', '+56111222334', 'I9153103', 'N', '2022-02-07 00:47:45.517', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Documento"
("IdDocumento", "IdTipoDocumento", "Esquema", "Version", "IdEmpresa", "FechaLimite", "IdEstadoDocumento", "FechaCreacion", "FechaPresentacion", "DocumentoIdentidadDeclarante", "DocumentoIdentidadRepresentante", "DireccionElectronicaDeclaracion", "DireccionFisicaDeclaracion", "ComunaDeclaracion", "TelefonoDeclaracion", "FolioDeclaracion", "EnProceso", "FechaInicioUltimoProceso", "AnoMes")
VALUES(262, 'DECLACORR', 'DJ1839', '2020/07', 1, '2020-12-31', 'ACT', '2020-01-01 00:00:00.000', '2020-12-31 00:00:00.000', '96489000-5', '56362550-3', 'BLOCKCHAINUSR2@BCSBLOCK.COM', 'ADDQE 1234', 'SANTIAGO', '+56111222334', 'I9153103', 'N', '2022-02-07 00:47:45.517', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Documento"
("IdDocumento", "IdTipoDocumento", "Esquema", "Version", "IdEmpresa", "FechaLimite", "IdEstadoDocumento", "FechaCreacion", "FechaPresentacion", "DocumentoIdentidadDeclarante", "DocumentoIdentidadRepresentante", "DireccionElectronicaDeclaracion", "DireccionFisicaDeclaracion", "ComunaDeclaracion", "TelefonoDeclaracion", "FolioDeclaracion", "EnProceso", "FechaInicioUltimoProceso", "AnoMes")
VALUES(263, 'DECLACORR', 'DJ1839', '2020/08', 1, '2020-12-31', 'ACT', '2020-01-01 00:00:00.000', '2020-12-31 00:00:00.000', '96489000-5', '56362550-3', 'BLOCKCHAINUSR2@BCSBLOCK.COM', 'ADDQE 1234', 'SANTIAGO', '+56111222334', 'I9153103', 'N', '2022-02-07 00:47:45.517', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Documento"
("IdDocumento", "IdTipoDocumento", "Esquema", "Version", "IdEmpresa", "FechaLimite", "IdEstadoDocumento", "FechaCreacion", "FechaPresentacion", "DocumentoIdentidadDeclarante", "DocumentoIdentidadRepresentante", "DireccionElectronicaDeclaracion", "DireccionFisicaDeclaracion", "ComunaDeclaracion", "TelefonoDeclaracion", "FolioDeclaracion", "EnProceso", "FechaInicioUltimoProceso", "AnoMes")
VALUES(264, 'DECLACORR', 'DJ1839', '2020/09', 1, '2020-12-31', 'ACT', '2020-01-01 00:00:00.000', '2020-12-31 00:00:00.000', '96489000-5', '56362550-3', 'BLOCKCHAINUSR2@BCSBLOCK.COM', 'ADDQE 1234', 'SANTIAGO', '+56111222334', 'I9153103', 'N', '2022-02-07 00:47:45.517', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Documento"
("IdDocumento", "IdTipoDocumento", "Esquema", "Version", "IdEmpresa", "FechaLimite", "IdEstadoDocumento", "FechaCreacion", "FechaPresentacion", "DocumentoIdentidadDeclarante", "DocumentoIdentidadRepresentante", "DireccionElectronicaDeclaracion", "DireccionFisicaDeclaracion", "ComunaDeclaracion", "TelefonoDeclaracion", "FolioDeclaracion", "EnProceso", "FechaInicioUltimoProceso", "AnoMes")
VALUES(265, 'DECLACORR', 'DJ1839', '2020/10', 1, '2020-12-31', 'ACT', '2020-01-01 00:00:00.000', '2020-12-31 00:00:00.000', '96489000-5', '56362550-3', 'BLOCKCHAINUSR2@BCSBLOCK.COM', 'ADDQE 1234', 'SANTIAGO', '+56111222334', 'I9153103', 'N', '2022-02-07 00:47:45.517', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Documento"
("IdDocumento", "IdTipoDocumento", "Esquema", "Version", "IdEmpresa", "FechaLimite", "IdEstadoDocumento", "FechaCreacion", "FechaPresentacion", "DocumentoIdentidadDeclarante", "DocumentoIdentidadRepresentante", "DireccionElectronicaDeclaracion", "DireccionFisicaDeclaracion", "ComunaDeclaracion", "TelefonoDeclaracion", "FolioDeclaracion", "EnProceso", "FechaInicioUltimoProceso", "AnoMes")
VALUES(266, 'DECLACORR', 'DJ1839', '2020/11', 1, '2020-12-31', 'ACT', '2020-01-01 00:00:00.000', '2020-12-31 00:00:00.000', '96489000-5', '56362550-3', 'BLOCKCHAINUSR2@BCSBLOCK.COM', 'ADDQE 1234', 'SANTIAGO', '+56111222334', 'I9153103', 'N', '2022-02-07 00:47:45.517', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Documento"
("IdDocumento", "IdTipoDocumento", "Esquema", "Version", "IdEmpresa", "FechaLimite", "IdEstadoDocumento", "FechaCreacion", "FechaPresentacion", "DocumentoIdentidadDeclarante", "DocumentoIdentidadRepresentante", "DireccionElectronicaDeclaracion", "DireccionFisicaDeclaracion", "ComunaDeclaracion", "TelefonoDeclaracion", "FolioDeclaracion", "EnProceso", "FechaInicioUltimoProceso", "AnoMes")
VALUES(267, 'DECLACORR', 'DJ1839', '2020/12', 1, '2020-12-31', 'ACT', '2020-01-01 00:00:00.000', '2020-12-31 00:00:00.000', '96489000-5', '56362550-3', 'BLOCKCHAINUSR2@BCSBLOCK.COM', 'ADDQE 1234', 'SANTIAGO', '+56111222334', 'I9153103', 'N', '2022-02-07 00:47:45.517', '');


--CONTROL PROCESO
INSERT INTO BSCL."BSCL.Normativa.BD::TRI.ControlProceso"
("Esquema", "Version", "EstadoCertificado", "Carga", "Validacion", "PreDeclaracion", "Certificacion")
VALUES('DJ1839', '2020/01', 'NGE', 'S', 'N', 'N', 'N');

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.ControlProceso"
("Esquema", "Version", "EstadoCertificado", "Carga", "Validacion", "PreDeclaracion", "Certificacion")
VALUES('DJ1839', '2020/02', 'NGE', 'S', 'N', 'N', 'N');

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.ControlProceso"
("Esquema", "Version", "EstadoCertificado", "Carga", "Validacion", "PreDeclaracion", "Certificacion")
VALUES('DJ1839', '2020/03', 'NGE', 'S', 'N', 'N', 'N');

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.ControlProceso"
("Esquema", "Version", "EstadoCertificado", "Carga", "Validacion", "PreDeclaracion", "Certificacion")
VALUES('DJ1839', '2020/04', 'NGE', 'S', 'N', 'N', 'N');

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.ControlProceso"
("Esquema", "Version", "EstadoCertificado", "Carga", "Validacion", "PreDeclaracion", "Certificacion")
VALUES('DJ1839', '2020/05', 'NGE', 'S', 'N', 'N', 'N');

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.ControlProceso"
("Esquema", "Version", "EstadoCertificado", "Carga", "Validacion", "PreDeclaracion", "Certificacion")
VALUES('DJ1839', '2020/06', 'NGE', 'S', 'N', 'N', 'N');

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.ControlProceso"
("Esquema", "Version", "EstadoCertificado", "Carga", "Validacion", "PreDeclaracion", "Certificacion")
VALUES('DJ1839', '2020/07', 'NGE', 'S', 'N', 'N', 'N');

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.ControlProceso"
("Esquema", "Version", "EstadoCertificado", "Carga", "Validacion", "PreDeclaracion", "Certificacion")
VALUES('DJ1839', '2020/08', 'NGE', 'S', 'N', 'N', 'N');

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.ControlProceso"
("Esquema", "Version", "EstadoCertificado", "Carga", "Validacion", "PreDeclaracion", "Certificacion")
VALUES('DJ1839', '2020/09', 'NGE', 'S', 'N', 'N', 'N');

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.ControlProceso"
("Esquema", "Version", "EstadoCertificado", "Carga", "Validacion", "PreDeclaracion", "Certificacion")
VALUES('DJ1839', '2020/10', 'NGE', 'S', 'N', 'N', 'N');

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.ControlProceso"
("Esquema", "Version", "EstadoCertificado", "Carga", "Validacion", "PreDeclaracion", "Certificacion")
VALUES('DJ1839', '2020/11', 'NGE', 'S', 'N', 'N', 'N');

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.ControlProceso"
("Esquema", "Version", "EstadoCertificado", "Carga", "Validacion", "PreDeclaracion", "Certificacion")
VALUES('DJ1839', '2020/12', 'NGE', 'S', 'N', 'N', 'N');



--CAMPOS
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/01', 'ID', '', '', 1, 'Secuencia', 'Ayuda de Secuencia', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/01', 'Numero', 'C24', '', 2, 'Numero', 'Ayuda de Numero', 'ID', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos principales', 1, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/01', 'RutCliente', 'C8', '', 3, 'Rut Cliente', 'Ayuda de RutCliente', 'RUT_CLIENTE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos principales', 2, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/01', 'TaxID', 'C10', '', 4, 'Tax ID', 'Ayuda de Tax ID', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 3, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/01', 'RutClienteB', 'C11', '', 5, 'Rut Cliente B', 'Ayuda de RutCliente B', 'RUT_CLIENTE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 4, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/01', 'TaxIDB', 'C13', '', 6, 'Tax ID B', 'Ayuda de Tax ID', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 5, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/01', 'CodigoPaisA', 'C14', 'CodigoPais', 7, 'Codigo Pais A', 'Ayuda de Codigo Pais A', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 6, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/01', 'CodigoPaisB', 'C15', 'CodigoPais', 8, 'Codigo Pais B', 'Ayuda de Codigo Pais B', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 7, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/01', 'AcuerdoMarcoTipo', 'C16', '', 9, 'Tipo', 'Ayuda de Tipo', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 8, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/01', 'AcuerdoMarcoNumero', 'C17', '', 10, 'Numero', 'Ayuda de Numero', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 9, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/01', 'AcuerdoMarcoFechaSuscripcion', 'C18', '', 11, 'Fecha de suscripcion Marco', 'Ayuda de Fecha de suscripcion', 'DATE_FILTER', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 10, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/01', 'ContratoNumero', 'C19', '', 12, 'Numero / Identificador', 'Ayuda de Numero / Identificador', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Contrato / Confirmacion', 11, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/01', 'ContratoFechaSuscripcion', 'C20', '', 13, 'Fecha de suscripcion Contrato', 'Ayuda de Fecha de suscripcion', 'DATE_FILTER', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '3. Contrato / Confirmacion', 12, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/01', 'ContratoTipo', 'C21', '', 14, 'Tipo de Contrato', 'Ayuda de Tipo de Contrato', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '3. Contrato / Confirmacion', 13, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/01', 'ContratoNombreInstrumento', 'C22', '', 15, 'Nombre del Instrumento', 'Ayuda de Nombre del Instrumento', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Contrato / Confirmacion', 14, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/01', 'IdDocumento', '', '', 16, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/01', 'VboUsuario', '', '', 17, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, 'N', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/01', 'Observaciones', '', '', 18, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/01', 'FuenteDatos', '', '', 19, 'Fuente', 'Ayuda de Fuente', 'TEXT', 0, 'INICIAL', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/01', 'IdCliente', '', '', 20, 'Secuencia Cliente', 'Ayuda de Secuencia Cliente', 'ID_CLIENTE', 0, '0', 'S', 'S', 'N', 'N', 'N', 'N', 'N', 'N', '1. Datos principales', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/01', 'IdPortafolio', '', '', 21, 'Secuencia Portafolio', 'Ayuda de Secuencia Portafolio', 'ID_PORTAFOLIO', 0, '0', 'S', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '1. Datos principales', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/02', 'ID', '', '', 1, 'Secuencia', 'Ayuda de Secuencia', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/02', 'Numero', 'C24', '', 2, 'Numero', 'Ayuda de Numero', 'ID', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos principales', 1, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/02', 'RutCliente', 'C8', '', 3, 'Rut Cliente', 'Ayuda de RutCliente', 'RUT_CLIENTE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos principales', 2, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/02', 'TaxID', 'C10', '', 4, 'Tax ID', 'Ayuda de Tax ID', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 3, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/02', 'RutClienteB', 'C11', '', 5, 'Rut Cliente B', 'Ayuda de RutCliente B', 'RUT_CLIENTE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 4, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/02', 'TaxIDB', 'C13', '', 6, 'Tax ID B', 'Ayuda de Tax ID', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 5, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/02', 'CodigoPaisA', 'C14', 'CodigoPais', 7, 'Codigo Pais A', 'Ayuda de Codigo Pais A', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 6, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/02', 'CodigoPaisB', 'C15', 'CodigoPais', 8, 'Codigo Pais B', 'Ayuda de Codigo Pais B', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 7, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/02', 'AcuerdoMarcoTipo', 'C16', '', 9, 'Tipo', 'Ayuda de Tipo', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 8, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/02', 'AcuerdoMarcoNumero', 'C17', '', 10, 'Numero', 'Ayuda de Numero', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 9, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/02', 'AcuerdoMarcoFechaSuscripcion', 'C18', '', 11, 'Fecha de suscripcion Marco', 'Ayuda de Fecha de suscripcion', 'DATE_FILTER', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 10, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/02', 'ContratoNumero', 'C19', '', 12, 'Numero / Identificador', 'Ayuda de Numero / Identificador', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Contrato / Confirmacion', 11, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/02', 'ContratoFechaSuscripcion', 'C20', '', 13, 'Fecha de suscripcion Contrato', 'Ayuda de Fecha de suscripcion', 'DATE_FILTER', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '3. Contrato / Confirmacion', 12, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/02', 'ContratoTipo', 'C21', '', 14, 'Tipo de Contrato', 'Ayuda de Tipo de Contrato', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '3. Contrato / Confirmacion', 13, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/02', 'ContratoNombreInstrumento', 'C22', '', 15, 'Nombre del Instrumento', 'Ayuda de Nombre del Instrumento', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Contrato / Confirmacion', 14, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/02', 'IdDocumento', '', '', 16, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/02', 'VboUsuario', '', '', 17, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, 'N', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/02', 'Observaciones', '', '', 18, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/02', 'FuenteDatos', '', '', 19, 'Fuente', 'Ayuda de Fuente', 'TEXT', 0, 'INICIAL', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/02', 'IdCliente', '', '', 20, 'Secuencia Cliente', 'Ayuda de Secuencia Cliente', 'ID_CLIENTE', 0, '0', 'S', 'S', 'N', 'N', 'N', 'N', 'N', 'N', '1. Datos principales', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/02', 'IdPortafolio', '', '', 21, 'Secuencia Portafolio', 'Ayuda de Secuencia Portafolio', 'ID_PORTAFOLIO', 0, '0', 'S', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '1. Datos principales', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/03', 'ID', '', '', 1, 'Secuencia', 'Ayuda de Secuencia', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/03', 'Numero', 'C24', '', 2, 'Numero', 'Ayuda de Numero', 'ID', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos principales', 1, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/03', 'RutCliente', 'C8', '', 3, 'Rut Cliente', 'Ayuda de RutCliente', 'RUT_CLIENTE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos principales', 2, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/03', 'TaxID', 'C10', '', 4, 'Tax ID', 'Ayuda de Tax ID', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 3, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/03', 'RutClienteB', 'C11', '', 5, 'Rut Cliente B', 'Ayuda de RutCliente B', 'RUT_CLIENTE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 4, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/03', 'TaxIDB', 'C13', '', 6, 'Tax ID B', 'Ayuda de Tax ID', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 5, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/03', 'CodigoPaisA', 'C14', 'CodigoPais', 7, 'Codigo Pais A', 'Ayuda de Codigo Pais A', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 6, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/03', 'CodigoPaisB', 'C15', 'CodigoPais', 8, 'Codigo Pais B', 'Ayuda de Codigo Pais B', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 7, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/03', 'AcuerdoMarcoTipo', 'C16', '', 9, 'Tipo', 'Ayuda de Tipo', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 8, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/03', 'AcuerdoMarcoNumero', 'C17', '', 10, 'Numero', 'Ayuda de Numero', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 9, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/03', 'AcuerdoMarcoFechaSuscripcion', 'C18', '', 11, 'Fecha de suscripcion Marco', 'Ayuda de Fecha de suscripcion', 'DATE_FILTER', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 10, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/03', 'ContratoNumero', 'C19', '', 12, 'Numero / Identificador', 'Ayuda de Numero / Identificador', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Contrato / Confirmacion', 11, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/03', 'ContratoFechaSuscripcion', 'C20', '', 13, 'Fecha de suscripcion Contrato', 'Ayuda de Fecha de suscripcion', 'DATE_FILTER', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '3. Contrato / Confirmacion', 12, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/03', 'ContratoTipo', 'C21', '', 14, 'Tipo de Contrato', 'Ayuda de Tipo de Contrato', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '3. Contrato / Confirmacion', 13, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/03', 'ContratoNombreInstrumento', 'C22', '', 15, 'Nombre del Instrumento', 'Ayuda de Nombre del Instrumento', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Contrato / Confirmacion', 14, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/03', 'IdDocumento', '', '', 16, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/03', 'VboUsuario', '', '', 17, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, 'N', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/03', 'Observaciones', '', '', 18, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/03', 'FuenteDatos', '', '', 19, 'Fuente', 'Ayuda de Fuente', 'TEXT', 0, 'INICIAL', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/03', 'IdCliente', '', '', 20, 'Secuencia Cliente', 'Ayuda de Secuencia Cliente', 'ID_CLIENTE', 0, '0', 'S', 'S', 'N', 'N', 'N', 'N', 'N', 'N', '1. Datos principales', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/03', 'IdPortafolio', '', '', 21, 'Secuencia Portafolio', 'Ayuda de Secuencia Portafolio', 'ID_PORTAFOLIO', 0, '0', 'S', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '1. Datos principales', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/04', 'ID', '', '', 1, 'Secuencia', 'Ayuda de Secuencia', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/04', 'Numero', 'C24', '', 2, 'Numero', 'Ayuda de Numero', 'ID', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos principales', 1, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/04', 'RutCliente', 'C8', '', 3, 'Rut Cliente', 'Ayuda de RutCliente', 'RUT_CLIENTE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos principales', 2, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/04', 'TaxID', 'C10', '', 4, 'Tax ID', 'Ayuda de Tax ID', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 3, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/04', 'RutClienteB', 'C11', '', 5, 'Rut Cliente B', 'Ayuda de RutCliente B', 'RUT_CLIENTE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 4, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/04', 'TaxIDB', 'C13', '', 6, 'Tax ID B', 'Ayuda de Tax ID', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 5, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/04', 'CodigoPaisA', 'C14', 'CodigoPais', 7, 'Codigo Pais A', 'Ayuda de Codigo Pais A', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 6, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/04', 'CodigoPaisB', 'C15', 'CodigoPais', 8, 'Codigo Pais B', 'Ayuda de Codigo Pais B', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 7, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/04', 'AcuerdoMarcoTipo', 'C16', '', 9, 'Tipo', 'Ayuda de Tipo', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 8, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/04', 'AcuerdoMarcoNumero', 'C17', '', 10, 'Numero', 'Ayuda de Numero', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 9, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/04', 'AcuerdoMarcoFechaSuscripcion', 'C18', '', 11, 'Fecha de suscripcion Marco', 'Ayuda de Fecha de suscripcion', 'DATE_FILTER', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 10, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/04', 'ContratoNumero', 'C19', '', 12, 'Numero / Identificador', 'Ayuda de Numero / Identificador', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Contrato / Confirmacion', 11, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/04', 'ContratoFechaSuscripcion', 'C20', '', 13, 'Fecha de suscripcion Contrato', 'Ayuda de Fecha de suscripcion', 'DATE_FILTER', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '3. Contrato / Confirmacion', 12, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/04', 'ContratoTipo', 'C21', '', 14, 'Tipo de Contrato', 'Ayuda de Tipo de Contrato', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '3. Contrato / Confirmacion', 13, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/04', 'ContratoNombreInstrumento', 'C22', '', 15, 'Nombre del Instrumento', 'Ayuda de Nombre del Instrumento', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Contrato / Confirmacion', 14, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/04', 'IdDocumento', '', '', 16, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/04', 'VboUsuario', '', '', 17, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, 'N', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/04', 'Observaciones', '', '', 18, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/04', 'FuenteDatos', '', '', 19, 'Fuente', 'Ayuda de Fuente', 'TEXT', 0, 'INICIAL', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/04', 'IdCliente', '', '', 20, 'Secuencia Cliente', 'Ayuda de Secuencia Cliente', 'ID_CLIENTE', 0, '0', 'S', 'S', 'N', 'N', 'N', 'N', 'N', 'N', '1. Datos principales', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/04', 'IdPortafolio', '', '', 21, 'Secuencia Portafolio', 'Ayuda de Secuencia Portafolio', 'ID_PORTAFOLIO', 0, '0', 'S', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '1. Datos principales', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/05', 'ID', '', '', 1, 'Secuencia', 'Ayuda de Secuencia', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/05', 'Numero', 'C24', '', 2, 'Numero', 'Ayuda de Numero', 'ID', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos principales', 1, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/05', 'RutCliente', 'C8', '', 3, 'Rut Cliente', 'Ayuda de RutCliente', 'RUT_CLIENTE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos principales', 2, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/05', 'TaxID', 'C10', '', 4, 'Tax ID', 'Ayuda de Tax ID', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 3, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/05', 'RutClienteB', 'C11', '', 5, 'Rut Cliente B', 'Ayuda de RutCliente B', 'RUT_CLIENTE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 4, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/05', 'TaxIDB', 'C13', '', 6, 'Tax ID B', 'Ayuda de Tax ID', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 5, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/05', 'CodigoPaisA', 'C14', 'CodigoPais', 7, 'Codigo Pais A', 'Ayuda de Codigo Pais A', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 6, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/05', 'CodigoPaisB', 'C15', 'CodigoPais', 8, 'Codigo Pais B', 'Ayuda de Codigo Pais B', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 7, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/05', 'AcuerdoMarcoTipo', 'C16', '', 9, 'Tipo', 'Ayuda de Tipo', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 8, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/05', 'AcuerdoMarcoNumero', 'C17', '', 10, 'Numero', 'Ayuda de Numero', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 9, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/05', 'AcuerdoMarcoFechaSuscripcion', 'C18', '', 11, 'Fecha de suscripcion Marco', 'Ayuda de Fecha de suscripcion', 'DATE_FILTER', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 10, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/05', 'ContratoNumero', 'C19', '', 12, 'Numero / Identificador', 'Ayuda de Numero / Identificador', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Contrato / Confirmacion', 11, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/05', 'ContratoFechaSuscripcion', 'C20', '', 13, 'Fecha de suscripcion Contrato', 'Ayuda de Fecha de suscripcion', 'DATE_FILTER', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '3. Contrato / Confirmacion', 12, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/05', 'ContratoTipo', 'C21', '', 14, 'Tipo de Contrato', 'Ayuda de Tipo de Contrato', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '3. Contrato / Confirmacion', 13, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/05', 'ContratoNombreInstrumento', 'C22', '', 15, 'Nombre del Instrumento', 'Ayuda de Nombre del Instrumento', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Contrato / Confirmacion', 14, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/05', 'IdDocumento', '', '', 16, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/05', 'VboUsuario', '', '', 17, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, 'N', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/05', 'Observaciones', '', '', 18, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/05', 'FuenteDatos', '', '', 19, 'Fuente', 'Ayuda de Fuente', 'TEXT', 0, 'INICIAL', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/05', 'IdCliente', '', '', 20, 'Secuencia Cliente', 'Ayuda de Secuencia Cliente', 'ID_CLIENTE', 0, '0', 'S', 'S', 'N', 'N', 'N', 'N', 'N', 'N', '1. Datos principales', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/05', 'IdPortafolio', '', '', 21, 'Secuencia Portafolio', 'Ayuda de Secuencia Portafolio', 'ID_PORTAFOLIO', 0, '0', 'S', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '1. Datos principales', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/06', 'ID', '', '', 1, 'Secuencia', 'Ayuda de Secuencia', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/06', 'Numero', 'C24', '', 2, 'Numero', 'Ayuda de Numero', 'ID', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos principales', 1, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/06', 'RutCliente', 'C8', '', 3, 'Rut Cliente', 'Ayuda de RutCliente', 'RUT_CLIENTE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos principales', 2, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/06', 'TaxID', 'C10', '', 4, 'Tax ID', 'Ayuda de Tax ID', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 3, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/06', 'RutClienteB', 'C11', '', 5, 'Rut Cliente B', 'Ayuda de RutCliente B', 'RUT_CLIENTE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 4, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/06', 'TaxIDB', 'C13', '', 6, 'Tax ID B', 'Ayuda de Tax ID', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 5, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/06', 'CodigoPaisA', 'C14', 'CodigoPais', 7, 'Codigo Pais A', 'Ayuda de Codigo Pais A', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 6, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/06', 'CodigoPaisB', 'C15', 'CodigoPais', 8, 'Codigo Pais B', 'Ayuda de Codigo Pais B', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 7, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/06', 'AcuerdoMarcoTipo', 'C16', '', 9, 'Tipo', 'Ayuda de Tipo', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 8, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/06', 'AcuerdoMarcoNumero', 'C17', '', 10, 'Numero', 'Ayuda de Numero', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 9, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/06', 'AcuerdoMarcoFechaSuscripcion', 'C18', '', 11, 'Fecha de suscripcion Marco', 'Ayuda de Fecha de suscripcion', 'DATE_FILTER', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 10, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/06', 'ContratoNumero', 'C19', '', 12, 'Numero / Identificador', 'Ayuda de Numero / Identificador', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Contrato / Confirmacion', 11, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/06', 'ContratoFechaSuscripcion', 'C20', '', 13, 'Fecha de suscripcion Contrato', 'Ayuda de Fecha de suscripcion', 'DATE_FILTER', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '3. Contrato / Confirmacion', 12, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/06', 'ContratoTipo', 'C21', '', 14, 'Tipo de Contrato', 'Ayuda de Tipo de Contrato', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '3. Contrato / Confirmacion', 13, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/06', 'ContratoNombreInstrumento', 'C22', '', 15, 'Nombre del Instrumento', 'Ayuda de Nombre del Instrumento', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Contrato / Confirmacion', 14, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/06', 'IdDocumento', '', '', 16, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/06', 'VboUsuario', '', '', 17, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, 'N', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/06', 'Observaciones', '', '', 18, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/06', 'FuenteDatos', '', '', 19, 'Fuente', 'Ayuda de Fuente', 'TEXT', 0, 'INICIAL', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/06', 'IdCliente', '', '', 20, 'Secuencia Cliente', 'Ayuda de Secuencia Cliente', 'ID_CLIENTE', 0, '0', 'S', 'S', 'N', 'N', 'N', 'N', 'N', 'N', '1. Datos principales', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/06', 'IdPortafolio', '', '', 21, 'Secuencia Portafolio', 'Ayuda de Secuencia Portafolio', 'ID_PORTAFOLIO', 0, '0', 'S', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '1. Datos principales', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/07', 'ID', '', '', 1, 'Secuencia', 'Ayuda de Secuencia', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/07', 'Numero', 'C24', '', 2, 'Numero', 'Ayuda de Numero', 'ID', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos principales', 1, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/07', 'RutCliente', 'C8', '', 3, 'Rut Cliente', 'Ayuda de RutCliente', 'RUT_CLIENTE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos principales', 2, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/07', 'TaxID', 'C10', '', 4, 'Tax ID', 'Ayuda de Tax ID', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 3, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/07', 'RutClienteB', 'C11', '', 5, 'Rut Cliente B', 'Ayuda de RutCliente B', 'RUT_CLIENTE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 4, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/07', 'TaxIDB', 'C13', '', 6, 'Tax ID B', 'Ayuda de Tax ID', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 5, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/07', 'CodigoPaisA', 'C14', 'CodigoPais', 7, 'Codigo Pais A', 'Ayuda de Codigo Pais A', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 6, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/07', 'CodigoPaisB', 'C15', 'CodigoPais', 8, 'Codigo Pais B', 'Ayuda de Codigo Pais B', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 7, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/07', 'AcuerdoMarcoTipo', 'C16', '', 9, 'Tipo', 'Ayuda de Tipo', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 8, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/07', 'AcuerdoMarcoNumero', 'C17', '', 10, 'Numero', 'Ayuda de Numero', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 9, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/07', 'AcuerdoMarcoFechaSuscripcion', 'C18', '', 11, 'Fecha de suscripcion Marco', 'Ayuda de Fecha de suscripcion', 'DATE_FILTER', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 10, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/07', 'ContratoNumero', 'C19', '', 12, 'Numero / Identificador', 'Ayuda de Numero / Identificador', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Contrato / Confirmacion', 11, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/07', 'ContratoFechaSuscripcion', 'C20', '', 13, 'Fecha de suscripcion Contrato', 'Ayuda de Fecha de suscripcion', 'DATE_FILTER', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '3. Contrato / Confirmacion', 12, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/07', 'ContratoTipo', 'C21', '', 14, 'Tipo de Contrato', 'Ayuda de Tipo de Contrato', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '3. Contrato / Confirmacion', 13, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/07', 'ContratoNombreInstrumento', 'C22', '', 15, 'Nombre del Instrumento', 'Ayuda de Nombre del Instrumento', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Contrato / Confirmacion', 14, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/07', 'IdDocumento', '', '', 16, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/07', 'VboUsuario', '', '', 17, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, 'N', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/07', 'Observaciones', '', '', 18, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/07', 'FuenteDatos', '', '', 19, 'Fuente', 'Ayuda de Fuente', 'TEXT', 0, 'INICIAL', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/07', 'IdCliente', '', '', 20, 'Secuencia Cliente', 'Ayuda de Secuencia Cliente', 'ID_CLIENTE', 0, '0', 'S', 'S', 'N', 'N', 'N', 'N', 'N', 'N', '1. Datos principales', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/07', 'IdPortafolio', '', '', 21, 'Secuencia Portafolio', 'Ayuda de Secuencia Portafolio', 'ID_PORTAFOLIO', 0, '0', 'S', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '1. Datos principales', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/08', 'ID', '', '', 1, 'Secuencia', 'Ayuda de Secuencia', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/08', 'Numero', 'C24', '', 2, 'Numero', 'Ayuda de Numero', 'ID', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos principales', 1, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/08', 'RutCliente', 'C8', '', 3, 'Rut Cliente', 'Ayuda de RutCliente', 'RUT_CLIENTE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos principales', 2, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/08', 'TaxID', 'C10', '', 4, 'Tax ID', 'Ayuda de Tax ID', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 3, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/08', 'RutClienteB', 'C11', '', 5, 'Rut Cliente B', 'Ayuda de RutCliente B', 'RUT_CLIENTE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 4, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/08', 'TaxIDB', 'C13', '', 6, 'Tax ID B', 'Ayuda de Tax ID', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 5, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/08', 'CodigoPaisA', 'C14', 'CodigoPais', 7, 'Codigo Pais A', 'Ayuda de Codigo Pais A', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 6, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/08', 'CodigoPaisB', 'C15', 'CodigoPais', 8, 'Codigo Pais B', 'Ayuda de Codigo Pais B', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 7, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/08', 'AcuerdoMarcoTipo', 'C16', '', 9, 'Tipo', 'Ayuda de Tipo', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 8, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/08', 'AcuerdoMarcoNumero', 'C17', '', 10, 'Numero', 'Ayuda de Numero', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 9, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/08', 'AcuerdoMarcoFechaSuscripcion', 'C18', '', 11, 'Fecha de suscripcion Marco', 'Ayuda de Fecha de suscripcion', 'DATE_FILTER', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 10, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/08', 'ContratoNumero', 'C19', '', 12, 'Numero / Identificador', 'Ayuda de Numero / Identificador', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Contrato / Confirmacion', 11, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/08', 'ContratoFechaSuscripcion', 'C20', '', 13, 'Fecha de suscripcion Contrato', 'Ayuda de Fecha de suscripcion', 'DATE_FILTER', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '3. Contrato / Confirmacion', 12, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/08', 'ContratoTipo', 'C21', '', 14, 'Tipo de Contrato', 'Ayuda de Tipo de Contrato', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '3. Contrato / Confirmacion', 13, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/08', 'ContratoNombreInstrumento', 'C22', '', 15, 'Nombre del Instrumento', 'Ayuda de Nombre del Instrumento', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Contrato / Confirmacion', 14, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/08', 'IdDocumento', '', '', 16, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/08', 'VboUsuario', '', '', 17, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, 'N', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/08', 'Observaciones', '', '', 18, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/08', 'FuenteDatos', '', '', 19, 'Fuente', 'Ayuda de Fuente', 'TEXT', 0, 'INICIAL', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/08', 'IdCliente', '', '', 20, 'Secuencia Cliente', 'Ayuda de Secuencia Cliente', 'ID_CLIENTE', 0, '0', 'S', 'S', 'N', 'N', 'N', 'N', 'N', 'N', '1. Datos principales', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/08', 'IdPortafolio', '', '', 21, 'Secuencia Portafolio', 'Ayuda de Secuencia Portafolio', 'ID_PORTAFOLIO', 0, '0', 'S', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '1. Datos principales', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/09', 'ID', '', '', 1, 'Secuencia', 'Ayuda de Secuencia', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/09', 'Numero', 'C24', '', 2, 'Numero', 'Ayuda de Numero', 'ID', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos principales', 1, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/09', 'RutCliente', 'C8', '', 3, 'Rut Cliente', 'Ayuda de RutCliente', 'RUT_CLIENTE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos principales', 2, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/09', 'TaxID', 'C10', '', 4, 'Tax ID', 'Ayuda de Tax ID', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 3, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/09', 'RutClienteB', 'C11', '', 5, 'Rut Cliente B', 'Ayuda de RutCliente B', 'RUT_CLIENTE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 4, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/09', 'TaxIDB', 'C13', '', 6, 'Tax ID B', 'Ayuda de Tax ID', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 5, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/09', 'CodigoPaisA', 'C14', 'CodigoPais', 7, 'Codigo Pais A', 'Ayuda de Codigo Pais A', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 6, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/09', 'CodigoPaisB', 'C15', 'CodigoPais', 8, 'Codigo Pais B', 'Ayuda de Codigo Pais B', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 7, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/09', 'AcuerdoMarcoTipo', 'C16', '', 9, 'Tipo', 'Ayuda de Tipo', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 8, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/09', 'AcuerdoMarcoNumero', 'C17', '', 10, 'Numero', 'Ayuda de Numero', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 9, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/09', 'AcuerdoMarcoFechaSuscripcion', 'C18', '', 11, 'Fecha de suscripcion Marco', 'Ayuda de Fecha de suscripcion', 'DATE_FILTER', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 10, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/09', 'ContratoNumero', 'C19', '', 12, 'Numero / Identificador', 'Ayuda de Numero / Identificador', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Contrato / Confirmacion', 11, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/09', 'ContratoFechaSuscripcion', 'C20', '', 13, 'Fecha de suscripcion Contrato', 'Ayuda de Fecha de suscripcion', 'DATE_FILTER', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '3. Contrato / Confirmacion', 12, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/09', 'ContratoTipo', 'C21', '', 14, 'Tipo de Contrato', 'Ayuda de Tipo de Contrato', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '3. Contrato / Confirmacion', 13, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/09', 'ContratoNombreInstrumento', 'C22', '', 15, 'Nombre del Instrumento', 'Ayuda de Nombre del Instrumento', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Contrato / Confirmacion', 14, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/09', 'IdDocumento', '', '', 16, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/09', 'VboUsuario', '', '', 17, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, 'N', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/09', 'Observaciones', '', '', 18, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/09', 'FuenteDatos', '', '', 19, 'Fuente', 'Ayuda de Fuente', 'TEXT', 0, 'INICIAL', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/09', 'IdCliente', '', '', 20, 'Secuencia Cliente', 'Ayuda de Secuencia Cliente', 'ID_CLIENTE', 0, '0', 'S', 'S', 'N', 'N', 'N', 'N', 'N', 'N', '1. Datos principales', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/09', 'IdPortafolio', '', '', 21, 'Secuencia Portafolio', 'Ayuda de Secuencia Portafolio', 'ID_PORTAFOLIO', 0, '0', 'S', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '1. Datos principales', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/10', 'ID', '', '', 1, 'Secuencia', 'Ayuda de Secuencia', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/10', 'Numero', 'C24', '', 2, 'Numero', 'Ayuda de Numero', 'ID', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos principales', 1, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/10', 'RutCliente', 'C8', '', 3, 'Rut Cliente', 'Ayuda de RutCliente', 'RUT_CLIENTE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos principales', 2, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/10', 'TaxID', 'C10', '', 4, 'Tax ID', 'Ayuda de Tax ID', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 3, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/10', 'RutClienteB', 'C11', '', 5, 'Rut Cliente B', 'Ayuda de RutCliente B', 'RUT_CLIENTE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 4, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/10', 'TaxIDB', 'C13', '', 6, 'Tax ID B', 'Ayuda de Tax ID', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 5, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/10', 'CodigoPaisA', 'C14', 'CodigoPais', 7, 'Codigo Pais A', 'Ayuda de Codigo Pais A', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 6, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/10', 'CodigoPaisB', 'C15', 'CodigoPais', 8, 'Codigo Pais B', 'Ayuda de Codigo Pais B', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 7, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/10', 'AcuerdoMarcoTipo', 'C16', '', 9, 'Tipo', 'Ayuda de Tipo', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 8, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/10', 'AcuerdoMarcoNumero', 'C17', '', 10, 'Numero', 'Ayuda de Numero', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 9, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/10', 'AcuerdoMarcoFechaSuscripcion', 'C18', '', 11, 'Fecha de suscripcion Marco', 'Ayuda de Fecha de suscripcion', 'DATE_FILTER', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 10, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/10', 'ContratoNumero', 'C19', '', 12, 'Numero / Identificador', 'Ayuda de Numero / Identificador', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Contrato / Confirmacion', 11, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/10', 'ContratoFechaSuscripcion', 'C20', '', 13, 'Fecha de suscripcion Contrato', 'Ayuda de Fecha de suscripcion', 'DATE_FILTER', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '3. Contrato / Confirmacion', 12, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/10', 'ContratoTipo', 'C21', '', 14, 'Tipo de Contrato', 'Ayuda de Tipo de Contrato', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '3. Contrato / Confirmacion', 13, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/10', 'ContratoNombreInstrumento', 'C22', '', 15, 'Nombre del Instrumento', 'Ayuda de Nombre del Instrumento', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Contrato / Confirmacion', 14, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/10', 'IdDocumento', '', '', 16, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/10', 'VboUsuario', '', '', 17, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, 'N', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/10', 'Observaciones', '', '', 18, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/10', 'FuenteDatos', '', '', 19, 'Fuente', 'Ayuda de Fuente', 'TEXT', 0, 'INICIAL', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/10', 'IdCliente', '', '', 20, 'Secuencia Cliente', 'Ayuda de Secuencia Cliente', 'ID_CLIENTE', 0, '0', 'S', 'S', 'N', 'N', 'N', 'N', 'N', 'N', '1. Datos principales', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/10', 'IdPortafolio', '', '', 21, 'Secuencia Portafolio', 'Ayuda de Secuencia Portafolio', 'ID_PORTAFOLIO', 0, '0', 'S', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '1. Datos principales', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/11', 'ID', '', '', 1, 'Secuencia', 'Ayuda de Secuencia', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/11', 'Numero', 'C24', '', 2, 'Numero', 'Ayuda de Numero', 'ID', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos principales', 1, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/11', 'RutCliente', 'C8', '', 3, 'Rut Cliente', 'Ayuda de RutCliente', 'RUT_CLIENTE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos principales', 2, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/11', 'TaxID', 'C10', '', 4, 'Tax ID', 'Ayuda de Tax ID', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 3, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/11', 'RutClienteB', 'C11', '', 5, 'Rut Cliente B', 'Ayuda de RutCliente B', 'RUT_CLIENTE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 4, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/11', 'TaxIDB', 'C13', '', 6, 'Tax ID B', 'Ayuda de Tax ID', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 5, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/11', 'CodigoPaisA', 'C14', 'CodigoPais', 7, 'Codigo Pais A', 'Ayuda de Codigo Pais A', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 6, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/11', 'CodigoPaisB', 'C15', 'CodigoPais', 8, 'Codigo Pais B', 'Ayuda de Codigo Pais B', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 7, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/11', 'AcuerdoMarcoTipo', 'C16', '', 9, 'Tipo', 'Ayuda de Tipo', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 8, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/11', 'AcuerdoMarcoNumero', 'C17', '', 10, 'Numero', 'Ayuda de Numero', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 9, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/11', 'AcuerdoMarcoFechaSuscripcion', 'C18', '', 11, 'Fecha de suscripcion Marco', 'Ayuda de Fecha de suscripcion', 'DATE_FILTER', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 10, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/11', 'ContratoNumero', 'C19', '', 12, 'Numero / Identificador', 'Ayuda de Numero / Identificador', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Contrato / Confirmacion', 11, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/11', 'ContratoFechaSuscripcion', 'C20', '', 13, 'Fecha de suscripcion Contrato', 'Ayuda de Fecha de suscripcion', 'DATE_FILTER', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '3. Contrato / Confirmacion', 12, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/11', 'ContratoTipo', 'C21', '', 14, 'Tipo de Contrato', 'Ayuda de Tipo de Contrato', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '3. Contrato / Confirmacion', 13, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/11', 'ContratoNombreInstrumento', 'C22', '', 15, 'Nombre del Instrumento', 'Ayuda de Nombre del Instrumento', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Contrato / Confirmacion', 14, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/11', 'IdDocumento', '', '', 16, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/11', 'VboUsuario', '', '', 17, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, 'N', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/11', 'Observaciones', '', '', 18, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/11', 'FuenteDatos', '', '', 19, 'Fuente', 'Ayuda de Fuente', 'TEXT', 0, 'INICIAL', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/11', 'IdCliente', '', '', 20, 'Secuencia Cliente', 'Ayuda de Secuencia Cliente', 'ID_CLIENTE', 0, '0', 'S', 'S', 'N', 'N', 'N', 'N', 'N', 'N', '1. Datos principales', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/11', 'IdPortafolio', '', '', 21, 'Secuencia Portafolio', 'Ayuda de Secuencia Portafolio', 'ID_PORTAFOLIO', 0, '0', 'S', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '1. Datos principales', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/12', 'ID', '', '', 1, 'Secuencia', 'Ayuda de Secuencia', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/12', 'Numero', 'C24', '', 2, 'Numero', 'Ayuda de Numero', 'ID', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos principales', 1, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/12', 'RutCliente', 'C8', '', 3, 'Rut Cliente', 'Ayuda de RutCliente', 'RUT_CLIENTE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos principales', 2, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/12', 'TaxID', 'C10', '', 4, 'Tax ID', 'Ayuda de Tax ID', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 3, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/12', 'RutClienteB', 'C11', '', 5, 'Rut Cliente B', 'Ayuda de RutCliente B', 'RUT_CLIENTE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 4, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/12', 'TaxIDB', 'C13', '', 6, 'Tax ID B', 'Ayuda de Tax ID', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 5, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/12', 'CodigoPaisA', 'C14', 'CodigoPais', 7, 'Codigo Pais A', 'Ayuda de Codigo Pais A', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 6, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/12', 'CodigoPaisB', 'C15', 'CodigoPais', 8, 'Codigo Pais B', 'Ayuda de Codigo Pais B', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos principales', 7, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/12', 'AcuerdoMarcoTipo', 'C16', '', 9, 'Tipo', 'Ayuda de Tipo', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 8, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/12', 'AcuerdoMarcoNumero', 'C17', '', 10, 'Numero', 'Ayuda de Numero', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 9, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/12', 'AcuerdoMarcoFechaSuscripcion', 'C18', '', 11, 'Fecha de suscripcion Marco', 'Ayuda de Fecha de suscripcion', 'DATE_FILTER', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Acuerdo marco', 10, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/12', 'ContratoNumero', 'C19', '', 12, 'Numero / Identificador', 'Ayuda de Numero / Identificador', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Contrato / Confirmacion', 11, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/12', 'ContratoFechaSuscripcion', 'C20', '', 13, 'Fecha de suscripcion Contrato', 'Ayuda de Fecha de suscripcion', 'DATE_FILTER', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '3. Contrato / Confirmacion', 12, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/12', 'ContratoTipo', 'C21', '', 14, 'Tipo de Contrato', 'Ayuda de Tipo de Contrato', 'QTY', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '3. Contrato / Confirmacion', 13, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/12', 'ContratoNombreInstrumento', 'C22', '', 15, 'Nombre del Instrumento', 'Ayuda de Nombre del Instrumento', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Contrato / Confirmacion', 14, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/12', 'IdDocumento', '', '', 16, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/12', 'VboUsuario', '', '', 17, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, 'N', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/12', 'Observaciones', '', '', 18, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/12', 'FuenteDatos', '', '', 19, 'Fuente', 'Ayuda de Fuente', 'TEXT', 0, 'INICIAL', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '4. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/12', 'IdCliente', '', '', 20, 'Secuencia Cliente', 'Ayuda de Secuencia Cliente', 'ID_CLIENTE', 0, '0', 'S', 'S', 'N', 'N', 'N', 'N', 'N', 'N', '1. Datos principales', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1839', '2020/12', 'IdPortafolio', '', '', 21, 'Secuencia Portafolio', 'Ayuda de Secuencia Portafolio', 'ID_PORTAFOLIO', 0, '0', 'S', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '1. Datos principales', 0, 0);



--VALIDACIONES
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/01', 'H1509', 'NORMA', 'N', 'La fecha de suscripcion del contrato/confirmacion (C20) debe ser anterior o igual al 31 de Diciembre del anio tributario', 'E', 'C20 31/12/AT');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/01', 'H1510', 'NORMA', 'S', 'Si no existe RutParte A (C8), debe existir tax ID Parte A (C10) , es decir si C8 es igual a cero entonces C10 debe ser distinto de cero', 'E', 'Si C8 = 0 => C10<>0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/01', 'H1511', 'NORMA', 'S', 'Si no existe RutParte B (C11), debe existir tax ID Parte B (C13) es decir si C11  es igual a cero entonces C1 debe ser distinto de cero', 'E', 'Si C11 = 0 => C13 <> 0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/01', 'H1512', 'NORMA', 'S', 'Debe existir al menos un valor mayor que cero en las columnas (C8) y (C10) ', 'E', 'C8 >0 .o.  C10 >0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/01', 'H1513', 'NORMA', 'S', 'Debe existir al menos un valor mayor que cero en las columnas (C11) y (C13)', 'E', 'C11 >0 .o.  C13 <> blanco o vacio');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/01', 'H1507', 'NORMA', 'S', 'La columna tipo de contrato (C21) debe indicar valores segun tabla tipo de contrato (TC) ', 'E', 'Si existe C21 => C21= {1 .o. 2 .o. 3 .o. 4 .o. 5 .o. 6 .o. 7 .o. 8.o. 9 .o.10.o.11.o.12.o.13.o.14}');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/01', 'H1514', 'NORMA', 'S', 'Si tipo de contrato (C21) es igual a opcion otros {12,13,14}, debe existir nombre del instrumento (C22)', 'E', 'Si C21= {12 .o. 13 .o. 14 }=> C22<> Blanco .o. vacio');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/01', 'H1515', 'NORMA', 'S', 'La fecha de suscripcion del acuerdo marco (C18) debe ser anterior o igual a la fecha del contrato de confirmacion. (C20)', 'E', 'C18  C20');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/02', 'H1509', 'NORMA', 'N', 'La fecha de suscripcion del contrato/confirmacion (C20) debe ser anterior o igual al 31 de Diciembre del anio tributario', 'E', 'C20 31/12/AT');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/02', 'H1510', 'NORMA', 'S', 'Si no existe RutParte A (C8), debe existir tax ID Parte A (C10) , es decir si C8 es igual a cero entonces C10 debe ser distinto de cero', 'E', 'Si C8 = 0 => C10<>0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/02', 'H1511', 'NORMA', 'S', 'Si no existe RutParte B (C11), debe existir tax ID Parte B (C13) es decir si C11  es igual a cero entonces C1 debe ser distinto de cero', 'E', 'Si C11 = 0 => C13 <> 0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/02', 'H1512', 'NORMA', 'S', 'Debe existir al menos un valor mayor que cero en las columnas (C8) y (C10) ', 'E', 'C8 >0 .o.  C10 >0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/02', 'H1513', 'NORMA', 'S', 'Debe existir al menos un valor mayor que cero en las columnas (C11) y (C13)', 'E', 'C11 >0 .o.  C13 <> blanco o vacio');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/02', 'H1507', 'NORMA', 'S', 'La columna tipo de contrato (C21) debe indicar valores segun tabla tipo de contrato (TC) ', 'E', 'Si existe C21 => C21= {1 .o. 2 .o. 3 .o. 4 .o. 5 .o. 6 .o. 7 .o. 8.o. 9 .o.10.o.11.o.12.o.13.o.14}');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/02', 'H1514', 'NORMA', 'S', 'Si tipo de contrato (C21) es igual a opcion otros {12,13,14}, debe existir nombre del instrumento (C22)', 'E', 'Si C21= {12 .o. 13 .o. 14 }=> C22<> Blanco .o. vacio');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/02', 'H1515', 'NORMA', 'S', 'La fecha de suscripcion del acuerdo marco (C18) debe ser anterior o igual a la fecha del contrato de confirmacion. (C20)', 'E', 'C18  C20');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/03', 'H1509', 'NORMA', 'N', 'La fecha de suscripcion del contrato/confirmacion (C20) debe ser anterior o igual al 31 de Diciembre del anio tributario', 'E', 'C20 31/12/AT');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/03', 'H1510', 'NORMA', 'S', 'Si no existe RutParte A (C8), debe existir tax ID Parte A (C10) , es decir si C8 es igual a cero entonces C10 debe ser distinto de cero', 'E', 'Si C8 = 0 => C10<>0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/03', 'H1511', 'NORMA', 'S', 'Si no existe RutParte B (C11), debe existir tax ID Parte B (C13) es decir si C11  es igual a cero entonces C1 debe ser distinto de cero', 'E', 'Si C11 = 0 => C13 <> 0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/03', 'H1512', 'NORMA', 'S', 'Debe existir al menos un valor mayor que cero en las columnas (C8) y (C10) ', 'E', 'C8 >0 .o.  C10 >0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/03', 'H1513', 'NORMA', 'S', 'Debe existir al menos un valor mayor que cero en las columnas (C11) y (C13)', 'E', 'C11 >0 .o.  C13 <> blanco o vacio');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/03', 'H1507', 'NORMA', 'S', 'La columna tipo de contrato (C21) debe indicar valores segun tabla tipo de contrato (TC) ', 'E', 'Si existe C21 => C21= {1 .o. 2 .o. 3 .o. 4 .o. 5 .o. 6 .o. 7 .o. 8.o. 9 .o.10.o.11.o.12.o.13.o.14}');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/03', 'H1514', 'NORMA', 'S', 'Si tipo de contrato (C21) es igual a opcion otros {12,13,14}, debe existir nombre del instrumento (C22)', 'E', 'Si C21= {12 .o. 13 .o. 14 }=> C22<> Blanco .o. vacio');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/03', 'H1515', 'NORMA', 'S', 'La fecha de suscripcion del acuerdo marco (C18) debe ser anterior o igual a la fecha del contrato de confirmacion. (C20)', 'E', 'C18  C20');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/04', 'H1509', 'NORMA', 'N', 'La fecha de suscripcion del contrato/confirmacion (C20) debe ser anterior o igual al 31 de Diciembre del anio tributario', 'E', 'C20 31/12/AT');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/04', 'H1510', 'NORMA', 'S', 'Si no existe RutParte A (C8), debe existir tax ID Parte A (C10) , es decir si C8 es igual a cero entonces C10 debe ser distinto de cero', 'E', 'Si C8 = 0 => C10<>0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/04', 'H1511', 'NORMA', 'S', 'Si no existe RutParte B (C11), debe existir tax ID Parte B (C13) es decir si C11  es igual a cero entonces C1 debe ser distinto de cero', 'E', 'Si C11 = 0 => C13 <> 0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/04', 'H1512', 'NORMA', 'S', 'Debe existir al menos un valor mayor que cero en las columnas (C8) y (C10) ', 'E', 'C8 >0 .o.  C10 >0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/04', 'H1513', 'NORMA', 'S', 'Debe existir al menos un valor mayor que cero en las columnas (C11) y (C13)', 'E', 'C11 >0 .o.  C13 <> blanco o vacio');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/04', 'H1507', 'NORMA', 'S', 'La columna tipo de contrato (C21) debe indicar valores segun tabla tipo de contrato (TC) ', 'E', 'Si existe C21 => C21= {1 .o. 2 .o. 3 .o. 4 .o. 5 .o. 6 .o. 7 .o. 8.o. 9 .o.10.o.11.o.12.o.13.o.14}');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/04', 'H1514', 'NORMA', 'S', 'Si tipo de contrato (C21) es igual a opcion otros {12,13,14}, debe existir nombre del instrumento (C22)', 'E', 'Si C21= {12 .o. 13 .o. 14 }=> C22<> Blanco .o. vacio');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/04', 'H1515', 'NORMA', 'S', 'La fecha de suscripcion del acuerdo marco (C18) debe ser anterior o igual a la fecha del contrato de confirmacion. (C20)', 'E', 'C18  C20');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/05', 'H1509', 'NORMA', 'N', 'La fecha de suscripcion del contrato/confirmacion (C20) debe ser anterior o igual al 31 de Diciembre del anio tributario', 'E', 'C20 31/12/AT');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/05', 'H1510', 'NORMA', 'S', 'Si no existe RutParte A (C8), debe existir tax ID Parte A (C10) , es decir si C8 es igual a cero entonces C10 debe ser distinto de cero', 'E', 'Si C8 = 0 => C10<>0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/05', 'H1511', 'NORMA', 'S', 'Si no existe RutParte B (C11), debe existir tax ID Parte B (C13) es decir si C11  es igual a cero entonces C1 debe ser distinto de cero', 'E', 'Si C11 = 0 => C13 <> 0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/05', 'H1512', 'NORMA', 'S', 'Debe existir al menos un valor mayor que cero en las columnas (C8) y (C10) ', 'E', 'C8 >0 .o.  C10 >0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/05', 'H1513', 'NORMA', 'S', 'Debe existir al menos un valor mayor que cero en las columnas (C11) y (C13)', 'E', 'C11 >0 .o.  C13 <> blanco o vacio');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/05', 'H1507', 'NORMA', 'S', 'La columna tipo de contrato (C21) debe indicar valores segun tabla tipo de contrato (TC) ', 'E', 'Si existe C21 => C21= {1 .o. 2 .o. 3 .o. 4 .o. 5 .o. 6 .o. 7 .o. 8.o. 9 .o.10.o.11.o.12.o.13.o.14}');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/05', 'H1514', 'NORMA', 'S', 'Si tipo de contrato (C21) es igual a opcion otros {12,13,14}, debe existir nombre del instrumento (C22)', 'E', 'Si C21= {12 .o. 13 .o. 14 }=> C22<> Blanco .o. vacio');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/05', 'H1515', 'NORMA', 'S', 'La fecha de suscripcion del acuerdo marco (C18) debe ser anterior o igual a la fecha del contrato de confirmacion. (C20)', 'E', 'C18  C20');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/06', 'H1509', 'NORMA', 'N', 'La fecha de suscripcion del contrato/confirmacion (C20) debe ser anterior o igual al 31 de Diciembre del anio tributario', 'E', 'C20 31/12/AT');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/06', 'H1510', 'NORMA', 'S', 'Si no existe RutParte A (C8), debe existir tax ID Parte A (C10) , es decir si C8 es igual a cero entonces C10 debe ser distinto de cero', 'E', 'Si C8 = 0 => C10<>0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/06', 'H1511', 'NORMA', 'S', 'Si no existe RutParte B (C11), debe existir tax ID Parte B (C13) es decir si C11  es igual a cero entonces C1 debe ser distinto de cero', 'E', 'Si C11 = 0 => C13 <> 0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/06', 'H1512', 'NORMA', 'S', 'Debe existir al menos un valor mayor que cero en las columnas (C8) y (C10) ', 'E', 'C8 >0 .o.  C10 >0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/06', 'H1513', 'NORMA', 'S', 'Debe existir al menos un valor mayor que cero en las columnas (C11) y (C13)', 'E', 'C11 >0 .o.  C13 <> blanco o vacio');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/06', 'H1507', 'NORMA', 'S', 'La columna tipo de contrato (C21) debe indicar valores segun tabla tipo de contrato (TC) ', 'E', 'Si existe C21 => C21= {1 .o. 2 .o. 3 .o. 4 .o. 5 .o. 6 .o. 7 .o. 8.o. 9 .o.10.o.11.o.12.o.13.o.14}');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/06', 'H1514', 'NORMA', 'S', 'Si tipo de contrato (C21) es igual a opcion otros {12,13,14}, debe existir nombre del instrumento (C22)', 'E', 'Si C21= {12 .o. 13 .o. 14 }=> C22<> Blanco .o. vacio');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/06', 'H1515', 'NORMA', 'S', 'La fecha de suscripcion del acuerdo marco (C18) debe ser anterior o igual a la fecha del contrato de confirmacion. (C20)', 'E', 'C18  C20');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/07', 'H1509', 'NORMA', 'N', 'La fecha de suscripcion del contrato/confirmacion (C20) debe ser anterior o igual al 31 de Diciembre del anio tributario', 'E', 'C20 31/12/AT');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/07', 'H1510', 'NORMA', 'S', 'Si no existe RutParte A (C8), debe existir tax ID Parte A (C10) , es decir si C8 es igual a cero entonces C10 debe ser distinto de cero', 'E', 'Si C8 = 0 => C10<>0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/07', 'H1511', 'NORMA', 'S', 'Si no existe RutParte B (C11), debe existir tax ID Parte B (C13) es decir si C11  es igual a cero entonces C1 debe ser distinto de cero', 'E', 'Si C11 = 0 => C13 <> 0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/07', 'H1512', 'NORMA', 'S', 'Debe existir al menos un valor mayor que cero en las columnas (C8) y (C10) ', 'E', 'C8 >0 .o.  C10 >0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/07', 'H1513', 'NORMA', 'S', 'Debe existir al menos un valor mayor que cero en las columnas (C11) y (C13)', 'E', 'C11 >0 .o.  C13 <> blanco o vacio');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/07', 'H1507', 'NORMA', 'S', 'La columna tipo de contrato (C21) debe indicar valores segun tabla tipo de contrato (TC) ', 'E', 'Si existe C21 => C21= {1 .o. 2 .o. 3 .o. 4 .o. 5 .o. 6 .o. 7 .o. 8.o. 9 .o.10.o.11.o.12.o.13.o.14}');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/07', 'H1514', 'NORMA', 'S', 'Si tipo de contrato (C21) es igual a opcion otros {12,13,14}, debe existir nombre del instrumento (C22)', 'E', 'Si C21= {12 .o. 13 .o. 14 }=> C22<> Blanco .o. vacio');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/07', 'H1515', 'NORMA', 'S', 'La fecha de suscripcion del acuerdo marco (C18) debe ser anterior o igual a la fecha del contrato de confirmacion. (C20)', 'E', 'C18  C20');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/08', 'H1509', 'NORMA', 'N', 'La fecha de suscripcion del contrato/confirmacion (C20) debe ser anterior o igual al 31 de Diciembre del anio tributario', 'E', 'C20 31/12/AT');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/08', 'H1510', 'NORMA', 'S', 'Si no existe RutParte A (C8), debe existir tax ID Parte A (C10) , es decir si C8 es igual a cero entonces C10 debe ser distinto de cero', 'E', 'Si C8 = 0 => C10<>0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/08', 'H1511', 'NORMA', 'S', 'Si no existe RutParte B (C11), debe existir tax ID Parte B (C13) es decir si C11  es igual a cero entonces C1 debe ser distinto de cero', 'E', 'Si C11 = 0 => C13 <> 0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/08', 'H1512', 'NORMA', 'S', 'Debe existir al menos un valor mayor que cero en las columnas (C8) y (C10) ', 'E', 'C8 >0 .o.  C10 >0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/08', 'H1513', 'NORMA', 'S', 'Debe existir al menos un valor mayor que cero en las columnas (C11) y (C13)', 'E', 'C11 >0 .o.  C13 <> blanco o vacio');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/08', 'H1507', 'NORMA', 'S', 'La columna tipo de contrato (C21) debe indicar valores segun tabla tipo de contrato (TC) ', 'E', 'Si existe C21 => C21= {1 .o. 2 .o. 3 .o. 4 .o. 5 .o. 6 .o. 7 .o. 8.o. 9 .o.10.o.11.o.12.o.13.o.14}');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/08', 'H1514', 'NORMA', 'S', 'Si tipo de contrato (C21) es igual a opcion otros {12,13,14}, debe existir nombre del instrumento (C22)', 'E', 'Si C21= {12 .o. 13 .o. 14 }=> C22<> Blanco .o. vacio');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/08', 'H1515', 'NORMA', 'S', 'La fecha de suscripcion del acuerdo marco (C18) debe ser anterior o igual a la fecha del contrato de confirmacion. (C20)', 'E', 'C18  C20');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/09', 'H1509', 'NORMA', 'N', 'La fecha de suscripcion del contrato/confirmacion (C20) debe ser anterior o igual al 31 de Diciembre del anio tributario', 'E', 'C20 31/12/AT');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/09', 'H1510', 'NORMA', 'S', 'Si no existe RutParte A (C8), debe existir tax ID Parte A (C10) , es decir si C8 es igual a cero entonces C10 debe ser distinto de cero', 'E', 'Si C8 = 0 => C10<>0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/09', 'H1511', 'NORMA', 'S', 'Si no existe RutParte B (C11), debe existir tax ID Parte B (C13) es decir si C11  es igual a cero entonces C1 debe ser distinto de cero', 'E', 'Si C11 = 0 => C13 <> 0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/09', 'H1512', 'NORMA', 'S', 'Debe existir al menos un valor mayor que cero en las columnas (C8) y (C10) ', 'E', 'C8 >0 .o.  C10 >0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/09', 'H1513', 'NORMA', 'S', 'Debe existir al menos un valor mayor que cero en las columnas (C11) y (C13)', 'E', 'C11 >0 .o.  C13 <> blanco o vacio');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/09', 'H1507', 'NORMA', 'S', 'La columna tipo de contrato (C21) debe indicar valores segun tabla tipo de contrato (TC) ', 'E', 'Si existe C21 => C21= {1 .o. 2 .o. 3 .o. 4 .o. 5 .o. 6 .o. 7 .o. 8.o. 9 .o.10.o.11.o.12.o.13.o.14}');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/09', 'H1514', 'NORMA', 'S', 'Si tipo de contrato (C21) es igual a opcion otros {12,13,14}, debe existir nombre del instrumento (C22)', 'E', 'Si C21= {12 .o. 13 .o. 14 }=> C22<> Blanco .o. vacio');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/09', 'H1515', 'NORMA', 'S', 'La fecha de suscripcion del acuerdo marco (C18) debe ser anterior o igual a la fecha del contrato de confirmacion. (C20)', 'E', 'C18  C20');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/10', 'H1509', 'NORMA', 'N', 'La fecha de suscripcion del contrato/confirmacion (C20) debe ser anterior o igual al 31 de Diciembre del anio tributario', 'E', 'C20 31/12/AT');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/10', 'H1510', 'NORMA', 'S', 'Si no existe RutParte A (C8), debe existir tax ID Parte A (C10) , es decir si C8 es igual a cero entonces C10 debe ser distinto de cero', 'E', 'Si C8 = 0 => C10<>0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/10', 'H1511', 'NORMA', 'S', 'Si no existe RutParte B (C11), debe existir tax ID Parte B (C13) es decir si C11  es igual a cero entonces C1 debe ser distinto de cero', 'E', 'Si C11 = 0 => C13 <> 0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/10', 'H1512', 'NORMA', 'S', 'Debe existir al menos un valor mayor que cero en las columnas (C8) y (C10) ', 'E', 'C8 >0 .o.  C10 >0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/10', 'H1513', 'NORMA', 'S', 'Debe existir al menos un valor mayor que cero en las columnas (C11) y (C13)', 'E', 'C11 >0 .o.  C13 <> blanco o vacio');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/10', 'H1507', 'NORMA', 'S', 'La columna tipo de contrato (C21) debe indicar valores segun tabla tipo de contrato (TC) ', 'E', 'Si existe C21 => C21= {1 .o. 2 .o. 3 .o. 4 .o. 5 .o. 6 .o. 7 .o. 8.o. 9 .o.10.o.11.o.12.o.13.o.14}');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/10', 'H1514', 'NORMA', 'S', 'Si tipo de contrato (C21) es igual a opcion otros {12,13,14}, debe existir nombre del instrumento (C22)', 'E', 'Si C21= {12 .o. 13 .o. 14 }=> C22<> Blanco .o. vacio');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/10', 'H1515', 'NORMA', 'S', 'La fecha de suscripcion del acuerdo marco (C18) debe ser anterior o igual a la fecha del contrato de confirmacion. (C20)', 'E', 'C18  C20');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/11', 'H1509', 'NORMA', 'N', 'La fecha de suscripcion del contrato/confirmacion (C20) debe ser anterior o igual al 31 de Diciembre del anio tributario', 'E', 'C20 31/12/AT');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/11', 'H1510', 'NORMA', 'S', 'Si no existe RutParte A (C8), debe existir tax ID Parte A (C10) , es decir si C8 es igual a cero entonces C10 debe ser distinto de cero', 'E', 'Si C8 = 0 => C10<>0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/11', 'H1511', 'NORMA', 'S', 'Si no existe RutParte B (C11), debe existir tax ID Parte B (C13) es decir si C11  es igual a cero entonces C1 debe ser distinto de cero', 'E', 'Si C11 = 0 => C13 <> 0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/11', 'H1512', 'NORMA', 'S', 'Debe existir al menos un valor mayor que cero en las columnas (C8) y (C10) ', 'E', 'C8 >0 .o.  C10 >0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/11', 'H1513', 'NORMA', 'S', 'Debe existir al menos un valor mayor que cero en las columnas (C11) y (C13)', 'E', 'C11 >0 .o.  C13 <> blanco o vacio');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/11', 'H1507', 'NORMA', 'S', 'La columna tipo de contrato (C21) debe indicar valores segun tabla tipo de contrato (TC) ', 'E', 'Si existe C21 => C21= {1 .o. 2 .o. 3 .o. 4 .o. 5 .o. 6 .o. 7 .o. 8.o. 9 .o.10.o.11.o.12.o.13.o.14}');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/11', 'H1514', 'NORMA', 'S', 'Si tipo de contrato (C21) es igual a opcion otros {12,13,14}, debe existir nombre del instrumento (C22)', 'E', 'Si C21= {12 .o. 13 .o. 14 }=> C22<> Blanco .o. vacio');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/11', 'H1515', 'NORMA', 'S', 'La fecha de suscripcion del acuerdo marco (C18) debe ser anterior o igual a la fecha del contrato de confirmacion. (C20)', 'E', 'C18  C20');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/12', 'H1509', 'NORMA', 'N', 'La fecha de suscripcion del contrato/confirmacion (C20) debe ser anterior o igual al 31 de Diciembre del anio tributario', 'E', 'C20 31/12/AT');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/12', 'H1510', 'NORMA', 'S', 'Si no existe RutParte A (C8), debe existir tax ID Parte A (C10) , es decir si C8 es igual a cero entonces C10 debe ser distinto de cero', 'E', 'Si C8 = 0 => C10<>0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/12', 'H1511', 'NORMA', 'S', 'Si no existe RutParte B (C11), debe existir tax ID Parte B (C13) es decir si C11  es igual a cero entonces C1 debe ser distinto de cero', 'E', 'Si C11 = 0 => C13 <> 0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/12', 'H1512', 'NORMA', 'S', 'Debe existir al menos un valor mayor que cero en las columnas (C8) y (C10) ', 'E', 'C8 >0 .o.  C10 >0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/12', 'H1513', 'NORMA', 'S', 'Debe existir al menos un valor mayor que cero en las columnas (C11) y (C13)', 'E', 'C11 >0 .o.  C13 <> blanco o vacio');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/12', 'H1507', 'NORMA', 'S', 'La columna tipo de contrato (C21) debe indicar valores segun tabla tipo de contrato (TC) ', 'E', 'Si existe C21 => C21= {1 .o. 2 .o. 3 .o. 4 .o. 5 .o. 6 .o. 7 .o. 8.o. 9 .o.10.o.11.o.12.o.13.o.14}');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/12', 'H1514', 'NORMA', 'S', 'Si tipo de contrato (C21) es igual a opcion otros {12,13,14}, debe existir nombre del instrumento (C22)', 'E', 'Si C21= {12 .o. 13 .o. 14 }=> C22<> Blanco .o. vacio');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1839', '2020/12', 'H1515', 'NORMA', 'S', 'La fecha de suscripcion del acuerdo marco (C18) debe ser anterior o igual a la fecha del contrato de confirmacion. (C20)', 'E', 'C18  C20');



