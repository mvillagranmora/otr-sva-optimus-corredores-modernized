SET SCHEMA "BSCL";
/*HABILITAR PROCESO PDF*/
INSERT INTO "BSCL"."BSCL.Plataforma.BD::BAT.TipoPrograma" ("IdTipoPrograma", "Descripcion", "MaximoIntentos", "Modulo", "NombreClasePrograma","TipoClasePrograma", "EsPauta","TipoSalida","FormatoSalida")
SELECT 'CERTIFICADO_TRIBUTARIO_MASIVO','Generacion certificados Tributarios en PDF',1,'Tributario','cl.bcs.normativa.jobs.BcsJobGenerarCertificadosTributarios','JAVA','N','R','S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM "BSCL"."BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='CERTIFICADO_TRIBUTARIO_MASIVO');


/*NORMATIVA*/

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Normativa"
("IdNormativa", "NombreNormativa", "IdEstado", "MetodoRelacionado", "EntidadDestino", "NombreDestinatario", "CargoDestinatario", "UI_Emision", "PermiteSolicitar")
SELECT 'TRIBUTARIO', 'Documentos Tributarios ', 'ACT', '', 'SII', '', '', 'N', 'N'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::NOR.Normativa" WHERE "IdNormativa"='TRIBUTARIO');

/*Documentos de Certificados*/

--SELECT * FROM BSCL."BSCL.Normativa.BD::DOC.TipoDocumento" WHERE "IdNormativa"='TRIBUTARIO';
--version anterior
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.TipoDocumento"
("IdTipoDocumento", "Descripcion", "IdNormativa", "UI_Visor", "Esquema", "IdTipoProgramaExportacion", "IdTipoProgramaProceso", "UI_Emision", "ExtensionSalida", "ComprimirSalida", "TitulosSalida", "LineasPorSalida", "IdReporte")
SELECT 'CERT43', 'Certificado 43 ', 'TRIBUTARIO', 'S', 'DJ1922', 'GenerarCertificadosMasivosDJs', '', 'N', '', 'N', 'N', 0, ''
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::DOC.TipoDocumento" WHERE "IdTipoDocumento"='CERT43');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.TipoDocumento"
("IdTipoDocumento", "Descripcion", "IdNormativa", "UI_Visor", "Esquema", "IdTipoProgramaExportacion", "IdTipoProgramaProceso", "UI_Emision", "ExtensionSalida", "ComprimirSalida", "TitulosSalida", "LineasPorSalida", "IdReporte")
SELECT 'CERT9', 'Certificado 9', 'TRIBUTARIO', 'S', 'DJ1889', 'GenerarCertificadosMasivosDJs', '', 'N', '', 'N', 'N', 0, ''
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::DOC.TipoDocumento" WHERE "IdTipoDocumento"='CERT9');


INSERT INTO BSCL."BSCL.Normativa.BD::DOC.TipoDocumento"
("IdTipoDocumento", "Descripcion", "IdNormativa", "UI_Visor", "Esquema", "IdTipoProgramaExportacion", "IdTipoProgramaProceso", "UI_Emision", "ExtensionSalida", "ComprimirSalida", "TitulosSalida", "LineasPorSalida", "IdReporte")
SELECT 'CERT24', 'Certificado 24', 'TRIBUTARIO', 'S', 'DJ1899', 'GenerarCertificadosMasivosDJs', '', 'N', '', 'N', 'N', 0, ''
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::DOC.TipoDocumento" WHERE "IdTipoDocumento"='CERT24');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.TipoDocumento"
("IdTipoDocumento", "Descripcion", "IdNormativa", "UI_Visor", "Esquema", "IdTipoProgramaExportacion", "IdTipoProgramaProceso", "UI_Emision", "ExtensionSalida", "ComprimirSalida", "TitulosSalida", "LineasPorSalida", "IdReporte")
SELECT 'CERT36', 'Certificado 36', 'TRIBUTARIO', 'S', 'DJ1899', 'GenerarCertificadosMasivosDJs', '', 'N', '', 'N', 'N', 0, ''
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::DOC.TipoDocumento" WHERE "IdTipoDocumento"='CERT36');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.TipoDocumento"
("IdTipoDocumento", "Descripcion", "IdNormativa", "UI_Visor", "Esquema", "IdTipoProgramaExportacion", "IdTipoProgramaProceso", "UI_Emision", "ExtensionSalida", "ComprimirSalida", "TitulosSalida", "LineasPorSalida", "IdReporte")
SELECT 'CERT8', 'Certificado 8', 'TRIBUTARIO', 'S', 'DJ1944', 'GenerarCertificadosMasivosDJs', '', 'N', '', 'N', 'N', 0, ''
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::DOC.TipoDocumento" WHERE "IdTipoDocumento"='CERT8');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.TipoDocumento"
("IdTipoDocumento", "Descripcion", "IdNormativa", "UI_Visor", "Esquema", "IdTipoProgramaExportacion", "IdTipoProgramaProceso", "UI_Emision", "ExtensionSalida", "ComprimirSalida", "TitulosSalida", "LineasPorSalida", "IdReporte")
SELECT 'CERT17', 'Certificado 17', 'TRIBUTARIO', 'S', 'DJ1944', 'GenerarCertificadosMasivosDJs', '', 'N', '', 'N', 'N', 0, ''
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::DOC.TipoDocumento" WHERE "IdTipoDocumento"='CERT17');


INSERT INTO BSCL."BSCL.Normativa.BD::DOC.TipoDocumento"
("IdTipoDocumento", "Descripcion", "IdNormativa", "UI_Visor", "Esquema", "IdTipoProgramaExportacion", "IdTipoProgramaProceso", "UI_Emision", "ExtensionSalida", "ComprimirSalida", "TitulosSalida", "LineasPorSalida", "IdReporte")
SELECT 'CERT71', 'Certificado 71', 'TRIBUTARIO', 'S', 'DJ1949', 'GenerarCertificadosMasivosDJs', '', 'N', '', 'N', 'N', 0, ''
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::DOC.TipoDocumento" WHERE "IdTipoDocumento"='CERT71');

---version nueva
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.TipoDocumento"
("IdTipoDocumento", "Descripcion", "IdNormativa", "UI_Visor", "Esquema", "IdTipoProgramaExportacion", "IdTipoProgramaProceso", "UI_Emision", "ExtensionSalida", "ComprimirSalida", "TitulosSalida", "LineasPorSalida", "IdReporte")
SELECT 'CERT21', 'Certificado 21 ', 'TRIBUTARIO', 'S', 'DJ1894', 'CERTIFICADO_TRIBUTARIO_MASIVO', '', 'N', '', 'N', 'N', 0, ''
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::DOC.TipoDocumento" WHERE "IdTipoDocumento"='CERT21');

/*DICCIONARIO*/

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )
SELECT  
'UI_ACTUALIZAR_CERTIFICADOS',
'CL',
'Actualizar Certificados',
'ACTCER'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Diccionario" WHERE  "CodDiccionario"='UI_ACTUALIZAR_CERTIFICADOS');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )
SELECT  
'UI_SOLICITAR_GENERACION_DE_PDF',
'CL',
'Solicitar generacion de PDF',
'SOLCER'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Diccionario" WHERE  "CodDiccionario"='UI_SOLICITAR_GENERACION_DE_PDF');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )
SELECT  
'UI_CERTIFICADOS_TRIBUTARIOS',
'CL',
'Certificados Tributarios',
'CERTRI'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Diccionario" WHERE  "CodDiccionario"='UI_CERTIFICADOS_TRIBUTARIOS');

/*habilita flujo optimizado*/
update BSCL."BSCL.Normativa.BD::DOC.TipoDocumento" SET "IdTipoProgramaExportacion"='CERTIFICADO_TRIBUTARIO_MASIVO'
WHERE "IdNormativa"='TRIBUTARIO' AND "IdTipoDocumento" IN ('CERT21','CERT43','CERT9','CERT24','CERT36','CERT17','CERT71');


/* (OPTIMUS CLIENTES) -  Actualizar ID servicios de Reportes Tributarios */

-- 1 Insertar en tabla servicio todos los reportes
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3554, 'BSCL.Normativa.CrystalReports.Servicios::Certificado21', 12, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3554);

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3614, 'BSCL.Normativa.CrystalReports.Servicios::Certificado17', 12, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3614);

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 600033, 'BSCL.Normativa.CrystalReports.Servicios::Certificado9', 12, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=600033);

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 600042, 'BSCL.Normativa.CrystalReports.Servicios::Certificado24', 12, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=600042);

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 600044, 'BSCL.Normativa.CrystalReports.Servicios::Certificado36', 12, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=600044);

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3586, 'BSCL.Normativa.CrystalReports.Servicios::Certificado43', 12, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3586);

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3613, 'BSCL.Normativa.CrystalReports.Servicios::Certificado71', 12, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3613);

/* 2 Actualizar tabla de servicios con nombres correcto del servicio de impresion de certificados */
UPDATE "BSCL.Plataforma.BD::PLA.Servicio" SET "RutaServicio"='BSCL.Normativa.CrystalReports.Servicios::Certificado21' WHERE "IdServicio"=3554;
UPDATE "BSCL.Plataforma.BD::PLA.Servicio" SET "RutaServicio"='BSCL.Normativa.CrystalReports.Servicios::Certificado9' WHERE "IdServicio"=600033;
UPDATE "BSCL.Plataforma.BD::PLA.Servicio" SET "RutaServicio"='BSCL.Normativa.CrystalReports.Servicios::Certificado17' WHERE "IdServicio"=3614;
UPDATE "BSCL.Plataforma.BD::PLA.Servicio" SET "RutaServicio"='BSCL.Normativa.CrystalReports.Servicios::Certificado24' WHERE "IdServicio"=600042;
UPDATE "BSCL.Plataforma.BD::PLA.Servicio" SET "RutaServicio"='BSCL.Normativa.CrystalReports.Servicios::Certificado36' WHERE "IdServicio"=600044;
UPDATE "BSCL.Plataforma.BD::PLA.Servicio" SET "RutaServicio"='BSCL.Normativa.CrystalReports.Servicios::Certificado43' WHERE "IdServicio"=3586;
UPDATE "BSCL.Plataforma.BD::PLA.Servicio" SET "RutaServicio"='BSCL.Normativa.CrystalReports.Servicios::Certificado71' WHERE "IdServicio"=3613;

/* 3 Actualizar IdReporte de todos los tipo documento certificados y ademas de actualizar servicioarchivo de tabla archivo normativa*/
update "BSCL.Normativa.BD::DOC.TipoDocumento"
SET "IdReporte"= (SELECT TOP 1 "IdServicio" FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "RutaServicio"='BSCL.Normativa.CrystalReports.Servicios::Certificado21')
WHERE "IdTipoDocumento" = 'CERT21';

update "BSCL.Normativa.BD::DOC.TipoDocumento"
SET "IdReporte"= (SELECT TOP 1 "IdServicio" FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "RutaServicio"='BSCL.Normativa.CrystalReports.Servicios::Certificado17')
WHERE "IdTipoDocumento" = 'CERT17';

update "BSCL.Normativa.BD::DOC.TipoDocumento"
SET "IdReporte"= (SELECT TOP 1 "IdServicio" FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "RutaServicio"='BSCL.Normativa.CrystalReports.Servicios::Certificado9')
WHERE "IdTipoDocumento" = 'CERT9';

update "BSCL.Normativa.BD::DOC.TipoDocumento"
SET "IdReporte"= (SELECT TOP 1 "IdServicio" FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "RutaServicio"='BSCL.Normativa.CrystalReports.Servicios::Certificado24')
WHERE "IdTipoDocumento" = 'CERT24';

update "BSCL.Normativa.BD::DOC.TipoDocumento"
SET "IdReporte"= (SELECT TOP 1 "IdServicio" FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "RutaServicio"='BSCL.Normativa.CrystalReports.Servicios::Certificado36')
WHERE "IdTipoDocumento" = 'CERT36';

update "BSCL.Normativa.BD::DOC.TipoDocumento"
SET "IdReporte"= (SELECT TOP 1 "IdServicio" FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "RutaServicio"='BSCL.Normativa.CrystalReports.Servicios::Certificado43')
WHERE "IdTipoDocumento" = 'CERT43';

update "BSCL.Normativa.BD::DOC.TipoDocumento"
SET "IdReporte"= (SELECT TOP 1 "IdServicio" FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "RutaServicio"='BSCL.Normativa.CrystalReports.Servicios::Certificado71')
WHERE "IdTipoDocumento" = 'CERT71';
update "BSCL.Normativa.BD::NOR.ArchivoNormativa"
SET "ServicioArchivo"= (SELECT TOP 1 "IdServicio" FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "RutaServicio"='BSCL.Normativa.CrystalReports.Servicios::Certificado21')
WHERE "IdTipoDocumento" = 'CERT21';

update "BSCL.Normativa.BD::NOR.ArchivoNormativa"
SET "ServicioArchivo"= (SELECT TOP 1 "IdServicio" FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "RutaServicio"='BSCL.Normativa.CrystalReports.Servicios::Certificado17')
WHERE "IdTipoDocumento" = 'CERT17';

update "BSCL.Normativa.BD::NOR.ArchivoNormativa"
SET "ServicioArchivo"= (SELECT TOP 1 "IdServicio" FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "RutaServicio"='BSCL.Normativa.CrystalReports.Servicios::Certificado9')
WHERE "IdTipoDocumento" = 'CERT9';

update "BSCL.Normativa.BD::NOR.ArchivoNormativa"
SET "ServicioArchivo"= (SELECT TOP 1 "IdServicio" FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "RutaServicio"='BSCL.Normativa.CrystalReports.Servicios::Certificado24')
WHERE "IdTipoDocumento" = 'CERT24';

update "BSCL.Normativa.BD::NOR.ArchivoNormativa"
SET "ServicioArchivo"= (SELECT TOP 1 "IdServicio" FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "RutaServicio"='BSCL.Normativa.CrystalReports.Servicios::Certificado36')
WHERE "IdTipoDocumento" = 'CERT36';

update "BSCL.Normativa.BD::NOR.ArchivoNormativa"
SET "ServicioArchivo"= (SELECT TOP 1 "IdServicio" FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "RutaServicio"='BSCL.Normativa.CrystalReports.Servicios::Certificado43')
WHERE "IdTipoDocumento" = 'CERT43';

update "BSCL.Normativa.BD::NOR.ArchivoNormativa"
SET "ServicioArchivo"= (SELECT TOP 1 "IdServicio" FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "RutaServicio"='BSCL.Normativa.CrystalReports.Servicios::Certificado71')
WHERE "IdTipoDocumento" = 'CERT71';


DROP TABLE  "BSCL.Normativa.BD::TRI.TipoCertificado";

CREATE COLUMN TABLE "BSCL.Normativa.BD::TRI.TipoCertificado" ("IdTipoCertificado" NVARCHAR(10) NOT NULL ,
"Nombre" NVARCHAR(100) NOT NULL ,
"Descripcion" NVARCHAR(250),
PRIMARY KEY ("IdTipoCertificado")) UNLOAD PRIORITY 5 AUTO MERGE;

INSERT INTO "BSCL.Normativa.BD::TRI.TipoCertificado" ("IdTipoCertificado", "Nombre", "Descripcion")
SELECT 'CERT9', 'CERTIFICADO 9', 'SOBRE RETIROS EFECTUADOS DE LAS CUENTAS DE AHORRO VOLUNTARIO '
FROM DUMMY
WHERE NOT EXISTS(SELECT 1 FROM "BSCL.Normativa.BD::TRI.TipoCertificado" WHERE "IdTipoCertificado" = 'CERT9');

INSERT INTO "BSCL.Normativa.BD::TRI.TipoCertificado" ("IdTipoCertificado", "Nombre", "Descripcion")
SELECT 'CERT17', 'CERTIFICADO 17', 'SOBRE LAS INVERSIONES EN LOS INSTRUMENTOS O VALORES EN CUSTODIA'
FROM DUMMY
WHERE NOT EXISTS(SELECT 1 FROM "BSCL.Normativa.BD::TRI.TipoCertificado" WHERE "IdTipoCertificado" = 'CERT17');

INSERT INTO "BSCL.Normativa.BD::TRI.TipoCertificado" ("IdTipoCertificado", "Nombre", "Descripcion")
SELECT 'CERT21', 'CERTIFICADO 21', 'SOBRE MAYOR O MENOR VALOR OBTENIDO EN EL RESCATE DE CUOTAS DE FONDOS MUTUOS'
FROM DUMMY
WHERE NOT EXISTS(SELECT 1 FROM "BSCL.Normativa.BD::TRI.TipoCertificado" WHERE "IdTipoCertificado" = 'CERT21');

INSERT INTO "BSCL.Normativa.BD::TRI.TipoCertificado" ("IdTipoCertificado", "Nombre", "Descripcion")
SELECT 'CERT24', 'CERTIFICADO 24', 'SOBRE MOVIMIENTO ANUAL DE LAS CUENTAS DE AHORRO PREVISIONAL VOLUNTARIO'
FROM DUMMY
WHERE NOT EXISTS(SELECT 1 FROM "BSCL.Normativa.BD::TRI.TipoCertificado" WHERE "IdTipoCertificado" = 'CERT24');

INSERT INTO "BSCL.Normativa.BD::TRI.TipoCertificado" ("IdTipoCertificado", "Nombre", "Descripcion")
SELECT 'CERT30', 'CERTIFICADO 30', 'SOBRE MOVIMIENTO ANUAL DE LAS CUENTAS DE AHORRO PREVISIONAL VOLUNTARIO COLECTIVO'
FROM DUMMY
WHERE NOT EXISTS(SELECT 1 FROM "BSCL.Normativa.BD::TRI.TipoCertificado" WHERE "IdTipoCertificado" = 'CERT30');

INSERT INTO "BSCL.Normativa.BD::TRI.TipoCertificado" ("IdTipoCertificado", "Nombre", "Descripcion")
SELECT 'CERT31', 'CERTIFICADO 31', 'SOBRE LIQUIDACION DE CUOTAS DE FONDOS MUTUOS NO CONSIDERADAS RESCATES'
FROM DUMMY
WHERE NOT EXISTS(SELECT 1 FROM "BSCL.Normativa.BD::TRI.TipoCertificado" WHERE "IdTipoCertificado" = 'CERT31');

INSERT INTO "BSCL.Normativa.BD::TRI.TipoCertificado" ("IdTipoCertificado", "Nombre", "Descripcion")
SELECT 'CERT36', 'CERTIFICADO 36', 'SOBRE DEPOSITOS CONVENIDOS'
FROM DUMMY
WHERE NOT EXISTS(SELECT 1 FROM "BSCL.Normativa.BD::TRI.TipoCertificado" WHERE "IdTipoCertificado" = 'CERT36');

INSERT INTO "BSCL.Normativa.BD::TRI.TipoCertificado" ("IdTipoCertificado", "Nombre", "Descripcion")
SELECT 'CERT43', 'CERTIFICADO 43', 'SOBRE SITUACIÓN TRIBUTARIA DE DIVIDENDOS, REMESAS, DISTRIBUCIONES, DEVOLUCIONES DE CAPITAL O RESCATE'
FROM DUMMY
WHERE NOT EXISTS(SELECT 1 FROM "BSCL.Normativa.BD::TRI.TipoCertificado" WHERE "IdTipoCertificado" = 'CERT43');

INSERT INTO "BSCL.Normativa.BD::TRI.TipoCertificado" ("IdTipoCertificado", "Nombre", "Descripcion")
SELECT 'CERT45', 'CERTIFICADO 45', 'SOBRE RETIROS  DE INVERSIONES DE AÑOS ANTERIORES'
FROM DUMMY
WHERE NOT EXISTS(SELECT 1 FROM "BSCL.Normativa.BD::TRI.TipoCertificado" WHERE "IdTipoCertificado" = 'CERT45');

INSERT INTO "BSCL.Normativa.BD::TRI.TipoCertificado" ("IdTipoCertificado", "Nombre", "Descripcion")
SELECT 'CERT71', 'CERTIFICADO 71', 'SOBRE SITUACIÓN TRIBUTARIA DE  DIVIDENDOS DISTRIBUIDOS Y CRÉDITOS RECIBIDOS POR ACCIONES EN CUSTODIA'
FROM DUMMY
WHERE NOT EXISTS(SELECT 1 FROM "BSCL.Normativa.BD::TRI.TipoCertificado" WHERE "IdTipoCertificado" = 'CERT71');

/*Inserta archivo normativa*/
INSERT INTO "BSCL.Normativa.BD::NOR.ArchivoNormativa" ("IdArchivoNormativa", "VersionArchivo", "Esquema", "Version", "IdTipoDocumento", "Descripcion", "NombreArchivo", "IdArchivo", "TipoArchivo", "ServicioArchivo", "IdEmpresa", "IdEstado") 
SELECT 'CERT71-PDF', '2022', 'DJ1949', '3', 'CERT71', 'CERTIFICADO 71 TRIBUTARIOS', 'CERTIFICADO71', '', 'pdf', '3613', 1, 'ACT' FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "IdArchivoNormativa" = 'CERT71-PDF' AND "VersionArchivo" = '2022');

INSERT INTO "BSCL.Normativa.BD::NOR.ArchivoNormativa" ("IdArchivoNormativa", "VersionArchivo", "Esquema", "Version", "IdTipoDocumento", "Descripcion", "NombreArchivo", "IdArchivo", "TipoArchivo", "ServicioArchivo", "IdEmpresa", "IdEstado") 
SELECT 'CERT21-PDF', '2022', 'DJ1894', '3', 'CERT21', 'CERTIFICADO 21 TRIBUTARIOS', 'CERTIFICADO21', '', 'pdf', '3554', 1, 'ACT' FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "IdArchivoNormativa" = 'CERT21-PDF' AND "VersionArchivo" = '2022');

INSERT INTO "BSCL.Normativa.BD::NOR.ArchivoNormativa" ("IdArchivoNormativa", "VersionArchivo", "Esquema", "Version", "IdTipoDocumento", "Descripcion", "NombreArchivo", "IdArchivo", "TipoArchivo", "ServicioArchivo", "IdEmpresa", "IdEstado") 
SELECT 'CERT43-PDF', '2022', 'DJ1922', '3', 'CERT43', 'CERTIFICADO 43 TRIBUTARIOS', 'CERTIFICADO43', '', 'pdf', '3586', 1, 'ACT' FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "IdArchivoNormativa" = 'CERT43-PDF' AND "VersionArchivo" = '2022');

INSERT INTO "BSCL.Normativa.BD::NOR.ArchivoNormativa" ("IdArchivoNormativa", "VersionArchivo", "Esquema", "Version", "IdTipoDocumento", "Descripcion", "NombreArchivo", "IdArchivo", "TipoArchivo", "ServicioArchivo", "IdEmpresa", "IdEstado") 
SELECT 'CERT24-PDF', '2022', 'DJ1899', '3', 'CERT24', 'CERTIFICADO 24 TRIBUTARIOS', 'CERTIFICADO24', '', 'pdf', '600042', 1, 'ACT' FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "IdArchivoNormativa" = 'CERT24-PDF' AND "VersionArchivo" = '2022');

INSERT INTO "BSCL.Normativa.BD::NOR.ArchivoNormativa" ("IdArchivoNormativa", "VersionArchivo", "Esquema", "Version", "IdTipoDocumento", "Descripcion", "NombreArchivo", "IdArchivo", "TipoArchivo", "ServicioArchivo", "IdEmpresa", "IdEstado") 
SELECT 'CERT36-PDF', '2022', 'DJ1899', '3', 'CERT36', 'CERTIFICADO 36 TRIBUTARIOS', 'CERTIFICADO36', '', 'pdf', '600044', 1, 'ACT' FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "IdArchivoNormativa" = 'CERT36-PDF' AND "VersionArchivo" = '2022');

INSERT INTO "BSCL.Normativa.BD::NOR.ArchivoNormativa" ("IdArchivoNormativa", "VersionArchivo", "Esquema", "Version", "IdTipoDocumento", "Descripcion", "NombreArchivo", "IdArchivo", "TipoArchivo", "ServicioArchivo", "IdEmpresa", "IdEstado") 
SELECT 'CERT9-PDF', '2022', 'DJ1889', '3', 'CERT9', 'CERTIFICADO 9 TRIBUTARIOS', 'CERTIFICADO9', '', 'pdf', '600033', 1, 'ACT' FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "IdArchivoNormativa" = 'CERT9-PDF' AND "VersionArchivo" = '2022');

INSERT INTO "BSCL.Normativa.BD::NOR.ArchivoNormativa" ("IdArchivoNormativa", "VersionArchivo", "Esquema", "Version", "IdTipoDocumento", "Descripcion", "NombreArchivo", "IdArchivo", "TipoArchivo", "ServicioArchivo", "IdEmpresa", "IdEstado") 
SELECT 'CERT17-PDF', '2022', 'DJ1944', '3', 'CERT17', 'CERTIFICADO 17 TRIBUTARIOS', 'CERTIFICADO17', '', 'pdf', '3614', 1, 'ACT' FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "IdArchivoNormativa" = 'CERT17-PDF' AND "VersionArchivo" = '2022');