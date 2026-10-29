INSERT INTO "BSCL.Portafolio.BD::POR.Portafolio" ("IdPortafolio","IdCliente","CodigoPortafolio","IdMatrizAgrupadorAPV","IdEstado","Proposito") VALUES (5001,7000,'APV-A',10,'ACT','PROPOSITOAPV');
INSERT INTO "BSCL.Portafolio.BD::POR.Portafolio" ("IdPortafolio","IdCliente","CodigoPortafolio","IdMatrizAgrupadorAPV","IdEstado","Proposito") VALUES (5002,7000,'APV-B',20,'ACT','PROPOSITOAPV');
INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" ("IdMatrizAgrupadorAPV","IdRegimenTributario") VALUES (10,30);
INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" ("IdMatrizAgrupadorAPV","IdRegimenTributario") VALUES (20,40);
INSERT INTO "BSCL.APV.BD::TRA.Traspaso" ("IdTraspaso","IdContratoSAAPV","IdEmpresa","TipoMovimiento","RazonRetiro","IdEstado") VALUES (151,500,1,'EGRESO','TRA','VIG');
INSERT INTO "BSCL.APV.BD::TRA.ConfiguracionRetencionTraspaso" ("IdConfiguracionRetencion","PorcentajeRetencion","MontoTopeRetencion","CodigoRetiro","IdPersonaRetencion","IdRegimenTributario","IdEstado") VALUES (1,15,0,'TRA',99,NULL,'VIG');
