UPDATE "BSCL.APV.BD::TRA.Traspaso" 
SET "IdEstado" = :IdEstado,
	"CierreSolicitud" = :CierreSolicitud
WHERE "IdEmpresa" = :IdEmpresa 
AND "IdTraspaso" = :IdTraspaso
AND "IdContratoSAAPV" = :IdContratoSAAPV;
