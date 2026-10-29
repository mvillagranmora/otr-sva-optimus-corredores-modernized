SET SCHEMA BSCL;

DROP TABLE "BSCL.Normativa.BD::TRI.DatosDJ1871";

create column table "BSCL.Normativa.BD::TRI.DatosDJ1871" (
    "ID" bigint not null,
    "IdDocumento" bigint not null,
    "VboUsuario" char(1) not null,
    "Observaciones" nvarchar(250) not null,
    "FuenteDatos" nvarchar(40) not null,
    ----------------------------------------
    "EjecutadoComplementoIPC" nvarchar(1),
    "Ejecutado" nvarchar(1),
    "Numero" BIGINT NOT NULL,
    "RutCliente" nvarchar(40) not null,
    "IdCliente" bigint default 0,
    "FechaOperacion" date,
    "AnoAhorro" bigint not null,
    --Ahorros
    "AhorrosTrabajador" decimal(26,8) not null,
    "AhorrosTrabajadorActualizado" decimal(26,8),--IPC
    "AhorrosDeposito" decimal(26,8) not null,
    "AhorrosDepositoActualizado" decimal(26,8),--IPC
    "AhorrosCotizaciones" decimal(26,8) not null,
    "AhorrosCotizacionesActualizado" decimal(26,8),--IPC
    --Retiros
    "RetirosTrabajador" decimal(26,8) not null,
    "RetirosTrabajadorActualizado" decimal(26,8),--IPC
    "RetirosDeposito" decimal(26,8) not null,
    "RetirosDepositoActualizado" decimal(26,8),--IPC
    "RetirosCotizaciones" decimal(26,8) not null,
    "RetirosCotizacionesActualizado" decimal(26,8),--IPC
    primary key ("ID")
);

alter table "BSCL.Normativa.BD::TRI.DatosDJ1871"
    add constraint "FK_Documento_DatosDJ1871" foreign key ("IdDocumento")
        references "BSCL.Normativa.BD::DOC.Documento" ("IdDocumento")
        on delete restrict on update restrict;


--Limpiar todas las versiones        
DELETE FROM "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1871";

DELETE FROM BSCL."BSCL.Normativa.BD::TRI.Complemento"  WHERE "Esquema"='DJ1871';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura"  WHERE "Esquema"='DJ1871';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"  WHERE "Esquema"='DJ1871';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1871';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema"='DJ1871';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1871';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Documento" where "Esquema" = 'DJ1871';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1871';

DELETE FROM "BSCL"."BSCL.Normativa.BD::TRI.ControlProceso" WHERE  "Esquema" = 'DJ1871';



INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Esquema"
("Esquema", "Version", "Modulo", "Periodicidad", "Objetivo", "PeriodoInicio", "IdEntidadReguladora", "FechaPresentacionLimite", "FechaDatosDesde", "FechaDatosHasta", "CodigoVersionSII", "HabilitadoMantenedor", "TieneCertificado")
SELECT 'DJ1871', '2020', 'TRI', 'A', 'Declaración Jurada Anual sobre Información para la bonificación', '2020-01-01', 'SII', '2021-01-01', '2020-01-01', '2020-12-31', 1, 'S', 'N'
FROM DUMMY;


INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") VALUES('DJ1871', '2020', 'ID', '', '', 1, 'Secuencia', 'Ayuda de Secuencia', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'S', 'S', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") VALUES('DJ1871', '2020', 'RutCliente', 'C2', '', 2, 'Rut Trabajador', 'Ayuda de RUT Trabajador', 'RUT', 40, '', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 1, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") VALUES('DJ1871', '2020', 'AnoAhorro', 'C3', '', 3, 'Año Ahorro', '', 'ID', 0, '0', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 2, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") VALUES('DJ1871', '2020', 'FechaOperacion', '', '', 4, 'Fecha Operacion', '', 'DATE_FILTER', 0, '0', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 3, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") VALUES('DJ1871', '2020', 'AhorrosTrabajador', 'C4', '', 5, 'Ahorro Previsional Voluntario Colectivo (Ahorro Trabajador)', 'Ahorro Previsional Voluntario Colectivo (Ahorro Trabajador)', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. AHORROS', 4, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") VALUES('DJ1871', '2020', 'AhorrosDeposito', 'C5', '', 6, 'Deposito Ahorro Previsional Voluntario', 'Ayuda Deposito Ahorro Previsional Voluntario', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. AHORROS', 5, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") VALUES('DJ1871', '2020', 'AhorrosCotizaciones', 'C6', '', 7, 'Cotizaciones Voluntarias', 'Ayuda Cotizaciones Voluntarias', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. AHORROS', 6, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") VALUES('DJ1871', '2020', 'RetirosTrabajador', 'C7', '', 8, 'Ahorro Previsional Voluntario Colectivo (Ahorro Trabajador)', 'Ahorro Previsional Voluntario Colectivo (Ahorro Trabajador)', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. RETIROS', 7, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") VALUES('DJ1871', '2020', 'RetirosDeposito', 'C8', '', 9, 'Deposito Ahorro Previsional Voluntario', 'Ayuda Deposito Ahorro Previsional Voluntario', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. RETIROS', 8, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") VALUES('DJ1871', '2020', 'RetirosCotizaciones', 'C9', '', 10, 'Cotizaciones Voluntarias', 'Ayuda Cotizaciones Voluntarias', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. RETIROS', 9, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") VALUES('DJ1871', '2020', 'Numero', 'C0', '', 11, 'Numero', '', 'ID', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '4. DATOS DE CONTROL', 10, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") VALUES('DJ1871', '2020', 'IdDocumento', '', '', 23, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'S', 'N', '4. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") VALUES('DJ1871', '2020', 'VboUsuario', '', 'VistoBueno', 13, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, 'N', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") VALUES('DJ1871', '2020', 'Observaciones', '', '', 14, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") VALUES('DJ1871', '2020', 'FuenteDatos', '', '', 15, 'Fuente', 'Ayuda de Fuente', 'TEXT', 40, 'INICIAL', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '4. DATOS DE CONTROL', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") VALUES('DJ1871', '2020', 'IdCliente', '', '', 16, 'Secuencia Cliente', 'Ayuda de Secuencia Cliente', 'ID_CLIENTE', 0, '0', 'N', 'N', 'N', 'N', 'S', 'N', 'N', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") VALUES('DJ1871', '2020', 'EjecutadoComplementoIPC', '', '', 17, 'Ejecucion complemento IPC', '', 'TEXT', 1, 'N', 'N', 'N', 'N', 'N', 'S', 'S', 'N', 'N', '4. DATOS DE CONTROL', 0, 0);


--Ejecucion complemento actualizacion IPC
SELECT 'DJ1871',2020,'EjecutadoComplementoIPC','C99','',17,'Ejecucion complemento IPC','','TEXT','1','N','N','N','N','N','S','S','N','N','4. DATOS DE CONTROL',0,0 FROM DUMMY;



insert into "BSCL.Normativa.BD::DOC.Documento"
("IdDocumento","IdTipoDocumento","Esquema","Version","IdEmpresa","FechaLimite","IdEstadoDocumento",
"FechaCreacion","FechaPresentacion","DocumentoIdentidadDeclarante",
"DocumentoIdentidadRepresentante","DireccionElectronicaDeclaracion",
"DireccionFisicaDeclaracion","ComunaDeclaracion","TelefonoDeclaracion",
"FolioDeclaracion","EnProceso","FechaInicioUltimoProceso","AnoMes")
SELECT "BSCL"."BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL AS "IdDocumento",
'DECLACORR' AS "IdTipoDocumento"
,'DJ1871' AS "Esquema"
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




INSERT INTO BSCL."BSCL.Normativa.BD::TRI.Complemento" ("Esquema","CodigoComplemento","Descripcion")
SELECT 'DJ1871','CARGAIPC','Carga IPC' FROM DUMMY;

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.Complemento" ("Esquema","CodigoComplemento","Descripcion")
SELECT 'DJ1871','AJURESDELPER','Ajustar Rescate del Periodo' FROM DUMMY;

--VALIDACIONES

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1871', '2020', 'H1703', 'NORMA', 'S', 'Si Año de Ahorro es igual a año comercial vigente y el Ahorro Previsional Voluntario colectivo acogido al inciso segundo del Art 42 Bis es igual a cero, entonces también debe tener retiros de Ahorro Previsional Voluntario colectivo igual a cero para el año comercial vigente.', 'E', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1871', '2020', 'H1704', 'NORMA', 'S', 'Si el año de Ahorro es igual al año comercial vigente y el ahorro por Depósito Ahorro Previsional Voluntario acogido al inciso segundo del Art 42 Bis es igual a cero, entonces también debe tener retiros por Depósito Ahorro Previsonal Voluntario  igual a cero  para el año comercial vigente.', 'E', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1871', '2020', 'H1705', 'NORMA', 'S', 'Si el Año de Ahorro es igual al año comercial vigente y el ahorro acogido al inciso segundo del Art 42 Bis por Cotizaciones Voluntarias  es igual a cero, entonces también debe tener retiro igual a cero en Cotizaciones Voluntarias para el año comercial vigente.', 'E', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1871', '2020', 'H1706', 'NORMA', 'S', 'Si el año de Ahorro es igual a año comercial vigente y el contribuyente presenta ahorro acogido al inciso segundo del Art 42 Bis por concepto de Ahorro Previsional Voluntario Colectivo (C4), dicho Ahorro debe ser mayor o igual a los Retiros por Ahorro Previsional ', 'E', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1871', '2020', 'H1707', 'NORMA', 'S', 'Si el Año de Ahorro es igual al año comercial vigente y el contribuyente presenta ahorro acogido al inciso segundo del Art 42 Bis por concepto de Depósito Ahorro Previsional Volutarios (C5), dicho ahorro debe ser mayor o igual a los Retiros por Depósito Ahorro Previsional Voluntario (C8).', 'E', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1871', '2020', 'H1708', 'NORMA', 'S', 'Si el Año de Ahorro es igual al año comercial vigente y el contribuyente presenta ahorro acogidos al inciso segundo del Art 42 Bis por concepto de Cotizaciones Voluntarias (C6), dicho ahorro debe ser mayor o igual a los Retiros por Cotizaciones Voluntarias (C9).', 'E', '');


--CONTROL PROCESO

insert into "BSCL"."BSCL.Normativa.BD::TRI.ControlProceso"
("Esquema","Version","EstadoCertificado","Carga","Validacion","PreDeclaracion","Certificacion")
SELECT "Esquema","Version",'NGE','N','N','N','N' FROM "BSCL"."BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1871' AND "Version" IN ('2020');

--SERVICIOS DJ1871

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 600023, 'BSCL.Normativa.BD.Vistas::TRI.DJ1871.Invalidos', 0, 'Q', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=600023);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 600054, 'PLUGIN_NOR.jar@cl.bcs.normativa.declaraciones.DJ1871.AjustarRescatesDelPeriodo', 2, 'J', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=600054);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 600027, 'PLUGIN_NOR.jar@cl.bcs.normativa.declaraciones.DJ1871.ReajustarValorIPC', 3, 'J', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=600027);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 600022, 'BSCL.Normativa.BD.Vistas::TRI.DJ1871.PreDatos', 180, 'Q', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=600022);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 600025, 'BSCL.Normativa.BD.Servicios::TRI.DJ1871.GrabarDetalle', 3, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=600025);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 600026, 'BSCL.Normativa.BD.Servicios::TRI.DJ1871.EliminarDetalles', 4, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=600026);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 600024, 'BSCL.Normativa.BD.Servicios::TRI.DJ1871.ObtenerTotales', 163, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=600024);


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4290706, 'BSCL.Normativa.BD.Servicios::TRI.DJ1871.GrabarMasivo', 1, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4290706);


