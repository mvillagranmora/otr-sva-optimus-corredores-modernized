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
where "Esquema" = 'CIR1064';
*/
/*==============================================================*/
/* ESQUEMAS                                                     */
/*==============================================================*/


insert into "BSCL"."BSCL.Normativa.BD::DOC.Esquema"
select
    'AMF' as "Esquema",
    'V1' as "Version",
    'D' as "Periodicidad",
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



insert into "BSCL"."BSCL.Normativa.BD::DOC.Esquema"
select
    'ACPE' as "Esquema",
    'V1' as "Version",
    'D' as "Periodicidad",
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


--llenar con Anexo 13
insert into "BSCL"."BSCL.Normativa.BD::DOC.Esquema"
select
    'ACPOB' as "Esquema",
    'V1' as "Version",
    'D' as "Periodicidad",
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
--select top 1 * from "BSCL"."BSCL.Normativa.BD::DOC.Esquema" 


/*==============================================================*/
/* DELETE CAMPOS                                                */
/*==============================================================*/

--delete from "BSCL"."BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'CIR1064';


/*==============================================================*/
/* CAMPOS                                                       */
/*==============================================================*/

--CIR1064








/*==============================================================*/
/* DELETE TIPO DOCUMENTO                                        */
/*==============================================================*/

--delete from "BSCL"."BSCL.Normativa.BD::DOC.TipoDocumento" where "IdTipoDocumento" = ''  ;

/*==============================================================*/
/* TIPO DOCUMENTO                                               */
/*==============================================================*/


INSERT INTO "BSCL.Normativa.BD::DOC.TipoDocumento" ("IdTipoDocumento","Descripcion","IdNormativa","Esquema","IdTipoProgramaExportacion")
VALUES ('ACPOB','Anexo 13','ANEXO','ACPOB','ExportaNCG380');
INSERT INTO "BSCL.Normativa.BD::DOC.TipoDocumento" ("IdTipoDocumento","Descripcion","IdNormativa","Esquema","IdTipoProgramaExportacion")
VALUES ('ACPE','Anexo 1','ANEXO','ACPE','ExportaNCG380');
INSERT INTO "BSCL.Normativa.BD::DOC.TipoDocumento" ("IdTipoDocumento","Descripcion","IdNormativa","Esquema","IdTipoProgramaExportacion")
VALUES ('AMF','Anexo 4','ANEXO','AMF','ExportaNCG380');

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/

commit;

