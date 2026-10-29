SELECT SIM."suby_IdInstrumento"										AS "IdInstrumento",
	SocNeg."IdSocioDeNegocio"										AS "IdSocioNegocio",
	'CNT'															AS "TipoSocioNegocio",
	COALESCE(CC."IdPortafolioDefecto",0)							AS "IdPortafolio",
	'R'																AS "IdTipoDocumentoIdentidad",			
	IFNULL(CO."DocumentoIdentidad",'')								AS "DocumentoIdentidad",				
	IFNULL(UPPER(CO."NombreCompleto"),'NO EXISTE CONTRAPARTE')		AS "NombreCliente",						
	CASE IFNULL(CO."DocumentoIdentidad",'')
		WHEN '' THEN 'PEN'
		ELSE 'PRO'
	END																AS "IdEstado"
	,SIM."IdCorredoraContraparte" AS "IdCorredorContraparte"
FROM  "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea" 						AS SIM
LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteSimple"				AS FAC
	ON (FAC."IdComprobante" = SIM."FolioComprobante")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora"								AS CO
	ON (CO."IdCorredora"	= SIM."IdCorredoraContraparte")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas.UI::ConsultarSocioDeNegocio"  AS SocNeg
	ON CO."IdPersona" = SocNeg."IdPersona"
	
LEFT JOIN  "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"			AS CC
	ON SocNeg."IdCliente" = CC."IdCliente"
   AND CC."IdEstado" = 'ACT'
   
WHERE SIM."IdEmpresa" 			= :IdEmpresa
	AND	SIM."IdTipoOperacion" 	= 'COMSIM'  
	AND (:FechaLimite BETWEEN SIM."INGFechaOperacion" and case when IFNULL(SIM."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(SIM."EGRFechaOperacion",'0001-01-01') end
		 AND :FechaLimite < case when IFNULL(SIM."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(SIM."EGRFechaOperacion",'0001-01-01') end)
	AND SIM."IdEstado" <> 'ANU' 
	AND SIM."suby_IdClasificacionTipoInstrumento" = 'RV'
	AND SIM."suby_IdTipoInstrumento" = CASE WHEN COALESCE(:IdTipoInstrumento,'') <> '' THEN :IdTipoInstrumento ELSE SIM."suby_IdTipoInstrumento" END
	AND SIM."suby_IdInstrumento" = CASE WHEN COALESCE(:IdInstrumento,0) > 0 THEN :IdInstrumento ELSE SIM."suby_IdInstrumento" END
GROUP BY
	SocNeg."IdSocioDeNegocio",
	CC."IdPortafolioDefecto",
	SIM."suby_IdInstrumento",
	IFNULL(CO."DocumentoIdentidad",''),
	IFNULL(UPPER(CO."NombreCompleto"),'NO EXISTE CONTRAPARTE'),
	SIM."IdCorredoraContraparte";
