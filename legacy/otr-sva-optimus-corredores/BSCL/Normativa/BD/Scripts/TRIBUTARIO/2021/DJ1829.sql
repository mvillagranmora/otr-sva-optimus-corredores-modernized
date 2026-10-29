


SET SCHEMA "BSCL";
--Limpiar todas las versiones        
DELETE FROM "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1829";
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura"  WHERE "Esquema"='DJ1829';-- AND "Version"='2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"  WHERE "Esquema"='DJ1829';-- AND "Version"='2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1829';-- and "Version" = '2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema"='DJ1829';-- AND "Version"='2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1829';-- AND "Version"='2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Documento" where "Esquema" = 'DJ1829';-- and "Version"='2016/12';
DELETE FROM "BSCL.Normativa.BD::DOC.Parametro" where "Esquema" = 'DJ1829';-- and "Version"='2016/12';

DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1829';-- AND "Version"='2016/12';

DELETE FROM "BSCL"."BSCL.Normativa.BD::TRI.ControlProceso" WHERE  "Esquema" = 'DJ1829';

INSERT INTO "BSCL.Normativa.BD::DOC.Esquema" ("Esquema","Version","Modulo","Periodicidad","Objetivo","PeriodoInicio",
		     "IdEntidadReguladora","FechaPresentacionLimite","FechaDatosDesde","FechaDatosHasta",
		     "CodigoVersionSII","HabilitadoMantenedor")
VALUES
('DJ1829','2020','TRI','A','Declaración Jurada sobre contratos de derivados','2020-01-01','SII','2018-01-01','2020-01-01','2020-12-31',0,'S');


insert into "BSCL.Normativa.BD::DOC.Documento"
("IdDocumento","IdTipoDocumento","Esquema","Version","IdEmpresa","FechaLimite","IdEstadoDocumento",
"FechaCreacion","FechaPresentacion","DocumentoIdentidadDeclarante",
"DocumentoIdentidadRepresentante","DireccionElectronicaDeclaracion",
"DireccionFisicaDeclaracion","ComunaDeclaracion","TelefonoDeclaracion",
"FolioDeclaracion","EnProceso","FechaInicioUltimoProceso","AnoMes")
SELECT "BSCL"."BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL AS "IdDocumento",
'DECLACORR' AS "IdTipoDocumento"
,'DJ1829' AS "Esquema"
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



/*CAMPOS*/

insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'ID' as "Nombre",'' as "TipoDiccionario",1 as "Orden",'Secuencia' as "Titulo",'ID' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'N' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'S' as "Clave",'' as "Grupo",0 as "OrdenCargaExterna",0 as "Cantidad Decimales",'' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'Numero' as "Nombre",'' as "TipoDiccionario",2 as "Orden",'Numero' as "Titulo",'ID' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'N' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'S' as "Clave",'2. DATOS DE CONTROL' as "Grupo",1 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C59' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'RutContraparte' as "Nombre",'' as "TipoDiccionario",3 as "Orden",'Rut Contraparte' as "Titulo",'RUT' as "TipoDato",0 as "Largo",'' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",2 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C8' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'TaxIDContraparte' as "Nombre",'' as "TipoDiccionario",4 as "Orden",'Tax ID de la Contraparte' as "Titulo",'RUT' as "TipoDato",0 as "Largo",'' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",3 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C10' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'CodigoPaisContraparte' as "Nombre",'' as "TipoDiccionario",5 as "Orden",'Código País Contraparte' as "Titulo",'NEMO' as "TipoDato",0 as "Largo",'' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",4 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C11' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'TipoRelacionContraparte' as "Nombre",'' as "TipoDiccionario",6 as "Orden",'Tipo de Relación con Contraparte' as "Titulo",'NEMO' as "TipoDato",0 as "Largo",'' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",5 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C12' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'ModalidadContratacion' as "Nombre",'' as "TipoDiccionario",7 as "Orden",'Modalidad de Contratación' as "Titulo",'NEMO' as "TipoDato",0 as "Largo",'' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",6 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C13' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'AcuerdoMarcoTipo' as "Nombre",'' as "TipoDiccionario",8 as "Orden",'Acuerdo Marco Tipo' as "Titulo",'TEXT' as "TipoDato",0 as "Largo",'' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",7 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C14' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'AcuerdoMarcoNumero' as "Nombre",'' as "TipoDiccionario",9 as "Orden",'Acuerdo Marco Numero' as "Titulo",'DECIMAL' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",8 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C15' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'AcuerdoMarcoFechaSuscripcion' as "Nombre",'' as "TipoDiccionario",10 as "Orden",'Acuerdo Marco Fecha de Suscripción' as "Titulo",'DATE' as "TipoDato",0 as "Largo",'' as "ValorDefecto",'S' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",9 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C16' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'ContratoNumero' as "Nombre",'' as "TipoDiccionario",11 as "Orden",'Contrato / Confirmación Número / Identificador' as "Titulo",'DECIMAL' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",10 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C17' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'ContratoFechaSuscripcion' as "Nombre",'' as "TipoDiccionario",12 as "Orden",'Contrato / Confirmación Fecha de Suscripción' as "Titulo",'DATE' as "TipoDato",0 as "Largo",'' as "ValorDefecto",'S' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",11 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C18' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'ContratoVencido' as "Nombre",'' as "TipoDiccionario",13 as "Orden",'Contrato / Confirmación Contrato vencido en el ejercicio' as "Titulo",'TEXT' as "TipoDato",0 as "Largo",'' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",12 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C19' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'ContratoEstadoConfirmacion' as "Nombre",'' as "TipoDiccionario",14 as "Orden",'Contrato / Confirmación Estado del contrato / confirmación ' as "Titulo",'TEXT' as "TipoDato",0 as "Largo",'' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",13 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C20' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'ContratoTipo' as "Nombre",'' as "TipoDiccionario",15 as "Orden",'Contrato / Confirmación Tipo Contrato' as "Titulo",'TEXT' as "TipoDato",0 as "Largo",'' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",14 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C21' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'ContratoNombreInstrumento' as "Nombre",'' as "TipoDiccionario",16 as "Orden",'Contrato / Confirmación Nombre del Instrumento' as "Titulo",'TEXT' as "TipoDato",0 as "Largo",'' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",15 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C22' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'ContratoModalidadCumplimiento' as "Nombre",'' as "TipoDiccionario",17 as "Orden",'Contrato / Confirmación Modalidad Cumplimiento' as "Titulo",'TEXT' as "TipoDato",0 as "Largo",'' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",16 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C23' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'ContratoPosicionDeclarante' as "Nombre",'' as "TipoDiccionario",18 as "Orden",'Contrato / Confirmación Posición del Declarante' as "Titulo",'TEXT' as "TipoDato",0 as "Largo",'' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",17 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C24' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'ActivoSubyacenteTipo' as "Nombre",'' as "TipoDiccionario",19 as "Orden",'Activo Subyacente Tipo' as "Titulo",'TEXT' as "TipoDato",0 as "Largo",'' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",18 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C25' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'ActivoSubyacenteCodigo' as "Nombre",'' as "TipoDiccionario",20 as "Orden",'Activo Subyacente Codigo' as "Titulo",'TEXT' as "TipoDato",0 as "Largo",'' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",19 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C26' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'ActivoSubyacenteOtros' as "Nombre",'' as "TipoDiccionario",21 as "Orden",'Activo Subyacente Otros' as "Titulo",'TEXT' as "TipoDato",0 as "Largo",'' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",20 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C27' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'ActivoSubyacenteTasaFija' as "Nombre",'' as "TipoDiccionario",22 as "Orden",'Activo Subyacente Tasa fija / Spread' as "Titulo",'QTY' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",21 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C28' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'SegundoActivoSubyacenteTipo' as "Nombre",'' as "TipoDiccionario",23 as "Orden",'Segundo Activo Subyacente Tipo' as "Titulo",'TEXT' as "TipoDato",0 as "Largo",'' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",22 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C29' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'SegundoActivoSubyacenteCodigo' as "Nombre",'' as "TipoDiccionario",24 as "Orden",'Segundo Activo Subyacente Codigo' as "Titulo",'TEXT' as "TipoDato",0 as "Largo",'' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",23 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C30' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'SegundoActivoSubyacenteOtros' as "Nombre",'' as "TipoDiccionario",25 as "Orden",'Segundo Activo Subyacente Otros' as "Titulo",'TEXT' as "TipoDato",0 as "Largo",'' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",24 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C31' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'SegundoActivoSubyacenteTasaFija' as "Nombre",'' as "TipoDiccionario",26 as "Orden",'Segundo Activo Subyacente Tasa fija / Spread' as "Titulo",'QTY' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",25 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C32' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'PrecioFuturoCodigoPrecio' as "Nombre",'' as "TipoDiccionario",27 as "Orden",'Precio futuro contratado Codigo de Precio' as "Titulo",'TEXT' as "TipoDato",0 as "Largo",'' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",26 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C33' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'PrecioFuturoPrecio' as "Nombre",'' as "TipoDiccionario",28 as "Orden",'Precio futuro contratado Precio' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",27 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C34' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'PrecioFuturoMoneda' as "Nombre",'' as "TipoDiccionario",29 as "Orden",'Precio futuro contratado Moneda' as "Titulo",'NEMO' as "TipoDato",0 as "Largo",'' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",28 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C35' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'Unidad' as "Nombre",'' as "TipoDiccionario",30 as "Orden",'Unidad' as "Titulo",'TEXT' as "TipoDato",0 as "Largo",'' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",29 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C36' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'MontoCantidadContratadoNocional' as "Nombre",'' as "TipoDiccionario",31 as "Orden",'Monto/cantidad contratado o nocional' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",30 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C37' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'SegundaUnidad' as "Nombre",'' as "TipoDiccionario",32 as "Orden",'Segunda unidad' as "Titulo",'TEXT' as "TipoDato",0 as "Largo",'' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",31 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C38' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'SegundoMontoNocional' as "Nombre",'' as "TipoDiccionario",33 as "Orden",'Segundo monto nocional ' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",32 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C39' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'FechaVencimiento' as "Nombre",'' as "TipoDiccionario",34 as "Orden",'Fecha de Vencimiento' as "Titulo",'DATE_FILTER' as "TipoDato",0 as "Largo",'' as "ValorDefecto",'S' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",33 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C40' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'FechaLiquidacionEjercicioOpcion' as "Nombre",'' as "TipoDiccionario",35 as "Orden",'Fecha de liquidación o de ejercicio de opción' as "Titulo",'DATE' as "TipoDato",0 as "Largo",'' as "ValorDefecto",'S' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",34 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C41' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'PrecioMercadoCodigoPrecio' as "Nombre",'' as "TipoDiccionario",36 as "Orden",'Precio de mercado del subyacente al cierre del periodo o liquidación Código de precio' as "Titulo",'TEXT' as "TipoDato",0 as "Largo",'' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",35 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C42' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'PrecioMercadoPrecio' as "Nombre",'' as "TipoDiccionario",37 as "Orden",'Precio de mercado del subyacente al cierre del periodo o liquidación Precio' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",36 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C43' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'ValorJustoContratoConfirnacion' as "Nombre",'' as "TipoDiccionario",38 as "Orden",'Valor justo del contrato / confirmación' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",37 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C44' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'ResultadoEjercicio' as "Nombre",'' as "TipoDiccionario",39 as "Orden",'Resultado del ejercicio $' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",38 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C45' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'CuentaContableAsociadaResultadoEjercicio' as "Nombre",'' as "TipoDiccionario",40 as "Orden",'Cuenta contable asociada al resultado del ejercicio' as "Titulo",'TEXT' as "TipoDato",0 as "Largo",'' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",39 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C46' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'EfectoPatrimonio' as "Nombre",'' as "TipoDiccionario",41 as "Orden",'Efecto en Patrimonio $' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",40 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C47' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'CuentaContableAsociadaEfectoPatrimonio' as "Nombre",'' as "TipoDiccionario",42 as "Orden",'Cuenta contable asociada al efecto en Patrimonio' as "Titulo",'TEXT' as "TipoDato",0 as "Largo",'' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",41 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C48' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'ComisionPactada' as "Nombre",'' as "TipoDiccionario",43 as "Orden",'Comisión pactada $' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",42 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C49' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'PrimaTotal' as "Nombre",'' as "TipoDiccionario",44 as "Orden",'Prima total $' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",43 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C50' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'InversionInicial' as "Nombre",'' as "TipoDiccionario",45 as "Orden",'Inversión Inicial $' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",44 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C51' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'OtrosGastosAsociadosContrato' as "Nombre",'' as "TipoDiccionario",46 as "Orden",'Otros gastos asociados al contrato $' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",45 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C52' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'OtrosIngresosAsociadosContrato' as "Nombre",'' as "TipoDiccionario",47 as "Orden",'Otros ingresos asociados al contrato $' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",46 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C53' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'PagosMontoPagadosAdeudados' as "Nombre",'' as "TipoDiccionario",48 as "Orden",'Montos pagados o adeudados $' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",47 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C54' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'PagosExteriorModalidadPago' as "Nombre",'' as "TipoDiccionario",49 as "Orden",'Modalidad de pago' as "Titulo",'TEXT' as "TipoDato",0 as "Largo",'' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",48 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C55' AS "CodigoSII" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo"("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales","CodigoSII" ) Select 'DJ1829' as "Esquema",'2020' as "Version",'SaldoGarantiasCierre' as "Nombre",'' as "TipoDiccionario",50 as "Orden",'Saldo de garantias al cierre (contratos futuros) $' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS CONTRATOS INFORMADOS ' as "Grupo",49 as "OrdenCargaExterna",0 as "Cantidad Decimales",'C56' AS "CodigoSII" FROM DUMMY;


insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales" ) values ('DJ1829','2020','IdDocumento','','',51,'Secuencia Docto','Ayuda de Secuencia Docto','ID','0','0','N','N','N','S','S','S','S','N','2. DATOS DE CONTROL',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales" ) values ('DJ1829','2020','VboUsuario','','',52,'VoBo','Ayuda de VoBo','TEXT','1','N','N','S','S','S','N','N','S','N','2. DATOS DE CONTROL',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales" ) values ('DJ1829','2020','Observaciones','','',53,'Observaciones','Ayuda de Observaciones','TEXT','250','','N','S','S','S','N','N','S','N','2. DATOS DE CONTROL',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales" ) values ('DJ1829','2020','FuenteDatos','','',54,'Fuente','Ayuda de Fuente','TEXT','0','INICIAL','N','N','N','S','N','N','S','N','2. DATOS DE CONTROL',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales" ) values ('DJ1829','2020','IdCliente','','',55,'Secuencia Cliente','Ayuda de Secuencia Cliente','ID_CLIENTE','0','0','S','S','N','S','N','N','N','N','2. DATOS DE CONTROL',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales" ) values ('DJ1829','2020','IdPortafolio','','',56,'Secuencia Portafolio','Ayuda de Secuencia Portafolio','ID_PORTAFOLIO','0','0','S','N','N','S','N','N','N','N','2. DATOS DE CONTROL',0,0);
--insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales" ) values ('DJ1829','2020','TipoDocumento','','TipoDocu',57,'Tipo Documento','Ayuda de Tipo Documento','NEMO','0','','S','S','N','S','N','N','N','S','2. DATOS DE CONTROL',0,'0');
--insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales" ) values ('DJ1829','2020','NumeroDocumento','','',58,'Número Documento','Ayuda de Número Documento','ID','0','','S','S','N','S','N','N','N','S','2. DATOS DE CONTROL',0,'0');
--insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales" ) values ('DJ1829','2020','FechaDocumento','','',59,'Fecha Documento','Ayuda de Fecha Documento','DATE_FILTER','0','','N','S','N','S','N','N','N','N','2. DATOS DE CONTROL',0,'0');
--insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales" ) values ('DJ1829','2020','TipoTransaccion','','TipoTransac',60,'Tipo Transacción','Ayuda de Tipo Transacción','NEMO','0','','S','S','N','S','N','N','N','N','2. DATOS DE CONTROL',0,'0');
--insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales" ) values ('DJ1829','2020','IdTipoOperacion','','TipoOperacion',61,'Tipo Operación','Ayuda de Tipo Operación','NEMO','0','','S','S','N','S','N','N','N','N','2. DATOS DE CONTROL',0,'0');
--insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales" ) values ('DJ1829','2020','IdTipoProducto','','TipoProducto',62,'Tipo Producto','Ayuda de Tipo Producto','NEMO','0','','S','S','N','S','N','N','N','N','2. DATOS DE CONTROL',0,'0');
--insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales" ) values ('DJ1829','2020','IdBaseDocumento','','',63,'Secuencia Base','Ayuda de Secuencia Base','ID','0','0','N','N','N','S','N','N','N','S','2. DATOS DE CONTROL',0,'0');


insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1451' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Los campos (C11), (C12), (C13), (C17), (C19), (C21), (C23), (C25), (C36), (C37), (C40), (C42), (C43) son de llenado obligatorio y con valores validos de acuerdo al  tipo de dato' as "Descripcion",
    'E' as "Severidad",
    'C11 <>(B) C12 <> 0 C13 <> 0 C17 <> (B) C18 <> 0 C19 <> 0 C21 <> 0 C23 <> 0 C25 <> 0 C36 <> 0 C37 <> 0 C40 <> 0 C42 <> 0 C43 <> 0' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1452' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'La columna relación de contraparte (C12) debe indicar valores según tabla “Tipo de Relación con Contraparte (TRC)”' as "Descripcion",
    'E' as "Severidad",
    'C12 => C12= {1 .o. 2 .o. 3 .o. 4.o. 99}' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1453' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'La columna código País contraparte (C11) se debe indicar valores según listado de Códigos País de Residencia para el AT respectivo, según instrucciones del suplemento tributario' as "Descripcion",
    'E' as "Severidad",
    'C11 pertenece listado de Códigos País de Residencia para el A.T' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1454' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si existe valor distinto de cero o vacío en la columna tipo de acuerdo marco (C14) entonces solo deben ser valores según tabla Acuerdo Marco, tipo (AMT)' as "Descripcion",
    'E' as "Severidad",
    'Si C14 <>  vacío o cero  => C14= {1 .o. 2 .o. 3}' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1455' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'La columna contrato vencido en el ejercicio (C19) debe indicar valores según tabla Contrato vencido en el ejercicio (CVE)' as "Descripcion",
    'E' as "Severidad",
    'C19 => C19= {1 .o. 2}' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1456' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si existe valor distinto de cero o vacío en la columna la columna Estado del Contrato/Confirmación (C20) entonces se debe indicar valores según tabla Contrato/Confirmación (ECC)' as "Descripcion",
    'E' as "Severidad",
    'Si C20 <>  vacío o cero => C20= {1 .o. 2 .o. 3 .o. 4}' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1457' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'La columna tipo de contrato (C21) debe indicar valores según tabla tipo de contrato (TC) ' as "Descripcion",
    'E' as "Severidad",
    ' C21 => C21= {1.o. 2.o.3.o.4.o.5.o.6.o.7.o.8.o.9.o.10.o.11.o.12.o.13.o.14}' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1458' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'La columna Modalidad de cumplimiento (C23) debe indicar valores según tabla Modalidad de cumplimiento (MC) ' as "Descripcion",
    'E' as "Severidad",
    ' C23 => C23= {1 .o. 2 }' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1459' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si el tipo de contrato es distinto de  Swap entonces la  columna Posición del declarante (C24) debe indicar valores según tabla Posición del declarante (PD) ' as "Descripcion",
    'E' as "Severidad",
    'Si C21 <> 3.o.4.o.5 => C24= {1 .o. 2 }' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1460' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'La columna tipo de activo subyacente (C25) debe indicar valores según tabla tipo de activo subyacente (AS) ' as "Descripcion",
    'E' as "Severidad",
    'C25 => C25= {1.o.2.o.3.o.4.o.5.o.6.o.7}' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1461' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Al indicar tipo de Activo Subyacente (C25) Moneda {1} Se debe indicar valores en (C26) según Tabla Códigos Monedas AT respectivo. ' as "Descripcion",
    'E' as "Severidad",
    'Si existe C25={1} => C26 pertenece listado según Tabla Códigos Monedas AT' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1462' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si el campo Tipo de activo subyacente (C25) es 2 (Tasa de interés), en el campo Código activo subyacente (C26) debe indicar valores según tabla instruida (1 o 2)' as "Descripcion",
    'E' as "Severidad",
    'Si C25={2} => C26={1.o.2}' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1463' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si el campo Tipo de activo subyacente (C25) es 3 (Producto básico), en el campo Código activo subyacente (C26) debe indicar valores según tabla instruida (1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 .o. 14)' as "Descripcion",
    'E' as "Severidad",
    'Si C25={3} => (C26)= {1 .o. 2 .o. 3 .o. 4 .o. 5 .o. 6 .o. 7 .o. 8 .o. 9 .o. 10 .o. 11 .o. 12 .o. 13 .o. 14}' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1464' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Al indicar código Activo Subyacente (C26) la opción VARIABLE {2}, cuando tipo de Activo Subyacente (C25) es tasa de interés {2} en la columna otros (C27) se debe indicar valores según tabla de tasas variables (TIV).' as "Descripcion",
    'E' as "Severidad",
    'Si C25={2} .y. (C26)={2} => C27= {1.o.2.o.3.o.4.o.5.o.6.o.7.o.8.o.9.o.10.o.11.o.12.o.13.o.14.o.15.o.16.o.17.o.18.o.19.o.20.o.21.o.22.o.23.o.24.o.25.o.26.o.27.o.28.o.29 o.30.o.31.o.32.o.33.o.34.o.35 o.36.o.37.o.38.o.39.o.40}' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1465' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si el campo Tipo de contrato (C21) es Swap (3, 4 o 5), en el campo Segundo activo subyacente (C29), debe indicar valores según tabla activo subyacente (1, 2, 3, 4, 5, 6 o 7)' as "Descripcion",
    'E' as "Severidad",
    'Si C21={3.o.4.o.5} => C29={1 .o. 2 .o. 3 .o. 4 .o. 5 .o. 6.o.7 }' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1466' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si el campo Tipo de segundo activo subyacente (C29) es 1 (Moneda), en el campo Código segundo activo subyacente (C30) debe indicar valores según tabla de códigos de monedas del AT.' as "Descripcion",
    'E' as "Severidad",
    'Si C29={1} => C30pertenece listado según Tabla Códigos Monedas AT' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1467' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si el campo Tipo de segundo activo subyacente (C29) es 2 (Tasa de interés), en el campo Código segundo activo subyacente (C30) debe indicar valores según tabla instruida (1 o 2)' as "Descripcion",
    'E' as "Severidad",
    'Si C29={2} => C30={1.o.2}' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1468' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si el campo Tipo de segundo activo subyacente (C29) es 2 (Tasa de interés) y campo Código activo subyacente (C30) es 2 (variable), en el campo Otros/Segundo activo subyacente (C31) debe indicar valores según tabla de variables (1 al 40)' as "Descripcion",
    'E' as "Severidad",
    'Si C29={2} .y. C30={2}=> C31= {1.o.2.o.3.o.4.o.5.o.6.o.7.o.8.o.9.o.10.o.11.o.12.o.13.o.14.o.15.o.16.o.17.o.18.o.19.o.20.o.21.o.22.o.23.o.24.o.25.o.26.o.27.o.28.o.29.o.30.o.31.o.32.o.33 o.34.o.35.o.36.o.37.o.38.o.39.o.40}' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1469' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si tipo de contrato ( C21 ) es distinto de Swap la columna código de precio (C33) debe indicar valores según tabla Código de precio (CP).' as "Descripcion",
    'E' as "Severidad",
    'Si C21 <> .o. 4 .o. 5 .=> C33= {1 .o. 2 }' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1470' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si existe valor distinto de cero o vacío  en la columna moneda (C35) entonces se debe indicar valores según Tabla Códigos Monedas AT respectivo.' as "Descripcion",
    'E' as "Severidad",
    'si  C35 <> vacío  o 0  => C35  pertenece listado según Tabla Códigos Monedas AT' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1471' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'En la columna unidad (C36) Se debe indicar valores según tabla unidad (UM)' as "Descripcion",
    'E' as "Severidad",
    'Si existe C36 => C36={1.o.2.o.3.o.4.o.5.o.6.o.7.o.8.o.9.o.10.o.11.o.12.o.13}' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1472' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'El campo Código de precio mercado (C42) debe indicar valores según tabla instruida: 1 o 2.' as "Descripcion",
    'E' as "Severidad",
    'Si existe C42 => C42= {1 .o. 2 }' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1473' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    ' Si el campo Tipo de activo subyacente (C25) es 2 y el código del activo subyacente (C26) opción ”FIJA” {1}, la columna Tasa fija / Spread (C28) debe ser distinta de cero (puede ser negativo).' as "Descripcion",
    'E' as "Severidad",
    'Si C25 = 2 .y. C26={1} => C28 <> 0' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1474' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si el campo tipo de segundo activo subyacente (C29) es 2 y el código del segundo activo subyacente (C30) opción ”FIJA”{1}, la columna (C32) debe ser distinto de cero (puede ser negativo)' as "Descripcion",
    'E' as "Severidad",
    'Si C29 = 2 .y. Si C30=1 => C32 <>= 0' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1475' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'La fecha de suscripción del contrato/confirmación (C18) debe ser anterior o igual al 31 de Diciembre del año tributario menos 1. Ejemplo si el AT 2013, (C18) debe ser a lo más 31/12/2012.' as "Descripcion",
    'E' as "Severidad",
    'C18 ≤31/12/AT-1' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1477' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'El campo Modalidad de contratación (C13) debe indicar valores según tabla instruida (1, 2, 3, 4, 5, 6)' as "Descripcion",
    'E' as "Severidad",
    ' C13 => C13={1 .o. 2 .o. 3 .o. 4 .o. 5 .o. 6 }' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1478' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si la columna Montos pagados o adeudados (C54)  es mayor a cero, entonces en la columna modalidad de pago (C55) se debe indicar valores según tabla (MPag)' as "Descripcion",
    'E' as "Severidad",
    'Si C54 > 0  => C55={1 .o. 2 .o. 3 .o. 4 }' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1481' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si no existe Rut de la contraparte (C8) debe existir Tax ID de la contraparte (C10)' as "Descripcion",
    'E' as "Severidad",
    'Si C8=Blanco .o. vacío => C10<> Blanco .o. vacío' as "Formula"
from dummy;

--Se comenta versión 2021
/*insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1482' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si contrato está vencido en el ejercicio informado, el estado del contrato descrito en (C20) debe ser vacío.' as "Descripcion",
    'E' as "Severidad",
    'Si C19= {1}=> C20= Blanco .o. vacío' as "Formula"
from dummy; */

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1483' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si el campo Tipo de contrato (C21) es 3, 4 o 5, el campo Posición del declarante (C24) debe ser cero' as "Descripcion",
    'E' as "Severidad",
    'Si C21={3 .o. 4 .o. 5}=> C24 =0' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1484' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si el campo Tipo de contrato (C21) es distinto de 3, 4 o 5, los campos del Segundo Activo Subyacente: Tipo (C29), Código (C30), Otros (C31), Tasa fija / Spread (C32) deben ser vacías o cero de acuerdo al tipo de dato' as "Descripcion",
    'E' as "Severidad",
    'Si C21<> {3 .o. 4 .o. 5}=> C29=0  .y. C30 = Blanco .o. vacío .y. C31= Blanco .o. vacío .y. C32=0' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1485' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si el campo Tipo de contrato (C21) es 3, 4 o 5, los campos de Precio futuro contratado: Código de Precio (C33), Precio (C34) y Moneda (C35) deben estar en blanco' as "Descripcion",
    'E' as "Severidad",
    'Si C21= {3 .o. 4 .o. 5}=> C33 = 0 y C34 = 0.y. C35= Blanco .o. vacío' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1486' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si el campo Código de precio (C33) es 2 (tasa), los campos de Precio futuro contratado: Precio (C34) y Moneda (C35) deben estar en blanco' as "Descripcion",
    'E' as "Severidad",
    'Si C33= {2 }=> C34=0 .y. C35= Blanco .o. vacío' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1487' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Alguno de los campos RutContraparte (C8) o Tax-ID de la Contraparte (C10) deben tener valor' as "Descripcion",
    'E' as "Severidad",
    'C8 > 0 .o.  C10 > blanco o vacío' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1488' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si tipo de contrato (C21) es igual a opción otros {12,13,14}, debe existir nombre del instrumento (C22)' as "Descripcion",
    'E' as "Severidad",
    'Si C21= {12 .o. 13 .o. 14 }=> C22<> Blanco .o. vacío' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1489' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si tipo de activo subyacente (C25) es igual a opción otros {7}, debe existir detalle de instrumento en otros (especificacion)  (C27) ' as "Descripcion",
    'E' as "Severidad",
    'Si C25= {7 }=> C27<> Blanco .o. vacío' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1491' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Al indicar tipo de contrato (C21) la opción Swap {3,4,5}  la columna Segunda unidad (solo Swap)  (C38)  y en monto segundo nocional swap (C39),  deben ser distinto de cero' as "Descripcion",
    'E' as "Severidad",
    'Si C21= {3.o. 4 .o. 5 }=>  C38<> 0 .y.  C39<> 0' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1492' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si existe código de precio mercado (C42) debe existir precio (C43)' as "Descripcion",
    'E' as "Severidad",
    'Si C42 >0 => C43 >0' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1493' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si existe valor de prima en columna (C50) es porque el tipo de contrato (C21) es ·    Opción Call Americana ·    Opción Put Americana ·    Opción Call Europea ·    Opción Put Europea ·    Opción Call Asiática ·    Opción Put Asiática ·    Otros derivados incluidos en el N° 2 del artículo 2°, de la Ley 20.544, de 2011 ·    Otros derivados incluidos en el N° 3 del artículo 2°, de la Ley 20.544, de 2011 ·    Otros (indicar nombre del instrumento)' as "Descripcion",
    'E' as "Severidad",
    'Si C50 >0 => C21= 6 .o.7 .o.8 .o.9 .o.10 .o.11 .o.12 .o.13 .o.14}' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1494' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si existe unidad (C36) debe existir monto de unidad (C37) ' as "Descripcion",
    'E' as "Severidad",
    'Si C36 >0 => C37 >0' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1495' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si tipo de contrato es swap (3 , 4, 5) y  existe segunda unidad (C38) debe existir segundo monto nocional (C39) ' as "Descripcion",
    'E' as "Severidad",
    'Si C21 = 3.o.4.o.5 y  C38 >0 => C39 >0' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1496' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si existe resultado ejercicio (C45) es distinto de cero,debe existir cuenta contable (C46) ' as "Descripcion",
    'E' as "Severidad",
    'Si {C45 <> 0} => C46 <> Blanco o vacío' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1497' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si efecto en patrimonio es negativo (C47) es distinto de cero, debe existir cuenta contable (C48)' as "Descripcion",
    'E' as "Severidad",
    'Si (C47 <> 0)  => C48 <> Blanco o vacío' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1498' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si existe monto pago al exterior ejercicio (C54) debe existir modalidad de pago (C55) ' as "Descripcion",
    'E' as "Severidad",
    'Si C54 >0 => C55 >0' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1499' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si contrato está vencido en el ejercicio (C19) es 1, la columna  fecha de liquidación (C41) debe ser distinta de cero' as "Descripcion",
    'E' as "Severidad",
    'Si C19= {1 }=> C41<> 0' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1500' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si existe fecha de suscripción del acuerdo marco (C16), debe ser anterior o igual a la fecha del contrato de confirmación. (C18) ' as "Descripcion",
    'E' as "Severidad",
    'Si existe C16 => C16 ≤ C18' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1501' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si la fecha de vencimiento o liquidacion (C40) es distinta de  01011900; debe ser  mayor o igual a la fecha de suscripción del contrato (C18) ' as "Descripcion",
    'E' as "Severidad",
    'Si C40 <> 01011900 => C40 >=C18' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1502' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si existe fecha de liquidación (C41)  y la fecha de vencimiento es distinta de 01011900, entonces la fecha de liquidacion  debe ser menor o igual a la fecha de vencimiento (C40).' as "Descripcion",
    'E' as "Severidad",
    'Si existe C41 y  C40 <> 01011900 => C41 ≤ C40' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1829' as "Esquema",
    '2020' as "Version",
    'H1516' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si el campo Tipo de segundo activo subyacente (C29) es 3 (Producto básico), en el campo Código activo subyacente (C30) debe indicar valores según tabla instruida (1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 .o. 14)' as "Descripcion",
    'E' as "Severidad",
    'Si C29={3} => C30 ={1 .o. 2 .o. 3 .o. 4 .o. 5 .o. 6 .o. 7 .o. 8 .o. 9 .o. 10 .o. 11 .o. 12 .o. 13 .o. 14}' as "Formula"
from dummy;


--CONTROL PROCESO

insert into "BSCL"."BSCL.Normativa.BD::TRI.ControlProceso"
("Esquema","Version","EstadoCertificado","Carga","Validacion","PreDeclaracion","Certificacion")
SELECT "Esquema","Version",'NGE','N','N','N','N' FROM "BSCL"."BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1829' AND "Version" IN ('2020');

