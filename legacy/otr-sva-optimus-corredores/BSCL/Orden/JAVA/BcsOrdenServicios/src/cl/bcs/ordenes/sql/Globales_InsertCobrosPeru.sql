INSERT INTO "BSCL.Orden.BD::ORD.CobroOrden"(
"IdCobroOrden","IdGrupoCobro","IdOrden","IdCobro","ValorCobroPerfil","ValorCobroAplicado",
"MontoCobroCalculado","MontoCobroCalculadoLocal","MontoCobroCalculadoLiquidacion","FormaCobro", 
"EsComprobante","FechaIngresoCobro", "MontoMinimo", "CodigoCobroClasificacion", "CodigoCobroClasificacionBase","IdMoneda","IdOrigenCobro","ValorCobroFinal","IdAsociacion","PermiteCambiarValor",

--CAMPOS PERU
"_CheckCobroMonedaOperacion","_IdMonedaCobroOtraMoneda","_MontoFijo_MontoCobro","_MontoFijo_CheckMontoOperacionMenor","_MontoFijo_ValorMontoOperacionMenor","_MontoFijo_CheckSiNo",
"_MontoFijo_ValorMontoSiNo","_PorcentualFijo_ValorCobro","_PorcentualFijo_CheckSobreMonto","_PorcentualFijo_CheckSobreCantidad","_PorcentualFijo_CheckMayorEntreDos","_PorcentualFijo_CheckMenorEntreDos"
,"_PorcentualFijo_CheckMontoMinimo",
"_PorcentualFijo_ValorMontoMinimo","_PorcentualFijo_CheckMontoMaximo","_PorcentualFijo_ValorMontoMaximo","_PorcentualFijo_IdMonedaMinimoMaximo","_PorcentualTasaPlazo_ValorCobro",
"_Otros_CheckAfectoImpuesto","_Otros_CheckPermiteAumentarValor","_Otros_CheckPermiteDisminuirValor","_Otros_CheckOperacionContado","_Otros_CheckOperacionPlazo","_Otros_CheckAllIn",
"_Otros_CheckInteresCompuesto","_Otros_CheckInteresSimple","_PorcentualRango_IdMonedaRangoMonto","_PorcentualRango_IdTipoRangoCobro",
"_MontoFijoPorUnidad_MontoCobro","_MontoFijoPorUnidad_AllInCheckValor"
		
)
VALUES(
:IdCobroOrden,:IdGrupoCobro,:IdOrden,:IdCobro,:ValorCobroPerfil,:ValorCobroAplicado,
:MontoCobroCalculado,:MontoCobroCalculadoLocal,:MontoCobroCalculadoLiquidacion,:FormaCobro,
:EsComprobante,CURRENT_UTCTIMESTAMP,:MontoMinimo,:CodigoCobroClasificacion,:CodigoCobroClasificacionBase,:IdMoneda,:IdOrigenCobro,:ValorCobroFinal,COALESCE(:IdAsociacion,0),:PermiteCambiarValor,

:CheckCobroMonedaOperacion,:IdMonedaCobroOtraMoneda,:MontoFijo_MontoCobro,:MontoFijo_CheckMontoOperacionMenor,:MontoFijo_ValorMontoOperacionMenor,:MontoFijo_CheckSiNo,
:MontoFijo_ValorMontoSiNo,:PorcentualFijo_ValorCobro,:PorcentualFijo_CheckSobreMonto,:PorcentualFijo_CheckSobreCantidad,:PorcentualFijo_CheckMayorEntreDos,:PorcentualFijo_CheckMenorEntreDos,
:PorcentualFijo_CheckMontoMinimo,
:PorcentualFijo_ValorMontoMinimo,:PorcentualFijo_CheckMontoMaximo,:PorcentualFijo_ValorMontoMaximo,:PorcentualFijo_IdMonedaMinimoMaximo,:PorcentualTasaPlazo_ValorCobro,
:Otros_CheckAfectoImpuesto,:Otros_CheckPermiteAumentarValor,:Otros_CheckPermiteDisminuirValor,:Otros_CheckOperacionContado,:Otros_CheckOperacionPlazo,:Otros_CheckAllIn,
:Otros_CheckInteresCompuesto,:Otros_CheckInteresSimple,:PorcentualRango_IdMonedaRangoMonto,:PorcentualRango_IdTipoRangoCobro,
:MontoFijoPorUnidad_MontoCobro,:MontoFijoPorUnidad_AllInCheckValor
		
);



