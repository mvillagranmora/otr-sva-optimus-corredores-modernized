SELECT 
	sby."IdInstrumento"												AS "IdInstrumento",
	ConsCli."IdSocioDeNegocio"										AS "IdSocioNegocio",					
	'CNT'															AS "TipoSocioNegocio",					
	CAST(0 AS BigInt)												AS "IdPortafolio",						
	'R'																AS "IdTipoDocumentoIdentidad",			
	IFNULL(ConsCli."DocumentoIdentidad",'')							AS "DocumentoIdentidad",				
	IFNULL(UPPER(ConsCli."NombreCompleto"),'NO EXISTE CONTRAPARTE')	AS "NombreCliente",						
	CASE IFNULL(ConsCli."DocumentoIdentidad",'')
		WHEN '' THEN 'PEN'
		ELSE 'PRO'
	END																AS "IdEstado"
FROM	"BSCL.Custodia.BD::CUS.Cartera" 											AS PRE
	LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteSimple"	AS FAC
		ON (FAC."IdComprobante" = PRE."FolioComprobante")
	INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" 	AS ultSub 
		on (PRE."FolioCartera" = ultSub."FolioCarteraRelacionada")
	INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" 		AS sby 
		on (sby."IdSubyacente" = ultSub."IdSubyacente") 
	LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarAsignacion"							AS ASI
		ON (ASI."FolioTransaccion" = PRE."FolioExternoReferencia") -- QUIZAS FALTE PORTAFOLIO
	INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" 										AS CartEmp
		ON PRE."FolioCartera" = CartEmp."FolioCarteraRelacionada"
	   AND CartEmp."IdTipoInstrumento" = 'EMP'
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 					AS ConsCli
		ON ConsCli."IdCliente" = CartEmp."IdCliente" 
WHERE	PRE."IdEmpresa" = :IdEmpresa
--	AND	EC."TipoCartera"		= 'PRT'	
	AND
	(	:FechaLimite BETWEEN PRE."INGFechaOperacion" and case when IFNULL(PRE."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PRE."EGRFechaOperacion",'0001-01-01') end
		AND	:FechaLimite < case when IFNULL(PRE."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PRE."EGRFechaOperacion",'0001-01-01') end
	)
	AND	  sby."IdClasificacionTipoInstrumento" = 'RV'
	AND	  sby."IdTipoInstrumento" = CASE WHEN IFNULL(:IdTipoInstrumento,'') <> '' THEN :IdTipoInstrumento ELSE sby."IdTipoInstrumento" END
	AND	  sby."IdInstrumento" = CASE WHEN IFNULL(:IdInstrumento,0) > 0 THEN :IdInstrumento ELSE sby."IdInstrumento" END
	AND   PRE."IdTipoInstrumento" = 'PRT'
	AND	  FAC."IndicadorOrdenDirecta" <> 'OD'
GROUP BY ConsCli."IdSocioDeNegocio", sby."IdInstrumento"
	,IFNULL(ConsCli."DocumentoIdentidad",'')							
	,IFNULL(UPPER(ConsCli."NombreCompleto"),'NO EXISTE CONTRAPARTE');