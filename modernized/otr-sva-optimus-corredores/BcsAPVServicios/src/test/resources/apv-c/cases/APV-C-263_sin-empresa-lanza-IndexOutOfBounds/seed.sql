INSERT INTO "BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias" ("IdCuentaBanco", "IdBanco", "IdTipoCuentaBancaria", "NombreTipoCuentaBancaria", "NumeroCuentaBancaria", "IdMoneda", "CodigoBanco") VALUES (136, 17, 'CC', 'Cuenta Corriente', '111222', 'CLP', '017');
INSERT INTO "BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias" ("IdCuentaBanco", "IdBanco", "IdTipoCuentaBancaria", "NombreTipoCuentaBancaria", "NumeroCuentaBancaria", "IdMoneda", "CodigoBanco") VALUES (137, 18, 'CV', 'Cuenta Vista', '333444', 'CLP', '018');
INSERT INTO "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCuentasBancarias" ("IdCuentaBancaria", "IdBanco", "IdTipoCuentaBancaria", "NumeroCuentaBancaria") VALUES (100, 19, 'CUCO', '555000111');
INSERT INTO "BSCL.APV.BD::APV.OrigenDestinoTransicion" ("IdOrigenDestinoTransicion", "IdEmpresa", "Operacion", "OrigenCtaBco", "SocioNegocioCtaBco", "DestinoCtaBco", "MueveCuentaCorriente", "MueveCuentaInversion", "IdEstado") VALUES (1, 1, 'DBF', 136, 100, 137, 'S', 'S', 'ACT');
INSERT INTO "BSCL.APV.BD::INV.Bonificacion" ("IdBonificacion", "IdCliente", "AnoAhorro", "IdEmpresa", "IdEstado") VALUES (61, 701, 2025, 1, 'PRO');
INSERT INTO "BSCL.APV.BD::TRA.Traspaso" ("IdTraspaso", "IdCliente", "FechaIngreso", "NombreEntidad", "IdEmpresa") VALUES (1, 701, '2025-03-01', 'AFP UNO', 1);
INSERT INTO "BSCL.APV.BD::TRA.DetalleOrigenTraspaso" ("IdTraspaso", "CodigoTipoAhorro", "Fondo", "MontoTraspaso") VALUES (1, 'APV', 'A', 80000);
INSERT INTO "BSCL.APV.BD.Vistas::SCS.ConsultarEntidadRecaudadora" ("Nombre", "IdPersona") VALUES ('AFP UNO', 8000);
