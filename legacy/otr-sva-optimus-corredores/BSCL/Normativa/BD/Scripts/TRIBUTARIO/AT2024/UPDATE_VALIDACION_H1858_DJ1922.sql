SET SCHEMA "BSCL";

UPDATE BSCL."BSCL.Normativa.BD::DOC.Validacion"
SET "Tipo"='NORMA', "Activa"='S', "Descripcion"='Si el campo Distribuciones o Remesas no afectas a Impuesto unico Tasa 10% tiene que ser mayor que cero, entonces los campos "Participe cumple requisitos Art. 82 Nб letra B) iii) Ley adm fondos contenida Art. 82 Ley 20712" y "Cuotas de fondos que cumplen requisitos art. 82 letra B) iii), Ley Adm. Fondos Contenida Art. 82 Ley 20712)" son iguales a 1', "Severidad"='E', "Formula"='Si entonces (C31) > 0, (C16) y (C17) = 1'
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Codigo"='H1858';

COMMIT;