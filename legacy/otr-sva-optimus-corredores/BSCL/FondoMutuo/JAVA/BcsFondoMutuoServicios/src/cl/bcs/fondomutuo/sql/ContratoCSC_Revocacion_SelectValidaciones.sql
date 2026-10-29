SELECT top 1 "IdFolioCSC"  FROM "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas"
WHERE "FolioCSC" in (:FolioCSC) AND "Canal" <> 'BIZNET' AND "Estado" = 'ING' AND "TipoPago" = 'PAC'

