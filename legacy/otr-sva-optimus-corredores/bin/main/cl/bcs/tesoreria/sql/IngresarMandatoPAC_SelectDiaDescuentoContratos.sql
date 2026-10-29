SELECT DISTINCT "FolioCSC" AS "FolioContrato","DiaDescuento",'CSC' AS "TipoContrato", CONCAT(CONCAT("FolioCSC",'_'), 'CSC') AS "Hash" 
FROM "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas" WHERE "FolioCSC" IN (:FolioCSC) AND "FolioCSC" <> '0'
UNION ALL
SELECT DISTINCT "FolioContratoSAAPV" AS "FolioContrato","DiaDescuento",'SAAPV' AS "TipoContrato", CONCAT(CONCAT("FolioContratoSAAPV",'_'),'SAAPV') AS "Hash" 
FROM "BSCL.APV.BD::SCS.ContratoSAAPV" WHERE "FolioContratoSAAPV" IN (:FolioContratoSAAPV) AND "FolioContratoSAAPV" <> '0';