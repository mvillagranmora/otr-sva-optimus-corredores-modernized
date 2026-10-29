SET SCHEMA "BSCL"; 
 
DROP VIEW  "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimitePortafolio"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimitePortafolio" 
AS
SELECT
			IFNULL(EXTRA."IdCliente",BASE."IdCliente") 										as "IdCliente",
		    IFNULL(EXTRA."IdPortafolio",BASE."IdPortafolio") 								as "IdPortafolio",
   		    IFNULL(EXTRA."PorDefecto",BASE."PorDefecto") 									as "PorDefecto",		    
			IFNULL(EXTRA."IdPlantillaLimite",BASE."IdPlantillaLimite") 						as "IdPlantillaLimite",
			IFNULL(EXTRA."IdLimite",BASE."IdLimite") 										as "IdLimite",
			IFNULL(EXTRA."IdTipoLimite",(CASE WHEN BASE."IdTipoLimite"='' THEN 'LIMPORTPRODUCTO' ELSE BASE."IdTipoLimite"END)) as "IdTipoLimite",
			IFNULL(EXTRA."IdTipoInstrumento",BASE."IdTipoInstrumento") 						as "IdTipoInstrumento",
			IFNULL(EXTRA."IdClasificacionTipoInstrumento",BASE."IdClasificacionTipoInstrumento") as "IdClasificacionTipoInstrumento",
			IFNULL(EXTRA."IdTipoProducto",BASE."IdTipoProducto") 							as "IdTipoProducto",
			IFNULL(EXTRA."IdTipoOperacion",BASE."IdTipoOperacion") 							as "IdTipoOperacion",
			IFNULL(EXTRA."Nombre",BASE."Nombre") 											as "Nombre",
			IFNULL(EXTRA."IdEstado",BASE."IdEstado") 										as "IdEstado",
			IFNULL(EXTRA."IdInstrumento",BASE."IdInstrumento") 								as "IdInstrumento",
			IFNULL(EXTRA."UnidadPlazo",BASE."UnidadPlazo") 									as "UnidadPlazo",
			IFNULL(EXTRA."Plazo",BASE."Plazo") 												as "Plazo",
			IFNULL(EXTRA."IdTipoReajuste",BASE."IdTipoReajuste") 							as "IdTipoReajuste",									
			IFNULL(EXTRA."CodigoOperador",BASE."CodigoOperador") 							as "CodigoOperador",
			IFNULL(EXTRA."MontoTotal",BASE."MontoTotal") 									as "MontoTotal",
			IFNULL(EXTRA."MontoIntraday",BASE."MontoIntraday") 								as "MontoIntraday",
			IFNULL(EXTRA."MontoOvernight",BASE."MontoOvernight") 							as "MontoOvernight",
			IFNULL(EXTRA."IdMoneda",BASE."IdMoneda") 										as "IdMoneda",
			IFNULL(EXTRA."IdMonedaCalculo",BASE."IdMonedaCalculo") 							as "IdMonedaCalculo",
			IFNULL(EXTRA."ClasificacionRiesgoMinima",BASE."ClasificacionRiesgoMinima") 		as "ClasificacionRiesgoMinima",
			IFNULL(EXTRA."EsPersonalizada",BASE."EsPersonalizada") 							as "EsPersonalizada",
			IFNULL(EXTRA."FechaVigencia",BASE."FechaVigencia") 								as "FechaVigencia",
			IFNULL(EXTRA."EsTotalizador",BASE."EsTotalizador") 								as "EsTotalizador",
			IFNULL(EXTRA."IdMesa",BASE."IdMesa") 											as "IdMesa",
			IFNULL(EXTRA."IdCanalIngreso",BASE."IdCanalIngreso") 							as "IdCanalIngreso",
			IFNULL(EXTRA."IdAgente",BASE."IdAgente")										as "IdAgente",
			IFNULL(EXTRA."Porcentaje",BASE."Porcentaje")									as "Porcentaje",
			IFNULL(EXTRA."IdGrupoEmisor",BASE."IdGrupoEmisor")								as "IdGrupoEmisor",
			IFNULL(EXTRA."IdEmpresa",BASE."IdEmpresa")										as "IdEmpresa",
			IFNULL(EXTRA."DocumentoIdentidad",BASE."DocumentoIdentidad")					as "DocumentoIdentidad",
			IFNULL(EXTRA."NombreCompleto",BASE."NombreCompleto")							as "NombreCompleto"
		FROM 
				(
					(SELECT  PLIM."IdCliente",PLIM."IdPortafolio",PLIM."PorDefecto",PLIM."IdPlantillaLimite",PLIM."IdLimite",PLIM."IdTipoLimite",PLIM."IdTipoInstrumento",
							 PLIM."IdTipoProducto",PLIM."IdTipoOperacion",PLIM."Nombre",PLIM."IdEstado",PLIM."IdInstrumento",PLIM."UnidadPlazo",
							 PLIM."Plazo",PLIM."IdTipoReajuste",PLIM."CodigoOperador",PLIM."MontoTotal",PLIM."MontoIntraday",PLIM."MontoOvernight",
							 PLIM."IdMoneda",PLIM."IdMonedaCalculo",PLIM."ClasificacionRiesgoMinima",PLIM."EsPersonalizada",PLIM."FechaVigencia",
							 PLIM."EsTotalizador",PLIM."IdMesa",PLIM."IdCanalIngreso",PLIM."IdAgente",
							 PLIM."IdClasificacionTipoInstrumento",PLIM."Porcentaje",PLIM."IdGrupoEmisor",
							 PLIM."IdEmpresa",PER."DocumentoIdentidad",PER."NombreCompleto"
								FROM "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteClienteBase"  PLIM
								INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente"  CLI ON PLIM."IdCliente" = CLI."IdCliente" 
								INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON CLI."IdPersona" = PER."IdPersona"
					) as BASE 
				FULL JOIN
					(SELECT  POR."IdCliente",POR."IdPortafolio",CABP."PorDefecto",PLIM."IdPlantillaLimite",PLIM."IdLimite",PLIM."IdTipoLimite",PLIM."IdTipoInstrumento",
							 PLIM."IdTipoProducto",PLIM."IdTipoOperacion",PLIM."Nombre",PLIM."IdEstado",PLIM."IdInstrumento",PLIM."UnidadPlazo",
							 PLIM."Plazo",PLIM."IdTipoReajuste",PLIM."CodigoOperador",PLIM."MontoTotal",PLIM."MontoIntraday",PLIM."MontoOvernight",
							 PLIM."IdMoneda",PLIM."IdMonedaCalculo",PLIM."ClasificacionRiesgoMinima",PLIM."EsPersonalizada",PLIM."FechaVigencia",
							 PLIM."EsTotalizador",PLIM."IdMesa",PLIM."IdCanalIngreso",PLIM."IdAgente",
							 PLIM."IdClasificacionTipoInstrumento",PLIM."Porcentaje",PLIM."IdGrupoEmisor",
							 CABP."IdEmpresa",PER."DocumentoIdentidad",PER."NombreCompleto"
								FROM "BSCL.Plataforma.BD::LIM.Limite" as PLIM
								INNER JOIN "BSCL.Plataforma.BD::LIM.PlantillaLimite"  CABP
									ON (CABP."IdPlantillaLimite"=PLIM."IdPlantillaLimite")					
								INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS POR
									ON ( PLIM."IdPlantillaLimite"=POR."IdPerfilLimitesExtra")
								INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON POR."IdCliente" = CLI."IdCliente" 
								INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON CLI."IdPersona" = PER."IdPersona"
								WHERE 
									 PLIM."IdEstado" = 'ACT'
									 AND CABP."IdEstado" = 'ACT'						 
					) as EXTRA	
					ON  --FULL JOIN
						(EXTRA."IdClasificacionTipoInstrumento" = BASE."IdClasificacionTipoInstrumento")
						AND (EXTRA."IdTipoInstrumento" 			= BASE."IdTipoInstrumento")
						AND (EXTRA."IdTipoOperacion" 			= BASE."IdTipoOperacion")
						AND (EXTRA."IdCanalIngreso" 			= BASE."IdCanalIngreso")
						AND (EXTRA."IdPortafolio" 				= BASE."IdPortafolio")
						)
		WHERE 
			IFNULL(EXTRA."IdEstado",BASE."IdEstado") = 'ACT';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimitePortafolio" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
