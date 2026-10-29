SET SCHEMA "BSCL";

DROP TABLE BSCL."BSCL.Normativa.BD::TRI.DatosDJ1820";

CREATE TABLE BSCL."BSCL.Normativa.BD::TRI.DatosDJ1820" (
	ID BIGINT NOT NULL,
	"IdDocumento" BIGINT NOT NULL,
	"VboUsuario" CHAR(1) NOT NULL,
	"Observaciones" NVARCHAR(250) NOT NULL,
	"FuenteDatos" NVARCHAR(40) NOT NULL,
	"IdCliente" BIGINT DEFAULT 0,
	"IdPortafolio" BIGINT DEFAULT 0,
	"IdCartera" BIGINT DEFAULT 0,
	"FolioOperacion" BIGINT DEFAULT 0,
	"FechaOperacion" DATE DEFAULT '0001-01-01' NOT NULL,
	"FechaLiquidacion" DATE DEFAULT '0001-01-01' NOT NULL,
	"FechaAnticipo" DATE DEFAULT '0001-01-01' NOT NULL,
	"IdInstrumento" BIGINT DEFAULT 0,
	"IdTipoOperacion" NVARCHAR(40) DEFAULT '' NOT NULL,
	"IdClasificacionTipoInstrumento" NVARCHAR(3) DEFAULT '' NOT NULL,
	"IdMonedaPrincipal" NVARCHAR(3) DEFAULT '' NOT NULL,
	"IdMonedaSecundaria" NVARCHAR(3) DEFAULT '' NOT NULL,
	"MontoMonedaPrincipal" DECIMAL(26,8) DEFAULT 0 NOT NULL,
	"MontoMonedaSecundaria" DECIMAL(26,8) DEFAULT 0 NOT NULL,
	"RutCliente" NVARCHAR(40) NOT NULL,
	"TaxID" NVARCHAR(40) DEFAULT '' NOT NULL, 
	"CodigoPaisResidencia" NVARCHAR(10) DEFAULT ''NOT NULL,
	"CodigoRelacion" NVARCHAR(10) DEFAULT ''NOT NULL,
	"CodigoModoContrato" NVARCHAR(40) DEFAULT ''NOT NULL,
	"TipoAcuerdoMarco" NVARCHAR(40) DEFAULT ''NOT NULL,
	"NumeroAcuerdoMarco" BIGINT DEFAULT 0 NOT NULL,
	"FechaSuscripcionAcuerdo" DATE DEFAULT '0001-01-01' NOT NULL,
	"NumeroContrato" BIGINT DEFAULT 0 NOT NULL,
	"FechaSuscripcionContrato" DATE DEFAULT '0001-01-01'NOT NULL,
	"TipoEvento" NVARCHAR(40) DEFAULT '' NOT NULL,
	"TipoContrato" NVARCHAR(40) DEFAULT ''NOT NULL,
	"Instrumento" NVARCHAR(40) DEFAULT ''NOT NULL,
	"ModalidadCumplimiento" NVARCHAR(40) DEFAULT ''NOT NULL,
	"CodigoPosicionDeclarante" NVARCHAR(40) DEFAULT ''NOT NULL,
	"TipoActivoSubyacente" NVARCHAR(40) DEFAULT ''NOT NULL,
	"CodigoActivoSubyacente" NVARCHAR(40) DEFAULT '' NOT NULL,
	"OtroActivoSubyacente" NVARCHAR(40) DEFAULT ''NOT NULL,
	"TasaActivoSubyacente" DECIMAL(26,8) DEFAULT 0 NOT NULL,
	"TipoSegundoActivoSubyacente" NVARCHAR(40) DEFAULT '' NOT NULL,
	"CodigoSegundoActivoSubyacente" NVARCHAR(40) DEFAULT ''NOT NULL,
	"OtroSegundoActivoSubyacente" NVARCHAR(40) DEFAULT '' NOT NULL,
	"TasaSegundoActivoSubyacente" DECIMAL(26,8) DEFAULT 0 NOT NULL,
	"CodigoPrecioFuturo" NVARCHAR(40) DEFAULT 0 NOT NULL,
	"PrecioFuturo" DECIMAL(27,8) DEFAULT 0 NOT NULL,
	"CodigoMonedaFuturo" NVARCHAR(40) DEFAULT '' NOT NULL,
	"CodigoUnidad" NVARCHAR(40) DEFAULT '' NOT NULL,
	"MontoNocional" DECIMAL(27,8) DEFAULT 0 NOT NULL,
	"CodigoSegundaUnidad" NVARCHAR(40) DEFAULT '' NOT NULL,
	"SegundoMontoNocional" NVARCHAR(50) DEFAULT '0' NOT NULL,
	"FechaVencimiento" DATE DEFAULT '0001-01-01' NOT NULL,
	PRIMARY KEY (ID),
	FOREIGN KEY ("IdDocumento") REFERENCES BSCL."BSCL.Normativa.BD::DOC.Documento"("IdDocumento") ON DELETE RESTRICT ON UPDATE RESTRICT
);
        
--Limpiar todas las versiones        
DELETE FROM "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1820";
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura"  WHERE "Esquema"='DJ1820';-- AND "Version"='2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"  WHERE "Esquema"='DJ1820';-- AND "Version"='2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1820';-- and "Version" = '2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema"='DJ1820';-- AND "Version"='2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1820';-- AND "Version"='2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Documento" where "Esquema" = 'DJ1820';-- and "Version"='2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1820';-- AND "Version"='2016/12';

DELETE FROM "BSCL"."BSCL.Normativa.BD::TRI.ControlProceso" WHERE  "Esquema" = 'DJ1820';

/*CONFIGURACIONES */

/*ESQUEMA*/
DELETE FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1820' AND "Version"='2016/12';

insert into "BSCL.Normativa.BD::DOC.Esquema" ("Esquema","Version","Modulo","Periodicidad","Objetivo","PeriodoInicio",
		     "IdEntidadReguladora","FechaPresentacionLimite","FechaDatosDesde","FechaDatosHasta",
		     "CodigoVersionSII","HabilitadoMantenedor")
values
('DJ1820','2016/12','TRI','M','','2016-01-30','SII','2016-02-05','2016-12-01','2016-12-31',24,'S');


/*DOCUMENTO*/


DELETE FROM "BSCL.Normativa.BD::DOC.Documento" where "Esquema" = 'DJ1820' and "Version"='2016/12';

insert into "BSCL.Normativa.BD::DOC.Documento"
("IdDocumento","IdTipoDocumento","Esquema","Version","IdEmpresa","FechaLimite","IdEstadoDocumento",
"FechaCreacion","FechaPresentacion","DocumentoIdentidadDeclarante",
"DocumentoIdentidadRepresentante","DireccionElectronicaDeclaracion",
"DireccionFisicaDeclaracion","ComunaDeclaracion","TelefonoDeclaracion",
"FolioDeclaracion","EnProceso","FechaInicioUltimoProceso","AnoMes")
SELECT "BSCL"."BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL AS "IdDocumento",
'DECLACORR' AS "IdTipoDocumento"
,'DJ1820' AS "Esquema"
,'2016/12' AS "Version"
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
--select * from "BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1820' and "Version" = '2016/12'-- and "Comun"='S'
--order by "Orden","Grupo" asc

--select * from "BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1885' and "Version" = '2020'-- and "Comun"='S'
--order by "Orden","Grupo" asc


DELETE FROM "BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1820' and "Version" = '2016/12';
---Comunes
insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','ID','','', '1','Secuencia','Ayuda  de Secuencia','ID',0, 0
,'N','N','N','S', 'N','N','S','N','', 0,0);
insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
 values ('DJ1820','2016/12','IdDocumento','','','2','Secuencia Docto','Ayuda de Secuencia Docto','ID','0','0'
 ,'N','N','N','S','S','S','S','N','8. Control',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales") 
values ('DJ1820','2016/12','VboUsuario','','VistoBueno','3','VoBo','Ayuda de VoBo','TEXT','1','N'
,'N','S','S','S','N','N','S','N','8. Control',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','Observaciones','','','4','Observaciones','Ayuda de Observaciones','TEXT','250',''
,'N','S','S','S','N','N','S','N','8. Control',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
 values ('DJ1820','2016/12','FuenteDatos','','','5','Fuente','Ayuda de Fuente','TEXT','0','INICIAL'
 ,'N','N','N','S','N','N','S','N','8. Control',0,0);

 

---Datos extra
insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales") 
values ('DJ1820','2016/12','IdCliente','','','6','Cliente','Ayuda de Secuencia Cliente','ID_CLIENTE','0'
--,'0','S','S','N','S','N','N','N','N','',0,0);
  ,'0','S','S','N','S','S','N','N','N','',0,0);

insert into "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','IdPortafolio','','','7','Portafolio','Ayuda de Secuencia Portafolio','ID_PORTAFOLIO','0'
--,'0','S','S','N','S','N','N','N','N','',0,0);
,'0','S','S','N','S','S','N','N','N','',0,0);

---Datos de las Operaciones

insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','RutCliente','C8','','8','Rut Cliente','Ayuda de Rut Cliente','RUT_CLIENTE',0,''
,'N','N','S','S','N','N','N','N','1. Datos de los contratos informados',4,0);

insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','TaxID','C10','','9','Tax ID Inversionista','Ayuda de Tax ID','TEXT',40,
'','N','S','N','S','N','N','N','N','1. Datos de los contratos informados',3,0);

insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','CodigoPaisResidencia','C11','DJ1820PAIS', '10','Codigo Pais Residencia','Ayuda de Codigo Pais Residencia','NEMO',0,
'S','N','S','N','S','N','N','N','N','1. Datos de los contratos informados',6,0);

insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','CodigoRelacion','C12','DJ1820RELACION', '11','Codigo RelaciÃ³n','Ayuda de Codigo RelaciÃ³n','NEMO',0,
'','N','S','N','S','N','N','N','N','1. Datos de los contratos informados',6,0);

insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','CodigoModoContrato','C13','DJ1820MODALIDADCONTRATO', '12','Codigo Modalidad Contrato','Ayuda de Modalidad Contrato','NEMO',0,
'','N','S','N','S','N','N','N','N','1. Datos de los contratos informados',6,0);

---Acuerdo Marco 		

insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','TipoAcuerdoMarco','C14','DJ1820ACUERDOMARCO', '13','Tipo','Ayuda de Tipo Acuerdo Marco','NEMO',0,
'','S','S','N','S','N','N','N','N','2. Acuerdo Marco',6,0);

insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','NumeroAcuerdoMarco','C15','', '14','Numero','Ayuda de Numero Acuerdo Marco','ID',0,
'','S','S','N','S','N','N','N','N','2. Acuerdo Marco',6,0);

insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','FechaSuscripcionAcuerdo','C16','', '15','Fecha SuscripciÃ³n','Ayuda de Fecha SuscripciÃ³n','DATE',0,
'','S','S','N','S','N','N','N','N','2. Acuerdo Marco',10,0);

---Contrato / ConfirmaciÃ³n
insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','NumeroContrato','C17','', '16','Numero Contrato','Ayuda de Numero Contrato','ID',0,
'','S','S','N','S','N','N','N','N','3. Contrato / ConfirmaciÃ³n',6,0);

insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','FechaSuscripcionContrato','C18','', '17','Fecha SuscripciÃ³n Contrato','Ayuda de Fecha SuscripciÃ³n','DATE',0,
'','S','S','N','S','N','N','N','N','3. Contrato / ConfirmaciÃ³n',10,0);

insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','TipoEvento','C19','DJ1820TIPOEVENTO', '18','Tipo Evento','Ayuda de Tipo Evento','NEMO',0,
'','S','S','N','S','N','N','N','N','3. Contrato / ConfirmaciÃ³n',6,0);

insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','TipoContrato','C21','DJ1820TIPOCONTRATO', '19','Tipo Contrato','Ayuda de Tipo Contrato','NEMO',0,
'','S','S','N','S','N','N','N','N','3. Contrato / ConfirmaciÃ³n',6,0);

insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','Instrumento','C22','', '20','Instrumento','Ayuda de Instrumento','NEMO',0,
'','N','S','N','S','N','N','N','N','3. Contrato / ConfirmaciÃ³n',6,0);

insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','ModalidadCumplimiento','C23','DJ1820MODALIDADCUMPLIMIENTO', '21','Modalidad Cumplimiento','Ayuda de Modalidad Cumplimiento','NEMO',0,
'','S','S','N','S','N','N','N','N','3. Contrato / ConfirmaciÃ³n',6,0);

insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','CodigoPosicionDeclarante','C24','DJ1820POSICIONDECLARANTE', '22','Codigo Posicion Declarante','Ayuda de Codigo Posicion Declarante','NEMO',0,
'','N','S','N','S','N','N','N','N','3. Contrato / ConfirmaciÃ³n',6,0);

---Activo Subyacente
insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','TipoActivoSubyacente','C25','DJ1820TIPOACTIVOSUBYACENTE', '23','Tipo Activo Subyacente','Ayuda de Tipo Activo Subyacente','NEMO',0,
'','N','S','N','S','N','N','N','N','4. Activo Subyacente',6,0);

insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','CodigoActivoSubyacente','C26','', '24','Codigo Activo Subyacente','Ayuda de Codigo Activo Subyacente','NEMO',0,
'','N','S','N','S','N','N','N','N','4. Activo Subyacente',6,0);

insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','OtroActivoSubyacente','C27','', '25','Otro Activo Subyacente','Ayuda de Otro Activo Subyacente','NEMO',0,
'','N','S','N','S','N','N','N','N','4. Activo Subyacente',6,0);

insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','TasaActivoSubyacente','C28','', '26','Tasa Activo Subyacente','Ayuda de Tasa Activo Subyacente','DECIMAL',0,
'','N','S','N','S','N','N','N','N','4. Activo Subyacente',6,4);

---Segundo Activo Subyacente (SÃ³lo Swap)

insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','TipoSegundoActivoSubyacente','C29','', '27','Tipo Segundo Activo Subyacente','Ayuda de Tipo Segundo Activo Subyacente','NEMO',0,
'','N','S','N','S','N','N','N','N','5. Segundo Activo Subyacente (SÃ³lo Swap)',6,0);

insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','CodigoSegundoActivoSubyacente','C30','', '28','Codigo Segundo Activo Subyacente','Ayuda de Codigo Segundo Activo Subyacente','NEMO',0,
'','N','S','N','S','N','N','N','N','5. Segundo Activo Subyacente (SÃ³lo Swap)',6,0);

insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','OtroSegundoActivoSubyacente','C31','', '29','Otro Segundo Activo Subyacente','Ayuda de Otro Segundo Activo Subyacente','NEMO',0,
'','N','S','N','S','N','N','N','N','5. Segundo Activo Subyacente (SÃ³lo Swap)',6,0);

insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','TasaSegundoActivoSubyacente','C32','', '30','Tasa Segundo Activo Subyacente','Ayuda de Tasa Segundo Activo Subyacente','DECIMAL',0,
'','N','S','N','S','N','N','N','N','5. Segundo Activo Subyacente (SÃ³lo Swap)',6,4);

---Precio futuro contratado (futuros, forwards y opciones)

insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','CodigoPrecioFuturo','C33','DJ1820CODIGOPRECIOFUTURO', '31','Codigo Precio Futuro','Ayuda de Codigo Precio Futuro','NEMO',0,
'','N','S','N','S','N','N','N','N','6. Precio futuro contratado (futuros, forwards y opciones)',6,0);

insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','PrecioFuturo','C34','', '32','Precio Futuro','Ayuda de Precio Futuro','AMOUNT',0,
'','N','S','N','S','N','N','N','N','6. Precio futuro contratado (futuros, forwards y opciones)',6,0);

insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','CodigoMonedaFuturo','C35','', '33','Codigo Moneda Futuro','Ayuda de Codigo Moneda Futuro','NEMO',0,
'','N','S','N','S','N','N','N','N','6. Precio futuro contratado (futuros, forwards y opciones)',6,0);

---Datos de los contratos informados
insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','CodigoUnidad','C36','DJ1820UNIDADMONEDA', '34','Codigo Unidad','Ayuda de Codigo Unidad','NEMO',0,
'','N','S','N','S','N','N','N','N','7. Datos de los contratos informados',6,0);

insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','MontoNocional','C37','', '35','Monto Nocional','Ayuda de Monto Nocional','AMOUNT',0,
'','N','S','N','S','N','N','N','N','7. Datos de los contratos informados',6,0);

insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','CodigoSegundaUnidad','C38','DJ1820UNIDADMONEDA', '36','Codigo Segunda Unidad','Ayuda de Codigo Segunda Unidad','NEMO',0,
'','N','S','N','S','N','N','N','N','7. Datos de los contratos informados',6,0);

insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','SegundoMontoNocional','C39','', '37','Segundo Monto Nocional','Ayuda de Segundo Monto Nocional','TEXT',40,
'','N','S','N','S','N','N','N','N','7. Datos de los contratos informados',6,0);

insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','FechaVencimiento','C40','', '38','Fecha Vencimiento','Ayuda de Fecha Vencimiento','DATE',0,
'','N','S','N','S','N','N','N','N','7. Datos de los contratos informados',10,0);


insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','FechaOperacion','','', '39','Fecha Operacion','Ayuda de Fecha Operacion','DATE_FILTER',0,
'','S','S','N','S','S','N','N','N','8. Control',10,0);

insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','IdCartera','','', '40','IdCartera','','ID',0,
'','N','S','N','N','S','N','N','S','8. Control',6,0);

insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
values ('DJ1820','2016/12','FolioOperacion','','', '41','FolioOperacion','','ID',0,
'','N','S','N','N','S','N','N','S','8. Control',6,0);



/*HOMOLOGACIONES*/

--SELECT * FROM "BSCL.Normativa.BD::DOC.Homologacion" order by 1; 

SET SCHEMA "BSCL";
--SELECT MAX("IdHomologacion") FROM "BSCL.Normativa.BD::DOC.Homologacion"; 
DELETE FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema"='DJ1820' AND "Version"='2016/12';


---CODIGO PRECIO FUTURO 
--DELETE FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema"='DJ1820' AND "Tipo"='CODIGOPRECIOFUTURO';

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','CODIGOPRECIOFUTURO','Codigo precio BCU','FBC','2','ACT');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','CODIGOPRECIOFUTURO','Codigo precio','FDO','1','ACT');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','CODIGOPRECIOFUTURO','Codigo precio','FUF','1','ACT');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','CODIGOPRECIOFUTURO','Codigo precio IPSA','FIP','1','ACT');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','CODIGOPRECIOFUTURO','Codigo precioICP','FIP','1','ACT');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','CODIGOPRECIOFUTURO','Codigo precio Bovespa','FBO','1','ACT');


---CODIGO MONEDA FUTURO 
--DELETE FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema"='DJ1820' AND "Tipo"='CODIGOMONEDAFUTURO';

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','CODIGOMONEDAFUTURO','Codigo moneda BCU','FBC','','ACT');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','CODIGOMONEDAFUTURO','Codigo moneda','FDO','CLP','ACT');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','CODIGOMONEDAFUTURO','Codigo moneda','FUF','CLP','ACT');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','CODIGOMONEDAFUTURO','Codigo moneda IPSA','FIP','CLP','ACT');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','CODIGOMONEDAFUTURO','Codigo moneda ICP','FIP','CLP','ACT');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','CODIGOMONEDAFUTURO','Codigo moneda Bovespa','FBO','CLP','ACT');



---TIPO ACTIVO SUBYACENTE
--DELETE FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema"='DJ1820' AND "Tipo"='TIPOACTIVOSUBYACENTE';

--SELECT * FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema"='DJ1820' AND "Tipo"='TIPOACTIVOSUBYACENTE' AND "ValorOrigen" ='FBO'

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','TIPOACTIVOSUBYACENTE','Activo tipo subyacente Futuro Dolar','FDO','1','ACT');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','TIPOACTIVOSUBYACENTE','Activo tipo subyacente Futuro BCU','FBC','2','ACT');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','TIPOACTIVOSUBYACENTE','Activo tipo subyacente Futuro UF','FUF','4','ACT');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','TIPOACTIVOSUBYACENTE','Activo tipo subyacente Futuro IPSA','FIP','7','ACT');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','TIPOACTIVOSUBYACENTE','Activo tipo subyacente Futuro ICP','FIP','7','ACT');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','TIPOACTIVOSUBYACENTE','Activo tipo subyacente Futuro Bovespa','FBO','7','ACT');

---TIPO CONTRATO
--DELETE FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema"='DJ1820' AND "Tipo"='TIPOCONTRATO';

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','TIPOCONTRATO','Tipo Contrato Opciones CALL AMERICANA','CALLA','6','ACT');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','TIPOCONTRATO','Tipo Contrato Opciones CALL EUROPEA','CALLE','7','ACT');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','TIPOCONTRATO','Tipo Contrato Opciones PUT AMERICANA','CALLE','8','ACT');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','TIPOCONTRATO','Tipo Contrato Opciones PUT EUROPEA','CALLE','9','ACT');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','TIPOCONTRATO','Tipo Contrato Forward','MEFW','1','ACT');


---RELACION
--DELETE FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema"='DJ1820' AND "Tipo"='RELACION';
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (455,'DJ1820','2016/12','RELACION','RELACION DE PROPIEDAD','PROPIEDAD','1','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (456,'DJ1820','2016/12','RELACION','PARTICIPACION EN LA DIRECCION O ADMINISTRACION','PARTICIPACION','2','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (457,'DJ1820','2016/12','RELACION','OTROS TIPOS DE RELACION','OTRO','3','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (458,'DJ1820','2016/12','RELACION','CONTRAPARTE CONSTITUIDA EN UN PAIS CONSIDERADO PARAISO TRIBUTARIO','PARAISO','4','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (459,'DJ1820','2016/12','RELACION','NO HAY RELACION','NOEXISTE','99','ACT');

---TIPO EVENTO

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (465,'DJ1820','2016/12','TIPOEVENTO','ANTICIPO','ANT','4','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (466,'DJ1820','2016/12','TIPOEVENTO','LIQUIDADO','LIQ','4','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (467,'DJ1820','2016/12','TIPOEVENTO','VENCIDO','VCD','5','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (468,'DJ1820','2016/12','TIPOEVENTO','VIGENTE','VIG','1','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (469,'DJ1820','2016/12','TIPOEVENTO','PENDIENTE','PEN','1','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (470,'DJ1820','2016/12','TIPOEVENTO','MODIFICADO','MOD','2','ACT');

---MODALIDAD CUMPLIMIENTO
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (549,'DJ1820','2016/12','MODALIDADCUMPLIMIENTO','ENTREGA FISICA','ENTREGA_FISICA','2','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (550,'DJ1820','2016/12','MODALIDADCUMPLIMIENTO','COMPENSACION','COMPENSADA','1','ACT');


---POSICION DECLARANTE
--SELECT * FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema"='DJ1820' AND "Tipo"='POSICIONDECLARANTE' AND "Esquema"='2016/12'

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (551,'DJ1820','2016/12','POSICIONDECLARANTE','VENTA','COMFWD','1','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (552,'DJ1820','2016/12','POSICIONDECLARANTE','COMPRA','VENFWD','2','ACT');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','POSICIONDECLARANTE','VENTA','VENOAC','2','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','POSICIONDECLARANTE','COMPRA','COMOAC','1','ACT');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','POSICIONDECLARANTE','VENTA','VENIOA','2','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','POSICIONDECLARANTE','COMPRA','COMIOA','1','ACT');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','POSICIONDECLARANTE','VENTA','VENIOA','2','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','POSICIONDECLARANTE','COMPRA','COMIOA','1','ACT');



INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','POSICIONDECLARANTE','VENTA','VENFDO','2','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','POSICIONDECLARANTE','COMPRA','COMFDO','1','ACT');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','POSICIONDECLARANTE','VENTA','VENFBO','2','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','POSICIONDECLARANTE','COMPRA','COMFBO','1','ACT');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','POSICIONDECLARANTE','VENTA','VENIOF','2','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','POSICIONDECLARANTE','COMPRA','COMIOF','1','ACT');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','POSICIONDECLARANTE','VENTA','VENFUF','2','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','POSICIONDECLARANTE','COMPRA','COMFUF','1','ACT');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','POSICIONDECLARANTE','VENTA','VENFIC','2','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','POSICIONDECLARANTE','COMPRA','COMFIC','1','ACT');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','POSICIONDECLARANTE','VENTA','VENFBC','2','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','POSICIONDECLARANTE','COMPRA','COMFBC','1','ACT');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','POSICIONDECLARANTE','VENTA','VENFIP','2','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL ,'DJ1820','2016/12','POSICIONDECLARANTE','COMPRA','COMFIP','1','ACT');








---MONEDA
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (553,'DJ1820','2016/12','MONEDA','REAL BRASILERO','BRL','BRL','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (554,'DJ1820','2016/12','MONEDA','DOLAR CANADIENSE','CAD','CAD','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (555,'DJ1820','2016/12','MONEDA','DOLAR','USD','USD','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (556,'DJ1820','2016/12','MONEDA','SOL PERUANO','PEN','PEN','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (557,'DJ1820','2016/12','MONEDA','DOLAR AUSTRALIA','AUD','AUD','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (558,'DJ1820','2016/12','MONEDA','RENMINBI CHINA','RMB','CNY','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (559,'DJ1820','2016/12','MONEDA','CORONA DANESA','DKK','DKK','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (560,'DJ1820','2016/12','MONEDA','YEN JAPONES','JPY','JPY','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (561,'DJ1820','2016/12','MONEDA','FRANCO SUIZO','CHF','CHF','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (562,'DJ1820','2016/12','MONEDA','CORONA NORUEGA','NOK','NOK','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (563,'DJ1820','2016/12','MONEDA','DOLAR NUEVA ZELANDA','NZD','NZD','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (564,'DJ1820','2016/12','MONEDA','LIBRA ESTERLINA','GBP','GBP','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (565,'DJ1820','2016/12','MONEDA','CORONA SUECA','SEK','SEK','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (566,'DJ1820','2016/12','MONEDA','DOLAR HONG KONG','HKD','HKD','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (567,'DJ1820','2016/12','MONEDA','PESO MEXICANO','MXN','MXP','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (568,'DJ1820','2016/12','MONEDA','EURO','EUR','EUR','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (569,'DJ1820','2016/12','MONEDA','PESO COLOMBIANO','COP','COP','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (570,'DJ1820','2016/12','MONEDA','PESO CUBANO','CUP','CUP','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (571,'DJ1820','2016/12','MONEDA','UNIDAD DE FOMENTO','UF','CLF','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (572,'DJ1820','2016/12','MONEDA','PESO CHILENO','CLP','CLP','ACT');


--UNIDAD MONEDA
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (573,'DJ1820','2016/12','UNIDADMONEDA','PESO CHILENO','CLP','1','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (574,'DJ1820','2016/12','UNIDADMONEDA','US DOLAR ','USD','2','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (575,'DJ1820','2016/12','UNIDADMONEDA','UNIDAD DE FOMENTO','UF','3','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (576,'DJ1820','2016/12','UNIDADMONEDA','LIBRA ESTERLINA','GBP','4','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (577,'DJ1820','2016/12','UNIDADMONEDA','EURO','EUR','5','ACT');


--PAIS
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	478	,'DJ1820','2016/12','PAISCONTRA',	'AFGANISTAN'	,	'AF'	,	'AF'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	479	,'DJ1820','2016/12','PAISCONTRA',	'ALBANIA'	,	'AL'	,	'AL'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	480	,'DJ1820','2016/12','PAISCONTRA',	'AUSTRIA'	,	'AT'	,	'AT'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	481	,'DJ1820','2016/12','PAISCONTRA',	'AUSTRALIA'	,	'AU'	,	'AU'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	482	,'DJ1820','2016/12','PAISCONTRA',	'BELGICA'	,	'BE'	,	'BE'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	483	,'DJ1820','2016/12','PAISCONTRA',	'BERMUDA'	,	'BM'	,	'BM'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	484	,'DJ1820','2016/12','PAISCONTRA',	'BOLIVIA'	,	'BO'	,	'BO'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	485	,'DJ1820','2016/12','PAISCONTRA',	'BRASIL'	,	'BR'	,	'BR'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	486	,'DJ1820','2016/12','PAISCONTRA',	'BAHAMAS'	,	'BS'	,	'BS'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	487	,'DJ1820','2016/12','PAISCONTRA',	'CANADA'	,	'CA'	,	'CA'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	488	,'DJ1820','2016/12','PAISCONTRA',	'CHILE'	,	'CL'	,	'CL'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	489	,'DJ1820','2016/12','PAISCONTRA',	'CHINA'	,	'CN'	,	'CN'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	490	,'DJ1820','2016/12','PAISCONTRA',	'COLOMBIA'	,	'CO'	,	'CO'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	491	,'DJ1820','2016/12','PAISCONTRA',	'COSTA RICA'	,	'CR'	,	'CR'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	492	,'DJ1820','2016/12','PAISCONTRA',	'CUBA'	,	'CU'	,	'CU'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	493	,'DJ1820','2016/12','PAISCONTRA',	'SUIZA'	,	'CH'	,	'CH'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	494	,'DJ1820','2016/12','PAISCONTRA',	'ALEMANIA'	,	'DE'	,	'DE'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	495	,'DJ1820','2016/12','PAISCONTRA',	'DINAMARCA'	,	'DK'	,	'DK'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	496	,'DJ1820','2016/12','PAISCONTRA',	'DOMINICA'	,	'DM'	,	'DM'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	497	,'DJ1820','2016/12','PAISCONTRA',	'REPUBLICA DOMINICANA'	,	'DO'	,	'DO'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	498	,'DJ1820','2016/12','PAISCONTRA',	'ECUADOR'	,	'EC'	,	'EC'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	499	,'DJ1820','2016/12','PAISCONTRA',	'ESPAÃ‘A'	,	'ES'	,	'ES'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	500	,'DJ1820','2016/12','PAISCONTRA',	'FINLANDIA'	,	'FI'	,	'FI'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	501	,'DJ1820','2016/12','PAISCONTRA',	'FRANCIA'	,	'FR'	,	'FR'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	502	,'DJ1820','2016/12','PAISCONTRA',	'INGLATERRA'	,	'IG'	,	'IG'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	503	,'DJ1820','2016/12','PAISCONTRA',	'GRECIA'	,	'GR'	,	'GR'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	504	,'DJ1820','2016/12','PAISCONTRA',	'HONG KONG RAE R.P. CHINA'	,	'HK'	,	'HK'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	505	,'DJ1820','2016/12','PAISCONTRA',	'INDONESIA'	,	'ID'	,	'ID'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	506	,'DJ1820','2016/12','PAISCONTRA',	'IRLANDA'	,	'IE'	,	'IE'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	507	,'DJ1820','2016/12','PAISCONTRA',	'INDIA'	,	'IN'	,	'IN'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	508	,'DJ1820','2016/12','PAISCONTRA',	'ISLAS CAIMAN'	,	'KY'	,	'KY'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	509	,'DJ1820','2016/12','PAISCONTRA',	'ITALIA'	,	'IT'	,	'IT'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	510	,'DJ1820','2016/12','PAISCONTRA',	'JAPON'	,	'JP'	,	'JP'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	511	,'DJ1820','2016/12','PAISCONTRA',	'COREA DEL SUR'	,	'KR'	,	'KR'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	512	,'DJ1820','2016/12','PAISCONTRA',	'LUXEMBURGO'	,	'LU'	,	'LU'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	513	,'DJ1820','2016/12','PAISCONTRA',	'HOLANDA'	,	'NL'	,	'NL'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	514	,'DJ1820','2016/12','PAISCONTRA',	'MALTA'	,	'MT'	,	'MT'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	515	,'DJ1820','2016/12','PAISCONTRA',	'MEXICO'	,	'MX'	,	'MX'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	516	,'DJ1820','2016/12','PAISCONTRA',	'MALASIA'	,	'MY'	,	'MY'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	517	,'DJ1820','2016/12','PAISCONTRA',	'NIGER'	,	'NE'	,	'NE'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	518	,'DJ1820','2016/12','PAISCONTRA',	'NIGERIA'	,	'NG'	,	'NG'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	519	,'DJ1820','2016/12','PAISCONTRA',	'NICARAGUA'	,	'NI'	,	'NI'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	520	,'DJ1820','2016/12','PAISCONTRA',	'HOLANDA'	,	'NL'	,	'NL'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	521	,'DJ1820','2016/12','PAISCONTRA',	'NORUEGA'	,	'NO'	,	'NO'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	522	,'DJ1820','2016/12','PAISCONTRA',	'Nueva Zelanda'	,	'NZ'	,	'NZ'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	523	,'DJ1820','2016/12','PAISCONTRA',	'PANAMA'	,	'PA'	,	'PA'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	524	,'DJ1820','2016/12','PAISCONTRA',	'PERU'	,	'PE'	,	'PE'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	525	,'DJ1820','2016/12','PAISCONTRA',	'FILIPINAS'	,	'PH'	,	'PH'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	526	,'DJ1820','2016/12','PAISCONTRA',	'PAKISTAN'	,	'PK'	,	'PK'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	527	,'DJ1820','2016/12','PAISCONTRA',	'POLONIA'	,	'PL'	,	'PL'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	528	,'DJ1820','2016/12','PAISCONTRA',	'PUERTO RICO'	,	'PR'	,	'PR'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	529	,'DJ1820','2016/12','PAISCONTRA',	'PORTUGAL'	,	'PT'	,	'PT'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	530	,'DJ1820','2016/12','PAISCONTRA',	'PARAGUAY'	,	'PY'	,	'PY'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	531	,'DJ1820','2016/12','PAISCONTRA',	'RUMANIA'	,	'RO'	,	'RO'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	532	,'DJ1820','2016/12','PAISCONTRA',	'Arabia Saudita'	,	'SA'	,	'SA'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	533	,'DJ1820','2016/12','PAISCONTRA',	'SUECIA'	,	'SE'	,	'SE'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	534	,'DJ1820','2016/12','PAISCONTRA',	'SINGAPUR'	,	'SG'	,	'SG'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	535	,'DJ1820','2016/12','PAISCONTRA',	'TURQUIA'	,	'TR'	,	'TR'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	536	,'DJ1820','2016/12','PAISCONTRA',	'TRINIDAD Y TOBAGO'	,	'TT'	,	'TT'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	537	,'DJ1820','2016/12','PAISCONTRA',	'TAIWAN'	,	'TW'	,	'TW'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	538	,'DJ1820','2016/12','PAISCONTRA',	'TANZANIA'	,	'TZ'	,	'TZ'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	539	,'DJ1820','2016/12','PAISCONTRA',	'UCRANIA'	,	'UA'	,	'UA'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	540	,'DJ1820','2016/12','PAISCONTRA',	'UGANDA'	,	'UG'	,	'UG'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	541	,'DJ1820','2016/12','PAISCONTRA',	'ESTADOS UNIDOS DE AMERICA'	,	'US'	,	'US'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	542	,'DJ1820','2016/12','PAISCONTRA',	'URUGUAY'	,	'UY'	,	'UY'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	543	,'DJ1820','2016/12','PAISCONTRA',	'VENEZUELA'	,	'VE'	,	'VE'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	544	,'DJ1820','2016/12','PAISCONTRA',	'VIETNAM'	,	'VN'	,	'VN'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	545	,'DJ1820','2016/12','PAISCONTRA',	'SUDAFRICA'	,	'ZA'	,	'ZA'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	546	,'DJ1820','2016/12','PAISCONTRA',	'CROACIA'	,	'HR'	,	'HR'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	547	,'DJ1820','2016/12','PAISCONTRA',	'REPUBLICA CHECA'	,	'CZ'	,	'CZ'	,'ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado") VALUES ( 	548	,'DJ1820','2016/12','PAISCONTRA',	'REPUBLICA ESLOVACA'	,	'SK'	,	'SK'	,'ACT');



/*DICCIONARIO*/

SET SCHEMA "BSCL"; 
SELECT * FROM "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" WHERE "Tipo"='DJ1820RELACION';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" WHERE "Tipo"='DJ1820RELACION';

---RELACION
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820RELACION',    '1',           1, 'RELACION DE PROPIEDAD', 'S', '', '' from dummy;	
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820RELACION',    '2',           2, 'PARTICIPACION EN LA DIRECCION O ADMINISTRACION', 'S', '', '' from dummy;	
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820RELACION',    '3',           3, 'OTROS TIPOS DE RELACION', 'S', '', '' from dummy;	
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820RELACION',    '4',           4, 'CONTRAPARTE CONSTITUIDA EN UN PAIS CONSIDERADO PARAISO TRIBUTARIO', 'S', '', '' from dummy;	
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820RELACION',    '99',           5, 'NO HAY RELACION', 'S', '', '' from dummy;	

---TIPO EVENTO
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" WHERE "Tipo"='DJ1820TIPOEVENTO';
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820TIPOEVENTO',    '1',           1, 'SUSCRITO', 'S', '', '' from dummy;	
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820TIPOEVENTO',    '2',           2, 'MODIFICADO', 'S', '', '' from dummy;	
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820TIPOEVENTO',    '3',           3, 'CEDIDO', 'S', '', '' from dummy;	
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820TIPOEVENTO',    '4',           4, 'LIQUIDADO', 'S', '', '' from dummy;	
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820TIPOEVENTO',    '5',           5, 'VENCIDO', 'S', '', '' from dummy;	

--MODALIDAD CONTRATO
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" WHERE "Tipo"='DJ1820MODALIDADCONTRATO';
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820MODALIDADCONTRATO',    '1',           1, 'En bolsa de valores nacional', 'S', '', '' from dummy;	
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820MODALIDADCONTRATO',    '2',           2, 'En bolsa extranjera', 'S', '', '' from dummy;	
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820MODALIDADCONTRATO',    '3',           3, 'Con intervencion de agentes o corredores', 'S', '', '' from dummy;	
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820MODALIDADCONTRATO',    '4',           4, 'Fuera de bolsas de valores,en acuerdos marco elab. por asociaciones extranjeras o internacionales', 'S', '', '' from dummy;	
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820MODALIDADCONTRATO',    '5',           5, 'Fuera de bolsas de valores, mediante confir. referenciada a los modelos senalados en el codigo 4', 'S', '', '' from dummy;	
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820MODALIDADCONTRATO',    '6',           6, 'Otra modalidad de contratacion', 'S', '', '' from dummy;	

--ACUERDO MARCO
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" WHERE "Tipo"='DJ1820ACUERDOMARCO';
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820ACUERDOMARCO',    '1',           1, 'Derivados en el Mercado Local', 'S', '', '' from dummy;	
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820ACUERDOMARCO',    '2',           2, 'International Swap and Derivatives Association (ISDA)', 'S', '', '' from dummy;	
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820ACUERDOMARCO',    '3',           3, 'Otros contratos marco', 'S', '', '' from dummy;	


--TIPO CONTRATO
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" WHERE "Tipo"='DJ1820TIPOCONTRATO';
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820TIPOCONTRATO',    '1',           1, 'Forward', 'S', '', '' from dummy;	
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820TIPOCONTRATO',    '2',           2, 'Futuro', 'S', '', '' from dummy;	
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820TIPOCONTRATO',    '3',           3, 'Swap', 'S', '', '' from dummy;	
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820TIPOCONTRATO',    '4',           4, 'Cross Currency Swap', 'S', '', '' from dummy;	
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820TIPOCONTRATO',    '5',           5, 'Credit Default Swap', 'S', '', '' from dummy;	


--MODALIDAD CUMPLIMIENTO
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" WHERE "Tipo"='DJ1820MODALIDADCUMPLIMIENTO';
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820MODALIDADCUMPLIMIENTO',    '1',           1, 'COMPENSACION', 'S', '', '' from dummy;	
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820MODALIDADCUMPLIMIENTO',    '2',           2, 'ENTREGA FISICA', 'S', '', '' from dummy;	

--POSICION DECLARANTE
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" WHERE "Tipo"='DJ1820POSICIONDECLARANTE';
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820POSICIONDECLARANTE',    '1',           1, 'VENTA', 'S', '', '' from dummy;	
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820POSICIONDECLARANTE',    '2',           2, 'COMPRA', 'S', '', '' from dummy;	


--Tipo Activo Subyacente
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" WHERE "Tipo"='DJ1820TIPOACTIVOSUBYACENTE';
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820TIPOACTIVOSUBYACENTE',    '1',           1, 'MONEDA', 'S', '', '' from dummy;	
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820TIPOACTIVOSUBYACENTE',    '2',           2, 'TASA DE INTERES', 'S', '', '' from dummy;	
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820TIPOACTIVOSUBYACENTE',    '3',           3, 'PRODUCTO BASICO', 'S', '', '' from dummy;	
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820TIPOACTIVOSUBYACENTE',    '4',           4, 'UNIDAD DE FOMENTO', 'S', '', '' from dummy;	
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820TIPOACTIVOSUBYACENTE',    '5',           5, 'ACCIONES', 'S', '', '' from dummy;	
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820TIPOACTIVOSUBYACENTE',    '6',           6, 'INDICE BURSATIL', 'S', '', '' from dummy;	
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820TIPOACTIVOSUBYACENTE',    '7',           7, 'OTROS', 'S', '', '' from dummy;	


--CODIGO PRECIO FUTURO
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" WHERE "Tipo"='DJ1820CODIGOPRECIOFUTURO';
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820CODIGOPRECIOFUTURO',    '1',           1, 'VALOR MONETARIO', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820CODIGOPRECIOFUTURO',    '2',           2, 'TASA (%)', 'S', '', '' from dummy;		


--UNIDAD MONEDA
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" WHERE "Tipo"='DJ1820UNIDADMONEDA';
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820UNIDADMONEDA',    '1',           1, 'PESOS CHILENOS', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820UNIDADMONEDA',    '2',           2, 'US DOLARES', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820UNIDADMONEDA',    '3',           3, 'UNIDAD DE FOMENTO', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820UNIDADMONEDA',    '4',           4, 'LIBRAS ESTERLINAS', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820UNIDADMONEDA',    '5',           5, 'EUROS', 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") 
select 'DJ1820UNIDADMONEDA',    '13',           13, 'OTROS', 'S', '', '' from dummy;

----PAIS
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Diccionario" WHERE "Tipo"='DJ1820PAIS';
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'AF'	,	1	,	'AFGANISTAN'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'AL'	,	2	,	'ALBANIA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'AT'	,	3	,	'AUSTRIA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'AU'	,	4	,	'AUSTRALIA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'BE'	,	5	,	'BELGICA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'BM'	,	6	,	'BERMUDA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'BO'	,	7	,	'BOLIVIA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'BR'	,	8	,	'BRASIL'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'BS'	,	9	,	'BAHAMAS'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'CA'	,	10	,	'CANADA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'CL'	,	11	,	'CHILE'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'CN'	,	12	,	'CHINA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'CO'	,	13	,	'COLOMBIA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'CR'	,	14	,	'COSTA RICA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'CU'	,	15	,	'CUBA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'CH'	,	16	,	'SUIZA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'DE'	,	17	,	'ALEMANIA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'DK'	,	18	,	'DINAMARCA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'DM'	,	19	,	'DOMINICA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'DO'	,	20	,	'REPUBLICA DOMINICANA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'EC'	,	21	,	'ECUADOR'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'ES'	,	22	,	'ESPAÃ‘A'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'FI'	,	23	,	'FINLANDIA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'FR'	,	24	,	'FRANCIA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'IG'	,	25	,	'INGLATERRA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'GR'	,	26	,	'GRECIA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'HK'	,	27	,	'HONG KONG RAE R.P. CHINA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'ID'	,	28	,	'INDONESIA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'IE'	,	29	,	'IRLANDA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'IN'	,	30	,	'INDIA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'KY'	,	31	,	'ISLAS CAIMAN'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'IT'	,	32	,	'ITALIA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'JP'	,	33	,	'JAPON'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'KR'	,	34	,	'COREA DEL SUR'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'LU'	,	35	,	'LUXEMBURGO'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'NL'	,	36	,	'HOLANDA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'MT'	,	37	,	'MALTA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'MX'	,	38	,	'MEXICO'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'MY'	,	39	,	'MALASIA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'NE'	,	40	,	'NIGER'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'NG'	,	41	,	'NIGERIA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'NI'	,	42	,	'NICARAGUA'	, 'S', '', '' from dummy;
--insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'NL'	,	43	,	'HOLANDA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'NO'	,	44	,	'NORUEGA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'NZ'	,	45	,	'Nueva Zelanda'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'PA'	,	46	,	'PANAMA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'PE'	,	47	,	'PERU'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'PH'	,	48	,	'FILIPINAS'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'PK'	,	49	,	'PAKISTAN'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'PL'	,	50	,	'POLONIA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'PR'	,	51	,	'PUERTO RICO'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'PT'	,	52	,	'PORTUGAL'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'PY'	,	53	,	'PARAGUAY'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'RO'	,	54	,	'RUMANIA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'SA'	,	55	,	'Arabia Saudita'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'SE'	,	56	,	'SUECIA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'SG'	,	57	,	'SINGAPUR'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'TR'	,	58	,	'TURQUIA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'TT'	,	59	,	'TRINIDAD Y TOBAGO'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'TW'	,	60	,	'TAIWAN'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'TZ'	,	61	,	'TANZANIA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'UA'	,	62	,	'UCRANIA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'UG'	,	63	,	'UGANDA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'US'	,	64	,	'ESTADOS UNIDOS DE AMERICA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'UY'	,	65	,	'URUGUAY'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'VE'	,	66	,	'VENEZUELA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'VN'	,	67	,	'VIETNAM'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'ZA'	,	68	,	'SUDAFRICA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'HR'	,	69	,	'CROACIA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'CZ'	,	70	,	'REPUBLICA CHECA'	, 'S', '', '' from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Diccionario"("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto") select 'DJ1820PAIS', 	'SK'	,	71	,	'REPUBLICA ESLOVACA'	, 'S', '', '' from dummy;


--select length('Los campos CÃ³digo paÃ­s contraparte (C11), Tipo de RelaciÃ³n con contraparte (C12), Modalidad de contrataciÃ³n (C13), Contrato / ConfirmaciÃ³n: NÃºmero / Identificador (C17), Contrato / ConfirmaciÃ³n: fecha suscripciÃ³n (C18), Contrato / ConfirmaciÃ³n: Evento Informado (C19); Contrato / ConfirmaciÃ³n: Tipo de Contrato (C21); Contrato / ConfirmaciÃ³n: Modalidad de cumplimiento (C23); Activo Subyacente: Tipo (C25); Unidad (C36), Monto/cantidad contratado o nocional (C37), fecha de vencimiento (C40),  son de llenado obligatorio y con valores validos de acuerdo al  tipo de dato') from dummy;

/*VALIDACIONES*/
SET SCHEMA "BSCL"; 
--SELECT * FROM "BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1820';
DELETE FROM "BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1820' AND "Version"='2016/12';

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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1411' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Los campos  (C11), (C12), (C13), (C17), (C18), (C19), (C21), (C23), (C25), (C36),(C37),  (C40),  son obligatorios y segÃºn tipo de dato' as "Descripcion",
    'E' as "Severidad",
    'C11<> (Blanco .y. vacÃ­o) .y. C12<> 0 .y. C13 <> 0  .y. C17 <> (Blanco .y. vacÃ­o) .y. C18<> 0 .y. C19<> 0  .y. C21<> 0 .y.  C23 <> 0  .y. C25 <> 0 .y. C36 <> 0 .y. C37 <> 0 .y. C40 <> 0' as "Formula"
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1412' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'La columna relaciÃ³n de contraparte (C12) debe indicar valores segÃºn tabla â€œTipo de RelaciÃ³n con Contraparte (TRC)â€�' as "Descripcion",
    'E' as "Severidad",
    'C12= {1 .o. 2 .o. 3 .o. 4.o. 99}' as "Formula"
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1413' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'La columna cÃ³digo PaÃ­s contraparte (C11) se debe indicar valores segÃºn listado de CÃ³digos PaÃ­s de Residencia para el AT respectivo, segÃºn instrucciones del suplemento tributario' as "Descripcion",
    'E' as "Severidad",
    'C11 pertenece listado de CÃ³digos PaÃ­s de Residencia para el A.T' as "Formula"
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1414' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si existe valor distinto de cero o vacÃ­o en la columna tipo de acuerdo marco (C14) entonces solo deben ser valores segÃºn tabla Acuerdo Marco, tipo (AMT)' as "Descripcion",
    'E' as "Severidad",
    'Si C14 <> vacÃ­o o cero  => C14= {1 .o. 2 .o. 3}' as "Formula"
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1415' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'La columna Evento informado (C19) debe indicar valores segÃºn tabla Evento informado (EI)' as "Descripcion",
    'E' as "Severidad",
    'C19= {1.o.2.o.3.o.4.o.5}' as "Formula"
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1416' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'La columna tipo de contrato (C21) debe indicar valores segÃºn tabla tipo de contrato (TC)' as "Descripcion",
    'E' as "Severidad",
    'C21 => C21= {1 .o. 2 .o. 3 .o. 4 .o. 5 .o. 6 .o. 7 .o. 8.o. 9 .o.10.o.11.o.12.o.13.o.14}' as "Formula"
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1417' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'La columna Modalidad de cumplimiento (C23) debe indicar valores segÃºn tabla Modalidad de cumplimiento (MC)' as "Descripcion",
    'E' as "Severidad",
    'C23 => C23= {1 .o. 2 }' as "Formula"
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1418' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si el tipo de contrato es distinto de  Swap entonces la  columna PosiciÃ³n del declarante (C24) debe indicar valores segÃºn tabla PosiciÃ³n del declarante (PD)' as "Descripcion",
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1419' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'La columna tipo de activo subyacente (C25) debe indicar valores segÃºn tabla tipo de activo subyacente (AS)' as "Descripcion",
    'E' as "Severidad",
    'C25= {1 .o. 2 .o. 3 .o. 4 .o. 5 .o. 6 .o.7}' as "Formula"
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1420' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Al indicar tipo de Activo Subyacente (C25) Moneda {1} Se debe indicar valores en (C26) segÃºn Tabla CÃ³digos Monedas AT respectivo.' as "Descripcion",
    'E' as "Severidad",
    'Si existe C25={1} => C26 pertenece listado segÃºn Tabla CÃ³digos Monedas AT' as "Formula"
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1421' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si el campo Tipo de activo subyacente (C25) es 2 (Tasa de interÃ©s), en el campo CÃ³digo activo subyacente (C26) debe indicar valores segÃºn tabla instruida (1 o 2)' as "Descripcion",
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1422' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Al indicar tipo de Activo Subyacente (C25) Producto BÃ¡sico {3} debe indicar valores en (C26) segÃºn tabla producto bÃ¡sico (PB)' as "Descripcion",
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1423' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Al indicar cÃ³digo Activo Subyacente (C26) la opciÃ³n VARIABLE {2}, cuando tipo de Activo Subyacente (C25) es tasa de interÃ©s {2} en la columna otros (C27) se debe indicar valores segÃºn tabla de tasas variables (TIV). ' as "Descripcion",
    'E' as "Severidad",
    'Si C25={2} .y. (C26)={2} => C27= {1 .o. 2 .o. 3 .o. 4 .o. 5 .o. 6 .o. 7 .o. 8 .o. 9 .o. 10 .o. 11 .o. 12 .o. 13 .o. 14 .o. 15 .o. 16 .o. 17 .o. 18 .o. 19 .o. 20 .o. 21 .o. 22 .o. 23 .o. 24 .o. 25 ... 39 .o. 40} ' as "Formula"
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1424' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si el campo Tipo de contrato (C21) es Swap (3, 4 o 5), en el campo Segundo activo subyacente (C29), debe indicar valores segÃºn tabla activo subyacente (1, 2, 3, 4, 5, 6 o 7)' as "Descripcion",
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1425' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Al indicar tipo de segundo activo Subyacente (C29) Moneda {1} Se debe indicar en (C30) valores segÃºn Tabla CÃ³digos Monedas AT respectivo.' as "Descripcion",
    'E' as "Severidad",
    'Si C29={1} => C30 pertenece listado segÃºn Tabla CÃ³digos Monedas AT' as "Formula"
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1426' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Al indicar tipo de segundo activo Subyacente (C29) Tasa de interÃ©s {2} Se debe indicar valores segÃºn tabla general instruida para cÃ³digos del TI respectivo' as "Descripcion",
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1427' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si el campo Tipo de segundo activo subyacente (C29) es 3 (Producto bÃ¡sico), en el campo CÃ³digo activo subyacente (C30) debe indicar valores segÃºn tabla instruida (1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 .o. 14' as "Descripcion",
    'E' as "Severidad",
    'Si C29={3} => C30 ={1 .o. 2 .o. 3 .o. 4 .o. 5 .o. 6 .o. 7 .o. 8 .o. 9 .o. 10 .o. 11 .o. 12 .o. 13 .o. 14}' as "Formula"
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1428' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si el campo Tipo de segundo activo subyacente (C29) es 2 (Tasa de interÃ©s) y campo CÃ³digo activo subyacente (C30) es 2 (variable), en el campo Otros/Segundo activo subyacente (C31) debe indicar valores segÃºn tabla de variables (1 al 40)' as "Descripcion",
    'E' as "Severidad",
    'Si C29={2} .y. C30={2}=> C31= {1 .o. 2 .o. 3 .o. 4 .o. 5 .o. 6 .o. 7 .o. 8 .o. 9 .o. 10 .o. 11 .o. 12 .o. 13 .o. 14 .o. 15 .o. 16 .o. 17 .o. 18 .o. 19 .o. 20 .o. 21 .o. 22 .o. 23 .o. 24 .o. 25 .o. 26 .o. 27 .o. 28 .o. 29 .o. 30 ... .o. 40}' as "Formula"
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1429' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si tipo de contrato ( C21 ) es distinto de Swap la columna cÃ³digo de precio (C33) debe indicar valores segÃºn tabla CÃ³digo de precio (CP).' as "Descripcion",
    'E' as "Severidad",
    'Si C21 <> 3.o. 4 .o. 5 .=> C33= {1 .o. 2 }' as "Formula"
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1430' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si existe valor distinto de cero o vacÃ­o  en la columna moneda (C35) entonces se debe indicar valores segÃºn Tabla CÃ³digos Monedas AT respectivo.' as "Descripcion",
    'E' as "Severidad",
    'si  C35 <> vacÃ­o  o 0  => C35  pertenece listado segÃºn Tabla CÃ³digos Monedas AT' as "Formula"
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1431' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'En la columna unidad (C36) Se debe indicar valores segÃºn tabla unidad (UM)' as "Descripcion",
    'E' as "Severidad",
    'C36 => C36={1 .o. 2 .o. 3 .o. 4 .o. 5 .o. 6 .o. 7 .o. 8 .o. 9 .o. 10 .o. 11 .o. 12.o. 13}' as "Formula"
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1432' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si el campo tipo de activo subyacente (C25) es 2 y el cÃ³digo del activo subyacente (C26) opciÃ³n â€�FIJAâ€� {1}, la columna (C28) debe ser distinto de cero ( puede ser con signo negativo)' as "Descripcion",
    'E' as "Severidad",
    'Si C25 =2 .y. Si C26={1} => C28 <>= 0' as "Formula"
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1433' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si el campo tipo de activo subyacente (C29) es 2 y el cÃ³digo del segundo activo subyacente (C30) opciÃ³n â€�FIJAâ€�{1}, la columna (C32) debe ser distinta de cero (puede ser negativo).' as "Descripcion",
    'E' as "Severidad",
    'Si C29 = 2 .y. C30={1} => C32 <>= 0' as "Formula"
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1434' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'La fecha de suscripciÃ³n del contrato/confirmaciÃ³n (C18) debe ser anterior o igual al 31 de Diciembre del aÃ±o tributario.' as "Descripcion",
    'E' as "Severidad",
    'C18 â‰¤31/12/AT' as "Formula"
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1436' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'El campo Modalidad de contrataciÃ³n (C13) debe indicar valores segÃºn tabla instruida (1, 2, 3, 4, 5, 6)' as "Descripcion",
    'E' as "Severidad",
    'C13={1 .o. 2 .o. 3 .o. 4 .o. 5 .o. 6 }' as "Formula"
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1437' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si no existe Rut de la contraparte (C8) debe existir Tax ID de la contraparte (C10)' as "Descripcion",
    'E' as "Severidad",
    'Si C8=Blanco .o. vacÃ­o => C10<> Blanco .o. vacÃ­o' as "Formula"
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1438' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si tipo de contrato (C21) es {3,4,5}, posiciÃ³n de declarante (C24) debe ser cero' as "Descripcion",
    'E' as "Severidad",
    'Si C21={3 .o. 4 .o. 5}=> C24=0' as "Formula"
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1439' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si tipo de contrato (C21) es distinto de swap {3,4,5}, las columnas del segundo activo subyacente (C29), (C30), (C31), (C32) son vacÃ­as o cero de acuerdo al tipo de dato' as "Descripcion",
    'E' as "Severidad",
    'Si C21<> {3 .o. 4 .o. 5}=> C29=0  .y. C30 = Blanco .o. vacÃ­o .y. C31= Blanco .o. vacÃ­o .y. C32=0' as "Formula"
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1440' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si tipo de contrato (C21) es swap {3,4,5}, las columnas asociadas a precios de futuros contratados (C33), (C34),(C35) son vacÃ­as.' as "Descripcion",
    'E' as "Severidad",
    'Si C21= {3 .o. 4 .o. 5}=> C33= 0 .y. C34 = 0 .y. C35= Blanco .o. vacÃ­o' as "Formula"
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1441' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si cÃ³digo de precio (C33) es 2, no debe existir moneda (C35) ni precio (C34)  es decir C34 debe ser cero y C35 debe ser vacÃ­o' as "Descripcion",
    'E' as "Severidad",
    'Si C33= {2 }=> C34=0 .y. C35= Blanco .o. vacÃ­o' as "Formula"
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1442' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Debe existir al menos un valor mayor que cero en las columnas (C8) y (C10)' as "Descripcion",
    'E' as "Severidad",
    'C8 > 0 .o.  C10 > blanco o vacÃ­o' as "Formula"
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1443' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si tipo de contrato (C21) es igual a opciÃ³n otros {12,13,14}, debe existir nombre del instrumento (C22)' as "Descripcion",
    'E' as "Severidad",
    'Si C21= {12 .o. 13 .o. 14 } => C22<> Blanco .o. vacÃ­o' as "Formula"
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1444' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si tipo de activo subyacente (C25) es igual a opciÃ³n otros {7}, debe existir nombre del instrumento (C27)' as "Descripcion",
    'E' as "Severidad",
    'Si C25= {7 }=> C27<> Blanco .o. vacÃ­o' as "Formula"
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1446' as "Codigo",
    'NORMA' as "Tipo",
    'N' as "Activa",--'S' as "Activa",CGO
    'Al indicar tipo de contrato (C21) la opciÃ³n Swap {3,4,5}  la columna Segunda unidad (solo Swap)  (C38)  y en monto segundo nocional swap (C39),  deben ser distinto de cero' as "Descripcion",
    'E' as "Severidad",
    'Si C21= {3.o. 4 .o. 5 }=>   C38<> 0 .y.  C39<> 0' as "Formula"
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1447' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si existe unidad (C36) debe existir monto de unidad (C37)' as "Descripcion",
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1448' as "Codigo",
    'NORMA' as "Tipo",
    'N' as "Activa",--'S' as "Activa",CGO
    'Si tipo de contrato es swap (3 , 4, 5) y  existe segunda unidad (C38) debe existir segundo monto nocional (C39)' as "Descripcion",
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1449' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si existe fecha de suscripciÃ³n del acuerdo marco (C16), debe ser anterior o igual a la fecha del contrato de confirmaciÃ³n. (C18)' as "Descripcion",
    'E' as "Severidad",
    'Si existe C16 => C16 â‰¤C18' as "Formula"
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
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'H1450' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si la fecha de vencimiento o liquidacion (C40) es distinta de  01011900; debe ser  mayor o igual a la fecha de suscripciÃ³n del contrato (C18)' as "Descripcion",
    'E' as "Severidad",
    'Si C40 <> 01011900 => C40 >=C18' as "Formula"
from dummy;

/*CUADRATURAS*/
SET SCHEMA "BSCL";
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"  WHERE "Esquema"='DJ1820' AND "Version"='2016/12';
insert into "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura" ("Esquema","Version","Tipo","Descripcion")
select
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'PorRutCliente' as "Tipo",
    'Cuadratura por RUT Cliente' as "Descripcion"
from dummy;

DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura"  WHERE "Esquema"='DJ1820' AND "Version"='2016/12';
insert into "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" ("Esquema","Version","TipoCuadratura","NombreCampo","Orden")
select
    'DJ1820' as "Esquema",
    '2016/12' as "Version",
    'PorRutCliente' as "TipoCuadratura",
    'RutCliente' as "NombreCampo",
    1 as "Orden"
from dummy;


--CONTROL PROCESO

insert into "BSCL"."BSCL.Normativa.BD::TRI.ControlProceso"
("Esquema","Version","EstadoCertificado","Carga","Validacion","PreDeclaracion","Certificacion")
SELECT "Esquema","Version",'NGE','N','N','N','N' FROM "BSCL"."BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1820' AND "Version" IN ('2016/12');


/*OTRAS VERTSIONES*/


DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura"  WHERE "Esquema"='DJ1820' AND "Version" IN ('2020/01','2020/02','2020/03','2020/04','2020/05','2020/06','2020/07','2020/08','2020/09','2020/10','2020/11','2020/12');
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"  WHERE "Esquema"='DJ1820' AND "Version" IN ('2020/01','2020/02','2020/03','2020/04','2020/05','2020/06','2020/07','2020/08','2020/09','2020/10','2020/11','2020/12');
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1820' and "Version" IN ('2020/01','2020/02','2020/03','2020/04','2020/05','2020/06','2020/07','2020/08','2020/09','2020/10','2020/11','2020/12');
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema"='DJ1820' AND "Version" IN ('2020/01','2020/02','2020/03','2020/04','2020/05','2020/06','2020/07','2020/08','2020/09','2020/10','2020/11','2020/12');
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1820' AND "Version" IN ('2020/01','2020/02','2020/03','2020/04','2020/05','2020/06','2020/07','2020/08','2020/09','2020/10','2020/11','2020/12');
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Documento" where "Esquema" = 'DJ1820' and "Version" IN ('2020/01','2020/02','2020/03','2020/04','2020/05','2020/06','2020/07','2020/08','2020/09','2020/10','2020/11','2020/12');
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1820' AND "Version" IN ('2020/01','2020/02','2020/03','2020/04','2020/05','2020/06','2020/07','2020/08','2020/09','2020/10','2020/11','2020/12');
DELETE FROM "BSCL"."BSCL.Normativa.BD::TRI.ControlProceso" WHERE  "Esquema" = 'DJ1820' AND "Version" IN ('2020/01','2020/02','2020/03','2020/04','2020/05','2020/06','2020/07','2020/08','2020/09','2020/10','2020/11','2020/12');



--'2020/01'

SET SCHEMA "BSCL";
DELETE FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1820' AND "Version"='2020/01';
insert into "BSCL.Normativa.BD::DOC.Esquema" ("Esquema","Version","Modulo","Periodicidad","Objetivo","PeriodoInicio",
		     "IdEntidadReguladora","FechaPresentacionLimite","FechaDatosDesde","FechaDatosHasta",
		     "CodigoVersionSII","HabilitadoMantenedor")
values
('DJ1820','2020/01','TRI','M','','2020-01-30','SII','2020-02-05','2020-01-01','2020-01-31',24,'S');

--SELECT "IdDocumento" FROM "BSCL.Normativa.BD::DOC.Documento" ORDER BY 1
--SELECT * FROM "BSCL.Normativa.BD::DOC.Documento" ORDER BY 1

DELETE FROM "BSCL.Normativa.BD::DOC.Documento" where "Esquema" = 'DJ1820' and "Version"='2020/01';
insert into "BSCL.Normativa.BD::DOC.Documento"
("IdDocumento","IdTipoDocumento","Esquema","Version","IdEmpresa","FechaLimite","IdEstadoDocumento",
"FechaCreacion","FechaPresentacion","DocumentoIdentidadDeclarante",
"DocumentoIdentidadRepresentante","DireccionElectronicaDeclaracion",
"DireccionFisicaDeclaracion","ComunaDeclaracion","TelefonoDeclaracion",
"FolioDeclaracion","EnProceso","FechaInicioUltimoProceso","AnoMes")
SELECT "BSCL"."BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL AS "IdDocumento",
'DECLACORR' AS "IdTipoDocumento"
,'DJ1820' AS "Esquema"
,'2020/01' AS "Version"
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



DELETE FROM "BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1820' and "Version" = '2020/01';
insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
select "Esquema", '2020/01' as "Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales"
from "BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1820' and "Version" = '2016/12'; 

SET SCHEMA "BSCL";
DELETE FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema"='DJ1820' AND "Version"='2020/01';
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL AS "IdHomologacion"
, "Esquema", '2020/01' AS "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado"
FROM  "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" ='DJ1820' AND "Version"='2016/12';


DELETE FROM "BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1820' AND "Version"='2020/01';
INSERT INTO "BSCL.Normativa.BD::DOC.Validacion" ("Esquema","Version",  "Codigo",   "Tipo",   "Activa",   "Descripcion",   "Severidad", "Formula")
SELECT  "Esquema",'2020/01' AS "Version",  "Codigo",   "Tipo",   "Activa",   "Descripcion",   "Severidad", "Formula"
FROM  "BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1820' AND "Version"='2016/12';

SET SCHEMA "BSCL";
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"  WHERE "Esquema"='DJ1820' AND "Version"='2020/01';
insert into "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura" ("Esquema","Version","Tipo","Descripcion")
SELECT "Esquema",'2020/01' AS "Version","Tipo","Descripcion" FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"
WHERE "Esquema"='DJ1820' AND "Version"='2016/12';

DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura"  WHERE "Esquema"='DJ1820' AND "Version"='2020/01';
insert into "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" ("Esquema","Version","TipoCuadratura","NombreCampo","Orden")
SELECT "Esquema",'2020/01' AS "Version","TipoCuadratura","NombreCampo","Orden" FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" 
WHERE "Esquema"='DJ1820' AND "Version"='2016/12';


--'2020/02'

SET SCHEMA "BSCL";
DELETE FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1820' AND "Version"='2020/02';
insert into "BSCL.Normativa.BD::DOC.Esquema" ("Esquema","Version","Modulo","Periodicidad","Objetivo","PeriodoInicio",
		     "IdEntidadReguladora","FechaPresentacionLimite","FechaDatosDesde","FechaDatosHasta",
		     "CodigoVersionSII","HabilitadoMantenedor")
values
('DJ1820','2020/02','TRI','M','','2020-01-30','SII','2020-02-05','2020-02-01','2020-02-28',24,'S');


DELETE FROM "BSCL.Normativa.BD::DOC.Documento" where "Esquema" = 'DJ1820' and "Version"='2020/02';
insert into "BSCL.Normativa.BD::DOC.Documento"
("IdDocumento","IdTipoDocumento","Esquema","Version","IdEmpresa","FechaLimite","IdEstadoDocumento",
"FechaCreacion","FechaPresentacion","DocumentoIdentidadDeclarante",
"DocumentoIdentidadRepresentante","DireccionElectronicaDeclaracion",
"DireccionFisicaDeclaracion","ComunaDeclaracion","TelefonoDeclaracion",
"FolioDeclaracion","EnProceso","FechaInicioUltimoProceso","AnoMes")
SELECT "BSCL"."BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL AS "IdDocumento",
'DECLACORR' AS "IdTipoDocumento"
,'DJ1820' AS "Esquema"
,'2020/02' AS "Version"
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





DELETE FROM "BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1820' and "Version" = '2020/02';
insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
select "Esquema", '2020/02' as "Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales"
from "BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1820' and "Version" = '2016/12'; 

SET SCHEMA "BSCL";
DELETE FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema"='DJ1820' AND "Version"='2020/02';
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL AS "IdHomologacion"
, "Esquema", '2020/02' AS "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado"
FROM  "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" ='DJ1820' AND "Version"='2016/12';


DELETE FROM "BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1820' AND "Version"='2020/02';
INSERT INTO "BSCL.Normativa.BD::DOC.Validacion" ("Esquema","Version",  "Codigo",   "Tipo",   "Activa",   "Descripcion",   "Severidad", "Formula")
SELECT  "Esquema",'2020/02' AS "Version",  "Codigo",   "Tipo",   "Activa",   "Descripcion",   "Severidad", "Formula"
FROM  "BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1820' AND "Version"='2016/12';


SET SCHEMA "BSCL";
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"  WHERE "Esquema"='DJ1820' AND "Version"='2020/02';
insert into "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura" ("Esquema","Version","Tipo","Descripcion")
SELECT "Esquema",'2020/02' AS "Version","Tipo","Descripcion" FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"
WHERE "Esquema"='DJ1820' AND "Version"='2016/12';

DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura"  WHERE "Esquema"='DJ1820' AND "Version"='2020/02';
insert into "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" ("Esquema","Version","TipoCuadratura","NombreCampo","Orden")
SELECT "Esquema",'2020/02' AS "Version","TipoCuadratura","NombreCampo","Orden" FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" 
WHERE "Esquema"='DJ1820' AND "Version"='2016/12';


--'2020/03'

SET SCHEMA "BSCL";
DELETE FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1820' AND "Version"='2020/03';
insert into "BSCL.Normativa.BD::DOC.Esquema" ("Esquema","Version","Modulo","Periodicidad","Objetivo","PeriodoInicio",
		     "IdEntidadReguladora","FechaPresentacionLimite","FechaDatosDesde","FechaDatosHasta",
		     "CodigoVersionSII","HabilitadoMantenedor")
values
('DJ1820','2020/03','TRI','M','','2020-03-30','SII','2020-03-05','2020-03-01','2020-03-28',24,'S');


DELETE FROM "BSCL.Normativa.BD::DOC.Documento" where "Esquema" = 'DJ1820' and "Version"='2020/03';
insert into "BSCL.Normativa.BD::DOC.Documento"
("IdDocumento","IdTipoDocumento","Esquema","Version","IdEmpresa","FechaLimite","IdEstadoDocumento",
"FechaCreacion","FechaPresentacion","DocumentoIdentidadDeclarante",
"DocumentoIdentidadRepresentante","DireccionElectronicaDeclaracion",
"DireccionFisicaDeclaracion","ComunaDeclaracion","TelefonoDeclaracion",
"FolioDeclaracion","EnProceso","FechaInicioUltimoProceso","AnoMes")
SELECT "BSCL"."BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL AS "IdDocumento",
'DECLACORR' AS "IdTipoDocumento"
,'DJ1820' AS "Esquema"
,'2020/03' AS "Version"
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


DELETE FROM "BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1820' and "Version" = '2020/03';
insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
select "Esquema", '2020/03' as "Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales"
from "BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1820' and "Version" = '2016/12'; 

SET SCHEMA "BSCL";
DELETE FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema"='DJ1820' AND "Version"='2020/03';
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL AS "IdHomologacion"
, "Esquema", '2020/03' AS "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado"
FROM  "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" ='DJ1820' AND "Version"='2016/12';


DELETE FROM "BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1820' AND "Version"='2020/03';
INSERT INTO "BSCL.Normativa.BD::DOC.Validacion" ("Esquema","Version",  "Codigo",   "Tipo",   "Activa",   "Descripcion",   "Severidad", "Formula")
SELECT  "Esquema",'2020/03' AS "Version",  "Codigo",   "Tipo",   "Activa",   "Descripcion",   "Severidad", "Formula"
FROM  "BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1820' AND "Version"='2016/12';

SET SCHEMA "BSCL";
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"  WHERE "Esquema"='DJ1820' AND "Version"='2020/03';
insert into "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura" ("Esquema","Version","Tipo","Descripcion")
SELECT "Esquema",'2020/03' AS "Version","Tipo","Descripcion" FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"
WHERE "Esquema"='DJ1820' AND "Version"='2016/12';

DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura"  WHERE "Esquema"='DJ1820' AND "Version"='2020/03';
insert into "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" ("Esquema","Version","TipoCuadratura","NombreCampo","Orden")
SELECT "Esquema",'2020/03' AS "Version","TipoCuadratura","NombreCampo","Orden" FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" 
WHERE "Esquema"='DJ1820' AND "Version"='2016/12';



--'2020/04'

SET SCHEMA "BSCL";
DELETE FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1820' AND "Version"='2020/04';
insert into "BSCL.Normativa.BD::DOC.Esquema" ("Esquema","Version","Modulo","Periodicidad","Objetivo","PeriodoInicio",
		     "IdEntidadReguladora","FechaPresentacionLimite","FechaDatosDesde","FechaDatosHasta",
		     "CodigoVersionSII","HabilitadoMantenedor")
values
('DJ1820','2020/04','TRI','M','','2020-04-30','SII','2020-04-05','2020-04-01','2020-04-30',24,'S');


DELETE FROM "BSCL.Normativa.BD::DOC.Documento" where "Esquema" = 'DJ1820' and "Version"='2020/04';
insert into "BSCL.Normativa.BD::DOC.Documento"
("IdDocumento","IdTipoDocumento","Esquema","Version","IdEmpresa","FechaLimite","IdEstadoDocumento",
"FechaCreacion","FechaPresentacion","DocumentoIdentidadDeclarante",
"DocumentoIdentidadRepresentante","DireccionElectronicaDeclaracion",
"DireccionFisicaDeclaracion","ComunaDeclaracion","TelefonoDeclaracion",
"FolioDeclaracion","EnProceso","FechaInicioUltimoProceso","AnoMes")
SELECT "BSCL"."BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL AS "IdDocumento",
'DECLACORR' AS "IdTipoDocumento"
,'DJ1820' AS "Esquema"
,'2020/04' AS "Version"
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



DELETE FROM "BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1820' and "Version" = '2020/04';
insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
select "Esquema", '2020/04' as "Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales"
from "BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1820' and "Version" = '2016/12'; 

SET SCHEMA "BSCL";
DELETE FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema"='DJ1820' AND "Version"='2020/04';
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL AS "IdHomologacion"
, "Esquema", '2020/04' AS "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado"
FROM  "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" ='DJ1820' AND "Version"='2016/12';


DELETE FROM "BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1820' AND "Version"='2020/04';
INSERT INTO "BSCL.Normativa.BD::DOC.Validacion" ("Esquema","Version",  "Codigo",   "Tipo",   "Activa",   "Descripcion",   "Severidad", "Formula")
SELECT  "Esquema",'2020/04' AS "Version",  "Codigo",   "Tipo",   "Activa",   "Descripcion",   "Severidad", "Formula"
FROM  "BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1820' AND "Version"='2016/12';

SET SCHEMA "BSCL";
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"  WHERE "Esquema"='DJ1820' AND "Version"='2020/04';
insert into "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura" ("Esquema","Version","Tipo","Descripcion")
SELECT "Esquema",'2020/04' AS "Version","Tipo","Descripcion" FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"
WHERE "Esquema"='DJ1820' AND "Version"='2016/12';

DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura"  WHERE "Esquema"='DJ1820' AND "Version"='2020/04';
insert into "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" ("Esquema","Version","TipoCuadratura","NombreCampo","Orden")
SELECT "Esquema",'2020/04' AS "Version","TipoCuadratura","NombreCampo","Orden" FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" 
WHERE "Esquema"='DJ1820' AND "Version"='2016/12';




--'2020/05'

SET SCHEMA "BSCL";
DELETE FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1820' AND "Version"='2020/05';
insert into "BSCL.Normativa.BD::DOC.Esquema" ("Esquema","Version","Modulo","Periodicidad","Objetivo","PeriodoInicio",
		     "IdEntidadReguladora","FechaPresentacionLimite","FechaDatosDesde","FechaDatosHasta",
		     "CodigoVersionSII","HabilitadoMantenedor")
values
('DJ1820','2020/05','TRI','M','','2020-05-30','SII','2020-05-05','2020-05-01','2020-05-30',24,'S');


DELETE FROM "BSCL.Normativa.BD::DOC.Documento" where "Esquema" = 'DJ1820' and "Version"='2020/05';
insert into "BSCL.Normativa.BD::DOC.Documento"
("IdDocumento","IdTipoDocumento","Esquema","Version","IdEmpresa","FechaLimite","IdEstadoDocumento",
"FechaCreacion","FechaPresentacion","DocumentoIdentidadDeclarante",
"DocumentoIdentidadRepresentante","DireccionElectronicaDeclaracion",
"DireccionFisicaDeclaracion","ComunaDeclaracion","TelefonoDeclaracion",
"FolioDeclaracion","EnProceso","FechaInicioUltimoProceso","AnoMes")
SELECT "BSCL"."BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL AS "IdDocumento",
'DECLACORR' AS "IdTipoDocumento"
,'DJ1820' AS "Esquema"
,'2020/05' AS "Version"
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


DELETE FROM "BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1820' and "Version" = '2020/05';
insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
select "Esquema", '2020/05' as "Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales"
from "BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1820' and "Version" = '2016/12'; 

SET SCHEMA "BSCL";
DELETE FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema"='DJ1820' AND "Version"='2020/05';
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL AS "IdHomologacion"
, "Esquema", '2020/05' AS "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado"
FROM  "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" ='DJ1820' AND "Version"='2016/12';


DELETE FROM "BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1820' AND "Version"='2020/05';
INSERT INTO "BSCL.Normativa.BD::DOC.Validacion" ("Esquema","Version",  "Codigo",   "Tipo",   "Activa",   "Descripcion",   "Severidad", "Formula")
SELECT  "Esquema",'2020/05' AS "Version",  "Codigo",   "Tipo",   "Activa",   "Descripcion",   "Severidad", "Formula"
FROM  "BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1820' AND "Version"='2016/12';

SET SCHEMA "BSCL";
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"  WHERE "Esquema"='DJ1820' AND "Version"='2020/05';
insert into "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura" ("Esquema","Version","Tipo","Descripcion")
SELECT "Esquema",'2020/05' AS "Version","Tipo","Descripcion" FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"
WHERE "Esquema"='DJ1820' AND "Version"='2016/12';

DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura"  WHERE "Esquema"='DJ1820' AND "Version"='2020/05';
insert into "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" ("Esquema","Version","TipoCuadratura","NombreCampo","Orden")
SELECT "Esquema",'2020/05' AS "Version","TipoCuadratura","NombreCampo","Orden" FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" 
WHERE "Esquema"='DJ1820' AND "Version"='2016/12';


--'2020/06'

SET SCHEMA "BSCL";
DELETE FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1820' AND "Version"='2020/06';
insert into "BSCL.Normativa.BD::DOC.Esquema" ("Esquema","Version","Modulo","Periodicidad","Objetivo","PeriodoInicio",
		     "IdEntidadReguladora","FechaPresentacionLimite","FechaDatosDesde","FechaDatosHasta",
		     "CodigoVersionSII","HabilitadoMantenedor")
values
('DJ1820','2020/06','TRI','M','','2020-06-30','SII','2020-06-05','2020-06-01','2020-06-30',24,'S');


DELETE FROM "BSCL.Normativa.BD::DOC.Documento" where "Esquema" = 'DJ1820' and "Version"='2020/06';
insert into "BSCL.Normativa.BD::DOC.Documento"
("IdDocumento","IdTipoDocumento","Esquema","Version","IdEmpresa","FechaLimite","IdEstadoDocumento",
"FechaCreacion","FechaPresentacion","DocumentoIdentidadDeclarante",
"DocumentoIdentidadRepresentante","DireccionElectronicaDeclaracion",
"DireccionFisicaDeclaracion","ComunaDeclaracion","TelefonoDeclaracion",
"FolioDeclaracion","EnProceso","FechaInicioUltimoProceso","AnoMes")
SELECT "BSCL"."BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL AS "IdDocumento",
'DECLACORR' AS "IdTipoDocumento"
,'DJ1820' AS "Esquema"
,'2020/06' AS "Version"
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



DELETE FROM "BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1820' and "Version" = '2020/06';
insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
select "Esquema", '2020/06' as "Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales"
from "BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1820' and "Version" = '2016/12'; 

SET SCHEMA "BSCL";
DELETE FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema"='DJ1820' AND "Version"='2020/06';
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL AS "IdHomologacion"
, "Esquema", '2020/06' AS "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado"
FROM  "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" ='DJ1820' AND "Version"='2016/12';


DELETE FROM "BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1820' AND "Version"='2020/06';
INSERT INTO "BSCL.Normativa.BD::DOC.Validacion" ("Esquema","Version",  "Codigo",   "Tipo",   "Activa",   "Descripcion",   "Severidad", "Formula")
SELECT  "Esquema",'2020/06' AS "Version",  "Codigo",   "Tipo",   "Activa",   "Descripcion",   "Severidad", "Formula"
FROM  "BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1820' AND "Version"='2016/12';

SET SCHEMA "BSCL";
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"  WHERE "Esquema"='DJ1820' AND "Version"='2020/06';
insert into "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura" ("Esquema","Version","Tipo","Descripcion")
SELECT "Esquema",'2020/06' AS "Version","Tipo","Descripcion" FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"
WHERE "Esquema"='DJ1820' AND "Version"='2016/12';

DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura"  WHERE "Esquema"='DJ1820' AND "Version"='2020/06';
insert into "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" ("Esquema","Version","TipoCuadratura","NombreCampo","Orden")
SELECT "Esquema",'2020/06' AS "Version","TipoCuadratura","NombreCampo","Orden" FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" 
WHERE "Esquema"='DJ1820' AND "Version"='2016/12';


--'2020/07'

SET SCHEMA "BSCL";
DELETE FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1820' AND "Version"='2020/07';
insert into "BSCL.Normativa.BD::DOC.Esquema" ("Esquema","Version","Modulo","Periodicidad","Objetivo","PeriodoInicio",
		     "IdEntidadReguladora","FechaPresentacionLimite","FechaDatosDesde","FechaDatosHasta",
		     "CodigoVersionSII","HabilitadoMantenedor")
values
('DJ1820','2020/07','TRI','M','','2020-07-30','SII','2020-07-05','2020-07-01','2020-07-30',24,'S');


DELETE FROM "BSCL.Normativa.BD::DOC.Documento" where "Esquema" = 'DJ1820' and "Version"='2020/07';
insert into "BSCL.Normativa.BD::DOC.Documento"
("IdDocumento","IdTipoDocumento","Esquema","Version","IdEmpresa","FechaLimite","IdEstadoDocumento",
"FechaCreacion","FechaPresentacion","DocumentoIdentidadDeclarante",
"DocumentoIdentidadRepresentante","DireccionElectronicaDeclaracion",
"DireccionFisicaDeclaracion","ComunaDeclaracion","TelefonoDeclaracion",
"FolioDeclaracion","EnProceso","FechaInicioUltimoProceso","AnoMes")
SELECT "BSCL"."BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL AS "IdDocumento",
'DECLACORR' AS "IdTipoDocumento"
,'DJ1820' AS "Esquema"
,'2020/07' AS "Version"
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



DELETE FROM "BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1820' and "Version" = '2020/07';
insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
select "Esquema", '2020/07' as "Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales"
from "BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1820' and "Version" = '2016/12'; 

SET SCHEMA "BSCL";
DELETE FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema"='DJ1820' AND "Version"='2020/07';
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL AS "IdHomologacion"
, "Esquema", '2020/07' AS "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado"
FROM  "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" ='DJ1820' AND "Version"='2016/12';


DELETE FROM "BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1820' AND "Version"='2020/07';
INSERT INTO "BSCL.Normativa.BD::DOC.Validacion" ("Esquema","Version",  "Codigo",   "Tipo",   "Activa",   "Descripcion",   "Severidad", "Formula")
SELECT  "Esquema",'2020/07' AS "Version",  "Codigo",   "Tipo",   "Activa",   "Descripcion",   "Severidad", "Formula"
FROM  "BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1820' AND "Version"='2016/12';

SET SCHEMA "BSCL";
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"  WHERE "Esquema"='DJ1820' AND "Version"='2020/07';
insert into "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura" ("Esquema","Version","Tipo","Descripcion")
SELECT "Esquema",'2020/07' AS "Version","Tipo","Descripcion" FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"
WHERE "Esquema"='DJ1820' AND "Version"='2016/12';

DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura"  WHERE "Esquema"='DJ1820' AND "Version"='2020/07';
insert into "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" ("Esquema","Version","TipoCuadratura","NombreCampo","Orden")
SELECT "Esquema",'2020/07' AS "Version","TipoCuadratura","NombreCampo","Orden" FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" 
WHERE "Esquema"='DJ1820' AND "Version"='2016/12';



--'2020/08'

SET SCHEMA "BSCL";
DELETE FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1820' AND "Version"='2020/08';
insert into "BSCL.Normativa.BD::DOC.Esquema" ("Esquema","Version","Modulo","Periodicidad","Objetivo","PeriodoInicio",
		     "IdEntidadReguladora","FechaPresentacionLimite","FechaDatosDesde","FechaDatosHasta",
		     "CodigoVersionSII","HabilitadoMantenedor")
values
('DJ1820','2020/08','TRI','M','','2020-08-30','SII','2020-08-05','2020-08-01','2020-08-30',24,'S');


DELETE FROM "BSCL.Normativa.BD::DOC.Documento" where "Esquema" = 'DJ1820' and "Version"='2020/08';
insert into "BSCL.Normativa.BD::DOC.Documento"
("IdDocumento","IdTipoDocumento","Esquema","Version","IdEmpresa","FechaLimite","IdEstadoDocumento",
"FechaCreacion","FechaPresentacion","DocumentoIdentidadDeclarante",
"DocumentoIdentidadRepresentante","DireccionElectronicaDeclaracion",
"DireccionFisicaDeclaracion","ComunaDeclaracion","TelefonoDeclaracion",
"FolioDeclaracion","EnProceso","FechaInicioUltimoProceso","AnoMes")
SELECT "BSCL"."BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL AS "IdDocumento",
'DECLACORR' AS "IdTipoDocumento"
,'DJ1820' AS "Esquema"
,'2020/08' AS "Version"
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


DELETE FROM "BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1820' and "Version" = '2020/08';
insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
select "Esquema", '2020/08' as "Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales"
from "BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1820' and "Version" = '2016/12'; 

SET SCHEMA "BSCL";
DELETE FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema"='DJ1820' AND "Version"='2020/08';
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL AS "IdHomologacion"
, "Esquema", '2020/08' AS "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado"
FROM  "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" ='DJ1820' AND "Version"='2016/12';


DELETE FROM "BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1820' AND "Version"='2020/08';
INSERT INTO "BSCL.Normativa.BD::DOC.Validacion" ("Esquema","Version",  "Codigo",   "Tipo",   "Activa",   "Descripcion",   "Severidad", "Formula")
SELECT  "Esquema",'2020/08' AS "Version",  "Codigo",   "Tipo",   "Activa",   "Descripcion",   "Severidad", "Formula"
FROM  "BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1820' AND "Version"='2016/12';

SET SCHEMA "BSCL";
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"  WHERE "Esquema"='DJ1820' AND "Version"='2020/08';
insert into "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura" ("Esquema","Version","Tipo","Descripcion")
SELECT "Esquema",'2020/08' AS "Version","Tipo","Descripcion" FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"
WHERE "Esquema"='DJ1820' AND "Version"='2016/12';

DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura"  WHERE "Esquema"='DJ1820' AND "Version"='2020/08';
insert into "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" ("Esquema","Version","TipoCuadratura","NombreCampo","Orden")
SELECT "Esquema",'2020/08' AS "Version","TipoCuadratura","NombreCampo","Orden" FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" 
WHERE "Esquema"='DJ1820' AND "Version"='2016/12';




--'2020/09'

SET SCHEMA "BSCL";
DELETE FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1820' AND "Version"='2020/09';
insert into "BSCL.Normativa.BD::DOC.Esquema" ("Esquema","Version","Modulo","Periodicidad","Objetivo","PeriodoInicio",
		     "IdEntidadReguladora","FechaPresentacionLimite","FechaDatosDesde","FechaDatosHasta",
		     "CodigoVersionSII","HabilitadoMantenedor")
values
('DJ1820','2020/09','TRI','M','','2020-09-30','SII','2020-09-05','2020-09-01','2020-09-30',24,'S');


DELETE FROM "BSCL.Normativa.BD::DOC.Documento" where "Esquema" = 'DJ1820' and "Version"='2020/09';
insert into "BSCL.Normativa.BD::DOC.Documento"
("IdDocumento","IdTipoDocumento","Esquema","Version","IdEmpresa","FechaLimite","IdEstadoDocumento",
"FechaCreacion","FechaPresentacion","DocumentoIdentidadDeclarante",
"DocumentoIdentidadRepresentante","DireccionElectronicaDeclaracion",
"DireccionFisicaDeclaracion","ComunaDeclaracion","TelefonoDeclaracion",
"FolioDeclaracion","EnProceso","FechaInicioUltimoProceso","AnoMes")
SELECT "BSCL"."BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL AS "IdDocumento",
'DECLACORR' AS "IdTipoDocumento"
,'DJ1820' AS "Esquema"
,'2020/09' AS "Version"
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



DELETE FROM "BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1820' and "Version" = '2020/09';
insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
select "Esquema", '2020/09' as "Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales"
from "BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1820' and "Version" = '2016/12'; 

SET SCHEMA "BSCL";
DELETE FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema"='DJ1820' AND "Version"='2020/09';
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL AS "IdHomologacion"
, "Esquema", '2020/09' AS "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado"
FROM  "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" ='DJ1820' AND "Version"='2016/12';


DELETE FROM "BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1820' AND "Version"='2020/09';
INSERT INTO "BSCL.Normativa.BD::DOC.Validacion" ("Esquema","Version",  "Codigo",   "Tipo",   "Activa",   "Descripcion",   "Severidad", "Formula")
SELECT  "Esquema",'2020/09' AS "Version",  "Codigo",   "Tipo",   "Activa",   "Descripcion",   "Severidad", "Formula"
FROM  "BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1820' AND "Version"='2016/12';

SET SCHEMA "BSCL";
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"  WHERE "Esquema"='DJ1820' AND "Version"='2020/09';
insert into "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura" ("Esquema","Version","Tipo","Descripcion")
SELECT "Esquema",'2020/09' AS "Version","Tipo","Descripcion" FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"
WHERE "Esquema"='DJ1820' AND "Version"='2016/12';

DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura"  WHERE "Esquema"='DJ1820' AND "Version"='2020/09';
insert into "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" ("Esquema","Version","TipoCuadratura","NombreCampo","Orden")
SELECT "Esquema",'2020/09' AS "Version","TipoCuadratura","NombreCampo","Orden" FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" 
WHERE "Esquema"='DJ1820' AND "Version"='2016/12';




--'2020/10'

SET SCHEMA "BSCL";
DELETE FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1820' AND "Version"='2020/10';
insert into "BSCL.Normativa.BD::DOC.Esquema" ("Esquema","Version","Modulo","Periodicidad","Objetivo","PeriodoInicio",
		     "IdEntidadReguladora","FechaPresentacionLimite","FechaDatosDesde","FechaDatosHasta",
		     "CodigoVersionSII","HabilitadoMantenedor")
values
('DJ1820','2020/10','TRI','M','','2020-10-30','SII','2020-10-05','2020-10-01','2020-10-30',24,'S');


DELETE FROM "BSCL.Normativa.BD::DOC.Documento" where "Esquema" = 'DJ1820' and "Version"='2020/10';
insert into "BSCL.Normativa.BD::DOC.Documento"
("IdDocumento","IdTipoDocumento","Esquema","Version","IdEmpresa","FechaLimite","IdEstadoDocumento",
"FechaCreacion","FechaPresentacion","DocumentoIdentidadDeclarante",
"DocumentoIdentidadRepresentante","DireccionElectronicaDeclaracion",
"DireccionFisicaDeclaracion","ComunaDeclaracion","TelefonoDeclaracion",
"FolioDeclaracion","EnProceso","FechaInicioUltimoProceso","AnoMes")
SELECT "BSCL"."BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL AS "IdDocumento",
'DECLACORR' AS "IdTipoDocumento"
,'DJ1820' AS "Esquema"
,'2020/10' AS "Version"
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



DELETE FROM "BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1820' and "Version" = '2020/10';
insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
select "Esquema", '2020/10' as "Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales"
from "BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1820' and "Version" = '2016/12'; 

SET SCHEMA "BSCL";
DELETE FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema"='DJ1820' AND "Version"='2020/10';
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL AS "IdHomologacion"
, "Esquema", '2020/10' AS "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado"
FROM  "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" ='DJ1820' AND "Version"='2016/12';


DELETE FROM "BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1820' AND "Version"='2020/10';
INSERT INTO "BSCL.Normativa.BD::DOC.Validacion" ("Esquema","Version",  "Codigo",   "Tipo",   "Activa",   "Descripcion",   "Severidad", "Formula")
SELECT  "Esquema",'2020/10' AS "Version",  "Codigo",   "Tipo",   "Activa",   "Descripcion",   "Severidad", "Formula"
FROM  "BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1820' AND "Version"='2016/12';

SET SCHEMA "BSCL";
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"  WHERE "Esquema"='DJ1820' AND "Version"='2020/10';
insert into "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura" ("Esquema","Version","Tipo","Descripcion")
SELECT "Esquema",'2020/10' AS "Version","Tipo","Descripcion" FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"
WHERE "Esquema"='DJ1820' AND "Version"='2016/12';

DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura"  WHERE "Esquema"='DJ1820' AND "Version"='2020/10';
insert into "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" ("Esquema","Version","TipoCuadratura","NombreCampo","Orden")
SELECT "Esquema",'2020/10' AS "Version","TipoCuadratura","NombreCampo","Orden" FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" 
WHERE "Esquema"='DJ1820' AND "Version"='2016/12';




--'2020/11'

SET SCHEMA "BSCL";
DELETE FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1820' AND "Version"='2020/11';
insert into "BSCL.Normativa.BD::DOC.Esquema" ("Esquema","Version","Modulo","Periodicidad","Objetivo","PeriodoInicio",
		     "IdEntidadReguladora","FechaPresentacionLimite","FechaDatosDesde","FechaDatosHasta",
		     "CodigoVersionSII","HabilitadoMantenedor")
values
('DJ1820','2020/11','TRI','M','','2020-11-30','SII','2020-11-05','2020-11-01','2020-11-30',24,'S');


DELETE FROM "BSCL.Normativa.BD::DOC.Documento" where "Esquema" = 'DJ1820' and "Version"='2020/11';
insert into "BSCL.Normativa.BD::DOC.Documento"
("IdDocumento","IdTipoDocumento","Esquema","Version","IdEmpresa","FechaLimite","IdEstadoDocumento",
"FechaCreacion","FechaPresentacion","DocumentoIdentidadDeclarante",
"DocumentoIdentidadRepresentante","DireccionElectronicaDeclaracion",
"DireccionFisicaDeclaracion","ComunaDeclaracion","TelefonoDeclaracion",
"FolioDeclaracion","EnProceso","FechaInicioUltimoProceso","AnoMes")
SELECT "BSCL"."BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL AS "IdDocumento",
'DECLACORR' AS "IdTipoDocumento"
,'DJ1820' AS "Esquema"
,'2020/11' AS "Version"
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



DELETE FROM "BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1820' and "Version" = '2020/11';
insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
select "Esquema", '2020/11' as "Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales"
from "BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1820' and "Version" = '2016/12'; 

SET SCHEMA "BSCL";
DELETE FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema"='DJ1820' AND "Version"='2020/11';
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL AS "IdHomologacion"
, "Esquema", '2020/11' AS "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado"
FROM  "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" ='DJ1820' AND "Version"='2016/12';


DELETE FROM "BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1820' AND "Version"='2020/11';
INSERT INTO "BSCL.Normativa.BD::DOC.Validacion" ("Esquema","Version",  "Codigo",   "Tipo",   "Activa",   "Descripcion",   "Severidad", "Formula")
SELECT  "Esquema",'2020/11' AS "Version",  "Codigo",   "Tipo",   "Activa",   "Descripcion",   "Severidad", "Formula"
FROM  "BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1820' AND "Version"='2016/12';

SET SCHEMA "BSCL";
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"  WHERE "Esquema"='DJ1820' AND "Version"='2020/11';
insert into "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura" ("Esquema","Version","Tipo","Descripcion")
SELECT "Esquema",'2020/11' AS "Version","Tipo","Descripcion" FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"
WHERE "Esquema"='DJ1820' AND "Version"='2016/12';

DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura"  WHERE "Esquema"='DJ1820' AND "Version"='2020/11';
insert into "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" ("Esquema","Version","TipoCuadratura","NombreCampo","Orden")
SELECT "Esquema",'2020/11' AS "Version","TipoCuadratura","NombreCampo","Orden" FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" 
WHERE "Esquema"='DJ1820' AND "Version"='2016/12';



--'2020/12'

SET SCHEMA "BSCL";
DELETE FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1820' AND "Version"='2020/12';
insert into "BSCL.Normativa.BD::DOC.Esquema" ("Esquema","Version","Modulo","Periodicidad","Objetivo","PeriodoInicio",
		     "IdEntidadReguladora","FechaPresentacionLimite","FechaDatosDesde","FechaDatosHasta",
		     "CodigoVersionSII","HabilitadoMantenedor")
values
('DJ1820','2020/12','TRI','M','','2020-12-30','SII','2020-12-05','2020-12-01','2020-12-30',24,'S');


DELETE FROM "BSCL.Normativa.BD::DOC.Documento" where "Esquema" = 'DJ1820' and "Version"='2020/12';
insert into "BSCL.Normativa.BD::DOC.Documento"
("IdDocumento","IdTipoDocumento","Esquema","Version","IdEmpresa","FechaLimite","IdEstadoDocumento",
"FechaCreacion","FechaPresentacion","DocumentoIdentidadDeclarante",
"DocumentoIdentidadRepresentante","DireccionElectronicaDeclaracion",
"DireccionFisicaDeclaracion","ComunaDeclaracion","TelefonoDeclaracion",
"FolioDeclaracion","EnProceso","FechaInicioUltimoProceso","AnoMes")
SELECT "BSCL"."BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL AS "IdDocumento",
'DECLACORR' AS "IdTipoDocumento"
,'DJ1820' AS "Esquema"
,'2020/12' AS "Version"
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



DELETE FROM "BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1820' and "Version" = '2020/12';
insert into "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
select "Esquema", '2020/12' as "Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales"
from "BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1820' and "Version" = '2016/12'; 

SET SCHEMA "BSCL";
DELETE FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema"='DJ1820' AND "Version"='2020/12';
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL AS "IdHomologacion"
, "Esquema", '2020/12' AS "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado"
FROM  "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" ='DJ1820' AND "Version"='2016/12';


DELETE FROM "BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1820' AND "Version"='2020/12';
INSERT INTO "BSCL.Normativa.BD::DOC.Validacion" ("Esquema","Version",  "Codigo",   "Tipo",   "Activa",   "Descripcion",   "Severidad", "Formula")
SELECT  "Esquema",'2020/12' AS "Version",  "Codigo",   "Tipo",   "Activa",   "Descripcion",   "Severidad", "Formula"
FROM  "BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1820' AND "Version"='2016/12';

SET SCHEMA "BSCL";
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"  WHERE "Esquema"='DJ1820' AND "Version"='2020/12';
insert into "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura" ("Esquema","Version","Tipo","Descripcion")
SELECT "Esquema",'2020/12' AS "Version","Tipo","Descripcion" FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"
WHERE "Esquema"='DJ1820' AND "Version"='2016/12';

DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura"  WHERE "Esquema"='DJ1820' AND "Version"='2020/12';
insert into "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" ("Esquema","Version","TipoCuadratura","NombreCampo","Orden")
SELECT "Esquema",'2020/12' AS "Version","TipoCuadratura","NombreCampo","Orden" FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" 
WHERE "Esquema"='DJ1820' AND "Version"='2016/12';


--CONTROL PROCESO

insert into "BSCL"."BSCL.Normativa.BD::TRI.ControlProceso"
("Esquema","Version","EstadoCertificado","Carga","Validacion","PreDeclaracion","Certificacion")
SELECT "Esquema","Version",'NGE','N','N','N','N' FROM "BSCL"."BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1820' AND "Version" IN ('2020/01','2020/02','2020/03','2020/04','2020/05','2020/06','2020/07','2020/08','2020/09','2020/10','2020/11','2020/12');


--Limpiar la version BASE        

DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura"  WHERE "Esquema"='DJ1820' AND "Version"='2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"  WHERE "Esquema"='DJ1820'AND "Version"='2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1820' AND "Version" = '2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema"='DJ1820' AND "Version"='2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1820' AND "Version"='2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Documento" where "Esquema" = 'DJ1820' and "Version"='2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1820' AND "Version"='2016/12';

DELETE FROM "BSCL"."BSCL.Normativa.BD::TRI.ControlProceso" WHERE  "Esquema" = 'DJ1820' AND "Version"='2016/12';


-- Actualiza Carga Externa 2020

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=0, "Exportable"='N'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='IdCartera';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=0, "Exportable"='N'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='ID';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=0, "Exportable"='N'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='IdDocumento';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=0, "Exportable"='N'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='VboUsuario';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=0, "Exportable"='N'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='Observaciones';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=0, "Exportable"='N'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='FuenteDatos';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=0, "Exportable"='N'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='IdCliente';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=0, "Exportable"='N'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='IdPortafolio';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=1, "Exportable"='S'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='RutCliente';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=2, "Exportable"='S'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='TaxID';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=3, "Exportable"='S'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='CodigoPaisResidencia';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=4, "Exportable"='S'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='CodigoRelacion';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=5, "Exportable"='S'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='CodigoModoContrato';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=6, "Exportable"='S'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='TipoAcuerdoMarco';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=7, "Exportable"='S'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='NumeroAcuerdoMarco';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=8, "Exportable"='S'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='FechaSuscripcionAcuerdo';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=9, "Exportable"='S'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='NumeroContrato';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=10, "Exportable"='S'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='FechaSuscripcionContrato';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=11, "Exportable"='S'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='TipoEvento';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=12, "Exportable"='S'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='TipoContrato';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=13, "Exportable"='S'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='Instrumento';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=14, "Exportable"='S'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='ModalidadCumplimiento';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=15, "Exportable"='S'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='CodigoPosicionDeclarante';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=16, "Exportable"='S'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='TipoActivoSubyacente';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=17, "Exportable"='S'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='CodigoActivoSubyacente';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=18, "Exportable"='S'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='OtroActivoSubyacente';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=19, "Exportable"='S'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='TasaActivoSubyacente';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=20, "Exportable"='S'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='TipoSegundoActivoSubyacente';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=21, "Exportable"='S'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='CodigoSegundoActivoSubyacente';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=22, "Exportable"='S'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='OtroSegundoActivoSubyacente';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=23, "Exportable"='S'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='TasaSegundoActivoSubyacente';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=24, "Exportable"='S'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='CodigoPrecioFuturo';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=25, "Exportable"='S', "TipoDato" = 'DECIMAL',  "CantidadDecimales" = 2 
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='PrecioFuturo';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=26, "Exportable"='S'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='CodigoMonedaFuturo';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=27, "Exportable"='S'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='CodigoUnidad';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=28, "Exportable"='S'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='MontoNocional';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=29, "Exportable"='S'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='CodigoSegundaUnidad';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=30, "Exportable"='S'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='SegundoMontoNocional';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=31, "Exportable"='S'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='FechaVencimiento';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=32, "Exportable"='S'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='FechaOperacion';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "OrdenCargaExterna"=33, "Exportable"='S'
WHERE "Esquema"='DJ1820' AND "Version" LIKE '2020/%' AND "Nombre"='FolioOperacion';



UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "ModoMasivo"='S'
WHERE "Esquema"='DJ1820' AND "Nombre" IN ('TaxID', 'CodigoRelacion', 'FechaSuscripcionAcuerdo', 'TipoAcuerdoMarco');

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "Obligatorio"='N'
WHERE "Esquema"='DJ1820' AND "Nombre" 'FechaSuscripcionAcuerdo';


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4290664, 'BSCL.Normativa.BD.Servicios::TRI.DJ1820.GrabarMasivo', 1, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4290664);


COMMIT;