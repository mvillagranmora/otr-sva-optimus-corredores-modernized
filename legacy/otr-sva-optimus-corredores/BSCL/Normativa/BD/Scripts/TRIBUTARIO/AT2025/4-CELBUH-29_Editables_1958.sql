SET SCHEMA "BSCL";

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='N'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='NumeroCuenta';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='N'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='DivisaSaldo';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='N'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='TipoPagoCuentaFinanciera';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Editable"='N'
WHERE "Esquema"='DJ1958' AND "Version"='2024' AND "Nombre"='DivisaPagoCuentaFinanciera';

COMMIT;