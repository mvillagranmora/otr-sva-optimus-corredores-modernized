SELECT
  IFNULL (MAX("ID"), 0) + 1 AS "idQuery"
 FROM "BSCL"."BSCL.Normativa.BD::NOR.DatosREPO2P1";