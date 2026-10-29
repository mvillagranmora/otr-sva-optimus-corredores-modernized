INSERT INTO "BSCL.Plataforma.BD::PLA.Empresa" ("IdEmpresa", "IdSocioDeNegocio") VALUES (1, 50);
INSERT INTO "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" ("IdSocioDeNegocio", "IdPersona") VALUES (50, 900);
INSERT INTO "BSCL.SocioNegocio.BD::CLI.Cliente" ("IdPersona", "IdPortafolioDefecto", "IdCliente", "IdSocioDeNegocio") VALUES (900, 777, 9000, 50);
INSERT INTO "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" ("IdEmpresa", "IdPersona", "IdSocioDeNegocio") VALUES (1, 900, 50);
INSERT INTO "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" ("IdSocioDeNegocio", "IdPortafolioDefecto", "IdCliente", "DocumentoIdentidad") VALUES (50, 777, 9000, '76000000-0');
INSERT INTO "BSCL.Plataforma.BD::BAN.Banco" ("IdBanco", "IdPersona") VALUES (12, 3012);
