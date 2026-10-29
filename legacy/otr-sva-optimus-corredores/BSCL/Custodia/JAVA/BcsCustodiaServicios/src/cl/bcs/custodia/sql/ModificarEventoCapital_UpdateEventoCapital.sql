UPDATE "BSCL.Custodia.BD::CUS.EventoCapital"
SET
--    "IdTipoEventoCapital"           = :p_IdTipoEventoCapital,
    "FechaLimite"                   = :p_FechaLimite,
    "FechaPago"                     = :p_FechaPago,
    "NumeroDividendo"               = :p_NumeroDividendo,
    "Descripcion"                   = :p_Descripcion,
    "EsInscrito"                    = :p_EsInscrito,
    "Factor"                        = :p_Factor,
    "Impuesto"                      = :p_Impuesto,
    "FactorNuevo"                   = :p_FactorNuevo,
    "IdMoneda"                      = :p_IdMoneda,
    "IdMonedaLiquidacion"           = :p_IdMonedaLiquidacion,
    "Paridad"                       = :p_Paridad,
    "FechaHoraUltimaActualizacion"  = :p_FechaHoraUltimaActualizacion,
    "IdEstado"                      = :p_IdEstado,
    "IdBoveda"                      = :p_IdBoveda,
    "ISFUT"                         = :p_EsISFUT,
    "TasaEfectiva"                  = :p_TasaEfectiva,
    "CantidadCuotas"				= :p_Cuotas,
    "SecuenciaRelacionada"			= :p_SecuenciaRelacionada,
    "SuscripcionComplementaria" 	= :p_SuscripcionComplementaria,
    "ImpuestoDivExtranjero"			= :p_ImpuestoDivExtranjero,
    "GastoTransferencia"			= :p_GastoTransferencia
WHERE
    "IdEventoCapital" = :p_IdEventoCapital;