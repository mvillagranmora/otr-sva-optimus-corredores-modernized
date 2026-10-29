SET SCHEMA "BSCL";

DROP  TABLE "BSCL.Normativa.BD::TRI.DatosDJ1922";

CREATE COLUMN TABLE "BSCL.Normativa.BD::TRI.DatosDJ1922" (
	"ID" 												BIGINT 			NOT NULL,
    "IdDocumento" 										BIGINT 			NOT NULL,
    "VboUsuario" 										CHAR(1) 		NOT NULL,
    "Observaciones" 									NVARCHAR(250) 	NOT NULL,
    "FuenteDatos" 										NVARCHAR(40) 	NOT NULL,
	"Numero"											BIGINT			NOT NULL,
	"IdOperacion"										BIGINT			,
	"RutAdministradora"									NVARCHAR(40)	NOT NULL,
	"IdEmisor"											NVARCHAR(40)	DEFAULT '',
	"RutFondo"											NVARCHAR(40)	NOT NULL,
	"RunFondo"											NVARCHAR(40)	NOT NULL,
	"Fondo"												NVARCHAR(100)	NOT NULL,
	"TipoFondo"											NVARCHAR(100)	NOT NULL,
	"RutCliente"										NVARCHAR(40)	NOT NULL,
	"NombreParticipe"									NVARCHAR(250)	NOT NULL,						
	"TaxId"												NVARCHAR(250)	NOT NULL,
	"CodigoPais"										NVARCHAR(100)	NOT NULL,
	"TipoOperacion"										NVARCHAR(100)	NOT NULL,
	"FechaInversion"									DATE			NOT NULL,
	"MonedaReajuste"									NVARCHAR(100)	NOT NULL,
	"MontoNominalAportes"								DECIMAL(26,8)	DEFAULT 0,
	"MontoNominalAportesActualizado"					DECIMAL(26,8)	DEFAULT 0,
	"NumeroSerieCuotas"									BIGINT			NOT NULL,
	"CantidadCuotasParticipe"							DECIMAL(26,8)	NOT NULL,
	"ParticipeCumpleRequisitos"							BIGINT			DEFAULT 0,
	"CuotasCumplenRequisitos"							BIGINT			NOT NULL,
	"ValorCuotaPesos"									DECIMAL(26,8)	NOT NULL,
	"NumCuotasSerieParticipe"							DECIMAL(26,8)	NOT NULL,
	"PorcentajeParticipacion31Dic"						DECIMAL(26,8)	NOT NULL,
	"ValNominalCuotasRescatadas"						DECIMAL(26,8)	NOT NULL,
	"CantidadCuotasRescatadas"							DECIMAL(26,8)	NOT NULL,
	"DiferenciaPorRescateOEnajenacion"					DECIMAL(26,8)	DEFAULT 0,
	"DiferenciaPorRescateOEnajenacionActualizado"		DECIMAL(26,8)	DEFAULT 0,
	"DifPorRescateOEnajenacionCFIAcogido107"			DECIMAL(26,8)	DEFAULT 0,
	"DifPorRescateOEnajenacionCFIAcogido107Actualizado"	DECIMAL(26,8)	DEFAULT 0,
	"ValNominalInversionaFechaLiquidacion"				DECIMAL(26,8)	NOT NULL,
	"CantCuotasCedidasPorLiquidacion"					DECIMAL(26,8)	NOT NULL,
	"DiferenciaValorCuotasFechaLiquidacion"				DECIMAL(26,8)	DEFAULT 0,
	"DiferenciaValorCuotasFechaLiquidacionActualizado"	DECIMAL(26,8)	DEFAULT 0,
	"TributaSa"											NVARCHAR(2)		DEFAULT '',
	"DivRemDistAfectGCConIDPC"  						DECIMAL(26,8)	DEFAULT 0,
	"DivRemDistAfectGCConIDPCal31122016"  				DECIMAL(26,8)	DEFAULT 0,--DivRemDistAfectGCConIDPCal31122016
	"DivRemDistAfectGCConIDPCVOL"  						DECIMAL(26,8)	DEFAULT 0,--DivRemDistAfectGCConIDPCVOL
	"DivRemDistAfectGCSINDC" 							DECIMAL(26,8)	DEFAULT 0, --DivRemDistAfectGCSINDC
	"DisRemRentasTribCProvRap_C96"  					DECIMAL(26,8)	DEFAULT 0,--DisRemRentasTribCProvRap_C96
	"DisRemRentasTribCORP_C97"  						DECIMAL(26,8)	DEFAULT 0,--DisRemRentasTribCORP_C97
	"DisRemRentasTribCExDD_C98"  						DECIMAL(26,8)	DEFAULT 0,--DisRemRentasTribCExDD_C98
	"DisRemRentasTribCUtAFFut_C99"  					DECIMAL(26,8)	DEFAULT 0,--DisRemRentasTribCUtAFFut_C99	
	"DivRemDistNoRentaFUT"  							DECIMAL(26,8)	DEFAULT 0,--DivRemDistNoRentaFUT_C70
	"DisRemRentasExIGCAfecImptoAdi_C100"  				DECIMAL(26,8)	DEFAULT 0,--DisRemRentasExIGCAfecImptoAdi_C100
	"CredImpGloCompNSRSD"  								DECIMAL(26,8)	DEFAULT 0,--CredImpGloCompNSRSD_C71  
	"CredImpGloCompNSRCD"  								DECIMAL(26,8)	DEFAULT 0,--CredImpGloCompNSRCD_C72
	"CredIGCAcumD01012017RANSRD01012020SDD_C101"  		DECIMAL(26,8)	DEFAULT 0,--CredIGCAcumD01012017RANSRD01012020SDD_C101
	"CredIGCAcumD01012017RANSRD01012020CDD_C102"  		DECIMAL(26,8)	DEFAULT 0,--CredIGCAcumD01012017RANSRD01012020CDD_C102
	"CredImpGloCompSRSD"  								DECIMAL(26,8)	DEFAULT 0,--CredImpGloCompSRSD_C73
	"CredImpGloCompSRCD"  								DECIMAL(26,8)	DEFAULT 0,--CredImpGloCompSRCD_C74
	"CredIGCAcumD01012017RXSRSDD_C103"  				DECIMAL(26,8)	DEFAULT 0,--CredIGCAcumD01012017RXSRSDD_C103
	"CredIGCAcumD01012017RXSRCDD_C104"  				DECIMAL(26,8)	DEFAULT 0,--CredIGCAcumD01012017RXSRCDD_C104
	"CredImpGloCompCTDCIF"  							DECIMAL(26,8)	DEFAULT 0,--CredImpGloCompCTDCIF_C75
	"CredImpGloCompCDD31122016"  						DECIMAL(26,8)	DEFAULT 0,--CredImpGloCompCDD31122016_C76
	"CredImpGloCompSDD31122016"  						DECIMAL(26,8)	DEFAULT 0,--CredImpGloCompSDD31122016_C77
	"CredIGCAcumH31122016RXSDD_C105"  					DECIMAL(26,8)	DEFAULT 0,--CredIGCAcumH31122016RXSDD_C105
	"CredIGCAcumH31122016RXCDD_C106"  					DECIMAL(26,8)	DEFAULT 0,--CredIGCAcumH31122016RXCDD_C106
	"CredImpGloCompCTDCIF31122016"  					DECIMAL(26,8)	DEFAULT 0,--CredImpGloCompCTDCIF31122016_C78
	"CredImpGloCompCITAExArt21"  						DECIMAL(26,8)	DEFAULT 0,--CredImpGloCompCITAExArt21_C79 
	"DetBenNetoPerRutFdo_C107" 							NVARCHAR(40)	DEFAULT'',--DetBenNetoPerRutFdo_C107
	"DetBenNetoPerPOS_C113"  							DECIMAL(26,8)	DEFAULT 0,--DetBenNetoPerPOS_C113
	"DetBenNetoPerNEG_C114"  							DECIMAL(26,8)	DEFAULT 0,--DetBenNetoPerNEG_C114
	"DividendosExentosIGCoAdicional"					DECIMAL(26,8)	DEFAULT 0,--antes
	"DividendosExentosIGCoAdicionalActualizado"			DECIMAL(26,8)	DEFAULT 0,
	"DividendosNoConstitutivosDeRenta"					DECIMAL(26,8)	DEFAULT 0,
	"DividendosNoConstitutivosDeRentaActualizado"		DECIMAL(26,8)	DEFAULT 0,
	"DivRemDistNoRentaFE" 								DECIMAL(26,8)	DEFAULT 0,--DivRemDistNoRentaFE_C31
	"DividendosAfectosImptoUnicoTasa10"					DECIMAL(26,8)	DEFAULT 0,
	"DividendosAfectosImptoUnicoTasa10Actualizado"		DECIMAL(26,8)	DEFAULT 0,
	"DividendosAfectosImptoUnicoTasa4"					DECIMAL(26,8)	DEFAULT 0,
	"DividendosAfectosImptoUnicoTasa4Actualizado"		DECIMAL(26,8)	DEFAULT 0,
	"DistribucionXDiminucionNoImputadaCapital"			DECIMAL(26,8)	NOT NULL,
	"DevolucionesDeCapital"								DECIMAL(26,8)	DEFAULT 0,
	"DevolucionesDeCapitalActualizado"					DECIMAL(26,8)	DEFAULT 0,
	"MontoInversionenCuotasCFIAnterior93"				DECIMAL(26,8)	NOT NULL,
	"NumeroCertificado"									BIGINT			NOT NULL,
	"FactorActualizacion"								DECIMAL(26,8)	DEFAULT 0,
	"FechaOrigenInversion"								DATE			DEFAULT '1900-01-01',
	"TipoDocumento" 									NVARCHAR(10),
	"NumeroDocumento" 									BIGINT,
	"FechaDocumento" 									DATE,
	"IdBaseDocumento" 									BIGINT,
	"IdCliente" 										BIGINT,
	"IdPortafolio" 										BIGINT,
	"TipoTransaccion" 									NVARCHAR(6),
	"IdTipoOperacion" 									NVARCHAR(6),
	"IdTipoProducto" 									NVARCHAR(4),
	"SecEvento"											BIGINT			DEFAULT 0,
   PRIMARY KEY ("ID")
);

ALTER TABLE "BSCL.Normativa.BD::TRI.DatosDJ1922"
    ADD CONSTRAINT "FK_Documento_DatosDJ1922" FOREIGN KEY ("IdDocumento")
        REFERENCES "BSCL.Normativa.BD::DOC.Documento" ("IdDocumento")
        ON DELETE RESTRICT ON UPDATE RESTRICT;


DROP  TABLE "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1922N43";

CREATE COLUMN TABLE "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1922N43"(
	"IdDetalleCertificadoClienteDJ1922N43"				BIGINT			NOT NULL,
	"IdCertificadoCliente"								BIGINT			NOT NULL,
	"FechaCertificado"									DATE			NOT NULL,
	"NumeroCertificado"									BIGINT			NOT NULL,	
	--------------------------------------------------------------------------------
	"IdOperacion"										BIGINT			NOT NULL,
	"RutAdministradora"									NVARCHAR(40)	NOT NULL,
	"IdEmisor"											NVARCHAR(40)	DEFAULT '',
	"RutFondo"											NVARCHAR(40)	NOT NULL,
	"RunFondo"											NVARCHAR(40)	NOT NULL,
	"Fondo"												NVARCHAR(100)	NOT NULL,
	"TipoFondo"											NVARCHAR(100)	NOT NULL,
	"RutCliente"										NVARCHAR(40)	NOT NULL,
	"NombreParticipe"									NVARCHAR(250)	NOT NULL,						
	"TaxId"												NVARCHAR(250)	NOT NULL,
	"CodigoPais"										NVARCHAR(100)	NOT NULL,
	"TipoOperacion"										NVARCHAR(100)	NOT NULL,
	"FechaInversion"									DATE			NOT NULL,
	"MonedaReajuste"									NVARCHAR(100)	NOT NULL,
	"MontoNominalAportes"								DECIMAL(26,8)	DEFAULT 0,
	"MontoNominalAportesActualizado"					DECIMAL(26,8)	DEFAULT 0,
	"NumeroSerieCuotas"									BIGINT			NOT NULL,
	"CantidadCuotasParticipe"							DECIMAL(26,8)	NOT NULL,
	"ParticipeCumpleRequisitos"							BIGINT			DEFAULT 0,
	"CuotasCumplenRequisitos"							BIGINT			NOT NULL,
	"ValorCuotaPesos"									DECIMAL(26,8)	NOT NULL,
	"NumCuotasSerieParticipe"							DECIMAL(26,8)	NOT NULL,
	"PorcentajeParticipacion31Dic"						DECIMAL(26,8)	NOT NULL,
	"ValNominalCuotasRescatadas"						DECIMAL(26,8)	NOT NULL,
	"CantidadCuotasRescatadas"							DECIMAL(26,8)	NOT NULL,
	"DiferenciaPorRescateOEnajenacion"					DECIMAL(26,8)	DEFAULT 0,
	"DiferenciaPorRescateOEnajenacionActualizado"		DECIMAL(26,8)	DEFAULT 0,
	"DifPorRescateOEnajenacionCFIAcogido107"			DECIMAL(26,8)	DEFAULT 0,
	"DifPorRescateOEnajenacionCFIAcogido107Actualizado"	DECIMAL(26,8)	DEFAULT 0,
	"ValNominalInversionaFechaLiquidacion"				DECIMAL(26,8)	NOT NULL,
	"CantCuotasCedidasPorLiquidacion"					DECIMAL(26,8)	NOT NULL,
	"DiferenciaValorCuotasFechaLiquidacion"				DECIMAL(26,8)	DEFAULT 0,
	"DiferenciaValorCuotasFechaLiquidacionActualizado"	DECIMAL(26,8)	DEFAULT 0,
	"TributaSa"											NVARCHAR(2)		DEFAULT '',
	"DivRemDistAfectGCConIDPC"  						DECIMAL(26,8)	DEFAULT 0,
	"DivRemDistAfectGCConIDPCal31122016"  				DECIMAL(26,8)	DEFAULT 0,--DivRemDistAfectGCConIDPCal31122016
	"DivRemDistAfectGCConIDPCVOL"  						DECIMAL(26,8)	DEFAULT 0,--DivRemDistAfectGCConIDPCVOL
	"DivRemDistAfectGCSINDC" 							DECIMAL(26,8)	DEFAULT 0, --DivRemDistAfectGCSINDC
	"DisRemRentasTribCProvRap_C96"  					DECIMAL(26,8)	DEFAULT 0,--DisRemRentasTribCProvRap_C96
	"DisRemRentasTribCORP_C97"  						DECIMAL(26,8)	DEFAULT 0,--DisRemRentasTribCORP_C97
	"DisRemRentasTribCExDD_C98"  						DECIMAL(26,8)	DEFAULT 0,--DisRemRentasTribCExDD_C98
	"DisRemRentasTribCUtAFFut_C99"  					DECIMAL(26,8)	DEFAULT 0,--DisRemRentasTribCUtAFFut_C99	
	"DivRemDistNoRentaFUT"  							DECIMAL(26,8)	DEFAULT 0,--DivRemDistNoRentaFUT_C70
	"DisRemRentasExIGCAfecImptoAdi_C100"  				DECIMAL(26,8)	DEFAULT 0,--DisRemRentasExIGCAfecImptoAdi_C100
	"CredImpGloCompNSRSD"  								DECIMAL(26,8)	DEFAULT 0,--CredImpGloCompNSRSD_C71  
	"CredImpGloCompNSRCD"  								DECIMAL(26,8)	DEFAULT 0,--CredImpGloCompNSRCD_C72
	"CredIGCAcumD01012017RANSRD01012020SDD_C101"  		DECIMAL(26,8)	DEFAULT 0,--CredIGCAcumD01012017RANSRD01012020SDD_C101
	"CredIGCAcumD01012017RANSRD01012020CDD_C102"  		DECIMAL(26,8)	DEFAULT 0,--CredIGCAcumD01012017RANSRD01012020CDD_C102
	"CredImpGloCompSRSD"  								DECIMAL(26,8)	DEFAULT 0,--CredImpGloCompSRSD_C73
	"CredImpGloCompSRCD"  								DECIMAL(26,8)	DEFAULT 0,--CredImpGloCompSRCD_C74
	"CredIGCAcumD01012017RXSRSDD_C103"  				DECIMAL(26,8)	DEFAULT 0,--CredIGCAcumD01012017RXSRSDD_C103
	"CredIGCAcumD01012017RXSRCDD_C104"  				DECIMAL(26,8)	DEFAULT 0,--CredIGCAcumD01012017RXSRCDD_C104
	"CredImpGloCompCTDCIF"  							DECIMAL(26,8)	DEFAULT 0,--CredImpGloCompCTDCIF_C75
	"CredImpGloCompCDD31122016"  						DECIMAL(26,8)	DEFAULT 0,--CredImpGloCompCDD31122016_C76
	"CredImpGloCompSDD31122016"  						DECIMAL(26,8)	DEFAULT 0,--CredImpGloCompSDD31122016_C77
	"CredIGCAcumH31122016RXSDD_C105"  					DECIMAL(26,8)	DEFAULT 0,--CredIGCAcumH31122016RXSDD_C105
	"CredIGCAcumH31122016RXCDD_C106"  					DECIMAL(26,8)	DEFAULT 0,--CredIGCAcumH31122016RXCDD_C106
	"CredImpGloCompCTDCIF31122016"  					DECIMAL(26,8)	DEFAULT 0,--CredImpGloCompCTDCIF31122016_C78
	"CredImpGloCompCITAExArt21"  						DECIMAL(26,8)	DEFAULT 0,--CredImpGloCompCITAExArt21_C79 
	"DetBenNetoPerRutFdo_C107" 							NVARCHAR(40)	DEFAULT'',--DetBenNetoPerRutFdo_C107
	"DetBenNetoPerPOS_C113"  							DECIMAL(26,8)	DEFAULT 0,--DetBenNetoPerPOS_C113
	"DetBenNetoPerNEG_C114"  							DECIMAL(26,8)	DEFAULT 0,--DetBenNetoPerNEG_C114
	"DividendosExentosIGCoAdicional"					DECIMAL(26,8)	DEFAULT 0,--antes
	"DividendosExentosIGCoAdicionalActualizado"			DECIMAL(26,8)	DEFAULT 0,
	"DividendosNoConstitutivosDeRenta"					DECIMAL(26,8)	DEFAULT 0,
	"DividendosNoConstitutivosDeRentaActualizado"		DECIMAL(26,8)	DEFAULT 0,
	"DivRemDistNoRentaFE" 								DECIMAL(26,8)	DEFAULT 0,--DivRemDistNoRentaFE_C31
	"DividendosAfectosImptoUnicoTasa10"					DECIMAL(26,8)	DEFAULT 0,
	"DividendosAfectosImptoUnicoTasa10Actualizado"		DECIMAL(26,8)	DEFAULT 0,
	"DividendosAfectosImptoUnicoTasa4"					DECIMAL(26,8)	DEFAULT 0,
	"DividendosAfectosImptoUnicoTasa4Actualizado"		DECIMAL(26,8)	DEFAULT 0,
	"DistribucionXDiminucionNoImputadaCapital"			DECIMAL(26,8)	NOT NULL,
	"DevolucionesDeCapital"								DECIMAL(26,8)	DEFAULT 0,
	"DevolucionesDeCapitalActualizado"					DECIMAL(26,8)	DEFAULT 0,
	"MontoInversionenCuotasCFIAnterior93"				DECIMAL(26,8)	NOT NULL,
	"FactorActualizacion"								DECIMAL(26,8)	DEFAULT 0,
	"FechaOrigenInversion"								DATE			DEFAULT '1900-01-01',

	PRIMARY KEY ("IdDetalleCertificadoClienteDJ1922N43")
);

ALTER TABLE "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1922N43"
    ADD CONSTRAINT "FK_CertificadoCliente_DetalleCertificadoClienteDJ1922N43" FOREIGN KEY ("IdCertificadoCliente")
        REFERENCES "BSCL.Normativa.BD::TRI.CertificadoCliente" ("IdCertificadoCliente")
        ON DELETE RESTRICT ON UPDATE RESTRICT;









--Limpiar todas las versiones        
DELETE FROM "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1922";
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura"  WHERE "Esquema"='DJ1922';-- AND "Version"='2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"  WHERE "Esquema"='DJ1922';-- AND "Version"='2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1922';-- and "Version" = '2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema"='DJ1922';-- AND "Version"='2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1922';-- AND "Version"='2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Documento" where "Esquema" = 'DJ1922';-- and "Version"='2016/12';
DELETE FROM "BSCL.Normativa.BD::DOC.Parametro" where "Esquema" = 'DJ1922';-- and "Version"='2016/12';


DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1922';-- AND "Version"='2016/12';

DELETE FROM "BSCL"."BSCL.Normativa.BD::TRI.ControlProceso" WHERE  "Esquema" = 'DJ1922';

INSERT INTO "BSCL.Normativa.BD::DOC.Esquema" ("Esquema","Version","Modulo","Periodicidad","Objetivo","PeriodoInicio",
		     "IdEntidadReguladora","FechaPresentacionLimite","FechaDatosDesde","FechaDatosHasta",
		     "CodigoVersionSII","HabilitadoMantenedor","TieneCertificado")
VALUES
('DJ1922','2020','TRI','A','Declaracion Jurada anual sobre informacion de fondos de terceros presentada por administradoras','2020-01-01','SII','2020-01-01','2020-01-01','2020-12-31',0,'S','S');


insert into "BSCL.Normativa.BD::DOC.Documento"
("IdDocumento","IdTipoDocumento","Esquema","Version","IdEmpresa","FechaLimite","IdEstadoDocumento",
"FechaCreacion","FechaPresentacion","DocumentoIdentidadDeclarante",
"DocumentoIdentidadRepresentante","DireccionElectronicaDeclaracion",
"DireccionFisicaDeclaracion","ComunaDeclaracion","TelefonoDeclaracion",
"FolioDeclaracion","EnProceso","FechaInicioUltimoProceso","AnoMes")
SELECT "BSCL"."BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL AS "IdDocumento",
'DECLACORR' AS "IdTipoDocumento"
,'DJ1922' AS "Esquema"
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
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'ID', '', '', 1, 'Secuencia', '', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'Numero', 'C2', '', 2, 'Numero', '', 'ID', 0, '1', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '1. DATOS DE LOS INFORMADOS', 1, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'RutAdministradora', 'C3', '', 3, 'RUT DE SOC. ADMINISTRADORA', '', 'RUT', 0, '2', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 2, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'RutFondo', 'C4', '', 4, 'RUT DEL FONDO MUTUO ', '', 'RUT', 0, '3', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 3, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'RunFondo', 'C80', '', 5, 'RUN DEL FONDO MUTUO ', '', 'RUT', 0, '4', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 4, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'Fondo', '', '', 6, 'Fondo', '', 'TEXT', 100, '5', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 5, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'TipoFondo', 'C5', 'TipoFondo1922', 7, 'TIPO DE FONDO', '', 'NEMO', 100, '6', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 6, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'NombreParticipe', 'C6', '', 8, 'NOMBRE O RAZON SOCIAL DEL PARTICIPE O APORTANTE', '', 'TEXT', 250, '7', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 7, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'RutCliente', 'C7', '', 9, 'RUT DEL PARTICIPE', '', 'RUT_CLIENTE', 0, '8', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '1. DATOS DE LOS INFORMADOS', 8, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'TaxId', 'C8', '', 10, 'TAX-ID', '', 'TEXT', 250, '9', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 9, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CodigoPais', 'C9', 'CodigoPais1922', 11, 'CODIGO PAIS DE RESIDENCIA DEL PARTICIPE O APORTANTE', '', 'NEMO', 100, '10', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 10, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'TipoOperacion', 'C10', 'TipoOperacionFondos1922', 12, 'TIPO DE OPERACION', '', 'NEMO', 100, '11', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 11, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'FechaInversion', 'C11', '', 13, 'FECHA DE LA OPERACION', '', 'DATE_FILTER', 0, '12', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. INFORMACION DE LOS APORTES AL FONDO', 12, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'MonedaReajuste', 'C12', 'TipoReajuste1922', 14, 'MONEDA O UNIDAD DE REAJUSTE', '', 'NEMO', 100, '13', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. INFORMACION DE LOS APORTES AL FONDO', 13, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'MontoNominalAportes', '', '', 15, 'MONTO NOMINAL DE  APORTES AL FONDO EN   $', '', 'AMOUNT', 0, '0', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. INFORMACION DE LOS APORTES AL FONDO', 14, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'MontoNominalAportesActualizado', 'C13', '', 16, 'MONTO NOMINAL DE  APORTES AL FONDO EN  $ Actualizado', '', 'AMOUNT', 0, '14', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. INFORMACION DE LOS APORTES AL FONDO', 15, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'NumeroSerieCuotas', 'C14', '', 17, 'NUMERO DE SERIE DE CUOTAS DE PROPIEDAD DEL PARTICIPE O APORTANTE', '', 'ID', 0, '15', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. INFORMACION DE LOS APORTES AL FONDO', 16, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CantidadCuotasParticipe', 'C15', '', 18, 'NUMERO DE CUOTAS DE LA SERIE DE PROPIEDAD DEL PARTICIPE O APORTANTE', '', 'AMOUNT', 0, '16', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. INFORMACION DE LOS APORTES AL FONDO', 17, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'ParticipeCumpleRequisitos', 'C16', 'ParticipeCumpleRequisitos1922', 19, 'PARTICIPE CUMPLE REQUISITOS (Art.82 N1  letra B) iii), Ley Adm.Fondos contenida Art. 1Ã‚ Ley 20.712)', '', 'ID', 0, '17', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. INFORMACION DE LOS APORTES AL FONDO', 18, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CuotasCumplenRequisitos', 'C17', 'CuotasCumplenRequisitos1922', 20, 'CUOTAS DE FONDOS QUE CUMPLEN REQUISITOS Art.82', '', 'ID', 0, '18', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. INFORMACION DE LOS APORTES AL FONDO', 19, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'ValorCuotaPesos', 'C18', '', 21, 'VALOR DE LA CUOTA ($)', '', 'AMOUNT', 0, '19', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. INFORMACION DE LOS APORTES AL FONDO', 20, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'NumCuotasSerieParticipe', 'C64', '', 22, 'VALOR DE MERCADO AL CIERRE DEL EJERCICIO', '', 'AMOUNT', 0, '20', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. INFORMACION DE LOS APORTES AL FONDO', 21, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'PorcentajeParticipacion31Dic', 'C19', '', 23, '% DE PARTICIPACION EN EL PATRIMONIO DEL FONDO AL 31.12', '', 'AMOUNT', 0, '21', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. INFORMACION DE LOS APORTES AL FONDO', 22, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'ValNominalCuotasRescatadas', 'C20', '', 24, 'VALOR NOMINAL CUOTAS RESCATADAS O ENAJENADAS', '', 'AMOUNT', 0, '22', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. DETALLE DE OPERACIONES Y MOVIMIENTOS', 23, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CantidadCuotasRescatadas', 'C21', '', 25, 'CANTIDAD DE CUOTAS RESCATADAS O ENAJENADAS', '', 'AMOUNT', 0, '23', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. DETALLE DE OPERACIONES Y MOVIMIENTOS', 24, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DiferenciaPorRescateOEnajenacion', '', '', 26, 'DIFERENCIA OBTENIDA EN EL RESCATE O ENAJENACION DE CUOTAS', '', 'AMOUNT', 0, '0', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. DETALLE DE OPERACIONES Y MOVIMIENTOS', 25, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DiferenciaPorRescateOEnajenacionActualizado', 'C22', '', 27, 'DIFERENCIA OBTENIDA EN EL RESCATE O ENAJENACION DE CUOTAS (ACTUALIZADA)', '', 'AMOUNT', 0, '24', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. DETALLE DE OPERACIONES Y MOVIMIENTOS', 26, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DifPorRescateOEnajenacionCFIAcogido107', '', '', 28, 'DIFERENCIA EN EL RESCATE DE CUOTAS DE FONDOS DE INVERSION ART.107', '', 'AMOUNT', 0, '0', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. DETALLE DE OPERACIONES Y MOVIMIENTOS', 27, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DifPorRescateOEnajenacionCFIAcogido107Actualizado', 'C23', '', 29, 'DIFERENCIA EN EL RESCATE DE CUOTAS DE FONDOS DE INVERSION ART.107 (ACTUALIZADA)', '', 'AMOUNT', 0, '25', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. DETALLE DE OPERACIONES Y MOVIMIENTOS', 28, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'ValNominalInversionaFechaLiquidacion', 'C24', '', 30, 'VALOR NOMINAL INVERSION A LA FECHA DE LIQUIDACION O DISMINUCION DE CAPITAL', '', 'AMOUNT', 0, '26', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. DETALLE DE OPERACIONES Y MOVIMIENTOS', 29, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CantCuotasCedidasPorLiquidacion', 'C25', '', 31, 'CUOTAS ENAJENADAS LIQUIDACION DEL FONDO O RESCATADAS POR DISMINUCION DE CAPITAL', '', 'AMOUNT', 0, '27', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. DETALLE DE OPERACIONES Y MOVIMIENTOS', 30, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DiferenciaValorCuotasFechaLiquidacion', '', '', 32, 'DIFERENCIA VALOR DE CUOTAS A FECHA LIQUIDACION V/S VALOR A FECHA DE ADQUISICION', '', 'AMOUNT', 0, '0', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. DETALLE DE OPERACIONES Y MOVIMIENTOS', 31, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DiferenciaValorCuotasFechaLiquidacionActualizado', 'C26', '', 33, 'DIFERENCIA VALOR DE CUOTAS A FECHA LIQUIDACION V/S VALOR A FECHA DE ADQUISICION (ACTUALIZADO)', '', 'AMOUNT', 0, '28', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. DETALLE DE OPERACIONES Y MOVIMIENTOS', 32, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'TributaSa', 'C65', '', 34, 'TRIBUTA S.A.', '', 'TEXT', 0, '29', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. DETALLE DE OPERACIONES Y MOVIMIENTOS', 33, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DivRemDistAfectGCConIDPC', 'C66', '', 35, 'DIVIDENDOS, REMESAS O DISTRIBUCIONES CON CRÃ‰DITO POR IDPC GENERADOS A CONTAR DEL 01.01.2017', '', 'AMOUNT', 0, '30', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 34, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DivRemDistAfectGCConIDPCal31122016', 'C67', '', 36, 'CON CDIVIDENDOS, REMESAS O DISTRIBUCIONES RÃ‰DITO POR IDPC ACUMULADOS HASTA EL 31.12.2016', '', 'AMOUNT', 0, '31', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 35, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DivRemDistAfectGCConIDPCVOL', 'C68', '', 37, 'DIVIDENDOS, REMESAS O DISTRIBUCIONES CON CRÃ‰DITO POR PAGO DE IDPC VOLUNTARIO', '', 'AMOUNT', 0, '32', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 36, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DivRemDistAfectGCSINDC', 'C69', '', 38, 'DIVIDENDOS, REMESAS O DISTRIBUCIONES SIN DERECHO A CRÃ‰DITO', '', 'AMOUNT', 0, '33', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 37, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DisRemRentasTribCProvRap_C96', 'C96', '', 39, 'RENTAS TRIB. CUMPLIDA RENTAS PROVENIENTES DEL REGISTRO RAP Y DIFERENCIA INICIAL DE SOCIEDAD', '', 'AMOUNT', 0, '34', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 38, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DisRemRentasTribCORP_C97', 'C97', '', 40, 'RENTAS TRIB. CUMPLIDA OTRAS RENTAS PERCIBIDAS SIN PRIORIDAD EN SU ORDEN DE IMPUTACIÃ“N', '', 'AMOUNT', 0, '35', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 39, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DisRemRentasTribCExDD_C98', 'C98', '', 41, 'RENTAS TRIB. CUMPLIDA EXCESO DISTRIBUCIONES DESPROPORCIONADAS (NÂ°9 ART.14 LETRA A)', '', 'AMOUNT', 0, '36', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 40, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DisRemRentasTribCUtAFFut_C99', 'C99', '', 42, 'RENTAS TRIB. CUMPLIDA UTILIDADES AFECTADAS CON IMPUESTO SUSTITUTIVO AL FUT', '', 'AMOUNT', 0, '37', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 41, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DivRemDistNoRentaFUT', 'C70', '', 43, 'RENTAS TRIB. CUMPLIDA RENTAS GENE. HASTA EL 31.12.1983 Y/O UTILIDADES AFECTADAS ', '', 'AMOUNT', 0, '38', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 42, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DisRemRentasExIGCAfecImptoAdi_C100', 'C100', '', 44, 'RENTAS EXENTAS DE IMPUESTO GLOBAL COMPLEMENTARIO (IGC) , AFECTAS A IMPUESTO ADICIONAL', '', 'AMOUNT', 0, '39', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 43, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DividendosExentosIGCoAdicional', '', '', 45, 'RENTAS EXENTAS DE IMPUESTO GLOBAL COMPLEMENTARIO (IGC) Y/O IMPUESTO ADICIONAL (IA)', '', 'AMOUNT', 0, '0', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 44, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DividendosExentosIGCoAdicionalActualizado', 'C28', '', 46, 'RENTAS EXENTAS DE IMPUESTO GLOBAL COMPLEMENTARIO (IGC) Y/O IMPUESTO ADICIONAL (IA) (ACTUALIZADO)', '', 'AMOUNT', 0, '40', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 45, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DividendosNoConstitutivosDeRenta', '', '', 47, 'DIVIDENDOS REMESAS O DISTRIBUCIONES NO CONSTITUTIVOS DE RENTA', '', 'AMOUNT', 0, '0', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 46, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DividendosNoConstitutivosDeRentaActualizado', 'C29', '', 48, 'DIVIDENDOS REMESAS O DISTRIBUCIONES NO CONSTITUTIVOS DE RENTA (ACTUALIZADO)', '', 'AMOUNT', 0, '41', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 47, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DivRemDistNoRentaFE', 'C31', '', 49, 'RENTAS O CANTIDADES DE FUENTE EXTRANJERA (', '', 'AMOUNT', 0, '42', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 48, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DividendosAfectosImptoUnicoTasa10', '', '', 50, 'DISTRIBUCIONES  O REMESAS AFECTAS A IMPUESTO UNICO TASA 10%', '', 'AMOUNT', 0, '0', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 49, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DividendosAfectosImptoUnicoTasa10Actualizado', 'C30', '', 51, 'DISTRIBUCIONES  O REMESAS AFECTAS A IMPUESTO UNICO TASA 10% (ACTUALIZADO)', '', 'AMOUNT', 0, '43', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 50, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DividendosAfectosImptoUnicoTasa4', '', '', 52, 'DISTRIBUCIONES  O REMESAS AFECTAS A IMPUESTO UNICO TASA 4%', '', 'AMOUNT', 0, '0', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 51, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DividendosAfectosImptoUnicoTasa4Actualizado', 'C32', '', 53, 'DISTRIBUCIONES  O REMESAS AFECTAS A IMPUESTO UNICO TASA 4% (ACTUALIZADO)', '', 'AMOUNT', 0, '44', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 52, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DistribucionXDiminucionNoImputadaCapital', 'C33', '', 54, 'DISTRIBUCIONES EFECTUADAS MEDIANTE LA DISMINUCIIN DEL VALOR DE CUOTA DEL FONDO NO IMPUTADA A CAPITAL', '', 'AMOUNT', 0, '45', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 53, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DevolucionesDeCapital', '', '', 55, 'DEVOLUCIONES DE CAPITAL', '', 'AMOUNT', 0, '0', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 54, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DevolucionesDeCapitalActualizado', 'C34', '', 56, 'DEVOLUCIONES DE CAPITAL (ACTUALIZADO)', '', 'AMOUNT', 0, '46', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', 55, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CredImpGloCompNSRSD', 'C71', '', 57, 'NO SUJ. A REST. GEN. HASTA EL 31.12.2019 SIN DER. A DEV. NO SUJETOS A REST. GEN. HASTA EL 31.12.2019', '', 'AMOUNT', 0, '47', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 56, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CredImpGloCompNSRCD', 'C72', '', 58, 'NO SUJ. A REST. GEN. HASTA EL 31.12.2019 CON DER. A DEV. NO SUJETOS A REST. GEN. HASTA EL 31.12.2019', '', 'AMOUNT', 0, '48', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 57, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CredIGCAcumD01012017RANSRD01012020SDD_C101', 'C101', '', 59, 'NO SUJ. A REST. GEN. DESDE 01.01 .2020 SIN DER. A DEV. NO SUJ. A REST. GEN.  DESDE 01.01.2020', '', 'AMOUNT', 0, '49', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 58, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CredIGCAcumD01012017RANSRD01012020CDD_C102', 'C102', '', 60, 'NO SUJ. A REST. GEN. DEL 01.01 .2020 CON DER. A DEV. NO SU. A RES. GEN. DESDE DEL 01.01.2020', '', 'AMOUNT', 0, '50', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 59, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CredImpGloCompSRSD', 'C73', '', 61, 'SUJETOS A RESTITUCIÃ“N SIN DERECHO A DEVOLUCIÃ“N SUJETOS A RESTITUCIÃ“N RENTAS AFECTAS', '', 'AMOUNT', 0, '51', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 60, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CredImpGloCompSRCD', 'C74', '', 62, 'SUJETOS A RESTITUCIÃ“N CON DERECHO A DEVOLUCIÃ“N SUJETOS A RESTITUCIÃ“N RENTAS AFECTAS', '', 'AMOUNT', 0, '52', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 61, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CredIGCAcumD01012017RXSRSDD_C103', 'C103', '', 63, 'ASOCIADOS A RENTAS EXENTAS SIN DERECHO A DEVOLUCIÃ“N SUJETOS A RESTITUCIÃ“N RENTAS EXENTAS', '', 'AMOUNT', 0, '53', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 62, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CredIGCAcumD01012017RXSRCDD_C104', 'C104', '', 64, 'ASOCIADOS A RENTAS EXENTAS CON DERECHO A DEVOLUCIÃ“N SUJETOS A RESTITUCIÃ“N RENTAS EXENTAS', '', 'AMOUNT', 0, '54', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 63, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CredImpGloCompCTDCIF', 'C75', '', 65, 'CRÃ‰DITO POR IPE', '', 'AMOUNT', 0, '55', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 64, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CredImpGloCompSDD31122016', 'C77', '', 66, 'ACUMULADOS HASTA EL 31.12.2016 SIN DERECHO A DEVOLUCIÃ“N RENTAS AFECTAS', '', 'AMOUNT', 0, '56', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 65, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CredImpGloCompCDD31122016', 'C76', '', 67, 'ACUMULADOS HASTA EL 31.12.2016 CON DERECHO A DEVOLUCIÃ“N RENTAS AFECTAS', '', 'AMOUNT', 0, '57', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 66, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CredIGCAcumH31122016RXSDD_C105', 'C105', '', 68, 'ACUMULADOS HASTA EL 31.12.2016 SIN DERECHO A DEVOLUCIÃ“N RENTAS EXENTAS', '', 'AMOUNT', 0, '58', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 67, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CredIGCAcumH31122016RXCDD_C106', 'C106', '', 69, 'ACUMULADOS HASTA EL 31.12.2016 CON DERECHO A DEVOLUCIÃ“N RENTAS EXENTAS', '', 'AMOUNT', 0, '59', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 68, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CredImpGloCompCTDCIF31122016', 'C78', '', 70, 'ACUMULADOS HASTA EL 31.12.2016 CRÃ‰DITO POR IPE', '', 'AMOUNT', 0, '60', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 69, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'CredImpGloCompCITAExArt21', 'C79', '', 71, 'CRÃ‰DITO POR IMPUESTO TASA ADICIONAL EX. ART. 21 LIR.', '', 'AMOUNT', 0, '61', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 70, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'MontoInversionenCuotasCFIAnterior93', 'C42', '', 72, 'MONTO INVERSION EN CUOTAS DE FONDOS DE INVERSION LEY NÂ° 18.815', '', 'AMOUNT', 0, '62', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 71, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'NumeroCertificado', 'C43', '', 73, 'NUMERO CERT.', '', 'ID', 0, '63', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', 72, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DetBenNetoPerRutFdo_C107', 'C107', '', 74, 'RUT FONDO DE INVERSIÃ“N', '', 'RUT', 0, '64', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO', 73, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DetBenNetoPerPOS_C113', 'C113', '', 75, 'BENEFICIO NETO PERCIBIDO (POSITIVO)', '', 'AMOUNT', 0, '65', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO', 74, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'DetBenNetoPerNEG_C114', 'C114', '', 76, 'BENEFICIO NETO PERCIBIDO (NEGATIVO)', '', 'AMOUNT', 0, '66', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO', 75, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'IdDocumento', '', '', 77, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'S', 'N', '7. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'VboUsuario', '', '', 78, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, '0', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '7. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'Observaciones', '', '', 79, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '0', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '7. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'FuenteDatos', '', '', 80, 'Fuente', 'Ayuda de Fuente', 'TEXT', 0, '0', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '7. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'IdCliente', '', '', 81, 'Secuencia Cliente', 'Ayuda de Secuencia Cliente', 'ID_CLIENTE', 0, '0', 'S', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '7. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'IdPortafolio', '', '', 82, 'Secuencia Portafolio', 'Ayuda de Secuencia Portafolio', 'ID_PORTAFOLIO', 0, '0', 'S', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '7. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'IdOperacion', '', '', 83, 'Id Operacion', '', 'ID', 0, '67', 'N', 'N', 'S', 'N', 'S', 'N', 'N', 'N', '7. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'IdEmisor', '', '', 84, 'Id Emisor', '', 'NEMO', 0, '0', 'N', 'N', 'S', 'N', 'S', 'N', 'N', 'N', '7. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'FactorActualizacion', '', '', 85, 'Factor Actualizacion', '', 'AMOUNT', 0, '0', 'N', 'N', 'S', 'S', 'S', 'N', 'N', 'N', '7. DATOS DE CONTROL', 76, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'FechaOrigenInversion', '', '', 86, 'Fecha Origen Inversion', '', 'DATE', 0, '0', 'N', 'N', 'S', 'S', 'S', 'N', 'N', 'N', '7. DATOS DE CONTROL', 77, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1922', '2020', 'SecEvento', '', '', 87, 'SecEvento', '', 'ID', 0, '68', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '7. DATOS DE CONTROL', 78, 0);



insert into "BSCL.Normativa.BD::DOC.Parametro"
("Esquema","Version","Codigo","Descripcion","Valor")
select
    'DJ1922' as "Esquema",
    '2020' as "Version",
    'P31' as "Codigo",
    'Tolerancia SII P31' as "Descripcion",
    '0' as "Valor"
from dummy;
insert into "BSCL.Normativa.BD::DOC.Parametro"
("Esquema","Version","Codigo","Descripcion","Valor")
select
    'DJ1922' as "Esquema",
    '2020' as "Version",
    'P84' as "Codigo",
    'Tolerancia SII P84' as "Descripcion",
    '0.2' as "Valor"
from dummy;
insert into "BSCL.Normativa.BD::DOC.Parametro"
("Esquema","Version","Codigo","Descripcion","Valor")
select
    'DJ1922' as "Esquema",
    '2020' as "Version",
    'P85' as "Codigo",
    'Tolerancia SII P85' as "Descripcion",
    '0.290322' as "Valor"
from dummy;

INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Parametro" (
"Esquema","Version","Codigo","Descripcion","Valor","EsModificableUsuario","Uso","ValorDefecto","ValoresPosibles","ModificablePorUsuario",
"ModificablePostProduccion","CreadorParametro","FechaCreacion","Observaciones","IdEmpresa")
SELECT 
  'DJ1922',"Version",'ExcluyePortafoliosAPV','Excluye portafolios con proposito APV','S','N','','N','S/N','N','N','',CURRENT_UTCTIMESTAMP,'',NULL
FROM DUMMY,(SELECT DISTINCT "Version" FROM "BSCL"."BSCL.Normativa.BD::DOC.Documento" WHERE "Esquema" = 'DJ1922');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1922','2020','UNIDADMONEDA','PESO CHILENO','CLP','1','ACT' FROM DUMMY;
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1922','2020','UNIDADMONEDA','US DOLAR','USD','2','ACT' FROM DUMMY;
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1922','2020','UNIDADMONEDA','EURO','EUR','3','ACT' FROM DUMMY;
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1922','2020','UNIDADMONEDA','UNIDAD DE FOMENTO','UF','4','ACT' FROM DUMMY;
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1922','2020','UNIDADMONEDA','INDICE VALOR PROMEDIO','IVP','5','ACT' FROM DUMMY;

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
    'DJ1922' as "Esquema",
    '2020' as "Version",
    'H1822' as "Codigo",
    'NORMA' as "Tipo",
    'N' as "Activa",
    'Debe existir valor en  las columnas: [C3], [C4], [C5], [C6],  [C7], [C8], [C9], [C10], [C11], [C12], [C13], [C14], [C15], [C16], [C17], [18], [C19]' as "Descripcion",
    'E' as "Severidad",
    '[C3], [C4], [C5], [C6],  [C7], [C8], [C9], [C10], [C11], [C12], [C13], [C14], [C15], [C16], [C17], [18], [C19]  < > blanco .o. vacio .o. 0, dependiendo del tipo de dato'  as "Formula"
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
    'DJ1922' as "Esquema",
    '2020' as "Version",
    'H1839' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'El campo "Tipo de fondo" informado debe ser: I,F,P' as "Descripcion",
    'E' as "Severidad",
    '(C5) = {I .o. F .o. P} '  as "Formula"
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
    'DJ1922' as "Esquema",
    '2020' as "Version",
    'H1840' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'El campo "Codigo Pais de Residencia del Participe o Aportente" informado debe corresponder a listado de paises del suplemento' as "Descripcion",
    'E' as "Severidad",
    '(C9) = {Tabla codigo pais respectivo} '  as "Formula"
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
    'DJ1922' as "Esquema",
    '2020' as "Version",
    'H1841' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'El campo "Tipo de Operacion" debe ser: 1,2,3,4,5,6,7' as "Descripcion",
    'E' as "Severidad",
    '(C10) = { 1 .o. 2 .o. 3 .o. 4 .o. 5.o. 6 o. 7}'  as "Formula"
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
    'DJ1922' as "Esquema",
    '2020' as "Version",
    'H1842' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si el campo "Tipo de Operacion" es igual a "7", entonces: C3,C4,C6,C7,C11,C12,C13,C15,C18,C64 <> blanco, vacio o cero .y. [C24], [C25], [C26],[C66], [C67],[C68], [C69], [C96], [C97],[C98], [C99], [C100], [C28], [C29], [C70], [C31], [C30], [C32], [C33],[C34] = blanco .o. vacio .o. 0' as "Descripcion",
    'E' as "Severidad",
    'Si (C10) = 7 , entonces: C3,C4,C6,C7,C11,C12,C13,C15,C18,C64 <> blanco, vacio o cero .y. [C24], [C25], [C26],[C66], [C67],[C68], [C69], [C96], [C97],[C98], [C99], [C100], [C28], [C29], [C70], [C31], [C30], [C32], [C33],[C34] = blanco .o. vacio .o. 0'  as "Formula"
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
    'DJ1922' as "Esquema",
    '2020' as "Version",
    'H1843' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si el campo "Tipo de Operacion" es igual a "2" o "3", entonces los campos  (C24), (C25) > 0.y. (C26) <> 0 .o. vacio C26 puede ser negativo ' as "Descripcion",
    'E' as "Severidad",
    'Si (C10) = 2 .o. 3, entonces (C24), (C25) > 0.y. (C26) <> 0 .o. vacio C26 puede ser negativo '  as "Formula"
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
    'DJ1922' as "Esquema",
    '2020' as "Version",
    'H1844' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si el campo "Tipo de Operacion" es igual a 4, entonces los campos  (C20), (C21) > 0 , (C22)  y (C23) <> Blanco .o. vacio' as "Descripcion",
    'E' as "Severidad",
    'Si (C10) = 4, entonces: (C20), (C21) > 0 , (C22)  y (C23) <> Blanco .o. vacio'  as "Formula"
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
    'DJ1922' as "Esquema",
    '2020' as "Version",
    'H1845' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si el campo "Tipo de Operacion" es igual a 6, entonces los campos Devoluciones de Capital no debe estar en blanco.' as "Descripcion",
    'E' as "Severidad",
    'Si (C10) = 6, entonces: (C34) <> Blanco'  as "Formula"
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
    'DJ1922' as "Esquema",
    '2020' as "Version",
    'H1846' as "Codigo",
    'NORMA' as "Tipo",
    'N' as "Activa",
    'La suma de los valores declarados en las columnas Creditos para G. Complem. o Adic. Impto. 1a Categoria (Afectos con y sin derecho a devolucion y Exentos con y sin derecho a devolucion)   menos un monto de tolerancia determinado por el SII, debe ser menor o igual que la suma de los montos Dividendos remesas o distribuciones Reajustados afectas y exentas, mas el incremento por impuesto de primera categoria y el credito por impuestos externos por la tasa del impuesto de Primera Categoria del AT' as "Descripcion",
    'E' as "Severidad",
    '[C36] + [C37] + [C38] + [C39]-P31 a REDONDEAR([C27] + [C28] + [C35]+[C40])*P84'  as "Formula"
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
    'DJ1922' as "Esquema",
    '2020' as "Version",
    'H1847' as "Codigo",
    'NORMA' as "Tipo",
    'N' as "Activa",
    'La suma de los valores declarados en las columnas Creditos para G. Complem. o Adic. Impto. 1ra Categoria (Afectos y Exentos)  menos un monto de tolerancia determinado por el SII, debe ser menor o igual que la suma de los montos Dividendos Remesas o Distribuciones Reajustadas afectas y exentas , mas el incremento por impuesto de primera categoria por la tasa del impuesto de Primera Categoria del AT' as "Descripcion",
    'E' as "Severidad",
    '[C36] + [C38]-P31 a REDONDEAR([C27] + [C28] + [C35])*P84'  as "Formula"
from dummy;
/*
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
    'DJ1922' as "Esquema",
    '2020' as "Version",
    'H1848' as "Codigo",
    'NORMA' as "Tipo",
    'N' as "Activa",
    'El valor declarado en la columna Incremento por Impuesto Primera Categoria menos un monto  de tolerancia determinado por el SII, debe ser menor o igual que la suma de las columnas creditos para GC o adic. por Impto. 1ra Categoria afectos a GC o adic (con y sin derecho a devolucion) y creditos para GC o adic. por Impto. 1ra Categoria exentos a GC o adic (con y sin derecho a devolucion) ' as "Descripcion",
    'E' as "Severidad",
    '[C35] Ã¢â‚¬â€œ P31 a [C36]+ [C37] + [C38] + [C39]'  as "Formula"
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
    'DJ1922' as "Esquema",
    '2020' as "Version",
    'H1849' as "Codigo",
    'NORMA' as "Tipo",
    'N' as "Activa",
    'El valor declarado en la columna Incremento por Impuesto Primera Categoria menos un monto  de tolerancia determinado por el SII, debe ser menor o igual que la suma de las columnas creditos para GC o adic. por Impto. 1Ã‚Â° Categoria afectos a GC o adic y creditos para GC o adic. por Impto. 1Ã‚Â° Categoria exentos a GC o adic' as "Descripcion",
    'E' as "Severidad",
    '[C35] Ã¢â‚¬â€œ P31 a [C36]+ [C38]'  as "Formula"
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
    'DJ1922' as "Esquema",
    '2020' as "Version",
    'H1850' as "Codigo",
    'NORMA' as "Tipo",
    'N' as "Activa",
    'El valor declarado en la columna Incremento por Impuesto Primera Categoria  menos P31 debe ser menor o igual que Monto de Dividendos Remesas o Distribuciones Reajustadas afectos mas Monto de Dividendos Remas o Distribuciones exentas y el credito por Impuestos Externos por factor de incremento del AT' as "Descripcion",
    'E' as "Severidad",
    '[C35] Ã¢â‚¬â€œ P31 a ([C27]+[C28] +[C40] )* P85'  as "Formula"
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
    'DJ1922' as "Esquema",
    '2020' as "Version",
    'H1851' as "Codigo",
    'NORMA' as "Tipo",
    'N' as "Activa",
    'El valor declarado en la columna Incremento por Impuesto Primera Categoria menos P31 debe ser menor o igual que Monto de Dividendos Remesas o Distribuciones Reajustadas afectas mas Monto de Dividendos Remesas o Distribuciones  exentas por factor de incremento del AT' as "Descripcion",
    'E' as "Severidad",
    '[C35] Ã¢â‚¬â€œ P31 Ã¢â€°Â¤ ([C27]+[C28] )* P85'  as "Formula"
from dummy;
*/
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
    'DJ1922' as "Esquema",
    '2020' as "Version",
    'H1855' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si el campo "Tipo de Operacion" es igual a 5, entonces EL campos Distribuciones o Remesas afectas a Impuesto unico Tasa 4% DEBE SER MAYOR QUE CERO ' as "Descripcion",
    'E' as "Severidad",
    'Si (C10) = 5, entonces: (C32) > 0'  as "Formula"
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
    'DJ1922' as "Esquema",
    '2020' as "Version",
    'H1856' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si tipo de fondo por Informado es F .o. P, entonces Monto Inversion en cuotas de Fondos de Inversion Ley N?18.815, adquiridas antes del 04.06.93 (Ex. Art. 32, Ley N? 18.815) debe ser igual a cero ' as "Descripcion",
    'E' as "Severidad",
    '(C5) = F .o. P, entonces (C42) = 0'  as "Formula"
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
    'DJ1922' as "Esquema",
    '2020' as "Version",
    'H1858' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si los campos "Participe cumple requisitos Art. 82 N?1 letra B) iii) Ley adm fondos contenida Art. 82 Ley 20712" y "Cuotas de fondos que cumplen requisitos art. 82 letra B) iii), Ley Adm. Fondos Contenida Art. 82 Ley 20712)" son iguales a 1, entonces el campo Distribuciones o Remesas no afectas a Impuesto unico Tasa 10% tiene que ser mayor que cero' as "Descripcion",
    'E' as "Severidad",
    '(C16) y (C17) = 1, entonces (C31) > 0'  as "Formula"
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
    'DJ1922' as "Esquema",
    '2020' as "Version",
    'H1859' as "Codigo",
    'NORMA' as "Tipo",
    'N' as "Activa",
    'Los campos: "Rut de sociedad administradora", "Rut del fondo","RUN del fondo"  y "rut del participe o aportante",  deben cumplir con modulo 11 (verificacion digito verificador)' as "Descripcion",
    'E' as "Severidad",
    '(C3), (C4), (C7)  (C80)= verificacion digito verificador'  as "Formula"
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
    'DJ1922' as "Esquema",
    '2020' as "Version",
    'H1860' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'El campo "Tax ID"  no debe contener como valor  55.555.555-5' as "Descripcion",
    'E' as "Severidad",
    '(C8) <> 55.555.555-5'  as "Formula"
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
    'DJ1922' as "Esquema",
    '2020' as "Version",
    'H1862' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'El campo "Moneda o Unidad de Reajuste" debe tomar valor 1, 2, 3, 4, 5 o 6' as "Descripcion",
    'E' as "Severidad",
    '(C12) = { 1 .o. 2 .o. 3 .o. 4 .o. 5.o. 6} '  as "Formula"
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
    'DJ1922' as "Esquema",
    '2020' as "Version",
    'H1863' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'El campo  "% de participacion en el patrimonio del fondo al 31.12"  debe ser un valor positivo y hasta 100 como valor maximo, usando como maximo dos decimales.' as "Descripcion",
    'E' as "Severidad",
    '(C19) > 0 y (C19) < 100, max. 2 decimales'  as "Formula"
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
    'DJ1922' as "Esquema",
    '2020' as "Version",
    'H1864' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'El campo "Numero de cuotas de la serie de propiedad del aportante o participe " debe tener un valor mayor o igual a cero' as "Descripcion",
    'E' as "Severidad",
    '(C15) > = 0'  as "Formula"
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
    'DJ1922' as "Esquema",
    '2020' as "Version",
    'H1865' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'El campo "Participe cumple requisitos (art. 82 letra B)  iii) ley Adm. Fondos contenidas Art. 82 ley 20712" debe tomar valor 1, 2, o 3' as "Descripcion",
    'E' as "Severidad",
    '(C16) = { 1 .o. 2 .o. 3}'  as "Formula"
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
    'DJ1922' as "Esquema",
    '2020' as "Version",
    'H1866' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'El campo "Cuotas de fondos que cumple requisitos (art. 82 letra B) iii)  ley Adm. Fondos contenida art. 82 ley 20712)" debe tomar valor 1, 2, o 3' as "Descripcion",
    'E' as "Severidad",
    '(C17) = { 1 .o. 2 .o. 3} '  as "Formula"
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
    'DJ1922' as "Esquema",
    '2020' as "Version",
    'H2043' as "Codigo",
    'NORMA' as "Tipo",
    'N' as "Activa",
    'Si ingresa monto en alguna de las columnas de credito afecto a GC o Adicional, debe existir monto en la columna de dividendos  remesas o   distribuciones reajustado afectas a GC adicional.' as "Descripcion",
    'E' as "Severidad",
    'Si (C36 + C37 > 0 ),  entonces:  C27>0 '  as "Formula"
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
    'DJ1922' as "Esquema",
    '2020' as "Version",
    'H2044' as "Codigo",
    'NORMA' as "Tipo",
    'N' as "Activa",
    'Si ingresa monto en alguna de las columnas de credito afecto a GC o Adicional, debe existir monto en la columna de dividendos  remesas o   distribuciones reajustado exentas de  GC adicional.' as "Descripcion",
    'E' as "Severidad",
    'Si (C38 + C39 > 0 ),  entonces:  C28>0'  as "Formula"
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
    'DJ1922' as "Esquema",
    '2020' as "Version",
    'H2668' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'El Campo Tributa como S.A debe corresponde a: SI: Fondo debe tributar como S.A. (conforme a lo dispuesto en el art. 92 de la LUF).NO. Fondo No debe tributar como S.A.' as "Descripcion",
    'E' as "Severidad",
    'C65 = SI .o. No'  as "Formula"
from dummy;





--CONTROL PROCESO

insert into "BSCL"."BSCL.Normativa.BD::TRI.ControlProceso"
("Esquema","Version","EstadoCertificado","Carga","Validacion","PreDeclaracion","Certificacion")
SELECT "Esquema","Version",'NGE','N','N','N','N' FROM "BSCL"."BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1922' AND "Version" IN ('2020');



----SERVICIOS
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3581, 'BSCL.Normativa.BD.Servicios::TRI.DJ1922.CargarPorcentajeAccionarioFondo', 1, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3581);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3307, 'BSCL.Normativa.BD.Vistas::TRI.DJ1922.Invalidos', 0, 'Q', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3307);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3306, 'BSCL.Normativa.BD.Vistas::TRI.DJ1922.Datos', 73, 'Q', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3306);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3582, 'BSCL.Normativa.BD.Servicios::TRI.DJ1922.CargarFondos', 2, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3582);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3308, 'BSCL.Normativa.BD.Servicios::TRI.DJ1922.EliminarDetalles', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3308);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3309, 'BSCL.Normativa.BD.Servicios::TRI.DJ1922.GrabarDetalle', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3309);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3585, 'BSCL.Normativa.BD.Servicios::TRI.DJ1922.ActualizarValorFactorIpc', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3585);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3583, 'BSCL.Normativa.BD.Servicios::TRI.DJ1922.CambiarEstadoFondo', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3583);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3584, 'BSCL.Normativa.BD.Servicios::TRI.DJ1922.GenerarCertificado43', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3584);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3586, 'BSCL.Normativa.BD.Servicios::TRI.DJ1922.GenerarDocumentoCertificado43', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3586);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3310, 'BSCL.Normativa.BD.Servicios::TRI.DJ1922.ObtenerTotales', 52, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3310);


-----DICCIONARIO



DELETE  FROM BSCL."BSCL.Normativa.BD::DOC.Diccionario" WHERE "Tipo" LIKE '%1922%';
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CuotasCumplenRequisitos1922', '1', 1, 'Cuotas cumplen requisitos Art. 82 ', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CuotasCumplenRequisitos1922', '2', 2, 'Cuotas NO cumplen requisitos Art. 82 ', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CuotasCumplenRequisitos1922', '3', 3, 'No Aplica', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TipoFondo1922', 'I', 1, 'Fondos de InversiÃ³n PÃºblicos', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TipoFondo1922', 'F', 2, 'Fondos Mutuos', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TipoFondo1922', 'P', 3, 'Fondos de Inversion Privados', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('ParticipeCumpleRequisitos1922', '1', 1, 'No tiene extranjero domiciliado con %5 participacion', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('ParticipeCumpleRequisitos1922', '2', 2, 'Si tiene extranjero domiciliado con %5 participacion', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('ParticipeCumpleRequisitos1922', '3', 3, 'No Aplica', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TipoOperacionFondos1922', '1', 1, 'Reparto de dividendos, remesas o distribuciones de Fondos de InversiÃ³n o Fondos Mutuos', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TipoOperacionFondos1922', '2', 2, 'LiquidaciÃ³n del Fondo de InversiÃ³n', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TipoOperacionFondos1922', '3', 3, 'DisminuciÃ³n de capital del Fondo de InversiÃ³n', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TipoOperacionFondos1922', '4', 4, 'Rescate o enajenaciÃ³n de cuotas de Fondo de InversiÃ³n', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TipoOperacionFondos1922', '5', 5, 'Cantidades distribuidas a partÃ­cipes sin domicilio ni residencia en Chile', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TipoOperacionFondos1922', '6', 6, 'Devoluciones de capital de Fondos de InversiÃ³n y Fondos Mutuos', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TipoOperacionFondos1922', '7', 7, 'Sin operaciones durante el ejercicio comercial', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'AF', 1, 'AfganistÃ¡n', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'AL', 2, 'Albania', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'DE', 3, 'Alemania', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'AD', 4, 'Andorra', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'AO', 5, 'Angola', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'AI', 6, 'Anguila', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'AQ', 7, 'AntÃ¡rtida', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'AG', 8, 'Antigua y Barbuda', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'SA', 9, 'Arabia Saudita', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'DZ', 10, 'Argelia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'AR', 11, 'Argentina', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'AM', 12, 'Armenia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'AW', 13, 'Aruba', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'AU', 14, 'Australia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'AT', 15, 'Austria', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'AZ', 16, 'AzerbaiyÃ¡n', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'BS', 17, 'Bahamas', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'BH', 18, 'Bahrein', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'BD', 19, 'Bangladesh', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'BB', 20, 'Barbados', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'BE', 21, 'BÃ©lgica', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'BZ', 22, 'Belice', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'BJ', 23, 'BenÃ­n', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'BM', 24, 'Bermudas', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'BY', 25, 'Bielorrusia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'BO', 26, 'Bolivia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'BQ', 27, 'Bonaire, San Eustaquio y Saba', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'BA', 28, 'Bosnia y Herzegovina', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'BW', 29, 'Botswana', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'BR', 30, 'Brasil', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'BN', 31, 'BrunÃ©i Darussalam', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'BG', 32, 'Bulgaria', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'BF', 33, 'Burkina Faso', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'BI', 34, 'Burundi', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'BT', 35, 'ButÃ¡n', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'CV', 36, 'Cabo Verde', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'KH', 37, 'Camboya', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'CM', 38, 'CamerÃºn', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'CA', 39, 'CanadÃ¡', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'TD', 40, 'Chad', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'CL', 41, 'Chile', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'CN', 42, 'China', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'CY', 43, 'Chipre', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'VA', 44, 'Ciudad del Vaticano', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'CO', 45, 'Colombia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'KM', 46, 'Comoras', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'CG', 47, 'Congo', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'KP', 48, 'Corea del Norte', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'KR', 49, 'Corea del Sur', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'CI', 50, 'Costa de Marfil', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'CR', 51, 'Costa Rica', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'HR', 52, 'Croacia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'CU', 53, 'Cuba', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'CW', 54, 'Curazao', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'DK', 55, 'Dinamarca', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'DM', 56, 'Dominica', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'EC', 57, 'Ecuador', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'EG', 58, 'Egipto', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'SV', 59, 'El Salvador', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'AE', 60, 'Emiratos Ã?rabes Unidos', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'ER', 61, 'Eritrea', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'GB', 62, 'Escocia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'SK', 63, 'Eslovaquia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'SI', 64, 'Eslovenia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'ES', 65, 'EspaÃ±a', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'US', 66, 'Estados Unidos', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'EE', 67, 'Estonia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'ET', 68, 'EtiopÃ­a', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'RU', 69, 'FederaciÃ³n Rusa (Rusia)', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'PH', 70, 'Filipinas', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'FI', 71, 'Finlandia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'FJ', 72, 'Fiyi', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'FR', 73, 'Francia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'GA', 74, 'GabÃ³n', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'GM', 76, 'Gambia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'GE', 77, 'Georgia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'GH', 78, 'Ghana', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'GI', 79, 'Gibraltar', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'GD', 80, 'Granada', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'GR', 81, 'Grecia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'GL', 82, 'Groenlandia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'GP', 83, 'Guadalupe (Francia)', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'GU', 84, 'Guam (EE.UU.)', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'GT', 85, 'Guatemala', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'GF', 86, 'Guayana Francesa', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'GG', 87, 'Guernsey', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'GN', 88, 'Guinea', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'GW', 89, 'Guinea Bissau', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'GQ', 90, 'Guinea Ecuatorial', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'GY', 91, 'Guyana', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'HT', 92, 'HaitÃ­', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'NL', 93, 'Holanda', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'HN', 94, 'Honduras', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'HK', 95, 'Hong Kong', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'HU', 96, 'HungrÃ­a', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'IN', 97, 'India', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'ID', 98, 'Indonesia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'IR', 100, 'IrÃ¡n', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'IQ', 101, 'Iraq', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'IE', 102, 'Irlanda', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'BV', 103, 'Isla Bouvet', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'IM', 104, 'Isla de Man', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'CX', 105, 'Isla de Navidad', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'MF', 106, 'Isla de San MartÃ­n (parte francesa)', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'SX', 107, 'Isla de San MartÃ­n (parte holandesa)', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'GS', 108, 'Isla Georgia del Sur e Islas Sandwich del Sur', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'NF', 109, 'Isla Norfolk', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'PN', 110, 'Isla Pitcairn', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'IS', 111, 'Islandia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'AX', 112, 'Islas Ã…land', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'KY', 113, 'Islas CaimÃ¡n', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'CC', 114, 'Islas Cocos', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'CK', 115, 'Islas Cook', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'FK', 116, 'Islas Falkland (Malvinas)', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'FO', 117, 'Islas Faroe', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'HM', 118, 'Islas Heard y McDonald', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'MP', 119, 'Islas Marianas del Norte', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'MH', 120, 'Islas Marshall', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'UM', 121, 'Islas Menores de Estados Unidos de Ultramar', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'SB', 122, 'Islas SalomÃ³n', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'SJ', 123, 'Islas Svalbard y Jan Mayen', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'TC', 124, 'Islas Turcas y Caicos', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'VI', 125, 'Islas VÃ­rgenes Americanas', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'VG', 126, 'Islas VÃ­rgenes BritÃ¡nicas', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'WF', 127, 'Islas Wallis y Futuna', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'IL', 128, 'Israel', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'IT', 129, 'Italia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'JM', 130, 'Jamaica', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'JP', 131, 'JapÃ³n', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'JE', 132, 'Jersey', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'JO', 133, 'Jordania', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'KZ', 134, 'KazajistÃ¡n', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'KE', 135, 'Kenia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'KG', 136, 'KirguistÃ¡n', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'KI', 137, 'Kiribati', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'KW', 138, 'Kuwait', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'LA', 139, 'Laos', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'LS', 140, 'Lesoto', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'LV', 141, 'Letonia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'LB', 142, 'LÃ­bano', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'LR', 143, 'Liberia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'LY', 144, 'Libia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'LI', 145, 'Liechtenstein', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'LT', 146, 'Lituania', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'LU', 147, 'Luxemburgo', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'MO', 148, 'Macao', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'MK', 149, 'Macedonia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'MG', 150, 'Madagascar', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'MY', 151, 'Malasia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'MW', 152, 'Malawi', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'MV', 153, 'Maldivas', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'ML', 154, 'MalÃ­', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'MT', 155, 'Malta', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'MA', 156, 'Marruecos', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'MQ', 157, 'Martinica', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'MU', 158, 'Mauricio', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'MR', 159, 'Mauritania', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'YT', 160, 'Mayotte', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'MX', 161, 'MÃ©xico', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'FM', 162, 'Micronesia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'MD', 163, 'Moldavia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'MC', 164, 'MÃ³naco', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'MN', 165, 'Mongolia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'ME', 166, 'Montenegro', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'MS', 167, 'Montserrat', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'MZ', 168, 'Mozambique', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'MM', 169, 'Myanmar (Birmania)', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'NA', 170, 'Namibia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'NR', 171, 'Nauru', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'NP', 172, 'Nepal', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'NI', 173, 'Nicaragua', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'NE', 174, 'NÃ­ger', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'NG', 175, 'Nigeria', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'NU', 176, 'Niue', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'NO', 177, 'Noruega', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'NC', 178, 'Nueva Caledonia (Francia)', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'NZ', 179, 'Nueva Zelanda', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'OM', 180, 'OmÃ¡n', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'PK', 181, 'PakistÃ¡n', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'PW', 182, 'Palaos', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'PS', 183, 'Palestina', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'PA', 184, 'PanamÃ¡', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'PG', 185, 'PapÃºa Nueva Guinea', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'PY', 186, 'Paraguay', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'PE', 187, 'PerÃº', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'PF', 188, 'Polinesia (Francia)', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'PL', 189, 'Polonia (std LE)', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'PT', 190, 'Portugal', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'PR', 191, 'Puerto Rico', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'QA', 192, 'Qatar', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'CF', 194, 'RepÃºblica Centro Africana', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'CZ', 195, 'RepÃºblica Checa', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'CD', 196, 'RepÃºblica DemocrÃ¡tica del Congo', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'DO', 197, 'RepÃºblica Dominicana', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'RE', 198, 'ReuniÃ³n (Francia)', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'RW', 199, 'Ruanda', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'RO', 200, 'Rumania', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'EH', 201, 'Sahara Occidental', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'WS', 202, 'Samoa', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'AS', 203, 'Samoa Americana', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'BL', 204, 'San BartolomÃ©', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'KN', 205, 'San CristÃ³bal y Nieves', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'SM', 206, 'San Marino', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'PM', 207, 'San Pedro y MiguelÃ³n', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'VC', 208, 'San Vicente y las Granadinas', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'SH', 209, 'Santa Elena, AscensiÃ³n y TristÃ¡n de AcuÃ±a', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'LC', 210, 'Santa LucÃ­a', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'ST', 211, 'Santo TomÃ© y PrÃ­ncipe', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'SN', 212, 'Senegal', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'YU', 213, 'Serbia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'SC', 214, 'Seychelles', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'SL', 215, 'Sierra Leona', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'SG', 216, 'Singapur', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'SY', 217, 'Siria', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'SO', 218, 'Somalia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'LK', 219, 'Sri Lanka', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'SZ', 220, 'Suazilandia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'ZA', 221, 'Sudafrica', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'SD', 222, 'Sudan', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'SS', 223, 'Sudan del Sur', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'SE', 224, 'Suecia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'CH', 225, 'Suiza', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'SR', 226, 'Surinam', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'TH', 227, 'Tailandia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'TW', 228, 'Taiwan', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'TZ', 229, 'Tanzania', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'TJ', 230, 'Tayikistan', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'IO', 231, 'Territorio Britanico del Oceano Indico', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'TF', 232, 'Territorios Australes Franceses', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'TL', 233, 'Timor Oriental', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'TG', 234, 'Togo', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'TK', 235, 'Tokelau', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'TO', 236, 'Tonga', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'TT', 237, 'Trinidad y Tobago', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'TN', 238, 'Tunez', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'TM', 239, 'Turkmenistan', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'TR', 240, 'Turquia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'TV', 241, 'Tuvalu', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'UA', 242, 'Ucrania', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'UG', 243, 'Uganda', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'UY', 244, 'Uruguay', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'UZ', 245, 'UzbekistÃ¡n', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'VU', 246, 'Vanuatu', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'VE', 247, 'Venezuela', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'VN', 248, 'Vietnam', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'YE', 249, 'Yemen', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'DJ', 250, 'Yibouti', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'ZR', 251, 'Zaire', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'ZM', 252, 'Zambia', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'ZW', 253, 'Zimbabwe', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1922', 'NT', 254, 'Zona neutral', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TipoReajuste1922', '1', 1, 'Peso Chileno', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TipoReajuste1922', '2', 2, 'DÃ³lar Americano', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TipoReajuste1922', '3', 3, 'Euro', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TipoReajuste1922', '4', 4, 'Unidad de Fomento', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TipoReajuste1922', '5', 5, 'IVP', 'S', '', '');
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TipoReajuste1922', '6', 6, 'Otro', 'S', '', '');


----Certificado

DELETE FROM BSCL."BSCL.Normativa.BD::TRI.CertificadoCliente" WHERE "Esquema"='DJ1922';

DELETE FROM BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente" WHERE "Esquema"='DJ1922'; 

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
("Esquema", "Version", "UltimoFolio", "CodigoCertificado", "NombreCertificado", "CodigoPrograma")
VALUES('DJ1922', '2020', 0, 'CERTIFICADO_43', 'Certificado 43', 'GenerarCertificado43');



DELETE FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa"  WHERE "Esquema"='DJ1922';

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa"
("IdArchivoNormativa", "VersionArchivo", "Esquema", "Version", "IdTipoDocumento", "Descripcion", "NombreArchivo", "IdArchivo", "TipoArchivo", "ServicioArchivo", "IdEmpresa", "IdEstado")
VALUES('CERT43-PDF', '2020', 'DJ1922', '3', 'CERT43', 'CERTIFICADO 43 TRIBUTARIOS', 'CERTIFICADO43', '', 'pdf', '3586', 1, 'ACT');

/*PARAMETRO:  Nombre de archivo CERTIFICADOS*/
  
DELETE  FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" WHERE "IdNormativa"='DJ1922' AND "CodigoParametro" LIKE 'TRI_NOM_CERTIFICADO%';
  
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
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1922' AND "VersionArchivo"=2020
AND NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" N WHERE N."CodigoParametro"='TRI_NOM_' || "NombreArchivo" ||  '_01' AND N."IdNormativa"="Esquema");  

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Configuracion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
SELECT 1 AS "IdEmpresa",'TRI_NOM_' || "NombreArchivo" ||  '_02'AS "CodigoParametro",'Nombre del archivo PDF en  ZIP masivo de certificados.' AS "Descripcion"
,'AV' AS "Valor"
,"Esquema" AS  "IdNormativa"
,'S' AS "EsModificableUsuario"
,'Certificado masivo' AS "Uso"
,'AV'  AS "ValorDefecto"
,'[RUT],[PERIODO],[DJ],[CERT],texto libre' AS "ValoresPosibles",'S' AS "ModificablePorUsuario",'' AS  "ModificablePostProduccion", '' AS  "CreadorParametro", CURRENT_UTCTIMESTAMP AS "FechaCreacion"
,'' AS "ModificadorParametro", CURRENT_UTCTIMESTAMP AS "FechaModificacion", '' AS "Observaciones"
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1922' AND "VersionArchivo"=2020
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
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1922' AND "VersionArchivo"=2020
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
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1922' AND "VersionArchivo"=2020
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
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1922' AND "VersionArchivo"=2020
AND NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" N WHERE N."CodigoParametro"='TRI_NOM_' || "NombreArchivo" ||  '_05' AND N."IdNormativa"="Esquema");  






---complementos
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.Complemento" WHERE "Esquema"='DJ1922' AND  "CodigoComplemento"='ACTUALIZARMONTOS';

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.Complemento"
("Esquema", "CodigoComplemento", "Descripcion")
VALUES('DJ1922', 'ACTUALIZARMONTOS', 'Procesar Montos');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4290667, 'PLUGIN_NOR.jar@cl.bcs.normativa.declaraciones.DJ1922.ActualizarDetalles', 1, 'J', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4290667);


DELETE FROM BSCL."BSCL.Normativa.BD::TRI.Complemento" WHERE "Esquema"='DJ1922' AND "CodigoComplemento"='CALIFICACIONPENDIENT';

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.Complemento" ("Esquema", "CodigoComplemento", "Descripcion")
VALUES('DJ1922', 'CALIFICACIONPENDIENT', 'Calificaciones Pendientes');

   
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4290674, 'BSCL.Normativa.BD.Vistas::TRI.DJ1922.CalificacionPendiente', 0, 'Q', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4290674);
  					
  				
-- Diccionario
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )
SELECT  
'UI_PROCESAR_MONTOS',
'CL',
'Procesar Montos',
'PROMON'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Diccionario" WHERE  "CodDiccionario"='UI_PROCESAR_MONTOS');


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )
SELECT  
'UI_PROCESAR_MONTOS_SEGUN_CALIFICACIONES_TRIBUTARIAS_INGRESADAS',
'CL',
'Procesar los montos segun Calificaciones Tributarias ingresadas',
'PROCAL'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Diccionario" WHERE  "CodDiccionario"='UI_PROCESAR_MONTOS_SEGUN_CALIFICACIONES_TRIBUTARIAS_INGRESADAS');



INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )
SELECT  
'UI_100_POR_AFECTO_CUANDO_NO_HAY_CALIFICACION_TRIBUTARIA',
'CL',
'100 % afecto cuando no hay Calificacion Tributaria',
'100AFEC'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Diccionario" WHERE  "CodDiccionario"='UI_100_POR_AFECTO_CUANDO_NO_HAY_CALIFICACION_TRIBUTARIA');



INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )
SELECT  
'UI_CONFIRMA',
'CL',
'Confirma',
'conf'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Diccionario" WHERE  "CodDiccionario"='UI_CONFIRMA');


INSERT INTO BSCL."BSCL.Normativa.BD::TRI.Complemento" ("Esquema", "CodigoComplemento", "Descripcion")
SELECT 'DJ1922', 'EVENTOCAPITAL', 'Evento Capital' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Normativa.BD::TRI.Complemento" WHERE  "Esquema" = 'DJ1922' AND "CodigoComplemento" = 'EVENTOCAPITAL');


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4290676, 'BSCL.Normativa.BD.Vistas::TRI.DJ1922.DatosComplemento', 0, 'Q', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4290676);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4290677, 'BSCL.Normativa.BD.Servicios::TRI.DJ1922.AsociarEventoCapital', 4, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4290677);



UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET  "Editable"='S'
WHERE "Esquema"='DJ1922' AND "Version"='2020' AND "Nombre"='SecEvento';



INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4290708,'BSCL.Normativa.BD.Servicios::TRI.DJ1922.GrabarMasivo',0,'P','','','' 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4290708);




