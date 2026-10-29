SET SCHEMA "BSCL";

/*Inscripcion en pauta (solo respaldo) */
/*
INSERT INTO BSCL."BSCL.Plataforma.BD::PAU.Proceso"
("IdEmpresa", "CodigoProceso", "DescripcionProceso", "CodigoModulo", "EsApertura", "EsCierre", "IdEstado", "EsTipoPrograma", "Orden", "NombreClase", "DetienePauta", "OrdenFinDeMesInhabil", "EjecutarFinDeMesInhabil", "Notifica")
VALUES(1, 'GENERAR1064PAUTA_R', 'Generacion Circular 1064 - BCS en Rueda', 'SIS', 'N', 'S', 'ACT', 'S', 2, 'cl.bcs.plataforma.jobs.PautaGenerar1064R', 'N', 0, 'N', 'N');

INSERT INTO BSCL."BSCL.Plataforma.BD::PAU.Proceso"
("IdEmpresa", "CodigoProceso", "DescripcionProceso", "CodigoModulo", "EsApertura", "EsCierre", "IdEstado", "EsTipoPrograma", "Orden", "NombreClase", "DetienePauta", "OrdenFinDeMesInhabil", "EjecutarFinDeMesInhabil", "Notifica")
VALUES(1, 'GENERAR1064PAUTA_FR', 'Generacion Circular 1064 - BCS Fuera de Rueda', 'SIS', 'N', 'S', 'ACT', 'S', 2, 'cl.bcs.plataforma.jobs.PautaGenerar1064FR', 'N', 0, 'N', 'N');
*/

/*Deshabilitar*/
/*
SET SCHEMA "BSCL";
UPDATE BSCL."BSCL.Plataforma.BD::BAT.Agenda"
SET "habilitado"='N'
WHERE "IdTipoPrograma"='GENERAR1064PAUTA_R';

UPDATE BSCL."BSCL.Plataforma.BD::BAT.Agenda"
SET "habilitado"='N'
WHERE "IdTipoPrograma"='GENERAR1064PAUTA_FR';
 */

/*Habilitar*/
/*
SET SCHEMA "BSCL";
UPDATE BSCL."BSCL.Plataforma.BD::BAT.Agenda"
SET "habilitado"='S'
WHERE "IdTipoPrograma"='GENERAR1064PAUTA_R';

UPDATE BSCL."BSCL.Plataforma.BD::BAT.Agenda"
SET "habilitado"='S'
WHERE "IdTipoPrograma"='GENERAR1064PAUTA_FR';
 */


/*Tablas*/

SET SCHEMA "BSCL";
DROP TABLE  "BSCL.Normativa.BD::GBL.SALIDA_CIRCULAR1064";
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Normativa.BD::GBL.SALIDA_CIRCULAR1064"(
		"IdInterno"					BIGINT
		,"EsCtaPropia"				NVARCHAR(1) DEFAULT '' NULL
		,"Uso"         				NVARCHAR(10)    DEFAULT '' NULL
		,"RutIntermediario"			NVARCHAR(40) DEFAULT '' NULL
		,"DVIntermediario"			NVARCHAR(1) DEFAULT '' NULL
		,"CodigoIntermediario"		NVARCHAR(3) DEFAULT '' NULL
		,"RazonSocialIntermediario"	NVARCHAR(100) DEFAULT '' NULL
		,"FechaTransaccionSistema"	DATETIME DEFAULT NULL
		,"FechaTransaccion"			NVARCHAR(8) DEFAULT '' NULL
		,"HoraTransaccion"			NVARCHAR(8) DEFAULT '' NULL
		,"Mercado"					NVARCHAR(3) DEFAULT '' NULL
		,"SistemaTransaccion"		NVARCHAR(5) DEFAULT '' NULL
		,"Bolsa"					NVARCHAR(3) DEFAULT '' NULL
		,"Folio"					NVARCHAR(40) DEFAULT '' NULL
		,"FolioFR"					NVARCHAR(40) DEFAULT '' NULL
		,"Secuencia"				NVARCHAR(3) DEFAULT '' NULL
		,"RutContraparte"			NVARCHAR(40) DEFAULT '' NULL
		,"DVContraparte"			NVARCHAR(1) DEFAULT '' NULL
		,"CodigoContraparte"		NVARCHAR(3) DEFAULT '' NULL
		,"NumeroOrden"				NVARCHAR(40) DEFAULT '' NULL
		,"UltimosDigitosNumeroOrden" NVARCHAR(40) DEFAULT '' NULL
		,"Nemotecnico"				NVARCHAR(100) DEFAULT '' NULL
		,"FechaVencimientoSistema"	DATETIME DEFAULT NULL
		,"FechaVencimiento"			NVARCHAR(8) DEFAULT '' NULL
		,"IdTipoInstrumento"		NVARCHAR(3) DEFAULT '' NULL
		,"HIdTipoInstrumento"		NVARCHAR(3) DEFAULT '' NULL
		,"IdTipoOperacion"			NVARCHAR(6) DEFAULT '' NULL
		,"HIdTipoOperacion"			NVARCHAR(6) DEFAULT '' NULL
		,"TipoOperacion1064"		NVARCHAR(7) DEFAULT '' NULL
		,"Persona"					NVARCHAR(1) DEFAULT '' NULL
		,"Plazo"					NVARCHAR(5) DEFAULT '' NULL
		,"IdCondicionLiquidacion"	NVARCHAR(3) DEFAULT '' NULL
		,"TipoCliente"				NVARCHAR(1) DEFAULT '' NULL
		,"UnidadesTransadasSistema"	DECIMAL(27,8) DEFAULT 0 NULL
		,"UnidadesTransadas"		NVARCHAR(40) DEFAULT '' NULL
		,"PrecioTransaccionSistema"	DECIMAL(27,8)	DEFAULT 0 NULL
		,"PrecioTransaccion"		NVARCHAR(40) DEFAULT '' NULL
		,"TotalTransadoSistema"		DECIMAL(27,8)	DEFAULT 0 NULL
		,"TotalTransado"			NVARCHAR(40) DEFAULT '' NULL
		,"TipoUnidad"				NVARCHAR(3) DEFAULT '' NULL
		,"TipoDolar"				NVARCHAR(4) DEFAULT '' NULL
		,"UnidadReajusteTransaccion"	NVARCHAR(3) DEFAULT '' NULL
		,"NumeroContratoFuturo"		NVARCHAR(40) DEFAULT '' NULL
		,"ValorParSistema"			DECIMAL(27,8)	DEFAULT 0 NULL
		,"ValorPar"					NVARCHAR(40) DEFAULT '' NULL
		,"PorcentajeValorPar"		NVARCHAR(40) DEFAULT '' NULL
		,"TIRSistema"				DECIMAL(27,8)	DEFAULT 0 NULL
		,"TIR"						NVARCHAR(40) DEFAULT '' NULL
		,"RentabilidadSistema"		DECIMAL(27,8)	DEFAULT 0 NULL
		,"Rentabilidad"				NVARCHAR(40) DEFAULT '' NULL
		,"TipoReajustePacto"		NVARCHAR(3) DEFAULT '' NULL
		,"CodObservacion"			NVARCHAR(2) DEFAULT '' NULL
		,"CodModificacion"			NVARCHAR(2) DEFAULT '' NULL
		,"FolioModificacion"		NVARCHAR(12) DEFAULT '' NULL
		,"EspaciosSVS"				NVARCHAR(18) DEFAULT '' NULL
		,"NacionalidadCliente"		NVARCHAR(100) DEFAULT '' NULL
		,"RutCliente"				NVARCHAR(40) DEFAULT '' NULL
		,"DVCliente"				NVARCHAR(1) DEFAULT '' NULL
		,"NombreCliente"			NVARCHAR(100) DEFAULT '' NULL
		,"NumeroFactura"			NVARCHAR(40) DEFAULT '' NULL
		,"RutAutorizado"			NVARCHAR(40) DEFAULT '' NULL
		,"DVAutorizado"			    NVARCHAR(1) DEFAULT '' NULL
		,"NombreAutorizado"			NVARCHAR(100) DEFAULT '' NULL
		,"IdClasificacionTipoInstrumento" NVARCHAR(6) DEFAULT '' NULL
		,"CodigoOperador"			NVARCHAR(100)  DEFAULT '' NULL
		,"IdMoneda"					NVARCHAR(3) DEFAULT '' NULL
		,"EsEnRueda"				NVARCHAR(1) DEFAULT '' NULL
		,"IdCliente"				BIGINT 		DEFAULT 0 NULL
		,"IdPortafolio"				BIGINT 		DEFAULT 0 NULL
		,"AccionOperacion"			NVARCHAR(40)  DEFAULT '' NULL
		,"IdEmisor"					NVARCHAR(40)  DEFAULT '' NULL
) ON COMMIT DELETE ROWS;






SET SCHEMA "BSCL"; 
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_CIRCULAR1064RUEDA";
CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_CIRCULAR1064RUEDA" AS TABLE
	(
		"IdInterno"					BIGINT
		,"EsCtaPropia"				NVARCHAR(1) DEFAULT '' NULL
		,"Uso"         				NVARCHAR(10)    DEFAULT '' NULL
		,"RutIntermediario"			NVARCHAR(40) DEFAULT '' NULL
		,"CodigoIntermediario"		NVARCHAR(3) DEFAULT '' NULL
		,"RazonSocialIntermediario"	NVARCHAR(100) DEFAULT '' NULL
		,"FechaTransaccionSistema"	DATETIME DEFAULT NULL
		,"FechaTransaccion"			NVARCHAR(10) DEFAULT '' NULL
		,"HoraTransaccion"			NVARCHAR(8) DEFAULT '' NULL
		,"Mercado"					NVARCHAR(3) DEFAULT '' NULL
		,"SistemaTransaccion"		NVARCHAR(5) DEFAULT '' NULL
		,"Bolsa"					NVARCHAR(3) DEFAULT '' NULL
		,"Folio"					NVARCHAR(40) DEFAULT '' NULL
		,"Secuencia"				NVARCHAR(3) DEFAULT '' NULL
		,"RutContraparte"			NVARCHAR(40) DEFAULT '' NULL
		,"DVContraparte"			NVARCHAR(1) DEFAULT '' NULL
		,"CodigoContraparte"		NVARCHAR(5) DEFAULT '' NULL
		,"NumeroOrden"				NVARCHAR(40) DEFAULT '' NULL
		,"UltimosDigitosNumeroOrden" NVARCHAR(40) DEFAULT '' NULL
		,"Nemotecnico"				NVARCHAR(100) DEFAULT '' NULL
		,"FechaVencimientoSistema"	DATETIME DEFAULT NULL
		,"FechaVencimiento"			NVARCHAR(8) DEFAULT '' NULL
		,"IdTipoInstrumento"		NVARCHAR(3) DEFAULT '' NULL
		,"HIdTipoInstrumento"		NVARCHAR(3) DEFAULT '' NULL
		,"IdTipoInstrumentoRueda"	NVARCHAR(3) DEFAULT '' NULL
		,"IdTipoOperacion"			NVARCHAR(6) DEFAULT '' NULL
		,"HIdTipoOperacion"			NVARCHAR(6) DEFAULT '' NULL
		,"Persona"					NVARCHAR(1) DEFAULT '' NULL
		,"Plazo"					NVARCHAR(5) DEFAULT '' NULL
		,"IdCondicionLiquidacion"	NVARCHAR(3) DEFAULT '' NULL
		,"TipoCliente"				NVARCHAR(1) DEFAULT '' NULL
		,"UnidadesTransadasSistema"	DECIMAL(27,8) DEFAULT 0 NULL
		,"UnidadesTransadas"		NVARCHAR(40) DEFAULT '' NULL
		,"PrecioTransaccionSistema"	DECIMAL(27,8)	DEFAULT 0 NULL
		,"PrecioTransaccion"		NVARCHAR(40) DEFAULT '' NULL
		,"TotalTransadoSistema"		DECIMAL(27,8)	DEFAULT 0 NULL
		,"TotalTransado"			NVARCHAR(40) DEFAULT '' NULL
		,"TipoUnidad"				NVARCHAR(3) DEFAULT '' NULL
		,"TipoDolar"				NVARCHAR(4) DEFAULT '' NULL
		,"UnidadReajusteTransaccion"	NVARCHAR(3) DEFAULT '' NULL
		,"NumeroContratoFuturo"		NVARCHAR(40) DEFAULT '' NULL
		,"ValorParSistema"			DECIMAL(27,8)	DEFAULT 0 NULL
		,"ValorPar"					NVARCHAR(40) DEFAULT '' NULL
		,"PorcentajeValorPar"		NVARCHAR(40) DEFAULT '' NULL
		,"TIRSistema"				DECIMAL(27,8)	DEFAULT 0 NULL
		,"TIR"						NVARCHAR(40) DEFAULT '' NULL
		,"RentabilidadSistema"		DECIMAL(27,8)	DEFAULT 0 NULL
		,"Rentabilidad"				NVARCHAR(40) DEFAULT '' NULL
		,"TipoReajustePacto"		NVARCHAR(3) DEFAULT '' NULL
		,"CodObservacion"			NVARCHAR(2) DEFAULT '' NULL
		,"CodModificacion"			NVARCHAR(2) DEFAULT '' NULL
		,"FolioModificacion"		NVARCHAR(12) DEFAULT '' NULL
		,"EspaciosSVS"				NVARCHAR(18) DEFAULT '' NULL
		,"NacionalidadCliente"		NVARCHAR(100) DEFAULT '' NULL
		,"RutCliente"				NVARCHAR(40) DEFAULT '' NULL
		--,"DVCliente"				NVARCHAR(1) DEFAULT '' NULL
		,"CodigoPortafolio" 		NVARCHAR(100) DEFAULT '' NULL
		,"NombreCliente"			NVARCHAR(100) DEFAULT '' NULL
		,"NumeroFactura"			NVARCHAR(40) DEFAULT '' NULL
		,"RutAutorizado"			NVARCHAR(40) DEFAULT '' NULL
		,"DVAutorizado"			    NVARCHAR(1) DEFAULT '' NULL
		,"NombreAutorizado"			NVARCHAR(100) DEFAULT '' NULL
		,"IdClasificacionTipoInstrumento" NVARCHAR(6) DEFAULT '' NULL
		,"CodigoOperador"			NVARCHAR(100)  DEFAULT '' NULL
		,"IdMoneda"					NVARCHAR(3) DEFAULT '' NULL
		,"Rueda"					NVARCHAR(1) DEFAULT '' NULL
		,"TipoOperacionRueda"		NVARCHAR(2) DEFAULT '' NULL
		
	);


	
DELETE FROM "BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema"='CIR1064R';
	
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064R', 'V1', 'FechaTransaccion', '', '', 1, 'Fecha Transaccion', 'Ayuda de Fecha Transaccion', 'TEXT', 10, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064R', 'V1', 'CodigoIntermediario', '', '', 2, 'Codigo Intermediario', 'Ayuda de Codigo Intermediario', 'TEXT', 3, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064R', 'V1', 'RazonSocialIntermediario', '', '', 3, 'Razon Social Intermediario', 'Ayuda de Razon Social Intermediario', 'TEXT', 10, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064R', 'V1', 'RutIntermediario', '', '', 4, 'Rut Intermediario', 'Ayuda de Rut Intermediario', 'TEXT', 11, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064R', 'V1', 'RutCliente', '', '', 5, 'Rut Intermediario', 'Ayuda de Rut Intermediario', 'TEXT', 11, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064R', 'V1', 'CodigoPortafolio', '', '', 6, 'Codigo Portafolio', 'Ayuda de Codigo Portafolio', 'TEXT', 2, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064R', 'V1', 'Nemotecnico', '', '', 7, 'Nemotecnico', 'Ayuda de Nemotecnico', 'TEXT', 10, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064R', 'V1', 'IdTipoInstrumentoRueda', '', '', 8, 'Id Tipo Instrumento Rueda', 'Ayuda de Id Tipo Instrumento Rueda', 'TEXT', 2, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064R', 'V1', 'Rueda', '', '', 9, 'Rueda', 'Ayuda de Rueda', 'TEXT', 1, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064R', 'V1', 'TipoCliente', '', '', 10, 'Tipo Cliente', 'Ayuda de Tipo Cliente', 'TEXT', 1, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064R', 'V1', 'TipoOperacionRueda', '', '', 11, 'Tipo Operacion Homologo BCS', 'Ayuda de Tipo Operacion 1064', 'TEXT', 2, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064R', 'V1', 'Folio', '', '', 12, 'Folio', 'Ayuda de Folio', 'ID', 8, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064R', 'V1', 'UnidadesTransadas', '', '', 13, 'Unidades Transadas', 'Ayuda de Unidades Transadas', 'TEXT', 15, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 4);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064R', 'V1', 'TotalTransado', '', '', 14, 'Total', 'Ayuda de Total', 'TEXT', 15, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 4);
	
	

/*==============================================================*/
/*                       Homologacion                           */
/*==============================================================*/
DELETE FROM  "BSCL.Normativa.BD::DOC.Homologacion"  WHERE "Esquema"='CIR1064R' AND "Version"='V1';


---BOLSA

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064R','V1','BOLSA','BOLSA DE COMERCIO DE SANTIAGO','BCS','01','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064R','V1','BOLSA','FUERA DE RUEDA','FRD','00','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064R','V1','BOLSA','BOLSA ELECTRONICA','BEC','03','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064R','V1','BOLSA','BOLSA DE COMERCIO DE VALPARAISO','BCV','02','ACT');
  

---CONDICION LIQUIDACION

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064R','V1','CONDICIONLIQUIDACION','PAGADERO HOY','TO','PH','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064R','V1','CONDICIONLIQUIDACION','PAGADERO MAÃ‘ANA','T1','PM','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064R','V1','CONDICIONLIQUIDACION','PAGADERO NORMAL','T2','CN','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064R','V1','CONDICIONLIQUIDACION','TRANSACCION A PLAZO','TP','TP','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064R','V1','CONDICIONLIQUIDACION','T3 (NO INFORMA)','T3','TP','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064R','V1','CONDICIONLIQUIDACION','T4 (NO INFORMA)','T4','TP','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064R','V1','CONDICIONLIQUIDACION','T5 (NO INFORMA)','T5','TP','ACT'); 
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064R','V1','CONDICIONLIQUIDACION','T6 (NO INFORMA)','T6','TP','ACT'); 
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064R','V1','CONDICIONLIQUIDACION','T7 (NO INFORMA)','T7','TP','ACT');  


---SistemaTransaccion

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064R','V1','SISTEMABURSATIL','BCS TELEPREGON','BCSTELEPREGON','RUED','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064R','V1','SISTEMABURSATIL','INGRESO MANUAL','MANUAL','RUED','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064R','V1','SISTEMABURSATIL','BCS TELERENTA','BCSTELERENTA','RUED','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064R','V1','SISTEMABURSATIL','BCS REMATE SERIALIZADO','BCSREMATESERIALIZADO','REMT','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064R','V1','SISTEMABURSATIL','BCS REMATE HOLANDES','BCSREMATEHOLANDES','REMT','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064R','V1','SISTEMABURSATIL','BCS REMATE ELECTRONICO','BCSREMATEELECTRONICO','REMT','ACT');


---Relacion 1064

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064R','V1','RELACION','VINCULADO POR PROPIEDAD','PROPIEDAD','P','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064R','V1','RELACION','VINCULADO LABORALMENTE','LABORAL','L','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064R','V1','RELACION','VINCULADO POR ADMINISTRACION','ADMINISTRACION','A','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064R','V1','RELACION','NO VINCULADO','NOVINCULADO','N','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064R','V1','RELACION','NO CORRESPONDE INFORMAR','NOINFORMAR','X','ACT');

---TIPO UNIDAD

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064R','V1','TIPOUNIDAD','Peso Chileno','CLP','$$','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064R','V1','TIPOUNIDAD','Unidad De Fomento','UF','UF','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064R','V1','TIPOUNIDAD','DÃ³lar de los Estados Unidos','USD','US','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064R','V1','TIPOUNIDAD','Euro','EUR','EU','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064R','V1','TIPOUNIDAD','Otras Monedas','','OT','ACT');



INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CIR1064R', 'V1', 'TIPOINSTRUMENTORUEDA', 'ACCIONES', 'AC', '03', 'ACT');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CIR1064R', 'V1', 'TIPOINSTRUMENTORUEDA', 'MONEDA EXTRANJERA NO DOLAR', 'MO', '00', 'ACT');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CIR1064R', 'V1', 'TIPOINSTRUMENTORUEDA', 'CONTRATO DE FUTURO', 'FU', '07', 'ACT');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CIR1064R', 'V1', 'TIPOINSTRUMENTORUEDA', 'OPCIONES', 'OP', '00', 'ACT');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CIR1064R', 'V1', 'TIPOINSTRUMENTORUEDA', 'CUOTAS DE FONDOS', 'CF', '08', 'ACT');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CIR1064R', 'V1', 'TIPOINSTRUMENTORUEDA', 'RENTA FIJA', 'RF', '02', 'ACT');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CIR1064R', 'V1', 'TIPOINSTRUMENTORUEDA', 'INTERMEDICION FINANCIERA', 'IF', '04', 'ACT');




/* Copia de 1064 a TANDEM */

 	 
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Parametro"
("Esquema", "Version", "Codigo", "Descripcion", "Valor", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones", "IdEmpresa")
SELECT 'CIR1064R', 'V1', 'NOMBRE_ENVIO_TANDEM_SVS', 'Nombre archivo enviado a ruta Tadem SVS ', 'SV[CODCORREDOR][MESTRAN][DIATRAN]','N','','SV[CODCORREDOR][MESTRAN][DIATRAN]', 'texto libre o vacio [CODCORREDOR] [MESTRAN] [DIATRAN]', '','', 'CFLORES ',  CURRENT_UTCTIMESTAMP, ' ', CURRENT_UTCTIMESTAMP, ' ', 1
FROM dummy WHERE 
 NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::DOC.Parametro" N WHERE N."Esquema"='CIR1064R' AND N."Codigo"='NOMBRE_ENVIO_TANDEM_SVS');
			
 DELETE FROM BSCL."BSCL.Normativa.BD::DOC.Parametro" N WHERE N."Esquema"='CIR1064FR' AND N."Codigo"='NOMBRE_ENVIO_TANDEM_SVS';
 
 INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Parametro"
("Esquema", "Version", "Codigo", "Descripcion", "Valor", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones", "IdEmpresa")
SELECT 'CIR1064FR', 'V1', 'NOMBRE_ENVIO_TANDEM_SVS', 'Nombre archivo enviado a ruta Tadem SVS ', 'FR[CODCORREDOR][MESTRAN][DIATRAN]','N','','SF[CODCORREDOR][MESTRAN][DIATRAN]', 'texto libre o vacio [CODCORREDOR] [MESTRAN] [DIATRAN]', '','', 'CFLORES ',  CURRENT_UTCTIMESTAMP, ' ', CURRENT_UTCTIMESTAMP, ' ', 1
FROM dummy WHERE 
 NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::DOC.Parametro" N WHERE N."Esquema"='CIR1064FR' AND N."Codigo"='NOMBRE_ENVIO_TANDEM_SVS');
			
 INSERT INTO BSCL."BSCL.Normativa.BD::NOR.TipoParametros"
("TipoParametro", "Entidad", "Nombre", "EsMasivo", "EsInterno")
SELECT 'NOMBRE_ENVIO_TANDEM_SVS', 'SOLICITUD', 'Nombre de envio a Tandem', 'N', 'N'
FROM dummy WHERE 
 NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::NOR.TipoParametros" N WHERE N."TipoParametro"='NOMBRE_ENVIO_TANDEM_SVS');
	
 
/*PRODUCTOS*/
 
 DELETE FROM "BSCL.Normativa.BD::NOR.Producto" WHERE "IdNormativa"='CIR1064';

/** DERIVADOS **/
INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Producto"
("IdNormativa", "IdTipoDocumento", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion", "IdEstado", "IdConcepto", "Uso", "Origen", "HIdTipoProducto", "HIdClasificacionTipoInstrumento", "HIdTipoInstrumento", "HIdTipoOperacion")
SELECT 'CIR1064' AS "IdNormativa",
'' AS "IdTipoDocumento",
"O"."IdTipoProducto",
"O"."IdClasificacionTipoInstrumento",
"O"."IdTipoInstrumento",
"O"."IdTipoOperacion",
'ACT' AS "IdEstado",
'' AS "IdConcepto",
'' AS "Uso",
'' AS "Origen",
"O"."IdTipoProducto" AS "HIdTipoProducto",
"O"."IdClasificacionTipoInstrumento" AS "HIdClasificacionTipoInstrumento",
 LEFT("T"."Nombre",2) AS "HIdTipoInstrumento",
"O"."IdTipoOperacion"  AS "HIdTipoOperacion"
 FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" AS "O"
 LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS "T" ON "T"."IdTipoInstrumento" = "O"."IdTipoInstrumento"
  WHERE "O"."EstadoTipoOperacionTipoInstrumento"='ACT' AND "O"."IdClasificacionTipoInstrumento" IS NOT NULL AND "O"."EsTransaccion"='S'
AND "O"."IdClasificacionTipoInstrumento"='DV' AND LEFT("T"."Nombre",2) IN ('FU','OP');



/** ACCIONES **/ /** MONETARIOS **/ /** CUOTAS DE FONDOS **/
INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Producto"
("IdNormativa", "IdTipoDocumento", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion", "IdEstado", "IdConcepto", "Uso", "Origen", "HIdTipoProducto", "HIdClasificacionTipoInstrumento", "HIdTipoInstrumento", "HIdTipoOperacion")
SELECT 'CIR1064' AS "IdNormativa",
'' AS "IdTipoDocumento",
"IdTipoProducto",
"IdClasificacionTipoInstrumento",
"IdTipoInstrumento",
"IdTipoOperacion",
'ACT' AS "IdEstado",
'' AS "IdConcepto",
'' AS "Uso",
'' AS "Origen",
"IdTipoProducto" AS "HIdTipoProducto",
"IdClasificacionTipoInstrumento" AS "HIdClasificacionTipoInstrumento",
 LEFT("IdTipoInstrumento",2) AS "HIdTipoInstrumento",
"IdTipoOperacion"  AS "HIdTipoOperacion"
 FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" WHERE "EstadoTipoOperacionTipoInstrumento"='ACT' AND "IdClasificacionTipoInstrumento" IS NOT NULL AND "EsTransaccion"='S'
AND "IdClasificacionTipoInstrumento"='RV'; 


/** RENTA FIJA , IIF**/
INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Producto"
("IdNormativa", "IdTipoDocumento", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion", "IdEstado", "IdConcepto", "Uso", "Origen", "HIdTipoProducto", "HIdClasificacionTipoInstrumento", "HIdTipoInstrumento", "HIdTipoOperacion")
SELECT 'CIR1064' AS "IdNormativa",
'' AS "IdTipoDocumento",
"IdTipoProducto",
"IdClasificacionTipoInstrumento",
"IdTipoInstrumento",
"IdTipoOperacion",
'ACT' AS "IdEstado",
'' AS "IdConcepto",
'' AS "Uso",
'' AS "Origen",
"IdTipoProducto" AS "HIdTipoProducto",
"IdClasificacionTipoInstrumento" AS "HIdClasificacionTipoInstrumento",
 "IdClasificacionTipoInstrumento" AS "HIdTipoInstrumento",
"IdTipoOperacion"  AS "HIdTipoOperacion"
 FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" WHERE "EstadoTipoOperacionTipoInstrumento"='ACT' AND "IdClasificacionTipoInstrumento" IS NOT NULL --AND "EsTransaccion"='S'
AND "IdClasificacionTipoInstrumento" IN ('RF','IF'); 


/** OTROS**/
INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Producto"
("IdNormativa", "IdTipoDocumento", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdTipoOperacion", "IdEstado", "IdConcepto", "Uso", "Origen", "HIdTipoProducto", "HIdClasificacionTipoInstrumento", "HIdTipoInstrumento", "HIdTipoOperacion")
SELECT 'CIR1064' AS "IdNormativa",
'' AS "IdTipoDocumento",
"IdTipoProducto",
"IdClasificacionTipoInstrumento",
"IdTipoInstrumento",
"IdTipoOperacion",
'ACT' AS "IdEstado",
'' AS "IdConcepto",
'' AS "Uso",
'' AS "Origen",
"IdTipoProducto" AS "HIdTipoProducto",
"IdClasificacionTipoInstrumento" AS "HIdClasificacionTipoInstrumento",
 "IdClasificacionTipoInstrumento" AS "HIdTipoInstrumento",
"IdTipoOperacion"  AS "HIdTipoOperacion"
 FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" WHERE "EstadoTipoOperacionTipoInstrumento"='ACT' AND "IdClasificacionTipoInstrumento" IS NOT NULL --AND "EsTransaccion"='S'
AND "IdClasificacionTipoInstrumento" NOT IN ('RF','IF','RV','DV') ;


UPDATE BSCL."BSCL.Normativa.BD::DOC.TipoDocumento" SET "UI_Emision"='N' WHERE "IdNormativa"='CIR1064' AND "IdTipoDocumento"='CIR1064';