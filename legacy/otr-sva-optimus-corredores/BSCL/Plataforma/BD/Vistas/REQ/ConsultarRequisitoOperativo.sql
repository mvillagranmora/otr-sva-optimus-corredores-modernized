SET SCHEMA "BSCL";
DROP VIEW "BSCL.Plataforma.BD.Vistas::REQ.ConsultarRequisitoOperativo";
CREATE VIEW "BSCL.Plataforma.BD.Vistas::REQ.ConsultarRequisitoOperativo" AS
	
	SELECT
		RO."IdRequisitoOperativo",
		RO."IdModeloRequisitoOperativo",
		RO."IdCondicionOperativa",
		CO."Nombre" as "NombreCondicionOperativa",
		RO."IdTipoEtapa",
		TE."Nombre" as "NombreTipoEtapa",
		RO."IdTipoComportamiento",
		TC."Nombre" as "NombreTipoComportamiento",
		--RO."IdTipoOperacionUsuario",
		--TIPOP."Nombre" as "NombreTipoOperacionUsuario",
		RO."IdTipoCanalIngreso",
		CASE RO."IdTipoCanalIngreso" WHEN '0' THEN 'TODOS' ELSE TCI."Nombre" END as "NombreTipoCanalIngreso",
		RO."IdMoneda",
		MON."Nombre" as "NombreMoneda",
		RO."ValorAtributo1",
		RO."ValorAtributo2",
		RO."IdUsuarioCreador",
		RO."FechaCreacion",
		RO."FechaModificacion",  
		RO."IdEmpresa",
		RO."IdEstado",
		ESTRO."Nombre" as "NombreEstado" 
	FROM "BSCL.Plataforma.BD::REQ.RequisitoOperativo" as RO
	LEFT JOIN "BSCL.Plataforma.BD::REQ.ModeloRequisitoOperativo" as MRO	ON (RO."IdModeloRequisitoOperativo" = MRO."IdModeloRequisitoOperativo" AND RO."IdEmpresa" = MRO."IdEmpresa")
	LEFT JOIN "BSCL.Plataforma.BD::REQ.CondicionOperativa" as CO		ON (RO."IdCondicionOperativa" = CO."IdCondicionOperativa")
	LEFT JOIN "BSCL.Plataforma.BD::REQ.TipoEtapa" as TE					ON (TE."IdTipoEtapa" = RO."IdTipoEtapa")
	LEFT JOIN "BSCL.Plataforma.BD::REQ.TipoComportamiento" as TC		ON (RO."IdTipoComportamiento" = TC."IdTipoComportamiento")
	--LEFT JOIN "BSCL.Plataforma.BD::REQ.TipoOperacionUsuario" as TIPOP	ON (TIPOP."IdTipoOperacionUsuario" = RO."IdTipoOperacionUsuario")
	LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" as TCI			ON (TCI."IdTipoCanalIngreso" = RO."IdTipoCanalIngreso")
	--LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as ESTRO					ON(ESTRO."IdEstado" = RO."IdEstado")
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::REQ.ConsultarEstadoCondicionOperativa" as ESTRO ON (ESTRO."IdEstado" = RO."IdEstado")
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" as MON					ON (MON."IdMoneda" = RO."IdMoneda");
	
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::REQ.ConsultarRequisitoOperativo" TO BCS_DESARROLLADOR WITH GRANT OPTION;
