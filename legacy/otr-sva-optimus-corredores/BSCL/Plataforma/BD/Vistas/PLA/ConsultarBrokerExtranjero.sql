SET SCHEMA "BSCL"; 
GO
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBrokerExtranjero"; 
GO

CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBrokerExtranjero" AS
	SELECT 	
		BROKER."Codigo", 
		BROKER."IdEstado", 
		BROKER."IdPersona",
		ConsCli."IdTipoCliente",
		ConsCli."IdCliente",
		ConsPor."IdPortafolio",
		BROKER."IdUsuarioCreador",
		BROKER."IdUsuarioUltimaModificacion",
		BROKER."FechaCreacion",
		BROKER."FechaUltimaModificacion", 
		ESTADO."Nombre" AS "NombreEstado",
		ConsCli."NombreCompleto" as "NombreCompleto",
		BROKER."Codigo" || ' - ' || ConsCli."NombreCompleto" as "CodigoNombreCompleto",
		ConsCli."DocumentoIdentidad" as "DocumentoIdentidad",
		ConsCli."IdCategoriaInversionista",
		IfNull(DirFis."Calle", '') AS "DireccionCalle",
		IfNull(DirFis."Numero", '') AS "DireccionNumero", 
		IfNull(DirFis."Departamento",'') As "DireccionDepartamento",
		ConsPor."IdPerfilComercialBaseOrigen",
		ConsCli."IdEmpleadoEjecutivo",
		ConsPor."IdPerfilProductos"
	FROM "BSCL.Plataforma.BD::PLA.BrokerExtranjero" AS BROKER
	  INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS ConsPer
	    ON ConsPer."IdPersona" = BROKER."IdPersona"
	  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS ConsCli 
	    ON ConsCli."IdPersona" = BROKER."IdPersona"
	  LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS ConsPor
	    ON ConsPor."IdCliente" = ConsCli."IdCliente"  
	  LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as ESTADO 
	    ON ESTADO."IdEstado" = BROKER."IdEstado"
	  LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" As DirFis
	     On DirFis."IdDireccionFisica" = ConsCli."IdDireccionFisicaPrincipal"
	    And DirFis."IdEstado" = 'ACT' 
	WHERE BROKER."Borrado" = 'N';
GO

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBrokerExtranjero" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
