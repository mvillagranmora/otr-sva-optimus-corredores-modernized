SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Orden.BD.Servicios::DeleteRegistrarIngresoOrden";

CREATE PROCEDURE "BSCL.Orden.BD.Servicios::DeleteRegistrarIngresoOrden" (
	 p_IdBloqueaOrden NVARCHAR(70)
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	DELETE FROM "BSCL.Orden.BD::ORD.CrearOrdenes_BloqueaOrden"
	WHERE"IdBloqueaOrden" = :p_IdBloqueaOrden;
	COMMIT;
END;