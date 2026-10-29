SET SCHEMA "BSCL";
--SELECT * FROM "BSCL.Normativa.BD::DOC.TipoDocumento" WHERE "IdTipoDocumento"='INFTRASEIL';


--UPDATE "BSCL.Normativa.BD::DOC.TipoDocumento" SET "LineasPorSalida"=15 WHERE "IdTipoDocumento"='INFTRASEIL';
/*==============================================================*/
/* Delete Tables                                                */
/*==============================================================*/

--DELETE FROM "BSCL.Normativa.BD::DOC.Documento" WHERE "Esquema"='INFTRASEIL';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema" = 'INFTRASEIL';
DELETE FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE "Esquema"='INFTRASEIL';
DELETE FROM "BSCL.Normativa.BD::DOC.TipoDocumento" WHERE "IdTipoDocumento"='INFTRASEIL';

/*==============================================================*/
/* Insert Table: TipoDocumento                     	            */
/*==============================================================*/

INSERT INTO "BSCL.Normativa.BD::DOC.TipoDocumento" ("IdTipoDocumento","Descripcion","IdNormativa","Esquema","IdTipoProgramaExportacion","ExtensionSalida","ComprimirSalida")
VALUES ('INFTRASEIL','Informe de Transacciones por Oficio (SEIL)','INFOTRANOFIC','INFTRASEIL','EXPORTACIONINFTRASEIL','txt','S');
--UPDATE  "BSCL.Normativa.BD::DOC.TipoDocumento" SET "ComprimirSalida"='S' WHERE "IdTipoDocumento"='INFTRASEIL';
/*==============================================================*/
/* Insert Table: Esquema                     	                */
/*==============================================================*/

INSERT INTO "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version", "Modulo", "Periodicidad", "Objetivo", "PeriodoInicio", "IdEntidadReguladora", "FechaPresentacionLimite", "FechaDatosDesde", "FechaDatosHasta", "CodigoVersionSII")
VALUES ('INFTRASEIL', 'V1', 'NOR', '', '', '', 'SVS', '20160110', '20160101', '9999-01-01', 1);


/*==============================================================*/
/* Insert Table: Campo          	                            */
/*==============================================================*/

insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"  ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES ('INFTRASEIL', 'V1', 'RutIntorma', '', '',  5, 'Rut Intermediario Informa', 'Ayuda de Rut Intermediario Informa', 'RUT',   15, '', 'N',  'S',  'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"  ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('INFTRASEIL', 'V1', 'FechaTransaccion', '', '',  6, 'Fecha Transaccion'  , 'Ayuda de Fecha Transaccion' , 'DATE',    8, '', 'N',  'S',  'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"  ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ( 'INFTRASEIL', 'V1', 'HoraTransaccion' , '', '',  7, 'Hora Transaccion'         , 'Ayuda de Hora Transaccion'         , 'HORA',    6, '', 'N',  'S',  'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"  ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ( 'INFTRASEIL', 'V1', 'Mercado'   , '', '',  8, 'Mercado'                  , 'Ayuda de Mercado'                  , 'TEXT',    1, '', 'N',  'S',  'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"  ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ( 'INFTRASEIL', 'V1', 'SistemaTransaccion'   , '', '',  9, 'Sistema Transaccion'      , 'Ayuda de Sistema Transaccion'      , 'TEXT',    4, '', 'N',  'S',  'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"  ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ( 'INFTRASEIL', 'V1', 'NumeroBolsa'     , '', '', 10, 'Numero Bolsa'             , 'Ayuda de Numero Bolsa'             , 'ID' ,    2, '', 'N',  'S',  'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"  ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ( 'INFTRASEIL', 'V1', 'Folio'     , '', '', 11, 'Folio'                    , 'Ayuda de Folio'                    , 'ID' ,   12, '', 'N',  'S',  'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"  ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ( 'INFTRASEIL', 'V1', 'RutContraparte'  , '', '', 12, 'Rut Intermediario Contraparte', 'Ayuda de Rut Intermediario Contraparte' , 'RUT',   15, '', 'N',  'S',  'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"  ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ( 'INFTRASEIL', 'V1', 'NumeroOrden'     , '', '', 13, 'Numero Orden'             , 'Ayuda de Numero Orden'             , 'ID' ,    8, '', 'N',  'S',  'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"  ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ( 'INFTRASEIL', 'V1', 'Nemotecnico'     , '', '', 14, 'Nemotecnico'              , 'Ayuda de Nemotecnico'              , 'TEXT',   20, '', 'N',  'S',  'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"  ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ( 'INFTRASEIL', 'V1', 'FechaVencimiento', '', '', 15, 'Fecha Vencimiento'        , 'Ayuda de Fecha Vencimiento'        , 'DATE',    8, '', 'N',  'S',  'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"  ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ( 'INFTRASEIL', 'V1', 'HTipoInstrumento' , '', '', 16, 'Tipo Instrumento H'         , 'Ayuda de Tipo Instrumento'         , 'TEXT',    2, '', 'N',  'S',  'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"  ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ( 'INFTRASEIL', 'V1', 'HTipoOperacion'   , '', '', 17, 'Tipo Operacion H'           , 'Ayuda de Tipo Operacion'           , 'TEXT',    2, '', 'N',  'S',  'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"  ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ( 'INFTRASEIL', 'V1', 'Persona'   , '', '', 18, 'Persona'                  , 'Ayuda de Persona'                  , 'TEXT',    1, '', 'N',  'S',  'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"  ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ( 'INFTRASEIL', 'V1', 'Plazo'     , '', '', 19, 'Plazo'                    , 'Ayuda de Plazo'                    , 'TEXT',    5, '', 'N',  'S',  'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"  ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ( 'INFTRASEIL', 'V1', 'CondicionLiquidacion' , '', '', 20, 'Condicion Liquidacion'    , 'Ayuda de Condicion Liquidacion'    , 'TEXT',    2, '', 'N',  'S',  'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"  ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ( 'INFTRASEIL', 'V1', 'TipoCliente'     , '', '', 21, 'Tipo Cliente'             , 'Ayuda de Tipo Cliente'             , 'TEXT',    1, '', 'N',  'S',  'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"  ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ( 'INFTRASEIL', 'V1', 'UnidadesTransadas'    , '', '', 22, 'Unidades Transadas'       , 'Ayuda de Unidades Transadas'       , 'DECIMAL',   19, '', 'N',  'S',  'N', 'S', 'N', 'N', 'N', 'N', '', 0, 4);
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"  ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ( 'INFTRASEIL', 'V1', 'Precio'    , '', '', 23, 'Precio'                   , 'Ayuda de Precio'                   , 'DECIMAL',   15, '', 'N',  'S',  'N', 'S', 'N', 'N', 'N', 'N', '', 0, 4);
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"  ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ( 'INFTRASEIL', 'V1', 'Total'     , '', '', 24, 'Total'                    , 'Ayuda de Total'                    , 'DECIMAL',   21, '', 'N',  'S',  'N', 'S', 'N', 'N', 'N', 'N', '', 0, 4);
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"  ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ( 'INFTRASEIL', 'V1', 'TipoUnidadesTransadas', '', '', 25, 'Tipo Unidades Transadas'  , 'Ayuda de TipoUnidadesTransadas'    , 'TEXT',    2, '', 'N',  'S',  'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"  ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ( 'INFTRASEIL', 'V1', 'ValorPar'  , '', '', 26, 'Valor Par'                , 'Ayuda de Valor Par'                , 'DECIMAL',    5, '', 'N',  'S',  'N', 'S', 'N', 'N', 'N', 'N', '', 0, 2);
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"  ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ( 'INFTRASEIL', 'V1', 'TIR'       , '', '', 27, 'TIR'                      , 'Ayuda de TIR'                      , 'DECIMAL',    6, '', 'N',  'S',  'N', 'S', 'N', 'N', 'N', 'N', '', 0, 2);
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"  ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ( 'INFTRASEIL', 'V1', 'Rentabilidad'    , '', '', 28, 'Rentabilidad'             , 'Ayuda de Rentabilidad'             , 'DECIMAL',    6, '', 'N',  'S',  'N', 'S', 'N', 'N', 'N', 'N', '', 0, 2);
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"  ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ( 'INFTRASEIL', 'V1', 'Nacionalidad'    , '', '', 29, 'Nacionalidad'             , 'Ayuda de Nacionalidad'             , 'TEXT',    1, '', 'N',  'S',  'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"  ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ( 'INFTRASEIL', 'V1', 'RutCliente', '', '', 30, 'Rut Cliente'              , 'Ayuda de Rut Cliente'              , 'RUT',   30, '', 'N',  'S',  'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"  ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ( 'INFTRASEIL', 'V1', 'NombreCliente'   , '', '', 31, 'Nombre Cliente'           , 'Ayuda de Nombre Cliente'           , 'TEXT',   30, '', 'N',  'S',  'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"  ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ( 'INFTRASEIL', 'V1', 'NumeroFactura'   , '', '', 32, 'Numero Factura'           , 'Ayuda de Numero Factura'           , 'ID' ,   12, '', 'N',  'S',  'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"  ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ( 'INFTRASEIL', 'V1', 'RutAutorizado'   , '', '', 33, 'Rut Autorizado'           , 'Ayuda de Rut Autorizado'           , 'RUT',   10, '', 'N',  'S',  'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"  ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ( 'INFTRASEIL', 'V1', 'NombreAutorizado', '', '', 34, 'Nombre Autorizado'        , 'Ayuda de Nombre Autorizado'        , 'TEXT',   30, '', 'N',  'S',  'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"  ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ( 'INFTRASEIL', 'V1', 'TipoOperacion'   , '', '', 35, 'Tipo Operacion'           , 'Ayuda de Tipo Operacion'           , 'TEXT',    2, '', 'N',  'S',  'N', 'N', 'N', 'N', 'N', 'N', '', 0, 0);
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"  ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ( 'INFTRASEIL', 'V1', 'TipoInstrumento' , '', '', 36, 'Tipo Instrumento'         , 'Ayuda de Tipo Instrumento'         , 'TEXT',    2, '', 'N',  'S',  'N', 'N', 'N', 'N', 'N', 'N', '', 0, 0);



/*==============================================================*/
/*                       Homologacion                           */
/*==============================================================*/
DELETE FROM  "BSCL.Normativa.BD::DOC.Homologacion"  WHERE "Esquema"='INFTRASEIL' AND "Version"='V1';


---BOLSA

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES(435,'INFTRASEIL','V1','BOLSA','BOLSA DE COMERCIO DE SANTIAGO','BCS','01','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES(436,'INFTRASEIL','V1','BOLSA','FUERA DE RUEDA','FRD','00','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES(437,'INFTRASEIL','V1','BOLSA','BOLSA ELECTRONICA','BEC','03','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES(438,'INFTRASEIL','V1','BOLSA','BOLSA DE COMERCIO DE VALPARAISO','BCV','02','ACT');
  
---SistemaTransaccion

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES(439,'INFTRASEIL','V1','SISTEMABURSATIL','BCS TELEPREGON','BCSTELEPREGON','RUED','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES(440,'INFTRASEIL','V1','SISTEMABURSATIL','INGRESO MANUAL','MANUAL','RUED','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES(441,'INFTRASEIL','V1','SISTEMABURSATIL','BCS TELERENTA','BCSTELERENTA','RUED','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES(442,'INFTRASEIL','V1','SISTEMABURSATIL','BCS REMATE SERIALIZADO','BCSREMATESERIALIZADO','REMT','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES(443,'INFTRASEIL','V1','SISTEMABURSATIL','BCS REMATE HOLANDES','BCSREMATEHOLANDES','REMT','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES(444,'INFTRASEIL','V1','SISTEMABURSATIL','BCS REMATE ELECTRONICO','BCSREMATEELECTRONICO','REMT','ACT');















