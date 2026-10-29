SELECT top 1 IFNULL("ValorIndicador",'N') AS p_out_TieneContrato
FROM "BSCL.Plataforma.BD::IND.TipoIndicador" TPO 
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR ON ATR."IdTipoIndicador" = TPO."IdTipoIndicador"
WHERE 
	"CodigoTipoIndicador" = CASE WHEN :p_IdMercado = 'RF' THEN 'ORDOCUSTRF' ELSE 'ORDOCUSTRV' END  
	AND ATR."IdTipoEntidad" = 'PORTAFOLIO' 
	AND ATR."IdEntidad" = TO_VARCHAR(p_IdPortafolio);	         