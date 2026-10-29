UPDATE "BSCL.APV.BD::REC.Recaudacion"
SET 
		"FolioCartola"			= COALESCE(:FolioCartola        ,"FolioCartola"			)
		,"LineaCartola"			= COALESCE(:LineaCartola        ,"LineaCartola"			)
		,"Sucursal"				= COALESCE(:Sucursal            ,"Sucursal"				)
		,"FechaDeposito"		= COALESCE(:FechaDeposito       ,"FechaDeposito"		)
		,"CuentaCorriente"		= COALESCE(:CuentaCorriente     ,"CuentaCorriente"		)
		,"MontoDeposito"		= COALESCE(:MontoDeposito       ,"MontoDeposito"		)
		,"ConceptoRecaudacion"	= COALESCE(:ConceptoRecaudacion ,"ConceptoRecaudacion"	)
		,"Origen"				= COALESCE(:Origen              ,"Origen"				)
		,"Observacion"			= COALESCE(:Observacion         ,"Observacion"			)
		,"NumeroDocumento"		= COALESCE(:NumeroDocumento     ,"NumeroDocumento"		)
		,"IdTipoDeposito"		= COALESCE(:IdTipoDeposito      ,"IdTipoDeposito"		)
		,"IdBanco"				= COALESCE(:IdBanco             ,"IdBanco"				)
		,"IdRegimenTributario"	= COALESCE(:IdRegimenTributario ,"IdRegimenTributario"	)
		,"IdCarga"				= COALESCE(:IdCarga             ,"IdCarga"				)
		,"IdEstado"				= COALESCE(:IdEstado            ,"IdEstado"				)

WHERE	"IdRecaudacion" 		= :IdRecaudacion
AND 	"IdEmpresa"				= :IdEmpresa;
