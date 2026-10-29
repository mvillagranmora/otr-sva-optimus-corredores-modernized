set schema "BSCL";

-- Diccionario
-- Esquema
--     Campo
--     Clasificador
--         CampoClasificador
--     Cuadratura
--         CampoCuadratura
--     Homologacion
--     Parametro
--     Validacion
--     Documento ----------> TipoDocumento
--         ResumenCarga
--         ResumenGeneracion
--         ResumenCuadratura
--             DetalleCuadratura
--         ResumenValidacion
--             DetalleValidacion
--         DatosDJ1885
--         DatosDJ1891
--         ...

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/
delete from "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1885";
delete from "BSCL"."BSCL.Normativa.BD::DOC.Documento" where "Esquema" = 'DJ1885';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Validacion" where "Esquema" = 'DJ1885';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" where "Esquema" = 'DJ1885';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Parametro" where "Esquema" = 'DJ1885';
delete from "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" where "Esquema" = 'DJ1885';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura" where "Esquema" = 'DJ1885';
delete from "BSCL"."BSCL.Normativa.BD::DOC.CampoClasificador" where "Esquema" = 'DJ1885';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Clasificador" where "Esquema" = 'DJ1885';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1885';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Esquema" where "Esquema" = 'DJ1885';

/*==============================================================*/
/* ESQUEMAS                                                     */
/*==============================================================*/
insert into "BSCL"."BSCL.Normativa.BD::DOC.Esquema"
select
    'DJ1885' as "Esquema",
    '2017' as "Version",
    'TRI' as "Modulo",
    'A' as "Periodicidad",
    '' as "Objetivo",
    '1996-02-15' as "PeriodoInicio",
    'SII' as "IdEntidadReguladora",
    '2017-03-26' as "FechaPresentacionLimite",
    '2016-01-01' as "FechaDatosDesde",
    '2016-12-31' as "FechaDatosHasta",
    24 as "CodigoVersionSII"
from dummy;

/*==============================================================*/
/* CAMPOS                                                       */
/*==============================================================*/
-- DJ1885-2017
--select * from "BSCL"."BSCL.Normativa.BD::DOC.Campo"
--select * from "BSCL"."BSCL.Normativa.BD::DOC.Esquema"
--select * from "BSCL"."BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1885'

insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'ID'                            as "Nombre", ''    as "CodigoSII", ''                  as "TipoDiccionario",  1 as "Orden", 'Secuencia'                                                  as "Titulo", 'Ayuda de Secuencia'                                                                                           as "Ayuda", 'ID'          as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'S' as "Comun", 'N' as "Clave", ''                                                   as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'IdDocumento'                   as "Nombre", ''    as "CodigoSII", ''                  as "TipoDiccionario",  2 as "Orden", 'Secuencia Docto'                                            as "Titulo", 'Ayuda de Secuencia Docto'                                                                                     as "Ayuda", 'ID'          as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'S' as "OcultoEnGrilla", 'S' as "OcultoEnMantenedor", 'S' as "Comun", 'N' as "Clave", '4. Control'                                         as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'VboUsuario'                    as "Nombre", ''    as "CodigoSII", 'VistoBueno'        as "TipoDiccionario",  3 as "Orden", 'VoBo'                                                       as "Titulo", 'Ayuda de VoBo'                                                                                                as "Ayuda", 'TEXT'        as "TipoDato",   1 as "Largo", 'N'       as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'S' as "Comun", 'N' as "Clave", '4. Control'                                         as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'Observaciones'                 as "Nombre", ''    as "CodigoSII", ''                  as "TipoDiccionario",  4 as "Orden", 'Observaciones'                                              as "Titulo", 'Ayuda de Observaciones'                                                                                       as "Ayuda", 'TEXT'        as "TipoDato", 250 as "Largo", ''        as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'S' as "Comun", 'N' as "Clave", '4. Control'                                         as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'FuenteDatos'                   as "Nombre", ''    as "CodigoSII", 'Origen'            as "TipoDiccionario",  5 as "Orden", 'Fuente'                                                     as "Titulo", 'Ayuda de Fuente'                                                                                              as "Ayuda", 'TEXT'        as "TipoDato",   0 as "Largo", 'INICIAL' as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'S' as "Comun", 'N' as "Clave", '4. Control'                                         as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'RutCliente'                    as "Nombre", 'C4'  as "CodigoSII", ''                  as "TipoDiccionario",  6 as "Orden", 'Rut Cliente'                                                as "Titulo", 'Ayuda de Rut Cliente'                                                                                         as "Ayuda", 'RUT_CLIENTE' as "TipoDato",  40 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'S' as "Clave", ''                                                   as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'IdCliente'                     as "Nombre", ''    as "CodigoSII", ''                  as "TipoDiccionario",  7 as "Orden", 'Secuencia Cliente'                                          as "Titulo", 'Ayuda de Secuencia Cliente'                                                                                   as "Ayuda", 'ID_CLIENTE'  as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'S' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'IdPortafolio'                  as "Nombre", ''    as "CodigoSII", ''                  as "TipoDiccionario",  8 as "Orden", 'Portafolio Cliente'                                         as "Titulo", 'Ayuda de Portafolio Cliente'                                                                                  as "Ayuda", 'NEMO'        as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'TipoInstrumento'               as "Nombre", ''    as "CodigoSII", 'TipoInstrumento'   as "TipoDiccionario",  9 as "Orden", 'Tipo Instrumento'                                           as "Titulo", 'Ayuda de Tipo Instrumento'                                                                                    as "Ayuda", 'NEMO'        as "TipoDato",  40 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'TipoRegistro'                  as "Nombre", ''    as "CodigoSII", 'TipoRegistro'      as "TipoDiccionario", 10 as "Orden", 'Recibido/Pagado'                                            as "Titulo", 'Ayuda de Recibido/Pagado'                                                                                     as "Ayuda", 'NEMO'        as "TipoDato",   3 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'Nemotecnico'                   as "Nombre", ''    as "CodigoSII", ''                  as "TipoDiccionario", 11 as "Orden", 'Nemotecnico'                                                as "Titulo", 'Ayuda de Nemotecnico'                                                                                         as "Ayuda", 'NEMO'        as "TipoDato", 100 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'FechaPago'                     as "Nombre", ''    as "CodigoSII", ''                  as "TipoDiccionario", 12 as "Orden", 'Fecha de Pago'                                              as "Titulo", 'Ayuda de Fecha de Pago'                                                                                       as "Ayuda", 'DATE_FILTER' as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'SecuenciaEvento'               as "Nombre", ''    as "CodigoSII", ''                  as "TipoDiccionario", 13 as "Orden", 'Secuencia Evento Capital'                                   as "Titulo", 'Ayuda de Secuencia Evento'                                                                                    as "Ayuda", 'ID'          as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'S' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'S' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'RutSociedad'                   as "Nombre", 'C2'  as "CodigoSII", ''                  as "TipoDiccionario", 14 as "Orden", 'RUT del Pagador '                                           as "Titulo", 'Ayuda de RUT del Pagador '                                                                                    as "Ayuda", 'RUT'         as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'S' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'RazonSocial'                   as "Nombre", ''    as "CodigoSII", ''                  as "TipoDiccionario", 15 as "Orden", 'Razon Social'                                               as "Titulo", 'Ayuda de Razon Social'                                                                                        as "Ayuda", 'NEMO'        as "TipoDato",   0 as "Largo", '100'     as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'TipoSociedad'                  as "Nombre", 'C3'  as "CodigoSII", 'TipoSociedad'      as "TipoDiccionario", 16 as "Orden", 'Tipo Sociedad'                                              as "Titulo", 'Ayuda de Tipo Sociedad'                                                                                       as "Ayuda", 'NEMO'        as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'CantidadAcciones31Dic'         as "Nombre", 'C5'  as "CodigoSII", ''                  as "TipoDiccionario", 17 as "Orden", 'Cantidad de Acciones al 31/12'                              as "Titulo", 'Ayuda de Cantidad de Acciones al 31/12'                                                                       as "Ayuda", 'QTY'         as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'MontoPagado'                   as "Nombre", ''    as "CodigoSII", ''                  as "TipoDiccionario", 18 as "Orden", 'Monto Pagado'                                               as "Titulo", 'Ayuda de Monto Pagado'                                                                                        as "Ayuda", 'AMOUNT'      as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'MontoRecibido'                 as "Nombre", ''    as "CodigoSII", ''                  as "TipoDiccionario", 19 as "Orden", 'Monto Recibido'                                             as "Titulo", 'Ayuda de Monto Recibido'                                                                                      as "Ayuda", 'AMOUNT'      as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'DividendosAfectos'             as "Nombre", 'C6'  as "CodigoSII", ''                  as "TipoDiccionario", 20 as "Orden", 'Afectos a IGC o Adicional'                                  as "Titulo", 'Montos Dividendos Reajustados Afectos a Global Complementario'                                                as "Ayuda", 'AMOUNT'      as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '2. Monto Dividendos Reajustados'                    as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'DividendosExentos'             as "Nombre", 'C7'  as "CodigoSII", ''                  as "TipoDiccionario", 21 as "Orden", 'Exentos de IGC'                                             as "Titulo", 'Montos Dividendos Reajustados Exentos de Global Complementario'                                               as "Ayuda", 'AMOUNT'      as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '2. Monto Dividendos Reajustados'                    as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'DividendosNoRentaNoAcogidos'   as "Nombre", 'C8'  as "CodigoSII", ''                  as "TipoDiccionario", 22 as "Orden", 'No Constitutivo de Renta No Acogido a Impuesto Sustitutivo' as "Titulo", 'Montos Dividendos Reajustados No Constitutivos de Renta No Acogidos a Impuesto Sustitutivo'                   as "Ayuda", 'AMOUNT'      as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '2. Monto Dividendos Reajustados'                    as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'DividendosNoRentaAcogidos'     as "Nombre", 'C33' as "CodigoSII", ''                  as "TipoDiccionario", 23 as "Orden", 'No Constitutivo de Renta Acogido a Impuesto Sustitutivo'    as "Titulo", 'Montos Dividendos Reajustados No Constitutivos de Renta Acogidos a Impuesto Sustitutivo'                      as "Ayuda", 'AMOUNT'      as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '2. Monto Dividendos Reajustados'                    as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'IncrementoImpuesto'            as "Nombre", 'C9'  as "CodigoSII", ''                  as "TipoDiccionario", 24 as "Orden", 'Incremento por Impuesto 1era. Categoría'                    as "Titulo", 'Incremento por Impuesto Primera Categoria'                                                                    as "Ayuda", 'AMOUNT'      as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '2. Monto Dividendos Reajustados'                    as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'CreditosAfectosConDevolucion'  as "Nombre", 'C12' as "CodigoSII", ''                  as "TipoDiccionario", 25 as "Orden", 'Afectos a IGC Con Devolucion'                               as "Titulo", 'Creditos para Global Complementario Adicional Impuesto de Primera Categoría Afectos Con Derecho a Devolucion' as "Ayuda", 'AMOUNT'      as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '3. Creditos para Global Complementario o Adicional' as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'CreditosAfectosSinDevolucion'  as "Nombre", 'C27' as "CodigoSII", ''                  as "TipoDiccionario", 26 as "Orden", 'Afectos a IGC Sin Devolucion'                               as "Titulo", 'Creditos para Global Complementario Adicional Impuesto de Primera Categoría Afectos Sin Derecho a Devolucion' as "Ayuda", 'AMOUNT'      as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '3. Creditos para Global Complementario o Adicional' as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'CreditosExentosConDevolucion'  as "Nombre", 'C13' as "CodigoSII", ''                  as "TipoDiccionario", 27 as "Orden", 'Afectos a IGC Con Devolucion'                               as "Titulo", 'Creditos para Global Complementario Adicional Impuesto de Primera Categoría Exentos Con Derecho a Devolucion' as "Ayuda", 'AMOUNT'      as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '3. Creditos para Global Complementario o Adicional' as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'CreditosExentosSinDevolucion'  as "Nombre", 'C28' as "CodigoSII", ''                  as "TipoDiccionario", 28 as "Orden", 'Afectos a IGC Sin Devolucion'                               as "Titulo", 'Creditos para Global Complementario Adicional Impuesto de Primera Categoría Exentos Sin Derecho a Devolucion' as "Ayuda", 'AMOUNT'      as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '3. Creditos para Global Complementario o Adicional' as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'CreditosImpuestosExternos'     as "Nombre", 'C31' as "CodigoSII", ''                  as "TipoDiccionario", 29 as "Orden", 'Créditos por Impuestos Externos'                            as "Titulo", 'Creditos para Global Complementario Adicional Creditos por Impuestos Externos'                                as "Ayuda", 'AMOUNT'      as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '3. Creditos para Global Complementario o Adicional' as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'CreditosImpuestoTasaAdicional' as "Nombre", 'C11' as "CodigoSII", ''                  as "TipoDiccionario", 30 as "Orden", 'ImpuestoTasaAdicional'                                      as "Titulo", 'Creditos para Global Complementario Adicional Impuesto Tasa Adicional'                                        as "Ayuda", 'AMOUNT'      as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '3. Creditos para Global Complementario o Adicional' as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'NumeroCertificado'             as "Nombre", 'C26' as "CodigoSII", ''                  as "TipoDiccionario", 31 as "Orden", 'Número de Certificado'                                      as "Titulo", 'Ayuda de Número de Certificado'                                                                               as "Ayuda", 'ID'          as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'CantidadDerecho'               as "Nombre", ''    as "CodigoSII", ''                  as "TipoDiccionario", 32 as "Orden", 'Cantidad Derecho'                                           as "Titulo", 'Ayuda de Cantidad Derecho'                                                                                    as "Ayuda", 'QTY'         as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'S' as "OcultoEnGrilla", 'S' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'IdInstrumento'                 as "Nombre", ''    as "CodigoSII", ''                  as "TipoDiccionario", 33 as "Orden", 'Secuencia Instrumento'                                      as "Titulo", 'Ayuda de Secuencia Instrumento'                                                                               as "Ayuda", 'ID'          as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'S' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'S' as "OcultoEnGrilla", 'S' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'IdEventoCapitalCliente'        as "Nombre", ''    as "CodigoSII", ''                  as "TipoDiccionario", 34 as "Orden", 'Secuencia Evento de Capital Cliente'                        as "Titulo", 'Ayuda de Secuencia Evento de Capital'                                                                        as "Ayuda", 'ID'          as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'S' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'S' as "OcultoEnGrilla", 'S' as "OcultoEnMantenedor", 'N' as "Comun", 'S' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'TipoSocioNegocio'              as "Nombre", ''    as "CodigoSII", ''                  as "TipoDiccionario", 35 as "Orden", 'Tipo Socio de Negocio'                                      as "Titulo", 'Ayuda de Tipo Socio de Negocio'                                                                               as "Ayuda", 'NEMO'        as "TipoDato",   3 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'TipoCartera'                   as "Nombre", ''    as "CodigoSII", ''                  as "TipoDiccionario", 36 as "Orden", 'Tipo Cartera'                                               as "Titulo", 'Ayuda de Tipo Cartera'                                                                                        as "Ayuda", 'NEMO'        as "TipoDato",   3 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'IdMoneda'                      as "Nombre", ''    as "CodigoSII", ''                  as "TipoDiccionario", 37 as "Orden", 'Moneda '                                                    as "Titulo", 'Ayuda de Moneda '                                                                                             as "Ayuda", 'NEMO'        as "TipoDato",   3 as "Largo", '0'       as "ValorDefecto", 'S' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1885' as "Esquema", '2017' as "Version", 'TipoEventoCapital'             as "Nombre", ''    as "CodigoSII", 'TipoEventoCapital' as "TipoDiccionario", 38 as "Orden", 'Tipo Evento Capital'                                        as "Titulo", 'Ayuda de Tipo Evento Capital'                                                                                 as "Ayuda", 'NEMO'        as "TipoDato",   3 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos de los Informados'                         as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;

/*==============================================================*/
/* HOMOLOGACIONES                                               */
/*==============================================================*/

/*==============================================================*/
/* PARAMETROS                                                   */
/*==============================================================*/
insert into "BSCL.Normativa.BD::DOC.Parametro"
select
    'DJ1885' as "Esquema",
    '2017' as "Version",
    'P28' as "Codigo",
    'Tolerancia SII P28' as "Descripcion",
    '0.4' as "Valor"
from dummy;
insert into "BSCL.Normativa.BD::DOC.Parametro"
select
    'DJ1885' as "Esquema",
    '2017' as "Version",
    'P31' as "Codigo",
    'Tolerancia SII P31' as "Descripcion",
    '0' as "Valor"
from dummy;
insert into "BSCL.Normativa.BD::DOC.Parametro"
select
    'DJ1885' as "Esquema",
    '2017' as "Version",
    'P84' as "Codigo",
    'Tolerancia SII P84' as "Descripcion",
    '0.2' as "Valor"
from dummy;
insert into "BSCL.Normativa.BD::DOC.Parametro"
select
    'DJ1885' as "Esquema",
    '2017' as "Version",
    'P85' as "Codigo",
    'Tolerancia SII P85' as "Descripcion",
    '0.290322' as "Valor"
from dummy;

/*==============================================================*/
/* VALIDACIONES                                                 */
/*==============================================================*/
insert into "BSCL.Normativa.BD::DOC.Validacion"
select
    'DJ1885' as "Esquema",
    '2017' as "Version",
    'H090' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'El Tipo de Sociedad Anónima del Pagador del Dividendo debe ser  igual a:  A = Abierta, C = Cerrada, E = En Comandita por Acciones' as "Descripcion",
    'E' as "Severidad",
    'Tipo Sociedad Pagador = {A .o. C .o. E}' as "Formula"
from dummy;
insert into "BSCL.Normativa.BD::DOC.Validacion"
select
    'DJ1885' as "Esquema",
    '2017' as "Version",
    'H095' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'El crédito impuesto de primera categoría (Afecto y exento, con y sin derecho a devolución) menos un monto  de tolerancia determinado por el SII, debe ser menor a la suma de los montos dividendos reajustados afectos y exentos más el incremento por impuesto de 1ra categoría y el  Crédito por Impuestos Externos, multiplicado  por la tasa del Impuesto Primera Categoría.' as "Descripcion",
    'E' as "Severidad",
    '[C12]+[C27]+[C13]+[C28]– P31 ≤ ([C6]+[C7]+[C9]+[C31])*P84' as "Formula"
from dummy;
insert into "BSCL.Normativa.BD::DOC.Validacion"
select
    'DJ1885' as "Esquema",
    '2017' as "Version",
    'H096' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'El crédito impuesto tasa adicional Ex. Art. 21 L.I.R menos un monto  de tolerancia determinado por el SII, debe ser menor a la suma de los montos dividendos reajustados afectos y exentos más el incremento por impuesto de 1ra categoría por tasa 40%.' as "Descripcion",
    'E' as "Severidad",
    '[CGAA] – P31 ≤ ([DRA]+[DRE]+[IIP])*P28' as "Formula"
from dummy;
insert into "BSCL.Normativa.BD::DOC.Validacion"
select
    'DJ1885' as "Esquema",
    '2017' as "Version",
    'H257' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'El incremento por impuesto de primera categoría menos un monto  de tolerancia determinado por el SII, debe ser menor o igual al crédito impuesto de primera categoría (Afecto y exento, con y sin derecho a devolución).' as "Descripcion",
    'E' as "Severidad",
    '[C9] – P31 ≤ [C12]+[C27]+[C13]+[C28]' as "Formula"
from dummy;
insert into "BSCL.Normativa.BD::DOC.Validacion"
select
    'DJ1885' as "Esquema",
    '2017' as "Version",
    'H258' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'El Incremento por impuesto de primera categoría menos un monto  de tolerancia determinado por el SII, debe ser menor a la suma de los montos dividendos reajustados afectos a global complementario o adicional y montos dividendos reajustados exentos de global complementario  y el  Crédito por Impuestos Externos por el factor de incremento del AT.' as "Descripcion",
    'E' as "Severidad",
    '[C9] – P31 ≤ ([C6]+[C7]+[C31])*P85' as "Formula"
from dummy;
insert into "BSCL.Normativa.BD::DOC.Validacion"
select
    'DJ1885' as "Esquema",
    '2017' as "Version",
    'H2038' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si ingresa monto en alguna de las columnas de crédito afecto a GC o Adicional, debe existir monto en la columna de dividendo reajustado afectos a GC adicional.' as "Descripcion",
    'E' as "Severidad",
    'Si (C12 + C27 > 0 ),  entonces:  C6>0' as "Formula"
from dummy;
insert into "BSCL.Normativa.BD::DOC.Validacion"
select
    'DJ1885' as "Esquema",
    '2017' as "Version",
    'H2039' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si ingresa monto en alguna de las columnas de crédito exento a GC o Adicional, debe existir monto en la columna de dividendo reajustado exentos de  GC o adicional.' as "Descripcion",
    'E' as "Severidad",
    'Si (C13 + C28 > 0 ),  entonces:  C7>0' as "Formula"
from dummy;

/*==============================================================*/
/* DOCUMENTOS                                                   */
/*==============================================================*/

insert into "BSCL"."BSCL.Normativa.BD::DOC.Documento"
select
    2 as "IdDocumento",
    'DECLACORR' as "IdTipoDocumento",
    'DJ1885' as "Esquema",
    '2017' as "Version",
    1 as "IdEmpresa",
    '2017-03-26' as "FechaLimite",
    'ACT' as "IdEstadoDocumento",
    '2017-01-01' as "FechaCreacion",
    '2017-03-26' as "FechaPresentacion",
    '90947000-1' as "DocumentoIdentidadDeclarante",
    '14448678-1' as "DocumentoIdentidadRepresentante",
    'contabilidad@imtrust.cl' as "DireccionElectronicaDeclaracion",
    'El Golf 47' as "DireccionFisicaDeclaracion",
    'Las Condes' as "ComunaDeclaracion",
    '25689854' as "TelefonoDeclaracion",
    'I9153103' as "FolioDeclaracion",
    'N' as "EnProceso",
    null as "EnProcesoDesde"
from dummy;

/*==============================================================*/
/* DETALLES DJ1885                                              */
/*==============================================================*/
/*
insert into "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1885"
SELECT
    row_number() over (order by "IdEventoCapitalCliente") as ID,
    2 as "IdDocumento",
    'N' as "VboUsuario",
    '' as "Observaciones",
    'INICIAL' as "FuenteDatos",

    EvCli."DocumentoIdentidad",
    Portafolio."IdCliente",
    EvCli."IdPortafolio",
    EvCli."IdTipoInstrumento",
    'P',
    EvCli."Nemo",
    EvCli."FechaPago",
    EvCli."Secuencia",

    --"A" si se trata de una Sociedad Anónima Abierta,
    --"C" si se trata de una Sociedad Anónima Cerrada o Sociedades por Acciones
    --"E" si se trata de una Sociedad en Comandita por Acciones)

    Emisor."RutEmisor",
    Emisor."RazonSocial",
    'A' as "TipoSociedad",
    EvCli."Total" as "MontoPagado",
    0 as "MontoRecibido",
    0 as "CantidadAcciones31Dic",
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    -- Datos de referencia
    EvCli."CantidadDerecho",
    EvCli."IdInstrumento",
    EvCli."IdEventoCapitalCliente",
    EvCli."TipoSocioNegocio",
    EvCli."TipoCartera",
    EvCli."IdMoneda",
    TipEvCap."Codigo"

FROM "BSCL"."BSCL.Custodia.BD::CUS.EventoCapitalCliente" as EvCli
    INNER JOIN "BSCL"."BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS Portafolio
        ON EvCli."IdPortafolio" = Portafolio."IdPortafolio"
    INNER JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento
        ON EvCli."IdInstrumento" = Instrumento."IdInstrumento"
    INNER JOIN "BSCL"."BSCL.Mercado.BD::MER.Emisor" AS Emisor
        ON Instrumento."IdEmisor" = Emisor."IdEmisor"
    INNER JOIN "BSCL"."BSCL.Custodia.BD.Vistas::CUS.ConsultarTipoEventoCapital" AS TipEvCap
        ON EvCli."IdTipoEventoCapital" = TipEvCap."IdTipoEventoCapital"
where EvCli."IdTipoInstrumento" in ('ACN');
*/


/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/

commit;
