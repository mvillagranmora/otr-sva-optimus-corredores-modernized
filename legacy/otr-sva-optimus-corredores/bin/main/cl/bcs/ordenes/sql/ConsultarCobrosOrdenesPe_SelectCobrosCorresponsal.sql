SELECT corr."IdCorredorCorresponsal",
def."IdClaseCobro",cla."IdClaseCobroBase",def."IdTipoCobro",tip."NombreTipoCobro",def."CodigoDefinicionCobro",def."IdEstado",def."Descripcion",def."CheckCobroMonedaOperacion",def."IdMonedaCobroOtraMoneda",def."MontoFijo_MontoCobro",
def."MontoFijo_CheckMontoOperacionMenor",def."MontoFijo_ValorMontoOperacionMenor",def."MontoFijo_CheckSiNo",def."MontoFijo_ValorMontoSiNo",def."PorcentualFijo_ValorCobro",
def."PorcentualFijo_CheckSobreMonto",def."PorcentualFijo_CheckSobreCantidad",def."PorcentualFijo_CheckMayorEntreDos",def."PorcentualFijo_CheckMenorEntreDos",
def."PorcentualFijo_CheckMontoMinimo",def."PorcentualFijo_ValorMontoMinimo",def."PorcentualFijo_CheckMontoMaximo",def."PorcentualFijo_ValorMontoMaximo",
def."PorcentualFijo_IdMonedaMinimoMaximo",def."Otros_CheckOperacionContado",def."Otros_CheckOperacionPlazo",def."Otros_CheckBaseTasa",def."Otros_ValorBaseTasa",
def."Otros_CheckInteresSimple",def."Otros_CheckInteresCompuesto",def."Otros_CheckAllIn",def."Otros_ValorAllInMontoOperacionBase",def."Otros_CheckAfectoImpuesto",
def."Otros_CheckAplicaPortafolio",def."Otros_CheckAplicaCorresponsal",def."Otros_CheckPermiteAumentarValor",def."Otros_CheckPermiteDisminuirValor",
def."PorcentualRango_IdMonedaRangoMonto",def."PorcentualRango_IdTipoRangoCobro",def."Otros_IncluyeExcluyeEmisoresCheck",def."Otros_IncluyeExcluyeInstrumentosCheck",
def."Otros_IncluyeExcluyeCorresponsalesCheck",def."Otros_IncluyeExcluyeEmisoresValor",def."Otros_IncluyeExcluyeInstrumentosValor",def."Otros_IncluyeExcluyeCorresponsalesValor",
def."PorcentualTasaPlazo_ValorCobro",def."MontoFijoPorUnidad_AllInCheckValor",def."MontoFijoPorUnidad_MontoCobro"

FROM "BSCL.Orden.BD::ORD.CorredorCorresponsal" corr
INNER JOIN "BSCL.Plataforma.BD::COM_2.DefinicionCobro" def on def."CodigoDefinicionCobro" = corr."IdCobro"
INNER JOIN "BSCL.Plataforma.BD::COM_2.TipoCobro" tip on def."IdTipoCobro" = tip."IdTipoCobro"
INNER JOIN "BSCL.Plataforma.BD::COM_2.ClaseCobro" cla on cla."IdClaseCobro" = def."IdClaseCobro"
WHERE corr."IdCorredorCorresponsal" in (:idsCorredorCorresponsal);
