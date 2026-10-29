INSERT INTO "BSCL.Tesoreria.BD::TES.NominaTransferencia" ("IdNominaTransferencia","IdEstado","Monto") VALUES (3,'ING',1500);
INSERT INTO "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" ("IdNominaTransferencia","IdComprobanteFormaDePago","IdEstado") VALUES (3,30,'ING');
INSERT INTO "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" ("IdComprobanteFormaDePago","IdComprobante","ClaveDCV","IdMedioDePago") VALUES (30,300,'CLAVE-OK','TDP');
INSERT INTO "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" ("IdComprobante","IdTipoOperacion") VALUES (300,'  ');
INSERT INTO "BSCL.Tesoreria.BD::TES.Comprobante" ("IdComprobante","IdPersona") VALUES (300,500);
