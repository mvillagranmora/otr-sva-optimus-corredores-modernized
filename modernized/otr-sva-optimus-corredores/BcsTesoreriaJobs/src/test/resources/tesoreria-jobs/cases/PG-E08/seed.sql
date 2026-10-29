DELETE FROM "BSCL.Tesoreria.BD.Vistas::ConsultarFormatoArchivo";
INSERT INTO "BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferencia" ("IdNominaTransferencia", "IdEmpresa", "IdMedioDePago", "Monto", "IdMoneda", "NombreBanco", "NumeroCuentaBancaria", "FechaLiquidacion") VALUES (48, 1, 'TRA', '3000000.00000000', 'CLP', 'BANCO ESTADO', '000123456789', '2025-04-01');
INSERT INTO "BSCL.Plataforma.BD.Vistas::BAN.ConsultarBanco" ("NombreBanco", "CodigoIso") VALUES ('BANCO DE CHILE', 'CHL');
INSERT INTO "BSCL.Plataforma.BD.Vistas::BAN.ConsultarBanco" ("NombreBanco", "CodigoIso") VALUES ('BANCO SANTANDER', 'SAN');
INSERT INTO "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" ("IdEmpresa", "RUT", "RazonSocial") VALUES (1, '76123456-7', 'CORREDORA FICTICIA SA');
