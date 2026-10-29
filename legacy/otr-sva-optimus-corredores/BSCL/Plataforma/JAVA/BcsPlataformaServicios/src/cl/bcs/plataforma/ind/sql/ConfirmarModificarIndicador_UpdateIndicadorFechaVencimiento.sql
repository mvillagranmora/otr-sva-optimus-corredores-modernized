UPDATE "BSCL.Plataforma.BD::IND.Indicador"
SET 
"FechaVencimiento" = :FechaVencimiento
,"FechaModificacion" = CURRENT_UTCTIMESTAMP
,"IdEstadoAutorizacion" = :IdEstadoAutorizacion
WHERE "IdIndicador" = :IdIndicador;