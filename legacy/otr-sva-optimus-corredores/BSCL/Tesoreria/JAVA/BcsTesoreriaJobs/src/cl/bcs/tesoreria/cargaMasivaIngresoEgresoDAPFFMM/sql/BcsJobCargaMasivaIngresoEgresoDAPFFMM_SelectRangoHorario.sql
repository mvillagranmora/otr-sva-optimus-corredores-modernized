SELECT 
 	TO_NVARCHAR("HoraInicio") AS "HoraInicio"
	,TO_NVARCHAR("HoraFin") AS "HoraFin"
FROM "BSCL.Plataforma.BD::BAT.RangoHorario"
WHERE "TipoProposito" = :TipoProposito;