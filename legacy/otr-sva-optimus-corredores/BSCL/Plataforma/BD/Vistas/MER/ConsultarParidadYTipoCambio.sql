SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::MER.ConsultarParidadYTipoCambio"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::MER.ConsultarParidadYTipoCambio" AS
	SELECT 	
		par."IdEmpresa",
		"Fecha", 
		par."IdMoneda",
		mon."Nombre" as "NombreMoneda",
		"IdTipoRegistro",
		"ParidadMonedaCambio",
		par."IdEstado",
		"IdUsuarioUltimaModificacion",
		usu."CodigoUsuario" AS "CodigoUsuarioUltimaModificacion",
		"FechaUltimaModificacion"
		
	FROM "BSCL.Plataforma.BD::MER.ParidadYTipoCambio" AS par
	INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" mon on par."IdMoneda" = mon."IdMoneda"
	LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" usu on par."IdUsuarioUltimaModificacion" = usu."IdUsuario";
			
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::MER.ConsultarParidadYTipoCambio" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
