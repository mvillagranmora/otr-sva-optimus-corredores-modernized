SET SCHEMA "BSCL"; 
 GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarDivisionPais"; 
 GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarDivisionPais"  
AS
SELECT
	DIVISIONPAIS."IdDivisionPais" , 
	DIVISIONPAIS."NombreDivisionPais" ,
	DIVISIONPAIS."Codigo" as "CodigoDivisionPais", --ES EL UBIGEO
	coalesce(DIVISIONPAIS."IdCiudad",0) as "IdCiudad", 
	(  ( DIVISIONPAIS."NombreDivisionPais" || ' - ' )  || PAIS."NombrePais" )  AS "NombreDivisionPaisPais" , 
	case when DIVISIONPAIS."IdClasificacion1"=0 then DIVISIONPAIS."NombreDivisionPais" 
		else --WORKARROUND, SI ES PERU CIUDAD-DISTRITO-PROVINCIA, EL RESTO CIUDAD-DISTRITO
            CASE WHEN PAIS."CodigoPais" <> 'PE' THEN DIVISIONPAIS."NombreDivisionPais"||' - '||CLASIF1."NombreClasificacionDivisionPais"
                 ELSE DIVISIONPAIS."NombreDivisionPais"||' - '||CLASIF1."NombreClasificacionDivisionPais" ||' - '||CLASIF2."NombreClasificacionDivisionPais"
            END
		end as "NombreDivisionPaisClasificacion1",
	case when DIVISIONPAIS."IdClasificacion2"=0 then DIVISIONPAIS."NombreDivisionPais" 
		else DIVISIONPAIS."NombreDivisionPais"||' - '||CLASIF2."NombreClasificacionDivisionPais" 
		end as "NombreDivisionPaisClasificacion2",
	PAIS."IdPais" , 
	PAIS."NombrePais" , 
	PAIS."CodigoPais",
	COALESCE(CLASIF1."IdClasificacionDivisionPais",0) as "IdClasificacionDivisionPais1",
	COALESCE(CLASIF1."NombreClasificacionDivisionPais",'') as "NombreClasificacionDivisionPais1",
	COALESCE(CLASIF1."TipoClasificacionDivisionPais",'') as "TipoClasificacionDivisionPais1",
	CASE COALESCE(CLASIF1."IdClasificacionDivisionPais",0)
		WHEN 0 THEN ''
		ELSE CONCAT(CONCAT(COALESCE(CLASIF1."TipoClasificacionDivisionPais",'-'),': '),COALESCE(CLASIF1."NombreClasificacionDivisionPais",'-')) 
		END as "TextoClasificacionDivisionPais1",
	COALESCE(CLASIF1."Codigo",'') as "CodigoClasificacionDivisionPais1",
	COALESCE(CLASIF2."IdClasificacionDivisionPais",0) as "IdClasificacionDivisionPais2",
	COALESCE(CLASIF2."NombreClasificacionDivisionPais",'') as "NombreClasificacionDivisionPais2",
	COALESCE(CLASIF2."TipoClasificacionDivisionPais",'') as "TipoClasificacionDivisionPais2",
	CASE COALESCE(CLASIF2."IdClasificacionDivisionPais",0)
		WHEN 0 THEN ''
		ELSE CONCAT(CONCAT(COALESCE(CLASIF2."TipoClasificacionDivisionPais",'-'),': '),COALESCE(CLASIF2."NombreClasificacionDivisionPais",'-')) 
		END as "TextoClasificacionDivisionPais2",
	COALESCE(CLASIF2."Codigo",'') as "CodigoClasificacionDivisionPais2"
FROM  "BSCL.Plataforma.BD::REG.DivisionPais" AS DIVISIONPAIS
LEFT JOIN "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" AS CLASIF1
	ON (DIVISIONPAIS."IdClasificacion1" = CLASIF1."IdClasificacionDivisionPais")
LEFT JOIN "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" AS CLASIF2
	ON (DIVISIONPAIS."IdClasificacion2" = CLASIF2."IdClasificacionDivisionPais")
LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Pais" AS PAIS 
	ON (PAIS."IdPais" = DIVISIONPAIS."IdPais");
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::REG.ConsultarDivisionPais" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 