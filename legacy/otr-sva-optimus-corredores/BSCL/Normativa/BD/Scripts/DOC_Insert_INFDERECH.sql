set schema "BSCL";


--delete esquemas
--create esquemas
--delete campos
--create campos
--delete tipo documento
--create tipo documento


/*==============================================================*/
/* ESQUEMAS                                                     */
/*==============================================================*/
--INFDERECH
insert into "BSCL"."BSCL.Normativa.BD::DOC.Esquema"
("Esquema","Version","Modulo","Periodicidad","Objetivo","PeriodoInicio","IdEntidadReguladora","FechaPresentacionLimite","FechaDatosDesde","FechaDatosHasta","CodigoVersionSII","HabilitadoMantenedor")
select
    'INFDERECH' as "Esquema",
    '2016' as "Version",
    'NOR' as "Modulo",
    'D' as "Periodicidad",
    'mostrar los cliente con derecho' as "Objetivo",
    '' as "PeriodoInicio",
    '' as "IdEntidadReguladora",
    '2016-12-31' as "FechaPresentacionLimite",
    '2016-01-01' as "FechaDatosDesde",
    '2016-12-31' as "FechaDatosHasta",
	 0 as "CodigoVersionSII",
	'N' as "HabilitadoMantenedor"
from dummy;

/*==============================================================*/
/* CAMPOS                                                       */
/*==============================================================*/




--INFDERECH
/*insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'INFDERECH' as "Esquema", '2016' as "Version", 'TipoRegistro' as "Nombre"			, '' as "CodigoSII", '' as "TipoDiccionario",  1 as "Orden", 'Tipo Registro' as "Titulo"		, 'Ayuda de	Fecha' as "Ayuda"		, 'TEXT' as "TipoDato", 0 as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'INFDERECH' as "Esquema", '2016' as "Version", 'Folio' as "Nombre"					, '' as "CodigoSII", '' as "TipoDiccionario",  2 as "Orden", 'Folio' as "Titulo"				, 'Ayuda de	Hora' as "Ayuda"		, 'TEXT' as "TipoDato", 0 as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'INFDERECH' as "Esquema", '2016' as "Version", 'Mercado' as "Nombre"				, '' as "CodigoSII", '' as "TipoDiccionario",  3 as "Orden", 'Mercado' as "Titulo"				, 'Ayuda de	Mercado' as "Ayuda"		, 'TEXT' as "TipoDato", 0 as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'INFDERECH' as "Esquema", '2016' as "Version", 'CorredorCompra' as "Nombre"		, '' as "CodigoSII", '' as "TipoDiccionario",  4 as "Orden", 'Corredor Compra' as "Titulo"		, 'Ayuda de	Bolsa' as "Ayuda"		, 'TEXT' as "TipoDato", 0 as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'INFDERECH' as "Esquema", '2016' as "Version", 'CorredorVenta' as "Nombre"			, '' as "CodigoSII", '' as "TipoDiccionario",  5 as "Orden", 'Corredor Venta' as "Titulo"		, 'Ayuda de	Folio' as "Ayuda"		, 'TEXT' as "TipoDato", 0 as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'INFDERECH' as "Esquema", '2016' as "Version", 'FechaVencimiento' as "Nombre"		, '' as "CodigoSII", '' as "TipoDiccionario",  6 as "Orden", 'Fecha Vencimiento' as "Titulo"	, 'Ayuda de	Secuencia' as "Ayuda"	, 'TEXT' as "TipoDato", 0 as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'INFDERECH' as "Esquema", '2016' as "Version", 'Nemotecnico' as "Nombre"			, '' as "CodigoSII", '' as "TipoDiccionario",  7 as "Orden", 'Nemotecnico' as "Titulo"			, 'Ayuda de	Interm_inf' as "Ayuda"	, 'TEXT' as "TipoDato", 0 as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'INFDERECH' as "Esquema", '2016' as "Version", 'Precio' as "Nombre"				, '' as "CodigoSII", '' as "TipoDiccionario",  8 as "Orden", 'Precio' as "Titulo"				, 'Ayuda de	Interm_con' as "Ayuda"	, 'TEXT' as "TipoDato", 0 as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'INFDERECH' as "Esquema", '2016' as "Version", 'Tasa' as "Nombre"					, '' as "CodigoSII", '' as "TipoDiccionario",  9 as "Orden", 'Tasa' as "Titulo"					, 'Ayuda de	Num_orden' as "Ayuda"	, 'TEXT' as "TipoDato", 0 as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'INFDERECH' as "Esquema", '2016' as "Version", 'SignoTasa' as "Nombre"				, '' as "CodigoSII", '' as "TipoDiccionario", 10 as "Orden", 'Signo Tasa' as "Titulo"			, 'Ayuda de	Nemo' as "Ayuda"		, 'TEXT' as "TipoDato", 0 as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'INFDERECH' as "Esquema", '2016' as "Version", 'DocumentoIdentidad' as "Nombre"	, '' as "CodigoSII", '' as "TipoDiccionario", 11 as "Orden", 'DocumentoIdentidad' as "Titulo"	, 'Ayuda de	Fecven' as "Ayuda"		, 'TEXT' as "TipoDato", 0 as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'INFDERECH' as "Esquema", '2016' as "Version", 'Cantidad' as "Nombre"				, '' as "CodigoSII", '' as "TipoDiccionario", 12 as "Orden", 'Cantidad' as "Titulo"				, 'Ayuda de	Tipo_ope' as "Ayuda"	, 'TEXT' as "TipoDato", 0 as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'INFDERECH' as "Esquema", '2016' as "Version", 'TipoOperacion' as "Nombre"			, '' as "CodigoSII", '' as "TipoDiccionario", 13 as "Orden", 'TipoOperacion' as "Titulo"		, 'Ayuda de	Plazo' as "Ayuda"		, 'TEXT' as "TipoDato", 0 as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'INFDERECH' as "Esquema", '2016' as "Version", 'TipoReajuste' as "Nombre"			, '' as "CodigoSII", '' as "TipoDiccionario", 14 as "Orden", 'TipoReajuste' as "Titulo"			, 'Ayuda de	Liquidacion' as "Ayuda"	, 'TEXT' as "TipoDato", 0 as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'INFDERECH' as "Esquema", '2016' as "Version", 'Monto' as "Nombre"					, '' as "CodigoSII", '' as "TipoDiccionario", 15 as "Orden", 'Monto' as "Titulo"				, 'Ayuda de	Sistema' as "Ayuda"		, 'TEXT' as "TipoDato", 0 as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" select 'INFDERECH' as "Esquema", '2016' as "Version", 'Emisor' as "Nombre"				, '' as "CodigoSII", '' as "TipoDiccionario", 16 as "Orden", 'Emisor' as "Titulo"				, 'Ayuda de	Tipo_cli' as "Ayuda"	, 'TEXT' as "TipoDato", 0 as "Largo", '0' as "ValorDefecto", 'N' as "Obligatorio",  'S' as "Editable",  'S' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'N' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;
*/
  
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" (
"Esquema"  
,"Version"  
,"Nombre"   
,"CodigoSII"  
,"TipoDiccionario"  
,"Orden"  
,"Titulo"		
,"Ayuda"	
,"TipoDato"  
,"Largo"  
,"ValorDefecto"  
,"Obligatorio"  
,"Editable"  
,"ModoMasivo"  
,"Exportable"  
,"OcultoEnGrilla"  
,"OcultoEnMantenedor"  
,"Comun"  
,"Clave"  
,"Grupo"  
,"OrdenCargaExterna"
,"CantidadDecimales"
)
select 'INFDERECH' as "Esquema", '2016' as "Version", 'RegistroConcatenado' as "Nombre"   , '' as "CodigoSII", '' as "TipoDiccionario", 1 as "Orden", 'RegConcatenado' as "Titulo"		, 'Ayuda de	Reg_Concatenado' as "Ayuda"	, 'TEXT' as "TipoDato", 0 as "Largo", '0' as "ValorDefecto", ' ' as "Obligatorio",  'S' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'S' as "OcultoEnGrilla", 'S' as "OcultoEnMantenedor", 'S' as "Comun", 'N' as "Clave", '' as "Grupo", 0 as "OrdenCargaExterna",0 as "CantidadDecimales" from dummy;


/*==============================================================*/
/* TIPO DOCUMENTO                                               */
/*==============================================================*/
--
                     
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.TipoDocumento"("IdTipoDocumento","Descripcion","IdNormativa","UI_Visor","Esquema","IdTipoProgramaExportacion","IdTipoProgramaProceso")values 
('INFDERECH','Informe Derechos','INFDERECH','N','INFDERECH','INFDERECH','INFDERECH');


/*==============================================================*/
/*                       Homologacion                           */
/*==============================================================*/

INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES(417,'INFDERECH','2016','TipoInstrumento','Acciones Nacionales','ACN','AC','ACT');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES(418,'INFDERECH','2016','TipoInstrumento','ETF nacional','ETN','EN','ACT');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES(419,'INFDERECH','2016','TipoInstrumento','ETX extranjero','ETX','ET','ACT');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES(420,'INFDERECH','2016','TipoInstrumento','Cuotas Fondo Inversion','CFI','CF','ACT');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES(421,'INFDERECH','2016','TipoReajuste','Reajuste Dolares','USD','US','ACT');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES(422,'INFDERECH','2016','TipoReajuste','Reajuste UF','UF','UF','ACT');
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") VALUES(423,'INFDERECH','2016','TipoReajuste','No Reajustable','CLP','NR','ACT');


/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/

commit;

