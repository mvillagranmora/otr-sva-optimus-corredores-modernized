SELECT 
	"IdDireccionFisica" 
FROM
"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica"
WHERE "IdPersona" = :IdPersona
AND "IdEstado" = 'ACT';