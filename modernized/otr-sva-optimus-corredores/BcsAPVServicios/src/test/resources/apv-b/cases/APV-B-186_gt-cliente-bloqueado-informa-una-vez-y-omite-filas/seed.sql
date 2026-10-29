INSERT INTO "BSCL.SocioNegocio.BD::PER.Persona" ("IdPersona","Tipo") VALUES (117943,'N');
INSERT INTO "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" ("IdPersona","DocumentoIdentidad","Predeterminado") VALUES (117943,'19263099-1','S');
INSERT INTO "BSCL.SocioNegocio.BD::CLI.Cliente" ("IdCliente","IdPersona","IdEstado") VALUES (8001,117943,'BLO');
INSERT INTO "BSCL.APV.BD::SCS.ContratoSAAPV" ("IdContratoSAAPV","IdEmpresa","FolioContratoSAAPV","IdCliente","IdPersona","CodigoTipoSolicitud","IdEstado","IdPerfilComercial") VALUES (1013908,1,'F1013908',8001,117943,'TRA','PDI','PERFIL BCS');
INSERT INTO "BSCL.APV.BD::TRA.Traspaso" ("IdTraspaso","IdContratoSAAPV","IdEmpresa","TipoMovimiento","RazonRetiro","IdEstado","CodigoTipoTraspaso","IdCliente") VALUES (2991747,1013908,1,'INGRESO','TRA','PDI','TOT',8001);
INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" ("IdMatrizAgrupadorAPV","IdPlanTipoInversion","IdProductoAgrupador","IdRegimenTributario") VALUES (10,2,2,2);
INSERT INTO "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario" ("IdMatrizAgrupadorAPV","IdPropositoPortafolio","NombreAgrupador","Codigo","CodigoAgrupador","NombrePlan") VALUES (10,'PROPOSITOAPV','GRUPO 2','APV-A','GRUPO 2','I. ACTIVO');
INSERT INTO "BSCL.Portafolio.BD::POR.Portafolio" ("IdPortafolio","IdCliente","CodigoPortafolio","IdMatrizAgrupadorAPV","IdEstado","IdEstadoAutorizacion","Proposito") VALUES (5001,8001,'APV-A',10,'ACT','AUT','PROPOSITOAPV');
INSERT INTO "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" ("IdPortafolio","CodigoPortafolio","Proposito","IdCliente","IdEstado") VALUES (5001,'APV-A','PROPOSITOAPV',8001,'ACT');
