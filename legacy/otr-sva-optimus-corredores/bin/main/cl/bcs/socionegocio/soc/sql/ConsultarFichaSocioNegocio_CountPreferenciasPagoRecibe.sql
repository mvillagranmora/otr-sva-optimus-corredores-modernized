SELECT PORP."IdPortafolio"                       AS "IdEntidad"
     , COUNT(PORP."IdPreferenciaPagoPortafolio") AS "Cantidad"
FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" PORP
     LEFT JOIN "BSCL.Plataforma.BD::PAG.PreferenciaPago" PAGP ON PORP."IdPreferenciaPago" = PAGP."IdPreferenciaPago"
     LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TIP ON PAGP."IdTipoOperacion" = TIP."IdTipoOperacion"
WHERE TIP."RecibePagaPortafolio" = 'R'
  AND PORP."IdPortafolio" IN (:IdsEntidades)
GROUP BY PORP."IdPortafolio";