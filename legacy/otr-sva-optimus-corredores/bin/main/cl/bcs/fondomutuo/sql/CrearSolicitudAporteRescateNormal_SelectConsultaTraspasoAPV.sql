SELECT COUNT(1) AS "TraspasoAPV", "IdTraspaso" FROM "BSCL.APV.BD::TRA.Traspaso" WHERE "IdTraspaso" IN (:FolioContrato)
GROUP BY "IdTraspaso";