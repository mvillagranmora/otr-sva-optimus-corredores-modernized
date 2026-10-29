UPDATE "BSCL.Custodia.BD::HIS.Cartera"
SET "EstadoLiquidacionBC"= :EstadoLiquidacionBC
WHERE "FolioSistemaExterno"= :FolioSistemaExterno AND
	  "FechaRespaldo"= ( SELECT MAX("FechaRespaldo") from "BSCL.Custodia.BD::HIS.Cartera" WHERE "FolioSistemaExterno"= :FolioSistemaExterno );