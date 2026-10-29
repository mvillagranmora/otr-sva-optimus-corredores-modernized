SET SCHEMA "BSCL";

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso", "ValidaAccesoServicio")
SELECT 101010107, 'PLUGIN_NOR.jar@cl.bcs.normativa.ValorizarNCG499', 0, 'J', 'Valorizar Carteras NCG499 desde Mantenedor', '', '', 'S', 'N'
FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=101010107 AND "RutaServicio"='PLUGIN_NOR.jar@cl.bcs.normativa.ValorizarNCG499' AND "TipoServicio"='J');

COMMIT;