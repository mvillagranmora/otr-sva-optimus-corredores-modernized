SET SCHEMA BSCL;

DROP TABLE "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1894";


CREATE COLUMN TABLE "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1894" (
"ID" BIGINT  NOT NULL ,
"IdDocumento" BIGINT  NOT NULL ,
"VboUsuario" CHAR(1)  NOT NULL ,
"Observaciones" NVARCHAR(250) NOT NULL ,
"FuenteDatos" NVARCHAR(40) NOT NULL ,
"Numero" BIGINT  NOT NULL ,											-- 	Sec_Movto	IdSolicitudAporteRescate
"FolioCartera" DECIMAL(26, 8)  DEFAULT 0,							--	Sec_Car_Inver
"IdFondo" NVARCHAR(40) DEFAULT '',
"Nemo" NVARCHAR(40) DEFAULT '',
"IdEmisor" NVARCHAR(40) DEFAULT '',
"RutEmisor" NVARCHAR(40) NOT NULL ,									--C1 RutAdministradora
"RutParticipe" NVARCHAR(40) NOT NULL ,								--C2
"RutFondo" NVARCHAR(40) NOT NULL ,									--C3
"RunFondo" NVARCHAR(40) NOT NULL ,									--C3
"TipoOperacion" NVARCHAR(10) NOT NULL ,								--C5
"FechaOperacion" DATE  NOT NULL ,									
"FechaInversion" DATE  NOT NULL ,									--C6
"MontoNominalInversionActualizado"	DECIMAL(26, 8)  NOT NULL ,		--C7
"MontoHistorico"	 DECIMAL(26, 8)  DEFAULT 0,
"FechaRescate" DATE  NOT NULL ,										--C8
"MontoNominalRescate" DECIMAL(26, 8)  NOT NULL ,					--C9
"MayorValorActualizado" DECIMAL(26, 8)  NOT NULL ,					--C10
"MenorValorActualizado" DECIMAL(26, 8)  NOT NULL ,					--C12
"RutAdminitradoraFondoReceptor" NVARCHAR(40) NOT NULL ,				--C13
"MontoReinvertido" DECIMAL(26, 8)  NOT NULL ,						--C14
"NumeroCertificado" BIGINT  NOT NULL ,								--C16
"Cantidad" 	DECIMAL(26, 8),
"CostoUf"  DECIMAL(26, 8)  DEFAULT 0,
"IdCliente" BIGINT ,
"IdPortafolio" BIGINT ,
"TipoDeclarante" NVARCHAR(40) NOT NULL,
PRIMARY KEY ("ID")) ;


ALTER TABLE "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1894" ADD CONSTRAINT "FK_Documento_DatosDJ1894" FOREIGN KEY ("IdDocumento") REFERENCES "BSCL"."BSCL.Normativa.BD::DOC.Documento" ("IdDocumento") ON
UPDATE    RESTRICT ON
    DELETE
    RESTRICT  INITIALLY IMMEDIATE;

--CREAR TABLA DETALLE CERTIFICADO 21
DROP TABLE "BSCL"."BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1894N21";

CREATE TABLE "BSCL"."BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1894N21" (
	"IdDetalleCertificadoClienteDJ1894N21"  BIGINT NOT NULL,
	"IdCertificadoCliente"                  BIGINT NOT NULL,
	"IdFondo"                               NVARCHAR(40) NOT NULL,
	"Factor"                                DECIMAL(26,8) NOT NULL,
	"MontoRescate"                          DECIMAL(26,8) NOT NULL,
	"MontoHistorico"                        DECIMAL(26,8) NOT NULL,
	"MayorValor"                            DECIMAL(26,8) NOT NULL,
	"MenorValor"                            DECIMAL(26,8) NOT NULL,
    "MayorValorActualizado"                 DECIMAL(26,8) NOT NULL,
	"MenorValorActualizado"                 DECIMAL(26,8) NOT NULL,
	"DescMes"                               NVARCHAR(100) NOT NULL,
	"IdEmisor"                              NVARCHAR(40) NOT NULL,
	"RutCliente"							NVARCHAR(40)	NOT NULL,
    "Version"                               NVARCHAR(10),
    PRIMARY KEY ("IdDetalleCertificadoClienteDJ1894N21")
);

ALTER TABLE "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1894N21"
    ADD CONSTRAINT "FK_CertificadoCliente_IdDetalleCertificadoClienteDJ1894N21" FOREIGN KEY ("IdCertificadoCliente")
        REFERENCES "BSCL.Normativa.BD::TRI.CertificadoCliente" ("IdCertificadoCliente")
        ON DELETE RESTRICT ON UPDATE RESTRICT;


DELETE FROM BSCL."BSCL.Normativa.BD::TRI.Complemento"  WHERE "Esquema"='DJ1894';-- AND "Version"='2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura"  WHERE "Esquema"='DJ1894';-- AND "Version"='2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"  WHERE "Esquema"='DJ1894';-- AND "Version"='2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1894';-- and "Version" = '2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema"='DJ1894';-- AND "Version"='2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1894';-- AND "Version"='2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Documento" where "Esquema" = 'DJ1894';-- and "Version"='2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1894';-- AND "Version"='2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::TRI.ControlProceso" WHERE  "Esquema" = 'DJ1894';



INSERT INTO "BSCL.Normativa.BD::DOC.Esquema" ("Esquema","Version","Modulo","Periodicidad","Objetivo","PeriodoInicio",
		     "IdEntidadReguladora","FechaPresentacionLimite","FechaDatosDesde","FechaDatosHasta",
		     "CodigoVersionSII","HabilitadoMantenedor","TieneCertificado")
VALUES
('DJ1894','2020','TRI','A','Declaración Jurada anual sobre inversiones, reinversiones, liquidación y rescate de cuotas','2020-01-01','SII','2020-01-01','2020-01-01','2020-12-31',0,'S','S');


INSERT INTO "BSCL.Normativa.BD::DOC.Documento"
("IdDocumento","IdTipoDocumento","Esquema","Version","IdEmpresa","FechaLimite","IdEstadoDocumento",
"FechaCreacion","FechaPresentacion","DocumentoIdentidadDeclarante",
"DocumentoIdentidadRepresentante","DireccionElectronicaDeclaracion",
"DireccionFisicaDeclaracion","ComunaDeclaracion","TelefonoDeclaracion",
"FolioDeclaracion","EnProceso","FechaInicioUltimoProceso","AnoMes")
SELECT "BSCL"."BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL AS "IdDocumento",
'DECLACORR' AS "IdTipoDocumento"
,'DJ1894' AS "Esquema"
,'2020' AS "Version"
,1 AS "IdEmpresa"
,'2020-12-31' AS "FechaLimite"
,'ACT' AS "IdEstadoDocumento"
,'2020-01-01 0:00:00.0' AS "FechaCreacion"
,'2020-12-31 0:00:00.0' AS "FechaPresentacion"
,"DocumentoIdentidad" AS "DocumentoIdentidadDeclarante"
,"DocumentoIdentidadRepresentante" AS "DocumentoIdentidadRepresentante"
,"DireccionElectronicaPrincipal" AS "DireccionElectronicaDeclaracion"
, rtrim("DireccionFisicaPrincipalCalle")||' '|| "DireccionFisicaPrincipalNumero" AS "DireccionFisicaDeclaracion"
, "DireccionFisicaPrincipalNombreDivisionPais" AS "ComunaDeclaracion"
,"TelefonoPrincipal" AS "TelefonoDeclaracion"
,'I9153103' AS "FolioDeclaracion"
,'N' AS "EnProceso"
,'2020-12-02 21:35:09.444' AS "FechaInicioUltimoProceso"
,''AS "AnoMes" FROM (
		SELECT top 1 * FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" ORDER BY "IdEmpresa"
) AS "EMP";


/*CAMPOS*/
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'Numero', 'C0', '', 2, 'Numero', '', 'ID', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '1. DATOS DE LOS INFORMADOS', 1, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'RutParticipe', 'C3', '', 4, 'Rut Participe', '', 'RUT', 0, '0', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 3, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'RutFondo', 'C23', '', 5, 'Rut del Fondo Mutuo', '', 'RUT', 0, '0', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 4, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'RunFondo', 'C4', '', 6, 'Run del Fondo Mutuo', '', 'RUT', 0, '0', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 5, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'FechaInversion', 'C6', '', 8, 'Fecha Inversion o Reinversion', '', 'DATE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. SECCI�N B1: INVERSIONES Y REINVERSIONES RECIBIDAS EN EL EJERCICIO', 7, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'FechaOperacion', '', '', 9, 'Fecha Operacion', '', 'DATE_FILTER', 0, '', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '5. DATOS DE CONTROL', 8, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'FechaRescate', 'C8', '', 11, 'Fecha Rescate', '', 'DATE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. SECCI�N B2: RESCATES', 10, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'RutAdminitradoraFondoReceptor', 'C13', '', 15, 'Rut Sociedad Administradora Fondo Receptor de la Reinversi�n', '', 'RUT', 0, '0', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '4. SECCI�N B3: LIQUIDACIONES DE CUOTAS PARA REINVERSIONES INCISO QUINTO ART. 108', 14, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'IdEmisor', '', '', 29, 'Emisor', '', 'NEMO', 0, '', 'S', 'N', 'N', 'N', 'S', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'TipoOperacion', 'C5', '', 7, 'TIpo Operacion', '', 'ID', 0, '1', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 6, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'MontoNominalInversionActualizado', 'C7', '', 10, 'Monto Nominal Inversion o Reinversion (actualizado)', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. SECCI�N B1: INVERSIONES Y REINVERSIONES RECIBIDAS EN EL EJERCICIO', 9, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'MontoNominalRescate', 'C9', '', 12, 'Monto Nominal Rescate a la Fecha en que se Efectuo', '', 'AMOUNT', 0, '0', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. SECCI�N B2: RESCATES', 11, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'MayorValorActualizado', 'C10', '', 13, 'Mayor Valor Actualizado', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. SECCI�N B2: RESCATES', 12, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'MenorValorActualizado', 'C12', '', 14, 'Menor Valor Actualizado', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. SECCI�N B2: RESCATES', 13, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'MontoReinvertido', 'C14', '', 16, 'Monto Reinvertido a Valor de Liquidacion', '', 'AMOUNT', 0, '0', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '4. SECCI�N B3: LIQUIDACIONES DE CUOTAS PARA REINVERSIONES INCISO QUINTO ART. 108', 15, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'NumeroCertificado', 'C16', '', 17, 'Numero Certificado', '', 'ID', 0, '0', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '6. DATOS CERTIFICADO', 16, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'MontoHistorico', '', '', 22, 'Monto Historico', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. SECCI�N B1: INVERSIONES Y REINVERSIONES RECIBIDAS EN EL EJERCICIO', 19, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'ID', '', '', 1, 'Secuencia', '', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'RutEmisor', 'C1', '', 3, 'Rut Emisor Instrumento', '', 'RUT', 0, '0', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '1. DATOS DE LOS INFORMADOS', 2, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'IdDocumento', '', '', 23, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'S', 'N', '5. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'VboUsuario', '', '', 24, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, 'N', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '5. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'Observaciones', '', '', 25, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '5. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'FuenteDatos', '', '', 26, 'Fuente', 'Ayuda de Fuente', 'TEXT', 0, 'INICIAL', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '5. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'CostoUf', '', '', 18, 'Costo Uf', '', 'DECIMAL', 0, '0', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '3. SECCION B2: RESCATES', 17, 4);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'IdCliente', '', '', 19, 'Cliente', 'Ayuda de Secuencia Cliente', 'ID_CLIENTE', 0, '0', 'S', 'S', 'N', 'N', 'N', 'N', 'N', 'N', '5. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'IdPortafolio', '', '', 20, 'Portafolio', 'Ayuda de Secuencia Portafolio', 'ID_PORTAFOLIO', 0, '0', 'S', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '5. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'FolioCartera', '', '', 21, 'FolioCartera', '', 'AMOUNT', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 18, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'IdFondo', '', '', 27, 'Fondo', '', 'NEMO', 0, '', 'S', 'N', 'N', 'N', 'S', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'Nemo', '', '', 28, 'Nemotecnico', '', 'NEMO', 0, '', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 20, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'TipoDeclarante', '', '', 30, 'TipoDeclarante', '', 'TEXT', 0, '', 'S', 'N', 'N', 'S', 'S', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 21, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'Cantidad', '', '', 31, 'Cantidad', '', 'AMOUNT', 0, '0', 'N', 'N', 'N', 'S', 'S', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 22, 0);


/*CAMPOS*/


DELETE FROM "BSCL.Normativa.BD::DOC.Diccionario" where "Tipo"='TipoOperacionDJ1894';

INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo","Codigo","Orden","Descripcion","Activo","Objetivo","Producto")
VALUES('TipoOperacionDJ1894','1',1,'Inversion Recibida','S','','');

INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo","Codigo","Orden","Descripcion","Activo","Objetivo","Producto")
VALUES('TipoOperacionDJ1894','2',2,'Reinversion Recibida, cuotas adquiridas hasta el 19.04.2001','S','','');

INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo","Codigo","Orden","Descripcion","Activo","Objetivo","Producto")
VALUES('TipoOperacionDJ1894','3',3,'Reinversion Recibida, cuotas adquiridas con posterioridad al 19.04.2001.','S','','');

INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo","Codigo","Orden","Descripcion","Activo","Objetivo","Producto")
VALUES('TipoOperacionDJ1894','4',4,'Rescate de Cuotas que No Provienen de Reinversiones Recibidas, adquiridas hasta el 19.04.2001.','S','','');

INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo","Codigo","Orden","Descripcion","Activo","Objetivo","Producto")
VALUES('TipoOperacionDJ1894','5',5,'Rescate de Cuotas que Provienen de Reinversiones Recibidas, adquiridas hasta el 19.04.2001.','S','','');

INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo","Codigo","Orden","Descripcion","Activo","Objetivo","Producto")
VALUES('TipoOperacionDJ1894','6',6,'Rescate de Cuotas que No Provienen de Reinversiones Recibidas, adquiridas con post al 19.04.2001.','S','','');

INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo","Codigo","Orden","Descripcion","Activo","Objetivo","Producto")
VALUES('TipoOperacionDJ1894','7',7,'Rescate de Cuotas que Provienen de Reinversiones Recibidas, adquiridas con post al 19.04.2001.','S','','');

INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo","Codigo","Orden","Descripcion","Activo","Objetivo","Producto")
VALUES('TipoOperacionDJ1894','8',8,'Liquidacion de Cuotas para Reinversion que No Provienen de Reinversiones Recibidas.','S','','');

INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo","Codigo","Orden","Descripcion","Activo","Objetivo","Producto")
VALUES('TipoOperacionDJ1894','9',9,'Liquidacion de Cuotas para Reinversion que Provienen de Reinversiones Recibidas.','S','','');

DELETE FROM "BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1894' AND "Version" = '2020';

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1894' as "Esquema",
    '2020' as "Version",
    'H063' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'RUN Fondo Mutuo[C4] no debe estar en blanco y debe cumplir modulo 11' as "Descripcion",
    'E' as "Severidad",
    'RUN Fondo[C4] <> B .y. M11{(RUN Fondo[C4])} = 1' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1894' as "Esquema",
    '2020' as "Version",
    'H064' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Los valores de Tipo de Operacion [C5], deben ser: 1 o 2 o 3 o 4 o 5 o 6 o 7 o 8 o 9' as "Descripcion",
    'E' as "Severidad",
    '[C5] = 1, 2, 3, 4, 5, 6, 7, 8 o 9. ' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1894' as "Esquema",
    '2020' as "Version",
    'H259' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'La suma del valor en las columnas Monto Nominal Inversion, Monto Nominal Rescate, Mayor Valor Actualizado, Menor Valor Actualizado  y Monto Reinvertido a valor de Liquidacion debe ser mayor que 0.' as "Descripcion",
    'E' as "Severidad",
    '[C7]+[C9]+[C10]+[C12]+[C14] > 0' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1894' as "Esquema",
    '2020' as "Version",
    'H842' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si Hay fecha Inversion o reinversion[C6], debe Haber Monto Nominal Inversion, y los campos: Monto Nominal Rescate , Mayor Valor Actualizado , Menor Valor Actualizado y Monto Reinvertido a valor de Liquidacion  deben estar vacios.' as "Descripcion",
    'E' as "Severidad",
    'Si [C6] <> B, entonces: [C7] >0 .y. E {[C9] + [C10] + [C12] + [C14] }=0' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1894' as "Esquema",
    '2020' as "Version",
    'H843' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si Tipo de Operacion es igual a 1, 2 o 3, la fecha de Inversion debe ser entre el 01/01/AT-1 y el 31/12/AT-1, incluidos.' as "Descripcion",
    'E' as "Severidad",
    'Si [C5] = { 1 .o. 2 .o. 3 }, entonces: 01/01/AT-1> [C6] < 31/12/AT-1' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1894' as "Esquema",
    '2020' as "Version",
    'H845' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Verificar, que se Haya indicado el tipo de declarante 1: Administradora de Fondos Mutuos 2: Institucion Intermediaria' as "Descripcion",
    'E' as "Severidad",
    'Tipo de Declarante = { 1.o.2} ' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1894' as "Esquema",
    '2020' as "Version",
    'H962' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si Tipo de Operacion [C5] es igual a 1, 2 o 3, entonces debe Haber fecha inversion o reinversion[FI], Monto Nominal inversion o reinversion[MNI] y Porcentaje accionario[ACC]' as "Descripcion",
    'E' as "Severidad",
    'Si [C5] = 1 .o. 2 .o. 3, entonces: [C6] <> B .y. [C7] <> B .y. [ACC] <> B' as "Formula"
    
    
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1894' as "Esquema",
    '2020' as "Version",
    'H964' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si Tipo de Operacion[C5] es igual a 8 o 9, donde: 8: Liquidacion de Cuotas para Reinversion que No Provienen de Reinversiones Recibidas. 9: Liquidacion de Cuotas para Reinversion que Provienen de Reinversiones Recibidas. entonces debe Haber Monto Reinvertido a valor de liquidacion[C14]' as "Descripcion",
    'E' as "Severidad",
    'Si [C5] = 8 .o. 9, entonces: [C14] <> B' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1894' as "Esquema",
    '2020' as "Version",
    'H965' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si Hay RutEmisor Instrumento, entonces este debe ser mayor o igual a 50.000.000.' as "Descripcion",
    'E' as "Severidad",
    'Si [C1] <> B, entonces: [C1] >= 50.000.000'  as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1894' as "Esquema",
    '2020' as "Version",
    'H966' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si hay RutSociedad Adm. fondo receptor de la reinversion, entonces debe ser mayor a 50.000.000' as "Descripcion",
    'E' as "Severidad",
    'Si RutSociedad Adm. fondo receptor de la inversion [C13]<> B, entonces: RutSoc. Adm, fondo receptor de la inversion [C13]>= 50.000.000'  as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1894' as "Esquema",
    '2020' as "Version",
    'H1291' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si Tipo de Operacion [C5] es igual a 4 o 5 o6 o 7.Entonces,la fecha de Rescate [C8] debe ser entre el 01/01/AT-1 y el 31/12/AT-1, incluidos.' as "Descripcion",
    'E' as "Severidad",
    'Si [C5] = { 4 .o. 5 .o. 6 .o. 7 }, entonces: 01/01/AT-1>= [C8] <= 31/12/AT-1'  as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1894' as "Esquema",
    '2020' as "Version",
    'H1292' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si Hay fecha Rescate [C8], debe Haber Monto Nominal Rescate.[C9]' as "Descripcion",
    'E' as "Severidad",
    'Si [C8] <> B, entonces: [C9] >0'  as "Formula"
from dummy;

DELETE FROM  "BSCL.Normativa.BD::NOR.Configuracion" WHERE "CodigoParametro"='TIPO_DECLARANTE_DJ1894';

INSERT INTO "BSCL.Normativa.BD::NOR.Configuracion"
("IdEmpresa","CodigoParametro","Descripcion","Valor","IdNormativa")
VALUES(1,'TIPO_DECLARANTE_DJ1894','Tipo declarante para DJ1894','1','DJ1894');

DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"  WHERE "Esquema"='DJ1894' AND "Version"='2020';
insert into "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura" ("Esquema","Version","Tipo","Descripcion")
select
    'DJ1894' as "Esquema",
    '2020' as "Version",
    'PorIdFondo' as "Tipo",
    'Cuadratura por Fondo' as "Descripcion"
from dummy;

DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura"  WHERE "Esquema"='DJ1894' AND "Version"='2020';
insert into "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" ("Esquema","Version","TipoCuadratura","NombreCampo","Orden")
select
    'DJ1894' as "Esquema",
    '2020' as "Version",
    'PorIdFondo' as "TipoCuadratura",
    'IdFondo' as "NombreCampo",
    1 as "Orden"
from dummy;


--CONTROL PROCESO

insert into "BSCL"."BSCL.Normativa.BD::TRI.ControlProceso"
("Esquema","Version","EstadoCertificado","Carga","Validacion","PreDeclaracion","Certificacion")
SELECT "Esquema","Version",'NGE','N','N','N','N' FROM "BSCL"."BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1894' AND "Version" IN ('2020');


--SERVICIOS
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3303, 'BSCL.Normativa.BD.Vistas::TRI.DJ1894.Invalidos', 0, 'Q', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3303);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3321, 'BSCL.Normativa.BD.Vistas::TRI.DJ1894.Origen', 0, 'Q', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3321);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3216, 'BSCL.Normativa.BD.Vistas::TRI.DJ1894.Datos', 95, 'Q', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3216);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3526, 'BSCL.Normativa.BD.Servicios::TRI.DJ1894.CargarPorcentajeAccionarioFondo', 1, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3526);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3304, 'BSCL.Normativa.BD.Servicios::TRI.DJ1894.EliminarDetalles', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3304);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3305, 'BSCL.Normativa.BD.Servicios::TRI.DJ1894.GrabarDetalle', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3305);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3527, 'BSCL.Normativa.BD.Servicios::TRI.DJ1894.CargarFondos', 1, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3527);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3539, 'BSCL.Normativa.BD.Servicios::TRI.DJ1894.ActualizarValorFactorIpc', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3539);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3528, 'BSCL.Normativa.BD.Servicios::TRI.DJ1894.CambiarEstadoFondo', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3528);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3543, 'BSCL.Normativa.BD.Servicios::TRI.DJ1894.GenerarCertificado21', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3543);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4168, 'BSCL.Normativa.BD.Servicios::TRI.DJ1894.GenerarCertificado31', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4168);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3554, 'BSCL.Normativa.BD.Servicios::TRI.DJ1894.GenerarDocumentoCertificado21', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3554);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3575, 'BSCL.Normativa.BD.Servicios::TRI.DJ1894.GenerarDocumentoCertificado31', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3575);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3073, 'BSCL.Normativa.BD.Servicios::TRI.DJ1894.ObtenerTotales', 113, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3073);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4290713, 'BSCL.Normativa.BD.Servicios::TRI.DJ1894.GrabarMasivo', 113, 'P', '', '', '', 'S'
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4290713);

/*
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Documento"
("IdDocumento", "IdTipoDocumento", "Esquema", "Version", "IdEmpresa", "FechaLimite", "IdEstadoDocumento", "FechaCreacion", "FechaPresentacion", "DocumentoIdentidadDeclarante", "DocumentoIdentidadRepresentante", "DireccionElectronicaDeclaracion", "DireccionFisicaDeclaracion", "ComunaDeclaracion", "TelefonoDeclaracion", "FolioDeclaracion", "EnProceso", "FechaInicioUltimoProceso", "AnoMes")
VALUES(1, 'DECLACORR', 'DJ1823', '2017', 1, '2017-12-31', 'ACT', '2017-01-01 00:00:00.000', '2017-12-31 00:00:00.000', '90947000-1', '14448678-1', 'contabilidad@imtrust.cl', 'El Golf 47', 'Las Condes', '5689854', 'I9153103', 'N', '2018-05-04 20:17:50.589', '');

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.Fondos"
("IdFondo", "Esquema", "Version", "PorcentajeAccionario", "Observacion", "IdEstado")
VALUES('FMLIQUIDEZ', 'DJ1894', '2020', 0.00000000, '', 'ACT');
INSERT INTO BSCL."BSCL.Normativa.BD::TRI.Fondos"
("IdFondo", "Esquema", "Version", "PorcentajeAccionario", "Observacion", "IdEstado")
VALUES('TESTCUSTODIA', 'DJ1894', '2020', 0.00000000, '', 'ACT');
INSERT INTO BSCL."BSCL.Normativa.BD::TRI.Fondos"
("IdFondo", "Esquema", "Version", "PorcentajeAccionario", "Observacion", "IdEstado")
VALUES('CUSTODIACHILE', 'DJ1894', '2020', 0.00000000, '', 'ACT');
*/

--CERTIFICADO
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.CertificadoCliente" WHERE "Esquema"='DJ1894';

DELETE FROM BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente" WHERE "Esquema"='DJ1894'; 

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
("Esquema", "Version", "UltimoFolio", "CodigoCertificado", "NombreCertificado", "CodigoPrograma")
VALUES('DJ1894', '2020', 0, 'CERTIFICADO_21', 'Certificado 21', 'GenerarCertificado21');

--Archivo Normativa
DELETE FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa"  WHERE "Esquema"='DJ1894';

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa"
("IdArchivoNormativa", "VersionArchivo", "Esquema", "Version", "IdTipoDocumento", "Descripcion", "NombreArchivo", "IdArchivo", "TipoArchivo", "ServicioArchivo", "IdEmpresa", "IdEstado")
VALUES('CERT21-PDF', '2020', 'DJ1894', '3', 'CERT21', 'CERTIFICADO 21 TRIBUTARIOS', 'CERTIFICADO21', '', 'pdf', '3543', 1, 'ACT');

-- PARAMETRO:  Nombre de archivo CERTIFICADOS
DELETE  FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" WHERE "IdNormativa"='DJ1894' AND "CodigoParametro" LIKE 'TRI_NOM_CERTIFICADO%';
  
INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Configuracion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
SELECT 1 AS "IdEmpresa",'TRI_NOM_' || "NombreArchivo" ||  '_01'AS "CodigoParametro",'Nombre del archivo PDF en  ZIP masivo de certificados.' AS "Descripcion"
,'[RUT]' AS "Valor"
,"Esquema" AS  "IdNormativa"
,'S' AS "EsModificableUsuario"
,'Certificado masivo' AS "Uso"
,'[RUT]'  AS "ValorDefecto"
,'[RUT],[PERIODO],[DJ],[CERT],texto libre' AS "ValoresPosibles",'S' AS "ModificablePorUsuario",'' AS  "ModificablePostProduccion", '' AS  "CreadorParametro", CURRENT_UTCTIMESTAMP AS "FechaCreacion"
,'' AS "ModificadorParametro", CURRENT_UTCTIMESTAMP AS "FechaModificacion", '' AS "Observaciones"
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1894' AND "VersionArchivo"=2020
AND NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" N WHERE N."CodigoParametro"='TRI_NOM_' || "NombreArchivo" ||  '_01' AND N."IdNormativa"="Esquema");  

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Configuracion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
SELECT 1 AS "IdEmpresa",'TRI_NOM_' || "NombreArchivo" ||  '_02'AS "CodigoParametro",'Nombre del archivo PDF en  ZIP masivo de certificados.' AS "Descripcion"
,'AV' AS "Valor"
,"Esquema" AS  "IdNormativa"
,'S' AS "EsModificableUsuario"
,'Certificado masivo' AS "Uso"
,'AV'  AS "ValorDefecto"
,'[RUT],[PERIODO],[DJ],[CERT],texto libre' AS "ValoresPosibles",'S' AS "ModificablePorUsuario",'' AS  "ModificablePostProduccion", '' AS  "CreadorParametro", CURRENT_UTCTIMESTAMP AS "FechaCreacion"
,'' AS "ModificadorParametro", CURRENT_UTCTIMESTAMP AS "FechaModificacion", '' AS "Observaciones"
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1894' AND "VersionArchivo"=2020
AND NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" N WHERE N."CodigoParametro"='TRI_NOM_' || "NombreArchivo" ||  '_02' AND N."IdNormativa"="Esquema");  

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Configuracion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
SELECT 1 AS "IdEmpresa",'TRI_NOM_' || "NombreArchivo" ||  '_03'AS "CodigoParametro",'Nombre del archivo PDF en  ZIP masivo de certificados.' AS "Descripcion"
,'[PERIODO]' AS "Valor"
,"Esquema" AS  "IdNormativa"
,'S' AS "EsModificableUsuario"
,'Certificado masivo' AS "Uso"
,'[PERIODO]'  AS "ValorDefecto"
,'[RUT],[PERIODO],[DJ],[CERT],texto libre' AS "ValoresPosibles",'S' AS "ModificablePorUsuario",'' AS  "ModificablePostProduccion", '' AS  "CreadorParametro", CURRENT_UTCTIMESTAMP AS "FechaCreacion"
,'' AS "ModificadorParametro", CURRENT_UTCTIMESTAMP AS "FechaModificacion", '' AS "Observaciones"
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1894' AND "VersionArchivo"=2020
AND NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" N WHERE N."CodigoParametro"='TRI_NOM_' || "NombreArchivo" ||  '_03' AND N."IdNormativa"="Esquema");  

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Configuracion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
SELECT 1 AS "IdEmpresa",'TRI_NOM_' || "NombreArchivo" ||  '_04'AS "CodigoParametro",'Nombre del archivo PDF en  ZIP masivo de certificados.' AS "Descripcion"
,'[DJ]' AS "Valor"
,"Esquema" AS  "IdNormativa"
,'S' AS "EsModificableUsuario"
,'Certificado masivo' AS "Uso"
,'[DJ]'  AS "ValorDefecto"
,'[RUT],[PERIODO],[DJ],[CERT],texto libre' AS "ValoresPosibles",'S' AS "ModificablePorUsuario",'' AS  "ModificablePostProduccion", '' AS  "CreadorParametro", CURRENT_UTCTIMESTAMP AS "FechaCreacion"
,'' AS "ModificadorParametro", CURRENT_UTCTIMESTAMP AS "FechaModificacion", '' AS "Observaciones"
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1894' AND "VersionArchivo"=2020
AND NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" N WHERE N."CodigoParametro"='TRI_NOM_' || "NombreArchivo" ||  '_04' AND N."IdNormativa"="Esquema");  


INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Configuracion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
SELECT 1 AS "IdEmpresa",'TRI_NOM_' || "NombreArchivo" ||  '_05'AS "CodigoParametro",'Nombre del archivo PDF en  ZIP masivo de certificados.' AS "Descripcion"
,'[CERT]' AS "Valor"
,"Esquema" AS  "IdNormativa"
,'S' AS "EsModificableUsuario"
,'Certificado masivo' AS "Uso"
,'[CERT]'  AS "ValorDefecto"
,'[RUT],[PERIODO],[DJ],[CERT],texto libre' AS "ValoresPosibles",'S' AS "ModificablePorUsuario",'' AS  "ModificablePostProduccion", '' AS  "CreadorParametro", CURRENT_UTCTIMESTAMP AS "FechaCreacion"
,'' AS "ModificadorParametro", CURRENT_UTCTIMESTAMP AS "FechaModificacion", '' AS "Observaciones"
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1894' AND "VersionArchivo"=2020
AND NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" N WHERE N."CodigoParametro"='TRI_NOM_' || "NombreArchivo" ||  '_05' AND N."IdNormativa"="Esquema");


/* PARAMETRO DJ1894 OMITE FONDO QUE COMIENZE EN 0 */
INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Configuracion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
SELECT 1 AS "IdEmpresa",'OMITIR_FONDOS_0'AS "CodigoParametro",'Parámetro que omite fondos Sura que inician con 0' AS "Descripcion"
, 'S' AS "Valor"
, "Esquema" AS  "IdNormativa"
, 'S' AS "EsModificableUsuario"
, '' AS "Uso"
, 'N' AS "ValorDefecto"
, 'S,N' AS "ValoresPosibles"
, 'S' AS "ModificablePorUsuario"
, '' AS  "ModificablePostProduccion"
, '' AS  "CreadorParametro"
, CURRENT_UTCTIMESTAMP AS "FechaCreacion"
, '' AS "ModificadorParametro"
, CURRENT_UTCTIMESTAMP AS "FechaModificacion"
, '' AS "Observaciones"
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1894' AND "VersionArchivo"=2020
AND NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" N WHERE N."CodigoParametro"='OMITIR_FONDOS_0' AND N."IdNormativa"="Esquema");


/* PARAMETRO DJ1894 OMITE MONTOS EN 0 APORTES/RESCATES */
INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Configuracion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
SELECT 1 AS "IdEmpresa",'OMITIR_MONTOS_0'AS "CodigoParametro",'Parámetro que omite montos en 0' AS "Descripcion"
, 'N' AS "Valor"
, "Esquema" AS  "IdNormativa"
, 'S' AS "EsModificableUsuario"
, '' AS "Uso"
, 'N' AS "ValorDefecto"
, 'S,N' AS "ValoresPosibles"
, 'S' AS "ModificablePorUsuario"
, '' AS  "ModificablePostProduccion"
, '' AS  "CreadorParametro"
, CURRENT_UTCTIMESTAMP AS "FechaCreacion"
, '' AS "ModificadorParametro"
, CURRENT_UTCTIMESTAMP AS "FechaModificacion"
, '' AS "Observaciones"
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1894' AND "VersionArchivo"=2020
AND NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" N WHERE N."CodigoParametro"='OMITIR_MONTOS_0' AND N."IdNormativa"="Esquema");


