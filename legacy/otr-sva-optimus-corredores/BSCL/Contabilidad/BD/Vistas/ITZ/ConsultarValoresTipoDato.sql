SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarValoresTipoDato";
GO
CREATE VIEW "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarValoresTipoDato" ( "IdTipoDatoEvento",
	 "Valor","Descripcion" ) AS SELECT
	 CTB_itzEventoTipoDatoValor."IdTipoDatoEvento" AS "IdTipoDatoEvento",
	 CASE WHEN CTB_itzEventoTipoDato."TipoDato" = 'V' 
THEN CTB_itzEventoTipoDatoValor."ValorVarchar" WHEN CTB_itzEventoTipoDato."TipoDato" = 'D' 
THEN CAST(CTB_itzEventoTipoDatoValor."ValorDecimal" AS NVARCHAR(60)) 
END AS "Valor",
CTB_itzEventoTipoDatoValor."Descripcion" 
FROM "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoValor" AS CTB_itzEventoTipoDatoValor 
LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" AS CTB_itzEventoTipoDato ON CTB_itzEventoTipoDato."IdTipoDatoEvento" = CTB_itzEventoTipoDatoValor."IdTipoDatoEvento" 
AND CTB_itzEventoTipoDato."Estado" = 'V' 
AND CTB_itzEventoTipoDatoValor."Estado" = 'V' 
WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarValoresTipoDato" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO

