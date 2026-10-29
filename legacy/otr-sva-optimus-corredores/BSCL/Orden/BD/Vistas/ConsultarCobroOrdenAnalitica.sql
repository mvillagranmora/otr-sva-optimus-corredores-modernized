SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarCobroOrdenAnalitica"; 

CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarCobroOrdenAnalitica" AS 
SELECT 
	CO."IdCobroOrden", 
	CASE WHEN IFNULL(CO."FechaIngresoCobro",'0001-01-01')= '0001-01-01' THEN '0001-01-01 00:00:00' 
	    ELSE  
	       CASE WHEN  CO."FechaIngresoCobro" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
	       ELSE LEFT(TO_CHAR(ADD_SECONDS (CO."FechaIngresoCobro", CONFIGURACION."Valor")),19)
	    END 
	END AS "FechaIngresoRegistro",
	CASE WHEN CO."IdOrden" = 0 THEN NULL ELSE CO."IdOrden" END AS "IdOrden",
	CASE WHEN CO."IdGrupoCobro" = 0 THEN NULL ELSE CO."IdGrupoCobro" END AS "IdGrupoCobro",
	CASE WHEN CO."IdCobro" = '' THEN NULL ELSE CAST(CO."IdCobro" AS NVARCHAR(40)) END AS "IdCobro",
	CAST(C."NombreCobro" AS NVARCHAR(100)) AS "NombreCobro",
	--CO."IdPerfilCobro",
	--PC."DescripcionPerfilCobros",
	CAST(CO."CodigoCobroClasificacion" AS NVARCHAR(40)) AS "CodigoCobroClasificacion",
	CO."ValorCobroPerfil",
	CO."ValorCobroAplicado",
	CO."MontoCobroCalculado",
	CO."MontoCobroCalculadoLocal",
	CO."MontoCobroCalculadoLiquidacion",
	CAST(CO."FormaCobro" AS NVARCHAR(100)) AS "FormaCobro",
	CO."EsComprobante",
	CASE WHEN IFNULL(CO."FechaIngresoCobro",'0001-01-01')= '0001-01-01' THEN '0001-01-01 00:00:00' 
	    ELSE  
	       CASE WHEN  CO."FechaIngresoCobro" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
	       ELSE LEFT(TO_CHAR(ADD_SECONDS (CO."FechaIngresoCobro", CONFIGURACION."Valor")),19)
	    END 
	END AS "FechaIngresoCobro",
	CO."IdCarga",
	CO."MontoMinimo",
	CO."IdMoneda"
	--CO."DiasPlazo"
	FROM "BSCL.Orden.BD::ORD.CobroOrden" CO
	INNER JOIN "BSCL.Plataforma.BD::COM.Cobro" C ON C."IdCobro" = CO."IdCobro"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS CONFIGURACION ON "CodigoParametro" = 'PLA.SegundosZonaHoraria';

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarCobroOrdenAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
