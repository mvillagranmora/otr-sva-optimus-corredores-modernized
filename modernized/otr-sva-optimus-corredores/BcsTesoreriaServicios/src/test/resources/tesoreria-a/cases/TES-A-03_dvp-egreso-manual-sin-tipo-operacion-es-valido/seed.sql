INSERT INTO "BSCL.Tesoreria.BD::TES.NominaTransferencia" ("IdNominaTransferencia","IdEstado","Monto") VALUES (2,'ING',1500);
INSERT INTO "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" ("IdNominaTransferencia","IdComprobanteFormaDePago","IdEstado") VALUES (2,20,'ING');
INSERT INTO "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" ("IdComprobanteFormaDePago","IdComprobante","ClaveDCV","IdMedioDePago") VALUES (20,200,'CLAVE-OK','TDP');
INSERT INTO "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" ("IdComprobante","IdTipoOperacion") VALUES (200,NULL);
INSERT INTO "BSCL.Tesoreria.BD::TES.Comprobante" ("IdComprobante","IdPersona") VALUES (200,500);
