SET SCHEMA "BSCL";



/*Tablas*/
SET SCHEMA "BSCL";
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_CIRCULAR1064FUERARUEDA" CASCADE;
CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_CIRCULAR1064FUERARUEDA" AS TABLE
	(
		 "IdInterno"					BIGINT
		,"CodigoCorredor"			NVARCHAR(3) DEFAULT '' NULL
		,"FechaInformacion"			NVARCHAR(6) DEFAULT '' NULL
		,"Disponible1"				NVARCHAR(200) DEFAULT '' NULL
		,"EsCtaPropia"				NVARCHAR(1) DEFAULT '' NULL
		,"Uso"         				NVARCHAR(10)    DEFAULT '' NULL
		,"RutIntermediario"			NVARCHAR(40) DEFAULT '' NULL
		,"DVIntermediario"			NVARCHAR(1) DEFAULT '' NULL
		,"CodigoIntermediario"		NVARCHAR(5) DEFAULT '' NULL
		,"FechaTransaccionSistema"	DATETIME DEFAULT NULL
		,"FechaTransaccion"			NVARCHAR(8) DEFAULT '' NULL
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
		,"IdTipoOperacion"			NVARCHAR(6) DEFAULT '' NULL
		,"HIdTipoOperacion"			NVARCHAR(6) DEFAULT '' NULL
		,"TipoOperacion1064"		NVARCHAR(7) DEFAULT '' NULL
		,"Persona"					NVARCHAR(1) DEFAULT '' NULL
		,"Plazo"					NVARCHAR(5) DEFAULT '' NULL
		,"PlazoPacto"				NVARCHAR(5) DEFAULT '' NULL
		,"IdCondicionLiquidacion"	NVARCHAR(3) DEFAULT '' NULL
		,"TipoCliente"				NVARCHAR(2) DEFAULT '' NULL
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
		,"Disponible2"				NVARCHAR(200) DEFAULT '' NULL
		,"Disponible3"				NVARCHAR(200) DEFAULT '' NULL
		,"IdEmisor"					NVARCHAR(40)  DEFAULT '' NULL
		,"TipoTransaccion"			NVARCHAR(1)  DEFAULT '' NULL
		,"TipoOperacionRueda"		NVARCHAR(1)  DEFAULT '' NULL
		,"TipoOperacionRueda2"		NVARCHAR(1)  DEFAULT '' NULL
		,"TipoRentabilidad"			NVARCHAR(5)  DEFAULT '' NULL
	);
	
	
	

	
DELETE FROM "BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema"='CIR1064FR';
	
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064FR', 'V1', 'CodigoCorredor', '', '', 1, 'Codigo Corredor', 'Ayuda de Codigo Corredor', 'TEXT', 3, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064FR', 'V1', 'FechaInformacion', '', '', 2, 'Fecha Informacion', 'Ayuda de Fecha Informacion', 'TEXT', 6, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064FR', 'V1', 'Disponible1', '', '', 3, 'Disponible1', 'Ayuda de Disponible1', 'TEXT', 109, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064FR', 'V1', 'CodigoIntermediario', '', '', 4, 'Codigo Intermediario', 'Ayuda de Codigo Intermediario', 'TEXT', 5, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064FR', 'V1', 'TipoTransaccion', '', '', 5, 'Tipo Transaccion', 'Ayuda de Tipo Transaccion', 'TEXT', 1, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064FR', 'V1', 'CodigoContraparte', '', '', 6, 'Codigo Contraparte', 'Ayuda de Codigo Contraparte', 'TEXT', 5, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064FR', 'V1', 'UnidadesTransadas', '', '', 7, 'Unidades Transadas', 'Ayuda de Unidades Transadas', 'TEXT', 13, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 4);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064FR', 'V1', 'Nemotecnico', '', '', 8, 'Nemotecnico', 'Ayuda de Nemotecnico', 'TEXT', 11, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064FR', 'V1', 'Plazo', '', '', 9, 'Plazo', 'Ayuda de Plazo', 'TEXT', 4, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064FR', 'V1', 'TIR', '', '', 10, 'TIR', 'Ayuda de TIR', 'TEXT', 5, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 2);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064FR', 'V1', 'PrecioTransaccion', '', '', 11, 'Precio', 'Ayuda de Precio', 'TEXT', 5, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 4);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064FR', 'V1', 'IdCondicionLiquidacion', '', '', 12, 'Condicion Liquidacion', 'Ayuda de Condicion Liquidacion', 'TEXT', 2, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064FR', 'V1', 'Disponible2', '', '', 13, 'Disponible2', 'Ayuda de Disponible2', 'TEXT', 2, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064FR', 'V1', 'TipoOperacionRueda', '', '', 14, 'Tipo Operacion Rueda', 'Ayuda de Tipo Operacion Rueda', 'TEXT', 1, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064FR', 'V1', 'UnidadReajusteTransaccion', '', '', 15, 'Unidad Reajuste Transaccion', 'Ayuda de Unidad Reajuste Transaccion', 'TEXT', 2, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 4);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064FR', 'V1', 'IdEmisor', '', '', 16, 'IdEmisor', 'Ayuda de IdEmisor', 'TEXT', 10, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 4);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064FR', 'V1', 'Folio', '', '', 17, 'Folio', 'Ayuda de Folio', 'ID', 4, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064FR', 'V1', 'TipoOperacionRueda2', '', '', 18, 'Tipo Operacion Rueda', 'Ayuda de Tipo Operacion Rueda', 'TEXT', 1, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064FR', 'V1', 'PlazoPacto', '', '', 19, 'Plazo', 'Ayuda de Plazo', 'TEXT', 5, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064FR', 'V1', 'Rentabilidad', '', '', 20, 'Rentabilidad', 'Ayuda de Rentabilidad', 'TEXT', 5, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 2);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064FR', 'V1', 'TipoRentabilidad', '', '', 21, 'Rentabilidad', 'Ayuda de Rentabilidad', 'TEXT', 5, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 2);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064FR', 'V1', 'Bolsa', '', '', 22, 'Numero Bolsa', 'Ayuda de Numero Bolsa', 'ID', 2, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064FR', 'V1', 'TotalTransado', '', '', 23, 'Total', 'Ayuda de Total', 'TEXT', 13, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 4);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('CIR1064FR', 'V1', 'Disponible3', '', '', 24, 'Disponible3', 'Ayuda de Disponible3', 'TEXT', 10, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 4);
	
/*==============================================================*/
/*                       Homologacion                           */
/*==============================================================*/
DELETE FROM  "BSCL.Normativa.BD::DOC.Homologacion"  WHERE "Esquema"='CIR1064FR' AND "Version"='V1';


---BOLSA

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064FR','V1','BOLSA','BOLSA DE COMERCIO DE SANTIAGO','BCS','01','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064FR','V1','BOLSA','FUERA DE RUEDA','FRD','00','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064FR','V1','BOLSA','BOLSA ELECTRONICA','BEC','03','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064FR','V1','BOLSA','BOLSA DE COMERCIO DE VALPARAISO','BCV','02','ACT');
  

---CONDICION LIQUIDACION

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064FR','V1','CONDICIONLIQUIDACION','PAGADERO HOY','T0','PH','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064FR','V1','CONDICIONLIQUIDACION','PAGADERO MAÃ‘ANA','T1','PM','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064FR','V1','CONDICIONLIQUIDACION','PAGADERO NORMAL','T2','CN','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064FR','V1','CONDICIONLIQUIDACION','TRANSACCION A PLAZO','TP','TP','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064FR','V1','CONDICIONLIQUIDACION','T3 (NO INFORMA)','T3','TP','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064FR','V1','CONDICIONLIQUIDACION','T4 (NO INFORMA)','T4','TP','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064FR','V1','CONDICIONLIQUIDACION','T5 (NO INFORMA)','T5','TP','ACT'); 
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064FR','V1','CONDICIONLIQUIDACION','T6 (NO INFORMA)','T6','TP','ACT'); 
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064FR','V1','CONDICIONLIQUIDACION','T7 (NO INFORMA)','T7','TP','ACT');  


---SistemaTransaccion

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064FR','V1','SISTEMABURSATIL','BCS TELEPREGON','BCSTELEPREGON','RUED','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064FR','V1','SISTEMABURSATIL','INGRESO MANUAL','MANUAL','RUED','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064FR','V1','SISTEMABURSATIL','BCS TELERENTA','BCSTELERENTA','RUED','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064FR','V1','SISTEMABURSATIL','BCS REMATE SERIALIZADO','BCSREMATESERIALIZADO','REMT','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064FR','V1','SISTEMABURSATIL','BCS REMATE HOLANDES','BCSREMATEHOLANDES','REMT','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064FR','V1','SISTEMABURSATIL','BCS REMATE ELECTRONICO','BCSREMATEELECTRONICO','REMT','ACT');


---Relacion 1064

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064FR','V1','RELACION','VINCULADO POR PROPIEDAD','PROPIEDAD','P','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064FR','V1','RELACION','VINCULADO LABORALMENTE','LABORAL','L','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064FR','V1','RELACION','VINCULADO POR ADMINISTRACION','ADMINISTRACION','A','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064FR','V1','RELACION','NO VINCULADO','NOVINCULADO','N','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064FR','V1','RELACION','NO CORRESPONDE INFORMAR','NOINFORMAR','X','ACT');

---TIPO UNIDAD

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064FR','V1','TIPOUNIDAD','Peso Chileno','CLP','$$','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064FR','V1','TIPOUNIDAD','Unidad De Fomento','UF','UF','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064FR','V1','TIPOUNIDAD','DÃ³lar de los Estados Unidos','USD','US','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064FR','V1','TIPOUNIDAD','Euro','EUR','EU','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
 VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'CIR1064FR','V1','TIPOUNIDAD','Otras Monedas','','OT','ACT');



INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CIR1064FR', 'V1', 'TIPOINSTRUMENTORUEDA', 'ACCIONES', 'AC', '03', 'ACT');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CIR1064FR', 'V1', 'TIPOINSTRUMENTORUEDA', 'MONEDA EXTRANJERA NO DOLAR', 'MO', '00', 'ACT');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CIR1064FR', 'V1', 'TIPOINSTRUMENTORUEDA', 'CONTRATO DE FUTURO', 'FU', '07', 'ACT');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CIR1064FR', 'V1', 'TIPOINSTRUMENTORUEDA', 'OPCIONES', 'OP', '00', 'ACT');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CIR1064FR', 'V1', 'TIPOINSTRUMENTORUEDA', 'CUOTAS DE FONDOS', 'CF', '08', 'ACT');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CIR1064FR', 'V1', 'TIPOINSTRUMENTORUEDA', 'RENTA FIJA', 'RF', '02', 'ACT');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CIR1064FR', 'V1', 'TIPOINSTRUMENTORUEDA', 'INTERMEDICION FINANCIERA', 'IF', '04', 'ACT');

/*Procesos finales*/


/* Copia de 1064 a TANDEM */

DELETE FROM BSCL."BSCL.Normativa.BD::DOC.Parametro" N WHERE N."Esquema"='CIR1064R' AND N."Codigo"='NOMBRE_ENVIO_TANDEM_SVS';
 	 
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Parametro"
("Esquema", "Version", "Codigo", "Descripcion", "Valor", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones", "IdEmpresa")
SELECT 'CIR1064R', 'V1', 'NOMBRE_ENVIO_TANDEM_SVS', 'Nombre archivo enviado a ruta Tadem SVS ', 'SV[CODCORREDOR][MESTRAN][DIATRAN]','N','','SV[CODCORREDOR][MESTRAN][DIATRAN]', 'texto libre o vacio [CODCORREDOR] [MESTRAN] [DIATRAN]', '','', 'CFLORES ',  CURRENT_UTCTIMESTAMP, ' ', CURRENT_UTCTIMESTAMP, ' ', 1
FROM dummy WHERE 
 NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::DOC.Parametro" N WHERE N."Esquema"='CIR1064R' AND N."Codigo"='NOMBRE_ENVIO_TANDEM_SVS');
			
 DELETE FROM BSCL."BSCL.Normativa.BD::DOC.Parametro" N WHERE N."Esquema"='CIR1064FR' AND N."Codigo"='NOMBRE_ENVIO_TANDEM_SVS';
 
 INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Parametro"
("Esquema", "Version", "Codigo", "Descripcion", "Valor", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones", "IdEmpresa")
SELECT 'CIR1064FR', 'V1', 'NOMBRE_ENVIO_TANDEM_SVS', 'Nombre archivo enviado a ruta Tadem SVS ', 'FR[CODCORREDOR][MESTRAN][DIATRAN]','N','','FR[CODCORREDOR][MESTRAN][DIATRAN]', 'texto libre o vacio [CODCORREDOR] [MESTRAN] [DIATRAN]', '','', 'CFLORES ',  CURRENT_UTCTIMESTAMP, ' ', CURRENT_UTCTIMESTAMP, ' ', 1
FROM dummy WHERE 
 NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::DOC.Parametro" N WHERE N."Esquema"='CIR1064FR' AND N."Codigo"='NOMBRE_ENVIO_TANDEM_SVS');
			
 INSERT INTO BSCL."BSCL.Normativa.BD::NOR.TipoParametros"
("TipoParametro", "Entidad", "Nombre", "EsMasivo", "EsInterno")
SELECT 'NOMBRE_ENVIO_TANDEM_SVS', 'SOLICITUD', 'Nombre de envio a Tandem', 'N', 'N'
FROM dummy WHERE 
 NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::NOR.TipoParametros" N WHERE N."TipoParametro"='NOMBRE_ENVIO_TANDEM_SVS');
	
/* Copia de Informe de Derechos TP a Tandem */


 DELETE FROM BSCL."BSCL.Normativa.BD::DOC.Parametro" N WHERE N."Esquema"='INFDERECH' AND N."Codigo"='NOMBRE_ENVIO_TANDEM_SVS';
 DELETE FROM BSCL."BSCL.Normativa.BD::DOC.Parametro" N WHERE N."Esquema"='INFDERECH' AND N."Codigo"='NOMBRE_ENVIO_TANDEM_CUS';
 
 INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Parametro"
("Esquema", "Version", "Codigo", "Descripcion", "Valor", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones", "IdEmpresa")
SELECT 'INFDERECH', '2023', 'NOMBRE_ENVIO_TANDEM_CUS', 'Nombre archivo enviado a ruta Tadem CUS ', 'TP[CODCORREDOR][MESTRAN][DIATRAN]','N','','TP[CODCORREDOR][MESTRAN][DIATRAN]', 'texto libre o vacio [CODCORREDOR] [MESTRAN] [DIATRAN]', '','', 'CFLORES ',  CURRENT_UTCTIMESTAMP, ' ', CURRENT_UTCTIMESTAMP, ' ', 1
FROM dummy WHERE 
 NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::DOC.Parametro" N WHERE N."Esquema"='INFDERECH' AND N."Codigo"='NOMBRE_ENVIO_TANDEM_CUS');
			
 INSERT INTO BSCL."BSCL.Normativa.BD::NOR.TipoParametros"
("TipoParametro", "Entidad", "Nombre", "EsMasivo", "EsInterno")
SELECT 'NOMBRE_ENVIO_TANDEM_CUS', 'SOLICITUD', 'Nombre de envio a Tandem cus', 'N', 'N'
FROM dummy WHERE 
 NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::NOR.TipoParametros" N WHERE N."TipoParametro"='NOMBRE_ENVIO_TANDEM_CUS');


 
  /*PROCESO EN PAUTA*/
 
DELETE FROM BSCL."BSCL.Plataforma.BD::PAU.Proceso" WHERE "CodigoProceso" = 'GENERARINFODERECHOS_PAUTA';

INSERT INTO BSCL."BSCL.Plataforma.BD::PAU.Proceso"
("IdEmpresa", "CodigoProceso", "DescripcionProceso", "CodigoModulo", "EsApertura", "EsCierre", "IdEstado", "EsTipoPrograma", "Orden", "NombreClase", "DetienePauta", "OrdenFinDeMesInhabil", "EjecutarFinDeMesInhabil")
VALUES(1, 'GENERARINFODERECHOS_PAUTA', 'Generacion Circular informe Derechos TP', 'SIS', 'N', 'S', 'INA', 'S', 3, 'cl.bcs.plataforma.jobs.PautaGenerarInfoDerechos', 'N', 0, 'N');

 
DELETE FROM BSCL."BSCL.Plataforma.BD::PAU.Proceso" WHERE "CodigoProceso" = 'GENERAR1064PAUTA_R';

INSERT INTO BSCL."BSCL.Plataforma.BD::PAU.Proceso"
("IdEmpresa", "CodigoProceso", "DescripcionProceso", "CodigoModulo", "EsApertura", "EsCierre", "IdEstado", "EsTipoPrograma", "Orden", "NombreClase", "DetienePauta", "OrdenFinDeMesInhabil", "EjecutarFinDeMesInhabil")
VALUES(1, 'GENERAR1064PAUTA_R', 'Generacion Circular 1064 - BCS en Rueda', 'SIS', 'N', 'S', 'ACT', 'S', 2, 'cl.bcs.plataforma.jobs.PautaGenerar1064R', 'N', 0, 'N');


DELETE FROM BSCL."BSCL.Plataforma.BD::PAU.Proceso" WHERE "CodigoProceso" = 'GENERAR1064PAUTA_FR';

INSERT INTO BSCL."BSCL.Plataforma.BD::PAU.Proceso"
("IdEmpresa", "CodigoProceso", "DescripcionProceso", "CodigoModulo", "EsApertura", "EsCierre", "IdEstado", "EsTipoPrograma", "Orden", "NombreClase", "DetienePauta", "OrdenFinDeMesInhabil", "EjecutarFinDeMesInhabil")
VALUES(1, 'GENERAR1064PAUTA_FR', 'Generacion Circular 1064 - BCS Fuera de Rueda', 'SIS', 'N', 'S', 'ACT', 'S', 4, 'cl.bcs.plataforma.jobs.PautaGenerar1064FR', 'N', 0, 'N');



---Se quita de la pauta :(
UPDATE BSCL."BSCL.Plataforma.BD::PAU.Proceso" SET  "IdEstado"='INA' WHERE "CodigoProceso" = 'GENERARINFODERECHOS_PAUTA';
UPDATE BSCL."BSCL.Plataforma.BD::PAU.Proceso" SET  "IdEstado"='ACT' WHERE "CodigoProceso" = 'GENERAR1064PAUTA_R';
UPDATE BSCL."BSCL.Plataforma.BD::PAU.Proceso" SET  "IdEstado"='ACT' WHERE "CodigoProceso" = 'GENERAR1064PAUTA_FR';


  
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )
SELECT  
'UI_ENVIO_BURSATIL',
'CL',
'Envio Bursatil',
'ENVBUR'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Diccionario" WHERE  "CodDiccionario"='UI_ENVIO_BURSATIL');
 