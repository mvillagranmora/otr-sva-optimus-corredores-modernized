SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarProductoCuentaContableBasilea";
GO
CREATE VIEW "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarProductoCuentaContableBasilea" AS 
WITH producto AS (
SELECT
	RANK() OVER (PARTITION BY "Producto" ORDER BY "Concepto", "TipoCuentaContable" ASC) AS RANK,
	"Producto"
FROM
	"BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarCuentasBasilea"
)SELECT "Producto" FROM producto WHERE RANK = 1 ORDER BY "Producto" ASC;