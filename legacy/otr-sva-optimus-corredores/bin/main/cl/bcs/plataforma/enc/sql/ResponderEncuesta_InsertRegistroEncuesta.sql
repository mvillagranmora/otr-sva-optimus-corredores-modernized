INSERT INTO "BSCL.SocioNegocio.BD::CLI.RegistroEncuesta"
("IdEmpresa", "IdCliente", "IdRegistroEncuesta", "IdCategoriaInversionista", "AsignadoPorEncuesta","NombreEncuesta", "Puntaje", "FechaIngresoRegistro", "FechaEncuesta", "IdUsuarioResponde","IdCanal","IdPortafolio","IdPerfilProductos")
SELECT 
:IdEmpresa,:IdCliente, :IdRegistroEncuesta, :IdCategoriaInversionista, :AsignadoPorEncuesta, :NombreEncuesta,:Puntaje, CURRENT_UTCTIMESTAMP, :FechaEncuesta, :IdUsuarioResponde,:IdCanal,:IdPortafolio,COALESCE("IdPerfilProductos",'')
FROM "BSCL.Plataforma.BD::COM.PerfilComercialBase" 
WHERE "IdCategoriaInversionista" = :IdCategoriaInversionista
AND "PorDefecto" = 'S'