--MARCELO ROJAS
--HOMOLOGACION PAIS Y INSTRUMENTO NUEVA CIRCULARCMF
SET SCHEMA "BSCL";

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion" 
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98220, 'CMF', 'V1', 'PAISCONTRACMF', 'AFGANISTAN', 'AF', 'AF', 'ACT'FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98220 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion" 
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98221, 'CMF', 'V1', 'PAISCONTRACMF', 'ALBANIA', 'AL', 'AL', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98221 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion" 
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98222, 'CMF', 'V1', 'PAISCONTRACMF', 'ALEMANIA', 'DE', 'DE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98222 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98223, 'CMF', 'V1', 'PAISCONTRACMF', 'ANGOLA', 'AO', 'AO', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98223 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98224, 'CMF', 'V1', 'PAISCONTRACMF', 'ANTILLAS HOLAND', 'AN', 'AN', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98224 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98225, 'CMF', 'V1', 'PAISCONTRACMF', 'ARABIA SAUDITA', 'SA', 'SA', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98225 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98226, 'CMF', 'V1', 'PAISCONTRACMF', 'ARGELIA', 'DZ', 'DZ', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98226 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98227, 'CMF', 'V1', 'PAISCONTRACMF', 'ARGENTINA', 'AR', 'AR', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98227 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98228, 'CMF', 'V1', 'PAISCONTRACMF', 'ARMENIA', 'AM', 'AM', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98228 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98229, 'CMF', 'V1', 'PAISCONTRACMF', 'AUSTRALIA', 'AU', 'AU', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98229 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98230, 'CMF', 'V1', 'PAISCONTRACMF', 'AUSTRIA', 'AT', 'AT', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98230 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98231, 'CMF', 'V1', 'PAISCONTRACMF', 'BAHAMAS', 'BS', 'BS', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98231 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98232, 'CMF', 'V1', 'PAISCONTRACMF', 'BARBADOS', 'BB', 'BB', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98232 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98233, 'CMF', 'V1', 'PAISCONTRACMF', 'BELGICA', 'BE', 'BE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98233 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98234, 'CMF', 'V1', 'PAISCONTRACMF', 'BELICE', 'BZ', 'BZ', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98234 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98235, 'CMF', 'V1', 'PAISCONTRACMF', 'BERMUDAS', 'BM', 'BM', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98235 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98236, 'CMF', 'V1', 'PAISCONTRACMF', 'BOLIVIA', 'BO', 'BO', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98236 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98237, 'CMF', 'V1', 'PAISCONTRACMF', 'BOSNIA', 'BA', 'BA', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98237 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98238, 'CMF', 'V1', 'PAISCONTRACMF', 'BRASIL', 'BR', 'BR', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98238 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98239, 'CMF', 'V1', 'PAISCONTRACMF', 'BULGARIA', 'BG', 'BG', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98239 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98240, 'CMF', 'V1', 'PAISCONTRACMF', 'CABO VERDE', 'CV', 'CV', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98240 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98241, 'CMF', 'V1', 'PAISCONTRACMF', 'CAMERUN', 'CM', 'CM', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98241 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98242, 'CMF', 'V1', 'PAISCONTRACMF', 'CANADA', 'CA', 'CA', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98242 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98243, 'CMF', 'V1', 'PAISCONTRACMF', 'CHILE', 'CL', 'CL', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98243 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98244, 'CMF', 'V1', 'PAISCONTRACMF', 'CHINA', 'CN', 'CN', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98244 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98245, 'CMF', 'V1', 'PAISCONTRACMF', 'CHIPRE', 'CY', 'CY', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98245 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98246, 'CMF', 'V1', 'PAISCONTRACMF', 'COLOMBIA', 'CO', 'CO', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98246 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98247, 'CMF', 'V1', 'PAISCONTRACMF', 'CONGO', 'CG', 'CG', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98247 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98248, 'CMF', 'V1', 'PAISCONTRACMF', 'COSTA DE MARFIL', 'CI', 'CI', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98248 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98249, 'CMF', 'V1', 'PAISCONTRACMF', 'COSTA RICA', 'CR', 'CR', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98249 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98250, 'CMF', 'V1', 'PAISCONTRACMF', 'CROACIA', 'HR', 'HR', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98250 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98251, 'CMF', 'V1', 'PAISCONTRACMF', 'CUBA', 'CU', 'CU', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98251 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98252, 'CMF', 'V1', 'PAISCONTRACMF', 'CURAZAO', 'CX', 'CX', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98252 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98253, 'CMF', 'V1', 'PAISCONTRACMF', 'DINAMARCA', 'DK', 'DK', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98253 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98254, 'CMF', 'V1', 'PAISCONTRACMF', 'DOMINICA', 'DM', 'DM', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98254 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98255, 'CMF', 'V1', 'PAISCONTRACMF', 'ECUADOR', 'EC', 'EC', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98255 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98256, 'CMF', 'V1', 'PAISCONTRACMF', 'EGIPTO', 'EG', 'EG', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98256 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98257, 'CMF', 'V1', 'PAISCONTRACMF', 'EL SALVADOR', 'SV', 'SV', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98257 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98258, 'CMF', 'V1', 'PAISCONTRACMF', 'EMIRATOS ARABES', 'AE', 'AE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98258 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98259, 'CMF', 'V1', 'PAISCONTRACMF', 'ESLOVAKIA', 'SK', 'SK', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98259 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98260, 'CMF', 'V1', 'PAISCONTRACMF', 'ESLOVENIA', 'SI', 'SI', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98260 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98261, 'CMF', 'V1', 'PAISCONTRACMF', 'ESPAÑA', 'ES', 'ES', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98261 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98262, 'CMF', 'V1', 'PAISCONTRACMF', 'ESTADOS UNIDOS', 'US', 'US', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98262 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98263, 'CMF', 'V1', 'PAISCONTRACMF', 'ESTONIA', 'EE', 'EE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98263 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98264, 'CMF', 'V1', 'PAISCONTRACMF', 'FILIPINAS', 'PH', 'PH', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98264 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98265, 'CMF', 'V1', 'PAISCONTRACMF', 'FINLANDIA', 'FI', 'FI', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98265 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98266, 'CMF', 'V1', 'PAISCONTRACMF', 'FRANCIA', 'FR', 'FR', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98266 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98267, 'CMF', 'V1', 'PAISCONTRACMF', 'GABON', 'GA', 'GA', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98267 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98268, 'CMF', 'V1', 'PAISCONTRACMF', 'GEORGIA', 'GE', 'GE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98268 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98269, 'CMF', 'V1', 'PAISCONTRACMF', 'GHANA', 'GH', 'GH', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98269 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98270, 'CMF', 'V1', 'PAISCONTRACMF', 'GRAN BRETAÑA', 'GB', 'GB', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98270 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98271, 'CMF', 'V1', 'PAISCONTRACMF', 'GRANADA', 'GD', 'GD', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98271 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98272, 'CMF', 'V1', 'PAISCONTRACMF', 'GRECIA', 'GR', 'GR', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98272 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98273, 'CMF', 'V1', 'PAISCONTRACMF', 'GUATEMALA', 'GT', 'GT', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98273 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98274, 'CMF', 'V1', 'PAISCONTRACMF', 'GUERNSEY', 'GG', 'GG', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98274 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98275, 'CMF', 'V1', 'PAISCONTRACMF', 'GUYANA', 'GY', 'GY', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98275 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98276, 'CMF', 'V1', 'PAISCONTRACMF', 'HAITI', 'HT', 'HT', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98276 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98277, 'CMF', 'V1', 'PAISCONTRACMF', 'HOLANDA', 'NL', 'NL', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98277 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98278, 'CMF', 'V1', 'PAISCONTRACMF', 'HONDURAS', 'HN', 'HN', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98278 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98279, 'CMF', 'V1', 'PAISCONTRACMF', 'HONG KONG', 'HK', 'HK', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98279 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98280, 'CMF', 'V1', 'PAISCONTRACMF', 'HUNGRIA', 'HU', 'HU', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98280 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98281, 'CMF', 'V1', 'PAISCONTRACMF', 'INDIA', 'IN', 'IN', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98281 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98282, 'CMF', 'V1', 'PAISCONTRACMF', 'INDONESIA', 'ID', 'ID', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98282 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98283, 'CMF', 'V1', 'PAISCONTRACMF', 'INGLATERRA', 'IG', 'IG', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98283 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98284, 'CMF', 'V1', 'PAISCONTRACMF', 'IRAN', 'IR', 'IR', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98284 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98285, 'CMF', 'V1', 'PAISCONTRACMF', 'IRAQ', 'IQ', 'IQ', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98285 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98286, 'CMF', 'V1', 'PAISCONTRACMF', 'IRLANDA', 'IE', 'IE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98286 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98287, 'CMF', 'V1', 'PAISCONTRACMF', 'ISLA DE MAN', 'IM', 'IM', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98287 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98288, 'CMF', 'V1', 'PAISCONTRACMF', 'ISLANDIA', 'IS', 'IS', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98288 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);


INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98289, 'CMF', 'V1', 'PAISCONTRACMF', 'ISLAS CAIMAN', 'KY', 'KY', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98289 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98290, 'CMF', 'V1', 'PAISCONTRACMF', 'ISLAS MARSHALL', 'MH', 'MH', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98290 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98291, 'CMF', 'V1', 'PAISCONTRACMF', 'ISLAS VIRGENES (U.S)', 'VI', 'VI', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98291 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98292, 'CMF', 'V1', 'PAISCONTRACMF', 'ISLAS VIRGENES BRITANICAS', 'VG', 'VG', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98292 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98293,'CMF', 'V1', 'PAISCONTRACMF', 'ISRAEL', 'IL', 'IL', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98293 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98294,'CMF', 'V1', 'PAISCONTRACMF', 'ITALIA', 'IT', 'IT', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98294 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98295,'CMF', 'V1', 'PAISCONTRACMF', 'JAMAICA', 'JM', 'JM', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98295 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98296,'CMF', 'V1', 'PAISCONTRACMF', 'JAPON', 'JP', 'JP', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98296 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98297, 'CMF', 'V1', 'PAISCONTRACMF', 'JERSEY', 'JE', 'JE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98297 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98298, 'CMF', 'V1', 'PAISCONTRACMF', 'JORDANIA', 'JO', 'JO', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98298 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98299, 'CMF', 'V1', 'PAISCONTRACMF', 'KAZAJISTAN', 'KZ', 'KZ', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98299 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98300, 'CMF', 'V1', 'PAISCONTRACMF', 'KENYA', 'KE', 'KE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98300 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98301, 'CMF', 'V1', 'PAISCONTRACMF', 'KUWAIT', 'KW', 'KW', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98301 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98302, 'CMF', 'V1', 'PAISCONTRACMF', 'LIBANO', 'LB', 'LB', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98302 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98303, 'CMF', 'V1', 'PAISCONTRACMF', 'LIBERIA', 'LR', 'LR', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98303 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98304, 'CMF', 'V1', 'PAISCONTRACMF', 'LUXEMBURGO', 'LU', 'LU', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98304 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98305, 'CMF', 'V1', 'PAISCONTRACMF', 'MALASIA', 'MY', 'MY', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98305 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98306, 'CMF', 'V1', 'PAISCONTRACMF', 'MALTA', 'MT', 'MT', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98306 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98307, 'CMF', 'V1', 'PAISCONTRACMF', 'MARRUECOS', 'MA', 'MA', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98307 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98308, 'CMF', 'V1', 'PAISCONTRACMF', 'MAURICIO', 'MU', 'MU', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98308 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98309, 'CMF', 'V1', 'PAISCONTRACMF', 'MAURITANIA', 'MR', 'MR', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98309 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98310, 'CMF', 'V1', 'PAISCONTRACMF', 'MEXICO', 'MX', 'MX', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98310 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98311, 'CMF', 'V1', 'PAISCONTRACMF', 'MONACO', 'MC', 'MC', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98311 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98312, 'CMF', 'V1', 'PAISCONTRACMF', 'MONGOLIA', 'MN', 'MN', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98312 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98313, 'CMF', 'V1', 'PAISCONTRACMF', 'MOZAMBIQUE', 'MZ', 'MZ', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98313 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98314, 'CMF', 'V1', 'PAISCONTRACMF', 'NAMIBIA', 'NA', 'NA', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98314 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98315, 'CMF', 'V1', 'PAISCONTRACMF', 'NEPAL', 'NP', 'NP', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98315 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98316, 'CMF', 'V1', 'PAISCONTRACMF', 'NICARAGUA', 'NI', 'NI', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98316 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98317, 'CMF', 'V1', 'PAISCONTRACMF', 'NIGERIA', 'NG', 'NG', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98317 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98318, 'CMF', 'V1', 'PAISCONTRACMF', 'NORUEGA', 'NO', 'NO', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98318 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98319, 'CMF', 'V1', 'PAISCONTRACMF', 'NUEVA ZELANDIA', 'NZ', 'NZ', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98319 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98320, 'CMF', 'V1', 'PAISCONTRACMF', 'OMAN', 'OM', 'OM', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98320 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98321, 'CMF', 'V1', 'PAISCONTRACMF', 'PAKISTAN', 'PK', 'PK', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98321 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98322, 'CMF', 'V1', 'PAISCONTRACMF', 'PALESTINA', 'PS', 'PS', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98322 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98323, 'CMF', 'V1', 'PAISCONTRACMF', 'PANAMA', 'PA', 'PA', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98323 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98324, 'CMF', 'V1', 'PAISCONTRACMF', 'PARAGUAY', 'PY', 'PY', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98324 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98325, 'CMF', 'V1', 'PAISCONTRACMF', 'PERU', 'PE', 'PE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98325 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98326, 'CMF', 'V1', 'PAISCONTRACMF', 'POLONIA', 'PL', 'PL', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98326 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98327, 'CMF', 'V1', 'PAISCONTRACMF', 'PORTUGAL', 'PT', 'PT', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98327 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98328, 'CMF', 'V1', 'PAISCONTRACMF', 'PUERTO RICO', 'PR', 'PR', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98328 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98329, 'CMF', 'V1', 'PAISCONTRACMF', 'QATAR', 'QA', 'QA', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98329 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98330, 'CMF', 'V1', 'PAISCONTRACMF', 'REINO DE BAHREIN', 'BH', 'BH', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98330 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98331, 'CMF', 'V1', 'PAISCONTRACMF', 'REP DOMINICANA', 'DO', 'DO', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98331 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98332, 'CMF', 'V1', 'PAISCONTRACMF', 'REP. COREA', 'KR', 'KR', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98332 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98333, 'CMF', 'V1', 'PAISCONTRACMF', 'REP. DEM. COREA', 'KP', 'KP', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98333 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98334, 'CMF', 'V1', 'PAISCONTRACMF', 'REP.ARABE SIRIA', 'SY', 'SY', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98334 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98335, 'CMF', 'V1', 'PAISCONTRACMF', 'REPUBLICA CHECA', 'CZ', 'CZ', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98335 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98336, 'CMF', 'V1', 'PAISCONTRACMF', 'REPUBLICA DE AZERBAIYAN', 'AZ', 'AZ', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98336 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98337, 'CMF', 'V1', 'PAISCONTRACMF', 'REPUBLICA DE BELARUS', 'BY', 'BY', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98337 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98338, 'CMF', 'V1', 'PAISCONTRACMF', 'REPUBLICA DE UZBEKISTAN', 'UZ', 'UZ', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98338 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98339, 'CMF', 'V1', 'PAISCONTRACMF', 'RUMANIA', 'RO', 'RO', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98339 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98340, 'CMF', 'V1', 'PAISCONTRACMF', 'RUSIA', 'RU', 'RU', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98340 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98341, 'CMF', 'V1', 'PAISCONTRACMF', 'SAMOA AMERICANA', 'AS', 'AS', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98341 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98342, 'CMF', 'V1', 'PAISCONTRACMF', 'SAN MARINO', 'SM', 'SM', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98342 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98343, 'CMF', 'V1', 'PAISCONTRACMF', 'SENEGAL', 'SN', 'SN', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98343 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98344, 'CMF', 'V1', 'PAISCONTRACMF', 'SERBIA', 'RS', 'RS', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98344 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98345, 'CMF', 'V1', 'PAISCONTRACMF', 'SIERRA LEONE', 'SL', 'SL', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98345 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98346, 'CMF', 'V1', 'PAISCONTRACMF', 'SINGAPUR', 'SG', 'SG', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98346 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98347, 'CMF', 'V1', 'PAISCONTRACMF', 'SOMALIA', 'SO', 'SO', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98347 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98348, 'CMF', 'V1', 'PAISCONTRACMF', 'SRI LANKA', 'LK', 'LK', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98348 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98349, 'CMF', 'V1', 'PAISCONTRACMF', 'SUD AFRICA', 'ZA', 'ZA', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98349 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98350, 'CMF', 'V1', 'PAISCONTRACMF', 'SUDAN', 'SD', 'SD', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98350 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98351, 'CMF', 'V1', 'PAISCONTRACMF', 'SUECIA', 'SE', 'SE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98351 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98352, 'CMF', 'V1', 'PAISCONTRACMF', 'SUIZA', 'CH', 'CH', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98352 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98353, 'CMF', 'V1', 'PAISCONTRACMF', 'SURINAM', 'SR', 'SR', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98353 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98354, 'CMF', 'V1', 'PAISCONTRACMF', 'TAILANDIA', 'TH', 'TH', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98354 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98355, 'CMF', 'V1', 'PAISCONTRACMF', 'TAIWAN', 'TW', 'TW', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98355 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98356, 'CMF', 'V1', 'PAISCONTRACMF', 'TAYIKISTAN', 'TJ', 'TJ', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98356 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98357, 'CMF', 'V1', 'PAISCONTRACMF', 'TRINIDAD TOBAGO', 'TT', 'TT', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98357 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98358, 'CMF', 'V1', 'PAISCONTRACMF', 'TURQUIA', 'TR', 'TR', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98358 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98359, 'CMF', 'V1', 'PAISCONTRACMF', 'UCRANIA', 'UA', 'UA', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98359 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98360, 'CMF', 'V1', 'PAISCONTRACMF', 'URUGUAY', 'UY', 'UY', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98360 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98361, 'CMF', 'V1', 'PAISCONTRACMF', 'VENEZUELA', 'VE', 'VE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98361 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98362, 'CMF', 'V1', 'PAISCONTRACMF', 'VIETNAM', 'VN', 'VN', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98362 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98363, 'CMF', 'V1', 'PAISCONTRACMF', 'ZAIRE', 'ZR', 'ZR', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98363 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98364, 'CMF', 'V1', 'PAISCONTRACMF', 'ZAMBIA', 'ZM', 'ZM', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98364 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'PAISCONTRACMF'
);


INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98365, 'CMF', 'V1', 'INSTRUMENTOCMF', 'APORTES DE FINANCIAMIENTO REEMBOLSABLES
', ' ', 'AFR', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98365 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);


INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98366, 'CMF', 'V1', 'INSTRUMENTOCMF', 'APORTES DE FINANCIAMIENTO REEMBOLSABLES DEPOSITADOS
', ' ', 'AFROIND', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98366 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98367, 'CMF', 'V1', 'INSTRUMENTOCMF', 'BONO BANCARIO PERPETUO', ' ', 'BBP', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98367 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);


INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98368, 'CMF', 'V1', 'INSTRUMENTOCMF', 'BONO DE LA REFORMA AGRARIA', 'COR', 'CORA', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98368 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98369, 'CMF', 'V1', 'INSTRUMENTOCMF', 'BONO  TESORERIA GENERAL DE REPUBLICA DE CHILE EN PESOS', 'BTP', 'BTP', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98369 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98370, 'CMF', 'V1', 'INSTRUMENTOCMF', 'BONO  TESORERIA GENERAL DE REPUBLICA DE CHILE EN UF
', 'BTU', 'BTU', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98370 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98371, 'CMF', 'V1', 'INSTRUMENTOCMF', 'BONO DE RECONOCIMIENTO Y COMPLEMENTO DE BONO', 'BR', 'BR', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98371 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98372, 'CMF', 'V1', 'INSTRUMENTOCMF', 'BONO EXTRANJERO DE EMISIÓN PRIVADA DEPOSITADO', '', 'BEEPOIED', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98372 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);


INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98373, 'CMF', 'V1', 'INSTRUMENTOCMF', 'BONO PERPETUOS BANCO NACIONALES EMITIDOS EN EL EXTRANJERO', '', 'BBPNEE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98373 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98374, 'CMF', 'V1', 'INSTRUMENTOCMF', 'BONO SOBERANO DEL ESTADO DE CHILE', '', 'BEC', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98374 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98375, 'CMF', 'V1', 'INSTRUMENTOCMF', 'BONO VIVIENDA LEASING (EMITIDO POR MINVU)', 'BVL', 'BVL', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98375 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98376, 'CMF', 'V1', 'INSTRUMENTOCMF', 'BONOS BANCO CENTRAL CHILE EN DÓLARES DE E.E.U.U.- PAGADERO DÓLAR', 'BCX', 'BCX', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98376 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98377, 'CMF', 'V1', 'INSTRUMENTOCMF', 'BONOS BANCO CENTRAL DE CHILE, EN DOLARES - PAGADERO PESOS', 'BCD', 'BCD', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98377 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98378, 'CMF', 'V1', 'INSTRUMENTOCMF', 'BONOS BANCO CENTRAL DE CHILE, EN PESOS', 'BCP', 'BCP', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98378 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98379, 'CMF', 'V1', 'INSTRUMENTOCMF', 'BONOS BANCO CENTRAL DE CHILE, EN UF', 'BCU', 'BCU', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98379 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98380, 'CMF', 'V1', 'INSTRUMENTOCMF', 'BONOS BANCOS NACIONALES EMITIDOS EN EL EXTRANJERO', '', 'BBNEE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98380 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98381, 'CMF', 'V1', 'INSTRUMENTOCMF', 'BONOS CACSA,DE TASA DE INTERES FLOTANTE', '', 'BC', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98381 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98382, 'CMF', 'V1', 'INSTRUMENTOCMF', 'BONOS DE BANCOS E INSTITUCIONES FINANCIERAS', '', 'BB', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98382 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98383, 'CMF', 'V1', 'INSTRUMENTOCMF', 'BONOS DE EMPRESAS', 'BEM', 'BE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98383 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98384, 'CMF', 'V1', 'INSTRUMENTOCMF', 'BONOS EMITIDOS POR BANCOS Y FINANCIERAS EXTRANJERAS', '', 'BBFE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98384 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98385, 'CMF', 'V1', 'INSTRUMENTOCMF', 'BONOS EMITIDOS POR EL ESTADO Y BANCO CENTRAL EXTRANJERO', '', 'BEBCE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98385 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98386, 'CMF', 'V1', 'INSTRUMENTOCMF', 'BONOS EMITIDOS POR EMPRESAS EXTRANJERAS', '', 'BEE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98386 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98387, 'CMF', 'V1', 'INSTRUMENTOCMF', 'BONOS EMPRESAS NACIONALES EMITIDOS EN EL EXTRANJERO', '', 'BNEE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98387 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98388, 'CMF', 'V1', 'INSTRUMENTOCMF', 'BONOS EXTRANJERO EMITIDO EN CHILE', '', 'BEEC', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98388 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98389, 'CMF', 'V1', 'INSTRUMENTOCMF', 'BONOS HIPOTECARIO', 'BH', 'BH', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98389 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98390, 'CMF', 'V1', 'INSTRUMENTOCMF', 'BONOS SECURITIZADOS', '', 'BS', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98390 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98391, 'CMF', 'V1', 'INSTRUMENTOCMF', 'BONOS SUBORDINADOS', 'BUS', 'BU', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98391 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98392, 'CMF', 'V1', 'INSTRUMENTOCMF', 'CONTRATOS DE LEASING DE BIENES RAICES', '', 'CLEAS', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98392 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98393, 'CMF', 'V1', 'INSTRUMENTOCMF', 'CONTRATOS DE LEASING DE BIENES RAICES NACIONALES DEPOSITADOS', '', 'CLEASOIND', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98393 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98394, 'CMF', 'V1', 'INSTRUMENTOCMF', 'CREDITOS DE CONSUMO A OTROS CLIENTES', '', 'PCNP', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98394 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98395, 'CMF', 'V1', 'INSTRUMENTOCMF', 'CREDITOS DE CONSUMO A PENSIONADOS', '', 'PPEN', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98395 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98396, 'CMF', 'V1', 'INSTRUMENTOCMF', 'CREDITOS SINDICADOS', '', 'CS', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98396 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98397, 'CMF', 'V1', 'INSTRUMENTOCMF', 'CREDITOS SINDICADOS DEPOSITADOS', '', 'CSOIND', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98397 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98398, 'CMF', 'V1', 'INSTRUMENTOCMF', 'DEPOSITOS A PLAZO FIJO CON VENCIMIENTO A CORTO PLAZO', '', 'DPC', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98398 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98399, 'CMF', 'V1', 'INSTRUMENTOCMF', 'DEPOSITOS A PLAZO FIJO CON VENCIMIENTO A LARGO PLAZO', '', 'DPL', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98399 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98400, 'CMF', 'V1', 'INSTRUMENTOCMF', 'DEPOSITOS Y PAGARES EMITIDOS POR BANCOS Y FINANCIERAS EXTRANJERAS', '', 'DPBFE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98400 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98401, 'CMF', 'V1', 'INSTRUMENTOCMF', 'EFECTOS DE COMERCIO', '', 'EC', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98401 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98402, 'CMF', 'V1', 'INSTRUMENTOCMF', 'LETRAS EMITIDAS POR BANCOS Y FINANCIERAS EXTRANJERAS', '', 'LBFE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98402 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98403, 'CMF', 'V1', 'INSTRUMENTOCMF', 'LETRAS HIPOTECARIAS DE BANCOS E INSTITUCIONES FINANCIERAS', 'LTH', 'LH', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98403 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98404, 'CMF', 'V1', 'INSTRUMENTOCMF', 'LIBRETAS DE AHORRO', '', 'LA', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98404 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98405, 'CMF', 'V1', 'INSTRUMENTOCMF', 'LINEAS DE CREDITO', '', 'PLC', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98405 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98406, 'CMF', 'V1', 'INSTRUMENTOCMF', 'LINEAS DE CREDITO A PENSIONADOS', '', 'PLCP', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98406 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98407, 'CMF', 'V1', 'INSTRUMENTOCMF', 'MUTUOS HIPOTECARIOS EMITIDOS POR AGENTES DE MM.HH.', '', 'MHA', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98407 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98408, 'CMF', 'V1', 'INSTRUMENTOCMF', 'MUTUOS HIPOTECARIOS EMITIDOS POR BANCOS', '', 'MHB', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98408 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98409, 'CMF', 'V1', 'INSTRUMENTOCMF', 'NOTA ESTRUCTURADA EMISOR EXTRANJERO RENTA FIJA', '', 'NEEINSTRUMENTOCMF', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98409 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98410, 'CMF', 'V1', 'INSTRUMENTOCMF', 'LINEAS DE CREDITO', '', 'PLC', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98410 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98411, 'CMF', 'V1', 'INSTRUMENTOCMF', 'NOTA ESTRUCTURADA EMISOR EXTRANJERO RENTA FIJA DEPOSITADO', '', 'NEERV', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98411 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98412, 'CMF', 'V1', 'INSTRUMENTOCMF', 'NOTA ESTRUCTURADA EMISOR EXTRANJERO RENTA VARIABLE DEPOSITADO ', '', 'NEERVOIND', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98412 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98413, 'CMF', 'V1', 'INSTRUMENTOCMF', 'OTROS INSTRUMENTOCMFS DE DEUDA', '', 'OTROD', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98413 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98414, 'CMF', 'V1', 'INSTRUMENTOCMF', 'OTROS INSTRUMENTOCMFS DE INVERSION DE RENTA FIJA', 'ORF', 'OTROF', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98414 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98415, 'CMF', 'V1', 'INSTRUMENTOCMF', 'OTROS INSTRUMENTOCMFS E INVERSIONES FINANCIERAS', '', 'OIIF', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98415 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98416, 'CMF', 'V1', 'INSTRUMENTOCMF', 'OTROS TITULOS DE DEUDA O CREDITO EMITIDOS POR EMPRESAS EXT.', '', 'OEE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98416 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98417, 'CMF', 'V1', 'INSTRUMENTOCMF', 'OTROS TITULOS DE DEUDA O CREDITO EMITIDOS POR BANCOS Y FINANCIERAS'
, '', 'OBF', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98417 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98418, 'CMF', 'V1', 'INSTRUMENTOCMF', 'OTROS TITULOS DE DEUDA/CREDITO EMITIDOS POR BANCOS Y FINANC. EXT.', '', 'OBFE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98418 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98419, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PAGARE AL PORTADOR DEL BANCO CENTRAL DE CHILE', '', 'PPBC', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98419 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98420, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PAGARE AL PORTADOR EXPRESADO Y REAJUSTADO EN UF SIN INTERESES'
, '', 'CERO', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98420 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98421, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PAGARE AL PORTADOR EXPRESADO Y REAJUSTADO EN US$ SIN INTERESES'
, 'ZER', 'ZERO', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98421 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98422, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PAGARE BANCO CENTRAL CHILE EN DÓLARES DE E.E.U.U.- PAGADERO DÓLAR'
, 'PCX', 'PCX', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98422 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98423, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PAGARE DE EMPRESAS'
, '', 'PE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98423 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98424, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PAGARE DOLAR PREFERENCIAL DEL BANCO CENTRAL DE CHILE'
, '', 'PDP', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98424 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98425, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PAGARE LEY N° 19.980 (EMITIDO POR INP)'
, '', '19980', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98425 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98426, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PAGARE REAJUSTABLE DE LA TESORERIA GENERAL DE LA REPUBLICA'
, '', 'PRT', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98426 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98427, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PAGARE REAJUSTABLE DE TESORERIA GENERAL DE LA REPUBLICA SERIE P'
, '', 'PRT-P', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98427 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98428, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PAGARES CAP. XVIII/XIX COMPENDIO NORMAS INT. BANCO CENTRAL CHILE'
, '', 'PCD', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98428 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98429, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PAGARES COMPRA DE CARTERA DEL BANCO CENTRAL DE CHILE'
, '', 'PCC', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98429 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98430, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PAGARES DE EMPRESAS DEPOSITADOS', '', 'PEOIND', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98430 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98431, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PAGARES DESCONTABLES DEL BANCO CENTRAL DE CHILE'
, '', 'PDBC', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98431 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98432, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PAGARES EMITIDOS POR EL ESTADO Y BANCO CENTRAL EXTRANJERO'
, '', 'PDBCE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98432 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98433, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PAGARES EMITIDOS POR EMPRESAS EXTRANJERAS'
, '', 'PEE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98433 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98434, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PAGARES EN DOLARES E.E.U.U. DE AMERICA DEL BANCO CENTRAL DE CHILE'
, '', '1460', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98434 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98435, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PAGARES REAJ. BANCO CENTRAL DE CHILE CON TASA DE INTERES FLOTANTE'
, '', 'PTF', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98435 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98436, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PAGARES REAJUSTABLES BANCO CENTRAL DE CHILE CON PAGO DE CUPONES'
, 'PRC', 'PRC', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98436 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98437, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PAGARES REAJUSTABLES DEL BANCO CENTRAL DE CHILE'
, '', 'PRBC', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98437 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98438, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PAGARES REAJUSTABLES EN DOLARES'
, 'PRD', 'PRD', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98438 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98439, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PRESTAMOS A ASEGURADOS DE RIESGO DE CRÉDITO'
, '', 'PARC', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98439 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98440, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PRESTAMOS COMERCIALES'
, '', 'PCOM', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98440 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98441, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PRESTAMOS COMERCIALES A PENSIONADOS'
, '', 'PCOMP', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98441 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98442, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PRESTAMOS COMPLEMENTARIOS DE HIPOTECARIOS'
, '', 'PHIP', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98442 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98443, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PRESTAMOS COMPLEMENTARIOS DE HIPOTECARIOS A PENSIONADOS'
, '', 'PHIPP', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98443 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);


INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98444, 'CMF', 'V1', 'INSTRUMENTOCMF', 'ACCIONES DE SOCIEDADES ANONIMAS ABIERTAS'
, '', 'ACC', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98444 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98445, 'CMF', 'V1', 'INSTRUMENTOCMF', 'ACCIONES DE SOCIEDADES ANONIMAS CERRADAS'
, '', 'ACCR', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98445 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98446, 'CMF', 'V1', 'INSTRUMENTOCMF', 'ACCIONES DE SOCIEDADES EXTRANJERAS'
, 'ACX', 'ACE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98446 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98447, 'CMF', 'V1', 'INSTRUMENTOCMF', 'AMERICAN DEPOSITARY RECEIPTS (Cert. Dep.Americano) Emp.Extranjera'
, '', 'ADR', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98447 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98448, 'CMF', 'V1', 'INSTRUMENTOCMF', 'AMERICAN DEPOSITARY RECEIPTS (Cert. Dep.Americano) Emp.Nacional'
, '', 'ADRN', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98448 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98449, 'CMF', 'V1', 'INSTRUMENTOCMF', 'Acciones de Empresas Concesionarias de Obras de Infraestructura'
, '', 'ACCOI', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98449 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98450, 'CMF', 'V1', 'INSTRUMENTOCMF', 'Acciones de Sociedades Anónimas Cerradas Depositadas'
, '', 'ACCROIND', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98450 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98451, 'CMF', 'V1', 'INSTRUMENTOCMF', 'CERTIFICADO DE DEPOSITO DE VALORES'
, '', 'CDV', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98451 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98452, 'CMF', 'V1', 'INSTRUMENTOCMF', 'CUOTAS DE FONDOS DE INVERSION'
, 'CFI', 'CFI', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98452 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98453, 'CMF', 'V1', 'INSTRUMENTOCMF', 'CUOTAS DE FONDOS DE INVERSION DE CAPITAL EXTRANJERO'
, '', 'CFICE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98453 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98454, 'CMF', 'V1', 'INSTRUMENTOCMF', 'CUOTAS DE FONDOS DE INVERSION PRIVADOS NACIONALES'
, '', 'CFIP', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98454 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98455, 'CMF', 'V1', 'INSTRUMENTOCMF', 'CUOTAS DE FONDOS INVERSION PRIVADOS NACIONALES ACTIVOS INV. EN VAL.EXT.'
, '', 'CFIIEP', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98455 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98456, 'CMF', 'V1', 'INSTRUMENTOCMF', 'CUOTAS DE FONDOS DE INVERSIÓN EXTRANJEROS'
, 'CFX', 'CFIE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98456 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98457, 'CMF', 'V1', 'INSTRUMENTOCMF', 'CUOTAS DE FONDOS DE INVERSIÓN EXTRANJEROS DEPOSITADOS'
, '', 'CFIEOIED', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98457 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98458, 'CMF', 'V1', 'INSTRUMENTOCMF', 'CUOTAS DE FONDOS MUTUOS'
, '', 'CFM', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98458 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98459, 'CMF', 'V1', 'INSTRUMENTOCMF', 'CUOTAS DE FONDOS MUTUOS EXTRANJEROS'
, '', 'CFME', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98459 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98460, 'CMF', 'V1', 'INSTRUMENTOCMF', 'CUOTAS F.I. CONST. EN PAIS, CUYOS ACTIVOS ESTEN INV. EN VAL. EXT'
, '', 'CFIIE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98460 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98461, 'CMF', 'V1', 'INSTRUMENTOCMF', 'CUOTAS F.M. CONST. EN PAIS, CUYOS ACTIVOS ESTEN INV. EN VAL. EXT'
, '', 'CFMIE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98461 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98462, 'CMF', 'V1', 'INSTRUMENTOCMF', 'CUOTAS FONDOS DE INVERSION PRIVADOS NACIONALES DEPOSITADO'
, '', 'CFIPOIND', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98462 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98463, 'CMF', 'V1', 'INSTRUMENTOCMF', 'CUOTAS FONDOS DE INVERSION PRIVADOS NACIONALES DEPOSITADOS ACTIVOS INV.VAL.EXT'
, '', 'CFIIEPOIED', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98463 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98464, 'CMF', 'V1', 'INSTRUMENTOCMF', 'DERECHOS PREFERENTES SUSCRIPCION CUOTAS FONDOS DE INVERSION'
, '', 'DPFI', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98464 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98465, 'CMF', 'V1', 'INSTRUMENTOCMF', 'DERECHOS PREFERENTES SUSCRIPCION CUOTAS FONDOS DE INVERSION EXT.'
, '', 'DPFIE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98465 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98466, 'CMF', 'V1', 'INSTRUMENTOCMF', 'DERECHOS PREFERENTES SUSCRIPCION DE ACCIONES EXTRANJERAS'
, '', 'DPSAE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98466 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98467, 'CMF', 'V1', 'INSTRUMENTOCMF', 'DERECHOS PREFERENTES SUSCRIPCION DE ACCIONES NACIONALES'
, '', 'DPSA', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98467 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98468, 'CMF', 'V1', 'INSTRUMENTOCMF', 'MONETARIOS (ORO, PLATA, ETC.)'
, 'MON', 'MON', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98468 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98469, 'CMF', 'V1', 'INSTRUMENTOCMF', 'OTROS INSTRUMENTOCMFS DE CAPITALIZACION'
, '', 'OTROC', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98469 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98470, 'CMF', 'V1', 'INSTRUMENTOCMF', 'SIMULTANEAS'
, 'SIM', 'SIM', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98470 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98471, 'CMF', 'V1', 'INSTRUMENTOCMF', 'TÍTULOS REPRESENTATIVOS DE INDICES ACCIONARIOS EXTRANJEROS'
, '', 'ETFA', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98471 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98472, 'CMF', 'V1', 'INSTRUMENTOCMF', 'TÍTULOS REPRESENTATIVOS DE INDICES DE COMMODITIES EXTRANJEROS'
, '', 'ETFC', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98472 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98473, 'CMF', 'V1', 'INSTRUMENTOCMF', 'TÍTULOS REPRESENTATIVOS DE INDICES DE DEUDA EXTRANJEROS'
, '', 'ETFB', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98473 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98474, 'CMF', 'V1', 'INSTRUMENTOCMF', 'CALL AMERICANA MERCADO ESTANDAR'
, '', 'CAMS', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98474 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98475, 'CMF', 'V1', 'INSTRUMENTOCMF', 'CALL AMERICANA OVER THE COUNTER'
, '', 'CAOC', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98475 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98476, 'CMF', 'V1', 'INSTRUMENTOCMF', 'CALL EUROPEA MERCADO ESTANDAR'
, '', 'CEOC', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98476 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98477, 'CMF', 'V1', 'INSTRUMENTOCMF', 'FORWARD COMPRADOR'
, '', 'FWC', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98477 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98478, 'CMF', 'V1', 'INSTRUMENTOCMF', 'FORWARD VENDEDOR'
, '', 'FWV', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98478 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98479, 'CMF', 'V1', 'INSTRUMENTOCMF', 'FUTURO COMPRADOR'
, '', 'FC', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98479 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98480, 'CMF', 'V1', 'INSTRUMENTOCMF', 'FUTURO VENDEDOR'
, '', 'FV', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98480 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98481, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PACTO COMPROMISO DE COMPRA'
, '', 'PCCO', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98481 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98482, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PACTO CON COMPROMISO DE VENTA'
, '', 'PCV', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98482 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98483, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PACTO DE COMPRA CON COMPROMISO DE VENTA'
, '', 'PCCV', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98483 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98484, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PACTO DE VENTA CON COMPROMISO DE COMPRA'
, '', 'PVCC', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98484 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98485, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PUT AMERICANA MERCADO ESTANDAR'
, '', 'PAMS', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98485 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98486, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PUT AMERICANA OVER THE COUNTER'
, '', 'PAOC', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98486 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98487, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PUT EUROPEA MERCADO ESTANDAR'
, '', 'PEMS', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98487 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98488, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PUT EUROPEA OVER THE COUNTER'
, '', 'PEOC', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98488 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98489, 'CMF', 'V1', 'INSTRUMENTOCMF', 'SWAPS'
, '', 'S', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98489 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98490, 'CMF', 'V1', 'INSTRUMENTOCMF', 'AVANCE TENEDORES DE POLIZAS'
, '', 'ATP', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98490 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98491, 'CMF', 'V1', 'INSTRUMENTOCMF', 'BIENES RAICES'
, '', 'BZ', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98491 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98492, 'CMF', 'V1', 'INSTRUMENTOCMF', 'FUTURO COMPRADOR'
, '', 'FC', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98492 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98493, 'CMF', 'V1', 'INSTRUMENTOCMF', 'BIENES RAICES HABITACIONALES SITUADOS EN EL EXTERIOR'
, '', 'BRHE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98493 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98494, 'CMF', 'V1', 'INSTRUMENTOCMF', 'BIENES RAICES NO HABITACIONALES SITUADOS EN EL EXTRANJERO'
, '', 'BRNHE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98494 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98495, 'CMF', 'V1', 'INSTRUMENTOCMF', 'CAJA'
, '', 'CJ', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98495 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98496, 'CMF', 'V1', 'INSTRUMENTOCMF', 'CONTRATO SOBRE PRODUCTOS QUE CONSTEN EN FACTURA (LEY N°19.220)'
, '', 'BPF', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98496 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98497, 'CMF', 'V1', 'INSTRUMENTOCMF', 'FILIAL EXTRANJERA'
, '', 'FEX', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98497 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98498, 'CMF', 'V1', 'INSTRUMENTOCMF', 'INVERSIONES MOBILIARIAS EN MUEBLES Y MAQUINAS'
, '', 'IMMM', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98498 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98499, 'CMF', 'V1', 'INSTRUMENTOCMF', 'INVERSIONES MOBILIARIAS EN INSTRUMENTOCMFS'
, '', 'IMOT', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98499 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98500, 'CMF', 'V1', 'INSTRUMENTOCMF', 'INVERSIONES MOBILIARIAS, EQUIPOS COMPUTACIONALES'
, '', 'IMEQ', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98500 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98501, 'CMF', 'V1', 'INSTRUMENTOCMF', 'INVERSIONES MOBILIARIAS, VEHICULOS'
, '', 'IMVH', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98501 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98502, 'CMF', 'V1', 'INSTRUMENTOCMF', 'OTRAS INVERSIONES EXTRANJERAS DEPOSITADAS (Número 7. NGC N° 152)'
, '', 'OIED', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98502 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);


INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98503, 'CMF', 'V1', 'INSTRUMENTOCMF', 'OTRAS INVERSIONES NACIONALES DEPOSITADAS (Número 7. NGC N° 152)'
, '', 'OIND', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98503 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98504, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PARTICIPACIONES EN SOCIEDADES LIMITADAS COLIGADAS'
, '', 'PSCO', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98504 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);


INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98505, 'CMF', 'V1', 'INSTRUMENTOCMF', 'PARTICIPACIONES EN SOCIEDADES LIMITADAS FILIALES'
, '', 'PSFI', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98505 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);


INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98506, 'CMF', 'V1', 'INSTRUMENTOCMF', 'SALDOS EN CUENTAS CORRIENTES (BANCOS)'
, '', 'CT', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98506 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98507, 'CMF', 'V1', 'INSTRUMENTOCMF', 'SALDOS EN CUENTAS CORRIENTES EXTRANJERAS (BANCOS)'
, '', 'CTE', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98507 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT 98508, 'CMF', 'V1', 'INSTRUMENTOCMF', 'TITULO REPRESENTATIVO DE PRODUCTOS (LEY N°19.220)'
, '', 'BTP', 'ACT'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1
    FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion"
    WHERE "IdHomologacion" = 98508 AND "Esquema" = 'CMF' AND "Version" = 'V1' AND "Tipo" = 'INSTRUMENTOCMF'
);

COMMIT ;