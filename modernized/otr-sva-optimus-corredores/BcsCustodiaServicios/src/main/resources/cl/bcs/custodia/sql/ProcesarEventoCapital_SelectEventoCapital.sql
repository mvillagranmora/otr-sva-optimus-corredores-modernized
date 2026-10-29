SELECT	"IdEventoCapital"                        ,
		"IdEmpresa"                              ,
		"IdTipoEventoCapital"                    ,
		"Secuencia"                              ,
		"Descripcion"                            ,
		"EsInscrito"                             ,
		"IdClasificacionTipoInstrumento"         ,
		"IdTipoInstrumento"                      ,
		"IdInstrumento"                          ,
		"Nemo"                                   ,
		"Factor"                                 ,
		"IdMoneda"                               ,
		"IdInstrumentoNuevo"                     ,
		"NemoNuevo"                              ,
		"FechaActualizacion"                     ,
		"FechaLimite"                            ,
		"FechaPago"                              ,
		"NumeroDividendo"                        ,
		"IdTipoDividendo"                        ,
		"PrecioAnterior"                         ,
		"FechaHoraIngresoRegistro"               ,
		"FechaHoraUltimaActualizacion"           ,
		"TipoIngreso"                            ,
		"IdUsuario"                              ,
		"IdEstado"                               ,
		"CuentaDepositanteSorteoLetras"          ,
		"MonedaSorteoLetras"                     ,
		"GlosaMonedaSorteoLetras"                ,
		"EmisorPagadorSorteoLetras"              ,
		"EmisorSorteoLetras"                     ,
		"InstrumentoSorteoLetras"                ,
		"GlosaInstrumentoSorteoLetras"           ,
		"BloqueoSorteoLetras"                    ,
		"SorteadoSorteoLetras"                   ,
		"APagarSorteoLetras"                     ,
		"APagarPesosSorteoLetras"                ,
		"Origen"                                 ,
		"IdEmisor"                               ,
		"IdMonedaLiquidacion"                    ,
		"Paridad"                                ,
		"IdEmisorNuevo"                          ,
		"EstadoNotificacion"                     ,
		"IdInstrumentoAnterior"                  ,
		"Indice"                                 ,
		"Impuesto"                               ,
		"CantidadCuotas"                         ,
		"Monto"					                 ,
		"SecuenciaRelacionada"			         ,
		"SuscripcionComplementaria"              ,
		"ImpuestoDivExtranjero"                  ,
		"GastoTransferencia"		       	     ,
		COALESCE("Valor", 0) AS "Valor"          ,
		COALESCE("FactorNuevo", 0) AS "FactorNuevo"
FROM "BSCL.Custodia.BD::CUS.EventoCapital"
WHERE  "IdEmpresa" = :p_IdEmpresa AND "FechaLimite" BETWEEN (:p_FechaLimiteMin) AND (:p_FechaLimiteMax) AND "IdEstado" = 'VIG'/*  */
/*
    AND (      "IdEventoCapital"       IN (:p_IdEventoCapital)
            OR "IdTipoEventoCapital"   IN (:p_TipoEventoCapital)
        )
   
   
SELECT * FROM "BSCL.Custodia.BD::CUS.EventoCapital"
WHERE  "IdEmpresa" = 1
    AND "FechaLimite"           IN ('2018-07-31')

*/
--SELECT * FROM "BSCL.Custodia.BD::CUS.TipoEventoCapital" WHERE  "IdEstado" = 'ACT'