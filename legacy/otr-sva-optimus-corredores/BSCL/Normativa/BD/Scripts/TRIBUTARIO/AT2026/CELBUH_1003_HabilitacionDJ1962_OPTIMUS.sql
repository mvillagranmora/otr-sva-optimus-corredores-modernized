SET SCHEMA BSCL;

-- LIMPIEZA

DELETE FROM "BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1962' AND "Version"='2025';
DELETE FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema"='DJ1962' AND "Version"='2025';
DELETE FROM "BSCL.Normativa.BD::DOC.Documento" where "Esquema" = 'DJ1962' AND "Version"='2025';
DELETE FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1962' AND "Version"='2025';
DELETE FROM "BSCL.Normativa.BD::TRI.ControlProceso" WHERE  "Esquema" = 'DJ1962' AND "Version"='2025';
DELETE FROM "BSCL.Normativa.BD::DOC.Diccionario" WHERE "Tipo" IN ('CodigoPais1962','TipoContribuyente1962','CuotasCumplenRequisitos1962','TipoFondo1962','ParticipeCumpleRequisitos1962','TributaSa1962','TipoReajuste1962');
DELETE FROM "BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1962' AND "Version"= '2025';

--- ESQUEMA PARA DJ 1962 AT 2026

INSERT INTO "BSCL.Normativa.BD::DOC.Esquema" ("Esquema","Version","Modulo","Periodicidad","Objetivo","PeriodoInicio",
		     "IdEntidadReguladora","FechaPresentacionLimite","FechaDatosDesde","FechaDatosHasta",
		     "CodigoVersionSII","HabilitadoMantenedor")
VALUES
('DJ1962','2025','TRI','A','Declaracion Jurada Anual sobre informacion de Fondos de Terceros relativa a su identificacion','2025-01-01','SII','2021-01-01','2025-01-01','2025-12-31',26,'S');


-- DOCUMENTO


INSERT INTO "BSCL.Normativa.BD::DOC.Documento"
("IdDocumento","IdTipoDocumento","Esquema","Version","IdEmpresa","FechaLimite","IdEstadoDocumento",
"FechaCreacion","FechaPresentacion","DocumentoIdentidadDeclarante",
"DocumentoIdentidadRepresentante","DireccionElectronicaDeclaracion",
"DireccionFisicaDeclaracion","ComunaDeclaracion","TelefonoDeclaracion",
"FolioDeclaracion","EnProceso","FechaInicioUltimoProceso","AnoMes")
SELECT 
    "BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL AS "IdDocumento",
    'DECLACORR' AS "IdTipoDocumento",
    'DJ1962' AS "Esquema",
    '2025' AS "Version",
    1 AS "IdEmpresa",
    '2025-12-31' AS "FechaLimite",
    'ACT' AS "IdEstadoDocumento",
    '2025-01-01 0:00:00.0' AS "FechaCreacion",
    '2025-12-31 0:00:00.0' AS "FechaPresentacion",
    "EMP"."DocumentoIdentidad" AS "DocumentoIdentidadDeclarante",
    COALESCE("EMP"."DocumentoIdentidadRepresentante","EMP"."DocumentoIdentidad") AS "DocumentoIdentidadRepresentante",
    "DireccionElectronicaPrincipal" AS "DireccionElectronicaDeclaracion",
    RTRIM("DireccionFisicaPrincipalCalle") || ' ' || "DireccionFisicaPrincipalNumero" AS "DireccionFisicaDeclaracion",
    "DireccionFisicaPrincipalNombreDivisionPais" AS "ComunaDeclaracion",
    "TelefonoPrincipal" AS "TelefonoDeclaracion",
    'I9153103' AS "FolioDeclaracion",
    'N' AS "EnProceso",
    '2025-12-09 21:35:09.444' AS "FechaInicioUltimoProceso",
    '' AS "AnoMes"
FROM (
    SELECT TOP 1 * 
    FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa"
    ORDER BY "IdEmpresa"
) AS "EMP"
WHERE NOT EXISTS (
    SELECT 1
    FROM "BSCL.Normativa.BD::DOC.Documento" D
    WHERE D."Esquema" = 'DJ1962'
      AND D."Version" = '2025'
);



-- CAMPOS CLAVE

-- FONDO,RUTCLIENTE

-- CAMPOS DJ 1962
				
-- SECCION B
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'ID', '', 1, 'Secuencia', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'S', 'S', 'N', '', 0, '', '', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'RutAdministradora', '', 2, 'Rut Sociedad Administradora', 'RUT', 0, '2', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS FONDOS DE INVERSION ADMINISTRADOS Y BENEFICIO NETO DETERMINADO AL CIERRE', 1, '', 'C1', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'RutFondoB', '', 3, 'Rut Del Fondo (Sec. B)', 'RUT', 0, '3', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS FONDOS DE INVERSION ADMINISTRADOS Y BENEFICIO NETO DETERMINADO AL CIERRE', 2, '', 'C2', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'RunFondo', '', 4, 'Run Del Fondo Mutuo', 'RUT', 0, '4', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS FONDOS DE INVERSION ADMINISTRADOS Y BENEFICIO NETO DETERMINADO AL CIERRE', 3, '', 'C3', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'TipoFondo', 'TipoFondo1962', 5, 'Tipo De Fondo', 'NEMO', 100, '6', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS FONDOS DE INVERSION ADMINISTRADOS Y BENEFICIO NETO DETERMINADO AL CIERRE', 4, '', 'C4', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'TributaSa', 'TributaSa1962', 6, 'Tributa S.A', 'TEXT', 0, '29', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS FONDOS DE INVERSION ADMINISTRADOS Y BENEFICIO NETO DETERMINADO AL CIERRE', 5, '', 'C5', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'DetBenNetoPerPOS_C6', '', 7, 'Beneficio Neto Percibido (Positivo)', 'AMOUNT', 0, '0', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS FONDOS DE INVERSION ADMINISTRADOS Y BENEFICIO NETO DETERMINADO AL CIERRE', 6, '', 'C6', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'DetBenNetoPerNEG_C7', '', 8, 'Beneficio Neto Percibido (Negativo)', 'AMOUNT', 0, '0', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS FONDOS DE INVERSION ADMINISTRADOS Y BENEFICIO NETO DETERMINADO AL CIERRE', 7, '', 'C7', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'DetBenNetoUtilPer_C8', '', 9, 'Utilidades Percibidas', 'AMOUNT', 0, '0', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS FONDOS DE INVERSION ADMINISTRADOS Y BENEFICIO NETO DETERMINADO AL CIERRE', 8, '', 'C8', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'DetBenNetoIntPer_C9', '', 10, 'Interes Percibidos', 'AMOUNT', 0, '0', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS FONDOS DE INVERSION ADMINISTRADOS Y BENEFICIO NETO DETERMINADO AL CIERRE', 9, '', 'C9', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'DetBenNetoDivPer_C10', '', 11, 'Dividendos Percibidos', 'AMOUNT', 0, '0', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS FONDOS DE INVERSION ADMINISTRADOS Y BENEFICIO NETO DETERMINADO AL CIERRE', 10, '', 'C10', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'DetBenNetoGanCapPer_C11', '', 12, 'Ganancias De Capital Percibidas', 'AMOUNT', 0, '0', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS FONDOS DE INVERSION ADMINISTRADOS Y BENEFICIO NETO DETERMINADO AL CIERRE', 11, '', 'C11', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'DetBenNetoPerGastDev_C12', '', 13, 'Perdidas Y Gastos Devengados', 'AMOUNT', 0, '0', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS FONDOS DE INVERSION ADMINISTRADOS Y BENEFICIO NETO DETERMINADO AL CIERRE', 12, '', 'C12', 0);


-- SECCION C
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'Numero', '', 14, 'Numero', 'ID', 0, '1', 'N', 'N', 'N', 'S', 'S', 'S', 'N', 'N', '2. DETALLE DE SALDOS POR PARTICIPE EN CADA FONDO ADMINISTRADO AL CIERRE DEL EJERCICIO', 13, '', 'C13', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'RutFondoC', '', 15, 'Rut Del Fondo (Sec. C)', 'RUT', 0, '3', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '2. DETALLE DE SALDOS POR PARTICIPE EN CADA FONDO ADMINISTRADO AL CIERRE DEL EJERCICIO', 14, '', 'C14', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'RutCliente', '', 16, 'Rut Del Participe', 'TEXT', 0, '8', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '2. DETALLE DE SALDOS POR PARTICIPE EN CADA FONDO ADMINISTRADO AL CIERRE DEL EJERCICIO', 15, '', 'C15', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'NombreParticipe', '', 17, 'Nombre O Razon Social Del Participe O Aportante', 'TEXT', 250, '7', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. DETALLE DE SALDOS POR PARTICIPE EN CADA FONDO ADMINISTRADO AL CIERRE DEL EJERCICIO', 16, '', 'C16', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'TaxId', '', 18, 'Tax-ID', 'TEXT', 250, '9', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. DETALLE DE SALDOS POR PARTICIPE EN CADA FONDO ADMINISTRADO AL CIERRE DEL EJERCICIO', 17, '', 'C17', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'CodigoPais', 'CodigoPais1962', 19, 'Codigo Pais De Residencia Del Participe O Aportante', 'NEMO', 100, '10', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. DETALLE DE SALDOS POR PARTICIPE EN CADA FONDO ADMINISTRADO AL CIERRE DEL EJERCICIO', 18, '', 'C18', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'MonedaReajuste', 'TipoReajuste1962', 20, 'Moneda O Unidad De Reasjute', 'NEMO', 100, '13', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. DETALLE DE SALDOS POR PARTICIPE EN CADA FONDO ADMINISTRADO AL CIERRE DEL EJERCICIO', 19, '', 'C19', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'MontoAporteNominal', '', 21, 'Monto Nominal De Aportes Al Fondo En  $ ', 'AMOUNT', 0, '14', 'N', 'N', 'N', 'N', 'N', 'S', 'N', 'S', '2. DETALLE DE SALDOS POR PARTICIPE EN CADA FONDO ADMINISTRADO AL CIERRE DEL EJERCICIO', 0, '', '', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'MontoNominalAportesActualizado', '', 22, 'Monto Nominal De Aportes Al Fondo En  $ Actualizado', 'AMOUNT', 0, '14', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. DETALLE DE SALDOS POR PARTICIPE EN CADA FONDO ADMINISTRADO AL CIERRE DEL EJERCICIO', 20, '', 'C20', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'CantidadCuotasParticipe', '', 23, 'Numero De Cuotas De La Serie De Propiedad Del Participe O Aportante', 'AMOUNT', 0, '16', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'S', '2. DETALLE DE SALDOS POR PARTICIPE EN CADA FONDO ADMINISTRADO AL CIERRE DEL EJERCICIO', 21, '', 'C21', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'ParticipeCumpleRequisitos', 'ParticipeCumpleRequisitos1962', 24, 'Participe Cumple Requisitos (Art.82 N1  letra B) iii), Ley Adm.Fondos contenida Art. 1‚ Ley 20.712)', 'ID', 0, '17', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. DETALLE DE SALDOS POR PARTICIPE EN CADA FONDO ADMINISTRADO AL CIERRE DEL EJERCICIO', 22, '', 'C22', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'CuotasCumplenRequisitos', 'CuotasCumplenRequisitos1962', 25, 'Cuotas De Fondos Que Cumplen Requisitos Art.82', 'ID', 0, '18', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. DETALLE DE SALDOS POR PARTICIPE EN CADA FONDO ADMINISTRADO AL CIERRE DEL EJERCICIO', 23, '', 'C23', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'ValorCuotaPesos', '', 26, 'Valor De La Cuota ($)', 'DECIMAL', 0, '19', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. DETALLE DE SALDOS POR PARTICIPE EN CADA FONDO ADMINISTRADO AL CIERRE DEL EJERCICIO', 24, '', 'C24', 6);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'PorcentajeParticipacion31Dic', '', 27, '% De Participacion En El Patrimonio Del Fondo AL 31.12', 'DECIMAL', 0, '21', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. DETALLE DE SALDOS POR PARTICIPE EN CADA FONDO ADMINISTRADO AL CIERRE DEL EJERCICIO', 25, '', 'C25', 6);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'TipoContribuyente', 'TipoContribuyente1962', 28, 'Tipo Contribuyente', 'TEXT', 1, '2', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. DETALLE DE SALDOS POR PARTICIPE EN CADA FONDO ADMINISTRADO AL CIERRE DEL EJERCICIO', 26, '', 'C27', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'FechaRespaldo', '', 39, 'Fecha Respaldo Custodia', 'DATE_FILTER', 0, '12', 'N', 'N', 'N', 'S', 'N', 'S', 'N', 'N', '5. DATOS DE CONTROL', 27, 'Fecha Respaldo Custodia', '', 0);


-- DATOS DE CONTROL

INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'IdDocumento', '', 40, 'Secuencia Docto', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'S', 'N', 'N', '5. DATOS DE CONTROL', 0, 'Ayuda de Secuencia Docto', '', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'VboUsuario', '', 41, 'VoBo', 'TEXT', 1, '0', 'N', 'N', 'N', 'N', 'N', 'S', 'N', 'N', '5. DATOS DE CONTROL', 0, 'Ayuda de VoBo', '', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'FuenteDatos', '', 42, 'Fuente', 'TEXT', 0, '0', 'N', 'N', 'N', 'N', 'N', 'S', 'N', 'N', '5. DATOS DE CONTROL', 0, 'Ayuda de Fuente', '', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'IdCliente', '', 43, 'Secuencia Cliente', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'S', 'N', 'N', '5. DATOS DE CONTROL', 0, 'Ayuda de Secuencia Cliente', '', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'IdOperacion', '', 44, 'Id Operacion', 'ID', 0, '67', 'N', 'N', 'N', 'N', 'N', 'S', 'N', 'N', '5. DATOS DE CONTROL', 0, '', '', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'IdPortafolio', '', 45, 'Secuencia Portafolio', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'S', 'N', 'N', '5. DATOS DE CONTROL', 0, 'Ayuda de Secuencia Portafolio', '', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'IdEmisor', '', 46, 'Id Emisor', 'NEMO', 0, '0', 'N', 'N', 'N', 'N', 'N', 'S', 'N', 'N', '5. DATOS DE CONTROL', 0, 'Emisor Del Fondo', '', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'Fondo', '', 47, 'Fondo', 'TEXT', 100, '5', 'N', 'S', 'N', 'N', 'N', 'N', 'N', 'S', '5. DATOS DE CONTROL', 5, 'Fondo En Custodia', '', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'FolioCartera', '', 48, 'Folio Cartera', 'ID', 250, '0', 'N', 'N', 'N', 'N', 'N', 'S', 'N', 'N', '5. DATOS DE CONTROL', 0, 'Ayuda Folio Cartera', '', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'FechaOperacion', '', 49, 'Fecha Aporte Nominal', 'DATE', 0, '0', 'N', 'N', 'N', 'N', 'N', 'S', 'N', 'N', '5. DATOS DE CONTROL', 0, 'Ayuda Fecha Operacion', '', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1962', '2025', 'Observaciones', '', 50, 'Observaciones', 'TEXT', 250, '0', 'N', 'N', 'N', 'N', 'N', 'S', 'S', 'N', '5. DATOS DE CONTROL', 0, 'Ayuda de Observaciones', '', 0);





























-- DICCIONARIOS --

SELECT * FROM "BSCL.Normativa.BD::DOC.Diccionario" WHERE "Tipo"='TipoContribuyente1962'
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TipoContribuyente1962', 1, 2, 'Sociedad Administradora De Fondos', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TipoContribuyente1962', 2, 1, 'Corredor de Bolsa o Intermediario', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TipoContribuyente1962', 3, 3, 'Banco', 'S', '', '');


INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CuotasCumplenRequisitos1962', '1', 2, 'Cuotas cumplen requisitos Art. 82 ', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CuotasCumplenRequisitos1962', '2', 3, 'Cuotas NO cumplen requisitos Art. 82 ', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CuotasCumplenRequisitos1962', '3', 1, 'No Aplica', 'S', '', '');


INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TipoFondo1962', '', 1, '', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TipoFondo1962', 'I', 2, 'Fondos de Inversion Publicos', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TipoFondo1962', 'F', 3, 'Fondos Mutuos', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TipoFondo1962', 'P', 4, 'Fondos de Inversion Privados', 'S', '', '');

INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('ParticipeCumpleRequisitos1962', '1', 1, 'No tiene extranjero domiciliado con %5 participacion', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('ParticipeCumpleRequisitos1962', '2', 2, 'Si tiene extranjero domiciliado con %5 participacion', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('ParticipeCumpleRequisitos1962', '3', 3, 'No Aplica', 'S', '', '');

INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'AF', 2, 'Afganistan', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'AL', 2, 'Albania', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'DE', 3, 'Alemania', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'AD', 4, 'Andorra', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'AO', 5, 'Angola', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'AI', 6, 'Anguila', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'AQ', 7, 'Antartida', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'AG', 8, 'Antigua y Barbuda', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'SA', 9, 'Arabia Saudita', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'DZ', 10, 'Argelia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'AR', 11, 'Argentina', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'AM', 12, 'Armenia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'AW', 13, 'Aruba', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'AU', 14, 'Australia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'AT', 15, 'Austria', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'AZ', 16, 'Azerbaiyan', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'BS', 17, 'Bahamas', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'BH', 18, 'Bahrein', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'BD', 19, 'Bangladesh', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'BB', 20, 'Barbados', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'BE', 21, 'Belgica', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'BZ', 22, 'Belice', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'BJ', 23, 'Benan', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'BM', 24, 'Bermudas', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'BY', 25, 'Bielorrusia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'BO', 26, 'Bolivia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'BQ', 27, 'Bonaire, San Eustaquio y Saba', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'BA', 28, 'Bosnia y Herzegovina', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'BW', 29, 'Botswana', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'BR', 30, 'Brasil', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'BN', 31, 'Brunei Darussalam', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'BG', 32, 'Bulgaria', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'BF', 33, 'Burkina Faso', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'BI', 34, 'Burundi', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'BT', 35, 'Butan', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'CV', 36, 'Cabo Verde', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'KH', 37, 'Camboya', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'CM', 38, 'Camerun', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'CA', 39, 'Canada', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'TD', 40, 'Chad', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'CL', 1, 'Chile', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'CN', 42, 'China', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'CY', 43, 'Chipre', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'VA', 44, 'Ciudad del Vaticano', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'CO', 45, 'Colombia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'KM', 46, 'Comoras', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'CG', 47, 'Congo', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'KP', 48, 'Corea del Norte', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'KR', 49, 'Corea del Sur', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'CI', 50, 'Costa de Marfil', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'CR', 51, 'Costa Rica', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'HR', 52, 'Croacia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'CU', 53, 'Cuba', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'CW', 54, 'Curazao', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'DK', 55, 'Dinamarca', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'DM', 56, 'Dominica', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'EC', 57, 'Ecuador', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'EG', 58, 'Egipto', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'SV', 59, 'El Salvador', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'AE', 60, 'Emiratos �?rabes Unidos', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'ER', 61, 'Eritrea', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'GB', 62, 'Escocia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'SK', 63, 'Eslovaquia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'SI', 64, 'Eslovenia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'ES', 65, 'Espana', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'US', 66, 'Estados Unidos', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'EE', 67, 'Estonia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'ET', 68, 'Etiopia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'RU', 69, 'Federacion Rusa (Rusia)', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'PH', 70, 'Filipinas', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'FI', 71, 'Finlandia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'FJ', 72, 'Fiyi', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'FR', 73, 'Francia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'GA', 74, 'Gabon', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'GM', 76, 'Gambia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'GE', 77, 'Georgia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'GH', 78, 'Ghana', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'GI', 79, 'Gibraltar', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'GD', 80, 'Granada', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'GR', 81, 'Grecia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'GL', 82, 'Groenlandia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'GP', 83, 'Guadalupe (Francia)', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'GU', 84, 'Guam (EE.UU.)', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'GT', 85, 'Guatemala', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'GF', 86, 'Guayana Francesa', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'GG', 87, 'Guernsey', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'GN', 88, 'Guinea', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'GW', 89, 'Guinea Bissau', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'GQ', 90, 'Guinea Ecuatorial', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'GY', 91, 'Guyana', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'HT', 92, 'Haiti', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'NL', 93, 'Holanda', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'HN', 94, 'Honduras', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'HK', 95, 'Hong Kong', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'HU', 96, 'Hungria', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'IN', 97, 'India', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'ID', 98, 'Indonesia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'IR', 100, 'Iran', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'IQ', 101, 'Iraq', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'IE', 102, 'Irlanda', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'BV', 103, 'Isla Bouvet', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'IM', 104, 'Isla de Man', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'CX', 105, 'Isla de Navidad', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'MF', 106, 'Isla de San Martin (parte francesa)', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'SX', 107, 'Isla de San Martin (parte holandesa)', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'GS', 108, 'Isla Georgia del Sur e Islas Sandwich del Sur', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'NF', 109, 'Isla Norfolk', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'PN', 110, 'Isla Pitcairn', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'IS', 111, 'Islandia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'AX', 112, 'Islas Aland', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'KY', 113, 'Islas Caiman', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'CC', 114, 'Islas Cocos', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'CK', 115, 'Islas Cook', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'FK', 116, 'Islas Falkland (Malvinas)', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'FO', 117, 'Islas Faroe', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'HM', 118, 'Islas Heard y McDonald', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'MP', 119, 'Islas Marianas del Norte', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'MH', 120, 'Islas Marshall', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'UM', 121, 'Islas Menores de Estados Unidos de Ultramar', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'SB', 122, 'Islas Salomon', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'SJ', 123, 'Islas Svalbard y Jan Mayen', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'TC', 124, 'Islas Turcas y Caicos', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'VI', 125, 'Islas Virgenes Americanas', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'VG', 126, 'Islas Virgenes Britanicas', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'WF', 127, 'Islas Wallis y Futuna', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'IL', 128, 'Israel', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'IT', 129, 'Italia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'JM', 130, 'Jamaica', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'JP', 131, 'Japon', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'JE', 132, 'Jersey', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'JO', 133, 'Jordania', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'KZ', 134, 'Kazajistan', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'KE', 135, 'Kenia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'KG', 136, 'Kirguistan', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'KI', 137, 'Kiribati', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'KW', 138, 'Kuwait', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'LA', 139, 'Laos', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'LS', 140, 'Lesoto', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'LV', 141, 'Letonia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'LB', 142, 'Libano', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'LR', 143, 'Liberia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'LY', 144, 'Libia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'LI', 145, 'Liechtenstein', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'LT', 146, 'Lituania', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'LU', 147, 'Luxemburgo', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'MO', 148, 'Macao', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'MK', 149, 'Macedonia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'MG', 150, 'Madagascar', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'MY', 151, 'Malasia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'MW', 152, 'Malawi', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'MV', 153, 'Maldivas', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'ML', 154, 'Mali', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'MT', 155, 'Malta', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'MA', 156, 'Marruecos', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'MQ', 157, 'Martinica', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'MU', 158, 'Mauricio', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'MR', 159, 'Mauritania', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'YT', 160, 'Mayotte', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'MX', 161, 'Mexico', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'FM', 162, 'Micronesia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'MD', 163, 'Moldavia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'MC', 164, 'Monaco', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'MN', 165, 'Mongolia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'ME', 166, 'Montenegro', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'MS', 167, 'Montserrat', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'MZ', 168, 'Mozambique', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'MM', 169, 'Myanmar (Birmania)', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'NA', 170, 'Namibia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'NR', 171, 'Nauru', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'NP', 172, 'Nepal', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'NI', 173, 'Nicaragua', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'NE', 174, 'Niger', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'NG', 175, 'Nigeria', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'NU', 176, 'Niue', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'NO', 177, 'Noruega', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'NC', 178, 'Nueva Caledonia (Francia)', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'NZ', 179, 'Nueva Zelanda', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'OM', 180, 'Oman', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'PK', 181, 'Pakistan', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'PW', 182, 'Palaos', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'PS', 183, 'Palestina', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'PA', 184, 'Panama', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'PG', 185, 'Papua Nueva Guinea', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'PY', 186, 'Paraguay', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'PE', 187, 'Peru', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'PF', 188, 'Polinesia (Francia)', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'PL', 189, 'Polonia (std LE)', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'PT', 190, 'Portugal', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'PR', 191, 'Puerto Rico', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'QA', 192, 'Qatar', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'CF', 194, 'Republica Centro Africana', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'CZ', 195, 'Republica Checa', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'CD', 196, 'Republica Democratica del Congo', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'DO', 197, 'Republica Dominicana', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'RE', 198, 'Reunion (Francia)', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'RW', 199, 'Ruanda', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'RO', 200, 'Rumania', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'EH', 201, 'Sahara Occidental', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'WS', 202, 'Samoa', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'AS', 203, 'Samoa Americana', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'BL', 204, 'San Bartolome', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'KN', 205, 'San Cristobal y Nieves', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'SM', 206, 'San Marino', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'PM', 207, 'San Pedro y Miguelon', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'VC', 208, 'San Vicente y las Granadinas', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'SH', 209, 'Santa Elena, Ascension y Tristan de Acuna', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'LC', 210, 'Santa Lucia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'ST', 211, 'Santo Tome y Principe', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'SN', 212, 'Senegal', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'YU', 213, 'Serbia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'SC', 214, 'Seychelles', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'SL', 215, 'Sierra Leona', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'SG', 216, 'Singapur', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'SY', 217, 'Siria', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'SO', 218, 'Somalia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'LK', 219, 'Sri Lanka', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'SZ', 220, 'Suazilandia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'ZA', 221, 'Sudafrica', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'SD', 222, 'Sudan', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'SS', 223, 'Sudan del Sur', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'SE', 224, 'Suecia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'CH', 225, 'Suiza', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'SR', 226, 'Surinam', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'TH', 227, 'Tailandia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'TW', 228, 'Taiwan', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'TZ', 229, 'Tanzania', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'TJ', 230, 'Tayikistan', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'IO', 231, 'Territorio Britanico del Oceano Indico', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'TF', 232, 'Territorios Australes Franceses', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'TL', 233, 'Timor Oriental', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'TG', 234, 'Togo', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'TK', 235, 'Tokelau', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'TO', 236, 'Tonga', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'TT', 237, 'Trinidad y Tobago', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'TN', 238, 'Tunez', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'TM', 239, 'Turkmenistan', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'TR', 240, 'Turquia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'TV', 241, 'Tuvalu', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'UA', 242, 'Ucrania', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'UG', 243, 'Uganda', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'UY', 244, 'Uruguay', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'UZ', 245, 'Uzbekistan', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'VU', 246, 'Vanuatu', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'VE', 247, 'Venezuela', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'VN', 248, 'Vietnam', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'YE', 249, 'Yemen', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'DJ', 250, 'Yibouti', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'ZR', 251, 'Zaire', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'ZM', 252, 'Zambia', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'ZW', 253, 'Zimbabwe', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1962', 'NT', 254, 'Zona neutral', 'S', '', '');

INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TipoReajuste1962', '1', 1, 'Peso Chileno', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TipoReajuste1962', '2', 2, 'Dolar Americano', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TipoReajuste1962', '3', 3, 'Euro', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TipoReajuste1962', '4', 4, 'Unidad de Fomento', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TipoReajuste1962', '5', 5, 'IVP', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TipoReajuste1962', '6', 6, 'Otro', 'S', '', '');

INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TributaSa1962', '0', 1, '', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TributaSa1962', '1', 2, 'SI', 'S', '', '');
INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TributaSa1962', '2', 3, 'NO', 'S', '', '');

------- HOMOLOGACIONES -------

insert into "BSCL.Normativa.BD::DOC.Homologacion"("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") select "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1962','2025','UNIDADMONEDA','PESO CHILENO','CLP','1','ACT' from dummy;
insert into "BSCL.Normativa.BD::DOC.Homologacion"("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") select "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1962','2025','UNIDADMONEDA','US DOLAR','USD','2','ACT' from dummy;
insert into "BSCL.Normativa.BD::DOC.Homologacion"("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") select "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1962','2025','UNIDADMONEDA','UNIDAD DE FOMENTO','UF','4','ACT' from dummy;
insert into "BSCL.Normativa.BD::DOC.Homologacion"("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") select "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1962','2025','UNIDADMONEDA','EURO','EUR','3','ACT' from dummy;
insert into "BSCL.Normativa.BD::DOC.Homologacion"("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") select "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1962','2025','UNIDADMONEDA','INDICE VALOR PROMEDIO','IVP','5','ACT' from dummy;




------- SERVICIOS ---------


INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4400050, 'BSCL.Normativa.BD.Vistas::TRI.DJ1962.PreDatos', 0, 'Q', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4400050);

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4400051, 'BSCL.Normativa.BD.Vistas::TRI.DJ1962.Datos', 0, 'Q', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4400051);

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4400052, 'BSCL.Normativa.BD.Servicios::TRI.DJ1962.EliminarDetalles', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4400052);

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4400053, 'BSCL.Normativa.BD.Servicios::TRI.DJ1962.ObtenerTotales', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4400053);

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4400054, 'BSCL.Normativa.BD.Servicios::TRI.DJ1962.GrabarDetalle', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4400054);

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4400055, 'BSCL.Normativa.BD.Vistas::TRI.DJ1962.Invalidos', 0, 'Q', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4400055);




-- FLUJO PROCESO


INSERT INTO "BSCL.Normativa.BD::TRI.ControlProceso"
("Esquema", "Version", "EstadoCertificado", "Carga", "Validacion", "PreDeclaracion", "Certificacion")
VALUES('DJ1962', '2025', 'NGE', 'N', 'N', 'N', 'N');


-- VALIDACIONES



INSERT INTO "BSCL.Normativa.BD::DOC.Validacion"
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES('DJ1962','2025','H3214','NORMA','S','La columna RUT Administradora debe ser distinto a la columna RUT del Fondo','E','[C1] <> [C2]');

INSERT INTO "BSCL.Normativa.BD::DOC.Validacion"
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES('DJ1962','2025','H3215','NORMA','S','Si beneficio neto percibido negativo > 0, entonces el beneficio neto positivo debe ser blanco, vacio o cero','E','Si [C7] > 0, entonces [C6] = 0 .o. blanco .o. vacio');

INSERT INTO "BSCL.Normativa.BD::DOC.Validacion"
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES('DJ1962','2025','H3216','NORMA','S','Si beneficio neto percibido positivo > 0, entonces el beneficio neto negativo debe ser blanco, vacio o cero','E','Si [C6] > 0, entonces [C7] = 0 .o. blanco .o. vacio');

INSERT INTO "BSCL.Normativa.BD::DOC.Validacion"
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES('DJ1962','2025','H3217','NORMA','S','Si beneficio neto percibido positivo o negativo tiene valor mayor a cero, entonces la suma de utilidades, intereses, dividendos, ganancias de capital y perdidas o gastos devengados debe ser mayor a cero','E','Si [C6] + [C7] > 0 entonces [C8] + [C9] + [C10] + [C11] + [C12] > 0');

INSERT INTO "BSCL.Normativa.BD::DOC.Validacion"
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES('DJ1962','2025','H3218','NORMA','N','Si el declarante es Sociedad Administradora de Fondos de Inversion, todo RUT de la seccion B unico debe estar informado en la seccion C','E','Si [C27] = 1, todo [C2] unico debe estar en [C14]');

INSERT INTO "BSCL.Normativa.BD::DOC.Validacion"
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES('DJ1962','2025','H3219','NORMA','N','Si el declarante es Sociedad Administradora de Fondos de Inversion Privado, Publico o Mutuo, se debe cumplir que para todo RUT Fondo de la seccion C unico, debe estar informado en la seccion B','E','Si [C27] = 1, todo [C14] unico debe estar en [C2]');

INSERT INTO "BSCL.Normativa.BD::DOC.Validacion"
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES('DJ1962','2025','H3220','NORMA','S','Si Monto Beneficio Neto Negativo es mayor a cero, debe ser igual al valor absoluto de la suma de Utilidades, Intereses, Dividendos y Ganancias de Capital menos Perdidas o Gastos Devengados, con tolerancia','E','Si [C7] > 0, entonces [C7] = valor absoluto ([C8] + [C9] + [C10] + [C11] - [C12]) +/- 5000000');

INSERT INTO "BSCL.Normativa.BD::DOC.Validacion"
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES('DJ1962','2025','H3221','NORMA','S','Si Monto Beneficio Neto Positivo es mayor a cero, debe ser igual a la suma de Utilidades, Intereses, Dividendos y Ganancias de Capital menos Perdidas o Gastos Devengados, con tolerancia','E','Si [C6] > 0, entonces [C6] = ([C8] + [C9] + [C10] + [C11] - [C12]) +/- 5000000');

INSERT INTO "BSCL.Normativa.BD::DOC.Validacion"
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES('DJ1962','2025','H3222','NORMA','N','El campo total registros debe ser igual a la cantidad de registros informados en la seccion C','E','[C26] = Contar([C14])');

INSERT INTO "BSCL.Normativa.BD::DOC.Validacion"
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES('DJ1962','2025','H3223','NORMA','S','El campo "Tax ID" no debe contener como valor 55.555.555-5','E','[C17] <> 55555555-5');

INSERT INTO "BSCL.Normativa.BD::DOC.Validacion"
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES('DJ1962','2025','H3224','NORMA','S','El campo Codigo Pais de Residencia del Participe o Aportante debe corresponder al listado de paises del suplemento, excepto RFE','E','[C18] = tabla paises SII .y. [C18] <> RFE');

INSERT INTO "BSCL.Normativa.BD::DOC.Validacion"
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES('DJ1962','2025','H3251','NORMA','S','El campo "Moneda o Unidad de Reajuste" debe tomar valor 1, 2, 3, 4, 5 o 6','E','[C19] = 1 .o. [C19] = 2 .o. [C19] = 3 .o. [C19] = 4 .o. [C19] = 5 .o. [C19] = 6');

INSERT INTO "BSCL.Normativa.BD::DOC.Validacion"
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES('DJ1962','2025','H3252','NORMA','S','El campo "Numero de cuotas de la serie de propiedad del aportante o participe " debe tener un valor mayor o igual a cero, con 6 decimales','E','[C21] >= 0 con 6 decimales');

INSERT INTO "BSCL.Normativa.BD::DOC.Validacion"
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES('DJ1962','2025','H3253','NORMA','S','El campo  "% de participacion en el patrimonio del fondo al cierre del ejercicio"  debe ser un valor positivo y hasta 100 como valor maximo, usando como maximo dos decimales.','E','[C25] >= 0 .y. [C25] <= 100 con maximo 6 decimales');

INSERT INTO "BSCL.Normativa.BD::DOC.Validacion"
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES('DJ1962','2025','H3254','NORMA','S','El campo "Cuotas de Fondos que cumplen requisitos (literal iii), de la letra B ), del art. 82 de la Ley N° 20.712)" debe tomar valor 1, 2 o 3','E','[C23] = 1 .o. [C23] = 2 .o. [C23] = 3');

INSERT INTO "BSCL.Normativa.BD::DOC.Validacion"
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES('DJ1962','2025','H3255','NORMA','S','El campo Tipo de Fondo debe ser I: Fondos de Inversion, F: Fondos Mutuos, P: Fondos de Inversion Privados','E','[C4] = I .o. [C4] = F .o. [C4] = P');

INSERT INTO "BSCL.Normativa.BD::DOC.Validacion"
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES('DJ1962','2025','H3256','NORMA','S','El campo Tributa como S.A. debe tomar valor 1 o 2','E','[C5] = 1 .o. [C5] = 2');



INSERT INTO "BSCL.Normativa.BD::DOC.Validacion"
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES('DJ1962','2025','H3257','NORMA','S','Las columnas "Beneficio Neto Percibido Positivo", "Beneficio Neto Percibido Negativo", "Utilidades Percibidas", "Intereses Percibidos", "Dividendos Percibidos", "Ganancias de Capital Percibidos" y "Perdidas o Gastos Devengados" no pueden ser negativos ','E','[C6] >= 0 .y. [C7] >= 0 .y. [C8] >= 0 .y. [C9] >= 0 .y. [C10] >= 0 .y. [C11] >= 0 .y. [C12] >= 0 .o. blanco');

INSERT INTO "BSCL.Normativa.BD::DOC.Validacion"
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES('DJ1962','2025','H3258','NORMA','S','El campo "Tipo Contribuyente." debe tomar valor 1, 2 o 3.','E','[C27] = 1 .o. [C27] = 2 .o. [C27] = 3');

INSERT INTO "BSCL.Normativa.BD::DOC.Validacion"
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES('DJ1962','2025','H3259','NORMA','S','El campo Rut de Sociedad Administradora (C1) es campo obligatorio, debe cumplir con modulo 11 y mayor a 50.000.000','E','[C1] <> 0 .y. [C1] <> blanco .o. vacio .y. [C1] cumple modulo 11');

INSERT INTO "BSCL.Normativa.BD::DOC.Validacion"
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES('DJ1962','2025','H3260','NORMA','S','RUT del participe o aportante  C15 es campo obligatorio, debe complir con modulo 11','E','[C15] <> blanco .o. vacio .y. [C15] cumple modulo 11');

INSERT INTO "BSCL.Normativa.BD::DOC.Validacion"
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES('DJ1962','2025','H3261','NORMA','S','RUT del Fondo no debe ser blanco y debe cumplir modulo 11 y mayor a 50.000.000','E','[C14] <> blanco .o. vacio .y. [C14] cumple modulo 11');


INSERT INTO "BSCL.Normativa.BD::DOC.Parametro" ("Esquema", "Version", "Codigo", "Descripcion", "Valor", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones", "IdEmpresa") 
SELECT 'DJ1962', '2025', 'LimiteRutH3259', 'Para validacion del RUT donde debe ser mayor o igual que el valor definido', '50000000', 'N', ' ', ' ', ' ', ' ', ' ', ' ', '0001-01-01 00:00:00.000', ' ', '0001-01-01 00:00:00.000', ' ', NULL
FROM DUMMY
WHERE NOT EXISTS(SELECT 1 FROM "BSCL.Normativa.BD::DOC.Parametro" WHERE "Esquema" = 'DJ1962' AND "Version" = '2025' AND "Codigo" = 'LimiteRutH3259');

INSERT INTO "BSCL.Normativa.BD::DOC.Parametro" ("Esquema", "Version", "Codigo", "Descripcion", "Valor", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones", "IdEmpresa") 
SELECT 'DJ1962', '2025', 'LimiteRutH3261', 'Para validacion del RUT donde debe ser mayor o igual que el valor definido', '50000000', 'N', ' ', ' ', ' ', ' ', ' ', ' ', '0001-01-01 00:00:00.000', ' ', '0001-01-01 00:00:00.000', ' ', NULL
FROM DUMMY
WHERE NOT EXISTS(SELECT 1 FROM "BSCL.Normativa.BD::DOC.Parametro" WHERE "Esquema" = 'DJ1962' AND "Version" = '2025' AND "Codigo" = 'LimiteRutH3261');

COMMIT;


