SET SCHEMA "BSCL";


/**
- Se inserta nuevo campo Valor Adquisicion para AT2023 de DJ1891
**/
DELETE FROM BSCL."BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema" = 'DJ1891' AND "Version" = '2022' AND "Nombre" = 'ValorAdquisicion' AND "CodigoSII" = 'C17';

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
SELECT 'DJ1891', '2022', 'ValorAdquisicion', 'C17', '', 25, 'Valor Adquisición', ' ', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos', 15, 0
FROM dummy
WHERE NOT EXISTS (
	SELECT 1 FROM BSCL."BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema" = 'DJ1891' AND "Version" = '2022' AND "Nombre" = 'ValorAdquisicion' AND "CodigoSII" = 'C17'
);
