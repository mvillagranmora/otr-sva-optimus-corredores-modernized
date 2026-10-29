UPDATE "BSCL.Plataforma.BD::IND.Indicador"
SET 
"ValorIndicador" = :ValorIndicador
,"FechaModificacion" = CURRENT_UTCTIMESTAMP
,"IdEstadoAutorizacion" = :IdEstadoAutorizacion
WHERE "IdIndicador" = :IdIndicador;