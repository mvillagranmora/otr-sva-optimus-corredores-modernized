SELECT COUNT(POR."IdPortafolio") AS "Contador"
FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
WHERE POR."IdPortafolio" IN (:IdPortafolios)
  AND POR."IdEstadoAutorizacion" = :IdEstadoAutorizacion
