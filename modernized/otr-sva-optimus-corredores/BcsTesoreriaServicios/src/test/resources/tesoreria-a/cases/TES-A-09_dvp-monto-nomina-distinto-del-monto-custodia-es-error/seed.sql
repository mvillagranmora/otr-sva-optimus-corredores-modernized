INSERT INTO "BSCL.Tesoreria.BD::TES.NominaTransferencia" ("IdNominaTransferencia","IdEstado","Monto") VALUES (8,'ING',999);
INSERT INTO "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" ("IdNominaTransferencia","IdComprobanteFormaDePago","IdEstado") VALUES (8,80,'ING');
INSERT INTO "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" ("IdComprobanteFormaDePago","IdComprobante","ClaveDCV","IdMedioDePago") VALUES (80,800,'CLAVE-OK','TDP');
INSERT INTO "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" ("IdComprobante","IdTipoOperacion") VALUES (800,'VTA');
INSERT INTO "BSCL.Tesoreria.BD::TES.Comprobante" ("IdComprobante","IdPersona") VALUES (800,800);
INSERT INTO "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" ("IdPersona","IdCliente") VALUES (800,900);
INSERT INTO "BSCL.Custodia.BD::CUS.OperacionesPendientes" ("IdTipoOperacion","FolioOperacion","Clave") VALUES ('VTA','F-1001','CLAVE-OK');
INSERT INTO "BSCL.Custodia.BD.Vistas::CUS.ConsultarOperacionesPendientes" ("IdTipoOperacion","FolioOperacion","Monto","IdCliente") VALUES ('VTA','F-1001',1500,900);
