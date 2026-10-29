SELECT C."IdContraparte" 
FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS A
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS B
		ON B."IdSocioDeNegocio" = A."IdSocioNegocio"
	INNER JOIN "BSCL.Custodia.BD.Vistas::GTA.ConsultarEntidadDestinoGarantia" AS C
	ON C."IdPersona" = B."IdPersona" 
WHERE "IdEventoCapital" = :IdEventoCapital
  AND "TipoSocioNegocio" = 'CNT'
  AND "TipoCartera" = 'GAR'
  AND ("IdEstado" = 'PRO' OR "IdEstado" = 'NOT')