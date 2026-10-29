SELECT	sby."IdInstrumento"												AS "IdInstrumento",
		CAST(0 AS BigInt)												AS "IdSocioNegocio",					
		'CNT'															AS "TipoSocioNegocio",					
		CAST(0 AS BigInt)												AS "IdPortafolio",						
		'R'																AS "IdTipoDocumentoIdentidad",			
		IFNULL(CO."DocumentoIdentidad",'')								AS "DocumentoIdentidad",				
		IFNULL(UPPER(CO."NombreCompleto"),'NO EXISTE CONTRAPARTE')		AS "NombreCliente",						
		CASE IFNULL(CO."DocumentoIdentidad",'')
			WHEN '' THEN 'PEN'
			ELSE 'PRO'
		END																AS "IdEstado"							
FROM 	"BSCL.Custodia.BD::CUS.Cartera" 													AS EMP			
INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" 				AS ultSub 
	on (EMP."FolioCartera" = ultSub."FolioCarteraRelacionada")
INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoCartera" 					AS ultPrt 
	on (EMP."IdEmpresa" 				= ultPrt."IdEmpresa" and 
		EMP."FolioCarteraRelacionada" 	= ultPrt."FolioCartera")
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" 													AS prt 
	on (prt."IdCartera" = ultPrt."IdCartera")
LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteSimple"				AS FAC
	ON (FAC."IdComprobante" = prt."FolioComprobante")
LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" 					AS sby 
	on (sby."IdSubyacente" = ultSub."IdSubyacente")
LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarAsignacion"										AS ASI
		ON (ASI."FolioTransaccion" = prt."FolioExternoReferencia")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora"								AS CO
	ON (CO."IdCorredora"	= ASI."IdCorredorContraparte")
WHERE 	EMP."IdEmpresa" 		= :IdEmpresa
	AND	EMP."IdTipoInstrumento" = 'EMP'
	AND prt."FolioExternoReferencia" <> ''
	AND (:FechaLimite BETWEEN EMP."INGFechaOperacion" and case when IFNULL(EMP."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(EMP."EGRFechaOperacion",'0001-01-01') end
		AND	:FechaLimite < case when IFNULL(EMP."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(EMP."EGRFechaOperacion",'0001-01-01') END)				
	AND	  sby."IdClasificacionTipoInstrumento" = 'RV'
	AND	  sby."IdTipoInstrumento" = CASE WHEN COALESCE(:IdTipoInstrumento,'') <> '' THEN :IdTipoInstrumento ELSE sby."IdTipoInstrumento" END
	AND	  sby."IdInstrumento" = CASE WHEN COALESCE(:IdInstrumento,0) > 0 THEN :IdInstrumento ELSE sby."IdInstrumento" END
	AND   EMP."IdTipoInstrumento" = 'EMP'
	AND   FAC."IndicadorOrdenDirecta" <> 'OD'

GROUP BY
	sby."IdInstrumento"
	,IFNULL(CO."DocumentoIdentidad",'')							
	,IFNULL(UPPER(CO."NombreCompleto"),'NO EXISTE CONTRAPARTE');