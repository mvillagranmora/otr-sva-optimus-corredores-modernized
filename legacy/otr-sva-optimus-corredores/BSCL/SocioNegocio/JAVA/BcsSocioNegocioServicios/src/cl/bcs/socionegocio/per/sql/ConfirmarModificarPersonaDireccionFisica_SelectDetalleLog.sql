SELECT
	MOD."IdTipoEntidad" AS "SubRegistro",
	MOD."CampoModificado" AS "CampoRegistro",
	CASE
		WHEN MOD."CampoModificado" = 'IdCiudad' AND CAST(CIUDANT."IdCiudad" AS NVARCHAR) = COALESCE(MOD."ValorAntiguo",'') THEN CIUDANT."NombreCiudad"
		WHEN MOD."CampoModificado" = 'IdDivisionPais' AND COALESCE(MOD."ValorAntiguo",'') = '0' THEN ''
		WHEN MOD."CampoModificado" = 'IdDivisionPais' AND CAST(DIVPAANT."IdDivisionPais" AS NVARCHAR) = COALESCE(MOD."ValorAntiguo",'') THEN DIVPAANT."NombreDivisionPais"
		WHEN MOD."CampoModificado" = 'CodigoProposito' AND PROPANT."IdPropositoMedioContacto" = COALESCE(MOD."ValorAntiguo",'') THEN PROPANT."Descripcion"
		WHEN MOD."CampoModificado" = 'IdEstado' AND ESTANT."IdEstado" = COALESCE(MOD."ValorAntiguo",'') THEN ESTANT."Nombre"
		ELSE MOD."ValorAntiguo"
	END AS "ValorAntiguo",
	CASE
		WHEN MOD."CampoModificado" = 'IdCiudad' AND CAST(CIUDNUE."IdCiudad" AS NVARCHAR) = COALESCE(MOD."ValorNuevo",'') THEN CIUDNUE."NombreCiudad"
		WHEN MOD."CampoModificado" = 'IdDivisionPais' AND COALESCE(MOD."ValorNuevo",'') = '0' THEN ''
		WHEN MOD."CampoModificado" = 'IdDivisionPais' AND CAST(DIVPANUE."IdDivisionPais" AS NVARCHAR) = COALESCE(MOD."ValorNuevo",'') THEN DIVPANUE."NombreDivisionPais"
		WHEN MOD."CampoModificado" = 'CodigoProposito' AND PROPNUE."IdPropositoMedioContacto" = COALESCE(MOD."ValorNuevo",'') THEN PROPNUE."Descripcion"
		WHEN MOD."CampoModificado" = 'IdEstado' AND ESTNUE."IdEstado" = COALESCE(MOD."ValorNuevo",'') THEN ESTNUE."Nombre"
		ELSE MOD."ValorNuevo"
	END AS "ValorNuevo"
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" MOD
LEFT JOIN "BSCL.Plataforma.BD::REG.DivisionPais" DIVPAANT ON (CAST(DIVPAANT."IdDivisionPais" AS NVARCHAR) = COALESCE(MOD."ValorAntiguo",''))
LEFT JOIN "BSCL.Plataforma.BD::REG.DivisionPais" DIVPANUE ON (CAST(DIVPANUE."IdDivisionPais" AS NVARCHAR) = COALESCE(MOD."ValorNuevo",''))
LEFT JOIN "BSCL.Plataforma.BD::REG.Ciudad" CIUDANT ON (CAST(CIUDANT."IdCiudad" AS NVARCHAR) = COALESCE(MOD."ValorAntiguo",''))
LEFT JOIN "BSCL.Plataforma.BD::REG.Ciudad" CIUDNUE ON (CAST(CIUDNUE."IdCiudad" AS NVARCHAR) = COALESCE(MOD."ValorNuevo",''))
LEFT JOIN "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto" PROPANT ON (PROPANT."IdPropositoMedioContacto" = COALESCE(MOD."ValorAntiguo",''))
LEFT JOIN "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto" PROPNUE ON (PROPNUE."IdPropositoMedioContacto" = COALESCE(MOD."ValorNuevo",''))
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" ESTANT ON (ESTANT."IdEstado" = COALESCE(MOD."ValorAntiguo",''))
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" ESTNUE ON (ESTNUE."IdEstado" = COALESCE(MOD."ValorNuevo",''))
WHERE MOD."IdSolicitudCambio" IN (:p_IdSolicitudCambio)
