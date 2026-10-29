SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarParidadYTipoCambio"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarParidadYTipoCambio" AS
	SELECT 	
		"IdParidadYTipoCambio",
		"Fecha",
		par."IdMoneda",
		mon."Nombre" as "NombreMoneda",
		"IdUsuarioIngreso",
		usu."CodigoUsuario" as "CodigoUsuarioIngreso",
		"TipoCambioMercado",
		"ParidadMercado",
		"Origen"
		
	FROM "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" AS par
	INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" mon on par."IdMoneda" = mon."IdMoneda"
	LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" usu on par."IdUsuarioIngreso" = usu."IdUsuario";
			
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarParidadYTipoCambio" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
