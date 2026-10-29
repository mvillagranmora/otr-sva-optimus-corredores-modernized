SELECT  "IdInstrumento",
--		"TipoTransito",
		"TipoCustodia",
		"IndicadorOD",
		"IdSocioNegocio",					
		"TipoSocioNegocio",					
		"IdPortafolio",					
		"IdTipoDocumentoIdentidad",			
		"DocumentoIdentidad",				
		"NombreCliente",						
		"IdEstado" 
FROM (
		SELECT 	Car."IdInstrumento"													AS "IdInstrumento",
--				'TransitoNoOD'														AS "TipoTransito",
--				ConAsi."IdFormaCustodia"											AS "TipoCustodia",
				'CUS'																AS "TipoCustodia",
				CASE Tran."IndicadorOrdenDirecta"
					WHEN ''	THEN 'N'
					ELSE 'S'
				END																	AS "IndicadorOD",
				SocNeg."IdSocioDeNegocio"											AS "IdSocioNegocio",
--				CAST(0 AS BigInt)													AS "IdSocioNegocio",					
				'CNT'																AS "TipoSocioNegocio",					
				CAST(0 AS BigInt)													AS "IdPortafolio",					
				Corre."TipoDocumentoIdentidad"										AS "IdTipoDocumentoIdentidad",			
				IFNULL(Corre."DocumentoIdentidad",'')								AS "DocumentoIdentidad",				
				IFNULL(UPPER(Corre."NombreCompleto"),'NO EXISTE CONTRAPARTE')		AS "NombreCliente",						
				CASE IFNULL(Corre."DocumentoIdentidad",'')
					WHEN '' THEN 'PEN'
					ELSE 'PRO'
				END																AS "IdEstado"
		FROM "BSCL.Custodia.BD::CUS.Cartera" Car
			INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalle" CompDet
				ON Car."IdTipoComprobante" = CompDet."IdTipoComprobante"
				AND Car."FolioComprobante" = CompDet."IdComprobante"
			INNER JOIN "BSCL.Orden.BD.Vistas::ConsultarTransaccion" Tran
				ON CompDet."IdTransaccion" = Tran."IdTransaccion"
			   AND Tran."IdCorredorLocal" <> Tran."IdCorredorContraparte"
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" Corre
				ON Tran."IdCorredorContraparte" = Corre."IdCorredora"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas.UI::ConsultarSocioDeNegocio" SocNeg
				ON Car."IdCliente" = SocNeg."IdCliente"
		WHERE   Car."IdInstrumento" = :IdInstrumento
			AND Car."IdEstado" = 'VIG'
			AND Car."FechaLiquidacion" > :FechaLimite
			AND Car."IdTipoComprobante" = 'FAE'
--			AND Tran."IndicadorOrdenDirecta" = ''
			
		UNION ALL
		
		SELECT 	Car."IdInstrumento"													AS "IdInstrumento",
--				'TransitoODNominal'												AS "TipoTransito",
				ConAsi."IdFormaCustodia"											AS "TipoCustodia",
				CASE Tran."IndicadorOrdenDirecta"
					WHEN ''	THEN 'N'
					ELSE 'S'
				END																	AS "IndicadorOD",
				SocNeg."IdSocioDeNegocio"											AS "IdSocioNegocio",
--				CAST(0 AS BigInt)													AS "IdSocioNegocio",					
				'CNT'																AS "TipoSocioNegocio",					
				CAST(0 AS BigInt)													AS "IdPortafolio",					
				DocIden."IdTipoDocumentoIdentidad"									AS "IdTipoDocumentoIdentidad",
				IFNULL(ConAsi."DocumentoIdentidad",'')								AS "DocumentoIdentidad",				
				IFNULL(UPPER(ConAsi."NombreCompleto"),'NO EXISTE CONTRAPARTE')		AS "NombreCliente",						
				CASE IFNULL(ConAsi."DocumentoIdentidad",'')
					WHEN '' THEN 'PEN'
					ELSE 'PRO'
				END																AS "IdEstado"
		FROM "BSCL.Custodia.BD::CUS.Cartera" Car
			INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalle" CompDet
				ON Car."IdTipoComprobante" = CompDet."IdTipoComprobante"
				AND Car."FolioComprobante" = CompDet."IdComprobante"
			INNER JOIN "BSCL.Orden.BD.Vistas::ConsultarTransaccion" Tran
				ON CompDet."IdTransaccion" = Tran."IdTransaccionOd"
			   AND Tran."IdCorredorLocal" = Tran."IdCorredorContraparte"
			LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarAsignacion" ConAsi
				ON Tran."IdTransaccion" = ConAsi."IdTransaccion"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarDocumentoIdentidadPersona" DocIden
				ON ConAsi."DocumentoIdentidad" = DocIden."DocumentoIdentidad"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas.UI::ConsultarSocioDeNegocio" SocNeg
				ON Car."IdCliente" = SocNeg."IdCliente"
		WHERE   Car."IdInstrumento" = :IdInstrumento
			AND Car."IdEstado" = 'VIG'
			AND Car."FechaLiquidacion" > :FechaLimite
			AND Car."IdTipoComprobante" = 'FAE'
--			AND Tran."IndicadorOrdenDirecta" = 'OD'
			AND ConAsi."IdFormaCustodia" = 'NOM'
			
		UNION ALL
		
		SELECT 	Car."IdInstrumento"													AS "IdInstrumento",
--				'TransitoODCustodia'												AS "TipoTransito",
				ConAsi."IdFormaCustodia"											AS "TipoCustodia",
				CASE Tran."IndicadorOrdenDirecta"
					WHEN ''	THEN 'N'
					ELSE 'S'
				END																	AS "IndicadorOD",
				SocNeg."IdSocioDeNegocio"											AS "IdSocioNegocio",
--				CAST(0 AS BigInt)													AS "IdSocioNegocio",					
				'CNT'																AS "TipoSocioNegocio",					
				CAST(0 AS BigInt)													AS "IdPortafolio",					
				Bove."TipoDocumentoIdentidad"										AS "IdTipoDocumentoIdentidad",
				IFNULL(Bove."DocumentoIdentidad",'')								AS "DocumentoIdentidad",				
				IFNULL(UPPER(Bove."Nombre"),'NO EXISTE CONTRAPARTE')				AS "NombreCliente",						
				CASE IFNULL(Bove."DocumentoIdentidad",'')
					WHEN '' THEN 'PEN'
					ELSE 'PRO'
				END																	AS "IdEstado"
		FROM "BSCL.Custodia.BD::CUS.Cartera" Car
			INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalle" CompDet
				ON Car."IdTipoComprobante" = CompDet."IdTipoComprobante"
				AND Car."FolioComprobante" = CompDet."IdComprobante"
			INNER JOIN "BSCL.Orden.BD.Vistas::ConsultarTransaccion" Tran
				ON CompDet."IdTransaccion" = Tran."IdTransaccionOd"
			   AND Tran."IdCorredorLocal" = Tran."IdCorredorContraparte"
			LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarAsignacion" ConAsi
				ON Tran."IdTransaccion" = ConAsi."IdTransaccion"
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBoveda" Bove
				ON Car."IdBoveda" = Bove."IdBoveda"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas.UI::ConsultarSocioDeNegocio" SocNeg
				ON Car."IdCliente" = SocNeg."IdCliente"
		WHERE   Car."IdInstrumento" = :IdInstrumento
			AND Car."IdEstado" = 'VIG'
			AND Car."FechaLiquidacion" > :FechaLimite
			AND Car."IdTipoComprobante" = 'FAE'
--			AND Tran."IndicadorOrdenDirecta" = 'OD'
			AND ConAsi."IdFormaCustodia" = 'CUS'
)
GROUP BY 	"IdInstrumento",
--			"TipoTransito",
			"TipoCustodia",
			"IndicadorOD",
			"IdSocioNegocio",					
			"TipoSocioNegocio",					
			"IdPortafolio",					
			"IdTipoDocumentoIdentidad",			
			"DocumentoIdentidad",				
			"NombreCliente",						
			"IdEstado"