SELECT 
	"IdTipoBloqueo" , 
	"NombreTipoBloqueo", 
	"IdEmpresa", 
	"IdAccion", 
	"IdTipoEntidad", 
	"IdEstado" 
FROM "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"
WHERE 
	"IdEmpresa" = :IdEmpresa AND
	"IdTipoEntidad" IN ('CLIENTE','PORTAFOLIO') AND 
	"IdEstado" = 'ACT' AND 
	"IdTipoBloqueo" NOT IN ('AAUTOM','BAUTOM')