SELECT GDD."IdGrupoDireccionDetalle"
     , GDD."IdDireccionElectronica"
     , GDD."IdGrupoDireccion"
     , DE."Direccion" AS "DireccionElectronica"
FROM "BSCL.SocioNegocio.BD::PER.GrupoDireccionDetalle" AS GDD
     JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS DE ON DE."IdDireccionElectronica" = GDD."IdDireccionElectronica"
WHERE GDD."IdEstado" = 'ACT'
  AND GDD."Borrado" = 'N'
  AND GDD."IdGrupoDireccion" IN (:GrupoDireccionIds)
  AND GDD."IdDireccionElectronica" IN (:DireccionElectronicaIds);