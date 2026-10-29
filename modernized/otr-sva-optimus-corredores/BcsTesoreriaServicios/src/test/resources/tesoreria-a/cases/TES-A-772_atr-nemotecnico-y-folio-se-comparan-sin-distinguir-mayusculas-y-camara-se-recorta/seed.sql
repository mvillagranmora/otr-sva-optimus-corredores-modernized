INSERT INTO "BSCL.Orden.BD::ORD.Transaccion" ("IdTransaccion","IdEmpresa","Folio","Nemotecnico","Cantidad","Precio","Monto","FechaSistemaCreacion","FechaLiquidacion","IdCamara","EsBilateral") VALUES (8012,1,'f1','copec',100,50,5000,'2025-04-01','2025-04-02','','N');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" ("IdSocioDeNegocio","IdEmpresa","IdPersona","Borrado") VALUES (5001,1,102,'N');
INSERT INTO "BSCL.Plataforma.BD::INS.Corredora" ("IdCorredora","IdPersona","NombreCorto") VALUES (33,102,'CORREDORA');
INSERT INTO "BSCL.Plataforma.BD::INS.CorredoraCodigos" ("IdCorredora","CodigoCorredora","IdBolsa") VALUES (33,'COR01','BCS');
