SET SCHEMA BSCL;
DROP VIEW "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarCuentasContables";

CREATE VIEW "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarCuentasContables" ( "Cuenta",
	  "CuentaContabilidad",
      "NombreCuenta",
      "DetalleCuenta",
      "NumeroCuentaNombre" ) AS SELECT
      "CuentaOptimus" AS "Cuenta",
      "CuentaContabilidad",
      "NombreCuenta",
      "DetalleCuenta",
      TRIM(' ' 
      FROM "CuentaContabilidad") || ' - ' || "NombreCuenta" AS "NumeroCuentaNombre" 
FROM "BSCL.Contabilidad.BD::ITZ.HomologaCuenta"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarCuentasContables" TO  BCS_DESARROLLADOR WITH GRANT OPTION;		  

