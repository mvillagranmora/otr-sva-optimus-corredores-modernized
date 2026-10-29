SELECT	PORT."IdPortafolio"
		,PORT."IdMatrizAgrupadorAPV"
		,PORT."CodigoPortafolio"
		,PORT."CodigoPortafolio" ||' '|| PORT."Proposito" as "CodigoPropositoPortafolio"
		,COALESCE (PER."NombreCompleto",'') AS "NombreEjecutivo"
FROM  	"BSCL.Portafolio.BD::POR.Portafolio" PORT
		LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" EMP ON EMP."IdEmpleado" = PORT."IdEmpleadoEjecutivo"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON PER."IdPersona" = EMP."IdPersona"
WHERE 	"IdPortafolio" IN (:IdPortafolio)

