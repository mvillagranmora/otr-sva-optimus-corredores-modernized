SET SCHEMA "BSCL";

DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.CONTRATOS_FIRMADOS";

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.CONTRATOS_FIRMADOS" AS

SELECT
    "per_id"               AS "PER_ID",
    "cuenta"               AS "CUENTA",
    "rpt_alias"            AS "RPT_ALIAS",
    "rpt_alias_version"    AS "RPT_ALIAS_VERSION",
    "firmado"              AS "FIRMADO",
    "fecha_firma"          AS "FECHA_FIRMA",
    "estado"               AS "ESTADO",
    "cod_age_ingresa"      AS "COD_AGE_INGRESA",
    "cod_age_confirma"     AS "COD_AGE_CONFIRMA",
    "codigo_seguridad"     AS "CODIGO_SEGURIDAD",
    "canal"                AS "CANAL",
    "intentos_ingreso"     AS "INTENTOS_INGRESO",
    "fecha_firma_confirmada" AS "FECHA_FIRMA_CONFIRMADA",
    "map_datos_persona"    AS "MAP_DATOS_PERSONA",
    "username_ingresa"     AS "USERNAME_INGRESA",
    "username_confirma"    AS "USERNAME_CONFIRMA",
    "id"                   AS "ID",
    "canal_confirma"       AS "CANAL_CONFIRMA",
    "fecha_caducidad"      AS "FECHA_CADUCIDAD",
    "fecha_final_confirma" AS "FECHA_FINAL_CONFIRMA"
FROM "BSCL.Replica.BD::MIG.CONTRATOS_FIRMADOS"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.CONTRATOS_FIRMADOS" TO BCS_DESARROLLADOR WITH GRANT OPTION;
