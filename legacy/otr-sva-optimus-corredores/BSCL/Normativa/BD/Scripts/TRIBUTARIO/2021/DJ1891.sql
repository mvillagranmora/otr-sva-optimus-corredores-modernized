SET SCHEMA "BSCL";

DROP TABLE BSCL."BSCL.Normativa.BD::TRI.DatosDJ1891";

CREATE TABLE BSCL."BSCL.Normativa.BD::TRI.DatosDJ1891" (
	ID BIGINT NOT NULL,
	"IdDocumento" BIGINT NOT NULL,
	"VboUsuario" CHAR(1) NOT NULL,
	"Observaciones" NVARCHAR(250) NOT NULL,
	"FuenteDatos" NVARCHAR(40) NOT NULL,
	"TipoDocumento" NVARCHAR(10) NOT NULL,
	"NumeroDocumento" BIGINT NOT NULL,
	"FechaDocumento" DATE,
	"RutEmisor" NVARCHAR(40),
	"Nemotecnico" NVARCHAR(100),
	"RutCliente" NVARCHAR(40),
	"Cuenta" NVARCHAR(10) NOT NULL,
	"Cantidad" BIGINT NOT NULL,
	"MontoCompras" DECIMAL(26,8) DEFAULT 0 NOT NULL,
	"MontoVentas" DECIMAL(26,8) DEFAULT 0 NOT NULL,
	"TipoTransaccion" NVARCHAR(6) NOT NULL,
	"IdTipoOperacion" NVARCHAR(6),
	"IdTipoProducto" NVARCHAR(4),
	"IdBaseDocumento" BIGINT,
	"IdCliente" BIGINT,
	"IdPortafolio" BIGINT,
	"LineaFactura" BIGINT,
	PRIMARY KEY ("ID"),
	CONSTRAINT "FK_DatosDJ1891_Documento" FOREIGN KEY ("IdDocumento") REFERENCES BSCL."BSCL.Normativa.BD::DOC.Documento"("IdDocumento") ON DELETE RESTRICT ON UPDATE RESTRICT
);
CREATE INDEX "IX01_DocumentoDJ1891" ON BSCL."BSCL.Normativa.BD::TRI.DatosDJ1891" ("IdDocumento","TipoDocumento","NumeroDocumento");
	

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
DELETE FROM "BSCL"."BSCL.Normativa.BD::TRI.ControlProceso" 	WHERE "Esquema"	= 'DJ1891';	

/*==============================================================*/
/* ESQUEMAS                                                     */
/*==============================================================*/

INSERT INTO "BSCL.Normativa.BD::DOC.Esquema" ("Esquema","Version","Modulo","Periodicidad","Objetivo","PeriodoInicio",
		     "IdEntidadReguladora","FechaPresentacionLimite","FechaDatosDesde","FechaDatosHasta",
		     "CodigoVersionSII","HabilitadoMantenedor")
select
    'DJ1891' as "Esquema",
    '2020' as "Version",
    'TRI' as "Modulo",
    'A' as "Periodicidad",
    '' as "Objetivo",
    '2020-01-01' as "PeriodoInicio",
    'SII' as "IdEntidadReguladora",
    '2020-03-30' as "FechaPresentacionLimite",
    '2020-01-01' as "FechaDatosDesde",
    '2020-12-31' as "FechaDatosHasta",
    15 as "CodigoVersionSII",
    'S' as "HabilitadoMantenedor"
from dummy;


INSERT INTO "BSCL.Normativa.BD::DOC.Esquema" ("Esquema","Version","Modulo","Periodicidad","Objetivo","PeriodoInicio",
		     "IdEntidadReguladora","FechaPresentacionLimite","FechaDatosDesde","FechaDatosHasta",
		     "CodigoVersionSII","HabilitadoMantenedor")
select
    'DJ1891' as "Esquema",
    '2021' as "Version",
    'TRI' as "Modulo",
    'A' as "Periodicidad",
    '' as "Objetivo",
    '2021-01-01' as "PeriodoInicio",
    'SII' as "IdEntidadReguladora",
    '2021-03-30' as "FechaPresentacionLimite",
    '2021-01-01' as "FechaDatosDesde",
    '2021-12-31' as "FechaDatosHasta",
    15 as "CodigoVersionSII",
    'S' as "HabilitadoMantenedor"
from dummy;


/*==============================================================*/
/* HOMOLOGACIONES                                               */
/*==============================================================*/

/*==============================================================*/
/* VALIDACIONES                                                 */
/*==============================================================*/
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" ("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
select
    'DJ1891' as "Esquema",
    '2020' as "Version",
    'H115' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'El número de documento debe ser mayor a cero' as "Descripcion",
    'E' as "Severidad",
    'N° de documento > 0' as "Formula"
from dummy;
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" ("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
select
    'DJ1891' as "Esquema",
    '2020' as "Version",
    'H116' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'El tipo de Documento debe indicarse con las letras:  F: Factura, C: Nota de Credito, D: Nota de Debito, O: Otro (no debe considera documentos nulos)' as "Descripcion",
    'E' as "Severidad",
    'Tipo Documento= {F .o. C .o. D .o. O}' as "Formula"
from dummy;
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" ("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
select
    'DJ1891' as "Esquema",
    '2020' as "Version",
    'H117' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'El tipo de transaccion debe ser alguno de los siguientes numeros: 0= Comisiones, 1= Acciones, 2= Dólares, 3= Oro, 4= Otras operaciones de Futuro, 5= Opciones ,  6= Documentos de Renta Fija, 7= Titulos Emitidos por Bolsa de Productos Agropecuarios sobre Certificados de Depositos de Productos, 8 = Cuotas de Fondos de Inversion, 9= Otros Instrumentos de Inversion, 10=  Aporte y Adquisicion de Valores (ETF - Exchange Traded Funds), 99= Otros que no sean Instrumentos de Inversion' as "Descripcion",
    'E' as "Severidad",
    'Tipo Transaccion={0.o.1.o.2.o. 3.o. 4.o. 5.o. 6.o. 7.o.8.o.9 .o.10.o. 99}' as "Formula"
from dummy;
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" ("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
select
    'DJ1891' as "Esquema",
    '2020' as "Version",
    'H1350' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'El campo Nemotecnico (C13 ) es un campo obligatorio en caso que el tipo de transaccion sea 1: Acciones o 8: Cuotas de Fondo de Inversion ' as "Descripcion",
    'E' as "Severidad",
    'Si C8 = 1.o. C8 = 8, entonces [C13] <> blanco. O. [C13] > 0' as "Formula"
from dummy;
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" ("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
select
    'DJ1891' as "Esquema",
    '2020' as "Version",
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

/*
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") select 'DJ1891' as "Esquema", '2020' as "Version", 'ID'              as "Nombre", '' as "CodigoSII", ''              as "TipoDiccionario",   1 as "Orden", 'Secuencia'            as "Titulo", 'Ayuda de Secuencia'            as "Ayuda", 'ID'            as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'S' as "Obligatorio", 'N' as "Editable", 'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'S' as "Comun", 'N' as "Clave", ''           as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") select 'DJ1891' as "Esquema", '2020' as "Version", 'RutCliente'      as "Nombre", '' as "CodigoSII", ''              as "TipoDiccionario",  11 as "Orden", 'RUT Cliente'          as "Titulo", 'Ayuda de RUT Cliente'          as "Ayuda", 'RUT_CLIENTE'   as "TipoDato",  40 as "Largo", ''        as "ValorDefecto", 'S' as "Obligatorio", 'N' as "Editable", 'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", ''           as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") select 'DJ1891' as "Esquema", '2020' as "Version", 'IdDocumento'     as "Nombre", '' as "CodigoSII", ''              as "TipoDiccionario",   2 as "Orden", 'Secuencia Docto'      as "Titulo", 'Ayuda de Secuencia Docto'      as "Ayuda", 'ID'            as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'S' as "Obligatorio", 'N' as "Editable", 'N' as "ModoMasivo", 'S' as "Exportable", 'S' as "OcultoEnGrilla", 'S' as "OcultoEnMantenedor", 'S' as "Comun", 'N' as "Clave", '2. Control' as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") select 'DJ1891' as "Esquema", '2020' as "Version", 'VboUsuario'      as "Nombre", '' as "CodigoSII", ''              as "TipoDiccionario",   3 as "Orden", 'VoBo'                 as "Titulo", 'Ayuda de VoBo'                 as "Ayuda", 'TEXT'          as "TipoDato",   1 as "Largo", 'N'       as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'S' as "Comun", 'N' as "Clave", '2. Control' as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") select 'DJ1891' as "Esquema", '2020' as "Version", 'Observaciones'   as "Nombre", '' as "CodigoSII", ''              as "TipoDiccionario",   4 as "Orden", 'Observaciones'        as "Titulo", 'Ayuda de Observaciones'        as "Ayuda", 'TEXT'          as "TipoDato", 250 as "Largo", ''        as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'S' as "Comun", 'N' as "Clave", '2. Control' as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") select 'DJ1891' as "Esquema", '2020' as "Version", 'FuenteDatos'     as "Nombre", '' as "CodigoSII", ''              as "TipoDiccionario",   5 as "Orden", 'Fuente'               as "Titulo", 'Ayuda de Fuente'               as "Ayuda", 'TEXT'          as "TipoDato",  40 as "Largo", 'INICIAL' as "ValorDefecto", 'N' as "Obligatorio", 'N' as "Editable", 'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'S' as "Comun", 'N' as "Clave", '2. Control' as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") select 'DJ1891' as "Esquema", '2020' as "Version", 'TipoDocumento'   as "Nombre", '' as "CodigoSII", 'TipoDocu'      as "TipoDiccionario",   6 as "Orden", 'Tipo Documento'       as "Titulo", 'Ayuda de Tipo Documento'       as "Ayuda", 'NEMO'          as "TipoDato",   0 as "Largo", ''        as "ValorDefecto", 'S' as "Obligatorio", 'S' as "Editable", 'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'S' as "Clave", '1. Datos'   as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") select 'DJ1891' as "Esquema", '2020' as "Version", 'NumeroDocumento' as "Nombre", '' as "CodigoSII", ''              as "TipoDiccionario",   7 as "Orden", 'Numero Documento'     as "Titulo", 'Ayuda de Numero Documento'     as "Ayuda", 'ID'            as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'S' as "Obligatorio", 'S' as "Editable", 'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'S' as "Clave", '1. Datos'   as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") select 'DJ1891' as "Esquema", '2020' as "Version", 'FechaDocumento'  as "Nombre", '' as "CodigoSII", ''              as "TipoDiccionario",   8 as "Orden", 'Fecha Documento'      as "Titulo", 'Ayuda de Fecha Documento'      as "Ayuda", 'DATE_FILTER'   as "TipoDato",   0 as "Largo", ''        as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos'   as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") select 'DJ1891' as "Esquema", '2020' as "Version", 'RutEmisor'       as "Nombre", '' as "CodigoSII", ''              as "TipoDiccionario",   9 as "Orden", 'RUT Emisor'           as "Titulo", 'Ayuda de RUT Emisor'           as "Ayuda", 'RUT'           as "TipoDato",  40 as "Largo", ''        as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos'   as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") select 'DJ1891' as "Esquema", '2020' as "Version", 'Nemotecnico'     as "Nombre", '' as "CodigoSII", ''              as "TipoDiccionario",  10 as "Orden", 'Instrumento'          as "Titulo", 'Ayuda de Instrumento'          as "Ayuda", 'NEMO'          as "TipoDato",  40 as "Largo", ''        as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos'   as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") select 'DJ1891' as "Esquema", '2020' as "Version", 'Cantidad'        as "Nombre", '' as "CodigoSII", ''              as "TipoDiccionario",  12 as "Orden", 'Cantidad'             as "Titulo", 'Ayuda de Cantidad'             as "Ayuda", 'QTY'           as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'S' as "Obligatorio", 'S' as "Editable", 'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos'   as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") select 'DJ1891' as "Esquema", '2020' as "Version", 'Monto'           as "Nombre", '' as "CodigoSII", ''              as "TipoDiccionario",  13 as "Orden", 'Monto'                as "Titulo", 'Ayuda de Monto'                as "Ayuda", 'AMOUNT'        as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'S' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos'   as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") select 'DJ1891' as "Esquema", '2020' as "Version", 'TipoTransaccion' as "Nombre", '' as "CodigoSII", 'TipoTransac'   as "TipoDiccionario",  14 as "Orden", 'Tipo Transaccion'     as "Titulo", 'Ayuda de Tipo Transaccion'     as "Ayuda", 'NEMO'          as "TipoDato",   0 as "Largo", ''        as "ValorDefecto", 'S' as "Obligatorio", 'S' as "Editable", 'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos'   as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") select 'DJ1891' as "Esquema", '2020' as "Version", 'IdTipoOperacion' as "Nombre", '' as "CodigoSII", 'TipoOperacion' as "TipoDiccionario",  15 as "Orden", 'Tipo Operacion'       as "Titulo", 'Ayuda de Tipo Operacion'       as "Ayuda", 'NEMO'          as "TipoDato",   0 as "Largo", ''        as "ValorDefecto", 'S' as "Obligatorio", 'S' as "Editable", 'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos'   as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") select 'DJ1891' as "Esquema", '2020' as "Version", 'IdTipoProducto'  as "Nombre", '' as "CodigoSII", 'TipoProducto'  as "TipoDiccionario",  16 as "Orden", 'Tipo Producto'        as "Titulo", 'Ayuda de Tipo Producto'        as "Ayuda", 'NEMO'          as "TipoDato",   0 as "Largo", ''        as "ValorDefecto", 'S' as "Obligatorio", 'S' as "Editable", 'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos'   as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") select 'DJ1891' as "Esquema", '2020' as "Version", 'IdBaseDocumento' as "Nombre", '' as "CodigoSII", ''              as "TipoDiccionario",  17 as "Orden", 'Secuencia Base'       as "Titulo", 'Ayuda de Secuencia Base'       as "Ayuda", 'ID'            as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'N' as "Obligatorio", 'N' as "Editable", 'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'S' as "Clave", '1. Datos'   as "Grupo",  4 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") select 'DJ1891' as "Esquema", '2020' as "Version", 'IdCliente'       as "Nombre", '' as "CodigoSII", ''              as "TipoDiccionario",  18 as "Orden", 'Secuencia Cliente'    as "Titulo", 'Ayuda de Secuencia Cliente'    as "Ayuda", 'ID_CLIENTE'    as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'S' as "Obligatorio", 'N' as "Editable", 'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos'   as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") select 'DJ1891' as "Esquema", '2020' as "Version", 'IdPortafolio'    as "Nombre", '' as "CodigoSII", ''              as "TipoDiccionario",  19 as "Orden", 'Secuencia Portafolio' as "Titulo", 'Ayuda de Secuencia Portafolio' as "Ayuda", 'ID_PORTAFOLIO' as "TipoDato",   0 as "Largo", '0'       as "ValorDefecto", 'S' as "Obligatorio", 'N' as "Editable", 'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '1. Datos'   as "Grupo",  0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
*/


/* 2020 */
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2020', 'ID', '', '', 1, 'Secuencia', 'Ayuda de Secuencia', 'ID', 0, '0', 'S', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2020', 'IdDocumento', '', '', 2, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'S', 'N', 'N', 'N', 'S', 'S', 'S', 'N', '2. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2020', 'VboUsuario', '', '', 3, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, 'N', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '2. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2020', 'Observaciones', '', '', 4, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '2. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2020', 'FuenteDatos', '', '', 5, 'Fuente', 'Ayuda de Fuente', 'TEXT', 40, 'INICIAL', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '2. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2020', 'TipoDocumento', 'C2', 'TipoDocu', 6, 'Tipo Documento', 'Ayuda de Tipo Documento', 'NEMO', 0, '', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos', 1, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2020', 'NumeroDocumento', 'C3', '', 7, 'Numero Documento', 'Ayuda de Numero Documento', 'ID', 0, '0', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos', 2, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2020', 'FechaDocumento', 'C4', '', 8, 'Fecha Documento', 'Ayuda de Fecha Documento', 'DATE_FILTER', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos', 3, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2020', 'RutEmisor', 'C5', '', 9, 'Rut Emisor', 'Ayuda de RUT Emisor', 'RUT', 40, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos', 4, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2020', 'Nemotecnico', 'C13', '', 10, 'Instrumento', 'Ayuda de Instrumento', 'NEMO', 40, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos', 5, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2020', 'RutCliente', 'C15', '', 11, 'Rut Cliente', 'Ayuda de RUT Cliente', 'RUT_CLIENTE', 40, '', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos', 6, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2020', 'Cantidad', 'C14', '', 12, 'Cantidad', 'Ayuda de Cantidad', 'QTY', 0, '0', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos', 7, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2020', 'MontoCompras', 'C6', '', 13, 'Compras', ' ', 'AMOUNT', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos', 8, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2020', 'MontoVentas', 'C7', '', 14, 'Ventas', ' ', 'AMOUNT', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos', 9, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2020', 'TipoTransaccion', 'C8', 'TipoTransac', 15, 'Tipo Transaccion', 'Ayuda de Tipo Transaccion', 'NEMO', 0, '', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos', 10, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2020', 'Cuenta', '', '', 16, 'Cuenta', '', 'TEXT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos', 11, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2020', 'IdTipoProducto', '', 'TipoProducto', 17, 'Tipo Producto', 'Ayuda de Tipo Producto', 'NEMO', 0, '', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2020', 'IdBaseDocumento', '', '', 18, 'Secuencia Base', 'Ayuda de Secuencia Base', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'N', 'N', '1. Datos', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2020', 'IdCliente', '', '', 19, 'Secuencia Cliente', 'Ayuda de Secuencia Cliente', 'ID_CLIENTE', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'N', 'N', '1. Datos', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2020', 'IdPortafolio', '', '', 20, 'Secuencia Portafolio', 'Ayuda de Secuencia Portafolio', 'ID_PORTAFOLIO', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'N', 'N', '1. Datos', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2020', 'IdTipoOperacion', '', 'TipoOperacion', 21, 'Tipo Operacion', 'Ayuda de Tipo Operacion', 'NEMO', 0, '', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2020', 'LineaFactura', '', '', 22, 'LineaFactura', '', 'AMOUNT', 0, '0', 'N', 'N', 'N', 'S', 'S', 'N', 'N', 'N', '2. Control', 0, 0);


/* 2021 */
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2021', 'ID', '', '', 1, 'Secuencia', 'Ayuda de Secuencia', 'ID', 0, '0', 'S', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2021', 'IdDocumento', '', '', 2, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'S', 'N', 'N', 'N', 'S', 'S', 'S', 'N', '2. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2021', 'VboUsuario', '', '', 3, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, 'N', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '2. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2021', 'Observaciones', '', '', 4, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '2. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2021', 'FuenteDatos', '', '', 5, 'Fuente', 'Ayuda de Fuente', 'TEXT', 40, 'INICIAL', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '2. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2021', 'TipoDocumento', 'C2', 'TipoDocu', 6, 'Tipo Documento', 'Ayuda de Tipo Documento', 'NEMO', 0, '', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos', 1, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2021', 'NumeroDocumento', 'C3', '', 7, 'Numero Documento', 'Ayuda de Numero Documento', 'ID', 0, '0', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos', 2, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2021', 'FechaDocumento', 'C4', '', 8, 'Fecha Documento', 'Ayuda de Fecha Documento', 'DATE_FILTER', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos', 3, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2021', 'RutEmisor', 'C5', '', 9, 'Rut Emisor', 'Ayuda de RUT Emisor', 'RUT', 40, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos', 4, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2021', 'Nemotecnico', 'C13', '', 10, 'Instrumento', 'Ayuda de Instrumento', 'NEMO', 40, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos', 5, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2021', 'RutCliente', 'C15', '', 11, 'Rut Cliente', 'Ayuda de RUT Cliente', 'RUT_CLIENTE', 40, '', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos', 6, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2021', 'Cantidad', 'C14', '', 12, 'Cantidad', 'Ayuda de Cantidad', 'QTY', 0, '0', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos', 7, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2021', 'MontoCompras', 'C6', '', 13, 'Compras', ' ', 'AMOUNT', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos', 8, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2021', 'MontoVentas', 'C7', '', 14, 'Ventas', ' ', 'AMOUNT', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos', 9, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2021', 'TipoTransaccion', 'C8', 'TipoTransac', 15, 'Tipo Transaccion', 'Ayuda de Tipo Transaccion', 'NEMO', 0, '', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos', 10, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2021', 'Cuenta', '', '', 16, 'Cuenta', '', 'TEXT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos', 11, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2021', 'IdTipoProducto', '', 'TipoProducto', 17, 'Tipo Producto', 'Ayuda de Tipo Producto', 'NEMO', 0, '', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2021', 'IdBaseDocumento', '', '', 18, 'Secuencia Base', 'Ayuda de Secuencia Base', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'N', 'N', '1. Datos', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2021', 'IdCliente', '', '', 19, 'Secuencia Cliente', 'Ayuda de Secuencia Cliente', 'ID_CLIENTE', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'N', 'N', '1. Datos', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2021', 'IdPortafolio', '', '', 20, 'Secuencia Portafolio', 'Ayuda de Secuencia Portafolio', 'ID_PORTAFOLIO', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'N', 'N', '1. Datos', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2021', 'IdTipoOperacion', '', 'TipoOperacion', 21, 'Tipo Operacion', 'Ayuda de Tipo Operacion', 'NEMO', 0, '', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")VALUES('DJ1891', '2021', 'LineaFactura', '', '', 22, 'LineaFactura', '', 'AMOUNT', 0, '0', 'N', 'N', 'N', 'S', 'S', 'N', 'N', 'N', '2. Control', 0, 0);


/*==============================================================*/
/* CUADRATURAS                                                  */
/*==============================================================*/

insert into "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"
select
    'DJ1891' as "Esquema",
    '2020' as "Version",
    'PorRutCliente' as "Tipo",
    'Cuadratura por RUT Cliente' as "Descripcion"
from dummy;

insert into "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura"
select
    'DJ1891' as "Esquema",
    '2020' as "Version",
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
    '2020' as "Version",
    'PorCompras' as "Tipo",
    'Compras' as "Descripcion"
from dummy;

insert into "BSCL"."BSCL.Normativa.BD::DOC.CampoClasificador"
select
    'DJ1891' as "Esquema",
    '2020' as "Version",
    'PorCompras' as "TipoClasificador",
    'IdTipoOperacion' as "NombreCampo",
    'CO' as "ValorClasificador"
from dummy;

/*==============================================================*/
/* DOCUMENTOS                                                   */
/*==============================================================*/

insert into "BSCL.Normativa.BD::DOC.Documento"
("IdDocumento","IdTipoDocumento","Esquema","Version","IdEmpresa","FechaLimite","IdEstadoDocumento",
"FechaCreacion","FechaPresentacion","DocumentoIdentidadDeclarante",
"DocumentoIdentidadRepresentante","DireccionElectronicaDeclaracion",
"DireccionFisicaDeclaracion","ComunaDeclaracion","TelefonoDeclaracion",
"FolioDeclaracion","EnProceso","FechaInicioUltimoProceso","AnoMes")
SELECT "BSCL"."BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL AS "IdDocumento",
'DECLACORR' AS "IdTipoDocumento"
,'DJ1891' AS "Esquema"
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


insert into "BSCL.Normativa.BD::DOC.Documento"
("IdDocumento","IdTipoDocumento","Esquema","Version","IdEmpresa","FechaLimite","IdEstadoDocumento",
"FechaCreacion","FechaPresentacion","DocumentoIdentidadDeclarante",
"DocumentoIdentidadRepresentante","DireccionElectronicaDeclaracion",
"DireccionFisicaDeclaracion","ComunaDeclaracion","TelefonoDeclaracion",
"FolioDeclaracion","EnProceso","FechaInicioUltimoProceso","AnoMes")
SELECT "BSCL"."BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL AS "IdDocumento",
'DECLACORR' AS "IdTipoDocumento"
,'DJ1891' AS "Esquema"
,'2021' AS "Version"
,1 AS "IdEmpresa"
,'2021-12-31' AS "FechaLimite"
,'ACT' AS "IdEstadoDocumento"
,'2021-01-01 0:00:00.0' AS "FechaCreacion"
,'2021-12-31 0:00:00.0' AS "FechaPresentacion"
,"DocumentoIdentidad" AS "DocumentoIdentidadDeclarante"
,"DocumentoIdentidadRepresentante" AS "DocumentoIdentidadRepresentante"
,"DireccionElectronicaPrincipal" AS "DireccionElectronicaDeclaracion"
, rtrim("DireccionFisicaPrincipalCalle")||' '|| "DireccionFisicaPrincipalNumero" AS "DireccionFisicaDeclaracion"
, "DireccionFisicaPrincipalNombreDivisionPais" AS "ComunaDeclaracion"
,"TelefonoPrincipal" AS "TelefonoDeclaracion"
,'I9153103' AS "FolioDeclaracion"
,'N' AS "EnProceso"
,'2021-12-02 21:35:09.444' AS "FechaInicioUltimoProceso"
,''AS "AnoMes" FROM (
        SELECT top 1 * FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" ORDER BY "IdEmpresa"
) AS "EMP";

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

INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Parametro" (
"Esquema","Version","Codigo","Descripcion","Valor","EsModificableUsuario","Uso","ValorDefecto","ValoresPosibles","ModificablePorUsuario",
"ModificablePostProduccion","CreadorParametro","FechaCreacion","Observaciones","IdEmpresa")
SELECT  
  'DJ1891',"Version",'ExcluyePortafoliosAPV','Excluye portafolios con proposito APV','S','N','','N','S/N','N','N','',CURRENT_UTCTIMESTAMP,'',NULL
FROM DUMMY,(SELECT DISTINCT "Version" FROM "BSCL"."BSCL.Normativa.BD::DOC.Documento" WHERE "Esquema" = 'DJ1891');

-- CONTROL PROCESO
insert into "BSCL"."BSCL.Normativa.BD::TRI.ControlProceso"
("Esquema","Version","EstadoCertificado","Carga","Validacion","PreDeclaracion","Certificacion")
SELECT "Esquema","Version",'NGE','N','N','N','N' FROM "BSCL"."BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1891' AND "Version" IN ('2020');


