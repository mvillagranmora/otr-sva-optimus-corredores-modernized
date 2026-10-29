set schema "BSCL";


/*==============================================================*/
/* DELETE ESQUEMAS                                              */
/*==============================================================*/

delete from "BSCL"."BSCL.Normativa.BD::DOC.Esquema" 
where "Esquema" in ('NCG380Aut', 'NCG380Cli', 'NCG380Ope', 'NCG380Fac', 'NCG380Ord', 'NCG380Asig', 'NCG380Tra', 'NCG380Cus','NCG380Rep','NCG380Ins');

/*==============================================================*/
/* ESQUEMAS                                                     */
/*==============================================================*/

--clientes
insert into "BSCL"."BSCL.Normativa.BD::DOC.Esquema"
select
    'NCG380Cli' as "Esquema",
    'V1' as "Version",
    'A' as "Periodicidad",
    '' as "Objetivo",
    '' as "PeriodoInicio",
    'SVS' as "IdEntidadReguladora",
    '2017-12-31' as "FechaPresentacionLimite",
    '2016-01-01' as "FechaDatosDesde",
    '9999-12-31' as "FechaDatosHasta",
	1 as "CodigoVersionSII",
	'NOR' as "Modulo",
	'N' as "HabilitadoMantenedor"
from dummy;


-- ordenes

insert into "BSCL"."BSCL.Normativa.BD::DOC.Esquema"
select
    'NCG380Ord' as "Esquema",
    'V1' as "Version",
    'A' as "Periodicidad",
    '' as "Objetivo",
    '' as "PeriodoInicio",
    'SVS' as "IdEntidadReguladora",
    '2017-12-31' as "FechaPresentacionLimite",
    '2016-01-01' as "FechaDatosDesde",
    '9999-12-31' as "FechaDatosHasta",
	1 as "CodigoVersionSII",
	'NOR' as "Modulo",
	'N' as "HabilitadoMantenedor"
from dummy;

--asignaciones

insert into "BSCL"."BSCL.Normativa.BD::DOC.Esquema"
select
    'NCG380Asig' as "Esquema",
    'V1' as "Version",
    'A' as "Periodicidad",
    '' as "Objetivo",
    '' as "PeriodoInicio",
    'SVS' as "IdEntidadReguladora",
    '2017-12-31' as "FechaPresentacionLimite",
    '2016-01-01' as "FechaDatosDesde",
    '9999-12-31' as "FechaDatosHasta",
	1 as "CodigoVersionSII",
	'NOR' as "Modulo",
	'N' as "HabilitadoMantenedor"
from dummy;

--facturacion
insert into "BSCL"."BSCL.Normativa.BD::DOC.Esquema"
select
    'NCG380Fac' as "Esquema",
    'V1' as "Version",
    'A' as "Periodicidad",
    '' as "Objetivo",
    '' as "PeriodoInicio",
    'SVS' as "IdEntidadReguladora",
    '2017-12-31' as "FechaPresentacionLimite",
    '2016-01-01' as "FechaDatosDesde",
    '9999-12-31' as "FechaDatosHasta",
	1 as "CodigoVersionSII",
	'NOR' as "Modulo",
	'N' as "HabilitadoMantenedor"
from dummy;

--custodia 
insert into "BSCL"."BSCL.Normativa.BD::DOC.Esquema"
select
    'NCG380Cus' as "Esquema",
    'V1' as "Version",
    'A' as "Periodicidad",
    '' as "Objetivo",
    '' as "PeriodoInicio",
    'SVS' as "IdEntidadReguladora",
    '2017-12-31' as "FechaPresentacionLimite",
    '2016-01-01' as "FechaDatosDesde",
    '9999-12-31' as "FechaDatosHasta",
	1 as "CodigoVersionSII",
	'NOR' as "Modulo",
	'N' as "HabilitadoMantenedor"
from dummy;


--Autorizados

insert into "BSCL"."BSCL.Normativa.BD::DOC.Esquema"
select
    'NCG380Aut' as "Esquema",
    'V1' as "Version",
    'A' as "Periodicidad",
    '' as "Objetivo",
    '' as "PeriodoInicio",
    'SVS' as "IdEntidadReguladora",
    '2017-12-31' as "FechaPresentacionLimite",
    '2016-01-01' as "FechaDatosDesde",
    '9999-12-31' as "FechaDatosHasta",
	1 as "CodigoVersionSII",
	'NOR' as "Modulo",
	'N' as "HabilitadoMantenedor"
from dummy;


--Trabajadores

insert into "BSCL"."BSCL.Normativa.BD::DOC.Esquema"
select
    'NCG380Tra' as "Esquema",
    'V1' as "Version",
    'A' as "Periodicidad",
    '' as "Objetivo",
    '' as "PeriodoInicio",
    'SVS' as "IdEntidadReguladora",
    '2017-12-31' as "FechaPresentacionLimite",
    '2016-01-01' as "FechaDatosDesde",
    '9999-12-31' as "FechaDatosHasta",
	1 as "CodigoVersionSII",
	'NOR' as "Modulo",
	'N' as "HabilitadoMantenedor"
from dummy;


--Operaciones

insert into "BSCL"."BSCL.Normativa.BD::DOC.Esquema"
select
    'NCG380Ope' as "Esquema",
    'V1' as "Version",
    'A' as "Periodicidad",
    '' as "Objetivo",
    '' as "PeriodoInicio",
    'SVS' as "IdEntidadReguladora",
    '2017-12-31' as "FechaPresentacionLimite",
    '2016-01-01' as "FechaDatosDesde",
    '9999-12-31' as "FechaDatosHasta",
	1 as "CodigoVersionSII",
	'NOR' as "Modulo",
	'N' as "HabilitadoMantenedor"
from dummy;

--Instrucciones

insert into "BSCL"."BSCL.Normativa.BD::DOC.Esquema"
select
    'NCG380Ins' as "Esquema",
    '2017' as "Version",
    'NOR' as "Modulo",
    'A' as "Periodicidad",
    '' as "Objetivo",
    '' as "PeriodoInicio",
    'SVS' as "IdEntidadReguladora",
    '2017-12-31' as "FechaPresentacionLimite",
    '2016-01-01' as "FechaDatosDesde",
    '9999-12-31' as "FechaDatosHasta",
	1 as "CodigoVersionSII",
	'N' AS "HabilitadoMantenedor"
from dummy;

--Repactados

insert into "BSCL.Normativa.BD::DOC.Esquema"
("Esquema","Version","Modulo","Periodicidad","Objetivo","PeriodoInicio","IdEntidadReguladora","FechaPresentacionLimite","FechaDatosDesde","FechaDatosHasta","CodigoVersionSII","HabilitadoMantenedor")
values
('NCG380Rep','2017','NOR','M','','2017-01-01','SVS','2017-12-31','2017-01-01','2017-12-31',1,'N');

/*==============================================================*/
/* DELETE CAMPOS                                                */
/*==============================================================*/

delete from "BSCL"."BSCL.Normativa.BD::DOC.Campo" 
where "Esquema" in ('NCG380Aut', 'NCG380Cli', 'NCG380Ope', 'NCG380Fac', 'NCG380Ord', 'NCG380Asig', 'NCG380Tra', 'NCG380Cus','NCG380Rep','NCG380Ins');


/*==============================================================*/
/* CAMPOS                                                       */
/*==============================================================*/

--CLIENTE
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'NumeroCliente' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 2 as "Orden", 'NumeroCliente' as "Titulo", 'Ayuda de	NumeroCliente' as "Ayuda", 'ID' as "TipoDato", 0 as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'Rut' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 3 as "Orden", 'Rut' as "Titulo", 'Ayuda de	Rut' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'Dig' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 4 as "Orden", 'Dig' as "Titulo", 'Ayuda de	Dig' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'Pasaporte' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 5 as "Orden", 'Pasaporte' as "Titulo", 'Ayuda de	Pasaporte' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'Glei' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 6 as "Orden", 'Glei' as "Titulo", 'Ayuda de	Glei' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ApellidoPaterno' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 7 as "Orden", 'ApellidoPaterno' as "Titulo", 'Ayuda de	ApellidoPaterno' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ApellidoMaterno' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 8 as "Orden", 'ApellidoMaterno' as "Titulo", 'Ayuda de	ApellidoMaterno' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'PrimerNombre' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 9 as "Orden", 'PrimerNombre' as "Titulo", 'Ayuda de	PrimerNombre' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'SegundoNombre' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 10 as "Orden", 'SegundoNombre' as "Titulo", 'Ayuda de	SegundoNombre' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'RazonSocial' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 11 as "Orden", 'RazonSocial' as "Titulo", 'Ayuda de	RazonSocial' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'Telefono' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 12 as "Orden", 'Telefono' as "Titulo", 'Ayuda de	Telefono' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'Email' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 13 as "Orden", 'Email' as "Titulo", 'Ayuda de	Email' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'DireccionTipo' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 14 as "Orden", 'DireccionTipo' as "Titulo", 'Ayuda de	DireccionTipo' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'DireccionNombre' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 15 as "Orden", 'DireccionNombre' as "Titulo", 'Ayuda de	DireccionNombre' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'DireccionNumero' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 16 as "Orden", 'DireccionNumero' as "Titulo", 'Ayuda de	DireccionNumero' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'DireccionDepto' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 17 as "Orden", 'DireccionDepto' as "Titulo", 'Ayuda de	DireccionDepto' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'DireccionComuna' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 18 as "Orden", 'DireccionComuna' as "Titulo", 'Ayuda de	DireccionComuna' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'DireccionCiudad' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 19 as "Orden", 'DireccionCiudad' as "Titulo", 'Ayuda de	DireccionCiudad' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'DireccionEstado' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 20 as "Orden", 'DireccionEstado' as "Titulo", 'Ayuda de	DireccionEstado' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'DireccionPais' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 21 as "Orden", 'DireccionPais' as "Titulo", 'Ayuda de	DireccionPais' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'CodigoPostal' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 22 as "Orden", 'CodigoPostal' as "Titulo", 'Ayuda de	CodigoPostal' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'Nacionalidad' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 23 as "Orden", 'Nacionalidad' as "Titulo", 'Ayuda de	Nacionalidad' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'RelacionIntermediario' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 24 as "Orden", 'RelacionIntermediario' as "Titulo", 'Ayuda de	RelacionIntermediario' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'AdministracionCartera' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 25 as "Orden", 'AdministracionCartera' as "Titulo", 'Ayuda de	AdministracionCartera' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'RutEntidadEmpleadora' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 26 as "Orden", 'RutEntidadEmpleadora' as "Titulo", 'Ayuda de	RutEntidadEmpleadora' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'DigEntidadEmpleadora' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 27 as "Orden", 'DigEntidadEmpleadora' as "Titulo", 'Ayuda de	DigEntidadEmpleadora' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'RazonSocialEntidadEmpleadora' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 28 as "Orden", 'RazonSocialEntidadEmpleadora' as "Titulo", 'Ayuda de RazonSocialEntidadEmpleadora' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'EstadoCivil' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 29 as "Orden", 'EstadoCivil' as "Titulo", 'Ayuda de	EstadoCivil' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyRut' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 30 as "Orden", 'ConyRut' as "Titulo", 'Ayuda de	ConyRut' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyDig' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 31 as "Orden", 'ConyDig' as "Titulo", 'Ayuda de	ConyDig' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyPasaporte' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 32 as "Orden", 'ConyPasaporte' as "Titulo", 'Ayuda de	ConyPasaporte' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyApellidoPaterno' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 33 as "Orden", 'ConyApellidoPaterno' as "Titulo", 'Ayuda de	ConyApellidoPaterno' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyApellidoMaterno' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 34 as "Orden", 'ConyApellidoMaterno' as "Titulo", 'Ayuda de	ConyApellidoMaterno' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyPrimerNombre' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 35 as "Orden", 'ConyPrimerNombre' as "Titulo", 'Ayuda de	ConyPrimerNombre' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConySegundoNombre' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 36 as "Orden", 'ConySegundoNombre' as "Titulo", 'Ayuda de	ConySegundoNombre' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyRazonSocial' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 37 as "Orden", 'ConyRazonSocial' as "Titulo", 'Ayuda de	ConyRazonSocial' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyTelefono' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 38 as "Orden", 'ConyTelefono' as "Titulo", 'Ayuda de ConyTelefono' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyEmail' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 39 as "Orden", 'ConyEmail' as "Titulo", 'Ayuda de	ConyEmail' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyDireccionTipo' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 40 as "Orden", 'ConyDireccionTipo' as "Titulo", 'Ayuda de	ConyDireccionTipo' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyDireccionNombre' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 41 as "Orden", 'ConyDireccionNombre' as "Titulo", 'Ayuda de	ConyDireccionNombre' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyDireccionNumero' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 42 as "Orden", 'ConyDireccionNumero' as "Titulo", 'Ayuda de	ConyDireccionNumero' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyDireccionDepto' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 43 as "Orden", 'ConyDireccionDepto' as "Titulo", 'Ayuda de	ConyDireccionDepto' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyDireccionComuna' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 44 as "Orden", 'ConyDireccionComuna' as "Titulo", 'Ayuda de	ConyDireccionComuna' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyDireccionCiudad' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 45 as "Orden", 'ConyDireccionCiudad' as "Titulo", 'Ayuda de	ConyDireccionCiudad' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyDireccionEstado' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 46 as "Orden", 'ConyDireccionEstado' as "Titulo", 'Ayuda de	ConyDireccionEstado' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyDireccionPais' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 47 as "Orden", 'ConyDireccionPais' as "Titulo", 'Ayuda de	ConyDireccionPais' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyCodigoPostal' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 48 as "Orden", 'ConyCodigoPostal' as "Titulo", 'Ayuda de	ConyCodigoPostal' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyNacionalidad' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 49 as "Orden", 'ConyNacionalidad' as "Titulo", 'Ayuda de	ConyNacionalidad' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyRelacionIntermediario' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 50 as "Orden", 'ConyRelacionIntermediario' as "Titulo", 'Ayuda de	ConyRelacionIntermediario' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'RepRut' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 51 as "Orden", 'RepRut' as "Titulo", 'Ayuda de	RepRut' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'RepDig' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 52 as "Orden", 'RepDig' as "Titulo", 'Ayuda de	RepDig' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'RepPasaporte' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 53 as "Orden", 'RepPasaporte' as "Titulo", 'Ayuda de	RepPasaporte' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'RepGlei' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 54 as "Orden", 'RepGlei' as "Titulo", 'Ayuda de	RepGlei' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'RepApellidoPaterno' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 55 as "Orden", 'RepApellidoPaterno' as "Titulo", 'Ayuda de	RepApellidoPaterno' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'RepApellidoMaterno' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 56 as "Orden", 'RepApellidoMaterno' as "Titulo", 'Ayuda de	RepApellidoMaterno' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'RepPrimerNombre' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 57 as "Orden", 'RepPrimerNombre' as "Titulo", 'Ayuda de	RepPrimerNombre' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'RepSegundoNombre' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 58 as "Orden", 'RepSegundoNombre' as "Titulo", 'Ayuda de	RepSegundoNombre' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'RepRazonSocial' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 59 as "Orden", 'RepRazonSocial' as "Titulo", 'Ayuda de	RepRazonSocial' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'RepTelefono' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 60 as "Orden", 'RepTelefono' as "Titulo", 'Ayuda de	RepTelefono' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'RepEmail' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 61 as "Orden", 'RepEmail' as "Titulo", 'Ayuda de	RepEmail' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'RepDireccionTipo' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 62 as "Orden", 'RepDireccionTipo' as "Titulo", 'Ayuda de	RepDireccionTipo' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'RepDireccionNombre' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 63 as "Orden", 'RepDireccionNombre' as "Titulo", 'Ayuda de	RepDireccionNombre' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'RepDireccionNumero' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 64 as "Orden", 'RepDireccionNumero' as "Titulo", 'Ayuda de	RepDireccionNumero' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'RepDireccionDepto' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 65 as "Orden", 'RepDireccionDepto' as "Titulo", 'Ayuda de	RepDireccionDepto' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'RepDireccionComuna' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 66 as "Orden", 'RepDireccionComuna' as "Titulo", 'Ayuda de	RepDireccionComuna' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'RepDireccionCiudad' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 67 as "Orden", 'RepDireccionCiudad' as "Titulo", 'Ayuda de	RepDireccionCiudad' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'RepDireccionProvincia' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 68 as "Orden", 'RepDireccionProvincia' as "Titulo", 'Ayuda de	RepDireccionProvincia' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'RepDireccionPais' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 69 as "Orden", 'RepDireccionPais' as "Titulo", 'Ayuda de	RepDireccionPais' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'RepCodigoPostal' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 70 as "Orden", 'RepCodigoPostal' as "Titulo", 'Ayuda de	RepCodigoPostal' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'RepNacionalidad' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 71 as "Orden", 'RepNacionalidad' as "Titulo", 'Ayuda de	RepNacionalidad' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'RepRelacionIntermediario' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 72 as "Orden", 'RepRelacionIntermediario' as "Titulo", 'Ayuda de	RepRelacionIntermediario' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'RepEstadoCivil' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 73 as "Orden", 'RepEstadoCivil' as "Titulo", 'Ayuda de	RepEstadoCivil' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyRepRut' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 74 as "Orden", 'ConyRepRut' as "Titulo", 'Ayuda de	ConyRepRut' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyRepDig' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 75 as "Orden", 'ConyRepDig' as "Titulo", 'Ayuda de	ConyRepDig' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyRepPasaporte' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 76 as "Orden", 'ConyRepPasaporte' as "Titulo", 'Ayuda de	ConyRepPasaporte' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyRepApellidoPaterno' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 77 as "Orden", 'ConyRepApellidoPaterno' as "Titulo", 'Ayuda de	ConyRepApellidoPaterno' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyRepApellidoMaterno' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 78 as "Orden", 'ConyRepApellidoMaterno' as "Titulo", 'Ayuda de	ConyRepApellidoMaterno' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyRepPrimerNombre' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 79 as "Orden", 'ConyRepPrimerNombre' as "Titulo", 'Ayuda de	ConyRepPrimerNombre' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyRepSegundoNombre' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 80 as "Orden", 'ConyRepSegundoNombre' as "Titulo", 'Ayuda de	ConyRepSegundoNombre' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyRepRazonSocial' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 81 as "Orden", 'ConyRepRazonSocial' as "Titulo", 'Ayuda de	ConyRepRazonSocial' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyRepTelefono' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 82 as "Orden", 'ConyRepTelefono' as "Titulo", 'Ayuda de	ConyRepTelefono' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyRepEmail' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 83 as "Orden", 'ConyRepEmail' as "Titulo", 'Ayuda de	ConyRepEmail' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyRepDireccionTipo' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 84 as "Orden", 'ConyRepDireccionTipo' as "Titulo", 'Ayuda de	ConyRepDireccionTipo' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyRepDireccionNombre' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 85 as "Orden", 'ConyRepDireccionNombre' as "Titulo", 'Ayuda de	ConyRepDireccionNombre' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyRepDireccionNumero' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 86 as "Orden", 'ConyRepDireccionNumero' as "Titulo", 'Ayuda de	ConyRepDireccionNumero' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyRepDireccionDepto' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 87 as "Orden", 'ConyRepDireccionDepto' as "Titulo", 'Ayuda de	ConyRepDireccionDepto' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyRepDireccionComuna' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 88 as "Orden", 'ConyRepDireccionComuna' as "Titulo", 'Ayuda de	ConyRepDireccionComuna' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyRepDireccionCiudad' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 89 as "Orden", 'ConyRepDireccionCiudad' as "Titulo", 'Ayuda de	ConyRepDireccionCiudad' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyRepDireccionEstado' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 90 as "Orden", 'ConyRepDireccionEstado' as "Titulo", 'Ayuda de	ConyRepDireccionEstado' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyRepDireccionPais' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 91 as "Orden", 'ConyRepDireccionPais' as "Titulo", 'Ayuda de	ConyRepDireccionPais' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyRepCodigoPostal' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 92 as "Orden", 'ConyRepCodigoPostal' as "Titulo", 'Ayuda de	ConyRepCodigoPostal' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyRepNacionalidad' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 93 as "Orden", 'ConyRepNacionalidad' as "Titulo", 'Ayuda de	ConyRepNacionalidad' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ConyRepRelacionIntermediario' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 94 as "Orden", 'ConyRepRelacionIntermediario' as "Titulo", 'Ayuda de	ConyRepRelacionIntermediario' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'CuentaBancariaUno' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 95 as "Orden", 'CuentaBancariaUno' as "Titulo", 'Ayuda de	CuentaBancariaUno' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'CuentaBancariaDos' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 96 as "Orden", 'CuentaBancariaDos' as "Titulo", 'Ayuda de	CuentaBancariaDos' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'CuentaBancariaTres' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 97 as "Orden", 'CuentaBancariaTres' as "Titulo", 'Ayuda de	CuentaBancariaTres' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'ClaseOrden' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 98 as "Orden", 'ClaseOrden' as "Titulo", 'Ayuda de	ClaseOrden' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'Perfil' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 99 as "Orden", 'Perfil' as "Titulo", 'Ayuda de	Perfil' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'PerfilAnterior' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 100 as "Orden", 'PerfilAnterior' as "Titulo", 'Ayuda de	PerfilAnterior' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cli' as "Esquema", 'V1' as "Version", 'FechaInicioPerfilActual' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 101 as "Orden", 'FechaInicioPerfilActual' as "Titulo", 'Ayuda de	FechaInicioPerfilActual' as "Ayuda", 'TEXT' as "TipoDato", 250 as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;



--ORDENES

insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ord' as "Esquema",'V1' as "Version",'NumeroOrden'	as "Nombre", ' ' as "CodigoSII", ' '	as "TipoDiccionario",	1	as "Orden", 'NumeroOrden'	as "Titulo", 'Ayuda de 	NumeroOrden'	as "Ayuda", 'ID'	as "TipoDato",	0	as "Largo", ' '	as "ValorDefecto", 'N'	as "Obligatorio",  'S'	as "Editable",  'S'	as "ModoMasivo", 'S'	as "Exportable", 'N'	as "OcultoEnGrilla", 'N'	as "OcultoEnMantenedor", 'N'	as "Comun", 'N'	as "Clave", ' '	as "Grupo",	0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ord' as "Esquema",'V1' as "Version",'NumeroCliente'	as "Nombre", ' ' as "CodigoSII", ' '	as "TipoDiccionario",	2	as "Orden", 'NumeroCliente'	as "Titulo", 'Ayuda de 	NumeroCliente'	as "Ayuda", 'ID'	as "TipoDato",	0	as "Largo", ' '	as "ValorDefecto", 'N'	as "Obligatorio",  'S'	as "Editable",  'S'	as "ModoMasivo", 'S'	as "Exportable", 'N'	as "OcultoEnGrilla", 'N'	as "OcultoEnMantenedor", 'N'	as "Comun", 'N'	as "Clave", ' '	as "Grupo",	0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ord' as "Esquema",'V1' as "Version",'NumeroCuenta'	as "Nombre", ' ' as "CodigoSII", ' '	as "TipoDiccionario",	3	as "Orden", 'NumeroCuenta'	as "Titulo", 'Ayuda de 	NumeroCuenta'	as "Ayuda", 'ID'	as "TipoDato",	0	as "Largo", ' '	as "ValorDefecto", 'N'	as "Obligatorio",  'S'	as "Editable",  'S'	as "ModoMasivo", 'S'	as "Exportable", 'N'	as "OcultoEnGrilla", 'N'	as "OcultoEnMantenedor", 'N'	as "Comun", 'N'	as "Clave", ' '	as "Grupo",	0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ord' as "Esquema",'V1' as "Version",'NumeroPersonaOrden'	as "Nombre", ' ' as "CodigoSII", ' '	as "TipoDiccionario",	4	as "Orden", 'NumeroPersonaOrden'	as "Titulo", 'Ayuda de 	NumeroPersonaOrden'	as "Ayuda", 'ID'	as "TipoDato",	0	as "Largo", ' '	as "ValorDefecto", 'N'	as "Obligatorio",  'S'	as "Editable",  'S'	as "ModoMasivo", 'S'	as "Exportable", 'N'	as "OcultoEnGrilla", 'N'	as "OcultoEnMantenedor", 'N'	as "Comun", 'N'	as "Clave", ' '	as "Grupo",	0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ord' as "Esquema",'V1' as "Version",'NumeroEjecutivo'	as "Nombre", ' ' as "CodigoSII", ' '	as "TipoDiccionario",	5	as "Orden", 'NumeroEjecutivo'	as "Titulo", 'Ayuda de 	NumeroEjecutivo'	as "Ayuda", 'ID'	as "TipoDato",	0	as "Largo", ' '	as "ValorDefecto", 'N'	as "Obligatorio",  'S'	as "Editable",  'S'	as "ModoMasivo", 'S'	as "Exportable", 'N'	as "OcultoEnGrilla", 'N'	as "OcultoEnMantenedor", 'N'	as "Comun", 'N'	as "Clave", ' '	as "Grupo",	0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ord' as "Esquema",'V1' as "Version",'ClaseOrden'	as "Nombre", ' ' as "CodigoSII", ' '	as "TipoDiccionario",	6	as "Orden", 'ClaseOrden'	as "Titulo", 'Ayuda de 	ClaseOrden'	as "Ayuda", 'TEXT'	as "TipoDato",	250	as "Largo", ' '	as "ValorDefecto", 'N'	as "Obligatorio",  'S'	as "Editable",  'S'	as "ModoMasivo", 'S'	as "Exportable", 'N'	as "OcultoEnGrilla", 'N'	as "OcultoEnMantenedor", 'N'	as "Comun", 'N'	as "Clave", ' '	as "Grupo",	0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ord' as "Esquema",'V1' as "Version",'TipoOrden'	as "Nombre", ' ' as "CodigoSII", ' '	as "TipoDiccionario",	7	as "Orden", 'TipoOrden'	as "Titulo", 'Ayuda de 	TipoOrden'	as "Ayuda", 'TEXT'	as "TipoDato",	250	as "Largo", ' '	as "ValorDefecto", 'N'	as "Obligatorio",  'S'	as "Editable",  'S'	as "ModoMasivo", 'S'	as "Exportable", 'N'	as "OcultoEnGrilla", 'N'	as "OcultoEnMantenedor", 'N'	as "Comun", 'N'	as "Clave", ' '	as "Grupo",	0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ord' as "Esquema",'V1' as "Version",'Isin'	as "Nombre", ' ' as "CodigoSII", ' '	as "TipoDiccionario",	8	as "Orden", 'Isin'	as "Titulo", 'Ayuda de 	Isin'	as "Ayuda", 'TEXT'	as "TipoDato",	250	as "Largo", ' '	as "ValorDefecto", 'N'	as "Obligatorio",  'S'	as "Editable",  'S'	as "ModoMasivo", 'S'	as "Exportable", 'N'	as "OcultoEnGrilla", 'N'	as "OcultoEnMantenedor", 'N'	as "Comun", 'N'	as "Clave", ' '	as "Grupo",	0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ord' as "Esquema",'V1' as "Version",'Nemotecnico'	as "Nombre", ' ' as "CodigoSII", ' '	as "TipoDiccionario",	9	as "Orden", 'Nemotecnico'	as "Titulo", 'Ayuda de 	Nemotecnico'	as "Ayuda", 'NEMO'	as "TipoDato",	100	as "Largo", ' '	as "ValorDefecto", 'N'	as "Obligatorio",  'S'	as "Editable",  'S'	as "ModoMasivo", 'S'	as "Exportable", 'N'	as "OcultoEnGrilla", 'N'	as "OcultoEnMantenedor", 'N'	as "Comun", 'N'	as "Clave", ' '	as "Grupo",	0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ord' as "Esquema",'V1' as "Version",'TipoInstrumento'	as "Nombre", ' ' as "CodigoSII", ' '	as "TipoDiccionario",	10	as "Orden", 'TipoInstrumento'	as "Titulo", 'Ayuda de 	TipoInstrumento'	as "Ayuda", 'TEXT'	as "TipoDato",	250	as "Largo", ' '	as "ValorDefecto", 'N'	as "Obligatorio",  'S'	as "Editable",  'S'	as "ModoMasivo", 'S'	as "Exportable", 'N'	as "OcultoEnGrilla", 'N'	as "OcultoEnMantenedor", 'N'	as "Comun", 'N'	as "Clave", ' '	as "Grupo",	0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ord' as "Esquema",'V1' as "Version",'Cantidad'	as "Nombre", ' ' as "CodigoSII", ' '	as "TipoDiccionario",	11	as "Orden", 'Cantidad'	as "Titulo", 'Ayuda de 	Cantidad'	as "Ayuda", 'QTY'	as "TipoDato",	0	as "Largo", ' '	as "ValorDefecto", 'N'	as "Obligatorio",  'S'	as "Editable",  'S'	as "ModoMasivo", 'S'	as "Exportable", 'N'	as "OcultoEnGrilla", 'N'	as "OcultoEnMantenedor", 'N'	as "Comun", 'N'	as "Clave", ' '	as "Grupo",	0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ord' as "Esquema",'V1' as "Version",'Monto'	as "Nombre", ' ' as "CodigoSII", ' '	as "TipoDiccionario",	12	as "Orden", 'Monto'	as "Titulo", 'Ayuda de 	Monto'	as "Ayuda", 'DECIMAL'	as "TipoDato",	0	as "Largo", ' '	as "ValorDefecto", 'N'	as "Obligatorio",  'S'	as "Editable",  'S'	as "ModoMasivo", 'S'	as "Exportable", 'N'	as "OcultoEnGrilla", 'N'	as "OcultoEnMantenedor", 'N'	as "Comun", 'N'	as "Clave", ' '	as "Grupo",	0	as "OrdenCargaExterna" ,4 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ord' as "Esquema",'V1' as "Version",'Moneda'	as "Nombre", ' ' as "CodigoSII", ' '	as "TipoDiccionario",	13	as "Orden", 'Moneda'	as "Titulo", 'Ayuda de 	Moneda'	as "Ayuda", 'TEXT'	as "TipoDato",	250	as "Largo", ' '	as "ValorDefecto", 'N'	as "Obligatorio",  'S'	as "Editable",  'S'	as "ModoMasivo", 'S'	as "Exportable", 'N'	as "OcultoEnGrilla", 'N'	as "OcultoEnMantenedor", 'N'	as "Comun", 'N'	as "Clave", ' '	as "Grupo",	0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ord' as "Esquema",'V1' as "Version",'Precio'	as "Nombre", ' ' as "CodigoSII", ' '	as "TipoDiccionario",	14	as "Orden", 'Precio'	as "Titulo", 'Ayuda de 	Precio'	as "Ayuda", 'DECIMAL'	as "TipoDato",	0	as "Largo", ' '	as "ValorDefecto", 'N'	as "Obligatorio",  'S'	as "Editable",  'S'	as "ModoMasivo", 'S'	as "Exportable", 'N'	as "OcultoEnGrilla", 'N'	as "OcultoEnMantenedor", 'N'	as "Comun", 'N'	as "Clave", ' '	as "Grupo",	0	as "OrdenCargaExterna" ,4 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ord' as "Esquema",'V1' as "Version",'PlazoValidez'	as "Nombre", ' ' as "CodigoSII", ' '	as "TipoDiccionario",	15	as "Orden", 'PlazoValidez'	as "Titulo", 'Ayuda de 	PlazoValidez'	as "Ayuda", 'QTY'	as "TipoDato",	0	as "Largo", ' '	as "ValorDefecto", 'N'	as "Obligatorio",  'S'	as "Editable",  'S'	as "ModoMasivo", 'S'	as "Exportable", 'N'	as "OcultoEnGrilla", 'N'	as "OcultoEnMantenedor", 'N'	as "Comun", 'N'	as "Clave", ' '	as "Grupo",	0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ord' as "Esquema",'V1' as "Version",'PreferenciaBolsa'	as "Nombre", ' ' as "CodigoSII", ' '	as "TipoDiccionario",	16	as "Orden", 'PreferenciaBolsa'	as "Titulo", 'Ayuda de 	PreferenciaBolsa'	as "Ayuda", 'TEXT'	as "TipoDato",	250	as "Largo", ' '	as "ValorDefecto", 'N'	as "Obligatorio",  'S'	as "Editable",  'S'	as "ModoMasivo", 'S'	as "Exportable", 'N'	as "OcultoEnGrilla", 'N'	as "OcultoEnMantenedor", 'N'	as "Comun", 'N'	as "Clave", ' '	as "Grupo",	0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ord' as "Esquema",'V1' as "Version",'PreferenciaMecanismo'	as "Nombre", ' ' as "CodigoSII", ' '	as "TipoDiccionario",	17	as "Orden", 'PreferenciaMecanismo'	as "Titulo", 'Ayuda de 	PreferenciaMecanismo'	as "Ayuda", 'TEXT'	as "TipoDato",	250	as "Largo", ' '	as "ValorDefecto", 'N'	as "Obligatorio",  'S'	as "Editable",  'S'	as "ModoMasivo", 'S'	as "Exportable", 'N'	as "OcultoEnGrilla", 'N'	as "OcultoEnMantenedor", 'N'	as "Comun", 'N'	as "Clave", ' '	as "Grupo",	0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ord' as "Esquema",'V1' as "Version",'FechaRecepcion'	as "Nombre", ' ' as "CodigoSII", ' '	as "TipoDiccionario",	18	as "Orden", 'FechaRecepcion'	as "Titulo", 'Ayuda de 	FechaRecepcion'	as "Ayuda", 'DATE_FILTER'	as "TipoDato",	0	as "Largo", ' '	as "ValorDefecto", 'N'	as "Obligatorio",  'S'	as "Editable",  'S'	as "ModoMasivo", 'S'	as "Exportable", 'N'	as "OcultoEnGrilla", 'N'	as "OcultoEnMantenedor", 'N'	as "Comun", 'N'	as "Clave", ' '	as "Grupo",	0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ord' as "Esquema",'V1' as "Version",'HoraRecepcion'	as "Nombre", ' ' as "CodigoSII", ' '	as "TipoDiccionario",	19	as "Orden", 'HoraRecepcion'	as "Titulo", 'Ayuda de 	HoraRecepcion'	as "Ayuda", 'TEXT'	as "TipoDato",	250	as "Largo", ' '	as "ValorDefecto", 'N'	as "Obligatorio",  'S'	as "Editable",  'S'	as "ModoMasivo", 'S'	as "Exportable", 'N'	as "OcultoEnGrilla", 'N'	as "OcultoEnMantenedor", 'N'	as "Comun", 'N'	as "Clave", ' '	as "Grupo",	0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ord' as "Esquema",'V1' as "Version",'FechaAceptacion'	as "Nombre", ' ' as "CodigoSII", ' '	as "TipoDiccionario",	20	as "Orden", 'FechaAceptacion'	as "Titulo", 'Ayuda de 	FechaAceptacion'	as "Ayuda", 'DATE_FILTER'	as "TipoDato",	0	as "Largo", ' '	as "ValorDefecto", 'N'	as "Obligatorio",  'S'	as "Editable",  'S'	as "ModoMasivo", 'S'	as "Exportable", 'N'	as "OcultoEnGrilla", 'N'	as "OcultoEnMantenedor", 'N'	as "Comun", 'N'	as "Clave", ' '	as "Grupo",	0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ord' as "Esquema",'V1' as "Version",'HoraAceptacion'	as "Nombre", ' ' as "CodigoSII", ' '	as "TipoDiccionario",	21	as "Orden", 'HoraAceptacion'	as "Titulo", 'Ayuda de 	HoraAceptacion'	as "Ayuda", 'TEXT'	as "TipoDato",	250	as "Largo", ' '	as "ValorDefecto", 'N'	as "Obligatorio",  'S'	as "Editable",  'S'	as "ModoMasivo", 'S'	as "Exportable", 'N'	as "OcultoEnGrilla", 'N'	as "OcultoEnMantenedor", 'N'	as "Comun", 'N'	as "Clave", ' '	as "Grupo",	0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ord' as "Esquema",'V1' as "Version",'NumeroPersona'	as "Nombre", ' ' as "CodigoSII", ' '	as "TipoDiccionario",	22	as "Orden", 'NUMEROPERSONA'	as "Titulo", 'Ayuda de 	NUMEROPERSONA'	as "Ayuda", 'ID'	as "TipoDato",	0	as "Largo", ' '	as "ValorDefecto", 'N'	as "Obligatorio",  'S'	as "Editable",  'S'	as "ModoMasivo", 'S'	as "Exportable", 'N'	as "OcultoEnGrilla", 'N'	as "OcultoEnMantenedor", 'N'	as "Comun", 'N'	as "Clave", ' '	as "Grupo",	0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ord' as "Esquema",'V1' as "Version",'Anulacion'	as "Nombre", ' ' as "CodigoSII", ' '	as "TipoDiccionario",	23	as "Orden", 'Anulacion'	as "Titulo", 'Ayuda de 	Anulacion'	as "Ayuda", 'ID'	as "TipoDato",	0	as "Largo", ' '	as "ValorDefecto", 'N'	as "Obligatorio",  'S'	as "Editable",  'S'	as "ModoMasivo", 'S'	as "Exportable", 'N'	as "OcultoEnGrilla", 'N'	as "OcultoEnMantenedor", 'N'	as "Comun", 'N'	as "Clave", ' '	as "Grupo",	0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ord' as "Esquema",'V1' as "Version",'NumeroOrdenReemplazo'	as "Nombre", ' ' as "CodigoSII", ' '	as "TipoDiccionario",	24	as "Orden", 'NumeroOrdenReemplazo'	as "Titulo", 'Ayuda de 	NumeroOrdenReemplazo'	as "Ayuda", 'ID'	as "TipoDato",	0	as "Largo", ' '	as "ValorDefecto", 'N'	as "Obligatorio",  'S'	as "Editable",  'S'	as "ModoMasivo", 'S'	as "Exportable", 'N'	as "OcultoEnGrilla", 'N'	as "OcultoEnMantenedor", 'N'	as "Comun", 'N'	as "Clave", ' '	as "Grupo",	0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ord' as "Esquema",'V1' as "Version",'FechaAnulacion'	as "Nombre", ' ' as "CodigoSII", ' '	as "TipoDiccionario",	25	as "Orden", 'FechaAnulacion'	as "Titulo", 'Ayuda de 	FechaAnulacion'	as "Ayuda", 'DATE_FILTER'	as "TipoDato",	0	as "Largo", ' '	as "ValorDefecto", 'N'	as "Obligatorio",  'S'	as "Editable",  'S'	as "ModoMasivo", 'S'	as "Exportable", 'N'	as "OcultoEnGrilla", 'N'	as "OcultoEnMantenedor", 'N'	as "Comun", 'N'	as "Clave", ' '	as "Grupo",	0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ord' as "Esquema",'V1' as "Version",'Observaciones'	as "Nombre", ' ' as "CodigoSII", ' '	as "TipoDiccionario",	26	as "Orden", 'Observaciones'	as "Titulo", 'Ayuda de 	Observaciones'	as "Ayuda", 'TEXT'	as "TipoDato",	250	as "Largo", ' '	as "ValorDefecto", 'N'	as "Obligatorio",  'S'	as "Editable",  'S'	as "ModoMasivo", 'S'	as "Exportable", 'N'	as "OcultoEnGrilla", 'N'	as "OcultoEnMantenedor", 'N'	as "Comun", 'N'	as "Clave", ' '	as "Grupo",	0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;



--CUSTODIA

insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cus' as "Esquema",	'V1'	as "Version",'NumeroCliente' as "Nombre", '' as "CodigoSII", ''	as "TipoDiccionario", 1 as "Orden", 'NumeroCliente'	as "Titulo", 'Ayuda de 	NumeroCliente'	as "Ayuda", 'ID' as "TipoDato",	0	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cus' as "Esquema",	'V1'	as "Version",'NumeroCuenta'	as "Nombre", ''	as "CodigoSII", ''	as "TipoDiccionario", 2 as "Orden", 'NumeroCuenta'	as "Titulo", 'Ayuda de 	NumeroCuenta'	as "Ayuda", 'ID' as "TipoDato",	0	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cus' as "Esquema",	'V1'	as "Version",'Fecha' as "Nombre", '' as "CodigoSII", ''	as "TipoDiccionario", 3 as "Orden", 'Fecha'	as "Titulo", 'Ayuda de 	Fecha'	as "Ayuda", 'DATE_FILTER'	as "TipoDato",	0	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cus' as "Esquema",	'V1'	as "Version",'Hora'	as "Nombre", ''	as "CodigoSII", ''	as "TipoDiccionario", 4	as "Orden", 'Hora'	as "Titulo", 'Ayuda de 	Hora'	as "Ayuda", 'TEXT'	as "TipoDato",	250	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cus' as "Esquema",	'V1'	as "Version",'NumeroComprobante' as "Nombre", '' as "CodigoSII", ''	as "TipoDiccionario", 5 as "Orden", 'NumeroComprobante'	as "Titulo", 'Ayuda de 	NumeroComprobante'	as "Ayuda", 'ID'	as "TipoDato",	0	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cus' as "Esquema",	'V1'	as "Version",'Emisor' as "Nombre", '' as "CodigoSII", ''	as "TipoDiccionario", 6 as "Orden", 'Emisor'	as "Titulo", 'Ayuda de 	Emisor'	as "Ayuda", 'TEXT'	as "TipoDato",	250	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0 as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cus' as "Esquema",	'V1'	as "Version",'TipoMovimiento' as "Nombre", '' as "CodigoSII", ''	as "TipoDiccionario", 7 as "Orden", 'TipoMovimiento' as "Titulo", 'Ayuda de TipoMovimiento'	as "Ayuda", 'TEXT'	as "TipoDato",	250	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cus' as "Esquema",	'V1'	as "Version",'Movimiento' as "Nombre", '' as "CodigoSII", ''	as "TipoDiccionario", 8	as "Orden", 'Movimiento'	as "Titulo", 'Ayuda de 	Movimiento'	as "Ayuda", 'TEXT'	as "TipoDato",	250	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cus' as "Esquema",	'V1'	as "Version",'ISIN'	as "Nombre", ''	as "CodigoSII", ''	as "TipoDiccionario", 9	as "Orden", 'ISIN'	as "Titulo", 'Ayuda de 	ISIN'	as "Ayuda", 'TEXT'	as "TipoDato",	250	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cus' as "Esquema",	'V1'	as "Version",'Nemotecnico' as "Nombre", '' as "CodigoSII", ''	as "TipoDiccionario", 10 as "Orden", 'Nemotecnico'	as "Titulo", 'Ayuda de 	Nemotecnico' as "Ayuda", 'TEXT'	as "TipoDato",	250	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cus' as "Esquema",	'V1'	as "Version",'Cantidad'	as "Nombre", ''	as "CodigoSII", ''	as "TipoDiccionario", 11 as "Orden", 'Cantidad'	as "Titulo", 'Ayuda de 	Cantidad'	as "Ayuda", 'QTY'	as "TipoDato",	0	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cus' as "Esquema",	'V1'	as "Version",'Monto' as "Nombre", '' as "CodigoSII", ''	as "TipoDiccionario", 12 as "Orden", 'Monto'	as "Titulo", 'Ayuda de 	Monto'	as "Ayuda", 'DECIMAL'	as "TipoDato",	0	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	4	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cus' as "Esquema",	'V1'	as "Version",'Moneda' as "Nombre", '' as "CodigoSII", ''	as "TipoDiccionario", 13 as "Orden", 'Moneda'	as "Titulo", 'Ayuda de 	Moneda'	as "Ayuda", 'TEXT'	as "TipoDato",	250	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cus' as "Esquema",	'V1'	as "Version",'Saldo' as "Nombre", '' as "CodigoSII", ''	as "TipoDiccionario", 14 as "Orden", 'Saldo'	as "Titulo", 'Ayuda de 	Saldo'	as "Ayuda", 'DECIMAL'	as "TipoDato",	0	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	4	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cus' as "Esquema",	'V1'	as "Version",'SubCustodio' as "Nombre", ''	as "CodigoSII", ''	as "TipoDiccionario", 15 as "Orden", 'SubCustodio'	as "Titulo", 'Ayuda de 	SubCustodio'	as "Ayuda", 'TEXT'	as "TipoDato",	250	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Cus' as "Esquema",	'V1'	as "Version",'Estado' as "Nombre", '' as "CodigoSII", ''	as "TipoDiccionario", 16 as "Orden", 'Estado'	as "Titulo", 'Ayuda de 	Estado'	as "Ayuda", 'TEXT'	as "TipoDato",	250	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;


--FACTURACION


insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select  'NCG380Fac'  as "Esquema",'V1' as "Version",'Rut' as "Nombre", ''	as "CodigoSII", ''	as "TipoDiccionario",	1	as "Orden", 'Rut'	as "Titulo", 'Ayuda de 	Rut' as "Ayuda", 'RUT'	as "TipoDato",	0	as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select  'NCG380Fac'  as "Esquema",'V1' as "Version",'Cuenta' as "Nombre", ''	as "CodigoSII", ''	as "TipoDiccionario",	2	as "Orden", 'Cuenta'	as "Titulo", 'Ayuda de 	Cuenta' as "Ayuda", 'TEXT'	as "TipoDato",	250	as "Largo", ''	as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select  'NCG380Fac'  as "Esquema",'V1' as "Version",'Nombre' as "Nombre", ''	as "CodigoSII", ''	as "TipoDiccionario",	3	as "Orden", 'Nombre'	as "Titulo", 'Ayuda de 	Nombre' as "Ayuda", 'TEXT'	as "TipoDato",	250	as "Largo", ''	as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select  'NCG380Fac'  as "Esquema",'V1' as "Version",'Bolsa'	as "Nombre", ''	as "CodigoSII", ''	as "TipoDiccionario",	4	as "Orden", 'Bolsa'	as "Titulo", 'Ayuda de 	Bolsa' as "Ayuda", 'TEXT'	as "TipoDato",	250	as "Largo", ''	as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select  'NCG380Fac'  as "Esquema",'V1' as "Version",'Folio'	as "Nombre", ''	as "CodigoSII", ''	as "TipoDiccionario",	5	as "Orden", 'Folio'	as "Titulo", 'Ayuda de 	Folio' as "Ayuda", 'ID'	as "TipoDato",	0	as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select  'NCG380Fac'  as "Esquema",'V1' as "Version",'Instrumento' as "Nombre", ''	as "CodigoSII", ''	as "TipoDiccionario",	6	as "Orden", 'Instrumento'	as "Titulo", 'Ayuda de 	Instrumento' as "Ayuda", 'TEXT'	as "TipoDato",	250	as "Largo", ''	as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select  'NCG380Fac'  as "Esquema",'V1' as "Version",'TipoOperacion'	as "Nombre", ''	as "CodigoSII", ''	as "TipoDiccionario",	7	as "Orden", 'TipoOperacion'	as "Titulo", 'Ayuda de 	TipoOperacion' as "Ayuda", 'TEXT'	as "TipoDato",	250	as "Largo", ''	as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select  'NCG380Fac'  as "Esquema",'V1' as "Version",'NumeroOrden' as "Nombre", ''	as "CodigoSII", ''	as "TipoDiccionario",	8	as "Orden", 'NumeroOrden'	as "Titulo", 'Ayuda de 	NumeroOrden' as "Ayuda", 'ID'	as "TipoDato",	0	as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select  'NCG380Fac'  as "Esquema",'V1' as "Version",'NumeroFactura'	as "Nombre", ''	as "CodigoSII", ''	as "TipoDiccionario",	6	as "Orden", 'NumeroFactura'	as "Titulo", 'Ayuda de 	NumeroFactura' as "Ayuda", 'ID'	as "TipoDato",	0	as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select  'NCG380Fac'  as "Esquema",'V1' as "Version",'Cantidad' as "Nombre", ''	as "CodigoSII", ''	as "TipoDiccionario",	10	as "Orden", 'Cantidad'	as "Titulo", 'Ayuda de 	Cantidad' as "Ayuda", 'QTY'	as "TipoDato",	0	as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select  'NCG380Fac'  as "Esquema",'V1' as "Version",'Precio' as "Nombre", ''	as "CodigoSII", ''	as "TipoDiccionario",	11	as "Orden", 'Precio'	as "Titulo", 'Ayuda de 	Precio' as "Ayuda", 'DECIMAL'	as "TipoDato",	0	as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",4 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select  'NCG380Fac'  as "Esquema",'V1' as "Version",'Monto'	as "Nombre", ''	as "CodigoSII", ''	as "TipoDiccionario",	12	as "Orden", 'Monto'	as "Titulo", 'Ayuda de 	Monto' as "Ayuda", 'DECIMAL'	as "TipoDato",	0	as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",4 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select  'NCG380Fac'  as "Esquema",'V1' as "Version",'Moneda' as "Nombre", ''	as "CodigoSII", ''	as "TipoDiccionario",	13	as "Orden", 'Moneda'	as "Titulo", 'Ayuda de 	Moneda' as "Ayuda", 'TEXT'	as "TipoDato",	250	as "Largo", ''	as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select  'NCG380Fac'  as "Esquema",'V1' as "Version",'Comision' as "Nombre", ''	as "CodigoSII", ''	as "TipoDiccionario",	14	as "Orden", 'Comision'	as "Titulo", 'Ayuda de 	Comision' as "Ayuda", 'DECIMAL'	as "TipoDato",	0	as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",4 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select  'NCG380Fac'  as "Esquema",'V1' as "Version",'DerechoBolsa'	as "Nombre", ''	as "CodigoSII", ''	as "TipoDiccionario",	15	as "Orden", 'DerechoBolsa'	as "Titulo", 'Ayuda de 	DerechoBolsa' as "Ayuda", 'DECIMAL	'	as "TipoDato",	0	as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",4 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select  'NCG380Fac'  as "Esquema",'V1' as "Version",'Gastos' as "Nombre", ''	as "CodigoSII", ''	as "TipoDiccionario",	16	as "Orden", 'Gastos'	as "Titulo", 'Ayuda de 	Gastos' as "Ayuda", 'DECIMAL'	as "TipoDato",	0	as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",4 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select  'NCG380Fac'  as "Esquema",'V1' as "Version",'Impuesto'	as "Nombre", ''	as "CodigoSII", ''	as "TipoDiccionario",	17	as "Orden", 'Impuesto'	as "Titulo", 'Ayuda de 	Impuesto' as "Ayuda", 'DECIMAL'	as "TipoDato",	0	as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",4 as "CantidadDecimales" from dummy;






--TRABAJADORES

insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Tra' as "Esquema",'V1' as "Version",'Rut'as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	1	as "Orden", 'Rut'as "Titulo", 'Ayuda de Rut'as "Ayuda", 'ID'as "TipoDato",	0	as "Largo", '0'as "ValorDefecto", 'N'as "Obligatorio",  'S'as "Editable",  'S'as "ModoMasivo", 'S'as "Exportable", 'N'as "OcultoEnGrilla", 'N'as "OcultoEnMantenedor", 'N'as "Comun", 'N'as "Clave", ''as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Tra' as "Esquema",'V1' as "Version",'DIG'as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	2	as "Orden", 'DIG'as "Titulo", 'Ayuda de DIG'as "Ayuda", 'TEXT'as "TipoDato",	250	as "Largo", ''as "ValorDefecto", 'N'as "Obligatorio",  'S'as "Editable",  'S'as "ModoMasivo", 'S'as "Exportable", 'N'as "OcultoEnGrilla", 'N'as "OcultoEnMantenedor", 'N'as "Comun", 'N'as "Clave", ''as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Tra' as "Esquema",'V1' as "Version",'Pasaporte'as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	3	as "Orden", 'Pasaporte'as "Titulo", 'Ayuda de Pasaporte'as "Ayuda", 'TEXT'as "TipoDato",	250	as "Largo", ''as "ValorDefecto", 'N'as "Obligatorio",  'S'as "Editable",  'S'as "ModoMasivo", 'S'as "Exportable", 'N'as "OcultoEnGrilla", 'N'as "OcultoEnMantenedor", 'N'as "Comun", 'N'as "Clave", ''as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Tra' as "Esquema",'V1' as "Version",'ApellidoPaterno'as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	4	as "Orden", 'ApellidoPaterno'as "Titulo", 'Ayuda de ApellidoPaterno'as "Ayuda", 'TEXT'as "TipoDato",	250	as "Largo", ''as "ValorDefecto", 'N'as "Obligatorio",  'S'as "Editable",  'S'as "ModoMasivo", 'S'as "Exportable", 'N'as "OcultoEnGrilla", 'N'as "OcultoEnMantenedor", 'N'as "Comun", 'N'as "Clave", ''as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Tra' as "Esquema",'V1' as "Version",'ApellidoMaterno'as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	5	as "Orden", 'ApellidoMaterno'as "Titulo", 'Ayuda de ApellidoMaterno'as "Ayuda", 'TEXT'as "TipoDato",	250	as "Largo", ''as "ValorDefecto", 'N'as "Obligatorio",  'S'as "Editable",  'S'as "ModoMasivo", 'S'as "Exportable", 'N'as "OcultoEnGrilla", 'N'as "OcultoEnMantenedor", 'N'as "Comun", 'N'as "Clave", ''as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Tra' as "Esquema",'V1' as "Version",'PrimerNombre'as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	6	as "Orden", 'PrimerNombre'as "Titulo", 'Ayuda de PrimerNombre'as "Ayuda", 'TEXT'as "TipoDato",	250	as "Largo", ''as "ValorDefecto", 'N'as "Obligatorio",  'S'as "Editable",  'S'as "ModoMasivo", 'S'as "Exportable", 'N'as "OcultoEnGrilla", 'N'as "OcultoEnMantenedor", 'N'as "Comun", 'N'as "Clave", ''as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Tra' as "Esquema",'V1' as "Version",'SegundoNombre'as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	7	as "Orden", 'SegundoNombre'as "Titulo", 'Ayuda de SegundoNombre'as "Ayuda", 'TEXT'as "TipoDato",	250	as "Largo", ''as "ValorDefecto", 'N'as "Obligatorio",  'S'as "Editable",  'S'as "ModoMasivo", 'S'as "Exportable", 'N'as "OcultoEnGrilla", 'N'as "OcultoEnMantenedor", 'N'as "Comun", 'N'as "Clave", ''as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Tra' as "Esquema",'V1' as "Version",'Telefono'as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	8	as "Orden", 'Telefono'as "Titulo", 'Ayuda de Telefono'as "Ayuda", 'TEXT'as "TipoDato",	250	as "Largo", ''as "ValorDefecto", 'N'as "Obligatorio",  'S'as "Editable",  'S'as "ModoMasivo", 'S'as "Exportable", 'N'as "OcultoEnGrilla", 'N'as "OcultoEnMantenedor", 'N'as "Comun", 'N'as "Clave", ''as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Tra' as "Esquema",'V1' as "Version",'Email'as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	9	as "Orden", 'Email'as "Titulo", 'Ayuda de Email'as "Ayuda", 'TEXT'as "TipoDato",	250	as "Largo", ''as "ValorDefecto", 'N'as "Obligatorio",  'S'as "Editable",  'S'as "ModoMasivo", 'S'as "Exportable", 'N'as "OcultoEnGrilla", 'N'as "OcultoEnMantenedor", 'N'as "Comun", 'N'as "Clave", ''as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Tra' as "Esquema",'V1' as "Version",'TipoDireccion'as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	10	as "Orden", 'TipoDireccion'as "Titulo", 'Ayuda de TipoDireccion'as "Ayuda", 'TEXT'as "TipoDato",	250	as "Largo", ''as "ValorDefecto", 'N'as "Obligatorio",  'S'as "Editable",  'S'as "ModoMasivo", 'S'as "Exportable", 'N'as "OcultoEnGrilla", 'N'as "OcultoEnMantenedor", 'N'as "Comun", 'N'as "Clave", ''as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Tra' as "Esquema",'V1' as "Version",'DireccionNombre'as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	11	as "Orden", 'DireccionNombre'as "Titulo", 'Ayuda de DireccionNombre'as "Ayuda", 'TEXT'as "TipoDato",	250	as "Largo", ''as "ValorDefecto", 'N'as "Obligatorio",  'S'as "Editable",  'S'as "ModoMasivo", 'S'as "Exportable", 'N'as "OcultoEnGrilla", 'N'as "OcultoEnMantenedor", 'N'as "Comun", 'N'as "Clave", ''as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Tra' as "Esquema",'V1' as "Version",'DireccionNumero'as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	12	as "Orden", 'DireccionNumero'as "Titulo", 'Ayuda de DireccionNumero'as "Ayuda", 'TEXT'as "TipoDato",	250	as "Largo", ''as "ValorDefecto", 'N'as "Obligatorio",  'S'as "Editable",  'S'as "ModoMasivo", 'S'as "Exportable", 'N'as "OcultoEnGrilla", 'N'as "OcultoEnMantenedor", 'N'as "Comun", 'N'as "Clave", ''as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Tra' as "Esquema",'V1' as "Version",'DireccionDepto'as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	13	as "Orden", 'DireccionDepto'as "Titulo", 'Ayuda de DireccionDepto'as "Ayuda", 'TEXT'as "TipoDato",	250	as "Largo", ''as "ValorDefecto", 'N'as "Obligatorio",  'S'as "Editable",  'S'as "ModoMasivo", 'S'as "Exportable", 'N'as "OcultoEnGrilla", 'N'as "OcultoEnMantenedor", 'N'as "Comun", 'N'as "Clave", ''as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Tra' as "Esquema",'V1' as "Version",'DireccionComuna'as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	14	as "Orden", 'DireccionComuna'as "Titulo", 'Ayuda de DireccionComuna'as "Ayuda", 'TEXT'as "TipoDato",	250	as "Largo", ''as "ValorDefecto", 'N'as "Obligatorio",  'S'as "Editable",  'S'as "ModoMasivo", 'S'as "Exportable", 'N'as "OcultoEnGrilla", 'N'as "OcultoEnMantenedor", 'N'as "Comun", 'N'as "Clave", ''as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Tra' as "Esquema",'V1' as "Version",'DireccionCiudad'as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	15	as "Orden", 'DireccionCiudad'as "Titulo", 'Ayuda de DireccionCiudad'as "Ayuda", 'TEXT'as "TipoDato",	250	as "Largo", ''as "ValorDefecto", 'N'as "Obligatorio",  'S'as "Editable",  'S'as "ModoMasivo", 'S'as "Exportable", 'N'as "OcultoEnGrilla", 'N'as "OcultoEnMantenedor", 'N'as "Comun", 'N'as "Clave", ''as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Tra' as "Esquema",'V1' as "Version",'DireccionEstado'as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	16	as "Orden", 'DireccionEstado'as "Titulo", 'Ayuda de DireccionEstado'as "Ayuda", 'TEXT'as "TipoDato",	250	as "Largo", ''as "ValorDefecto", 'N'as "Obligatorio",  'S'as "Editable",  'S'as "ModoMasivo", 'S'as "Exportable", 'N'as "OcultoEnGrilla", 'N'as "OcultoEnMantenedor", 'N'as "Comun", 'N'as "Clave", ''as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Tra' as "Esquema",'V1' as "Version",'DireccionPais'as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	17	as "Orden", 'DireccionPais'as "Titulo", 'Ayuda de DireccionPais'as "Ayuda", 'TEXT'as "TipoDato",	250	as "Largo", ''as "ValorDefecto", 'N'as "Obligatorio",  'S'as "Editable",  'S'as "ModoMasivo", 'S'as "Exportable", 'N'as "OcultoEnGrilla", 'N'as "OcultoEnMantenedor", 'N'as "Comun", 'N'as "Clave", ''as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Tra' as "Esquema",'V1' as "Version",'DireccionCodPostal'as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	18	as "Orden", 'DireccionCodPostal'as "Titulo", 'Ayuda de DireccionCodPostal'as "Ayuda", 'TEXT'as "TipoDato",	250	as "Largo", ''as "ValorDefecto", 'N'as "Obligatorio",  'S'as "Editable",  'S'as "ModoMasivo", 'S'as "Exportable", 'N'as "OcultoEnGrilla", 'N'as "OcultoEnMantenedor", 'N'as "Comun", 'N'as "Clave", ''as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Tra' as "Esquema",'V1' as "Version",'Nacionalidad'as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	19	as "Orden", 'Nacionalidad'as "Titulo", 'Ayuda de Nacionalidad'as "Ayuda", 'TEXT'as "TipoDato",	250	as "Largo", ''as "ValorDefecto", 'N'as "Obligatorio",  'S'as "Editable",  'S'as "ModoMasivo", 'S'as "Exportable", 'N'as "OcultoEnGrilla", 'N'as "OcultoEnMantenedor", 'N'as "Comun", 'N'as "Clave", ''as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Tra' as "Esquema",'V1' as "Version",'FechaIniFunciones'as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	20	as "Orden", 'FechaIniFunciones'as "Titulo", 'Ayuda de FechaIniFunciones'as "Ayuda", 'DATE_FILTER'as "TipoDato",	0	as "Largo", ''as "ValorDefecto", 'N'as "Obligatorio",  'S'as "Editable",  'S'as "ModoMasivo", 'S'as "Exportable", 'N'as "OcultoEnGrilla", 'N'as "OcultoEnMantenedor", 'N'as "Comun", 'N'as "Clave", ''as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Tra' as "Esquema",'V1' as "Version",'FechaTerFunciones'as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	21	as "Orden", 'FechaTerFunciones'as "Titulo", 'Ayuda de FechaTerFunciones'as "Ayuda", 'DATE_FILTER'as "TipoDato",	0	as "Largo", ''as "ValorDefecto", 'N'as "Obligatorio",  'S'as "Editable",  'S'as "ModoMasivo", 'S'as "Exportable", 'N'as "OcultoEnGrilla", 'N'as "OcultoEnMantenedor", 'N'as "Comun", 'N'as "Clave", ''as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Tra' as "Esquema",'V1' as "Version",'Funcion'as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	22	as "Orden", 'Funcion'as "Titulo", 'Ayuda de Funcion'as "Ayuda", 'TEXT'as "TipoDato",	250	as "Largo", ''as "ValorDefecto", 'N'as "Obligatorio",  'S'as "Editable",  'S'as "ModoMasivo", 'S'as "Exportable", 'N'as "OcultoEnGrilla", 'N'as "OcultoEnMantenedor", 'N'as "Comun", 'N'as "Clave", ''as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Tra' as "Esquema",'V1' as "Version",'FechaAcreditacion'as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	23	as "Orden", 'FechaAcreditacion'as "Titulo", 'Ayuda de FechaAcreditacion'as "Ayuda", 'DATE_FILTER'as "TipoDato",	0	as "Largo", ''as "ValorDefecto", 'N'as "Obligatorio",  'S'as "Editable",  'S'as "ModoMasivo", 'S'as "Exportable", 'N'as "OcultoEnGrilla", 'N'as "OcultoEnMantenedor", 'N'as "Comun", 'N'as "Clave", ''as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Tra' as "Esquema",'V1' as "Version",'InfoPrivilegiada'as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	24	as "Orden", 'InfoPrivilegiada'as "Titulo", 'Ayuda de InfoPrivilegiada'as "Ayuda", 'TEXT'as "TipoDato",	250	as "Largo", ''as "ValorDefecto", 'N'as "Obligatorio",  'S'as "Editable",  'S'as "ModoMasivo", 'S'as "Exportable", 'N'as "OcultoEnGrilla", 'N'as "OcultoEnMantenedor", 'N'as "Comun", 'N'as "Clave", ''as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;



--AUTORIZADOS
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Aut' as "Esquema", 'V1' as "Version",'NumeroCliente' as "Nombre",'' as "CodigoSII", '' as "TipoDiccionario",	1	as "Orden", 'NumeroCliente'	as "Titulo", 'Ayuda de 	NumeroCliente	'	as "Ayuda", 'ID'	as "TipoDato",	0	as "Largo", '0'	as "ValorDefecto",'N' as "Obligatorio",  'S' as "Editable",  'S'	as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Aut' as "Esquema", 'V1' as "Version",'NumeroPersona' as "Nombre",'' as "CodigoSII", '' as "TipoDiccionario",	2	as "Orden", 'NumeroPersona'	as "Titulo", 'Ayuda de 	NumeroPersona	'	as "Ayuda", 'ID'	as "TipoDato",	0	as "Largo", '0'	as "ValorDefecto",'N' as "Obligatorio",  'S' as "Editable",  'S'	as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Aut' as "Esquema", 'V1' as "Version",'Rut' as "Nombre",'' as "CodigoSII", '' as "TipoDiccionario",	3	as "Orden", 'Rut'	as "Titulo", 'Ayuda de 	Rut	'	as "Ayuda", 'TEXT'	as  "TipoDato",	250	as "Largo", '' as "ValorDefecto", 'N'	as "Obligatorio",  'S'	as "Editable",  'S'	as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Aut' as "Esquema", 'V1' as "Version",'Dig' as "Nombre",'' as "CodigoSII", '' as "TipoDiccionario",	4	as "Orden", 'Dig'	as "Titulo", 'Ayuda de 	Dig	'	as "Ayuda", 'TEXT'	as  "TipoDato",	250	as "Largo", '' as "ValorDefecto", 'N'	as "Obligatorio",  'S'	as "Editable",  'S'	as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Aut' as "Esquema", 'V1' as "Version",'Pasaporte' as "Nombre",'' as "CodigoSII", '' as "TipoDiccionario",	5	as "Orden", 'Pasaporte'	as "Titulo", 'Ayuda de 	Pasaporte	'	as "Ayuda", 'TEXT'	as  "TipoDato",	250	as "Largo", '' as "ValorDefecto",'N' as "Obligatorio",  'S' as "Editable",  'S'	as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Aut' as "Esquema", 'V1' as "Version",'Glei' as "Nombre",'' as "CodigoSII", '' as "TipoDiccionario",	6	as "Orden", 'Glei'	as "Titulo", 'Ayuda de 	Glei	'	as "Ayuda", 'TEXT'	as  "TipoDato",	250	as "Largo", '' as "ValorDefecto",'N' as "Obligatorio",  'S' as "Editable",  'S'	as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Aut' as "Esquema", 'V1' as "Version",'ApellidoPaterno' as "Nombre",'' as "CodigoSII", '' as "TipoDiccionario",	7	as "Orden", 'ApellidoPaterno'	as "Titulo", 'Ayuda de 	ApellidoPaterno	'	as "Ayuda", 'TEXT'	as  "TipoDato",	250	as "Largo", '' as "ValorDefecto",'N' as "Obligatorio",  'S' as "Editable",  'S'	as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Aut' as "Esquema", 'V1' as "Version",'ApellidoMaterno' as "Nombre",'' as "CodigoSII", '' as "TipoDiccionario",	8	as "Orden", 'ApellidoMaterno'	as "Titulo", 'Ayuda de 	ApellidoMaterno	'	as "Ayuda", 'TEXT'	as  "TipoDato",	250	as "Largo", '' as "ValorDefecto",'N' as "Obligatorio",  'S' as "Editable",  'S'	as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Aut' as "Esquema", 'V1' as "Version",'PrimerNombre' as "Nombre",'' as "CodigoSII", '' as "TipoDiccionario",	9	as "Orden", 'PrimerNombre'	as "Titulo", 'Ayuda de 	PrimerNombre	'	as "Ayuda", 'TEXT'	as  "TipoDato",	250	as "Largo", '' as "ValorDefecto",'N' as "Obligatorio",  'S' as "Editable",  'S'	as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Aut' as "Esquema", 'V1' as "Version",'SegundoNombre' as "Nombre",'' as "CodigoSII", '' as "TipoDiccionario",	10	as "Orden", 'SegundoNombre'	as "Titulo", 'Ayuda de 	SegundoNombre	'	as "Ayuda", 'TEXT'	as  "TipoDato",	250	as "Largo", '' as "ValorDefecto",'N' as "Obligatorio",  'S' as "Editable",  'S'	as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Aut' as "Esquema", 'V1' as "Version",'RazonSocial' as "Nombre",'' as "CodigoSII", '' as "TipoDiccionario",	11	as "Orden", 'RazonSocial'	as "Titulo", 'Ayuda de 	RazonSocial	'	as "Ayuda", 'TEXT'	as  "TipoDato",	250	as "Largo", '' as "ValorDefecto",'N' as "Obligatorio",  'S' as "Editable",  'S'	as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Aut' as "Esquema", 'V1' as "Version",'Telefono' as "Nombre",'' as "CodigoSII", '' as "TipoDiccionario",	12	as "Orden", 'Telefono'	as "Titulo", 'Ayuda de 	Telefono	'	as "Ayuda", 'TEXT'	as  "TipoDato",	250	as "Largo", '' as "ValorDefecto",'N' as "Obligatorio",  'S' as "Editable",  'S'	as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Aut' as "Esquema", 'V1' as "Version",'Email' as "Nombre",'' as "CodigoSII", '' as "TipoDiccionario",	13	as "Orden", 'Email'	as "Titulo", 'Ayuda de 	Email	'	as "Ayuda", 'TEXT'	as  "TipoDato",	250	as "Largo", '' as "ValorDefecto",'N' as "Obligatorio",  'S' as "Editable",  'S'	as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Aut' as "Esquema", 'V1' as "Version",'DireccionTipo' as "Nombre",'' as "CodigoSII", '' as "TipoDiccionario",	14	as "Orden", 'DireccionTipo'	as "Titulo", 'Ayuda de 	DireccionTipo	'	as "Ayuda", 'TEXT'	as  "TipoDato",	250	as "Largo", '' as "ValorDefecto",'N' as "Obligatorio",  'S' as "Editable",  'S'	as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Aut' as "Esquema", 'V1' as "Version",'DireccionNombre' as "Nombre",'' as "CodigoSII", '' as "TipoDiccionario",	15	as "Orden", 'DireccionNombre'	as "Titulo", 'Ayuda de 	DireccionNombre	'	as "Ayuda", 'TEXT'	as  "TipoDato",	250	as "Largo", '' as "ValorDefecto",'N' as "Obligatorio",  'S' as "Editable",  'S'	as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Aut' as "Esquema", 'V1' as "Version",'DireccionNumero' as "Nombre",'' as "CodigoSII", '' as "TipoDiccionario",	16	as "Orden", 'DireccionNumero'	as "Titulo", 'Ayuda de 	DireccionNumero	'	as "Ayuda", 'TEXT'	as  "TipoDato",	250	as "Largo", '' as "ValorDefecto",'N' as "Obligatorio",  'S' as "Editable",  'S'	as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Aut' as "Esquema", 'V1' as "Version",'DireccionDepto' as "Nombre",'' as "CodigoSII", '' as "TipoDiccionario",	17	as "Orden", 'DireccionDepto'	as "Titulo", 'Ayuda de 	DireccionDepto	'	as "Ayuda", 'TEXT'	as  "TipoDato",	250	as "Largo", '' as "ValorDefecto",'N' as "Obligatorio",  'S' as "Editable",  'S'	as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Aut' as "Esquema", 'V1' as "Version",'DireccionComuna' as "Nombre",'' as "CodigoSII", '' as "TipoDiccionario",	18	as "Orden", 'DireccionComuna'	as "Titulo", 'Ayuda de 	DireccionComuna	'	as "Ayuda", 'TEXT'	as  "TipoDato",	250	as "Largo", '' as "ValorDefecto",'N' as "Obligatorio",  'S' as "Editable",  'S'	as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Aut' as "Esquema", 'V1' as "Version",'DireccionCiudad' as "Nombre",'' as "CodigoSII", '' as "TipoDiccionario",	19	as "Orden", 'DireccionCiudad'	as "Titulo", 'Ayuda de 	DireccionCiudad	'	as "Ayuda", 'TEXT'	as  "TipoDato",	250	as "Largo", '' as "ValorDefecto",'N' as "Obligatorio",  'S' as "Editable",  'S'	as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Aut' as "Esquema", 'V1' as "Version",'DireccionEstado' as "Nombre",'' as "CodigoSII", '' as "TipoDiccionario",	20	as "Orden", 'DireccionEstado'	as "Titulo", 'Ayuda de 	DireccionEstado	'	as "Ayuda", 'TEXT'	as  "TipoDato",	250	as "Largo", '' as "ValorDefecto",'N' as "Obligatorio",  'S' as "Editable",  'S'	as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Aut' as "Esquema", 'V1' as "Version",'DireccionPais' as "Nombre",'' as "CodigoSII", '' as "TipoDiccionario",	21	as "Orden", 'DireccionPais'	as "Titulo", 'Ayuda de 	DireccionPais	'	as "Ayuda", 'TEXT'	as  "TipoDato",	250	as "Largo", '' as "ValorDefecto",'N' as "Obligatorio",  'S' as "Editable",  'S'	as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Aut' as "Esquema", 'V1' as "Version",'DireccionCodPostal' as "Nombre",'' as "CodigoSII", '' as "TipoDiccionario",	22	as "Orden", 'DireccionCodPostal'	as "Titulo", 'Ayuda de 	DireccionCodPostal	'	as "Ayuda", 'TEXT'	as  "TipoDato",	250	as "Largo", '' as "ValorDefecto",'N' as "Obligatorio",  'S' as "Editable",  'S'	as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Aut' as "Esquema", 'V1' as "Version",'Nacionalidad' as "Nombre",'' as "CodigoSII", '' as "TipoDiccionario",	23	as "Orden", 'Nacionalidad'	as "Titulo", 'Ayuda de 	Nacionalidad	'	as "Ayuda", 'TEXT'	as  "TipoDato",	250	as "Largo", '' as "ValorDefecto",'N' as "Obligatorio",  'S' as "Editable",  'S'	as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Aut' as "Esquema", 'V1' as "Version",'RelacionIntermediario' as "Nombre",'' as "CodigoSII", '' as "TipoDiccionario",	24	as "Orden", 'RelacionIntermediario'	as "Titulo", 'Ayuda de 	RelacionIntermediario	'	as "Ayuda", 'TEXT'	as  "TipoDato",	250	as "Largo", '' as "ValorDefecto",'N' as "Obligatorio",  'S' as "Editable",  'S'	as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;


--ASIGNACION
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Asig' as "Esquema",	'V1'	as "Version", 'Rut'	as "Nombre", ' ' as "CodigoSII", ''	as "TipoDiccionario",	1	as "Orden", 'Rut'	as "Titulo", 'Ayuda de 	Rut	'	as "Ayuda", 'TEXT'	as "TipoDato",	100	as "Largo", ''	as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", ' ' as "Grupo",0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Asig' as "Esquema",	'V1'	as "Version", 'Cuenta'	as "Nombre", ' ' as "CodigoSII", ''	as "TipoDiccionario",	2	as "Orden", 'Cuenta'	as "Titulo", 'Ayuda de 	Cuenta	'	as "Ayuda", 'ID'	as "TipoDato",	0	as "Largo", '0'	as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", ' ' as "Grupo",0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Asig' as "Esquema",	'V1'	as "Version", 'Nombre'	as "Nombre", ' ' as "CodigoSII", ''	as "TipoDiccionario",	3	as "Orden", 'Nombre'	as "Titulo", 'Ayuda de 	Nombre	'	as "Ayuda", 'TEXT'	as "TipoDato",	100	as "Largo", ''	as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", ' ' as "Grupo",0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Asig' as "Esquema",	'V1'	as "Version", 'Bolsa'	as "Nombre", ' ' as "CodigoSII", ''	as "TipoDiccionario",	4	as "Orden", 'Bolsa'	as "Titulo", 'Ayuda de 	Bolsa	'	as "Ayuda", 'TEXT'	as "TipoDato",	100	as "Largo", ''	as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", ' ' as "Grupo",0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Asig' as "Esquema",	'V1'	as "Version", 'Folio'	as "Nombre", ' ' as "CodigoSII", ''	as "TipoDiccionario",	5	as "Orden", 'Folio'	as "Titulo", 'Ayuda de 	Folio	'	as "Ayuda", 'ID'	as "TipoDato",	100	as "Largo", '0'	as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", ' ' as "Grupo",0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Asig' as "Esquema",	'V1'	as "Version", 'Instrumento'	as "Nombre", ' ' as "CodigoSII", ''	as "TipoDiccionario",	6	as "Orden", 'Instrumento'	as "Titulo", 'Ayuda de 	Instrumento	' as "Ayuda", 'TEXT'	as "TipoDato",	100	as "Largo", ''	as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", ' ' as "Grupo",0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Asig' as "Esquema",	'V1'	as "Version", 'TipoOperacion'	as "Nombre", ' ' as "CodigoSII", ''	as "TipoDiccionario",	7	as "Orden", 'TipoOperacion'	as "Titulo", 'Ayuda de 	TipoOperacion	'	as "Ayuda", 'TEXT'	as "TipoDato",	100	as "Largo", ''	as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", ' ' as "Grupo",0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Asig' as "Esquema",	'V1'	as "Version", 'NumeroOrden'	as "Nombre", ' ' as "CodigoSII", ''	as "TipoDiccionario",	8	as "Orden", 'NumeroOrden'	as "Titulo", 'Ayuda de 	NumeroOrden	'	as "Ayuda", 'ID'	as "TipoDato",	0	as "Largo", '0'	as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", ' ' as "Grupo",0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Asig' as "Esquema",	'V1'	as "Version", 'NumeroOperacion'	as "Nombre", ' ' as "CodigoSII", ''	as "TipoDiccionario",	9	as "Orden", 'NumeroOperacion'	as "Titulo", 'Ayuda de 	NumeroOperacion	'	as "Ayuda", 'ID'	as "TipoDato",	0	as "Largo", '0'	as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", ' ' as "Grupo",0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Asig' as "Esquema",	'V1'	as "Version", 'UnidadesAsignadas'	as "Nombre", ' ' as "CodigoSII", ''	as "TipoDiccionario",	10	as "Orden", 'UnidadesAsignadas'	as "Titulo", 'Ayuda de 	UnidadesAsignadas'	as "Ayuda", 'QTY'	as "TipoDato",	0	as "Largo", '0'	as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", ' ' as "Grupo",0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Asig' as "Esquema",	'V1'	as "Version", 'MontoTotal'	as "Nombre", ' ' as "CodigoSII", ''	as "TipoDiccionario",	11	as "Orden", 'MontoTotal'	as "Titulo", 'Ayuda de 	MontoTotal	'	as "Ayuda", 'DECIMAL'	as "TipoDato",	0	as "Largo", '0'	as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", ' ' as "Grupo",0	as "OrdenCargaExterna" ,4 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Asig' as "Esquema",	'V1'	as "Version", 'Moneda'	as "Nombre", ' ' as "CodigoSII", ''	as "TipoDiccionario",	12	as "Orden", 'Moneda'	as "Titulo", 'Ayuda de 	Moneda'	as "Ayuda", 'TEXT'	as "TipoDato",	0	as "Largo", ''	as "ValorDefecto", 'N'	as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", ' ' as "Grupo",0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Asig' as "Esquema",	'V1'	as "Version", 'FechaAsignacion'	as "Nombre", ' ' as "CodigoSII", ''	as "TipoDiccionario",	13	as "Orden", 'FechaAsignacion'	as "Titulo", 'Ayuda de 	FechaAsignacion	'	as "Ayuda", 'DATE_FILTER'	as "TipoDato",	0	as "Largo", ''	as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", ' ' as "Grupo",0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Asig' as "Esquema",	'V1'	as "Version", 'HoraAsignacion'	as "Nombre", ' ' as "CodigoSII", ''	as "TipoDiccionario",	14	as "Orden", 'HoraAsignacion'	as "Titulo", 'Ayuda de 	HoraAsignacion	'	as "Ayuda", 'TEXT'	as "TipoDato",	100	as "Largo", ''	as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", ' ' as "Grupo",0	as "OrdenCargaExterna" ,0 as "CantidadDecimales" from dummy;



--OPERACIONES
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ope'  as "Esquema", 'V1' as "Version",'NombreCliente' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	1	as "Orden", 'Nombre' as "Titulo", 'Ayuda de 	Nombre' as "Ayuda", 'TEXT' as "TipoDato",	0	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ope'  as "Esquema", 'V1' as "Version",'RutCliente' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	2	as "Orden", 'Rut' as "Titulo", 'Ayuda de 	Rut' as "Ayuda", 'TEXT' as "TipoDato",	0	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ope'  as "Esquema", 'V1' as "Version",'CodigoPortafolio' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	3	as "Orden", 'Cuenta' as "Titulo", 'Ayuda de 	Cuenta' as "Ayuda", 'TEXT' as "TipoDato",	250	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ope'  as "Esquema", 'V1' as "Version",'CodigoBolsa' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	4	as "Orden", 'Bolsa' as "Titulo", 'Ayuda de 	Bolsa' as "Ayuda", 'TEXT' as "TipoDato",	250	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ope'  as "Esquema", 'V1' as "Version",'Folio' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	5	as "Orden", 'Folio' as "Titulo", 'Ayuda de 	Folio' as "Ayuda", 'ID' as "TipoDato",	0	as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ope'  as "Esquema", 'V1' as "Version",'Instrumento' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	6	as "Orden", 'Instrumento' as "Titulo", 'Ayuda de 	Instrumento' as "Ayuda", 'TEXT' as "TipoDato",	250	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ope'  as "Esquema", 'V1' as "Version",'TipoOperacion' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	7	as "Orden", 'TipoOperacion' as "Titulo", 'Ayuda de 	TipoOperacion' as "Ayuda", 'TEXT' as "TipoDato",	250	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ope'  as "Esquema", 'V1' as "Version",'IdOperacion' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	8	as "Orden", 'NumeroOperacion' as "Titulo", 'Ayuda de 	NumeroOperacion' as "Ayuda", 'ID' as "TipoDato",	0	as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ope'  as "Esquema", 'V1' as "Version",'IdBolsa' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	9	as "Orden", 'IdBolsa' as "Titulo", 'Ayuda de 	IdBolsa' as "Ayuda", 'ID' as "TipoDato",	0	as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ope'  as "Esquema", 'V1' as "Version",'Fecha' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	10	as "Orden", 'Fecha' as "Titulo", 'Ayuda de 	Fecha' as "Ayuda", 'TEXT' as "TipoDato",	10	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ope'  as "Esquema", 'V1' as "Version",'Hora' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	11	as "Orden", 'Hora' as "Titulo", 'Ayuda de 	Hora' as "Ayuda", 'TEXT' as "TipoDato",	250	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ope'  as "Esquema", 'V1' as "Version",'NumeroOperador' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	12	as "Orden", 'NumeroOperador' as "Titulo", 'Ayuda de 	NumeroOperador' as "Ayuda", 'ID' as "TipoDato",	0	as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ope'  as "Esquema", 'V1' as "Version",'ISIN' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	13	as "Orden", 'Isin' as "Titulo", 'Ayuda de 	Isin' as "Ayuda", 'TEXT' as "TipoDato",	250	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ope'  as "Esquema", 'V1' as "Version",'Nemotecnico' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	14	as "Orden", 'Nemotecnico' as "Titulo", 'Ayuda de 	Nemotecnico' as "Ayuda", 'TEXT' as "TipoDato",	250	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ope'  as "Esquema", 'V1' as "Version",'TipoInstrumento' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	15	as "Orden", 'TipoInstrumento' as "Titulo", 'Ayuda de 	TipoInstrumento' as "Ayuda", 'TEXT' as "TipoDato",	250	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ope'  as "Esquema", 'V1' as "Version",'TipoOperacionCod' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	16	as "Orden", 'TipoOperacion' as "Titulo", 'Ayuda de 	TipoOperacion' as "Ayuda", 'TEXT' as "TipoDato",	250	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ope'  as "Esquema", 'V1' as "Version",'CondicionLiquidacion' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	17	as "Orden", 'CondicionLiquidacion' as "Titulo", 'Ayuda de 	CondicionLiquidacion' as "Ayuda", 'TEXT' as "TipoDato",	250	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ope'  as "Esquema", 'V1' as "Version",'Cantidad' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	18	as "Orden", 'Cantidad' as "Titulo", 'Ayuda de 	Cantidad' as "Ayuda", 'QTY' as "TipoDato",	0	as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ope'  as "Esquema", 'V1' as "Version",'MontoTotal' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	19	as "Orden", 'MontoTotal' as "Titulo", 'Ayuda de 	MontoTotal' as "Ayuda", 'DECIMAL' as "TipoDato",	250	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	4	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ope'  as "Esquema", 'V1' as "Version",'Moneda' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	20	as "Orden", 'Moneda' as "Titulo", 'Ayuda de 	Moneda' as "Ayuda", 'TEXT' as "TipoDato",	250	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ope'  as "Esquema", 'V1' as "Version",'Precio' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	21	as "Orden", 'Precio' as "Titulo", 'Ayuda de 	Precio' as "Ayuda", 'DECIMAL' as "TipoDato",	250	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	4	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ope'  as "Esquema", 'V1' as "Version",'Mercado' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	22	as "Orden", 'Mercado' as "Titulo", 'Ayuda de 	Mercado' as "Ayuda", 'TEXT' as "TipoDato",	250	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ope'  as "Esquema", 'V1' as "Version",'SistemaTransaccion' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	23	as "Orden", 'SistemaTransaccion' as "Titulo", 'Ayuda de 	SistemaTransaccion' as "Ayuda", 'TEXT' as "TipoDato",	250	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ope'  as "Esquema", 'V1' as "Version",'ModalidadEjecucion' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	24	as "Orden", 'ModalidadEjecucion' as "Titulo", 'Ayuda de 	ModalidadEjecucion' as "Ayuda", 'TEXT' as "TipoDato",	250	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ope'  as "Esquema", 'V1' as "Version",'RutContraparte' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	25	as "Orden", 'RutContraparte' as "Titulo", 'Ayuda de 	RutContraparte' as "Ayuda", 'TEXT' as "TipoDato",	250	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ope'  as "Esquema", 'V1' as "Version",'DigitoContraparte' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	26	as "Orden", 'DigContraparte' as "Titulo", 'Ayuda de 	DigContraparte' as "Ayuda", 'TEXT' as "TipoDato",	250	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ope'  as "Esquema", 'V1' as "Version",'Pasaporte' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	27	as "Orden", 'Pasaporte' as "Titulo", 'Ayuda de 	Pasaporte' as "Ayuda", 'TEXT' as "TipoDato",	250	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ope'  as "Esquema", 'V1' as "Version",'GLEI' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	28	as "Orden", 'Glei' as "Titulo", 'Ayuda de 	Glei' as "Ayuda", 'TEXT' as "TipoDato",	250	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'NCG380Ope'  as "Esquema", 'V1' as "Version",'Observaciones' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",	29	as "Orden", 'Observaciones' as "Titulo", 'Ayuda de 	Observaciones' as "Ayuda", 'TEXT' as "TipoDato",	250	as "Largo", '' as "ValorDefecto", 'N' as "Obligatorio", 'S' as "Editable", 'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo",	0	as "OrdenCargaExterna" ,	0	as "CantidadDecimales"	from dummy;

--Instrucciones

insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Ins' as "Esquema",
'2017' as "Version",
'DocumentoIdentidad'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
1	as "Orden",
'DocumentoIdentidad'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de DocumentoIdentidad'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Ins' as "Esquema",
'2017' as "Version",
'CodigoISIN'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
2	as "Orden",
'CodigoISIN'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de CodigoISIN'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Ins' as "Esquema",
'2017' as "Version",
'Nemotecnico'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
3	as "Orden",
'Nemotecnico'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de Nemotecnico'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Ins' as "Esquema",
'2017' as "Version",
'IdEmisor'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
4	as "Orden",
'IdEmisor'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de IdEmisor'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Ins' as "Esquema",
'2017' as "Version",
'TipoJunta'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
5	as "Orden",
'TipoJunta'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de TipoJunta'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Ins' as "Esquema",
'2017' as "Version",
'Lugar'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
6	as "Orden",
'Lugar'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de Lugar'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Ins' as "Esquema",
'2017' as "Version",
'FechaRealizacion'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
7	as "Orden",
'FechaRealizacion'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de FechaRealizacion'	as "Ayuda",
'DATE'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Ins' as "Esquema",
'2017' as "Version",
'Materias'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
8	as "Orden",
'Materias'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de Materias'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Ins' as "Esquema",
'2017' as "Version",
'Instruccion'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
9	as "Orden",
'Instruccion'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de Instruccion'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Ins' as "Esquema",
'2017' as "Version",
'CantidadDerecho'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
10	as "Orden",
'CantidadDerecho'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de CantidadDerecho'	as "Ayuda",
'DECIMAL'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
8 as "CantidadDecimales" 
from dummy;

--Repactaciones

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Rep' as "Esquema",
'2017' as "Version",
'DocumentoIdentidad'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
1	as "Orden",
'DocumentoIdentidad'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de DocumentoIdentidad'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Rep' as "Esquema",
'2017' as "Version",
'DigitoVerificador'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
2	as "Orden",
'DigitoVerificador'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de DigitoVerificador'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Rep' as "Esquema",
'2017' as "Version",
'Pasaporte'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
3	as "Orden",
'Pasaporte'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de Pasaporte'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Rep' as "Esquema",
'2017' as "Version",
'GlobalLegalEntity'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
4	as "Orden",
'GlobalLegalEntity'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de GlobalLegalEntity'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Rep' as "Esquema",
'2017' as "Version",
'ApellidoPaterno'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
5	as "Orden",
'ApellidoPaterno'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de ApellidoPaterno'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Rep' as "Esquema",
'2017' as "Version",
'ApellidoMaterno'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
6	as "Orden",
'ApellidoMaterno'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de ApellidoMaterno'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Rep' as "Esquema",
'2017' as "Version",
'PrimerNombre'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
7	as "Orden",
'PrimerNombre'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de PrimerNombre'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Rep' as "Esquema",
'2017' as "Version",
'SegundoNombre'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
8	as "Orden",
'SegundoNombre'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de SegundoNombre'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Rep' as "Esquema",
'2017' as "Version",
'RazonSocial'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
9	as "Orden",
'RazonSocial'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de RazonSocial'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Rep' as "Esquema",
'2017' as "Version",
'Telefono'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
10	as "Orden",
'Telefono'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de Telefono'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Rep' as "Esquema",
'2017' as "Version",
'DireccionElectronica'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
11	as "Orden",
'DireccionElectronica'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de DireccionElectronica'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Rep' as "Esquema",
'2017' as "Version",
'TipoDireccion'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
12	as "Orden",
'TipoDireccion'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de TipoDireccion'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Rep' as "Esquema",
'2017' as "Version",
'Calle'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
13	as "Orden",
'Calle'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de Calle'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Rep' as "Esquema",
'2017' as "Version",
'Numero'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
14	as "Orden",
'Numero'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de Numero'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Rep' as "Esquema",
'2017' as "Version",
'Departamento'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
15	as "Orden",
'Departamento'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de Departamento'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Rep' as "Esquema",
'2017' as "Version",
'Comuna'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
16	as "Orden",
'Comuna'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de Comuna'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Rep' as "Esquema",
'2017' as "Version",
'Ciudad'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
17	as "Orden",
'Ciudad'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de Ciudad'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Rep' as "Esquema",
'2017' as "Version",
'Provincia'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
18	as "Orden",
'Provincia'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de Provincia'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Rep' as "Esquema",
'2017' as "Version",
'Pais'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
19	as "Orden",
'Pais'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de Pais'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Rep' as "Esquema",
'2017' as "Version",
'CodigoPostal'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
20	as "Orden",
'CodigoPostal'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de CodigoPostal'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Rep' as "Esquema",
'2017' as "Version",
'PaisNacimiento'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
21	as "Orden",
'PaisNacimiento'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de PaisNacimiento'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Rep' as "Esquema",
'2017' as "Version",
'Fecha'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
22	as "Orden",
'Fecha'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de Fecha'	as "Ayuda",
'DATE'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Rep' as "Esquema",
'2017' as "Version",
'Monto'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
23	as "Orden",
'Monto'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de Monto'	as "Ayuda",
'DECIMAL'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
8 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Rep' as "Esquema",
'2017' as "Version",
'Moneda'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
24	as "Orden",
'Moneda'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de Moneda'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Rep' as "Esquema",
'2017' as "Version",
'FechaRepactacion'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
25	as "Orden",
'FechaRepactacion'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de FechaRepactacion'	as "Ayuda",
'DATE'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Rep' as "Esquema",
'2017' as "Version",
'FechaAbono'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
26	as "Orden",
'FechaAbono'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de FechaAbono'	as "Ayuda",
'DATE'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Rep' as "Esquema",
'2017' as "Version",
'MontoAbono'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
27	as "Orden",
'MontoAbono'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de MontoAbono'	as "Ayuda",
'DECIMAL'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
8 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Rep' as "Esquema",
'2017' as "Version",
'MonedaAbono'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
28	as "Orden",
'MonedaAbono'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de MonedaAbono'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

--Deudores

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Deu' as "Esquema",
'2017' as "Version",
'DocumentoIdentidad'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
1	as "Orden",
'DocumentoIdentidad'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de DocumentoIdentidad'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Deu' as "Esquema",
'2017' as "Version",
'DigitoVerificador'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
2	as "Orden",
'DigitoVerificador'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de DigitoVerificador'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Deu' as "Esquema",
'2017' as "Version",
'Pasaporte'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
3	as "Orden",
'Pasaporte'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de Pasaporte'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Deu' as "Esquema",
'2017' as "Version",
'GlobalLegalEntity'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
4	as "Orden",
'GlobalLegalEntity'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de GlobalLegalEntity'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Deu' as "Esquema",
'2017' as "Version",
'ApellidoPaterno'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
5	as "Orden",
'ApellidoPaterno'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de ApellidoPaterno'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Deu' as "Esquema",
'2017' as "Version",
'ApellidoMaterno'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
6	as "Orden",
'ApellidoMaterno'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de ApellidoMaterno'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Deu' as "Esquema",
'2017' as "Version",
'PrimerNombre'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
7	as "Orden",
'PrimerNombre'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de PrimerNombre'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Deu' as "Esquema",
'2017' as "Version",
'SegundoNombre'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
8	as "Orden",
'SegundoNombre'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de SegundoNombre'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Deu' as "Esquema",
'2017' as "Version",
'RazonSocial'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
9	as "Orden",
'RazonSocial'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de RazonSocial'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Deu' as "Esquema",
'2017' as "Version",
'Telefono'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
10	as "Orden",
'Telefono'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de Telefono'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Deu' as "Esquema",
'2017' as "Version",
'DireccionElectronica'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
11	as "Orden",
'DireccionElectronica'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de DireccionElectronica'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Deu' as "Esquema",
'2017' as "Version",
'TipoDireccion'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
12	as "Orden",
'TipoDireccion'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de TipoDireccion'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Deu' as "Esquema",
'2017' as "Version",
'Calle'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
13	as "Orden",
'Calle'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de Calle'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Deu' as "Esquema",
'2017' as "Version",
'Numero'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
14	as "Orden",
'Numero'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de Numero'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Deu' as "Esquema",
'2017' as "Version",
'Departamento'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
15	as "Orden",
'Departamento'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de Departamento'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Deu' as "Esquema",
'2017' as "Version",
'Comuna'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
16	as "Orden",
'Comuna'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de Comuna'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Deu' as "Esquema",
'2017' as "Version",
'Ciudad'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
17	as "Orden",
'Ciudad'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de Ciudad'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Deu' as "Esquema",
'2017' as "Version",
'Provincia'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
18	as "Orden",
'Provincia'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de Provincia'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Deu' as "Esquema",
'2017' as "Version",
'Pais'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
19	as "Orden",
'Pais'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de Pais'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Deu' as "Esquema",
'2017' as "Version",
'CodigoPostal'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
20	as "Orden",
'CodigoPostal'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de CodigoPostal'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Deu' as "Esquema",
'2017' as "Version",
'PaisNacimiento'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
21	as "Orden",
'PaisNacimiento'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de PaisNacimiento'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Deu' as "Esquema",
'2017' as "Version",
'Fecha'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
22	as "Orden",
'Fecha'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de Fecha'	as "Ayuda",
'DATE'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Deu' as "Esquema",
'2017' as "Version",
'Monto'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
23	as "Orden",
'Monto'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de Monto'	as "Ayuda",
'DECIMAL'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
8 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Deu' as "Esquema",
'2017' as "Version",
'Moneda'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
24	as "Orden",
'Moneda'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de Moneda'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Deu' as "Esquema",
'2017' as "Version",
'FechaRepactacion'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
25	as "Orden",
'FechaRepactacion'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de FechaRepactacion'	as "Ayuda",
'DATE'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Deu' as "Esquema",
'2017' as "Version",
'FechaAbono'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
26	as "Orden",
'FechaAbono'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de FechaAbono'	as "Ayuda",
'DATE'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Deu' as "Esquema",
'2017' as "Version",
'MontoAbono'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
27	as "Orden",
'MontoAbono'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de MontoAbono'	as "Ayuda",
'DECIMAL'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
8 as "CantidadDecimales" 
from dummy;

insert into "BSCL.Normativa.BD::DOC.Campo" 
select 'NCG380Deu' as "Esquema",
'2017' as "Version",
'MonedaAbono'	as "Nombre", --nombre de la columna
' ' as "CodigoSII", 
' '	as "TipoDiccionario",
28	as "Orden",
'MonedaAbono'	as "Titulo", -- de cabecera cuando va con cabecera
'Ayuda de MonedaAbono'	as "Ayuda",
'TEXT'	as "TipoDato", --TEXT DATE DECIMAL ID
0	as "Largo", 
' '	as "ValorDefecto",
'N'	as "Obligatorio", 
'S'	as "Editable",  
'S'	as "ModoMasivo", 
'S'	as "Exportable", 
'N'	as "OcultoEnGrilla", 
'N'	as "OcultoEnMantenedor",
'N'	as "Comun", 
'N'	as "Clave", 
' '	as "Grupo",	
0	as "OrdenCargaExterna" ,
0 as "CantidadDecimales" 
from dummy;

/*==============================================================*/
/* DELETE TIPO DOCUMENTO                                        */
/*==============================================================*/

delete from "BSCL"."BSCL.Normativa.BD::DOC.TipoDocumento" where "IdTipoDocumento" 
in ('NCG380CLI','NCG380ORD','NCG380ASIG','NCG380FAC','NCG380OPE','NCG380CUS','NCG380AUT','NCG380TRA','NCG380REP','NCG380INS','NCG380INTB');

/*==============================================================*/
/* TIPO DOCUMENTO                                               */
/*==============================================================*/

INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.TipoDocumento"("IdTipoDocumento","Descripcion")values 
('NCG380CLI','Interfaz NCG380 Cliente');

INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.TipoDocumento"("IdTipoDocumento","Descripcion")values 
('NCG380ORD','Interfaz NCG380 Ordenes');

INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.TipoDocumento"("IdTipoDocumento","Descripcion")values 
('NCG380ASIG','Interfaz NCG380 Asignaciones');

INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.TipoDocumento"("IdTipoDocumento","Descripcion")values 
('NCG380FAC','Interfaz NCG380 Facturacion');

INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.TipoDocumento"("IdTipoDocumento","Descripcion")values 
('NCG380OPE','Interfaz NCG380 Operaciones');

INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.TipoDocumento"("IdTipoDocumento","Descripcion")values 
('NCG380CUS','Interfaz NCG380 Custodia');

INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.TipoDocumento"("IdTipoDocumento","Descripcion")values 
('NCG380AUT','Interfaz NCG380 Autorizados');

INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.TipoDocumento"("IdTipoDocumento","Descripcion")values 
('NCG380TRA','Interfaz NCG380 Trabajadores');

INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.TipoDocumento"("IdTipoDocumento","Descripcion")values 
('NCG380REP','Interfaz NCG380 Repactaciones');

INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.TipoDocumento"("IdTipoDocumento","Descripcion")values 
('NCG380INS','Interfaz NCG380 Instrucciones');

INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.TipoDocumento"("IdTipoDocumento","Descripcion")values 
('NCG380INTB','Interfaz NCG380 Operaciones Bursatiles');


/*==============================================================*/
/*                       Homologacion                           */
/*==============================================================*/

--delete from "BSCL.Normativa.BD::DOC.Homologacion" where "Esquema" = 'NCG380Cus'


insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (1,'NCG380Cus','V1','','','ANSIMC','ECV','RET');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (2,'NCG380Cus','V1','','','ANSIMV','ECV','RET');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (3,'NCG380Cus','V1','','','ANTEMP','ECV','RET');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (4,'NCG380Cus','V1','','','ANTPRT','ECV','RET');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (5,'NCG380Cus','V1','','','COMACN','ICC','ING');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (6,'NCG380Cus','V1','','','COMACX','ICC','ING');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (7,'NCG380Cus','V1','','','COMCFI','ICC','ING');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (8,'NCG380Cus','V1','','','COMCFM','ICC','ING');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (9,'NCG380Cus','V1','','','COMCFX','ICC','ING');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (10,'NCG380Cus','V1','','','COMETN','ICC','ING');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (11,'NCG380Cus','V1','','','COMETX','ICC','ING');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (12,'NCG380Cus','V1','','','COMFUT','ICC','ING');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (13,'NCG380Cus','V1','','','COMFWD','ICC','ING');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (14,'NCG380Cus','V1','','','COMIIF','ICC','ING');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (15,'NCG380Cus','V1','','','COMMIL','ICC','ING');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (16,'NCG380Cus','V1','','','COMMON','ICC','ING');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (17,'NCG380Cus','V1','','','COMPAC','ICC','ING');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (18,'NCG380Cus','V1','','','COMPAC','ECV','RET');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (19,'NCG380Cus','V1','','','COMRF','ICC','ING');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (20,'NCG380Cus','V1','','','COMRF','ECV','RET');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (21,'NCG380Cus','V1','','','COMSIM','ICC','ING');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (22,'NCG380Cus','V1','','','COMSIM','ECV','RET');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (23,'NCG380Cus','V1','','','COMSPT','ICC','ING');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (24,'NCG380Cus','V1','','','COMTER','ICC','ING');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (25,'NCG380Cus','V1','','','COMTER','ECV','RET');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (26,'NCG380Cus','V1','','','EMPRES','ICC','ING');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (27,'NCG380Cus','V1','','','INGCAN','ICC','ING');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (28,'NCG380Cus','V1','','','INGCAN','ECV','RET');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (29,'NCG380Cus','V1','','','INGDIV','ICC','ING');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (30,'NCG380Cus','V1','','','INGDIV','ECV','RET');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (31,'NCG380Cus','V1','','','INGOSA','ICC','ING');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (32,'NCG380Cus','V1','','','INGTIT','CO','ING');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (33,'NCG380Cus','V1','','','PRESTA','ICC','ING');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (34,'NCG380Cus','V1','','','RETTIT','ECO','RET');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (35,'NCG380Cus','V1','','','SUSOSA','ICC','ING');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (36,'NCG380Cus','V1','','','SUSOSA','ECV','RET');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (37,'NCG380Cus','V1','','','TRATIT','ICC','ING');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (38,'NCG380Cus','V1','','','TRATIT','ECV','RET');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (39,'NCG380Cus','V1','','','VENACN','ECV','RET');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (40,'NCG380Cus','V1','','','VENACX','ECV','RET');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (41,'NCG380Cus','V1','','','VENCFI','ECV','RET');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (42,'NCG380Cus','V1','','','VENCFM','ECV','RET');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (43,'NCG380Cus','V1','','','VENCFX','ECV','RET');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (44,'NCG380Cus','V1','','','VENEMP','ICC','ING');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (45,'NCG380Cus','V1','','','VENEMP','ECV','RET');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (46,'NCG380Cus','V1','','','VENETN','ECV','RET');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (47,'NCG380Cus','V1','','','VENETX','ECV','RET');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (48,'NCG380Cus','V1','','','VENFUT','ECV','RET');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (49,'NCG380Cus','V1','','','VENFWD','ECV','RET');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (50,'NCG380Cus','V1','','','VENIIF','ECV','RET');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (51,'NCG380Cus','V1','','','VENMIL','ECV','RET');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (52,'NCG380Cus','V1','','','VENMON','ECV','RET');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (53,'NCG380Cus','V1','','','VENOSA','ECV','RET');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (54,'NCG380Cus','V1','','','VENPAC','ICC','ING');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (55,'NCG380Cus','V1','','','VENPAC','ECV','RET');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (56,'NCG380Cus','V1','','','VENPRT','ICC','ING');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (57,'NCG380Cus','V1','','','VENPRT','ECV','RET');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (58,'NCG380Cus','V1','','','VENRF','ECV','RET');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (59,'NCG380Cus','V1','','','VENSIM','ICC','ING');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (60,'NCG380Cus','V1','','','VENSIM','ECV','RET');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (61,'NCG380Cus','V1','','','VENSPT','ECV','RET');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (62,'NCG380Cus','V1','','','VENTER','ICC','ING');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (63,'NCG380Cus','V1','','','VENTER','ECV','RET');



insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (64,'NCG380Cus','V1','','','INGGTA','ECO','INGG');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (65,'NCG380Cus','V1','','','INGGTA','CO','RETG');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (66,'NCG380Cus','V1','','','INGOSA','ECO','INGG');
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (67,'NCG380Cus','V1','','','INGOSA','CO' ,'RETG');		
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (68,'NCG380Cus','V1','','','COMSIM','ECO','INGG');		
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (69,'NCG380Cus','V1','','','COMSIM','CO','RETG');		
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (70,'NCG380Cus','V1','','','EMPRES','ECO','INGG');		
insert into "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") values (71,'NCG380Cus','V1','','','EMPRES','CO','RETG');		




/*==============================================================*/
/*                HOMOLOGACION  NCG380Ope                       */
/*==============================================================*/

INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 72, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'COMACN', 'C', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 73, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'INGCAN', '', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 74, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'INGDIV', '', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 75, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'INGOSA', '', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 76, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'SUSOSA', '', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 77, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'VENACN', 'V', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 78, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'ANTEMP', '', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 79, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'EMPRES', '', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 80, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'VENEMP', 'V', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 81, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'ANTPRT', '', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 82, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'PRESTA', '', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 83, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'VENPRT', 'V', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 84, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'COMACX', 'C', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 85, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'VENACX', 'V', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 86, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'COMCFI', 'C', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 87, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'VENCFI', 'V', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 88, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'COMCFM', 'C', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 89, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'VENCFM', 'V', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 90, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'COMCFX', 'C', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 91, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'VENCFX', 'V', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 92, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'COMETN', 'C', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 93, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'VENETN', 'V', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 94, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'COMETX', 'C', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 95, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'VENETX', 'V', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 96, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'COMFUT', 'C', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 97, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'VENFUT', 'V', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 98, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'COMIIF', 'C', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 99, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'VENIIF', 'V', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 100, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'COMMIL', 'C', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 101, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'VENMIL', 'V', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 102, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'COMMON', 'C', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 103, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'VENMON', 'V', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 104, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'COMOSA', 'C', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 105, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'VENOSA', 'V', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 106, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'VENRF', 'V', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 107, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'COMRF', 'C', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 108, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'ANSIMC', '', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 109, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'ANSIMV', '', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 110, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'COMSIM', 'C', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 111, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'VENSIM', 'V', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 112, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'COMFWD', 'C', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 113, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'VENFWD', 'V', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 114, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'COMSPT', 'C', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 115, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'VENSPT', 'V', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 116, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'COMPAC', 'C', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 117, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'COMTER', 'C', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 118, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'VENPAC', 'V', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 119, 'NCG380Ope', 'V1', 'TipoOperacion', '', 'VENTER', 'V', '');


INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 120, 'NCG380Ope', 'V1', 'TipoInstrumento', '', 'ACN', 'ACCN', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 121, 'NCG380Ope', 'V1', 'TipoInstrumento', '', 'ACX', 'ACCE', '');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES ( 122, 'NCG380Ope', 'V1', 'TipoInstrumento', '', 'SPT', 'MONE', '');


/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/

commit;

