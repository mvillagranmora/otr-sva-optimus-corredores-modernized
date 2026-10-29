INSERT INTO "BSCL.Tesoreria.BD::TES.NominaTransferencia" ("IdNominaTransferencia","IdEstado","Monto") VALUES (6,'ING',1500);
INSERT INTO "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" ("IdNominaTransferencia","IdComprobanteFormaDePago","IdEstado") VALUES (6,60,'ING');
INSERT INTO "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" ("IdComprobanteFormaDePago","IdComprobante","ClaveDCV","IdMedioDePago") VALUES (60,600,'CLAVE-OK','TDP');
INSERT INTO "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" ("IdComprobante","IdTipoOperacion") VALUES (600,'VTA');
INSERT INTO "BSCL.Tesoreria.BD::TES.Comprobante" ("IdComprobante","IdPersona") VALUES (600,600);
INSERT INTO "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" ("IdPersona","IdCliente") VALUES (600,900);
INSERT INTO "BSCL.Custodia.BD::CUS.OperacionesPendientes" ("IdTipoOperacion","FolioOperacion","Clave") VALUES ('VTA','F-1001','CLAVE-OK');
INSERT INTO "BSCL.Custodia.BD.Vistas::CUS.ConsultarOperacionesPendientes" ("IdTipoOperacion","FolioOperacion","Monto","IdCliente") VALUES ('VTA','F-1001',1500,900);
