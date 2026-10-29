set schema bscl;
DROP table #seriefondos;
DROP TABLE #Instrumentos;
DROP TABLE #ValoresRiesgo;
create local temporary table #seriefondos(
											"IdInterno" 						 BIGINT,
				    					  		"IdSerieFondo"						 NVARCHAR(40),
				    					  		"IdInstrumento"						 BIGINT,
				    					  		"IdFondo"							 NVARCHAR(40),
				    					  		"NombreSerieFondo"					 NVARCHAR(100),
				    					  		"CodigoSerie"						 NVARCHAR(10),
				    					  		"Alias"								 NVARCHAR(40),
				    					  		"DecimalesValorCuota"		  		 BIGINT		,
		 										"DecimalesCuota"			  		 BIGINT		,
				    					  		"MontoMinimo"						 DECIMAL(28,6),
				    					  		"MontoMaximo"						 DECIMAL(28,6),
				    					  		"DiasRetencionesRescate"			 BIGINT,
				    					  		"ExcluyeGarantia"					 NVARCHAR(1),
				    					  		"CobraComision"			  			 NVARCHAR(1),
				    					  		"Diferido"					  		 NVARCHAR(1),
				    					  		"InstrumentoDCV"			  		 NVARCHAR(40),
				    					  		"Omnibus"					  		 NVARCHAR(1),
				    					  		"PatrimonioMinimo"					 DECIMAL(28,6),
				    					  		"IdSerieFondoOrigen"				 NVARCHAR(40),
				    					  		"IdEstado"							 NVARCHAR(3),
				    					  		"IdCliente"							 BIGINT,
				    					  		"IdPortafolio"						 BIGINT,
				    					  		"IdMedioDePagoAbono"				 NVARCHAR(3),
				    					  		"IdMedioDePagoCargo"				 NVARCHAR(3),
				    					  		"DiasValutaCertificacionAporte" 	 BIGINT,
				    					  		"DiasValutaCertificacionRescate" 	 BIGINT,
				    					  		"DiasValutaCurseAporte"				 BIGINT,
	 											"DiasValutaCurseRescate"			 BIGINT,
	 											"IdFamiliaFondo"			  		 NVARCHAR(40)
);

create local temporary table #Instrumentos(
												"IdInterno"			BIGINT,
		"Nemotecnico"		NVARCHAR(40),
		"ISIN"				NVARCHAR(40),
		"CorteMinimo"		DECIMAL(26,8),
		"EstaInscrito"		NVARCHAR(1),
		"RazonSocial"		NVARCHAR(100),
		"FechaEmision"		NVARCHAR(10),
		"FechaVencimiento"	NVARCHAR(10),
		"IdEmisor"			NVARCHAR(40),
		"IdTipoReajuste"	NVARCHAR(3),
		"IdMoneda"			NVARCHAR(3),
		"IdTipoInstrumento"	NVARCHAR(3),
		"IdEstado"			NVARCHAR(3),
		"Tamano"			BIGINT,
		"IdEntidadClasificadoraRiesgo"	NVARCHAR(10),
		"ValorClasificacionRiesgo"		NVARCHAR(20),
		"PrecioBase"						DECIMAL(26,8),
		"PrecioReferencia"				DECIMAL(26,8),
		"FechaReferencia"				NVARCHAR(10)
);

create local temporary table #ValoresRiesgo(
	"IdInterno"						BIGINT,
		"IdEntidadClasificadoraRiesgo"		NVARCHAR(10),
		"ValorClasificacionRiesgo"			NVARCHAR(20)
);

DELETE FROM  #seriefondos;
INSERT INTO #seriefondos(
"IdInterno","IdSerieFondo","IdFondo","NombreSerieFondo","CodigoSerie","Alias","DecimalesValorCuota","DecimalesCuota","MontoMinimo","MontoMaximo","DiasRetencionesRescate",
"ExcluyeGarantia","CobraComision","Diferido","InstrumentoDCV","Omnibus","PatrimonioMinimo","IdSerieFondoOrigen","IdEstado","IdCliente","IdPortafolio","IdMedioDePagoAbono",
"IdMedioDePagoCargo","DiasValutaCertificacionAporte","DiasValutaCertificacionRescate","DiasValutaCurseAporte","DiasValutaCurseRescate","IdFamiliaFondo")
VALUES(0,'PRUEBA_245','PRUEBA_11','NombreSerieFondo24','A','PRUEBA_24',0,0,0,0,0,'N','N','N','DCV','N',0,'','ACT',2323,2323,'','',0,0,0,0,'');


INSERT INTO #Instrumentos
SELECT "Serie"."IdInterno"
								,"Serie"."IdSerieFondo" AS "Nemotecnico"
								,''   AS "ISIN"
								,0    AS "CorteMinimo"
								,'N'  AS "EstaInscrito"
								,"Serie"."NombreSerieFondo" AS "RazonSocial"
								,'0001-01-01' AS "FechaEmision"
								,'0001-01-01' AS "FechaVencimiento"
								,"Fondo"."IdEmisor"
								,'' AS "IdTipoReajuste"
								,"Fondo"."IdMoneda" AS "IdMoneda"
								,"Fondo"."IdTipoInstrumento"
								,"Serie"."IdEstado"
								,0 AS "Tamano"
								,'' AS "IdEntidadClasificadoraRiesgo"
								,'' AS "ValorClasificacionRiesgo"
								,0  AS "PrecioBase"
								,0  AS "PrecioReferencia"
								,'0001-01-01' AS "FechaReferencia"
						  FROM #seriefondos AS "Serie"
						  INNER JOIN  "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("Fondo"."IdFondo" = "Serie"."IdFondo");

CALL "BSCL.Plataforma.BD.Servicios::INS.CrearInstrumentos"(
			p_IdEmpresa => 1,
			p_IdUsuario => 21,
			p_IdModulo => 'FMM',
			p_simularOperacion => 'N',
			p_UsaReglasNegocio => 'N',
			p_Instrumentos => #Instrumentos,
			p_ValoresRiesgo => #ValoresRiesgo,
			p_out_Resultado => ?,
			p_out_AdvertenciasContador => ?,
			p_out_AdvertenciasDetalle => ?,
			p_out_ErroresContador => ?,
			p_out_ErroresDetalle => ?
		);


UPDATE SERIES
	SET "IdInstrumento" = INST."IdInstrumento"
FROM #seriefondos SERIES
INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS INST ON (INST."Nemotecnico" = SERIES."IdSerieFondo");

select * from #seriefondos;



call "BSCL.FondoMutuo.BD.Servicios::CrearSerieDeFondo" (
	p_IdEmpresa	 => 1,
	p_SerieFondo => #seriefondos,
	p_out_Resultado => ?
);


SELECT * FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo";
