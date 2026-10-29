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
--         DatosDJ1894
--         DatosDJ1891
--         ...

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/
--delete from "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1894";
delete from "BSCL"."BSCL.Normativa.BD::DOC.Documento" where "Esquema" = 'DJ1894';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Validacion" where "Esquema" = 'DJ1894';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" where "Esquema" = 'DJ1894';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Parametro" where "Esquema" = 'DJ1894';
delete from "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" where "Esquema" = 'DJ1894';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura" where "Esquema" = 'DJ1894';
delete from "BSCL"."BSCL.Normativa.BD::DOC.CampoClasificador" where "Esquema" = 'DJ1894';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Clasificador" where "Esquema" = 'DJ1894';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1894';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Esquema" where "Esquema" = 'DJ1894';

SELECT *   from "BSCL"."BSCL.Normativa.BD::DOC.Esquema" where "Esquema" = 'DJ1894';

UPDATE  "BSCL"."BSCL.Normativa.BD::DOC.Esquema" SET "HabilitadoMantenedor"='S'
 where "Esquema" = 'DJ1894';

/*==============================================================*/
/* ESQUEMAS                                                     */
/*==============================================================*/
insert into "BSCL"."BSCL.Normativa.BD::DOC.Esquema"("Esquema","Version","Modulo","Periodicidad","Objetivo","PeriodoInicio","IdEntidadReguladora","FechaPresentacionLimite","FechaDatosDesde","FechaDatosHasta","CodigoVersionSII")
select
    'DJ1894' as "Esquema",
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
-- DJ1894-2017
--select * from "BSCL"."BSCL.Normativa.BD::DOC.Campo"
--select * from "BSCL"."BSCL.Normativa.BD::DOC.Esquema"
--select * from "BSCL"."BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1894'

insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave", "Grupo","OrdenCargaExterna", "CantidadDecimales")
select 'DJ1894' as "Esquema", '2017' as "Version", 'ID' as "Nombre", ''  as "CodigoSII", '' as "TipoDiccionario",  1 as "Orden", 'Secuencia'  as "Titulo", 'Ayuda de Secuencia' as "Ayuda", 'ID' as "TipoDato",   0 as "Largo", '0'  as "ValorDefecto", 'N' as "Obligatorio",  'N' as "Editable",  'N' as "ModoMasivo", 'S' as "Exportable", 'N' as "OcultoEnGrilla", 'N' as "OcultoEnMantenedor", 'S' as "Comun", 'N' as "Clave", ''as "Grupo", 0 as "OrdenCargaExterna", 0 as "CantidadDecimales" from dummy;
("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave", "Grupo","OrdenCargaExterna", "Cantidad Decimales")

DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Campo"  WHERE "Esquema"='DJ1894'
SELECT * FROM "BSCL"."BSCL.Normativa.BD::DOC.Campo"  WHERE "Esquema"='DJ1894'order by "Orden"
SELECT * FROM "BSCL"."BSCL.Normativa.BD::DOC.Campo"  WHERE "Esquema"='DJ1891'and "Version"=2015 order by "Orden"
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" 
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave", "Grupo","OrdenCargaExterna", "CantidadDecimales")
Select 'DJ1894' as "Esquema",'2017' as "Version",'ID' as "Nombre",'' as "TipoDiccionario",1 as "Orden",'Secuencia' as "Titulo",'ID' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'N' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'S' as "Comun",'N' as "Clave",'5. DATOS DE CONTROL' as "Grupo",0 as "OrdenCargaExterna",0 as "Cantidad Decimales" FROM DUMMY;

UPDATE "BSCL"."BSCL.Normativa.BD::DOC.Campo" SET "Ayuda"="Titulo"
  WHERE "Esquema"='DJ1894'
  
  UPDATE "BSCL"."BSCL.Normativa.BD::DOC.Campo" SET "Titulo"="Nombre"
  WHERE "Esquema"='DJ1894'
SELECT * FROM "BSCL"."BSCL.Normativa.BD::DOC.Campo"  WHERE "Esquema"='DJ1894'order by "Orden"
SELECT * FROM "BSCL"."BSCL.Normativa.BD::DOC.Campo"  WHERE "Esquema"='DJ1894'order by "Orden"
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave", "Grupo","OrdenCargaExterna", "CantidadDecimales")
 Select 'DJ1894' as "Esquema",'2017' as "Version",'ID' as "Nombre",'' as "TipoDiccionario",1 as "Orden",'Secuencia' as "Titulo",'ID' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'N' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'S' as "Comun",'N' as "Clave",'' as "Grupo",0 as "OrdenCargaExterna",0 as "Cantidad Decimales" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave", "Grupo","OrdenCargaExterna", "CantidadDecimales")
 Select 'DJ1894' as "Esquema",'2017' as "Version",'IdDocumento' as "Nombre",'' as "TipoDiccionario",2 as "Orden",'Secuencia Docto.' as "Titulo",'ID' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'N' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'S' as "OcultoEnGrilla",'S' as "OcultoEnMantenedor",'S' as "Comun",'N' as "Clave",'5. DATOS DE CONTROL' as "Grupo",0 as "OrdenCargaExterna",0 as "Cantidad Decimales" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave", "Grupo","OrdenCargaExterna", "CantidadDecimales")
 Select 'DJ1894' as "Esquema",'2017' as "Version",'VboUsuario' as "Nombre",'VistoBueno' as "TipoDiccionario",3 as "Orden",'VoBo' as "Titulo",'TEXT' as "TipoDato",1 as "Largo",'N' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'S' as "Comun",'N' as "Clave",'5. DATOS DE CONTROL' as "Grupo",0 as "OrdenCargaExterna",0 as "Cantidad Decimales" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave", "Grupo","OrdenCargaExterna", "CantidadDecimales")
 Select 'DJ1894' as "Esquema",'2017' as "Version",'Observaciones' as "Nombre",'' as "TipoDiccionario",4 as "Orden",'Observaciones' as "Titulo",'TEXT' as "TipoDato",250 as "Largo",'' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'S' as "Comun",'N' as "Clave",'5. DATOS DE CONTROL' as "Grupo",0 as "OrdenCargaExterna",0 as "Cantidad Decimales" FROM DUMMY;
 
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave", "Grupo","OrdenCargaExterna", "CantidadDecimales")
 Select 'DJ1894' as "Esquema",'2017' as "Version",'FuenteDatos' as "Nombre",'' as "TipoDiccionario",5 as "Orden",'Fuente' as "Titulo",'TEXT' as "TipoDato",10 as "Largo",'INICIAL' as "ValorDefecto",'N' as "Obligatorio",'N' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'S' as "Comun",'N' as "Clave",'5. DATOS DE CONTROL' as "Grupo",0 as "OrdenCargaExterna",0 as "Cantidad Decimales" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave", "Grupo","OrdenCargaExterna", "CantidadDecimales")
 Select 'DJ1894' as "Esquema",'2017' as "Version",'RutCliente' as "Nombre",'' as "TipoDiccionario",6 as "Orden",'RUT DEL PARTICIPE O APORTANTE' as "Titulo",'RUT_CLIENTE' as "TipoDato",40 as "Largo",'' as "ValorDefecto",'S' as "Obligatorio",'N' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'' as "Grupo",1 as "OrdenCargaExterna",0 as "Cantidad Decimales" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave", "Grupo","OrdenCargaExterna", "CantidadDecimales")
 Select 'DJ1894' as "Esquema",'2017' as "Version",'IdCliente' as "Nombre",'' as "TipoDiccionario",7 as "Orden",'Secuencia Cliente' as "Titulo",'ID_CLIENTE' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'S' as "Obligatorio",'N' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS INFORMADOS' as "Grupo",2 as "OrdenCargaExterna",0 as "Cantidad Decimales" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave", "Grupo","OrdenCargaExterna", "CantidadDecimales")
 Select 'DJ1894' as "Esquema",'2017' as "Version",'IdPortafolio' as "Nombre",'' as "TipoDiccionario",8 as "Orden",'Secuencia Portafolio' as "Titulo",'ID_PORTAFOLIO' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'S' as "Obligatorio",'N' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS INFORMADOS' as "Grupo",3 as "OrdenCargaExterna",0 as "Cantidad Decimales" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave", "Grupo","OrdenCargaExterna", "CantidadDecimales")
Select 'DJ1894' as "Esquema",'2017' as "Version",'RutAdministradora' as "Nombre",'' as "TipoDiccionario",9 as "Orden",'RUT DE SOC. ADMINISTRADORA' as "Titulo",'RUT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'S' as "Obligatorio",'N' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS INFORMADOS' as "Grupo",4 as "OrdenCargaExterna",0 as "Cantidad Decimales" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave", "Grupo","OrdenCargaExterna", "CantidadDecimales")
 Select 'DJ1894' as "Esquema",'2017' as "Version",'RunFondo' as "Nombre",'' as "TipoDiccionario",10 as "Orden",'RUN DEL FONDO MUTUO' as "Titulo",'RUT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'S' as "Obligatorio",'N' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS INFORMADOS' as "Grupo",5 as "OrdenCargaExterna",0 as "Cantidad Decimales" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave", "Grupo","OrdenCargaExterna", "CantidadDecimales")
 Select 'DJ1894' as "Esquema",'2017' as "Version",'TipoOperacion' as "Nombre",'TipoOperacionFFMM' as "TipoDiccionario",11 as "Orden",'TIPO DE OPERACIÓN' as "Titulo",'ID' as "TipoDato",0 as "Largo",'1' as "ValorDefecto",'S' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS INFORMADOS' as "Grupo",6 as "OrdenCargaExterna",0 as "Cantidad Decimales" FROM DUMMY;
 
 
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave", "Grupo","OrdenCargaExterna", "CantidadDecimales")
 Select 'DJ1894' as "Esquema",'2017' as "Version",'FechaInversion' as "Nombre",'' as "TipoDiccionario",12 as "Orden",'FECHA INVERSIÓN O REINVERSIÓN' as "Titulo",'DATE_FILTER' as "TipoDato",0 as "Largo",'' as "ValorDefecto",'S' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'2. SECCIÓN B1: INVERSIONES Y REINVERSIONES RECIBIDAS EN EL EJERCICIO' as "Grupo",7 as "OrdenCargaExterna",0 as "Cantidad Decimales" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave", "Grupo","OrdenCargaExterna", "CantidadDecimales")
 Select 'DJ1894' as "Esquema",'2017' as "Version",'MontoNominalInversion' as "Nombre",'' as "TipoDiccionario",13 as "Orden",'MONTO NOMINAL INVERSIÓN O REINVERSIÓN (ACTUALIZADO)' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'2. SECCIÓN B1: INVERSIONES Y REINVERSIONES RECIBIDAS EN EL EJERCICIO' as "Grupo",8 as "OrdenCargaExterna",0 as "Cantidad Decimales" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave", "Grupo","OrdenCargaExterna", "CantidadDecimales")
 Select 'DJ1894' as "Esquema",'2017' as "Version",'FechaRescate' as "Nombre",'' as "TipoDiccionario",14 as "Orden",'FECHA RESCATE' as "Titulo",'DATE_FILTER' as "TipoDato",0 as "Largo",'' as "ValorDefecto",'S' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'3. SECCION B2: RESCATES' as "Grupo",9 as "OrdenCargaExterna",0 as "Cantidad Decimales" FROM DUMMY;
 
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave", "Grupo","OrdenCargaExterna", "CantidadDecimales")
 Select 'DJ1894' as "Esquema",'2017' as "Version",'MontoNominalRescate' as "Nombre",'' as "TipoDiccionario",15 as "Orden",'MONTO NOMINAL RESCATE A LA FECHA EN QUE SE EFECTUÓ' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'3. SECCIÓN B2: RESCATES' as "Grupo",10 as "OrdenCargaExterna",0 as "Cantidad Decimales" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave", "Grupo","OrdenCargaExterna", "CantidadDecimales")
 Select 'DJ1894' as "Esquema",'2017' as "Version",'MayorValorActualizado' as "Nombre",'' as "TipoDiccionario",16 as "Orden",'MAYOR VALOR ACTUALIZADO' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'3. SECCIÓN B2: RESCATES' as "Grupo",11 as "OrdenCargaExterna",0 as "Cantidad Decimales" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave", "Grupo","OrdenCargaExterna", "CantidadDecimales")
 Select 'DJ1894' as "Esquema",'2017' as "Version",'MenorValorActualizado' as "Nombre",'' as "TipoDiccionario",17 as "Orden",'MENOR VALOR ACTUALIZADO' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'3. SECCIÓN B2: RESCATES' as "Grupo",12 as "OrdenCargaExterna",0 as "Cantidad Decimales" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave", "Grupo","OrdenCargaExterna", "CantidadDecimales")
 Select 'DJ1894' as "Esquema",'2017' as "Version",'RutAdminitradoraFondoReceptor' as "Nombre",'' as "TipoDiccionario",18 as "Orden",'RUT SOCIEDAD ADMINISTRADORA FONDO RECEPTOR DE LA REINVERSIÓN' as "Titulo",'RUT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'S' as "Obligatorio",'N' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'4. SECCIÓN B3: LIQUIDACIONES DE CUOTAS PARA REINVERSIONES INCISO QUINTO ART. 108' as "Grupo",13 as "OrdenCargaExterna",0 as "Cantidad Decimales" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave", "Grupo","OrdenCargaExterna", "CantidadDecimales")
 Select 'DJ1894' as "Esquema",'2017' as "Version",'MontoReinvertido' as "Nombre",'' as "TipoDiccionario",19 as "Orden",'MONTO REINVERTIDO A VALOR DE LIQUIDACIÓN' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'4. SECCIÓN B3: LIQUIDACIONES DE CUOTAS PARA REINVERSIONES INCISO QUINTO ART. 108' as "Grupo",14 as "OrdenCargaExterna",0 as "Cantidad Decimales" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave", "Grupo","OrdenCargaExterna", "CantidadDecimales")
 Select 'DJ1894' as "Esquema",'2017' as "Version",'PorcentajePromedioAnualAcciones' as "Nombre",'' as "TipoDiccionario",20 as "Orden",'PORCENTAJE PROMEDIO ANUAL INVERTIDO EN ACCIONES DEL ACTIVO DEL FONDO MUTUO' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS INFORMADOS' as "Grupo",15 as "OrdenCargaExterna",0 as "Cantidad Decimales" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave", "Grupo","OrdenCargaExterna", "CantidadDecimales")
 Select 'DJ1894' as "Esquema",'2017' as "Version",'NumeroCertificado' as "Nombre",'' as "TipoDiccionario",21 as "Orden",'NÚMERO DE CERTIFICADO' as "Titulo",'ID' as "TipoDato",0 as "Largo",'1' as "ValorDefecto",'S' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS INFORMADOS' as "Grupo",6 as "OrdenCargaExterna",0 as "Cantidad Decimales" FROM DUMMY;


/*==============================================================*/
/* HOMOLOGACIONES                                               */
/*==============================================================*/

/*==============================================================*/
/* PARAMETROS                                                   */
/*==============================================================*/


/*==============================================================*/
/* VALIDACIONES                                                 */
/*==============================================================*/

/*==============================================================*/
/* DOCUMENTOS                                                   */
/*==============================================================*/

insert into "BSCL"."BSCL.Normativa.BD::DOC.Documento"
(
   "IdDocumento","IdTipoDocumento",
   "Esquema","Version","IdEmpresa",
   "FechaLimite",
   "IdEstadoDocumento",
   "FechaCreacion",
   "FechaPresentacion",
   "DocumentoIdentidadDeclarante",
   "DocumentoIdentidadRepresentante",
   "DireccionElectronicaDeclaracion",
   "DireccionFisicaDeclaracion",
   "ComunaDeclaracion",
   "TelefonoDeclaracion",
   "FolioDeclaracion",
   "EnProceso"
--   ,   "EnProcesoDesde"
)
select
    3 as "IdDocumento",
    'DECLACORR' as "IdTipoDocumento",
    'DJ1894' as "Esquema",
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
    'N' as "EnProceso"
--    ,    null as "EnProcesoDesde"
from dummy;

/*==============================================================*/
/* DETALLES DJ1894                                              */
/*==============================================================*/
/*
insert into "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1894"
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
