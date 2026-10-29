SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::MER.ConsultarUsoParidadYTipoCambio"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::MER.ConsultarUsoParidadYTipoCambio" AS
	SELECT 	
		"IdEmpresa"
		,"IdUso"
		,"TipoBusqueda"
		,"NombreUso"
		,"DescripcionUso"	
	FROM "BSCL.Plataforma.BD::MER.UsoParidadYTipoCambio";
			
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::MER.ConsultarUsoParidadYTipoCambio" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
