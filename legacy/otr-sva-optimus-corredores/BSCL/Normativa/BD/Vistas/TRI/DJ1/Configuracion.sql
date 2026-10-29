SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1.Configuracion";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1.Configuracion"  AS
SELECT
	C1."Valor" AS pNaturaInfo,
	C2."Valor" AS pObjOpera,
	C3."Valor" AS pGarantias,
	C4."Valor" AS pCompCarte,
	C5."Valor" AS pClauRecoup,
	C6."Valor" AS pClauTermi,
	C7."Valor" AS pMetValora,
	C8."Valor" AS pPlataforma,
	C9."Valor" AS pRutEntCoCe,
	C10."Valor" AS pRutLiqCtP1,
	C11."Valor" AS pRutAgeCal,
	C12."Valor" AS pLeiCtraparte1,
	C13."Valor" AS pLeiEntCoCe,
	C14."Valor" AS pLeiLiqCtP1,
	C15."Valor" AS pLeiAgeCal,
	C16."Valor" AS pLeiInterme,
	C17."Valor" AS pNomAgeCal,
	C18."Valor" AS pKRutCtraparte1,
	C19."Valor" AS pTPagoEfec
FROM
	"BSCL.Normativa.BD::NOR.Configuracion" C1,
	"BSCL.Normativa.BD::NOR.Configuracion" C2,
	"BSCL.Normativa.BD::NOR.Configuracion" C3,
	"BSCL.Normativa.BD::NOR.Configuracion" C4,
	"BSCL.Normativa.BD::NOR.Configuracion" C5,
	"BSCL.Normativa.BD::NOR.Configuracion" C6,
	"BSCL.Normativa.BD::NOR.Configuracion" C7,
	"BSCL.Normativa.BD::NOR.Configuracion" C8,
	"BSCL.Normativa.BD::NOR.Configuracion" C9,
	"BSCL.Normativa.BD::NOR.Configuracion" C10,
	"BSCL.Normativa.BD::NOR.Configuracion" C11,
	"BSCL.Normativa.BD::NOR.Configuracion" C12,
	"BSCL.Normativa.BD::NOR.Configuracion" C13,
	"BSCL.Normativa.BD::NOR.Configuracion" C14,
	"BSCL.Normativa.BD::NOR.Configuracion" C15,
	"BSCL.Normativa.BD::NOR.Configuracion" C16,
	"BSCL.Normativa.BD::NOR.Configuracion" C17,
	(SELECT TOP 1 "DocumentoIdentidadDeclarante" AS "Valor" FROM "BSCL.Normativa.BD::DOC.Documento" WHERE "Esquema" = 'DJ1') AS C18,
	"BSCL.Normativa.BD::NOR.Configuracion" C19
WHERE
	C1."CodigoParametro" = 'DJ1_pNaturaInfo'	AND
	C2."CodigoParametro" = 'DJ1_pObjOpera'		AND
	C3."CodigoParametro" = 'DJ1_pGarantias'		AND
	C4."CodigoParametro" = 'DJ1_pCompCarte'		AND
	C5."CodigoParametro" = 'DJ1_pClauRecoup'	AND
	C6."CodigoParametro" = 'DJ1_pClauTermi'		AND
	C7."CodigoParametro" = 'DJ1_pMetValora'		AND
	C8."CodigoParametro" = 'DJ1_pPlataforma'	AND
	C9."CodigoParametro" = 'DJ1_pRutEntCoCe'	AND
	C10."CodigoParametro" = 'DJ1_pRutLiqCtP1'	AND
	C11."CodigoParametro" = 'DJ1_pRutAgeCal'	AND
	C12."CodigoParametro" = 'DJ1_pLeiCtraparte1'AND
	C13."CodigoParametro" = 'DJ1_pLeiEntCoCe'	AND
	C14."CodigoParametro" = 'DJ1_pLeiLiqCtP1'	AND
	C15."CodigoParametro" = 'DJ1_pLeiAgeCal'	AND
	C16."CodigoParametro" = 'DJ1_pLeiInterme'	AND
	C17."CodigoParametro" = 'DJ1_pNomAgeCal'	AND
	C19."CodigoParametro" = 'DJ1_pTPagoEfec'

WITH READ ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1.Configuracion" TO bcs_desarrollador WITH GRANT OPTION;