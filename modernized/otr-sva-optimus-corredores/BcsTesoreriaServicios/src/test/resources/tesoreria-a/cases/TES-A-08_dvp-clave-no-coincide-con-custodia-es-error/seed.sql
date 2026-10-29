INSERT INTO "BSCL.Tesoreria.BD::TES.NominaTransferencia" ("IdNominaTransferencia","IdEstado","Monto") VALUES (7,'ING',1500);
INSERT INTO "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" ("IdNominaTransferencia","IdComprobanteFormaDePago","IdEstado") VALUES (7,70,'ING');
INSERT INTO "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" ("IdComprobanteFormaDePago","IdComprobante","ClaveDCV","IdMedioDePago") VALUES (70,700,'CLAVE-MALA','TDP');
INSERT INTO "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" ("IdComprobante","IdTipoOperacion") VALUES (700,'VTA');
INSERT INTO "BSCL.Tesoreria.BD::TES.Comprobante" ("IdComprobante","IdPersona") VALUES (700,700);
INSERT INTO "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" ("IdPersona","IdCliente") VALUES (700,900);
INSERT INTO "BSCL.Custodia.BD::CUS.OperacionesPendientes" ("IdTipoOperacion","FolioOperacion","Clave") VALUES ('VTA','F-1001','CLAVE-OK');
INSERT INTO "BSCL.Custodia.BD.Vistas::CUS.ConsultarOperacionesPendientes" ("IdTipoOperacion","FolioOperacion","Monto","IdCliente") VALUES ('VTA','F-1001',1500,900);
