INSERT INTO "BSCL.SocioNegocio.BD::PER.Persona" ("IdPersona","Tipo") VALUES (117943,'N');
INSERT INTO "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" ("IdPersona","DocumentoIdentidad","Predeterminado") VALUES (117943,'7813510-7','S');
INSERT INTO "BSCL.SocioNegocio.BD::CLI.Cliente" ("IdCliente","IdPersona","IdEstado") VALUES (8001,117943,'ACT');
INSERT INTO "BSCL.SocioNegocio.BD::PER.Persona" ("IdPersona","Tipo") VALUES (117950,'J');
INSERT INTO "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" ("IdPersona","DocumentoIdentidad","Predeterminado") VALUES (117950,'54364384-K','S');
INSERT INTO "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacion" ("IdRelacionPersona","IdPersonaOrigen","IdPersonaDestino") VALUES (5,117943,117950);
INSERT INTO "BSCL.APV.BD::SCS.ContratoSAAPV" ("IdContratoSAAPV","IdEmpresa","FolioContratoSAAPV","IdCliente","IdPersona","CodigoTipoSolicitud","IdEstado","IdPerfilComercial") VALUES (100,1,'100',8001,117943,'TRA','PDI','PERFIL BCS');
