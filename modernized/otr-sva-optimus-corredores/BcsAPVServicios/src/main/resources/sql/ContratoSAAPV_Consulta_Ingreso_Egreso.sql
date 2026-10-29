SELECT TRA."IdContratoSAAPV" ,TRA."IdTraspaso" , TRA."IdEstado",CON."IdGlosa" AS "IdMandatoPAC"
FROM "BSCL.APV.BD::TRA.Traspaso" AS TRA
INNER JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" AS CON ON CON."IdContratoSAAPV" = TRA."IdContratoSAAPV"
WHERE "FolioContratoSAAPV" = :FolioContratoSAAPV;