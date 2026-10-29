SELECT STRING_AGG("Tabla",'|') AS "Tabla" ,"Fecha","FechaSistema"
	FROM "BSCL.Replica.BD::EnvioPostPauta"
	WHERE "FechaSistema" = :FechaSistema
	GROUP BY "Fecha","FechaSistema"