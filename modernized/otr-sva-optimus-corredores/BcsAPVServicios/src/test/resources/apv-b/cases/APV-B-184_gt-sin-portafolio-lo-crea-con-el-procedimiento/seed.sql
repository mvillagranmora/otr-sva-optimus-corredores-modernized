INSERT INTO "BSCL.SocioNegocio.BD::PER.Persona" ("IdPersona","Tipo") VALUES (117943,'N');
INSERT INTO "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" ("IdPersona","DocumentoIdentidad","Predeterminado") VALUES (117943,'19263099-1','S');
INSERT INTO "BSCL.SocioNegocio.BD::CLI.Cliente" ("IdCliente","IdPersona","IdEstado") VALUES (8001,117943,'ACT');
INSERT INTO "BSCL.APV.BD::SCS.ContratoSAAPV" ("IdContratoSAAPV","IdEmpresa","FolioContratoSAAPV","IdCliente","IdPersona","CodigoTipoSolicitud","IdEstado","IdPerfilComercial") VALUES (1013908,1,'F1013908',8001,117943,'TRA','PDI','PERFIL BCS');
INSERT INTO "BSCL.APV.BD::TRA.Traspaso" ("IdTraspaso","IdContratoSAAPV","IdEmpresa","TipoMovimiento","RazonRetiro","IdEstado","CodigoTipoTraspaso","IdCliente") VALUES (2991747,1013908,1,'INGRESO','TRA','PDI','TOT',8001);
