set schema bscl;
DROP table #seriefondos;
DROP table #fondos;
DROP TABLE #RegimenSerieFondo;
create local temporary table #seriefondos(
											"IdInterno" 						 BIGINT,
				    					  		"IdSerieFondo"						 NVARCHAR(40),
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

create local temporary table #fondos(
												"IdInterno" 				  BIGINT,
		    									"IdFondo"					  NVARCHAR(40),
		    									"IdEmisor"					  NVARCHAR(40),
		    									"Nemotecnico"			  	  NVARCHAR(40),
		    									"NombreFondo"				  NVARCHAR(100),
		    									"IdTipoFondo"				  NVARCHAR(40),
		    									"IdPaisOrigen"				  BIGINT,
		    									"CodigoSVS"				      NVARCHAR(40),
		    									"MoneyMarket"   			  NVARCHAR(1),
		    									"Vigencia"					  NVARCHAR(1),
		    									"FondoExtranjero"			  NVARCHAR(1),
		    									"IdMoneda"					  NVARCHAR(3),
		    									"IdTipoInstrumento"			  NVARCHAR(3),
		    									"IdCategoriaPerfilFondoMutuo" NVARCHAR(40),
		    									"AperturaAutomaticaFondo"	  NVARCHAR(1),
		    									"FondoNormativo"			  NVARCHAR(1) -- SOLO DESARROLLO
);

create local temporary table #RegimenSerieFondo(
												"IdInterno"	BIGINT,
												"IdRegimenFondoMutuo" 	NVARCHAR(40),
												"IdSerieFondo"			NVARCHAR(40)
);


DELETE FROM  #seriefondos;
INSERT INTO #seriefondos(
"IdInterno","IdSerieFondo","IdFondo","NombreSerieFondo","CodigoSerie","Alias","DecimalesValorCuota","DecimalesCuota","MontoMinimo","MontoMaximo","DiasRetencionesRescate",
"ExcluyeGarantia","CobraComision","Diferido","InstrumentoDCV","Omnibus","PatrimonioMinimo","IdSerieFondoOrigen","IdEstado","IdCliente","IdPortafolio","IdMedioDePagoAbono",
"IdMedioDePagoCargo","DiasValutaCertificacionAporte","DiasValutaCertificacionRescate","DiasValutaCurseAporte","DiasValutaCurseRescate","IdFamiliaFondo")
VALUES(0,'PRUEBA_24','PRUEBA_1','NombreSerieFondo24','A','PRUEBA_24',0,0,0,0,0,'N','N','N','DCV','N',0,'','ACT',2323,2323,'','',0,0,0,0,'');



DELETE FROM  #fondos;
INSERT INTO #fondos 
("IdInterno","IdFondo","IdEmisor","Nemotecnico","NombreFondo","IdTipoFondo","IdPaisOrigen","CodigoSVS","MoneyMarket","Vigencia","FondoExtranjero","IdMoneda","IdTipoInstrumento",
"IdCategoriaPerfilFondoMutuo","AperturaAutomaticaFondo","FondoNormativo" -- SOLO DESARROLLO
)
VALUES(0,'PRUEBA_1','CREDICORP','PRUEBA','NombreFondoA',0,0,'CODSVS','N','N','N','CLP','FFM', '', 'S','N');


INSERT INTO #RegimenSerieFondo
("IdInterno","IdRegimenFondoMutuo","IdSerieFondo")
VALUES(0,'54BIS','PRUEBA_24');


call "BSCL.FondoMutuo.BD.Servicios::CrearInstrumentoFondoMutuo" (
	p_IdEmpresa	 => 1,
	p_IdUsuario	 => 21,
	p_IdModulo	 => 'FMM',
	p_UsaReglasNegocio => 'S',
	p_Fondo => #fondos,
	p_SerieFondo => #seriefondos,
	p_RegimenSerieFondo => #RegimenSerieFondo,
	p_out_Resultado => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);


SELECT * FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo";
SELECT * FROM "BSCL.FondoMutuo.BD::FMM.Fondo";
