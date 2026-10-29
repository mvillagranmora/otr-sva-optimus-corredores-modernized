INSERT INTO "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" ("IdEmpresa","IdSocioDeNegocio","IdPersona") VALUES (1,777,900);
INSERT INTO "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" ("IdSocioDeNegocio","IdPortafolioDefecto") VALUES (777,4500);
INSERT INTO "BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias" ("IdCuentaBanco","IdBanco","IdTipoCuentaBancaria","NombreTipoCuentaBancaria","NumeroCuentaBancaria") VALUES (136,17,'CC','Cuenta Corriente','111222');
INSERT INTO "BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias" ("IdCuentaBanco","IdBanco","IdTipoCuentaBancaria","NombreTipoCuentaBancaria","NumeroCuentaBancaria") VALUES (137,18,'CV','Cuenta Vista','333444');
INSERT INTO "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCuentasBancarias" ("IdCuentaBancaria","IdBanco","IdTipoCuentaBancaria","NumeroCuentaBancaria") VALUES (100,19,'CUCO','555000111');
INSERT INTO "BSCL.APV.BD::APV.OrigenDestinoTransicion" ("IdOrigenDestinoTransicion","IdEmpresa","Operacion","OrigenCtaBco","SocioNegocioCtaBco","DestinoCtaBco","MueveCuentaCorriente","MueveCuentaInversion","IdEstado") VALUES (1,1,'DIS',136,100,137,'S','S','ACT');
INSERT INTO "BSCL.APV.BD::REC.Recaudacion" ("IdRecaudacion","IdEmpresa","IdBanco","CuentaCorriente","MontoDeposito","IdEstado") VALUES (300,1,17,111222,250000,1);
INSERT INTO "BSCL.SocioNegocio.BD::CLI.Cliente" ("IdCliente","IdPersona","IdEstado") VALUES (8001,1774553,'ACT');
INSERT INTO "BSCL.APV.BD::REC.Cliente" ("IdRecaudacionCliente","IdRecaudacion","IdCliente","MontoDepositoConvenido","MontoDepositoAPV","IdEstado","Origen") VALUES (9001,300,8001,100000,150000,'DIS','TRASPA');
