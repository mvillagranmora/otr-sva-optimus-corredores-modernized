SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Orden.BD.Servicios::RegistrarIngresoOrden";

CREATE PROCEDURE "BSCL.Orden.BD.Servicios::RegistrarIngresoOrden" (
 	p_IdBloqueaOrden		NVARCHAR(70),
	p_IdPortafolioCliente	BIGINT,
	p_IdInstrumento			BIGINT,
	p_IdTipoOperacion       NVARCHAR(6),
	p_Cantidad  			DECIMAL(26,8),
	p_CantidadOrdenes       INTEGER,
	p_PrecioLimite 			DECIMAL(26,8),
	p_FechaSistema 			DATE
)
 LANGUAGE SQLSCRIPT
 SQL SECURITY INVOKER
 DEFAULT SCHEMA "BSCL"
 AS
 BEGIN
	 BEGIN AUTONOMOUS TRANSACTION
		 INSERT INTO "BSCL.Orden.BD::ORD.CrearOrdenes_BloqueaOrden"
		 ("IdBloqueaOrden","IdPortafolioCliente","IdInstrumento","IdTipoOperacion","Cantidad","CantidadOrdenes","PrecioLimite", "FechaSistema")
		 VALUES(:p_IdBloqueaOrden,:p_IdPortafolioCliente,:p_IdInstrumento,:p_IdTipoOperacion,:p_Cantidad,:p_CantidadOrdenes, :p_PrecioLimite, :p_FechaSistema );
	 END;
 END;