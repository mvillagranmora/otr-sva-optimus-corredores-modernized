SELECT POR."IdPortafolio",POR."IdPreferenciaPagoPortafolio",PAG."IdMoneda",PAG."IdTipoOperacion"
FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" POR
INNER JOIN "BSCL.Plataforma.BD::PAG.PreferenciaPago" PAG ON POR."IdPreferenciaPago" = PAG."IdPreferenciaPago"
WHERE "IdPortafolio" IN (:idsPortafolio) AND POR."PorDefecto" = 'S'