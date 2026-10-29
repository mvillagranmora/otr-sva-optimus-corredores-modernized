-- Marcelo Rojas
-- CELBUHOPRO-1096
-- Script para insertar Diccionario
--------INSERT SERVICIO--------------------
SET SCHEMA "BSCL";

    INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 101010106 , 'BSCL.Custodia.BD.Vistas::CUS.ConsultarHomologacionCMF', 0, 'Q', 'Obtiene Paises', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=101010106);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 101010105, 'PLUGIN_NOR.jar@cl.bcs.normativa.ModificarNCG499', 0, 'J', 'Modificar Circular NCG499', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=101010105);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 101010104, 'BSCL.Custodia.BD.Vistas::CUS.CircularCarteraCMF', 0, 'Q', 'Obtiene CarteraCMF', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=101010104);

--------INSERT TIPOPROGRAMA------------
INSERT INTO BSCL."BSCL.Plataforma.BD::BAT.TipoPrograma"
("IdTipoPrograma", "Descripcion", "MaximoIntentos", "Modulo", "NombreClasePrograma", "TipoClasePrograma", "Nodo", "EsPauta", "TipoSalida", "FormatoSalida", "TieneLog")
SELECT 'EXPORTACIONCIRCMF', 'Circular CMF', 1, 'Normativa', 'cl.bcs.normativa.jobs.BcsJobExportacionServ', 'JAVA', '', 'N', 'P', '', 'N'
FROM DUMMY
WHERE NOT EXISTS (  SELECT 1 FROM BSCL."BSCL.Plataforma.BD::BAT.TipoPrograma"  WHERE "IdTipoPrograma"='EXPORTACIONCIRCMF');


------INSERT ESQUEMA-----------------
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Esquema"
("Esquema", "Version", "Periodicidad", "Objetivo", "PeriodoInicio", "IdEntidadReguladora", "FechaPresentacionLimite", "FechaDatosDesde", "FechaDatosHasta", "CodigoVersionSII", "Modulo", "HabilitadoMantenedor", "TieneCertificado")
SELECT 'CIRCMF', 'V1', 'D', '', '', 'BCS', '2016-01-10', '2016-01-10', '9999-01-01', 1, 'NOR', 'N', 'N'
FROM DUMMY WHERE NOT EXISTS ( SELECT 1 FROM BSCL."BSCL.Normativa.BD::DOC.Esquema" WHERE "Esquema"='CIRCMF');

--------INSERT TIPODOCUMENTO--------------------
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.TipoDocumento"
("IdTipoDocumento", "Descripcion", "IdTipoProgramaExportacion", "IdNormativa", "Esquema", "UI_Visor", "IdTipoProgramaProceso", "UI_Emision", "TitulosSalida", "ExtensionSalida", "ComprimirSalida", "LineasPorSalida", "IdReporte")
SELECT 'CIRCMF', 'Solicitar Circular Cartera CMF', 'EXPORTACIONCIRCMF', 'CIRCMF', 'CIRCMF', 'N', '', 'S', 'S', 'txt', 'N', 0, ''
FROM DUMMY
WHERE NOT EXISTS ( SELECT 1  FROM BSCL."BSCL.Normativa.BD::DOC.TipoDocumento"   WHERE "IdTipoDocumento"='CIRCMF');

-------- INSERT NORMATIVA---------------------
INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Normativa"
("IdNormativa", "NombreNormativa", "IdEstado", "MetodoRelacionado", "EntidadDestino", "NombreDestinatario", "CargoDestinatario", "UI_Emision", "PermiteSolicitar")
SELECT 'CIRCMF', 'Circular CarteraCMF', 'ACT', 'CLASIFICACION', 'SVS', '', '', 'S', 'S'
FROM DUMMY
WHERE NOT EXISTS ( SELECT 1  FROM BSCL."BSCL.Normativa.BD::NOR.Normativa"WHERE "IdNormativa"='CIRCMF');


---------INSERT DOCUMENTOUI---------
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.TipoDocumentoUI" ("IdTipoDocumento", UI, "IdEstado")
SELECT 'CIRCMF', 'EDICION', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1 FROM BSCL."BSCL.Normativa.BD::DOC.TipoDocumentoUI"
    WHERE "IdTipoDocumento" = 'CIRCMF' AND UI = 'EDICION' AND "IdEstado" = 'ACT'
);

--INICIO INSERT ARCHIVO DE SALIDA-------------

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
SELECT'CIRCMF', 'V1', 'Disponible2', '', 1, 'Disponible1', 'TEXT', 1, '', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '', 0, 'Ayuda de Disponible1', '', 0
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Campo"
    WHERE "Esquema"='CIRCMF' AND "Version"='V1' AND "Nombre"='Disponible2'
);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
SELECT 'CIRCMF', 'V1', 'Disponible3', '', 2, 'Disponible1', 'TEXT', 1, '', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '', 0, 'Ayuda de Disponible1', '', 0
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Campo"
    WHERE "Esquema"='CIRCMF' AND "Version"='V1' AND "Nombre"='Disponible3'
);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
SELECT'CIRCMF', 'V1', 'Digito', '', 8, 'Digito', 'TEXT', 1, '', 'N', 'S', 'S', 'S', 'S', 'N', 'N', 'N', '', 0, 'ayuda Digito', '', 0
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Campo"
    WHERE "Esquema"='CIRCMF' AND "Version"='V1' AND "Nombre"='Digito'
);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
SELECT'CIRCMF', 'V1', 'RutCliente', '', 7, 'RutCliente ', 'TEXT', 9, '', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '', 0, 'Ayuda de RutCliente', '', 0
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Campo"
    WHERE "Esquema"='CIRCMF' AND "Version"='V1' AND "Nombre"='RutCliente'
);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
SELECT'CIRCMF', 'V1', 'Nemotecnico', '', 4, 'Nemo', 'TEXT', 50, '', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '', 0, 'Ayuda de Nemotecnico', '', 0
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Campo"
    WHERE "Esquema"='CIRCMF' AND "Version"='V1' AND "Nombre"='Nemotecnico'
);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
SELECT'CIRCMF', 'V1', 'ISIN', '', 5, 'ISIN O CUSIP', 'TEXT', 12, '', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 'Ayuda de ISIN', '', 0
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Campo"
    WHERE "Esquema"='CIRCMF' AND "Version"='V1' AND "Nombre"='ISIN'
);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
SELECT'CIRCMF', 'V1', 'SituacionInstrumento', '', 6, 'SituacionInstrumento', 'TEXT', 2, '', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 'Ayuda de SituacionInstrumento', '', 0
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Campo"
    WHERE "Esquema"='CIRCMF' AND "Version"='V1' AND "Nombre"='SituacionInstrumento'
);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
SELECT'CIRCMF', 'V1', 'Fecha', '', 9, 'Periodo', 'TEXT', 10, '', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 'Ayuda de Periodo', '', 0
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Campo"
    WHERE "Esquema"='CIRCMF' AND "Version"='V1' AND "Nombre"='Fecha'
);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
SELECT'CIRCMF', 'V1', 'Disponible1', '', 28, 'Disponible1', 'TEXT', 328, '', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '', 0, 'Ayuda de Disponible1', '', 0
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Campo"
    WHERE "Esquema"='CIRCMF' AND "Version"='V1' AND "Nombre"='Disponible1'
);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
SELECT'CIRCMF', 'V1', 'TotalRegistros', '', 27, 'TotalRegistros', 'TEXT', 10, '', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '', 0, 'Ayuda de Disponible2', '', 0
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Campo"
    WHERE "Esquema"='CIRCMF' AND "Version"='V1' AND "Nombre"='TotalRegistros'
);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
SELECT'CIRCMF', 'V1', 'Disponible4', '', 26, 'Disponible1', 'TEXT', 1, '', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '', 0, 'Ayuda de Disponible1', '', 0
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Campo"
    WHERE "Esquema"='CIRCMF' AND "Version"='V1' AND "Nombre"='Disponible4'
);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
SELECT'CIRCMF', 'V1', 'NombrePersona', '', 10, 'RazonSocial', 'TEXT', 100, '', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '', 0, 'ayuda NombrePersona', '', 0
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Campo"
    WHERE "Esquema"='CIRCMF' AND "Version"='V1' AND "Nombre"='NombrePersona'
);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
SELECT'CIRCMF', 'V1', 'CodigoPais', '', 11, 'CODIGO PAIS', 'TEXT', 2, '', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 'Ayuda CodigoPais', '', 0
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Campo"
    WHERE "Esquema"='CIRCMF' AND "Version"='V1' AND "Nombre"='CodigoPais'
);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
SELECT'CIRCMF', 'V1', 'IdTipoInstrumento', '', 12, 'TIPOINSTRUMENTO', 'TEXT', 10, '', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 'Ayuda TipoInstrumento', '', 0
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Campo"
    WHERE "Esquema"='CIRCMF' AND "Version"='V1' AND "Nombre"='IdTipoInstrumento'
);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
SELECT 'CIRCMF', 'V1', 'FechaVencimiento', '', 13, 'FechaVencimiento', 'TEXT', 10, '', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 'Periodo', '', 8
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Campo"
    WHERE "Esquema"='CIRCMF' AND "Version"='V1' AND "Nombre"='FechaVencimiento'
);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
SELECT'CIRCMF', 'V1', 'ClasificacionRiesgoInstrumento', '', 14, 'ClasificacionRiesgoInstrumento', 'TEXT', 3, '', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 'Periodo', '', 8
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Campo"
    WHERE "Esquema"='CIRCMF' AND "Version"='V1' AND "Nombre"='ClasificacionRiesgoInstrumento'
);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
SELECT'CIRCMF', 'V1', 'TendenciaClasificacionriesgo', '', 15, 'TendenciaClasificacionriesgo', 'TEXT', 50, '', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 'Ayuda de TendenciaClasificacionriesgo', '', 8
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Campo"
    WHERE "Esquema"='CIRCMF' AND "Version"='V1' AND "Nombre"='TendenciaClasificacionriesgo'
);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
SELECT'CIRCMF', 'V1', 'IdMonedaPrincipal', '', 17, 'IdMonedaPrincipal', 'TEXT', 3, '', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 'Ayuda MonedaPrincipal', '', 0
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Campo"
    WHERE "Esquema"='CIRCMF' AND "Version"='V1' AND "Nombre"='IdMonedaPrincipal'
);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
SELECT'CIRCMF', 'V1', 'CodigoValorizacion', '', 19, 'CODIGOVALORIZACION', 'TEXT', 2, '', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 'Ayuda de CodigoValorizacion', '', 0
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Campo"
    WHERE "Esquema"='CIRCMF' AND "Version"='V1' AND "Nombre"='CodigoValorizacion'
);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
SELECT'CIRCMF', 'V1', 'BaseTasa', '', 20, 'BASETASA', 'TEXT', 3, '', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 'AYUDA BASE TASA', '', 0
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Campo"
    WHERE "Esquema"='CIRCMF' AND "Version"='V1' AND "Nombre"='BaseTasa'
);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
SELECT'CIRCMF', 'V1', 'CodigoPaisEmisionInstrumento', '', 22, 'CODIGOPAISEMISOR', 'TEXT', 4, '', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 'Ayuda CodigoPaisEmisionInstrumento', '', 0
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Campo"
    WHERE "Esquema"='CIRCMF' AND "Version"='V1' AND "Nombre"='CodigoPaisEmisionInstrumento'
);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
SELECT'CIRCMF', 'V1', 'Relacionado', '', 23, 'Relacionado', 'TEXT', 4, '', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 'Ayuda Relacionado', '', 0
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Campo"
    WHERE "Esquema"='CIRCMF' AND "Version"='V1' AND "Nombre"='Relacionado'
);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
SELECT'CIRCMF', 'V1', 'FuentePrecios', '', 24, 'FUENTEPRECIOS', 'TEXT', 100, '', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 'AYUDA FUENTEPRECIOS', '', 0
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Campo"
    WHERE "Esquema"='CIRCMF' AND "Version"='V1' AND "Nombre"='FuentePrecios'
);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
SELECT'CIRCMF', 'V1', 'CantidadUnidades', '', 16, 'CantidadUnidades', 'TEXT', 22, '', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 'Periodo', '', 8
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Campo"
    WHERE "Esquema"='CIRCMF' AND "Version"='V1' AND "Nombre"='CantidadUnidades'
);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
SELECT'CIRCMF', 'V1', 'Tir', '', 18, 'TIR', 'TEXT', 22, '', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 'ayuda de tir', '', 8
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Campo"
    WHERE "Esquema"='CIRCMF' AND "Version"='V1' AND "Nombre"='Tir'
);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
SELECT'CIRCMF', 'V1', 'ValorizacionCierre', '', 21, 'VALORIZACIONCIERRE', 'TEXT', 22, '', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 'Periodo', '', 0
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Campo"
    WHERE "Esquema"='CIRCMF' AND "Version"='V1' AND "Nombre"='ValorizacionCierre'
);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
SELECT'CIRCMF', 'V1', 'Duracion', '', 25, 'Duracion', 'TEXT', 22, '', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 'Periodo', '', 8
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Campo"
    WHERE "Esquema"='CIRCMF' AND "Version"='V1' AND "Nombre"='Duracion'
);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
SELECT'CIRCMF', 'V1', 'IdClasificacionTipoInstrumentoModificado', '', 3, 'IdClasificacionTipoInstrumentoModificado', 'TEXT', 10, '', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 'Periodo', '', 8
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Campo"
    WHERE "Esquema"='CIRCMF' AND "Version"='V1' AND "Nombre"='IdClasificacionTipoInstrumentoModificado'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
SELECT'CIRCMF', 'V1', 'Disponible5', '', 29, 'Disponible1', 'TEXT', 108, '', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '', 0, 'Ayuda de Disponible5', '', 0
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Campo"
    WHERE "Esquema"='CIRCMF' AND "Version"='V1' AND "Nombre"='Disponible5'
);

------FIN ARCHIVO DE SALIDA-------------
commit;