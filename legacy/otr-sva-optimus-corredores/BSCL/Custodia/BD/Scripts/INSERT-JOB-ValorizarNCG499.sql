SET SCHEMA "BSCL";

INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma"
("IdTipoPrograma", "Descripcion", "MaximoIntentos", "Modulo", "NombreClasePrograma", "TipoClasePrograma", "Nodo", "EsPauta", "TipoSalida", "FormatoSalida", "TieneLog")
SELECT 'VALORIZAR_NCG499', 'Valorizar NCG499', 1, 'Custodia', 'cl.bcs.normativa.jobs.BcsJobValorizarNCG499', 'JAVA', '', 'N', NULL, NULL, NULL
FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='VALORIZAR_NCG499' AND "NombreClasePrograma"='cl.bcs.normativa.jobs.BcsJobValorizarNCG499');

COMMIT;