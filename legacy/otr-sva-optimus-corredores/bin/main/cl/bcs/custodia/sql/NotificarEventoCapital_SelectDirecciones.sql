/**************************************************************
Proposito	: Script consulta direcciones para notificaciones
autor		: RU
fecha		: 01/02/2017
***********************************************************/

SELECT
	CIPO."IdPortafolio",
	CASE 
		WHEN COALESCE(CIPO."IdDireccionNotificacion", '')='' OR CIPO."IdDireccionNotificacion"='N' OR CIPO."IdDireccionNotificacion"='S' OR CIPO."IdDireccionNotificacion"='false' THEN  
			CASE CIP."ValorIndicador" 
				WHEN '' THEN 0
				WHEN 'S' THEN 0
				WHEN 'N' THEN 0
				ELSE CIP."ValorIndicador" 
			END 
		ELSE CIPO."IdDireccionNotificacion" 
	END AS "IdDireccion",
	'TO' AS "TipoDestinatario"
FROM "BSCL.Plataforma.BD::EVT.TipoEvento" TE 
INNER JOIN DUMMY DU ON TE."CodigoTipoEvento" = 'CARTAVAR_CAP'
INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle" CED ON CED."CodigoTipoEvento" = TE."CodigoTipoEvento"
INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" CE ON CED."IdConfiguracionEvento" =   CE."IdConfiguracionEvento" 
	AND CE."FormaDespacho" IN('T','C')
LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarIndicadoresPortafolioOrdenes" CIPO ON 
	CIPO."IdPortafolio" IN  ( :ListaIdPortafolio1 )
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" CIP ON 
	CIP."IdPortafolio"  =  CIPO."IdPortafolio"
	AND CIP."IdUso" = 'PRE' 
	AND CIP."CodigoTipoIndicador" = 'NOT_EMAIL'
WHERE 
	CED."IdEstado" ='ACT';