SELECT	ROW_NUMBER() OVER (ORDER BY EvCapCli."IdPortafolio") 																							AS "IdGenerarMovimientoCustodia"
		,ConCliente."IdCliente"                         																								AS "IdCliente"
		,EvCapCli."IdPortafolio"	                  																									AS "IdPortafolio"
		,EvCapCli."IdBolsa"                           																									AS "IdBolsa"
		,EvCapCli."FechaPago"            																												AS "FechaPago"
		,EvCapCli."IdClasificacionTipoInstrumento"       																								AS "IdClasificacionTipoInstrumento"
		,EvCapCli."IdTipoInstrumento"                    																								AS "IdTipoInstrumento"
		,EvCapCli."IdInstrumento"                          																								AS "IdInstrumento"
		,EvCapCli."Nemo"		                          																								AS "Nemotecnico"
		,Instrumento."IdEmisor"                        																									AS "IdEmisor"
		,Instrumento."IdTipoReajuste"                  																									AS "IdTipoReajuste"
		,Instrumento."FechaEmision"  	            																									AS "FechaEmision"
		,Instrumento."IdMoneda"    		              																									AS "IdMoneda"
		,EvCapCli."IdBoveda"                           																									AS "IdBoveda"
		,TO_DECIMAL(EvCapCli."CantidadNueva",26,8)     																									AS "Cantidad"
		,EvCapCli."Total"       		               																									AS "ValorRescate"
		,EvCapCli."PrecioNuevo"			                																								AS "PrecioCompra"
		,'Canjes N°:' ||Cast(:IdEventoCapital as nvarchar(8))|| ' ' || Trim(EvCapCli."Nemo")  || ' Fecha: ' ||CAST(EvCapCli."FechaPago" AS DATE)		AS "Glosa"
		,TO_BIGINT(0) 																																	AS "FolioCustodiaBusqueda"
FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EvCapCli
	INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS ConCliente
		ON EvCapCli."IdPortafolio" = ConCliente."IdPortafolio"
	INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"  AS Instrumento
		ON EvCapCli."IdInstrumento" = Instrumento."IdInstrumento"
WHERE	EvCapCli."IdEventoCapital" 	= :IdEventoCapital		AND
		EvCapCli."IdEstado"			= 'PRO' 