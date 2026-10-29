SELECT PORT."IdCliente",
	   PORT."IdPortafolio"
FROM "BSCL.SocioNegocio.BD::CLI.GrupoClientes" GC
INNER JOIN	"BSCL.Portafolio.BD::POR.Portafolio" PORT ON PORT."IdPortafolio" = GC."IdPortafolio"
WHERE GC."IdGrupo" =:p_IdGrupo;