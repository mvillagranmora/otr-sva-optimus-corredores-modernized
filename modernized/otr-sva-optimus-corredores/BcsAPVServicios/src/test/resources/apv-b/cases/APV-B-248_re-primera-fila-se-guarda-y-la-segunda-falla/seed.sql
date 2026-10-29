INSERT INTO "BSCL.SocioNegocio.BD::PER.Persona" ("IdPersona","Tipo") VALUES (117940,'N');
INSERT INTO "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" ("IdPersona","DocumentoIdentidad","Predeterminado") VALUES (117940,'15571571-5','S');
INSERT INTO "BSCL.SocioNegocio.BD::CLI.Cliente" ("IdCliente","IdPersona","IdEstado") VALUES (85050,117940,'ACT');
INSERT INTO "BSCL.APV.BD::SCS.ContratoSAAPV" ("IdContratoSAAPV","IdEmpresa","FolioContratoSAAPV","IdCliente","IdPersona","CodigoTipoSolicitud","IdEstado","IdPerfilComercial") VALUES (1013894,1,'F1013894',85050,117943,'TRA','PDI','PERFIL BCS');
INSERT INTO "BSCL.APV.BD::TRA.Traspaso" ("IdTraspaso","IdContratoSAAPV","IdEmpresa","TipoMovimiento","RazonRetiro","IdEstado","CodigoTipoTraspaso","IdCliente") VALUES (2991733,1013894,1,'INGRESO','TRA','PDI','TOT',8001);
INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" ("IdMatrizAgrupadorAPV","IdPlanTipoInversion","IdProductoAgrupador","IdRegimenTributario") VALUES (100,2,2,4);
INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" ("IdMatrizAgrupadorAPV","IdPlanTipoInversion","IdProductoAgrupador","IdRegimenTributario") VALUES (200,2,2,2);
INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" ("IdMatrizAgrupadorAPV","IdPlanTipoInversion","IdProductoAgrupador","IdRegimenTributario") VALUES (300,3,2,6);
INSERT INTO "BSCL.Portafolio.BD::POR.Portafolio" ("IdPortafolio","IdCliente","CodigoPortafolio","IdMatrizAgrupadorAPV","IdEstado","IdEstadoAutorizacion","Proposito") VALUES (5003,85050,'OTRO',300,'ACT','AUT','PROPOSITOAPV');
