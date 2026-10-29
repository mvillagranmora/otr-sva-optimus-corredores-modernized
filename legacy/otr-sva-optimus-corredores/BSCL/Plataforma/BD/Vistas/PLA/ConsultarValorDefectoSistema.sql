SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarValorDefectoSistema";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarValorDefectoSistema" AS
	SELECT 
		"IdValorDefectoSistema",
		"Nombre",
		"Valor",
		"IdEmpresa",
		"IdUsuarioCreador",
		"FechaCreacion",
		"FechaModificacion",
		"IdEstado",
		"TipoEntidad",
		"IdTipoCanalIngreso"
	FROM "BSCL.Plataforma.BD::PLA.ValorDefectoSistema";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarValorDefectoSistema" TO BCS_DESARROLLADOR WITH GRANT OPTION; 