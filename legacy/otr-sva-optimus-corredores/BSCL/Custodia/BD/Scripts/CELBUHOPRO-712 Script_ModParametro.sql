set schema "BSCL";

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3400, 'IGKIdEventoCapitalCliente', '', 1, 30, 'S', '', '', 'N', 0, 'S'FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKIdEventoCapitalCliente');
UPDATE "BSCL.Plataforma.BD::PLA.Formato" SET "LargoCampo"=30 WHERE "IdFormato" = 3400;


COMMIT;