INSERT INTO "BSCL.Plataforma.BD::CUA.SaldosCuadraturaRF" 
			("FechaInicio","FechaFin","DocumentoIdentidad","IdTipoDocumentoIdentidad","IdPortafolio","CodigoPortafolio","IdClasificacionTipoInstrumento","IdCliente","IdInstrumento","Nemotecnico","SaldoInicial") 
			SELECT 	
			    :p_FechaInicio,
			    :p_FechaFin,
			    Ident."DocumentoIdentidad",
			    Ident."IdTipoDocumentoIdentidad" ,
			    Port."IdPortafolio",
			 	Port."CodigoPortafolio",
			 	car."IdClasificacionTipoInstrumento",
				car."IdCliente",
				car."IdInstrumento",
			    (CASE WHEN car."IdClasificacionTipoInstrumento" = 'IF' THEN car."NemoSVS" 
					ELSE car."Nemotecnico" END) AS "Nemotecnico",
				SUM(CASE WHEN car."IdClasificacionTipoInstrumento"='IF' THEN car."INGValorRescate" 
			        ELSE car."INGCantidad" END) AS INGCantidad
			FROM "BSCL.Custodia.BD::HIS.Cartera" AS car
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" as Port ON Port."IdPortafolio" = car."IdPortafolio" 
			LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cli ON Cli."IdCliente" = Port."IdCliente"
			LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Per ON Per."IdPersona" = Cli."IdPersona"
			LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as Ident ON Per."IdDocumentoIdentidad" = Ident."IdDocumentoIdentidad"
			WHERE car."IdClasificacionTipoInstrumento" in ('RF','IF') AND car."IdEstado"  NOT IN ('ANU','RPA') AND car."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC', 'FWD','RRV','RRF') 
			AND car."FechaRespaldo"=:p_FechaInicio
			AND (car."FechaOperacion" <= :p_FechaInicio AND car."EGRCantidad" = 0 AND car."IdEstado" = 'VIG') 
			GROUP BY Ident."DocumentoIdentidad",Ident."IdTipoDocumentoIdentidad",Port."IdPortafolio",Port."CodigoPortafolio",car."IdClasificacionTipoInstrumento",car."IdCliente",car."IdPortafolio",car."IdInstrumento",car."Nemotecnico",car."NemoSVS"