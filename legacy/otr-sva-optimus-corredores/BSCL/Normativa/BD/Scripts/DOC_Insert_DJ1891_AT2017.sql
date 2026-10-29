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
/* Delete Data:                                                 */
/*==============================================================*/
delete from "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1891";
delete from "BSCL"."BSCL.Normativa.BD::DOC.Documento" where "Esquema" = 'DJ1891';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Validacion" where "Esquema" = 'DJ1891';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" where "Esquema" = 'DJ1891';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Parametro" where "Esquema" = 'DJ1891';
delete from "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" where "Esquema" = 'DJ1891';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura" where "Esquema" = 'DJ1891';
delete from "BSCL"."BSCL.Normativa.BD::DOC.CampoClasificador" where "Esquema" = 'DJ1891';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Clasificador" where "Esquema" = 'DJ1891';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1891';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Esquema" where "Esquema" = 'DJ1891';

/*==============================================================*/
/* ESQUEMAS                                                     */
/*==============================================================*/

insert into "BSCL"."BSCL.Normativa.BD::DOC.Esquema"
select
    'DJ1891' as "Esquema",
    '2017' as "Version",
    'TRI' as "Modulo",
    'A' as "Periodicidad",
    '' as "Objetivo",
    '1996-02-15' as "PeriodoInicio",
    'SII' as "IdEntidadReguladora",
    '2017-03-30' as "FechaPresentacionLimite",
    '2016-01-01' as "FechaDatosDesde",
    '2016-12-31' as "FechaDatosHasta",
    15 as "CodigoVersionSII"
from dummy;

/*==============================================================*/
/* HOMOLOGACIONES                                               */
/*==============================================================*/

/*==============================================================*/
/* VALIDACIONES                                                 */
/*==============================================================*/
insert into "BSCL.Normativa.BD::DOC.Validacion"
select
    'DJ1891' as "Esquema",
    '2017' as "Version",
    'H115' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'El número de documento debe ser mayor a cero' as "Descripcion",
    'E' as "Severidad",
    'N° de documento > 0' as "Formula"
from dummy;
insert into "BSCL.Normativa.BD::DOC.Validacion"
select
    'DJ1891' as "Esquema",
    '2017' as "Version",
    'H116' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'El tipo de Documento debe indicarse con las letras:  F: Factura, C: Nota de Crédito, D: Nota de Débito, O: Otro (no debe considera documentos nulos)' as "Descripcion",
    'E' as "Severidad",
    'Tipo Documento= {F .o. C .o. D .o. O}' as "Formula"
from dummy;
insert into "BSCL.Normativa.BD::DOC.Validacion"
select
    'DJ1891' as "Esquema",
    '2017' as "Version",
    'H117' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'El tipo de transacción debe ser alguno de los siguientes números: 0= Comisiones, 1= Acciones, 2= Dólares, 3= Oro, 4= Otras operaciones de Futuro, 5= Opciones ,  6= Documentos de Renta Fija, 7= Títulos Emitidos por Bolsa de Productos Agropecuarios sobre Certificados de Depósitos de Productos, 8 = Cuotas de Fondos de Inversión, 9= Otros Instrumentos de Inversión, 10=  Aporte y Adquisición de Valores (ETF - Exchange Traded Funds), 99= Otros que no sean Instrumentos de Inversión' as "Descripcion",
    'E' as "Severidad",
    'Tipo Transacción={0.o.1.o.2.o. 3.o. 4.o. 5.o. 6.o. 7.o.8.o.9 .o.10.o. 99}' as "Formula"
from dummy;
insert into "BSCL.Normativa.BD::DOC.Validacion"
select
    'DJ1891' as "Esquema",
    '2017' as "Version",
    'H1350' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'El campo Nemotecnico (C13 ) es un campo obligatorio en caso que el tipo de transacción sea 1: Acciones o 8: Cuotas de Fondo de Inversión ' as "Descripcion",
    'E' as "Severidad",
    'Si C8 = 1.o. C8 = 8, entonces [C13] <> blanco. O. [C13] > 0' as "Formula"
from dummy;
insert into "BSCL.Normativa.BD::DOC.Validacion"
select
    'DJ1891' as "Esquema",
    '2017' as "Version",
    'H611' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'La fecha del documento debe ser anterior al 01.01.AT y posterior al 31.12.AT-2 y debe ser distinto de blanco.' as "Descripcion",
    'E' as "Severidad",
    '31.12.AT-2 < fecha < 01.01.AT .y. fecha <> Blanco' as "Formula"
from dummy;

/*==============================================================*/
/* CAMPOS                                                       */
/*==============================================================*/

--
-- DJ1891-2016
--
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1891' as "Esquema", '2017' as "Version", 'ID'              as "Nombre", '' as "CodigoSII", ''              as "TipoDiccionario",   1 as "Orden", 'Secuencia'            as "Titulo", 'Ayuda de Secuencia'            as "Ayuda", 'ID'            as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'S' as "Obligatorio", 'N' as "Editable", 'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'S' as "Comun", 'N' as "Clave", ''           as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1891' as "Esquema", '2017' as "Version", 'RutCliente'      as "Nombre", '' as "CodigoSII", ''              as "TipoDiccionario",  11 as "Orden", 'RUT Cliente'          as "Titulo", 'Ayuda de RUT Cliente'          as "Ayuda", 'RUT_CLIENTE'   as "TipoDato",  40 as "Largo", ''        as "ValorDefecto", 'S' as "Obligatorio", 'N' as "Editable", 'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", ''           as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1891' as "Esquema", '2017' as "Version", 'IdDocumento'     as "Nombre", '' as "CodigoSII", ''              as "TipoDiccionario",   2 as "Orden", 'Secuencia Docto'      as "Titulo", 'Ayuda de Secuencia Docto'      as "Ayuda", 'ID'            as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'S' as "Obligatorio", 'N' as "Editable", 'N' as "ModoMasivo", 'S' as "Exportable", 'S' as "OcultoEnGrilla", 'S' as "OcultoEnMantenedor", 'S' as "Comun", 'N' as "Clave", '2. Control' as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1891' as "Esquema", '2017' as "Version", 'VboUsuario'      as "Nombre", '' as "CodigoSII", ''              as "TipoDiccionario",   3 as "Orden", 'VoBo'                 as "Titulo", 'Ayuda de VoBo'                 as "Ayuda", 'TEXT'          as "TipoDato",   1 as "Largo", 'N'       as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'S' as "Comun", 'N' as "Clave", '2. Control' as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1891' as "Esquema", '2017' as "Version", 'Observaciones'   as "Nombre", '' as "CodigoSII", ''              as "TipoDiccionario",   4 as "Orden", 'Observaciones'        as "Titulo", 'Ayuda de Observaciones'        as "Ayuda", 'TEXT'          as "TipoDato", 250 as "Largo", ''        as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'S' as "Comun", 'N' as "Clave", '2. Control' as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1891' as "Esquema", '2017' as "Version", 'FuenteDatos'     as "Nombre", '' as "CodigoSII", ''              as "TipoDiccionario",   5 as "Orden", 'Fuente'               as "Titulo", 'Ayuda de Fuente'               as "Ayuda", 'TEXT'          as "TipoDato",  40 as "Largo", 'INICIAL' as "ValorDefecto", 'N' as "Obligatorio", 'N' as "Editable", 'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'S' as "Comun", 'N' as "Clave", '2. Control' as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1891' as "Esquema", '2017' as "Version", 'TipoDocumento'   as "Nombre", '' as "CodigoSII", 'TipoDocu'      as "TipoDiccionario",   6 as "Orden", 'Tipo Documento'       as "Titulo", 'Ayuda de Tipo Documento'       as "Ayuda", 'NEMO'          as "TipoDato",   0 as "Largo", ''        as "ValorDefecto", 'S' as "Obligatorio", 'S' as "Editable", 'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'S' as "Clave", '1. Datos'   as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1891' as "Esquema", '2017' as "Version", 'NumeroDocumento' as "Nombre", '' as "CodigoSII", ''              as "TipoDiccionario",   7 as "Orden", 'Número Documento'     as "Titulo", 'Ayuda de Número Documento'     as "Ayuda", 'ID'            as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'S' as "Obligatorio", 'S' as "Editable", 'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'S' as "Clave", '1. Datos'   as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1891' as "Esquema", '2017' as "Version", 'FechaDocumento'  as "Nombre", '' as "CodigoSII", ''              as "TipoDiccionario",   8 as "Orden", 'Fecha Documento'      as "Titulo", 'Ayuda de Fecha Documento'      as "Ayuda", 'DATE_FILTER'   as "TipoDato",   0 as "Largo", ''        as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos'   as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1891' as "Esquema", '2017' as "Version", 'RutEmisor'       as "Nombre", '' as "CodigoSII", ''              as "TipoDiccionario",   9 as "Orden", 'RUT Emisor'           as "Titulo", 'Ayuda de RUT Emisor'           as "Ayuda", 'RUT'           as "TipoDato",  40 as "Largo", ''        as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos'   as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1891' as "Esquema", '2017' as "Version", 'Nemotecnico'     as "Nombre", '' as "CodigoSII", ''              as "TipoDiccionario",  10 as "Orden", 'Instrumento'          as "Titulo", 'Ayuda de Instrumento'          as "Ayuda", 'NEMO'          as "TipoDato",  40 as "Largo", ''        as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos'   as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1891' as "Esquema", '2017' as "Version", 'Cantidad'        as "Nombre", '' as "CodigoSII", ''              as "TipoDiccionario",  12 as "Orden", 'Cantidad'             as "Titulo", 'Ayuda de Cantidad'             as "Ayuda", 'QTY'           as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'S' as "Obligatorio", 'S' as "Editable", 'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos'   as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1891' as "Esquema", '2017' as "Version", 'Monto'           as "Nombre", '' as "CodigoSII", ''              as "TipoDiccionario",  13 as "Orden", 'Monto'                as "Titulo", 'Ayuda de Monto'                as "Ayuda", 'AMOUNT'        as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'S' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos'   as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1891' as "Esquema", '2017' as "Version", 'TipoTransaccion' as "Nombre", '' as "CodigoSII", 'TipoTransac'   as "TipoDiccionario",  14 as "Orden", 'Tipo Transacción'     as "Titulo", 'Ayuda de Tipo Transacción'     as "Ayuda", 'NEMO'          as "TipoDato",   0 as "Largo", ''        as "ValorDefecto", 'S' as "Obligatorio", 'S' as "Editable", 'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos'   as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1891' as "Esquema", '2017' as "Version", 'IdTipoOperacion' as "Nombre", '' as "CodigoSII", 'TipoOperacion' as "TipoDiccionario",  15 as "Orden", 'Tipo Operación'       as "Titulo", 'Ayuda de Tipo Operación'       as "Ayuda", 'NEMO'          as "TipoDato",   0 as "Largo", ''        as "ValorDefecto", 'S' as "Obligatorio", 'S' as "Editable", 'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos'   as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1891' as "Esquema", '2017' as "Version", 'IdTipoProducto'  as "Nombre", '' as "CodigoSII", 'TipoProducto'  as "TipoDiccionario",  16 as "Orden", 'Tipo Producto'        as "Titulo", 'Ayuda de Tipo Producto'        as "Ayuda", 'NEMO'          as "TipoDato",   0 as "Largo", ''        as "ValorDefecto", 'S' as "Obligatorio", 'S' as "Editable", 'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos'   as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1891' as "Esquema", '2017' as "Version", 'IdBaseDocumento' as "Nombre", '' as "CodigoSII", ''              as "TipoDiccionario",  17 as "Orden", 'Secuencia Base'       as "Titulo", 'Ayuda de Secuencia Base'       as "Ayuda", 'ID'            as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio", 'N' as "Editable", 'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'S' as "Clave", '1. Datos'   as "Grupo",  4 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1891' as "Esquema", '2017' as "Version", 'IdCliente'       as "Nombre", '' as "CodigoSII", ''              as "TipoDiccionario",  18 as "Orden", 'Secuencia Cliente'    as "Titulo", 'Ayuda de Secuencia Cliente'    as "Ayuda", 'ID_CLIENTE'    as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'S' as "Obligatorio", 'N' as "Editable", 'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos'   as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'DJ1891' as "Esquema", '2017' as "Version", 'IdPortafolio'    as "Nombre", '' as "CodigoSII", ''              as "TipoDiccionario",  19 as "Orden", 'Secuencia Portafolio' as "Titulo", 'Ayuda de Secuencia Portafolio' as "Ayuda", 'ID_PORTAFOLIO' as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'S' as "Obligatorio", 'N' as "Editable", 'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos'   as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;

/*==============================================================*/
/* CUADRATURAS                                                  */
/*==============================================================*/

insert into "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"
select
    'DJ1891' as "Esquema",
    '2017' as "Version",
    'PorRutCliente' as "Tipo",
    'Cuadratura por RUT Cliente' as "Descripcion"
from dummy;

insert into "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura"
select
    'DJ1891' as "Esquema",
    '2017' as "Version",
    'PorRutCliente' as "TipoCuadratura",
    'RutCliente' as "NombreCampo",
    1 as "Orden"
from dummy;

/*==============================================================*/
/* CLASIFICADORES                                               */
/*==============================================================*/

insert into "BSCL"."BSCL.Normativa.BD::DOC.Clasificador"
select
    'DJ1891' as "Esquema",
    '2017' as "Version",
    'PorCompras' as "Tipo",
    'Compras' as "Descripcion"
from dummy;

insert into "BSCL"."BSCL.Normativa.BD::DOC.CampoClasificador"
select
    'DJ1891' as "Esquema",
    '2017' as "Version",
    'PorCompras' as "TipoClasificador",
    'IdTipoOperacion' as "NombreCampo",
    'CO' as "ValorClasificador"
from dummy;

/*==============================================================*/
/* DOCUMENTOS                                                   */
/*==============================================================*/

insert into "BSCL"."BSCL.Normativa.BD::DOC.Documento"
select
    1 as "IdDocumento",
    'DECLACORR' as "IdTipoDocumento",
    'DJ1891' as "Esquema",
    '2017' as "Version",
    1 as "IdEmpresa",
    '2017-03-31' as "FechaLimite",
    'ACT' as "IdEstadoDocumento",
    '2016-01-01' as "FechaCreacion",
    '2017-03-30' as "FechaPresentacion",
    '90947000-1' as "DocumentoIdentidadDeclarante",
    '14448678-1' as "DocumentoIdentidadRepresentante",
    'contabilidad@imtrust.cl' as "DireccionElectronicaDeclaracion",
    'El Golf 47' as "DireccionFisicaDeclaracion",
    'Las Condes' as "ComunaDeclaracion",
    '5689854' as "TelefonoDeclaracion",
    'I9153103' as "FolioDeclaracion",
    'N' as "EnProceso",
    null as "EnProcesoDesde"
from dummy;

/*==============================================================*/
/* DETALLES DJ1891                                              */
/*==============================================================*/
/*
insert into "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1891"
select
    row_number() over (order by "IdComprobanteDetalle") as ID,
    1 as "IdDocumento",
    'N' as "VboUsuario",
    '' as "Observaciones",
    'INICIAL' as "FuenteDatos",
    'F' as "TipoDocumento",
    comp."FolioComprobante" as "NumeroDocumento",
    comp."FechaEmision" as "FechaDocumento",
    ifnull(soc."RUT",'') as "RutEmisor",
    det."NemotecnicoInstrumento" as "Nemotecnico",
    doc."DocumentoIdentidad" as "RutCliente",
    det."Cantidad" as "Cantidad",
    det."Monto",
    '01' as "TipoTransaccion",
    substring(comp."IdTipoOperacion",1,2) as "IdTipoOperacion",
    comp."IdTipoProducto" as "IdTipoProducto",
    row_number() over (order by "IdComprobanteDetalle") as "IdBaseDocumento",
    comp."IdCliente",
    comp."IdPortafolio"
from "BSCL"."BSCL.Facturacion.BD::FAC.Comprobante" as comp,
    "BSCL"."BSCL.Facturacion.BD::FAC.ComprobanteDetalle" as det
left outer join "RUEBAS"."TBASSOC" as soc on ((ltrim(rtrim(det."NemotecnicoInstrumento")) = ltrim(rtrim(soc."NEMO_SASA")))),
    "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as cli,
    "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as per,
    "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as doc
where comp."IdComprobante" = det."IdComprobante"
and comp."IdCliente" = cli."IdCliente"
and cli."IdPersona" = per."IdPersona"
and per."IdDocumentoIdentidad" = doc."IdDocumentoIdentidad";
*/

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/

commit;
