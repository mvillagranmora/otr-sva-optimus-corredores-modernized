SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarLibroOrdenesOperadores"; 
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarLibroOrdenesOperadores" AS 
	SELECT
		LOOPE."IdLibroOrdenesOperador",
		LOOPE."IdEmpresa",
		LOOPE."IdLibroOrdenes",
		LOOPE."CodigoOperador",
		LOOPE."IdEstado"
	FROM "BSCL.Orden.BD::ORD.LibroOrdenesOperador" AS LOOPE
	INNER JOIN "BSCL.Plataforma.BD.Vistas::TRA.ConsultarConfiguracionOperador" CONF ON LOOPE."CodigoOperador" = CONF."CodigoOperador"
	WHERE LOOPE."IdEstado" = 'ACT';

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarLibroOrdenesOperadores" TO BCS_DESARROLLADOR WITH GRANT OPTION;
