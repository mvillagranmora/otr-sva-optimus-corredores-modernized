SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDocumentoIdentidad";
GO
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDocumentoIdentidad" 
AS 

	SELECT 
		"DOC"."IdDocumentoIdentidad",
		"DOC"."IdPersona",
		"DOC"."IdTipoDocumentoIdentidad",
		"TIPDOC"."NombreTipoDocumentoIdentidad",
		"DOC"."DocumentoIdentidad",
		"DOC"."NumeroSerie",
		"DOC"."FechaEmision",
		"DOC"."FechaVencimiento",
		--"DOC"."IdEstado",
        "BSCL.SocioNegocio.BD.Utilitarios::SOC.ObtenerEstadoAutorizacion"('DOCUMENTOIDENTIDAD',"DOC"."IdEstado","DOC"."IdEstadoAutorizacion") as "IdEstado",
		--"BSCL.SocioNegocio.BD.Utilitarios::SOC.ObtenerEstadoAutorizacion"('DOCUMENTOIDENTIDAD',"DOC"."IdEstado","DOC"."IdEstadoAutorizacion") AS "IdEstado",
		"EST"."Nombre" as "Estado",
		"DOC"."FechaIngreso",
		"DOC"."FechaCreacion",
		"DOC"."FechaModificacion",
		"DOC"."IdCarga",
		"DOC"."IdEstadoAutorizacion",
		"DOC"."IdOrigen",
		CASE WHEN "DOC"."Predeterminado" = 'S' THEN 'UI_SI' ELSE 'UI_NO' END AS "Predeterminado",
		"SOC"."IdEmpresa",
		"SOC"."IdSocioDeNegocio"
	FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC"
	LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" as "SOC" on "SOC"."IdPersona" = "DOC"."IdPersona"
	LEFT JOIN "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" as "TIPDOC" on "DOC"."IdTipoDocumentoIdentidad" = "TIPDOC"."IdTipoDocumentoIdentidad"
	LEFT JOIN "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" as "ESTSOC" on "BSCL.SocioNegocio.BD.Utilitarios::SOC.ObtenerEstadoAutorizacion"('DOCUMENTOIDENTIDAD',"DOC"."IdEstado","DOC"."IdEstadoAutorizacion") = "ESTSOC"."IdEstado" AND "ESTSOC"."NombreEntidad" = 'DOCUMENTOIDENTIDAD'
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as "EST" on "ESTSOC"."IdEstado" = "EST"."IdEstado"
	WHERE "DOC"."Borrado"='N'
WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDocumentoIdentidad" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO