INSERT INTO "BSCL.Tesoreria.BD::TES.NominaTransferencia" ("IdNominaTransferencia","IdEstado","Monto") VALUES (9,'RCP',1500);
INSERT INTO "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" ("IdNominaTransferencia","IdComprobanteFormaDePago","IdEstado") VALUES (9,90,'ING');
INSERT INTO "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" ("IdComprobanteFormaDePago","IdComprobante","ClaveDCV","IdMedioDePago") VALUES (90,900,'CLAVE-OK','TDP');
INSERT INTO "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" ("IdComprobante","IdTipoOperacion") VALUES (900,'VTA');
INSERT INTO "BSCL.Tesoreria.BD::TES.Comprobante" ("IdComprobante","IdPersona") VALUES (900,500);
