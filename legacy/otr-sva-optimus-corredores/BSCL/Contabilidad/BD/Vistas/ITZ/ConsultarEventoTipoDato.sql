SET SCHEMA BSCL;
GO
DROP VIEW "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarEventoTipoDato";
GO
CREATE VIEW "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarEventoTipoDato" 
(  
  "IdTipoDatoEvento"
  ,"NombreTipoDatoEvento"
  ,"TipoDato"
  ,"ValorLibre"
  ,"LargoVarchar"
  ,"Enteros"
  ,"Decimales"
  ,"FormatoEdicion"
  ,"DatosPreDefinidos"
  ,"Estado"
  ,"EsConceptoNormativo"
) AS 
SELECT
  "IdTipoDatoEvento"
  ,"IdTipoDatoEvento" AS "NombreTipoDatoEvento"
  ,"TipoDato"
  ,"ValorLibre"
  ,"LargoVarchar"
  ,"Enteros"
  ,"Decimales"
  ,"FormatoEdicion"
  ,"DatosPreDefinidos"
  ,"Estado"
  ,"EsConceptoNormativo"
FROM "BSCL.Contabilidad.BD::ITZ.EventoTipoDato"
WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarEventoTipoDato" TO  BCS_DESARROLLADOR WITH GRANT OPTION;		  
GO
