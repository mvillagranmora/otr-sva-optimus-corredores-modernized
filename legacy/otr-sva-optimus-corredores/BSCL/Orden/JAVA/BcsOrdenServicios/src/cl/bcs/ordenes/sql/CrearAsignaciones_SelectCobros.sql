/********************************************************** 
Proposito	: consulta de cobros para asignaciones
autor		: RU
fecha		: 01/02/2017
***********************************************************/
SELECT "IdOrden","IdCobroOrden",
"IdCobro","ValorCobroPerfil","ValorCobroAplicado","MontoMinimo","IdMoneda","FormaCobro","CodigoCobroClasificacion","CodigoCobroClasificacionBase","IdGrupoCobro","ValorCobroFinal","PermiteCambiarValor",

--CAMPOS PERU
"_CheckCobroMonedaOperacion","_IdMonedaCobroOtraMoneda","_MontoFijo_MontoCobro","_MontoFijo_CheckMontoOperacionMenor","_MontoFijo_ValorMontoOperacionMenor","_MontoFijo_CheckSiNo",
"_MontoFijo_ValorMontoSiNo","_PorcentualFijo_ValorCobro","_PorcentualFijo_CheckSobreMonto","_PorcentualFijo_CheckSobreCantidad","_PorcentualFijo_CheckMayorEntreDos","_PorcentualFijo_CheckMontoMinimo",
"_PorcentualFijo_ValorMontoMinimo","_PorcentualFijo_CheckMontoMaximo","_PorcentualFijo_ValorMontoMaximo","_PorcentualFijo_IdMonedaMinimoMaximo","_PorcentualTasaPlazo_ValorCobro",
"_Otros_CheckAfectoImpuesto","_Otros_CheckPermiteAumentarValor","_Otros_CheckPermiteDisminuirValor","_Otros_CheckOperacionContado","_Otros_CheckOperacionPlazo","_Otros_CheckAllIn",

"_Otros_CheckInteresCompuesto","_Otros_CheckInteresSimple","_PorcentualRango_IdMonedaRangoMonto","_PorcentualRango_IdTipoRangoCobro",
"_MontoFijoPorUnidad_MontoCobro","_MontoFijoPorUnidad_AllInCheckValor"
		

FROM "BSCL.Orden.BD::ORD.CobroOrden" cob
WHERE "IdGrupoCobro" in (:idsGrupoCobro)-- and cob."IdOrigenCobro" = 'PORTAFOLIO'
