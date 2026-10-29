SET SCHEMA "BSCL";
DROP VIEW   "BSCL.Plataforma.BD.Vistas::ConsultarBolsaInstrumentoPreciosActual";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::ConsultarBolsaInstrumentoPreciosActual" AS 
SELECT
	cast(0 as BIGINT) "IdInstrumento",
	cast(' ' as NVARCHAR(40)) "Nemotecnico",
	cast(' ' as NVARCHAR(40)) "ISIN",
	cast(0 as DECIMAL(26,8)) "CorteMinimo",
	cast(' ' as NVARCHAR(1)) "EstaInscrito",
	cast(' ' as NVARCHAR(1)) "EsDivisible",
	cast(' ' as NVARCHAR(100)) "RazonSocial",
	cast(' ' as NVARCHAR(520)) "Lista",
	cast('0001-01-01' as DATE) "FechaEmision",
	cast('0001-01-01' as DATE) "FechaVencimiento",
	cast(0 as BIGINT) "IdInstrumentoRelacionado",
	cast(' ' as NVARCHAR(40)) "IdEmisor",
	cast(' ' as NVARCHAR(3)) "IdTipoReajuste",
	cast(' ' as NVARCHAR(1))  "Reajustable",
	cast(' ' as NVARCHAR(3)) "IdMoneda",
	cast(' ' as NVARCHAR(3)) "IdTipoInstrumento",
	cast(' ' as NVARCHAR(3)) "IdEstado",
 	cast(0 as INT) "DecimalesPrecio",
	cast(' ' as NVARCHAR(100)) "NombreTipoInstrumento",
	cast(' ' as NVARCHAR(3)) "IdClasificacionTipoInstrumento",
	cast(' ' as NVARCHAR(100)) "NombreClasificacionTipoInstrumento",
	cast(' ' as NVARCHAR(100)) "NombreCompletoEmisor",
	cast(' ' as NVARCHAR(3)) "IdBolsa",
--	cast(0 as BIGINT) "IdBolsaInstrumentoPrecios",
	cast(0 as DECIMAL(26,8)) "UltimoPrecioTransado",
	cast(0 as DECIMAL(26,8)) "VariacionDiaria",
	cast(0 as DECIMAL(26,8)) "VolumenTransado",
	cast(0 as DECIMAL(26,8)) "MontoTransado",
	cast(0 as DECIMAL(26,8)) "MejorPrecioCompra",
	cast(0 as DECIMAL(26,8)) "MejorPrecioVenta",
	cast(0 as BIGINT) "Tamano"
FROM dummy;
	
	
