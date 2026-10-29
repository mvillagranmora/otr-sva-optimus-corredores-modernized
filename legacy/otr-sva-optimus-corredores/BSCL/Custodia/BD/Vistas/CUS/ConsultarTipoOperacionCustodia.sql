SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarTipoOperacionCustodia";

CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarTipoOperacionCustodia" ( "IdTipoOperacion",
	 "Codigo",
	 "Nombre",
	 "Accion" ) AS SELECT
	 tiop."IdTipoOperacion",
	 tiop."Codigo",
	 tiop."Nombre",
	 tiop."Accion" 
FROM "BSCL.Plataforma.BD::INS.TipoOperacion" tiop 
WHERE "IdTipoOperacion" IN ('INGTIT',
	 'RETTIT',
	 'TRATIT') 
ORDER BY tiop."Nombre";

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarTipoOperacionCustodia" TO  BCS_DESARROLLADOR WITH GRANT OPTION;