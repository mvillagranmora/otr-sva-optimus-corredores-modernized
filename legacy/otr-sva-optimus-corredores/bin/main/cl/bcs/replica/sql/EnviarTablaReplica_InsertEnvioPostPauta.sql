UPSERT "BSCL.Replica.BD::EnvioPostPauta"
("FechaSistema", "Tabla", "Fecha")
VALUES(:FechaSistema,:Tabla,:Fecha) WITH PRIMARY KEY;