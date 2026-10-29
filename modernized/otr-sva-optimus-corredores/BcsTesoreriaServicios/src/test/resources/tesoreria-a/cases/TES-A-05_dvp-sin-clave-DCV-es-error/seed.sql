INSERT INTO "BSCL.Tesoreria.BD::TES.NominaTransferencia" ("IdNominaTransferencia","IdEstado","Monto") VALUES (4,'ING',1500);
INSERT INTO "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" ("IdNominaTransferencia","IdComprobanteFormaDePago","IdEstado") VALUES (4,40,'ING');
INSERT INTO "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" ("IdComprobanteFormaDePago","IdComprobante","ClaveDCV","IdMedioDePago") VALUES (40,400,'','TDP');
INSERT INTO "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" ("IdComprobante","IdTipoOperacion") VALUES (400,'VTA');
INSERT INTO "BSCL.Tesoreria.BD::TES.Comprobante" ("IdComprobante","IdPersona") VALUES (400,400);
INSERT INTO "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" ("IdPersona","IdCliente") VALUES (400,900);
INSERT INTO "BSCL.Custodia.BD::CUS.OperacionesPendientes" ("IdTipoOperacion","FolioOperacion","Clave") VALUES ('VTA','F-1001','CLAVE-OK');
INSERT INTO "BSCL.Custodia.BD.Vistas::CUS.ConsultarOperacionesPendientes" ("IdTipoOperacion","FolioOperacion","Monto","IdCliente") VALUES ('VTA','F-1001',1500,900);
