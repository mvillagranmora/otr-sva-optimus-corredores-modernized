SELECT IND."IdIndicador", IND."IdTipoIndicador", IND."ValorIndicador"
FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" IND
WHERE IND."CodigoTipoIndicador" = 'PEP'
AND IND."IdPersona" = :IdPersona;