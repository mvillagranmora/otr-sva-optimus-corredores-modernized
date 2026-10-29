/********************************************************** 
Proposito	: consulta de cobros para asignaciones
autor		: RU
fecha		: 01/02/2017
***********************************************************/
SELECT "BSCL.Orden.BD.Secuencias::CobroAsignacion".NEXTVAL as "IdCobroAsignacion",
"CodigoDefinicionCobro" AS "IdCobro",def."IdClaseCobro" AS "CodigoCobroClasificacion",cla."IdClaseCobroBase" AS "CodigoCobroClasificacionBase","IdTipoCobro" as "FormaCobro",
"PorcentualFijo_ValorCobro" AS "ValorCobroAplicado",
--CAMPOS PERU
"CheckCobroMonedaOperacion" AS "_CheckCobroMonedaOperacion","IdMonedaCobroOtraMoneda" AS "_IdMonedaCobroOtraMoneda",
"MontoFijo_MontoCobro" AS "_MontoFijo_MontoCobro",

"MontoFijo_CheckMontoOperacionMenor" 	AS "_MontoFijo_CheckMontoOperacionMenor",
"MontoFijo_ValorMontoOperacionMenor" 	AS "_MontoFijo_ValorMontoOperacionMenor",
"MontoFijo_CheckSiNo" 					AS "_MontoFijo_CheckSiNo",

"MontoFijo_ValorMontoSiNo" 				AS "_MontoFijo_ValorMontoSiNo",

"MontoFijoPorUnidad_MontoCobro" 		AS "_MontoFijoPorUnidad_MontoCobro",
"MontoFijoPorUnidad_AllInCheckValor" 	AS "_MontoFijoPorUnidad_AllInCheckValor",

"PorcentualFijo_ValorCobro" 			AS "_PorcentualFijo_ValorCobro",
"PorcentualFijo_CheckSobreMonto" 		AS "_PorcentualFijo_CheckSobreMonto",
"PorcentualFijo_CheckSobreCantidad" 	AS "_PorcentualFijo_CheckSobreCantidad",
"PorcentualFijo_CheckMayorEntreDos" 	AS "_PorcentualFijo_CheckMayorEntreDos",
"PorcentualFijo_CheckMontoMinimo" 		AS "_PorcentualFijo_CheckMontoMinimo",
"PorcentualFijo_ValorMontoMinimo" 		AS "_PorcentualFijo_ValorMontoMinimo",
"PorcentualFijo_CheckMontoMaximo" 		AS "_PorcentualFijo_CheckMontoMaximo",
"PorcentualFijo_ValorMontoMaximo" 		AS "_PorcentualFijo_ValorMontoMaximo",
"PorcentualFijo_IdMonedaMinimoMaximo" 	AS "_PorcentualFijo_IdMonedaMinimoMaximo",
"PorcentualTasaPlazo_ValorCobro" 		AS "_PorcentualTasaPlazo_ValorCobro",
"Otros_CheckAfectoImpuesto" 			AS "_Otros_CheckAfectoImpuesto",
"Otros_CheckPermiteAumentarValor" 		AS "_Otros_CheckPermiteAumentarValor",
"Otros_CheckPermiteDisminuirValor" 		AS "_Otros_CheckPermiteDisminuirValor",
"Otros_CheckOperacionContado"			AS "_Otros_CheckOperacionContado",
"Otros_CheckOperacionPlazo" 			AS "_Otros_CheckOperacionPlazo",
"Otros_CheckAllIn"                      AS "_Otros_CheckAllIn"


FROM "BSCL.Plataforma.BD::COM_2.DefinicionCobro" def
INNER JOIN "BSCL.Plataforma.BD::COM_2.ClaseCobro" cla on cla."IdClaseCobro" = def."IdClaseCobro"
WHERE def."CodigoDefinicionCobro" in (:idsCodigoDefinicionCobro)



