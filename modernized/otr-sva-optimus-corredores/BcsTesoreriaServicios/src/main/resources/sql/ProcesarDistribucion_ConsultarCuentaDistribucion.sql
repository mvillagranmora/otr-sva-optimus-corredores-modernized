SELECT 
	TRA."DestinoCtaBco", 
	COALESCE(BCO."IdPersona",0) AS "IdPersonaBanco"
FROM "BSCL.APV.BD::APV.OrigenDestinoTransicion" AS TRA
LEFT JOIN "BSCL.Tesoreria.BD::TES.CuentaBanco" AS CBCO ON (CBCO."IdCuentaBanco" = TRA."DestinoCtaBco")
LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" AS BCO ON (BCO."IdBanco" = CBCO."IdBanco")
WHERE TRA."Operacion"='DIS' 
AND TRA."IdEmpresa"=:IdEmpresa 
AND TRA."IdEstado"='ACT';

