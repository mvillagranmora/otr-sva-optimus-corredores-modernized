
SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Plataforma.BD.Vistas::IND.UIConsultarIndicadoresClientePortafolio"; 
GO		
CREATE VIEW "BSCL.Plataforma.BD.Vistas::IND.UIConsultarIndicadoresClientePortafolio" AS
SELECT
	IND."IdCliente",
	CLI."NombreCompleto" AS "NombreCliente",
	CLI."DocumentoIdentidad",
	POR."IdPortafolio",
	POR."CodigoPortafolio",
	IND."IdPersona",
	IND."IdTipoCliente",
	IND."IdTipoEntidad",
	IND."NombreTipoIndicador",
	IND."GrupoIndicador",
	IND."IdUso",
	IND."IdTipoIndicador", 
	IND."CodigoTipoIndicador",
	IND."ClaseDato", 
	IND."CantidadDecimales",
	IND."LongitudMinima",
	IND."LongitudMaxima",
	IND."IdTipoPersona",
	IND."ModeloCliente",
	IND."ValorIndicador",
	IND."CodigoValorIndicador",
	IND."NombreValorIndicador",
	POR."IdEstado" AS "EstadoPortafolio",
	(CASE IND."ClaseDato" WHEN 'COMBO' THEN (IND."NombreValorIndicador" )
	 					   ELSE IND."ValorIndicador"
	 END
	 ) AS "UINombreValorIndicador",
	IND."IdIndicador"
FROM 
"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" IND
INNER JOIN 
"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"  CLI
ON (IND."IdCliente"=CLI."IdCliente")
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON (IND."IdPortafolio" = POR."IdPortafolio")
WHERE (CASE IND."ClaseDato" WHEN 'COMBO' THEN (IND."NombreValorIndicador" )
	 					   ELSE IND."ValorIndicador"
	 END
	 ) <> ''



GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::IND.UIConsultarIndicadoresClientePortafolio" TO  BCS_DESARROLLADOR WITH GRANT OPTION;  