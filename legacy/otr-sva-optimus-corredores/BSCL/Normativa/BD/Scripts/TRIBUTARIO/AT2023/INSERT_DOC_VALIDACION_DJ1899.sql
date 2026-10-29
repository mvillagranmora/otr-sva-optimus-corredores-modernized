SET SCHEMA "BSCL";

INSERT INTO "BSCL.Normativa.BD::DOC.Validacion"("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES('DJ1899',
'2022',
'H925',
'NORMA',
'S',
'Si en el campo “Aporte empleador”(C7)  se tiene valor, se indicar un valor distinto a vacío en el campo “Rutempleador” (C4).',
'E',
'');

COMMIT;