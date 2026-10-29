SELECT
por."IdPortafolio",pp."IdPerfilCobros",pp."EsPerfilPersonalizado",pp."IdEstado" AS "IdEstadoPerfilCobros",
det."IdTipoProducto",det."IdTipoOperacion",det."IdTipoCanalIngreso",det."IdBolsa",
def."IdClaseCobro",cla."IdClaseCobroBase",def."IdTipoCobro",tip."NombreTipoCobro",def."CodigoDefinicionCobroBase" AS "CodigoDefinicionCobro",def."IdEstado",def."Descripcion",def."CheckCobroMonedaOperacion",def."IdMonedaCobroOtraMoneda",def."MontoFijo_MontoCobro",
def."MontoFijo_CheckMontoOperacionMenor",def."MontoFijo_ValorMontoOperacionMenor",def."MontoFijo_CheckSiNo",def."MontoFijo_ValorMontoSiNo",def."PorcentualFijo_ValorCobro",
def."PorcentualFijo_CheckSobreMonto",def."PorcentualFijo_CheckSobreCantidad",def."PorcentualFijo_CheckMayorEntreDos",def."PorcentualFijo_CheckMenorEntreDos",
def."PorcentualFijo_CheckMontoMinimo",def."PorcentualFijo_ValorMontoMinimo",def."PorcentualFijo_CheckMontoMaximo",def."PorcentualFijo_ValorMontoMaximo",
def."PorcentualFijo_IdMonedaMinimoMaximo",def."Otros_CheckOperacionContado",def."Otros_CheckOperacionPlazo",def."Otros_CheckBaseTasa",def."Otros_ValorBaseTasa",
def."Otros_CheckInteresSimple",def."Otros_CheckInteresCompuesto",def."Otros_CheckAllIn",def."Otros_ValorAllInMontoOperacionBase",def."Otros_CheckAfectoImpuesto",
def."Otros_CheckAplicaPortafolio",def."Otros_CheckAplicaCorresponsal",def."Otros_CheckPermiteAumentarValor",def."Otros_CheckPermiteDisminuirValor",
def."PorcentualRango_IdMonedaRangoMonto",def."PorcentualRango_IdTipoRangoCobro",def."Otros_IncluyeExcluyeEmisoresCheck",def."Otros_IncluyeExcluyeInstrumentosCheck",
def."Otros_IncluyeExcluyeCorresponsalesCheck",def."Otros_IncluyeExcluyeEmisoresValor",def."Otros_IncluyeExcluyeInstrumentosValor",def."Otros_IncluyeExcluyeCorresponsalesValor",
def."PorcentualTasaPlazo_ValorCobro",def."MontoFijoPorUnidad_AllInCheckValor",def."MontoFijoPorUnidad_MontoCobro",
baseTasa."ValorBaseTasaCobro"
FROM "BSCL.Portafolio.BD::POR.Portafolio" por
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" cli on por."IdCliente" = cli."IdCliente"
INNER JOIN "BSCL.Plataforma.BD::COM_2.PerfilCobros" pp on por."IdPerfilCobrosPeruExtra" = pp."IdPerfilCobros"
INNER JOIN "BSCL.Plataforma.BD::COM_2.DetallePerfilCobro" det on det."IdPerfilCobros" = pp."IdPerfilCobros"
INNER JOIN "BSCL.Plataforma.BD::COM_2.DefinicionCobro" def on def."CodigoDefinicionCobro" = det."CodigoDefinicionCobro"
INNER JOIN "BSCL.Plataforma.BD::COM_2.TipoCobro" tip on def."IdTipoCobro" = tip."IdTipoCobro"
INNER JOIN "BSCL.Plataforma.BD::COM_2.ClaseCobro" cla on def."IdClaseCobro" = cla."IdClaseCobro"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarBaseTasaCobro" baseTasa ON def."Otros_ValorBaseTasa" = baseTasa."IdBaseTasaCobro"
LEFT JOIN "BSCL.Plataforma.BD::COM_2.DefinicionCobroEmisor" defEmi ON defEmi."CodigoDefinicionCobro" = def."CodigoDefinicionCobro" AND defEmi."IdEmisor" IN (:idsEmisor)
LEFT JOIN "BSCL.Plataforma.BD::COM_2.DefinicionCobroInstrumento" defInst ON defInst."CodigoDefinicionCobro" = def."CodigoDefinicionCobro" AND defInst."IdInstrumento" IN (:idsInstrumento)
LEFT JOIN "BSCL.Plataforma.BD::COM_2.DefinicionCobroCorredoraCorresponsal" defCorr ON defCorr."CodigoDefinicionCobro" = def."CodigoDefinicionCobro" AND defCorr."IdCorredoraCorresponsal" IN (:idsCorredoraCorresponsal)
WHERE
	por."IdPortafolio" IN (:idsPortafolios)
AND cli."IdEmpresa" = :IdEmpresa
AND det."Borrado" = 'N'
AND	(
		CASE
			WHEN def."Otros_IncluyeExcluyeEmisoresValor" = 'E' AND
				 def."Otros_IncluyeExcluyeInstrumentosValor" = 'N' AND
				 def."Otros_IncluyeExcluyeCorresponsalesValor" = 'N' AND
				 defEmi."IdEmisor" IS NOT NULL THEN 0 
			ELSE 1
		END
	) = 1
AND (
		CASE
			WHEN def."Otros_IncluyeExcluyeInstrumentosValor" = 'E' AND
				 def."Otros_IncluyeExcluyeCorresponsalesValor" = 'N' AND
				 defInst."IdInstrumento" IS NOT NULL THEN 0
			ELSE 1
		END
	) = 1
AND (
		CASE
			WHEN def."Otros_IncluyeExcluyeCorresponsalesValor" = 'E' AND
				 defCorr."IdCorredoraCorresponsal" IS NOT NULL THEN 0
			ELSE 1
		END
	) = 1
ORDER BY
	def."Otros_CheckOperacionContado" DESC
   ,def."IdClaseCobro" DESC;