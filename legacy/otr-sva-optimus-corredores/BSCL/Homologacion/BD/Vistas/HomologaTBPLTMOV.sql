SET SCHEMA "BSCL";
DROP VIEW "BSCL.Homologacion.BD.Vistas::HomologaTBPLTMOV";

CREATE VIEW "BSCL.Homologacion.BD.Vistas::HomologaTBPLTMOV" AS
WITH 
	conceptosCuentaInversion as (
		SELECT "IdConcepto", 'AB' AS "TipoConcepto", '' AS "IdTipoOperacion"
		FROM "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"
		WHERE "TipoConcepto" IN ('AB','AM')
		  AND "Origen" IN ('CTA')
		UNION ALL
		SELECT "IdConcepto", 'CA' AS "TipoConcepto", '' AS "IdTipoOperacion"
		FROM "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion"
		WHERE "TipoConcepto" IN ('CA','AM')
		  AND "Origen" IN ('CTA')
	),
	conceptosOperaciones as (
		SELECT "IdConcepto", 'CM' AS "TipoConcepto", "IdTipoOperacion"
		FROM "BSCL.Facturacion.BD::FAC.ConceptoFacturacion"
		WHERE "Origen" IN ('ORD')
	),
	conceptosIngresos as (
		SELECT "IdConcepto", 'IT' AS "TipoConcepto", "IdTipoOperacion"
		FROM "BSCL.Custodia.BD::CUS.ConceptoCustodia"
		WHERE "IdTipoOperacion" IN ('INGTIT')
	)
SELECT
	 SINFO."TipoConcepto" AS TIPO_REG				--char	2
	,CASE WHEN CPT."TipoConcepto" = 'SISTEMA' THEN TOPE."IdTipoOperacion"
		  ELSE ''
	  END AS CODIGO									--char	4
	,'' AS COD_MOVTO_CONT							--char	4
	,SINFO."IdConcepto" AS CONCEPTO_MVTO			--char	2
	,'' AS CTA_CONTAB								--char	12
	,CPT."Nombre" AS DESCRIP_MOVTO					--char	40
	, 0 AS FPT_CAN_ACUM_VC	--decimal	0
	, 0 AS FPT_CAN_COM_APLAZO	--decimal	0
	, 0 AS FPT_CAN_CUST	--decimal	0
	, 0 AS FPT_CAN_GTIA	--decimal	0
	, 0 AS FPT_CAN_TRAM_COM	--decimal	0
	, 0 AS FPT_CAN_TRAM_VTA	--decimal	0
	, 0 AS FPT_CAN_VTA_APLAZO	--decimal	0
	, 0 AS FPT_GASTO_APLAZO	--decimal	0
	, 0 AS FPT_GASTO_CTDO	--decimal	0
	, 0 AS FPT_MTO_ACUM_CUP	--decimal	0
	, 0 AS FPT_MTO_REAL_ACUM_VC	--decimal	0
	, 0 AS FPT_REAL_ACUM_VC	--decimal	0
	, 0 AS FPT_REAL_ACUM_VTA	--decimal	0
	, 0 AS FPT_SDO_APLAZO	--decimal	0
	, 0 AS FPT_SDO_COMPROM	--decimal	0
	, 0 AS FPT_SDO_DISP	--decimal	0
	, 0 AS FPT_SDO_RETEN	--decimal	0
	, 0 AS FSP_CAN_ACUM_VC	--decimal	0
	, 0 AS FSP_CAN_COM_APLAZO	--decimal	0
	, 0 AS FSP_CAN_CUST	--decimal	0
	, 0 AS FSP_CAN_GTIA	--decimal	0
	, 0 AS FSP_CAN_TRAM_COM	--decimal	0
	, 0 AS FSP_CAN_TRAM_VTA	--decimal	0
	, 0 AS FSP_CAN_VTA_APLAZO	--decimal	0
	, 0 AS FSP_GASTO_APLAZO	--decimal	0
	, 0 AS FSP_GASTO_CTDO	--decimal	0
	, 0 AS FSP_MTO_ACUM_CUP	--decimal	0
	, 0 AS FSP_MTO_REAL_ACUM_VC	--decimal	0
	, 0 AS FSP_REAL_ACUM_VC	--decimal	0
	, 0 AS FSP_REAL_ACUM_VTA	--decimal	0
	, 0 AS FSP_SDO_APLAZO	--decimal	0
	, 0 AS FSP_SDO_COMPROM	--decimal	0
	, 0 AS FSP_SDO_DISP	--decimal	0
	, 0 AS FSP_SDO_RETEN	--decimal	0
	,'' AS IND_CPDTPC	--char	1
	,'' AS IND_CTCIFM	--char	1
	,'' AS IND_CTCIIF	--char	1
	,'' AS IND_CTCIRF	--char	1
	,'' AS IND_CTCIRV	--char	1
	,'' AS IND_CTCTCL	--char	1
	,'' AS IND_CTCUFM	--char	1
	,'' AS IND_CTCURF	--char	1
	,'' AS IND_CTCURV	--char	1
	,'' AS IND_CTTRAS	--char	1
	,'' AS IND_CTTRTP	--char	1
	,'' AS IND_DEB_HAB	--char	1
	,'' AS IND_ENV_MVTO	--char	1
	,'' AS IND_ORSACL	--char	1
	,'' AS IND_PLLINR	--char	1
	,'' AS IND_TIT_CCTE	--char	1
	,'' AS IND_TRASPASO	--char	1
	,'' AS TIPO_CARTERA	--char	2
	,'' AS FAM_OPERAC	--char	2
	,'' AS CONCEP_MVTO_EX1	--char	2
	,'' AS CONCEP_MVTO_EX2	--char	2
	, 0 AS VALUTA	--decimal	0
	,'' AS CONTRA_CTA	--char	12
	,'' AS IND_USO_GARANTIA	--char	1
	,'' AS IND_AFECTO_GMF	--char	1
	,'' AS IND_CTRFTP	--char	2
	,'' AS INDICADOR_RENDIMIENTOS	--char	1
	,'' AS IND_VENTA	--char	1
	,'' AS GENERA_MOVTO_APT	--char	1
	,'' AS RUT_CLI	--char	11
	,'' AS CTA_CONTAB_RELA	--char	12
	,'' AS IND_CENTRAL	--char	1
FROM (
	SELECT "IdConcepto", "TipoConcepto", "IdTipoOperacion" FROM conceptosCuentaInversion
	UNION ALL
	SELECT "IdConcepto", "TipoConcepto", "IdTipoOperacion" FROM conceptosOperaciones
	UNION ALL
	SELECT "IdConcepto", "TipoConcepto", "IdTipoOperacion" FROM conceptosIngresos
) AS SINFO
INNER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS CPT ON CPT."IdConcepto" = SINFO."IdConcepto"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TOPE ON TOPE."IdTipoOperacion" = SINFO."IdTipoOperacion"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Homologacion.BD.Vistas::HomologaTBPLTMOV" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
