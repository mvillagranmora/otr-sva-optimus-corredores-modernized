/* 

Consultar para obtener datos de configuracion cuenta inversion

*/
SELECT "IdEmpresa","IdClaseCobro","IdTipoCobro","CodigoDefinicionCobro","IdEstado","Descripcion","CheckCobroMonedaOperacion","IdMonedaCobroOtraMoneda"
    ,"MontoFijo_MontoCobro","MontoFijo_CheckMontoOperacionMenor","MontoFijo_ValorMontoOperacionMenor","MontoFijo_CheckSiNo","MontoFijo_ValorMontoSiNo"
    ,"PorcentualFijo_ValorCobro","PorcentualFijo_CheckSobreMonto","PorcentualFijo_CheckSobreCantidad","PorcentualFijo_CheckMayorEntreDos","PorcentualFijo_CheckMenorEntreDos"
    ,"PorcentualFijo_CheckMontoMinimo","PorcentualFijo_ValorMontoMinimo","PorcentualFijo_CheckMontoMaximo","PorcentualFijo_ValorMontoMaximo","PorcentualFijo_IdMonedaMinimoMaximo"
    ,"Otros_CheckOperacionContado","Otros_CheckOperacionPlazo","Otros_CheckBaseTasa","Otros_ValorBaseTasa","Otros_CheckInteresSimple","Otros_CheckInteresCompuesto"
    ,"Otros_CheckAllIn","Otros_ValorAllInMontoOperacionBase","Otros_CheckAfectoImpuesto","Otros_CheckAplicaPortafolio","Otros_CheckAplicaCorresponsal"
    ,"Otros_CheckPermiteAumentarValor","Otros_CheckPermiteDisminuirValor","PorcentualRango_IdMonedaRangoMonto","PorcentualRango_IdTipoRangoCobro","Otros_IncluyeExcluyeEmisoresCheck"
    ,"Otros_IncluyeExcluyeInstrumentosCheck","Otros_IncluyeExcluyeTiposDeClienteCheck","Otros_IncluyeExcluyeClientesCheck","Otros_IncluyeExcluyeCorresponsalesCheck"
    ,"Otros_IncluyeExcluyeEmisoresValor","Otros_IncluyeExcluyeInstrumentosValor","Otros_IncluyeExcluyeTiposDeClienteValor","Otros_IncluyeExcluyeClientesValor"
    ,"Otros_IncluyeExcluyeCorresponsalesValor","IdTipoProducto","PorcentualTasaPlazo_ValorCobro","MontoFijoPorUnidad_MontoCobro","MontoFijoPorUnidad_AllInCheckValor"
FROM "BSCL.Plataforma.BD::COM_2.DefinicionCobro" WHERE "IdEmpresa" = :IdEmpresa AND (("CodigoDefinicionCobro" IN (:CodigoDefinicionCobro) AND "CodigoDefinicionCobroBase" = '') OR ("CodigoDefinicionCobro" IN (:CodigoDefinicionCobro) AND "CodigoDefinicionCobroBase" != '' AND "IdEstado" = 'ACT'));
