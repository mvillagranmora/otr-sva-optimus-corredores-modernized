SET SCHEMA BSCL;


DROP TABLE "BSCL.Normativa.BD::TRI.DatosDJ1899";
CREATE COLUMN TABLE "BSCL.Normativa.BD::TRI.DatosDJ1899" (
	ID BIGINT NOT NULL,
	"IdDocumento" BIGINT NOT NULL,
	"VboUsuario" CHAR(1) NOT NULL,
	"Observaciones" NVARCHAR(250) NOT NULL,
	"FuenteDatos" NVARCHAR(40) NOT NULL,
	"EjecutadoComplementoMontos" NVARCHAR(1),
	"EjecutadoComplementoIPC" NVARCHAR(1),
	"Numero" BIGINT NOT NULL,
	"IdMoneda" NVARCHAR(3),
	"RutCliente" NVARCHAR(40) NOT NULL,
	"IdCliente" BIGINT,
	"ActivoPensionado" INTEGER NOT NULL,
	"RutEmpleador" NVARCHAR(40) NOT NULL,
	"FechaOperacion" DATE,
	"DepositoEmpleador" DECIMAL(26,8) NOT NULL,
	"DepositoEmpleadorMonedaDJ" DECIMAL(26,8),
	"DepositoEmpleadorSegundaMonedaDJ" DECIMAL(26,8),
	"AporteEmpleador" DECIMAL(26,8) NOT NULL,
	"AporteEmpleadorMonedaDJ" DECIMAL(26,8),
	"TraspasoEmpleador" DECIMAL(26,8) NOT NULL,
	"TraspasoEmpleadorMonedaDJ" DECIMAL(26,8),
	"RetiroEmpleadorActualizado" DECIMAL(26,8) NOT NULL,
	"RetiroEmpleadorActualizadoMonedaDJ" DECIMAL(26,8),
	"RetiroEmpleadorActualizadoIPC" DECIMAL(26,8),
	"MontoRetencionActualizado" DECIMAL(26,8) NOT NULL,
	"MontoRetencionActualizadoMonedaDJ" DECIMAL(26,8),
	"MontoRetencionActualizadoIPC" DECIMAL(26,8),
	"AhorrosModalidadIndirectaEmpleador" DECIMAL(26,8) NOT NULL,
	"AhorrosModalidadIndirectaEmpleadorMonedaDJ" DECIMAL(26,8),
	"AhorrosModalidadIndirectaEmpleadorSegundaMonedaDJ" DECIMAL(26,8),
	"AhorrosModalidadDirectaTrabajador" DECIMAL(26,8) NOT NULL,
	"AhorrosModalidadDirectaTrabajadorMonedaDJ" DECIMAL(26,8),
	"AhorrosModalidadDirectaTrabajadorSegundaMonedaDJ" DECIMAL(26,8),
	"CotizacionesModalidadIndirectaEmpleador" DECIMAL(26,8) NOT NULL,
	"CotizacionesModalidadIndirectaEmpleadorMonedaDJ" DECIMAL(26,8),
	"CotizacionesModalidadDirectaTrabajador" DECIMAL(26,8) NOT NULL,
	"CotizacionesModalidadDirectaTrabajadorMonedaDJ" DECIMAL(26,8),
	"CotizacionesTrabajadorIndependiente" DECIMAL(26,8) NOT NULL,
	"CotizacionesTrabajadorIndependienteMonedaDJ" DECIMAL(26,8),
	"CotizacionesTrabajadorIndependienteSegundaMonedaDJ" DECIMAL(26,8),
	"MontoRetiroPorAhorrosIncisoPrimero" DECIMAL(26,8) NOT NULL,
	"MontoRetiroPorAhorrosIncisoPrimeroMonedaDJ" DECIMAL(26,8),
	"MontoRetiroPorAhorrosIncisoPrimeroIPC" DECIMAL(26,8),
	"RetencionImpuestoSobreRetiros" DECIMAL(26,8) NOT NULL,
	"RetencionImpuestoSobreRetirosMonedaDJ" DECIMAL(26,8),
	"RetencionImpuestoSobreRetirosIPC" DECIMAL(26,8),
	"MontoRetiroPorAhorrosIncisoSegundo" DECIMAL(26,8) NOT NULL,
	"MontoRetiroPorAhorrosIncisoSegundoMonedaDJ" DECIMAL(26,8),
	"NumeroCertificadoTrabajador" BIGINT NOT NULL,
	"NumeroCertificadoEmpleador" BIGINT NOT NULL,
	"NumeroCertificadoDepositosConvenidos" BIGINT,
	PRIMARY KEY (ID)
);


ALTER TABLE "BSCL.Normativa.BD::TRI.DatosDJ1899"
    ADD CONSTRAINT "FK_Documento_DatosDJ1899" FOREIGN KEY ("IdDocumento")
        REFERENCES "BSCL.Normativa.BD::DOC.Documento" ("IdDocumento")
        ON DELETE RESTRICT ON UPDATE RESTRICT;


DROP TABLE BSCL."BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1899N24";

create column table BSCL."BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1899N24"(
    "IdDetalleCertificadoClienteDJ1899N24" 			bigint not null,
    "IdCertificadoCliente" 							bigint,
    "IdCliente" 									bigint,
    "Mes" 											int,
    "IdTipoDocumento" 								nvarchar(10),
    --AHORRO PREVISIONAL VOLUNTARIO EFECTUADO EN CALIDAD DE TRABAJADOR DEPENDIENTE DEL ART.42 N°1 LIR, ACTUALIZADO
    --Cotizaciones   Voluntarias y Depósitos de Ahorro Previsional Voluntario en $
    "ModalidadIndirectaViaEmpleador" 				decimal(26,8),
    "ModalidadDirectaViaTrabajador" 				decimal(26,8),
    --AHORRO PREVISIONAL VOLUNTARIO EFECTUADO EN CALIDAD DE TRABAJADOR INDEPENDIENTE DEL ART.42 N°2 LIR, ACTUALIZADO
    "CotizacionesObligatorias" 						decimal(26,8),--($)
    "CotizacionesVoluntarias" 						decimal(26,8),--Cotizaciones Voluntarias y Depósitos de Ahorro Previsional Voluntario en $
    --MONTO DE LOS RETIROS EFECTUADOS CON CARGO A LOS AHORROS PREVISIONALES VOLUNTARIOS REALIZADOS, ACTUALIZADO
    "TrabajadoresActivos" 							decimal(26,8), --($)
    "Pensionados" 									decimal(26,8),--Pensionados o personas que cumplen con los requisitos para pensionarse que exigen los Arts.3° y 68 letra B) D.L. N°3.500/80 o D.L. N°2.448/7
    "RetencionImpuesto" 							decimal(26,8),--Retención de Impuesto de 15% practicada sobre los retiros efectuados con cargo a los ahorros previsionales voluntarios realizados actualizada en $
    "IdEmpresa" 									BIGINT,
    "Version" 										nvarchar(10),
	"RutCliente"									NVARCHAR(40)	NOT NULL,
    primary key ("IdDetalleCertificadoClienteDJ1899N24")
);

ALTER TABLE "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1899N24"
    ADD CONSTRAINT "FK_CertificadoCliente_DetalleCertificadoClienteDJ1899N24" FOREIGN KEY ("IdCertificadoCliente")
        REFERENCES "BSCL.Normativa.BD::TRI.CertificadoCliente" ("IdCertificadoCliente")
        ON DELETE RESTRICT ON UPDATE RESTRICT;



DROP TABLE BSCL."BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1899N36";

create column table BSCL."BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1899N36"(
    "IdDetalleCertificadoClienteDJ1899N36" 		bigint not null,
    "IdCertificadoCliente" 						bigint,
    "IdCliente" 								bigint,
    "RutEmpleador" 								nvarchar(40),
    "Mes" 										int,
    "IdTipoDocumento" 							nvarchar(10),
    "MontoDepositoConvenido" 					decimal(26,8),
    "IdEmpresa" 								BIGINT,
    "Version" 									nvarchar(10),
	"RutCliente"								NVARCHAR(40)	NOT NULL,
    primary key ("IdDetalleCertificadoClienteDJ1899N36")
);

ALTER TABLE "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1899N36"
    ADD CONSTRAINT "FK_CertificadoCliente_DetalleCertificadoClienteDJ1899N36" FOREIGN KEY ("IdCertificadoCliente")
        REFERENCES "BSCL.Normativa.BD::TRI.CertificadoCliente" ("IdCertificadoCliente")
        ON DELETE RESTRICT ON UPDATE RESTRICT;




--Limpiar todas las versiones        
DELETE FROM "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1899";
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" WHERE "Esquema"	= 'DJ1899';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"  	WHERE "Esquema"	= 'DJ1899';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Campo" 			WHERE "Esquema" = 'DJ1899';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" 	WHERE "Esquema"	= 'DJ1899';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Validacion" 		WHERE "Esquema"	= 'DJ1899';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Documento" 		WHERE "Esquema" = 'DJ1899';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Esquema" 		WHERE "Esquema"	= 'DJ1899';
DELETE FROM "BSCL"."BSCL.Normativa.BD::TRI.ControlProceso" 	WHERE "Esquema"	= 'DJ1899';
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente" WHERE "Esquema" = 'DJ1899';

--PeriodoInicio??,FechaPresentacionLimite??,CodigoVersionSII??
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Esquema"(
"Esquema","Version","Modulo","Periodicidad","Objetivo","PeriodoInicio","IdEntidadReguladora","FechaPresentacionLimite",
"FechaDatosDesde","FechaDatosHasta","CodigoVersionSII","HabilitadoMantenedor","TieneCertificado")
SELECT
'DJ1899',2020,'TRI','A','Declaración Jurada Anual sobre Movimientos de las Cuentas de Ahorro Previsional Voluntario',
'2020-01-01','SII','2022-01-01','2020-01-01','2020-12-31',1,'S','S'
FROM DUMMY;
--COMMIT


INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1899', '2020', 'ID', '', '', 1, 'Secuencia', 'Ayuda de Secuencia', 'ID', 0, '0', 'N', 'N', 'N', 'S', 'N', 'S', 'S', 'N', '', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1899', '2020', 'IdDocumento', '', '', 2, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'N', 'N', 'N', 'S', 'S', 'S', 'S', 'N', '8. DATOS DE CONTROL', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1899', '2020', 'VboUsuario', '', 'VistoBueno', 3, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, 'N', 'N', 'S', 'S', 'S', 'N', 'N', 'S', 'N', '8. DATOS DE CONTROL', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1899', '2020', 'Observaciones', '', '', 4, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '', 'N', 'S', 'S', 'S', 'N', 'N', 'S', 'N', '8. DATOS DE CONTROL', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1899', '2020', 'FuenteDatos', '', '', 5, 'Fuente', 'Ayuda de Fuente', 'TEXT', 40, 'INICIAL', 'N', 'N', 'N', 'S', 'N', 'N', 'S', 'N', '8. DATOS DE CONTROL', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1899', '2020', 'Numero', 'C0', '', 6, 'Numero', '', 'ID', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '', 1, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1899', '2020', 'IdMoneda', '', 'IdMoneda', 7, 'Moneda', '', 'TEXT', 3, '', 'N', 'N', 'N', 'N', 'S', 'S', 'N', 'N', '', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1899', '2020', 'RutCliente', 'C2', '', 8, 'Rut Trabajador', 'Ayuda de Rut Trabajador', 'RUT_CLIENTE', 40, '', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '', 2, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1899', '2020', 'IdCliente', '', '', 9, 'Secuencia Trabajador', 'Ayuda de Secuencia Trabajador', 'ID_CLIENTE', 0, '0', 'S', 'N', 'N', 'N', 'S', 'N', 'N', 'N', '', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1899', '2020', 'ActivoPensionado', 'C3', '', 10, 'Activo o Pensionado', '', 'AMOUNT', 0, '0', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 3, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1899', '2020', 'RutEmpleador', 'C4', '', 11, 'Rut Empleador', 'Ayuda de Rut Empleador', 'RUT_CLIENTE', 40, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 4, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1899', '2020', 'FechaOperacion', '', '', 12, 'Fecha Operacion', '', 'DATE_FILTER', 0, '0', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 5, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1899', '2020', 'DepositoEmpleador', 'C5', '', 13, 'Depositos Convenidos con el empleador en UF', 'Ayuda Depositos Convenidos con el empleador en UF', 'DECIMAL', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 6, 3);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1899', '2020', 'AporteEmpleador', 'C7', '', 14, 'Aporte Empleador UF', 'Ayuda Aporte Empleador UF', 'DECIMAL', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. APORTE EMPLEADOR', 7, 3);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1899', '2020', 'TraspasoEmpleador', 'C8', '', 15, 'Traspaso Empleador UF', 'Ayuda Traspaso Empleador UF', 'DECIMAL', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. APORTE EMPLEADOR', 8, 3);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1899', '2020', 'RetiroEmpleadorActualizado', 'C9', '', 16, 'Retiro Empleador Actualizado $', 'Ayuda Retiro Empleador Actualizado $', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. APORTE EMPLEADOR', 9, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1899', '2020', 'MontoRetencionActualizado', 'C10', '', 17, 'Monto Retencion 15% Actualizado $', 'Ayuda Monto Retencion 15% Actualizado $', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. APORTE EMPLEADOR', 10, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1899', '2020', 'AhorrosModalidadIndirectaEmpleador', 'C11', '', 18, 'Modalidad Indirecta via Empleador', 'Ayuda Modalidad Indirecta via Empleador', 'DECIMAL', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. AHORROS DEL TRABAJADOR', 11, 3);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1899', '2020', 'AhorrosModalidadDirectaTrabajador', 'C12', '', 19, 'Modalidad Directa via Trabajador', 'Ayuda Modalidad Directa via Trabajador', 'DECIMAL', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. AHORROS DEL TRABAJADOR', 12, 3);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1899', '2020', 'CotizacionesModalidadIndirectaEmpleador', 'C13', '', 20, 'Modalidad Indirecta. Via Empleador', 'Ayuda Modalidad Indirecta. Via Empleador', 'DECIMAL', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '4. TRABAJADOR DEPENDIENTE', 13, 3);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1899', '2020', 'CotizacionesModalidadDirectaTrabajador', 'C14', '', 21, 'Modalidad Directa. Via Trabajador', 'Ayuda Modalidad Directa. Via Trabajador', 'DECIMAL', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '4. TRABAJADOR DEPENDIENTE', 14, 3);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1899', '2020', 'CotizacionesTrabajadorIndependiente', 'C15', '', 22, 'Trabajador independiente en UF', 'Ayuda Trabajador independiente en UF', 'DECIMAL', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '5. TRABAJADOR INDEPENDIENTE', 15, 3);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1899', '2020', 'MontoRetiroPorAhorrosIncisoPrimero', 'C16', '', 23, 'Monto del retiro por ahorros Inciso Primero ($)', 'Ayuda Monto del retiro por ahorros Inciso Primero en $', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '6. RETIROS EN AHORROS VOLUNTARIOS', 16, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1899', '2020', 'RetencionImpuestoSobreRetiros', 'C17', '', 24, 'Retencion de Impto de 15% ($)', 'Ayuda Retencion de Impto de 15% en $', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '6. RETIROS EN AHORROS VOLUNTARIOS', 17, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1899', '2020', 'MontoRetiroPorAhorrosIncisoSegundo', 'C18', '', 25, 'Monto del retiro por ahorros Inciso Segundo ($)', 'Ayuda v en $', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '6. RETIROS EN AHORROS VOLUNTARIOS', 18, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1899', '2020', 'NumeroCertificadoTrabajador', 'C35', '', 26, 'N Certificado Trabajador', 'Ayuda N Certificado Trabajador', 'AMOUNT', 0, '0', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '7.- CERTIFICADOS', 19, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1899', '2020', 'NumeroCertificadoEmpleador', 'C36', '', 27, 'N Certificado Empleador', 'Ayuda Nº Certificado Empleador', 'AMOUNT', 0, '0', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '7.- CERTIFICADOS', 20, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1899', '2020', 'EjecutadoComplementoMontos', 'C99', '', 28, 'Ejecucion complemento actualizacion montos', '', 'TEXT', 1, 'N', 'N', 'N', 'N', 'N', 'S', 'S', 'N', 'N', '8. DATOS DE CONTROL', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1899', '2020', 'EjecutadoComplementoIPC', 'C100', '', 29, 'Ejecucion complemento IPC', '', 'TEXT', 1, 'N', 'N', 'N', 'N', 'N', 'S', 'S', 'N', 'N', '8. DATOS DE CONTROL', 0, 0);


--TODO, CORREGIR IdTipoDocumento??,FechaLimite??,"FechaPresentacion"??
insert into "BSCL.Normativa.BD::DOC.Documento"
("IdDocumento","IdTipoDocumento","Esquema","Version","IdEmpresa","FechaLimite","IdEstadoDocumento",
"FechaCreacion","FechaPresentacion","DocumentoIdentidadDeclarante",
"DocumentoIdentidadRepresentante","DireccionElectronicaDeclaracion",
"DireccionFisicaDeclaracion","ComunaDeclaracion","TelefonoDeclaracion",
"FolioDeclaracion","EnProceso","FechaInicioUltimoProceso","AnoMes")
SELECT "BSCL"."BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL AS "IdDocumento",
'DECLACORR' AS "IdTipoDocumento"
,'DJ1899' AS "Esquema"
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

/*
--no posee validaciones, sgun archivo set_validacines_ddjj_at2017.xls bajado desde internet, sii
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Validacion" (
    "Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
SELECT 'DJ1899','2020','H0','NORMA','N','En la columna Activo o Pensionado (C3) Se debe indicar valores (1 o 2)','E','' FROM DUMMY union all
SELECT 'DJ1899','2020','H1','NORMA','N','Debe ejecutar el complemento actualizar montos','E','' FROM DUMMY UNION ALL
SELECT 'DJ1899','2020','H2','NORMA','N','Debe ejecutar el complemento carga IPC','E','' FROM DUMMY;
*/


/*
INSERT INTO BSCL."BSCL.Plataforma.BD::BAT.TipoPrograma"(
    "IdTipoPrograma","Descripcion","MaximoIntentos","Modulo","NombreClasePrograma","TipoClasePrograma","Nodo","EsPauta","TipoSalida","FormatoSalida","TieneLog")
SELECT 'GenerarCertificado24','Generar Certificado 24',1,'Tributario','cl.bcs.normativa.complementos.BcsJobGenerarCertificado24','JAVA','','N','P','','N' FROM DUMMY UNION ALL
SELECT 'GenerarCertificado30','Generar Certificado 30',1,'Tributario','cl.bcs.normativa.complementos.BcsJobGenerarCertificado30','JAVA','','N','P','','N' FROM DUMMY UNION ALL
SELECT 'GenerarCertificado36','Generar Certificado 36',1,'Tributario','cl.bcs.normativa.complementos.BcsJobGenerarCertificado36','JAVA','','N','P','','N' FROM DUMMY;

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" (
    "IdArchivoNormativa","VersionArchivo","Esquema","Version","IdTipoDocumento","Descripcion","NombreArchivo","IdArchivo","TipoArchivo","ServicioArchivo","IdEmpresa","IdEstado")
SELECT 'CERT24-PDF',3,'DJ1899',3,'CERT24','CERTIFICADO 24 TRIBUTARIOS','CERTIFICADO24','305EFC5A32631D17E1000000A1EE000E','pdf',600042,1,'ACT' FROM DUMMY UNION ALL
SELECT 'CERT30-PDF',3,'DJ1899',3,'CERT30','CERTIFICADO 30 TRIBUTARIOS','CERTIFICADO30','2A7A055B266EBA16E1000000A1EE000E','pdf',600043,1,'ACT' FROM DUMMY UNION ALL
SELECT 'CERT36-PDF',3,'DJ1899',3,'CERT36','CERTIFICADO 36 TRIBUTARIOS','CERTIFICADO36','305EFE5A73DDC216E1000000A1EE000E','pdf',600044,1,'ACT' FROM DUMMY;
*/


--alter table "BSCL"."BSCL.Normativa.BD::TRI.CertificadoCliente" add ("IdTipoCertificado" NVARCHAR(6) NOT NULL DEFAULT '');
/*
--SERVICIOS
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 600035,'BSCL.Normativa.BD.Vistas::TRI.DJ1899.PreDatos',0,'Q','','','' 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=600035);


--100053
Insert Into "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" (
"IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
values (600036,'BSCL.Normativa.BD.Vistas::TRI.DJ1899.Invalidos',0,'Q','','','');

--100054
Insert Into "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" (
"IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
values (600037,'BSCL.Normativa.BD.Servicios::TRI.DJ1899.ObtenerTotales',0,'P','','','');

--100055
Insert Into "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" (
"IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
values (600038,'BSCL.Normativa.BD.Servicios::TRI.DJ1899.GrabarDetalle',0,'P','','','');

--100056
Insert Into "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" (
"IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
values (600039,'BSCL.Normativa.BD.Servicios::TRI.DJ1899.EliminarDetalles',0,'P','','','');

--100057
Insert Into "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" (
"IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
values (600040,'PLUGIN_NOR.jar@cl.bcs.normativa.declaraciones.DJ1899.ActualizarDetalles',0,'J','','','');

--100058
Insert Into "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" (
"IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
values (600041,'BSCL.Normativa.BD.Vistas::TRI.DJ1899.DatosActualizarMontos',0,'Q','','','');

--100059
Insert Into "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" (
"IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
values (600042,'BSCL.Normativa.BD.Servicios::TRI.DJ1899.GenerarDocumentoCertificado24',0,'P','','','');

--100060
Insert Into "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" (
"IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
values (600043,'BSCL.Normativa.BD.Servicios::TRI.DJ1899.GenerarDocumentoCertificado30',0,'P','','','');

--100061
Insert Into "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" (
"IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
values (600044,'BSCL.Normativa.BD.Servicios::TRI.DJ1899.GenerarDocumentoCertificado36',0,'P','','','');

--100066
Insert Into "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" (
"IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
values (600045,'PLUGIN_NOR.jar@cl.bcs.normativa.declaraciones.DJ1899.ReajustarValorIPC',0,'J','','',''); 
*/

insert into "BSCL"."BSCL.Normativa.BD::TRI.ControlProceso"
("Esquema","Version","EstadoCertificado","Carga","Validacion","PreDeclaracion","Certificacion")
SELECT "Esquema","Version",'NGE','N','N','N','N' FROM "BSCL"."BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1899' AND "Version" IN ('2020');


DELETE FROM  BSCL."BSCL.Normativa.BD::NOR.Configuracion" WHERE "IdNormativa"='DJ1899';

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Configuracion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
VALUES(1, 'P16', 'Factor P16 fijo 15%', '0.15', 'DJ1899', 'N', 'Validacion DJ1889', '1', 'Numerico, (10000)', 'N', '', 'SISTEMA', '2020-05-03 12:00:00.000', 'PROJO', '2021-07-07 23:05:04.000', ' ');

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Configuracion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
VALUES(1, 'P31', 'Factor P31', '0', 'DJ1899', 'N', 'Validacion DJ1899', '0', 'Numerico, (10000)', 'N', '', 'SISTEMA', '2020-05-03 12:00:00.000', 'PROJO', '2021-07-07 23:05:04.000', ' ');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1899', '2020', 'H264', 'NORMA', 'S', 'La retención del 15% practicada sobre retiros efectuados con cargoÂ  a ahorros Previsionales acogidos al inciso primero del Art. 42 bis de la LIR y/oÂ  ahorros del trabajador generados por aportes del empleador (C17), debe ser igual o superior al 15% de los montos de Retiros Provenientes de ahorros acogidos al inciso primero del Art. 42 bis. (C16)', 'E', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1899', '2020', 'H923', 'NORMA', 'S', 'La retención del 15% (C10) practicada sobre los retiros efectuados por el empleador debe ser mayor o igual al 15% del monto del retiro empleador (C9). .', 'E', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1899', '2020', 'H924', 'NORMA', 'S', 'Si en el campo Monto de retiro de ahorros inciso primero en pesos (C16) si tiene valor, se debe indicar en el campo Activo o Pensionado (C3) el valor es 1 o 2 ', 'E', '');



--Certificado
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.CertificadoCliente" WHERE "Esquema"='DJ1899';

DELETE FROM BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente" WHERE "Esquema"='DJ1899'; 


INSERT INTO BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
("Esquema", "Version", "UltimoFolio", "CodigoCertificado", "NombreCertificado", "CodigoPrograma")
VALUES('DJ1899', '2020', 0, 'CERTIFICADO_24', 'Certificado 24', 'GenerarCertificado24');
INSERT INTO BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
("Esquema", "Version", "UltimoFolio", "CodigoCertificado", "NombreCertificado", "CodigoPrograma")
VALUES('DJ1899', '2020', 0, 'CERTIFICADO_30', 'Certificado 30', 'GenerarCertificado30');
INSERT INTO BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
("Esquema", "Version", "UltimoFolio", "CodigoCertificado", "NombreCertificado", "CodigoPrograma")
VALUES('DJ1899', '2020', 0, 'CERTIFICADO_36', 'Certificado 36', 'GenerarCertificado36');

--Archivo Normativa
DELETE FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa"  WHERE "Esquema"='DJ1899';

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa"
("IdArchivoNormativa", "VersionArchivo", "Esquema", "Version", "IdTipoDocumento", "Descripcion", "NombreArchivo", "IdArchivo", "TipoArchivo", "ServicioArchivo", "IdEmpresa", "IdEstado")
VALUES('CERT24-PDF', '2020', 'DJ1899', '3', 'CERT24', 'CERTIFICADO 24 TRIBUTARIOS', 'CERTIFICADO24', '', 'pdf', '600042', 1, 'ACT');
INSERT INTO BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa"
("IdArchivoNormativa", "VersionArchivo", "Esquema", "Version", "IdTipoDocumento", "Descripcion", "NombreArchivo", "IdArchivo", "TipoArchivo", "ServicioArchivo", "IdEmpresa", "IdEstado")
VALUES('CERT36-PDF', '2020', 'DJ1899', '3', 'CERT36', 'CERTIFICADO 36 TRIBUTARIOS', 'CERTIFICADO36', '', 'pdf', '600044', 1, 'ACT');
INSERT INTO BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa"
("IdArchivoNormativa", "VersionArchivo", "Esquema", "Version", "IdTipoDocumento", "Descripcion", "NombreArchivo", "IdArchivo", "TipoArchivo", "ServicioArchivo", "IdEmpresa", "IdEstado")
VALUES('CERT30-PDF', '2020', 'DJ1899', '3', 'CERT30', 'CERTIFICADO 30 TRIBUTARIOS', 'CERTIFICADO30', '', 'pdf', '600043', 1, 'ACT');


/*
INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Configuracion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
SELECT 1 AS "IdEmpresa",'TRI_NOM_' || "NombreArchivo" AS "CodigoParametro",'Nombre del archivo PDF en  ZIP masivo de certificados.' AS "Descripcion"
,"IdTipoDocumento" AS "Valor"
,"Esquema" AS  "IdNormativa"
,'S' AS "EsModificableUsuario"
,'Certificado masivo' AS "Uso"
,"IdTipoDocumento"  AS "ValorDefecto"
,'' AS "ValoresPosibles",'S' AS "ModificablePorUsuario",'' AS  "ModificablePostProduccion", '' AS  "CreadorParametro", CURRENT_UTCTIMESTAMP AS "FechaCreacion"
,'' AS "ModificadorParametro", CURRENT_UTCTIMESTAMP AS "FechaModificacion", '' AS "Observaciones"
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1899' AND "VersionArchivo"=2020
AND NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" N WHERE N."CodigoParametro"='TRI_NOM_' || "NombreArchivo" AND N."IdNormativa"="Esquema");  
*/


--UPDATE "BSCL"."BSCL.Normativa.BD::DOC.Validacion" SET "Activa"='N' WHERE "Esquema"='DJ1899';

-- PARAMETRO:  Nombre de archivo CERTIFICADOS
DELETE  FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" WHERE "IdNormativa"='DJ1899' AND "CodigoParametro" LIKE 'TRI_NOM_CERTIFICADO%';
  
INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Configuracion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
SELECT 1 AS "IdEmpresa",'TRI_NOM_' || "NombreArchivo" ||  '_01'AS "CodigoParametro",'Nombre del archivo PDF en  ZIP masivo de certificados.' AS "Descripcion"
,'[RUT]' AS "Valor"
,"Esquema" AS  "IdNormativa"
,'S' AS "EsModificableUsuario"
,'Certificado masivo' AS "Uso"
,'[RUT]'  AS "ValorDefecto"
,'[RUT],[PERIODO],[DJ],[CERT],texto libre' AS "ValoresPosibles",'S' AS "ModificablePorUsuario",'' AS  "ModificablePostProduccion", '' AS  "CreadorParametro", CURRENT_UTCTIMESTAMP AS "FechaCreacion"
,'' AS "ModificadorParametro", CURRENT_UTCTIMESTAMP AS "FechaModificacion", '' AS "Observaciones"
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1899' AND "VersionArchivo"=2020
AND NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" N WHERE N."CodigoParametro"='TRI_NOM_' || "NombreArchivo" ||  '_01' AND N."IdNormativa"="Esquema");  

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Configuracion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
SELECT 1 AS "IdEmpresa",'TRI_NOM_' || "NombreArchivo" ||  '_02'AS "CodigoParametro",'Nombre del archivo PDF en  ZIP masivo de certificados.' AS "Descripcion"
,'CB' AS "Valor"
,"Esquema" AS  "IdNormativa"
,'S' AS "EsModificableUsuario"
,'Certificado masivo' AS "Uso"
,'CB'  AS "ValorDefecto"
,'[RUT],[PERIODO],[DJ],[CERT],texto libre' AS "ValoresPosibles",'S' AS "ModificablePorUsuario",'' AS  "ModificablePostProduccion", '' AS  "CreadorParametro", CURRENT_UTCTIMESTAMP AS "FechaCreacion"
,'' AS "ModificadorParametro", CURRENT_UTCTIMESTAMP AS "FechaModificacion", '' AS "Observaciones"
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1899' AND "VersionArchivo"=2020
AND NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" N WHERE N."CodigoParametro"='TRI_NOM_' || "NombreArchivo" ||  '_02' AND N."IdNormativa"="Esquema");  

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Configuracion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
SELECT 1 AS "IdEmpresa",'TRI_NOM_' || "NombreArchivo" ||  '_03'AS "CodigoParametro",'Nombre del archivo PDF en  ZIP masivo de certificados.' AS "Descripcion"
,'[PERIODO]' AS "Valor"
,"Esquema" AS  "IdNormativa"
,'S' AS "EsModificableUsuario"
,'Certificado masivo' AS "Uso"
,'[PERIODO]'  AS "ValorDefecto"
,'[RUT],[PERIODO],[DJ],[CERT],texto libre' AS "ValoresPosibles",'S' AS "ModificablePorUsuario",'' AS  "ModificablePostProduccion", '' AS  "CreadorParametro", CURRENT_UTCTIMESTAMP AS "FechaCreacion"
,'' AS "ModificadorParametro", CURRENT_UTCTIMESTAMP AS "FechaModificacion", '' AS "Observaciones"
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1899' AND "VersionArchivo"=2020
AND NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" N WHERE N."CodigoParametro"='TRI_NOM_' || "NombreArchivo" ||  '_03' AND N."IdNormativa"="Esquema");  

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Configuracion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
SELECT 1 AS "IdEmpresa",'TRI_NOM_' || "NombreArchivo" ||  '_04'AS "CodigoParametro",'Nombre del archivo PDF en  ZIP masivo de certificados.' AS "Descripcion"
,'[DJ]' AS "Valor"
,"Esquema" AS  "IdNormativa"
,'S' AS "EsModificableUsuario"
,'Certificado masivo' AS "Uso"
,'[DJ]'  AS "ValorDefecto"
,'[RUT],[PERIODO],[DJ],[CERT],texto libre' AS "ValoresPosibles",'S' AS "ModificablePorUsuario",'' AS  "ModificablePostProduccion", '' AS  "CreadorParametro", CURRENT_UTCTIMESTAMP AS "FechaCreacion"
,'' AS "ModificadorParametro", CURRENT_UTCTIMESTAMP AS "FechaModificacion", '' AS "Observaciones"
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1899' AND "VersionArchivo"=2020
AND NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" N WHERE N."CodigoParametro"='TRI_NOM_' || "NombreArchivo" ||  '_04' AND N."IdNormativa"="Esquema");  


INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Configuracion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
SELECT 1 AS "IdEmpresa",'TRI_NOM_' || "NombreArchivo" ||  '_05'AS "CodigoParametro",'Nombre del archivo PDF en  ZIP masivo de certificados.' AS "Descripcion"
,'[CERT]' AS "Valor"
,"Esquema" AS  "IdNormativa"
,'S' AS "EsModificableUsuario"
,'Certificado masivo' AS "Uso"
,'[CERT]'  AS "ValorDefecto"
,'[RUT],[PERIODO],[DJ],[CERT],texto libre' AS "ValoresPosibles",'S' AS "ModificablePorUsuario",'' AS  "ModificablePostProduccion", '' AS  "CreadorParametro", CURRENT_UTCTIMESTAMP AS "FechaCreacion"
,'' AS "ModificadorParametro", CURRENT_UTCTIMESTAMP AS "FechaModificacion", '' AS "Observaciones"
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1899' AND "VersionArchivo"=2020
AND NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" N WHERE N."CodigoParametro"='TRI_NOM_' || "NombreArchivo" ||  '_05' AND N."IdNormativa"="Esquema");  

