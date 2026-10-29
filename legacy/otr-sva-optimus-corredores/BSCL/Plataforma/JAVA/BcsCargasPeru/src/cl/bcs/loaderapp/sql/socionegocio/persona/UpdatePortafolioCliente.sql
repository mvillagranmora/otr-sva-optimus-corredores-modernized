UPDATE P
SET
	P."CodigoPortafolio" = :CodigoPortafolio,
	P."DescripcionPortafolio" = :DescripcionPortafolio,
	P."Proposito" = :CodigoProposito,
	P."Alias" = :AliasPortafolio,
	P."IdEstado" = :EstadoPortafolio,
	P."FechaBloqueo" = TO_DATE(:FechaBloqueo,'DD-MM-YYYY'),
	P."IdTipoBloqueo" = :CodigoTipoBloqueo,
	P."ComentarioBloqueo" = :ComentarioBloqueo
FROM "BSCL.Portafolio.BD::POR.Portafolio" P
WHERE P."IdCliente" = :IdCliente