SELECT COR."IdCorredorCorresponsal",pp."IdPerfilCobros",pp."EsPerfilPersonalizado",pp."IdEstado" AS "IdEstadoPerfilCobros",
det."IdTipoProducto",det."IdTipoOperacion",det."IdTipoCanalIngreso",det."IdBolsa",
def."IdClaseCobro",cla."IdClaseCobroBase",def."IdTipoCobro",tip."NombreTipoCobro",def."CodigoDefinicionCobro",def."IdEstado",def."Descripcion",def."CheckCobroMonedaOperacion",def."IdMonedaCobroOtraMoneda",def."MontoFijo_MontoCobro",
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
FROM "BSCL.Orden.BD::ORD.CorredorCorresponsal" COR
INNER JOIN "BSCL.Plataforma.BD::COM_2.PerfilCobros" pp on COR."IdPerfilCobros" = pp."IdPerfilCobros"
INNER JOIN "BSCL.Plataforma.BD::COM_2.DetallePerfilCobro" det on det."IdPerfilCobros" = pp."IdPerfilCobros"
INNER JOIN "BSCL.Plataforma.BD::COM_2.DefinicionCobro" def on def."CodigoDefinicionCobro" = det."CodigoDefinicionCobro"
INNER JOIN "BSCL.Plataforma.BD::COM_2.TipoCobro" tip on def."IdTipoCobro" = tip."IdTipoCobro"
INNER JOIN "BSCL.Plataforma.BD::COM_2.ClaseCobro" cla on def."IdClaseCobro" = cla."IdClaseCobro"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarBaseTasaCobro" baseTasa ON def."Otros_ValorBaseTasa" = baseTasa."IdBaseTasaCobro"
WHERE COR."IdCorredorCorresponsal" IN (:idsCorredorCorresponsal) AND det."Borrado" = 'N'
ORDER BY def."Otros_CheckOperacionContado" DESC  ,def."IdClaseCobro" DESC ;

