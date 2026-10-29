set schema "BSCL";


--delete esquemas
--create esquemas
--delete campos
--create campos
--delete tipo documento
--create tipo documento



/*==============================================================*/
/* DELETE ESQUEMAS                                              */
/*==============================================================*/
/*
delete from "BSCL"."BSCL.Normativa.BD::DOC.Esquema" 
where "Esquema" = 'LOGNOR';
*/
/*==============================================================*/
/* ESQUEMAS                                                     */
/*==============================================================*/

--LOGNOR
insert into "BSCL"."BSCL.Normativa.BD::DOC.Esquema"
select
    'LOGNOR' as "Esquema",
    '2016' as "Version",
    'NOR' as "Modulo",
    'D' as "Periodicidad",
    '' as "Objetivo",
    '' as "PeriodoInicio",
    'SVS' as "IdEntidadReguladora",
    '2016-12-31' as "FechaPresentacionLimite",
    '2016-01-01' as "FechaDatosDesde",
    '2016-12-31' as "FechaDatosHasta",
	1 as "CodigoVersionSII",
	'N' as "HabilitadoMantenedor"
from dummy;

--select top 1 * from "BSCL"."BSCL.Normativa.BD::DOC.Esquema" 



/*==============================================================*/
/* DELETE CAMPOS                                                */
/*==============================================================*/

--delete from "BSCL"."BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'LOGNOR';


/*==============================================================*/
/* CAMPOS                                                       */
/*==============================================================*/

--LOGNOR
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'LOGNOR' as "Esquema", '2016' as "Version", 'IdLogNormativa' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 1 as "Orden", 'IdLogNormativa' as "Titulo", 'Ayuda de	IdLogNormativa' as "Ayuda", 'TEXT' as "TipoDato", 0 as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'LOGNOR' as "Esquema", '2016' as "Version", 'IdTipoLogNormativa' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 2 as "Orden", 'IdTipoLogNormativa' as "Titulo", 'Ayuda de IdTipoLogNormativa' as "Ayuda", 'TEXT' as "TipoDato", 0 as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'LOGNOR' as "Esquema", '2016' as "Version", 'IdUsuario' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 3 as "Orden", 'IdUsuario' as "Titulo", 'Ayuda de	IdUsuario' as "Ayuda", 'TEXT' as "TipoDato", 0 as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'LOGNOR' as "Esquema", '2016' as "Version", 'FechaLog' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 4 as "Orden", 'FechaLog' as "Titulo", 'Ayuda de	FechaLog' as "Ayuda", 'TEXT' as "TipoDato", 0 as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'LOGNOR' as "Esquema", '2016' as "Version", 'IdProceso' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 5 as "Orden", 'IdProceso' as "Titulo", 'Ayuda de	IdProceso' as "Ayuda", 'TEXT' as "TipoDato", 0 as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'LOGNOR' as "Esquema", '2016' as "Version", 'IdSolicitudCircular' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 6 as "Orden", 'IdSolicitudCircular' as "Titulo", 'Ayuda de	IdSolicitudCircular' as "Ayuda", 'TEXT' as "TipoDato", 0 as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'LOGNOR' as "Esquema", '2016' as "Version", 'IdOperacion' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 7 as "Orden", 'IdOperacion' as "Titulo", 'Ayuda de	IdOperacion' as "Ayuda", 'TEXT' as "TipoDato", 0 as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'LOGNOR' as "Esquema", '2016' as "Version", 'Registro' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario",8 as "Orden", 'Registro' as "Titulo", 'Ayuda de	Registro' as "Ayuda", 'TEXT' as "TipoDato", 0 as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'LOGNOR' as "Esquema", '2016' as "Version", 'Observacion' as "Nombre", '' as "CodigoSII", '' as "TipoDiccionario", 9 as "Orden", 'Observacion' as "Titulo", 'Ayuda de	Observacion' as "Ayuda", 'TEXT' as "TipoDato", 0 as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;

/*==============================================================*/
/* DELETE TIPO DOCUMENTO                                        */
/*==============================================================*/

--delete from "BSCL"."BSCL.Normativa.BD::DOC.TipoDocumento" where "IdTipoDocumento" = 'LOGNOR'  ;

/*==============================================================*/
/* TIPO DOCUMENTO                                               */
/*==============================================================*/

INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.TipoDocumento"("IdTipoDocumento","Descripcion","IdNormativa","UI_Visor","Esquema","IdTipoProgramaExportacion","IdTipoProgramaProceso")values 
('LOGNOR','log normativo','LOGNOR','N','LOGNOR','ExportaNCG380','');


/*==============================================================*/
/*                       Homologacion                           */
/*==============================================================*/

--delete from "BSCL.Normativa.BD::DOC.Homologacion" where "Esquema" = 'LOGNOR'
--select * from "BSCL.Normativa.BD::DOC.Homologacion" where "Esquema" = 'LOGNOR'

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/

commit;

