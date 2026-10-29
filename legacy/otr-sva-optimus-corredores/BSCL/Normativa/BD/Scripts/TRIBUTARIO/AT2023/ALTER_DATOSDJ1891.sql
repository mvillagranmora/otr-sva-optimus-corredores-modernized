SET SCHEMA "BSCL";


/*
- Se agrega columna ValorAdquisicion para DJ1891
*/

ALTER TABLE "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1891" ADD ("ValorAdquisicion" DECIMAL(26,
8) CS_FIXED DEFAULT 0 NOT NULL);