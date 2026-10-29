-- Actualiza los campos de la relacion persona
UPDATE  REL 
SET     REL."FechaInicioRelacion" 		= COALESCE(:FechaInicioRelacion, REL."FechaInicioRelacion"),
		REL."FechaFinRelacion" 			= COALESCE(:FechaFinRelacion, REL."FechaFinRelacion"),
		REL."IdEstado" 					= COALESCE(:IdEstado, REL."IdEstado"),
		REL."IdCiudad" 					= COALESCE(:IdCiudad, REL."IdCiudad"),
		REL."Ciudad" 					= COALESCE(:Ciudad, REL."Ciudad"),
		REL."PorcentajeParticipacion" 	= COALESCE(:PorcentajeParticipacion, REL."PorcentajeParticipacion"),
		REL."RetiraDocumentos" 			= COALESCE(:RetiraDocumentos, REL."RetiraDocumentos"),
		REL."FinalidadRelacion" 		= COALESCE(:FinalidadRelacion, REL."FinalidadRelacion"),
		REL."CondicionesRelacion" 		= COALESCE(:CondicionesRelacion, REL."CondicionesRelacion"),
		REL."Calle" 					= COALESCE(:Calle, REL."Calle"),
		REL."Numero" 					= COALESCE(:Numero, REL."Numero"),
		REL."Email" 					= COALESCE(:Email, REL."Email"),
		REL."Telefono" 					= COALESCE(:Telefono, REL."Telefono"),
		REL."Complemento" 				= COALESCE(:Complemento, REL."Complemento"),
		REL."FechaModificacion" 		= CURRENT_UTCTIMESTAMP
FROM	"BSCL.SocioNegocio.BD::REL.RelacionPersona" REL
WHERE   "IdRelacionPersona" 			= :IdRelacionPersona;

