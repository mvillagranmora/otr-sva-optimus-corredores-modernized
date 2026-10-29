SET SCHEMA "BSCL"; 
 
DROP VIEW  "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteClienteBase"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteClienteBase" 
AS
SELECT
    IFNULL(BASE."IdCliente",DEFECTO."IdCliente") 											as "IdCliente",
    IFNULL(BASE."IdPortafolio",DEFECTO."IdPortafolio") 										as "IdPortafolio",
    IFNULL(BASE."PorDefecto",DEFECTO."PorDefecto") 											as "PorDefecto",		    
	IFNULL(BASE."IdPlantillaLimite",DEFECTO."IdPlantillaLimite") 							as "IdPlantillaLimite",
	IFNULL(BASE."IdLimite",DEFECTO."IdLimite") 												as "IdLimite",
	IFNULL(BASE."IdTipoLimite",(CASE WHEN DEFECTO."IdTipoLimite"='' THEN 'LIMPORTPRODUCTO' ELSE DEFECTO."IdTipoLimite"END)) as "IdTipoLimite",
	IFNULL(BASE."IdTipoInstrumento",DEFECTO."IdTipoInstrumento") 							as "IdTipoInstrumento",
	IFNULL(BASE."IdClasificacionTipoInstrumento",DEFECTO."IdClasificacionTipoInstrumento") 	as "IdClasificacionTipoInstrumento",			
	IFNULL(BASE."IdTipoProducto",DEFECTO."IdTipoProducto") 									as "IdTipoProducto",
	IFNULL(BASE."IdTipoOperacion",DEFECTO."IdTipoOperacion") 								as "IdTipoOperacion",
	IFNULL(BASE."Nombre",DEFECTO."Nombre") 													as "Nombre",
	IFNULL(BASE."IdEstado",DEFECTO."IdEstado") 												as "IdEstado",
	IFNULL(BASE."IdInstrumento",DEFECTO."IdInstrumento") 									as "IdInstrumento",
	IFNULL(BASE."UnidadPlazo",DEFECTO."UnidadPlazo") 										as "UnidadPlazo",
	IFNULL(BASE."Plazo",DEFECTO."Plazo") 													as "Plazo",
	IFNULL(BASE."IdTipoReajuste",DEFECTO."IdTipoReajuste") 									as "IdTipoReajuste",									
	IFNULL(BASE."CodigoOperador",DEFECTO."CodigoOperador") 									as "CodigoOperador",
	IFNULL(BASE."MontoTotal",DEFECTO."MontoTotal") 											as "MontoTotal",
	IFNULL(BASE."MontoIntraday",DEFECTO."MontoIntraday") 									as "MontoIntraday",
	IFNULL(BASE."MontoOvernight",DEFECTO."MontoOvernight") 									as "MontoOvernight",
	IFNULL(BASE."IdMoneda",DEFECTO."IdMoneda") 												as "IdMoneda",
	IFNULL(BASE."IdMonedaCalculo",DEFECTO."IdMonedaCalculo") 								as "IdMonedaCalculo",
	IFNULL(BASE."ClasificacionRiesgoMinima",DEFECTO."ClasificacionRiesgoMinima") 			as "ClasificacionRiesgoMinima",
	IFNULL(BASE."EsPersonalizada",DEFECTO."EsPersonalizada") 								as "EsPersonalizada",
	IFNULL(BASE."FechaVigencia",DEFECTO."FechaVigencia") 									as "FechaVigencia",
	IFNULL(BASE."EsTotalizador",DEFECTO."EsTotalizador") 									as "EsTotalizador",
	IFNULL(BASE."IdMesa",DEFECTO."IdMesa") 													as "IdMesa",
	IFNULL(BASE."IdCanalIngreso",DEFECTO."IdCanalIngreso") 									as "IdCanalIngreso",
	IFNULL(BASE."IdAgente",DEFECTO."IdAgente")												as "IdAgente",
	IFNULL(BASE."Porcentaje",DEFECTO."Porcentaje")											as "Porcentaje",
	IFNULL(BASE."IdGrupoEmisor",DEFECTO."IdGrupoEmisor")									as "IdGrupoEmisor",
	IFNULL(BASE."IdEmpresa",DEFECTO."IdEmpresa")											as "IdEmpresa"
FROM (
	(SELECT POR."IdCliente",POR."IdPortafolio",CABP."PorDefecto",PLIM."IdPlantillaLimite",PLIM."IdLimite",PLIM."IdTipoLimite",PLIM."IdTipoInstrumento",
			PLIM."IdClasificacionTipoInstrumento",PLIM."IdTipoProducto",PLIM."IdTipoOperacion",PLIM."Nombre",PLIM."IdEstado",PLIM."IdInstrumento",PLIM."UnidadPlazo",
			PLIM."Plazo",PLIM."IdTipoReajuste",PLIM."CodigoOperador",PLIM."MontoTotal",PLIM."MontoIntraday",PLIM."MontoOvernight",
			PLIM."IdMoneda",PLIM."IdMonedaCalculo",PLIM."ClasificacionRiesgoMinima",PLIM."EsPersonalizada",PLIM."FechaVigencia",
			PLIM."EsTotalizador",PLIM."IdMesa",PLIM."IdCanalIngreso",PLIM."IdAgente",
			PLIM."Porcentaje",PLIM."IdGrupoEmisor",CABP."IdEmpresa"
			FROM "BSCL.Plataforma.BD::LIM.Limite" as PLIM
			INNER JOIN "BSCL.Plataforma.BD::LIM.PlantillaLimite"  CABP
				ON (CABP."IdPlantillaLimite"=PLIM."IdPlantillaLimite")
			INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS POR
				ON (PLIM."IdPlantillaLimite"=POR."IdPerfilLimites")
			WHERE
				 PLIM."IdEstado" = 'ACT'
				 AND CABP."IdEstado" = 'ACT'
				 AND CABP."PorDefecto"='N'
	) as BASE 
FULL JOIN		---PLANTILLA POR DEFECTO (CLIENTE)
	(SELECT PLACP."IdCliente",PLACP."IdPortafolio",PLACP."PorDefecto",PLIM."IdPlantillaLimite",PLIM."IdLimite",PLIM."IdTipoLimite",PLIM."IdTipoInstrumento",
			PLIM."IdClasificacionTipoInstrumento",PLIM."IdTipoProducto",PLIM."IdTipoOperacion",PLIM."Nombre",PLIM."IdEstado",PLIM."IdInstrumento",PLIM."UnidadPlazo",
			PLIM."Plazo",PLIM."IdTipoReajuste",PLIM."CodigoOperador",PLIM."MontoTotal",PLIM."MontoIntraday",PLIM."MontoOvernight",
			PLIM."IdMoneda",PLIM."IdMonedaCalculo",PLIM."ClasificacionRiesgoMinima",PLIM."EsPersonalizada",PLIM."FechaVigencia",
			PLIM."EsTotalizador",PLIM."IdMesa",PLIM."IdCanalIngreso",PLIM."IdAgente",
			PLIM."Porcentaje",PLIM."IdGrupoEmisor",PLACP."IdEmpresa"
			FROM "BSCL.Plataforma.BD::LIM.Limite" as PLIM
			INNER JOIN (SELECT PORC."IdCliente",PORC."IdPortafolio",PLAC."IdPlantillaLimite",PLAC."PorDefecto",PLAC."IdEmpresa"
						FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS PORC
						INNER JOIN (SELECT POR."IdCliente",CABP."IdPlantillaLimite",CABP."PorDefecto",CABP."IdEmpresa"
									FROM "BSCL.Plataforma.BD::LIM.PlantillaLimite"  CABP
									INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS POR
										ON (CABP."IdPlantillaLimite"=POR."IdPerfilLimites")
									WHERE
										CABP."IdEstado" = 'ACT'
										AND CABP."PorDefecto"='S'
									GROUP BY POR."IdCliente",CABP."IdPlantillaLimite",CABP."PorDefecto",CABP."IdEmpresa") PLAC --plantilla cliente por defecto						 
							ON (PLAC."IdCliente" =PORC."IdCliente"))	PLACP				 --plantilla cliente/portafolio por defecto		
				ON (PLACP."IdPlantillaLimite"=PLIM."IdPlantillaLimite")
	) as DEFECTO	
	ON  --FULL JOIN
		(DEFECTO."IdClasificacionTipoInstrumento" 	= BASE."IdClasificacionTipoInstrumento")
		AND (DEFECTO."IdTipoInstrumento" 			= BASE."IdTipoInstrumento")
		AND (DEFECTO."IdTipoOperacion" 				= BASE."IdTipoOperacion")
		AND (DEFECTO."IdCanalIngreso" 				= BASE."IdCanalIngreso")
		AND (DEFECTO."IdPortafolio" 				= BASE."IdPortafolio")
		AND (DEFECTO."IdCliente" 					= BASE."IdCliente")
		)
WHERE 
	IFNULL(DEFECTO."IdEstado",BASE."IdEstado") = 'ACT';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteClienteBase" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
