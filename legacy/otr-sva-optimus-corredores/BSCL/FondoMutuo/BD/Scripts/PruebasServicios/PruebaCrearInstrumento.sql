set schema bscl;
DROP table #instrumentos;
create local temporary table #instrumentos(
"IdInterno"	BIGINT,
									"Nemotecnico" NVARCHAR(40),
									"ISIN" NVARCHAR(40),
									"CorteMinimo" DECIMAL,
									"EstaInscrito" NVARCHAR(1),
									"EsDivisible" NVARCHAR(1),
									"RazonSocial" NVARCHAR(255),
									"Lista" NVARCHAR(255),
									"FechaEmision" DATE,
									"FechaVencimiento" DATE,
									"IdInstrumentoRelacionado" BIGINT,
									"IdEmisor" NVARCHAR(40),
									"IdTipoReajuste" NVARCHAR(3),
									"Reajustable" NVARCHAR(1),
									"IdMoneda" NVARCHAR(3),
									"IdTipoInstrumento" NVARCHAR(3),
									"IdEstado" NVARCHAR(3),
									"DecimalesPrecio" INTEGER,
									"IdClasificacionRiesgo" BIGINT,
									"Tamano" BIGINT,
									"DiasAlVencimiento" INTEGER,
									"FechaFijaCierre" DATE,
									"FechaInicioVigencia" DATE,
									"HoraFijaCierre" TIME,
									"TasaFiscal" DECIMAL(26,8),
									"IndArt104" NVARCHAR(1),
									"IdEntidadClasificadoraRiesgo" NVARCHAR(10),
									"ValorClasificacionRiesgo" NVARCHAR(20)
);

INSERT INTO #instrumentos
SELECT 0
								,"Serie"."IdSerieFondo" AS "Nemotecnico"
								,''   AS "ISIN"
								,0    AS "CorteMinimo"
								,'N'  AS "EstaInscrito"
								,'N'  AS "EsDivisible"
								,"Serie"."NombreSerieFondo" AS "RazonSocial"
								,'' AS "Lista"
								,'0001-01-01' AS "FechaEmision"
								,'0001-01-01' AS "FechaVencimiento"
								,0 AS "IdInstrumentoRelacionado"
								,"Fondo"."IdEmisor"
								,'' AS "IdTipoReajuste"
								,'' AS "Reajustable"
								,"Fondo"."IdMoneda" AS "IdMoneda"
								,"Fondo"."IdTipoInstrumento"
								,"Serie"."IdEstado"
								,"Serie"."DecimalesValorCuota" AS "DecimalesPrecio"
								,0 AS "IdClasificacionRiesgo"
								,0 AS "Tamano"
								,0 AS "DiasAlVencimiento"
								,'0001-01-01'  AS "FechaFijaCierre"
								,'0001-01-01'  AS "FechaInicioVigencia"
								,'00:00:00'	   AS "HoraFijaCierre"
								,0 AS "TasaFiscal"
								,'' AS "IndArt104"
								,'' AS "IdEntidadClasificadoraRiesgo"
								,'' AS "ValorClasificacionRiesgo"
						  FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "Serie"
						  INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("Fondo"."IdFondo" = "Serie"."IdFondo")
						  WHERE "Serie"."IdSerieFondo" = 'FMLIQUIDEZ_D';
						  
SELECT * from #instrumentos;

/*call "BSCL.Mercado.BD.Servicios::CrearInstrumentos" (
	p_IdEmpresa	 => 1,
	p_IdUsuario	 => 21,
	p_IdModulo	 => 'FMM',
	p_Instrumentos => #instrumentos,
	p_out_Instrumentos => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);

UPDATE
"BSCL.FondoMutuo.BD::FMM.SerieDeFondo"
SET "IdInstrumento" = 346398
WHERE "IdSerieFondo" = 'FMLIQUIDEZ_D';


SELECT * FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo";*/

